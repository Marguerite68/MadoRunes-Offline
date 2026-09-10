#!/usr/bin/env python3
"""Compress review images without writing to released Android assets."""

from __future__ import annotations

import argparse
import json
import os
import shutil
import sys
import tempfile
from dataclasses import asdict, dataclass
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:  # pragma: no cover - depends on the local environment
    raise SystemExit(
        "缺少 Pillow。请先运行：python3 -m pip install Pillow"
    ) from exc


SUPPORTED_SUFFIXES = {".png", ".jpg", ".jpeg", ".webp"}
DEFAULT_MAX_BYTES = 300 * 1024
DEFAULT_RATIO = 0.80
REPO_ROOT = Path(__file__).resolve().parent.parent
PROTECTED_OUTPUT_DIRS = (
    REPO_ROOT / "app/src/main/assets/item",
    REPO_ROOT / "app/src/main/assets/wikiImg",
    REPO_ROOT / ".review/approved",
    REPO_ROOT / ".review/rejected",
)


@dataclass(frozen=True)
class CompressionResult:
    source: str
    output: str
    source_format: str
    output_format: str
    source_width: int
    source_height: int
    output_width: int
    output_height: int
    source_has_alpha: bool
    output_has_alpha: bool
    source_bytes: int
    output_bytes: int
    byte_ratio: float
    target_bytes: int
    meets_ratio_target: bool
    meets_max_bytes: bool


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "压缩单张图片或目录中的图片；默认目标为原体积的 80% 以下，"
            "并尽量不超过 300 KB。"
        )
    )
    parser.add_argument("input", type=Path, help="输入图片或目录")
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        help=(
            "输出文件或目录。省略时，单文件写为 *.compressed.ext，"
            "目录写到同级 *_compressed 目录。"
        ),
    )
    parser.add_argument(
        "--in-place",
        action="store_true",
        help="原地替换输入文件；不能与 --output 同时使用",
    )
    parser.add_argument(
        "--recursive",
        action="store_true",
        help="输入为目录时递归处理子目录",
    )
    parser.add_argument(
        "--max-kb",
        type=float,
        default=300.0,
        help="输出体积上限目标，默认 300 KB（1 KB = 1024 bytes）",
    )
    parser.add_argument(
        "--ratio",
        type=float,
        default=DEFAULT_RATIO,
        help="相对原体积目标，默认 0.80",
    )
    parser.add_argument(
        "--min-width",
        type=int,
        default=160,
        help="为满足体积目标而缩小时允许的最小宽度，默认 160 px",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="允许覆盖已存在的非原地输出文件",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="以 JSON Lines 输出逐文件报告",
    )
    args = parser.parse_args()

    if args.in_place and args.output is not None:
        parser.error("--in-place 不能与 --output 同时使用")
    if args.max_kb <= 0:
        parser.error("--max-kb 必须大于 0")
    if not 0 < args.ratio <= 1:
        parser.error("--ratio 必须大于 0 且不超过 1")
    if args.min_width < 1:
        parser.error("--min-width 必须大于 0")
    return args


def is_relative_to(path: Path, parent: Path) -> bool:
    try:
        path.relative_to(parent)
        return True
    except ValueError:
        return False


def reject_protected_output(path: Path) -> None:
    resolved = path.resolve()
    for protected in PROTECTED_OUTPUT_DIRS:
        if is_relative_to(resolved, protected.resolve()):
            raise ValueError(f"拒绝写入受保护目录：{resolved}")


def image_has_alpha(image: Image.Image) -> bool:
    return image.mode in {"RGBA", "LA", "PA"} or "transparency" in image.info


def normalized_format(image: Image.Image, path: Path) -> str:
    if image.format:
        value = image.format.upper()
        return "JPEG" if value == "JPG" else value
    return {".jpg": "JPEG", ".jpeg": "JPEG", ".webp": "WEBP"}.get(
        path.suffix.lower(), "PNG"
    )


def scaled_image(source: Image.Image, scale: float, source_has_alpha: bool) -> Image.Image:
    converted = source.convert("RGBA" if source_has_alpha else "RGB")
    if scale >= 0.999:
        return converted
    width = max(1, round(source.width * scale))
    height = max(1, round(source.height * scale))
    resized = converted.resize((width, height), Image.Resampling.LANCZOS)
    converted.close()
    return resized


def save_png_candidate(
    image: Image.Image,
    output: Path,
    source_has_alpha: bool,
    colors: int | None,
) -> None:
    if colors is None:
        prepared = image.convert("RGBA" if source_has_alpha else "RGB")
    elif source_has_alpha:
        rgba = image.convert("RGBA")
        alpha = rgba.getchannel("A")
        rgb = rgba.convert("RGB")
        fully_transparent = alpha.point(lambda value: 255 if value == 0 else 0)
        rgb.paste((0, 0, 0), mask=fully_transparent)
        quantized = rgb.quantize(
            colors=colors,
            method=Image.Quantize.MEDIANCUT,
            dither=Image.Dither.NONE,
        )
        prepared = quantized.convert("RGB")
        prepared.putalpha(alpha)
        fully_transparent.close()
        rgb.close()
        alpha.close()
        quantized.close()
        rgba.close()
    else:
        prepared = image.convert("RGB").quantize(
            colors=colors,
            method=Image.Quantize.MEDIANCUT,
            dither=Image.Dither.NONE,
        )
    try:
        prepared.save(output, format="PNG", optimize=True, compress_level=9)
    finally:
        prepared.close()


def save_lossy_candidate(
    image: Image.Image,
    output: Path,
    output_format: str,
    source_has_alpha: bool,
    quality: int,
) -> None:
    prepared = image.convert(
        "RGBA" if output_format == "WEBP" and source_has_alpha else "RGB"
    )
    try:
        if output_format == "JPEG":
            prepared.save(
                output,
                format="JPEG",
                quality=quality,
                optimize=True,
                progressive=True,
                subsampling="4:2:0",
            )
        else:
            prepared.save(
                output,
                format="WEBP",
                quality=quality,
                method=6,
                exact=source_has_alpha,
            )
    finally:
        prepared.close()


def compression_scales(width: int, min_width: int) -> list[float]:
    scales = [1.0, 0.92, 0.84, 0.76, 0.68, 0.60, 0.52, 0.44]
    valid = [scale for scale in scales if round(width * scale) >= min_width]
    if valid:
        return valid
    return [max(min_width / width, 0.01)] if width > min_width else [1.0]


def compress_to_temporary(
    source_path: Path,
    temporary_dir: Path,
    target_bytes: int,
    min_width: int,
) -> Path:
    with Image.open(source_path) as source:
        source.load()
        output_format = normalized_format(source, source_path)
        source_has_alpha = image_has_alpha(source)
        candidates: list[Path] = []
        settings_phases: tuple[tuple[int | None, ...], ...]
        settings_phases = (
            ((None,), (256, 192, 128, 96, 64))
            if output_format == "PNG"
            else ((90, 85, 80, 75, 70, 65, 60),)
        )

        scales = compression_scales(source.width, min_width)
        for phase_index, settings in enumerate(settings_phases):
            for scale_index, scale in enumerate(scales):
                resized = scaled_image(source, scale, source_has_alpha)
                try:
                    for setting_index, setting in enumerate(settings):
                        candidate = temporary_dir / (
                            f"candidate-{phase_index:02d}-{scale_index:02d}-"
                            f"{setting_index:02d}{source_path.suffix.lower()}"
                        )
                        if output_format == "PNG":
                            save_png_candidate(
                                resized, candidate, source_has_alpha, setting
                            )
                        else:
                            save_lossy_candidate(
                                resized,
                                candidate,
                                output_format,
                                source_has_alpha,
                                int(setting),
                            )
                        candidates.append(candidate)
                        if candidate.stat().st_size <= target_bytes:
                            return candidate
                finally:
                    resized.close()

        return min(candidates, key=lambda path: path.stat().st_size)


def resolve_file_output(source: Path, args: argparse.Namespace) -> Path:
    if args.in_place:
        return source
    if args.output is not None:
        return args.output
    return source.with_name(f"{source.stem}.compressed{source.suffix}")


def list_directory_images(directory: Path, recursive: bool) -> list[Path]:
    iterator = directory.rglob("*") if recursive else directory.glob("*")
    return sorted(
        path
        for path in iterator
        if path.is_file() and path.suffix.lower() in SUPPORTED_SUFFIXES
    )


def atomic_install(candidate: Path, destination: Path, force: bool, in_place: bool) -> None:
    reject_protected_output(destination)
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists() and not (force or in_place):
        raise FileExistsError(
            f"输出文件已存在：{destination}；使用 --force 或更换输出路径"
        )

    file_descriptor, temporary_name = tempfile.mkstemp(
        prefix=f".{destination.name}.", suffix=".tmp", dir=destination.parent
    )
    os.close(file_descriptor)
    temporary_path = Path(temporary_name)
    try:
        shutil.copyfile(candidate, temporary_path)
        os.replace(temporary_path, destination)
    finally:
        temporary_path.unlink(missing_ok=True)


def compress_one(
    source: Path,
    destination: Path,
    max_bytes: int,
    ratio: float,
    min_width: int,
    force: bool,
    in_place: bool,
) -> CompressionResult:
    source = source.resolve()
    destination = destination.resolve()
    if source.suffix.lower() not in SUPPORTED_SUFFIXES:
        raise ValueError(f"不支持的图片格式：{source}")
    source_suffix = source.suffix.lower()
    destination_suffix = destination.suffix.lower()
    same_format = source_suffix == destination_suffix or {
        source_suffix,
        destination_suffix,
    } <= {".jpg", ".jpeg"}
    if not same_format:
        raise ValueError(
            "输出扩展名必须与输入格式一致；本工具只压缩，不转换图片格式："
            f"{source_suffix} -> {destination_suffix or '(无扩展名)'}"
        )

    source_bytes = source.stat().st_size
    target_bytes = min(max_bytes, max(1, int(source_bytes * ratio)))
    with Image.open(source) as before:
        before.load()
        source_format = normalized_format(before, source)
        source_width, source_height = before.size
        source_has_alpha = image_has_alpha(before)

    with tempfile.TemporaryDirectory(prefix="madorunes-image-compress-") as temp_name:
        candidate = compress_to_temporary(
            source, Path(temp_name), target_bytes, min_width
        )
        if candidate.stat().st_size >= source_bytes:
            candidate = source
        atomic_install(candidate, destination, force, in_place)

    output_bytes = destination.stat().st_size
    with Image.open(destination) as after:
        after.load()
        output_format = normalized_format(after, destination)
        output_width, output_height = after.size
        output_has_alpha = image_has_alpha(after)

    if source_has_alpha and not output_has_alpha:
        raise RuntimeError(f"压缩后丢失 Alpha 通道：{destination}")

    return CompressionResult(
        source=str(source),
        output=str(destination),
        source_format=source_format,
        output_format=output_format,
        source_width=source_width,
        source_height=source_height,
        output_width=output_width,
        output_height=output_height,
        source_has_alpha=source_has_alpha,
        output_has_alpha=output_has_alpha,
        source_bytes=source_bytes,
        output_bytes=output_bytes,
        byte_ratio=round(output_bytes / source_bytes, 4),
        target_bytes=target_bytes,
        meets_ratio_target=output_bytes <= int(source_bytes * ratio),
        meets_max_bytes=output_bytes <= max_bytes,
    )


def print_result(result: CompressionResult, as_json: bool) -> None:
    if as_json:
        print(json.dumps(asdict(result), ensure_ascii=False))
        return
    alpha = "保留" if result.source_has_alpha else "无"
    print(
        f"{result.source} -> {result.output} | "
        f"{result.source_format} {result.source_width}x{result.source_height} "
        f"{result.source_bytes} bytes -> "
        f"{result.output_format} {result.output_width}x{result.output_height} "
        f"{result.output_bytes} bytes | ratio={result.byte_ratio:.1%} | "
        f"alpha={alpha} | <=max={result.meets_max_bytes} | "
        f"<=ratio={result.meets_ratio_target}"
    )


def main() -> int:
    args = parse_args()
    input_path = args.input.resolve()
    if not input_path.exists():
        print(f"输入不存在：{input_path}", file=sys.stderr)
        return 2

    max_bytes = round(args.max_kb * 1024)
    jobs: list[tuple[Path, Path]] = []
    if input_path.is_file():
        jobs.append((input_path, resolve_file_output(input_path, args)))
    elif input_path.is_dir():
        files = list_directory_images(input_path, args.recursive)
        output_root = (
            input_path
            if args.in_place
            else (args.output or input_path.with_name(f"{input_path.name}_compressed"))
        ).resolve()
        jobs.extend(
            (
                source,
                output_root / source.relative_to(input_path),
            )
            for source in files
        )
    else:
        print(f"输入必须是普通文件或目录：{input_path}", file=sys.stderr)
        return 2

    if not jobs:
        print("未找到受支持的图片（PNG/JPEG/WebP）。", file=sys.stderr)
        return 1

    failures = 0
    for source, destination in jobs:
        try:
            result = compress_one(
                source=source,
                destination=destination,
                max_bytes=max_bytes,
                ratio=args.ratio,
                min_width=args.min_width,
                force=args.force,
                in_place=args.in_place,
            )
            print_result(result, args.json)
        except Exception as exc:  # continue a directory batch and report every failure
            failures += 1
            print(f"ERROR {source}: {exc}", file=sys.stderr)

    if not args.json:
        print(f"处理完成：成功 {len(jobs) - failures}，失败 {failures}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())

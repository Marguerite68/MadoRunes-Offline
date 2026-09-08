import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const fontPath = resolve("app/src/main/res/font/huitian.ttf");
const requestedLabels = process.argv.slice(2);
const labels = requestedLabels.length ? requestedLabels : readDisplayHeadings();

const font = readFileSync(fontPath);
const supportedCodePoints = readCmap(font);
let hasMissingGlyphs = false;

for (const label of labels) {
  const missing = [...label].filter((character) => !supportedCodePoints.has(character.codePointAt(0)));
  if (missing.length) hasMissingGlyphs = true;
  console.log(`${label}\t${missing.length ? `缺字：${[...new Set(missing)].join("")}` : "完整支持"}`);
}

process.exitCode = hasMissingGlyphs ? 1 : 0;

function readDisplayHeadings() {
  const html = readFileSync(resolve("reviewer/index.html"), "utf8");
  return [...html.matchAll(/<h[12]\b[^>]*>([\s\S]*?)<\/h[12]>/g)]
    .map((match) => match[1].replace(/<[^>]+>/g, "").trim())
    .filter(Boolean);
}

function readCmap(buffer) {
  const tableCount = u16(buffer, 4);
  let cmapOffset = null;
  for (let index = 0; index < tableCount; index += 1) {
    const recordOffset = 12 + index * 16;
    const tag = buffer.toString("ascii", recordOffset, recordOffset + 4);
    if (tag === "cmap") {
      cmapOffset = u32(buffer, recordOffset + 8);
      break;
    }
  }
  if (cmapOffset === null) throw new Error("字体中没有 cmap 表");

  const codePoints = new Set();
  const subtableCount = u16(buffer, cmapOffset + 2);
  const visitedOffsets = new Set();
  for (let index = 0; index < subtableCount; index += 1) {
    const recordOffset = cmapOffset + 4 + index * 8;
    const subtableOffset = cmapOffset + u32(buffer, recordOffset + 4);
    if (visitedOffsets.has(subtableOffset)) continue;
    visitedOffsets.add(subtableOffset);
    const format = u16(buffer, subtableOffset);
    if (format === 4) readFormat4(buffer, subtableOffset, codePoints);
    if (format === 12 || format === 13) readFormat12(buffer, subtableOffset, codePoints, format);
  }
  return codePoints;
}

function readFormat4(buffer, offset, codePoints) {
  const segmentCount = u16(buffer, offset + 6) / 2;
  const endCodesOffset = offset + 14;
  const startCodesOffset = endCodesOffset + segmentCount * 2 + 2;
  const deltasOffset = startCodesOffset + segmentCount * 2;
  const rangeOffsetsOffset = deltasOffset + segmentCount * 2;

  for (let segment = 0; segment < segmentCount; segment += 1) {
    const start = u16(buffer, startCodesOffset + segment * 2);
    const end = u16(buffer, endCodesOffset + segment * 2);
    const delta = i16(buffer, deltasOffset + segment * 2);
    const rangeOffsetPosition = rangeOffsetsOffset + segment * 2;
    const rangeOffset = u16(buffer, rangeOffsetPosition);
    if (start === 0xffff && end === 0xffff) continue;

    for (let codePoint = start; codePoint <= end; codePoint += 1) {
      let glyphId;
      if (rangeOffset === 0) {
        glyphId = (codePoint + delta) & 0xffff;
      } else {
        const glyphOffset = rangeOffsetPosition + rangeOffset + (codePoint - start) * 2;
        if (glyphOffset + 2 > buffer.length) continue;
        glyphId = u16(buffer, glyphOffset);
        if (glyphId !== 0) glyphId = (glyphId + delta) & 0xffff;
      }
      if (glyphId !== 0) codePoints.add(codePoint);
    }
  }
}

function readFormat12(buffer, offset, codePoints, format) {
  const groupCount = u32(buffer, offset + 12);
  for (let index = 0; index < groupCount; index += 1) {
    const groupOffset = offset + 16 + index * 12;
    const start = u32(buffer, groupOffset);
    const end = u32(buffer, groupOffset + 4);
    const startGlyph = u32(buffer, groupOffset + 8);
    for (let codePoint = start; codePoint <= end; codePoint += 1) {
      const glyphId = format === 13 ? startGlyph : startGlyph + codePoint - start;
      if (glyphId !== 0) codePoints.add(codePoint);
    }
  }
}

function u16(buffer, offset) {
  return buffer.readUInt16BE(offset);
}

function i16(buffer, offset) {
  return buffer.readInt16BE(offset);
}

function u32(buffer, offset) {
  return buffer.readUInt32BE(offset);
}

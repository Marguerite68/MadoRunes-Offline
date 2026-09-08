# 魔法字典条目审核台

这是一个纯前端、本地运行的条目审核工具。文件不会上传到网络。

## 审核目录

在项目根目录创建 `.review`，并使用以下结构：

```text
.review/
├── pending/
│   ├── item/
│   └── wikiImg/
├── approved/
│   ├── item/
│   └── wikiImg/
└── rejected/
    ├── item/
    └── wikiImg/
```

JSON 与图片必须位于同一状态下对应的目录中，且除扩展名外文件名完全一致。例如：

```text
pending/item/0021_Example.json
pending/wikiImg/0021_Example.png
```

## 使用方式

推荐在项目根目录启动本地静态服务器：

```bash
python3 -m http.server 8765 --bind 127.0.0.1
```

然后在 Chrome 或 Edge 中打开 `http://127.0.0.1:8765/reviewer/`，点击“选择 .review 文件夹”。首次使用时，工作台会自动创建缺失的状态目录。

如果浏览器不支持目录读写 API，页面会切换为只读导入模式；审核状态只在当前页面会话中保留，不会移动本地文件。

开发或样式检查时，可以通过 `http://127.0.0.1:8765/reviewer/?demo=1` 加载项目内置的示例词条，不会写入任何文件。

## 审核行为

- “通过”会将同名 JSON 和图片一起移动到 `approved`。
- “拒绝”会将同名 JSON 和图片一起移动到 `rejected`。
- “恢复待审核”会将两个文件一起移回 `pending`。
- JSON 可以在右侧编辑并保存，保存时会重新解析和校验。
- 快捷键：`A` 通过、`R` 拒绝、`P` 恢复待审核。

`.review/` 已加入 Git 忽略，审核文件不会进入项目提交历史。

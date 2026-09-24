"use strict";

const STATUS = Object.freeze({
  pending: "待审核",
  approved: "已通过",
  rejected: "已拒绝"
});

const IMAGE_EXTENSIONS = new Set(["png", "jpg", "jpeg", "webp"]);
const MAX_IMAGE_BYTES = 300 * 1024;
const ENTRY_ID_PATTERN = /^\d{6}$/;
const CATEGORY_BY_NAMESPACE = Object.freeze({
  "00": 2,
  "01": 1,
  "02": 0
});

const state = {
  rootHandle: null,
  writable: false,
  entries: [],
  selectedKey: null,
  filter: "all",
  query: "",
  pendingMove: null,
  objectUrls: new Set()
};

const dom = {};

document.addEventListener("DOMContentLoaded", async () => {
  bindDom();
  bindEvents();
  restoreTheme();
  configureFileApi();
  if (new URLSearchParams(window.location.search).has("demo")) await loadDemoWorkspace();
  else render();
});

function bindDom() {
  [
    "themeButton", "reloadButton", "openWorkspaceButton", "fallbackImportLabel",
    "fallbackDirectoryInput", "emptyOpenButton", "workspaceBeacon", "workspaceName",
    "countAll", "countPending", "countApproved", "countRejected", "countIssues",
    "resultCount", "searchInput", "entryList", "emptyState", "phonePreview",
    "activeStatus", "previewTopName", "previewImage", "missingImage", "previewName",
    "categoryDot", "previewCategory", "previewRuneName", "previewEnglishName",
    "previewContent", "previewLinksWrap", "previewLinks", "reviewActions",
    "rejectButton", "pendingButton", "approveButton", "inspectorEmpty",
    "inspectorContent", "saveJsonButton", "validationCard", "validationIcon",
    "validationTitle", "validationSummary", "validationList", "jsonFileName",
    "jsonFileSize", "jsonEditor", "imageFileName", "imageFileSize", "sourceImage",
    "sourceMissingImage", "imageDimensions", "imageFormat", "imageTransparency",
    "toastRegion", "confirmDialog", "dialogTitle", "dialogMessage", "dialogConfirmButton"
  ].forEach((id) => { dom[id] = document.getElementById(id); });
}

function bindEvents() {
  dom.themeButton.addEventListener("click", toggleTheme);
  dom.openWorkspaceButton.addEventListener("click", openWorkspace);
  dom.emptyOpenButton.addEventListener("click", () => {
    if (supportsFileSystemAccess()) openWorkspace();
    else dom.fallbackDirectoryInput.click();
  });
  dom.reloadButton.addEventListener("click", reloadWorkspace);
  dom.fallbackDirectoryInput.addEventListener("change", importFallbackFiles);
  dom.searchInput.addEventListener("input", (event) => {
    state.query = event.target.value.trim().toLowerCase();
    renderEntryList();
  });
  document.querySelectorAll(".metric").forEach((button) => {
    button.addEventListener("click", () => {
      state.filter = button.dataset.filter;
      document.querySelectorAll(".metric").forEach((item) => item.classList.toggle("active", item === button));
      renderEntryList();
    });
  });
  dom.entryList.addEventListener("click", (event) => {
    const card = event.target.closest("[data-entry-key]");
    if (!card) return;
    state.selectedKey = card.dataset.entryKey;
    render();
  });
  dom.approveButton.addEventListener("click", () => requestMove("approved"));
  dom.rejectButton.addEventListener("click", () => requestMove("rejected"));
  dom.pendingButton.addEventListener("click", () => requestMove("pending"));
  dom.confirmDialog.addEventListener("close", async () => {
    if (dom.confirmDialog.returnValue === "confirm" && state.pendingMove) {
      const targetStatus = state.pendingMove;
      state.pendingMove = null;
      await moveSelectedEntry(targetStatus);
    } else {
      state.pendingMove = null;
    }
  });
  dom.saveJsonButton.addEventListener("click", saveJsonChanges);
  dom.jsonEditor.addEventListener("input", () => {
    dom.saveJsonButton.disabled = false;
  });
  document.addEventListener("keydown", (event) => {
    if (event.target === dom.jsonEditor || event.target === dom.searchInput) return;
    if (event.key.toLowerCase() === "a") requestMove("approved");
    if (event.key.toLowerCase() === "r") requestMove("rejected");
    if (event.key.toLowerCase() === "p") requestMove("pending");
  });
}

function supportsFileSystemAccess() {
  return "showDirectoryPicker" in window;
}

function configureFileApi() {
  const supported = supportsFileSystemAccess();
  dom.openWorkspaceButton.classList.toggle("hidden", !supported);
  dom.fallbackImportLabel.style.display = supported ? "none" : "inline-flex";
}

async function openWorkspace() {
  try {
    const handle = await window.showDirectoryPicker({ mode: "readwrite", id: "madorunes-review" });
    state.rootHandle = handle;
    state.writable = true;
    await ensureWorkspaceDirectories();
    await loadFromWorkspaceHandle();
    toast(`已读取 ${handle.name}`);
  } catch (error) {
    if (error.name !== "AbortError") {
      console.error(error);
      toast(`无法读取目录：${error.message}`);
    }
  }
}

async function ensureWorkspaceDirectories() {
  for (const status of Object.keys(STATUS)) {
    const statusHandle = await state.rootHandle.getDirectoryHandle(status, { create: true });
    await statusHandle.getDirectoryHandle("item", { create: true });
    await statusHandle.getDirectoryHandle("wikiImg", { create: true });
  }
}

async function reloadWorkspace() {
  if (state.rootHandle) await loadFromWorkspaceHandle();
  else if (dom.fallbackDirectoryInput.files?.length) await importFallbackFiles({ target: dom.fallbackDirectoryInput });
}

async function loadFromWorkspaceHandle() {
  clearObjectUrls();
  const entries = [];
  for (const status of Object.keys(STATUS)) {
    const statusHandle = await state.rootHandle.getDirectoryHandle(status);
    const itemHandle = await statusHandle.getDirectoryHandle("item");
    const imageHandle = await statusHandle.getDirectoryHandle("wikiImg");
    const jsonFiles = await readDirectFiles(itemHandle, (name) => extensionOf(name) === "json");
    const imageFiles = await readDirectFiles(imageHandle, (name) => IMAGE_EXTENSIONS.has(extensionOf(name)));
    entries.push(...await pairFiles(jsonFiles, imageFiles, status, { itemHandle, imageHandle }));
  }
  state.entries = entries.sort(entrySort);
  state.selectedKey = chooseSelection(state.selectedKey);
  dom.workspaceName.textContent = `${state.rootHandle.name} · 可读写`;
  dom.workspaceBeacon.classList.add("connected");
  dom.reloadButton.disabled = false;
  render();
}

async function readDirectFiles(directoryHandle, predicate) {
  const files = [];
  for await (const [name, handle] of directoryHandle.entries()) {
    if (handle.kind !== "file" || !predicate(name)) continue;
    files.push({ file: await handle.getFile(), handle });
  }
  return files;
}

async function importFallbackFiles(event) {
  clearObjectUrls();
  const files = Array.from(event.target.files || []);
  if (!files.length) return;
  const byStatus = { pending: { json: [], image: [] }, approved: { json: [], image: [] }, rejected: { json: [], image: [] } };
  for (const file of files) {
    const parts = (file.webkitRelativePath || file.name).split("/");
    const status = parts.find((part) => part in STATUS) || "pending";
    const ext = extensionOf(file.name);
    if (ext === "json") byStatus[status].json.push({ file, handle: null });
    if (IMAGE_EXTENSIONS.has(ext)) byStatus[status].image.push({ file, handle: null });
  }
  state.entries = [];
  for (const status of Object.keys(STATUS)) {
    state.entries.push(...await pairFiles(byStatus[status].json, byStatus[status].image, status, null));
  }
  state.entries.sort(entrySort);
  state.rootHandle = null;
  state.writable = false;
  state.selectedKey = chooseSelection(null);
  const rootName = files[0].webkitRelativePath?.split("/")[0] || "导入目录";
  dom.workspaceName.textContent = `${rootName} · 只读模式`;
  dom.workspaceBeacon.classList.add("connected");
  dom.reloadButton.disabled = false;
  render();
  toast("当前浏览器使用只读导入；审核状态仅在本次页面中保留");
}

async function loadDemoWorkspace() {
  try {
    const [jsonResponse, imageResponse] = await Promise.all([
      fetch("../app/src/main/assets/item/000006_Elsa_Maria.json"),
      fetch("../app/src/main/assets/wikiImg/000006_Elsa_Maria.png")
    ]);
    if (!jsonResponse.ok || !imageResponse.ok) throw new Error("示例资源读取失败");
    const jsonFile = new File([await jsonResponse.blob()], "000006_Elsa_Maria.json", { type: "application/json" });
    const imageFile = new File([await imageResponse.blob()], "000006_Elsa_Maria.png", { type: "image/png" });
    state.entries = await pairFiles(
      [{ file: jsonFile, handle: null }],
      [{ file: imageFile, handle: null }],
      "pending",
      null
    );
    state.rootHandle = null;
    state.writable = false;
    state.selectedKey = state.entries[0]?.key || null;
    dom.workspaceName.textContent = "示例数据 · 只读模式";
    dom.workspaceBeacon.classList.add("connected");
    render();
  } catch (error) {
    console.error(error);
    render();
    toast(error.message);
  }
}

async function pairFiles(jsonItems, imageItems, status, directoryHandles) {
  const grouped = new Map();
  const add = (item, type) => {
    const basename = basenameOf(item.file.name);
    if (!grouped.has(basename)) grouped.set(basename, { basename, status, directoryHandles });
    const group = grouped.get(basename);
    if (group[type]) {
      group.duplicateFiles ||= [];
      group.duplicateFiles.push(item.file.name);
    } else {
      group[type] = item;
    }
  };
  jsonItems.forEach((item) => add(item, "jsonItem"));
  imageItems.forEach((item) => add(item, "imageItem"));

  const entries = [];
  for (const group of grouped.values()) entries.push(await hydrateEntry(group));
  return entries;
}

async function hydrateEntry(group) {
  let raw = "";
  let data = null;
  let parseError = null;
  if (group.jsonItem) {
    raw = await group.jsonItem.file.text();
    try { data = JSON.parse(raw); }
    catch (error) { parseError = error.message; }
  }

  let imageUrl = null;
  let imageMeta = null;
  if (group.imageItem) {
    imageUrl = URL.createObjectURL(group.imageItem.file);
    state.objectUrls.add(imageUrl);
    imageMeta = await inspectImage(group.imageItem.file, imageUrl);
  }

  const entry = {
    key: `${group.status}:${group.basename}`,
    basename: group.basename,
    status: group.status,
    directoryHandles: group.directoryHandles,
    duplicateFiles: group.duplicateFiles || [],
    jsonItem: group.jsonItem || null,
    imageItem: group.imageItem || null,
    raw,
    data,
    parseError,
    imageUrl,
    imageMeta,
    issues: []
  };
  entry.issues = validateEntry(entry);
  return entry;
}

async function inspectImage(file, url) {
  return new Promise((resolve) => {
    const image = new Image();
    image.onload = () => {
      const meta = {
        width: image.naturalWidth,
        height: image.naturalHeight,
        type: file.type || `image/${extensionOf(file.name)}`,
        hasTransparency: null
      };
      if (extensionOf(file.name) === "png") {
        try {
          const canvas = document.createElement("canvas");
          const scale = Math.min(1, 160 / Math.max(image.naturalWidth, image.naturalHeight));
          canvas.width = Math.max(1, Math.round(image.naturalWidth * scale));
          canvas.height = Math.max(1, Math.round(image.naturalHeight * scale));
          const context = canvas.getContext("2d", { willReadFrequently: true });
          context.drawImage(image, 0, 0, canvas.width, canvas.height);
          const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
          meta.hasTransparency = false;
          for (let index = 3; index < pixels.length; index += 4) {
            if (pixels[index] < 250) { meta.hasTransparency = true; break; }
          }
        } catch (_) { meta.hasTransparency = null; }
      }
      resolve(meta);
    };
    image.onerror = () => resolve({ width: 0, height: 0, type: file.type || "未知", hasTransparency: null, broken: true });
    image.src = url;
  });
}

function validateEntry(entry) {
  const issues = [];
  if (!entry.jsonItem) issues.push("缺少同名 JSON 文件");
  if (!entry.imageItem) issues.push("缺少同名图片文件");
  if (entry.duplicateFiles.length) issues.push(`存在重复文件：${entry.duplicateFiles.join("、")}`);
  if (entry.parseError) issues.push(`JSON 解析失败：${entry.parseError}`);
  if (!entry.data) return issues;

  const data = entry.data;
  if (typeof data.id !== "string" || !ENTRY_ID_PATTERN.test(data.id)) {
    issues.push("id 必须是 6 位数字（2 位类别码 + 4 位类内序号）");
  } else {
    const expectedCategory = CATEGORY_BY_NAMESPACE[data.id.slice(0, 2)];
    if (expectedCategory === undefined) issues.push("id 使用了尚未登记的类别码");
    else if (data.category !== expectedCategory) issues.push(`id 类别码与 category 不一致（应为 ${expectedCategory}）`);
  }
  if (![0, 1, 2].includes(data.category)) issues.push("category 必须是 0、1 或 2");
  if (typeof data.name !== "string" || !data.name.trim()) issues.push("name 必须是非空字符串");
  if (!(data.enName === null || typeof data.enName === "string")) issues.push("enName 必须是字符串或 null");
  if (typeof data.content !== "string" || !data.content.trim()) issues.push("content 必须是非空字符串");
  if (!Number.isInteger(data.version) || data.version < 1) issues.push("version 必须是大于 0 的整数");
  if (!(data.externalLinks === null || Array.isArray(data.externalLinks))) issues.push("externalLinks 必须是数组或 null");
  else if (Array.isArray(data.externalLinks) && data.externalLinks.some((link) => !link || typeof link.label !== "string" || typeof link.url !== "string")) {
    issues.push("externalLinks 中每一项都必须包含 label 和 url");
  }

  if (entry.imageItem) {
    if (basenameOf(entry.imageItem.file.name) !== entry.basename) issues.push("图片名与 JSON 文件名不一致");
    if (entry.imageItem.file.size > MAX_IMAGE_BYTES) issues.push(`图片超过 300 KB（${formatBytes(entry.imageItem.file.size)}）`);
    if (extensionOf(entry.imageItem.file.name) !== "png") issues.push("图片不是 PNG 格式");
    if (entry.imageMeta?.broken) issues.push("图片无法读取");
    if (entry.imageMeta?.hasTransparency === false && extensionOf(entry.imageItem.file.name) === "png") issues.push("PNG 图片没有检测到透明区域");
  }

  if (typeof data.imagePath !== "string" || !data.imagePath.trim()) issues.push("imagePath 必须指向同名图片");
  else {
    if (basenameOf(data.imagePath) !== entry.basename) issues.push("imagePath 与 JSON 文件名不一致");
    if (entry.imageItem && data.imagePath !== entry.imageItem.file.name) issues.push("imagePath 与实际图片文件名不一致");
  }
  if (typeof data.id === "string" && !entry.basename.startsWith(`${data.id}_`)) issues.push("文件名应以条目 id 开头");
  return issues;
}

function chooseSelection(preferredKey) {
  if (preferredKey && state.entries.some((entry) => entry.key === preferredKey)) return preferredKey;
  return state.entries.find((entry) => entry.status === "pending")?.key || state.entries[0]?.key || null;
}

function entrySort(a, b) {
  const statusOrder = { pending: 0, approved: 1, rejected: 2 };
  return statusOrder[a.status] - statusOrder[b.status] || a.basename.localeCompare(b.basename, "zh-CN");
}

function render() {
  renderSummary();
  renderEntryList();
  renderSelection();
}

function renderSummary() {
  dom.countAll.textContent = state.entries.length;
  dom.countPending.textContent = countBy((entry) => entry.status === "pending");
  dom.countApproved.textContent = countBy((entry) => entry.status === "approved");
  dom.countRejected.textContent = countBy((entry) => entry.status === "rejected");
  dom.countIssues.textContent = countBy((entry) => entry.issues.length > 0);
}

function renderEntryList() {
  const entries = filteredEntries();
  dom.resultCount.textContent = `${entries.length} 条`;
  if (!entries.length) {
    dom.entryList.innerHTML = `<div class="list-empty">${state.entries.length ? "没有符合当前筛选条件的条目" : "选择 .review 文件夹后，条目会显示在这里"}</div>`;
    return;
  }
  dom.entryList.innerHTML = entries.map((entry) => {
    const name = escapeHtml(entry.data?.name || entry.basename);
    const thumb = entry.imageUrl
      ? `<img class="entry-thumb checkerboard" src="${entry.imageUrl}" alt="">`
      : `<span class="entry-thumb entry-thumb-placeholder">◇</span>`;
    return `<button class="entry-card ${entry.key === state.selectedKey ? "active" : ""}" data-entry-key="${escapeAttribute(entry.key)}" type="button">
      ${thumb}
      <span class="entry-main">
        <span class="entry-name">${name}</span>
        <span class="entry-file">${escapeHtml(entry.basename)}</span>
      </span>
      <span class="entry-indicators">
        <i class="tiny-status ${entry.status}" title="${STATUS[entry.status]}"></i>
        ${entry.issues.length ? `<span class="issue-count" title="${entry.issues.length} 个问题">${entry.issues.length}</span>` : ""}
      </span>
    </button>`;
  }).join("");
}

function filteredEntries() {
  return state.entries.filter((entry) => {
    const matchesFilter = state.filter === "all"
      || (state.filter === "issues" ? entry.issues.length > 0 : entry.status === state.filter);
    const haystack = `${entry.basename} ${entry.data?.id || ""} ${entry.data?.name || ""} ${entry.data?.enName || ""}`.toLowerCase();
    return matchesFilter && (!state.query || haystack.includes(state.query));
  });
}

function renderSelection() {
  const entry = selectedEntry();
  const hasEntry = Boolean(entry);
  dom.emptyState.classList.toggle("hidden", hasEntry);
  dom.phonePreview.classList.toggle("hidden", !hasEntry);
  dom.reviewActions.classList.toggle("hidden", !hasEntry);
  dom.inspectorEmpty.classList.toggle("hidden", hasEntry);
  dom.inspectorContent.classList.toggle("hidden", !hasEntry);
  if (!entry) return;

  renderPreview(entry);
  renderInspector(entry);
  renderReviewActions(entry);
}

function renderPreview(entry) {
  const data = entry.data || {};
  const name = data.name || entry.basename;
  dom.previewTopName.textContent = name;
  dom.previewName.textContent = name;
  dom.previewRuneName.textContent = data.category === 2 && data.enName ? String(data.enName).toUpperCase() : "";
  dom.previewRuneName.classList.toggle("hidden", !(data.category === 2 && data.enName));
  dom.previewEnglishName.textContent = data.enName ? `英文名： ${data.enName}` : "";
  dom.previewEnglishName.classList.toggle("hidden", !data.enName);

  const category = categoryInfo(data.category);
  dom.previewCategory.textContent = category.label;
  dom.categoryDot.style.backgroundColor = category.color;

  if (entry.imageUrl) {
    dom.previewImage.src = entry.imageUrl;
    dom.previewImage.classList.remove("hidden");
    dom.missingImage.classList.add("hidden");
  } else {
    dom.previewImage.removeAttribute("src");
    dom.previewImage.classList.add("hidden");
    dom.missingImage.classList.remove("hidden");
  }

  const paragraphs = typeof data.content === "string" ? data.content.split(/\n\s*\n/).filter(Boolean) : [];
  dom.previewContent.innerHTML = paragraphs.length
    ? paragraphs.map((paragraph) => `<p>${escapeHtml(paragraph).replace(/\n/g, "<br>")}</p>`).join("")
    : `<p class="muted-copy">JSON 无法解析或正文为空。</p>`;

  const links = Array.isArray(data.externalLinks) ? data.externalLinks : [];
  dom.previewLinksWrap.classList.toggle("hidden", !links.length);
  dom.previewLinks.innerHTML = links.map((link) => `<a href="${escapeAttribute(safeUrl(link.url))}" target="_blank" rel="noreferrer">${escapeHtml(link.label || link.url || "链接")}</a>`).join("");

  dom.activeStatus.textContent = STATUS[entry.status];
  dom.activeStatus.className = `status-pill status-${entry.status}`;
}

function renderInspector(entry) {
  const issueCount = entry.issues.length;
  dom.validationCard.classList.toggle("has-issues", issueCount > 0);
  dom.validationIcon.textContent = issueCount ? "!" : "✓";
  dom.validationTitle.textContent = issueCount ? `发现 ${issueCount} 个问题` : "校验通过";
  dom.validationSummary.textContent = issueCount ? "请在通过前检查以下项目" : "文件配对与字段格式正常";
  dom.validationList.innerHTML = entry.issues.map((issue) => `<li>${escapeHtml(issue)}</li>`).join("");

  dom.jsonFileName.textContent = entry.jsonItem?.file.name || "缺少 JSON";
  dom.jsonFileSize.textContent = entry.jsonItem ? formatBytes(entry.jsonItem.file.size) : "—";
  dom.jsonEditor.value = entry.raw || "";
  dom.jsonEditor.disabled = !entry.jsonItem;
  dom.saveJsonButton.disabled = true;

  dom.imageFileName.textContent = entry.imageItem?.file.name || "缺少图片";
  dom.imageFileSize.textContent = entry.imageItem ? formatBytes(entry.imageItem.file.size) : "—";
  if (entry.imageUrl) {
    dom.sourceImage.src = entry.imageUrl;
    dom.sourceImage.classList.remove("hidden");
    dom.sourceMissingImage.classList.add("hidden");
  } else {
    dom.sourceImage.removeAttribute("src");
    dom.sourceImage.classList.add("hidden");
    dom.sourceMissingImage.classList.remove("hidden");
  }
  dom.imageDimensions.textContent = entry.imageMeta ? `${entry.imageMeta.width} × ${entry.imageMeta.height} px` : "尺寸 —";
  dom.imageFormat.textContent = entry.imageItem ? extensionOf(entry.imageItem.file.name).toUpperCase() : "格式 —";
  dom.imageTransparency.textContent = entry.imageMeta?.hasTransparency === true
    ? "含透明区域"
    : entry.imageMeta?.hasTransparency === false ? "无透明区域" : "透明度未知";
}

function renderReviewActions(entry) {
  dom.approveButton.disabled = entry.status === "approved";
  dom.rejectButton.disabled = entry.status === "rejected";
  dom.pendingButton.disabled = entry.status === "pending";
  dom.approveButton.title = entry.issues.length ? "当前条目存在校验问题，仍可在确认后通过" : "标记为通过（快捷键 A）";
}

function requestMove(targetStatus) {
  const entry = selectedEntry();
  if (!entry || entry.status === targetStatus) return;
  if (!state.writable) {
    entry.status = targetStatus;
    entry.key = `${targetStatus}:${entry.basename}`;
    state.selectedKey = entry.key;
    state.entries.sort(entrySort);
    render();
    toast(`已在本次会话中标记为${STATUS[targetStatus]}`);
    return;
  }
  state.pendingMove = targetStatus;
  dom.dialogTitle.textContent = "条目去向？";
  dom.dialogMessage.textContent = `“${entry.data?.name || entry.basename}”的 JSON 与图片将一起移动到 ${targetStatus}/ 目录。`;
  dom.dialogConfirmButton.textContent = `确认${STATUS[targetStatus]}`;
  dom.confirmDialog.showModal();
}

async function moveSelectedEntry(targetStatus) {
  const entry = selectedEntry();
  if (!entry || entry.status === targetStatus || !state.rootHandle) return;
  if (!entry.jsonItem || !entry.imageItem) {
    toast("JSON 与图片必须完整配对后才能移动");
    return;
  }

  try {
    const oldHandles = entry.directoryHandles;
    const targetStatusHandle = await state.rootHandle.getDirectoryHandle(targetStatus);
    const targetItemHandle = await targetStatusHandle.getDirectoryHandle("item");
    const targetImageHandle = await targetStatusHandle.getDirectoryHandle("wikiImg");

    if (await basenameExists(targetItemHandle, entry.basename) || await basenameExists(targetImageHandle, entry.basename)) {
      throw new Error("目标目录中已有同名文件，请先处理冲突");
    }

    await copyFileToDirectory(entry.jsonItem.file, targetItemHandle);
    await copyFileToDirectory(entry.imageItem.file, targetImageHandle);
    await oldHandles.itemHandle.removeEntry(entry.jsonItem.file.name);
    await oldHandles.imageHandle.removeEntry(entry.imageItem.file.name);

    toast(`已将 ${entry.data?.name || entry.basename} 移至 ${STATUS[targetStatus]}`);
    const nextPreferred = state.entries.find((item) => item.status === "pending" && item.key !== entry.key)?.key;
    await loadFromWorkspaceHandle();
    state.selectedKey = nextPreferred || `${targetStatus}:${entry.basename}`;
    render();
  } catch (error) {
    console.error(error);
    toast(`移动失败：${error.message}`);
  }
}

async function copyFileToDirectory(file, directoryHandle) {
  const targetHandle = await directoryHandle.getFileHandle(file.name, { create: true });
  const writable = await targetHandle.createWritable();
  await writable.write(file);
  await writable.close();
}

async function basenameExists(directoryHandle, basename) {
  for await (const [name, handle] of directoryHandle.entries()) {
    if (handle.kind === "file" && basenameOf(name) === basename) return true;
  }
  return false;
}

async function saveJsonChanges() {
  const entry = selectedEntry();
  if (!entry?.jsonItem) return;
  const raw = dom.jsonEditor.value;
  let data;
  try {
    data = JSON.parse(raw);
  } catch (error) {
    toast(`JSON 格式错误：${error.message}`);
    return;
  }

  const formatted = `${JSON.stringify(data, null, 2)}\n`;
  if (state.writable && entry.jsonItem.handle) {
    try {
      const writable = await entry.jsonItem.handle.createWritable();
      await writable.write(formatted);
      await writable.close();
      toast("JSON 已保存并重新校验");
      await loadFromWorkspaceHandle();
      state.selectedKey = `${entry.status}:${entry.basename}`;
      render();
    } catch (error) {
      console.error(error);
      toast(`保存失败：${error.message}`);
    }
  } else {
    entry.raw = formatted;
    entry.data = data;
    entry.parseError = null;
    entry.issues = validateEntry(entry);
    dom.saveJsonButton.disabled = true;
    render();
    toast("修改已应用到本次会话；只读模式不会写回文件");
  }
}

function selectedEntry() {
  return state.entries.find((entry) => entry.key === state.selectedKey) || null;
}

function categoryInfo(category) {
  if (category === 1) return { label: "角色", color: "#ffb7c5" };
  if (category === 2) return { label: "魔女", color: "#d9d9d9" };
  return { label: "一般词条", color: "#70838f" };
}

function extensionOf(name) {
  const clean = String(name || "").split("/").pop();
  const index = clean.lastIndexOf(".");
  return index < 0 ? "" : clean.slice(index + 1).toLowerCase();
}

function basenameOf(name) {
  const clean = String(name || "").split("/").pop();
  const index = clean.lastIndexOf(".");
  return index < 0 ? clean : clean.slice(0, index);
}

function formatBytes(bytes) {
  if (!Number.isFinite(bytes)) return "—";
  if (bytes < 1024) return `${bytes} B`;
  return `${(bytes / 1024).toFixed(bytes >= 100 * 1024 ? 0 : 1)} KB`;
}

function countBy(predicate) {
  return state.entries.reduce((count, entry) => count + (predicate(entry) ? 1 : 0), 0);
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}

function safeUrl(value) {
  try {
    const url = new URL(String(value || ""));
    return ["http:", "https:"].includes(url.protocol) ? url.href : "#";
  } catch (_) {
    return "#";
  }
}

function toast(message) {
  const element = document.createElement("div");
  element.className = "toast";
  element.textContent = message;
  dom.toastRegion.appendChild(element);
  window.setTimeout(() => element.remove(), 3600);
}

function clearObjectUrls() {
  state.objectUrls.forEach((url) => URL.revokeObjectURL(url));
  state.objectUrls.clear();
}

function toggleTheme() {
  const current = document.documentElement.dataset.theme || "light";
  const next = current === "dark" ? "light" : "dark";
  document.documentElement.dataset.theme = next;
  try { localStorage.setItem("madorunes-review-theme", next); }
  catch (_) { /* Local file origins may disable storage. */ }
}

function restoreTheme() {
  let stored = null;
  try { stored = localStorage.getItem("madorunes-review-theme"); }
  catch (_) { /* Local file origins may disable storage. */ }
  const preferredDark = window.matchMedia?.("(prefers-color-scheme: dark)").matches;
  document.documentElement.dataset.theme = stored || (preferredDark ? "dark" : "light");
}

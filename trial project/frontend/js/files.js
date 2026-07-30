const message = document.getElementById("message");
const fileInput = document.getElementById("fileInput");
const uploadBtn = document.getElementById("uploadBtn");
const deleteFileBtn = document.getElementById("deleteFileBtn");
const fileList = document.getElementById("fileList");
const emptyList = document.getElementById("emptyList");
const fileMeta = document.getElementById("fileMeta");
const fileContent = document.getElementById("fileContent");
const maxFileSizeLabel = document.getElementById("maxFileSizeLabel");

/** Must match backend app.files.max-size / spring.servlet.multipart.max-file-size (default 10MB). */
const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
const MAX_FILE_SIZE_LABEL = "10 MB";

let selectedFileId = null;

if (maxFileSizeLabel) {
    maxFileSizeLabel.textContent = MAX_FILE_SIZE_LABEL;
}

if (!requireAuth()) {
    // redirect handled by requireAuth
} else {
    loadFiles();
}

uploadBtn.addEventListener("click", async function () {
    const file = fileInput.files && fileInput.files[0];
    if (!file) {
        message.innerHTML = `<div class="error">Please choose a file first.</div>`;
        return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
        message.innerHTML = formatApiError(new ApiError(
            "File upload failed",
            { file: `File exceeds the maximum allowed size of ${MAX_FILE_SIZE_LABEL}` }
        ));
        return;
    }

    uploadBtn.disabled = true;
    uploadBtn.textContent = "Uploading...";
    message.innerHTML = "";

    try {
        const { file: uploaded, envelope } = await uploadFile(file);
        message.innerHTML = `<div class="success">${escapeHtml(envelope.message || uploaded.message || "Upload complete")}</div>`;
        fileInput.value = "";
        selectedFileId = uploaded.id;
        await loadFiles();
        await showFile(uploaded.id);
    } catch (error) {
        message.innerHTML = formatApiError(error);
    } finally {
        uploadBtn.disabled = false;
        uploadBtn.textContent = "Upload";
    }
});

deleteFileBtn.addEventListener("click", async function () {
    if (selectedFileId == null) {
        return;
    }

    const confirmed = window.confirm("Delete this file permanently?");
    if (!confirmed) {
        return;
    }

    deleteFileBtn.disabled = true;
    deleteFileBtn.textContent = "Deleting...";
    message.innerHTML = "";

    try {
        const envelope = await deleteFile(selectedFileId);
        message.innerHTML = `<div class="success">${escapeHtml(envelope.message || "File deleted")}</div>`;
        clearContentPanel();
        selectedFileId = null;
        await loadFiles();
    } catch (error) {
        message.innerHTML = formatApiError(error);
    } finally {
        deleteFileBtn.disabled = false;
        deleteFileBtn.textContent = "Delete file";
    }
});

async function loadFiles() {
    try {
        const { files } = await listFiles();
        renderFileList(files);
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }
}

function renderFileList(files) {
    fileList.innerHTML = "";
    if (!files.length) {
        emptyList.style.display = "block";
        return;
    }
    emptyList.style.display = "none";

    files.forEach((file) => {
        const li = document.createElement("li");
        li.className = "file-list-item" + (file.id === selectedFileId ? " active" : "");
        li.dataset.id = String(file.id);

        const button = document.createElement("button");
        button.type = "button";
        button.className = "file-list-main";
        button.title = file.originalFilename || "file";
        button.innerHTML = `
            <span class="file-list-name">${escapeHtml(file.originalFilename || "file")}</span>
        `;
        button.addEventListener("click", () => showFile(file.id));

        li.appendChild(button);
        fileList.appendChild(li);
    });
}

async function showFile(fileId) {
    selectedFileId = fileId;
    Array.from(fileList.children).forEach((li) => {
        li.classList.toggle("active", li.dataset.id === String(fileId));
    });

    fileMeta.textContent = "Loading...";
    fileContent.textContent = "";
    deleteFileBtn.hidden = true;

    try {
        const { file } = await getFile(fileId);
        const sizeKb = file.sizeBytes != null ? (file.sizeBytes / 1024).toFixed(1) : "-";
        fileMeta.innerHTML = `
            <div><strong>${escapeHtml(file.originalFilename || "file")}</strong></div>
            <div>Type: ${escapeHtml(file.contentType || "-")} · Size: ${escapeHtml(sizeKb)} KB</div>
            <div>Status: <span class="status-${escapeHtml(String(file.extractionStatus || "").toLowerCase())}">${escapeHtml(file.extractionStatus || "-")}</span></div>
            ${file.errorMessage ? `<div class="file-error">${escapeHtml(file.errorMessage)}</div>` : ""}
        `;

        if (file.extractionStatus === "EMPTY") {
            fileContent.textContent = "(No extractable text found in this file.)";
        } else if (file.extractionStatus === "FAILED") {
            fileContent.textContent = "(Text extraction failed. See status details above.)";
        } else {
            fileContent.textContent = file.extractedText || "(Empty extraction result.)";
        }
        deleteFileBtn.hidden = false;
    } catch (error) {
        message.innerHTML = formatApiError(error);
        fileMeta.textContent = "Could not load file.";
        deleteFileBtn.hidden = true;
    }
}

function clearContentPanel() {
    fileMeta.textContent = "Select a file to view extracted text.";
    fileContent.textContent = "";
    deleteFileBtn.hidden = true;
}

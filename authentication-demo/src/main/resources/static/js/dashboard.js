const urlParams = new URLSearchParams(window.location.search);
const urlUsername = urlParams.get('username');
const urlToken = urlParams.get('token');
if (urlUsername) {
  sessionStorage.setItem('username', urlUsername);
}
if (urlToken) {
  sessionStorage.setItem('token', urlToken);
}

const storedUsername = sessionStorage.getItem('username');
if (!storedUsername) {
  window.location.href = 'index.html';
}

const currentUsernameLabel = document.getElementById('currentUsernameLabel');
currentUsernameLabel.textContent = storedUsername;

const msg = document.getElementById('msg');
function showMsg(text, isError) {
  msg.textContent = text;
  msg.className = 'msg ' + (isError ? 'error' : 'success');
}

// ================= Tab Switching =================
const tabProfile = document.getElementById('tabProfile');
const tabFiles = document.getElementById('tabFiles');
const profileForm = document.getElementById('profileForm');
const filesForm = document.getElementById('filesForm');

tabProfile.addEventListener('click', () => {
  tabProfile.classList.add('active');
  tabFiles.classList.remove('active');
  profileForm.classList.add('active');
  filesForm.classList.remove('active');
  msg.textContent = '';
});

tabFiles.addEventListener('click', () => {
  tabFiles.classList.add('active');
  tabProfile.classList.remove('active');
  filesForm.classList.add('active');
  profileForm.classList.remove('active');
  msg.textContent = '';
  loadUploadedFiles();
});

// ================= Profile Form Handlers =================
const currentPasswordInput = document.getElementById('currentPassword');

function wireUpdateRow({ inputId, buttonId, endpoint, buildBody, successLabel }) {
  const input = document.getElementById(inputId);
  const button = document.getElementById(buttonId);

  function refreshButtonState() {
    const hasCurrentPassword = currentPasswordInput.value.length > 0;
    const hasValidNewValue = input.value.trim() !== '' && input.checkValidity();
    button.disabled = !(hasCurrentPassword && hasValidNewValue);
  }

  input.addEventListener('input', refreshButtonState);
  currentPasswordInput.addEventListener('input', refreshButtonState);

  button.addEventListener('click', async () => {
    const username = sessionStorage.getItem('username');
    const body = buildBody(username, currentPasswordInput.value, input.value.trim());

    setLoading(button, true);
    const result = await callApi(endpoint, 'PUT', body);
    setLoading(button, false);

    if (result.ok) {
      showMsg(successLabel, false);

      if (inputId === 'newUsername') {
        sessionStorage.setItem('username', input.value.trim());
        currentUsernameLabel.textContent = input.value.trim();
      }

      input.value = '';
      button.disabled = true;
    } else {
      showMsg(extractErrorText(result.payload), true);
    }
  });
}

wireUpdateRow({
  inputId: 'newUsername',
  buttonId: 'updateUsernameBtn',
  endpoint: `${API_BASE}/update/username`,
  buildBody: (username, currentPassword, newUsername) => ({
    currentUsername: username,
    currentPassword,
    newUsername
  }),
  successLabel: 'Username updated successfully'
});

wireUpdateRow({
  inputId: 'newPassword',
  buttonId: 'updatePasswordBtn',
  endpoint: `${API_BASE}/update/password`,
  buildBody: (username, currentPassword, newPassword) => ({
    currentUsername: username,
    currentPassword,
    newPassword
  }),
  successLabel: 'Password updated successfully'
});

wireUpdateRow({
  inputId: 'newEmail',
  buttonId: 'updateEmailBtn',
  endpoint: `${API_BASE}/update/email`,
  buildBody: (username, currentPassword, newEmail) => ({
    currentUsername: username,
    currentPassword,
    newEmail
  }),
  successLabel: 'Email updated successfully'
});

wireUpdateRow({
  inputId: 'newPhoneNumber',
  buttonId: 'updatePhoneBtn',
  endpoint: `${API_BASE}/update/phone`,
  buildBody: (username, currentPassword, newPhoneNumber) => ({
    currentUsername: username,
    currentPassword,
    newPhoneNumber
  }),
  successLabel: 'Phone number updated successfully'
});

const deleteBtn = document.getElementById('deleteBtn');
currentPasswordInput.addEventListener('input', () => {
  deleteBtn.disabled = currentPasswordInput.value.length === 0;
});

deleteBtn.addEventListener('click', async () => {
  const confirmed = window.confirm(
    'This will permanently delete your account. This cannot be undone. Continue?'
  );
  if (!confirmed) {
    return;
  }

  const username = sessionStorage.getItem('username');

  setLoading(deleteBtn, true);
  const result = await callApi(`${API_BASE}/delete`, 'DELETE', {
    username,
    password: currentPasswordInput.value
  });
  setLoading(deleteBtn, false);

  if (result.ok) {
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('token');
    alert('Your account has been deleted.');
    window.location.href = 'index.html';
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  sessionStorage.removeItem('username');
  sessionStorage.removeItem('token');
  window.location.href = 'index.html';
});

// ================= File Upload & Document Viewer Handlers =================
const fileDropzone = document.getElementById('fileDropzone');
const fileInput = document.getElementById('fileInput');
const dropzoneText = document.getElementById('dropzoneText');
const uploadFileBtn = document.getElementById('uploadFileBtn');

const documentViewerPanel = document.getElementById('documentViewerPanel');
const openDocTitle = document.getElementById('openDocTitle');
const openDocBadge = document.getElementById('openDocBadge');
const docSearchInput = document.getElementById('docSearchInput');
const docSearchBtn = document.getElementById('docSearchBtn');
const documentContentBox = document.getElementById('documentContentBox');

const excelPaginationControls = document.getElementById('excelPaginationControls');
const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');
const pageIndicator = document.getElementById('pageIndicator');

const recentFilesList = document.getElementById('recentFilesList');
const historySearchInput = document.getElementById('historySearchInput');
const refreshHistoryBtn = document.getElementById('refreshHistoryBtn');

let selectedFile = null;
let currentOpenDocId = null;
let currentDocPage = 0;
let currentDocTotalPages = 1;

fileDropzone.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', (e) => {
  if (e.target.files.length > 0) {
    handleFileSelect(e.target.files[0]);
  }
});

fileDropzone.addEventListener('dragover', (e) => {
  e.preventDefault();
  fileDropzone.classList.add('dragover');
});

fileDropzone.addEventListener('dragleave', () => {
  fileDropzone.classList.remove('dragover');
});

fileDropzone.addEventListener('drop', (e) => {
  e.preventDefault();
  fileDropzone.classList.remove('dragover');
  if (e.dataTransfer.files.length > 0) {
    handleFileSelect(e.dataTransfer.files[0]);
  }
});

function handleFileSelect(file) {
  selectedFile = file;
  dropzoneText.innerHTML = `<strong>Selected:</strong> ${escapeHtml(file.name)} (${(file.size / 1024).toFixed(1)} KB)`;
  uploadFileBtn.disabled = false;
}

uploadFileBtn.addEventListener('click', async () => {
  if (!selectedFile) return;

  const formData = new FormData();
  formData.append('file', selectedFile);

  setLoading(uploadFileBtn, true);

  try {
    const token = sessionStorage.getItem('token');
    const headers = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch('/api/v1/files/upload', {
      method: 'POST',
      headers,
      body: formData
    });

    const result = await response.json();
    setLoading(uploadFileBtn, false);

    if (response.ok && result.success) {
      showMsg(result.message || 'File uploaded successfully!', result.data.duplicate);
      openDocument(result.data.fileId, result.data.originalFilename);
      loadUploadedFiles();
      selectedFile = null;
      fileInput.value = '';
      dropzoneText.textContent = 'Click or drag & drop a file here';
      uploadFileBtn.disabled = true;
    } else {
      showMsg(result.message || 'File upload failed', true);
    }
  } catch (err) {
    setLoading(uploadFileBtn, false);
    showMsg('File upload error: ' + err.message, true);
  }
});

async function openDocument(id, filename, page = 0, search = '') {
  currentOpenDocId = id;
  currentDocPage = page;

  const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
  const result = await callApi(`/api/v1/files/${id}/content?page=${page}&size=10${searchParam}`, 'GET');

  if (result.ok && result.payload.data) {
    const content = result.payload.data;
    documentViewerPanel.style.display = 'block';
    openDocTitle.textContent = content.originalFilename || filename || 'Document Viewer';
    openDocBadge.textContent = `${content.totalExtractedCount} items (${content.fileType.toUpperCase()})`;

    if (content.fileType === 'pdf') {
      excelPaginationControls.style.display = 'none';
      documentContentBox.innerHTML = `<div class="extracted-text-box">${escapeHtml(content.rawText || 'No matching text found')}</div>`;
    } else if (content.fileType === 'excel') {
      excelPaginationControls.style.display = 'flex';
      const pageData = content.paginatedExcelRows;
      renderExcelTable(pageData);
    }

    documentViewerPanel.scrollIntoView({ behavior: 'smooth' });
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
}

function renderExcelTable(pageData) {
  if (!pageData || !pageData.content || pageData.content.length === 0) {
    documentContentBox.innerHTML = '<p class="hint">No matching rows found in Excel document.</p>';
    currentDocTotalPages = 1;
    updatePaginationUI();
    return;
  }

  currentDocTotalPages = pageData.totalPages || 1;
  updatePaginationUI();

  const rows = pageData.content;
  const headers = Object.keys(rows[0].cellData || {});
  let tableHtml = '<table class="excel-data-table"><thead><tr><th>Row #</th>';
  headers.forEach(h => tableHtml += `<th>${escapeHtml(h)}</th>`);
  tableHtml += '</tr></thead><tbody>';

  rows.forEach(row => {
    tableHtml += `<tr><td><strong>${row.rowIndex}</strong></td>`;
    headers.forEach(h => tableHtml += `<td>${escapeHtml(row.cellData[h] || '')}</td>`);
    tableHtml += '</tr>';
  });

  tableHtml += '</tbody></table>';
  documentContentBox.innerHTML = tableHtml;
}

function updatePaginationUI() {
  pageIndicator.textContent = `Page ${currentDocPage + 1} of ${currentDocTotalPages}`;
  prevPageBtn.disabled = currentDocPage <= 0;
  nextPageBtn.disabled = currentDocPage >= currentDocTotalPages - 1;
}

prevPageBtn.addEventListener('click', () => {
  if (currentDocPage > 0) {
    openDocument(currentOpenDocId, null, currentDocPage - 1, docSearchInput.value.trim());
  }
});

nextPageBtn.addEventListener('click', () => {
  if (currentDocPage < currentDocTotalPages - 1) {
    openDocument(currentOpenDocId, null, currentDocPage + 1, docSearchInput.value.trim());
  }
});

docSearchBtn.addEventListener('click', () => {
  if (currentOpenDocId) {
    openDocument(currentOpenDocId, null, 0, docSearchInput.value.trim());
  }
});

docSearchInput.addEventListener('keyup', (e) => {
  if (e.key === 'Enter' && currentOpenDocId) {
    openDocument(currentOpenDocId, null, 0, docSearchInput.value.trim());
  }
});

historySearchInput.addEventListener('input', () => {
  loadUploadedFiles(historySearchInput.value.trim());
});

refreshHistoryBtn.addEventListener('click', () => {
  historySearchInput.value = '';
  loadUploadedFiles();
});

async function loadUploadedFiles(search = '') {
  const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
  const result = await callApi(`/api/v1/files/paginated?page=0&size=20${searchParam}`, 'GET');
  if (result.ok && result.payload.data) {
    const files = result.payload.data.content || [];
    if (files.length === 0) {
      recentFilesList.innerHTML = '<p class="hint">No uploaded files found.</p>';
      return;
    }

    let html = '';
    files.forEach(file => {
      const badgeColor = file.uploadStatus === 'COMPLETED' ? 'var(--color-success)' : 'var(--color-danger)';
      html += `
        <div class="file-item">
          <div class="file-info" onclick="openDocument('${file.id}', '${escapeHtml(file.originalFilename)}')" style="cursor: pointer; flex: 1;">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <div>
              <div class="file-name">${escapeHtml(file.originalFilename)}</div>
              <div class="file-sub">${(file.fileSize / 1024).toFixed(1)} KB • ${file.extractedRowCount} items • Status: <span style="color:${badgeColor}; font-weight:600;">${file.uploadStatus}</span></div>
            </div>
          </div>
          <div class="file-actions" style="display: flex; gap: 6px;">
            <button type="button" class="save" style="min-height: 32px; padding: 4px 10px; font-size: 12px; margin-top:0; width:auto;" onclick="openDocument('${file.id}', '${escapeHtml(file.originalFilename)}')">View</button>
            <button type="button" class="danger" style="min-height: 32px; padding: 4px 10px; font-size: 12px; margin-top:0; width:auto;" onclick="deleteUploadedFile('${file.id}')">Delete</button>
          </div>
        </div>
      `;
    });
    recentFilesList.innerHTML = html;
  }
}

async function deleteUploadedFile(id) {
  if (!confirm('Are you sure you want to delete this document? All stored extracted data will be removed.')) return;
  const result = await callApi(`/api/v1/files/${id}`, 'DELETE');
  if (result.ok) {
    showMsg('Document deleted successfully', false);
    if (currentOpenDocId === id) {
      documentViewerPanel.style.display = 'none';
      currentOpenDocId = null;
    }
    loadUploadedFiles();
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
}

function escapeHtml(text) {
  if (!text) return '';
  return text.toString()
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

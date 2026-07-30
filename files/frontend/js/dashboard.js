let currentPage = 0;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', async () => {
    await loadUserInfo();
    await loadFiles();
    document.getElementById('upload-form').addEventListener('submit', handleUpload);
});

async function loadUserInfo() {
    try {
        const user = await apiJson('/api/auth/me');
        document.getElementById('user-name').textContent = user.name;

        const actions = document.getElementById('auth-actions');
        if (user.loggedIn) {
            actions.innerHTML = '<a href="' + API_BASE + '/api/auth/logout" class="btn btn-secondary">Logout</a>';
        } else {
            actions.innerHTML = '<a href="' + API_BASE + '/oauth2/authorization/github" class="btn btn-secondary">Login</a>';
        }
    } catch (e) {
        document.getElementById('user-name').textContent = 'Guest';
    }
}

async function handleUpload(e) {
    e.preventDefault();
    const fileInput = document.getElementById('file-input');
    const status = document.getElementById('upload-status');

    if (!fileInput.files[0]) return;

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        status.innerHTML = 'Uploading...';
        await apiJson('/api/files/upload', { method: 'POST', body: formData });
        status.innerHTML = '<span class="success">File uploaded successfully!</span>';
        fileInput.value = '';
        currentPage = 0;
        await loadFiles();
    } catch (err) {
        status.innerHTML = '<span class="error">Upload failed: ' + err.message + '</span>';
    }
}

async function loadFiles() {
    try {
        const data = await apiJson('/api/files?page=' + currentPage + '&size=' + pageSize);
        renderFiles(data);
        renderPagination(data);
    } catch (e) {
        document.getElementById('files-list').innerHTML = '<p>Failed to load files.</p>';
    }
}

function renderFiles(data) {
    const container = document.getElementById('files-list');

    if (!data.content || data.content.length === 0) {
        container.innerHTML = '<p>No files uploaded yet.</p>';
        return;
    }

    let html = '<table><thead><tr>';
    html += '<th>Name</th><th>Type</th><th>Size</th><th>Uploaded</th><th>Actions</th>';
    html += '</tr></thead><tbody>';

    for (const file of data.content) {
        html += '<tr>';
        html += '<td><span class="file-link" onclick="viewFile(\'' + file.id + '\')">' + escapeHtml(file.fileName) + '</span></td>';
        html += '<td>' + escapeHtml(file.fileType) + '</td>';
        html += '<td>' + formatFileSize(file.fileSize) + '</td>';
        html += '<td>' + formatDate(file.uploadedAt) + '</td>';
        html += '<td class="file-actions">';
        html += '<a href="' + API_BASE + '/api/files/' + file.id + '/download" class="btn btn-primary" target="_blank">Download</a>';
        html += '<button onclick="deleteFile(\'' + file.id + '\')" class="btn btn-danger">Delete</button>';
        html += '</td></tr>';
    }

    html += '</tbody></table>';
    container.innerHTML = html;
}

function renderPagination(data) {
    const container = document.getElementById('pagination');
    if (data.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '<div class="pagination">';
    html += '<button onclick="goToPage(' + (currentPage - 1) + ')" ' + (currentPage === 0 ? 'disabled' : '') + '>Previous</button>';

    for (let i = 0; i < data.totalPages; i++) {
        html += '<button onclick="goToPage(' + i + ')" class="' + (i === currentPage ? 'active' : '') + '">' + (i + 1) + '</button>';
    }

    html += '<button onclick="goToPage(' + (currentPage + 1) + ')" ' + (currentPage >= data.totalPages - 1 ? 'disabled' : '') + '>Next</button>';
    html += '</div>';
    container.innerHTML = html;
}

function goToPage(page) {
    currentPage = page;
    loadFiles();
}

function viewFile(id) {
    window.location.href = './viewer.html?id=' + id;
}

async function deleteFile(id) {
    if (!confirm('Delete this file?')) return;
    try {
        await apiFetch('/api/files/' + id, { method: 'DELETE' });
        await loadFiles();
    } catch (e) {
        alert('Failed to delete file.');
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

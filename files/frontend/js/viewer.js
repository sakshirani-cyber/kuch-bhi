let currentPage = 0;
let fileId = null;

document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    fileId = params.get('id');

    if (!fileId) {
        document.getElementById('file-content').innerHTML = '<p>No file specified.</p>';
        return;
    }

    await loadPreview();
});

async function loadPreview() {
    try {
        const data = await apiJson('/api/files/' + fileId + '/preview?page=' + currentPage + '&size=100');

        document.getElementById('file-name').textContent = data.fileName || 'File Viewer';

        if (data.error) {
            document.getElementById('file-content').innerHTML = '<p class="error">' + escapeHtml(data.error) + '</p>';
            return;
        }

        if (data.type === 'table') {
            renderTable(data);
        } else if (data.type === 'text') {
            renderText(data);
        }
    } catch (e) {
        document.getElementById('file-content').innerHTML = '<p class="error">Failed to load file preview.</p>';
    }
}

function renderTable(data) {
    const container = document.getElementById('file-content');

    let html = '<table><thead><tr>';
    for (const header of data.headers) {
        html += '<th>' + escapeHtml(header) + '</th>';
    }
    html += '</tr></thead><tbody>';

    for (const row of data.rows) {
        html += '<tr>';
        for (const cell of row) {
            html += '<td>' + escapeHtml(cell) + '</td>';
        }
        html += '</tr>';
    }
    html += '</tbody></table>';
    html += '<p class="info-text">Page ' + (data.currentPage + 1) + ' of ' + data.totalPages + ' (' + data.totalRows + ' rows)</p>';

    container.innerHTML = html;
    renderContentPagination(data.totalPages, data.currentPage);
}

function renderText(data) {
    const container = document.getElementById('file-content');
    container.innerHTML = '<pre>' + escapeHtml(data.content) + '</pre>';
    container.innerHTML += '<p class="info-text">Page ' + (data.currentPage + 1) + ' of ' + data.totalPages + ' (' + data.totalLines + ' lines)</p>';
    renderContentPagination(data.totalPages, data.currentPage);
}

function renderContentPagination(totalPages, current) {
    const container = document.getElementById('content-pagination');
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '<div class="pagination">';
    html += '<button onclick="goToContentPage(' + (current - 1) + ')" ' + (current === 0 ? 'disabled' : '') + '>Previous</button>';

    const start = Math.max(0, current - 3);
    const end = Math.min(totalPages, current + 4);

    if (start > 0) {
        html += '<button onclick="goToContentPage(0)">1</button>';
        if (start > 1) html += '<span>...</span>';
    }

    for (let i = start; i < end; i++) {
        html += '<button onclick="goToContentPage(' + i + ')" class="' + (i === current ? 'active' : '') + '">' + (i + 1) + '</button>';
    }

    if (end < totalPages) {
        if (end < totalPages - 1) html += '<span>...</span>';
        html += '<button onclick="goToContentPage(' + (totalPages - 1) + ')">' + totalPages + '</button>';
    }

    html += '<button onclick="goToContentPage(' + (current + 1) + ')" ' + (current >= totalPages - 1 ? 'disabled' : '') + '>Next</button>';
    html += '</div>';
    container.innerHTML = html;
}

function goToContentPage(page) {
    currentPage = page;
    loadPreview();
}

function escapeHtml(text) {
    if (text == null) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

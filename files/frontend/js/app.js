// Shared utilities
const API_BASE = window.location.port === '8080' ? '' : 'http://localhost:8080';

async function apiFetch(url, options = {}) {
    const defaultOptions = {
        credentials: 'include'
    };
    const finalUrl = url.startsWith('http') ? url : API_BASE + url;
    const response = await fetch(finalUrl, { ...defaultOptions, ...options });
    if (!response.ok) {
        throw new Error('HTTP ' + response.status);
    }
    return response;
}

async function apiJson(url, options = {}) {
    const response = await apiFetch(url, options);
    return response.json();
}

function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString();
}

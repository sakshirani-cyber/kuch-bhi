const API_BASE = window.API_BASE || '';

function getUserId() {
    return sessionStorage.getItem('userId');
}

function setUserId(id) {
    sessionStorage.setItem('userId', String(id));
}

function clearUserId() {
    sessionStorage.removeItem('userId');
}

function getToken() {
    return sessionStorage.getItem('token');
}

function setToken(token) {
    sessionStorage.setItem('token', token);
}

function clearToken() {
    sessionStorage.removeItem('token');
}

function requireAuth() {
    const userId = getUserId();
    const token = getToken();
    if (!userId || !token) {
        window.location.href = '/login.html';
        return null;
    }
    return userId;
}

function resolveAuthenticatedUserId() {
    const userId = requireAuth();
    if (!userId) {
        return null;
    }

    const urlId = new URLSearchParams(window.location.search).get('id');
    if (urlId && urlId !== userId) {
        const path = window.location.pathname;
        window.location.replace(`${path}?id=${encodeURIComponent(userId)}`);
        return null;
    }

    return userId;
}

async function apiRequest(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const token = getToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(API_BASE + url, {
        headers,
        ...options
    });

    let data = null;
    if (response.status !== 204) {
        const text = await response.text();
        if (text) {
            data = JSON.parse(text);
        }
    }

    if (!response.ok) {
        if (response.status === 401 && getToken()) {
            clearUserId();
            clearToken();
            window.location.href = '/login.html';
            return;
        }
        throw { status: response.status, data };
    }

    return data;
}

function showMessage(elementId, text, isError = false) {
    const element = document.getElementById(elementId);
    if (!element) {
        return;
    }

    element.textContent = text;
    element.className = isError ? 'error' : 'message';
    element.hidden = !text;
}

function formatError(error) {
    if (!error?.data) {
        return 'Something went wrong. Please try again.';
    }

    const { message, fieldErrors } = error.data;

    if (fieldErrors && Object.keys(fieldErrors).length > 0) {
        return Object.values(fieldErrors).join(' ');
    }

    return message || 'Request failed.';
}

function logout() {
    clearUserId();
    clearToken();
    window.location.href = '/login.html';
}

const BASE_URL = "http://localhost:8080/api/v1/auth";

class ApiError extends Error {
    constructor(message, errors = {}) {
        super(message);
        this.errors = errors;
    }
}

function formatApiError(error) {
    const title = error.message || "Request failed";
    const errors = error.errors || {};
    const details = Object.values(errors).filter(Boolean);

    let html = `<div class="error"><div class="error-title">${title}</div>`;

    if (details.length > 0) {
        html += "<ul class=\"error-list\">";
        details.forEach((detail) => {
            html += `<li>${detail}</li>`;
        });
        html += "</ul>";
    }

    html += "</div>";
    return html;
}

async function parseResponse(response) {
    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text, errors: {} };
        }
    }

    if (!response.ok) {
        throw new ApiError(
            (data && data.message) || "Request failed",
            (data && data.errors) || {}
        );
    }

    return data;
}

async function signup(user) {
    const response = await fetch(`${BASE_URL}/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    });

    return parseResponse(response);
}

async function login(user) {
    const response = await fetch(`${BASE_URL}/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(user)
    });

    return parseResponse(response);
}

async function getUser(id) {
    const response = await fetch(`${BASE_URL}/${id}`);
    return parseResponse(response);
}

async function updateUsername(id, newUsername) {
    const response = await fetch(`${BASE_URL}/${id}/username`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            newUsername: newUsername
        })
    });

    return parseResponse(response);
}

async function updatePassword(id, newPassword) {
    const response = await fetch(`${BASE_URL}/${id}/password`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            newPassword: newPassword
        })
    });

    return parseResponse(response);
}

function logout() {
    localStorage.removeItem("userId");
    window.location.href = "login.html";
}

const BASE_URL = "http://localhost:8080/api/v1/auth";

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,20}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const CONTACT_PATTERN = /^\d{10}$/;

const VALIDATORS = {
    username(value) {
        const v = value.trim();
        if (!v) {
            return "Username is mandatory";
        }
        if (v.length < 3 || v.length > 50) {
            return "Username must be between 3 and 50 characters";
        }
        return "";
    },
    email(value) {
        const v = value.trim();
        if (!v) {
            return "Email is mandatory";
        }
        if (v.length > 100) {
            return "Email must be at most 100 characters";
        }
        if (!EMAIL_PATTERN.test(v)) {
            return "Invalid email format";
        }
        return "";
    },
    contactNumber(value) {
        const v = value.trim();
        if (!v) {
            return "Contact number is mandatory";
        }
        if (!CONTACT_PATTERN.test(v)) {
            return "Contact number must be exactly 10 digits";
        }
        return "";
    },
    dateOfBirth(value) {
        if (!value) {
            return "Date of birth is mandatory";
        }
        const dob = new Date(value + "T00:00:00");
        if (Number.isNaN(dob.getTime())) {
            return "Invalid date of birth";
        }
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (dob >= today) {
            return "Date of birth must be in the past";
        }
        return "";
    },
    password(value) {
        if (!value) {
            return "Password is mandatory";
        }
        if (value.length < 8 || value.length > 20) {
            return "Password must be 8-20 characters";
        }
        if (!PASSWORD_PATTERN.test(value)) {
            return "Password must include uppercase, lowercase, number and special character";
        }
        return "";
    },
    currentPassword(value) {
        if (!value) {
            return "Current password is mandatory";
        }
        if (value.length < 8 || value.length > 20) {
            return "Current password must be 8-20 characters";
        }
        if (!PASSWORD_PATTERN.test(value)) {
            return "Current password must include uppercase, lowercase, number and special character";
        }
        return "";
    },
    newPassword(value) {
        if (!value) {
            return "New password is mandatory";
        }
        if (value.length < 8 || value.length > 20) {
            return "New password must be 8-20 characters";
        }
        if (!PASSWORD_PATTERN.test(value)) {
            return "New password must include uppercase, lowercase, number and special character";
        }
        return "";
    },
    currentUsername(value) {
        const v = value.trim();
        if (!v) {
            return "Current username is mandatory";
        }
        if (v.length < 3 || v.length > 50) {
            return "Current username must be between 3 and 50 characters";
        }
        return "";
    },
    newUsername(value) {
        const v = value.trim();
        if (!v) {
            return "New username is mandatory";
        }
        if (v.length < 3 || v.length > 50) {
            return "New username must be between 3 and 50 characters";
        }
        return "";
    }
};

class ApiError extends Error {
    constructor(message, errors = {}) {
        super(message);
        this.errors = errors;
    }
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatApiError(error) {
    const title = escapeHtml(error.message || "Request failed");
    const errors = error.errors || {};
    const details = Object.values(errors).filter(Boolean);

    let html = `<div class="error"><div class="error-title">${title}</div>`;

    if (details.length > 0) {
        html += "<ul class=\"error-list\">";
        details.forEach((detail) => {
            html += `<li>${escapeHtml(detail)}</li>`;
        });
        html += "</ul>";
    }

    html += "</div>";
    return html;
}

function setFieldValidity(fieldId, errorMessage) {
    const field = document.getElementById(fieldId);
    const errorEl = document.getElementById(`${fieldId}Error`);
    if (!field) {
        return !errorMessage;
    }

    if (errorMessage) {
        field.classList.add("invalid");
        field.setAttribute("aria-invalid", "true");
        if (errorEl) {
            errorEl.textContent = errorMessage;
        }
        return false;
    }

    field.classList.remove("invalid");
    field.setAttribute("aria-invalid", "false");
    if (errorEl) {
        errorEl.textContent = "";
    }
    return true;
}

function validateField(fieldId, validatorName) {
    const field = document.getElementById(fieldId);
    const validator = VALIDATORS[validatorName];
    if (!field || !validator) {
        return false;
    }
    const message = validator(field.value);
    return setFieldValidity(fieldId, message);
}

/**
 * Enables submit only when every listed field passes backend-aligned validation.
 * fieldSpecs: [{ id: "email", validator: "email" }, ...]
 */
function bindValidatedSubmit(form, button, fieldSpecs) {
    function refreshSubmitState() {
        const allValid = fieldSpecs.every((spec) => {
            const field = document.getElementById(spec.id);
            const validator = VALIDATORS[spec.validator];
            if (!field || !validator) {
                return false;
            }

            const isEmpty = field.type === "password"
                ? field.value === ""
                : field.value.trim() === "";

            // Empty: keep submit disabled, hide error until user types or blurs
            if (isEmpty) {
                setFieldValidity(spec.id, "");
                return false;
            }

            return setFieldValidity(spec.id, validator(field.value));
        });
        button.disabled = !allValid;
    }

    fieldSpecs.forEach((spec) => {
        const field = document.getElementById(spec.id);
        if (!field) {
            return;
        }
        field.addEventListener("input", refreshSubmitState);
        field.addEventListener("change", refreshSubmitState);
        field.addEventListener("blur", () => {
            validateField(spec.id, spec.validator);
            refreshSubmitState();
        });
    });

    refreshSubmitState();
    return refreshSubmitState;
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

/**
 * Supports ApiResponse envelope ({ status, message, errors, data })
 * and legacy flat UserResponse ({ userId, userName, userEmail, ... }).
 */
function unwrapUser(response) {
    if (!response || typeof response !== "object") {
        return null;
    }
    if (response.data && typeof response.data === "object") {
        return response.data;
    }
    if (response.userId != null || response.userEmail) {
        return response;
    }
    return null;
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

    const envelope = await parseResponse(response);
    const userData = unwrapUser(envelope);
    if (!userData || userData.userId == null) {
        throw new ApiError(
            (envelope && envelope.message) || "Login response missing user details",
            (envelope && envelope.errors && Object.keys(envelope.errors).length)
                ? envelope.errors
                : { userId: "User id not found in login response" }
        );
    }
    return { envelope: envelope, user: userData };
}

async function getUserByEmail(email) {
    const response = await fetch(`${BASE_URL}/user/${encodeURIComponent(email)}`);
    const envelope = await parseResponse(response);
    const userData = unwrapUser(envelope);
    if (!userData || userData.userId == null) {
        throw new ApiError(
            (envelope && envelope.message) || "User response missing details",
            (envelope && envelope.errors && Object.keys(envelope.errors).length)
                ? envelope.errors
                : { userId: "User id not found in user response" }
        );
    }
    return { envelope: envelope, user: userData };
}

async function updateUsername(email, currentUsername, newUsername) {
    const response = await fetch(`${BASE_URL}/update-username/${encodeURIComponent(email)}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            currentUsername: currentUsername,
            newUsername: newUsername
        })
    });

    return parseResponse(response);
}

async function updatePassword(email, currentPassword, newPassword) {
    const response = await fetch(`${BASE_URL}/update-password/${encodeURIComponent(email)}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            currentPassword: currentPassword,
            newPassword: newPassword
        })
    });

    return parseResponse(response);
}

async function deleteUser(email, password) {
    const response = await fetch(`${BASE_URL}/delete-user/${encodeURIComponent(email)}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            password: password
        })
    });

    return parseResponse(response);
}

function logout() {
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userEmail");
    window.location.href = "login.html";
}

function clearSession() {
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userEmail");
}

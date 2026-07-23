const form = document.getElementById("usernameForm");
const message = document.getElementById("message");
const button = document.getElementById("updateBtn");
const userEmail = localStorage.getItem("userEmail");

if (!userEmail) {
    window.location.href = "login.html";
}

const currentUsernameInput = document.getElementById("currentUsername");
const savedUsername = localStorage.getItem("userName");
if (savedUsername) {
    currentUsernameInput.value = savedUsername;
}

const refreshSubmitState = bindValidatedSubmit(form, button, [
    { id: "currentUsername", validator: "currentUsername" },
    { id: "newUsername", validator: "newUsername" }
]);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (button.disabled) {
        return;
    }

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Updating...";

    const currentUsername = document.getElementById("currentUsername").value.trim();
    const newUsername = document.getElementById("newUsername").value.trim();

    try {
        const response = await updateUsername(userEmail, currentUsername, newUsername);

        localStorage.setItem("userName", newUsername);
        message.innerHTML = `<div class="success">${escapeHtml(response.message || "Username Updated Successfully.")}</div>`;

        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 1200);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Update Username";
        refreshSubmitState();
    }
});

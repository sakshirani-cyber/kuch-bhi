const form = document.getElementById("passwordForm");
const message = document.getElementById("message");
const button = document.getElementById("updateBtn");
const userEmail = localStorage.getItem("userEmail");

if (!userEmail) {
    window.location.href = "login.html";
}

const refreshSubmitState = bindValidatedSubmit(form, button, [
    { id: "currentPassword", validator: "currentPassword" },
    { id: "newPassword", validator: "newPassword" }
]);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (button.disabled) {
        return;
    }

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Updating...";

    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;

    try {
        const response = await updatePassword(userEmail, currentPassword, newPassword);

        message.innerHTML = `<div class="success">${escapeHtml(response.message || "Password Updated Successfully.")}</div>`;

        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 1200);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Update Password";
        refreshSubmitState();
    }
});

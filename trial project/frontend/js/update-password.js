const form = document.getElementById("passwordForm");
const message = document.getElementById("message");
const button = document.getElementById("updateBtn");
const userId = localStorage.getItem("userId");

if (!userId) {
    window.location.href = "login.html";
}

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Updating...";

    const password = document.getElementById("password").value.trim();

    try {
        const response = await updatePassword(userId, password);

        message.innerHTML = `<div class="success">${response.message || "Password Updated Successfully."}</div>`;

        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 1200);
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }

    button.disabled = false;
    button.innerHTML = "Update Password";
});

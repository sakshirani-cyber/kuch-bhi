const form = document.getElementById("usernameForm");
const message = document.getElementById("message");
const button = document.getElementById("updateBtn");
const userId = localStorage.getItem("userId");

if (!userId) {
    window.location.href = "login.html";
}

const refreshSubmitState = bindRequiredSubmit(form, button, ["username"]);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (button.disabled) {
        return;
    }

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Updating...";

    const username = document.getElementById("username").value.trim();

    try {
        const response = await updateUsername(userId, username);

        message.innerHTML = `<div class="success">${response.message || "Username Updated Successfully."}</div>`;

        setTimeout(() => {
            window.location.href = "dashboard.html";
        }, 1200);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Update Username";
        refreshSubmitState();
    }
});

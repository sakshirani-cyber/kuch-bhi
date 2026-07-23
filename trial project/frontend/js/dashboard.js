const userEmail = localStorage.getItem("userEmail");
const message = document.getElementById("message");
const deleteBtn = document.getElementById("deleteAccountBtn");

if (!userEmail) {
    window.location.href = "login.html";
}

loadUser();

async function loadUser() {
    try {
        const { user } = await getUserByEmail(userEmail);

        document.getElementById("userId").textContent = user.userId;
        document.getElementById("username").textContent = user.userName;
        document.getElementById("email").textContent = user.userEmail;
        document.getElementById("contactNumber").textContent = user.contactNumber || "-";
        document.getElementById("dateOfBirth").textContent = user.dateOfBirth || "-";
        document.getElementById("age").textContent = user.age != null ? user.age : "-";

        localStorage.setItem("userName", user.userName || "");
        localStorage.setItem("userId", String(user.userId));
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }
}

deleteBtn.addEventListener("click", async function () {
    const confirmed = window.confirm(
        "Are you sure you want to permanently delete your account?"
    );
    if (!confirmed) {
        return;
    }

    const password = window.prompt("Enter your password to confirm deletion:");
    if (!password) {
        message.innerHTML = `<div class="error">Password is required to delete account.</div>`;
        return;
    }

    const passwordError = VALIDATORS.password(password);
    if (passwordError) {
        message.innerHTML = `<div class="error"><div class="error-title">Invalid password</div><ul class="error-list"><li>${escapeHtml(passwordError)}</li></ul></div>`;
        return;
    }

    deleteBtn.disabled = true;
    deleteBtn.textContent = "Deleting...";

    try {
        await deleteUser(userEmail, password);
        clearSession();
        window.location.replace("login.html");
    } catch (error) {
        message.innerHTML = formatApiError(error);
        deleteBtn.disabled = false;
        deleteBtn.textContent = "Delete Account";
    }
});

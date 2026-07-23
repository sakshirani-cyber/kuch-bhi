const form = document.getElementById("loginForm");
const message = document.getElementById("message");
const button = document.getElementById("loginBtn");
const refreshSubmitState = bindValidatedSubmit(form, button, [
    { id: "email", validator: "email" },
    { id: "password", validator: "password" }
]);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (button.disabled) {
        return;
    }

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Logging In...";

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    try {
        const { user } = await login({
            email: email,
            password: password
        });

        localStorage.setItem("userId", String(user.userId));
        localStorage.setItem("userName", user.userName || "");
        localStorage.setItem("userEmail", user.userEmail || email);
        window.location.href = "dashboard.html";
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Login";
        refreshSubmitState();
    }
});

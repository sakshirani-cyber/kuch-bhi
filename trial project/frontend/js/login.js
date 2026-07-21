const form = document.getElementById("loginForm");
const message = document.getElementById("message");
const button = document.getElementById("loginBtn");

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Logging In...";

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    try {
        const response = await login({
            email: email,
            password: password
        });

        localStorage.setItem("userId", response.userId);
        window.location.href = "dashboard.html";
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }

    button.disabled = false;
    button.innerHTML = "Login";
});

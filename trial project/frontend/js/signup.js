const form = document.getElementById("signupForm");
const message = document.getElementById("message");
const button = document.getElementById("signupBtn");
const refreshSubmitState = bindRequiredSubmit(
    form,
    button,
    ["username", "email", "contactNumber", "dateOfBirth", "password"]
);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (button.disabled) {
        return;
    }

    message.innerHTML = "";
    button.disabled = true;
    button.innerHTML = "Creating Account...";

    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const contactNumber = document.getElementById("contactNumber").value.trim();
    const dateOfBirth = document.getElementById("dateOfBirth").value;
    const password = document.getElementById("password").value.trim();

    try {
        const response = await signup({
            username: username,
            email: email,
            contactNumber: contactNumber,
            dateOfBirth: dateOfBirth,
            password: password
        });

        message.innerHTML = `<div class="success">${response.message || "Account Created Successfully!"}</div>`;

        setTimeout(() => {
            window.location.href = "login.html";
        }, 1500);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Create Account";
        refreshSubmitState();
    }
});

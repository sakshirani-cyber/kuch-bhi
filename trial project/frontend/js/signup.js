const form = document.getElementById("signupForm");
const message = document.getElementById("message");
const button = document.getElementById("signupBtn");

const dobInput = document.getElementById("dateOfBirth");
const yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
dobInput.max = yesterday.toISOString().slice(0, 10);

const contactInput = document.getElementById("contactNumber");
contactInput.addEventListener("input", function () {
    this.value = this.value.replace(/\D/g, "").slice(0, 10);
});

const refreshSubmitState = bindValidatedSubmit(form, button, [
    { id: "username", validator: "username" },
    { id: "email", validator: "email" },
    { id: "contactNumber", validator: "contactNumber" },
    { id: "dateOfBirth", validator: "dateOfBirth" },
    { id: "password", validator: "password" }
]);

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
    const password = document.getElementById("password").value;

    try {
        const response = await signup({
            username: username,
            email: email,
            contactNumber: contactNumber,
            dateOfBirth: dateOfBirth,
            password: password
        });

        message.innerHTML = `<div class="success">${escapeHtml(response.message || "Account Created Successfully!")}</div>`;

        setTimeout(() => {
            window.location.href = "login.html";
        }, 1500);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        button.innerHTML = "Create Account";
        refreshSubmitState();
    }
});

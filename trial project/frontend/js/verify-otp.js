const form = document.getElementById("otpForm");
const message = document.getElementById("message");
const verifyBtn = document.getElementById("verifyBtn");
const resendBtn = document.getElementById("resendBtn");
const emailInput = document.getElementById("email");
const otpInput = document.getElementById("otp");

const pendingEmail = sessionStorage.getItem("pendingVerificationEmail");
if (pendingEmail) {
    emailInput.value = pendingEmail;
}

otpInput.addEventListener("input", function () {
    this.value = this.value.replace(/\D/g, "").slice(0, 6);
});

const refreshSubmitState = bindValidatedSubmit(form, verifyBtn, [
    { id: "email", validator: "email" },
    { id: "otp", validator: "otp" }
]);

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    if (verifyBtn.disabled) {
        return;
    }

    message.innerHTML = "";
    verifyBtn.disabled = true;
    verifyBtn.innerHTML = "Verifying...";

    const email = emailInput.value.trim();
    const otp = otpInput.value.trim();

    try {
        const response = await verifyOtp(email, otp);
        sessionStorage.removeItem("pendingVerificationEmail");
        message.innerHTML = `<div class="success">${escapeHtml(response.message || "Email verified successfully")}</div>`;
        setTimeout(() => {
            window.location.href = "login.html";
        }, 1200);
    } catch (error) {
        message.innerHTML = formatApiError(error);
        verifyBtn.innerHTML = "Verify OTP";
        refreshSubmitState();
    }
});

resendBtn.addEventListener("click", async function () {
    message.innerHTML = "";
    const email = emailInput.value.trim();
    const emailError = VALIDATORS.email(email);
    if (emailError) {
        setFieldValidity("email", emailError);
        message.innerHTML = `<div class="error">${escapeHtml(emailError)}</div>`;
        return;
    }

    resendBtn.disabled = true;
    resendBtn.textContent = "Sending...";

    try {
        const response = await resendOtp(email);
        sessionStorage.setItem("pendingVerificationEmail", email);
        message.innerHTML = `<div class="success">${escapeHtml(response.message || "OTP resent successfully")}</div>`;
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }

    resendBtn.disabled = false;
    resendBtn.textContent = "Resend OTP";
});

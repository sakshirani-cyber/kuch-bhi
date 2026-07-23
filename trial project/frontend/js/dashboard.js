const userId = localStorage.getItem("userId");
const message = document.getElementById("message");

if (!userId) {
    window.location.href = "login.html";
}

loadUser();

async function loadUser() {
    try {
        const user = await getUser(userId);

        document.getElementById("userId").textContent = user.userId;
        document.getElementById("username").textContent = user.userName;
        document.getElementById("email").textContent = user.userEmail;
        document.getElementById("contactNumber").textContent = user.contactNumber || "-";
        document.getElementById("dateOfBirth").textContent = user.dateOfBirth || "-";
        document.getElementById("age").textContent = user.age != null ? user.age : "-";
    } catch (error) {
        message.innerHTML = formatApiError(error);
    }
}

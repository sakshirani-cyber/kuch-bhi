const storedUsername = sessionStorage.getItem('username');
if (!storedUsername) {
  window.location.href = 'index.html';
}

const currentUsernameLabel = document.getElementById('currentUsernameLabel');
currentUsernameLabel.textContent = storedUsername;

const msg = document.getElementById('msg');
function showMsg(text, isError) {
  msg.textContent = text;
  msg.className = 'msg ' + (isError ? 'error' : 'success');
}

const currentPasswordInput = document.getElementById('currentPassword');

function wireUpdateRow({ inputId, buttonId, endpoint, buildBody, successLabel }) {
  const input = document.getElementById(inputId);
  const button = document.getElementById(buttonId);

  function refreshButtonState() {
    const hasCurrentPassword = currentPasswordInput.value.length > 0;
    const hasValidNewValue = input.value.trim() !== '' && input.checkValidity();
    button.disabled = !(hasCurrentPassword && hasValidNewValue);
  }

  input.addEventListener('input', refreshButtonState);
  currentPasswordInput.addEventListener('input', refreshButtonState);

  button.addEventListener('click', async () => {
    const username = sessionStorage.getItem('username');
    const body = buildBody(username, currentPasswordInput.value, input.value.trim());

    const result = await callApi(endpoint, 'PUT', body);

    if (result.ok) {
      showMsg(successLabel, false);

      if (inputId === 'newUsername') {
        sessionStorage.setItem('username', input.value.trim());
        currentUsernameLabel.textContent = input.value.trim();
      }

      input.value = '';
      button.disabled = true;
    } else {
      showMsg(extractErrorText(result.payload), true);
    }
  });
}

wireUpdateRow({
  inputId: 'newUsername',
  buttonId: 'updateUsernameBtn',
  endpoint: `${API_BASE}/update/username`,
  buildBody: (username, currentPassword, newUsername) => ({
    currentUsername: username,
    currentPassword,
    newUsername
  }),
  successLabel: 'Username updated successfully'
});

wireUpdateRow({
  inputId: 'newPassword',
  buttonId: 'updatePasswordBtn',
  endpoint: `${API_BASE}/update/password`,
  buildBody: (username, currentPassword, newPassword) => ({
    currentUsername: username,
    currentPassword,
    newPassword
  }),
  successLabel: 'Password updated successfully'
});

wireUpdateRow({
  inputId: 'newEmail',
  buttonId: 'updateEmailBtn',
  endpoint: `${API_BASE}/update/email`,
  buildBody: (username, currentPassword, newEmail) => ({
    currentUsername: username,
    currentPassword,
    newEmail
  }),
  successLabel: 'Email updated successfully'
});

wireUpdateRow({
  inputId: 'newPhoneNumber',
  buttonId: 'updatePhoneBtn',
  endpoint: `${API_BASE}/update/phone`,
  buildBody: (username, currentPassword, newPhoneNumber) => ({
    currentUsername: username,
    currentPassword,
    newPhoneNumber
  }),
  successLabel: 'Phone number updated successfully'
});

const deleteBtn = document.getElementById('deleteBtn');
currentPasswordInput.addEventListener('input', () => {
  deleteBtn.disabled = currentPasswordInput.value.length === 0;
});

deleteBtn.addEventListener('click', async () => {
  const confirmed = window.confirm(
    'This will permanently delete your account. This cannot be undone. Continue?'
  );
  if (!confirmed) {
    return;
  }

  const username = sessionStorage.getItem('username');
  const result = await callApi(`${API_BASE}/delete`, 'DELETE', {
    username,
    password: currentPasswordInput.value
  });

  if (result.ok) {
    sessionStorage.removeItem('username');
    alert('Your account has been deleted.');
    window.location.href = 'index.html';
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  sessionStorage.removeItem('username');
  window.location.href = 'index.html';
});

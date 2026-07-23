const tabLogin = document.getElementById('tabLogin');
const tabSignup = document.getElementById('tabSignup');
const loginForm = document.getElementById('loginForm');
const signupForm = document.getElementById('signupForm');
const loginSubmit = document.getElementById('loginSubmit');
const signupSubmit = document.getElementById('signupSubmit');
const msg = document.getElementById('msg');

function showMsg(text, isError) {
  msg.textContent = text;
  msg.className = 'msg ' + (isError ? 'error' : 'success');
}

wireUpValidation(loginForm, loginSubmit);
wireUpValidation(signupForm, signupSubmit);

tabLogin.addEventListener('click', () => {
  tabLogin.classList.add('active');
  tabSignup.classList.remove('active');
  loginForm.classList.add('active');
  signupForm.classList.remove('active');
  showMsg('', false);
});

tabSignup.addEventListener('click', () => {
  tabSignup.classList.add('active');
  tabLogin.classList.remove('active');
  signupForm.classList.add('active');
  loginForm.classList.remove('active');
  showMsg('', false);
});

loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;

  const result = await callApi(`${API_BASE}/login`, 'POST', { username, password });

  if (result.ok) {
    sessionStorage.setItem('username', username);
    window.location.href = 'dashboard.html';
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

signupForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('signupUsername').value.trim();
  const password = document.getElementById('signupPassword').value;
  const email = document.getElementById('signupEmail').value.trim();
  const phoneNumber = document.getElementById('signupPhone').value.trim();
  const dateOfBirth = document.getElementById('signupDob').value;

  const result = await callApi(`${API_BASE}/signup`, 'POST', {
    username, password, email, phoneNumber, dateOfBirth
  });

  if (result.ok) {
    showMsg('Account created! You can log in now.', false);
    tabLogin.click();
    document.getElementById('loginUsername').value = username;
    loginForm.dispatchEvent(new Event('input'));
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

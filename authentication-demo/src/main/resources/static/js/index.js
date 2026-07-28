const tabLogin = document.getElementById('tabLogin');
const tabSignup = document.getElementById('tabSignup');
const tabOtp = document.getElementById('tabOtp');

const loginForm = document.getElementById('loginForm');
const signupForm = document.getElementById('signupForm');
const otpForm = document.getElementById('otpForm');

const loginSubmit = document.getElementById('loginSubmit');
const signupSubmit = document.getElementById('signupSubmit');
const otpSubmit = document.getElementById('otpSubmit');
const otpResendBtn = document.getElementById('otpResendBtn');
const msg = document.getElementById('msg');

function showMsg(text, isError) {
  msg.textContent = text;
  msg.className = 'msg ' + (isError ? 'error' : 'success');
}

function activateTab(tabToActivate, formToActivate) {
  [tabLogin, tabSignup, tabOtp].forEach(tab => tab.classList.remove('active'));
  [loginForm, signupForm, otpForm].forEach(form => form.classList.remove('active'));
  
  tabToActivate.classList.add('active');
  formToActivate.classList.add('active');
}

wireUpValidation(loginForm, loginSubmit);
wireUpValidation(signupForm, signupSubmit);
wireUpValidation(otpForm, otpSubmit);

tabLogin.addEventListener('click', () => {
  activateTab(tabLogin, loginForm);
  showMsg('', false);
});

tabSignup.addEventListener('click', () => {
  activateTab(tabSignup, signupForm);
  showMsg('', false);
});

tabOtp.addEventListener('click', () => {
  activateTab(tabOtp, otpForm);
  showMsg('', false);
});

loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;

  setLoading(loginSubmit, true);
  const result = await callApi(`${API_BASE}/login`, 'POST', { username, password });
  setLoading(loginSubmit, false);

  if (result.ok) {
    sessionStorage.setItem('username', username);
    window.location.href = 'dashboard.html';
  } else {
    const errorText = extractErrorText(result.payload);
    showMsg(errorText, true);

    if (errorText.toLowerCase().includes('not verified')) {
      setTimeout(() => {
        activateTab(tabOtp, otpForm);
        showMsg('Account not verified. Enter your 6-digit OTP code below (check server console output).', true);
      }, 1200);
    }
  }
});

signupForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('signupUsername').value.trim();
  const password = document.getElementById('signupPassword').value;
  const email = document.getElementById('signupEmail').value.trim();
  const phoneNumber = document.getElementById('signupPhone').value.trim();
  const dateOfBirth = document.getElementById('signupDob').value;

  setLoading(signupSubmit, true);
  const result = await callApi(`${API_BASE}/signup`, 'POST', {
    username, password, email, phoneNumber, dateOfBirth
  });
  setLoading(signupSubmit, false);

  if (result.ok) {
    document.getElementById('otpEmail').value = email;
    otpForm.dispatchEvent(new Event('input'));
    
    activateTab(tabOtp, otpForm);
    showMsg('Account registered! Enter the 6-digit OTP code sent to your email (check server console log).', false);
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

otpForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const email = document.getElementById('otpEmail').value.trim();
  const otp = document.getElementById('otpCode').value.trim();

  setLoading(otpSubmit, true);
  const result = await callApi(`${API_BASE}/verify-otp`, 'POST', { email, otp });
  setLoading(otpSubmit, false);

  if (result.ok) {
    showMsg('OTP verified successfully! You can now log in.', false);
    document.getElementById('otpCode').value = '';
    activateTab(tabLogin, loginForm);
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

otpResendBtn.addEventListener('click', async () => {
  const email = document.getElementById('otpEmail').value.trim();
  if (!email) {
    showMsg('Please enter your email address to resend OTP.', true);
    return;
  }

  setLoading(otpResendBtn, true);
  const result = await callApi(`${API_BASE}/resend-otp`, 'POST', { email });
  setLoading(otpResendBtn, false);

  if (result.ok) {
    showMsg('New OTP sent successfully! Check your server console log.', false);
  } else {
    showMsg(extractErrorText(result.payload), true);
  }
});

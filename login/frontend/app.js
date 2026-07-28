const API = 'http://localhost:8080/api/auth';
let currentUser = null;
let pendingOtpEmail = null;
let otpTimerInterval = null;
let otpTimeRemaining = 300;
let otpTriesRemaining = 3;
let isEmailVerified = false;

function getToken() { return localStorage.getItem('accessToken'); }
function setToken(t) { localStorage.setItem('accessToken', t); }
function clearToken() { 
    localStorage.removeItem('accessToken'); 
    localStorage.removeItem('currentUser');
    currentUser = null;
}
function saveUser(u) {
    currentUser = u;
    if (u) {
        localStorage.setItem('currentUser', JSON.stringify(u));
    } else {
        localStorage.removeItem('currentUser');
    }
}

// UI Helpers
function showScreen(id) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    const screen = document.getElementById(id);
    if (screen) {
        screen.classList.add('active');
    }
    clearAlerts();
    
    const appEl = document.querySelector('.app');
    if (id === 'screen-dashboard' || id === 'screen-edit-profile' || id === 'screen-signup') {
        appEl.classList.add('wide');
    } else {
        appEl.classList.remove('wide');
    }
}

function showAlert(id, message, type = 'error') {
    const el = document.getElementById(id);
    if (!el) return;
    if (typeof message === 'object' && message !== null) {
        const errorList = Object.values(message);
        if (errorList.length > 1) {
            el.innerHTML = '<ul style="margin: 0; padding-left: 18px; text-align: left;">' + 
                           errorList.map(err => `<li>${err}</li>`).join('') + 
                           '</ul>';
        } else if (errorList.length === 1) {
            el.textContent = errorList[0];
        } else {
            el.textContent = 'Validation failed.';
        }
    } else {
        el.textContent = message;
    }
    el.className = `alert alert-${type} show`;
}

function clearAlerts() {
    document.querySelectorAll('.alert').forEach(el => {
        el.className = 'alert';
        el.textContent = '';
        el.innerHTML = '';
    });
}

function setLoading(btnId, loading) {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.disabled = loading;
    btn.classList.toggle('loading', loading);
}

// Auth Handlers
async function handleLogin(e) {
    e.preventDefault();
    const identifier = document.getElementById('login-identifier').value.trim();
    const password   = document.getElementById('login-password').value;

    setLoading('login-btn', true);
    try {
        const res = await fetch(`${API}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier, password })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            saveUser(data.user);
            if (data.accessToken) setToken(data.accessToken);
            populateDashboard();
            showScreen('screen-dashboard');
        } else if (res.status === 404) {
            showAlert('login-alert', 'No account found. Redirecting to sign up...', 'info');
            setTimeout(() => {
                showScreen('screen-signup');
                document.getElementById('signup-username').value = identifier;
            }, 1500);
        } else {
            const errPayload = data.errors || data.message || 'Incorrect credentials.';
            showAlert('login-alert', errPayload);
        }
    } catch {
        showAlert('login-alert', 'Cannot reach server.');
    } finally {
        setLoading('login-btn', false);
    }
}

async function handleSignup(e) {
    e.preventDefault();
    const username = document.getElementById('signup-username').value.trim();
    const firstName = document.getElementById('signup-firstName').value.trim();
    const lastName = document.getElementById('signup-lastName').value.trim();
    const gender = document.getElementById('signup-gender').value.trim();
    const email = document.getElementById('signup-email').value.trim();
    const password = document.getElementById('signup-password').value;
    const confirmPassword = document.getElementById('signup-confirmPassword').value;

    if (password !== confirmPassword) {
        showAlert('signup-alert', 'Password and confirm password do not match');
        return;
    }

    const contactNumber = document.getElementById('signup-contact').value.trim();
    const dob = document.getElementById('signup-dob').value;
    const address = document.getElementById('signup-address').value.trim();
    const collegeName = document.getElementById('signup-college').value.trim();
    const schoolName = document.getElementById('signup-school').value.trim();
    const currentCompany = document.getElementById('signup-company').value.trim();

    // Step 1: If email is not yet verified, request OTP and open verification dialogue
    if (!isEmailVerified) {
        setLoading('signup-btn', true);
        try {
            const res = await fetch(`${API}/send-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email })
            });
            const data = await res.json();

            if (res.ok && data.success) {
                showAlert('signup-alert', 'OTP sent to your email! Please enter it in the verification popup.', 'info');
                openOtpModal(email);
            } else {
                const errPayload = data.errors || data.message || 'Failed to send OTP.';
                showAlert('signup-alert', errPayload);
            }
        } catch {
            showAlert('signup-alert', 'Cannot reach server.');
        } finally {
            setLoading('signup-btn', false);
        }
        return;
    }

    // Step 2: Email is verified, submit final registration
    setLoading('signup-btn', true);
    try {
        const res = await fetch(`${API}/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, firstName, lastName, gender, email, password, confirmPassword, contactNumber, dob, address, collegeName, schoolName, currentCompany })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            saveUser(data.user);
            if (data.accessToken) setToken(data.accessToken);
            populateDashboard();
            showScreen('screen-dashboard');
            resetSignupFormState();
        } else {
            const errPayload = data.errors || data.message || 'Something went wrong during account creation.';
            showAlert('signup-alert', errPayload);
        }
    } catch {
        showAlert('signup-alert', 'Cannot reach server.');
    } finally {
        setLoading('signup-btn', false);
    }
}

function resetSignupFormState() {
    isEmailVerified = false;
    document.getElementById('signup-form').reset();
    document.getElementById('signup-email').readOnly = false;
    document.getElementById('email-verified-badge').classList.remove('show');
    document.getElementById('signup-btn-text').textContent = 'Verify Email';
}

// OTP Verification Modal Flow
function openOtpModal(email) {
    pendingOtpEmail = email;
    otpTriesRemaining = 3;
    
    document.getElementById('otp-modal').classList.add('active');
    document.getElementById('otp-target-email').textContent = email;
    document.getElementById('otp-input').value = '';
    document.getElementById('otp-tries-left').textContent = '3/3';
    clearAlerts();

    startOtpTimer(300); // 5 minutes
}

function closeOtpModal() {
    document.getElementById('otp-modal').classList.remove('active');
    if (otpTimerInterval) {
        clearInterval(otpTimerInterval);
        otpTimerInterval = null;
    }
}

function startOtpTimer(seconds) {
    if (otpTimerInterval) {
        clearInterval(otpTimerInterval);
    }
    otpTimeRemaining = seconds;
    updateOtpTimerDisplay();

    otpTimerInterval = setInterval(() => {
        otpTimeRemaining--;
        updateOtpTimerDisplay();
        if (otpTimeRemaining <= 0) {
            clearInterval(otpTimerInterval);
            otpTimerInterval = null;
            document.getElementById('otp-timer').textContent = 'Expired';
            showAlert('otp-alert', 'OTP has expired. Please click Resend OTP.');
        }
    }, 1000);
}

function updateOtpTimerDisplay() {
    const mins = Math.floor(Math.max(0, otpTimeRemaining) / 60);
    const secs = Math.max(0, otpTimeRemaining) % 60;
    const formatted = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    document.getElementById('otp-timer').textContent = formatted;
}

async function handleVerifyOtp() {
    const otpInput = document.getElementById('otp-input').value.trim();
    if (!otpInput || otpInput.length !== 6) {
        showAlert('otp-alert', 'Please enter a valid 6-digit OTP code.');
        return;
    }

    if (!pendingOtpEmail) {
        showAlert('otp-alert', 'Missing email address for verification.');
        return;
    }

    setLoading('verify-otp-btn', true);
    try {
        const res = await fetch(`${API}/verify-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingOtpEmail, otp: otpInput })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            isEmailVerified = true;
            closeOtpModal();

            // Lock email input and update signup UI to "Create Account"
            document.getElementById('signup-email').readOnly = true;
            document.getElementById('email-verified-badge').classList.add('show');
            document.getElementById('signup-btn-text').textContent = 'Create Account';

            showAlert('signup-alert', 'Email verified successfully! Click Create Account to complete registration.', 'success');
        } else {
            const errPayload = data.errors || data.message || 'Invalid OTP code.';
            if (otpTriesRemaining > 0) {
                otpTriesRemaining--;
            }
            document.getElementById('otp-tries-left').textContent = `${otpTriesRemaining}/3`;
            showAlert('otp-alert', errPayload);
        }
    } catch {
        showAlert('otp-alert', 'Cannot reach server.');
    } finally {
        setLoading('verify-otp-btn', false);
    }
}

async function handleResendOtp() {
    if (!pendingOtpEmail) {
        showAlert('otp-alert', 'Missing email address.');
        return;
    }

    setLoading('resend-otp-btn', true);
    try {
        const res = await fetch(`${API}/resend-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingOtpEmail })
        });
        const data = await res.json();

        if (res.ok && data.success) {
            otpTriesRemaining = 3;
            document.getElementById('otp-tries-left').textContent = '3/3';
            startOtpTimer(300);
            showAlert('otp-alert', 'A new OTP has been sent to your email!', 'success');
        } else {
            const errPayload = data.errors || data.message || 'Failed to resend OTP.';
            showAlert('otp-alert', errPayload);
        }
    } catch {
        showAlert('otp-alert', 'Cannot reach server.');
    } finally {
        setLoading('resend-otp-btn', false);
    }
}

// Dashboard
function populateDashboard() {
    if (!currentUser) return;
    
    document.getElementById('dash-name').textContent = currentUser.username || '-';
    document.getElementById('dash-firstname').textContent = currentUser.firstName || '-';
    document.getElementById('dash-lastname').textContent = currentUser.lastName || '-';
    document.getElementById('dash-gender').textContent = currentUser.gender || '-';
    document.getElementById('dash-email').textContent = currentUser.email || '-';
    document.getElementById('dash-contact').textContent = currentUser.contactNumber || '-';
    document.getElementById('dash-dob').textContent = currentUser.dob || '-';
    document.getElementById('dash-age').textContent = currentUser.age || '0';
    document.getElementById('dash-address').textContent = currentUser.address || '-';
    document.getElementById('dash-college').textContent = currentUser.collegeName || '-';
    document.getElementById('dash-school').textContent = currentUser.schoolName || '-';
    document.getElementById('dash-company').textContent = currentUser.currentCompany || '-';
}

function handleLogout() {
    clearToken();
    showScreen('screen-login');
    document.getElementById('login-form').reset();
    resetSignupFormState();
    document.getElementById('edit-profile-form').reset();
}

// Edit Profile Flow
function openEditModal() {
    document.getElementById('password-modal').classList.add('active');
    document.getElementById('modal-password').value = '';
    clearAlerts();
}

function closeEditModal() {
    document.getElementById('password-modal').classList.remove('active');
}

function proceedToEditProfile() {
    const pwd = document.getElementById('modal-password').value;
    if (!pwd) {
        showAlert('modal-alert', 'Please enter your password to continue.');
        return;
    }
    
    window.tempPassword = pwd;
    closeEditModal();
    
    document.getElementById('edit-firstName').value = currentUser.firstName || '';
    document.getElementById('edit-lastName').value = currentUser.lastName || '';
    document.getElementById('edit-gender').value = currentUser.gender || 'OTHER';
    document.getElementById('edit-contact').value = currentUser.contactNumber || '';
    document.getElementById('edit-dob').value = currentUser.dob || '';
    document.getElementById('edit-address').value = currentUser.address || '';
    document.getElementById('edit-college').value = currentUser.collegeName || '';
    document.getElementById('edit-school').value = currentUser.schoolName || '';
    document.getElementById('edit-company').value = currentUser.currentCompany || '';
    
    showScreen('screen-edit-profile');
}

async function handleUpdateProfile(e) {
    e.preventDefault();
    
    const firstName = document.getElementById('edit-firstName').value.trim();
    const lastName = document.getElementById('edit-lastName').value.trim();
    const gender = document.getElementById('edit-gender').value;
    const contactNumber = document.getElementById('edit-contact').value.trim();
    const dob = document.getElementById('edit-dob').value;
    const address = document.getElementById('edit-address').value.trim();
    const collegeName = document.getElementById('edit-college').value.trim();
    const schoolName = document.getElementById('edit-school').value.trim();
    const currentCompany = document.getElementById('edit-company').value.trim();
    
    setLoading('edit-save-btn', true);
    
    try {
        const res = await fetch(`${API}/update-profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify({ 
                password: window.tempPassword,
                firstName, lastName, gender, contactNumber, dob, address, collegeName, schoolName, currentCompany 
            })
        });
        
        const data = await res.json();
        
        if (res.ok && data.success) {
            saveUser(data.user);
            populateDashboard();
            showScreen('screen-dashboard');
            window.tempPassword = null;
        } else {
            const errPayload = data.errors || data.message || 'Failed to update profile.';
            showAlert('edit-alert', errPayload);
            if (res.status === 401) {
                setTimeout(() => {
                    showScreen('screen-dashboard');
                    openEditModal();
                    showAlert('modal-alert', 'Incorrect password. Try again.');
                }, 1500);
            }
        }
    } catch {
        showAlert('edit-alert', 'Cannot reach server.');
    } finally {
        setLoading('edit-save-btn', false);
    }
}

// Restore session on load
document.addEventListener('DOMContentLoaded', () => {
    const token = getToken();
    const userStr = localStorage.getItem('currentUser');
    if (token && userStr) {
        try {
            currentUser = JSON.parse(userStr);
            populateDashboard();
            showScreen('screen-dashboard');
        } catch {
            clearToken();
        }
    }
});

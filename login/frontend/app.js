const API = 'http://localhost:8080/api/auth';
let currentUser = null;

// UI Helpers
function showScreen(id) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    const screen = document.getElementById(id);
    screen.classList.add('active');
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
    el.textContent = message;
    el.className = `alert alert-${type} show`;
}

function clearAlerts() {
    document.querySelectorAll('.alert').forEach(el => {
        el.className = 'alert';
        el.textContent = '';
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
		//console.log(data);

        if (res.ok && data.success) {
            currentUser = data.user;
            populateDashboard();
            showScreen('screen-dashboard');
        } else if (res.status === 404) {
            showAlert('login-alert', 'No account found. Redirecting to sign up...', 'info');
            setTimeout(() => {
                showScreen('screen-signup');
                document.getElementById('signup-username').value = identifier;
            }, 1500);
        } else {
            showAlert('login-alert', data.message || 'Incorrect credentials.');
        }
    } catch {
        showAlert('login-alert', 'Cannot reach server. Is the backend running?');
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
    const contactNumber = document.getElementById('signup-contact').value.trim();
    const dob = document.getElementById('signup-dob').value;
    const address = document.getElementById('signup-address').value.trim();
    const collegeName = document.getElementById('signup-college').value.trim();
    const schoolName = document.getElementById('signup-school').value.trim();
    const currentCompany = document.getElementById('signup-company').value.trim();

    setLoading('signup-btn', true);
    try {
        const res = await fetch(`${API}/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, firstName, lastName, gender, email, password, contactNumber, dob, address, collegeName, schoolName, currentCompany })
        });
        const data = await res.json();
		//console.log(data);

        if (res.ok && data.success) {
            showAlert('signup-alert', 'Account created. Redirecting to sign in...', 'success');
            setTimeout(() => {
                showScreen('screen-login');
                document.getElementById('login-identifier').value = username;
            }, 1500);
        } else {
            showAlert('signup-alert', data.message || 'Something went wrong.');
        }
    } catch {
        showAlert('signup-alert', 'Cannot reach server. Is the backend running?');
    } finally {
        setLoading('signup-btn', false);
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
    currentUser = null;
    showScreen('screen-login');
    document.getElementById('login-form').reset();
    document.getElementById('signup-form').reset();
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
    
    // We store it temporarily to use in the update request
    window.tempPassword = pwd;
    closeEditModal();
    
    // Pre-fill edit form
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
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                identifier: currentUser.username, 
                password: window.tempPassword,
                firstName, lastName, gender, contactNumber, dob, address, collegeName, schoolName, currentCompany 
            })
        });
        
        const data = await res.json();
        
        if (res.ok && data.success) {
            currentUser = data.user;
            populateDashboard();
            showScreen('screen-dashboard');
            window.tempPassword = null;
        } else {
            const errorMsg = data.errors ? Object.values(data.errors).join(' | ') : (data.message || 'Failed to update profile.');
            showAlert('edit-alert', errorMsg);
            if (res.status === 401) {
                // Password was wrong
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

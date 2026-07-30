const API_BASE = '/api/v1/auth';

async function callApi(url, method = 'GET', body) {
  const headers = { 'Content-Type': 'application/json' };
  
  const token = sessionStorage.getItem('token');
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const username = sessionStorage.getItem('username');
  if (username) {
    headers['X-User-Name'] = username;
  }

  const options = { method, headers };
  if (method !== 'GET' && method !== 'HEAD' && body !== undefined) {
    options.body = JSON.stringify(body);
  }

  const res = await fetch(url, options);

  const contentType = res.headers.get('content-type') || '';
  const payload = contentType.includes('application/json')
    ? await res.json()
    : await res.text();

  return { ok: res.ok, status: res.status, payload };
}

function extractErrorText(payload) {
  if (!payload) {
    return 'Something went wrong';
  }
  if (typeof payload === 'string') {
    return payload;
  }
  if (payload.errors && typeof payload.errors === 'object' && Object.keys(payload.errors).length > 0) {
    return Object.values(payload.errors).join(' | ');
  }
  if (payload.message) {
    return payload.message;
  }
  return 'Something went wrong';
}

function wireUpValidation(form, button) {
  form.addEventListener('input', () => {
    button.disabled = !form.checkValidity();
  });
}

function setLoading(button, isLoading) {
  if (isLoading) {
    button.dataset.wasDisabled = button.disabled;
    button.disabled = true;
    button.classList.add('loading');
  } else {
    button.classList.remove('loading');
    button.disabled = button.dataset.wasDisabled === 'true';
  }
}

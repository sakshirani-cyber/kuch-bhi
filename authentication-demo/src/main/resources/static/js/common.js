const API_BASE = '/api/v1/auth';

async function callApi(url, method, body) {
  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

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

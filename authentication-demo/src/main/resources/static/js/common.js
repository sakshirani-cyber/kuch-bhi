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
  if (typeof payload === 'string') {
    return payload;
  }
  if (payload && typeof payload === 'object') {
    return Object.values(payload).join(' | ');
  }
  return 'Something went wrong';
}

function wireUpValidation(form, button) {
  form.addEventListener('input', () => {
    button.disabled = !form.checkValidity();
  });
}

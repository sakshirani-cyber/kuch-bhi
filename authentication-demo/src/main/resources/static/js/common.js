// Shared helpers used by both index.js and dashboard.js.
// Keeping this in one file avoids the same fetch/error-handling
// logic being copy-pasted (and drifting out of sync) across pages.

const API_BASE = '/api/v1/auth';

/**
 * Calls the given API endpoint and normalizes the response into
 * { ok, status, payload }, whether the backend replied with plain
 * text (most success messages) or JSON (validation error maps).
 */
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

/**
 * The backend returns either a plain error string (business
 * exceptions) or a field->message JSON map (validation errors).
 * This turns either shape into one readable line.
 */
function extractErrorText(payload) {
  if (typeof payload === 'string') {
    return payload;
  }
  if (payload && typeof payload === 'object') {
    return Object.values(payload).join(' | ');
  }
  return 'Something went wrong';
}

/**
 * Keeps a submit button disabled until its form passes native
 * HTML5 validation (required, pattern, type=email, etc.).
 */
function wireUpValidation(form, button) {
  form.addEventListener('input', () => {
    button.disabled = !form.checkValidity();
  });
}

/**
 * Toggles a button into/out of its "loading" visual state. Expects
 * the button to contain a <span class="btn-label"> for the text and
 * a <span class="spinner"> for the CSS spinner (see styles.css).
 * The disabled state is restored to whatever it was before loading
 * only when explicitly told to (restoreEnabled), since some buttons
 * should stay disabled after a successful action (e.g. cleared field).
 */
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

const BASE = '/api/files';

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${BASE}/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Upload failed');
  }

  return res.json();
}

export async function fetchFiles() {
  const res = await fetch(BASE);

  if (!res.ok) {
    throw new Error('Failed to load files');
  }

  return res.json();
}

export async function fetchRecords(fileId, page = 0, size = 20) {
  const res = await fetch(`${BASE}/${fileId}/records?page=${page}&size=${size}`);

  if (!res.ok) {
    throw new Error('Failed to load records');
  }

  return res.json();
}

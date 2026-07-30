import { useState } from 'react';
import { uploadFile } from '../api/fileApi';

export default function FileUpload({ onUploaded }) {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) return;

    setLoading(true);
    setError('');

    try {
      const result = await uploadFile(file);
      onUploaded(result);
      setFile(null);
      e.target.reset();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>Upload Excel File</h2>
      <form onSubmit={handleSubmit} className="upload-form">
        <input
          type="file"
          accept=".xlsx,.xls"
          onChange={(e) => setFile(e.target.files[0])}
        />
        <button type="submit" disabled={!file || loading}>
          {loading ? 'Uploading…' : 'Upload'}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
    </section>
  );
}

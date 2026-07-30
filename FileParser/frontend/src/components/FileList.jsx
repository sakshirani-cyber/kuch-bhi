export default function FileList({ files, selectedId, onSelect, loading, error }) {
  if (loading) return <p className="muted">Loading files…</p>;
  if (error) return <p className="error">{error}</p>;
  if (!files.length) return <p className="muted">No files uploaded yet.</p>;

  return (
    <section className="card">
      <h2>Uploaded Files</h2>
      <ul className="file-list">
        {files.map((file) => (
          <li key={file.id}>
            <button
              type="button"
              className={selectedId === file.id ? 'file-item selected' : 'file-item'}
              onClick={() => onSelect(file.id)}
            >
              <span className="file-name">{file.originalFileName}</span>
              <span className="file-meta">
                {file.rowCount} rows · {file.columnCount} columns · {file.status}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}

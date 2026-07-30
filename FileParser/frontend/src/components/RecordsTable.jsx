import { useEffect, useState } from 'react';
import { fetchRecords } from '../api/fileApi';

const PAGE_SIZE = 20;

export default function RecordsTable({ fileId }) {
  const [page, setPage] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!fileId) return;

    setPageNum(0);
  }, [fileId]);

  useEffect(() => {
    if (!fileId) return;

    setLoading(true);
    setError('');

    fetchRecords(fileId, pageNum, PAGE_SIZE)
      .then(setPage)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [fileId, pageNum]);

  if (!fileId) return null;

  const rows = page.content ?? [];
  const headers = rows.length > 0 ? Object.keys(rows[0].data ?? {}) : [];
  const totalPages = page.totalPages ?? 0;

  return (
    <section className="card">
      <div className="records-header">
        <h2>Parsed Records</h2>
        {page.totalElements != null && (
          <span className="muted">{page.totalElements} total rows</span>
        )}
      </div>

      {loading && <p className="muted">Loading records…</p>}
      {error && <p className="error">{error}</p>}

      {!loading && !error && rows.length === 0 && (
        <p className="muted">No records found for this file.</p>
      )}

      {!loading && !error && rows.length > 0 && (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Row #</th>
                  {headers.map((header) => (
                    <th key={header}>{header}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id}>
                    <td>{row.rowNumber}</td>
                    {headers.map((header) => (
                      <td key={header}>{formatCell(row.data?.[header])}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button
              type="button"
              disabled={pageNum === 0}
              onClick={() => setPageNum((current) => current - 1)}
            >
              Previous
            </button>
            <span>
              Page {pageNum + 1} of {Math.max(totalPages, 1)}
            </span>
            <button
              type="button"
              disabled={pageNum >= totalPages - 1}
              onClick={() => setPageNum((current) => current + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </section>
  );
}

function formatCell(value) {
  if (value == null) return '';
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

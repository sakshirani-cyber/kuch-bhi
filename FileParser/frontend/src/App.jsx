import { useCallback, useEffect, useState } from 'react';
import FileUpload from './components/FileUpload';
import FileList from './components/FileList';
import RecordsTable from './components/RecordsTable';
import { fetchFiles } from './api/fileApi';
import './App.css';

export default function App() {
  const [files, setFiles] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadFiles = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const data = await fetchFiles();
      setFiles(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadFiles();
  }, [loadFiles]);

  const handleUploaded = (result) => {
    loadFiles();
    setSelectedId(result.id);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>File Parser</h1>
        <p>Upload an Excel file to parse and view stored records.</p>
      </header>

      <main className="app-main">
        <FileUpload onUploaded={handleUploaded} />
        <FileList
          files={files}
          selectedId={selectedId}
          onSelect={setSelectedId}
          loading={loading}
          error={error}
        />
        <RecordsTable fileId={selectedId} />
      </main>
    </div>
  );
}

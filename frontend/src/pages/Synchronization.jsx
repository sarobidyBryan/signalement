import { useState, useEffect } from 'react';
import './css/Synchronization.css';
import { bidirectional, getLogs } from '../services/syncService';

export default function Synchronization() {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [logs, setLogs] = useState([]);
  // fetch logs once on mount; further fetch is triggered after a sync
  useEffect(() => {
    fetchLogs();
  }, []);

  async function fetchLogs() {
    try {
      const data = await getLogs();
      setLogs(data || []);
    } catch (err) {
      console.error(err);
      setLogs([]);
    }
  }

  async function handleSync() {
    setMessage(null);
    setLoading(true);
    try {
      const res = await bidirectional();
      // backend wraps results in ApiResponse.data.syncResults
      // we don't display the raw API payload anymore
      setMessage('Synchronisation terminée avec succès.');
      await fetchLogs();
      return res;
    } catch (err) {
      console.error(err);
      setMessage('Erreur durant la synchronisation.');
      throw err;
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="sync-page">
      <h1>Synchronisation</h1>
      <p>Effectue une synchronisation complète entre PostgreSQL et Firebase.</p>

      <div className="sync-actions">
        <button className="sync-button" onClick={handleSync} disabled={loading}>
          {loading ? (
            <span className="sync-spinner" aria-hidden="true" />
          ) : null}
          {loading ? 'Synchronisation en cours...' : 'Lancer la synchronisation (bidirectionnelle)'}
        </button>
        {message && <div className="sync-message">{message}</div>}
      </div>

      <section className="sync-logs">
        <h2>Historique des synchronisations</h2>
        {logs.length === 0 ? (
          <div className="sync-empty">Aucun log disponible.</div>
        ) : (
          <table className="sync-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Table</th>
                <th>Type</th>
                <th>Records</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{new Date(log.syncDate).toLocaleString()}</td>
                  <td>{log.tableName}</td>
                  <td>{log.syncType}</td>
                  <td>{log.recordsSynced}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* raw API result removed — we now display only the synchronized logs */}
    </div>
  );
}

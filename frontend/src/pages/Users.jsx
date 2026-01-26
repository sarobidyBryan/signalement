import { useEffect, useState } from 'react';
import './Users.css';

const API_BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8180').replace(/\/$/, '');

export default function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchBlocked = async () => {
    setLoading(true);
    setError('');
    try {
      const url = `${API_BASE}/api/users/blocked?perPage=100`;
      const res = await fetch(url, { credentials: 'include' });
      const contentType = res.headers.get('content-type') || '';
      if (!res.ok) {
        const text = await res.text();
        throw new Error(`HTTP ${res.status} - ${text.slice(0,200)}`);
      }
      if (!contentType.includes('application/json')) {
        const text = await res.text();
        throw new Error('Expected JSON but received: ' + (contentType || 'no content-type') + '\n' + text.slice(0,300));
      }
      const body = await res.json();
      const data = body && body.data ? body.data.users || [] : [];
      setUsers(data);
    } catch (e) {
      setError(e.message || 'Erreur');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchBlocked(); }, []);

  const unblock = async (id) => {
    if (!confirm('Débloquer cet utilisateur ?')) return;
    try {
      const res = await fetch(`${API_BASE}/api/users/${id}/status`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ statusCode: 'ACTIVE' })
      });
      if (!res.ok) {
        const t = await res.text();
        throw new Error(t || `HTTP ${res.status}`);
      }
      await fetchBlocked();
    } catch (e) {
      alert('Erreur: ' + (e.message || e));
    }
  };

  return (
    <div className="users-page">
      <h1>Utilisateurs bloqués</h1>
      {loading && <div>Chargement...</div>}
      {error && <div className="error">{error}</div>}
      {!loading && !error && (
        <table className="users-table">
          <thead>
            <tr><th>Id</th><th>Nom</th><th>Email</th><th>Role</th><th>Action</th></tr>
          </thead>
          <tbody>
            {users.length === 0 && (
              <tr><td colSpan={5}>Aucun utilisateur bloqué</td></tr>
            )}
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td><button onClick={() => unblock(u.id)}>Débloquer</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

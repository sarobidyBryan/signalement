import { useEffect, useState } from 'react';
import './Users.css';

const API_BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8180').replace(/\/$/, '');

export default function Users() {
  const [allUsers, setAllUsers] = useState([]);
  const [blockedUsers, setBlockedUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const normalizeUsers = (raw) => {
    return raw.map((u) => {
      const roleCode = u.role?.roleCode || u.role || null;
      const statusCode = u.statusCode || u.userStatusType?.statusCode || u.user_status_type?.status_code || (u.userStatusType && u.userStatusType.statusCode) || null;
      return {
        id: u.id,
        name: u.name,
        email: u.email,
        role: roleCode,
        statusCode: statusCode,
      };
    });
  };

  const fetchUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const url = `${API_BASE}/api/users`;
      const res = await fetch(url, { credentials: 'include' });
      const contentType = res.headers.get('content-type') || '';
      if (!res.ok) {
        // If unauthorized or forbidden, try the blocked endpoint as a fallback
        if (res.status === 401 || res.status === 403) {
          // try blocked list
          return fetchBlockedFallback();
        }
        const text = await res.text();
        throw new Error(`HTTP ${res.status} - ${text.slice(0,200)}`);
      }
      if (!contentType.includes('application/json')) {
        const text = await res.text();
        throw new Error('Expected JSON but received: ' + (contentType || 'no content-type') + '\n' + text.slice(0,300));
      }
      const body = await res.json();
      // ApiResponse may put users under data.users or directly return an array
      let raw = [];
      if (body && body.data) {
        raw = body.data.users || body.data || [];
      } else if (Array.isArray(body)) {
        raw = body;
      } else if (body && body.users) {
        raw = body.users;
      }
      const normalized = normalizeUsers(raw);
      setAllUsers(normalized);
      setBlockedUsers(normalized.filter(u => u.statusCode === 'SUSPENDED'));
    } catch (e) {
      setError(e.message || 'Erreur');
    } finally {
      setLoading(false);
    }
  };

  const fetchBlockedFallback = async () => {
    try {
      const url = `${API_BASE}/api/users/blocked?perPage=100`;
      const res = await fetch(url, { credentials: 'include' });
      if (!res.ok) {
        const t = await res.text();
        throw new Error(`HTTP ${res.status} - ${t.slice(0,200)}`);
      }
      const body = await res.json();
      const raw = body && body.data ? body.data.users || [] : [];
      const normalized = normalizeUsers(raw);
      // when only blocked endpoint is available, populate blockedUsers
      setAllUsers(normalized);
      setBlockedUsers(normalized.filter(u => u.statusCode === 'SUSPENDED'));
    } catch (e) {
      setError(e.message || 'Erreur');
    }
  };

  useEffect(() => { fetchUsers(); }, []);

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
      await fetchUsers();
    } catch (e) {
      alert('Erreur: ' + (e.message || e));
    }
  };

  return (
    <div className="users-page">
      {loading && <div>Chargement...</div>}
      {error && <div className="error">{error}</div>}
        {!loading && !error && (
          <>
            <h2>Liste complète des utilisateurs</h2>
            <table className="users-table">
              <thead>
                <tr><th>Id</th><th>Nom</th><th>Email</th><th>Role</th><th>Statut</th></tr>
              </thead>
              <tbody>
                {allUsers.length === 0 && (
                  <tr><td colSpan={5}>Aucun utilisateur</td></tr>
                )}
                {allUsers.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>{u.statusCode || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <h2 style={{marginTop: '24px'}}>Utilisateurs bloqués</h2>
            <table className="users-table">
              <thead>
                <tr><th>Id</th><th>Nom</th><th>Email</th><th>Role</th><th>Action</th></tr>
              </thead>
              <tbody>
                {blockedUsers.length === 0 && (
                  <tr><td colSpan={5}>Aucun utilisateur bloqué</td></tr>
                )}
                {blockedUsers.map((u) => (
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
          </>
        )}
    </div>
  );
}

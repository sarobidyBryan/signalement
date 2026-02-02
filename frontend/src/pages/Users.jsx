import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { userService } from '../services';
import { ApiError } from '../services/api';
import './css/Users.css';

export default function Users() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all'); // 'all' | 'blocked' | 'active'
  const [searchTerm, setSearchTerm] = useState('');

  const normalizeUser = (u) => {
    const roleCode = u.role?.roleCode || u.role || null;
    const statusCode = u.statusCode || u.userStatusType?.statusCode || u.user_status_type?.status_code || null;
    return {
      id: u.id,
      name: u.name,
      email: u.email,
      role: roleCode,
      statusCode: statusCode,
    };
  };

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const raw = await userService.getAll();
      const normalized = (raw || []).map(normalizeUser);
      setUsers(normalized);
    } catch (err) {
      handleError(err, setError);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleError = (err, setter) => {
    if (!setter) return;
    if (err instanceof ApiError) {
      setter({ message: err.message, errorCode: err.errorCode, status: err.status });
    } else {
      setter({ message: err?.message || 'Erreur de connexion au serveur' });
    }
  };

  const filteredUsers = users.filter((u) => {
    // Filter by status
    if (filter === 'blocked' && u.statusCode !== 'SUSPENDED') return false;
    if (filter === 'active' && u.statusCode === 'SUSPENDED') return false;
    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      return (
        (u.name && u.name.toLowerCase().includes(term)) ||
        (u.email && u.email.toLowerCase().includes(term)) ||
        (u.role && u.role.toLowerCase().includes(term))
      );
    }
    return true;
  });

  const handleUnblock = async (user) => {
    if (!confirm(`Débloquer l'utilisateur "${user.name}" ?`)) return;
    try {
      await userService.updateStatus(user.id, 'ACTIVE');
      await loadUsers();
    } catch (err) {
      alert('Erreur: ' + (err.message || err));
    }
  };

  const handleBlock = async (user) => {
    if (!confirm(`Bloquer l'utilisateur "${user.name}" ?`)) return;
    try {
      await userService.updateStatus(user.id, 'SUSPENDED');
      await loadUsers();
    } catch (err) {
      alert('Erreur: ' + (err.message || err));
    }
  };

  const handleDelete = async (user) => {
    if (!confirm(`Supprimer définitivement l'utilisateur "${user.name}" ?`)) return;
    try {
      await userService.delete(user.id);
      await loadUsers();
    } catch (err) {
      alert('Erreur: ' + (err.message || err));
    }
  };

  const getStatusBadge = (statusCode) => {
    if (statusCode === 'SUSPENDED') {
      return <span className="status-badge blocked">Bloqué</span>;
    }
    if (statusCode === 'ACTIVE') {
      return <span className="status-badge active">Actif</span>;
    }
    return <span className="status-badge">{statusCode || '—'}</span>;
  };

  return (
    <div className="users-page">
      <div className="users-header">
        <div className="users-header-left">
          <p className="users-overline">Gestion</p>
          <h1>Utilisateurs</h1>
        </div>
        <Button variant="primary" onClick={() => navigate('/backoffice/users/new')}>
          + Nouveau
        </Button>
      </div>

      {error && <ErrorBanner error={error} onDismiss={() => setError(null)} />}

      <div className="users-filters">
        <div className="filter-group">
          <label>Rechercher</label>
          <input
            type="text"
            placeholder="Nom, email, rôle..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="filter-input"
          />
        </div>
        <div className="filter-group">
          <label>Statut</label>
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tous</option>
            <option value="active">Actifs</option>
            <option value="blocked">Bloqués</option>
          </select>
        </div>
      </div>

      <div className="users-table-container">
        {loading ? (
          <div className="users-loading">Chargement...</div>
        ) : (
          <table className="users-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom</th>
                <th>Email</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="users-empty">
                    Aucun utilisateur trouvé
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.name || '—'}</td>
                    <td>{user.email || '—'}</td>
                    <td>{user.role || '—'}</td>
                    <td>{getStatusBadge(user.statusCode)}</td>
                    <td className="actions-cell">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => navigate(`/backoffice/users/${user.id}/edit`)}
                      >
                        Modifier
                      </Button>
                      {user.statusCode === 'SUSPENDED' ? (
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleUnblock(user)}
                        >
                          Débloquer
                        </Button>
                      ) : (
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => handleBlock(user)}
                        >
                          Bloquer
                        </Button>
                      )}
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleDelete(user)}
                      >
                        Supprimer
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { configurationService } from '../services';
import { ApiError } from '../services/api';
import './Configurations.css';

export default function Configurations() {
  const navigate = useNavigate();
  const [configurations, setConfigurations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');

  const loadConfigurations = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await configurationService.getAll();
      setConfigurations(data || []);
    } catch (err) {
      handleError(err, setError);
      setConfigurations([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadConfigurations();
  }, [loadConfigurations]);

  const handleError = (err, setter) => {
    if (!setter) return;
    if (err instanceof ApiError) {
      setter({ message: err.message, errorCode: err.errorCode, status: err.status });
    } else {
      setter({ message: err?.message || 'Erreur de connexion au serveur' });
    }
  };

  const handleDelete = async (config) => {
    if (!confirm(`Supprimer la configuration "${config.key}" ?`)) return;
    try {
      await configurationService.delete(config.key);
      await loadConfigurations();
    } catch (err) {
      alert('Erreur: ' + (err.message || err));
    }
  };

  // Get unique types for filter
  const types = [...new Set(configurations.map(c => c.type).filter(Boolean))];

  const filteredConfigurations = configurations.filter((c) => {
    // Filter by type
    if (typeFilter !== 'all' && c.type !== typeFilter) return false;
    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      return (
        (c.key && c.key.toLowerCase().includes(term)) ||
        (c.value && c.value.toLowerCase().includes(term))
      );
    }
    return true;
  });

  return (
    <div className="configurations-page">
      <div className="configurations-header">
        <div className="configurations-header-left">
          <p className="configurations-overline">Administration</p>
          <h1>Configurations</h1>
        </div>
        <Button variant="primary" onClick={() => navigate('/backoffice/configurations/new')}>
          + Nouvelle
        </Button>
      </div>

      {error && <ErrorBanner error={error} onDismiss={() => setError(null)} />}

      <div className="configurations-filters">
        <div className="filter-group">
          <label>Rechercher</label>
          <input
            type="text"
            placeholder="Clé, valeur..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="filter-input"
          />
        </div>
        <div className="filter-group">
          <label>Type</label>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">Tous</option>
            {types.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="configurations-table-container">
        {loading ? (
          <div className="configurations-loading">Chargement...</div>
        ) : (
          <table className="configurations-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Clé</th>
                <th>Valeur</th>
                <th>Type</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredConfigurations.length === 0 ? (
                <tr>
                  <td colSpan={5} className="configurations-empty">
                    Aucune configuration trouvée
                  </td>
                </tr>
              ) : (
                filteredConfigurations.map((config) => (
                  <tr key={config.id || config.key}>
                    <td>{config.id || '—'}</td>
                    <td><code>{config.key}</code></td>
                    <td>{config.value || '—'}</td>
                    <td><span className="type-badge">{config.type || '—'}</span></td>
                    <td className="actions-cell">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => navigate(`/backoffice/configurations/${encodeURIComponent(config.key)}/edit`)}
                      >
                        Modifier
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleDelete(config)}
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

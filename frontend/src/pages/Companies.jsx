import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { companyService } from '../services';
import { ApiError } from '../services/api';
import './css/Companies.css';

export default function Companies() {
  const navigate = useNavigate();
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const loadCompanies = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await companyService.getAll();
      setCompanies(data || []);
    } catch (err) {
      handleError(err, setError);
      setCompanies([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCompanies();
  }, [loadCompanies]);

  const handleError = (err, setter) => {
    if (!setter) return;
    if (err instanceof ApiError) {
      setter({ message: err.message, errorCode: err.errorCode, status: err.status });
    } else {
      setter({ message: err?.message || 'Erreur de connexion au serveur' });
    }
  };

  const handleDelete = async (company) => {
    if (!confirm(`Supprimer l'entreprise "${company.name}" ?`)) return;
    try {
      await companyService.delete(company.id);
      await loadCompanies();
    } catch (err) {
      alert('Erreur: ' + (err.message || err));
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const filteredCompanies = companies.filter((c) => {
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      return (
        (c.name && c.name.toLowerCase().includes(term)) ||
        (c.email && c.email.toLowerCase().includes(term))
      );
    }
    return true;
  });

  return (
    <div className="companies-page">
      <div className="companies-header">
        <div className="companies-header-left">
          <p className="companies-overline">Gestion</p>
          <h1>Entreprises</h1>
        </div>
        <Button variant="primary" onClick={() => navigate('/backoffice/companies/new')}>
          + Nouvelle
        </Button>
      </div>

      {error && <ErrorBanner error={error} onDismiss={() => setError(null)} />}

      <div className="companies-filters">
        <div className="filter-group">
          <label>Rechercher</label>
          <input
            type="text"
            placeholder="Nom, email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="filter-input"
          />
        </div>
      </div>

      <div className="companies-table-container">
        {loading ? (
          <div className="companies-loading">Chargement...</div>
        ) : (
          <table className="companies-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom</th>
                <th>Email</th>
                <th>Créé le</th>
                <th>Mis à jour</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredCompanies.length === 0 ? (
                <tr>
                  <td colSpan={6} className="companies-empty">
                    Aucune entreprise trouvée
                  </td>
                </tr>
              ) : (
                filteredCompanies.map((company) => (
                  <tr key={company.id}>
                    <td>{company.id}</td>
                    <td><strong>{company.name || '—'}</strong></td>
                    <td>{company.email || '—'}</td>
                    <td>{formatDate(company.createdAt)}</td>
                    <td>{formatDate(company.updatedAt)}</td>
                    <td className="actions-cell">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => navigate(`/backoffice/companies/${company.id}/edit`)}
                      >
                        Modifier
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

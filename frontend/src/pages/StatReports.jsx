import { useState, useEffect, useCallback } from 'react';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { delayStatService, companyService } from '../services';
import './css/StatReports.css';

export default function StatReports() {
  const [filterReport, setFilterReport] = useState('');
  const [filterCompany, setFilterCompany] = useState('');
  const [selectedStat, setSelectedStat] = useState(null);
  const [stats, setStats] = useState([]);
  const [summary, setSummary] = useState({});
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Charger les entreprises
  useEffect(() => {
    async function loadCompanies() {
      try {
        const data = await companyService.getAll();
        setCompanies(data || []);
      } catch (err) {
        console.error('Erreur chargement entreprises:', err);
      }
    }
    loadCompanies();
  }, []);

  // Charger les stats de délai
  const loadDelayStats = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {};
      if (filterCompany) {
        params.companyId = parseInt(filterCompany);
      }
      
      const data = await delayStatService.getDelayStats(params);
      setStats(data.stats || []);
      setSummary(data.summary || {});
    } catch (err) {
      console.error('Erreur chargement stats:', err);
      setError(err?.message || 'Erreur lors du chargement des statistiques');
      setStats([]);
      setSummary({});
    } finally {
      setLoading(false);
    }
  }, [filterCompany]);

  useEffect(() => {
    loadDelayStats();
  }, [loadDelayStats]);

  const filteredStats = stats.filter(stat => {
    const matchReport = !filterReport || 
      `RPT-${stat.reportId}`.toLowerCase().includes(filterReport.toLowerCase()) || 
      (stat.description && stat.description.toLowerCase().includes(filterReport.toLowerCase()));
    return matchReport;
  });

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const formatDelay = (days) => {
    if (days === null || days === undefined) return '—';
    
    const totalHours = days * 24;
    if (totalHours < 24) {
      return `${Math.round(totalHours)}h`;
    }
    
    if (days < 30) {
      return `${Math.round(days)}j`;
    }
    
    const months = Math.floor(days / 30);
    const remainingDays = Math.round(days % 30);
    
    if (remainingDays === 0) {
      return `${months}m`;
    }
    
    return `${months}m${remainingDays}j`;
  };

  const globalStats = {
    total: summary.count || 0,
    completed: summary.completedCount || 0,
    inProgress: summary.inProgressCount || 0,
    assigned: summary.assignedCount || 0,
    avgDelay: summary.averageDelayDays || 0
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'Terminé': return 'status_completed';
      case 'Travaux en cours': return 'status-in-progress';
      case 'Assigné à une entreprise': return 'status-assigned';
      default: return '';
    }
  };

  const handleRowClick = (stat) => {
    setSelectedStat(stat);
  };

  const closePopup = () => {
    setSelectedStat(null);
  };

  return (
    <div className="stat-reports-page">
      <h1>Statistiques des Délais</h1>
      <p className="stat-subtitle">Analyse des délais de traitement des signalements par entreprise</p>

      {error && <ErrorBanner error={error} />}

      <div className="stat-filters">
        <div className="stat-filter-group">
          <label htmlFor="filter-report">
            Rechercher un signalement
          </label>
          <input
            id="filter-report"
            type="text"
            placeholder="ID ou description..."
            value={filterReport}
            onChange={(e) => setFilterReport(e.target.value)}
            className="stat-filter-input"
          />
        </div>

        <div className="stat-filter-group">
          <label htmlFor="filter-company">
            Filtrer par entreprise
          </label>
          <select
            id="filter-company"
            value={filterCompany}
            onChange={(e) => setFilterCompany(e.target.value)}
            className="stat-filter-input"
          >
            <option value="">Toutes les entreprises</option>
            {companies.map(company => (
              <option key={company.id} value={company.id}>{company.name}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="stat-summary">
        <div className="stat-summary-item">
          <span className="stat-summary-label">Total de signalements</span>
          <span className="stat-summary-value">{globalStats.total}</span>
        </div>
        <div className="stat-summary-item">
          <span className="stat-summary-label">Terminés</span>
          <span className="stat-summary-value">{globalStats.completed}</span>
        </div>
        <div className="stat-summary-item">
          <span className="stat-summary-label">En cours</span>
          <span className="stat-summary-value">{globalStats.inProgress}</span>
        </div>
        <div className="stat-summary-item">
          <span className="stat-summary-label">Assignés</span>
          <span className="stat-summary-value">{globalStats.assigned}</span>
        </div>
        <div className="stat-summary-item stat-summary-item-highlight">
          <span className="stat-summary-label">Délai moyen (terminés)</span>
          <span className="stat-summary-value">
            {globalStats.avgDelay > 0 ? formatDelay(globalStats.avgDelay) : '—'}
          </span>
        </div>
      </div>

      {loading ? (
        <div className="stat-loading">Chargement des statistiques...</div>
      ) : (
        <div className="stat-table-container">
          <table className="stat-table">
            <thead>
              <tr>
                <th>ID Report</th>
                <th>Description</th>
                <th>Entreprise</th>
                <th>État actuel</th>
                <th>Avancement</th>
                <th>Délai Total</th>
              </tr>
            </thead>
            <tbody>
              {filteredStats.length === 0 ? (
                <tr>
                  <td colSpan="6" className="stat-empty">Aucun résultat trouvé</td>
                </tr>
              ) : (
                filteredStats.map(stat => (
                  <tr key={stat.reportId} onClick={() => handleRowClick(stat)} className="stat-row-clickable">
                    <td className="stat-report-id">RPT-{String(stat.reportId).padStart(3, '0')}</td>
                    <td className="stat-description">{stat.description || 'Sans description'}</td>
                    <td>{stat.companyName || '—'}</td>
                    <td>
                      <span className={`stat-status-badge ${getStatusClass(stat.statusLabel)}`}>
                        {stat.statusLabel || 'Inconnu'}
                      </span>
                    </td>
                    <td className="stat-progress-text">{Math.round(stat.progressPercentage || 0)}%</td>
                    <td className="stat-delay stat-delay-total">{formatDelay(stat.delayInDays)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {selectedStat && (
        <div className="stat-popup-overlay" onClick={closePopup}>
          <div className="stat-popup" onClick={(e) => e.stopPropagation()}>
            <div className="stat-popup-header">
              <h2>Détails du Signalement</h2>
              <button className="stat-popup-close" onClick={closePopup}>✕</button>
            </div>

            <div className="stat-popup-content">
              <div className="stat-popup-section">
                <h3>Informations générales</h3>
                <div className="stat-popup-grid">
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">ID Report:</span>
                    <span className="stat-popup-value">RPT-{String(selectedStat.reportId).padStart(3, '0')}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">Surface:</span>
                    <span className="stat-popup-value">{selectedStat.area} m²</span>
                  </div>
                  <div className="stat-popup-field stat-popup-field-full">
                    <span className="stat-popup-label">Description:</span>
                    <span className="stat-popup-value">{selectedStat.description || 'Sans description'}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">Entreprise:</span>
                    <span className="stat-popup-value">{selectedStat.companyName || '—'}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">État:</span>
                    <span className={`stat-status-badge ${getStatusClass(selectedStat.statusLabel)}`}>
                      {selectedStat.statusLabel || 'Inconnu'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="stat-popup-section">
                <h3>Progression</h3>
                <div className="stat-popup-progress">
                  <span className="stat-progress-text-large">{Math.round(selectedStat.progressPercentage || 0)}% complété</span>
                </div>
              </div>
            </div>

            <div className="stat-popup-footer">
              <Button onClick={closePopup}>Fermer</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

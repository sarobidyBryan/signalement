import { useState } from 'react';
import Button from '../components/Button/Button';
import './css/StatReports.css';

// Données fictives pour démonstration
const MOCK_STATS = [
  {
    id: 1,
    reportId: 'RPT-001',
    description: 'Nid-de-poule profond avenue de l\'Indépendance',
    area: 25.5,
    company: 'COLAS Madagascar',
    currentStatus: 'Terminé',
    progress: 100,
    assignedDate: '2024-01-10T08:00:00',
    inProgressDate: '2024-01-15T09:30:00',
    completedDate: '2024-02-05T16:00:00',
    delayAssignedToInProgress: 5.06, // jours
    delayInProgressToCompleted: 21.27,
    delayAssignedToCompleted: 26.33,
    location: { lat: -18.8792, lng: 47.5079 }
  },
  {
    id: 2,
    reportId: 'RPT-002',
    description: 'Fissures multiples sur Route Digue',
    area: 150.0,
    company: 'Groupe ETI Construction',
    currentStatus: 'Travaux en cours',
    progress: 67,
    assignedDate: '2024-01-20T10:00:00',
    inProgressDate: '2024-01-25T08:00:00',
    completedDate: null,
    delayAssignedToInProgress: 4.92,
    delayInProgressToCompleted: null,
    delayAssignedToCompleted: null,
    location: { lat: -18.9100, lng: 47.5361 }
  },
  {
    id: 3,
    reportId: 'RPT-003',
    description: 'Effondrement partiel chaussée Analakely',
    area: 45.8,
    company: 'SOGEA SATOM Madagascar',
    currentStatus: 'Assigné à une entreprise',
    progress: 0,
    assignedDate: '2024-02-01T14:00:00',
    inProgressDate: null,
    completedDate: null,
    delayAssignedToInProgress: null,
    delayInProgressToCompleted: null,
    delayAssignedToCompleted: null,
    location: { lat: -18.9137, lng: 47.5214 }
  },
  {
    id: 4,
    reportId: 'RPT-004',
    description: 'Dégradation route nationale RN7',
    area: 320.0,
    company: 'TRAVOTECH',
    currentStatus: 'Terminé',
    progress: 100,
    assignedDate: '2023-12-15T09:00:00',
    inProgressDate: '2023-12-20T07:30:00',
    completedDate: '2024-01-30T18:00:00',
    delayAssignedToInProgress: 5.02,
    delayInProgressToCompleted: 41.44,
    delayAssignedToCompleted: 46.46,
    location: { lat: -18.9333, lng: 47.5167 }
  },
  {
    id: 5,
    reportId: 'RPT-005',
    description: 'Trous profonds Avenue Ranavalona',
    area: 12.3,
    company: 'BTP Madagascar',
    currentStatus: 'Travaux en cours',
    progress: 35,
    assignedDate: '2024-01-28T11:00:00',
    inProgressDate: '2024-02-02T10:00:00',
    completedDate: null,
    delayAssignedToInProgress: 5.0,
    delayInProgressToCompleted: null,
    delayAssignedToCompleted: null,
    location: { lat: -18.8792, lng: 47.5279 }
  },
  {
    id: 6,
    reportId: 'RPT-006',
    description: 'Chaussée affaissée Boulevard de l\'Europe',
    area: 88.5,
    company: 'Mad\'Artisan',
    currentStatus: 'Terminé',
    progress: 100,
    assignedDate: '2024-01-05T13:00:00',
    inProgressDate: '2024-01-08T08:00:00',
    completedDate: '2024-01-22T17:30:00',
    delayAssignedToInProgress: 2.79,
    delayInProgressToCompleted: 14.40,
    delayAssignedToCompleted: 17.19,
    location: { lat: -18.8792, lng: 47.5379 }
  },
  {
    id: 7,
    reportId: 'RPT-007',
    description: 'Dégâts multiples Route Ivato',
    area: 200.0,
    company: 'Road Masters Tana',
    currentStatus: 'Travaux en cours',
    progress: 82,
    assignedDate: '2024-01-12T09:30:00',
    inProgressDate: '2024-01-18T07:00:00',
    completedDate: null,
    delayAssignedToInProgress: 5.73,
    delayInProgressToCompleted: null,
    delayAssignedToCompleted: null,
    location: { lat: -18.7969, lng: 47.4781 }
  },
  {
    id: 8,
    reportId: 'RPT-008',
    description: 'Nid-de-poule profond Rue Rainandriamampandry',
    area: 8.2,
    company: 'COLAS Madagascar',
    currentStatus: 'Assigné à une entreprise',
    progress: 0,
    assignedDate: '2024-02-03T15:00:00',
    inProgressDate: null,
    completedDate: null,
    delayAssignedToInProgress: null,
    delayInProgressToCompleted: null,
    delayAssignedToCompleted: null,
    location: { lat: -18.9047, lng: 47.5216 }
  },
];

const COMPANIES = [...new Set(MOCK_STATS.map(s => s.company))].sort();

export default function StatReports() {
  const [filterReport, setFilterReport] = useState('');
  const [filterCompany, setFilterCompany] = useState('');
  const [selectedStat, setSelectedStat] = useState(null);

  const filteredStats = MOCK_STATS.filter(stat => {
    const matchReport = !filterReport || stat.reportId.toLowerCase().includes(filterReport.toLowerCase()) || stat.description.toLowerCase().includes(filterReport.toLowerCase());
    const matchCompany = !filterCompany || stat.company === filterCompany;
    return matchReport && matchCompany;
  });

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const formatDelay = (days) => {
    if (days === null || days === undefined) return '—';
    
    if (days < 1) {
      return `${Math.round(days * 24)}h`;
    }
    
    if (days < 30) {
      return `${Math.round(days)}j`;
    }
    
    // Pour les délais >= 30 jours, afficher en mois + jours
    const months = Math.floor(days / 30);
    const remainingDays = Math.round(days % 30);
    
    if (remainingDays === 0) {
      return `${months}m`;
    }
    
    return `${months}m${remainingDays}j`;
  };

  const calculateStats = () => {
    if (filteredStats.length === 0) {
      return { avgTotal: null, count: 0 };
    }

    const completedReports = filteredStats.filter(s => s.delayAssignedToCompleted !== null);
    
    if (completedReports.length === 0) {
      return { avgTotal: null, count: 0, completed: 0 };
    }

    const totalDelay = completedReports.reduce((sum, s) => sum + s.delayAssignedToCompleted, 0);
    const avgTotal = totalDelay / completedReports.length;

    return {
      avgTotal,
      count: filteredStats.length,
      completed: completedReports.length,
      inProgress: filteredStats.filter(s => s.currentStatus === 'Travaux en cours').length,
      assigned: filteredStats.filter(s => s.currentStatus === 'Assigné à une entreprise').length,
    };
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
            {COMPANIES.map(company => (
              <option key={company} value={company}>{company}</option>
            ))}
          </select>
        </div>
      </div>

      {(() => {
        const stats = calculateStats();
        return (
          <div className="stat-summary">
            <div className="stat-summary-item">
              <span className="stat-summary-label">Total de signalements</span>
              <span className="stat-summary-value">{stats.count}</span>
            </div>
            <div className="stat-summary-item">
              <span className="stat-summary-label">Terminés</span>
              <span className="stat-summary-value">{stats.completed}</span>
            </div>
            <div className="stat-summary-item">
              <span className="stat-summary-label">En cours</span>
              <span className="stat-summary-value">{stats.inProgress}</span>
            </div>
            <div className="stat-summary-item">
              <span className="stat-summary-label">Assignés</span>
              <span className="stat-summary-value">{stats.assigned}</span>
            </div>
            <div className="stat-summary-item stat-summary-item-highlight">
              <span className="stat-summary-label">Délai moyen (terminés)</span>
              <span className="stat-summary-value">
                {stats.avgTotal !== null ? formatDelay(stats.avgTotal) : '—'}
              </span>
            </div>
          </div>
        );
      })()}

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
                <tr key={stat.id} onClick={() => handleRowClick(stat)} className="stat-row-clickable">
                  <td className="stat-report-id">{stat.reportId}</td>
                  <td className="stat-description">{stat.description}</td>
                  <td>{stat.company}</td>
                  <td>
                    <span className={`stat-status-badge ${getStatusClass(stat.currentStatus)}`}>
                      {stat.currentStatus}
                    </span>
                  </td>
                  <td className="stat-progress-text">{stat.progress}%</td>
                  <td className="stat-delay stat-delay-total">{formatDelay(stat.delayAssignedToCompleted)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

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
                    <span className="stat-popup-value">{selectedStat.reportId}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">Surface:</span>
                    <span className="stat-popup-value">{selectedStat.area} m²</span>
                  </div>
                  <div className="stat-popup-field stat-popup-field-full">
                    <span className="stat-popup-label">Description:</span>
                    <span className="stat-popup-value">{selectedStat.description}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">Entreprise:</span>
                    <span className="stat-popup-value">{selectedStat.company}</span>
                  </div>
                  <div className="stat-popup-field">
                    <span className="stat-popup-label">État:</span>
                    <span className={`stat-status-badge ${getStatusClass(selectedStat.currentStatus)}`}>
                      {selectedStat.currentStatus}
                    </span>
                  </div>
                </div>
              </div>

              <div className="stat-popup-section">
                <h3>Progression</h3>
                <div className="stat-popup-progress">
                  <span className="stat-progress-text-large">{selectedStat.progress}% complété</span>
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

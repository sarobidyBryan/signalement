import { useEffect, useState, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Card from '../components/Card/Card';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { ApiError } from '../services/api';
import { assignationProgressService, assignationService, companyService, reportService, statusService, userService } from '../services';
import { authService } from '../services/auth';
import './css/Reports.css';

const emptyFilters = {
  areaMin: '',
  areaMax: '',
  statusCode: '',
  userId: '',
  reportDateFrom: '',
  reportDateTo: '',
};

function Reports() {
  const [filters, setFilters] = useState(emptyFilters);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);
  const [statuses, setStatuses] = useState([]);
  const [users, setUsers] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [assignationForm, setAssignationForm] = useState({ companyId: '', budget: '', startDate: '', deadline: '' });
  const [progressForm, setProgressForm] = useState({ assignationId: '', comment: '', registrationDate: '' });
  const [assignationLoading, setAssignationLoading] = useState(false);
  const [progressLoading, setProgressLoading] = useState(false);
  const [showAssignationForm, setShowAssignationForm] = useState(false);
  const [showProgressForm, setShowProgressForm] = useState(false);
  const [detailFocus, setDetailFocus] = useState('view');
  const location = useLocation();
  const navigate = useNavigate();
  const currentUser = authService.getStoredUser();

  const loadReferenceData = useCallback(async () => {
    try {
      const [statusList, userList, companyList] = await Promise.all([
        statusService.getAll(),
        userService.getAll(),
        companyService.getAll(),
      ]);
      setStatuses(statusList);
      setUsers(userList);
      setCompanies(companyList);
    } catch (err) {
      console.error('Ref data error', err);
    }
  }, []);

  const loadReports = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const list = await reportService.getAll(params);
      setReports(list);
    } catch (err) {
      handleError(err, setError);
      setReports([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadReferenceData();
    loadReports();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (detail?.assignations?.length) {
      setProgressForm((prev) => ({
        ...prev,
        assignationId: detail.assignations[0].id?.toString() || '',
      }));
      setShowAssignationForm(false);
    }
  }, [detail]);

  const handleError = (err, setter) => {
    if (!setter) return;
    if (err instanceof ApiError) {
      setter({ message: err.message, errorCode: err.errorCode, status: err.status });
    } else {
      setter({ message: err?.message || 'Impossible de joindre le serveur' });
    }
  };

  const handleFilterSubmit = (event) => {
    event.preventDefault();
    const sanitized = {};
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value != null) sanitized[key] = value;
    });
    loadReports(sanitized);
  };

  const resetFilters = () => {
    setFilters({ ...emptyFilters });
    loadReports();
  };

  const openDetail = async (reportId) => {
    setDetailLoading(true);
    setDetailError(null);
    try {
      const detailData = await reportService.getDetail(reportId);
      if (detailData) {
        setDetail(detailData);
      } else {
        setDetail(null);
        setDetailError({ message: 'Détails introuvables pour ce signalement' });
      }
    } catch (err) {
      handleError(err, setDetailError);
      setDetail(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const openDetailPanel = (reportId, focus = 'view') => {
    setDetailFocus(focus);
    openDetail(reportId);
  };

  const closeDetail = () => {
    setDetail(null);
    setDetailError(null);
    setShowAssignationForm(false);
    setShowProgressForm(false);
    setDetailFocus('view');
  };

  const handleAssignationSubmit = async (event) => {
    event.preventDefault();
    if (!detail?.report?.id) return;

    const companyId = parseInt(assignationForm.companyId, 10);
    if (!companyId) {
      setDetailError({ message: 'Sélectionnez une entreprise' });
      return;
    }

    setAssignationLoading(true);
    setDetailError(null);
    try {
      await assignationService.create({
        company: { id: companyId },
        report: { id: detail.report.id },
        budget: assignationForm.budget ? parseFloat(assignationForm.budget) : 0,
        startDate: assignationForm.startDate || undefined,
        deadline: assignationForm.deadline || undefined,
      });
      setAssignationForm({ companyId: '', budget: '', startDate: '', deadline: '' });
      await loadReports();
      await openDetail(detail.report.id);
    } catch (err) {
      handleError(err, setDetailError);
    } finally {
      setAssignationLoading(false);
    }
  };

  const handleProgressSubmit = async (event) => {
    event.preventDefault();
    if (!detail?.report?.id || !progressForm.assignationId) return;
    setProgressLoading(true);
    setDetailError(null);
    try {
        await assignationProgressService.create({
          reportsAssignation: { id: parseInt(progressForm.assignationId, 10) },
          comment: progressForm.comment,
          registrationDate: progressForm.registrationDate || undefined,
        });
        setProgressForm((prev) => ({ ...prev, comment: '', registrationDate: '' }));
      await loadReports();
      await openDetail(detail.report.id);
    } catch (err) {
      handleError(err, setDetailError);
    } finally {
      setProgressLoading(false);
    }
  };

  const formatNumber = (value) => {
    if (value == null) return '—';
    return Number(value).toLocaleString('fr-FR', { maximumFractionDigits: 2 });
  };

  const formatDate = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '—';
    return date.toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' });
  };

  return (
    <div className="reports-page">
      <header className="reports-main-header">
        <div>
          <h1>Signalements</h1>
          {currentUser && <p className="reports-welcome">{currentUser.name ?? currentUser.email}</p>}
        </div>
      </header>

      <section className="reports-grid">
            <Card className="filters-panel">
              <div className="panel-header">
                <p>Filtrer</p>
              </div>
              <form className="filter-form" onSubmit={handleFilterSubmit}>
                <div className="filter-grid">
                  <label>
                    Surface min (m²)
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      name="areaMin"
                      value={filters.areaMin}
                      onChange={(e) => setFilters({ ...filters, areaMin: e.target.value })}
                    />
                  </label>
                  <label>
                    Surface max (m²)
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      name="areaMax"
                      value={filters.areaMax}
                      onChange={(e) => setFilters({ ...filters, areaMax: e.target.value })}
                    />
                  </label>
                  <label>
                    Statut
                    <select
                      name="statusCode"
                      value={filters.statusCode}
                      onChange={(e) => setFilters({ ...filters, statusCode: e.target.value })}
                    >
                      <option value="">Tous</option>
                      {statuses.map((status) => (
                        <option key={status.id} value={status.statusCode}>
                          {status.label || status.statusCode}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Responsable
                    <select
                      name="userId"
                      value={filters.userId}
                      onChange={(e) => setFilters({ ...filters, userId: e.target.value })}
                    >
                      <option value="">Tous</option>
                      {users.map((user) => (
                        <option key={user.id} value={user.id}>
                          {user.name || user.email}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Date de signalement (début)
                    <input
                      type="date"
                      name="reportDateFrom"
                      value={filters.reportDateFrom}
                      onChange={(e) => setFilters({ ...filters, reportDateFrom: e.target.value })}
                    />
                  </label>
                  <label>
                    Date de signalement (fin)
                    <input
                      type="date"
                      name="reportDateTo"
                      value={filters.reportDateTo}
                      onChange={(e) => setFilters({ ...filters, reportDateTo: e.target.value })}
                    />
                  </label>
                </div>
                <div className="filter-actions">
                  <Button variant="primary" type="submit" size="md">
                    Appliquer
                  </Button>
                  <Button variant="ghost" type="button" onClick={resetFilters}>
                    Réinitialiser
                  </Button>
                </div>
              </form>
            </Card>

            <Card className="table-panel">
              <div className="panel-header">
                <div>
                  <p className="panel-overline">Liste</p>
                  <h2>Signalements</h2>
                </div>
                <span className="table-count">{reports.length} signalement(s)</span>
              </div>
              {loading ? (
                <div className="table-loading">Chargement des signalements...</div>
              ) : error ? (
                <div className="table-error">
                  <ErrorBanner error={error} />
                  <Button onClick={() => loadReports()} variant="secondary">
                    Réessayer
                  </Button>
                </div>
              ) : (
                <div className="reports-table-wrapper">
                  <div className="reports-table-header">
                    <span>ID</span>
                    <span>Date</span>
                    <span>Utilisateur</span>
                    <span>Surface</span>
                    <span>Statut</span>
                    <span>Actions</span>
                  </div>
                  {reports.map((report) => (
                    <div className="reports-table-row" key={report.id}>
                      <span>#{report.id}</span>
                      <span>{formatDate(report.reportDate)}</span>
                      <span>{report.user?.name || report.user?.email || '—'}</span>
                      <span>{formatNumber(report.area)} m²</span>
                      <span>
                        <span className={`status-badge ${report.status && (report.status.statusCode || '') ? 'status-' + report.status.statusCode.toString().toLowerCase().replace(/[^a-z0-9]+/g,'_') : 'status-default'}`}>
                          {report.status?.label || report.status?.statusCode}
                        </span>
                      </span>
                      <span className="action-buttons">
                        <Button variant="ghost" size="sm" onClick={() => openDetailPanel(report.id, 'view')}>
                          Voir
                        </Button>
                        <Button variant="secondary" size="sm" onClick={() => navigate(`/backoffice/reports/${report.id}/edit`)}>
                          Modifier
                        </Button>
                      </span>
                    </div>
                  ))}
                  {!reports.length && <p className="empty-table">Aucun signalement ne correspond aux filtres</p>}
                </div>
              )}
            </Card>
          </section>
        <section className={`detail-panel ${detail ? 'detail-open' : ''}`}>
          <div className="detail-panel-inner">
            <div className="detail-panel-header">
              <div>
                <p className="panel-overline">Détails</p>
                <h2>{detail?.report ? `Signalement #${detail.report.id}` : 'Sélectionnez un signalement'}</h2>
                {detail && <p className="detail-focus">Focus: {detailFocus}</p>}
              </div>
              <button className="detail-panel-close" onClick={closeDetail}>
                ×
              </button>
            </div>
            {detailLoading ? (
              <div className="detail-loading">Chargement du signalement...</div>
            ) : detailError ? (
              <ErrorBanner error={detailError} />
            ) : detail ? (
              <div className="detail-body">
                <div className="detail-summary">
                  <div>
                    <p className="summary-label">Statut actuel</p>
                    <p className="summary-value">
                      <span className={`status-badge ${((detail.report.status && (detail.report.status.statusCode || '') ) || '').toString().toLowerCase().replace(/[^a-z0-9]+/g,'_') ? 'status-' + ((detail.report.status && (detail.report.status.statusCode || '') ) || '').toString().toLowerCase().replace(/[^a-z0-9]+/g,'_') : 'status-default'}`}>
                        {detail.report.status?.label || detail.report.status?.statusCode}
                      </span>
                    </p>
                  </div>
                  <div>
                    <p className="summary-label">Surface totale</p>
                    <p className="summary-value">{formatNumber(detail.totalArea)} m²</p>
                  </div>
                  <div>
                    <p className="summary-label">Surface traitée</p>
                    <p className="summary-value">{formatNumber(detail.treatedArea)} m²</p>
                  </div>
                  <div>
                    <p className="summary-label">Pourcentage</p>
                    <p className="summary-value">{Math.min(100, Number(detail.progressPercentage) || 0).toFixed(1)}%</p>
                  </div>
                </div>
                <div className="detail-progress">
                  <div className="progress-track">
                    <div
                      className="progress-fill"
                      style={{ width: `${Math.min(100, Number(detail.progressPercentage) || 0)}%` }}
                    ></div>
                  </div>
                  <p className="progress-text">
                    {formatNumber(detail.treatedArea)} / {formatNumber(detail.totalArea)} m² traité
                  </p>
                </div>
                <div className="detail-grid">
                  <div>
                    <p className="summary-label">Coordonnées</p>
                    <p>{detail.report.latitude || '—'}, {detail.report.longitude || '—'}</p>
                  </div>
                  <div>
                    <p className="summary-label">Description</p>
                    <p className="description-text">{detail.report.description || 'Aucune description fournie'}</p>
                  </div>
                </div>
                <div className="assignations-section">
                  <div className="section-header">
                    <h3>Assignations</h3>
                    {detail.assignations?.length ? (
                      <span>{detail.assignations.length} assignation(s)</span>
                    ) : (
                      <Button variant="primary" size="sm" onClick={() => setShowAssignationForm((prev) => !prev)}>
                        Créer une assignation
                      </Button>
                    )}
                  </div>
                  <div className="assignations-list">
                    {detail.assignations?.map((assignation) => (
                      <Card key={assignation.id} className="assignation-card">
                        <div>
                          <h4>{assignation.company?.name || 'Entreprise inconnue'}</h4>
                          <p>Budget : {formatNumber(assignation.budget)} Ar</p>
                        </div>
                        <div className="assignation-dates">
                          <span>Début : {assignation.startDate || '—'}</span>
                          <span>Deadline : {assignation.deadline || '—'}</span>
                        </div>
                      </Card>
                    ))}
                    {!detail.assignations?.length && <p className="inline-hint">Aucune assignation pour ce signalement.</p>}
                  </div>
                  {showAssignationForm && !detail.assignations?.length && (
                    <form className="assignation-form" onSubmit={handleAssignationSubmit}>
                      <label>
                        Entreprise
                        <select
                          value={assignationForm.companyId}
                          onChange={(e) => setAssignationForm({ ...assignationForm, companyId: e.target.value })}
                        >
                          <option value="">Choisir</option>
                          {companies.map((company) => (
                            <option key={company.id} value={company.id}>
                              {company.name}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        Budget (Ar)
                        <input
                          type="number"
                          min="0"
                          step="0.01"
                          value={assignationForm.budget}
                          onChange={(e) => setAssignationForm({ ...assignationForm, budget: e.target.value })}
                        />
                      </label>
                      <label>
                        Début
                        <input
                          type="date"
                          value={assignationForm.startDate}
                          onChange={(e) => setAssignationForm({ ...assignationForm, startDate: e.target.value })}
                        />
                      </label>
                      <label>
                        Deadline
                        <input
                          type="date"
                          value={assignationForm.deadline}
                          onChange={(e) => setAssignationForm({ ...assignationForm, deadline: e.target.value })}
                        />
                      </label>
                      <div className="form-actions">
                        <Button type="submit" variant="primary" disabled={assignationLoading}>
                          {assignationLoading ? 'Enregistrement...' : 'Ajouter'}
                        </Button>
                        <Button type="button" variant="ghost" onClick={() => setShowAssignationForm(false)}>
                          Annuler
                        </Button>
                      </div>
                    </form>
                  )}
                </div>

                <div className="progress-section">
                  <div className="section-header">
                    <h3>Progrès</h3>
                    {detail.assignations?.length ? (
                      <Button type="button" variant="secondary" size="sm" onClick={() => setShowProgressForm((prev) => !prev)}>
                        Ajouter un progrès
                      </Button>
                    ) : (
                      <p className="inline-hint">Ajoutez une assignation pour démarrer le suivi.</p>
                    )}
                  </div>
                  {showProgressForm && detail.assignations?.length > 0 && (
                    <form className="progress-form" onSubmit={handleProgressSubmit}>
                      <label>
                        Assignation
                        <select
                          value={progressForm.assignationId}
                          onChange={(e) => setProgressForm({ ...progressForm, assignationId: e.target.value })}
                        >
                          {detail.assignations.map((assignation) => (
                            <option key={assignation.id} value={assignation.id}>
                              {assignation.company?.name || `#${assignation.id}`}
                            </option>
                          ))}
                        </select>
                      </label>
                      <label>
                        Date d'enregistrement
                        <input
                          type="datetime-local"
                          value={progressForm.registrationDate}
                          onChange={(e) => setProgressForm({ ...progressForm, registrationDate: e.target.value })}
                        />
                      </label>
                      <label>
                        Commentaire
                        <input
                          type="text"
                          value={progressForm.comment}
                          onChange={(e) => setProgressForm({ ...progressForm, comment: e.target.value })}
                        />
                      </label>
                      <div className="form-actions">
                        <Button type="submit" variant="primary" disabled={progressLoading}>
                          {progressLoading ? 'Publication...' : 'Publier'}
                        </Button>
                        <Button type="button" variant="ghost" onClick={() => setShowProgressForm(false)}>
                          Fermer
                        </Button>
                      </div>
                    </form>
                  )}
                  <div className="progress-list">
                    {detail.progressEntries?.map((entry) => (
                      <div className="progress-card" key={entry.id}>
                        <div className="progress-card-top">
                          <div>
                            <p className="summary-label">Assignation</p>
                            <p>{entry.reportsAssignation?.company?.name || `#${entry.reportsAssignation?.id}`}</p>
                          </div>
                          <div>
                            <p className="summary-label">Progression</p>
                            <p>{Number(entry.percentage || 0).toFixed(1)}%</p>
                          </div>
                        </div>
                        <div className="progress-details">
                          <div className="progress-detail-row">
                            <span className="detail-label">Surface traitée:</span>
                            <span className="detail-value">{formatNumber(entry.treatedArea)} m²</span>
                          </div>
                          <div className="progress-detail-row">
                            <span className="detail-label">Date d'enregistrement:</span>
                            <span className="detail-value">{formatDate(entry.registrationDate)}</span>
                          </div>
                        </div>
                        <p className="progress-card-body">{entry.comment || 'Aucun commentaire'}</p>
                      </div>
                    ))}
                    {!detail.progressEntries?.length && <p className="inline-hint">Aucun progrès enregistré.</p>}
                  </div>
                </div>
              </div>
            ) : (
              <div className="detail-empty">Sélectionnez un signalement pour afficher ses détails.</div>
            )}
          </div>
        </section>
    </div>
  );
}

export default Reports;

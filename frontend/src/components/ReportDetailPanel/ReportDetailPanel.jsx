import { useState } from 'react';
import Card from '../Card/Card';
import Button from '../Button/Button';
import ErrorBanner from '../ErrorBanner';
import { assignationProgressService, assignationService } from '../../services';
import '../css/ReportDetailPanel.css';

/**
 * ReportDetailPanel - displays full report details with assignations and progress
 * Props:
 * - detail: full report detail object from reportService.getDetail()
 * - detailLoading: boolean
 * - detailError: error object
 * - onClose: callback
 * - onRefresh: callback to reload report details after updates
 * - companies: array of companies for assignation form
 * - isOpen: boolean to control panel visibility
 * - readOnly: boolean to hide all action buttons (for public view)
 */
const ReportDetailPanel = ({
  detail,
  detailLoading = false,
  detailError = null,
  onClose = () => {},
  onRefresh = () => {},
  companies = [],
  isOpen = false,
  readOnly = false
}) => {
  const [assignationForm, setAssignationForm] = useState({ companyId: '', budget: '', startDate: '', deadline: '' });
  const [progressForm, setProgressForm] = useState({ assignationId: '', treatedArea: '', comment: '' });
  const [assignationLoading, setAssignationLoading] = useState(false);
  const [progressLoading, setProgressLoading] = useState(false);
  const [showAssignationForm, setShowAssignationForm] = useState(false);
  const [showProgressForm, setShowProgressForm] = useState(false);
  const [formError, setFormError] = useState(null);

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

  const handleAssignationSubmit = async (event) => {
    event.preventDefault();
    if (!detail?.report?.id) return;

    const companyId = parseInt(assignationForm.companyId, 10);
    if (!companyId) {
      setFormError({ message: 'Sélectionnez une entreprise' });
      return;
    }

    setAssignationLoading(true);
    setFormError(null);
    try {
      await assignationService.create({
        company: { id: companyId },
        report: { id: detail.report.id },
        budget: assignationForm.budget ? parseFloat(assignationForm.budget) : 0,
        startDate: assignationForm.startDate || undefined,
        deadline: assignationForm.deadline || undefined,
      });
      setAssignationForm({ companyId: '', budget: '', startDate: '', deadline: '' });
      setShowAssignationForm(false);
      onRefresh(detail.report.id);
    } catch (err) {
      setFormError({ message: err?.message || 'Erreur lors de la création de l\'assignation' });
    } finally {
      setAssignationLoading(false);
    }
  };

  const handleProgressSubmit = async (event) => {
    event.preventDefault();
    if (!detail?.report?.id) return;

    const assignationId = progressForm.assignationId || detail.assignations?.[0]?.id;
    if (!assignationId) {
      setFormError({ message: 'Aucune assignation sélectionnée' });
      return;
    }

    setProgressLoading(true);
    setFormError(null);
    try {
      await assignationProgressService.create({
        reportsAssignation: { id: parseInt(assignationId, 10) },
        treatedArea: progressForm.treatedArea ? parseFloat(progressForm.treatedArea) : 0,
        comment: progressForm.comment,
      });
      setProgressForm((prev) => ({ ...prev, treatedArea: '', comment: '' }));
      setShowProgressForm(false);
      onRefresh(detail.report.id);
    } catch (err) {
      setFormError({ message: err?.message || 'Erreur lors de l\'ajout du progrès' });
    } finally {
      setProgressLoading(false);
    }
  };

  // Auto-select first assignation when detail loads
  useState(() => {
    if (detail?.assignations?.length && !progressForm.assignationId) {
      setProgressForm((prev) => ({
        ...prev,
        assignationId: detail.assignations[0].id?.toString() || '',
      }));
    }
  });

  return (
    <section className={`report-detail-panel ${isOpen ? 'detail-open' : ''}`}>
      <div className="detail-panel-inner">
        <div className="detail-panel-header">
          <div>
            <p className="panel-overline">Détails</p>
            <h2>{detail?.report ? `Signalement #${detail.report.id}` : 'Sélectionnez un signalement'}</h2>
          </div>
          <button className="detail-panel-close" onClick={onClose}>
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
                <p className="summary-value">{detail.report.status?.label || detail.report.status?.statusCode}</p>
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
                <p className="summary-label">Date de signalement</p>
                <p>{formatDate(detail.report.reportDate)}</p>
              </div>
              <div>
                <p className="summary-label">Utilisateur</p>
                <p>{detail.report.user?.name || detail.report.user?.email || '—'}</p>
              </div>
              <div>
                <p className="summary-label">Coordonnées</p>
                <p>{detail.report.latitude || '—'}, {detail.report.longitude || '—'}</p>
              </div>
              <div>
                <p className="summary-label">Description</p>
                <p className="description-text">{detail.report.description || 'Aucune description fournie'}</p>
              </div>
            </div>

            {formError && <ErrorBanner error={formError} />}

            <div className="assignations-section">
              <div className="section-header">
                <h3>Assignations</h3>
                {detail.assignations?.length ? (
                  <span>{detail.assignations.length} assignation(s)</span>
                ) : !readOnly && companies.length > 0 ? (
                  <Button variant="primary" size="sm" onClick={() => setShowAssignationForm((prev) => !prev)}>
                    Créer une assignation
                  </Button>
                ) : null}
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
              {showAssignationForm && !detail.assignations?.length && companies.length > 0 && (
                <form className="assignation-form" onSubmit={handleAssignationSubmit}>
                  <label>
                    Entreprise
                    <select
                      value={assignationForm.companyId}
                      onChange={(e) => setAssignationForm({ ...assignationForm, companyId: e.target.value })}
                      required
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
                {!readOnly && detail.assignations?.length && companies.length > 0 ? (
                  <Button type="button" variant="secondary" size="sm" onClick={() => setShowProgressForm((prev) => !prev)}>
                    Ajouter un progrès
                  </Button>
                ) : !readOnly && detail.assignations?.length === 0 ? (
                  <p className="inline-hint">Ajoutez une assignation pour démarrer le suivi.</p>
                ) : null}
              </div>
              {showProgressForm && detail.assignations?.length > 0 && (
                <form className="progress-form" onSubmit={handleProgressSubmit}>
                  <label>
                    Assignation
                    <select
                      value={progressForm.assignationId}
                      onChange={(e) => setProgressForm({ ...progressForm, assignationId: e.target.value })}
                      required
                    >
                      {detail.assignations.map((assignation) => (
                        <option key={assignation.id} value={assignation.id}>
                          {assignation.company?.name || `#${assignation.id}`}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Surface traitée (m²)
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={progressForm.treatedArea}
                      onChange={(e) => setProgressForm({ ...progressForm, treatedArea: e.target.value })}
                      required
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
          <div className="detail-empty">Sélectionnez un signalement sur la carte pour afficher ses détails.</div>
        )}
      </div>
    </section>
  );
};

export default ReportDetailPanel;

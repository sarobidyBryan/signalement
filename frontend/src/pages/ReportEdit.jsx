import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Card from '../components/Card/Card';
import Button from '../components/Button/Button';
import ErrorBanner from '../components/ErrorBanner';
import { ApiError } from '../services/api';
import { reportService, statusService, userService, companyService, assignationService, configurationService } from '../services';
import { authService } from '../services/auth';
import './css/ReportEdit.css';

const ReportEdit = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [report, setReport] = useState(null);
  const [statuses, setStatuses] = useState([]);
  const [users, setUsers] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [assignationForm, setAssignationForm] = useState({
    companyId: '',
    startDate: '',
    deadline: '',
    niveau: 5
  });
  const [showAssignationForm, setShowAssignationForm] = useState(false);
  const [hasExistingAssignation, setHasExistingAssignation] = useState(false);
  const [priceM2, setPriceM2] = useState(null);

  const currentUser = authService.getStoredUser();

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Charger les données de référence
      const [statusList, userList, companyList, configList] = await Promise.all([
        statusService.getAll(),
        userService.getAll(),
        companyService.getAll(),
        configurationService.getAll()
      ]);

      setStatuses(statusList);
      setUsers(userList);
      setCompanies(companyList);

      // Récupérer price_m2 depuis les configurations
      const priceConfig = configList.find(c => c.key === 'price_m2');
      const pM2 = priceConfig ? parseFloat(priceConfig.value) : null;
      setPriceM2(pM2);

      // Charger les détails du report
      const reportDetail = await reportService.getDetail(parseInt(id));
      setReport(reportDetail.report);

      // Initialiser le niveau du formulaire avec la valeur du report si disponible
      const rpt = reportDetail.report;
      if (rpt.niveau != null) {
        setAssignationForm(prev => ({ ...prev, niveau: rpt.niveau }));
      }

      // Vérifier s'il y a déjà une assignation
      const hasAssignation = reportDetail.assignations && reportDetail.assignations.length > 0;
      setHasExistingAssignation(hasAssignation);

    } catch (err) {
      handleError(err, setError);
    } finally {
      setLoading(false);
    }
  };

  const handleError = (err, setErrorState) => {
    if (err instanceof ApiError) {
      setErrorState(err.message);
    } else {
      setErrorState('Une erreur inattendue s\'est produite');
    }
  };

  const handleReportSubmit = async (event) => {
    event.preventDefault();
    if (!report) return;

    setSaving(true);
    setError(null);

    try {
      await reportService.update(parseInt(id), report);
      navigate('/backoffice/reports');
    } catch (err) {
      handleError(err, setError);
    } finally {
      setSaving(false);
    }
  };

  const handleAssignationSubmit = async (event) => {
    event.preventDefault();
    if (!report || hasExistingAssignation) return;

    const companyId = parseInt(assignationForm.companyId, 10);
    if (!companyId) {
      setError('Sélectionnez une entreprise');
      return;
    }

    setSaving(true);
    setError(null);

    try {
      // Mettre à jour le niveau du report avant de créer l'assignation
      try {
        await reportService.update(parseInt(id), { niveau: parseInt(assignationForm.niveau, 10) });
      } catch (uErr) {
        console.warn('Report update (niveau) failed', uErr);
      }

      await assignationService.create({
        company: { id: companyId },
        report: { id: parseInt(id) },
        startDate: assignationForm.startDate || undefined,
        deadline: assignationForm.deadline || undefined,
      });

      // Recharger les données pour mettre à jour l'état
      await loadData();
      setShowAssignationForm(false);
      setAssignationForm({ companyId: '', startDate: '', deadline: '', niveau: 5 });

    } catch (err) {
      handleError(err, setError);
    } finally {
      setSaving(false);
    }
  };

  const handleReportChange = (field, value) => {
    setReport(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '';
    return date.toISOString().split('T')[0];
  };

  const formatDateTimeForInput = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '';
    return date.toISOString().slice(0, 16);
  };

  if (loading) {
    return (
      <div className="report-edit-page">
        <div className="loading-state">Chargement du signalement...</div>
      </div>
    );
  }

  if (!report) {
    return (
      <div className="report-edit-page">
        <div className="error-state">
          <ErrorBanner error="Signalement introuvable" />
          <Button onClick={() => navigate('/backoffice/reports')}>Retour aux signalements</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="report-edit-page">
      <header className="report-edit-header">
        <div>
          <h1>Modifier le signalement #{report.id}</h1>
          {currentUser && <p className="report-edit-welcome">{currentUser.name ?? currentUser.email}</p>}
        </div>
      </header>

      <div className="report-edit-grid">
            <Card className="report-form-card">
              <div className="card-header">
                <h2>Informations du signalement</h2>
              </div>

              {error && <ErrorBanner error={error} />}

              <form className="report-form" onSubmit={handleReportSubmit}>
                <div className="form-grid">
                  <div className="form-group">
                    <label htmlFor="area">Surface (m²) *</label>
                    <input
                      id="area"
                      type="number"
                      min="0"
                      step="0.01"
                      value={report.area || ''}
                      onChange={(e) => handleReportChange('area', parseFloat(e.target.value) || 0)}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="longitude">Longitude</label>
                    <input
                      id="longitude"
                      type="number"
                      step="any"
                      value={report.longitude || ''}
                      onChange={(e) => handleReportChange('longitude', parseFloat(e.target.value) || null)}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="latitude">Latitude</label>
                    <input
                      id="latitude"
                      type="number"
                      step="any"
                      value={report.latitude || ''}
                      onChange={(e) => handleReportChange('latitude', parseFloat(e.target.value) || null)}
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="reportDate">Date du signalement</label>
                    <input
                      id="reportDate"
                      type="datetime-local"
                      value={formatDateTimeForInput(report.reportDate)}
                      onChange={(e) => handleReportChange('reportDate', e.target.value ? new Date(e.target.value) : null)}
                    />
                  </div>

                  <div className="form-group form-group-full">
                    <label htmlFor="description">Description</label>
                    <textarea
                      id="description"
                      value={report.description || ''}
                      onChange={(e) => handleReportChange('description', e.target.value)}
                      rows={4}
                    />
                  </div>
                </div>

                <div className="form-actions">
                  <Button type="submit" variant="primary" disabled={saving}>
                    {saving ? 'Enregistrement...' : 'Enregistrer les modifications'}
                  </Button>
                  <Button type="button" variant="ghost" onClick={() => navigate('/backoffice/reports')}>
                    Annuler
                  </Button>
                </div>
              </form>
            </Card>

            <Card className="assignation-card">
              <div className="card-header">
                <h2>Assignation</h2>
                {!hasExistingAssignation && !showAssignationForm && (
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    onClick={() => setShowAssignationForm(true)}
                  >
                    Assigner à une entreprise
                  </Button>
                )}
              </div>

              {hasExistingAssignation ? (
                <div className="assignation-info">
                  <p className="assignation-status">Ce signalement est déjà assigné et ne peut pas être réassigné.</p>
                  <div className="existing-assignation">
                    {report.assignations?.map(assignation => (
                      <div key={assignation.id} className="assignation-item">
                        <h4>{assignation.company?.name || 'Entreprise inconnue'}</h4>
                        <p>Budget : {assignation.budget ? `${assignation.budget.toLocaleString('fr-FR')} Ar` : 'Non défini'}</p>
                        <p>Début : {assignation.startDate || 'Non défini'}</p>
                        <p>Deadline : {assignation.deadline || 'Non défini'}</p>
                      </div>
                    ))}
                  </div>
                </div>
              ) : showAssignationForm ? (
                <form className="assignation-form" onSubmit={handleAssignationSubmit}>
                  <div className="form-grid">
                    <div className="form-group form-group-full">
                      <label htmlFor="companyId">Entreprise *</label>
                      <select
                        id="companyId"
                        value={assignationForm.companyId}
                        onChange={(e) => setAssignationForm(prev => ({ ...prev, companyId: e.target.value }))}
                        required
                      >
                        <option value="">Choisir une entreprise</option>
                        {companies.map(company => (
                          <option key={company.id} value={company.id}>
                            {company.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-group form-group-full">
                      <label htmlFor="niveau">Niveau de dégâts</label>
                      <div className="niveau-control">
                        <input
                          id="niveau"
                          type="range"
                          min="1"
                          max="10"
                          step="1"
                          value={assignationForm.niveau}
                          onChange={(e) => setAssignationForm(prev => ({ ...prev, niveau: parseInt(e.target.value, 10) }))}
                        />
                        <div className="niveau-display">Niveau: {assignationForm.niveau}</div>
                      </div>
                    </div>

                    <div className="form-group">
                      <label htmlFor="budget">Budget estimé (Ar)</label>
                      <input
                        id="budget"
                        type="text"
                        value={(() => {
                          const area = report?.area ? parseFloat(report.area) : null;
                          const niveau = assignationForm.niveau;
                          if (priceM2 && niveau && area) return (priceM2 * niveau * area).toLocaleString('fr-FR') + ' Ar';
                          return 'Non calculable (niveau ou surface manquant)';
                        })()}
                        disabled
                        className="form-input-disabled"
                      />
                      <span className="form-hint">Calculé automatiquement : prix/m² × niveau × surface</span>
                    </div>

                    <div className="form-group">
                      <label htmlFor="startDate">Date de début</label>
                      <input
                        id="startDate"
                        type="date"
                        value={assignationForm.startDate}
                        onChange={(e) => setAssignationForm(prev => ({ ...prev, startDate: e.target.value }))}
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="deadline">Date limite</label>
                      <input
                        id="deadline"
                        type="date"
                        value={assignationForm.deadline}
                        onChange={(e) => setAssignationForm(prev => ({ ...prev, deadline: e.target.value }))}
                      />
                    </div>
                  </div>

                  <div className="form-actions">
                    <Button type="submit" variant="primary" disabled={saving}>
                      {saving ? 'Assignation...' : 'Assigner'}
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => {
                        setShowAssignationForm(false);
                        setAssignationForm({ companyId: '', startDate: '', deadline: '', niveau: 5 });
                      }}
                    >
                      Annuler
                    </Button>
                  </div>
                </form>
              ) : (
                <p className="no-assignation">Ce signalement n'est pas encore assigné à une entreprise.</p>
              )}
            </Card>
          </div>
    </div>
  );
};

export default ReportEdit;
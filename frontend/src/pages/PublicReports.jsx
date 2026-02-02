import './css/PublicReports.css';
import { useEffect, useState, useCallback } from 'react';
import Map from '../components/Map';
import { reportService } from '../services';
import { getGlobal } from '../services/summaryService';
import ReportDetailPanel from '../components/ReportDetailPanel/ReportDetailPanel';

function PublicReports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState(null);
  const [selectedDetail, setSelectedDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState(null);
  // No companies loading for public view

  const loadReports = useCallback(async () => {
    setLoading(true);
    try {
      const list = await reportService.getAll();
      setReports(list || []);
    } catch (err) {
      console.error('Failed to load reports', err);
      setReports([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const loadStats = useCallback(async () => {
    try {
      const result = await getGlobal();
      // result has { summary: { nbPoints, totalSurface, totalBudget, totalTreated, overallProgressPercent }, reports: [...] }
      const summary = result?.summary || {};
      setStats({
        totalReports: summary.nbPoints || 0,
        totalArea: summary.totalSurface || 0,
        totalTreated: summary.totalTreated || 0,
        totalBudget: summary.totalBudget || 0,
        progressPercentage: summary.overallProgressPercent || 0
      });
      // also update reports from summary if available
      if (result?.reports && result.reports.length > 0) {
        setReports(result.reports);
      }
    } catch (err) {
      console.error('Failed to load stats', err);
      setStats(null);
    }
  }, []);

  useEffect(() => {
    loadReports();
    loadStats();
  }, [loadReports, loadStats]);

  const openDetail = async (reportId) => {
    if (!reportId) {
      setSelectedDetail(null);
      return;
    }
    setDetailLoading(true);
    setError(null);
    try {
      const detail = await reportService.getDetail(reportId);
      setSelectedDetail(detail || null);
    } catch (err) {
      console.error('Detail load error', err);
      setError({ message: 'Impossible de charger les détails' });
      setSelectedDetail(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const formatNumber = (value) => {
    if (value == null) return '—';
    return Number(value).toLocaleString('fr-FR', { maximumFractionDigits: 2 });
  };

  return (
    <div className="summary">
      <div className="summary-header">
        <h1>Suivi Public Signaleo</h1>
        <p>Vue d'ensemble des travaux et signalements à Antananarivo</p>
      </div>

      {/* Top metrics cards */}
      <div className="summary-metrics">
        <div className="metric-card">
          <div className="metric-card-label">Nombre de signalements</div>
          <div className="metric-card-value">{stats?.totalReports ?? 0}</div>
        </div>
        <div className="metric-card">
          <div className="metric-card-label">Surface totale</div>
          <div className="metric-card-value">{formatNumber(stats?.totalArea)} m²</div>
        </div>
        <div className="metric-card">
          <div className="metric-card-label">Surface traitée</div>
          <div className="metric-card-value">{formatNumber(stats?.totalTreated)} m²</div>
        </div>
        {/* Only show budget if public? User requested exactly same code/style. Summary has budget. I'll include it. */}
        <div className="metric-card">
          <div className="metric-card-label">Budget alloué</div>
          <div className="metric-card-value">{formatNumber(stats?.totalBudget)} Ar</div>
        </div>
        <div className="metric-card highlight">
          <div className="metric-card-label">Progression globale</div>
          <div className="metric-card-value">{Math.min(100, Number(stats?.progressPercentage || 0)).toFixed(1)}%</div>
          <div className="metric-card-bar">
            <div className="metric-card-bar-fill" style={{ width: `${Math.min(100, Number(stats?.progressPercentage || 0))}%` }} />
          </div>
        </div>
      </div>

      <div className="summary-content">
        <div className="map-section">
          <Map reports={reports} onReportClick={openDetail} />
        </div>

        <div className={`detail-inline ${selectedDetail ? 'open' : ''}`}>
          <ReportDetailPanel
            detail={selectedDetail}
            detailLoading={detailLoading}
            detailError={error}
            onClose={() => setSelectedDetail(null)}
            onRefresh={openDetail}
            companies={[]}
            isOpen={!!selectedDetail}
            readOnly={true}
          />
        </div>
      </div>
    </div>
  );
}

export default PublicReports;

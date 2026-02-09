import { fetchAPI, unwrapList } from './api';

const DELAY_STAT_BASE = '/delay-stats';

/**
 * Récupère les statistiques de délai avec filtres optionnels
 * @param {Object} params - Filtres { companyId, reportId }
 * @returns {Promise<Object>} { stats: [...], summary: {...} }
 */
async function getDelayStats(params = {}) {
  const queryParams = new URLSearchParams();
  
  if (params.companyId) {
    queryParams.append('companyId', params.companyId);
  }
  
  if (params.reportId) {
    queryParams.append('reportId', params.reportId);
  }
  
  const url = queryParams.toString() 
    ? `${DELAY_STAT_BASE}?${queryParams.toString()}`
    : DELAY_STAT_BASE;
    
  const data = await fetchAPI(url, { method: 'GET' });
  return data?.data?.delayStats || { stats: [], summary: {} };
}

/**
 * Récupère les statistiques de délai pour un report spécifique
 * @param {number} reportId
 * @returns {Promise<Object>}
 */
async function getDelayStatForReport(reportId) {
  const data = await fetchAPI(`${DELAY_STAT_BASE}/report/${reportId}`, { method: 'GET' });
  return data?.data?.delayStat || null;
}

/**
 * Récupère les statistiques de délai pour une entreprise
 * @param {number} companyId
 * @returns {Promise<Object>} { stats: [...], summary: {...} }
 */
async function getDelayStatsByCompany(companyId) {
  const data = await fetchAPI(`${DELAY_STAT_BASE}/company/${companyId}`, { method: 'GET' });
  return data?.data?.delayStats || { stats: [], summary: {} };
}

/**
 * Récupère le résumé global des délais
 * @param {number} companyId - Optionnel, filtre par entreprise
 * @returns {Promise<Object>}
 */
async function getDelaySummary(companyId = null) {
  const url = companyId 
    ? `${DELAY_STAT_BASE}/summary?companyId=${companyId}`
    : `${DELAY_STAT_BASE}/summary`;
    
  const data = await fetchAPI(url, { method: 'GET' });
  return data?.data?.summary || {};
}

export default {
  getDelayStats,
  getDelayStatForReport,
  getDelayStatsByCompany,
  getDelaySummary,
};

import { fetchAPI } from './api';

const SUMMARY_BASE = '/reports/summary';

async function getInProgress() {
  const data = await fetchAPI(`${SUMMARY_BASE}/in-progress`, { method: 'GET' });
  return data?.data || data || {};
}

async function getVerified() {
  const data = await fetchAPI(`${SUMMARY_BASE}/verified`, { method: 'GET' });
  return data?.data || data || {};
}

async function getCompleted() {
  const data = await fetchAPI(`${SUMMARY_BASE}/completed`, { method: 'GET' });
  return data?.data || data || {};
}

async function getGlobal() {
  const data = await fetchAPI(`${SUMMARY_BASE}/global`, { method: 'GET' });
  return data?.data || data || {};
}

export { getInProgress, getVerified, getCompleted, getGlobal };

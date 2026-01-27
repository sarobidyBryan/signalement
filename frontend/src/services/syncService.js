import { fetchAPI, unwrapList } from './api';

const SYNC_BASE = '/sync';

async function bidirectional() {
  return await fetchAPI(`${SYNC_BASE}/bidirectional`, { method: 'POST' });
}

async function postgresToFirebase() {
  return await fetchAPI(`${SYNC_BASE}/postgres-to-firebase`, { method: 'POST' });
}

async function firebaseToPostgres() {
  return await fetchAPI(`${SYNC_BASE}/firebase-to-postgres`, { method: 'POST' });
}

async function getLogs() {
  const data = await fetchAPI(`${SYNC_BASE}/logs`, { method: 'GET' });
  return unwrapList(data, 'logs');
}

export { bidirectional, postgresToFirebase, firebaseToPostgres, getLogs };

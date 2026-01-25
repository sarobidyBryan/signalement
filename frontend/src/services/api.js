const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8180/api';

class ApiError extends Error {
  constructor(message, status, errorCode) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
  }
}

async function fetchAPI(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    credentials: 'include',
  };

  try {
    const response = await fetch(url, config);
    const contentType = response.headers.get('content-type') || '';
    let data = null;

    if (contentType.includes('application/json')) {
      try {
        data = await response.json();
      } catch (error) {
        data = null;
      }
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      const message = data && typeof data === 'object' ? data.message : data || response.statusText;
      const errorCode = data && typeof data === 'object' ? data.errorCode : null;
      throw new ApiError(message || 'Une erreur est survenue', response.status, errorCode);
    }

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError('Erreur de connexion au serveur', 0, 'NETWORK_ERROR');
  }
}

function unwrapList(payload, key) {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload;
  if (key && Object.prototype.hasOwnProperty.call(payload, key)) {
    return payload[key];
  }
  return [];
}

export { fetchAPI, ApiError, unwrapList };

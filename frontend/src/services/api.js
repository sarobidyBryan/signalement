const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8180/api';

class ApiError extends Error {
  constructor(message, status, errorCode, url) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.url = url;
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
    const contentType = (response.headers.get('content-type') || '').toLowerCase();
    const responseText = await response.text();
    let data = responseText;

    if (contentType.includes('application/json')) {
      if (!responseText) {
        data = null;
      } else {
        try {
          data = JSON.parse(responseText);
        } catch (parseError) {
          const snippet = responseText.slice(0, 800);
          console.error(`[API] Invalid JSON response from ${url}`, snippet);
          throw new ApiError(
            `Le serveur a renvoyé un contenu inattendu pour ${url}: ${snippet}`,
            response.status,
            null,
            url
          );
        }
      }
    }

    if (!response.ok) {
      const bodySnippet = responseText ? responseText.slice(0, 800) : '';
      const messageParts = [`HTTP ${response.status} ${response.statusText}`];
      if (bodySnippet) messageParts.push(bodySnippet);
      throw new ApiError(
        messageParts.join(' - '),
        response.status,
        null,
        url
      );
    }

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError('Erreur de connexion au serveur', 0, 'NETWORK_ERROR', url);
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

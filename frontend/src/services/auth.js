import { fetchAPI, ApiError } from './api';

export const authService = {
  async login(email, password) {
    const response = await fetchAPI('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });

    // Backend returns ApiResponse with `status` and `data`
    if (response && response.status === 'success' && response.data) {
      // AuthenticationController returns data map with 'user' inside
      const user = response.data.user || response.data;
      localStorage.setItem('user', JSON.stringify(user));
      return response.data;
    }

    // If backend returns error object
    if (response && response.error) {
      const err = response.error;
      throw new ApiError(err.getMessage ? err.getMessage() : err.message || 'Login failed', 400, err.getCode ? err.getCode() : err.code);
    }

    throw new ApiError('Login failed', 400, 'LOGIN_FAILED');
  },

  async logout() {
    try {
      await fetchAPI('/auth/logout', { method: 'POST' });
    } finally {
      localStorage.removeItem('user');
    }
  },

  async getCurrentUser() {
    try {
      const response = await fetchAPI('/auth/current-user');
      if (response && response.status === 'success' && response.data) {
        localStorage.setItem('user', JSON.stringify(response.data));
        return response.data;
      }
      return null;
    } catch (error) {
      localStorage.removeItem('user');
      return null;
    }
  },

  getStoredUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!this.getStoredUser();
  },
};

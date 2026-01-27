import { fetchAPI } from './api';

export const assignationService = {
  async create(payload) {
    const response = await fetchAPI('/reports/assignations', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return response.data;
  },
};

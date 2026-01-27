import { fetchAPI } from './api';

export const assignationProgressService = {
  async create(payload) {
    const response = await fetchAPI('/reports/assignation-progress', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return response.data;
  },
};

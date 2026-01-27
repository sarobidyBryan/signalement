import { fetchAPI, unwrapList } from './api';

export const reportService = {
  async getAll(params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const endpoint = `/reports${queryString ? `?${queryString}` : ''}`;
    const response = await fetchAPI(endpoint);
    return unwrapList(response.data, 'reports');
  },

  async getById(id) {
    const response = await fetchAPI(`/reports/${id}`);
    return response.data;
  },

  async getDetail(id) {
    const response = await fetchAPI(`/reports/${id}/detail`);
    return response.data?.reportDetail ?? null;
  },

  async updateStatus(reportId, statusId) {
    const response = await fetchAPI(`/reports/${reportId}/status`, {
      method: 'POST',
      body: JSON.stringify({ statusId }),
    });
    return response.data;
  },

  async create(reportData) {
    const response = await fetchAPI('/reports', {
      method: 'POST',
      body: JSON.stringify(reportData),
    });
    return response.data;
  },

  async update(id, reportData) {
    const response = await fetchAPI(`/reports/${id}`, {
      method: 'PUT',
      body: JSON.stringify(reportData),
    });
    return response.data;
  },

  async delete(id) {
    const response = await fetchAPI(`/reports/${id}`, {
      method: 'DELETE',
    });
    return response.data;
  },
};

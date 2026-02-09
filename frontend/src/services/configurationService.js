import { fetchAPI, unwrapList } from './api';

export const configurationService = {
  async getAll() {
    const response = await fetchAPI('/configuration');
    return unwrapList(response.data, 'configurations');
  },

  async getByKey(key) {
    const response = await fetchAPI(`/configuration/${encodeURIComponent(key)}`);
    return response.data;
  },

  async create(data) {
    const response = await fetchAPI('/configuration', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async update(key, data) {
    const response = await fetchAPI(`/configuration/${encodeURIComponent(key)}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async delete(key) {
    const response = await fetchAPI(`/configuration/${encodeURIComponent(key)}`, {
      method: 'DELETE',
    });
    return response.data;
  },
};

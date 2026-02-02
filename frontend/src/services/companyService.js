import { fetchAPI, unwrapList } from './api';

export const companyService = {
  async getAll() {
    const response = await fetchAPI('/companies');
    return unwrapList(response.data, 'companies');
  },

  async getById(id) {
    const response = await fetchAPI(`/companies/${id}`);
    return response.data;
  },

  async create(data) {
    const response = await fetchAPI('/companies', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async update(id, data) {
    const response = await fetchAPI(`/companies/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async delete(id) {
    const response = await fetchAPI(`/companies/${id}`, {
      method: 'DELETE',
    });
    return response.data;
  },
};

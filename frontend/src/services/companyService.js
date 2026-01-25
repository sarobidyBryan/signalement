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
};

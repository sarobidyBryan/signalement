import { fetchAPI, unwrapList } from './api';

export const userService = {
  async getAll() {
    const response = await fetchAPI('/users');
    return unwrapList(response.data, 'users');
  },

  async getById(id) {
    const response = await fetchAPI(`/users/${id}`);
    return response.data;
  },
};

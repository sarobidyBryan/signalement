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

  async getBlocked() {
    const response = await fetchAPI('/users/blocked?perPage=100');
    return unwrapList(response.data, 'users');
  },

  async getRoles() {
    const response = await fetchAPI('/users/roles');
    return unwrapList(response.data, 'roles');
  },

  async create(data) {
    const response = await fetchAPI('/users', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async updateStatus(id, statusCode) {
    const response = await fetchAPI(`/users/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ statusCode }),
    });
    return response.data;
  },

  async update(id, data) {
    const response = await fetchAPI(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return response.data;
  },

  async delete(id) {
    const response = await fetchAPI(`/users/${id}`, {
      method: 'DELETE',
    });
    return response.data;
  },
};

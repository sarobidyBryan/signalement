import { fetchAPI, unwrapList } from './api';

export const statusService = {
  async getAll() {
    const response = await fetchAPI('/statuses');
    return unwrapList(response.data, 'statuses');
  },
};

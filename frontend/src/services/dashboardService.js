import { fetchAPI } from './api';

export const dashboardService = {
  async getStats() {
    const response = await fetchAPI('/dashboard/stats');
    return response.data;
  },

  async getRecentReports(limit = 10) {
    const response = await fetchAPI(`/dashboard/recent-reports?limit=${limit}`);
    return response.data;
  },

  async getReportsByStatus() {
    const response = await fetchAPI('/dashboard/reports-by-status');
    return response.data;
  },
};

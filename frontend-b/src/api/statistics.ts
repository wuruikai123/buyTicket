import request from '@/utils/request'

export const statisticsApi = {
  getDashboardData() {
    return request.get('/admin/statistics/dashboard')
  },

  getSalesReport(params: any) {
    return request.get('/admin/statistics/sales', { params })
  },

  getUserAnalysis(params: any) {
    return request.get('/admin/statistics/user-analysis')
  }
}


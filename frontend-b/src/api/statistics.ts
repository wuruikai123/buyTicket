import request from '@/utils/request'

export const statisticsApi = {
  getDashboardData() {
    return request.get('/api/v1/admin/stats/dashboard')
  },

  getSalesReport(params: any) {
    // 后端暂未实现
    // return request.get('/stats/sales', { params })
    return Promise.resolve({
      summary: { totalSales: 0, ticketSales: 0, mallSales: 0, totalOrders: 0 },
      dailyData: []
    })
  },

  getUserAnalysis(params: any) {
    // 后端暂未实现
    // return request.get('/stats/user-analysis', { params })
    return Promise.resolve({
      totalUsers: 0,
      activeUsers: 0,
      newUsers: 0,
      userGrowth: [],
      consumptionRank: []
    })
  }
}


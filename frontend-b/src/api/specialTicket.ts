import request from '@/utils/request'

export const specialTicketApi = {
  // 批量生成特殊票券
  batchGenerate(count: number) {
    return request.post('/admin/special-ticket/batch-generate', null, {
      params: { count }
    })
  },

  // 获取列表
  getList(params: any) {
    return request.get('/admin/special-ticket/list', { params })
  },

  // 获取统计信息
  getStatistics() {
    return request.get('/admin/special-ticket/statistics')
  },

  // 导出票券
  export(status?: number) {
    return request.get('/admin/special-ticket/export', {
      params: { status }
    })
  },

  // 删除票券
  delete(id: number) {
    return request.delete(`/admin/special-ticket/${id}`)
  }
}

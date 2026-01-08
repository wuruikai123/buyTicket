import request from '@/utils/request'

export const exhibitionApi = {
  getList(params: any) {
    return request.get('/api/v1/admin/exhibition/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/api/v1/admin/exhibition/${id}`)
  },

  create(data: any) {
    return request.post('/api/v1/admin/exhibition/create', data)
  },

  update(data: any) {
    return request.post('/api/v1/admin/exhibition/update', data)
  },

  delete(id: number) {
    return request.delete(`/api/v1/admin/exhibition/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/api/v1/admin/exhibition/${id}/status/${status}`)
  }
}


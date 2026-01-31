import request from '@/utils/request'

export const exhibitionApi = {
  getList(params: any) {
    return request.get('/admin/exhibition/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/admin/exhibition/${id}`)
  },

  create(data: any) {
    return request.post('/admin/exhibition/create', data)
  },

  update(data: any) {
    return request.post('/admin/exhibition/update', data)
  },

  delete(id: number) {
    return request.delete(`/admin/exhibition/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/admin/exhibition/${id}/status/${status}`)
  }
}


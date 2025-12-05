import request from '@/utils/request'

export const exhibitionApi = {
  getList(params: any) {
    return request.get('/exhibition/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/exhibition/${id}`)
  },

  create(data: any) {
    return request.post('/exhibition/create', data)
  },

  update(data: any) {
    return request.post('/exhibition/update', data)
  },

  delete(id: number) {
    return request.delete(`/exhibition/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/exhibition/${id}/status/${status}`)
  }
}


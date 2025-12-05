import request from '@/utils/request'

export const productApi = {
  getList(params: any) {
    return request.get('/product/list', { params })
  },

  getDetail(id: number) {
    return request.get(`/product/${id}`)
  },

  create(data: any) {
    return request.post('/product/create', data)
  },

  update(data: any) {
    return request.post('/product/update', data)
  },

  delete(id: number) {
    return request.delete(`/product/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/product/${id}/status/${status}`)
  }
}


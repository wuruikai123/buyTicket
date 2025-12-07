import request from '@/utils/request'

// ============ 图片上传 ============
export interface UploadResult {
  url: string
  filename: string
}

export const uploadApi = {
  // 上传单张图片
  uploadImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<any, UploadResult>('/admin/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传多张图片
  uploadImages(files: File[]) {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return request.post<any, UploadResult[]>('/admin/upload/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// ============ 展览管理 ============
export interface AdminExhibition {
  id?: number
  name: string
  shortDesc: string
  description: string
  startDate: string
  endDate: string
  price: number
  coverImage: string
  detailImages?: string[]
  tags?: string[]
  status: number
}

export const adminExhibitionApi = {
  getList(params?: { page?: number; size?: number; name?: string; status?: number }) {
    return request.get<any, { records: AdminExhibition[]; total: number }>('/admin/exhibition/list', { params })
  },

  getDetail(id: number) {
    return request.get<any, AdminExhibition>(`/admin/exhibition/${id}`)
  },

  create(data: AdminExhibition) {
    return request.post('/admin/exhibition/create', data)
  },

  update(data: AdminExhibition) {
    return request.post('/admin/exhibition/update', data)
  },

  delete(id: number) {
    return request.delete(`/admin/exhibition/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/admin/exhibition/${id}/status/${status}`)
  }
}

// ============ 商品管理 ============
export interface AdminProduct {
  id?: number
  name: string
  description: string
  price: number
  stock: number
  coverImage: string
  status: number
}

export const adminProductApi = {
  getList(params?: { page?: number; size?: number; name?: string; status?: number }) {
    return request.get<any, { records: AdminProduct[]; total: number }>('/admin/product/list', { params })
  },

  getDetail(id: number) {
    return request.get<any, AdminProduct>(`/admin/product/${id}`)
  },

  create(data: AdminProduct) {
    return request.post('/admin/product/create', data)
  },

  update(data: AdminProduct) {
    return request.post('/admin/product/update', data)
  },

  delete(id: number) {
    return request.delete(`/admin/product/${id}`)
  },

  updateStatus(id: number, status: number) {
    return request.put(`/admin/product/${id}/status/${status}`)
  }
}

// ============ 库存管理 ============
export interface TicketInventory {
  id?: number
  exhibitionId: number
  exhibitionName?: string
  ticketDate: string
  timeSlot: string
  totalCount: number
  soldCount?: number
}

export interface BatchInventoryCreate {
  exhibitionId: number
  startDate: string
  endDate: string
  timeSlots: string[]
  totalCount: number
}

export const adminInventoryApi = {
  getList(params?: { exhibitionId?: number; startDate?: string; endDate?: string; page?: number; size?: number }) {
    return request.get<any, { records: TicketInventory[]; total: number }>('/admin/ticket/inventory/list', { params })
  },

  create(data: TicketInventory) {
    return request.post('/admin/ticket/inventory/create', data)
  },

  // 批量创建库存
  batchCreate(data: BatchInventoryCreate) {
    return request.post('/admin/ticket/inventory/batch-create', data)
  },

  update(data: TicketInventory) {
    return request.post('/admin/ticket/inventory/update', data)
  },

  delete(id: number) {
    return request.delete(`/admin/ticket/inventory/${id}`)
  }
}

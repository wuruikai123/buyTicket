import request from '@/utils/request'

export interface TicketAvailability {
    remainingCount: number
}

export interface TicketItemRequest {
    date: string
    timeSlot: string
    quantity: number
    unitPrice: number
}

export interface TicketBuyerRequest {
    name: string
    idCard: string
}

export interface TicketOrderCreateRequest {
    exhibitionId: number
    contactName: string
    contactPhone: string
    totalAmount: number
    items: TicketItemRequest[]
    buyers: TicketBuyerRequest[]
}

export const ticketApi = {
    // 查询余票
    getAvailability(params: { exhibitionId: number; date: string; timeSlot: string }) {
        return request.get<any, TicketAvailability>('/ticket/availability', { params })
    },

    // 创建门票订单
    createOrder(data: TicketOrderCreateRequest) {
        return request.post<any, { orderId: string | number }>('/order/ticket/create', data)
    },
    
    // 支付
    pay(data: { orderId: string | number; type: 'ticket' | 'mall'; password: string }) {
        return request.post('/order/pay', data)
    },

    // 获取门票订单列表
    getOrderList() {
        return request.get<any, any[]>('/order/ticket/list')
    },

    // 获取订单详情
    getOrderDetail(id: number) {
        return request.get<any, any>(`/order/ticket/${id}`)
    },

    // 取消订单
    cancelOrder(id: number) {
        return request.put(`/order/ticket/${id}/cancel`)
    },

    // 按订单号核销1张待使用子票
    verifyOrderByOrderNo(orderNo: string) {
        return request.post('/order/ticket/verify', { orderNo })
    },

    // 根据订单号获取订单详情
    getOrderByOrderNo(orderNo: string) {
        return request.get<any, any>(`/order/ticket/by-order-no/${orderNo}`)
    },

    // 删除订单
    deleteOrder(id: number) {
        return request.delete(`/order/ticket/${id}`)
    },

    // 申请退款（支持子票维度）
    requestRefund(id: number, ticketItemIds?: number[]) {
        return request.post(`/order/ticket/${id}/request-refund`, { ticketItemIds: ticketItemIds || [] })
    },

    // 取消退款申请（支持子票维度）
    cancelRefund(id: number, ticketItemIds?: number[]) {
        return request.post(`/order/ticket/${id}/cancel-refund`, { ticketItemIds: ticketItemIds || [] })
    }
}

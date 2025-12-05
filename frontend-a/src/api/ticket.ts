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

export interface TicketOrderCreateRequest {
    exhibitionId: number
    contactName: string
    contactPhone: string
    totalAmount: number
    items: TicketItemRequest[]
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
    }
}

import request from '@/utils/request'

export interface Product {
    id: number
    name: string
    description?: string
    price: number
    stock?: number
    coverImage?: string
    status?: number
}

export interface CartItem {
    id: number
    productId: number
    productName: string
    price: number
    quantity: number
    coverImage: string
}

export const mallApi = {
    // 商品列表
    getProductList(params?: { page?: number; size?: number; keyword?: string }) {
        return request.get<any, { records: Product[]; total: number }>('/mall/product/list', { params })
    },

    // 商品详情
    getProductDetail(id: number) {
        return request.get<any, Product>(`/mall/product/${id}`)
    },

    // 购物车列表
    getCartList() {
        return request.get<any, CartItem[]>('/mall/cart/list')
    },

    // 添加到购物车
    addToCart(data: { productId: number; quantity: number }) {
        return request.post('/mall/cart/add', data)
    },

    // 更新购物车
    updateCart(data: { id: number; quantity: number }) {
        return request.put('/mall/cart/update', data)
    },

    // 删除购物车项
    removeCartItem(ids: string) {
        return request.delete('/mall/cart/remove', { params: { ids } })
    },

    // 创建商城订单
    createOrder(data: any) {
        return request.post<any, { orderId: string | number }>('/order/mall/create', data)
    },

    // 获取商城订单列表
    getOrderList() {
        return request.get<any, any[]>('/order/mall/list')
    },

    // 获取订单详情
    getOrderDetail(id: number) {
        return request.get<any, any>(`/order/mall/${id}`)
    },

    // 取消订单
    cancelOrder(id: number) {
        return request.put(`/order/mall/${id}/cancel`)
    },

    // 支付订单
    pay(data: { orderId: string | number; type: string; password: string }) {
        return request.post('/order/pay', data)
    }
}

import request from '@/utils/request'

export interface User {
    id: number
    username: string
    uid?: string
    avatar?: string
    phone?: string
    balance?: number
}

export interface Address {
    id: number
    userId?: number
    name: string
    phone: string
    province: string
    city: string
    district: string
    detail: string
    isDefault: boolean
}

export const userApi = {
    // 获取当前用户信息
    getUserInfo() {
        return request.get<any, User>('/user/info')
    },

    // 更新用户信息
    updateUserInfo(data: Partial<User>) {
        return request.put('/user/update', data)
    },

    // 修改密码
    changePassword(data: { oldPassword: string; newPassword: string }) {
        return request.put('/user/password', data)
    },

    // 获取地址列表
    getAddressList() {
        return request.get<any, Address[]>('/user/address/list')
    },

    // 添加地址
    addAddress(data: Omit<Address, 'id'>) {
        return request.post('/user/address/add', data)
    },

    // 更新地址
    updateAddress(data: Address) {
        return request.put('/user/address/update', data)
    },

    // 删除地址
    deleteAddress(id: number) {
        return request.delete(`/user/address/${id}`)
    },

    // 设置默认地址
    setDefaultAddress(id: number) {
        return request.put(`/user/address/${id}/default`)
    }
}

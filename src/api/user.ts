import request from '@/utils/request'

export interface User {
    id: number
    username: string
    uid?: string
    avatar?: string
    balance?: number
}

export const userApi = {
    // 获取当前用户信息 (目前后端还没实现专门的 /info，暂时用 list 代替或 mock)
    // 假设后端补充了 /user/info
    getUserInfo() {
        // 由于后端只有 /user/list，我们暂时 mock 一下或者调用 list 取第一个
        return request.get<any, User[]>('/user/list').then(list => list[0])
    },
    
    // 登录注册暂略
}

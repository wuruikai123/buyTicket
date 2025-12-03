import request from '@/utils/request'

export interface Exhibition {
    id: number
    name: string
    shortDesc?: string
    description?: string
    startDate?: string
    endDate?: string
    status?: number
    price?: number
    coverImage?: string
    tags?: string
    // 兼容前端可能的字段名
    date?: string 
}

export const exhibitionApi = {
    // 获取当前主推展览
    getCurrent() {
        return request.get<any, Exhibition>('/exhibition/current')
    },

    // 获取展览列表
    getList(status?: 'ongoing' | 'upcoming') {
        return request.get<any, Exhibition[]>('/exhibition/list', {
            params: { status }
        })
    },

    // 获取详情
    getDetail(id: number) {
        return request.get<any, Exhibition>(`/exhibition/${id}`)
    }
}

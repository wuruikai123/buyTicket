export type OrderRecord = {
  id: number
  orderNo: string
  exhibition: string
  validTime: string
  buyer: string
  verifyTime?: string
}

const mockOrders: OrderRecord[] = [
  {
    id: 1,
    orderNo: '12345678900011',
    exhibition: 'XXXXXXXXXXXXXXXX展',
    validTime: '2025年6月9日 14:30-15:30',
    buyer: '137777581964',
    verifyTime: '2025年6月10日 14:59'
  },
  {
    id: 2,
    orderNo: '1234589663333',
    exhibition: 'XXXXXXXXXXXXXXXX展',
    validTime: '2025年6月9日 xx:xx-xx:xx',
    buyer: 'xxxxxxxxxxx',
    verifyTime: '2025年6月9日 xx:xx'
  },
  {
    id: 3,
    orderNo: '9999999999999',
    exhibition: 'XXXXXXXXXXXXXXXX展',
    validTime: '2025年6月9日 xx:xx-xx:xx',
    buyer: 'xxxxxxxxxxx',
    verifyTime: '2025年6月9日 xx:xx'
  }
]

export function queryOrder(orderNo: string) {
  if (!orderNo) return null
  return mockOrders.find((item) => item.orderNo === orderNo) || null
}

export function getTodayVerifiedCount() {
  return 16
}

export function listRecordsByDate(year: number, month: number, day: number) {
  return mockOrders.filter(() => {
    // 模拟筛选，真实项目应调用后端接口
    return true
  })
}


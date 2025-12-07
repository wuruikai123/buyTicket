/**
 * 购物车计算工具函数
 */

export interface CartItemVO {
  id: number
  productId: number
  productName: string
  price: number
  quantity: number
  coverImage: string
  selected?: boolean
}

/**
 * 计算单个商品的小计
 * @param price 商品单价
 * @param quantity 数量
 * @returns 小计金额
 */
export function calculateSubtotal(price: number, quantity: number): number {
  return price * quantity
}

/**
 * 计算购物车总金额
 * @param items 购物车商品列表
 * @returns 总金额
 */
export function calculateTotal(items: CartItemVO[]): number {
  return items.reduce((sum, item) => sum + calculateSubtotal(item.price, item.quantity), 0)
}

/**
 * 计算已选中商品的总金额
 * @param items 购物车商品列表
 * @returns 已选中商品的总金额
 */
export function calculateSelectedTotal(items: CartItemVO[]): number {
  return items
    .filter(item => item.selected)
    .reduce((sum, item) => sum + calculateSubtotal(item.price, item.quantity), 0)
}

/**
 * 计算已选中商品的数量
 * @param items 购物车商品列表
 * @returns 已选中商品数量
 */
export function calculateSelectedCount(items: CartItemVO[]): number {
  return items.filter(item => item.selected).length
}

/**
 * 获取购物车商品总数
 * @param items 购物车商品列表
 * @returns 商品总数
 */
export function getCartItemCount(items: CartItemVO[]): number {
  return items.length
}

/**
 * 切换全选状态
 * @param items 购物车商品列表
 * @param selectAll 是否全选
 * @returns 更新后的商品列表
 */
export function toggleSelectAll(items: CartItemVO[], selectAll: boolean): CartItemVO[] {
  return items.map(item => ({ ...item, selected: selectAll }))
}

/**
 * 检查是否全部选中
 * @param items 购物车商品列表
 * @returns 是否全选
 */
export function isAllSelected(items: CartItemVO[]): boolean {
  return items.length > 0 && items.every(item => item.selected)
}

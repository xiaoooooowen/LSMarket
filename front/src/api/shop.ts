import request from '@/utils/request'
import type { Shop, ShopType } from '@/types/shop'
import type { PageResult } from '@/types/common'

export function getShopList(typeId?: number, current = 1, size = 10) {
  return request.get<PageResult<Shop>>('/shop/list', {
    params: { typeId, current, size }
  })
}

export function getShopById(id: number) {
  return request.get<Shop>(`/shop/${id}`)
}

export function getShopTypes() {
  return request.get<ShopType[]>('/shop-type/list')
}

export function getShopsOfCurrent() {
  return request.get<ShopType[]>('/shop-type/list')
}

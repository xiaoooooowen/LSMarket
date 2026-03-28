import request from '@/utils/request'
import type { Shop, ShopType } from '@/types/shop'

export function getShopList(typeId?: number, current = 1, size = 10) {
  if (typeof typeId === 'number') {
    return request.get<Shop[]>('/shop/of/type', {
      params: { typeId, current }
    }).then((records: any) => ({
      records,
      total: Array.isArray(records) ? records.length : 0,
      pages: 1,
      current,
      size
    }))
  }
  return request.get<Shop[]>('/shop/of/name', {
    params: { current }
  }).then((records: any) => ({
    records,
    total: Array.isArray(records) ? records.length : 0,
    pages: 1,
    current,
    size
  }))
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

import request from '@/utils/request'
import type { Voucher, VoucherOrder } from '@/types/voucher'

export function getVoucherList(shopId: number) {
  return request.get<Voucher[]>(`/voucher/list/${shopId}`)
}

export function addSeckillVoucher(voucher: Partial<Voucher>) {
  return request.post('/voucher/seckill', voucher)
}

export function seckillVoucher(voucherId: number) {
  return request.post<VoucherOrder>(`/voucher-order/seckill/${voucherId}`)
}

export function getVoucherOrder(_voucherId: number) {
  return Promise.reject(new Error('订单查询接口暂未开放'))
}

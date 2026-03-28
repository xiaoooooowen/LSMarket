export interface Voucher {
  id: number
  shopId: number
  title: string
  subTitle: string
  rules: string
  payValue: number
  actualValue: number
  type: number
  status: number
  createTime: string
  updateTime: string
  stock?: number
  beginTime?: string
  endTime?: string
}

export interface VoucherOrder {
  id: number
  userId: number
  voucherId: number
  orderId: number
  status: number
  createTime: string
  updateTime: string
}

export interface Shop {
  id: number
  name: string
  typeId: number
  images: string
  area: string
  address: string
  x: number
  y: number
  avgPrice: number
  sold: number
  comments: number
  score: number
  openHours: string
  createTime: string
  updateTime: string
}

export interface ShopType {
  id: number
  name: string
  icon: string
  sort: number
}

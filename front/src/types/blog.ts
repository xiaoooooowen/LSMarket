export interface Blog {
  id: number
  shopId: number
  userId: number
  title: string
  images: string
  content: string
  type: number
  thumb: number
  createTime: string
  updateTime: string
  name?: string
  icon?: string
  isLike?: boolean
}

export interface BlogComment {
  id: number
  userId: number
  blogId: number
  parentId: number
  content: string
  createTime: string
  updateTime: string
}

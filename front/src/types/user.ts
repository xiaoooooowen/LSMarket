export interface UserDTO {
  id: number
  nickName: string
  icon: string
}

export interface User {
  id: number
  phone: string
  password: string
  nickName: string
  icon: string
  createTime: string
  updateTime: string
}

export interface UserInfo {
  userId: number
  city: string
  introduce: string
  fans: number
  followee: number
  likes: number
  createTime: string
  updateTime: string
}

export interface LoginFormDTO {
  phone: string
  code: string
  password?: string
}

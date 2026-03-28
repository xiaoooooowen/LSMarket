import request from '@/utils/request'
import type { UserDTO, LoginFormDTO } from '@/types/user'
import type { UserInfo } from '@/types/user'

export function sendCode(phone: string) {
  return request.post('/user/code', { phone })
}

export function login(data: LoginFormDTO) {
  return request.post<{ token: string }>('/user/login', data)
}

export function getCurrentUser() {
  return request.get<UserDTO>('/user/me')
}

export function getUserInfo(userId: number) {
  return request.get<UserInfo>(`/user/info/${userId}`)
}

export function sign() {
  return request.post('/user/sign')
}

export function isSign() {
  return request.get<boolean>('/user/sign')
}

export function signCount() {
  return request.get<number>('/user/sign/count')
}

export function logout() {
  return request.post('/user/logout')
}

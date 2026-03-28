import request from '@/utils/request'
import type { UserDTO, LoginFormDTO } from '@/types/user'
import type { UserInfo } from '@/types/user'

export function sendCode(phone: string) {
  return request.post('/user/code', null, {
    params: { phone }
  })
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
  // Backend has no GET /user/sign endpoint.
  // Keep UI stable by returning false by default.
  return Promise.resolve(false)
}

export function signCount() {
  return request.get<number>('/user/sign/count')
}

export function logout() {
  return request.post('/user/logout')
}

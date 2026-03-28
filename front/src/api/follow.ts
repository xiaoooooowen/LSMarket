import request from '@/utils/request'

export function followUser(followUserId: number, isFollow: boolean) {
  return request.put(`/follow/${followUserId}/${isFollow}`)
}

export function isFollow(followUserId: number) {
  return request.get<boolean>(`/follow/or/not/${followUserId}`)
}

export function getFollowCommons(followUserId: number) {
  return request.get<number[]>(`/follow/common/${followUserId}`)
}

export function getFollowList() {
  return request.get('/follow/list')
}

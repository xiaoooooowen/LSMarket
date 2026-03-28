import request from '@/utils/request'
import type { Blog } from '@/types/blog'
import type { ScrollResult } from '@/types/common'

export function getBlogList(current = 1) {
  return request.get<Blog[]>('/blog/hot', {
    params: { current }
  })
}

export function getBlogById(id: number) {
  return request.get<Blog>(`/blog/${id}`)
}

export function getBlogOfUser(userId: number, current = 1) {
  return request.get<Blog[]>(`/blog/of/user`, {
    params: { id: userId, current }
  })
}

export function getBlogOfFollow(lastId?: number, offset = 0) {
  return request.get<ScrollResult<Blog>>('/blog/of/follow', {
    params: { lastId, offset }
  })
}

export function likeBlog(id: number) {
  return request.put(`/blog/like/${id}`)
}

export function saveBlog(blog: Partial<Blog>) {
  return request.post<Blog>('/blog', blog)
}

export function getBlogComments(_id: number, _current = 1) {
  // Backend comments endpoints are not implemented yet.
  return Promise.resolve([])
}

export function addComment(_blogId: number, _content: string, _parentId?: number) {
  return Promise.reject(new Error('评论接口暂未开放'))
}

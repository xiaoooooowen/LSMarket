import request from '@/utils/request'
import type { Blog } from '@/types/blog'
import type { ScrollResult } from '@/types/common'

export function getBlogList(current = 1) {
  return request.get<{ records: Blog[] }>('/blog/hot', {
    params: { current }
  })
}

export function getBlogById(id: number) {
  return request.get<Blog>(`/blog/${id}`)
}

export function getBlogOfUser(userId: number, current = 1) {
  return request.get<{ records: Blog[] }>(`/blog/of/user`, {
    params: { userId, current }
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

export function getBlogComments(id: number, current = 1) {
  return request.get(`/blog/comments/${id}`, {
    params: { current }
  })
}

export function addComment(blogId: number, content: string, parentId?: number) {
  return request.post('/blog/comment', { blogId, content, parentId })
}

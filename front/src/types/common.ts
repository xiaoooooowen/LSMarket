export interface Result<T = unknown> {
  success: boolean
  data: T
  errorMessage?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
  current: number
  size: number
}

export interface ScrollResult<T> {
  list: T[]
  minTime: number
  offset: number
}

import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { Result } from '@/types/common'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['authorization'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const res = response.data as Result
    if (res.success) {
      return response
    } else {
      ElMessage.error(res.errorMessage || '请求失败')
      return Promise.reject(new Error(res.errorMessage || '请求失败'))
    }
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function unwrap<T>(promise: Promise<AxiosResponse<Result<T>>>) {
  return promise.then((response) => response.data.data)
}

const http = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(request.get(url, config))
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(request.post(url, data, config))
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(request.put(url, data, config))
  },
  delete<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(request.delete(url, config))
  }
}

export default http

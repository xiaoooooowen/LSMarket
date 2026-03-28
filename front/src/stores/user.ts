import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserDTO } from '@/types/user'
import { login as loginApi, getCurrentUser, sendCode as sendCodeApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserDTO | null>(null)
  const isLoggedIn = ref(!!token.value)

  async function login(phone: string, code: string) {
    const res = await loginApi({ phone, code })
    token.value = res.token
    localStorage.setItem('token', res.token)
    isLoggedIn.value = true
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      userInfo.value = await getCurrentUser()
    } catch {
      logout()
    }
  }

  async function sendCode(phone: string) {
    await sendCodeApi(phone)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    fetchUserInfo,
    sendCode,
    logout
  }
})

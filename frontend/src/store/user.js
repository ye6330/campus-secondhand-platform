import { defineStore } from 'pinia'
import request from '../utils/request'

// 用户状态管理（Pinia）
// 保存当前登录用户的信息，并提供注册、登录、获取当前用户等方法
export const useUserStore = defineStore('user', {
  // 状态：初始值从 localStorage 读取，刷新页面后不会丢失
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    nickname: localStorage.getItem('nickname') || '',
    phone: localStorage.getItem('phone') || '',
    avatar: localStorage.getItem('avatar') || '',
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    // 注册
    async register(data) {
      const res = await request.post('/api/users/register', data)
      if (res.code === 200) {
        return res
      }
      throw new Error(res.message)
    },
    // 登录
    async login(data) {
      const res = await request.post('/api/users/login', data)
      if (res.code === 200) {
        // 保存 token 和用户信息到 Pinia 和 localStorage
        this.token = res.data.token
        this.userId = res.data.userId
        this.username = res.data.username
        this.nickname = res.data.nickname
        this.role = res.data.role || ''
        this.avatar = res.data.avatar || ''
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userId', res.data.userId)
        localStorage.setItem('username', res.data.username)
        localStorage.setItem('nickname', res.data.nickname)
        localStorage.setItem('avatar', res.data.avatar || '')
        return res
      }
      throw new Error(res.message)
    },
    // 获取当前登录用户信息（从后端 /me 接口获取，不是从本地缓存）
    async fetchCurrentUser() {
      const res = await request.get('/api/users/me')
      if (res.code === 200) {
        this.userId = res.data.id
        this.username = res.data.username
        this.nickname = res.data.nickname
        this.phone = res.data.phone || ''
        this.avatar = res.data.avatar || ''
        this.role = res.data.role || ''
        localStorage.setItem('userId', res.data.id)
        localStorage.setItem('username', res.data.username)
        localStorage.setItem('nickname', res.data.nickname || '')
        localStorage.setItem('phone', res.data.phone || '')
        localStorage.setItem('avatar', res.data.avatar || '')
        localStorage.setItem('role', res.data.role || '')
        return res
      }
      throw new Error(res.message)
    },
    // 退出登录：清空所有登录信息
    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.nickname = ''
      this.phone = ''
      this.avatar = ''
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('nickname')
      localStorage.removeItem('phone')
      localStorage.removeItem('avatar')
      localStorage.removeItem('role')
    }
  }
})

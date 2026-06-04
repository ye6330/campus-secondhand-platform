import axios from 'axios'

// Axios 请求工具
// 所有前端请求都经过这里，自动带上 token 并统一处理 401
const request = axios.create({
  // 前端统一请求 gateway，由 gateway 再转发到具体服务
  baseURL: 'http://localhost:9000',
  timeout: 10000
})

// 请求拦截器：每次请求前自动带上 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    // 把 token 放进请求头，后端 JwtAuthenticationFilter 会解析它
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理后端返回的数据和错误
request.interceptors.response.use(
  response => response.data,
  error => {
    // 如果后端返回 401（未登录或 token 失效），自动清空登录信息并跳回登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('nickname')
      localStorage.removeItem('phone')
      localStorage.removeItem('avatar')
      localStorage.removeItem('role')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request

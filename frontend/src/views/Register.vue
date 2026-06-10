<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import request from '../utils/request'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  captchaCode: ''
})

const loading = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')

const loadCaptcha = async () => {
  try {
    const res = await request.get('/api/users/captcha')
    if (res.code === 200) {
      captchaKey.value = res.data.captchaKey
      captchaImage.value = res.data.imageBase64
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('验证码加载失败')
  }
}

onMounted(() => {
  loadCaptcha()
})

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.nickname || !form.value.phone || !form.value.captchaCode) {
    ElMessage.warning('请填写必要信息')
    return
  }
  if (!/^1\d{10}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  loading.value = true
  try {
    const res = await userStore.register({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname,
      phone: form.value.phone,
      captchaKey: captchaKey.value,
      captchaCode: form.value.captchaCode
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
    loadCaptcha()
    form.value.captchaCode = ''
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-icon">
            <el-icon :size="36"><School /></el-icon>
          </div>
          <h2>创建账号</h2>
          <p>加入校园二手交易平台</p>
        </div>
        <el-form :model="form" class="register-form">
          <el-form-item>
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.nickname"
              placeholder="昵称"
              :prefix-icon="EditPen"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（至少6位）"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.phone"
              placeholder="手机号"
              :prefix-icon="Iphone"
              maxlength="11"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <div class="captcha-row">
              <el-input
                v-model="form.captchaCode"
                placeholder="请输入验证码"
                :prefix-icon="Key"
                size="large"
              />
              <img
                v-if="captchaImage"
                :src="captchaImage"
                class="captcha-image"
                alt="验证码"
                @click="loadCaptcha"
              />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="register-footer">
          已有账号？
          <router-link to="/login">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  position: relative;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: #a18cd1;
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: #fbc2eb;
  bottom: -50px;
  left: -50px;
  animation: float 10s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: #a6c1ee;
  top: 30%;
  left: 20%;
  animation: float 12s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

.register-container {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  padding: 20px;
}

.register-card {
  width: 440px;
  padding: 30px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  line-height: 64px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.register-header h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 6px;
}

.register-header p {
  font-size: 14px;
  color: #909399;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #667eea inset;
}

.captcha-row {
  width: 100%;
  display: flex;
  gap: 12px;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  background: #fff;
  object-fit: cover;
}

.register-form :deep(.el-input__inner) {
  height: 42px;
}

.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  letter-spacing: 4px;
}

.register-btn:hover {
  background: linear-gradient(135deg, #5a6fd6, #6a4198);
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-top: -6px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-footer a:hover {
  color: #764ba2;
}
</style>

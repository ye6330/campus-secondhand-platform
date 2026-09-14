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
  <div class="auth-page">
    <aside class="auth-aside">
      <div class="aside-brand">
        <span class="brand-mark">校</span>
        <span class="brand-name">校园二手交易平台</span>
      </div>

      <div class="aside-body">
        <h1>注册之后，<br />就能开始<br />发布和淘好物</h1>
        <p>填写基本资料即可加入平台。注册信息仅用于校内交易身份识别。</p>
        <ul class="aside-points">
          <li><span>01</span>用户名用于登录，昵称用于展示</li>
          <li><span>02</span>手机号用于交易联系</li>
          <li><span>03</span>头像和资料可以稍后在个人中心补充</li>
        </ul>
      </div>

      <div class="aside-foot">校内闲置流转 · 仅限同学之间使用</div>
    </aside>

    <main class="auth-main">
      <div class="auth-form-wrap">
        <header class="auth-head">
          <span class="form-brand">校</span>
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </header>

        <el-form :model="form" class="auth-form">
          <div class="field-grid">
            <el-form-item>
              <label class="field-label">用户名</label>
              <el-input v-model="form.username" placeholder="用于登录" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item>
              <label class="field-label">昵称</label>
              <el-input v-model="form.nickname" placeholder="用于展示" :prefix-icon="EditPen" size="large" />
            </el-form-item>
            <el-form-item>
              <label class="field-label">密码</label>
              <el-input v-model="form.password" type="password" placeholder="至少6位" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
            <el-form-item>
              <label class="field-label">确认密码</label>
              <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
          </div>

          <el-form-item>
            <label class="field-label">手机号</label>
            <el-input v-model="form.phone" placeholder="用于交易联系" :prefix-icon="Iphone" maxlength="11" size="large" />
          </el-form-item>

          <el-form-item>
            <label class="field-label">验证码</label>
            <div class="captcha-row">
              <el-input v-model="form.captchaCode" placeholder="请输入验证码" :prefix-icon="Key" size="large" />
              <img
                v-if="captchaImage"
                :src="captchaImage"
                class="captcha-image"
                alt="验证码"
                @click="loadCaptcha"
              />
            </div>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="auth-submit"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form>

        <div class="auth-foot">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  background: var(--paper);
}

.auth-aside {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 56px;
  background: var(--ink-900);
  color: #f5f1ea;
}

.aside-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark,
.form-brand {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--brand-500);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
}

.brand-name {
  font-size: 15px;
  font-weight: 600;
}

.aside-body h1 {
  margin: 0 0 22px;
  font-size: 40px;
  line-height: 1.25;
  font-weight: 700;
}

.aside-body p {
  max-width: 420px;
  margin: 0 0 34px;
  color: #b8b1a6;
  font-size: 15px;
  line-height: 1.9;
}

.aside-points {
  list-style: none;
  display: grid;
  gap: 14px;
}

.aside-points li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ded8ce;
  font-size: 14px;
}

.aside-points span {
  color: var(--brand-400);
  font-size: 12px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.aside-foot {
  color: #7d766c;
  font-size: 12px;
}

.auth-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.auth-form-wrap {
  width: 100%;
  max-width: 460px;
}

.auth-head {
  margin-bottom: 26px;
}

.form-brand {
  display: none;
  margin-bottom: 16px;
}

.auth-head h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: var(--ink-900);
}

.auth-head p {
  margin: 0;
  color: var(--ink-500);
  font-size: 14px;
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 14px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.field-label {
  display: block;
  margin-bottom: 7px;
  color: var(--ink-700);
  font-size: 13px;
  font-weight: 600;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: 3px 14px;
  background: var(--surface);
  box-shadow: 0 0 0 1px var(--line-strong) inset;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--brand-300) inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--brand-500) inset;
}

.auth-form :deep(.el-input__inner) {
  height: 42px;
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
  width: 118px;
  height: 42px;
  border-radius: var(--radius-md);
  border: 1px solid var(--line-strong);
  cursor: pointer;
  background: #fff;
  object-fit: cover;
}

.auth-submit {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border-radius: var(--radius-md);
  font-size: 15px;
  letter-spacing: 0.06em;
}

.auth-foot {
  margin-top: 20px;
  color: var(--ink-500);
  font-size: 13px;
  text-align: center;
}

.auth-foot a {
  color: var(--brand-600);
  font-weight: 600;
  text-decoration: none;
}

.auth-foot a:hover {
  text-decoration: underline;
}

@media (max-width: 1000px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-aside {
    display: none;
  }

  .auth-main {
    padding: 32px 20px;
  }

  .form-brand {
    display: inline-flex;
  }
}

@media (max-width: 520px) {
  .field-grid {
    grid-template-columns: 1fr;
  }
}
</style>

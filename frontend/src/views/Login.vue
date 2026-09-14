<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

// 登录表单数据
const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)

// 点击登录
const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.value)
    ElMessage.success('登录成功')
    if (userStore.role === 'ADMIN') {
      router.push('/admin/home')
    } else {
      router.push('/home')
    }
  } catch (e) {
    // 显示后端返回的真实错误信息（如"用户名或密码错误"）
    const message = e?.response?.data?.message || e?.message || '登录失败'
    ElMessage.error(message)
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
        <h1>把闲置<br />换成下一个人<br />需要的东西</h1>
        <p>面向校园的二手交易与互助平台，支持商品发布、收藏、下单、站内私信和交易评价。</p>
        <ul class="aside-points">
          <li><span>01</span>同校交易，当面取货更放心</li>
          <li><span>02</span>闲置发布后即可被同学搜到</li>
          <li><span>03</span>下单、私信、通知一站式完成</li>
        </ul>
      </div>

      <div class="aside-foot">校内闲置流转 · 仅限同学之间使用</div>
    </aside>

    <main class="auth-main">
      <div class="auth-form-wrap">
        <img class="auth-art" src="/login-illustration.svg" alt="校园二手交易插画" />
        <header class="auth-head">
          <span class="form-brand">校</span>
          <h2>登录账号</h2>
          <p>使用你的用户名和密码进入平台</p>
        </header>

        <el-form :model="form" class="auth-form" @keyup.enter="handleLogin">
          <el-form-item>
            <label class="field-label">用户名</label>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <label class="field-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="auth-submit"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="auth-foot">
          还没有账号？<router-link to="/register">注册一个</router-link>
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
  letter-spacing: 0.02em;
}

.aside-body h1 {
  margin: 0 0 22px;
  font-size: 40px;
  line-height: 1.25;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.aside-body p {
  max-width: 420px;
  margin: 0 0 22px;
  color: #b8b1a6;
  font-size: 15px;
  line-height: 1.9;
}

.auth-art {
  display: block;
  width: 100%;
  max-width: 300px;
  margin: 0 auto 18px;
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
  max-width: 360px;
}

.auth-head {
  margin-bottom: 30px;
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

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
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

.auth-submit {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border-radius: var(--radius-md);
  font-size: 15px;
  letter-spacing: 0.06em;
}

.auth-foot {
  margin-top: 22px;
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

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-aside {
    display: none;
  }

  .auth-main {
    padding: 32px 20px;
  }

  .auth-form-wrap {
    max-width: 420px;
  }

  .form-brand {
    display: inline-flex;
  }
}
</style>

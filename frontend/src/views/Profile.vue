<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref(route.query.tab === 'password' ? 'password' : 'profile')
const profileSubmitting = ref(false)
const passwordSubmitting = ref(false)

const profileForm = ref({
  username: '',
  nickname: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loadCurrentUser = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/users/me')
    if (res.code === 200) {
      profileForm.value = {
        username: res.data.username || '',
        nickname: res.data.nickname || '',
        phone: res.data.phone || ''
      }
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载个人资料失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCurrentUser()
})

const saveProfile = async () => {
  if (!profileForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  if (!profileForm.value.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(profileForm.value.phone.trim())) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  profileSubmitting.value = true
  try {
    const res = await request.put('/api/users/profile', {
      nickname: profileForm.value.nickname.trim(),
      phone: profileForm.value.phone.trim()
    })
    if (res.code === 200) {
      userStore.nickname = res.data.nickname || ''
      userStore.phone = res.data.phone || ''
      localStorage.setItem('nickname', res.data.nickname || '')
      localStorage.setItem('phone', res.data.phone || '')
      ElMessage.success('个人资料已更新')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '更新个人资料失败')
  } finally {
    profileSubmitting.value = false
  }
}

const savePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  passwordSubmitting.value = true
  try {
    const res = await request.put('/api/users/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.code === 200) {
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      ElMessage.success('密码修改成功')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '修改密码失败')
  } finally {
    passwordSubmitting.value = false
  }
}

const goBack = () => {
  router.push(userStore.role === 'ADMIN' ? '/admin/home' : '/home')
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="container">
      <div class="page-header">
        <el-button @click="goBack">&lt; 返回</el-button>
        <h2>{{ activeTab === 'password' ? '修改密码' : '个人资料' }}</h2>
        <div class="placeholder"></div>
      </div>

      <div v-if="activeTab === 'password'" class="card">
        <el-form label-width="100px">
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="passwordSubmitting" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-else class="card">
        <el-form label-width="88px">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" maxlength="50" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号">
             <el-input v-model="profileForm.phone" maxlength="11" placeholder="请输入11位手机号" />
           </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSubmitting" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  padding: 32px 20px;
}

.container {
  max-width: 820px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.placeholder {
  width: 72px;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px 8px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.08);
}
</style>

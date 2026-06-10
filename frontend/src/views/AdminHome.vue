<script setup>
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const avatarUploading = ref(false)
const avatarPreviewVisible = ref(false)
const avatarInput = ref(null)

const handleAvatarCommand = (cmd) => {
  if (cmd === 'view') {
    avatarPreviewVisible.value = true
  } else if (cmd === 'change') {
    avatarInput.value?.click()
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'password') {
    router.push('/profile?tab=password')
  }
}

const handleAvatarFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.error('仅支持 jpg、png、webp 图片')
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  avatarUploading.value = true
  request.post('/api/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    if (res.code === 200) {
      userStore.avatar = res.data
      localStorage.setItem('avatar', res.data)
      ElMessage.success('头像更新成功')
      return
    }
    throw new Error(res.message)
  }).catch(e => {
    ElMessage.error(e?.response?.data?.message || e?.message || '头像上传失败')
  }).finally(() => {
    avatarUploading.value = false
    e.target.value = ''
  })
}
const pendingCount = ref('—')
const totalProducts = ref('—')
const pendingReports = ref('—')
const totalOrders = ref('—')

const loadStats = async () => {
  try {
    const [pendingRes, productRes, reportRes, orderRes] = await Promise.all([
      request.get('/api/products/pending'),
      request.get('/api/products'),
      request.get('/api/reports/admin', { params: { status: '待处理' } }),
      request.get('/api/orders/admin')
    ])
    if (pendingRes.code === 200) pendingCount.value = pendingRes.data.length
    if (productRes.code === 200) totalProducts.value = productRes.data.length
    if (reportRes.code === 200) pendingReports.value = reportRes.data.length
    if (orderRes.code === 200) totalOrders.value = orderRes.data.length
  } catch (e) {
    // silent
  }
}

const loadCurrentUser = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  try {
    await userStore.fetchCurrentUser()
    if (userStore.role !== 'ADMIN') {
      router.push('/home')
    }
  } catch (e) {
    userStore.logout()
    ElMessage.error('登录已失效，请重新登录')
    router.push('/login')
  }
}

onMounted(async () => {
  loading.value = true
  await loadCurrentUser()
  if (userStore.role === 'ADMIN') {
    await loadStats()
  }
  loading.value = false
})

const goToReview = () => router.push('/admin/review')
const goToReports = () => router.push('/admin/reports')
const goToOrders = () => router.push('/admin/orders')
const goToProducts = () => router.push('/products')

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

</script>

<template>
  <div class="admin-page" v-loading="loading">
    <header class="admin-header">
      <div class="header-left">
        <el-icon :size="28" class="header-logo"><School /></el-icon>
        <span class="header-title">管理后台</span>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleAvatarCommand">
          <div class="avatar-wrapper">
            <el-avatar
              :size="36"
              :src="userStore.avatar"
              v-loading="avatarUploading"
            >
              {{ (userStore.nickname || userStore.username)[0] }}
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon :size="16"><Camera /></el-icon>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="userStore.avatar" command="view">查看头像</el-dropdown-item>
              <el-dropdown-item command="change">{{ userStore.avatar ? '修改头像' : '设置头像' }}</el-dropdown-item>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <input
          ref="avatarInput"
          type="file"
          accept="image/png,image/jpeg,image/webp"
          style="display:none"
          @change="handleAvatarFileChange"
        />
        <el-dialog v-model="avatarPreviewVisible" title="头像预览" width="360px" :close-on-click-modal="true">
          <div style="text-align:center;padding:20px 0">
            <img v-if="userStore.avatar" :src="userStore.avatar" style="max-width:100%;border-radius:12px" />
            <el-empty v-else description="还没有设置头像" />
          </div>
        </el-dialog>
        <span class="welcome-text">
          {{ userStore.nickname || userStore.username }}
          <el-tag size="small" type="danger" style="margin-left:6px">管理员</el-tag>
        </span>
        <el-button text style="color:rgba(255,255,255,0.7)" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </div>
    </header>

    <main class="admin-main">
      <div class="welcome-card">
        <h2>管理后台</h2>
        <p>审核商品、管理平台内容</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <el-icon :size="28" class="stat-icon warn"><WarningFilled /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ pendingCount }}</span>
            <span class="stat-label">待审核商品</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="28" class="stat-icon primary"><Goods /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ totalProducts }}</span>
            <span class="stat-label">已上架商品</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="28" class="stat-icon success"><User /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ pendingReports }}</span>
            <span class="stat-label">待处理举报</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="28" class="stat-icon info"><List /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ totalOrders }}</span>
            <span class="stat-label">总订单</span>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <el-button type="warning" size="large" class="action-btn" @click="goToReview">
          <el-icon><Checked /></el-icon>
          商品审核
        </el-button>
        <el-button type="danger" size="large" class="action-btn" @click="goToReports">
          <el-icon><WarningFilled /></el-icon>
          举报处理
          <el-badge v-if="pendingReports !== '—' && Number(pendingReports) > 0" :value="pendingReports" class="report-badge" />
        </el-button>
        <el-button type="primary" size="large" class="action-btn" @click="goToOrders">
          <el-icon><Tickets /></el-icon>
          订单管理
        </el-button>
        <el-button size="large" class="action-btn" @click="goToProducts">
          <el-icon><Collection /></el-icon>
          浏览商品广场
        </el-button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100%;
  background: #f0f2f5;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 64px;
  background: #1a1a2e;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo {
  color: #e94560;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  line-height: 0;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 14px;
  color: rgba(255,255,255,0.7);
}

.admin-main {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 16px;
  padding: 36px 40px;
  color: #fff;
  margin-bottom: 32px;
}

.welcome-card h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.welcome-card p {
  font-size: 15px;
  opacity: 0.7;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon.warn { color: #e6a23c; }
.stat-icon.primary { color: #409eff; }
.stat-icon.success { color: #67c23a; }
.stat-icon.info { color: #909399; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  height: 48px;
  font-size: 15px;
  border-radius: 12px;
}

.report-badge {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .admin-header {
    padding: 0 16px;
  }
}
</style>

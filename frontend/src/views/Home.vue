<script setup>
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userStore = useUserStore()

// 首页统计卡片（后续接真实数据）
const stats = ref([
  { label: '在售商品', value: '—', icon: 'Goods' },
  { label: '待确认订单', value: '—', icon: 'List' },
  { label: '我的收藏', value: '—', icon: 'Star' },
  { label: '消息通知', value: '—', icon: 'Bell' }
])

const loadProductsCount = async () => {
  try {
    const res = await request.get('/api/products')
    if (res.code === 200) {
      stats.value[0].value = res.data.length
    }
  } catch (e) {
    stats.value[0].value = '—'
  }
}

const loadFavoritesCount = async () => {
  try {
    const res = await request.get('/api/favorites/my')
    if (res.code === 200) {
      stats.value[2].value = res.data.length
    }
  } catch (e) {
    // ignore
  }
}

const loadPendingSellOrdersCount = async () => {
  try {
    const res = await request.get('/api/orders/sell', { params: { status: '待确认' } })
    if (res.code === 200) {
      stats.value[1].value = res.data.length
    }
  } catch (e) {
    stats.value[1].value = '—'
  }
}

const loadNotificationsCount = async () => {
  try {
    const res = await request.get('/api/notifications/unread-count')
    if (res.code === 200) {
      stats.value[3].value = res.data.count
    }
  } catch (e) {
    stats.value[3].value = '—'
  }
}

// 进入首页时从后端获取当前用户信息
const loadCurrentUser = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }

  try {
    await userStore.fetchCurrentUser()
    if (userStore.role === 'ADMIN') {
      router.push('/admin/home')
      return
    }
  } catch (e) {
    // token 失效，清空登录态，跳回登录页
    userStore.logout()
    ElMessage.error(e?.response?.data?.message || e?.message || '登录已失效，请重新登录')
    router.push('/login')
  }
}

onMounted(() => {
  loadCurrentUser()
  loadProductsCount()
  loadPendingSellOrdersCount()
  loadFavoritesCount()
  loadNotificationsCount()
})

const goToPublish = () => {
  router.push('/products/publish')
}

const goToProducts = () => {
  router.push('/products')
}

const goToMyProducts = () => {
  router.push('/my/products')
}

const goToMyFavorites = () => {
  router.push('/my/favorites')
}

const goToMyOrders = () => {
  router.push('/my/orders')
}

const goToMyNotifications = () => {
  router.push('/my/notifications')
}

// 退出登录
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
</script>

<template>
  <div class="home-page">
    <header class="home-header">
      <div class="header-left">
        <el-icon :size="28" class="header-logo"><School /></el-icon>
        <span class="header-title">校园二手交易平台</span>
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
          <el-tag size="small" type="info" style="margin-left:6px">用户</el-tag>
        </span>
        <el-button text @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </div>
    </header>

    <main class="home-main">
      <div class="welcome-card">
        <h2>欢迎回来 👋</h2>
        <p>今天有什么想买的，或者想卖的吗？</p>
      </div>

      <div class="stats-grid">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="stat-card"
          :class="{ highlight: (stat.label === '待确认订单' || stat.label === '消息通知') && stat.value !== '—' && Number(stat.value) > 0 }"
          @click="stat.label === '消息通知' ? goToMyNotifications() : stat.label === '待确认订单' ? goToMyOrders() : null"
        >
          <el-icon :size="28" class="stat-icon"><component :is="stat.icon" /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <el-button type="primary" size="large" class="action-btn" @click="goToPublish">
          <el-icon><Plus /></el-icon>
          发布商品
        </el-button>
        <el-button size="large" class="action-btn" @click="goToMyProducts">
          <el-icon><Goods /></el-icon>
          我的商品
        </el-button>
        <el-button size="large" class="action-btn" @click="goToProducts">
          <el-icon><Collection /></el-icon>
          浏览商品
        </el-button>
        <el-button size="large" class="action-btn" @click="goToMyOrders">
          <el-icon><List /></el-icon>
          我的订单
          <el-badge v-if="stats[1].value !== '—' && Number(stats[1].value) > 0" :value="stats[1].value" class="order-badge" />
        </el-button>
        <el-button size="large" class="action-btn" @click="goToMyFavorites">
          <el-icon><StarFilled /></el-icon>
          我的收藏
        </el-button>
        <el-button size="large" class="action-btn" @click="goToMyNotifications">
          <el-icon><Bell /></el-icon>
          我的通知
          <el-badge v-if="stats[3].value !== '—' && Number(stats[3].value) > 0" :value="stats[3].value" class="order-badge" />
        </el-button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100%;
  background: #f0f2f5;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 64px;
  background: #fff;
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
  color: #667eea;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 14px;
  color: #606266;
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

.home-main {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  padding: 36px 40px;
  color: #fff;
  margin-bottom: 32px;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-card h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.welcome-card p {
  font-size: 15px;
  opacity: 0.85;
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
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card {
  cursor: default;
}

.stat-card.highlight {
  border: 1px solid rgba(245, 108, 108, 0.35);
  box-shadow: 0 10px 28px rgba(245, 108, 108, 0.14);
}

.stat-icon {
  color: #667eea;
}

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

.order-badge {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .home-header {
    padding: 0 16px;
  }
}


</style>

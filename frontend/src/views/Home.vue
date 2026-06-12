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
        <div>
          <span class="welcome-badge">校园闲置交易</span>
          <h2>欢迎回来</h2>
          <p>看看今天有哪些值得带走的好东西，或者把你的闲置挂上来。</p>
        </div>
      </div>

      <div class="stats-grid">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="stat-card"
          :class="{ highlight: (stat.label === '待确认订单' || stat.label === '消息通知') && stat.value !== '—' && Number(stat.value) > 0 }"
          @click="stat.label === '消息通知' ? goToMyNotifications() : stat.label === '待确认订单' ? goToMyOrders() : null"
        >
          <div class="stat-icon-wrap">
            <el-icon :size="24" class="stat-icon"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <button class="action-card primary" @click="goToPublish">
          <div class="action-top">
            <el-icon><Plus /></el-icon>
            <span>发布商品</span>
          </div>
          <p>快速挂出你的闲置</p>
        </button>
        <button class="action-card" @click="goToMyProducts">
          <div class="action-top">
            <el-icon><Goods /></el-icon>
            <span>我的商品</span>
          </div>
          <p>查看发布和状态</p>
        </button>
        <button class="action-card" @click="goToProducts">
          <div class="action-top">
            <el-icon><Collection /></el-icon>
            <span>购买商品</span>
          </div>
          <p>逛逛最新上架内容</p>
        </button>
        <button class="action-card" @click="goToMyOrders">
          <div class="action-top">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
            <el-badge v-if="stats[1].value !== '—' && Number(stats[1].value) > 0" :value="stats[1].value" class="order-badge" />
          </div>
          <p>跟进待确认交易</p>
        </button>
        <button class="action-card" @click="goToMyFavorites">
          <div class="action-top">
            <el-icon><StarFilled /></el-icon>
            <span>我的收藏</span>
          </div>
          <p>回看感兴趣商品</p>
        </button>
        <button class="action-card" @click="goToMyNotifications">
          <div class="action-top">
            <el-icon><Bell /></el-icon>
            <span>我的通知</span>
            <el-badge v-if="stats[3].value !== '—' && Number(stats[3].value) > 0" :value="stats[3].value" class="order-badge" />
          </div>
          <p>查看平台最新消息</p>
        </button>
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
  max-width: 1120px;
  margin: 0 auto;
  padding: 24px 20px 36px;
}

.welcome-card {
  background: linear-gradient(135deg, #5b6ee1 0%, #6a63d7 45%, #7a52b3 100%);
  border-radius: 24px;
  padding: 24px 28px;
  color: #fff;
  margin-bottom: 18px;
  display: flex;
  align-items: stretch;
  gap: 20px;
  animation: slideDown 0.5s ease-out;
  box-shadow: 0 18px 40px rgba(95, 103, 214, 0.22);
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
  font-size: 28px;
  line-height: 1.15;
  margin: 8px 0 10px;
}

.welcome-card p {
  font-size: 14px;
  opacity: 0.88;
  max-width: 620px;
  margin: 0;
}

.welcome-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 11px;
  letter-spacing: 0.8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(103, 116, 142, 0.08);
  border-radius: 20px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 10px 30px rgba(31, 41, 55, 0.06);
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
  border: 1px solid rgba(96, 96, 255, 0.18);
  box-shadow: 0 16px 30px rgba(97, 97, 214, 0.14);
  background: linear-gradient(180deg, #ffffff, #f8f8ff);
}

.stat-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(91, 110, 225, 0.14), rgba(122, 82, 179, 0.14));
}

.stat-icon {
  color: #6170e1;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #8a94a6;
  margin-top: 4px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.action-card {
  border: 1px solid rgba(103, 116, 142, 0.12);
  background: #fff;
  border-radius: 18px;
  padding: 18px 18px 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  box-shadow: 0 10px 24px rgba(31, 41, 55, 0.05);
}

.action-card:hover {
  transform: translateY(-3px);
  border-color: rgba(91, 110, 225, 0.24);
  box-shadow: 0 16px 30px rgba(91, 110, 225, 0.1);
}

.action-card.primary {
  background: linear-gradient(135deg, #4294f0, #4a7eea);
  color: #fff;
  border: none;
}

.action-top {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.action-card p {
  margin: 12px 0 0;
  font-size: 13px;
  color: #8a94a6;
}

.action-card.primary p {
  color: rgba(255, 255, 255, 0.84);
}

.order-badge {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .home-main {
    padding-top: 20px;
  }

  .welcome-card {
    padding: 20px 18px;
    flex-direction: column;
  }

  .welcome-card h2 {
    font-size: 24px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }

  .home-header {
    padding: 0 16px;
  }
}


</style>

<script setup>
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userStore = useUserStore()

// 首页统计卡片（后续接真实数据）
const stats = ref([
  { label: '在售商品', value: '—', icon: 'Goods' },
  { label: '已完成订单', value: '—', icon: 'List' },
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

// 进入首页时从后端获取当前用户信息
const loadCurrentUser = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }

  try {
    // 调用 /api/users/me，验证 token 是否还有效
    await userStore.fetchCurrentUser()
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

const goToAdminReview = () => {
  router.push('/admin/review')
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
</script>

<template>
  <div class="home-page">
    <header class="home-header">
      <div class="header-left">
        <el-icon :size="28" class="header-logo"><School /></el-icon>
        <span class="header-title">校园二手交易平台</span>
      </div>
      <div class="header-right">
        <span class="welcome-text">你好，{{ userStore.nickname || userStore.username }}</span>
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
      </div>
      <div class="quick-actions" v-if="userStore.role === 'ADMIN'" style="margin-top: 12px">
        <el-button size="large" class="action-btn admin-btn" @click="goToAdminReview">
          <el-icon><Checked /></el-icon>
          商品审核
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

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .home-header {
    padding: 0 16px;
  }
}

.admin-btn {
  background: #fff3e0;
  border-color: #ffb300;
  color: #e65100;
}

.admin-btn:hover {
  background: #ffe0b2;
}
</style>

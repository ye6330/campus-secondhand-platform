<script setup>
import request from '../utils/request'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { StarFilled, Star } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const keyword = ref('')
const avatarUploading = ref(false)
const avatarPreviewVisible = ref(false)
const avatarInput = ref(null)

const stats = ref({
  onSale: '—',
  pendingOrders: '—',
  favorites: '—',
  notifications: '—',
  unreadMessages: '—'
})

const quickStats = computed(() => [
  { label: '在售商品', value: stats.value.onSale, icon: 'Goods' },
  { label: '待确认订单', value: stats.value.pendingOrders, icon: 'List' },
  { label: '收藏夹', value: stats.value.favorites, icon: 'Star' },
  { label: '未读私信', value: stats.value.unreadMessages, icon: 'ChatDotSquare' }
])

const sidebarItems = computed(() => [
  { label: '商品广场', icon: 'Grid', action: () => router.push('/home'), active: true },
  { label: '发布商品', icon: 'Plus', action: () => router.push('/products/publish') },
  { label: '我的商品', icon: 'Goods', action: () => router.push('/my/products') },
  { label: '我的订单', icon: 'List', action: () => router.push('/my/orders'), badge: stats.value.pendingOrders },
  { label: '我的收藏', icon: 'Star', action: () => router.push('/my/favorites') },
  { label: '我的私信', icon: 'ChatDotSquare', action: () => router.push('/my/messages'), badge: stats.value.unreadMessages },
  { label: '我的通知', icon: 'Bell', action: () => router.push('/my/notifications'), badge: stats.value.notifications },
  { label: '个人资料', icon: 'User', action: () => router.push('/profile') }
])

const loadProducts = async (kw) => {
  loading.value = true
  try {
    const params = kw ? { keyword: kw } : {}
    const res = await request.get('/api/products', { params })
    if (res.code === 200) {
      products.value = res.data
      stats.value.onSale = res.data.length
      return
    }
    throw new Error(res.message)
  } catch (e) {
    products.value = []
    stats.value.onSale = '—'
    ElMessage.error(e?.response?.data?.message || e?.message || '加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadFavoritesCount = async () => {
  try {
    const res = await request.get('/api/favorites/my')
    if (res.code === 200) {
      stats.value.favorites = res.data.length
    }
  } catch (e) {
    stats.value.favorites = '—'
  }
}

const loadPendingSellOrdersCount = async () => {
  try {
    const res = await request.get('/api/orders/sell', { params: { status: '待确认' } })
    if (res.code === 200) {
      stats.value.pendingOrders = res.data.length
    }
  } catch (e) {
    stats.value.pendingOrders = '—'
  }
}

const loadNotificationsCount = async () => {
  try {
    const res = await request.get('/api/notifications/unread-count')
    if (res.code === 200) {
      stats.value.notifications = res.data.count
    }
  } catch (e) {
    stats.value.notifications = '—'
  }
}

const loadUnreadMessagesCount = async () => {
  try {
    const res = await request.get('/api/messages/unread-count')
    if (res.code === 200) {
      stats.value.unreadMessages = res.data.count
      return
    }
    throw new Error(res.message)
  } catch (e) {
    stats.value.unreadMessages = '—'
  }
}

const loadCurrentUser = async () => {
  if (!userStore.token) {
    router.push('/login')
    return false
  }

  try {
    await userStore.fetchCurrentUser()
    if (userStore.role === 'ADMIN') {
      router.push('/admin/home')
      return false
    }
    return true
  } catch (e) {
    userStore.logout()
    ElMessage.error(e?.response?.data?.message || e?.message || '登录已失效，请重新登录')
    router.push('/login')
    return false
  }
}

onMounted(async () => {
  const ok = await loadCurrentUser()
  if (!ok) return
  loadProducts()
  loadPendingSellOrdersCount()
  loadFavoritesCount()
  loadNotificationsCount()
  loadUnreadMessagesCount()
})

const handleSearch = () => {
  loadProducts(keyword.value.trim())
}

const clearSearch = () => {
  keyword.value = ''
  loadProducts()
}

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const goToSeller = (e, sellerId) => {
  e.stopPropagation()
  router.push(`/seller/${sellerId}`)
}

const toggleFavorite = async (e, product) => {
  e.stopPropagation()
  try {
    if (product.favorited) {
      const res = await request.delete(`/api/favorites/${product.id}`)
      if (res.code === 200) {
        product.favorited = false
        product.favoriteCount--
        if (stats.value.favorites !== '—') stats.value.favorites = Number(stats.value.favorites) - 1
        return
      }
    } else {
      const res = await request.post(`/api/favorites/${product.id}`)
      if (res.code === 200) {
        product.favorited = true
        product.favoriteCount++
        if (stats.value.favorites !== '—') stats.value.favorites = Number(stats.value.favorites) + 1
        return
      }
    }
    throw new Error('操作失败')
  } catch (e2) {
    ElMessage.error(e2?.response?.data?.message || e2?.message || '操作失败')
  }
}

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
  }).catch(e2 => {
    ElMessage.error(e2?.response?.data?.message || e2?.message || '头像上传失败')
  }).finally(() => {
    avatarUploading.value = false
    e.target.value = ''
  })
}

const displayName = computed(() => userStore.nickname || userStore.username)
</script>

<template>
  <div class="workspace-page">
    <header class="topbar">
      <div class="brand">
        <el-icon :size="24" class="brand-icon"><School /></el-icon>
        <div>
          <div class="brand-title">校园二手交易平台</div>
          <div class="brand-subtitle">闲置流转工作台</div>
        </div>
      </div>
      <div class="topbar-actions">
        <el-input
          v-model="keyword"
          class="header-search"
          placeholder="搜索在售商品"
          clearable
          @keyup.enter="handleSearch"
          @clear="clearSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-dropdown trigger="click" @command="handleAvatarCommand">
          <div class="account-trigger">
            <el-avatar :size="38" :src="userStore.avatar" v-loading="avatarUploading">
              {{ displayName?.[0] }}
            </el-avatar>
            <div class="account-meta">
              <span class="account-name">{{ displayName }}</span>
              <span class="account-role">普通用户</span>
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
        <el-button text @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
        <input
          ref="avatarInput"
          type="file"
          accept="image/png,image/jpeg,image/webp"
          style="display:none"
          @change="handleAvatarFileChange"
        />
      </div>
    </header>

    <div class="workspace-body">
      <aside class="sidebar">
        <div class="sidebar-section">
          <div class="section-label">快捷菜单</div>
          <button
            v-for="item in sidebarItems"
            :key="item.label"
            class="nav-item"
            :class="{ active: item.active }"
            @click="item.action()"
          >
            <span class="nav-left">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </span>
            <el-badge
              v-if="item.badge !== undefined && item.badge !== '—' && Number(item.badge) > 0"
              :value="item.badge"
            />
          </button>
        </div>

        <div class="sidebar-card">
          <div class="sidebar-card-title">今日概览</div>
          <div class="mini-stats">
            <div v-for="stat in quickStats" :key="stat.label" class="mini-stat">
              <el-icon class="mini-stat-icon"><component :is="stat.icon" /></el-icon>
              <div>
                <div class="mini-stat-value">{{ stat.value }}</div>
                <div class="mini-stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <main class="content-area">
        <section class="hero-panel">
          <div>
            <span class="hero-badge">商品广场</span>
            <h1>在售商品</h1>
            <p>默认展示已上架商品，支持搜索、收藏和快速进入详情。</p>
          </div>
          <div class="hero-actions">
            <el-button @click="clearSearch">查看全部</el-button>
            <el-button type="primary" @click="router.push('/products/publish')">发布商品</el-button>
          </div>
        </section>

        <section class="product-section">
          <div class="section-header">
            <div>
              <h2>最新在售</h2>
              <p>{{ products.length ? `当前展示 ${products.length} 件商品` : '暂无可展示商品' }}</p>
            </div>
            <div class="section-actions">
              <el-button text @click="router.push('/products')">进入完整商品列表</el-button>
            </div>
          </div>

          <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

          <div v-else class="product-grid" v-loading="loading">
            <article v-for="product in products" :key="product.id" class="product-card" @click="goToDetail(product.id)">
              <img :src="product.coverImage" alt="商品封面" class="product-cover" />
              <div class="product-body">
                <div class="product-price-row">
                  <span class="product-price">￥{{ product.price }}</span>
                  <span class="product-time">{{ product.createdAt }}</span>
                </div>
                <h3>{{ product.title }}</h3>
                <p>{{ product.description }}</p>
                <div class="product-footer">
                  <button class="seller-link" @click="goToSeller($event, product.sellerId)">
                    {{ product.sellerName }}
                  </button>
                  <div class="product-meta" @click.stop>
                    <span>浏览 {{ product.viewCount || 0 }}</span>
                    <span class="favorite-chip">
                      <el-icon
                        :color="product.favorited ? '#f59e0b' : '#9ca3af'"
                        :size="18"
                        @click="toggleFavorite($event, product)"
                      >
                        <StarFilled v-if="product.favorited" />
                        <Star v-else />
                      </el-icon>
                      {{ product.favoriteCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </section>
      </main>
    </div>

    <el-dialog v-model="avatarPreviewVisible" title="头像预览" width="360px" :close-on-click-modal="true">
      <div class="avatar-preview">
        <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar-preview-image" />
        <el-empty v-else description="还没有设置头像" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.workspace-page {
  min-height: 100%;
  background: #eef2f7;
  color: #111827;
}

.topbar {
  height: 72px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 20;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  color: #4f46e5;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-search {
  width: 300px;
}

.account-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.account-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.account-name {
  font-size: 14px;
  font-weight: 600;
}

.account-role {
  font-size: 12px;
  color: #6b7280;
}

.workspace-body {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section,
.sidebar-card,
.hero-panel,
.product-section,
.product-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}

.sidebar-section,
.sidebar-card {
  padding: 18px;
}

.section-label,
.sidebar-card-title {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.nav-item {
  width: 100%;
  border: 0;
  background: transparent;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.nav-item + .nav-item {
  margin-top: 6px;
}

.nav-item:hover,
.nav-item.active {
  background: #eef2ff;
  color: #4338ca;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-stats {
  display: grid;
  gap: 12px;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.mini-stat-icon {
  color: #4f46e5;
}

.mini-stat-value {
  font-size: 18px;
  font-weight: 700;
}

.mini-stat-label {
  font-size: 12px;
  color: #6b7280;
}

.content-area {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-panel {
  padding: 28px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  background: linear-gradient(135deg, #1e3a8a 0%, #4338ca 55%, #7c3aed 100%);
  color: #ffffff;
  border: 0;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  margin-bottom: 14px;
}

.hero-panel h1 {
  margin: 0 0 10px;
  font-size: 34px;
  line-height: 1.1;
}

.hero-panel p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.product-section {
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0 0 6px;
  font-size: 24px;
}

.section-header p {
  margin: 0;
  color: #6b7280;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.product-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.08);
}

.product-cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  background: #e5e7eb;
  display: block;
}

.product-body {
  padding: 16px;
}

.product-price-row,
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-price {
  font-size: 24px;
  font-weight: 700;
  color: #dc2626;
}

.product-time,
.product-meta,
.product-body p {
  color: #6b7280;
}

.product-time {
  font-size: 12px;
}

.product-body h3 {
  margin: 14px 0 8px;
  font-size: 18px;
  line-height: 1.35;
}

.product-body p {
  margin: 0 0 16px;
  min-height: 40px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.seller-link {
  border: 0;
  background: transparent;
  padding: 0;
  color: #2563eb;
  cursor: pointer;
  font-size: 14px;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.favorite-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.avatar-preview {
  text-align: center;
  padding: 20px 0;
}

.avatar-preview-image {
  max-width: 100%;
  border-radius: 12px;
}

@media (max-width: 1180px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .workspace-body {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: 2;
  }

  .content-area {
    order: 1;
  }
}

@media (max-width: 760px) {
  .topbar {
    height: auto;
    padding: 16px;
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .topbar-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-search {
    width: 100%;
  }

  .hero-panel,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>

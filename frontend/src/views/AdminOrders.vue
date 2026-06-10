<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const statusTab = ref('all')
const orders = ref([])
const counts = ref({ all: 0, '待确认': 0, '已确认': 0, '已拒绝': 0, '已取消': 0 })
const previewVisible = ref(false)
const previewImage = ref('')

const buildCounts = (items) => ({
  all: items.length,
  '待确认': items.filter(item => item.status === '待确认').length,
  '已确认': items.filter(item => item.status === '已确认').length,
  '已拒绝': items.filter(item => item.status === '已拒绝').length,
  '已取消': items.filter(item => item.status === '已取消').length
})

const loadOrders = async () => {
  loading.value = true
  try {
    const params = statusTab.value === 'all' ? {} : { status: statusTab.value }
    const res = await request.get('/api/orders/admin', { params })
    if (res.code === 200) {
      orders.value = res.data || []
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadCounts = async () => {
  try {
    const res = await request.get('/api/orders/admin')
    if (res.code === 200) {
      counts.value = buildCounts(res.data || [])
    }
  } catch (e) {
    // ignore count errors
  }
}

const reloadAll = () => {
  loadOrders()
  loadCounts()
}

onMounted(() => {
  reloadAll()
})

watch(statusTab, () => {
  loadOrders()
})

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

const openPreview = (image) => {
  previewImage.value = image
  previewVisible.value = true
}

const statusType = (status) => {
  if (status === '待确认') return 'warning'
  if (status === '已确认') return 'success'
  if (status === '已取消') return 'info'
  return 'danger'
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/admin/home')">&lt; 返回后台</el-button>
      <h2>订单管理</h2>
      <div class="placeholder"></div>
    </div>

    <div class="toolbar">
      <el-tabs v-model="statusTab" class="tabs">
        <el-tab-pane :label="`全部 (${counts.all})`" name="all" />
        <el-tab-pane :label="`待确认 (${counts['待确认']})`" name="待确认" />
        <el-tab-pane :label="`已确认 (${counts['已确认']})`" name="已确认" />
        <el-tab-pane :label="`已拒绝 (${counts['已拒绝']})`" name="已拒绝" />
        <el-tab-pane :label="`已取消 (${counts['已取消']})`" name="已取消" />
      </el-tabs>
    </div>

    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="item in orders" :key="item.id" class="order-card">
        <div class="order-main" @click="goToDetail(item.productId)">
          <img :src="item.productCoverImage" class="cover" />
          <div class="info">
            <div class="title-row">
              <strong>{{ item.productTitle }}</strong>
              <el-tag size="small" :type="statusType(item.status)">{{ item.status }}</el-tag>
            </div>
            <p>订单号：{{ item.orderNo }}</p>
            <p>价格：￥{{ item.productPrice }}</p>
            <p>买家：{{ item.buyerName }}（{{ item.buyerPhone || '未填写' }}）</p>
            <p>卖家：{{ item.sellerName }}（{{ item.sellerPhone || '未填写' }}）</p>
            <p>下单时间：{{ item.createdAt }}</p>
            <p v-if="item.tradeConfirmedAt">成交时间：{{ item.tradeConfirmedAt }}</p>
            <p v-if="item.note">备注：{{ item.note }}</p>
            <div v-if="item.tradeImage" class="trade-image-wrap">
              <span class="trade-label">交易图：</span>
              <img :src="item.tradeImage" class="trade-image clickable" @click.stop="openPreview(item.tradeImage)" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="previewVisible" title="交易图预览" width="640px">
      <div class="preview-wrap">
        <img :src="previewImage" class="preview-large" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  padding: 32px 20px;
}
.page-header, .toolbar, .order-list {
  max-width: 1080px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; color: #303133; }
.placeholder { width: 88px; }
.toolbar { margin-bottom: 16px; }
.tabs { margin-top: 12px; }
.order-list { display: flex; flex-direction: column; gap: 14px; }
.order-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}
.order-main { display: flex; gap: 16px; cursor: pointer; }
.cover { width: 132px; height: 96px; object-fit: cover; border-radius: 10px; flex-shrink: 0; }
.info { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.info strong { color: #303133; font-size: 16px; }
.info p { margin: 6px 0; color: #606266; }
.trade-image-wrap {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 8px;
}
.trade-label {
  color: #606266;
  line-height: 72px;
}
.trade-image {
  width: 108px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
.clickable {
  cursor: zoom-in;
}
.preview-wrap {
  display: flex;
  justify-content: center;
}
.preview-large {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 10px;
}
@media (max-width: 768px) {
  .order-main {
    flex-direction: column;
  }
  .cover {
    width: 100%;
    height: 180px;
  }
}
</style>

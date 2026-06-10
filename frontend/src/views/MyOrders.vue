<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const orderType = ref('buy')
const statusTab = ref('all')
const confirmDialogVisible = ref(false)
const confirmSubmitting = ref(false)
const confirmForm = ref({ note: '', tradeImage: '' })
const currentOrder = ref(null)
const previewVisible = ref(false)
const previewImage = ref('')
const counts = ref({
  buy: { all: 0, '待确认': 0, '已确认': 0, '已拒绝': 0, '已取消': 0 },
  sell: { all: 0, '待确认': 0, '已确认': 0, '已拒绝': 0, '已取消': 0 }
})

const activeCounts = computed(() => counts.value[orderType.value])

const loadOrders = async () => {
  loading.value = true
  try {
    const params = statusTab.value === 'all' ? {} : { status: statusTab.value }
    const path = orderType.value === 'buy' ? '/api/orders/buy' : '/api/orders/sell'
    const res = await request.get(path, { params })
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

const buildCounts = (items) => ({
  all: items.length,
  '待确认': items.filter(item => item.status === '待确认').length,
  '已确认': items.filter(item => item.status === '已确认').length,
  '已拒绝': items.filter(item => item.status === '已拒绝').length,
  '已取消': items.filter(item => item.status === '已取消').length
})

const loadCounts = async () => {
  try {
    const [buyRes, sellRes] = await Promise.all([
      request.get('/api/orders/buy'),
      request.get('/api/orders/sell')
    ])
    if (buyRes.code === 200) counts.value.buy = buildCounts(buyRes.data || [])
    if (sellRes.code === 200) counts.value.sell = buildCounts(sellRes.data || [])
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

watch([orderType, statusTab], () => {
  loadOrders()
})

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

const openPreview = (image) => {
  previewImage.value = image
  previewVisible.value = true
}

const openConfirmDialog = (item) => {
  currentOrder.value = item
  confirmForm.value = { note: '', tradeImage: '' }
  confirmDialogVisible.value = true
}

const uploadTradeImage = async (uploadFile) => {
  const file = uploadFile.file
  if (!file) return
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
    ElMessage.error('仅支持 jpg、png、webp 图片')
    return
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/api/products/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      confirmForm.value.tradeImage = res.data
      ElMessage.success('交易图上传成功')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '交易图上传失败')
  }
}

const submitConfirm = async () => {
  if (!currentOrder.value) return
  if (!confirmForm.value.tradeImage) {
    ElMessage.warning('请先上传交易现场图')
    return
  }
  confirmSubmitting.value = true
  try {
    const res = await request.put(`/api/orders/${currentOrder.value.id}/confirm`, {
      note: confirmForm.value.note.trim(),
      tradeImage: confirmForm.value.tradeImage
    })
    if (res.code === 200) {
      ElMessage.success('确认成交成功')
      confirmDialogVisible.value = false
      reloadAll()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '确认成交失败')
  } finally {
    confirmSubmitting.value = false
  }
}

const handleReject = async (item) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（选填）', '拒绝订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: ''
    })
    const res = await request.put(`/api/orders/${item.id}/reject`, { note: value || '' })
    if (res.code === 200) {
      ElMessage.success('拒绝订单成功')
      reloadAll()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || e?.message || '拒绝订单失败')
    }
  }
}

const handleCancel = async (item) => {
  try {
    await ElMessageBox.confirm('确定取消这笔购买意向吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/api/orders/${item.id}`)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      reloadAll()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || e?.message || '取消失败')
    }
  }
}

const statusType = (status) => {
  if (status === '待确认') return 'warning'
  if (status === '已确认') return 'success'
  if (status === '已取消') return 'info'
  return 'danger'
}

const contactText = (item) => {
  if (orderType.value === 'buy') {
    return `卖家：${item.sellerName}（${item.sellerPhone || '未填写'}）`
  }
  return `买家：${item.buyerName}（${item.buyerPhone || '未填写'}）`
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/home')">&lt; 返回首页</el-button>
      <h2>我的订单</h2>
      <div class="placeholder"></div>
    </div>

    <div class="toolbar">
      <el-radio-group v-model="orderType">
        <el-radio-button label="buy">
          <span class="type-label">
            我买入的
            <el-badge v-if="counts.buy['待确认'] > 0" :value="counts.buy['待确认']" class="mini-badge" />
          </span>
        </el-radio-button>
        <el-radio-button label="sell">
          <span class="type-label">
            我卖出的
            <el-badge v-if="counts.sell['待确认'] > 0" :value="counts.sell['待确认']" class="mini-badge" />
          </span>
        </el-radio-button>
      </el-radio-group>
      <el-tabs v-model="statusTab" class="tabs">
        <el-tab-pane :label="`全部 (${activeCounts.all})`" name="all" />
        <el-tab-pane :label="`待确认 (${activeCounts['待确认']})`" name="待确认" />
        <el-tab-pane :label="`已确认 (${activeCounts['已确认']})`" name="已确认" />
        <el-tab-pane :label="`已拒绝 (${activeCounts['已拒绝']})`" name="已拒绝" />
        <el-tab-pane :label="`已取消 (${activeCounts['已取消']})`" name="已取消" />
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
            <p>{{ contactText(item) }}</p>
            <p>下单时间：{{ item.createdAt }}</p>
            <p v-if="item.tradeConfirmedAt">成交时间：{{ item.tradeConfirmedAt }}</p>
            <p v-if="item.note">备注：{{ item.note }}</p>
            <div v-if="item.tradeImage" class="trade-image-wrap">
              <span class="trade-label">交易图：</span>
              <img :src="item.tradeImage" class="trade-image clickable" @click.stop="openPreview(item.tradeImage)" />
            </div>
          </div>
        </div>
        <div class="actions">
          <el-button v-if="orderType === 'sell' && item.status === '待确认'" type="success" size="small" @click="openConfirmDialog(item)">确认成交</el-button>
          <el-button v-if="orderType === 'sell' && item.status === '待确认'" type="danger" size="small" @click="handleReject(item)">拒绝订单</el-button>
          <el-button v-if="orderType === 'buy' && item.status === '待确认'" type="info" size="small" @click="handleCancel(item)">取消意向</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="confirmDialogVisible" title="确认成交" width="520px">
      <el-form label-width="90px">
        <el-form-item label="交易图">
          <div class="upload-block">
            <el-upload :show-file-list="false" :http-request="uploadTradeImage" accept="image/png,image/jpeg,image/webp">
              <el-button type="primary" plain>上传交易图</el-button>
            </el-upload>
            <img v-if="confirmForm.tradeImage" :src="confirmForm.tradeImage" class="preview-image" />
            <span v-else class="upload-tip">请上传买卖双方线下交易现场图</span>
          </div>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="confirmForm.note" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="可填写成交说明、交付情况等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="confirmSubmitting" @click="submitConfirm">确认成交</el-button>
      </template>
    </el-dialog>

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
  max-width: 960px;
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
.type-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.mini-badge {
  transform: translateY(-1px);
}
.order-list { display: flex; flex-direction: column; gap: 14px; }
.order-card {
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
  background: #fff; border-radius: 14px; padding: 18px 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}
.order-main { display: flex; gap: 16px; flex: 1; cursor: pointer; }
.cover { width: 132px; height: 96px; object-fit: cover; border-radius: 10px; flex-shrink: 0; }
.info { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.info strong { color: #303133; font-size: 16px; }
.info p { margin: 6px 0; color: #606266; }
.actions { display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; }
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
.trade-image, .preview-image {
  width: 108px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
.clickable {
  cursor: zoom-in;
}
.upload-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}
.upload-tip {
  color: #909399;
  font-size: 13px;
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
  .order-card, .order-main {
    flex-direction: column;
    align-items: stretch;
  }
  .cover {
    width: 100%;
    height: 180px;
  }
  .actions {
    flex-direction: row;
    justify-content: flex-end;
  }
}
</style>

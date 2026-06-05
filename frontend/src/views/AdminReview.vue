<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const products = ref([])

const loadPending = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/products/pending')
    if (res.code === 200) {
      products.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('加载待审核商品失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPending()
})

const handleReview = (id, action, title) => {
  const text = action === 'approve' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定${text}「${title}」？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.put(`/api/products/${id}/review?action=${action}`)
      if (res.code === 200) {
        ElMessage.success(`${text}成功`)
        loadPending()
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || `${text}失败`)
    }
  }).catch(() => {})
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <h2>商品审核</h2>
    </div>

    <el-empty v-if="products.length === 0" description="暂无待审核商品" />

    <div v-else class="review-list">
      <div v-for="p in products" :key="p.id" class="review-card">
        <img :src="p.coverImage" class="thumb" />
        <div class="review-info">
          <strong>{{ p.title }}</strong>
          <p>{{ p.description }}</p>
          <span class="price">￥{{ p.price }}</span>
          <span class="seller">卖家：{{ p.sellerName }}</span>
        </div>
        <div class="review-actions">
          <el-button type="success" @click="handleReview(p.id, 'approve', p.title)">通过</el-button>
          <el-button type="danger" @click="handleReview(p.id, 'reject', p.title)">拒绝</el-button>
        </div>
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

.page-header {
  max-width: 800px;
  margin: 0 auto 24px;
}

.page-header h2 {
  font-size: 28px;
  color: #303133;
}

.review-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  display: flex;
  gap: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  align-items: center;
}

.thumb {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.review-info {
  flex: 1;
}

.review-info strong {
  font-size: 16px;
  color: #303133;
  display: block;
  margin-bottom: 4px;
}

.review-info p {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-info .price {
  color: #f56c6c;
  font-weight: 700;
  margin-right: 16px;
}

.review-info .seller {
  color: #909399;
  font-size: 13px;
}

.review-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>

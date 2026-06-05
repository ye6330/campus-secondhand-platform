<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const product = ref(null)
const isSeller = ref(false)

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/products/${route.params.id}`)
    if (res.code === 200) {
      product.value = res.data
      isSeller.value = String(product.value.sellerId) === String(userStore.userId)
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载商品详情失败')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})

const goBack = () => {
  router.push('/products')
}

const goToEdit = () => {
  router.push(`/products/${route.params.id}/edit`)
}

const handleDelete = () => {
  ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/api/products/${route.params.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        router.push('/products')
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
    }
  }).catch(() => {})
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="detail-container" v-if="product">
      <div class="detail-header">
        <el-button @click="goBack">&lt; 返回列表</el-button>
        <div class="detail-actions" v-if="isSeller">
          <el-button type="primary" @click="goToEdit">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>
      <div class="detail-content">
        <div class="detail-image">
          <img :src="product.coverImage" alt="商品封面" />
        </div>
        <div class="detail-info">
          <div class="detail-title-row">
            <h1>{{ product.title }}</h1>
            <el-tag v-if="product.status === '待审核'" type="warning">待审核</el-tag>
            <el-tag v-else-if="product.status === '已上架'" type="success">已上架</el-tag>
            <el-tag v-else-if="product.status === '已拒绝'" type="danger">已拒绝</el-tag>
          </div>
          <div class="detail-price">￥{{ product.price }}</div>
          <div class="detail-meta">
            <span>卖家：{{ product.sellerName }}</span>
            <span>发布时间：{{ product.createdAt }}</span>
          </div>
          <el-divider />
          <div class="detail-desc">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>
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

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-actions {
  display: flex;
  gap: 12px;
}

.detail-content {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.08);
}

.detail-image {
  flex: 0 0 420px;
}

.detail-image img {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
}

.detail-info {
  flex: 1;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-title-row h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.detail-price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 16px;
}

.detail-meta {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
}

.detail-desc h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.detail-desc p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>

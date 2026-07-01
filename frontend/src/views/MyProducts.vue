<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const activeTab = ref('all')
const counts = ref({
  all: 0,
  '待审核': 0,
  '已上架': 0,
  '已拒绝': 0,
  '已下架': 0,
  '已售出': 0
})

const reloadAll = () => {
  loadMyProducts()
  loadCounts()
}

const loadMyProducts = async () => {
  loading.value = true
  try {
    const params = activeTab.value === 'all' ? {} : { status: activeTab.value }
    const res = await request.get('/api/products/my', { params })
    if (res.code === 200) {
      products.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('加载我的商品失败')
  } finally {
    loading.value = false
  }
}

const loadCounts = async () => {
  try {
    const res = await request.get('/api/products/my')
    if (res.code === 200) {
      const allProducts = res.data || []
      counts.value = {
        all: allProducts.length,
        '待审核': allProducts.filter(item => item.status === '待审核').length,
        '已上架': allProducts.filter(item => item.status === '已上架').length,
        '已拒绝': allProducts.filter(item => item.status === '已拒绝').length,
        '已下架': allProducts.filter(item => item.status === '已下架').length,
        '已售出': allProducts.filter(item => item.status === '已售出').length
      }
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载商品统计失败')
  }
}

onMounted(() => {
  loadMyProducts()
  loadCounts()
})

watch(activeTab, () => {
  loadMyProducts()
})

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const handleDelete = (id, title) => {
  ElMessageBox.confirm(`确定删除「${title}」？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
        const res = await request.delete(`/api/products/${id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          reloadAll()
          return
        }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleOffShelf = (id, title) => {
  ElMessageBox.confirm(`确定下架「${title}」？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.put(`/api/products/${id}/off-shelf`)
      if (res.code === 200) {
        ElMessage.success('下架成功')
        reloadAll()
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '下架失败')
    }
  }).catch(() => {})
}

const handleRelist = (id, title) => {
  ElMessageBox.confirm(`确定重新上架「${title}」？重新上架后将进入待审核。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const res = await request.put(`/api/products/${id}/relist`)
      if (res.code === 200) {
        ElMessage.success('已提交重新上架审核')
        reloadAll()
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '重新上架失败')
    }
  }).catch(() => {})
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/home')">&lt; 返回首页</el-button>
      <h2>我的发布</h2>
      <el-button type="primary" @click="router.push('/products/publish')">发布新商品</el-button>
    </div>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane :label="`全部 (${counts.all})`" name="all" />
      <el-tab-pane :label="`待审核 (${counts['待审核']})`" name="待审核" />
      <el-tab-pane :label="`已上架 (${counts['已上架']})`" name="已上架" />
      <el-tab-pane :label="`已下架 (${counts['已下架']})`" name="已下架" />
      <el-tab-pane :label="`已售出 (${counts['已售出']})`" name="已售出" />
      <el-tab-pane :label="`已拒绝 (${counts['已拒绝']})`" name="已拒绝" />
    </el-tabs>

    <el-empty v-if="products.length === 0" description="暂无商品" />

    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="goToDetail(p.id)">
        <img :src="p.coverImage" class="cover" />
        <div class="info">
          <div class="title-row">
            <strong>{{ p.title }}</strong>
            <el-tag size="small" v-if="p.status === '待审核'" type="warning">待审核</el-tag>
            <el-tag size="small" v-else-if="p.status === '已上架'" type="success">已上架</el-tag>
            <el-tag size="small" v-else-if="p.status === '已下架'" type="info">已下架</el-tag>
            <el-tag size="small" v-else-if="p.status === '已售出'" type="success">已售出</el-tag>
            <el-tag size="small" v-else-if="p.status === '已拒绝'" type="danger">已拒绝</el-tag>
          </div>
          <p>{{ p.description }}</p>
          <div class="row">
            <span class="price">￥{{ p.price }}</span>
            <span class="time">{{ p.createdAt }}</span>
          </div>
        </div>
        <div class="actions" @click.stop>
          <el-button v-if="!['交易中', '已售出'].includes(p.status)" size="small" @click="router.push(`/products/${p.id}/edit`)">编辑</el-button>
          <el-button v-if="p.status === '已上架'" size="small" type="warning" @click="handleOffShelf(p.id, p.title)">下架</el-button>
          <el-button v-if="p.status === '已下架'" size="small" type="success" @click="handleRelist(p.id, p.title)">上架</el-button>
          <el-button size="small" type="danger" @click="handleDelete(p.id, p.title)">删除</el-button>
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
  max-width: 900px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.tabs {
  max-width: 900px;
  margin: 0 auto 16px;
}

.product-grid {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-card {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: box-shadow 0.15s;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.cover {
  width: 140px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.title-row strong {
  font-size: 16px;
  color: #303133;
}

.info p {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 700;
}

.time {
  color: #909399;
  font-size: 13px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
  flex-shrink: 0;
}
</style>

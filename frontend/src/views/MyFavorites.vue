<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const selectedIds = ref([])

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/favorites/my')
    if (res.code === 200) {
      products.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const toggleFavorite = async (id) => {
  try {
    const res = await request.delete(`/api/favorites/${id}`)
    if (res.code === 200) {
      products.value = products.value.filter(p => p.id !== id)
      selectedIds.value = selectedIds.value.filter(sid => sid !== id)
      ElMessage.success('已取消收藏')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const batchCancel = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要取消收藏的商品')
    return
  }
  ElMessageBox.confirm(`确定取消 ${selectedIds.value.length} 个商品的收藏？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete('/api/favorites/batch', { data: selectedIds.value })
      if (res.code === 200) {
        products.value = products.value.filter(p => !selectedIds.value.includes(p.id))
        selectedIds.value = []
        ElMessage.success('批量取消成功')
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error('批量取消失败')
    }
  }).catch(() => {})
}

const allSelected = computed(() => {
  return products.value.length > 0 && selectedIds.value.length === products.value.length
})

const handleSelectAll = () => {
  if (allSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = products.value.map(p => p.id)
  }
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/home')">&lt; 返回首页</el-button>
      <h2>我的收藏</h2>
      <el-button type="danger" @click="batchCancel">批量取消</el-button>
    </div>

    <div class="toolbar" v-if="products.length > 0">
      <el-checkbox :model-value="allSelected" @change="handleSelectAll">全选</el-checkbox>
      <span class="count">共 {{ products.length }} 件</span>
    </div>

    <el-empty v-if="!loading && products.length === 0" description="还没有收藏任何商品" />

    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card">
        <el-checkbox
          class="card-checkbox"
          :model-value="selectedIds.includes(p.id)"
          @change="v => v ? selectedIds.push(p.id) : selectedIds = selectedIds.filter(x => x !== p.id)"
          @click.stop
        />
        <div class="card-body" @click="goToDetail(p.id)">
          <img :src="p.coverImage" class="cover" />
          <div class="info">
            <strong>{{ p.title }}</strong>
            <p>{{ p.description }}</p>
            <div class="row">
              <span class="price">￥{{ p.price }}</span>
              <span class="time">{{ p.createdAt }}</span>
            </div>
          </div>
        </div>
        <div class="actions" @click.stop>
          <el-button text type="warning" @click="toggleFavorite(p.id)">
            <el-icon><StarFilled /></el-icon>
          </el-button>
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

.toolbar {
  max-width: 900px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.count {
  color: #909399;
  font-size: 13px;
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
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-body {
  flex: 1;
  display: flex;
  gap: 16px;
  cursor: pointer;
  align-items: center;
}

.cover {
  width: 100px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.info strong {
  display: block;
  font-size: 15px;
  color: #303133;
}

.info p {
  color: #909399;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 4px;
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
  flex-shrink: 0;
}
</style>

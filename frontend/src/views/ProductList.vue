<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, Star } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const keyword = ref('')

const loadProducts = async (kw) => {
  loading.value = true
  try {
    const params = kw ? { keyword: kw } : {}
    const res = await request.get('/api/products', { params })
    if (res.code === 200) {
      products.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载商品列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProducts()
})

const querySearch = async (queryString, cb) => {
  if (!queryString.trim()) {
    cb([])
    return
  }
  try {
    const res = await request.get('/api/products/suggest', { params: { keyword: queryString } })
    if (res.code === 200) {
      cb(res.data.map(v => ({ value: v })))
      return
    }
  } catch (e) {
    // silent
  }
  cb([])
}

const handleSearch = () => {
  loadProducts(keyword.value.trim())
}

const handleSelect = (item) => {
  keyword.value = item.value
  loadProducts(item.value)
}

const clearSearch = () => {
  keyword.value = ''
  loadProducts()
}

const goBackHome = () => {
  router.push('/home')
}

const goToPublish = () => {
  router.push('/products/publish')
}

const goToDetail = (id) => {
  router.push(`/products/${id}`)
}

const toggleFavorite = async (e, product) => {
  e.stopPropagation()
  try {
    if (product.favorited) {
      const res = await request.delete(`/api/favorites/${product.id}`)
      if (res.code === 200) {
        product.favorited = false
        product.favoriteCount--
        return
      }
    } else {
      const res = await request.post(`/api/favorites/${product.id}`)
      if (res.code === 200) {
        product.favorited = true
        product.favoriteCount++
        return
      }
    }
    throw new Error('操作失败')
  } catch (e2) {
    ElMessage.error(e2?.response?.data?.message || e2?.message || '操作失败')
  }
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>商品广场</h2>
        <p>浏览所有已上架的商品。</p>
      </div>
      <div class="page-actions">
        <el-button @click="goBackHome">返回首页</el-button>
        <el-button type="primary" @click="goToPublish">发布商品</el-button>
      </div>
    </div>

    <div class="search-row">
      <el-autocomplete
        v-model="keyword"
        :fetch-suggestions="querySearch"
        placeholder="搜索商品..."
        :trigger-on-focus="false"
        clearable
        @keyup.enter="handleSearch"
        @select="handleSelect"
        class="search-input"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="keyword" @click="clearSearch">清除</el-button>
    </div>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <div v-else class="product-grid" v-loading="loading">
      <div v-for="product in products" :key="product.id" class="product-card" @click="goToDetail(product.id)">
        <img :src="product.coverImage" alt="商品封面" class="product-cover" />
        <div class="price-row">
          <span class="price">￥{{ product.price }}</span>
          <span class="time">{{ product.createdAt }}</span>
        </div>
        <h3>{{ product.title }}</h3>
        <p>{{ product.description }}</p>
        <div class="footer-row">
          <span>卖家：{{ product.sellerName }}</span>
          <span class="fav-action" @click.stop>
            <el-icon
              :color="product.favorited ? '#e6a23c' : '#c0c4cc'"
              :size="18"
              style="cursor:pointer"
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
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  padding: 32px 20px;
}

.page-header {
  max-width: 1100px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  color: #909399;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.search-row {
  max-width: 1100px;
  margin: 0 auto 24px;
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
}

.product-grid {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.08);
  cursor: pointer;
  transition: transform 0.15s;
}

.product-card:hover {
  transform: translateY(-2px);
}

.product-cover {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 12px;
  display: block;
  margin-bottom: 16px;
  background: #eef2ff;
}

.price-row,
.footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.time,
.footer-row {
  color: #909399;
  font-size: 13px;
}

.fav-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: default;
  user-select: none;
}

.product-card h3 {
  font-size: 20px;
  color: #303133;
  margin: 16px 0 10px;
}

.product-card p {
  color: #606266;
  line-height: 1.7;
  min-height: 72px;
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

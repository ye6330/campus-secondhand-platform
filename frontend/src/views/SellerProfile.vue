<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { StarFilled, Star } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const products = ref([])
const sellerName = ref('卖家')

const loadSellerProducts = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/products/seller/${route.params.sellerId}`)
    if (res.code === 200) {
      products.value = res.data || []
      if (products.value.length > 0) {
        sellerName.value = products.value[0].sellerName || '卖家'
      }
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载卖家主页失败')
  } finally {
    loading.value = false
  }
}

const loadSellerContact = async () => {
  try {
    const res = await request.get(`/api/users/${route.params.sellerId}/contact`)
    if (res.code === 200) {
      sellerName.value = res.data?.nickname || res.data?.username || sellerName.value
      return
    }
    throw new Error(res.message)
  } catch (e) {
    // ignore contact load failure for seller homepage
  }
}

onMounted(() => {
  loadSellerProducts()
  loadSellerContact()
})

watch(() => route.params.sellerId, () => {
  loadSellerProducts()
  loadSellerContact()
})

const goBack = () => {
  router.back()
}

const goToChat = () => {
  router.push(`/my/messages/${route.params.sellerId}`)
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
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="goBack">&lt; 返回</el-button>
      <div>
        <h2>{{ sellerName }}的主页</h2>
        <p>查看该卖家的在售、交易中和已售出商品</p>
      </div>
      <el-button type="primary" plain @click="goToChat">联系卖家</el-button>
    </div>

    <el-empty v-if="!loading && products.length === 0" description="该卖家暂时没有可展示商品" />

    <div v-else class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card" :class="[`status-${product.status}`]" @click="goToDetail(product.id)">
        <div class="cover-wrap">
          <img :src="product.coverImage" alt="商品封面" class="product-cover" />
          <div v-if="product.status === '已售出'" class="sold-stamp">已售出</div>
          <div v-else-if="product.status === '交易中'" class="trading-chip">交易中</div>
        </div>
        <div class="card-head">
          <el-tag v-if="product.status === '已上架'" size="small" type="success">在售</el-tag>
          <el-tag v-else-if="product.status === '交易中'" size="small" type="warning">交易中</el-tag>
          <el-tag v-else-if="product.status === '已售出'" size="small" type="danger">已售出</el-tag>
        </div>
        <div class="price-row">
          <span class="price">￥{{ product.price }}</span>
          <span class="time">{{ product.createdAt }}</span>
        </div>
        <h3>{{ product.title }}</h3>
        <p>{{ product.description }}</p>
        <div class="footer-row">
          <div class="card-stats" @click.stop>
            <span class="view-count">浏览 {{ product.viewCount || 0 }}</span>
            <span class="fav-action">
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
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  padding: 32px 20px;
}

.page-header,
.product-grid {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 8px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.placeholder {
  width: 72px;
}

.product-grid {
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

.product-card.status-已售出 {
  background: #fff;
  border: 1px solid #f3d0d0;
}

.product-card.status-交易中 {
  background: #fff;
  border: 1px solid #f3dfb2;
}

.cover-wrap {
  position: relative;
  margin-bottom: 16px;
}

.product-cover {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 12px;
  display: block;
  background: #eef2ff;
}

.product-card.status-已售出 .product-cover {
  filter: grayscale(0.75) brightness(0.92);
}

.sold-stamp {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  right: 14px;
  top: 14px;
  width: 88px;
  height: 88px;
  border: 4px solid rgba(220, 38, 38, 0.9);
  border-radius: 999px;
  color: rgba(220, 38, 38, 0.92);
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.08em;
  background: rgba(255, 255, 255, 0.72);
  transform: rotate(-18deg);
  box-shadow: 0 8px 18px rgba(220, 38, 38, 0.18);
  pointer-events: none;
}

.trading-chip {
  position: absolute;
  left: 14px;
  top: 14px;
  height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  font-weight: 700;
  color: #92400e;
  background: rgba(255, 251, 235, 0.95);
  border: 1px solid #fcd34d;
  box-shadow: 0 6px 14px rgba(180, 83, 9, 0.12);
  pointer-events: none;
}

.card-head {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
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

.card-stats {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.view-count {
  color: #909399;
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
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

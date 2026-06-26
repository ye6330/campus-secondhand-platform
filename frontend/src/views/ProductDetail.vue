<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import { StarFilled, Star } from '@element-plus/icons-vue'
import request from '../utils/request'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const product = ref(null)
const sellerPhone = ref('')
const isSeller = ref(false)
const comments = ref([])
const commentLoading = ref(false)
const commentSubmitting = ref(false)
const commentContent = ref('')
const reportVisible = ref(false)
const reportSubmitting = ref(false)
const reportReason = ref('')
const orderVisible = ref(false)
const orderSubmitting = ref(false)
const orderNote = ref('')
const imagePreviewVisible = ref(false)

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

const loadSellerContact = async (sellerId) => {
  if (!sellerId) {
    sellerPhone.value = ''
    return
  }
  try {
    const res = await request.get(`/api/users/${sellerId}/contact`)
    if (res.code === 200) {
      sellerPhone.value = res.data?.phone || ''
      return
    }
    throw new Error(res.message)
  } catch (e) {
    sellerPhone.value = ''
  }
}

const loadComments = async () => {
  commentLoading.value = true
  try {
    const res = await request.get('/api/comments', { params: { productId: route.params.id } })
    if (res.code === 200) {
      comments.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载留言失败')
  } finally {
    commentLoading.value = false
  }
}

onMounted(() => {
  loadDetail().then(() => {
    if (product.value?.sellerId) {
      loadSellerContact(product.value.sellerId)
    }
  })
  loadComments()
})

const goBack = () => {
  router.push('/products')
}

const goToEdit = () => {
  router.push(`/products/${route.params.id}/edit`)
}

const goToSeller = () => {
  if (!product.value?.sellerId) return
  router.push(`/seller/${product.value.sellerId}`)
}

const goToChatSeller = () => {
  if (!product.value?.sellerId) return
  router.push(`/my/messages/${product.value.sellerId}`)
}

const openImagePreview = () => {
  if (!product.value?.coverImage) return
  imagePreviewVisible.value = true
}

const toggleFavorite = async () => {
  if (!product.value) return
  try {
    if (product.value.favorited) {
      const res = await request.delete(`/api/favorites/${product.value.id}`)
      if (res.code === 200) {
        product.value.favorited = false
        product.value.favoriteCount--
        return
      }
    } else {
      const res = await request.post(`/api/favorites/${product.value.id}`)
      if (res.code === 200) {
        product.value.favorited = true
        product.value.favoriteCount++
        return
      }
    }
    throw new Error('操作失败')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
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

const handleOffShelf = () => {
  ElMessageBox.confirm('确定下架这个商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.put(`/api/products/${route.params.id}/off-shelf`)
      if (res.code === 200) {
        ElMessage.success('下架成功')
        loadDetail()
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '下架失败')
    }
  }).catch(() => {})
}

const handleRelist = () => {
  ElMessageBox.confirm('确定重新上架这个商品吗？重新上架后将进入待审核。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const res = await request.put(`/api/products/${route.params.id}/relist`)
      if (res.code === 200) {
        ElMessage.success('已提交重新上架审核')
        loadDetail()
        return
      }
      throw new Error(res.message)
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || e?.message || '重新上架失败')
    }
  }).catch(() => {})
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  commentSubmitting.value = true
  try {
    const res = await request.post('/api/comments', {
      productId: Number(route.params.id),
      content: commentContent.value.trim()
    })
    if (res.code === 200) {
      comments.value.unshift(res.data)
      commentContent.value = ''
      ElMessage.success('留言成功')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '留言失败')
  } finally {
    commentSubmitting.value = false
  }
}

const deleteComment = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除这条留言吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/api/comments/${item.id}`)
    if (res.code === 200) {
      comments.value = comments.value.filter(comment => comment.id !== item.id)
      ElMessage.success('删除成功')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
    }
  }
}

const submitReport = async () => {
  if (!reportReason.value.trim()) {
    ElMessage.warning('请输入举报原因')
    return
  }
  reportSubmitting.value = true
  try {
    const res = await request.post('/api/reports', {
      productId: Number(route.params.id),
      reason: reportReason.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('举报已提交，请等待管理员处理')
      reportVisible.value = false
      reportReason.value = ''
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '举报提交失败')
  } finally {
    reportSubmitting.value = false
  }
}

const submitOrder = async () => {
  orderSubmitting.value = true
  try {
    const res = await request.post('/api/orders', {
      productId: Number(route.params.id),
      note: orderNote.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('购买意向已提交，请等待卖家确认')
      orderVisible.value = false
      orderNote.value = ''
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交购买意向失败')
  } finally {
    orderSubmitting.value = false
  }
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="detail-container" v-if="product">
      <div class="detail-header">
        <el-button @click="goBack">&lt; 返回列表</el-button>
        <div class="detail-actions" v-if="isSeller">
          <el-button type="primary" @click="goToEdit">编辑</el-button>
          <el-button v-if="product.status === '已上架'" type="warning" @click="handleOffShelf">下架</el-button>
          <el-button v-if="product.status === '已下架'" type="success" @click="handleRelist">上架</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>
      <div class="detail-content">
        <div class="detail-image">
          <img :src="product.coverImage" alt="商品封面" class="previewable-image" @click="openImagePreview" />
        </div>
        <div class="detail-info">
          <div class="detail-title-row">
            <h1>{{ product.title }}</h1>
             <el-tag v-if="product.status === '待审核'" type="warning">待审核</el-tag>
             <el-tag v-else-if="product.status === '已上架'" type="success">已上架</el-tag>
             <el-tag v-else-if="product.status === '交易中'" type="warning">交易中</el-tag>
             <el-tag v-else-if="product.status === '已下架'" type="info">已下架</el-tag>
             <el-tag v-else-if="product.status === '已售出'" type="success">已售出</el-tag>
             <el-tag v-else-if="product.status === '已拒绝'" type="danger">已拒绝</el-tag>
          </div>
          <div class="detail-price-row">
            <div class="detail-price">￥{{ product.price }}</div>
            <div class="detail-actions-row">
              <div class="detail-fav" @click="toggleFavorite">
                <el-icon :color="product.favorited ? '#e6a23c' : '#c0c4cc'" :size="22" style="cursor:pointer">
                  <StarFilled v-if="product.favorited" />
                  <Star v-else />
                </el-icon>
                <span>{{ product.favoriteCount || 0 }}</span>
              </div>
              <el-button v-if="!isSeller && product.status === '已上架'" type="primary" plain @click="orderVisible = true">我想要</el-button>
              <el-tag v-else-if="!isSeller && product.status === '交易中'" type="warning" effect="dark">该商品正在交易中</el-tag>
              <el-tag v-else-if="!isSeller && product.status === '已售出'" type="info" effect="dark">该商品已售出</el-tag>
              <el-button v-if="!isSeller" plain @click="goToChatSeller">联系卖家</el-button>
              <el-button v-if="!isSeller" text type="danger" @click="reportVisible = true">举报商品</el-button>
            </div>
          </div>
          <div class="detail-meta">
            <div class="seller-inline">
              <span class="meta-label">卖家</span>
              <button class="seller-link" @click="goToSeller">{{ product.sellerName }}</button>
              <span class="phone-inline">{{ sellerPhone || '暂未填写' }}</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-inline">
              <span class="meta-label">发布时间</span>
              <span class="meta-value">{{ product.createdAt }}</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-inline">
              <span class="meta-label">浏览量</span>
              <span class="meta-value">{{ product.viewCount || 0 }}</span>
            </div>
          </div>
          <el-divider />
          <div class="detail-desc">
            <h3>商品描述</h3>
            <p>{{ product.description }}</p>
          </div>
        </div>
      </div>

      <div class="comment-section">
        <div class="comment-header">
          <h3>商品留言</h3>
          <span>{{ comments.length }} 条</span>
        </div>
        <div class="comment-editor">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可以留言问价格、成色、交易方式等"
          />
          <div class="comment-submit-row">
            <el-button type="primary" :loading="commentSubmitting" @click="submitComment">发表评论</el-button>
          </div>
        </div>

        <div class="comment-list" v-loading="commentLoading">
          <el-empty v-if="!commentLoading && comments.length === 0" description="还没有人留言" />
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <div class="comment-top-row">
              <div class="comment-user">
                <strong>{{ item.username }}</strong>
                <el-tag v-if="item.mine" size="small" type="success">我</el-tag>
              </div>
              <div class="comment-right">
                <span class="comment-time">{{ item.createdAt }}</span>
                <el-button v-if="item.mine" text type="danger" @click="deleteComment(item)">删除</el-button>
              </div>
            </div>
            <p class="comment-content">{{ item.content }}</p>
          </div>
        </div>
      </div>

      <el-dialog v-model="reportVisible" title="举报商品" width="480px">
        <el-input
          v-model="reportReason"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请填写举报原因，例如虚假信息、违规商品、恶意描述等"
        />
        <template #footer>
          <el-button @click="reportVisible = false">取消</el-button>
          <el-button type="danger" :loading="reportSubmitting" @click="submitReport">提交举报</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="orderVisible" title="发起购买意向" width="480px">
        <el-input
          v-model="orderNote"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="可以填写你的交易时间、地点偏好等（选填）"
        />
        <template #footer>
          <el-button @click="orderVisible = false">取消</el-button>
          <el-button type="primary" :loading="orderSubmitting" @click="submitOrder">提交意向</el-button>
        </template>
      </el-dialog>

      <el-image-viewer
        v-if="imagePreviewVisible"
        :url-list="[product.coverImage]"
        :initial-index="0"
        @close="imagePreviewVisible = false"
      />
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

.comment-section {
  margin-top: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.08);
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.comment-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.comment-header span {
  color: #909399;
  font-size: 13px;
}

.comment-editor {
  margin-bottom: 20px;
}

.comment-submit-row {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 14px 16px;
  background: #fafcff;
}

.comment-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-right {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.comment-content {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.detail-image {
  flex: 0 0 420px;
}

.detail-image img {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
}

.previewable-image {
  cursor: zoom-in;
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

.detail-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.detail-actions-row {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.detail-price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.detail-fav {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  color: #909399;
  font-size: 14px;
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-inline,
.seller-inline {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta-inline {
  color: #606266;
  font-size: 14px;
}

.meta-label {
  color: #909399;
  font-size: 12px;
  letter-spacing: 0.5px;
}

.meta-value {
  color: #606266;
}

.phone-inline {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f4f6f8;
  color: #606266;
  font-size: 13px;
}

.meta-divider {
  width: 1px;
  height: 16px;
  background: #e4e7ed;
}

.seller-link {
  border: none;
  background: transparent;
  color: #409eff;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
  font-weight: 400;
}

.seller-link:hover {
  color: #66b1ff;
}

@media (max-width: 768px) {
  .detail-meta {
    align-items: flex-start;
    gap: 10px;
  }

  .meta-divider {
    display: none;
  }

  .meta-inline,
  .seller-inline {
    width: 100%;
  }
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

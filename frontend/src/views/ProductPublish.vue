<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const PRODUCT_DRAFT_KEY = 'product_publish_draft'
const router = useRouter()
const loading = ref(false)
const imageUploading = ref(false)

const form = ref({
  title: '',
  description: '',
  price: null,
  coverImage: ''
})

const loadDraft = () => {
  const draftText = localStorage.getItem(PRODUCT_DRAFT_KEY)
  if (!draftText) {
    return
  }

  try {
    const draft = JSON.parse(draftText)
    form.value = {
      title: draft.title || '',
      description: draft.description || '',
      price: draft.price ?? null,
      coverImage: draft.coverImage || ''
    }
  } catch (e) {
    localStorage.removeItem(PRODUCT_DRAFT_KEY)
  }
}

const clearDraft = () => {
  localStorage.removeItem(PRODUCT_DRAFT_KEY)
}

watch(
  form,
  newValue => {
    localStorage.setItem(PRODUCT_DRAFT_KEY, JSON.stringify(newValue))
  },
  { deep: true }
)

onMounted(() => {
  loadDraft()
})

const handleSubmit = async () => {
  if (!form.value.title || !form.value.description || !form.value.price || !form.value.coverImage) {
    ElMessage.warning('请填写完整的商品信息')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/api/products', form.value)
    if (res.code === 200) {
      clearDraft()
      ElMessage.success('商品发布成功，等待管理员审核')
      router.push('/my/products')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '商品发布失败')
  } finally {
    loading.value = false
  }
}

const handleImageChange = async uploadFile => {
  const rawFile = uploadFile.raw
  if (!rawFile) {
    return
  }

  if (!['image/jpeg', 'image/png', 'image/webp'].includes(rawFile.type)) {
    ElMessage.error('仅支持 jpg、png、webp 图片')
    return
  }

  const formData = new FormData()
  formData.append('file', rawFile)

  imageUploading.value = true
  try {
    const res = await request.post('/api/products/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.code === 200) {
      form.value.coverImage = res.data
      ElMessage.success('图片上传成功')
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '图片上传失败')
  } finally {
    imageUploading.value = false
  }
}

const handleImageRemove = () => {
  form.value.coverImage = ''
}

const goBack = () => {
  router.push('/home')
}
</script>

<template>
  <div class="publish-page">
    <div class="publish-card">
      <div class="header-row">
        <div>
          <h2>发布商品</h2>
          <p>先把最小可用的商品发布流程跑通。</p>
        </div>
        <el-button @click="goBack">返回首页</el-button>
      </div>

      <el-form label-position="top" class="publish-form">
        <el-form-item label="商品标题">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="例如：九成新机械键盘" />
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="描述商品成色、购买时间、适用情况等"
          />
        </el-form-item>

        <el-form-item label="商品价格">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>

        <el-form-item label="商品封面图">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/png,image/jpeg,image/webp"
            :on-change="handleImageChange"
          >
            <div v-if="form.coverImage" class="cover-preview-wrapper">
              <img :src="form.coverImage" alt="商品封面" class="cover-preview" />
              <div class="cover-actions">
                <el-button size="small" :loading="imageUploading">重新上传</el-button>
                <el-button size="small" type="danger" plain @click.stop="handleImageRemove">删除</el-button>
              </div>
            </div>
            <div v-else class="cover-uploader-placeholder" v-loading="imageUploading">
              <el-icon :size="28"><Plus /></el-icon>
              <span>点击上传商品封面图</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading || imageUploading" @click="handleSubmit">立即发布</el-button>
          <el-button @click="router.push('/products')">查看商品列表</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.publish-page {
  min-height: 100%;
  background: linear-gradient(180deg, #eef2ff 0%, #f8fafc 100%);
  padding: 40px 20px;
}

.publish-card {
  max-width: 760px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 16px 50px rgba(102, 126, 234, 0.12);
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.header-row h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}

.header-row p {
  color: #909399;
}

.publish-form :deep(.el-input__wrapper),
.publish-form :deep(.el-textarea__inner),
.publish-form :deep(.el-input-number) {
  width: 100%;
}

.cover-uploader {
  width: 100%;
}

.cover-uploader-placeholder {
  width: 100%;
  min-height: 220px;
  border: 1px dashed #cdd0d6;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  background: #fafbff;
}

.cover-preview-wrapper {
  width: 100%;
}

.cover-preview {
  width: 100%;
  max-height: 320px;
  object-fit: cover;
  border-radius: 16px;
  display: block;
}

.cover-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

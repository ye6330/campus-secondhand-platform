<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = ref({
  title: '',
  description: '',
  price: '',
  coverImage: ''
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/products/${route.params.id}`)
    if (res.code === 200) {
      form.value = {
        title: res.data.title,
        description: res.data.description,
        price: res.data.price,
        coverImage: res.data.coverImage
      }
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error('加载商品信息失败')
    router.push('/products')
  } finally {
    loading.value = false
  }
})

const submit = async () => {
  if (!form.value.title || !form.value.description || !form.value.price) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await request.put(`/api/products/${route.params.id}`, form.value)
    if (res.code === 200) {
      ElMessage.success('修改成功，等待审核')
      router.push(`/products/${route.params.id}`)
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '修改失败')
  }
}

const cancel = () => {
  router.push(`/products/${route.params.id}`)
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="form-container">
      <h2>编辑商品</h2>
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price" type="number" min="0.01" step="0.01">
            <template #prefix>￥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="封面">
          <img v-if="form.coverImage" :src="form.coverImage" class="preview" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">保存修改</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  padding: 32px 20px;
}

.form-container {
  max-width: 600px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.08);
}

.form-container h2 {
  margin-bottom: 24px;
  color: #303133;
}

.preview {
  width: 200px;
  border-radius: 8px;
}
</style>

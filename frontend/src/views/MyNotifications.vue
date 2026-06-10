<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const notifications = ref([])

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/notifications/my')
    if (res.code === 200) {
      notifications.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载通知失败')
  } finally {
    loading.value = false
  }
}

const markRead = async (item) => {
  if (item.read) return
  try {
    const res = await request.put(`/api/notifications/${item.id}/read`)
    if (res.code === 200) {
      item.read = true
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '标记已读失败')
  }
}

onMounted(() => {
  loadNotifications()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/home')">&lt; 返回首页</el-button>
      <h2>我的通知</h2>
      <div class="placeholder"></div>
    </div>

    <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />

    <div v-else class="notification-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-card"
        :class="{ unread: !item.read }"
        @click="markRead(item)"
      >
        <div class="card-header">
          <strong>{{ item.title }}</strong>
          <div class="right-area">
            <el-tag v-if="!item.read" type="danger" size="small">未读</el-tag>
            <span class="time">{{ item.createdAt }}</span>
          </div>
        </div>
        <p>{{ item.content }}</p>
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
.notification-list {
  max-width: 860px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.placeholder {
  width: 88px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notification-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  border: 1px solid transparent;
}

.notification-card.unread {
  border-color: #f56c6c;
  background: #fffafa;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.right-area {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.time {
  color: #909399;
  font-size: 12px;
}

.notification-card p {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>

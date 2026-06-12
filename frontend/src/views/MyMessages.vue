<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const conversations = ref([])

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/messages/conversations')
    if (res.code === 200) {
      conversations.value = res.data || []
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载会话列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConversations()
})

const openChat = (item) => {
  router.push(`/my/messages/${item.targetUserId}`)
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <el-button @click="router.push('/home')">&lt; 返回首页</el-button>
      <h2>我的私信</h2>
      <div class="placeholder"></div>
    </div>

    <el-empty v-if="!loading && conversations.length === 0" description="暂无私信" />

    <div v-else class="conversation-list">
      <div
        v-for="item in conversations"
        :key="item.targetUserId"
        class="conversation-card"
        @click="openChat(item)"
      >
        <div class="conv-left">
          <el-avatar :size="44" :src="item.targetAvatar">
            {{ (item.targetNickname || item.targetUsername)[0] }}
          </el-avatar>
        </div>
        <div class="conv-main">
          <div class="conv-top-row">
            <strong>{{ item.targetNickname || item.targetUsername }}</strong>
            <span class="conv-time">{{ item.lastMessageTime }}</span>
          </div>
          <div class="conv-bottom-row">
            <span class="conv-last">{{ item.lastMessage }}</span>
            <span v-if="item.lastMessageMine" class="conv-read-status" :class="{ read: item.lastMessageRead }">{{ item.lastMessageRead ? '已读' : '已发送' }}</span>
            <el-badge v-if="item.unreadCount > 0" :value="item.unreadCount" class="unread-badge" />
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
.page-header, .conversation-list {
  max-width: 720px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0; color: #303133; }
.placeholder { width: 88px; }
.conversation-list { display: flex; flex-direction: column; gap: 12px; }
.conversation-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 14px; padding: 16px 18px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05); cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.conversation-card:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.conv-left { flex-shrink: 0; }
.conv-main { flex: 1; min-width: 0; }
.conv-top-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 6px; }
.conv-top-row strong { color: #303133; font-size: 15px; }
.conv-time { color: #909399; font-size: 12px; white-space: nowrap; }
.conv-bottom-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.conv-last { color: #909399; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.unread-badge { flex-shrink: 0; }
.conv-read-status {
  font-size: 11px;
  color: #909399;
  flex-shrink: 0;
}
.conv-read-status.read {
  color: #67c23a;
}
</style>

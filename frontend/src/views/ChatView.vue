<script setup>
import { onMounted, ref, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const messages = ref([])
const inputContent = ref('')
const sending = ref(false)
const targetUser = ref({ nickname: '', username: '' })
const messagesEnd = ref(null)

const targetUserId = () => route.params.targetUserId

const scrollToBottom = () => {
  nextTick(() => {
    messagesEnd.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/messages/conversations/${targetUserId()}`)
    if (res.code === 200) {
      messages.value = res.data || []
      if (messages.value.length > 0) {
        const first = messages.value[0]
        targetUser.value.nickname = first.fromUserId === Number(targetUserId()) ? first.fromNickname : first.toNickname
        targetUser.value.username = first.fromUserId === Number(targetUserId()) ? first.fromUsername : first.toUsername
      } else {
        const contactRes = await request.get(`/api/users/${targetUserId()}/contact`)
        if (contactRes.code === 200) {
          targetUser.value.nickname = contactRes.data?.nickname || contactRes.data?.username
          targetUser.value.username = contactRes.data?.username || ''
        }
      }
      scrollToBottom()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载聊天记录失败')
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sending.value = true
  try {
    const res = await request.post('/api/messages', { toUserId: Number(targetUserId()), content })
    if (res.code === 200) {
      messages.value.push(res.data)
      inputContent.value = ''
      scrollToBottom()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const displayName = () => targetUser.value.nickname || targetUser.value.username || '对方'

onMounted(() => {
  loadMessages()
})

watch(() => route.params.targetUserId, () => {
  loadMessages()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="chat-header">
      <el-button @click="router.push('/my/messages')">&lt; 返回</el-button>
      <h3>{{ displayName() }}</h3>
      <div class="placeholder"></div>
    </div>

    <div class="chat-body" v-if="!loading">
      <div v-if="messages.length === 0" class="chat-empty">
        <p>开始和 {{ displayName() }} 聊天吧</p>
      </div>
      <div v-else class="message-list">
        <div
          v-for="item in messages"
          :key="item.id"
          class="message-item"
          :class="{ mine: item.mine }"
        >
          <div class="bubble">{{ item.content }}</div>
          <div class="msg-meta">
            <span v-if="item.mine" class="msg-status">{{ item.read ? '已读' : '已发送' }}</span>
            <span class="msg-time">{{ item.createdAt }}</span>
          </div>
        </div>
        <div ref="messagesEnd"></div>
      </div>
    </div>

    <div class="chat-footer">
      <el-input
        v-model="inputContent"
        type="textarea"
        :rows="2"
        maxlength="500"
        show-word-limit
        placeholder="输入消息..."
        @keyup.ctrl.enter="sendMessage"
      />
      <el-button type="primary" :loading="sending" @click="sendMessage" class="send-btn">发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100%;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 10;
}
.chat-header h3 { margin: 0; color: #303133; }
.placeholder { width: 72px; }
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
.chat-empty {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}
.message-list { display: flex; flex-direction: column; gap: 16px; }
.message-item { display: flex; flex-direction: column; align-items: flex-start; max-width: 72%; }
.message-item.mine { align-self: flex-end; align-items: flex-end; }
.bubble {
  padding: 12px 16px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
  border-bottom-left-radius: 6px;
}
.message-item.mine .bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-bottom-left-radius: 18px;
  border-bottom-right-radius: 6px;
}
.msg-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 0 4px;
}

.msg-status {
  font-size: 11px;
  color: #909399;
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
}
.chat-footer {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
.send-btn {
  height: 52px;
  border-radius: 12px;
  flex-shrink: 0;
}
</style>

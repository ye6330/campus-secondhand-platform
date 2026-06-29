<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const logs = ref([])
const filters = ref({
  action: '',
  result: ''
})

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.action.trim()) params.action = filters.value.action.trim()
    if (filters.value.result) params.result = filters.value.result
    const res = await request.get('/api/operation-logs/admin', { params })
    if (res.code === 200) {
      logs.value = res.data || []
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载操作日志失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = { action: '', result: '' }
  loadLogs()
}

const resultTagType = (result) => {
  if (result === '成功') return 'success'
  if (result === '失败') return 'danger'
  return 'info'
}

onMounted(() => {
  loadLogs()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h2>操作日志</h2>
        <p>查看关键业务操作的执行结果、操作者和耗时。</p>
      </div>
      <el-button @click="router.push('/admin/home')">&lt; 返回后台</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="filters.action"
        placeholder="按操作名称筛选，如：确认订单"
        clearable
        @keyup.enter="loadLogs"
      />
      <el-select v-model="filters.result" clearable placeholder="执行结果">
        <el-option label="成功" value="成功" />
        <el-option label="失败" value="失败" />
      </el-select>
      <el-button type="primary" @click="loadLogs">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-empty v-if="!loading && logs.length === 0" description="暂无操作日志" />

    <div v-else class="log-list">
      <div v-for="item in logs" :key="item.id" class="log-card">
        <div class="log-head">
          <div class="title-group">
            <strong>{{ item.action }}</strong>
            <el-tag size="small" :type="resultTagType(item.result)">{{ item.result }}</el-tag>
          </div>
          <span class="time">{{ item.createdAt }}</span>
        </div>

        <div class="log-grid">
          <div><span class="label">操作者：</span>{{ item.operatorName || '未知用户' }}</div>
          <div><span class="label">角色：</span>{{ item.operatorRole || '未知角色' }}</div>
          <div><span class="label">用户ID：</span>{{ item.operatorId ?? '—' }}</div>
          <div><span class="label">耗时：</span>{{ item.durationMs }} ms</div>
        </div>

        <div class="detail-block">
          <div><span class="label">方法：</span>{{ item.methodName }}</div>
          <div><span class="label">参数：</span>{{ item.params || '[]' }}</div>
          <div v-if="item.errorMessage"><span class="label">异常：</span>{{ item.errorMessage }}</div>
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
.filter-bar,
.log-list {
  max-width: 1080px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.filter-bar {
  display: grid;
  grid-template-columns: 1.8fr 160px 100px 100px;
  gap: 12px;
  margin-bottom: 20px;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.log-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-group strong {
  font-size: 18px;
  color: #303133;
}

.time {
  font-size: 13px;
  color: #909399;
}

.log-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
  color: #606266;
}

.detail-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #606266;
  line-height: 1.6;
  word-break: break-all;
}

.label {
  color: #909399;
}

@media (max-width: 900px) {
  .filter-bar {
    grid-template-columns: 1fr 1fr;
  }

  .log-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .page-header,
  .log-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-bar,
  .log-grid {
    grid-template-columns: 1fr;
  }
}
</style>

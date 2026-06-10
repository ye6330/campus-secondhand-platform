<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const loading = ref(false)
const reports = ref([])
const activeTab = ref('all')

const loadReports = async () => {
  loading.value = true
  try {
    const params = activeTab.value === 'all' ? {} : { status: activeTab.value }
    const res = await request.get('/api/reports/admin', { params })
    if (res.code === 200) {
      reports.value = res.data
      return
    }
    throw new Error(res.message)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '加载举报记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReports()
})

watch(activeTab, () => {
  loadReports()
})

const handleReport = async (item, action) => {
  const actionTextMap = {
    resolve: '举报属实，但商品无需处罚',
    reject: '举报不成立',
    off_shelf: '举报属实，违规下架商品',
    delete_product: '举报属实，违规删除商品'
  }
  const actionText = actionTextMap[action] || '处理举报'
  try {
    const { value } = await ElMessageBox.prompt(`确定执行“${actionText}”吗？可填写处理说明（选填）`, '处理举报', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: ''
    })
    const res = await request.put(`/api/reports/${item.id}/handle?action=${action}`, {
      handleNote: value || ''
    })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadReports()
      return
    }
    throw new Error(res.message)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.response?.data?.message || e?.message || `${actionText}失败`)
    }
  }
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <h2>举报处理</h2>
      <p>管理员审核举报后，可判断举报是否成立，并决定是否下架或删除商品。</p>
    </div>

    <el-tabs v-model="activeTab" class="tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待处理" name="待处理" />
      <el-tab-pane label="已处理" name="已处理" />
      <el-tab-pane label="违规已下架" name="违规已下架" />
      <el-tab-pane label="违规已删除" name="违规已删除" />
      <el-tab-pane label="已驳回" name="已驳回" />
    </el-tabs>

    <el-empty v-if="!loading && reports.length === 0" description="暂无举报记录" />

    <div v-else class="report-list">
      <div v-for="item in reports" :key="item.id" class="report-card">
        <div class="report-main">
          <div class="title-row">
            <strong>{{ item.productTitle }}</strong>
            <el-tag v-if="item.status === '待处理'" type="warning">待处理</el-tag>
            <el-tag v-else-if="item.status === '已处理'" type="success">已处理</el-tag>
            <el-tag v-else-if="item.status === '违规已下架'" type="danger">违规已下架</el-tag>
            <el-tag v-else-if="item.status === '违规已删除'" type="danger">违规已删除</el-tag>
            <el-tag v-else type="info">已驳回</el-tag>
          </div>
          <p>举报人：{{ item.reporterName }}</p>
          <p>举报原因：{{ item.reason }}</p>
          <p>提交时间：{{ item.createdAt }}</p>
          <p v-if="item.handleNote">处理说明：{{ item.handleNote }}</p>
          <p v-if="item.handledAt">处理时间：{{ item.handledAt }}</p>
        </div>
        <div v-if="item.status === '待处理'" class="report-actions">
          <el-button @click="handleReport(item, 'resolve')">举报属实，无需处罚</el-button>
          <el-button type="warning" @click="handleReport(item, 'off_shelf')">举报属实，下架商品</el-button>
          <el-button type="danger" @click="handleReport(item, 'delete_product')">举报属实，删除商品</el-button>
          <el-button type="info" @click="handleReport(item, 'reject')">举报不成立</el-button>
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
.tabs,
.report-list {
  max-width: 860px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.tabs {
  margin-bottom: 16px;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.report-main {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.report-main strong {
  color: #303133;
  font-size: 17px;
}

.report-main p {
  margin: 6px 0;
  color: #606266;
}

.report-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>

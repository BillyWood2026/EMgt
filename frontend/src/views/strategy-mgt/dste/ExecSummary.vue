<template>
  <div class="page-container">
    <div class="page-header"><h2>执行台账汇总</h2></div>
    <el-row :gutter="20">
      <el-col :span="6"><el-card><div class="stat-item"><div class="stat-label">任务总数</div><div class="stat-value">{{ summary.taskTotal || 0 }}</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat-item"><div class="stat-label">已完成</div><div class="stat-value" style="color:#67c23a">{{ summary.taskCompleted || 0 }}</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat-item"><div class="stat-label">逾期</div><div class="stat-value" style="color:#f56c6c">{{ summary.taskOverdue || 0 }}</div></div></el-card></el-col>
      <el-col :span="6"><el-card><div class="stat-item"><div class="stat-label">计划完成率</div><div class="stat-value">{{ summary.planCompletionRate || 0 }}%</div></div></el-card></el-col>
    </el-row>
    <el-card style="margin-top:20px">
      <template #header>风险与督办概览</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="未闭环风险">{{ summary.riskOpen || 0 }}</el-descriptions-item>
        <el-descriptions-item label="未销号督办">{{ summary.supervisionOpen || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { dsteApi } from '@/api/strategy'
import { ElMessage } from 'element-plus'
const summary = ref({})
async function loadData() {
  try {
    const res = await dsteApi.execSummary()
    if (res.data.code === 200) summary.value = res.data.data
  } catch (e) { ElMessage.error('加载汇总数据失败') }
}
onMounted(loadData)
</script>
<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-label { color: #909399; font-size: 14px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
</style>

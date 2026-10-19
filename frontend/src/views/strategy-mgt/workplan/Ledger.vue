<template>
  <div class="page-container">
    <div class="page-header">
      <h2>台账统计</h2>
      <el-button type="primary" @click="handleGenerate">生成台账</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="台账按计划+统计周期由系统自动汇总生成：总任务数/已完成/逾期数来自WBS任务，完成率=∑权重×进度，平均得分来自考核打分。月结归档后台账只读。" />

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="台账统计" title="台账统计导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px" show-summary :summary-method="summaryMethod">
      <el-table-column prop="planId" label="计划ID" width="90" />
      <el-table-column prop="planName" label="计划名称" min-width="150" show-overflow-tooltip>
        <template #default="{row}">{{ planNameOf(row.planId) }}</template>
      </el-table-column>
      <el-table-column prop="statPeriod" label="统计周期" width="110" />
      <el-table-column prop="totalTasks" label="总任务数" width="100" />
      <el-table-column prop="completedTasks" label="已完成" width="90" />
      <el-table-column prop="overdueTasks" label="逾期数" width="90">
        <template #default="{row}">
          <span :style="{color: Number(row.overdueTasks) > 0 ? '#f56c6c' : '#67c23a', fontWeight: Number(row.overdueTasks) > 0 ? 'bold' : 'normal'}">{{ row.overdueTasks ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="completionRate" label="完成率%" width="110">
        <template #default="{row}">
          <el-progress :percentage="Math.min(100, Math.round(Number(row.completionRate) || 0))" :stroke-width="14" :status="rateStatus(row.completionRate)" />
        </template>
      </el-table-column>
      <el-table-column prop="avgScore" label="平均得分" width="100">
        <template #default="{row}">
          <span :style="{color: scoreColor(row.avgScore), fontWeight:'bold'}">{{ row.avgScore ?? '—' }}</span>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="archiveStatus" label="归档状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.archiveStatus === '已归档' ? 'success' : 'info'" size="small">{{ row.archiveStatus || '未归档' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="生成时间" width="170" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="warning" @click="regenerate(row)">重新生成</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="生成台账" width="520px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="所属计划" required>
          <el-select v-model="formData.planId" filterable placeholder="请选择计划" style="width:100%">
            <el-option v-for="p in planList" :key="p.id" :label="`${p.planName}（${p.planCode}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计周期" required>
          <el-input v-model="formData.statPeriod" placeholder="如 2026-01 / 2026-Q1" />
        </el-form-item>
        <el-alert type="warning" :closable="false" title="生成台账将重新统计该计划当前WBS任务与考核打分数据" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const tableData = ref([])
const planList = ref([])

const ioColumns = [
  { key: 'planId', title: '计划ID' },
  { key: 'statPeriod', title: '统计周期' },
  { key: 'totalTasks', title: '总任务数' },
  { key: 'completedTasks', title: '已完成' },
  { key: 'overdueTasks', title: '逾期数' },
  { key: 'completionRate', title: '完成率%' },
  { key: 'avgScore', title: '平均得分' }
]

const dialogVisible = ref(false)
const formData = reactive({ planId: null, statPeriod: '' })

function planNameOf(id) {
  const p = planList.value.find(x => x.id === id)
  return p ? p.planName : `#${id}`
}
function rateStatus(rate) {
  const v = Number(rate) || 0
  if (v >= 100) return 'success'
  if (v >= 80) return ''
  if (v >= 60) return 'warning'
  return 'exception'
}
function scoreColor(s) {
  const v = Number(s) || 0
  if (v >= 80) return '#67c23a'
  if (v >= 60) return '#e6a23c'
  return '#f56c6c'
}
function summaryMethod({ columns, data }) {
  const sums = []
  columns.forEach((col, i) => {
    if (i === 0) { sums[i] = '合计/均值'; return }
    const prop = col.property
    if (['totalTasks', 'completedTasks', 'overdueTasks'].includes(prop)) {
      sums[i] = data.reduce((a, r) => a + (Number(r[prop]) || 0), 0)
    } else if (prop === 'completionRate' || prop === 'avgScore') {
      if (!data.length) { sums[i] = '—'; return }
      const avg = data.reduce((a, r) => a + (Number(r[prop]) || 0), 0) / data.length
      sums[i] = avg.toFixed(1)
    } else sums[i] = ''
  })
  return sums
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listLedgers()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}
async function loadPlans() {
  try {
    const res = await workplanApi.listPlans()
    if (res.data.code === 200) planList.value = res.data.data || []
  } catch (e) {}
}

function handleGenerate() {
  formData.planId = null
  formData.statPeriod = ''
  dialogVisible.value = true
}
function regenerate(row) {
  formData.planId = row.planId
  formData.statPeriod = row.statPeriod
  dialogVisible.value = true
}

async function submitGenerate() {
  if (!formData.planId) { ElMessage.warning('请选择所属计划'); return }
  if (!formData.statPeriod) { ElMessage.warning('请填写统计周期'); return }
  try {
    await ElMessageBox.confirm('确认生成台账？将按当前数据重新统计。', '生成台账', { type: 'warning' })
    const res = await workplanApi.generateLedger(formData.planId, formData.statPeriod)
    if (res.data.code === 200) {
      ElMessage.success('台账已生成')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || '生成失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '生成失败') }
}

onMounted(() => { loadData(); loadPlans() })
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

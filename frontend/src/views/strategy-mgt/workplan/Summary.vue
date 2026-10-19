<template>
  <div class="page-container">
    <div class="page-header">
      <h2>总结复盘</h2>
      <el-button type="primary" @click="handleAdd">新增复盘</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="复盘状态机：草稿 → 已提交 → 已归档。提交后不可随意修改；复盘需覆盖亮点、问题、改进措施与下期计划四要素。" />

    <div class="filter-bar">
      <el-input-number v-model="queryPlanId" :min="0" controls-position="right" placeholder="按计划ID筛选" style="width:160px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="queryPlanId=null;loadData()">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="总结复盘" title="总结复盘导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="planId" label="计划ID" width="90" />
      <el-table-column prop="summaryPeriod" label="复盘周期" width="110" />
      <el-table-column prop="overallDesc" label="总体描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="highlights" label="亮点" min-width="130" show-overflow-tooltip />
      <el-table-column prop="problems" label="问题" min-width="130" show-overflow-tooltip />
      <el-table-column prop="improvement" label="改进措施" min-width="130" show-overflow-tooltip />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="rectifyOwner" label="整改责任人" width="105">
        <template #default="{row}">{{ row.rectifyOwner || '—' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="rectifyDeadline" label="整改时限" width="110">
        <template #default="{row}">{{ row.rectifyDeadline || '—' }}</template>
      </el-table-column>
      <el-table-column prop="summaryStatus" label="状态" width="95">
        <template #default="{row}">
          <el-tag :type="statusType(row.summaryStatus)" size="small">{{ row.summaryStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.summaryStatus === '草稿'" size="small" type="success" @click="flow(row, '已提交')">提交</el-button>
          <el-button v-if="row.summaryStatus === '已提交'" size="small" type="info" @click="flow(row, '已归档')">归档</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="780px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计划ID" required>
              <el-input-number v-model="formData.planId" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="复盘周期" required>
              <el-input v-model="formData.summaryPeriod" placeholder="如 2026-01 / 2026-Q1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="总体描述">
          <el-input v-model="formData.overallDesc" type="textarea" :rows="3" placeholder="计划执行总体情况" />
        </el-form-item>
        <el-form-item label="亮点">
          <el-input v-model="formData.highlights" type="textarea" :rows="2" placeholder="超额/创新/协同亮点" />
        </el-form-item>
        <el-form-item label="问题">
          <el-input v-model="formData.problems" type="textarea" :rows="2" placeholder="未完成项及原因分析" />
        </el-form-item>
        <el-form-item label="改进措施">
          <el-input v-model="formData.improvement" type="textarea" :rows="2" placeholder="针对性改进措施" />
        </el-form-item>
        <el-row :gutter="12">
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="12">
            <el-form-item label="整改责任人">
              <el-input v-model="formData.rectifyOwner" placeholder="改进措施责任人" />
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="12">
            <el-form-item label="整改时限">
              <el-date-picker v-model="formData.rectifyDeadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="下期计划">
          <el-input v-model="formData.nextPlan" type="textarea" :rows="2" placeholder="下一周期计划要点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="复盘详情" width="720px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="计划ID">{{ viewRow.planId }}</el-descriptions-item>
        <el-descriptions-item label="复盘周期">{{ viewRow.summaryPeriod }}</el-descriptions-item>
        <el-descriptions-item label="总体描述">{{ viewRow.overallDesc }}</el-descriptions-item>
        <el-descriptions-item label="亮点">{{ viewRow.highlights }}</el-descriptions-item>
        <el-descriptions-item label="问题">{{ viewRow.problems }}</el-descriptions-item>
        <el-descriptions-item label="改进措施">{{ viewRow.improvement }}</el-descriptions-item>
        <el-descriptions-item label="整改责任人">{{ viewRow.rectifyOwner || '—' }}</el-descriptions-item>
        <el-descriptions-item label="整改时限">{{ viewRow.rectifyDeadline || '—' }}</el-descriptions-item>
        <el-descriptions-item label="下期计划">{{ viewRow.nextPlan }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewRow.summaryStatus)" size="small">{{ viewRow.summaryStatus }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
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
const queryPlanId = ref(null)

const ioColumns = [
  { key: 'planId', title: '计划ID' },
  { key: 'summaryPeriod', title: '复盘周期' },
  { key: 'overallDesc', title: '总体描述' },
  { key: 'highlights', title: '亮点' },
  { key: 'problems', title: '问题' },
  { key: 'improvement', title: '改进措施' },
  { key: 'rectifyOwner', title: '整改责任人' },
  { key: 'rectifyDeadline', title: '整改时限' },
  { key: 'nextPlan', title: '下期计划' },
  { key: 'summaryStatus', title: '状态' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增复盘')
const viewVisible = ref(false)
const viewRow = ref({})
const formData = reactive({
  id: null, planId: null, summaryPeriod: '', overallDesc: '', highlights: '',
  problems: '', improvement: '', rectifyOwner: '', rectifyDeadline: '', nextPlan: '', summaryStatus: '草稿'
})

function statusType(s) {
  return { '草稿': 'info', '已提交': 'warning', '已归档': 'success' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listSummaries(queryPlanId.value || undefined)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增复盘'
  Object.assign(formData, { id: null, planId: null, summaryPeriod: '', overallDesc: '', highlights: '', problems: '', improvement: '', rectifyOwner: '', rectifyDeadline: '', nextPlan: '', summaryStatus: '草稿' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑复盘'
  Object.assign(formData, row)
  dialogVisible.value = true
}
function handleView(row) {
  viewRow.value = row
  viewVisible.value = true
}

async function flow(row, target) {
  const tip = target === '已提交' ? '提交后进入审核环节，确认提交？' : '确认归档该复盘？归档后仅可查看'
  try {
    await ElMessageBox.confirm(tip, '状态流转', { type: 'warning' })
    const res = await workplanApi.saveSummary({ ...row, summaryStatus: target })
    if (res.data.code === 200) { ElMessage.success('操作成功'); loadData() }
    else ElMessage.error(res.data.message || '操作失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败') }
}

async function handleSubmit() {
  if (!formData.planId) { ElMessage.warning('请填写计划ID'); return }
  if (!formData.summaryPeriod) { ElMessage.warning('请填写复盘周期'); return }
  try {
    const res = await workplanApi.saveSummary(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { display: flex; gap: 10px; margin-top: 14px; align-items: center; }
</style>

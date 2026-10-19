<template>
  <div class="page-container">
    <div class="page-header">
      <h2>三级计划WBS</h2>
      <el-button type="primary" @click="handleAdd">新增计划</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="计划状态机：草稿 → 待审批 → 执行中 → 已完成/逾期未完成/已终止 → 已归档；月结锁定后不可修改。计划完成率 = ∑(一级任务权重 × 任务进度)，WBS 一级任务权重合计必须=100%。" />

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input-number v-model="queryYear" :min="2000" :max="2100" controls-position="right" placeholder="计划年度" style="width:140px" />
      <el-select v-model="queryStatus" placeholder="计划状态" clearable style="width:150px">
        <el-option v-for="s in statusList" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="三级计划WBS" title="三级计划WBS导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="planCode" label="计划编码" width="120" />
      <el-table-column prop="planName" label="计划名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="planType" label="类型" width="80" />
      <el-table-column prop="planYear" label="年度" width="70" />
      <el-table-column prop="planPeriod" label="周期" width="110" show-overflow-tooltip />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="sourceType" label="计划来源" width="110">
        <template #default="{row}">{{ row.sourceType || '—' }}</template>
      </el-table-column>
      <el-table-column prop="ownerDept" label="责任部门" width="110" show-overflow-tooltip />
      <el-table-column prop="owner" label="负责人" width="90" />
      <el-table-column prop="totalWeight" label="WBS权重%" width="95">
        <template #default="{row}">
          <span :style="{color: Number(row.totalWeight) === 100 ? '#67c23a' : '#e6a23c'}">{{ row.totalWeight ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="完成率%" width="110">
        <template #default="{row}">
          <el-button size="small" link type="primary" @click="showRate(row)">查看</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="planStatus" label="状态" width="105">
        <template #default="{row}">
          <el-tag :type="statusType(row.planStatus)" size="small">{{ row.planStatus }}</el-tag>
          <el-tag v-if="row.lockStatus === 1" type="danger" size="small" style="margin-left:4px">已锁定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="330" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="openWbs(row)">WBS拆解</el-button>
          <el-button v-if="row.planStatus === '草稿'" size="small" type="success" @click="changeStatus(row, '待审批')">提交审批</el-button>
          <el-button v-if="['草稿','待审批','执行中'].includes(row.planStatus)" size="small" type="danger" @click="changeStatus(row, '已终止')">终止</el-button>
          <el-button v-if="['已完成','逾期未完成','已终止'].includes(row.planStatus)" size="small" type="info" @click="changeStatus(row, '已归档')">归档</el-button>
          <el-button v-if="row.lockStatus !== 1" size="small" type="danger" plain @click="lockPlan(row)">月结锁定</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑计划 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计划编码" required>
              <el-input v-model="formData.planCode" placeholder="请输入计划编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划名称" required>
              <el-input v-model="formData.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计划类型">
              <el-select v-model="formData.planType" style="width:100%">
                <el-option label="年度" value="年度" />
                <el-option label="季度" value="季度" />
                <el-option label="月度" value="月度" />
                <el-option label="专项" value="专项" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划年度">
              <el-input-number v-model="formData.planYear" :min="2000" :max="2100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计划周期">
          <el-input v-model="formData.planPeriod" placeholder="如 2026-Q1 / 2026-01" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="计划来源">
          <el-select v-model="formData.sourceType" style="width:100%" placeholder="战略解码60% / 部门职责25% / 临时交办15%">
            <el-option label="战略解码" value="战略解码" />
            <el-option label="部门职责" value="部门职责" />
            <el-option label="临时交办" value="临时交办" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="战略对齐项">
          <el-input v-model="formData.alignItem" placeholder="关联的战略目标/重点任务" />
        </el-form-item>
        <el-form-item label="关联模板">
          <el-input-number v-model="formData.templateId" :min="0" controls-position="right" style="width:100%" placeholder="计划模板ID" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="责任部门">
              <el-input v-model="formData.ownerDept" placeholder="请输入责任部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="formData.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="formData.startTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="formData.endTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- WBS 任务拆解抽屉 -->
    <el-drawer v-model="wbsVisible" :title="`WBS任务拆解 — ${currentPlan?.planName || ''}`" size="72%">
      <div style="padding:0 16px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
          <div>
            <el-tag :type="weightCheck.isValid ? 'success' : 'warning'">
              一级任务权重合计：{{ weightCheck.totalWeight ?? 0 }}% {{ weightCheck.isValid ? '（校验通过）' : '（须=100%）' }}
            </el-tag>
            <el-tag type="info" style="margin-left:8px">完成率 = ∑权重×进度</el-tag>
          </div>
          <el-button type="primary" size="small" @click="handleAddTask">新增任务</el-button>
        </div>
        <el-table :data="wbsList" border stripe size="small" v-loading="wbsLoading">
          <el-table-column prop="taskCode" label="任务编码" width="110" />
          <el-table-column prop="taskName" label="任务名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="taskLevel" label="层级" width="70" />
          <el-table-column prop="taskWeight" label="权重%" width="80" />
          <el-table-column prop="taskGoal" label="任务目标" min-width="150" show-overflow-tooltip />
          <el-table-column prop="milestone" label="里程碑" width="80">
            <template #default="{row}">
              <el-tag v-if="row.milestone === 1 || row.milestone === '1'" type="danger" size="small">是</el-tag>
              <span v-else>否</span>
            </template>
          </el-table-column>
          <el-table-column prop="assignee" label="责任人" width="90" />
          <el-table-column prop="progress" label="进度%" width="90" />
          <el-table-column prop="taskStatus" label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="taskStatusType(row.taskStatus)" size="small">{{ row.taskStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dependencyIds" label="前置任务" width="100">
            <template #default="{row}">{{ row.dependencyIds || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" @click="handleEditTask(row)">编辑</el-button>
              <el-button v-if="row.taskStatus !== '已完成'" size="small" type="success" @click="completeTask(row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 任务编辑弹窗 -->
    <el-dialog v-model="taskDialogVisible" :title="taskDialogTitle" width="720px" append-to-body>
      <el-form :model="taskForm" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="任务编码" required>
              <el-input v-model="taskForm.taskCode" placeholder="请输入任务编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务名称" required>
              <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="任务层级">
              <el-input-number v-model="taskForm.taskLevel" :min="1" :max="3" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="父任务ID">
              <el-input-number v-model="taskForm.parentTaskId" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务权重%">
              <el-input-number v-model="taskForm.taskWeight" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务目标">
          <el-input v-model="taskForm.taskGoal" type="textarea" :rows="2" placeholder="请输入任务目标（量化）" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="开始日期">
              <el-date-picker v-model="taskForm.startTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束日期">
              <el-date-picker v-model="taskForm.endTime" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="里程碑">
              <el-radio-group v-model="taskForm.milestone">
                <el-radio-button :value="1">是</el-radio-button>
                <el-radio-button :value="0">否</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="前置任务ID">
              <el-input v-model="taskForm.dependencyIds" placeholder="多个用逗号分隔，前置未完成不可完成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任人">
              <el-input v-model="taskForm.assignee" placeholder="请输入责任人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="当前进度%">
          <el-input-number v-model="taskForm.progress" :min="0" :max="100" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const loading = ref(false)
const tableData = ref([])
const queryYear = ref(null)
const queryStatus = ref('')
const statusList = ['草稿', '待审批', '执行中', '已完成', '逾期未完成', '已终止', '已归档']

const ioColumns = [
  { key: 'planCode', title: '计划编码' },
  { key: 'planName', title: '计划名称' },
  { key: 'planType', title: '计划类型' },
  { key: 'planYear', title: '计划年度' },
  { key: 'planPeriod', title: '计划周期' },
  { key: 'sourceType', title: '计划来源' },
  { key: 'ownerDept', title: '责任部门' },
  { key: 'owner', title: '负责人' },
  { key: 'startTime', title: '开始日期' },
  { key: 'endTime', title: '结束日期' },
  { key: 'planStatus', title: '状态' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增计划')
const formData = reactive({
  id: null, planCode: '', planName: '', planType: '年度', planYear: null, planPeriod: '',
  templateId: null, sourceType: '', alignItem: '', ownerDept: '', owner: '', startTime: '', endTime: ''
})

// WBS
const wbsVisible = ref(false)
const wbsLoading = ref(false)
const wbsList = ref([])
const currentPlan = ref(null)
const weightCheck = reactive({ totalWeight: 0, isValid: false })
const taskDialogVisible = ref(false)
const taskDialogTitle = ref('新增任务')
const taskForm = reactive({
  id: null, planId: null, taskCode: '', taskName: '', taskLevel: 1, parentTaskId: null,
  taskWeight: null, taskGoal: '', startTime: '', endTime: '', milestone: 0,
  dependencyIds: '', assignee: '', progress: 0, taskStatus: '未启动'
})

function statusType(s) {
  return { '草稿': 'info', '待审批': 'warning', '执行中': 'primary', '已完成': 'success', '逾期未完成': 'danger', '已终止': 'danger', '已归档': 'info' }[s] || 'info'
}
function taskStatusType(s) {
  return { '未启动': 'info', '进行中': 'primary', '已完成': 'success', '已逾期': 'danger' }[s] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listPlans(queryYear.value || undefined, queryStatus.value || undefined)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}
function resetQuery() { queryYear.value = null; queryStatus.value = ''; loadData() }

function handleAdd() {
  dialogTitle.value = '新增计划'
  Object.keys(formData).forEach(k => formData[k] = (k === 'planType' ? '年度' : null) || (k === 'planType' ? '年度' : ''))
  formData.planType = '年度'
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑计划'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.planCode || !formData.planName) { ElMessage.warning('请填写计划编码与计划名称'); return }
  try {
    const res = await workplanApi.savePlan(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

async function changeStatus(row, target) {
  try {
    await ElMessageBox.confirm(`确认将计划【${row.planName}】流转为「${target}」？`, '状态流转', { type: 'warning' })
    // 提交审批时优先走流程引擎
    if (target === '待审批') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_WORKPLAN,
          bizCode: 'workplan',
          bizId: row.id,
          bizJson: row,
          title: `工作计划-${row.planName}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        const res = await workplanApi.changeStatus(row.id, target)
        if (res.data.code === 200) {
          ElMessage.success('已发起流程审批，实例号：' + instNo)
          loadData()
        } else ElMessage.error(res.data.message || '流转失败')
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    const res = await workplanApi.changeStatus(row.id, target)
    if (res.data.code === 200) { ElMessage.success('流转成功'); loadData() }
    else ElMessage.error(res.data.message || '流转失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '流转失败') }
}

async function lockPlan(row) {
  try {
    await ElMessageBox.confirm(`月结锁定后计划【${row.planName}】不可修改，确认锁定？`, '月结锁定', { type: 'error', confirmButtonText: '确认锁定' })
    const res = await workplanApi.lockPlan(row.id)
    if (res.data.code === 200) { ElMessage.success('已锁定'); loadData() }
    else ElMessage.error(res.data.message || '锁定失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '锁定失败') }
}

async function showRate(row) {
  try {
    const res = await workplanApi.completionRate(row.id)
    if (res.data.code === 200) {
      const d = res.data.data
      const rate = (d && (d.completionRate ?? d.rate ?? d)) ?? 0
      ElMessageBox.alert(`计划【${row.planName}】完成率：${rate}%（∑一级任务权重×进度）`, '计划完成率', { confirmButtonText: '知道了' })
    } else ElMessage.error(res.data.message || '查询失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '查询失败') }
}

// ===== WBS =====
async function openWbs(row) {
  currentPlan.value = row
  wbsVisible.value = true
  await loadWbs()
}
async function loadWbs() {
  wbsLoading.value = true
  try {
    const res = await workplanApi.listWbs(currentPlan.value.id)
    if (res.data.code === 200) wbsList.value = res.data.data || []
    const chk = await workplanApi.checkWeight(currentPlan.value.id)
    if (chk.data.code === 200) Object.assign(weightCheck, chk.data.data || {})
  } catch (e) { ElMessage.error('加载WBS失败') }
  finally { wbsLoading.value = false }
}
function handleAddTask() {
  taskDialogTitle.value = '新增任务'
  Object.assign(taskForm, { id: null, planId: currentPlan.value.id, taskCode: '', taskName: '', taskLevel: 1, parentTaskId: null, taskWeight: null, taskGoal: '', startTime: '', endTime: '', milestone: 0, dependencyIds: '', assignee: '', progress: 0, taskStatus: '未启动' })
  taskDialogVisible.value = true
}
function handleEditTask(row) {
  taskDialogTitle.value = '编辑任务'
  Object.assign(taskForm, row)
  taskDialogVisible.value = true
}
async function submitTask() {
  if (!taskForm.taskCode || !taskForm.taskName) { ElMessage.warning('请填写任务编码与任务名称'); return }
  taskForm.planId = currentPlan.value.id
  try {
    const res = await workplanApi.saveWbs(taskForm)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      taskDialogVisible.value = false
      await loadWbs()
      loadData()
    } else ElMessage.error(res.data.message || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}
async function completeTask(row) {
  try {
    await ElMessageBox.confirm(`确认完成任务【${row.taskName}】？前置任务未完成将被阻断。`, '完成任务', { type: 'warning' })
    const res = await workplanApi.completeTask(row.id)
    if (res.data.code === 200) { ElMessage.success('任务已完成'); await loadWbs(); loadData() }
    else ElMessage.error(res.data.message || '操作失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { display: flex; gap: 10px; margin-top: 14px; align-items: center; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>计划审批</h2>
      <el-button type="primary" @click="handleAdd">登记审批</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="审批保存后自动同步计划状态：已通过→执行中，已驳回→草稿。驳回时必须填写审批意见。" />

    <div class="filter-bar">
      <el-input-number v-model="queryPlanId" :min="0" controls-position="right" placeholder="按计划ID筛选" style="width:160px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="queryPlanId=null;loadData()">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="计划审批" title="计划审批导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="planId" label="计划ID" width="90" />
      <el-table-column prop="planName" label="计划名称" min-width="150" show-overflow-tooltip>
        <template #default="{row}">{{ planNameOf(row.planId) }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="approvalNode" label="审批节点" width="110">
        <template #default="{row}">{{ row.approvalNode || '—' }}</template>
      </el-table-column>
      <el-table-column prop="approvalStatus" label="审批状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.approvalStatus)" size="small">{{ row.approvalStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="approver" label="审批人" width="100" />
      <el-table-column prop="approvalOpinion" label="审批意见" min-width="180" show-overflow-tooltip />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="rejectRequire" label="驳回整改要求" width="130" show-overflow-tooltip>
        <template #default="{row}">{{ row.rejectRequire || '—' }}</template>
      </el-table-column>
      <el-table-column prop="approvalTime" label="审批时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button v-if="row.approvalStatus === '待审批'" size="small" type="success" @click="doApprove(row, '已通过')">通过</el-button>
          <el-button v-if="row.approvalStatus === '待审批'" size="small" type="danger" @click="doApprove(row, '已驳回')">驳回</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="110px">
        <el-form-item label="所属计划" required>
          <el-select v-model="formData.planId" filterable placeholder="请选择计划" style="width:100%">
            <el-option v-for="p in planList" :key="p.id" :label="`${p.planName}（${p.planCode}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="审批节点">
          <el-select v-model="formData.approvalNode" style="width:100%" placeholder="部门负责人/分管领导/总经理">
            <el-option label="部门负责人" value="部门负责人" />
            <el-option label="分管领导" value="分管领导" />
            <el-option label="总经理" value="总经理" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态" required>
          <el-radio-group v-model="formData.approvalStatus">
            <el-radio-button value="待审批">待审批</el-radio-button>
            <el-radio-button value="已通过">已通过</el-radio-button>
            <el-radio-button value="已驳回">已驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批人" required>
          <el-input v-model="formData.approver" placeholder="请输入审批人" />
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="formData.approvalOpinion" type="textarea" :rows="3" placeholder="驳回时必填" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="驳回整改要求" v-if="formData.approvalStatus === '已驳回'">
          <el-input v-model="formData.rejectRequire" type="textarea" :rows="2" placeholder="请输入驳回后的整改要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
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
const planList = ref([])
const queryPlanId = ref(null)

const ioColumns = [
  { key: 'planId', title: '计划ID' },
  { key: 'approvalNode', title: '审批节点' },
  { key: 'approvalStatus', title: '审批状态' },
  { key: 'approver', title: '审批人' },
  { key: 'approvalOpinion', title: '审批意见' },
  { key: 'rejectRequire', title: '驳回整改要求' },
  { key: 'approvalTime', title: '审批时间' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('登记审批')
const formData = reactive({
  id: null, planId: null, approvalNode: '', approvalStatus: '待审批',
  approver: '', approvalOpinion: '', rejectRequire: ''
})

function statusType(s) {
  return { '待审批': 'warning', '已通过': 'success', '已驳回': 'danger' }[s] || 'info'
}
function planNameOf(id) {
  const p = planList.value.find(x => x.id === id)
  return p ? p.planName : `#${id}`
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listApprovals(queryPlanId.value || undefined)
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

function handleAdd() {
  dialogTitle.value = '登记审批'
  Object.assign(formData, { id: null, planId: null, approvalNode: '', approvalStatus: '待审批', approver: '', approvalOpinion: '', rejectRequire: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑审批'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function doApprove(row, target) {
  if (target === '已驳回') {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回意见（必填）', '驳回计划', {
        confirmButtonText: '确认驳回', cancelButtonText: '取消',
        inputValidator: v => !!v && v.trim().length > 0 || '驳回意见必填'
      })
      const res = await workplanApi.saveApproval({ planId: row.planId, approvalStatus: '已驳回', approver: row.approver, approvalOpinion: value })
      afterSave(res)
    } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败') }
  } else {
    try {
      await ElMessageBox.confirm('确认审批通过？通过后计划进入「执行中」。', '审批通过', { type: 'success' })
      const res = await workplanApi.saveApproval({ planId: row.planId, approvalStatus: '已通过', approver: row.approver, approvalOpinion: row.approvalOpinion || '同意' })
      afterSave(res)
    } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.message || '操作失败') }
  }
}
function afterSave(res) {
  if (res.data.code === 200) { ElMessage.success('操作成功'); loadData() }
  else ElMessage.error(res.data.message || '操作失败')
}

async function handleSubmit() {
  if (!formData.planId) { ElMessage.warning('请选择所属计划'); return }
  if (!formData.approver) { ElMessage.warning('请填写审批人'); return }
  if (formData.approvalStatus === '已驳回' && !formData.approvalOpinion) { ElMessage.warning('驳回时审批意见必填'); return }
  try {
    const res = await workplanApi.saveApproval(formData)
    if (res.data.code === 200) {
      // 登记待审批时尝试走流程引擎
      if (formData.approvalStatus === '待审批') {
        try {
          const instNo = await startApproval({
            defCode: FLOW_DEF_CODES.HRM_WORKPLAN,
            bizCode: 'workplan',
            bizId: formData.planId,
            bizJson: formData,
            title: `工作计划审批-${formData.planId}`,
            initiator: localStorage.getItem('username') || 'dev-user'
          })
          ElMessage.success('已发起流程审批，实例号：' + instNo)
        } catch (e) {
          ElMessage.warning('流程引擎不可用，已本地提交')
        }
      } else {
        ElMessage.success('保存成功')
      }
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

onMounted(() => { loadData(); loadPlans() })
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { display: flex; gap: 10px; margin-top: 14px; align-items: center; }
</style>

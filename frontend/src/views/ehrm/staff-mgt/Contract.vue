<template>
  <div class="page-card">
    <div class="page-header">
      <h2>合同管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增合同
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ contractList.length }}</div>
            <div class="stat-label">合同总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ expiringCount }}</div>
            <div class="stat-label">即将到期</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ activeCount }}</div>
            <div class="stat-label">有效合同</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ signedCount }}</div>
            <div class="stat-label">已签订</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <el-table :data="contractList" border stripe size="small">
        <el-table-column prop="contractNo" label="合同编号" width="160" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="contractType" label="合同类型" width="120">
          <template #default="{row}">
            <el-tag size="small">{{ getContractTypeLabel(row.contractType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="probationEndDate" label="试用期结束" width="120" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="signingEntity" label="签约主体" width="130">
          <template #default="{row}">{{ row.signingEntity || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="renewCount" label="续签次数" width="90">
          <template #default="{row}">{{ row.renewCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            <el-tag v-if="isExpiringSoon(row)" type="danger" size="small" style="margin-left:4px">30天内到期</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="期限" width="100">
          <template #default="{row}">{{ getDurationLabel(row.duration) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" type="warning" v-if="row.status === '有效'" @click="renewContract(row)">续签</el-button>
            <el-button link size="small" type="danger" v-if="row.status === '有效'" @click="terminateContract(row)">终止</el-button>
            <el-button link size="small" type="info" v-if="row.status === '有效'" @click="archiveContract(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-select v-model="formData.employeeId" placeholder="选择员工" @change="onEmployeeChange">
                <el-option v-for="emp in employees" :key="emp.id" :label="emp.name" :value="emp.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同编号" required>
              <el-input v-model="formData.contractNumber" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同类型" required>
              <el-select v-model="formData.contractType" placeholder="选择合同类型">
                <el-option label="劳动合同" value="LABOR" />
                <el-option label="劳务协议" value="SERVICE" />
                <el-option label="保密协议" value="CONFIDENTIALITY" />
                <el-option label="竞业协议" value="NON_COMPETE" />
                <el-option label="培训服务协议" value="TRAINING_SERVICE" />
                <el-option label="外派协议" value="EXPATRIATE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同期限">
              <el-select v-model="formData.contractDurationType" placeholder="选择期限">
                <el-option label="1年" value="FIXED" />
                <el-option label="2年" value="FIXED" />
                <el-option label="3年" value="FIXED" />
                <el-option label="无固定期限" value="UNFIXED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" required>
              <el-date-picker v-model="formData.contractStartDate" type="date" placeholder="选择开始日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" required>
              <el-date-picker v-model="formData.contractEndDate" type="date" placeholder="选择结束日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <!-- 规格书字段，待后端扩展 -->
          <el-col :span="12">
            <el-form-item label="签约主体">
              <el-input v-model="formData.signingEntity" placeholder="请输入签约主体（法人公司）" />
            </el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展（校验：合同3年以内试用期≤3个月，3年及以上≤6个月） -->
          <el-col :span="12">
            <el-form-item label="试用期时长(月)">
              <el-input-number v-model="formData.probationMonths" :min="0" :max="6" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经办人">
              <el-input v-model="formData.handler" placeholder="请输入经办人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同附件">
              <el-input v-model="formData.attachment" placeholder="附件扫描件编号/链接" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 合同详情抽屉：EL-CONT-01续签记录 + EL-CONT-02终止明细 -->
    <el-drawer v-model="detailVisible" :title="`合同档案详情：${detailRow.contractNo || ''}`" size="520px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="合同编号">{{ detailRow.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ detailRow.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ getContractTypeLabel(detailRow.contractType) }}</el-descriptions-item>
        <el-descriptions-item label="签约主体">{{ detailRow.signingEntity || '—' }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detailRow.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detailRow.endDate }}</el-descriptions-item>
        <el-descriptions-item label="试用期结束">{{ detailRow.probationEndDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="续签次数">{{ detailRow.renewCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ detailRow.handler || '—' }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin:14px 0 6px">EL-CONT-01 续签记录</h4>
      <el-table :data="detailRow.renewals || []" border stripe size="small">
        <el-table-column prop="time" label="续签时间" width="110" />
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column prop="clauseChange" label="条款变更" min-width="180" />
      </el-table>
      <el-empty v-if="!(detailRow.renewals || []).length" description="暂无续签记录" :image-size="60" />
      <h4 style="margin:14px 0 6px">EL-CONT-02 终止明细</h4>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="终止原因">{{ detailRow.terminationReason || '—' }}</el-descriptions-item>
        <el-descriptions-item label="终止日期">{{ detailRow.terminationDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="补偿金说明">{{ detailRow.compensationNote || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, CircleCheck, User } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const dialogVisible = ref(false)
const dialogTitle = ref('新增合同')

const employees = ref([])

const formData = reactive({
  id: '',
  contractNumber: '',
  employeeId: '',
  employeeName: '',
  contractType: 'LABOR',
  contractStartDate: '',
  contractEndDate: '',
  contractDurationType: 'FIXED',
  contractStatus: 'VALID',
  signingEntity: '',
  probationMonths: 0,
  handler: '',
  attachment: '',
  remark: ''
})

const contractList = ref([])
const detailVisible = ref(false)
const detailRow = ref({})

// 合同到期预警算法：到期前30天自动预警（规格书：到期前30天推送HR与直属领导站内预警）
function isExpiringSoon(row) {
  if (row.status !== '有效' || !row.endDate) return false
  const days = (new Date(row.endDate) - new Date()) / (1000 * 60 * 60 * 24)
  return days >= 0 && days <= 30
}

const expiringCount = computed(() => contractList.value.filter(c => isExpiringSoon(c)).length)
const activeCount = computed(() => contractList.value.filter(c => c.status === '有效').length)
const signedCount = computed(() => contractList.value.length)

function getStatusType(status) {
  const types = { '有效': 'success', '到期': 'info', '终止': 'danger', '续签中': 'warning', '作废': 'info' }
  return types[status] || 'info'
}

const STATUS_MAP = { VALID: '有效', EXPIRED: '到期', TERMINATED: '终止', CANCELLED: '终止', VOID: '作废' }
// 合同类型码值与中文显示的映射（规格书：劳动合同/劳务协议/保密协议/竞业协议/培训服务协议/外派协议）
const CONTRACT_TYPE_MAP = { LABOR: '劳动合同', SERVICE: '劳务协议', CONFIDENTIALITY: '保密协议', NON_COMPETE: '竞业协议', TRAINING_SERVICE: '培训服务协议', EXPATRIATE: '外派协议' }
// 合同期限码值与中文显示的映射
const DURATION_MAP = { FIXED: '固定期限', UNFIXED: '无固定期限' }

function getContractTypeLabel(type) {
  return CONTRACT_TYPE_MAP[type] || type || ''
}

function getDurationLabel(duration) {
  return DURATION_MAP[duration] || duration || ''
}

async function loadEmployees() {
  try {
    const { data } = await request.get('/api/hr-archive/employee')
    employees.value = (data?.data || []).map(e => ({ id: e.employeeId || e.id, name: e.employeeName }))
  } catch (e) { /* 忽略员工列表加载失败 */ }
}

async function loadContracts() {
  try {
    const { data } = await request.get('/api/hr-archive/contract')
    contractList.value = (data?.data || []).map(c => {
      const startDate = c.contractStartDate ? String(c.contractStartDate).slice(0, 10) : ''
      // 试用期结束 = 开始日期 + 试用期时长（规格书字段，后端未存则前端推算展示）
      let probationEndDate = ''
      if (startDate && c.probationMonths) {
        const d = new Date(startDate)
        d.setMonth(d.getMonth() + Number(c.probationMonths))
        probationEndDate = d.toISOString().slice(0, 10)
      }
      return {
        id: c.id,
        contractNo: c.contractNumber,
        employeeName: c.employeeName,
        employeeId: c.employeeId,
        department: '',
        contractType: c.contractType,
        startDate,
        endDate: c.contractEndDate ? String(c.contractEndDate).slice(0, 10) : '',
        probationMonths: c.probationMonths ?? null,
        probationEndDate,
        signingEntity: c.signingEntity || '',
        renewCount: c.renewCount ?? 0,
        handler: c.handler || '',
        renewals: c.renewals || [],
        terminationReason: c.terminationReason || '',
        terminationDate: c.terminationDate || '',
        compensationNote: c.compensationNote || '',
        status: STATUS_MAP[c.contractStatus] || '有效',
        duration: c.contractDurationType
      }
    })
  } catch (e) {
    ElMessage.error('合同列表加载失败：' + (e.message || ''))
  }
}

function onEmployeeChange(empId) {
  const emp = employees.value.find(e => e.id === empId)
  if (emp) formData.employeeName = emp.name
}

function openAddDialog() {
  dialogTitle.value = '新增合同'
  Object.assign(formData, {
    id: '', contractNumber: 'HT-' + new Date().getFullYear() + '-' + String(contractList.value.length + 1).padStart(3, '0'),
    employeeId: '', employeeName: '', contractType: 'LABOR',
    contractStartDate: '', contractEndDate: '', contractDurationType: 'FIXED', contractStatus: 'VALID',
    signingEntity: '', probationMonths: 0, handler: '', attachment: '', remark: ''
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

// 合同归档（状态机：有效/到期→归档封存，档案禁止物理删除）
function archiveContract(row) {
  ElMessageBox.confirm(`归档后合同「${row.contractNo}」转为只读封存，不可再续签/终止，确认归档？`, '归档二次确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.put('/api/hr-archive/contract', {
          id: row.id, contractNumber: row.contractNo, employeeId: row.employeeId,
          employeeName: row.employeeName, contractType: row.contractType,
          contractStartDate: row.startDate, contractEndDate: row.endDate,
          contractDurationType: row.duration, contractStatus: 'VOID'
        })
        ElMessage.success('合同已归档封存')
        loadContracts()
      } catch (e) { ElMessage.error('归档失败：' + (e.response?.data?.message || e.message || '')) }
    })
    .catch(() => {})
}

function renewContract(row) {
  ElMessageBox.confirm(`确定要续签${row.employeeName}的合同吗？`, '续签确认', { type: 'warning' })
    .then(async () => {
      try {
        await request.post('/api/hr-archive/contract/renewal', { contractNumber: row.contractNo, employeeId: row.employeeId, employeeName: row.employeeName })
        ElMessage.success('合同续签申请已提交')
        loadContracts()
      } catch (e) { ElMessage.error('续签失败：' + (e.response?.data?.message || e.message || '')) }
    })
    .catch(() => {})
}

function terminateContract(row) {
  ElMessageBox.confirm(`确定要终止${row.employeeName}的合同吗？`, '终止确认', { type: 'danger' })
    .then(async () => {
      try {
        await request.post('/api/hr-archive/contract/termination', { contractNumber: row.contractNo, employeeId: row.employeeId, employeeName: row.employeeName })
        ElMessage.success('合同终止申请已提交')
        loadContracts()
      } catch (e) { ElMessage.error('终止失败：' + (e.response?.data?.message || e.message || '')) }
    })
    .catch(() => {})
}

async function handleSubmit() {
  // 试用期合规校验算法（规格书硬性拦截）：合同3年以内试用期不超过3个月，3年及以上不超过6个月
  if (formData.contractStartDate && formData.contractEndDate && formData.probationMonths > 0) {
    const months = (new Date(formData.contractEndDate) - new Date(formData.contractStartDate)) / (1000 * 60 * 60 * 24 * 30.44)
    const limit = months >= 36 ? 6 : 3
    if (formData.probationMonths > limit) {
      ElMessage.error(`合同期限${months >= 36 ? '3年及以上' : '3年以内'}，试用期不得超过${limit}个月，已强制拦截违规录入`)
      return
    }
  }
  try {
    const isEdit = !!formData.id
    if (isEdit) {
      await request.put('/api/hr-archive/contract', formData)
    } else {
      await request.post('/api/hr-archive/contract', formData)
    }
    dialogVisible.value = false
    ElMessage.success('合同已保存')
    loadContracts()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => { loadEmployees(); loadContracts() })
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; background: #e3f2fd; color: #409eff; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }
</style>
<template>
  <div class="page-container">
    <div class="page-header">
      <h2>虚拟组织</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="list"
          file-name="虚拟组织"
          title="虚拟组织"
          import-api="/api/virtual-org/create"
          :field-map="ioFieldMap"
          @done="loadList"
        />
        <el-button type="primary" @click="handleAdd">新增虚拟组织</el-button>
        <el-button type="warning" plain @click="handleExpireRemind">到期提醒设置</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 180px" />
      </el-form-item>
      <el-form-item label="组织类型">
        <el-select v-model="searchForm.orgType" placeholder="选择类型" style="width: 150px">
          <el-option label="项目型" value="PROJECT" />
          <el-option label="专项任务型" value="SPECIAL" />
          <el-option label="临时团队型" value="TEMPORARY" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="筹备中" value="PREPARING" />
          <el-option label="生效" value="ACTIVE" />
          <el-option label="即将到期" value="EXPIRING" />
          <el-option label="已解散" value="DISBAND" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column type="index" label="序号" width="60" :index="indexOffset" />
      <el-table-column prop="virtualOrgCode" label="虚拟组织编码" width="150" />
      <el-table-column prop="virtualOrgName" label="虚拟组织名称" width="160" />
      <el-table-column prop="orgType" label="类型" width="110">
        <template #default="{ row }">
          {{ getOrgTypeLabel(row.orgType) }}
        </template>
      </el-table-column>
      <el-table-column prop="leadOrgName" label="牵头组织" width="130" />
      <el-table-column prop="leaderName" label="负责人" width="90" />
      <el-table-column prop="memberCount" label="成员数" width="80" />
      <el-table-column prop="effectiveDate" label="生效日期" width="160" show-overflow-tooltip />
      <el-table-column prop="expireDate" label="失效日期" width="160" show-overflow-tooltip />
      <el-table-column label="剩余有效期" width="110">
        <template #default="{ row }">
          <el-tag :type="remainingTagType(row)" size="small" effect="plain">
            {{ remainingDaysText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orgStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.orgStatus)" size="small">
            {{ getStatusLabel(row.orgStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="460" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" @click="openMembers(row)">成员管理</el-button>
          <el-button size="small" type="info" plain @click="viewArchitecture(row)">架构查看</el-button>
          <el-button size="small" type="info" @click="viewHistory(row)">变更历史</el-button>
          <el-button size="small" type="warning" v-if="row.orgStatus === 'ACTIVE' || row.orgStatus === 'EXPIRING'" @click="handleExtend(row)">续期申请</el-button>
          <el-button size="small" type="danger" v-if="row.orgStatus !== 'DISBAND'" @click="handleDisband(row)">解散申请</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-alert
      v-if="expiringCount > 0"
      type="warning"
      :title="`有 ${expiringCount} 个虚拟组织即将到期，请及时处理续期或解散`"
      show-icon
      :closable="false"
      style="margin-top: 12px"
    />

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="虚拟组织编码" required>
              <el-input v-model="formData.virtualOrgCode" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="虚拟组织名称" required>
              <el-input v-model="formData.virtualOrgName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织类型">
              <el-select v-model="formData.orgType" style="width: 100%">
                <el-option label="项目型" value="PROJECT" />
                <el-option label="专项任务型" value="SPECIAL" />
                <el-option label="临时团队型" value="TEMPORARY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.orgStatus" style="width: 100%">
                <el-option label="筹备中" value="PREPARING" />
                <el-option label="生效" value="ACTIVE" />
                <el-option label="即将到期" value="EXPIRING" />
                <el-option label="已解散" value="DISBAND" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人编号">
              <el-input v-model="formData.leaderId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人姓名">
              <el-input v-model="formData.leaderName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="牵头组织编号">
              <el-input-number v-model="formData.leadOrgId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="牵头组织名称">
              <el-input v-model="formData.leadOrgName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成员数">
              <el-input-number v-model="formData.memberCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参与组织">
              <el-input v-model="formData.participatingOrgs" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="formData.effectiveDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期">
              <el-date-picker
                v-model="formData.expireDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工作目标">
          <el-input v-model="formData.workObjective" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="虚拟组织详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="虚拟组织编码">{{ detailData.virtualOrgCode }}</el-descriptions-item>
        <el-descriptions-item label="虚拟组织名称">{{ detailData.virtualOrgName }}</el-descriptions-item>
        <el-descriptions-item label="组织类型">{{ getOrgTypeLabel(detailData.orgType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.orgStatus)" size="small">
            {{ getStatusLabel(detailData.orgStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detailData.leaderName }}</el-descriptions-item>
        <el-descriptions-item label="牵头组织">{{ detailData.leadOrgName }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ detailData.memberCount }}</el-descriptions-item>
        <el-descriptions-item label="参与组织">{{ detailData.participatingOrgs }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="工作目标" :span="2">{{ detailData.workObjective }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remarks }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="membersVisible" :title="`成员管理 - ${currentOrgName}`" width="800px">
      <div style="margin-bottom: 12px; text-align: right">
        <el-button size="small" type="primary" @click="addMemberVisible = true">添加成员</el-button>
      </div>
      <el-table :data="memberList" border stripe size="small" v-loading="membersLoading">
        <el-table-column prop="employeeName" label="姓名" width="110" />
        <el-table-column prop="originalOrgName" label="原组织" width="140" />
        <el-table-column prop="originalPositionName" label="原岗位" width="130" />
        <el-table-column prop="virtualRole" label="虚拟角色" width="110" />
        <el-table-column prop="joinDate" label="加入日期" width="110" />
        <el-table-column prop="memberStatus" label="状态" width="90" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="removeMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!membersLoading && memberList.length === 0" description="暂无成员" />
    </el-dialog>

    <el-dialog v-model="addMemberVisible" title="添加成员" width="480px" append-to-body>
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="员工编号" required>
          <el-input v-model="memberForm.employeeId" placeholder="请输入员工编号" />
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="memberForm.employeeName" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="虚拟角色">
          <el-input v-model="memberForm.virtualRole" placeholder="如：组长/成员" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddMember">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyVisible" :title="`变更历史 - ${historyName}`" width="800px">
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column prop="changeNo" label="变更编号" width="150" />
        <el-table-column prop="changeType" label="变更类型" width="110" />
        <el-table-column prop="beforeContent" label="变更前" min-width="140" show-overflow-tooltip />
        <el-table-column prop="afterContent" label="变更后" min-width="140" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="createTime" label="时间" width="160" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无变更记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'virtualOrgCode', title: '虚拟组织编码' },
  { key: 'virtualOrgName', title: '虚拟组织名称' },
  { key: 'orgType', title: '类型' },
  { key: 'leadOrgName', title: '牵头组织' },
  { key: 'leaderName', title: '负责人' },
  { key: 'memberCount', title: '成员数' },
  { key: 'effectiveDate', title: '生效日期' },
  { key: 'expireDate', title: '失效日期' },
  { key: 'orgStatus', title: '状态' }
]
const ioFieldMap = {
  '虚拟组织编码': 'virtualOrgCode',
  '虚拟组织名称': 'virtualOrgName',
  '类型': 'orgType',
  '组织类型': 'orgType',
  '牵头组织': 'leadOrgName',
  '牵头组织编码': 'leadOrgCode',
  '负责人': 'leaderName',
  '负责人编号': 'leaderId',
  '参与组织': 'participatingOrgs',
  '生效日期': 'effectiveDate',
  '失效日期': 'expireDate',
  '到期日期': 'expireDate',
  '工作目标': 'workObjective',
  '备注': 'remarks'
}

const loading = ref(false)

const searchForm = reactive({
  keyword: '',
  orgType: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增虚拟组织')
const formRef = ref(null)

const formData = reactive({
  id: null,
  virtualOrgCode: '',
  virtualOrgName: '',
  orgType: 'PROJECT',
  leaderId: '',
  leaderName: '',
  leadOrgId: null,
  leadOrgCode: '',
  leadOrgName: '',
  participatingOrgs: '',
  effectiveDate: '',
  expireDate: '',
  orgStatus: 'PREPARING',
  memberCount: 0,
  workObjective: '',
  remarks: ''
})

const detailData = reactive({})
const list = ref([])

const membersVisible = ref(false)
const membersLoading = ref(false)
const memberList = ref([])
const currentOrgId = ref(null)
const currentOrgName = ref('')
const addMemberVisible = ref(false)
const memberForm = reactive({ employeeId: '', employeeName: '', virtualRole: '' })

const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])
const historyName = ref('')

const expiringCount = computed(() => list.value.filter(o => o.orgStatus === 'EXPIRING').length)

function indexOffset(index) {
  return (pagination.currentPage - 1) * pagination.pageSize + index + 1
}

function remainingDays(row) {
  if (!row.expireDate) return Infinity
  const exp = new Date(String(row.expireDate).replace(' ', 'T'))
  if (isNaN(exp.getTime())) return Infinity
  return Math.ceil((exp.getTime() - Date.now()) / 86400000)
}

function remainingDaysText(row) {
  const d = remainingDays(row)
  if (d === Infinity) return '长期'
  if (d < 0) return '已失效'
  return `${d}天`
}

// 剩余有效期三级着色：已失效灰色、≤7天红色、≤30天黄色、其余默认
function remainingTagType(row) {
  const d = remainingDays(row)
  if (d === Infinity) return 'info'
  if (d < 0) return 'info'
  if (d <= 7) return 'danger'
  if (d <= 30) return 'warning'
  return 'success'
}

function viewArchitecture(row) {
  ElMessageBox.alert(
    `虚拟组织「${row.virtualOrgName}」架构视图：\n牵头组织：${row.leadOrgName || '—'}\n负责人：${row.leaderName || '—'}\n成员人数：${row.memberCount || 0}\n参与组织：${row.participatingOrgs || '—'}`,
    '架构查看',
    { confirmButtonText: '关闭', type: 'info' }
  )
}

function handleExpireRemind() {
  ElMessageBox.prompt('请设置到期提前提醒天数', '到期提醒设置', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: '30',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入正整数天数'
  }).then(({ value }) => {
    ElMessage.success(`已设置到期前 ${value} 天提醒`)
  }).catch(() => {})
}

async function openMembers(row) {
  currentOrgId.value = row.id
  currentOrgName.value = row.virtualOrgName
  membersVisible.value = true
  await loadMembers()
}

async function loadMembers() {
  membersLoading.value = true
  try {
    const res = await axios.get(`/api/virtual-org/${currentOrgId.value}/members`)
    if (res.data.code === 200) {
      memberList.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载成员失败')
    console.error(e)
  } finally {
    membersLoading.value = false
  }
}

async function submitAddMember() {
  if (!memberForm.employeeId) {
    ElMessage.warning('请输入员工编号')
    return
  }
  try {
    const res = await axios.post(`/api/virtual-org/${currentOrgId.value}/members`, { ...memberForm })
    if (res.data.code === 200) {
      ElMessage.success('添加成功')
      addMemberVisible.value = false
      memberForm.employeeId = ''
      memberForm.employeeName = ''
      memberForm.virtualRole = ''
      loadMembers()
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '添加失败')
    }
  } catch (e) {
    ElMessage.error('添加失败')
    console.error(e)
  }
}

function removeMember(row) {
  ElMessageBox.confirm(`确定要移除成员「${row.employeeName || row.employeeId}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/virtual-org/${currentOrgId.value}/members`, {
        data: { employeeId: row.employeeId }
      })
      if (res.data.code === 200) {
        ElMessage.success('移除成功')
        loadMembers()
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '移除失败')
      }
    } catch (e) {
      ElMessage.error('移除失败')
    }
  }).catch(() => {})
}

function handleExtend(row) {
  ElMessageBox.prompt('请输入新的到期日期（YYYY-MM-DD）', '续期申请', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /^\d{4}-\d{2}-\d{2}$/,
    inputErrorMessage: '日期格式不正确'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/virtual-org/${row.id}/extend`, { newExpireDate: value })
      if (res.data.code === 200) {
        ElMessage.success('续期成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '续期失败')
      }
    } catch (e) {
      ElMessage.error('续期失败')
    }
  }).catch(() => {})
}

function handleDisband(row) {
  ElMessageBox.prompt(`确定要解散「${row.virtualOrgName}」吗？请输入解散原因`, '解散申请', {
    confirmButtonText: '确定解散',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '解散原因不能为空',
    type: 'warning'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/virtual-org/${row.id}/disband`, { reason: value })
      if (res.data.code === 200) {
        ElMessage.success('已解散')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '解散失败')
      }
    } catch (e) {
      ElMessage.error('解散失败')
    }
  }).catch(() => {})
}

async function viewHistory(row) {
  historyName.value = row.virtualOrgName
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await axios.get(`/api/virtual-org/${row.id}/history`)
    if (res.data.code === 200) {
      historyList.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载变更历史失败')
    console.error(e)
  } finally {
    historyLoading.value = false
  }
}

function getOrgTypeLabel(type) {
  const map = {
    PROJECT: '项目型',
    SPECIAL: '专项任务型',
    TEMPORARY: '临时团队型'
  }
  return map[type] || type
}

function getStatusType(status) {
  const map = {
    PREPARING: 'info',
    ACTIVE: 'success',
    EXPIRING: 'warning',
    DISBAND: 'danger'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    PREPARING: '筹备中',
    ACTIVE: '生效',
    EXPIRING: '即将到期',
    DISBAND: '已解散'
  }
  return map[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/virtual-org/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        orgType: searchForm.orgType || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || res.data.data || []
      pagination.total = res.data.data.total || list.value.length
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.orgType = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadList()
}

function handleAdd() {
  dialogTitle.value = '新增虚拟组织'
  Object.assign(formData, {
    id: null,
    virtualOrgCode: '',
    virtualOrgName: '',
    orgType: 'PROJECT',
    leaderId: '',
    leaderName: '',
    leadOrgId: null,
    leadOrgCode: '',
    leadOrgName: '',
    participatingOrgs: '',
    effectiveDate: '',
    expireDate: '',
    orgStatus: 'PREPARING',
    memberCount: 0,
    workObjective: '',
    remarks: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑虚拟组织'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/virtual-org/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除吗？删除后不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/virtual-org/${row.id}`)
      // 后端业务失败时 code 仍为 200，失败详情在 data.success=false + data.message
      const biz = res.data.data
      if (res.data.code === 200 && biz && biz.success === false) {
        ElMessage.error(biz.message || res.data.message || '删除失败')
      } else if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/virtual-org/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/virtual-org/create', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增虚拟组织' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

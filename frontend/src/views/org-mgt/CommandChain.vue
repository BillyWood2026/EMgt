<template>
  <div class="page-container">
    <div class="page-header">
      <h2>指挥链</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="list"
          file-name="指挥链"
          title="指挥链列表"
          import-api="/api/org-mgt/command-chain"
          :field-map="ioFieldMap"
          @done="loadList"
        />
        <el-button type="primary" @click="handleAdd">新增指挥链</el-button>
      </div>
    </div>

    <div style="margin-bottom: 12px; display: flex; gap: 8px">
      <el-button size="small" type="success" :disabled="selectedRows.length === 0" @click="batchEnable">批量启用</el-button>
      <el-button size="small" type="warning" :disabled="selectedRows.length === 0" @click="batchPause">批量暂停</el-button>
      <el-button size="small" type="info" @click="handleValidate">链路校验</el-button>
      <el-button size="small" @click="handleSyncOrg">同步组织架构</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 180px" />
      </el-form-item>
      <el-form-item label="链类型">
        <el-select v-model="searchForm.chainType" placeholder="选择类型" style="width: 150px">
          <el-option label="固定链" value="FIXED" />
          <el-option label="专项链" value="SPECIAL" />
          <el-option label="授权链" value="AUTHORIZED" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="生效" value="ACTIVE" />
          <el-option label="暂停" value="PAUSED" />
          <el-option label="到期失效" value="EXPIRED" />
          <el-option label="作废" value="ABANDONED" />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型">
        <el-input v-model="searchForm.business" placeholder="请输入业务" style="width: 150px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="chainCode" label="链编码" width="140" />
      <el-table-column prop="chainName" label="链名称" width="160" />
      <el-table-column prop="chainType" label="类型" width="100">
        <template #default="{ row }">
          {{ getChainTypeLabel(row.chainType) }}
        </template>
      </el-table-column>
      <el-table-column prop="applicableOrgScope" label="适用组织范围" width="130" show-overflow-tooltip />
      <el-table-column prop="applicableBusiness" label="适用业务" width="120" />
      <el-table-column prop="startPosition" label="起始职位" width="120" />
      <el-table-column prop="endPosition" label="结束职位" width="120" />
      <el-table-column prop="levelCount" label="层级数" width="80" />
      <el-table-column label="越级权限" width="100">
        <template #default="{ row }">
          <el-tag :type="row.allowSkipLevel ? 'danger' : 'success'" size="small">
            {{ row.allowSkipLevel ? '允许越级' : '禁止越级' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="chainStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.chainStatus)" size="small">
            {{ getStatusLabel(row.chainStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveStartTime" label="生效开始" width="160" />
      <el-table-column prop="effectiveEndTime" label="生效结束" width="160" />
      <el-table-column label="剩余有效期" width="110">
        <template #default="{ row }">
          <el-tag :type="remainingTagType(row)" size="small" effect="plain">
            {{ remainingText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="100" />
      <el-table-column label="操作" width="430" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" plain @click="viewVisual(row)">可视化预览</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.chainStatus === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.chainStatus === 'ACTIVE' ? '暂停' : '启用' }}
          </el-button>
          <el-button size="small" type="info" @click="viewHistory(row)">变更历史</el-button>
          <el-button size="small" type="warning" plain @click="handleExtend(row)">到期续期</el-button>
          <el-button size="small" type="danger" plain v-if="row.chainStatus !== 'ABANDONED'" @click="handleAbandon(row)">作废</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
            <el-form-item label="链编码" required>
              <el-input v-model="formData.chainCode" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="链名称" required>
              <el-input v-model="formData.chainName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="链类型">
              <el-select v-model="formData.chainType" style="width: 100%">
                <el-option label="固定链" value="FIXED" />
                <el-option label="专项链" value="SPECIAL" />
                <el-option label="授权链" value="AUTHORIZED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用业务">
              <el-input v-model="formData.applicableBusiness" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起始职位">
              <el-input v-model="formData.startPosition" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束职位">
              <el-input v-model="formData.endPosition" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效开始">
              <el-date-picker
                v-model="formData.effectiveStartTime"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效结束">
              <el-date-picker
                v-model="formData.effectiveEndTime"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="允许越级">
              <el-switch v-model="formData.allowSkipLevel" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许抄送">
              <el-switch v-model="formData.allowCrossCopy" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="强制流转">
              <el-switch v-model="formData.forceChainFlow" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="超时预警(小时)">
              <el-input-number v-model="formData.timeoutWarningHours" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="驳回规则">
              <el-select v-model="formData.rejectReturnRule" style="width: 100%">
              <el-option label="退回起点" value="RETURN_TO_START" />
              <el-option label="退回上一步" value="RETURN_TO_PREVIOUS" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用组织范围">
          <el-input v-model="formData.applicableOrgScope" />
        </el-form-item>
        <el-form-item label="授权依据">
          <el-input v-model="formData.authorizationBasis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="链描述">
          <el-input v-model="formData.chainDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="指挥链详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="链编码">{{ detailData.chainCode }}</el-descriptions-item>
        <el-descriptions-item label="链名称">{{ detailData.chainName }}</el-descriptions-item>
        <el-descriptions-item label="链类型">{{ getChainTypeLabel(detailData.chainType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.chainStatus)" size="small">
            {{ getStatusLabel(detailData.chainStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="适用业务">{{ detailData.applicableBusiness }}</el-descriptions-item>
        <el-descriptions-item label="层级数">{{ detailData.levelCount }}</el-descriptions-item>
        <el-descriptions-item label="起始职位">{{ detailData.startPosition }}</el-descriptions-item>
        <el-descriptions-item label="结束职位">{{ detailData.endPosition }}</el-descriptions-item>
        <el-descriptions-item label="生效开始">{{ detailData.effectiveStartTime }}</el-descriptions-item>
        <el-descriptions-item label="生效结束">{{ detailData.effectiveEndTime }}</el-descriptions-item>
        <el-descriptions-item label="允许越级">{{ detailData.allowSkipLevel ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="允许抄送">{{ detailData.allowCrossCopy ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="链描述" :span="2">{{ detailData.chainDescription }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="historyVisible" title="变更历史" width="800px">
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="changeType" label="变更类型" width="120" />
        <el-table-column prop="changeContent" label="变更内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="operateTime" label="操作时间" width="170" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无变更历史" />
    </el-dialog>

    <el-dialog v-model="extendVisible" title="到期续期" width="500px">
      <el-form label-width="120px">
        <el-form-item label="链名称">
          <span>{{ extendRow.chainName }}</span>
        </el-form-item>
        <el-form-item label="当前生效结束">
          <span>{{ extendRow.effectiveEndTime }}</span>
        </el-form-item>
        <el-form-item label="新生效结束" required>
          <el-date-picker
            v-model="extendForm.newEndTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="续期说明">
          <el-input v-model="extendForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExtend">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="visualVisible" :title="`链路可视化 - ${visualRow.chainName || ''}`" width="520px">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="链路类型">{{ getChainTypeLabel(visualRow.chainType) }}</el-descriptions-item>
        <el-descriptions-item label="适用业务">{{ visualRow.applicableBusiness }}</el-descriptions-item>
        <el-descriptions-item label="层级数量">{{ visualRow.levelCount }}</el-descriptions-item>
        <el-descriptions-item label="越级权限">
          <el-tag :type="visualRow.allowSkipLevel ? 'danger' : 'success'" size="small">
            {{ visualRow.allowSkipLevel ? '允许越级' : '禁止越级' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="chain-viz">
        <el-steps direction="vertical" :active="visualSteps.length" finish-status="success">
          <el-step
            v-for="(node, idx) in visualSteps"
            :key="idx"
            :title="node.title"
            :description="node.desc"
          />
        </el-steps>
      </div>
      <el-empty v-if="visualSteps.length === 0" description="暂无链路节点" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'chainCode', title: '链编码' },
  { key: 'chainName', title: '链名称' },
  { key: 'chainType', title: '类型' },
  { key: 'applicableOrgScope', title: '适用组织范围' },
  { key: 'applicableBusiness', title: '适用业务' },
  { key: 'startPosition', title: '起始职位' },
  { key: 'endPosition', title: '结束职位' },
  { key: 'levelCount', title: '层级数' },
  { key: 'allowSkipLevel', title: '越级权限' },
  { key: 'effectiveStartTime', title: '生效开始' },
  { key: 'effectiveEndTime', title: '生效结束' },
  { key: 'chainStatus', title: '状态' }
]
const ioFieldMap = {
  '链编码': 'chainCode',
  '链名称': 'chainName',
  '类型': 'chainType',
  '适用组织范围': 'applicableOrgScope',
  '适用业务': 'applicableBusiness',
  '起始职位': 'startPosition',
  '结束职位': 'endPosition',
  '层级数': 'levelCount',
  '越级权限': 'allowSkipLevel',
  '生效开始': 'effectiveStartTime',
  '生效结束': 'effectiveEndTime'
}

const loading = ref(false)
const selectedRows = ref([])
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])
const extendVisible = ref(false)
const extendRow = ref({})
const extendForm = reactive({ newEndTime: '', remark: '' })

const visualVisible = ref(false)
const visualRow = ref({})
const visualSteps = ref([])

const searchForm = reactive({
  keyword: '',
  chainType: '',
  status: '',
  business: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增指挥链')
const formRef = ref(null)

const formData = reactive({
  id: null,
  chainCode: '',
  chainName: '',
  chainType: 'FIXED',
  applicableOrgScope: '',
  applicableBusiness: '',
  startPosition: '',
  endPosition: '',
  levelCount: 0,
  effectiveStartTime: '',
  effectiveEndTime: '',
  chainStatus: 'DRAFT',
  allowSkipLevel: false,
  allowCrossCopy: false,
  timeoutWarningHours: 24,
  rejectReturnRule: 'RETURN_TO_PREVIOUS',
  forceChainFlow: false,
  relatedVirtualOrgId: null,
  authorizationBasis: '',
  chainDescription: '',
  approvalNo: '',
  remark: ''
})

const detailData = reactive({})
const list = ref([])

function getChainTypeLabel(type) {
  const map = {
    FIXED: '固定链',
    SPECIAL: '专项链',
    AUTHORIZED: '授权链'
  }
  return map[type] || type
}

function getStatusType(status) {
  const map = {
    DRAFT: 'info',
    ACTIVE: 'success',
    PAUSED: 'warning',
    EXPIRED: 'info',
    ABANDONED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    DRAFT: '草稿',
    ACTIVE: '生效',
    PAUSED: '暂停',
    EXPIRED: '到期失效',
    ABANDONED: '作废'
  }
  return map[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/org-mgt/command-chain/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        chainType: searchForm.chainType || undefined,
        status: searchForm.status || undefined,
        business: searchForm.business || undefined
      }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || res.data.data
      pagination.total = res.data.data.total || (res.data.data || []).length
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
  searchForm.chainType = ''
  searchForm.status = ''
  searchForm.business = ''
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
  dialogTitle.value = '新增指挥链'
  Object.assign(formData, {
    id: null,
    chainCode: '',
    chainName: '',
    chainType: 'FIXED',
    applicableOrgScope: '',
    applicableBusiness: '',
    startPosition: '',
    endPosition: '',
    levelCount: 0,
    effectiveStartTime: '',
    effectiveEndTime: '',
    chainStatus: 'DRAFT',
    allowSkipLevel: false,
    allowCrossCopy: false,
    timeoutWarningHours: 24,
    rejectReturnRule: 'RETURN_TO_PREVIOUS',
    forceChainFlow: false,
    relatedVirtualOrgId: null,
    authorizationBasis: '',
    chainDescription: '',
    approvalNo: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑指挥链'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/org-mgt/command-chain/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function toggleStatus(row) {
  const newStatus = row.chainStatus === 'ACTIVE' ? 'PAUSED' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '启用' : '暂停'
  ElMessageBox.confirm(`确定要${action}该指挥链吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-mgt/command-chain/${row.id}/toggle-status`, {
        newStatus: newStatus
      })
      if (res.data.code === 200) {
        ElMessage.success(`${action}成功`)
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
      }
    } catch (e) {
      ElMessage.error(`${action}失败`)
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除吗？删除后不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/org-mgt/command-chain/${row.id}`)
      if (res.data.code === 200) {
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
      res = await axios.put(`/api/org-mgt/command-chain/${formData.id}`, {
        chain: formData,
        nodes: []
      })
    } else {
      res = await axios.post('/api/org-mgt/command-chain', {
        chain: formData,
        nodes: []
      })
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增指挥链' ? '新增成功' : '修改成功')
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

function remainingDays(row) {
  if (!row.effectiveEndTime) return null
  const end = new Date(String(row.effectiveEndTime).replace(' ', 'T'))
  if (isNaN(end.getTime())) return null
  return Math.floor((end.getTime() - Date.now()) / 86400000)
}

function remainingText(row) {
  const d = remainingDays(row)
  if (d === null) return '长期有效'
  if (d < 0) return '已失效'
  return `剩余${d}天`
}

// 剩余有效期三级着色：已失效灰、≤7天红、≤30天黄、其余绿
function remainingTagType(row) {
  const d = remainingDays(row)
  if (d === null) return 'info'
  if (d < 0) return 'info'
  if (d <= 7) return 'danger'
  if (d <= 30) return 'warning'
  return 'success'
}

// 指挥链可视化：基于起始/终止节点与层级数构建纵向节点流程图
function viewVisual(row) {
  visualRow.value = row
  const steps = []
  const start = row.startPosition || '起始节点'
  const end = row.endPosition || '终止节点'
  const count = Math.max(Number(row.levelCount) || 2, 2)
  steps.push({ title: start, desc: '链路起始节点' })
  for (let i = 1; i < count - 1; i++) {
    steps.push({ title: `中间节点 ${i}`, desc: `审批/指挥节点` })
  }
  if (count >= 2) {
    steps.push({ title: end, desc: '链路终止节点' })
  }
  visualSteps.value = steps
  visualVisible.value = true
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function batchAction(action, url) {
  const ids = selectedRows.value.map((r) => r.id)
  try {
    const res = await axios.post(url, { ids })
    if (res.data.code === 200) {
      ElMessage.success(`${action}成功`)
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
    }
  } catch (e) {
    ElMessage.error(`${action}失败`)
  }
}

function batchEnable() {
  ElMessageBox.confirm(`确定要批量启用选中的 ${selectedRows.value.length} 条指挥链吗？`, '提示', {
    type: 'warning'
  }).then(() => batchAction('批量启用', '/api/org-mgt/command-chain/batch-enable')).catch(() => {})
}

function batchPause() {
  ElMessageBox.confirm(`确定要批量暂停选中的 ${selectedRows.value.length} 条指挥链吗？`, '提示', {
    type: 'warning'
  }).then(() => batchAction('批量暂停', '/api/org-mgt/command-chain/batch-pause')).catch(() => {})
}

async function handleValidate() {
  try {
    const res = await axios.post('/api/org-mgt/command-chain/validate')
    if (res.data.code === 200) {
      const data = res.data.data
      if (typeof data === 'string') {
        ElMessage.success(data)
      } else if (Array.isArray(data) && data.length > 0) {
        ElMessage.warning(`校验发现 ${data.length} 项问题，请检查`)
      } else {
        ElMessage.success('链路校验通过')
      }
    } else {
      ElMessage.error(res.data.message || res.data.msg || '校验失败')
    }
  } catch (e) {
    ElMessage.error('链路校验失败')
  }
}

function handleSyncOrg() {
  ElMessageBox.confirm('同步组织架构将按最新架构刷新链路节点，确定继续吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post('/api/org-mgt/command-chain/sync-org')
      if (res.data.code === 200) {
        ElMessage.success('同步成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '同步失败')
      }
    } catch (e) {
      ElMessage.error('同步失败')
    }
  }).catch(() => {})
}

async function viewHistory(row) {
  historyVisible.value = true
  historyLoading.value = true
  historyList.value = []
  try {
    const res = await axios.get(`/api/org-mgt/command-chain/${row.id}/history`)
    if (res.data.code === 200) {
      historyList.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('获取变更历史失败')
  } finally {
    historyLoading.value = false
  }
}

function handleExtend(row) {
  extendRow.value = row
  extendForm.newEndTime = ''
  extendForm.remark = ''
  extendVisible.value = true
}

async function submitExtend() {
  if (!extendForm.newEndTime) {
    ElMessage.warning('请选择新生效结束时间')
    return
  }
  try {
    const res = await axios.post(`/api/org-mgt/command-chain/${extendRow.value.id}/extend`, {
      newEndTime: extendForm.newEndTime,
      remark: extendForm.remark
    })
    if (res.data.code === 200) {
      ElMessage.success('续期成功')
      extendVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '续期失败')
    }
  } catch (e) {
    ElMessage.error('续期失败')
  }
}

function handleAbandon(row) {
  ElMessageBox.confirm('确定要作废该指挥链吗？作废后不可恢复。', '作废确认', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-mgt/command-chain/${row.id}/abandon`)
      if (res.data.code === 200) {
        ElMessage.success('作废成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '作废失败')
      }
    } catch (e) {
      ElMessage.error('作废失败')
    }
  }).catch(() => {})
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

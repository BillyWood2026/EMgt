<template>
  <div class="page-container">
    <div class="page-header">
      <h2>五级组织架构管理</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="exportColumns"
          :rows="orgList"
          file-name="五级组织架构"
          title="组织架构列表"
          import-api="/api/org-unit/batch-import"
          :field-map="importFieldMap"
          @done="loadOrgList(); loadTree()"
        />
        <el-button type="primary" @click="handleAdd" style="margin-left: 8px">新增组织</el-button>
        <el-button @click="loadTree" plain>刷新</el-button>
      </div>
    </div>

    <el-alert
      v-if="rigidRuleWarning"
      :title="rigidRuleWarning"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 12px"
    />

    <div class="main-content">
      <div class="left-panel">
        <div class="panel-header">
          <span>五级组织树</span>
          <el-button size="small" @click="loadTree">刷新</el-button>
        </div>
        <el-input
          v-model="treeFilterText"
          placeholder="搜索组织名称/编码"
          clearable
          size="small"
          style="margin-bottom: 10px"
        />
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ label: 'label', children: 'children' }"
          :filter-node-method="filterTreeNode"
          node-key="id"
          @node-click="handleNodeClick"
          highlight-current
          default-expand-all
        />
      </div>

      <div class="right-panel">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="组织名称/编码" style="width: 180px" />
          </el-form-item>
          <el-form-item label="组织层级">
            <el-select v-model="searchForm.orgLevel" placeholder="选择层级" style="width: 150px" clearable>
              <el-option label="一级（公司）" :value="1" />
              <el-option label="二级（中心）" :value="2" />
              <el-option label="三级（部门）" :value="3" />
              <el-option label="四级（科室）" :value="4" />
              <el-option label="五级（小组）" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px" clearable>
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="核心组织">
            <el-select v-model="searchForm.isCoreOrg" placeholder="全部" style="width: 100px" clearable>
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="pagedList" border stripe size="small" v-loading="loading">
          <el-table-column type="index" label="序号" width="60" :index="indexOffset" />
          <el-table-column prop="orgCode" label="组织编码" width="140" />
          <el-table-column prop="orgName" label="组织名称" width="160" show-overflow-tooltip />
          <el-table-column prop="orgLevel" label="层级" width="100">
            <template #default="{ row }">
              <el-tag :type="levelTagType(row.orgLevel)" size="small">{{ getLevelLabel(row.orgLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="parentOrgCode" label="上级组织" width="120" />
          <el-table-column prop="orgManager" label="负责人" width="100" />
          <el-table-column prop="orgStatus" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getOrgStatusType(row.orgStatus)" size="small">
                {{ getOrgStatusLabel(row.orgStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isCoreOrg" label="核心组织" width="80">
            <template #default="{ row }">
              <el-tag v-if="toBool(row.isCoreOrg)" type="danger" size="small">是</el-tag>
              <span v-else>否</span>
            </template>
          </el-table-column>
          <el-table-column prop="isIndependentAccounting" label="独立核算" width="90">
            <template #default="{ row }">
              {{ toBool(row.isIndependentAccounting) ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column prop="isStaffingControlled" label="定编管控" width="90">
            <template #default="{ row }">
              {{ toBool(row.isStaffingControlled) ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column prop="establishDate" label="成立日期" width="110" />
          <el-table-column prop="updatedTime" label="更新时间" width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="440" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button
                size="small"
                type="success"
                :disabled="row.orgLevel >= 5"
                @click="handleAddChild(row)"
              >新增下级</el-button>
              <el-button size="small" type="warning" plain @click="handleMove(row)">迁移</el-button>
              <el-button size="small" type="info" plain @click="viewChangeHistory(row)">变更历史</el-button>
              <el-button
                size="small"
                :type="row.orgStatus === 'ENABLED' ? 'warning' : 'success'"
                plain
                @click="toggleStatus(row)"
              >{{ row.orgStatus === 'ENABLED' ? '停用' : '启用' }}</el-button>
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
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织编码" required>
              <el-input v-model="formData.orgCode" placeholder="自动生成，可修改">
                <template #append>
                  <el-button @click="genOrgCode">生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织名称" required>
              <el-input v-model="formData.orgName" placeholder="请输入组织名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织层级" required>
              <el-select v-model="formData.orgLevel" placeholder="选择层级" style="width: 100%" @change="onLevelChange">
                <el-option label="一级（公司）" :value="1" :disabled="companyExists && !formData.id" />
                <el-option label="二级（中心）" :value="2" />
                <el-option label="三级（部门）" :value="3" />
                <el-option label="四级（科室）" :value="4" />
                <el-option label="五级（小组）" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上级组织" required>
              <el-select
                v-model="formData.parentOrgId"
                placeholder="选择上级组织"
                style="width: 100%"
                filterable
                :disabled="formData.orgLevel === 1"
              >
                <el-option
                  v-for="o in parentCandidates"
                  :key="o.id"
                  :label="`${o.orgName}（${o.orgCode}）`"
                  :value="o.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织状态">
              <el-select v-model="formData.orgStatus" style="width: 100%">
                <el-option label="启用" value="ENABLED" />
                <el-option label="停用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期">
              <el-date-picker
                v-model="formData.establishDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织负责人">
              <el-input v-model="formData.orgManager" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属业务线">
              <el-input v-model="formData.businessLine" placeholder="请输入业务线" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="属地">
              <el-input v-model="formData.location" placeholder="请输入属地" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序序号">
              <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否核心组织">
              <el-switch v-model="formData.isCoreOrg" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否独立核算">
              <el-switch v-model="formData.isIndependentAccounting" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否定编管控">
              <el-switch v-model="formData.isStaffingControlled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="组织详情" width="760px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="组织编码">{{ detailData.orgCode }}</el-descriptions-item>
        <el-descriptions-item label="组织名称">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="组织层级">{{ getLevelLabel(detailData.orgLevel) }}</el-descriptions-item>
        <el-descriptions-item label="上级编码">{{ detailData.parentOrgCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getOrgStatusType(detailData.orgStatus)" size="small">
            {{ getOrgStatusLabel(detailData.orgStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成立日期">{{ detailData.establishDate }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detailData.orgManager }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="业务线">{{ detailData.businessLine }}</el-descriptions-item>
        <el-descriptions-item label="属地">{{ detailData.location }}</el-descriptions-item>
        <el-descriptions-item label="排序序号">{{ detailData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="核心组织">{{ toBool(detailData.isCoreOrg) ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="独立核算">{{ toBool(detailData.isIndependentAccounting) ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="定编管控">{{ toBool(detailData.isStaffingControlled) ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 迁移弹窗 -->
    <el-dialog v-model="moveVisible" title="组织层级迁移" width="500px">
      <el-form label-width="110px">
        <el-form-item label="当前组织">
          <span>{{ moveRow.orgName }}（{{ getLevelLabel(moveRow.orgLevel) }}）</span>
        </el-form-item>
        <el-form-item label="新上级组织" required>
          <el-select v-model="moveTarget" placeholder="选择新的上级组织" style="width: 100%" filterable>
            <el-option
              v-for="o in moveCandidates"
              :key="o.id"
              :label="`${o.orgName}（${getLevelLabel(o.orgLevel)}）`"
              :value="o.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="迁移原因">
          <el-input v-model="moveReason" type="textarea" :rows="3" placeholder="请输入迁移原因（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMove">确定迁移</el-button>
      </template>
    </el-dialog>

    <!-- 变更历史弹窗 -->
    <el-dialog v-model="historyVisible" :title="`变更历史 - ${historyOrgName}`" width="900px">
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column prop="changeNo" label="变更编号" width="160" />
        <el-table-column prop="changeTitle" label="变更标题" width="160" show-overflow-tooltip />
        <el-table-column prop="changeType" label="变更类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ changeTypeLabel(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="approvalTime" label="审批时间" width="160" show-overflow-tooltip />
        <el-table-column prop="approvalStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="approvalStatusType(row.approvalStatus)" size="small">
              {{ approvalStatusLabel(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无变更记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const treeData = ref([])
const treeFilterText = ref('')
const treeRef = ref(null)

const searchForm = reactive({
  keyword: '',
  orgLevel: null,
  status: '',
  parentOrgId: null,
  isCoreOrg: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增组织')
const formRef = ref(null)

const formData = reactive({
  id: null,
  orgCode: '',
  orgName: '',
  orgLevel: null,
  parentOrgId: null,
  parentOrgCode: '',
  orgStatus: 'ENABLED',
  establishDate: '',
  orgManager: '',
  contactPhone: '',
  businessLine: '',
  location: '',
  sortOrder: 0,
  remark: '',
  isCoreOrg: false,
  isIndependentAccounting: false,
  isStaffingControlled: false
})

const detailData = reactive({})
const orgList = ref([])
const allOrgs = ref([])

const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])
const historyOrgName = ref('')

const moveVisible = ref(false)
const moveRow = ref({})
const moveTarget = ref(null)
const moveReason = ref('')

const companyExists = computed(() => allOrgs.value.some(o => o.orgLevel === 1))

// 五级刚性规则：禁止跨层级创建上级候选
const parentCandidates = computed(() => {
  if (!formData.orgLevel || formData.orgLevel === 1) return []
  const targetParentLevel = formData.orgLevel - 1
  return allOrgs.value.filter(o => o.orgLevel === targetParentLevel && o.id !== formData.id)
})

// 迁移候选：新上级层级必须 = 当前层级 - 1（不跨层级）
const moveCandidates = computed(() => {
  if (!moveRow.value.orgLevel) return []
  if (moveRow.value.orgLevel === 1) return []
  const targetLevel = moveRow.value.orgLevel - 1
  return allOrgs.value.filter(o => o.orgLevel === targetLevel && o.id !== moveRow.value.id)
})

const rigidRuleWarning = computed(() => {
  if (formData.orgLevel === 1 && companyExists.value && !formData.id) {
    return '刚性规则：公司级（一级）为唯一顶级节点，已存在公司级组织，禁止新增顶级节点。'
  }
  if (formData.orgLevel && formData.orgLevel > 1 && !formData.parentOrgId) {
    return '刚性规则：禁止跨层级创建，必须选择对应层级的上级组织。'
  }
  return ''
})

const pagedList = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return orgList.value.slice(start, start + pagination.pageSize)
})

const exportColumns = [
  { key: 'orgCode', title: '组织编码' },
  { key: 'orgName', title: '组织名称' },
  { key: 'orgLevel', title: '层级' },
  { key: 'parentOrgCode', title: '上级组织' },
  { key: 'orgManager', title: '负责人' },
  { key: 'contactPhone', title: '联系电话' },
  { key: 'orgStatus', title: '状态' },
  { key: 'isCoreOrg', title: '核心组织' },
  { key: 'isIndependentAccounting', title: '独立核算' },
  { key: 'isStaffingControlled', title: '定编管控' },
  { key: 'establishDate', title: '成立日期' },
  { key: 'sortOrder', title: '排序序号' },
  { key: 'updatedTime', title: '更新时间' }
]

const importFieldMap = {
  '组织编码': 'orgCode',
  '组织名称': 'orgName',
  '层级': 'orgLevel',
  '上级组织': 'parentOrgCode',
  '负责人': 'orgManager',
  '联系电话': 'contactPhone',
  '业务线': 'businessLine',
  '属地': 'location',
  '成立日期': 'establishDate',
  '排序序号': 'sortOrder',
  '核心组织': 'isCoreOrg',
  '独立核算': 'isIndependentAccounting',
  '定编管控': 'isStaffingControlled',
  '备注': 'remark'
}

function toBool(v) {
  return v === true || v === 1 || v === '1'
}

function indexOffset(index) {
  return (pagination.currentPage - 1) * pagination.pageSize + index + 1
}

function changeTypeLabel(type) {
  const map = { CREATE: '新增', UPDATE: '调整', MERGE: '合并', SPLIT: '拆分', TRANSFER: '迁移', DISABLE: '停用', DELETE: '删除', MIGRATE: '层级变更' }
  return map[type] || type
}

function getLevelLabel(level) {
  const map = { 1: '一级（公司）', 2: '二级（中心）', 3: '三级（部门）', 4: '四级（科室）', 5: '五级（小组）' }
  return map[level] || level
}

function levelTagType(level) {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'success', 5: 'info' }
  return map[level] || 'info'
}

function getOrgStatusType(status) {
  const map = { ENABLED: 'success', DISABLED: 'danger', PENDING: 'warning' }
  return map[status] || 'info'
}

function getOrgStatusLabel(status) {
  const map = { ENABLED: '启用', DISABLED: '停用', PENDING: '待审批' }
  return map[status] || status
}

function approvalStatusType(s) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', DRAFT: 'info', CANCELLED: 'info' }
  return map[s] || 'info'
}

function approvalStatusLabel(s) {
  const map = { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已驳回', DRAFT: '草稿', CANCELLED: '已撤销' }
  return map[s] || s
}

function filterTreeNode(value, data) {
  if (!value) return true
  return (data.label || '').includes(value) || (data.orgCode || '').includes(value)
}

watch(treeFilterText, (val) => {
  treeRef.value?.filter(val)
})

async function loadTree() {
  try {
    const res = await axios.get('/api/org-unit/tree')
    if (res.data.code === 200) treeData.value = res.data.data || []
  } catch (e) {
    ElMessage.error('加载组织树失败')
  }
}

async function loadOrgList() {
  loading.value = true
  try {
    const res = await axios.get('/api/org-unit/list', {
      params: {
        page: 1, pageSize: 1000,
        keyword: searchForm.keyword || undefined,
        orgLevel: searchForm.orgLevel || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      let data = res.data.data.list || res.data.data || []
      if (searchForm.isCoreOrg !== null && searchForm.isCoreOrg !== '') {
        data = data.filter(o => toBool(o.isCoreOrg) === toBool(searchForm.isCoreOrg))
      }
      orgList.value = data
      allOrgs.value = data
      pagination.total = data.length
    }
  } catch (e) {
    ElMessage.error('加载组织列表失败')
  } finally {
    loading.value = false
  }
}

function handleNodeClick(data) {
  searchForm.parentOrgId = data.id
  pagination.currentPage = 1
  loadOrgList()
}

function handleSearch() {
  pagination.currentPage = 1
  loadOrgList()
}

function resetSearch() {
  Object.assign(searchForm, { keyword: '', orgLevel: null, status: '', parentOrgId: null, isCoreOrg: null })
  pagination.currentPage = 1
  loadOrgList()
}

function handleSizeChange(val) { pagination.pageSize = val; pagination.currentPage = 1 }
function handleCurrentChange(val) { pagination.currentPage = val }

function onLevelChange() {
  formData.parentOrgId = null
  formData.parentOrgCode = ''
}

async function genOrgCode() {
  try {
    const res = await axios.get('/api/org-unit/generate-code', { params: { parentId: formData.parentOrgId || undefined } })
    if (res.data.code === 200) formData.orgCode = res.data.data
  } catch (e) {
    ElMessage.error('生成编码失败')
  }
}

function handleAdd() {
  dialogTitle.value = '新增组织'
  Object.assign(formData, {
    id: null, orgCode: '', orgName: '', orgLevel: null,
    parentOrgId: searchForm.parentOrgId, parentOrgCode: '',
    orgStatus: 'ENABLED', establishDate: '', orgManager: '', contactPhone: '',
    businessLine: '', location: '', sortOrder: 0, remark: '',
    isCoreOrg: false, isIndependentAccounting: false, isStaffingControlled: false
  })
  dialogVisible.value = true
}

function handleAddChild(row) {
  handleAdd()
  formData.parentOrgId = row.id
  formData.parentOrgCode = row.orgCode
  formData.orgLevel = Math.min((row.orgLevel || 0) + 1, 5)
  ElMessage.info(`已锁定上级为「${row.orgName}」，层级自动设为${getLevelLabel(formData.orgLevel)}`)
}

function handleEdit(row) {
  dialogTitle.value = '编辑组织'
  Object.assign(formData, {
    id: row.id, orgCode: row.orgCode, orgName: row.orgName, orgLevel: row.orgLevel,
    parentOrgId: row.parentOrgId, parentOrgCode: row.parentOrgCode,
    orgStatus: row.orgStatus, establishDate: row.establishDate, orgManager: row.orgManager,
    contactPhone: row.contactPhone, businessLine: row.businessLine, location: row.location,
    sortOrder: row.sortOrder, remark: row.remark,
    isCoreOrg: toBool(row.isCoreOrg), isIndependentAccounting: toBool(row.isIndependentAccounting),
    isStaffingControlled: toBool(row.isStaffingControlled)
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/org-unit/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取组织详情失败')
  }
}

function toggleStatus(row) {
  const newStatus = row.orgStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = newStatus === 'ENABLED' ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}该组织吗？状态变更将触发变更审批流程。`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-unit/${row.id}/status`, null, { params: { status: newStatus } })
      if (res.data.code === 200) {
        ElMessage.success(`${action}成功`)
        loadOrgList(); loadTree()
      } else {
        ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
      }
    } catch (e) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

function handleMove(row) {
  if (row.orgLevel === 1) {
    ElMessage.warning('公司级顶级节点不可迁移')
    return
  }
  moveRow.value = row
  moveTarget.value = null
  moveReason.value = ''
  moveVisible.value = true
}

function submitMove() {
  if (!moveTarget.value) {
    ElMessage.warning('请选择新的上级组织')
    return
  }
  ElMessageBox.confirm('迁移操作将触发统一流程引擎审批，确定继续吗？', '迁移确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-unit/${moveRow.value.id}/move`, null, {
        params: { newParentId: moveTarget.value }
      })
      if (res.data.code === 200) {
        ElMessage.success('迁移申请已提交')
        moveVisible.value = false
        loadOrgList(); loadTree()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '迁移失败')
      }
    } catch (e) {
      ElMessage.error('迁移失败')
    }
  }).catch(() => {})
}

async function viewChangeHistory(row) {
  historyOrgName.value = row.orgName
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await axios.get('/api/org-change/history/list', {
      params: { page: 1, pageSize: 100, orgCode: row.orgCode }
    })
    if (res.data.code === 200) {
      const data = res.data.data
      const list = data.list || data || []
      historyList.value = list
    }
  } catch (e) {
    ElMessage.error('加载变更历史失败')
  } finally {
    historyLoading.value = false
  }
}

async function handleSubmit() {
  // 刚性规则校验
  if (formData.orgLevel === 1 && companyExists.value && !formData.id) {
    ElMessage.error('刚性规则：公司级为唯一顶级节点，已存在公司级组织，禁止新增。')
    return
  }
  if (formData.orgLevel && formData.orgLevel > 1 && !formData.parentOrgId) {
    ElMessage.error('刚性规则：禁止跨层级创建，请选择合法的上级组织。')
    return
  }
  if (formData.orgLevel && formData.orgLevel > 1) {
    const parent = allOrgs.value.find(o => o.id === formData.parentOrgId)
    if (parent && parent.orgLevel !== formData.orgLevel - 1) {
      ElMessage.error(`刚性规则：上级层级必须为${getLevelLabel(formData.orgLevel - 1)}，禁止跨层级创建。`)
      return
    }
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/org-unit/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/org-unit', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增组织' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadOrgList(); loadTree()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadTree()
  loadOrgList()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-actions { display: flex; align-items: center; }
.main-content { display: flex; gap: 20px; }
.left-panel { width: 280px; flex-shrink: 0; border: 1px solid #e4e7ed; border-radius: 4px; padding: 10px; background: #fff; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-weight: bold; }
.right-panel { flex: 1; min-width: 0; }
.search-form { margin-bottom: 20px; }
</style>

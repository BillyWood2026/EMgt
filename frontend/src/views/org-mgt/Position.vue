<template>
  <div class="page-container">
    <div class="page-header">
      <h2>岗位管理</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="positionList"
          file-name="岗位设置"
          title="岗位设置"
          import-api="/api/position"
          :field-map="ioFieldMap"
          @done="loadPositionList"
        />
        <el-button type="primary" @click="handleAdd">新增岗位</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="岗位名称/编码" style="width: 160px" />
      </el-form-item>
      <el-form-item label="组织编码">
        <el-input v-model="searchForm.orgCode" placeholder="请输入组织编码" style="width: 150px" />
      </el-form-item>
      <el-form-item label="岗位层级">
        <el-select v-model="searchForm.positionLevel" placeholder="选择层级" style="width: 150px">
          <el-option label="一级" value="1" />
          <el-option label="二级" value="2" />
          <el-option label="三级" value="3" />
          <el-option label="四级" value="4" />
          <el-option label="五级" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.positionStatus" placeholder="选择状态" style="width: 120px">
          <el-option label="启用" value="ENABLED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="pagedPositionList" border stripe size="small" v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="positionCode" label="岗位编码" width="140" />
      <el-table-column prop="positionName" label="岗位名称" width="160" />
      <el-table-column prop="orgCode" label="组织编码" width="120" />
      <el-table-column prop="orgName" label="所属组织" width="140" />
      <el-table-column prop="positionLevel" label="层级" width="80" />
      <el-table-column prop="positionRank" label="职级" width="100" />
      <el-table-column prop="positionCategory" label="岗位类别" width="100">
        <template #default="{ row }">
          {{ getCategoryLabel(row.positionCategory) }}
        </template>
      </el-table-column>
      <el-table-column prop="staffingType" label="编制类型" width="100">
        <template #default="{ row }">
          {{ row.staffingType === 'FIXED' ? '固定编制' : '弹性编制' }}
        </template>
      </el-table-column>
      <el-table-column prop="staffingCount" label="定编人数" width="90" />
      <el-table-column prop="currentCount" label="在岗人数" width="90" />
      <el-table-column label="空缺人数" width="90">
        <template #default="{ row }">
          <span :style="{ color: vacancyCount(row) > 0 ? '#e6a23c' : 'inherit' }">{{ vacancyCount(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="编制占用率" width="110">
        <template #default="{ row }">
          <span :style="{ color: usageRate(row) > 100 ? '#f56c6c' : 'inherit' }">{{ usageRateText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="positionStatus" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.positionStatus === 'ENABLED' ? 'success' : 'danger'" size="small">
            {{ row.positionStatus === 'ENABLED' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column prop="expireDate" label="失效日期" width="120" />
      <el-table-column prop="updatedTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.positionStatus === 'ENABLED' ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.positionStatus === 'ENABLED' ? '停用' : '启用' }}
          </el-button>
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

    <div class="stats-bar">
      <el-card shadow="never" class="stat-card">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">总岗位数</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#67c23a">{{ stats.enabled }}</div>
        <div class="stat-label">启用</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#f56c6c">{{ stats.disabled }}</div>
        <div class="stat-label">停用</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#e6a23c">{{ stats.vacant }}</div>
        <div class="stat-label">空缺岗位</div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位编码" required>
              <el-input v-model="formData.positionCode" placeholder="请输入岗位编码" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称" required>
              <el-input v-model="formData.positionName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位简称">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.positionShortName" placeholder="请输入岗位简称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类别">
              <!-- 规格书字段，待后端扩展 -->
              <el-select v-model="formData.positionCategory" placeholder="选择岗位类别" style="width: 100%">
                <el-option label="管理" value="MANAGEMENT" />
                <el-option label="技术" value="TECHNICAL" />
                <el-option label="职能" value="FUNCTIONAL" />
                <el-option label="操作" value="OPERATIONAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属组织编号">
              <el-input-number v-model="formData.orgId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织编码">
              <el-input v-model="formData.orgCode" placeholder="请输入组织编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位层级">
              <el-select v-model="formData.positionLevel" placeholder="选择层级" style="width: 100%">
                <el-option label="一级" value="1" />
                <el-option label="二级" value="2" />
                <el-option label="三级" value="3" />
                <el-option label="四级" value="4" />
                <el-option label="五级" value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职级">
              <el-input v-model="formData.positionRank" placeholder="请输入职级" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪酬档位">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.salaryGrade" placeholder="请输入薪酬档位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.owner" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="编制类型">
              <el-select v-model="formData.staffingType" style="width: 100%">
                <el-option label="固定编制" value="FIXED" />
                <el-option label="弹性编制" value="FLEXIBLE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编制数">
              <el-input-number v-model="formData.staffingCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="现有人数">
              <el-input-number v-model="formData.currentCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位状态">
              <el-select v-model="formData.positionStatus" style="width: 100%">
                <el-option label="启用" value="ENABLED" />
                <el-option label="停用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="formData.effectiveDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期">
              <el-date-picker
                v-model="formData.expireDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位职责摘要">
          <el-input v-model="formData.positionDuty" type="textarea" :rows="2" placeholder="请输入岗位职责摘要" />
        </el-form-item>
        <el-form-item label="岗位职责">
          <el-input v-model="formData.responsibilities" type="textarea" :rows="3" placeholder="请输入岗位职责" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="formData.qualifications" type="textarea" :rows="3" placeholder="请输入任职要求" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织名称">
              <el-input v-model="formData.orgName" placeholder="请输入组织名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="岗位详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位编码">{{ detailData.positionCode }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="组织编码">{{ detailData.orgCode }}</el-descriptions-item>
        <el-descriptions-item label="组织名称">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ detailData.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="职级">{{ detailData.positionRank }}</el-descriptions-item>
        <el-descriptions-item label="岗位简称">{{ detailData.positionShortName }}</el-descriptions-item>
        <el-descriptions-item label="岗位类别">{{ getCategoryLabel(detailData.positionCategory) }}</el-descriptions-item>
        <el-descriptions-item label="薪酬档位">{{ detailData.salaryGrade }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detailData.owner }}</el-descriptions-item>
        <el-descriptions-item label="编制类型">
          {{ detailData.staffingType === 'FIXED' ? '固定编制' : '弹性编制' }}
        </el-descriptions-item>
        <el-descriptions-item label="定编人数">{{ detailData.staffingCount }}</el-descriptions-item>
        <el-descriptions-item label="在岗人数">{{ detailData.currentCount }}</el-descriptions-item>
        <el-descriptions-item label="空缺人数">{{ vacancyCount(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="编制占用率">{{ usageRateText(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.positionStatus === 'ENABLED' ? 'success' : 'danger'" size="small">
            {{ detailData.positionStatus === 'ENABLED' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="岗位职责摘要" :span="2">{{ detailData.positionDuty }}</el-descriptions-item>
        <el-descriptions-item label="岗位职责" :span="2">{{ detailData.responsibilities }}</el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="2">{{ detailData.qualifications }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="更新人">{{ detailData.updatedBy }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updatedTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'positionCode', title: '岗位编码' },
  { key: 'positionName', title: '岗位名称' },
  { key: 'orgCode', title: '组织编码' },
  { key: 'orgName', title: '组织名称' },
  { key: 'positionLevel', title: '层级' },
  { key: 'positionRank', title: '职级' },
  { key: 'positionCategory', title: '岗位类别' },
  { key: 'positionDuty', title: '岗位职责摘要' },
  { key: 'staffingCount', title: '定编人数' },
  { key: 'currentCount', title: '在岗人数' },
  { key: 'salaryGrade', title: '薪酬档位' },
  { key: 'owner', title: '负责人' },
  { key: 'effectiveDate', title: '生效日期' },
  { key: 'expireDate', title: '失效日期' },
  { key: 'updatedTime', title: '更新时间' }
]
const ioFieldMap = {
  '岗位编码': 'positionCode',
  '岗位名称': 'positionName',
  '岗位简称': 'positionShortName',
  '组织编码': 'orgCode',
  '组织名称': 'orgName',
  '层级': 'positionLevel',
  '岗位层级': 'positionLevel',
  '职级': 'positionRank',
  '岗位类别': 'positionCategory',
  '编制数': 'staffingCount',
  '定编人数': 'staffingCount',
  '现有人数': 'currentCount',
  '在岗人数': 'currentCount',
  '编制类型': 'staffingType',
  '岗位职责摘要': 'positionDuty',
  '岗位职责': 'responsibilities',
  '任职要求': 'qualifications',
  '薪酬档位': 'salaryGrade',
  '负责人': 'owner',
  '岗位状态': 'positionStatus',
  '生效日期': 'effectiveDate',
  '失效日期': 'expireDate'
}

const loading = ref(false)

const searchForm = reactive({
  keyword: '',
  orgCode: '',
  positionLevel: '',
  positionStatus: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增岗位')
const formRef = ref(null)

const formData = reactive({
  id: null,
  positionCode: '',
  positionName: '',
  positionShortName: '',
  positionCategory: '',
  salaryGrade: '',
  owner: '',
  orgId: null,
  orgCode: '',
  orgName: '',
  positionLevel: '',
  positionRank: '',
  positionDuty: '',
  staffingType: 'FIXED',
  staffingCount: 0,
  currentCount: 0,
  positionStatus: 'ENABLED',
  responsibilities: '',
  qualifications: '',
  effectiveDate: '',
  expireDate: '',
  sortOrder: 0,
  remark: ''
})

const detailData = reactive({})
const positionList = ref([])

function getCategoryLabel(category) {
  const map = {
    MANAGEMENT: '管理',
    TECHNICAL: '技术',
    FUNCTIONAL: '职能',
    OPERATIONAL: '操作'
  }
  return map[category] || category || ''
}

function vacancyCount(row) {
  const staffing = Number(row.staffingCount) || 0
  const current = Number(row.currentCount) || 0
  return Math.max(staffing - current, 0)
}

// 编制占用率 = 在岗人数 / 定编人数 * 100%
function usageRate(row) {
  const staffing = Number(row.staffingCount) || 0
  if (staffing <= 0) return 0
  const current = Number(row.currentCount) || 0
  return Math.round((current / staffing) * 100)
}

function usageRateText(row) {
  const staffing = Number(row.staffingCount) || 0
  if (staffing <= 0) return '-'
  return usageRate(row) + '%'
}

const filteredPositionList = computed(() => {
  let list = positionList.value
  if (searchForm.keyword) {
    const kw = searchForm.keyword.toLowerCase()
    list = list.filter(p =>
      (p.positionName || '').toLowerCase().includes(kw) ||
      (p.positionCode || '').toLowerCase().includes(kw)
    )
  }
  return list
})

const pagedPositionList = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredPositionList.value.slice(start, start + pagination.pageSize)
})

const stats = computed(() => {
  const list = filteredPositionList.value
  return {
    total: list.length,
    enabled: list.filter(p => p.positionStatus === 'ENABLED').length,
    disabled: list.filter(p => p.positionStatus === 'DISABLED').length,
    vacant: list.filter(p => vacancyCount(p) > 0).length
  }
})

async function loadPositionList() {
  loading.value = true
  try {
    const params = {}
    if (searchForm.orgCode) params.orgCode = searchForm.orgCode
    if (searchForm.positionLevel) params.positionLevel = searchForm.positionLevel
    if (searchForm.positionStatus) params.positionStatus = searchForm.positionStatus

    const res = await axios.get('/api/position', { params })
    if (res.data.code === 200) {
      positionList.value = res.data.data || []
      pagination.total = filteredPositionList.value.length
    }
  } catch (e) {
    ElMessage.error('加载岗位列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadPositionList().then(() => {
    pagination.total = filteredPositionList.value.length
  })
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.orgCode = ''
  searchForm.positionLevel = ''
  searchForm.positionStatus = ''
  pagination.currentPage = 1
  loadPositionList().then(() => {
    pagination.total = filteredPositionList.value.length
  })
}

function handleSizeChange(val) {
  pagination.pageSize = val
}

function handleCurrentChange(val) {
  pagination.currentPage = val
}

function handleAdd() {
  dialogTitle.value = '新增岗位'
  Object.assign(formData, {
    id: null,
    positionCode: '',
    positionName: '',
    positionShortName: '',
    positionCategory: '',
    salaryGrade: '',
    owner: '',
    orgId: null,
    orgCode: '',
    orgName: '',
    positionLevel: '',
    positionRank: '',
    positionDuty: '',
    staffingType: 'FIXED',
    staffingCount: 0,
    currentCount: 0,
    positionStatus: 'ENABLED',
    responsibilities: '',
    qualifications: '',
    effectiveDate: '',
    expireDate: '',
    sortOrder: 0,
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑岗位'
  Object.assign(formData, {
    id: row.id,
    positionCode: row.positionCode,
    positionName: row.positionName,
    positionShortName: row.positionShortName || '',
    positionCategory: row.positionCategory || '',
    salaryGrade: row.salaryGrade || '',
    owner: row.owner || '',
    orgId: row.orgId,
    orgCode: row.orgCode,
    orgName: row.orgName,
    positionLevel: row.positionLevel,
    positionRank: row.positionRank,
    positionDuty: row.positionDuty,
    staffingType: row.staffingType,
    staffingCount: row.staffingCount,
    currentCount: row.currentCount,
    positionStatus: row.positionStatus,
    responsibilities: row.responsibilities,
    qualifications: row.qualifications,
    effectiveDate: row.effectiveDate,
    expireDate: row.expireDate,
    sortOrder: row.sortOrder,
    remark: row.remark
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/position/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取岗位详情失败')
  }
}

function toggleStatus(row) {
  const newStatus = row.positionStatus === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = newStatus === 'ENABLED' ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}该岗位吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.put(`/api/position/${row.id}/status`, null, {
        params: { status: newStatus }
      })
      if (res.data.code === 200) {
        ElMessage.success(`${action}成功`)
        loadPositionList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
      }
    } catch (e) {
      ElMessage.error(`${action}失败`)
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该岗位吗？删除后不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/position/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadPositionList()
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
      res = await axios.put(`/api/position/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/position', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增岗位' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadPositionList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadPositionList()
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

.stats-bar {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.stat-card {
  flex: 1;
  text-align: center;
}

.stat-num {
  font-size: 22px;
  font-weight: bold;
}

.stat-label {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}
</style>

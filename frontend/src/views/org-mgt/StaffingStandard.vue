<template>
  <div class="page-container">
    <div class="page-header">
      <h2>编制标准</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="list"
          file-name="岗位编制标准"
          title="岗位编制标准"
          import-api="/api/staffing-standard"
          :field-map="ioFieldMap"
          @done="loadList"
        />
        <el-button type="primary" @click="handleAdd">新增编制标准</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="岗位层级">
        <el-select v-model="searchForm.positionLevel" placeholder="选择层级" style="width: 150px">
          <el-option label="一级" value="L1" />
          <el-option label="二级" value="L2" />
          <el-option label="三级" value="L3" />
          <el-option label="四级" value="L4" />
          <el-option label="五级" value="L5" />
        </el-select>
      </el-form-item>
      <el-form-item label="编制类型">
        <el-select v-model="searchForm.staffingType" placeholder="选择类型" style="width: 150px">
          <el-option label="固定编制" value="FIXED" />
          <el-option label="弹性编制" value="FLEXIBLE" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="生效中" value="ACTIVE" />
          <el-option label="已失效" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="orgQuotaCheck.length > 0"
      type="error"
      :closable="false"
      show-icon
      style="margin-bottom: 12px"
    >
      <template #title>
        层级总额校验：以下组织在岗总额已突破定编总额（下级总额＞上级总额），请及时整改：
        <span v-for="o in orgQuotaCheck" :key="o.orgCode" style="margin-right: 12px; font-weight: bold">
          {{ o.orgName || o.orgCode }}（定编{{ o.totalQuota }} / 在岗{{ o.totalOccupied }}）
        </span>
      </template>
    </el-alert>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="standardCode" label="标准编码" width="140" />
      <el-table-column prop="orgName" label="所属组织" width="140" />
      <el-table-column prop="positionName" label="岗位名称" width="140" />
      <el-table-column prop="positionLevel" label="岗位层级" width="100" />
      <el-table-column prop="positionCategory" label="岗位类别" width="120">
        <template #default="{ row }">
          {{ getCategoryLabel(row.positionCategory) }}
        </template>
      </el-table-column>
      <el-table-column prop="staffingType" label="编制类型" width="100">
        <template #default="{ row }">
          {{ row.staffingType === 'FIXED' ? '固定编制' : '弹性编制' }}
        </template>
      </el-table-column>
      <el-table-column prop="standardQuota" label="核定定编数" width="100" />
      <el-table-column prop="occupiedCount" label="在岗人数" width="90">
        <template #default="{ row }">
          {{ row.occupiedCount ?? 0 }}
        </template>
      </el-table-column>
      <el-table-column label="空缺编制数" width="100">
        <template #default="{ row }">
          <span :style="{ color: vacancyCount(row) > 0 ? '#e6a23c' : 'inherit' }">{{ vacancyCount(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="超编数量" width="90">
        <template #default="{ row }">
          <span :style="{ color: overCount(row) > 0 ? '#f56c6c' : 'inherit' }">{{ overCount(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="编制使用率" width="100">
        <template #default="{ row }">
          {{ usageRate(row) }}
        </template>
      </el-table-column>
      <el-table-column label="编制状态" width="100">
        <template #default="{ row }">
          <el-tag :type="staffingStateType(row)" size="small">{{ staffingState(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column prop="expireDate" label="失效日期" width="120" />
      <el-table-column prop="versionNo" label="版本号" width="100" />
      <el-table-column prop="status" label="标准状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '生效中' : '已失效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">调整</el-button>
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
        <div class="stat-num">{{ stats.totalQuota }}</div>
        <div class="stat-label">总定编</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#67c23a">{{ stats.totalOccupied }}</div>
        <div class="stat-label">总在岗</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#e6a23c">{{ stats.totalVacant }}</div>
        <div class="stat-label">总空缺</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-num" style="color:#f56c6c">{{ stats.totalOver }}</div>
        <div class="stat-label">总超编</div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标准编码">
              <el-input v-model="formData.standardCode" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织编号">
              <el-input-number v-model="formData.orgId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织编码">
              <el-input v-model="formData.orgCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织名称">
              <el-input v-model="formData.orgName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位编码">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.positionCode" placeholder="请输入岗位编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.positionName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位层级">
              <el-select v-model="formData.positionLevel" style="width: 100%">
                <el-option label="一级" value="L1" />
                <el-option label="二级" value="L2" />
                <el-option label="三级" value="L3" />
                <el-option label="四级" value="L4" />
                <el-option label="五级" value="L5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类别">
              <el-select v-model="formData.positionCategory" style="width: 100%">
                <el-option label="管理类" value="MANAGEMENT" />
                <el-option label="技术类" value="TECHNICAL" />
                <el-option label="职能类" value="FUNCTIONAL" />
                <el-option label="操作类" value="OPERATIONAL" />
              </el-select>
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
            <el-form-item label="核定定编数">
              <el-input-number v-model="formData.standardQuota" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="在岗人数">
              <!-- 规格书字段，待后端扩展 -->
              <el-input-number v-model="formData.occupiedCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许超编标识">
              <!-- 规格书字段，待后端扩展 -->
              <el-switch v-model="formData.allowOverstaff" active-text="允许超编" inactive-text="禁止超编" />
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input v-model="formData.versionNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option label="生效中" value="ACTIVE" />
                <el-option label="已失效" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="编制调整依据">
          <el-input v-model="formData.standardBasis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="最后调整批次">
          <!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.lastAdjustBatch" placeholder="请输入最后调整批次" />
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

    <el-dialog v-model="detailVisible" title="编制标准详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标准编码">{{ detailData.standardCode }}</el-descriptions-item>
        <el-descriptions-item label="组织名称">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="岗位编码">{{ detailData.positionCode }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ detailData.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="岗位类别">{{ getCategoryLabel(detailData.positionCategory) }}</el-descriptions-item>
        <el-descriptions-item label="编制类型">
          {{ detailData.staffingType === 'FIXED' ? '固定编制' : '弹性编制' }}
        </el-descriptions-item>
        <el-descriptions-item label="核定定编数">{{ detailData.standardQuota }}</el-descriptions-item>
        <el-descriptions-item label="在岗人数">{{ detailData.occupiedCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="允许超编标识">{{ detailData.allowOverstaff ? '允许超编' : '禁止超编' }}</el-descriptions-item>
        <el-descriptions-item label="空缺编制数">{{ vacancyCount(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="超编数量">{{ overCount(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="编制使用率">{{ usageRate(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="编制状态">
          <el-tag :type="staffingStateType(detailData)" size="small">{{ staffingState(detailData) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="版本号">{{ detailData.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ detailData.status === 'ACTIVE' ? '生效中' : '已失效' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新人">{{ detailData.updatedBy }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="最后调整批次">{{ detailData.lastAdjustBatch }}</el-descriptions-item>
        <el-descriptions-item label="编制调整依据" :span="2">{{ detailData.standardBasis }}</el-descriptions-item>
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
  { key: 'standardCode', title: '标准编码' },
  { key: 'orgName', title: '组织名称' },
  { key: 'positionCode', title: '岗位编码' },
  { key: 'positionName', title: '岗位名称' },
  { key: 'positionLevel', title: '岗位层级' },
  { key: 'positionCategory', title: '岗位类别' },
  { key: 'staffingType', title: '编制类型' },
  { key: 'standardQuota', title: '核定定编数' },
  { key: 'occupiedCount', title: '在岗人数' },
  { key: 'effectiveDate', title: '生效日期' },
  { key: 'expireDate', title: '失效日期' },
  { key: 'versionNo', title: '版本号' },
  { key: 'updateTime', title: '更新时间' }
]
const ioFieldMap = {
  '标准编码': 'standardCode',
  '组织编码': 'orgCode',
  '组织名称': 'orgName',
  '岗位编码': 'positionCode',
  '岗位名称': 'positionName',
  '岗位层级': 'positionLevel',
  '岗位类别': 'positionCategory',
  '编制类型': 'staffingType',
  '标准编制': 'standardQuota',
  '核定定编数': 'standardQuota',
  '在岗人数': 'occupiedCount',
  '允许超编': 'allowOverstaff',
  '允许超编标识': 'allowOverstaff',
  '编制调整依据': 'standardBasis',
  '标准依据': 'standardBasis',
  '最后调整批次': 'lastAdjustBatch',
  '生效日期': 'effectiveDate',
  '失效日期': 'expireDate',
  '版本号': 'versionNo',
  '状态': 'status',
  '备注': 'remark'
}

const loading = ref(false)

const searchForm = reactive({
  positionLevel: '',
  staffingType: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增编制标准')
const formRef = ref(null)

const formData = reactive({
  id: null,
  standardCode: '',
  orgId: null,
  orgCode: '',
  orgName: '',
  positionCode: '',
  positionName: '',
  positionLevel: '',
  positionCategory: '',
  staffingType: 'FIXED',
  standardQuota: 0,
  occupiedCount: 0,
  allowOverstaff: false,
  effectiveDate: '',
  expireDate: '',
  standardBasis: '',
  versionNo: '',
  status: 'ACTIVE',
  lastAdjustBatch: '',
  remark: ''
})

const detailData = reactive({})
const list = ref([])

function getCategoryLabel(category) {
  const map = {
    MANAGEMENT: '管理类',
    TECHNICAL: '技术类',
    FUNCTIONAL: '职能类',
    OPERATIONAL: '操作类'
  }
  return map[category] || category || ''
}

function vacancyCount(row) {
  const quota = Number(row.standardQuota) || 0
  const occupied = Number(row.occupiedCount) || 0
  return Math.max(quota - occupied, 0)
}

function overCount(row) {
  const quota = Number(row.standardQuota) || 0
  const occupied = Number(row.occupiedCount) || 0
  return Math.max(occupied - quota, 0)
}

function usageRate(row) {
  const quota = Number(row.standardQuota) || 0
  if (quota <= 0) return '-'
  const occupied = Number(row.occupiedCount) || 0
  return Math.round((occupied / quota) * 100) + '%'
}

function staffingState(row) {
  if (overCount(row) > 0) return '超编'
  if (vacancyCount(row) > 0) return '空缺'
  return '正常'
}

function staffingStateType(row) {
  const state = staffingState(row)
  if (state === '超编') return row.allowOverstaff ? 'warning' : 'danger'
  if (state === '空缺') return 'warning'
  return 'success'
}

const stats = computed(() => {
  const listVal = list.value
  return {
    totalQuota: listVal.reduce((s, r) => s + (Number(r.standardQuota) || 0), 0),
    totalOccupied: listVal.reduce((s, r) => s + (Number(r.occupiedCount) || 0), 0),
    totalVacant: listVal.reduce((s, r) => s + vacancyCount(r), 0),
    totalOver: listVal.reduce((s, r) => s + overCount(r), 0)
  }
})

// 层级总额校验：按组织聚合定编与在岗，在岗总额 > 定编总额即视为突破上级总额，标红提示
const orgQuotaCheck = computed(() => {
  const map = {}
  list.value.forEach(r => {
    const key = r.orgCode || r.orgName || '未知组织'
    if (!map[key]) map[key] = { orgCode: r.orgCode, orgName: r.orgName, totalQuota: 0, totalOccupied: 0 }
    map[key].totalQuota += Number(r.standardQuota) || 0
    map[key].totalOccupied += Number(r.occupiedCount) || 0
  })
  return Object.values(map).filter(o => o.totalOccupied > o.totalQuota)
})

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/staffing-standard/page', {
      params: {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        positionLevel: searchForm.positionLevel || undefined,
        staffingType: searchForm.staffingType || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.records || res.data.data.list || []
      pagination.total = res.data.data.total || 0
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
  searchForm.positionLevel = ''
  searchForm.staffingType = ''
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
  dialogTitle.value = '新增编制标准'
  Object.assign(formData, {
    id: null,
    standardCode: '',
    orgId: null,
    orgCode: '',
    orgName: '',
    positionCode: '',
    positionName: '',
    positionLevel: '',
    positionCategory: '',
    staffingType: 'FIXED',
    standardQuota: 0,
    occupiedCount: 0,
    allowOverstaff: false,
    effectiveDate: '',
    expireDate: '',
    standardBasis: '',
    versionNo: '',
    status: 'ACTIVE',
    lastAdjustBatch: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑编制标准'
  Object.assign(formData, {
    positionCode: '',
    positionName: '',
    occupiedCount: 0,
    allowOverstaff: false,
    lastAdjustBatch: ''
  }, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/staffing-standard/${row.id}`)
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
      const res = await axios.delete(`/api/staffing-standard/${row.id}`)
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
      res = await axios.put(`/api/staffing-standard/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/staffing-standard', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增编制标准' ? '新增成功' : '修改成功')
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

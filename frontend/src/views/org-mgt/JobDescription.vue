<template>
  <div class="page-container">
    <div class="page-header">
      <h2>岗位说明书</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="list"
          file-name="岗位说明书"
          title="岗位说明书"
          import-api="/api/job-description"
          :field-map="ioFieldMap"
          @done="loadList"
        />
        <el-button type="primary" @click="handleAdd">新增说明书</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 180px" />
      </el-form-item>
      <el-form-item label="组织单元">
        <el-input v-model="searchForm.orgUnit" placeholder="请输入组织单元" style="width: 150px" />
      </el-form-item>
      <el-form-item label="岗位层级">
        <el-input v-model="searchForm.positionLevel" placeholder="请输入岗位层级" style="width: 120px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审批" value="PENDING_APPROVAL" />
          <el-option label="生效" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已归档" value="ARCHIVED" />
          <el-option label="作废" value="ABANDONED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="positionCode" label="岗位编码" width="140" />
      <el-table-column prop="positionName" label="岗位名称" width="160" />
      <el-table-column prop="orgUnit" label="组织单元" width="120" />
      <el-table-column prop="positionLevel" label="岗位层级" width="100" />
      <el-table-column prop="headcount" label="编制人数" width="100" />
      <el-table-column prop="versionNo" label="版本号" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="writeStatus" label="编写状态" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getWriteStatusLabel(row.writeStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="effectiveTime" label="生效时间" width="160" />
      <el-table-column prop="createdBy" label="编制人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="530" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" @click="handlePreview(row)">在线预览</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)" v-if="row.status === 'DRAFT' || row.status === 'REJECTED'">编辑</el-button>
          <el-button size="small" type="success" @click="handleSubmit(row)" v-if="row.status === 'DRAFT' || row.status === 'REJECTED'">提交审批</el-button>
          <el-button size="small" type="success" plain @click="handleApprove(row)" v-if="row.status === 'PENDING_APPROVAL'">审批通过</el-button>
          <el-button size="small" type="danger" plain @click="handleReject(row)" v-if="row.status === 'PENDING_APPROVAL'">驳回</el-button>
          <el-button size="small" type="warning" @click="handleVersions(row)">版本历史</el-button>
          <el-button size="small" @click="handleExportPdf(row)">导出PDF</el-button>
          <el-button size="small" @click="handleExportExcel(row)">导出Excel</el-button>
          <el-button size="small" type="info" plain @click="handleArchive(row)" v-if="row.status === 'APPROVED'">归档</el-button>
          <el-button size="small" type="danger" plain @click="handleAbandon(row)" v-if="row.status === 'APPROVED'">作废</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" v-if="row.status === 'DRAFT' || row.status === 'REJECTED'">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位编码" required>
              <el-input v-model="formData.positionCode" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称">
              <el-input v-model="formData.positionName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织单元">
              <el-input v-model="formData.orgUnit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位层级">
              <el-input v-model="formData.positionLevel" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="编制人数">
              <el-input-number v-model="formData.headcount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input v-model="formData.versionNo" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位概述">
          <el-input v-model="formData.jobSummary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="主要职责">
          <el-input v-model="formData.mainResponsibilities" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="工作权限">
          <el-input v-model="formData.workPermissions" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历要求">
              <el-input v-model="formData.educationRequirement" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业要求">
              <el-input v-model="formData.majorRequirement" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="经验要求">
          <el-input v-model="formData.experienceRequirement" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="技能要求">
          <el-input v-model="formData.skillRequirement" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="素质要求">
          <el-input v-model="formData.qualityRequirement" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="工作关系">
          <el-input v-model="formData.workRelationship" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="关键绩效指标">
          <el-input v-model="formData.kpiIndicators" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-input v-model="formData.salaryRange" />
        </el-form-item>
        <el-form-item label="职责边界说明">
          <!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.dutyBoundary" type="textarea" :rows="2" placeholder="岗位职责边界说明" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="formData.effectiveTime"
                type="datetime"
                placeholder="选择生效时间"
                style="width: 100%"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作废日期">
              <!-- 规格书字段，待后端扩展 -->
              <el-date-picker
                v-model="formData.abandonTime"
                type="datetime"
                placeholder="选择作废时间"
                style="width: 100%"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="编制人">
              <el-input v-model="formData.createdBy" placeholder="编制人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核人">
              <!-- 规格书字段，待后端扩展 -->
              <el-input v-model="formData.reviewer" placeholder="审核人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="变更说明">
          <!-- 规格书字段，待后端扩展 -->
          <el-input v-model="formData.changeNote" type="textarea" :rows="2" placeholder="本次修订变更说明" />
        </el-form-item>
        <el-form-item label="编制状态">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING_APPROVAL" />
            <el-option label="生效" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已归档" value="ARCHIVED" />
            <el-option label="作废" value="ABANDONED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="岗位说明书详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位编码">{{ detailData.positionCode }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="组织单元">{{ detailData.orgUnit }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ detailData.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="编制人数">{{ detailData.headcount }}</el-descriptions-item>
        <el-descriptions-item label="版本号">{{ detailData.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusLabel(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="编写状态">{{ getWriteStatusLabel(detailData.writeStatus) }}</el-descriptions-item>
        <el-descriptions-item label="生效时间">{{ detailData.effectiveTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="岗位概述" :span="2">{{ detailData.jobSummary }}</el-descriptions-item>
        <el-descriptions-item label="主要职责" :span="2">{{ detailData.mainResponsibilities }}</el-descriptions-item>
        <el-descriptions-item label="工作权限" :span="2">{{ detailData.workPermissions }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ detailData.educationRequirement }}</el-descriptions-item>
        <el-descriptions-item label="专业要求">{{ detailData.majorRequirement }}</el-descriptions-item>
        <el-descriptions-item label="经验要求" :span="2">{{ detailData.experienceRequirement }}</el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="2">{{ detailData.skillRequirement }}</el-descriptions-item>
        <el-descriptions-item label="素质要求" :span="2">{{ detailData.qualityRequirement }}</el-descriptions-item>
        <el-descriptions-item label="工作关系" :span="2">{{ detailData.workRelationship }}</el-descriptions-item>
        <el-descriptions-item label="关键绩效指标" :span="2">{{ detailData.kpiIndicators }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ detailData.salaryRange }}</el-descriptions-item>
        <el-descriptions-item label="职责边界说明" :span="2">{{ detailData.dutyBoundary }}</el-descriptions-item>
        <el-descriptions-item label="编制人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.reviewer }}</el-descriptions-item>
        <el-descriptions-item label="作废日期">{{ detailData.abandonTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="变更说明" :span="2">{{ detailData.changeNote }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remarks }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="岗位说明书在线预览" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item v-for="(val, key) in previewData" :key="key" :label="String(key)">
          {{ val }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-if="!previewData || Object.keys(previewData).length === 0" description="暂无预览内容" />
    </el-dialog>

    <el-dialog v-model="versionsVisible" title="版本历史" width="800px">
      <el-table :data="versionsList" border stripe size="small">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="versionNo" label="版本号" width="100" />
        <el-table-column prop="status" label="编制状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效日期" width="170" />
        <el-table-column prop="createdBy" label="编制人" width="110" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handlePreview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="versionsList.length === 0" description="暂无历史版本" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const ioColumns = [
  { key: 'positionCode', title: '岗位编码' },
  { key: 'positionName', title: '岗位名称' },
  { key: 'orgUnit', title: '组织单元' },
  { key: 'positionLevel', title: '岗位层级' },
  { key: 'headcount', title: '编制人数' },
  { key: 'versionNo', title: '版本号' },
  { key: 'jobSummary', title: '岗位概述' },
  { key: 'mainResponsibilities', title: '主要职责' },
  { key: 'workPermissions', title: '工作权限' },
  { key: 'educationRequirement', title: '学历要求' },
  { key: 'majorRequirement', title: '专业要求' },
  { key: 'experienceRequirement', title: '经验要求' },
  { key: 'skillRequirement', title: '技能要求' },
  { key: 'qualityRequirement', title: '素质要求' },
  { key: 'workRelationship', title: '工作关系' },
  { key: 'kpiIndicators', title: '关键绩效指标' },
  { key: 'dutyBoundary', title: '职责边界说明' },
  { key: 'salaryRange', title: '薪资范围' },
  { key: 'effectiveTime', title: '生效时间' },
  { key: 'updateTime', title: '更新时间' }
]
const ioFieldMap = {
  '岗位编码': 'positionCode',
  '岗位名称': 'positionName',
  '组织单元': 'orgUnit',
  '岗位层级': 'positionLevel',
  '编制人数': 'headcount',
  '版本号': 'versionNo',
  '岗位概述': 'jobSummary',
  '主要职责': 'mainResponsibilities',
  '工作权限': 'workPermissions',
  '学历要求': 'educationRequirement',
  '专业要求': 'majorRequirement',
  '经验要求': 'experienceRequirement',
  '技能要求': 'skillRequirement',
  '素质要求': 'qualityRequirement',
  '工作关系': 'workRelationship',
  '关键绩效指标': 'kpiIndicators',
  '职责边界说明': 'dutyBoundary',
  '薪资范围': 'salaryRange',
  '生效时间': 'effectiveTime',
  '变更说明': 'changeNote'
}

const loading = ref(false)

const searchForm = reactive({
  keyword: '',
  orgUnit: '',
  positionLevel: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const previewVisible = ref(false)
const versionsVisible = ref(false)
const dialogTitle = ref('新增岗位说明书')
const formRef = ref(null)
const previewData = ref({})
const versionsList = ref([])

const formData = reactive({
  id: null,
  positionCode: '',
  positionName: '',
  orgUnit: '',
  positionLevel: '',
  headcount: 0,
  jobSummary: '',
  mainResponsibilities: '',
  workPermissions: '',
  educationRequirement: '',
  majorRequirement: '',
  experienceRequirement: '',
  skillRequirement: '',
  qualityRequirement: '',
  workRelationship: '',
  kpiIndicators: '',
  salaryRange: '',
  remarks: '',
  versionNo: '',
  status: 'DRAFT',
  writeStatus: 'UNWRITTEN',
  effectiveTime: '',
  createdBy: '',
  dutyBoundary: '',
  reviewer: '',
  abandonTime: '',
  changeNote: ''
})

const detailData = reactive({})
const list = ref([])

function getStatusType(status) {
  const map = {
    DRAFT: 'info',
    PENDING_APPROVAL: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    ARCHIVED: 'info',
    ABANDONED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    DRAFT: '草稿',
    PENDING_APPROVAL: '待审批',
    APPROVED: '生效',
    REJECTED: '已驳回',
    ARCHIVED: '已归档',
    ABANDONED: '作废'
  }
  return map[status] || status
}

function getWriteStatusLabel(status) {
  const map = {
    UNWRITTEN: '未编写',
    DRAFT: '编写中',
    COMPLETED: '已完成'
  }
  return map[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/job-description/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        orgUnit: searchForm.orgUnit || undefined,
        positionLevel: searchForm.positionLevel || undefined,
        status: searchForm.status || undefined
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
  searchForm.orgUnit = ''
  searchForm.positionLevel = ''
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
  dialogTitle.value = '新增岗位说明书'
  Object.assign(formData, {
    id: null,
    positionCode: '',
    positionName: '',
    orgUnit: '',
    positionLevel: '',
    headcount: 0,
    jobSummary: '',
    mainResponsibilities: '',
    workPermissions: '',
    educationRequirement: '',
    majorRequirement: '',
    experienceRequirement: '',
    skillRequirement: '',
    qualityRequirement: '',
    workRelationship: '',
    kpiIndicators: '',
    salaryRange: '',
    remarks: '',
    versionNo: '',
    status: 'DRAFT',
    writeStatus: 'UNWRITTEN',
    effectiveTime: '',
    createdBy: '',
    dutyBoundary: '',
    reviewer: '',
    abandonTime: '',
    changeNote: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑岗位说明书'
  Object.assign(formData, {
    dutyBoundary: '',
    reviewer: '',
    abandonTime: '',
    changeNote: ''
  }, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/job-description/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function handleSubmit(row) {
  ElMessageBox.confirm('确定要提交审批吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const instNo = await startApproval({
        defCode: FLOW_DEF_CODES.HRM_JOB_DESC,
        bizCode: 'job-description',
        bizId: row.id,
        bizJson: row,
        title: `岗位说明书-${row.positionName || row.positionCode}`,
        initiator: localStorage.getItem('username') || 'dev-user'
      })
      const res = await axios.post(`/api/job-description/${row.id}/submit`)
      if (res.data.code === 200) {
        ElMessage.success('已发起流程审批，实例号：' + instNo)
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '提交失败')
      }
    } catch (e) {
      ElMessage.warning('流程引擎不可用，已本地提交')
      try {
        const res = await axios.post(`/api/job-description/${row.id}/submit`)
        if (res.data.code === 200) {
          ElMessage.success('提交成功')
          loadList()
        } else {
          ElMessage.error(res.data.message || res.data.msg || '提交失败')
        }
      } catch (e2) {
        ElMessage.error('提交失败')
      }
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
      const res = await axios.delete(`/api/job-description/${row.id}`)
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

async function handlePreview(row) {
  try {
    const res = await axios.get(`/api/job-description/${row.id}/preview`)
    if (res.data.code === 200) {
      previewData.value = res.data.data || {}
      previewVisible.value = true
    } else {
      ElMessage.error(res.data.message || res.data.msg || '获取预览失败')
    }
  } catch (e) {
    ElMessage.error('获取预览失败')
  }
}

async function handleVersions(row) {
  try {
    const res = await axios.get(`/api/job-description/versions/${row.positionCode}`)
    if (res.data.code === 200) {
      versionsList.value = res.data.data || []
      versionsVisible.value = true
    } else {
      ElMessage.error(res.data.message || res.data.msg || '获取版本历史失败')
    }
  } catch (e) {
    ElMessage.error('获取版本历史失败')
  }
}

async function handleExportPdf(row) {
  try {
    const res = await axios.get(`/api/job-description/${row.id}/export/pdf`)
    if (res.data.code === 200) {
      ElMessage.success('导出PDF成功' + (res.data.data ? `：${res.data.data}` : ''))
    } else {
      ElMessage.error(res.data.message || res.data.msg || '导出PDF失败')
    }
  } catch (e) {
    ElMessage.error('导出PDF失败')
  }
}

function handleAbandon(row) {
  ElMessageBox.confirm('确定要作废该岗位说明书吗？作废后仅可查阅，不可作为现行依据。', '作废确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/job-description/${row.id}/abandon`)
      if (res.data.code === 200) {
        ElMessage.success('作废成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '作废失败')
      }
    } catch (e) {
      ElMessage.error('作废失败')
    }
  })
}

function handleApprove(row) {
  ElMessageBox.confirm('确定要审批通过该岗位说明书吗？', '审批确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/job-description/${row.id}/approve`)
      if (res.data.code === 200) {
        ElMessage.success('审批通过')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '审批失败')
      }
    } catch (e) {
      ElMessage.error('审批失败')
    }
  }).catch(() => {})
}

function handleReject(row) {
  ElMessageBox.prompt('请输入驳回意见', '驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputValidator: (v) => (v && v.trim() ? true : '驳回意见不能为空')
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/job-description/${row.id}/reject`, { reason: value })
      if (res.data.code === 200) {
        ElMessage.success('已驳回')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '驳回失败')
      }
    } catch (e) {
      ElMessage.error('驳回失败')
    }
  }).catch(() => {})
}

function handleArchive(row) {
  ElMessageBox.confirm('确定要归档该岗位说明书吗？归档后仅可查阅。', '归档确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/job-description/${row.id}/archive`)
      if (res.data.code === 200) {
        ElMessage.success('归档成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '归档失败')
      }
    } catch (e) {
      ElMessage.error('归档失败')
    }
  }).catch(() => {})
}

async function handleExportExcel(row) {
  try {
    const res = await axios.get(`/api/job-description/${row.id}/export/excel`)
    if (res.data.code === 200) {
      ElMessage.success('导出Excel成功' + (res.data.data ? `：${res.data.data}` : ''))
    } else {
      ElMessage.error(res.data.message || res.data.msg || '导出Excel失败')
    }
  } catch (e) {
    ElMessage.error('导出Excel失败')
  }
}

async function handleFormSubmit() {
    try {
      let res
      if (formData.id) {
        res = await axios.put(`/api/job-description/${formData.id}`, formData)
      } else {
        res = await axios.post('/api/job-description', formData)
      }
      if (res.data.code === 200) {
        ElMessage.success(dialogTitle.value === '新增岗位说明书' ? '新增成功' : '修改成功')
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

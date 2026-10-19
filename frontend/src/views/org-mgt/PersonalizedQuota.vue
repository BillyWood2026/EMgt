<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个性化编制配置</h2>
      <el-button type="primary" @click="handleAdd">新增配置</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="组织编码">
        <el-input v-model="searchForm.orgCode" placeholder="请输入组织编码" style="width: 150px" />
      </el-form-item>
      <el-form-item label="岗位层级">
        <el-select v-model="searchForm.positionLevel" placeholder="选择层级" style="width: 120px">
          <el-option label="一级" value="L1" />
          <el-option label="二级" value="L2" />
          <el-option label="三级" value="L3" />
          <el-option label="四级" value="L4" />
          <el-option label="五级" value="L5" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 140px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审批" value="PENDING_APPROVAL" />
          <el-option label="生效中" value="ACTIVE" />
          <el-option label="已失效" value="INACTIVE" />
          <el-option label="已过期" value="EXPIRED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="orgCode" label="组织编码" width="120" />
      <el-table-column prop="orgName" label="组织名称" width="140" />
      <el-table-column prop="orgLevel" label="组织层级" width="100" />
      <el-table-column prop="positionLevel" label="岗位层级" width="100" />
      <el-table-column prop="customMaxQuota" label="自定义最大职数" width="130" />
      <el-table-column prop="customMinAmplitude" label="最小幅度" width="100" />
      <el-table-column prop="customStdAmplitude" label="标准幅度" width="100" />
      <el-table-column prop="customMaxAmplitude" label="最大幅度" width="100" />
      <el-table-column prop="warningFactor" label="预警系数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="启用" width="80">
        <template #default="{ row }">
          {{ row.enabled ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column prop="expireDate" label="失效日期" width="120" />
      <el-table-column prop="versionNo" label="版本" width="80" />
      <el-table-column prop="configuredBy" label="配置人" width="100" />
      <el-table-column prop="configureTime" label="配置时间" width="160" />
      <el-table-column label="操作" width="480" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" v-if="row.status === 'DRAFT'" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" v-if="row.status === 'DRAFT'" @click="handleSubmit(row)">提交审批</el-button>
          <el-button size="small" type="success" plain v-if="row.status === 'PENDING_APPROVAL'" @click="handleApprove(row)">审批通过</el-button>
          <el-button size="small" type="danger" plain v-if="row.status === 'PENDING_APPROVAL'" @click="handleReject(row)">驳回</el-button>
          <el-button size="small" :type="row.enabled ? 'warning' : 'success'" plain v-if="row.status === 'ACTIVE'" @click="handleToggle(row)">
            {{ row.enabled ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="success" plain v-if="row.status === 'PENDING_APPROVAL'" @click="handleEffective(row)">生效</el-button>
          <el-button size="small" type="info" plain v-if="row.status === 'ACTIVE'" @click="handleExpire(row)">失效</el-button>
          <el-button size="small" type="danger" v-if="row.status === 'DRAFT'" @click="handleDelete(row)">删除</el-button>
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
            <el-form-item label="组织编号" required>
              <el-input-number v-model="formData.orgId" :min="0" style="width: 100%" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织编码">
              <el-input v-model="formData.orgCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织名称">
              <el-input v-model="formData.orgName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织层级">
              <el-select v-model="formData.orgLevel" style="width: 100%">
                <el-option label="公司级" value="COMPANY" />
                <el-option label="中心级" value="CENTER" />
                <el-option label="部门级" value="DEPARTMENT" />
                <el-option label="科级" value="SECTION" />
                <el-option label="组级" value="TEAM" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位层级" required>
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
            <el-form-item label="状态">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="待审批" value="PENDING_APPROVAL" />
                <el-option label="生效中" value="ACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>职数配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="自定义最大职数">
              <el-input-number v-model="formData.customMaxQuota" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警系数">
              <el-input-number v-model="formData.warningFactor" :min="0" :max="1" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>幅度配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小幅度">
              <el-input-number v-model="formData.customMinAmplitude" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标准幅度">
              <el-input-number v-model="formData.customStdAmplitude" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大幅度">
              <el-input-number v-model="formData.customMaxAmplitude" :min="0" style="width: 100%" />
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
            <el-form-item label="是否启用">
              <el-switch v-model="formData.enabled" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input v-model="formData.versionNo" disabled placeholder="由系统自动维护" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="配置说明">
          <el-input v-model="formData.configDesc" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="个性化编制配置详情" width="700px">
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="组织编码">{{ detailData.orgCode }}</el-descriptions-item>
        <el-descriptions-item label="组织名称">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="组织层级">{{ detailData.orgLevel }}</el-descriptions-item>
        <el-descriptions-item label="岗位层级">{{ detailData.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="自定义最大职数">{{ detailData.customMaxQuota }}</el-descriptions-item>
        <el-descriptions-item label="预警系数">{{ detailData.warningFactor }}</el-descriptions-item>
        <el-descriptions-item label="最小幅度">{{ detailData.customMinAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="标准幅度">{{ detailData.customStdAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="最大幅度">{{ detailData.customMaxAmplitude }}</el-descriptions-item>
        <el-descriptions-item label="是否启用">{{ detailData.enabled ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusLabel(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ detailData.versionNo }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="配置人">{{ detailData.configuredBy }}</el-descriptions-item>
        <el-descriptions-item label="配置时间">{{ detailData.configureTime }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detailData.approver }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ detailData.approvalTime }}</el-descriptions-item>
        <el-descriptions-item label="配置说明" :span="2">{{ detailData.configDesc }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)

const searchForm = reactive({
  orgCode: '',
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
const dialogTitle = ref('新增配置')
const formRef = ref(null)

const formData = reactive({
  id: null,
  orgId: null,
  orgCode: '',
  orgName: '',
  orgLevel: '',
  positionLevel: '',
  customMaxQuota: 0,
  customMinAmplitude: 0,
  customStdAmplitude: 0,
  customMaxAmplitude: 0,
  warningFactor: 0.8,
  effectiveDate: '',
  expireDate: '',
  configDesc: '',
  enabled: false,
  approver: '',
  approvalTime: '',
  approvalResult: '',
  configuredBy: '',
  configureTime: '',
  status: 'DRAFT',
  versionNo: '',
  remark: ''
})

const detailData = reactive({})
const list = ref([])

function getStatusType(status) {
  const map = {
    DRAFT: 'info',
    PENDING_APPROVAL: 'warning',
    ACTIVE: 'success',
    INACTIVE: 'danger',
    EXPIRED: 'danger',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    DRAFT: '草稿',
    PENDING_APPROVAL: '待审批',
    ACTIVE: '生效中',
    INACTIVE: '已失效',
    EXPIRED: '已过期',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/personalized-quota/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        orgCode: searchForm.orgCode || undefined,
        positionLevel: searchForm.positionLevel || undefined,
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
  searchForm.orgCode = ''
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
  dialogTitle.value = '新增配置'
  Object.assign(formData, {
    id: null,
    orgId: null,
    orgCode: '',
    orgName: '',
    orgLevel: '',
    positionLevel: '',
    customMaxQuota: 0,
    customMinAmplitude: 0,
    customStdAmplitude: 0,
    customMaxAmplitude: 0,
    warningFactor: 0.8,
    effectiveDate: '',
    expireDate: '',
    configDesc: '',
    enabled: false,
    approver: '',
    approvalTime: '',
    approvalResult: '',
    configuredBy: '',
    configureTime: '',
    status: 'DRAFT',
    versionNo: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑配置'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/personalized-quota/${row.id}`)
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
      const res = await axios.post(`/api/personalized-quota/${row.id}/submit`)
      if (res.data.code === 200) {
        ElMessage.success('提交成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '提交失败')
      }
    } catch (e) {
      ElMessage.error('提交失败')
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
      const res = await axios.delete(`/api/personalized-quota/${row.id}`)
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

async function handleSubmitForm() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/personalized-quota/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/personalized-quota', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增配置' ? '新增成功' : '修改成功')
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

function handleApprove(row) {
  ElMessageBox.confirm('确定要审批通过该配置吗？', '审批确认', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.put(`/api/personalized-quota/${row.id}/approve`, { result: 'APPROVED' })
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
      const res = await axios.put(`/api/personalized-quota/${row.id}/reject`, { reason: value })
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

function handleToggle(row) {
  const action = row.enabled ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${action}该配置吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.put(`/api/personalized-quota/${row.id}/toggle`)
      if (res.data.code === 200) {
        ElMessage.success(`${action}成功`)
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
      }
    } catch (e) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

function handleEffective(row) {
  ElMessageBox.confirm('确定要将该配置置为生效吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.put(`/api/personalized-quota/${row.id}/effective`)
      if (res.data.code === 200) {
        ElMessage.success('生效成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '生效失败')
      }
    } catch (e) {
      ElMessage.error('生效失败')
    }
  }).catch(() => {})
}

function handleExpire(row) {
  ElMessageBox.confirm('确定要将该配置置为失效吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post(`/api/personalized-quota/${row.id}/expire`)
      if (res.data.code === 200) {
        ElMessage.success('已失效')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '操作失败')
      }
    } catch (e) {
      ElMessage.error('操作失败')
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

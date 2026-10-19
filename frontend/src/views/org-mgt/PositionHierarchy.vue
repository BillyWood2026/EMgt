<template>
  <div class="page-container">
    <div class="page-header">
      <h2>职务层级</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="list"
          file-name="职务层级"
          title="职务层级"
          import-api="/api/position-hierarchy"
          :field-map="ioFieldMap"
          @done="loadList"
        />
        <el-button type="primary" @click="handleAdd">新增职务层级</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 180px" />
      </el-form-item>
      <el-form-item label="组织层级">
        <el-select v-model="searchForm.orgLevel" placeholder="选择层级" style="width: 150px">
          <el-option label="一级" :value="1" />
          <el-option label="二级" :value="2" />
          <el-option label="三级" :value="3" />
          <el-option label="四级" :value="4" />
          <el-option label="五级" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px">
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="hierarchyCode" label="层级编码" width="140" />
      <el-table-column prop="hierarchyName" label="层级名称" width="160" />
      <el-table-column prop="orgLevel" label="组织层级" width="100" />
      <el-table-column prop="orgName" label="所属组织" width="140" />
      <el-table-column prop="hierarchyGrade" label="职等" width="100" />
      <el-table-column prop="hierarchyStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.hierarchyStatus === 'ENABLED' ? 'success' : 'danger'" size="small">
            {{ row.hierarchyStatus === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="incumbents" label="任职人" width="120" />
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column prop="expireDate" label="失效日期" width="120" />
      <el-table-column prop="isCoreManagement" label="核心管理" width="100">
        <template #default="{ row }">
          {{ row.isCoreManagement === 1 || row.isCoreManagement === true ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="isConcurrent" label="兼任" width="80">
        <template #default="{ row }">
          {{ row.isConcurrent === 1 || row.isConcurrent === true ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="createdBy" label="创建人" width="100" />
      <el-table-column prop="updatedTime" label="更新时间" width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="330" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleAppoint(row)">任免</el-button>
          <el-button size="small" type="info" @click="viewHistory(row)">变更历史</el-button>
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
            <el-form-item label="层级编码" required>
              <el-input v-model="formData.hierarchyCode" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级名称" required>
              <el-input v-model="formData.hierarchyName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织层级">
              <el-select v-model="formData.orgLevel" style="width: 100%">
                <el-option label="一级" :value="1" />
                <el-option label="二级" :value="2" />
                <el-option label="三级" :value="3" />
                <el-option label="四级" :value="4" />
                <el-option label="五级" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职等">
              <el-input v-model="formData.hierarchyGrade" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织编号">
              <el-input-number v-model="formData.orgId" :min="0" style="width: 100%" />
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
            <el-form-item label="状态">
              <el-select v-model="formData.hierarchyStatus" style="width: 100%">
                <el-option label="启用" value="ENABLED" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任职人">
              <el-input v-model="formData.incumbents" />
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
          <el-col :span="8">
            <el-form-item label="核心管理">
              <el-switch v-model="formData.isCoreManagement" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="兼任">
              <el-switch v-model="formData.isConcurrent" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="借调">
              <el-switch v-model="formData.isSeconded" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="职责描述">
          <el-input v-model="formData.dutyDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="职务层级详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="层级编码">{{ detailData.hierarchyCode }}</el-descriptions-item>
        <el-descriptions-item label="层级名称">{{ detailData.hierarchyName }}</el-descriptions-item>
        <el-descriptions-item label="组织层级">{{ detailData.orgLevel }}</el-descriptions-item>
        <el-descriptions-item label="职等">{{ detailData.hierarchyGrade }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.hierarchyStatus === 'ENABLED' ? 'success' : 'danger'" size="small">
            {{ detailData.hierarchyStatus === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任职人">{{ detailData.incumbents }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="核心管理">{{ detailData.isCoreManagement ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="兼任">{{ detailData.isConcurrent ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="职责描述" :span="2">{{ detailData.dutyDescription }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="appointVisible" title="任免操作" width="500px">
      <el-form :model="appointForm" label-width="100px">
        <el-form-item label="层级名称">
          <el-input v-model="appointForm.hierarchyName" disabled />
        </el-form-item>
        <el-form-item label="任职人" required>
          <el-input v-model="appointForm.incumbents" placeholder="请输入任职人，多人用逗号分隔" />
        </el-form-item>
        <el-form-item label="生效日期">
          <el-date-picker
            v-model="appointForm.effectiveDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="任免说明">
          <el-input v-model="appointForm.remark" type="textarea" :rows="3" placeholder="请输入任免说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppoint">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyVisible" :title="`变更历史 - ${historyName}`" width="900px">
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column prop="changeNo" label="变更编号" width="150" />
        <el-table-column prop="changeType" label="变更类型" width="100" />
        <el-table-column prop="beforeContent" label="变更前" min-width="140" show-overflow-tooltip />
        <el-table-column prop="afterContent" label="变更后" min-width="140" show-overflow-tooltip />
        <el-table-column prop="initiator" label="操作人" width="100" />
        <el-table-column prop="approvalTime" label="审批时间" width="160" show-overflow-tooltip />
        <el-table-column prop="operationRecord" label="操作记录" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无变更记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'hierarchyCode', title: '层级编码' },
  { key: 'hierarchyName', title: '层级名称' },
  { key: 'orgLevel', title: '组织层级' },
  { key: 'orgName', title: '所属组织' },
  { key: 'hierarchyGrade', title: '职等' },
  { key: 'incumbents', title: '任职人' },
  { key: 'effectiveDate', title: '生效日期' },
  { key: 'expireDate', title: '失效日期' },
  { key: 'updatedTime', title: '更新时间' }
]
const ioFieldMap = {
  '层级编码': 'hierarchyCode',
  '层级名称': 'hierarchyName',
  '组织层级': 'orgLevel',
  '组织编码': 'orgCode',
  '所属组织': 'orgName',
  '职等': 'hierarchyGrade',
  '任职人': 'incumbents',
  '生效日期': 'effectiveDate',
  '失效日期': 'expireDate',
  '职责描述': 'dutyDescription',
  '排序': 'sortOrder',
  '备注': 'remark'
}

const loading = ref(false)

const searchForm = reactive({
  keyword: '',
  orgLevel: null,
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增职务层级')
const formRef = ref(null)

const formData = reactive({
  id: null,
  hierarchyCode: '',
  hierarchyName: '',
  orgLevel: null,
  orgId: null,
  orgCode: '',
  orgName: '',
  hierarchyGrade: '',
  hierarchyStatus: 'ENABLED',
  incumbents: '',
  effectiveDate: '',
  expireDate: '',
  dutyDescription: '',
  sortOrder: 0,
  isCoreManagement: 0,
  isConcurrent: 0,
  isSeconded: 0,
  remark: ''
})

const detailData = reactive({})
const list = ref([])

const appointVisible = ref(false)
const appointForm = reactive({
  id: null,
  hierarchyName: '',
  incumbents: '',
  effectiveDate: '',
  remark: ''
})

const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])
const historyName = ref('')

function handleAppoint(row) {
  appointForm.id = row.id
  appointForm.hierarchyName = row.hierarchyName
  appointForm.incumbents = row.incumbents || ''
  appointForm.effectiveDate = ''
  appointForm.remark = ''
  appointVisible.value = true
}

async function submitAppoint() {
  try {
    const res = await axios.put(`/api/position-hierarchy/${appointForm.id}/appoint`, {
      incumbents: appointForm.incumbents,
      effectiveDate: appointForm.effectiveDate,
      remark: appointForm.remark
    })
    if (res.data.code === 200) {
      ElMessage.success('任免成功')
      appointVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '任免失败')
    }
  } catch (e) {
    ElMessage.error('任免失败')
    console.error(e)
  }
}

async function viewHistory(row) {
  historyName.value = row.hierarchyName
  historyVisible.value = true
  historyLoading.value = true
  try {
    const res = await axios.get(`/api/position-hierarchy/${row.id}/history`)
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

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/position-hierarchy/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        orgLevel: searchForm.orgLevel || undefined,
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
  searchForm.orgLevel = null
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
  dialogTitle.value = '新增职务层级'
  Object.assign(formData, {
    id: null,
    hierarchyCode: '',
    hierarchyName: '',
    orgLevel: null,
    orgId: null,
    orgCode: '',
    orgName: '',
    hierarchyGrade: '',
    hierarchyStatus: 'ENABLED',
    incumbents: '',
    effectiveDate: '',
    expireDate: '',
    dutyDescription: '',
    sortOrder: 0,
    isCoreManagement: 0,
    isConcurrent: 0,
    isSeconded: 0,
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑职务层级'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/position-hierarchy/${row.id}`)
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
      const res = await axios.delete(`/api/position-hierarchy/${row.id}`)
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
      res = await axios.put(`/api/position-hierarchy/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/position-hierarchy', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增职务层级' ? '新增成功' : '修改成功')
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

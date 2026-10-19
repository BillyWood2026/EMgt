<template>
  <div class="page-container">
    <div class="page-header">
      <h2>组织权限</h2>
      <div>
        <el-button type="success" :disabled="selectedRows.length === 0" @click="batchGrant" style="margin-right: 8px">批量授权</el-button>
        <el-button type="primary" @click="handleAdd">新增权限配置</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="角色编码">
        <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" style="width: 150px" />
      </el-form-item>
      <el-form-item label="模块编码">
        <el-input v-model="searchForm.moduleCode" placeholder="请输入模块编码" style="width: 150px" />
      </el-form-item>
      <el-form-item label="数据范围">
        <el-select v-model="searchForm.dataScope" placeholder="选择范围" style="width: 150px">
          <el-option label="全部" value="ALL" />
          <el-option label="本中心" value="OWN_CENTER" />
          <el-option label="本部门" value="OWN_DEPT" />
          <el-option label="本科室" value="OWN_SECTION" />
          <el-option label="个人" value="PERSONAL" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="roleCode" label="角色编码" width="140" />
      <el-table-column prop="roleName" label="角色名称" width="140" />
      <el-table-column prop="moduleCode" label="模块编码" width="140" />
      <el-table-column prop="moduleName" label="模块名称" width="140" />
      <el-table-column prop="functionCode" label="功能编码" width="140" />
      <el-table-column prop="functionName" label="功能名称" width="140" />
      <el-table-column prop="hasPermission" label="是否有权限" width="100">
        <template #default="{ row }">
          <el-tag :type="row.hasPermission ? 'success' : 'danger'" size="small">
            {{ row.hasPermission ? '有权限' : '无权限' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dataScope" label="数据范围" width="120">
        <template #default="{ row }">
          {{ getDataScopeLabel(row.dataScope) }}
        </template>
      </el-table-column>
      <el-table-column prop="orgLevel" label="组织层级" width="100" />
      <el-table-column prop="createdBy" label="创建人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" v-if="!row.hasPermission" @click="handleGrant(row)">授权</el-button>
          <el-button size="small" type="warning" v-if="row.hasPermission" @click="handleRevoke(row)">撤销</el-button>
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
            <el-form-item label="角色编码" required>
              <el-select v-model="formData.roleCode" placeholder="选择角色" style="width: 100%" :disabled="!!formData.id" @change="onRoleChange">
                <el-option v-for="r in roleOptions" :key="r.roleCode || r" :label="r.roleName ? `${r.roleName}(${r.roleCode})` : r" :value="r.roleCode || r" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称">
              <el-input v-model="formData.roleName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模块编码" required>
              <el-select v-model="formData.moduleCode" placeholder="选择模块" style="width: 100%" @change="onModuleChange">
                <el-option v-for="m in moduleOptions" :key="m.moduleCode || m" :label="m.moduleName ? `${m.moduleName}(${m.moduleCode})` : m" :value="m.moduleCode || m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块名称">
              <el-input v-model="formData.moduleName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="功能编码">
              <el-input v-model="formData.functionCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="功能名称">
              <el-input v-model="formData.functionName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否有权限">
              <el-switch v-model="formData.hasPermission" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据范围">
              <el-select v-model="formData.dataScope" style="width: 100%">
                <el-option label="全部" value="ALL" />
                <el-option label="本中心" value="OWN_CENTER" />
                <el-option label="本部门" value="OWN_DEPT" />
                <el-option label="本科室" value="OWN_SECTION" />
                <el-option label="个人" value="PERSONAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织层级">
            <el-select v-model="formData.orgLevel" style="width: 100%">
              <el-option label="一级" value="L1" />
              <el-option label="二级" value="L2" />
              <el-option label="三级" value="L3" />
              <el-option label="四级" value="L4" />
              <el-option label="五级" value="L5" />
            </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="组织权限详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="角色编码">{{ detailData.roleCode }}</el-descriptions-item>
        <el-descriptions-item label="角色名称">{{ detailData.roleName }}</el-descriptions-item>
        <el-descriptions-item label="模块编码">{{ detailData.moduleCode }}</el-descriptions-item>
        <el-descriptions-item label="模块名称">{{ detailData.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="功能编码">{{ detailData.functionCode }}</el-descriptions-item>
        <el-descriptions-item label="功能名称">{{ detailData.functionName }}</el-descriptions-item>
        <el-descriptions-item label="是否有权限">
          <el-tag :type="detailData.hasPermission ? 'success' : 'danger'" size="small">
            {{ detailData.hasPermission ? '有权限' : '无权限' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据范围">{{ getDataScopeLabel(detailData.dataScope) }}</el-descriptions-item>
        <el-descriptions-item label="组织层级">{{ detailData.orgLevel }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description }}</el-descriptions-item>
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
  roleCode: '',
  moduleCode: '',
  dataScope: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增权限配置')
const formRef = ref(null)

const formData = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  moduleCode: '',
  moduleName: '',
  functionCode: '',
  functionName: '',
  hasPermission: true,
  dataScope: 'ALL',
  orgLevel: '',
  description: ''
})

const detailData = reactive({})
const list = ref([])
const selectedRows = ref([])
const roleOptions = ref([])
const moduleOptions = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function loadRoleOptions() {
  try {
    const res = await axios.get('/api/org-permission/roles')
    if (res.data.code === 200) roleOptions.value = res.data.data || []
  } catch (e) { /* 忽略，回退手输 */ }
}

async function loadModuleOptions() {
  try {
    const res = await axios.get('/api/org-permission/modules')
    if (res.data.code === 200) moduleOptions.value = res.data.data || []
  } catch (e) { /* 忽略，回退手输 */ }
}

function onRoleChange(val) {
  const hit = roleOptions.value.find((r) => (r.roleCode || r) === val)
  if (hit && hit.roleName) formData.roleName = hit.roleName
}

function onModuleChange(val) {
  const hit = moduleOptions.value.find((m) => (m.moduleCode || m) === val)
  if (hit && hit.moduleName) formData.moduleName = hit.moduleName
}

function handleGrant(row) {
  ElMessageBox.confirm(`确定要为角色 ${row.roleCode} 授权功能 ${row.functionCode || row.moduleCode} 吗？`, '授权确认', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post('/api/org-permission/grant', row)
      if (res.data.code === 200) {
        ElMessage.success('授权成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '授权失败')
      }
    } catch (e) {
      ElMessage.error('授权失败')
    }
  }).catch(() => {})
}

function handleRevoke(row) {
  ElMessageBox.confirm(`确定要撤销角色 ${row.roleCode} 对功能 ${row.functionCode || row.moduleCode} 的权限吗？`, '撤销确认', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post('/api/org-permission/revoke', row)
      if (res.data.code === 200) {
        ElMessage.success('撤销成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '撤销失败')
      }
    } catch (e) {
      ElMessage.error('撤销失败')
    }
  }).catch(() => {})
}

function batchGrant() {
  ElMessageBox.confirm(`确定要批量授权选中的 ${selectedRows.value.length} 条配置吗？`, '批量授权', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post('/api/org-permission/batch', { ids: selectedRows.value.map((r) => r.id), action: 'GRANT' })
      if (res.data.code === 200) {
        ElMessage.success('批量授权成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '批量授权失败')
      }
    } catch (e) {
      ElMessage.error('批量授权失败')
    }
  }).catch(() => {})
}

function getDataScopeLabel(scope) {
  const map = {
    ALL: '全部',
    OWN_CENTER: '本中心',
    OWN_DEPT: '本部门',
    OWN_SECTION: '本科室',
    PERSONAL: '个人'
  }
  return map[scope] || scope
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/org-permission/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        roleCode: searchForm.roleCode || undefined,
        moduleCode: searchForm.moduleCode || undefined,
        dataScope: searchForm.dataScope || undefined
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
  searchForm.roleCode = ''
  searchForm.moduleCode = ''
  searchForm.dataScope = ''
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
  dialogTitle.value = '新增权限配置'
  Object.assign(formData, {
    id: null,
    roleCode: '',
    roleName: '',
    moduleCode: '',
    moduleName: '',
    functionCode: '',
    functionName: '',
    hasPermission: true,
    dataScope: 'ALL',
    orgLevel: '',
    description: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑权限配置'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/org-permission/${row.id}`)
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
      const res = await axios.delete(`/api/org-permission/${row.id}`)
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
      res = await axios.put(`/api/org-permission/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/org-permission', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增权限配置' ? '新增成功' : '修改成功')
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
  loadRoleOptions()
  loadModuleOptions()
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

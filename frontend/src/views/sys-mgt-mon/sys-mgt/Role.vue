<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="角色名称">
        <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" style="width:180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="禁用" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="roleList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="roleCategory" label="角色分类" width="120" />
      <el-table-column prop="description" label="角色描述" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
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
      style="margin-top: 20px; text-align: right;"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色分类">
          <el-input v-model="formData.roleCategory" placeholder="请输入角色分类" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="角色详情" width="600px">
      <el-form :model="detailData" label-width="100px" :disabled="true">
        <el-form-item label="角色名称">
          <el-input v-model="detailData.roleName" disabled />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="detailData.roleCode" disabled />
        </el-form-item>
        <el-form-item label="角色分类">
          <el-input v-model="detailData.roleCategory" disabled />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="detailData.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="排序">
          <el-input :value="detailData.sortOrder" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="detailData.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ detailData.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="detailData.createdTime" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const searchForm = reactive({
  roleName: '',
  roleCode: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)

const formData = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  roleCategory: '',
  status: 'ACTIVE',
  sortOrder: 0
})

const detailData = reactive({})

const roleList = ref([])

async function loadRoleList() {
  loading.value = true
  try {
    const res = await axios.get('/api/sys/role/list', {
      params: {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        roleName: searchForm.roleName || undefined,
        roleCode: searchForm.roleCode || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      roleList.value = res.data.data.list
      pagination.total = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载角色列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadRoleList()
}

function resetSearch() {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadRoleList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadRoleList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadRoleList()
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  Object.assign(formData, {
    id: null,
    roleCode: '',
    roleName: '',
    description: '',
    roleCategory: '',
    status: 'ACTIVE',
    sortOrder: 0
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑角色'
  Object.assign(formData, {
    id: row.id,
    roleCode: row.roleCode,
    roleName: row.roleName,
    description: row.description,
    roleCategory: row.roleCategory,
    status: row.status,
    sortOrder: row.sortOrder
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/sys/role/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取角色详情失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/role/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadRoleList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
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
      res = await axios.put(`/api/sys/role/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/sys/role', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增角色' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadRoleList()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadRoleList()
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

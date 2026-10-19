<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入用户名" style="width:180px" />
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" style="width:180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width:150px">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="禁用" value="INACTIVE" />
          <el-option label="锁定" value="LOCKED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="userList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="mobile" label="手机号" width="150" />
      <el-table-column prop="employeeId" label="员工编号" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'INACTIVE' ? 'danger' : 'warning'" size="small">
            {{ row.status === 'ACTIVE' ? '启用' : row.status === 'INACTIVE' ? '禁用' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleResetPwd(row)">重置密码</el-button>
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
        <el-form-item label="用户名" required>
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.defaultRoleId" placeholder="选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
            <el-option label="锁定" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!formData.id" label="密码" required>
          <el-input v-model="formData.passwordHash" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <el-form :model="detailData" label-width="100px" :disabled="true">
        <el-form-item label="用户名">
          <el-input v-model="detailData.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="detailData.realName" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="detailData.email" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="detailData.mobile" disabled />
        </el-form-item>
        <el-form-item label="员工编号">
          <el-input v-model="detailData.employeeId" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="detailData.status === 'ACTIVE' ? 'success' : detailData.status === 'INACTIVE' ? 'danger' : 'warning'" size="small">
            {{ detailData.status === 'ACTIVE' ? '启用' : detailData.status === 'INACTIVE' ? '禁用' : '锁定' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="detailData.createdTime" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="新密码" required>
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const searchForm = reactive({
  username: '',
  realName: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const pwdVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)

const formData = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  mobile: '',
  defaultRoleId: null,
  status: 'ACTIVE',
  passwordHash: ''
})

const detailData = reactive({})
const pwdForm = reactive({ userId: null, newPassword: '' })

const roles = ref([])
const userList = ref([])

async function loadRoles() {
  try {
    const res = await axios.get('/api/sys/user/roles')
    if (res.data.code === 200) {
      roles.value = res.data.data
    }
  } catch (e) {
    console.error('加载角色失败', e)
  }
}

async function loadUserList() {
  loading.value = true
  try {
    const res = await axios.get('/api/sys/user/list', {
      params: {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        username: searchForm.username || undefined,
        realName: searchForm.realName || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      userList.value = res.data.data.list
      pagination.total = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载用户列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadUserList()
}

function resetSearch() {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadUserList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadUserList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadUserList()
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  Object.assign(formData, {
    id: null,
    username: '',
    realName: '',
    email: '',
    mobile: '',
    defaultRoleId: null,
    status: 'ACTIVE',
    passwordHash: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    email: row.email,
    mobile: row.mobile,
    defaultRoleId: row.defaultRoleId || null,
    status: row.status,
    passwordHash: ''
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/sys/user/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/user/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadUserList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

function handleResetPwd(row) {
  pwdForm.userId = row.id
  pwdForm.newPassword = ''
  pwdVisible.value = true
}

async function submitResetPwd() {
  if (!pwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    const res = await axios.put(`/api/sys/user/${pwdForm.userId}/password`, {
      newPassword: pwdForm.newPassword
    })
    if (res.data.code === 200) {
      ElMessage.success('密码重置成功')
      pwdVisible.value = false
    } else {
      ElMessage.error(res.data.message || '密码重置失败')
    }
  } catch (e) {
    ElMessage.error('密码重置失败')
  }
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/sys/user/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/sys/user', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增用户' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadRoles()
  loadUserList()
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

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">用户全生命周期管理 · 失败5次锁定15分钟 · 密码超90天预警 · 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-input v-model="q.accountLike" placeholder="账号" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-input v-model="q.nameLike" placeholder="姓名" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-select v-model="q.status" placeholder="全部状态" clearable size="small" style="width:120px">
          <el-option label="启用" value="启用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02')" size="small" @click="importVisible = true">批量导入</el-button>
        <el-button v-if="can('R02')" size="small" type="primary" @click="openEdit(null)">新增用户</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" show-overflow-tooltip />
        <el-table-column prop="orgName" label="所属组织" width="130" show-overflow-tooltip />
        <el-table-column prop="position" label="岗位" width="110" show-overflow-tooltip />
        <el-table-column prop="roleCodes" label="角色" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
            <el-tooltip v-if="isLocked(row)" :content="'锁定至 ' + fmt(row.lockUntil)"><el-tag size="small" type="danger" style="margin-left:4px">锁</el-tag></el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="150">
          <template #default="{ row }">{{ fmt(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02')" size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('R02')" size="small" link :type="row.status === '启用' ? 'danger' : 'success'" @click="toggle(row)">{{ row.status === '启用' ? '停用' : '启用' }}</el-button>
            <el-button v-if="can('R02')" size="small" link type="warning" @click="resetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="q.pageNum" v-model:page-size="q.pageSize"
        :total="total" layout="total, prev, pager, next" size="small"
        style="margin-top:10px; justify-content:flex-end" @current-change="load" />
    </el-card>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="账号" required><el-input v-model="form.account" :disabled="!!form.id" placeholder="登录账号" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" required>
          <el-input v-model="form.pwdHash" type="password" show-password placeholder=">=8位，含大小写字母/数字/特殊字符" />
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="组织ID"><el-input-number v-model="form.orgId" :min="1" style="width:160px" /></el-form-item>
        <el-form-item label="组织名称"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="岗位"><el-input v-model="form.position" /></el-form-item>
        <el-form-item label="角色编码"><el-input v-model="form.roleCodes" placeholder="如 R02,R07（逗号分隔）" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="批量导入用户" width="640px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:8px"
        title='粘贴 JSON 数组，如 [{"account":"zhangsan","name":"张三","pwdHash":"Abc@12345","orgName":"第一党支部","roleCodes":"R99"}]，逐行校验并返回失败明细' />
      <el-input v-model="importText" type="textarea" :rows="10" spellcheck="false" style="font-family:monospace" />
      <div v-if="importResult" style="margin-top:8px">
        <el-tag size="small" type="success">成功 {{ importResult.ok }}</el-tag>
        <el-tag size="small" type="danger" style="margin-left:6px">失败 {{ importResult.fail }}</el-tag>
        <div v-for="(e, i) in importResult.errors" :key="i" style="color:#f56c6c; font-size:12px; margin-top:4px">{{ e }}</div>
      </div>
      <template #footer>
        <el-button size="small" @click="importVisible = false">关闭</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doImport">执行导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/system/user'
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const q = reactive({ accountLike: '', nameLike: '', status: '', pageNum: 1, pageSize: 10 })

const editVisible = ref(false)
const importVisible = ref(false)
const importText = ref('')
const importResult = ref(null)
const empty = { id: null, account: '', name: '', pwdHash: '', phone: '', orgId: 1, orgName: '', position: '', roleCodes: 'R99', remark: '' }
const form = reactive({ ...empty })

const fmt = t => (t || '').replace('T', ' ').slice(0, 19) || '—'
const isLocked = row => row.lockUntil && new Date(row.lockUntil) > new Date()

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { ...q, accountLike: q.accountLike || undefined, nameLike: q.nameLike || undefined, status: q.status || undefined } })
    rows.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}
function openEdit(row) {
  Object.assign(form, empty, row ? { ...row, pwdHash: '' } : {})
  editVisible.value = true
}
async function save() {
  saving.value = true
  try {
    if (form.id) {
      await axios.put(`${api}/${form.id}`, form)
      ElMessage.success('已保存')
    } else {
      await axios.post(api, form)
      ElMessage.success('已创建')
    }
    editVisible.value = false
    load()
  } catch {} finally { saving.value = false }
}
async function toggle(row) {
  await ElMessageBox.confirm(`确认${row.status === '启用' ? '停用' : '启用'}用户「${row.name}」？`, '提示', { type: 'warning' })
  await axios.post(`${api}/${row.id}/toggle`)
  ElMessage.success('已切换状态')
  load()
}
async function resetPwd(row) {
  await ElMessageBox.confirm(`确认重置「${row.name}」的密码？`, '重置密码', { type: 'warning' })
  const res = await axios.post(`${api}/${row.id}/reset-pwd`)
  ElMessageBox.alert(`新密码：${res.data.data.newPassword}`, '重置成功', { confirmButtonText: '我已记录' })
  load()
}
async function doImport() {
  let list
  try { list = JSON.parse(importText.value) } catch { ElMessage.error('JSON 格式错误'); return }
  if (!Array.isArray(list)) { ElMessage.error('必须是 JSON 数组'); return }
  saving.value = true
  try {
    const res = await axios.post(`${api}/import`, list)
    importResult.value = res.data.data
    load()
  } catch {} finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
</style>

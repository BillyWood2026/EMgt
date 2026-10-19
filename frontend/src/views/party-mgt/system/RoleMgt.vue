<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">角色与三维权限（页面/按钮/字段）· 数据范围控制 · 被引用角色禁止删除（E-SY-07）· 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-input v-model="q.roleCodeLike" placeholder="角色编码" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-input v-model="q.roleNameLike" placeholder="角色名称" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-select v-model="q.status" placeholder="全部状态" clearable size="small" style="width:120px">
          <el-option label="启用" value="启用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02')" size="small" type="primary" @click="openEdit(null)">新增角色</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleCode" label="角色编码" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="150" show-overflow-tooltip />
        <el-table-column prop="dataScope" label="数据范围" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.dataScope === '全部' ? 'danger' : row.dataScope === '本组织' ? 'warning' : 'info'">{{ row.dataScope }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pagePerms" label="页面权限" min-width="150" show-overflow-tooltip />
        <el-table-column prop="buttonPerms" label="按钮权限" min-width="130" show-overflow-tooltip />
        <el-table-column prop="fieldPerms" label="字段权限" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02')" size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('R02')" size="small" link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" :title="form.id ? '编辑角色' : '新增角色'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="角色编码" required><el-input v-model="form.roleCode" :disabled="!!form.id" placeholder="如 R15" /></el-form-item>
        <el-form-item label="角色名称" required><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" style="width:180px">
            <el-option label="全部" value="全部" />
            <el-option label="本组织" value="本组织" />
            <el-option label="本人" value="本人" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:180px">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="页面权限"><el-input v-model="form.pagePerms" type="textarea" :rows="2" placeholder='JSON数组，如 ["user","role"]' /></el-form-item>
        <el-form-item label="按钮权限"><el-input v-model="form.buttonPerms" type="textarea" :rows="2" placeholder='JSON数组，如 ["user:create","user:reset-pwd"]' /></el-form-item>
        <el-form-item label="字段权限"><el-input v-model="form.fieldPerms" type="textarea" :rows="2" placeholder='JSON数组，如 ["phone:masked"]' /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
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

const api = '/api/party/system/role'
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const q = reactive({ roleCodeLike: '', roleNameLike: '', status: '' })

const editVisible = ref(false)
const empty = { id: null, roleCode: '', roleName: '', dataScope: '本组织', status: '启用', pagePerms: '[]', buttonPerms: '[]', fieldPerms: '[]', remark: '' }
const form = reactive({ ...empty })

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { roleCodeLike: q.roleCodeLike || undefined, roleNameLike: q.roleNameLike || undefined, status: q.status || undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function validJson(s) { try { const v = JSON.parse(s || '[]'); return Array.isArray(v) } catch { return false } }
function openEdit(row) {
  Object.assign(form, empty, row || {})
  editVisible.value = true
}
async function save() {
  for (const f of ['pagePerms', 'buttonPerms', 'fieldPerms']) {
    if (!validJson(form[f])) { ElMessage.error(`${f} 必须是 JSON 数组`); return }
  }
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
async function del(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？被引用的角色将被阻断（E-SY-07）`, '提示', { type: 'warning' })
  await axios.delete(`${api}/${row.id}`)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">组织岗位任职管理 · 同组织同职务在任冲突校验（E-SY-08）· 兼职标识 · 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-input v-model="q.positionName" placeholder="职务名称" size="small" clearable style="width:140px" @keyup.enter="load" />
        <el-select v-model="q.status" placeholder="全部状态" clearable size="small" style="width:120px">
          <el-option label="在任" value="在任" />
          <el-option label="离任" value="离任" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02')" size="small" type="primary" @click="openEdit(null)">新增任职</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orgName" label="所属组织" min-width="150" show-overflow-tooltip />
        <el-table-column prop="positionName" label="职务名称" width="130" />
        <el-table-column prop="memberName" label="任职人员" width="120" />
        <el-table-column label="兼职" width="70">
          <template #default="{ row }">
            <el-tag size="small" :type="row.partTime ? 'warning' : 'info'">{{ row.partTime ? '兼职' : '本职' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="任职日期" width="120" />
        <el-table-column prop="endDate" label="离任日期" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '在任' ? 'success' : 'info'">{{ row.status }}</el-tag>
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

    <el-dialog v-model="editVisible" :title="form.id ? '编辑任职' : '新增任职'" width="480px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="组织ID"><el-input-number v-model="form.orgId" :min="1" style="width:160px" /></el-form-item>
        <el-form-item label="组织名称"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="职务名称" required><el-input v-model="form.positionName" /></el-form-item>
        <el-form-item label="人员ID"><el-input-number v-model="form.memberId" :min="1" style="width:160px" /></el-form-item>
        <el-form-item label="人员姓名"><el-input v-model="form.memberName" /></el-form-item>
        <el-form-item label="是否兼职"><el-switch v-model="form.partTime" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="任职日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:160px" /></el-form-item>
        <el-form-item label="离任日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:160px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:160px">
            <el-option label="在任" value="在任" />
            <el-option label="离任" value="离任" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
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

const api = '/api/party/system/position'
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const q = reactive({ positionName: '', status: '' })

const editVisible = ref(false)
const empty = { id: null, orgId: 1, orgName: '', positionName: '', memberId: null, memberName: '', partTime: 0, startDate: '', endDate: '', status: '在任', remark: '' }
const form = reactive({ ...empty })

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { positionName: q.positionName || undefined, status: q.status || undefined } })
    rows.value = res.data.data || []
  } finally { loading.value = false }
}
function openEdit(row) {
  Object.assign(form, empty, row || {})
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
async function del(row) {
  await ElMessageBox.confirm(`确认删除任职「${row.positionName} / ${row.memberName}」？`, '提示', { type: 'warning' })
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

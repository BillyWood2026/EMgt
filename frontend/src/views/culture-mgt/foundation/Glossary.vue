<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">文化专员(C02/C03)编制 → 集团文化管理员(C03)审核生效；释义标签供案例/培训引用统计</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="q.valueTag" placeholder="价值观标签" size="small" clearable style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03')" size="small" type="primary" @click="openCreate">新增释义</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="500">
        <el-table-column prop="valueTag" label="价值观标签" width="150" />
        <el-table-column prop="positiveText" label="正向释义" min-width="240" show-overflow-tooltip />
        <el-table-column prop="negativeText" label="反面警示" min-width="180" show-overflow-tooltip />
        <el-table-column prop="applyOrg" label="适用组织" width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03') && (row.status==='草稿'||row.status==='归档')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03') && (row.status==='草稿'||row.status==='归档')" size="small" type="primary" @click="doSubmit(row)">提交</el-button>
            <el-button v-if="can('C03') && row.status==='待审'" size="small" type="warning" @click="openApprove(row)">审核</el-button>
            <el-button v-if="can('C03') && row.status==='生效'" size="small" type="danger" @click="doArchive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id?'编辑释义':'新增释义'" width="640px">
      <el-form :model="form" label-width="92px" size="small">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="价值观标签" required><el-input v-model="form.valueTag" maxlength="64" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用组织" required><el-input v-model="form.applyOrg" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="正向释义" required><el-input v-model="form.positiveText" type="textarea" :rows="3" maxlength="2000" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="反面警示"><el-input v-model="form.negativeText" type="textarea" :rows="2" maxlength="2000" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="文化FAQ"><el-input v-model="form.faq" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="apVis" title="释义审核" width="480px">
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="驳回时须填写不少于5字的意见" />
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="danger" @click="doApprove(false)">驳回</el-button>
        <el-button size="small" type="primary" @click="doApprove(true)">审核通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const STATUS = ['草稿', '待审', '生效', '归档']
const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', valueTag: '' })
const formVis = ref(false)
const form = reactive({ id: null, valueTag: '', positiveText: '', negativeText: '', faq: '', applyOrg: '' })
const apVis = ref(false)
const apId = ref(null)
const opinion = ref('')

function statusType(s) {
  if (s === '生效') return 'success'
  if (s === '待审') return 'warning'
  if (s === '归档') return 'info'
  return ''
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/foundation/glossary/list', { params: { status: q.status || undefined, valueTag: q.valueTag || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, valueTag: '', positiveText: '', negativeText: '', faq: '', applyOrg: '' })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, valueTag: row.valueTag, positiveText: row.positiveText, negativeText: row.negativeText, faq: row.faq, applyOrg: row.applyOrg })
  formVis.value = true
}
async function doSave() {
  await axios({ url: form.id ? '/api/culture/foundation/glossary/' + form.id : '/api/culture/foundation/glossary', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await axios.post(`/api/culture/foundation/glossary/${row.id}/submit`)
  ElMessage.success('已提交审核')
  load()
}
function openApprove(row) {
  apId.value = row.id
  opinion.value = ''
  apVis.value = true
}
async function doApprove(pass) {
  await axios.post(`/api/culture/foundation/glossary/${apId.value}/approve`, null, { params: { pass, opinion: opinion.value } })
  ElMessage.success(pass ? '已生效' : '已驳回')
  apVis.value = false
  load()
}
async function doArchive(row) {
  await axios.post(`/api/culture/foundation/glossary/${row.id}/archive`)
  ElMessage.success('已归档')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">企业发展史里程碑｜文化岗维护，仅启用且"前台展示"的记录进入文化全景</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="启用" value="启用" />
          <el-option label="归档" value="归档" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03')" size="small" type="primary" @click="openCreate">新增里程碑</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="500">
        <el-table-column prop="occurDate" label="发生时间" width="110" />
        <el-table-column prop="title" label="事件标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="事件描述" min-width="260" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" width="160" show-overflow-tooltip />
        <el-table-column label="前台展示" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="row.showFront===1?'success':'info'">{{ row.showFront === 1 ? '展示' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag size="small" :type="row.status==='启用'?'success':'info'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03') && row.status==='启用'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03') && row.showFront===1" size="small" @click="toggle(row,'frontOff')">取消前台</el-button>
            <el-button v-if="can('C02','C03') && row.showFront!==1 && row.status==='启用'" size="small" type="success" @click="toggle(row,'frontOn')">前台展示</el-button>
            <el-button v-if="can('C02','C03') && row.status==='启用'" size="small" type="warning" @click="toggle(row,'archive')">归档</el-button>
            <el-button v-if="can('C02','C03') && row.status==='归档'" size="small" @click="toggle(row,'enable')">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id?'编辑里程碑':'新增里程碑'" width="560px">
      <el-form :model="form" label-width="88px" size="small">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="发生时间" required><el-date-picker v-model="form.occurDate" type="date" value-format="YYYY-MM-DD" :disabled-date="futureOnly" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="前台展示"><el-switch v-model="form.showFront" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="事件标题" required><el-input v-model="form.title" maxlength="120" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="事件描述" required><el-input v-model="form.content" type="textarea" :rows="3" maxlength="2000" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签逗号分隔" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="配图URL"><el-input v-model="form.imageUrl" placeholder="图片附件地址（可后续由资产库补充）" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
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

const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '' })
const formVis = ref(false)
const form = reactive({ id: null, occurDate: '', title: '', content: '', tags: '', imageUrl: '', showFront: 1 })

function futureOnly(d) {
  return d.getTime() > Date.now()
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/foundation/milestone/list', { params: { status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, occurDate: '', title: '', content: '', tags: '', imageUrl: '', showFront: 1 })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, occurDate: row.occurDate, title: row.title, content: row.content, tags: row.tags, imageUrl: row.imageUrl, showFront: row.showFront })
  formVis.value = true
}
async function doSave() {
  await axios({ url: form.id ? '/api/culture/foundation/milestone/' + form.id : '/api/culture/foundation/milestone', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function toggle(row, action) {
  await axios.post(`/api/culture/foundation/milestone/${row.id}/toggle`, null, { params: { action } })
  ElMessage.success('操作成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
</style>

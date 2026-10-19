<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">敏感词检测 · 自动摘要 · 状态机流转</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.columnId" placeholder="全部栏目" clearable size="small" filterable style="width:200px" @change="load">
          <el-option v-for="c in columns" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filter.status" placeholder="全部状态" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="filter.keyword" placeholder="标题/作者" size="small" style="width:180px" clearable @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <el-button v-if="can('R02','R04','R13')" size="small" type="primary" @click="openForm()">新建稿件</el-button>
      </div>

      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="栏目" width="110">
          <template #default="{ row }">{{ colName(row.columnId) }}</template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="90" />
        <el-table-column label="敏感词" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.sensitiveStatus === '命中' ? 'danger' : (row.sensitiveStatus === '已处理' ? 'success' : 'info')">
              {{ row.sensitiveStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读" width="70" />
        <el-table-column prop="likeCount" label="点赞" width="70" />
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04','R13') && (row.status==='草稿'||row.status==='已退回')" size="small" link @click="openForm(row)">编辑</el-button>
            <el-button v-if="can('R02','R04','R13') && (row.status==='草稿'||row.status==='已退回')" size="small" link type="success" @click="submitReview(row)">提交审核</el-button>
            <el-button v-if="can('R02','R04') && row.status==='待审'" size="small" link type="primary" @click="publish(row)">发布</el-button>
            <el-button v-if="can('R02','R04') && row.status==='已发布'" size="small" link type="warning" @click="offline(row)">下线</el-button>
            <el-button v-if="can('R02','R03') && (row.status==='已发布'||row.status==='已下线')" size="small" link type="danger" @click="withdraw(row)">撤稿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑稿件' : '新建稿件'" width="720px" top="4vh">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="栏目" required>
          <el-select v-model="form.columnId" filterable style="width:100%">
            <el-option v-for="c in columns" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="来源"><el-input v-model="form.source" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="逗号分隔，如 党建,会议" /></el-form-item>
        <el-form-item label="正文" required><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImage" placeholder="图片URL" /></el-form-item>
        <el-form-item label="附件"><el-input v-model="form.attachment" placeholder="附件URL" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存草稿</el-button>
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

const API = '/api/party/news/article'
const COL_API = '/api/party/news/column'
const statuses = ['草稿', '待审', '已发布', '已退回', '已下线']
const columns = ref([])
const list = ref([])
const loading = ref(false)
const filter = reactive({ columnId: null, status: null, keyword: '' })
const formVisible = ref(false)
const form = reactive({})

function statusType(s) {
  return { 草稿: 'info', 待审: 'warning', 已发布: 'success', 已退回: 'danger', 已下线: 'info' }[s] || 'info'
}
function colName(id) { const c = columns.value.find(x => x.id === id); return c ? c.name : '-' }

async function loadColumns() {
  const res = await axios.get(`${COL_API}/list`)
  if (res.data.code === 200) columns.value = res.data.data
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: { ...filter, keyword: filter.keyword || undefined } })
    if (res.data.code === 200) list.value = res.data.data
  } finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach(k => delete form[k])
  if (row) Object.assign(form, JSON.parse(JSON.stringify(row)))
  else Object.assign(form, { status: '草稿' })
  formVisible.value = true
}
async function save() {
  if (!form.title) { ElMessage.warning('标题必填'); return }
  // 自动摘要
  if (!form.summary && form.content) {
    const text = form.content.replace(/<[^>]+>/g, '').trim()
    form.summary = text.length > 120 ? text.substring(0, 120) : text
  }
  try {
    await axios.post(API, form)
    ElMessage.success('草稿已保存')
    formVisible.value = false
    load()
  } catch {}
}
async function submitReview(row) {
  try { await axios.post(`${API}/${row.id}/submit`); ElMessage.success('已提交审核'); load() } catch {}
}
async function publish(row) {
  try { await axios.post(`${API}/${row.id}/publish`); ElMessage.success('已发布'); load() } catch {}
}
async function offline(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入下线原因', '下线确认', { inputPlaceholder: '下线原因' })
    await axios.post(`${API}/${row.id}/offline`, null, { params: { reason: value } })
    ElMessage.success('已下线'); load()
  } catch {}
}
async function withdraw(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入撤稿原因', '撤稿确认', { inputPlaceholder: '撤稿原因（必填）' })
    if (!value) return
    await axios.post(`${API}/${row.id}/withdraw`, null, { params: { reason: value } })
    ElMessage.success('已撤稿'); load()
  } catch {}
}
onMounted(() => { loadColumns(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
</style>

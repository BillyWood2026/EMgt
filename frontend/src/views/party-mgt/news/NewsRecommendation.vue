<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">置顶上限3条 · 到期自动取消 · 时间重叠提示</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.columnId" placeholder="全部栏目" clearable size="small" filterable style="width:180px" @change="load">
          <el-option v-for="c in columns" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filter.recType" placeholder="全部类型" clearable size="small" style="width:120px" @change="load">
          <el-option label="置顶" value="置顶" />
          <el-option label="推荐" value="推荐" />
        </el-select>
        <el-select v-model="filter.status" placeholder="全部状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="生效中" value="生效中" />
          <el-option label="已取消" value="已取消" />
          <el-option label="已过期" value="已过期" />
        </el-select>
        <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openForm()">新增推荐</el-button>
        <el-button v-if="can('R02')" size="small" @click="expire">到期自动取消</el-button>
      </div>

      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="articleTitle" label="稿件标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="栏目" width="110"><template #default="{ row }">{{ colName(row.columnId) }}</template></el-table-column>
        <el-table-column prop="recType" label="类型" width="80">
          <template #default="{ row }"><el-tag size="small" :type="row.recType==='置顶'?'warning':'success'">{{ row.recType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="rankScore" label="排序值" width="80" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="row.status==='生效中'?'success':'info'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04') && row.status==='生效中'" size="small" link type="danger" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" title="设置推荐/置顶" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择稿件" required>
          <el-select v-model="form.articleId" filterable style="width:100%">
            <el-option v-for="a in articles" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐类型" required>
          <el-radio-group v-model="form.recType">
            <el-radio value="置顶">置顶</el-radio>
            <el-radio value="推荐">推荐</el-radio>
          </el-radio-group>
          <span class="tip">同栏目置顶上限3条</span>
        </el-form-item>
        <el-form-item label="栏目"><el-select v-model="form.columnId" filterable style="width:100%"><el-option v-for="c in columns" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="排序值"><el-input-number v-model="form.rankScore" :min="0" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
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

const API = '/api/party/news/recommendation'
const ART_API = '/api/party/news/article'
const COL_API = '/api/party/news/column'
const columns = ref([])
const articles = ref([])
const list = ref([])
const loading = ref(false)
const filter = reactive({ columnId: null, recType: null, status: null })
const formVisible = ref(false)
const form = reactive({})

function colName(id) { const c = columns.value.find(x => x.id === id); return c ? c.name : '-' }

async function loadColumns() {
  const res = await axios.get(`${COL_API}/list`)
  if (res.data.code === 200) columns.value = res.data.data
}
async function loadArticles() {
  const res = await axios.get(`${ART_API}/list`, { params: { status: '已发布' } })
  if (res.data.code === 200) articles.value = res.data.data
}
async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: { ...filter } })
    if (res.data.code === 200) list.value = res.data.data
  } finally { loading.value = false }
}
function openForm() {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { recType: '置顶', rankScore: 100, startTime: new Date(), endTime: null })
  formVisible.value = true
}
async function submit() {
  if (!form.articleId || !form.recType) { ElMessage.warning('稿件和类型必填'); return }
  try {
    await axios.post(API, form)
    ElMessage.success('设置成功')
    formVisible.value = false
    load()
  } catch {}
}
async function cancel(row) {
  try {
    await ElMessageBox.confirm(`确认取消「${row.articleTitle}」的${row.recType}？`, '提示', { type: 'warning' })
    await axios.post(`${API}/${row.id}/cancel`)
    ElMessage.success('已取消')
    load()
  } catch {}
}
async function expire() {
  try {
    const res = await axios.post(`${API}/expire`)
    ElMessage.success(`已处理 ${res.data.data} 条过期推荐`)
    load()
  } catch {}
}
onMounted(() => { loadColumns(); loadArticles(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #e6a23c; font-size: 12px; margin-left: 8px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">官网 · 公众号 · 移动端 · 失败重试3次</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.articleId" placeholder="全部稿件" clearable size="small" filterable style="width:220px" @change="load">
          <el-option v-for="a in articles" :key="a.id" :label="a.title" :value="a.id" />
        </el-select>
        <el-select v-model="filter.status" placeholder="全部状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="待同步" value="待同步" />
          <el-option label="已同步" value="已同步" />
          <el-option label="同步失败" value="同步失败" />
        </el-select>
        <el-select v-model="filter.channelType" placeholder="全部渠道" clearable size="small" style="width:120px" @change="load">
          <el-option label="官网" value="官网" />
          <el-option label="公众号" value="公众号" />
          <el-option label="移动端" value="移动端" />
        </el-select>
        <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openSync">同步到多端</el-button>
        <el-button v-if="can('R02','R04')" size="small" type="warning" @click="retryAll">重试失败同步</el-button>
      </div>

      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="articleTitle" label="稿件标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道" width="110" />
        <el-table-column prop="channelType" label="渠道类型" width="90">
          <template #default="{ row }"><el-tag size="small">{{ row.channelType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="90" />
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="URL" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.url" :href="row.url" target="_blank">{{ row.url }}</a>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试" width="60" />
        <el-table-column prop="lastError" label="错误信息" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04') && row.status==='同步失败'" size="small" link type="warning" @click="retryOne(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="syncVisible" title="同步到多端" width="480px">
      <el-form label-width="100px">
        <el-form-item label="选择稿件" required>
          <el-select v-model="syncArticleId" filterable style="width:100%">
            <el-option v-for="a in articles" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
        </el-form-item>
        <div class="tip">将自动同步到：门户网站、微信公众号、移动端APP</div>
      </el-form>
      <template #footer>
        <el-button @click="syncVisible=false">取消</el-button>
        <el-button type="primary" @click="doSync">开始同步</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/news/publish'
const ART_API = '/api/party/news/article'
const articles = ref([])
const list = ref([])
const loading = ref(false)
const filter = reactive({ articleId: null, status: null, channelType: null })
const syncVisible = ref(false)
const syncArticleId = ref(null)

function statusType(s) {
  return { 待同步: 'warning', 已同步: 'success', 同步失败: 'danger' }[s] || 'info'
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
function openSync() { syncArticleId.value = null; syncVisible.value = true }
async function doSync() {
  if (!syncArticleId.value) { ElMessage.warning('请选择稿件'); return }
  try {
    await axios.post(`${API}/sync/${syncArticleId.value}`)
    ElMessage.success('同步任务已发起')
    syncVisible.value = false
    load()
  } catch {}
}
async function retryOne(row) {
  try {
    await axios.post(`${API}/retry`)
    ElMessage.success('重试完成')
    load()
  } catch {}
}
async function retryAll() {
  try {
    const res = await axios.post(`${API}/retry`)
    ElMessage.success(`重试成功 ${res.data.data} 条`)
    load()
  } catch {}
}
onMounted(() => { loadArticles(); load() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.tip { color: #909399; font-size: 13px; padding-left: 100px; }
</style>

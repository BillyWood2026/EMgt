<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">文化公告栏：企业公告的编辑、发布、置顶与生效时间管理</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" class="mt">
      <el-tab-pane label="公告管理" name="admin">
        <div class="bar">
          <el-input v-model="query.keyword" size="small" placeholder="搜索标题" clearable style="width:200px" @change="load" />
          <el-select v-model="query.status" size="small" placeholder="全部状态" clearable style="width:120px" @change="load">
            <el-option label="草稿" value="草稿" />
            <el-option label="已发布" value="已发布" />
            <el-option label="已下线" value="已下线" />
          </el-select>
          <el-button size="small" @click="load">查询</el-button>
          <el-button v-if="canEdit" size="small" type="primary" @click="openCreate">新建公告</el-button>
        </div>

        <el-table :data="rows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
            <template #default="{row}">
              <el-tag v-if="row.topFlag" size="small" type="danger" effect="plain" style="margin-right:4px">置顶</el-tag>
              {{ row.title }}
            </template>
          </el-table-column>
          <el-table-column prop="effectTime" label="生效时间" width="150" />
          <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '草稿'" size="small" link @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === '草稿'" size="small" type="primary" link @click="doPublish(row)">发布</el-button>
              <el-button v-if="row.status === '已发布'" size="small" type="warning" link @click="doOffline(row)">下线</el-button>
              <el-button size="small" link @click="doView(row)">预览</el-button>
              <el-popconfirm v-if="row.status === '草稿'" title="确定删除该公告？" @confirm="doDelete(row)">
                <template #reference>
                  <el-button size="small" type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="公告栏" name="public">
        <div class="bar">
          <el-button size="small" @click="loadPublic">刷新</el-button>
        </div>
        <div v-loading="loading" class="mt">
          <el-card v-for="b in publicRows" :key="b.id" class="bulletin-card" :class="{ top: b.topFlag }">
            <div class="bulletin-header">
              <el-tag v-if="b.topFlag" size="small" type="danger" effect="dark">置顶</el-tag>
              <span class="bulletin-title" @click="doView(b)">{{ b.title }}</span>
            </div>
            <div class="bulletin-meta">
              <span>生效：{{ b.effectTime }}</span>
              <span>浏览：{{ b.viewCount }}</span>
            </div>
            <div class="bulletin-content" v-html="b.content" />
          </el-card>
          <el-empty v-if="!loading && publicRows.length === 0" description="暂无生效中的公告" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="showForm" :title="form.id ? '编辑公告' : '新建公告'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="200" placeholder="2-200字" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="富文本内容（支持HTML）" />
        </el-form-item>
        <el-form-item label="生效时间" required>
          <el-date-picker v-model="form.effectTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择生效时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showForm=false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览抽屉 -->
    <el-drawer v-model="showRead" :title="reading ? reading.title : ''" size="640px">
      <div v-if="reading" class="read-body">
        <div class="read-meta">
          <span>生效：{{ reading.effectTime }}</span>
          <span>浏览：{{ reading.viewCount }}</span>
        </div>
        <el-divider />
        <div class="read-content" v-html="reading.content" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const tab = ref('admin')
const loading = ref(false)
const rows = ref([])
const publicRows = ref([])
const query = reactive({ keyword: '', status: '' })

const canEdit = computed(() => can('C02', 'C03', 'C99'))

const showForm = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', content: '', effectTime: '', topFlag: 0 })

const showRead = ref(false)
const reading = ref(null)

function statusType(s) {
  if (s === '草稿') return 'info'
  if (s === '已发布') return 'success'
  return 'danger'
}

function load() {
  loading.value = true
  const params = new URLSearchParams()
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.status) params.set('status', query.status)
  axios.get(`/api/culture/bulletin/list?${params}`)
    .then(r => { rows.value = r.data.data || [] })
    .finally(() => { loading.value = false })
}

function loadPublic() {
  loading.value = true
  axios.get('/api/culture/bulletin/public')
    .then(r => { publicRows.value = r.data.data || [] })
    .finally(() => { loading.value = false })
}

function openCreate() {
  Object.assign(form, { id: null, title: '', content: '', effectTime: '', topFlag: 0 })
  showForm.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, title: row.title, content: row.content, effectTime: row.effectTime, topFlag: row.topFlag })
  showForm.value = true
}

function doSave() {
  saving.value = true
  axios.post('/api/culture/bulletin', { ...form })
    .then(r => {
      ElMessage.success(r.data.message || '保存成功')
      showForm.value = false
      load()
    })
    .catch(e => ElMessage.error(e.response?.data?.message || '保存失败'))
    .finally(() => { saving.value = false })
}

function doPublish(row) {
  axios.post(`/api/culture/bulletin/${row.id}/publish`)
    .then(() => { ElMessage.success('已发布'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '发布失败'))
}

function doOffline(row) {
  axios.post(`/api/culture/bulletin/${row.id}/offline`)
    .then(() => { ElMessage.success('已下线'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '下线失败'))
}

function doDelete(row) {
  axios.delete(`/api/culture/bulletin/${row.id}`)
    .then(() => { ElMessage.success('已删除'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '删除失败'))
}

function doView(row) {
  axios.post(`/api/culture/bulletin/${row.id}/view`)
    .then(r => {
      reading.value = r.data.data
      showRead.value = true
      if (tab.value === 'admin') load(); else loadPublic()
    })
}

onMounted(() => { load(); loadPublic() })
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.bulletin-card { margin-bottom: 12px; }
.bulletin-card.top { border-left: 3px solid #f56c6c; }
.bulletin-header { display: flex; align-items: center; gap: 8px; }
.bulletin-title { font-size: 15px; font-weight: 600; color: #303133; cursor: pointer; }
.bulletin-title:hover { color: #409eff; }
.bulletin-meta { display: flex; gap: 16px; color: #909399; font-size: 12px; margin-top: 8px; }
.bulletin-content { margin-top: 8px; line-height: 1.6; color: #606266; }
.read-body { padding: 8px; }
.read-meta { display: flex; gap: 16px; color: #909399; font-size: 13px; }
.read-content { line-height: 1.8; color: #303133; }
</style>

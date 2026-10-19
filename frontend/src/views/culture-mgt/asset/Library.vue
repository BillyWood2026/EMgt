<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">文化资产库：海报/宣传片/PPT模板/文化手册等数字资产统一管理</el-tag>
    </RoleBar>

    <div class="bar mt">
      <el-input v-model="query.keyword" size="small" placeholder="搜索名称/编号/标签" clearable style="width:200px" @change="load" />
      <el-select v-model="query.assetType" size="small" placeholder="全部分类" clearable style="width:140px" @change="load">
        <el-option v-for="t in ASSET_TYPES" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="query.status" size="small" placeholder="全部状态" clearable style="width:120px" @change="load">
        <el-option label="启用" value="启用" />
        <el-option label="停用" value="停用" />
      </el-select>
      <el-button size="small" @click="load">查询</el-button>
      <el-button v-if="canEdit" size="small" type="primary" @click="openCreate">上传资产</el-button>
    </div>

    <el-table :data="rows" size="small" border v-loading="loading" class="mt">
      <el-table-column prop="assetCode" label="资产编号" width="150" />
      <el-table-column prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="assetType" label="分类" width="90" align="center">
        <template #default="{row}">
          <el-tag size="small" type="info">{{ row.assetType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" width="120" show-overflow-tooltip>
        <template #default="{row}">
          <el-tag v-for="t in parseTags(row.tags)" :key="t" size="small" effect="plain" style="margin-right:4px">{{ t }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="统计" width="110" align="center">
        <template #default="{row}">
          <span title="浏览/下载">👁{{ row.viewCount }} ⬇{{ row.downloadCount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="70" align="center">
        <template #default="{row}">
          <el-tag size="small" :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="90" />
      <el-table-column prop="createTime" label="创建时间" width="150" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{row}">
          <el-button size="small" link @click="openDetail(row)">详情</el-button>
          <el-button size="small" link @click="doView(row)">浏览</el-button>
          <el-button size="small" link @click="doDownload(row)">下载</el-button>
          <template v-if="canEdit">
            <el-button size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === '启用' ? 'warning' : 'success'" link @click="doToggle(row)">{{ row.status === '启用' ? '停用' : '启用' }}</el-button>
            <el-popconfirm title="确定删除该资产？" @confirm="doDelete(row)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="showForm" :title="form.id ? '编辑资产' : '上传资产'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="资产名称" required>
          <el-input v-model="form.assetName" maxlength="200" placeholder="2-200字" />
        </el-form-item>
        <el-form-item label="资产分类" required>
          <el-select v-model="form.assetType" placeholder="请选择" style="width:100%">
            <el-option v-for="t in ASSET_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tagsText" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="附件地址" required>
          <el-input v-model="form.fileUrl" placeholder="/files/xxx 或 https://..." />
        </el-form-item>
        <el-form-item label="可下载组织" required>
          <el-checkbox-group v-model="form.orgScopeArr">
            <el-checkbox label="集团总部" />
            <el-checkbox label="华东子公司" />
            <el-checkbox label="华南子公司" />
            <el-checkbox label="西部子公司" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showForm=false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="showDetail" title="资产详情" size="480px">
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="资产编号">{{ detail.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ detail.assetName }}</el-descriptions-item>
          <el-descriptions-item label="分类"><el-tag size="small">{{ detail.assetType }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="标签">
            <el-tag v-for="t in parseTags(detail.tags)" :key="t" size="small" effect="plain" style="margin-right:4px">{{ t }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="附件地址">{{ detail.fileUrl }}</el-descriptions-item>
          <el-descriptions-item label="可下载组织">{{ parseTags(detail.orgScope).join('、') }}</el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ detail.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ detail.downloadCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detail.creator }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>
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

const ASSET_TYPES = ['海报', '宣传片', 'PPT模板', '文化手册', '文创物料', '其他']

const loading = ref(false)
const rows = ref([])
const query = reactive({ keyword: '', assetType: '', status: '' })

const canEdit = computed(() => can('C02', 'C03', 'C99'))

const showForm = ref(false)
const saving = ref(false)
const form = reactive({ id: null, assetName: '', assetType: '', tagsText: '', fileUrl: '', orgScopeArr: [] })

const showDetail = ref(false)
const detail = ref(null)

function parseTags(json) {
  try { return JSON.parse(json || '[]') } catch { return [] }
}

function load() {
  loading.value = true
  const params = new URLSearchParams()
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.assetType) params.set('assetType', query.assetType)
  if (query.status) params.set('status', query.status)
  axios.get(`/api/culture/asset/list?${params}`)
    .then(r => { rows.value = r.data.data || [] })
    .finally(() => { loading.value = false })
}

function openCreate() {
  Object.assign(form, { id: null, assetName: '', assetType: '', tagsText: '', fileUrl: '', orgScopeArr: [] })
  showForm.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    assetName: row.assetName,
    assetType: row.assetType,
    tagsText: parseTags(row.tags).join(','),
    fileUrl: row.fileUrl,
    orgScopeArr: parseTags(row.orgScope)
  })
  showForm.value = true
}

function doSave() {
  saving.value = true
  const body = {
    id: form.id,
    assetName: form.assetName,
    assetType: form.assetType,
    tags: JSON.stringify(form.tagsText ? form.tagsText.split(',').map(s => s.trim()).filter(Boolean) : []),
    fileUrl: form.fileUrl,
    orgScope: JSON.stringify(form.orgScopeArr)
  }
  axios.post('/api/culture/asset', body)
    .then(r => {
      ElMessage.success(r.data.message || '保存成功')
      showForm.value = false
      load()
    })
    .catch(e => ElMessage.error(e.response?.data?.message || '保存失败'))
    .finally(() => { saving.value = false })
}

function doToggle(row) {
  axios.post(`/api/culture/asset/${row.id}/toggle`)
    .then(() => { ElMessage.success('状态已切换'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function doDelete(row) {
  axios.delete(`/api/culture/asset/${row.id}`)
    .then(() => { ElMessage.success('已删除'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '删除失败'))
}

function doView(row) {
  axios.post(`/api/culture/asset/${row.id}/view`)
    .then(r => { row.viewCount = r.data.data.viewCount; openDetail(row) })
}

function doDownload(row) {
  axios.post(`/api/culture/asset/${row.id}/download`)
    .then(r => {
      row.downloadCount = r.data.data.downloadCount
      ElMessage.success(`开始下载：${row.fileUrl}`)
    })
}

function openDetail(row) {
  detail.value = row
  showDetail.value = true
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.detail-body { padding: 8px; }
</style>

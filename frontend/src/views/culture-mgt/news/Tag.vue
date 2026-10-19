<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">新闻标签管理：价值观标签字典 · 引用数量实时统计 · 仅集团管理员可维护</el-tag>
    </RoleBar>

    <div class="bar mt">
      <el-input v-model="keyword" size="small" placeholder="搜索标签名" clearable style="width:200px" />
      <el-button size="small" @click="load">查询</el-button>
      <el-button v-if="canHQ" size="small" type="primary" @click="openCreate">新增标签</el-button>
    </div>

    <el-table :data="filtered" size="small" border v-loading="loading" class="mt">
      <el-table-column prop="tagName" label="标签名称" min-width="160" />
      <el-table-column prop="tagScope" label="适用范围" width="120" align="center">
        <template #default="{row}"><el-tag size="small" :type="row.tagScope === '集团全局' ? 'success' : 'info'">{{ row.tagScope }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="refCount" label="引用稿件数" width="110" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{row}">
          <el-tag size="small" :type="row.enabled === 1 ? 'success' : 'danger'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <template v-if="canHQ">
            <el-button size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.enabled === 1 ? 'warning' : 'success'" link @click="doToggle(row)">
              {{ row.enabled === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
          <span v-else class="hint">仅C03可维护</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="hint mt">引用停用标签的稿件会被拦截（E-NW-14）；停用标签不影响已引用稿件的前台展示筛选。</div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showForm" :title="form.id ? '编辑标签' : '新增标签'" width="440px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.tagName" maxlength="50" show-word-limit placeholder="2-50字，不可重名（E-NW-13）" />
        </el-form-item>
        <el-form-item label="适用范围" required>
          <el-select v-model="form.tagScope" style="width:100%">
            <el-option label="集团全局" value="集团全局" />
            <el-option label="子公司私有" value="子公司私有" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showForm = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const loading = ref(false)
const rows = ref([])
const keyword = ref('')

const canHQ = computed(() => can('C03', 'C99'))
const filtered = computed(() => rows.value.filter(t => !keyword.value || t.tagName.includes(keyword.value)))

const showForm = ref(false)
const saving = ref(false)
const form = reactive({ id: null, tagName: '', tagScope: '集团全局' })

function load() {
  loading.value = true
  axios.get('/api/culture/news/tag/list')
    .then(r => { rows.value = r.data.data || [] })
    .catch(e => ElMessage.error(e.response?.data?.message || '加载失败'))
    .finally(() => { loading.value = false })
}

function openCreate() {
  Object.assign(form, { id: null, tagName: '', tagScope: '集团全局' })
  showForm.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, tagName: row.tagName, tagScope: row.tagScope })
  showForm.value = true
}

function doSave() {
  if (!form.tagName || form.tagName.trim().length < 2 || form.tagName.trim().length > 50) {
    ElMessage.warning('标签名需2-50字'); return
  }
  saving.value = true
  axios.post('/api/culture/news/tag', { tagName: form.tagName.trim(), tagScope: form.tagScope, enabled: 1 })
    .then(r => { ElMessage.success('保存成功'); showForm.value = false; load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '保存失败'))
    .finally(() => { saving.value = false })
}

function doToggle(row) {
  axios.post(`/api/culture/news/tag/${row.id}/toggle`)
    .then(r => { ElMessage.success('状态已切换'); load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.hint { color: #909399; font-size: 12px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">标签激励配置：案例终审发布时按"首个价值观标签"读取分值；正向基础分、反面基础分、新闻引用+分、培训引用+分均可配（子公司文化专员/集团文化岗）</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="启用" value="启用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增标签</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="tagName" label="价值观标签" min-width="160">
          <template #default="{row}">
            <span>{{ row.tagName }}</span>
            <el-tag v-if="row.status==='停用'" size="small" type="info" class="ml4">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scorePositive" label="正向基础分" width="110" align="center" />
        <el-table-column prop="scoreNegative" label="反面基础分" width="110" align="center" />
        <el-table-column prop="scoreNews" label="新闻引用+分" width="110" align="center" />
        <el-table-column prop="scoreTraining" label="培训引用+分" width="110" align="center" />
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" :type="row.status==='启用'?'warning':''" @click="doToggle(row)">
              {{ row.status==='启用' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">停用后：历史已发布案例积分不变；新发布案例若命中停用标签将回退默认分值（正向10/反面5/新闻5/培训3）</div>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑标签激励' : '新增标签'" width="560px">
      <el-form :model="form" label-width="130px" size="small">
        <el-form-item label="价值观标签" required>
          <el-input v-model="form.tagName" :disabled="!!form.id" maxlength="64" show-word-limit placeholder="2-64字，如：以客户为中心" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="正向基础分"><el-input-number v-model="form.scorePositive" :min="0" :max="100" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="反面基础分"><el-input-number v-model="form.scoreNegative" :min="0" :max="100" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新闻引用+分"><el-input-number v-model="form.scoreNews" :min="0" :max="50" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训引用+分"><el-input-number v-model="form.scoreTraining" :min="0" :max="50" controls-position="right" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" /></el-form-item>
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
const form = reactive({ id: null, tagName: '', scorePositive: 10, scoreNegative: 5, scoreNews: 5, scoreTraining: 3, remark: '' })

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/case-tag/list', { params: { status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, tagName: '', scorePositive: 10, scoreNegative: 5, scoreNews: 5, scoreTraining: 3, remark: '' })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, tagName: row.tagName, scorePositive: row.scorePositive, scoreNegative: row.scoreNegative, scoreNews: row.scoreNews, scoreTraining: row.scoreTraining, remark: row.remark })
  formVis.value = true
}
async function doSave() {
  if ((form.tagName || '').length < 2) return ElMessage.warning('标签名称需2-64字')
  await axios({ url: form.id ? '/api/culture/case-tag/' + form.id : '/api/culture/case-tag', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/case-tag/${row.id}/toggle`)
  ElMessage.success(row.status === '启用' ? '已停用' : '已启用')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.ml4 { margin-left:4px; }
.tip { color:#909399; font-size:12px; }
</style>

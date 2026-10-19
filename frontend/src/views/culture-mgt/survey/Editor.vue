<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">问卷编辑器：内置3套模板可复制创建；题型支持单选/多选/量表打分/开放文本；仅草稿可编辑，发布后不可修改（E-SR-04/07）</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <template #header><b>内置模板库</b>（模板不可直接发布，复制后可编辑）</template>
      <el-row :gutter="12">
        <el-col v-for="t in templates" :key="t.id" :span="8">
          <el-card shadow="hover" class="tpl-card">
            <div class="tpl-title">{{ t.title }}</div>
            <div class="tpl-line">
              <el-tag size="small">{{ t.periodType }}</el-tag>
              <el-tag size="small" :type="t.anonymousFlag === 1 ? 'success' : 'warning'">{{ t.anonymousFlag === 1 ? '匿名' : '实名' }}</el-tag>
              <el-tag size="small" type="info" effect="plain">{{ qCount(t.questions) }} 题</el-tag>
            </div>
            <el-button v-if="can('C02','C03','C99')" size="small" type="primary" plain class="mt"
                       @click="doCopy(t)">复制创建问卷</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="草稿" value="草稿" />
          <el-option label="已发布" value="已发布" />
          <el-option label="已结束" value="已结束" />
        </el-select>
        <el-select v-model="filter.periodType" placeholder="周期" clearable size="small" style="width:120px" @change="load">
          <el-option label="季度" value="季度" />
          <el-option label="年度" value="年度" />
          <el-option label="专项" value="专项" />
        </el-select>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新建问卷</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="title" label="问卷名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="periodType" label="周期" width="80" align="center" />
        <el-table-column label="匿名" width="70" align="center">
          <template #default="{row}">{{ row.anonymousFlag === 1 ? '匿名' : '实名' }}</template>
        </el-table-column>
        <el-table-column label="题量" width="70" align="center">
          <template #default="{row}">{{ qCount(row.questions) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status === '已发布' ? 'success' : row.status === '已结束' ? 'info' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="110" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99') && row.status === '草稿'" size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99') && row.status === '草稿'" size="small" type="danger" link @click="doDelete(row)">删除</el-button>
            <span v-else class="locked">发布后锁定</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVis" :title="form.id ? '编辑问卷草稿' : '新建问卷'" width="900px" :close-on-click-modal="false" top="5vh">
      <el-form :model="form" label-width="92px" size="small">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="问卷名称" required>
              <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="如 2026Q4集团文化氛围调研" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="周期类型" required>
              <el-select v-model="form.periodType" style="width:100%">
                <el-option label="季度" value="季度" />
                <el-option label="年度" value="年度" />
                <el-option label="专项" value="专项" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="是否匿名">
              <el-switch v-model="form.anonymousFlag" :active-value="1" :inactive-value="0" active-text="匿名" inactive-text="实名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参与组织">
          <el-input v-model="form.targetOrg" placeholder="如 集团总部/全部分公司（发布时仍可调整）" />
        </el-form-item>
      </el-form>

      <div class="q-head">
        <b>题目设置（{{ form.qs.length }}）</b>
        <el-dropdown trigger="click" @command="addQuestion">
          <el-button size="small" type="primary">+ 添加题目<el-icon class="el-icon--right"><arrow-down /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="单选">单选题</el-dropdown-item>
              <el-dropdown-item command="多选">多选题</el-dropdown-item>
              <el-dropdown-item command="量表">量表打分题</el-dropdown-item>
              <el-dropdown-item command="文本">开放文本题</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <el-card v-for="(item, i) in form.qs" :key="i" shadow="never" class="q-edit">
        <div class="q-edit-head">
          <el-tag size="small">{{ typeLabel(item.type) }}</el-tag>
          <span class="q-idx">第 {{ i + 1 }} 题</span>
          <el-switch v-model="item.required" :active-value="1" :inactive-value="0" active-text="必答" inline-prompt size="small" />
          <span style="flex:1"></span>
          <el-button size="small" link :disabled="i === 0" @click="move(i, -1)">上移</el-button>
          <el-button size="small" link :disabled="i === form.qs.length - 1" @click="move(i, 1)">下移</el-button>
          <el-button size="small" type="danger" link @click="form.qs.splice(i, 1)">删除</el-button>
        </div>
        <el-input v-model="item.stem" size="small" placeholder="题干（必填）" class="mt" />
        <div v-if="item.type === '量表'" class="mt q-inline">
          <span class="q-lab">归属维度（必选）：</span>
          <el-select v-model="item.dimension" size="small" style="width:200px" placeholder="选择维度">
            <el-option v-for="d in dimensions" :key="d.id" :label="`${d.dimensionName}（权重${d.weight}）`" :value="d.dimensionName" />
          </el-select>
          <span class="q-lab ml">量表最大分：</span>
          <el-input-number v-model="item.maxScale" :min="2" :max="10" size="small" controls-position="right" />
          <span class="q-tip">按 (打分-1)/(最大分-1)×100 换算百分制</span>
        </div>
        <div v-if="item.type === '单选' || item.type === '多选'" class="mt">
          <div v-for="(opt, oi) in item.options" :key="oi" class="opt-line">
            <span class="opt-letter">{{ letter(oi) }}.</span>
            <el-input v-model="item.options[oi]" size="small" placeholder="选项内容（至少2项）" />
            <el-button size="small" type="danger" plain :disabled="item.options.length <= 2" @click="item.options.splice(oi, 1)">删</el-button>
          </div>
          <el-button size="small" class="mt" :disabled="item.options.length >= 8" @click="item.options.push('')">+ 加选项</el-button>
        </div>
      </el-card>
      <el-empty v-if="!form.qs.length" description="尚未添加题目，请从右上角选择题型" :image-size="70" />
      <template #footer>
        <el-button size="small" @click="editVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const rows = ref([])
const templates = ref([])
const dimensions = ref([])
const loading = ref(false)
const saving = ref(false)
const editVis = ref(false)
const filter = reactive({ status: '', periodType: '' })
const form = reactive({ id: null, title: '', periodType: '季度', anonymousFlag: 1, targetOrg: '', qs: [] })

const letter = i => String.fromCharCode(65 + i)
const typeLabel = t => ({ 单选: '单选题', 多选: '多选题', 量表: '量表打分', 文本: '开放文本' }[t] || t)
function qCount(s) { try { return (JSON.parse(s || '[]')).length } catch (e) { return 0 } }

async function load() {
  loading.value = true
  try {
    const [r1, r2, r3] = await Promise.all([
      axios.get('/api/culture/survey-questionnaire/list', { params: { status: filter.status || undefined, periodType: filter.periodType || undefined } }),
      axios.get('/api/culture/survey-questionnaire/list', { params: { templateOnly: true } }),
      axios.get('/api/culture/survey-dimension/list', { params: { status: '启用' } })
    ])
    rows.value = (r1.data.data || []).filter(x => x.templateFlag !== 1)
    templates.value = r2.data.data || []
    dimensions.value = r3.data.data || []
  } finally { loading.value = false }
}

function blankQuestion(type) {
  const base = { qid: '', type, stem: '', required: 1 }
  if (type === '单选' || type === '多选') return { ...base, options: ['', ''], dimension: '', maxScale: 5 }
  if (type === '量表') return { ...base, options: [], dimension: dimensions.value[0]?.dimensionName || '', maxScale: 5 }
  return { ...base, options: [], dimension: '', maxScale: 5 }
}

function addQuestion(type) { form.qs.push(blankQuestion(type)) }
function move(i, dir) {
  const j = i + dir
  const arr = form.qs
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
}

function parseQs(s) {
  try { return JSON.parse(s || '[]') } catch (e) { return [] }
}

function openCreate() {
  Object.assign(form, { id: null, title: '', periodType: '季度', anonymousFlag: 1, targetOrg: '', qs: [] })
  editVis.value = true
}
function openEdit(row) {
  Object.assign(form, {
    id: row.id, title: row.title, periodType: row.periodType,
    anonymousFlag: row.anonymousFlag, targetOrg: row.targetOrg || '',
    qs: parseQs(row.questions).map(q => ({
      qid: q.qid || '', type: q.type, stem: q.stem || '',
      options: Array.isArray(q.options) ? q.options.slice() : [],
      dimension: q.dimension || '', required: q.required === 1 || q.required === true ? 1 : 0,
      maxScale: q.maxScale || 5
    }))
  })
  editVis.value = true
}

async function doCopy(t) {
  const r = await axios.post(`/api/culture/survey-questionnaire/template/${t.id}/copy`)
  ElMessage.success('已复制为草稿，可继续编辑')
  await load()
  const draft = rows.value.find(x => x.id === r.data.data.id)
  if (draft) openEdit(draft)
}

function validateQs() {
  if (!form.qs.length) return '请至少添加1道题目'
  for (let i = 0; i < form.qs.length; i++) {
    const q = form.qs[i]
    if (!q.stem || !q.stem.trim()) return `第${i + 1}题题干为空`
    if (q.type === '量表') {
      if (!q.dimension) return `第${i + 1}量表题须选择归属维度`
      if (!(q.maxScale >= 2 && q.maxScale <= 10)) return `第${i + 1}题量表最大分须在2-10之间`
    }
    if (q.type === '单选' || q.type === '多选') {
      const opts = q.options.map(o => (o || '').trim()).filter(Boolean)
      if (opts.length < 2) return `第${i + 1}题至少需要2个有效选项`
      q.options = opts
    }
  }
  return null
}

async function doSave() {
  if (!form.title || form.title.trim().length < 2) return ElMessage.warning('问卷名称需2-100字')
  const err = validateQs()
  if (err) return ElMessage.warning(err)
  // qid 按顺序重排，保证唯一稳定
  const questions = form.qs.map((q, i) => ({
    qid: 'q' + (i + 1), type: q.type, stem: q.stem.trim(),
    options: q.options || [], dimension: q.dimension || '',
    required: q.required, maxScale: q.maxScale
  }))
  saving.value = true
  try {
    const payload = {
      title: form.title.trim(), periodType: form.periodType,
      anonymousFlag: form.anonymousFlag, targetOrg: form.targetOrg || null,
      questions: JSON.stringify(questions)
    }
    if (form.id) await axios.put('/api/culture/survey-questionnaire/' + form.id, payload)
    else await axios.post('/api/culture/survey-questionnaire', payload)
    ElMessage.success('草稿已保存')
    editVis.value = false
    load()
  } finally { saving.value = false }
}

async function doDelete(row) {
  await ElMessageBox.confirm(`确认删除草稿《${row.title}》？`, '删除确认', { type: 'warning' })
  await axios.delete('/api/culture/survey-questionnaire/' + row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 12px; }
.mt { margin-top: 8px; }
.ml { margin-left: 12px; }
.bar { display: flex; gap: 8px; align-items: center; }
.tpl-card { min-height: 130px; }
.tpl-title { font-weight: 600; margin-bottom: 10px; }
.tpl-line { display: flex; gap: 6px; flex-wrap: wrap; }
.locked { color: #aaa; font-size: 12px; }
.q-head { display: flex; justify-content: space-between; align-items: center; margin: 14px 0 8px; }
.q-edit { margin-bottom: 10px; background: #fafbfc; }
.q-edit-head { display: flex; align-items: center; gap: 10px; }
.q-idx { font-size: 13px; color: #555; }
.q-inline { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.q-lab { color: #555; font-size: 13px; white-space: nowrap; }
.q-tip { color: #909399; font-size: 12px; }
.opt-line { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.opt-letter { width: 20px; color: #777; }
</style>

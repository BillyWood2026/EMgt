<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">题库：单选/多选/判断；题干●选项●正确答案●价值观标签●红线条款○解析○；支持Excel模板批量导入（题干[Tab]类型[Tab]选项竖线分隔[Tab]答案[Tab]标签[Tab]红线[Tab]解析）；考试引用的题目禁删（E-TR-14）</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.type" placeholder="题型" clearable size="small" style="width:110px" @change="load">
          <el-option label="单选题" value="单选" />
          <el-option label="多选题" value="多选" />
          <el-option label="判断题" value="判断" />
        </el-select>
        <el-input v-model="q.tag" placeholder="价值观标签" clearable size="small" style="width:150px" @keyup.enter="load" />
        <el-input v-model="q.keyword" placeholder="题干关键字" clearable size="small" style="width:180px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" @click="importVis=true">批量导入</el-button>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增题目</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="questionType" label="题型" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.questionType==='多选'?'warning':row.questionType==='判断'?'success':''">{{ row.questionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stem" label="题干" min-width="280" show-overflow-tooltip />
        <el-table-column label="选项" min-width="200" show-overflow-tooltip>
          <template #default="{row}">{{ optionsText(row.optionItems) }}</template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" width="80" align="center">
          <template #default="{row}"><b>{{ row.answer }}</b></template>
        </el-table-column>
        <el-table-column prop="valueTag" label="价值观标签" width="130" show-overflow-tooltip />
        <el-table-column prop="redlineRef" label="红线条款" width="130" show-overflow-tooltip />
        <el-table-column prop="creator" label="录入人" width="110" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑题目' : '新增题目'" width="680px">
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="题型" required>
          <el-radio-group v-model="form.questionType" @change="onTypeChange">
            <el-radio value="单选">单选题</el-radio>
            <el-radio value="多选">多选题</el-radio>
            <el-radio value="判断">判断题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干" required>
          <el-input v-model="form.stem" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <template v-if="form.questionType !== '判断'">
          <el-form-item v-for="(opt, i) in form.optionList" :key="i" :label="'选项' + letter(i)" required>
            <div style="display:flex; gap:8px; width:100%">
              <el-input v-model="form.optionList[i]" placeholder="选项内容" />
              <el-button size="small" type="danger" plain :disabled="form.optionList.length<=2" @click="form.optionList.splice(i,1)">删</el-button>
            </div>
          </el-form-item>
          <el-form-item label=" ">
            <el-button size="small" :disabled="form.optionList.length>=6" @click="form.optionList.push('')">+ 加选项</el-button>
          </el-form-item>
        </template>
        <el-form-item label="正确答案" required>
          <el-radio-group v-if="form.questionType !== '多选题'" v-model="form.answer">
            <template v-if="form.questionType === '判断题'">
              <el-radio value="正确">正确</el-radio>
              <el-radio value="错误">错误</el-radio>
            </template>
            <template v-else>
              <el-radio v-for="(opt, i) in form.optionList" :key="i" :value="opt">{{ letter(i) }}. {{ opt }}</el-radio>
            </template>
          </el-radio-group>
          <el-checkbox-group v-else v-model="form.answerArr">
            <el-checkbox v-for="(opt, i) in form.optionList" :key="i" :value="opt">{{ letter(i) }}. {{ opt }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="价值观标签" required>
              <el-input v-model="form.valueTag" placeholder="如 以客户为中心" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="红线条款">
              <el-input v-model="form.redlineRef" placeholder="如 HX-2026-001" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" maxlength="300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importVis" title="Excel批量导入题库" width="720px">
      <el-alert type="info" :closable="false" class="mb">
        <div>每行一题，列以 Tab 或逗号分隔：<b>题干、题型(单选/多选/判断)、选项(竖线|分隔)、答案(如 客户需求 / 分享践行案例,组织文化活动 / 正确)、价值观标签、红线条款(可空)、解析(可空)</b>；多选答案须≥2项，判断答案须为 正确/错误</div>
        <div style="margin-top:4px">Excel 中编辑后整列复制粘贴到下方文本框即可（与文件导入等效，模板解析为 JSON 行提交）</div>
      </el-alert>
      <el-input v-model="importText" type="textarea" :rows="10" placeholder="文化考试的目的是？&#9;单选&#9;检验学习成果|提升文化认同|应付检查|发放福利&#9;检验学习成果&#9;以客户为中心&#9;&#9;培训年检必考题" />
      <template #footer>
        <el-button size="small" @click="importVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doImport">解析并导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const rows = ref([])
const loading = ref(false)
const q = reactive({ type: '', tag: '', keyword: '' })
const formVis = ref(false)
const importVis = ref(false)
const importText = ref('')
const form = reactive({ id: null, questionType: '单选题', stem: '', optionList: ['', '', '', ''], answer: '', answerArr: [], valueTag: '', redlineRef: '', analysis: '' })

const letter = i => String.fromCharCode(65 + i)

function optionsText(s) {
  try {
    const arr = typeof s === 'string' ? JSON.parse(s) : s
    return (arr || []).map((o, i) => letter(i) + '.' + o).join('  ')
  } catch (e) { return s }
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/question/list', { params: { type: q.type || undefined, tag: q.tag || undefined, keyword: q.keyword || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}

function onTypeChange() {
  form.answer = ''
  form.answerArr = []
  if (form.questionType === '判断') form.optionList = []
  else if (form.optionList.length < 2) form.optionList = ['', '', '', '']
}

function openCreate() {
  Object.assign(form, { id: null, questionType: '单选', stem: '', optionList: ['', '', '', ''], answer: '', answerArr: [], valueTag: '', redlineRef: '', analysis: '' })
  formVis.value = true
}
function openEdit(row) {
  let opts = []
  try { opts = JSON.parse(row.optionItems || '[]') } catch (e) { opts = [] }
  Object.assign(form, {
    id: row.id, questionType: row.questionType, stem: row.stem,
    optionList: row.questionType === '判断' ? [] : opts.slice(),
    answer: row.questionType === '多选' ? '' : row.answer,
    answerArr: row.questionType === '多选' ? row.answer.split(',') : [],
    valueTag: row.valueTag || '', redlineRef: row.redlineRef || '', analysis: row.analysis || ''
  })
  formVis.value = true
}

async function doSave() {
  if (!form.stem || form.stem.length < 5) return ElMessage.warning('题干至少5字')
  if (form.questionType === '判断') {
    if (!form.answer) return ElMessage.warning('请选择正确答案')
  } else {
    if (form.optionList.some(o => !o || !o.trim())) return ElMessage.warning('选项内容不能为空')
    if (form.questionType === '多选') {
      if (form.answerArr.length < 2) return ElMessage.warning('多选题答案需选2项以上')
    } else if (!form.answer) return ElMessage.warning('请选择正确答案')
  }
  if (!form.valueTag) return ElMessage.warning('价值观标签必填')
  const payload = {
    id: form.id, questionType: form.questionType, stem: form.stem,
    optionItems: JSON.stringify(form.questionType === '判断' ? ['正确', '错误'] : form.optionList.map(o => o.trim())),
    answer: form.questionType === '多选' ? form.answerArr.join(',') : form.answer,
    valueTag: form.valueTag, redlineRef: form.redlineRef || null, analysis: form.analysis || null
  }
  await axios({ url: form.id ? '/api/culture/question/' + form.id : '/api/culture/question', method: form.id ? 'put' : 'post', data: payload })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}

function parseImport() {
  return importText.value.split('\n').map(l => l.trim()).filter(Boolean).map(line => {
    const cells = line.split(/\t|,|，/).map(c => c.trim())
    return {
      stem: cells[0] || '', questionType: cells[1] || '',
      optionItems: (cells[2] || '').split('|').map(o => o.trim()).filter(Boolean),
      answer: cells[3] || '', valueTag: cells[4] || '',
      redlineRef: cells[5] || null, analysis: cells[6] || null
    }
  })
}

async function doImport() {
  const parsed = parseImport()
  if (!parsed.length) return ElMessage.warning('未解析到任何题目行')
  const r = await axios.post('/api/culture/question/import-batch', { rows: parsed })
  ElMessage.success(r.data.data || '导入完成')
  importVis.value = false
  importText.value = ''
  load()
}

async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该题目？被考试引用将无法删除。', '删除确认', { type: 'warning' })
  await axios.delete(`/api/culture/question/${row.id}`)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.mb { margin-bottom:10px; }
</style>

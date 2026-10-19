<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">考试：引用题库自动组卷（题量≥5）自动阅卷；及格线默认70；防切屏≥3次前端强制交卷；不及格自动冻结评优/晋升/调薪资格，补考通过自动解冻；合格率=合格人数/参考总人数×100%（按部门）</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="未开始" value="未开始" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
          <el-option label="已停用" value="已停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增考试</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="examName" label="考试名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="题库" width="90" align="center">
          <template #default="{row}">{{ bankCount(row.bankIds) }} 题</template>
        </el-table-column>
        <el-table-column prop="durationMin" label="时长(分)" width="90" align="center" />
        <el-table-column prop="passLine" label="及格线" width="80" align="center" />
        <el-table-column prop="maxRetake" label="补考次数" width="90" align="center" />
        <el-table-column prop="antiSwitch" label="防切屏" width="80" align="center">
          <template #default="{row}">
            <el-tag v-if="row.antiSwitch===1" size="small" type="danger">开启</el-tag>
            <el-tag v-else size="small" type="info">关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考试窗口" width="240">
          <template #default="{row}">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='进行中'?'success':row.status==='已结束'?'info':'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" type="primary" plain @click="openAssign(row)">分配</el-button>
            <el-button size="small" type="warning" plain @click="openMonitor(row)">监考</el-button>
            <el-button size="small" type="success" plain @click="openStats(row)">合格率</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" :type="row.status==='已停用'?'success':'info'" plain @click="doToggle(row)">
              {{ row.status==='已停用' ? '启用' : '停用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">监考视图展示每人考试实例：状态/得分/第几次作答/切屏次数/资格冻结；分配后按考试生成待考试实例</div>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑考试' : '新增考试'" width="640px">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item label="考试名称" required>
          <el-input v-model="form.examName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="引用题库" required>
          <el-select v-model="form.bankIdArr" multiple style="width:100%" placeholder="选择题目（≥5题）">
            <el-option v-for="x in questions" :key="x.id" :label="'[' + x.questionType + '] ' + x.stem.slice(0, 30)" :value="x.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="时长(分)" required>
              <el-input-number v-model="form.durationMin" :min="5" :max="240" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格线">
              <el-input-number v-model="form.passLine" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="补考次数">
              <el-input-number v-model="form.maxRetake" :min="0" :max="5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="防切屏">
          <el-switch v-model="form.antiSwitch" :active-value="1" :inactive-value="0" active-text="开启(≥3次强制交卷)" />
        </el-form-item>
        <el-form-item label="组织范围" required>
          <el-input v-model="form.orgScope" placeholder="如 全员 / 集团本部" />
        </el-form-item>
        <el-form-item label="考试窗口" required>
          <el-date-picker v-model="form.window" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="开始" end-placeholder="结束" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVis" title="分配考试对象" width="520px">
      <el-alert type="info" :closable="false" class="mb">每行一人：<b>姓名,部门</b>；已存在实例的自动跳过</el-alert>
      <el-input v-model="assignText" type="textarea" :rows="6" placeholder="王五,客户服务部&#10;赵六,人力资源部" />
      <template #footer>
        <el-button size="small" @click="assignVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doAssign">确定分配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="monitorVis" :title="'监考视图：' + (monitorExam ? monitorExam.examName : '')" width="900px">
      <el-table :data="monitorData.instances || []" size="small" border max-height="420">
        <el-table-column prop="userName" label="姓名" width="110" />
        <el-table-column prop="userDept" label="部门" min-width="120" />
        <el-table-column prop="status" label="状态" width="130" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="instType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="70" align="center">
          <template #default="{row}">{{ row.score === null ? '-' : row.score }}</template>
        </el-table-column>
        <el-table-column prop="attempt" label="第几次" width="70" align="center" />
        <el-table-column prop="switchCount" label="切屏" width="70" align="center">
          <template #default="{row}">
            <el-tag v-if="row.switchCount >= 3" size="small" type="danger">{{ row.switchCount }}</el-tag>
            <span v-else>{{ row.switchCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="submitTime" label="交卷时间" width="160" />
        <el-table-column prop="frozen" label="资格" width="90" align="center">
          <template #default="{row}">
            <el-tag v-if="row.frozen===1" size="small" type="danger">已冻结</el-tag>
            <el-tag v-else size="small" type="success">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="statsVis" :title="'培训合格率（算法51，按部门）：' + (statsExam ? statsExam.examName : '全部考试')" width="640px">
      <el-table :data="statsData" size="small" border max-height="400">
        <el-table-column prop="dept" label="部门" min-width="160" />
        <el-table-column prop="total" label="参考人数" width="100" align="center" />
        <el-table-column prop="pass" label="合格人数" width="100" align="center" />
        <el-table-column prop="rate" label="合格率%" width="110" align="center">
          <template #default="{row}">
            <el-tag :type="row.rate >= 80 ? 'success' : row.rate >= 60 ? 'warning' : 'danger'" size="small">{{ row.rate }}%</el-tag>
          </template>
        </el-table-column>
      </el-table>
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
const questions = ref([])
const loading = ref(false)
const q = reactive({ status: '' })
const formVis = ref(false)
const form = reactive({ id: null, examName: '', bankIdArr: [], durationMin: 30, passLine: 70, maxRetake: 1, antiSwitch: 1, orgScope: '全员', window: [] })

const assignVis = ref(false)
const assignText = ref('')
let assignId = null

const monitorVis = ref(false)
const monitorExam = ref(null)
const monitorData = reactive({})

const statsVis = ref(false)
const statsExam = ref(null)
const statsData = ref([])

function bankCount(s) {
  try { return (JSON.parse(s || '[]')).length } catch (e) { return 0 }
}
function fmt(d) { return (d || '').replace('T', ' ').slice(0, 16) }
function instType(s) {
  if (s === '已完成合格') return 'success'
  if (s === '补考完成合格') return 'success'
  if (s === '已完成不合格待补考') return 'warning'
  if (s === '补考仍不合格') return 'danger'
  return ''
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/exam/list', { params: { status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
async function loadQuestions() {
  const r = await axios.get('/api/culture/question/list')
  questions.value = r.data.data || []
}
function openCreate() {
  Object.assign(form, { id: null, examName: '', bankIdArr: [], durationMin: 30, passLine: 70, maxRetake: 1, antiSwitch: 1, orgScope: '全员', window: [] })
  formVis.value = true
}
function openEdit(row) {
  let ids = []
  try { ids = JSON.parse(row.bankIds || '[]') } catch (e) { ids = [] }
  Object.assign(form, {
    id: row.id, examName: row.examName, bankIdArr: ids, durationMin: row.durationMin,
    passLine: row.passLine, maxRetake: row.maxRetake, antiSwitch: row.antiSwitch, orgScope: row.orgScope,
    window: [row.startTime ? row.startTime.replace('T', ' ').slice(0, 19) : '', row.endTime ? row.endTime.replace('T', ' ').slice(0, 19) : '']
  })
  formVis.value = true
}
async function doSave() {
  if (!form.examName || form.examName.length < 2) return ElMessage.warning('考试名称需2-100字')
  if (form.bankIdArr.length < 5) return ElMessage.warning('题库需至少选择5道题（E-TR-07）')
  if (!form.window || form.window.length !== 2 || !form.window[0]) return ElMessage.warning('请选择考试窗口')
  await axios({
    url: form.id ? '/api/culture/exam/' + form.id : '/api/culture/exam',
    method: form.id ? 'put' : 'post',
    data: {
      id: form.id, examName: form.examName, bankIds: JSON.stringify(form.bankIdArr),
      durationMin: form.durationMin, passLine: form.passLine, maxRetake: form.maxRetake,
      antiSwitch: form.antiSwitch, orgScope: form.orgScope,
      startTime: form.window[0], endTime: form.window[1]
    }
  })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/exam/${row.id}/toggle`)
  ElMessage.success('已切换状态')
  load()
}
function openAssign(row) {
  assignId = row.id
  assignText.value = ''
  assignVis.value = true
}
async function doAssign() {
  const users = assignText.value.split('\n').map(l => l.trim()).filter(Boolean).map(l => {
    const [userName, userDept] = l.split(/,|，/)
    return { userName: (userName || '').trim(), userDept: (userDept || '').trim() }
  }).filter(u => u.userName)
  if (!users.length) return ElMessage.warning('请至少填写一行')
  const r = await axios.post(`/api/culture/exam/${assignId}/assign`, { users })
  ElMessage.success(`新增 ${r.data.data.created} 人，跳过 ${r.data.data.skipped} 人`)
  assignVis.value = false
}
async function openMonitor(row) {
  monitorExam.value = row
  const r = await axios.get(`/api/culture/exam/${row.id}/instances`)
  Object.assign(monitorData, r.data.data)
  monitorVis.value = true
}
async function openStats(row) {
  statsExam.value = row
  const r = await axios.get('/api/culture/exam/stats', { params: { examId: row.id } })
  statsData.value = r.data.data || []
  statsVis.value = true
}
onMounted(() => { load(); loadQuestions() })
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.mb { margin-bottom:10px; }
.tip { color:#909399; font-size:12px; }
</style>

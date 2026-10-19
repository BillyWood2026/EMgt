<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">学习任务：新员工必修|全员复训|部门专项；到期提醒（临近 remindDays 天/已逾期未完成名单）；进度由培训中台回写（进度&lt;80% 不计学时）</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.taskType" placeholder="任务类型" clearable size="small" style="width:130px" @change="load">
          <el-option label="新员工必修" value="新员工必修" />
          <el-option label="全员复训" value="全员复训" />
          <el-option label="部门专项" value="部门专项" />
        </el-select>
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
          <el-option label="已停用" value="已停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增任务</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="taskType" label="类型" width="110" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.taskType==='新员工必修'?'danger':row.taskType==='全员复训'?'warning':''">{{ row.taskType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="关联课程" min-width="180" show-overflow-tooltip />
        <el-table-column prop="orgScope" label="组织范围" min-width="120" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止日期" width="110" align="center">
          <template #default="{row}">
            <span :class="{ overdue: isOverdue(row) }">{{ row.deadline }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remindDays" label="提前提醒(天)" width="110" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='进行中'?'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" type="primary" plain @click="openAssign(row)">分配</el-button>
            <el-button size="small" @click="openRecords(row)">明细</el-button>
            <el-button size="small" type="warning" plain @click="openRemind(row)">到期提醒</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" :type="row.status==='已停用'?'success':'info'" plain @click="doToggle(row)">
              {{ row.status==='已停用' ? '启用' : '停用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">分配后按任务生成学习记录；"模拟中台回写"可在明细弹窗中按人回写进度（进度≥100%自动置已完成，≥80%才计学时）</div>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑任务' : '新增任务'" width="560px">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item label="任务名称" required>
          <el-input v-model="form.taskName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="任务类型" required>
          <el-select v-model="form.taskType" style="width:100%">
            <el-option label="新员工必修" value="新员工必修" />
            <el-option label="全员复训" value="全员复训" />
            <el-option label="部门专项" value="部门专项" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联课程" required>
          <el-select v-model="form.courseId" style="width:100%" filterable @change="onCourseChange">
            <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="组织范围" required>
          <el-input v-model="form.orgScope" placeholder="如 全员 / 集团本部" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="截止日期" required>
              <el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提前提醒(天)">
              <el-input-number v-model="form.remindDays" :min="1" :max="30" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVis" title="分配学习对象" width="520px">
      <el-alert type="info" :closable="false" class="mb">每行一人：<b>姓名,部门</b>；已存在记录的自动跳过</el-alert>
      <el-input v-model="assignText" type="textarea" :rows="6" placeholder="张三,客户服务部&#10;李四,人力资源部" />
      <template #footer>
        <el-button size="small" @click="assignVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doAssign">确定分配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordsVis" :title="'学习明细：' + (recordsTask ? recordsTask.taskName : '')" width="760px">
      <div class="mb">
        <el-tag size="small">应学 {{ recordsData.total || 0 }} 人</el-tag>
        <el-tag size="small" type="success" class="ml4">已完成 {{ recordsData.done || 0 }} 人</el-tag>
        <el-tag size="small" type="warning" class="ml4">完成率 {{ recordsData.rate || 0 }}%</el-tag>
      </div>
      <el-table :data="recordsData.records || []" size="small" border max-height="360">
        <el-table-column prop="userName" label="姓名" width="120" />
        <el-table-column prop="userDept" label="部门" min-width="130" />
        <el-table-column prop="progress" label="进度%" width="90" align="center">
          <template #default="{row}">{{ row.progress }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='已完成'?'success':'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="160" />
        <el-table-column label="模拟中台回写" width="170" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="doSync(row, 100)">完成100%</el-button>
            <el-button v-else size="small" disabled>仅管理岗</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="remindVis" :title="'到期提醒：' + (remindTask ? remindTask.taskName : '')" width="640px">
      <div class="mb">
        <el-tag size="small" type="danger">未完成 {{ remindData.undoneCount || 0 }} 人</el-tag>
        <el-tag size="small" type="warning" class="ml4">临近截止 {{ (remindData.nearDeadline || []).length }} 人</el-tag>
        <el-tag size="small" type="danger" class="ml4">已逾期 {{ (remindData.overdue || []).length }} 人</el-tag>
      </div>
      <div v-if="(remindData.nearDeadline || []).length" class="mb">
        <div class="tip">临近截止（{{ remindTask ? remindTask.remindDays : '' }}天内）：</div>
        <el-tag v-for="u in remindData.nearDeadline" :key="'n'+u" size="small" class="ml4 mt4">{{ u }}</el-tag>
      </div>
      <div v-if="(remindData.overdue || []).length" class="mb">
        <div class="tip">已逾期未完成：</div>
        <el-tag v-for="u in remindData.overdue" :key="'o'+u" size="small" type="danger" class="ml4 mt4">{{ u }}</el-tag>
      </div>
      <div class="tip">发送提醒将通知名单内人员（演示环境记录审计日志）</div>
      <template #footer>
        <el-button size="small" @click="remindVis=false">关闭</el-button>
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
const courses = ref([])
const loading = ref(false)
const q = reactive({ taskType: '', status: '' })
const formVis = ref(false)
const form = reactive({ id: null, taskName: '', taskType: '全员复训', courseId: null, courseName: '', orgScope: '全员', deadline: '', remindDays: 7 })

const assignVis = ref(false)
const assignText = ref('')
let assignId = null

const recordsVis = ref(false)
const recordsTask = ref(null)
const recordsData = reactive({})

const remindVis = ref(false)
const remindTask = ref(null)
const remindData = reactive({})

function isOverdue(row) {
  return row.status === '进行中' && row.deadline && row.deadline < new Date().toISOString().slice(0, 10)
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/study-task/list', { params: { taskType: q.taskType || undefined, status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
async function loadCourses() {
  const r = await axios.get('/api/culture/course/list')
  courses.value = (r.data.data || []).filter(c => c.status === '已启用')
}
function onCourseChange(id) {
  const c = courses.value.find(x => x.id === id)
  form.courseName = c ? c.courseName : ''
}
function openCreate() {
  Object.assign(form, { id: null, taskName: '', taskType: '全员复训', courseId: null, courseName: '', orgScope: '全员', deadline: '', remindDays: 7 })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, taskName: row.taskName, taskType: row.taskType, courseId: row.courseId, courseName: row.courseName, orgScope: row.orgScope, deadline: row.deadline, remindDays: row.remindDays })
  formVis.value = true
}
async function doSave() {
  if (!form.taskName || form.taskName.length < 2) return ElMessage.warning('任务名称需2-100字')
  if (!form.courseId) return ElMessage.warning('请选择关联课程')
  if (!form.deadline) return ElMessage.warning('请选择截止日期')
  await axios({ url: form.id ? '/api/culture/study-task/' + form.id : '/api/culture/study-task', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/study-task/${row.id}/toggle`)
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
  const r = await axios.post(`/api/culture/study-task/${assignId}/assign`, { users })
  ElMessage.success(`新增 ${r.data.data.created} 人，跳过 ${r.data.data.skipped} 人`)
  assignVis.value = false
}
async function openRecords(row) {
  recordsTask.value = row
  const r = await axios.get(`/api/culture/study-task/${row.id}/records`)
  Object.assign(recordsData, r.data.data)
  recordsVis.value = true
}
async function doSync(row, progress) {
  await axios.post(`/api/culture/study-task/${recordsTask.value.id}/sync`, { userName: row.userName, progress })
  ElMessage.success(`已回写 ${row.userName} 进度${progress}%`)
  const r = await axios.get(`/api/culture/study-task/${recordsTask.value.id}/records`)
  Object.assign(recordsData, r.data.data)
}
async function openRemind(row) {
  remindTask.value = row
  const r = await axios.get(`/api/culture/study-task/${row.id}/remind`)
  Object.assign(remindData, r.data.data)
  remindVis.value = true
}
onMounted(() => { load(); loadCourses() })
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.mb { margin-bottom:10px; }
.ml4 { margin-left:4px; }
.mt4 { margin-top:4px; }
.tip { color:#909399; font-size:12px; }
.overdue { color:#f56c6c; font-weight:bold; }
</style>

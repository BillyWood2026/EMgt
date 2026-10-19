<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="danger" effect="plain">红线禁令：编号自动(HX年份+3位)｜文化审核C03→人力合规C05→高管终审C07｜重大等级自动一票否决且锁定不可解除</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="q.level" placeholder="违规等级" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="l in LEVELS" :key="l" :label="l" :value="l" />
        </el-select>
        <el-select v-model="q.category" placeholder="红线分类" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C03','C07')" size="small" type="primary" @click="openCreate">编制红线</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="470">
        <el-table-column prop="code" label="编号" width="105" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="description" label="违规情形" min-width="240" show-overflow-tooltip />
        <el-table-column label="等级" width="78">
          <template #default="{row}"><el-tag size="small" :type="levelType(row.violationLevel)">{{ row.violationLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="一票否决" width="90">
          <template #default="{row}"><el-tag v-if="row.vetoFlag===1" size="small" type="danger">否决锁定</el-tag><span v-else>—</span></template>
        </el-table-column>
        <el-table-column label="状态" width="82">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="节点" width="110">
          <template #default="{row}">{{ nodeText(row) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C03','C07') && row.status==='草稿'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status==='草稿'" size="small" type="primary" @click="doSubmit(row)">提交</el-button>
            <el-button v-if="canApprove(row)" size="small" type="warning" @click="openApprove(row)">审批</el-button>
            <el-button v-if="can('C07') && row.status==='生效'" size="small" type="danger" @click="doArchive(row)">归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id?'编辑红线':'编制红线'" width="640px">
      <el-form :model="form" label-width="100px" size="small">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="红线分类" required>
            <el-select v-model="form.category" style="width:100%"><el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="违规等级" required>
            <el-select v-model="form.violationLevel" style="width:100%">
              <el-option v-for="l in LEVELS" :key="l" :label="l" :value="l" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="24">
            <el-alert v-if="form.violationLevel==='重大'" type="error" :closable="false" show-icon
              title="重大违规：系统自动携带评优/晋升/调薪一票否决，锁定后不可人工解除" class="mb" />
          </el-col>
          <el-col :span="24"><el-form-item label="违规描述" required><el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="处罚标准" required><el-input v-model="form.punishStd" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用范围"><el-input v-model="form.scopeText" placeholder="默认全员" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="一票否决">
            <el-switch :model-value="form.violationLevel==='重大' || form.vetoFlag===1" disabled
              active-text="系统判定" inactive-text="不涉及" />
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="apVis" title="红线审批" width="520px">
      <el-alert :title="'当前节点：' + nodeText(apRow)" type="info" :closable="false" class="mb" />
      <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="审批意见（驳回必填，不少于5字）" />
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="danger" @click="doApprove(false)">驳回</el-button>
        <el-button size="small" type="primary" @click="doApprove(true)">通过</el-button>
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

const STATUS = ['草稿', '待审', '生效', '归档']
const LEVELS = ['一般', '较重', '严重', '重大']
const CATEGORIES = ['客户导向', '奋斗作风', '团队协作', '廉洁诚信', '自我批判', '职场作风']
const NODE = { 1: '文化审核(C03)', 2: '人力合规复核(C05)', 3: '高管终审(C07)' }

const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', level: '', category: '' })
const formVis = ref(false)
const form = reactive({})
const apVis = ref(false)
const apRow = ref(null)
const opinion = ref('')

function levelType(lv) {
  if (lv === '重大') return 'danger'
  if (lv === '严重') return 'danger'
  if (lv === '较重') return 'warning'
  return 'info'
}
function statusType(s) {
  if (s === '生效') return 'success'
  if (s === '待审') return 'warning'
  if (s === '归档') return 'info'
  return ''
}
function nodeText(row) {
  return row && row.status === '待审' ? (NODE[row.stage] || ('节点' + row.stage)) : '—'
}
function canApprove(row) {
  if (row.status !== '待审') return false
  if (row.stage === 1) return can('C03')
  if (row.stage === 2) return can('C05')
  if (row.stage === 3) return can('C07')
  return false
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/foundation/redline/list', { params: { status: q.status || undefined, level: q.level || undefined, category: q.category || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, category: '', violationLevel: '一般', description: '', punishStd: '', scopeText: '全员', vetoFlag: 0 })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  formVis.value = true
}
async function doSave() {
  const payload = { ...form }
  if (payload.violationLevel === '重大') payload.vetoFlag = 1
  await axios({ url: form.id ? '/api/culture/foundation/redline/' + form.id : '/api/culture/foundation/redline', method: form.id ? 'put' : 'post', data: payload })
  ElMessage.success('已保存草稿')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('确认提交红线审批？', '提交', { type: 'warning' })
  await axios.post(`/api/culture/foundation/redline/${row.id}/submit`)
  ElMessage.success('已提交')
  load()
}
function openApprove(row) {
  apRow.value = row
  opinion.value = ''
  apVis.value = true
}
async function doApprove(pass) {
  await axios.post(`/api/culture/foundation/redline/${apRow.value.id}/approve`, null, { params: { pass, opinion: opinion.value } })
  ElMessage.success(pass ? '审批通过' : '已驳回至草稿')
  apVis.value = false
  load()
}
async function doArchive(row) {
  const { value } = await ElMessageBox.prompt('请输入归档原因（不少于5字）', '红线归档', {
    confirmButtonText: '确认归档', cancelButtonText: '取消', inputPattern: /.{5,}/, inputErrorMessage: '归档原因不少于5字'
  })
  await axios.post(`/api/culture/foundation/redline/${row.id}/archive`, null, { params: { reason: value } })
  ElMessage.success('已归档')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
</style>

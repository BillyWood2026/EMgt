<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">我的荣誉申报：草稿可编辑提交 → 部门初审 → 专员复核（自动读取文化得分）→ 公示（可异议）→ 高管终审表彰；个人申报对象锁定为本人</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <span class="tip">共 {{ rows.length }} 条申报</span>
        <span style="flex:1"></span>
        <el-button size="small" type="primary" @click="openCreate">新建荣誉申报</el-button>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="projectName" label="荣誉项目" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{row}">
            <el-tag size="small" :type="row.applyType==='个人' ? 'primary':'success'" effect="plain">{{ row.applyType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="candidate" label="申报对象" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userDept" label="申报部门" min-width="120" show-overflow-tooltip />
        <el-table-column label="文化得分" width="90" align="center">
          <template #default="{row}"><span v-if="row.cultureScore!=null" class="pts">{{ row.cultureScore }}</span><span v-else class="tip">—</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="表彰时间" width="110">
          <template #default="{row}">{{ row.awardTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status==='草稿'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status==='草稿'" size="small" type="primary" @click="doSubmit(row)">提交申报</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑申报草稿' : '新建荣誉申报'" width="720px" top="6vh">
      <el-alert v-if="form.id && form.reviewOpinion" type="error" :closable="false" class="mb"
        :title="'上次驳回意见：' + form.reviewOpinion" />
      <el-form :model="form" label-width="96px" size="small">
        <el-form-item label="荣誉项目" required>
          <el-select v-model="form.projectId" style="width:100%" placeholder="仅显示申报期内的已启用项目" @change="onProjectChange">
            <el-option v-for="p in openProjects" :key="p.id"
              :label="p.projectName + '（' + p.periodType + ' · ' + p.applyStart + ' ~ ' + p.applyEnd + '）'"
              :value="p.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="9">
            <el-form-item label="申报类型">
              <el-tag v-if="curProject" size="small" :type="curProject.applyType==='个人' ? 'primary':'success'">{{ curProject.applyType }}申报</el-tag>
              <span v-else class="tip">先选择项目</span>
            </el-form-item>
          </el-col>
          <el-col :span="15">
            <el-form-item label="申报对象" required>
              <el-input v-if="curProject && curProject.applyType==='个人'" :model-value="me" disabled />
              <el-input v-else v-model="form.candidate" placeholder="团队/部门名称（团队申报必填）" />
              <div v-if="curProject && curProject.applyType==='个人'" class="tip">个人申报锁定为本人</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申报部门" required>
          <el-input v-model="form.userDept" maxlength="50" placeholder="如：集团办公室 / XX子公司XX部" />
        </el-form-item>
        <el-form-item label="先进事迹" required>
          <el-input v-model="form.deeds" type="textarea" :rows="6" maxlength="3000" show-word-limit
            placeholder="不少于100字：事迹背景、具体行动、产生的结果与价值观体现" />
          <div class="tip" :style="(form.deeds||'').length < 100 ? 'color:#f56c6c' : ''">当前 {{ (form.deeds||'').length }} 字，须不少于100字</div>
        </el-form-item>
        <el-form-item label="佐证附件" required>
          <div class="att-box">
            <div v-for="(a,i) in attArr" :key="i" class="att-row">
              <el-input v-model="attArr[i]" size="small" placeholder="附件地址/文件名（至少1份）" />
              <el-button size="small" link type="danger" @click="attArr.splice(i,1)">删除</el-button>
            </div>
            <el-button size="small" @click="attArr.push('')">+ 添加附件</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVis" size="540px" :title="cur.projectName || '申报详情'">
      <div v-if="cur.id">
        <div class="d-row">
          <el-tag size="small" :type="cur.applyType==='个人' ? 'primary':'success'" effect="plain">{{ cur.applyType }}申报</el-tag>
          <el-tag size="small" :type="statusType(cur.status)">{{ cur.status }}</el-tag>
          <el-tag v-if="cur.cultureScore!=null" size="small" type="warning">文化得分 {{ cur.cultureScore }}</el-tag>
          <el-tag v-if="cur.syncCase===1" size="small" type="success">已同步案例库{{ cur.caseId ? ' #'+cur.caseId : '' }}</el-tag>
        </div>
        <el-alert v-if="cur.status==='评审驳回' && cur.reviewOpinion" type="error" :closable="false" class="d-block"
          :title="'驳回意见：' + cur.reviewOpinion" />
        <el-descriptions :column="1" border size="small" class="d-block">
          <el-descriptions-item label="申报对象">{{ cur.candidate }}</el-descriptions-item>
          <el-descriptions-item label="申报部门">{{ cur.userDept }}</el-descriptions-item>
          <el-descriptions-item label="公示期">{{ cur.publicityStart || '—' }} ~ {{ cur.publicityEnd || '—' }}</el-descriptions-item>
          <el-descriptions-item label="表彰时间">{{ cur.awardTime || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="d-block sec-title">先进事迹</div>
        <div class="d-content">{{ cur.deeds }}</div>
        <div class="d-block sec-title">佐证附件</div>
        <div v-if="atts(cur.attachments).length">
          <div v-for="(a,i) in atts(cur.attachments)" :key="i" class="att-line">📎 {{ a }}</div>
        </div>
        <div v-else class="tip">无</div>
        <div v-if="atts(cur.certFiles).length" class="d-block">
          <div class="sec-title">表彰凭证</div>
          <div v-for="(a,i) in atts(cur.certFiles)" :key="'c'+i" class="att-line">🏅 {{ a }}</div>
        </div>
        <div class="d-block sec-title">评审流程</div>
        <el-timeline v-if="logs(cur.approvalLog).length">
          <el-timeline-item v-for="(l,i) in logs(cur.approvalLog)" :key="i" :type="l.pass ? 'success':'danger'" :timestamp="fmt(l.time)">
            {{ stageName(l.stage) }} · {{ l.pass ? '通过' : '驳回' }} · {{ l.by }}
            <div v-if="l.opinion" class="opinion">{{ l.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="tip">暂无评审记录</div>
        <div v-if="objs(cur.objections).length" class="d-block">
          <div class="sec-title">公示异议</div>
          <div v-for="(o,i) in objs(cur.objections)" :key="'o'+i" class="obj-line">
            <span class="opinion">{{ o.reason }}</span>
            <span class="tip">（{{ o.by }} · {{ fmt(o.time) }}）</span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const rows = ref([])
const projects = ref([])
const loading = ref(false)
const formVis = ref(false)
const detailVis = ref(false)
const cur = ref({})
const form = reactive({ id: null, projectId: null, candidate: '', userDept: '', deeds: '' })
const attArr = ref([])
const me = localStorage.getItem('username') || localStorage.getItem('userId') || 'dev-user'

const openProjects = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return projects.value.filter(p => p.status === '已启用' && p.applyStart <= today && today <= p.applyEnd)
})
const curProject = computed(() => projects.value.find(p => p.id === form.projectId))

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function statusType(s) {
  if (s === '终审表彰') return 'success'
  if (s === '公示中') return 'warning'
  if (s === '评审驳回') return 'danger'
  if (s === '草稿') return 'info'
  return ''
}
function stageName(s) {
  const m = { 0: '提交申报', 1: '部门初审', 2: '专员复核', 3: '公示', 5: '终审表彰' }
  if (typeof s === 'number' || /^\d+$/.test(String(s))) return m[Number(s)] || ('节点' + s)
  return '「' + s + '」节点'
}
function parse(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function atts(json) { return parse(json) }
function logs(json) { return parse(json) }
function objs(json) { return parse(json) }

async function load() {
  loading.value = true
  try { const r = await axios.get('/api/culture/honor/mine'); rows.value = r.data.data || [] }
  finally { loading.value = false }
}
async function loadProjects() {
  const r = await axios.get('/api/culture/honor-project/list')
  projects.value = r.data.data || []
}
function resetForm() {
  Object.assign(form, { id: null, projectId: null, candidate: '', userDept: '', deeds: '' })
  attArr.value = ['']
}
function openCreate() { resetForm(); formVis.value = true }
function openEdit(row) {
  Object.assign(form, { id: row.id, projectId: row.projectId, candidate: row.candidate, userDept: row.userDept, deeds: row.deeds })
  try { const v = JSON.parse(row.attachments || '[]'); attArr.value = Array.isArray(v) && v.length ? v : [''] } catch { attArr.value = [''] }
  formVis.value = true
}
function onProjectChange() { form.candidate = '' }
async function doSave() {
  if (!form.projectId) return ElMessage.warning('请选择荣誉项目')
  if (!(form.userDept || '').trim()) return ElMessage.warning('请填写申报部门')
  if ((form.deeds || '').length < 100) return ElMessage.warning('先进事迹须不少于100字')
  const atts2 = attArr.value.map(a => (a || '').trim()).filter(Boolean)
  if (!atts2.length) return ElMessage.warning('请至少添加1个佐证附件')
  const p = curProject.value
  const candidate = p && p.applyType === '个人' ? me : (form.candidate || '').trim()
  if (!candidate) return ElMessage.warning('团队申报须填写团队/部门名称')
  const payload = {
    projectId: form.projectId, candidate, userDept: form.userDept.trim(),
    deeds: form.deeds, attachments: JSON.stringify(atts2)
  }
  await axios({ url: form.id ? '/api/culture/honor/' + form.id : '/api/culture/honor', method: form.id ? 'put' : 'post', data: payload })
  ElMessage.success('草稿已保存')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('提交后将进入部门初审，申报期内同项目同对象不可重复申报；存在文化红线记录将被系统自动拦截，确认提交？', '提交申报', { type: 'warning' })
  await axios.post(`/api/culture/honor/${row.id}/submit`)
  ElMessage.success('已提交申报，等待部门初审')
  load()
}
function openDetail(row) { cur.value = row; detailVis.value = true }
onMounted(() => { load(); loadProjects() })
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; margin-bottom:10px; }
.tip { color:#909399; font-size:12px; }
.mb { margin-bottom:10px; }
.pts { font-weight:700; color:var(--el-color-warning); }
.att-box { width:100%; }
.att-row { display:flex; gap:8px; margin-bottom:6px; }
.d-row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; }
.d-block { margin-top:12px; }
.sec-title { font-weight:600; margin-bottom:8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.att-line { line-height:1.9; font-size:13px; color:#606266; }
.obj-line { margin-bottom:6px; }
.opinion { color:#606266; }
</style>

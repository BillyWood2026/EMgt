<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">我的投稿：草稿可编辑提交 → 部门初审 → 子公司复审 → 集团终审；驳回后修改可重新提交；发布后不可编辑仅可申请归档</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <span class="tip">共 {{ rows.length }} 篇投稿</span>
        <span style="flex:1"></span>
        <el-button size="small" type="primary" @click="openCreate">我要投稿</el-button>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="caseTitle" label="案例标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.caseType==='正向标杆' ? 'success':'danger'">{{ row.caseType }}</el-tag></template>
        </el-table-column>
        <el-table-column label="标签" min-width="160">
          <template #default="{row}">
            <el-tag v-for="t in splitTags(row.valueTags)" :key="t" size="small" effect="plain" class="mr4">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            <el-tag v-if="row.status==='待评审'" size="small" type="warning" class="mt2">第{{ row.stage }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="90">
          <template #default="{row}"><span class="pts">{{ row.pointTotal || 0 }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status==='草稿'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status==='草稿'" size="small" type="primary" @click="doSubmit(row)">提交评审</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑案例' : '我要投稿'" width="760px" top="6vh">
      <el-alert v-if="form.id && form.rejectReason" type="error" :closable="false" class="mb"
        :title="'上次驳回意见：' + form.rejectReason" />
      <el-form :model="form" label-width="92px" size="small">
        <el-form-item label="案例标题" required>
          <el-input v-model="form.caseTitle" maxlength="120" show-word-limit placeholder="20-120字，概括事件与价值观" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="案例类型" required>
              <el-radio-group v-model="form.caseType" @change="onTypeChange">
                <el-radio label="正向标杆">正向标杆</el-radio>
                <el-radio label="反面警示">反面警示</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="事件时间" required>
              <el-date-picker v-model="form.occurTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"
                :disabled-date="futureOnly" placeholder="不能晚于当前时间" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="所属组织"><el-input v-model="form.orgName" placeholder="如：XX子公司XX部" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="价值观标签" required>
          <el-select v-model="tagArr" multiple collapse-tags placeholder="至少选择1个（可多选）" style="width:100%">
            <el-option v-for="t in tagOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="案例正文" required>
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="4000" show-word-limit placeholder="不少于80字：背景、行动、结果、价值观体现" />
        </el-form-item>
        <el-form-item label="佐证附件" required>
          <div class="att-box">
            <div v-for="(a,i) in attArr" :key="i" class="att-row">
              <el-input v-model="attArr[i]" size="small" placeholder="附件地址/文件名（至少1个）" />
              <el-button size="small" link type="danger" @click="attArr.splice(i,1)">删除</el-button>
            </div>
            <el-button size="small" @click="attArr.push('')">+ 添加附件</el-button>
          </div>
        </el-form-item>
        <el-form-item label="公开范围">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0"
            :disabled="form.caseType==='反面警示'" active-text="广场公开" inactive-text="仅内部可见" />
          <span class="tip">反面警示案例强制内部可见</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVis" size="520px" :title="cur.caseTitle || '案例详情'">
      <div v-if="cur.id">
        <div class="d-row">
          <el-tag size="small" :type="cur.caseType==='正向标杆' ? 'success':'danger'">{{ cur.caseType }}</el-tag>
          <el-tag size="small" :type="statusType(cur.status)">{{ cur.status }}</el-tag>
          <el-tag v-if="cur.status==='待评审'" size="small" type="warning">第{{ cur.stage }}级评审</el-tag>
        </div>
        <el-alert v-if="cur.rejectReason" type="error" :closable="false" class="d-block" :title="'驳回意见：'+cur.rejectReason" />
        <el-descriptions :column="1" border size="small" class="d-block">
          <el-descriptions-item label="标签">{{ cur.valueTags }}</el-descriptions-item>
          <el-descriptions-item label="事件时间">{{ fmt(cur.occurTime) }}</el-descriptions-item>
          <el-descriptions-item label="积分明细">{{ cur.pointDetail || '尚未发布' }}</el-descriptions-item>
        </el-descriptions>
        <div class="d-block sec-title">正文</div>
        <div class="d-content">{{ cur.content }}</div>
        <div class="d-block sec-title">评审流程</div>
        <el-timeline v-if="logs(cur.approvalLog).length">
          <el-timeline-item v-for="(l,i) in logs(cur.approvalLog)" :key="i" :type="l.pass?'success':'danger'" :timestamp="fmt(l.time)">
            第{{ l.stage }}级 · {{ l.pass?'通过':'驳回' }} · {{ l.by }}
            <div v-if="l.opinion" class="opinion">{{ l.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="tip">暂无评审记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const rows = ref([])
const loading = ref(false)
const tagOptions = ref(['以客户为中心', '以奋斗者为本', '长期艰苦奋斗', '坚持自我批判'])
const formVis = ref(false)
const detailVis = ref(false)
const cur = ref({})
const form = reactive({ id: null, caseTitle: '', caseType: '正向标杆', occurTime: '', orgName: '', content: '', isPublic: 1 })
const tagArr = ref([])
const attArr = ref([])

function splitTags(csv) { return (csv || '').split(',').map(s => s.trim()).filter(Boolean) }
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function logs(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function futureOnly(d) { return d.getTime() > Date.now() }
function statusType(s) {
  if (s === '已发布') return 'success'
  if (s === '待评审') return 'warning'
  if (s === '已归档') return 'info'
  return ''
}
async function load() {
  loading.value = true
  try { const r = await axios.get('/api/culture/case/mine'); rows.value = r.data.data || [] }
  finally { loading.value = false }
}
async function loadTags() {
  const r = await axios.get('/api/culture/case-tag/list', { params: { status: '启用' } })
  if (r.data.data && r.data.data.length) tagOptions.value = r.data.data.map(t => t.tagName)
}
function resetForm() {
  Object.assign(form, { id: null, caseTitle: '', caseType: '正向标杆', occurTime: '', orgName: '', content: '', isPublic: 1 })
  tagArr.value = []
  attArr.value = []
}
function openCreate() { resetForm(); formVis.value = true }
function openEdit(row) {
  Object.assign(form, {
    id: row.id, caseTitle: row.caseTitle, caseType: row.caseType, occurTime: row.occurTime,
    orgName: row.orgName, content: row.content, isPublic: row.isPublic, rejectReason: row.rejectReason
  })
  tagArr.value = splitTags(row.valueTags)
  try { const v = JSON.parse(row.attachments || '[]'); attArr.value = Array.isArray(v) ? v : [] } catch { attArr.value = [] }
  formVis.value = true
}
function onTypeChange(v) { if (v === '反面警示') form.isPublic = 0 }
async function doSave() {
  const titleLen = (form.caseTitle || '').length
  if (titleLen < 20 || titleLen > 120) return ElMessage.warning('案例标题需20-120字')
  if (!tagArr.value.length) return ElMessage.warning('请至少选择1个价值观标签')
  if (!form.occurTime) return ElMessage.warning('请选择事件发生时间')
  if ((form.content || '').length < 80) return ElMessage.warning('案例正文不少于80字')
  const atts = attArr.value.map(a => a.trim()).filter(Boolean)
  if (!atts.length) return ElMessage.warning('请至少添加1个佐证附件')
  const payload = { ...form, valueTags: tagArr.value.join(','), attachments: JSON.stringify(atts) }
  await axios({ url: form.id ? '/api/culture/case/' + form.id : '/api/culture/case', method: form.id ? 'put' : 'post', data: payload })
  ElMessage.success('草稿已保存')
  formVis.value = false
  load()
}
async function doSubmit(row) {
  await ElMessageBox.confirm('提交后进入三级评审，评审期间案例锁定不可编辑，确认提交？', '提交评审', { type: 'warning' })
  await axios.post(`/api/culture/case/${row.id}/submit`)
  ElMessage.success('已提交，等待部门初审')
  load()
}
function openDetail(row) { cur.value = row; detailVis.value = true }
onMounted(() => { load(); loadTags() })
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; margin-bottom:10px; }
.tip { color:#909399; font-size:12px; }
.mb { margin-bottom:10px; }
.mr4 { margin-right:4px; }
.mt2 { margin-top:2px; }
.pts { font-weight:700; color:var(--el-color-warning); }
.att-box { width:100%; }
.att-row { display:flex; gap:8px; margin-bottom:6px; }
.d-row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; }
.d-block { margin-top:12px; }
.sec-title { font-weight:600; margin-bottom:8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.opinion { color:#606266; }
</style>

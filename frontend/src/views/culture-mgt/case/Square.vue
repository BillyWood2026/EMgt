<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">案例广场：汇聚经三级评审发布的价值观标杆案例；反面警示案例默认内部可见，不对外公开</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-radio-group v-model="q.type" size="small" @change="load">
          <el-radio-button label="">全部类型</el-radio-button>
          <el-radio-button label="正向标杆">正向标杆</el-radio-button>
          <el-radio-button label="反面警示">反面警示</el-radio-button>
        </el-radio-group>
        <el-select v-model="q.tag" placeholder="价值观标签" clearable size="small" style="width:180px" @change="load">
          <el-option v-for="t in tags" :key="t" :label="t" :value="t" />
        </el-select>
        <el-switch v-if="can('C02','C03','C99')" v-model="showInternal" active-text="含内部反面案例" @change="load" />
        <span style="flex:1"></span>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="wall">
      <el-card v-for="c in rows" :key="c.id" shadow="hover" class="case-card" :class="{ neg: c.caseType==='反面警示' }" @click="openDetail(c)">
        <div class="cc-head">
          <el-tag size="small" :type="c.caseType==='正向标杆' ? 'success' : 'danger'">{{ c.caseType }}</el-tag>
          <el-tag v-if="c.isPublic===0" size="small" type="info" effect="plain">内部可见</el-tag>
          <span class="pts">+{{ c.pointTotal || 0 }} 分</span>
        </div>
        <div class="cc-title">{{ c.caseTitle }}</div>
        <div class="cc-tags">
          <el-tag v-for="t in splitTags(c.valueTags)" :key="t" size="small" effect="plain" round>{{ t }}</el-tag>
        </div>
        <div class="cc-content">{{ c.content }}</div>
        <div class="cc-foot">
          <span>{{ c.contributor }} · {{ c.orgName }}</span>
          <span>{{ fmt(c.occurTime) }}</span>
        </div>
      </el-card>
      <el-empty v-if="!loading && rows.length===0" description="暂无已发布案例" />
    </div>

    <el-drawer v-model="detailVis" size="560px" :title="cur.caseTitle || '案例详情'">
      <div v-if="cur.id" class="detail">
        <div class="d-row">
          <el-tag size="small" :type="cur.caseType==='正向标杆' ? 'success' : 'danger'">{{ cur.caseType }}</el-tag>
          <el-tag v-for="t in splitTags(cur.valueTags)" :key="t" size="small" effect="plain" round>{{ t }}</el-tag>
          <el-tag v-if="cur.isPublic===0" size="small" type="info">内部可见</el-tag>
          <el-tag size="small" type="warning">积分 {{ cur.pointTotal || 0 }}</el-tag>
        </div>
        <el-descriptions :column="2" border size="small" class="d-block">
          <el-descriptions-item label="投稿人">{{ cur.contributor }}</el-descriptions-item>
          <el-descriptions-item label="所属组织">{{ cur.orgName }}</el-descriptions-item>
          <el-descriptions-item label="事件时间">{{ fmt(cur.occurTime) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ fmt(cur.publishTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="d-block sec-title">案例正文</div>
        <div class="d-content">{{ cur.content }}</div>
        <div class="d-block sec-title">佐证附件</div>
        <div v-if="attachList(cur.attachments).length">
          <div v-for="(a,i) in attachList(cur.attachments)" :key="i" class="att">
            <el-icon><Paperclip /></el-icon><span>{{ a }}</span>
          </div>
        </div>
        <el-empty v-else description="无附件" :image-size="60" />
        <div class="d-block sec-title">积分构成</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="明细">{{ cur.pointDetail || '—' }}</el-descriptions-item>
          <el-descriptions-item label="新闻引用">{{ cur.refNewsCount || 0 }} 次</el-descriptions-item>
          <el-descriptions-item label="培训引用">{{ cur.refTrainingCount || 0 }} 次</el-descriptions-item>
        </el-descriptions>
        <div class="d-block sec-title">评审记录</div>
        <el-timeline v-if="logs(cur.approvalLog).length">
          <el-timeline-item v-for="(l,i) in logs(cur.approvalLog)" :key="i" :type="l.pass ? 'success' : 'danger'" :timestamp="fmt(l.time)">
            第{{ l.stage }}级评审 · {{ l.pass ? '通过' : '驳回' }} · {{ l.by }}
            <div v-if="l.opinion" class="opinion">意见：{{ l.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="无评审记录" :image-size="60" />
        <div class="d-block" style="margin-top:16px; text-align:right">
          <el-button v-if="can('C03','C99')" size="small" type="primary" plain @click="doCite">引用到新闻/培训</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox, ElRadioGroup, ElRadioButton } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const TAGS = ['以客户为中心', '以奋斗者为本', '长期艰苦奋斗', '坚持自我批判']
const rows = ref([])
const tags = ref(TAGS)
const loading = ref(false)
const q = reactive({ type: '', tag: '' })
const showInternal = ref(false)
const detailVis = ref(false)
const cur = ref({})

function splitTags(csv) { return (csv || '').split(',').map(s => s.trim()).filter(Boolean) }
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function attachList(json) {
  try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] }
}
function logs(json) {
  try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] }
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/case/square', {
      params: { type: q.type || undefined, tag: q.tag || undefined, publicOnly: !showInternal.value }
    })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openDetail(c) { cur.value = c; detailVis.value = true }

async function doCite() {
  const citeType = ref('news')
  try {
    await ElMessageBox({
      title: '引用案例',
      message: h('div', { style: 'padding:6px 0' }, [
        h('div', { style: 'margin-bottom:12px;color:#606266' }, '请选择引用渠道（新闻稿 +5 分 / 培训课件 +3 分）'),
        h(ElRadioGroup, { modelValue: citeType.value, 'onUpdate:modelValue': v => { citeType.value = v } }, () => [
          h(ElRadioButton, { value: 'news' }, () => '新闻稿'),
          h(ElRadioButton, { value: 'training' }, () => '培训课件')
        ])
      ]),
      confirmButtonText: '确认引用',
      cancelButtonText: '取消'
    })
    const r = await axios.post(`/api/culture/case/${cur.value.id}/cite`, null, { params: { type: citeType.value } })
    if (r.data.code === 200) {
      ElMessage.success('引用成功，案例引用计数已更新')
      openDetail({ ...cur.value })
      load()
    } else {
      ElMessage.error(r.data.message || '引用失败')
    }
  } catch (e) { if (e !== 'cancel' && e !== 'close') ElMessage.error('引用失败') }
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.mb { margin-bottom:12px; }
.wall { display:grid; grid-template-columns:repeat(auto-fill,minmax(320px,1fr)); gap:12px; }
.case-card { cursor:pointer; transition:transform .15s; }
.case-card:hover { transform:translateY(-2px); }
.case-card.neg { border-left:3px solid var(--el-color-danger); }
.cc-head { display:flex; align-items:center; gap:6px; }
.pts { margin-left:auto; font-weight:700; color:var(--el-color-warning); }
.cc-title { font-size:15px; font-weight:600; margin:8px 0 6px; line-height:1.5; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.cc-tags { display:flex; gap:6px; flex-wrap:wrap; margin-bottom:6px; }
.cc-content { color:#606266; font-size:13px; line-height:1.6; height:62px; overflow:hidden; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; }
.cc-foot { display:flex; justify-content:space-between; color:#909399; font-size:12px; margin-top:8px; }
.d-row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; }
.d-block { margin-top:14px; }
.sec-title { font-weight:600; margin-bottom:8px; color:#303133; }
.d-content { white-space:pre-wrap; line-height:1.8; color:#303133; background:#f7f9fc; padding:10px; border-radius:6px; }
.att { display:flex; align-items:center; gap:6px; color:var(--el-color-primary); font-size:13px; padding:3px 0; }
.opinion { color:#606266; }
</style>

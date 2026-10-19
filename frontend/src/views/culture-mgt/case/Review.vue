<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">三级评审：①部门初审(部门负责人C04) → ②子公司文化专员复审(C02) → ③集团文化岗终审发布(C03)；驳回必须填写意见</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="待评审" name="待评审" />
        <el-tab-pane label="已发布" name="已发布" />
        <el-tab-pane label="草稿" name="草稿" />
        <el-tab-pane label="已归档" name="已归档" />
      </el-tabs>
      <div class="bar">
        <el-radio-group v-model="q.type" size="small" @change="load">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="正向标杆">正向</el-radio-button>
          <el-radio-button label="反面警示">反面</el-radio-button>
        </el-radio-group>
        <el-input v-model="q.keyword" placeholder="标题关键词" size="small" clearable style="width:200px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="caseTitle" label="案例标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.caseType==='正向标杆' ? 'success':'danger'">{{ row.caseType }}</el-tag></template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{row}"><el-tag v-for="t in splitTags(row.valueTags)" :key="t" size="small" effect="plain" class="mr4">{{ t }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="contributor" label="投稿人" width="110" />
        <el-table-column prop="orgName" label="组织" width="150" show-overflow-tooltip />
        <el-table-column label="评审节点" width="170">
          <template #default="{row}">
            <template v-if="row.status==='待评审'">
              <el-tag size="small" :type="myStage(row)!==null ? 'danger' : 'info'" effect="plain">
                {{ stageName(row.stage) }}
              </el-tag>
              <el-tag v-if="myStage(row)!==null" size="small" type="danger" class="ml4">我的待办</el-tag>
            </template>
            <span v-else class="tip">—</span>
          </template>
        </el-table-column>
        <el-table-column label="积分" width="80">
          <template #default="{row}"><span class="pts">{{ row.pointTotal || 0 }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status==='待评审' && myStage(row)!==null" size="small" type="primary" @click="openApprove(row)">评审</el-button>
            <el-button v-if="(row.status==='已发布'||row.status==='已归档') && can('C02','C03','C99')"
              size="small" :type="row.status==='已发布'?'danger':''" @click="doToggle(row)">
              {{ row.status==='已发布' ? '归档' : '恢复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVis" size="520px" :title="cur.caseTitle || '案例详情'">
      <div v-if="cur.id">
        <div class="d-row">
          <el-tag size="small" :type="cur.caseType==='正向标杆' ? 'success':'danger'">{{ cur.caseType }}</el-tag>
          <el-tag size="small" type="info">{{ cur.status }}</el-tag>
          <el-tag v-if="cur.isPublic===0" size="small" type="warning">内部可见</el-tag>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="投稿人">{{ cur.contributor }}</el-descriptions-item>
          <el-descriptions-item label="组织">{{ cur.orgName }}</el-descriptions-item>
          <el-descriptions-item label="事件时间">{{ fmt(cur.occurTime) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ fmt(cur.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="sec-title">正文</div>
        <div class="d-content">{{ cur.content }}</div>
        <div class="sec-title">附件</div>
        <div v-if="attachList(cur.attachments).length">
          <div v-for="(a,i) in attachList(cur.attachments)" :key="i" class="att">{{ a }}</div>
        </div>
        <div v-else class="tip">无</div>
        <div class="sec-title">评审记录</div>
        <el-timeline v-if="logs(cur.approvalLog).length">
          <el-timeline-item v-for="(l,i) in logs(cur.approvalLog)" :key="i" :type="l.pass?'success':'danger'" :timestamp="fmt(l.time)">
            {{ stageName(l.stage) }} · {{ l.pass?'通过':'驳回' }} · {{ l.by }}
            <div v-if="l.opinion" class="opinion">{{ l.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="tip">暂无</div>
        <div v-if="cur.status==='待评审' && myStage(cur)!==null" class="d-foot">
          <el-button size="small" type="danger" @click="openApprove(cur)">进入评审</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="apVis" :title="'评审：' + stageName(apRow.stage)" width="520px">
      <div class="ap-box">
        <div class="tip">当前案例：{{ apRow.caseTitle }}</div>
        <el-radio-group v-model="apPass" class="mt">
          <el-radio :label="true">评审通过（进入下一节点/终审发布）</el-radio>
          <el-radio :label="false">驳回（退回投稿人草稿，须说明原因）</el-radio>
        </el-radio-group>
        <el-input v-model="opinion" type="textarea" :rows="4" maxlength="500" show-word-limit class="mt"
          :placeholder="apPass ? '可填写评审意见（选填）' : '驳回意见不少于5字（必填）'" />
      </div>
      <template #footer>
        <el-button size="small" @click="apVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doApprove">确认提交</el-button>
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
const tab = ref('待评审')
const q = reactive({ type: '', keyword: '' })
const detailVis = ref(false)
const cur = ref({})
const apVis = ref(false)
const apRow = ref({})
const apPass = ref(true)
const opinion = ref('')

function splitTags(csv) { return (csv || '').split(',').map(s => s.trim()).filter(Boolean) }
function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function attachList(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function logs(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function stageName(s) {
  if (s === 1) return '①部门初审'
  if (s === 2) return '②子公司复审'
  if (s === 3) return '③集团终审'
  return '未提交'
}
/** 当前角色是否是该节点经办人；C99全通。 */
function myStage(row) {
  if (row.status !== '待评审') return null
  const s = row.stage
  if (s === 1 && can('C04', 'C99')) return s
  if (s === 2 && can('C02', 'C99')) return s
  if (s === 3 && can('C03', 'C99')) return s
  return null
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/case/list', { params: { status: tab.value, type: q.type || undefined, keyword: q.keyword || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openDetail(row) { cur.value = row; detailVis.value = true }
function openApprove(row) { apRow.value = row; apPass.value = true; opinion.value = ''; apVis.value = true }
async function doApprove() {
  if (!apPass.value && (opinion.value || '').trim().length < 5) {
    return ElMessage.warning('驳回必须填写不少于5字的意见')
  }
  await axios.post(`/api/culture/case/${apRow.value.id}/approve`, null, { params: { pass: apPass.value, opinion: opinion.value } })
  ElMessage.success(apPass.value ? '评审通过' : '已驳回投稿人')
  apVis.value = false
  detailVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/case/${row.id}/toggle-archive`)
  ElMessage.success(row.status === '已发布' ? '已归档' : '已恢复发布')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
.mt { margin-top:8px; }
.mr4 { margin-right:4px; }
.ml4 { margin-left:4px; }
.tip { color:#909399; font-size:12px; }
.pts { font-weight:700; color:var(--el-color-warning); }
.d-row { display:flex; gap:8px; flex-wrap:wrap; margin-bottom:12px; }
.sec-title { font-weight:600; margin:14px 0 8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.att { font-size:13px; color:var(--el-color-primary); padding:2px 0; }
.opinion { color:#606266; }
.d-foot { margin-top:16px; text-align:right; }
</style>

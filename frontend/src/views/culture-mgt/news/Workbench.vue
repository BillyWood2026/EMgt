<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">新闻稿件工作台：草稿→待审核→已发布→已下线归档 全流程管理（版本快照+审批留痕）</el-tag>
    </RoleBar>

    <!-- 统计KPI -->
    <el-row v-if="stats" :gutter="8" class="mt">
      <el-col v-for="k in kpis" :key="k.label" :span="3">
        <el-card shadow="never" class="kpi">
          <div class="kpi-num" :class="k.cls">{{ k.value }}</div>
          <div class="kpi-label">{{ k.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <div v-if="stats" class="bar mt">
      <span class="dist-label">类型占比：</span>
      <div v-for="t in stats.byType" :key="t.type" class="dist-item">
        <span class="dist-name">{{ t.type }}</span>
        <el-progress :percentage="stats.total ? Math.round(t.count * 100 / stats.total) : 0" :stroke-width="8" style="width:110px" />
        <span class="dist-cnt">{{ t.count }}篇</span>
      </div>
      <el-divider direction="vertical" />
      <el-tag v-for="o in stats.byOrg" :key="o.org" size="small" effect="plain" style="margin-right:6px">
        {{ o.org }}：{{ o.count }}篇 / 阅读{{ o.read }}
      </el-tag>
    </div>

    <div class="bar mt">
      <el-input v-model="query.keyword" size="small" placeholder="搜索标题/摘要" clearable style="width:180px" @change="load" />
      <el-select v-model="query.status" size="small" placeholder="全部状态" clearable style="width:130px" @change="load">
        <el-option v-for="s in STATUS" :key="s" :label="s" :value="s" />
      </el-select>
      <el-select v-model="query.newsType" size="small" placeholder="全部类型" clearable style="width:120px" @change="load">
        <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="query.sourceOrg" size="small" placeholder="全部来源组织" clearable style="width:140px" @change="load">
        <el-option v-for="o in ORGS" :key="o" :label="o" :value="o" />
      </el-select>
      <el-button size="small" @click="load">查询</el-button>
      <el-button v-if="canStaff" size="small" type="primary" @click="openCreate">新建稿件</el-button>
      <el-button v-if="canHQ" size="small" @click="exportCsv">导出CSV</el-button>
    </div>

    <el-table :data="rows" size="small" border v-loading="loading" class="mt">
      <el-table-column prop="newsNo" label="编号" width="150" />
      <el-table-column label="标题" min-width="200" show-overflow-tooltip>
        <template #default="{row}">
          <el-icon v-if="row.isTop === 1" color="#e6a23c" title="置顶"><StarFilled /></el-icon>
          <span>{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="newsType" label="类型" width="90" align="center">
        <template #default="{row}"><el-tag size="small" type="info">{{ row.newsType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="sourceOrg" label="来源组织" width="110" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{row}">
          <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
          <div v-if="row.status === '待审核'" class="node-hint">{{ stageNode(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="阅读/点赞" width="110" align="center">
        <template #default="{row}"><span title="PC/移动/点赞">👁{{ row.readCount }} 📱{{ row.mobileReadCount }} 👍{{ row.likeCount }}</span></template>
      </el-table-column>
      <el-table-column prop="creator" label="撰稿人" width="90" />
      <el-table-column prop="updateTime" label="更新时间" width="150" />
      <el-table-column label="操作" width="270" fixed="right">
        <template #default="{row}">
          <el-button size="small" link @click="openDetail(row)">详情</el-button>
          <el-button v-if="canStaff && row.status === '草稿'" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button v-if="canStaff && row.status === '草稿'" size="small" type="primary" link @click="doSubmit(row)">提交审批</el-button>
          <el-button v-if="canStaff && row.status === '待审核'" size="small" link @click="doWithdraw(row)">撤回</el-button>
          <template v-if="row.status === '待审核' && canApprove(row)">
            <el-button size="small" type="success" link @click="doApprove(row)">通过</el-button>
            <el-button size="small" type="danger" link @click="openReject(row)">驳回</el-button>
          </template>
          <el-button v-if="canHQ && row.status === '已发布'" size="small" type="warning" link @click="openOffline(row)">下线归档</el-button>
          <el-popconfirm v-if="canStaff && row.status === '草稿'" title="确定删除该草稿？" @confirm="doDelete(row)">
            <template #reference><el-button size="small" type="danger" link>删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="showForm" :title="form.id ? '编辑稿件' : '新建稿件'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="10-120字" />
        </el-form-item>
        <el-form-item label="摘要" required>
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="300" show-word-limit placeholder="30-300字" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="支持HTML片段" />
        </el-form-item>
        <el-form-item label="新闻类型" required>
          <el-select v-model="form.newsType" style="width:100%">
            <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源组织" required>
          <el-select v-model="form.sourceOrg" style="width:100%">
            <el-option v-for="o in ORGS" :key="o" :label="o" :value="o" :disabled="!isHqRole && o === '集团总部'" />
          </el-select>
          <div v-if="!isHqRole" class="hint">子公司文化专员不可归属集团总部（E-NW-06）</div>
        </el-form-item>
        <el-form-item label="价值观标签">
          <el-select v-model="form.tagArr" multiple style="width:100%" placeholder="选择启用中的标签">
            <el-option v-for="t in enabledTags" :key="t.id" :label="t.tagName" :value="t.tagName" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联案例ID">
          <el-input-number v-model="form.relCaseId" :min="1" controls-position="right" placeholder="可选，发布后案例引用计数+1" style="width:200px" />
          <span class="hint" style="margin-left:8px">发布后案例被新闻引用计数+1</span>
        </el-form-item>
        <el-form-item label="关联榜样ID">
          <el-input-number v-model="form.relModelId" :min="1" controls-position="right" placeholder="可选" style="width:200px" />
        </el-form-item>
        <el-form-item label="发布设置">
          <el-checkbox v-model="form.isMobileShow">移动端同步展示</el-checkbox>
          <el-checkbox v-model="form.isTop">置顶（同组织上限5条）</el-checkbox>
        </el-form-item>
        <el-form-item label="计划发布时间">
          <el-date-picker v-model="form.planPublishTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="可选，不可早于当前" style="width:200px" />
        </el-form-item>
        <el-form-item label="内部备注">
          <el-input v-model="form.innerNote" type="textarea" :rows="2" placeholder="仅后台可见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showForm = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave(false)">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：5个页签 -->
    <el-drawer v-model="showDetail" :title="dNews ? dNews.newsNo + ' · 详情' : '稿件详情'" size="620px">
      <el-tabs v-if="dNews" v-model="dTab">
        <el-tab-pane label="正文预览" name="content">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="标题" :span="2">{{ dNews.title }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ dNews.newsType }}</el-descriptions-item>
            <el-descriptions-item label="来源">{{ dNews.sourceOrg }}</el-descriptions-item>
            <el-descriptions-item label="状态"><el-tag size="small" :type="statusType(dNews.status)">{{ dNews.status }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="撰稿人">{{ dNews.creator }}</el-descriptions-item>
          </el-descriptions>
          <p class="mt"><b>摘要：</b>{{ dNews.summary }}</p>
          <div class="content" v-html="dNews.content"></div>
        </el-tab-pane>
        <el-tab-pane label="审批流转" name="approval">
          <el-empty v-if="!approvals.length" description="暂无审批记录" />
          <el-timeline v-else style="padding-left:8px">
            <el-timeline-item v-for="a in approvals" :key="a.id" :timestamp="a.approveTime" :type="a.action === '通过' ? 'success' : 'danger'">
              <b>{{ a.nodeName }}</b> · {{ a.approver }}
              <el-tag size="small" :type="a.action === '通过' ? 'success' : 'danger'" style="margin-left:6px">{{ a.action }}</el-tag>
              <div v-if="a.opinion" class="opinion">意见：{{ a.opinion }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
        <el-tab-pane label="关联引用" name="rel">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="关联案例">{{ dNews.relCaseId ? (relInfo.relCaseTitle || ('案例#' + dNews.relCaseId)) : '—' }}</el-descriptions-item>
            <el-descriptions-item label="关联榜样">{{ dNews.relModelId ? (relInfo.relModelName || ('榜样#' + dNews.relModelId)) : '—' }}</el-descriptions-item>
          </el-descriptions>
          <div class="hint mt">发布后关联案例的被新闻引用计数自动+1（跨模块联动）</div>
        </el-tab-pane>
        <el-tab-pane label="阅读统计" name="stat">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="PC阅读">{{ dNews.readCount }}</el-descriptions-item>
            <el-descriptions-item label="移动端阅读">{{ dNews.mobileReadCount }}</el-descriptions-item>
            <el-descriptions-item label="点赞">{{ dNews.likeCount }}</el-descriptions-item>
            <el-descriptions-item label="移动端展示">{{ dNews.isMobileShow === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="计划发布">{{ dNews.planPublishTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ dNews.publishTime || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="dNews.offlineReason" label="下线原因" :span="2">{{ dNews.offlineReason }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="版本历史" name="ver">
          <el-table :data="versions" size="small" border>
            <el-table-column prop="verNo" label="版本" width="60" align="center" />
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="editor" label="编辑人" width="90" />
            <el-table-column prop="editTime" label="编辑时间" width="150" />
          </el-table>
          <div class="hint mt">每次保存自动生成版本快照，永久保存不可删除</div>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showReject" title="驳回稿件" width="480px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="驳回意见" required>
          <el-input v-model="rejectOpinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="必填20-500字，将随记录永久留痕" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showReject = false">取消</el-button>
        <el-button size="small" type="danger" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 下线归档弹窗 -->
    <el-dialog v-model="showOffline" title="下线归档" width="480px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="下线原因" required>
          <el-input v-model="offlineReason" type="textarea" :rows="3" placeholder="必填，归档后仅可查看不可编辑删除" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showOffline = false">取消</el-button>
        <el-button size="small" type="warning" @click="doOffline">确认下线</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { StarFilled } from '@element-plus/icons-vue'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const TYPES = ['文化动态', '榜样事迹', '高管寄语', '文化专题', '活动报道']
const ORGS = ['集团总部', '华东子公司', '华南子公司', '西部子公司']
const STATUS = ['草稿', '待审核', '已发布', '已下线归档']

const loading = ref(false)
const rows = ref([])
const query = reactive({ keyword: '', status: '', newsType: '', sourceOrg: '' })
const stats = ref(null)
const tags = ref([])

const canStaff = computed(() => can('C02', 'C03', 'C99'))
const canHQ = computed(() => can('C03', 'C99'))
const isHqRole = computed(() => can('C03', 'C99'))

const kpis = computed(() => !stats.value ? [] : [
  { label: '稿件总量', value: stats.value.total },
  { label: '草稿', value: stats.value.draft },
  { label: '待审核', value: stats.value.pending, cls: 'warn' },
  { label: '已发布', value: stats.value.published, cls: 'ok' },
  { label: '已下线归档', value: stats.value.offline },
  { label: 'PC总阅读', value: stats.value.totalRead },
  { label: '移动端阅读', value: stats.value.mobileRead },
  { label: '总点赞', value: stats.value.totalLike }
])

const showForm = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', summary: '', content: '', newsType: '文化动态', sourceOrg: '', tagArr: [], relCaseId: null, relModelId: null, isMobileShow: false, isTop: false, planPublishTime: null, innerNote: '' })

const showDetail = ref(false)
const dTab = ref('content')
const dNews = ref(null)
const approvals = ref([])
const versions = ref([])
const relInfo = ref({})

const showReject = ref(false)
const rejectRow = ref(null)
const rejectOpinion = ref('')

const showOffline = ref(false)
const offlineRow = ref(null)
const offlineReason = ref('')

const enabledTags = computed(() => tags.value.filter(t => t.enabled === 1))

function statusType(s) {
  return { '草稿': 'info', '待审核': 'warning', '已发布': 'success', '已下线归档': 'danger' }[s] || 'info'
}

function stageNode(row) {
  const hq = row.sourceOrg === '集团总部'
  const s = row.stage || 0
  if (hq) return s === 1 ? '待集团文化管理员审核' : '待分管高管终审'
  return ['待子公司负责人初审', '待集团文化管理员复核', '待集团高管终审'][s - 1] || ''
}

function canApprove(row) {
  const hq = row.sourceOrg === '集团总部'
  const s = row.stage || 0
  const roles = hq ? (s === 1 ? ['C03'] : ['C07']) : (s === 1 ? ['C04'] : s === 2 ? ['C03'] : ['C07'])
  return can(...roles)
}

function load() {
  loading.value = true
  const params = new URLSearchParams()
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.status) params.set('status', query.status)
  if (query.newsType) params.set('newsType', query.newsType)
  if (query.sourceOrg) params.set('sourceOrg', query.sourceOrg)
  axios.get(`/api/culture/news/list?${params}`)
    .then(r => { rows.value = r.data.data || [] })
    .catch(e => ElMessage.error(e.response?.data?.message || '加载失败'))
    .finally(() => { loading.value = false })
}

function loadStats() {
  axios.get('/api/culture/news/stats')
    .then(r => { stats.value = r.data.data })
    .catch(() => { stats.value = null })
}

function loadTags() {
  axios.get('/api/culture/news/tag/list')
    .then(r => { tags.value = r.data.data || [] })
}

function openCreate() {
  Object.assign(form, {
    id: null, title: '', summary: '', content: '', newsType: '文化动态',
    sourceOrg: isHqRole.value ? '集团总部' : '华东子公司', tagArr: [],
    relCaseId: null, relModelId: null, isMobileShow: true, isTop: false,
    planPublishTime: null, innerNote: ''
  })
  showForm.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id, title: row.title, summary: row.summary, content: row.content,
    newsType: row.newsType, sourceOrg: row.sourceOrg, tagArr: parseTags(row.valueTags),
    relCaseId: row.relCaseId, relModelId: row.relModelId,
    isMobileShow: row.isMobileShow === 1, isTop: row.isTop === 1,
    planPublishTime: row.planPublishTime, innerNote: row.innerNote
  })
  showForm.value = true
}

function parseTags(json) {
  try { return JSON.parse(json || '[]') } catch { return [] }
}

function doSave(confirmDuplicate) {
  if (!form.title || form.title.trim().length < 10) { ElMessage.warning('标题需10-120字'); return }
  if (!form.summary || form.summary.trim().length < 30) { ElMessage.warning('摘要需30-300字'); return }
  if (!form.content) { ElMessage.warning('正文内容必填'); return }
  saving.value = true
  axios.post('/api/culture/news', {
    id: form.id, title: form.title, summary: form.summary, content: form.content,
    newsType: form.newsType, sourceOrg: form.sourceOrg,
    valueTags: JSON.stringify(form.tagArr), relCaseId: form.relCaseId, relModelId: form.relModelId,
    isMobileShow: form.isMobileShow ? 1 : 0, isTop: form.isTop ? 1 : 0,
    planPublishTime: form.planPublishTime, innerNote: form.innerNote,
    confirmDuplicate: !!confirmDuplicate
  }).then(r => {
    ElMessage.success('保存成功，已自动生成版本快照')
    showForm.value = false
    load()
    loadStats()
  }).catch(e => {
    const d = e.response?.data
    if (d?.data?.errorCode === 'E-NW-18') {
      ElMessageBox.confirm(d.message || '存在相同标题的稿件，是否确认重复提交？', '重复标题预警', { type: 'warning', confirmButtonText: '确认提交', cancelButtonText: '返回修改' })
        .then(() => doSave(true))
        .catch(() => {})
    } else {
      ElMessage.error(d?.message || '保存失败')
    }
  }).finally(() => { saving.value = false })
}

function doSubmit(row) {
  axios.post(`/api/culture/news/${row.id}/submit`)
    .then(() => { ElMessage.success('已提交审批，进入第1审批节点'); load(); loadStats() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function doWithdraw(row) {
  axios.post(`/api/culture/news/${row.id}/withdraw`)
    .then(() => { ElMessage.success('已撤回草稿'); load(); loadStats() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function doApprove(row) {
  ElMessageBox.confirm(`确认在节点【${stageNode(row).replace('待', '')}】通过《${row.title}》？`, '审批确认', { type: 'success' })
    .then(() => axios.post(`/api/culture/news/${row.id}/approve?pass=true`))
    .then(r => {
      const d = r.data.data
      ElMessage.success(d.status === '已发布' ? '终审通过，稿件已发布' : '已通过，流转至下一节点')
      load()
      loadStats()
    })
    .catch(e => { if (e?.response) ElMessage.error(e.response?.data?.message || '操作失败') })
}

function openReject(row) {
  rejectRow.value = row
  rejectOpinion.value = ''
  showReject.value = true
}

function doReject() {
  const len = rejectOpinion.value.trim().length
  if (len < 20 || len > 500) { ElMessage.warning('驳回意见必填且需20-500字'); return }
  axios.post(`/api/culture/news/${rejectRow.value.id}/approve?pass=false&opinion=${encodeURIComponent(rejectOpinion.value.trim())}`)
    .then(() => { ElMessage.success('已驳回退回草稿，意见已留痕'); showReject.value = false; load(); loadStats() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function openOffline(row) {
  offlineRow.value = row
  offlineReason.value = ''
  showOffline.value = true
}

function doOffline() {
  if (!offlineReason.value.trim()) { ElMessage.warning('下线原因必填'); return }
  axios.post(`/api/culture/news/${offlineRow.value.id}/offline`, { reason: offlineReason.value.trim() })
    .then(() => { ElMessage.success('已下线归档，置顶已自动取消'); showOffline.value = false; load(); loadStats() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function doDelete(row) {
  axios.delete(`/api/culture/news/${row.id}`)
    .then(() => { ElMessage.success('已删除'); load(); loadStats() })
    .catch(e => ElMessage.error(e.response?.data?.message || '删除失败'))
}

function openDetail(row) {
  axios.get(`/api/culture/news/${row.id}`)
    .then(r => {
      dNews.value = r.data.data.news
      relInfo.value = { relCaseTitle: r.data.data.relCaseTitle, relModelName: r.data.data.relModelName }
      dTab.value = 'content'
      showDetail.value = true
    })
  axios.get(`/api/culture/news/approvals/${row.id}`).then(r => { approvals.value = r.data.data || [] })
  axios.get(`/api/culture/news/versions/${row.id}`).then(r => { versions.value = r.data.data || [] })
}

function exportCsv() {
  ElMessageBox.confirm(`确认导出当前筛选的 ${rows.value.length} 条稿件CSV？`, '导出确认', { type: 'warning' })
    .then(() => {
      const head = '稿件编号,标题,类型,来源组织,状态,置顶,PC阅读,移动端阅读,点赞,撰稿人,更新时间'
      const lines = rows.value.map(n => [n.newsNo, n.title, n.newsType, n.sourceOrg, n.status,
        n.isTop === 1 ? '是' : '否', n.readCount, n.mobileReadCount, n.likeCount, n.creator, n.updateTime]
        .map(v => '"' + String(v ?? '').replaceAll('"', '""') + '"').join(','))
      const blob = new Blob(['\ufeff' + head + '\n' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = 'news_workbench.csv'
      a.click()
      URL.revokeObjectURL(a.href)
      ElMessage.success('已导出 ' + rows.value.length + ' 条')
    })
    .catch(() => {})
}

onMounted(() => { load(); loadStats(); loadTags() })
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.kpi { text-align: center; }
.kpi-num { font-size: 20px; font-weight: 700; }
.kpi-num.warn { color: #e6a23c; }
.kpi-num.ok { color: #67c23a; }
.kpi-label { color: #909399; font-size: 12px; }
.dist-label { color: #606266; font-size: 12px; }
.dist-item { display: flex; align-items: center; gap: 6px; margin-right: 8px; }
.dist-name { font-size: 12px; color: #606266; }
.dist-cnt { font-size: 12px; color: #909399; }
.node-hint { font-size: 11px; color: #e6a23c; }
.hint { color: #909399; font-size: 12px; }
.content { line-height: 1.8; font-size: 13px; }
.opinion { color: #606266; font-size: 12px; margin-top: 4px; }
</style>

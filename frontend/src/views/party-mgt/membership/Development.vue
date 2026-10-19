<template>
  <div class="page-container">
    <RoleBar />
    <div class="page-header">
      <h2>党员发展管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 1. 看板/计划 ==================== -->
      <el-tab-pane label="看板/计划" name="board">
        <div class="toolbar board-toolbar">
          <el-input-number v-model="boardFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:150px" />
          <el-input-number v-model="boardFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-button type="primary" @click="loadBoard">查询</el-button>
          <el-button v-if="can('R02','R04')" type="primary" class="right-btn" @click="openPlanDialog">新建计划</el-button>
        </div>

        <!-- 五阶段漏斗 -->
        <el-row :gutter="12" class="funnel-cards">
          <el-col v-for="s in FUNNEL_STAGES" :key="s.key" :span="4">
            <el-card shadow="hover">
              <div class="stat-label">{{ s.label }}</div>
              <div class="funnel-value" :style="{ color: s.color }">{{ board.funnel?.[s.key] ?? 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="4">
            <el-card shadow="hover">
              <div class="stat-label">在途发展对象</div>
              <div class="funnel-value" style="color:#303133">{{ board.totalInProgress ?? 0 }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 结构指标 -->
        <el-card shadow="never" class="structure-card">
          <div class="structure-items">
            <div class="structure-item">
              <span class="stat-label">青年占比</span>
              <span class="structure-value youth">{{ fmtPct(board.structure?.youth) }}</span>
            </div>
            <div class="structure-item">
              <span class="stat-label">一线占比</span>
              <span class="structure-value frontline">{{ fmtPct(board.structure?.frontline) }}</span>
            </div>
            <div class="structure-item">
              <span class="stat-label">高知占比</span>
              <span class="structure-value intellectual">{{ fmtPct(board.structure?.intellectual) }}</span>
            </div>
            <div class="structure-item">
              <span class="stat-label">发展总数</span>
              <span class="structure-value">{{ board.structure?.total ?? 0 }}</span>
            </div>
            <div v-if="board.structure?.note" class="structure-note">{{ board.structure.note }}</div>
          </div>
        </el-card>

        <h3 class="section-title">发展计划完成情况</h3>
        <el-table :data="board.plans || []" border stripe size="small" v-loading="boardLoading">
          <el-table-column prop="orgName" label="组织名称" min-width="160" />
          <el-table-column prop="year" label="年度" width="90" align="center" />
          <el-table-column prop="targetTotal" label="计划数" width="90" align="right" />
          <el-table-column prop="finished" label="已完成" width="90" align="right" />
          <el-table-column label="完成率" min-width="180">
            <template #default="{ row }">
              <el-progress
                :percentage="pctValue(row.completionRate)"
                :color="pctValue(row.completionRate) >= 100 ? '#67c23a' : '#409eff'"
              />
            </template>
          </el-table-column>
        </el-table>

        <h3 class="section-title">超时预警</h3>
        <el-table :data="timeouts" border stripe size="small" v-loading="timeoutLoading">
          <el-table-column prop="name" label="姓名" width="110" />
          <el-table-column prop="orgName" label="所在组织" min-width="150" />
          <el-table-column prop="code" label="环节编码" width="130" />
          <el-table-column prop="elapsedDays" label="已耗时(天)" width="100" align="right" />
          <el-table-column prop="message" label="预警信息" min-width="200" show-overflow-tooltip />
          <el-table-column label="级别" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.level === '告警' ? 'danger' : 'warning'" size="small">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 2. 人员台账 ==================== -->
      <el-tab-pane label="人员台账" name="persons">
        <div class="toolbar">
          <el-select v-model="personFilter.stage" placeholder="发展阶段" clearable style="width:140px">
            <el-option v-for="s in stageOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-select v-model="personFilter.orgId" placeholder="所在组织" clearable filterable style="width:180px">
            <el-option v-for="o in orgOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-input v-model="personFilter.keyword" placeholder="姓名 / 关键字" clearable style="width:180px" />
          <el-select v-model="personFilter.status" placeholder="状态" clearable style="width:130px">
            <el-option v-for="s in STATUS_OPTIONS" :key="s" :label="s" :value="s" />
          </el-select>
          <el-button type="primary" @click="loadPersons">查询</el-button>
          <el-button v-if="can('R02','R04','R07','R08')" type="primary" @click="openPersonDialog">新建发展对象</el-button>
        </div>

        <el-table :data="personList" border stripe size="small" v-loading="personLoading">
          <el-table-column prop="name" label="姓名" width="110" />
          <el-table-column label="发展阶段" width="120">
            <template #default="{ row }">{{ stageLabel(row.stage) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="currentNode" label="当前节点" min-width="140" />
          <el-table-column prop="orgName" label="所在组织" min-width="150" />
          <el-table-column prop="createTime" label="建档时间" width="170" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="openDetail(row)">详情</el-button>
              <el-button
                v-if="can('R02','R04','R07','R08') && row.currentNode !== '已完成' && row.currentNode !== '已取消'"
                size="small"
                type="success"
                link
                @click="openNodeDialog(row)"
              >办理节点</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 3. 党委审批 ==================== -->
      <el-tab-pane label="党委审批" name="audit">
        <div class="toolbar">
          <el-select v-model="auditFilter.stage" placeholder="发展阶段" clearable style="width:140px">
            <el-option v-for="s in stageOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-select v-model="auditFilter.orgId" placeholder="所在组织" clearable filterable style="width:180px">
            <el-option v-for="o in orgOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-input v-model="auditFilter.keyword" placeholder="姓名 / 关键字" clearable style="width:180px" />
          <el-select v-model="auditFilter.status" placeholder="状态" clearable style="width:130px">
            <el-option v-for="s in STATUS_OPTIONS" :key="s" :label="s" :value="s" />
          </el-select>
          <el-button type="primary" @click="loadAuditPersons">查询</el-button>
        </div>

        <el-table :data="auditList" border stripe size="small" v-loading="auditLoading">
          <el-table-column prop="name" label="姓名" width="110" />
          <el-table-column prop="currentNode" label="当前节点" min-width="150" />
          <el-table-column label="发展阶段" width="120">
            <template #default="{ row }">{{ stageLabel(row.stage) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="can('R03') && isPendingAudit(row)"
                size="small"
                type="warning"
                link
                @click="openAuditDialog(row)"
              >审批</el-button>
              <template v-if="can('R02','R03','R07') && row.stage === 'PROBATION' && row.status === '在途'">
                <el-button size="small" type="primary" link @click="openActionDialog(row, 'EXTEND')">延长</el-button>
                <el-button size="small" type="primary" link @click="openActionDialog(row, 'POSTPONE')">推迟</el-button>
                <el-button size="small" type="danger" link @click="openActionDialog(row, 'CANCEL')">取消</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建计划弹窗 -->
    <el-dialog v-model="planDialogVisible" title="新建发展计划" width="560px" :close-on-click-modal="false">
      <el-form :model="planForm" label-width="110px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="planForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model="planForm.orgName" />
        </el-form-item>
        <el-form-item label="计划年度" required>
          <el-input-number v-model="planForm.planYear" :min="2000" :max="2100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="计划总数" required>
          <el-input-number v-model="planForm.targetTotal" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="青年指标">
          <el-input-number v-model="planForm.youthTarget" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="一线指标">
          <el-input-number v-model="planForm.frontlineTarget" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="高知指标">
          <el-input-number v-model="planForm.intellectualTarget" :min="0" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建发展对象弹窗 -->
    <el-dialog v-model="personDialogVisible" title="新建发展对象（建档）" width="620px" :close-on-click-modal="false">
      <el-form :model="personForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="personForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" required>
              <el-select v-model="personForm.gender" style="width:100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" required>
              <el-date-picker v-model="personForm.birthDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="personForm.ethnicity" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="personForm.education" clearable style="width:100%">
                <el-option v-for="e in EDUCATION_OPTIONS" :key="e" :label="e" :value="e" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="personForm.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务">
              <el-input v-model="personForm.position" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="personForm.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="personForm.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织ID" required>
              <el-input-number v-model="personForm.orgId" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="组织名称" required>
              <el-input v-model="personForm.orgName" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="personDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPerson">确定</el-button>
      </template>
    </el-dialog>

    <!-- 办理节点弹窗 -->
    <el-dialog v-model="nodeDialogVisible" title="办理节点" width="620px" :close-on-click-modal="false">
      <el-form :model="nodeForm" label-width="110px">
        <el-form-item label="发展对象">
          <el-input :model-value="nodeForm.personName" disabled />
        </el-form-item>
        <el-form-item label="办理节点" required>
          <el-select v-model="nodeForm.nodeKey" style="width:100%" @change="onNodeKeyChange">
            <el-option
              v-for="n in dict.nodes"
              :key="n.key"
              :label="`${n.name}${n.needAudit ? '（需审批）' : ''}`"
              :value="n.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="办理日期" required>
          <el-date-picker v-model="nodeForm.nodeDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="currentNodeMaterials.length" label="材料清单">
          <el-checkbox-group v-model="nodeForm.materials">
            <el-checkbox v-for="m in currentNodeMaterials" :key="m" :value="m">{{ m }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="扩展数据">
          <el-input
            v-model="nodeForm.payloadText"
            type="textarea"
            :rows="3"
            placeholder='JSON 格式，例如 {"meetingVotes": 12}'
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="nodeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item v-if="currentNodeDef?.needAudit" label="提示">
          <el-tag type="warning" size="small">该节点需党委审批，提交后状态为「待审批」</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNode">提交</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="auditDialogVisible" title="党委审批" width="520px" :close-on-click-modal="false">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="发展对象">
          <el-input :model-value="auditForm.personName" disabled />
        </el-form-item>
        <el-form-item label="审批节点">
          <el-input :model-value="auditForm.nodeName" disabled />
        </el-form-item>
        <el-form-item label="审批结果" required>
          <el-radio-group v-model="auditForm.pass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.opinion" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 延长/推迟/取消弹窗 -->
    <el-dialog v-model="actionDialogVisible" :title="actionTitle" width="520px" :close-on-click-modal="false">
      <el-form :model="actionForm" label-width="100px">
        <el-form-item label="发展对象">
          <el-input :model-value="actionForm.personName" disabled />
        </el-form-item>
        <el-form-item v-if="actionForm.action !== 'CANCEL'" label="月数" required>
          <el-input-number v-model="actionForm.months" :min="1" :max="24" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="actionForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAction">确定</el-button>
      </template>
    </el-dialog>

    <!-- 人员详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" :title="`发展详情：${detail.person?.name || ''}`" size="620px">
      <el-descriptions :column="2" border size="small" class="detail-desc">
        <el-descriptions-item label="姓名">{{ detail.person?.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.person?.gender }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detail.person?.birthDate }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ detail.person?.ethnicity }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ detail.person?.education }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.person?.department }}</el-descriptions-item>
        <el-descriptions-item label="职务">{{ detail.person?.position }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.person?.phone }}</el-descriptions-item>
        <el-descriptions-item label="所在组织">{{ detail.person?.orgName }}</el-descriptions-item>
        <el-descriptions-item label="发展阶段">
          {{ detail.stageLabel || stageLabel(detail.person?.stage) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.person?.status)" size="small">{{ detail.person?.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <h3 class="section-title">发展节点时间轴</h3>
      <el-timeline class="dev-timeline">
        <el-timeline-item
          v-for="t in detail.timeline"
          :key="t.key"
          :color="timelineColor(t)"
          :hollow="!t.done"
          :timestamp="t.nodeDate || ''"
          placement="top"
        >
          <div class="timeline-node">
            <span class="timeline-name" :class="{ undone: !t.done }">{{ t.name }}</span>
            <el-tag :type="nodeTagType(t)" size="small">{{ nodeTagLabel(t) }}</el-tag>
            <el-tag v-if="t.repeatable && (t.records?.length || 0) > 0" type="info" size="small">
              记录 {{ t.records.length }} 次
            </el-tag>
          </div>
          <div v-if="t.repeatable && (t.records?.length || 0) > 0" class="timeline-records">
            <div v-for="(r, i) in t.records" :key="i" class="record-item">
              {{ r.nodeDate || r.date || '' }} {{ r.remark || '' }}
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/membership/development'

const activeTab = ref('board')

const STATUS_OPTIONS = ['在途', '已发展', '已取消', '已延长', '已推迟']
const EDUCATION_OPTIONS = ['高中', '中专', '大专', '本科', '硕士', '博士']

const FUNNEL_STAGES = [
  { key: 'APPLICANT', label: '申请入党', color: '#409eff' },
  { key: 'ACTIVIST', label: '入党积极分子', color: '#67c23a' },
  { key: 'TARGET', label: '发展对象', color: '#e6a23c' },
  { key: 'PROBATION', label: '预备党员', color: '#f56c6c' },
  { key: 'FORMAL', label: '正式党员', color: '#909399' }
]

// ==================== 字典 ====================
const dict = reactive({ nodes: [], stages: {} })

const stageOptions = computed(() => {
  const s = dict.stages
  if (Array.isArray(s)) {
    return s.map(x => (typeof x === 'string'
      ? { value: x, label: x }
      : { value: x.value ?? x.key ?? x.stage, label: x.label ?? x.name ?? x.value ?? x.key }))
  }
  if (s && typeof s === 'object') {
    return Object.entries(s).map(([k, v]) => ({ value: k, label: v }))
  }
  return []
})

function stageLabel(stage) {
  const hit = stageOptions.value.find(o => o.value === stage)
  return hit ? hit.label : (stage || '')
}

async function loadDict() {
  try {
    const res = await axios.get(`${API}/dict`)
    const d = res.data?.data || res.data || {}
    dict.nodes = d.nodes || []
    dict.stages = d.stages || {}
  } catch (e) {
    console.error(e)
  }
}

// ==================== 通用展示 ====================
function statusTagType(s) {
  const map = { 在途: 'primary', 已发展: 'success', 已取消: 'info', 已延长: 'warning', 已推迟: 'danger' }
  return map[s] || 'info'
}

function fmtPct(v) {
  if (v == null) return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return (n <= 1 ? n * 100 : n).toFixed(1) + '%'
}

function pctValue(v) {
  if (v == null) return 0
  const n = Number(v)
  if (Number.isNaN(n)) return 0
  return Math.min(100, Math.round(n <= 1 ? n * 100 : n))
}

// 组织下拉：从计划与人员数据汇总
const orgOptions = computed(() => {
  const map = new Map()
  ;(board.plans || []).forEach(p => { if (p.orgId != null) map.set(p.orgId, p.orgName) })
  personList.value.forEach(p => { if (p.orgId != null) map.set(p.orgId, p.orgName) })
  auditList.value.forEach(p => { if (p.orgId != null) map.set(p.orgId, p.orgName) })
  return [...map.entries()].map(([value, label]) => ({ value, label: label || `组织${value}` }))
})

// ==================== 1. 看板/计划 ====================
const boardFilter = reactive({ orgId: null, year: new Date().getFullYear() })
const board = reactive({ funnel: {}, totalInProgress: 0, structure: {}, plans: [] })
const boardLoading = ref(false)
const timeouts = ref([])
const timeoutLoading = ref(false)

async function loadBoard() {
  boardLoading.value = true
  try {
    const res = await axios.get(`${API}/board`, {
      params: { orgId: boardFilter.orgId || undefined, year: boardFilter.year }
    })
    const d = res.data?.data || res.data || {}
    board.funnel = d.funnel || {}
    board.totalInProgress = d.totalInProgress ?? 0
    board.structure = d.structure || {}
    board.plans = d.plans || []
  } catch (e) {
    console.error(e)
  } finally {
    boardLoading.value = false
  }
  loadTimeouts()
}

async function loadTimeouts() {
  timeoutLoading.value = true
  try {
    const res = await axios.get(`${API}/timeouts`, {
      params: { orgId: boardFilter.orgId || undefined }
    })
    timeouts.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    timeoutLoading.value = false
  }
}

// 新建计划
const planDialogVisible = ref(false)
const planForm = reactive({
  orgId: null, orgName: '', planYear: new Date().getFullYear(),
  targetTotal: null, youthTarget: null, frontlineTarget: null, intellectualTarget: null, remark: ''
})

function openPlanDialog() {
  Object.assign(planForm, {
    orgId: boardFilter.orgId, orgName: '', planYear: boardFilter.year || new Date().getFullYear(),
    targetTotal: null, youthTarget: null, frontlineTarget: null, intellectualTarget: null, remark: ''
  })
  planDialogVisible.value = true
}

async function submitPlan() {
  if (!planForm.orgId || !planForm.planYear || planForm.targetTotal == null) {
    ElMessage.warning('请填写组织ID、计划年度与计划总数')
    return
  }
  try {
    await axios.post(`${API}/plan`, { ...planForm })
    ElMessage.success('保存计划成功')
    planDialogVisible.value = false
    loadBoard()
  } catch (e) {
    console.error(e)
  }
}

// ==================== 2. 人员台账 ====================
const personFilter = reactive({ stage: '', orgId: null, keyword: '', status: '' })
const personList = ref([])
const personLoading = ref(false)

async function loadPersons() {
  personLoading.value = true
  try {
    const res = await axios.get(`${API}/person/list`, {
      params: {
        stage: personFilter.stage || undefined,
        orgId: personFilter.orgId || undefined,
        keyword: personFilter.keyword || undefined,
        status: personFilter.status || undefined
      }
    })
    personList.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    personLoading.value = false
  }
}

// 新建发展对象
const personDialogVisible = ref(false)
const personForm = reactive({
  name: '', gender: '男', birthDate: '', ethnicity: '', education: '',
  department: '', position: '', phone: '', idCard: '', orgId: null, orgName: ''
})

function openPersonDialog() {
  Object.assign(personForm, {
    name: '', gender: '男', birthDate: '', ethnicity: '', education: '',
    department: '', position: '', phone: '', idCard: '', orgId: null, orgName: ''
  })
  personDialogVisible.value = true
}

function isAdult(birthDate) {
  if (!birthDate) return false
  const b = new Date(birthDate)
  const now = new Date()
  let age = now.getFullYear() - b.getFullYear()
  const m = now.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < b.getDate())) age--
  return age >= 18
}

async function submitPerson() {
  if (!personForm.name || !personForm.birthDate || !personForm.orgId) {
    ElMessage.warning('请填写姓名、出生日期与组织ID')
    return
  }
  if (!isAdult(personForm.birthDate)) {
    ElMessage.error('发展对象必须年满 18 周岁')
    return
  }
  try {
    await axios.post(`${API}/person`, { ...personForm })
    ElMessage.success('建档成功')
    personDialogVisible.value = false
    loadPersons()
    loadBoard()
  } catch (e) {
    console.error(e)
  }
}

// 详情抽屉
const detailDrawerVisible = ref(false)
const detail = reactive({ person: null, timeline: [], stageLabel: '' })

async function openDetail(row) {
  try {
    const res = await axios.get(`${API}/person/${row.id}`)
    const d = res.data?.data || res.data || {}
    detail.person = d.person || row
    detail.timeline = d.timeline || []
    detail.stageLabel = d.stageLabel || ''
    detailDrawerVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

function timelineColor(t) {
  if (!t.done) return '#c0c4cc'
  if (t.auditStatus === '待审批') return '#e6a23c'
  if (t.auditStatus === '已驳回') return '#f56c6c'
  return '#67c23a'
}

function nodeTagLabel(t) {
  if (!t.done) return '未办理'
  if (t.needAudit) return t.auditStatus || '已通过'
  return '已完成'
}

function nodeTagType(t) {
  if (!t.done) return 'info'
  if (t.auditStatus === '待审批') return 'warning'
  if (t.auditStatus === '已驳回') return 'danger'
  return 'success'
}

// 办理节点
const nodeDialogVisible = ref(false)
const nodeForm = reactive({
  personId: null, personName: '', nodeKey: '', nodeDate: '', materials: [], payloadText: '', remark: ''
})

const currentNodeDef = computed(() => dict.nodes.find(n => n.key === nodeForm.nodeKey) || null)
const currentNodeMaterials = computed(() => currentNodeDef.value?.materials || [])

function openNodeDialog(row) {
  let defaultKey = row.currentNodeKey || ''
  if (!defaultKey && row.currentNode) {
    const hit = dict.nodes.find(n => n.name === row.currentNode)
    defaultKey = hit ? hit.key : ''
  }
  Object.assign(nodeForm, {
    personId: row.id, personName: row.name, nodeKey: defaultKey,
    nodeDate: '', materials: [], payloadText: '', remark: ''
  })
  nodeDialogVisible.value = true
}

function onNodeKeyChange() {
  nodeForm.materials = []
}

async function submitNode() {
  if (!nodeForm.nodeKey || !nodeForm.nodeDate) {
    ElMessage.warning('请选择办理节点并填写办理日期')
    return
  }
  let payload = {}
  if (nodeForm.payloadText && nodeForm.payloadText.trim()) {
    try {
      payload = JSON.parse(nodeForm.payloadText)
    } catch (e) {
      ElMessage.error('扩展数据必须是合法 JSON')
      return
    }
  }
  try {
    await axios.post(`${API}/person/${nodeForm.personId}/node/${nodeForm.nodeKey}`, {
      nodeDate: nodeForm.nodeDate,
      materials: nodeForm.materials,
      payload,
      remark: nodeForm.remark || undefined
    })
    ElMessage.success(currentNodeDef.value?.needAudit ? '提交成功，待党委审批' : '办理成功')
    nodeDialogVisible.value = false
    loadPersons()
    loadBoard()
    if (detailDrawerVisible.value && detail.person?.id === nodeForm.personId) {
      openDetail({ id: nodeForm.personId })
    }
  } catch (e) {
    console.error(e)
  }
}

// ==================== 3. 党委审批 ====================
const auditFilter = reactive({ stage: '', orgId: null, keyword: '', status: '' })
const auditList = ref([])
const auditLoading = ref(false)

async function loadAuditPersons() {
  auditLoading.value = true
  try {
    const res = await axios.get(`${API}/person/list`, {
      params: {
        stage: auditFilter.stage || undefined,
        orgId: auditFilter.orgId || undefined,
        keyword: auditFilter.keyword || undefined,
        status: auditFilter.status || undefined
      }
    })
    auditList.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    auditLoading.value = false
  }
}

function isPendingAudit(row) {
  const needAudit = row.needAudit === 1 || row.needAudit === true || row.needAudit === '1'
  return needAudit && row.auditStatus === '待审批'
}

// 审批
const auditDialogVisible = ref(false)
const auditForm = reactive({ nodeId: null, personId: null, personName: '', nodeName: '', pass: true, opinion: '' })

async function openAuditDialog(row) {
  try {
    const res = await axios.get(`${API}/person/${row.id}`)
    const d = res.data?.data || res.data || {}
    const node = (d.timeline || []).find(t => t.needAudit && t.auditStatus === '待审批')
    if (!node) {
      ElMessage.warning('该人员当前没有待审批节点')
      return
    }
    Object.assign(auditForm, {
      nodeId: node.nodeId, personId: row.id, personName: row.name,
      nodeName: node.name, pass: true, opinion: ''
    })
    auditDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

async function submitAudit() {
  try {
    await axios.post(`${API}/node/${auditForm.nodeId}/audit`, {
      pass: auditForm.pass,
      opinion: auditForm.opinion
    })
    ElMessage.success(auditForm.pass ? '已通过' : '已驳回')
    auditDialogVisible.value = false
    loadAuditPersons()
    loadPersons()
    loadBoard()
  } catch (e) {
    console.error(e)
  }
}

// 延长/推迟/取消
const ACTION_LABELS = { EXTEND: '延长预备期', POSTPONE: '推迟转正', CANCEL: '取消资格' }
const actionDialogVisible = ref(false)
const actionForm = reactive({ personId: null, personName: '', action: 'EXTEND', months: 6, reason: '' })

const actionTitle = computed(() => ACTION_LABELS[actionForm.action] || '操作')

function openActionDialog(row, action) {
  Object.assign(actionForm, { personId: row.id, personName: row.name, action, months: 6, reason: '' })
  actionDialogVisible.value = true
}

async function submitAction() {
  if (actionForm.action !== 'CANCEL' && !actionForm.months) {
    ElMessage.warning('请填写月数')
    return
  }
  if (!actionForm.reason) {
    ElMessage.warning('请填写原因')
    return
  }
  try {
    await axios.post(`${API}/person/${actionForm.personId}/action/${actionForm.action}`, {
      months: actionForm.months,
      reason: actionForm.reason
    })
    ElMessage.success(`${actionTitle.value}成功`)
    actionDialogVisible.value = false
    loadAuditPersons()
    loadPersons()
    loadBoard()
  } catch (e) {
    console.error(e)
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadDict()
  loadBoard()
  loadPersons()
  loadAuditPersons()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.board-toolbar {
  width: 100%;
}

.right-btn {
  margin-left: auto;
}

.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  color: #303133;
}

.funnel-cards {
  margin-bottom: 12px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.funnel-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.structure-card {
  margin-bottom: 4px;
}

.structure-items {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.structure-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.structure-item .stat-label {
  margin-bottom: 0;
}

.structure-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.structure-value.youth { color: #409eff; }
.structure-value.frontline { color: #67c23a; }
.structure-value.intellectual { color: #e6a23c; }

.structure-note {
  margin-left: auto;
  color: #909399;
  font-size: 13px;
}

.detail-desc {
  margin-bottom: 8px;
}

.dev-timeline {
  padding-left: 4px;
}

.timeline-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.timeline-name {
  font-weight: 600;
  color: #303133;
}

.timeline-name.undone {
  color: #a8abb2;
  font-weight: 400;
}

.timeline-records {
  margin-top: 6px;
  padding: 6px 10px;
  background: #f7f8fa;
  border-radius: 4px;
}

.record-item {
  font-size: 12px;
  color: #606266;
  line-height: 1.8;
}
</style>

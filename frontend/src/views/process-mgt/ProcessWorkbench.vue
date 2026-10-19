<template>
  <div class="page-card">
    <div class="page-header">
      <h2>流程工作台</h2>
      <div class="header-actions">
        <el-button @click="openSettings">
          <el-icon><Setting /></el-icon>偏好设置
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="统一聚合入口：待我审批、我已审批、我发起的、抄送我的、我的收藏、最近浏览、待学习制度，对标钉钉/飞书工作台模式。" />

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card warning" @click="activeTab = 'pending'">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待我审批</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card success" @click="activeTab = 'approved'">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ approvedCount }}</div>
            <div class="stat-label">我已审批</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card primary" @click="activeTab = 'initiated'">
          <div class="stat-icon"><el-icon><Share /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ initiatedCount }}</div>
            <div class="stat-label">我发起的</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card info" @click="activeTab = 'cc'">
          <div class="stat-icon"><el-icon><Message /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ ccCount }}</div>
            <div class="stat-label">抄送我的</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card" @click="activeTab = 'favorites'">
          <div class="stat-icon"><el-icon><Star /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ favoritesCount }}</div>
            <div class="stat-label">我的收藏</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card" @click="activeTab = 'learning'">
          <div class="stat-icon"><el-icon><Reading /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ learningCount }}</div>
            <div class="stat-label">待学习</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card system-stat">
          <div class="stat-icon"><el-icon><Grid /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalFlows }}</div>
            <div class="stat-label">流程总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card system-stat">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.policyCount }}</div>
            <div class="stat-label">制度文件</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="12">
        <el-card shadow="never" class="quick-actions-card">
          <h3 style="margin:0 0 12px 0; font-size:14px; font-weight:600;">快捷操作</h3>
          <div class="action-grid">
            <div class="action-card" @click="quickAction('new-flow')">
              <el-icon class="action-icon">Plus</el-icon>
              <span>新建流程</span>
            </div>
            <div class="action-card" @click="quickAction('new-policy')">
              <el-icon class="action-icon">Document</el-icon>
              <span>新建制度</span>
            </div>
            <div class="action-card" @click="quickAction('design')">
              <el-icon class="action-icon">Edit</el-icon>
              <span>流程设计</span>
            </div>
            <div class="action-card" @click="quickAction('import')">
              <el-icon class="action-icon">Upload</el-icon>
              <span>导入流程</span>
            </div>
            <div class="action-card" @click="quickAction('export')">
              <el-icon class="action-icon">Download</el-icon>
              <span>导出流程</span>
            </div>
            <div class="action-card" @click="quickAction('report')">
              <el-icon class="action-icon">TrendCharts</el-icon>
              <span>流程报表</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="system-status-card">
          <h3 style="margin:0 0 12px 0; font-size:14px; font-weight:600;">系统状态</h3>
          <div class="status-grid">
            <div class="status-card">
              <div class="status-dot online"></div>
              <span>BPMN设计器</span>
              <span class="status-text">正常运行</span>
            </div>
            <div class="status-card">
              <div class="status-dot online"></div>
              <span>流程引擎</span>
              <span class="status-text">正常运行</span>
            </div>
            <div class="status-card">
              <div class="status-dot online"></div>
              <span>版本管理</span>
              <span class="status-text">正常运行</span>
            </div>
            <div class="status-card">
              <div class="status-dot warning"></div>
              <span>表单引擎</span>
              <span class="status-text">维护中</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待我审批" name="pending">
        <el-card shadow="never">
          <el-table :data="pendingList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="180" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column prop="submitTime" label="发起时间" width="160" />
            <el-table-column prop="currentNode" label="当前节点" width="120">
              <template #default="{row}"><el-tag size="small" type="warning">{{ row.currentNode }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button link size="small" type="primary" @click="openApprove(row)">审批</el-button>
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
                <el-button link size="small" type="danger" @click="urgeItem(row)">催办</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我已审批" name="approved">
        <el-card shadow="never">
          <el-table :data="approvedList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="180" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="approveTime" label="审批时间" width="160" />
            <el-table-column prop="myAction" label="我的操作" width="100">
              <template #default="{row}">
                <el-tag v-if="row.myAction === '通过'" type="success" size="small">通过</el-tag>
                <el-tag v-else-if="row.myAction === '驳回'" type="danger" size="small">驳回</el-tag>
                <el-tag v-else type="info" size="small">{{ row.myAction }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="myComment" label="审批意见" min-width="150" />
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我发起的" name="initiated">
        <el-card shadow="never">
          <el-table :data="initiatedList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="180" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="submitTime" label="发起时间" width="160" />
            <el-table-column prop="currentNode" label="当前节点" width="120">
              <template #default="{row}"><el-tag size="small" type="warning">{{ row.currentNode }}</el-tag></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag v-if="row.status === '审批中'" type="warning" size="small">审批中</el-tag>
                <el-tag v-else-if="row.status === '已通过'" type="success" size="small">已通过</el-tag>
                <el-tag v-else-if="row.status === '已驳回'" type="danger" size="small">已驳回</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
                <el-button link size="small" type="warning" v-if="row.status === '审批中'" @click="withdraw(row)">撤回</el-button>
                <el-button link size="small" type="danger" v-if="row.status === '已通过'" @click="applyCancel(row)">申请作废</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="抄送我的" name="cc">
        <el-card shadow="never">
          <el-table :data="ccList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="180" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDetail(row)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="ccType" label="抄送类型" width="100">
              <template #default="{row}">
                <el-tag v-if="row.ccType === '审批节点'" type="warning" size="small">审批节点</el-tag>
                <el-tag v-else type="info" size="small">审批完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ccTime" label="抄送时间" width="160" />
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="我的收藏" name="favorites">
        <el-card shadow="never">
          <el-table :data="favoritesList" border stripe size="small">
            <el-table-column prop="docName" label="制度名称" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDoc(row)">{{ row.docName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="collectTime" label="收藏时间" width="160" />
            <el-table-column label="操作" width="200">
              <template #default="{row}">
                <el-button link size="small" type="primary" @click="viewDoc(row)">查阅</el-button>
                <el-button link size="small" @click="startLearn(row)">学习</el-button>
                <el-button link size="small" type="danger" @click="cancelFav(row)">取消</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="最近更新" name="recent">
        <el-card shadow="never">
          <div class="section-header">
            <h3 style="margin:0; font-size:14px;">最近更新</h3>
            <el-button size="small" @click="viewAllUpdates">查看全部</el-button>
          </div>
          <el-table :data="recentUpdates" border stripe size="small">
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" width="200" />
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="action" label="操作" width="100" />
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="time" label="时间" width="140" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="待学习制度" name="learning">
        <el-card shadow="never">
          <el-table :data="learningList" border stripe size="small">
            <el-table-column prop="docName" label="制度名称" min-width="200">
              <template #default="{row}">
                <el-link type="primary" @click="viewDoc(row)">{{ row.docName }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="requiredBy" label="要求来源" width="120" />
            <el-table-column prop="deadline" label="完成期限" width="120" />
            <el-table-column label="进度" width="150">
              <template #default="{row}">
                <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{row}">
                <el-button link size="small" type="primary" @click="continueLearn(row)">
                  {{ row.progress > 0 ? '继续学习' : '开始学习' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="approveVisible" title="审批操作" width="600px">
      <el-form label-width="100px">
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.comment" :rows="3" placeholder="请填写审批意见" />
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="pass">通过</el-radio>
            <el-radio label="reject">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="常用意见">
          <el-space wrap>
            <el-tag v-for="op in commonOpinions" :key="op" @click="approveForm.comment = op" style="cursor:pointer">{{ op }}</el-tag>
          </el-space>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible=false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="primary" @click="doApprove">通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="settingsVisible" title="偏好设置" width="600px">
      <el-form label-width="120px">
        <el-form-item label="常用审批意见">
          <el-input type="textarea" v-model="preferences.commonComments" :rows="3" placeholder="每行一条常用审批意见" />
        </el-form-item>
        <el-form-item label="默认抄送人">
          <el-select v-model="preferences.defaultCC" multiple placeholder="选择默认抄送人" style="width:100%">
            <el-option label="部门主管" value="dept_leader" />
            <el-option label="体系专员" value="system_specialist" />
            <el-option label="综合管理部" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批偏好">
          <el-checkbox v-model="preferences.autoAgree">自动同意我已审核过的同类流程</el-checkbox>
          <el-checkbox v-model="preferences.autoCC">审批完成后自动抄送我</el-checkbox>
        </el-form-item>
        <el-form-item label="消息提醒">
          <el-checkbox v-model="preferences.emailNotify">邮件通知</el-checkbox>
          <el-checkbox v-model="preferences.smsNotify">短信通知</el-checkbox>
          <el-checkbox v-model="preferences.appNotify">APP推送</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsVisible=false">取消</el-button>
        <el-button type="primary" @click="savePreferences">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, CircleCheck, Share, Message, Star, Reading, Setting, Grid, Document, Plus, Edit, Upload, Download, TrendCharts } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

const activeTab = ref('pending')
const approveVisible = ref(false)
const settingsVisible = ref(false)

// 后端流程实例与审批记录（用于工作台聚合，不新增后端端点）
const instances = ref([])
const approvals = ref([])

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

const statusName = (s) => {
  const map = { pending: '审批中', processing: '审批中', approved: '已通过', rejected: '已驳回', cancelled: '已取消' }
  return map[s] || s || '-'
}

const commonOpinions = ['同意发布', '内容完整，符合规范', '审核通过', '材料齐全，流程合规']

const approveForm = reactive({
  comment: '',
  action: 'pass',
  id: null
})

const preferences = reactive({
  commonComments: '同意发布\n内容完整，符合规范',
  defaultCC: [],
  autoAgree: false,
  autoCC: true,
  emailNotify: true,
  smsNotify: false,
  appNotify: true
})

// 待我审批：审批记录中 status=pending
const pendingList = computed(() =>
  approvals.value
    .filter(a => a.status === 'pending')
    .map(a => ({
      id: a.id,
      flowNo: a.instanceId ? 'INS-' + a.instanceId : 'APR-' + a.id,
      title: a.processName || '审批任务#' + a.id,
      type: a.bizType || '审批',
      initiator: a.applicant || '-',
      submitTime: fmtTime(a.createTime),
      currentNode: a.nodeName || '-'
    }))
)

// 我已审批：审批记录中 status=approved/rejected
const approvedList = computed(() =>
  approvals.value
    .filter(a => ['approved', 'rejected'].includes(a.status))
    .map(a => ({
      id: a.id,
      flowNo: a.instanceId ? 'INS-' + a.instanceId : 'APR-' + a.id,
      title: a.processName || '审批任务#' + a.id,
      type: a.bizType || '审批',
      approveTime: fmtTime(a.approveTime),
      myAction: a.status === 'approved' ? '通过' : '驳回',
      myComment: a.comment || ''
    }))
)

// 我发起的：流程实例列表
const initiatedList = computed(() =>
  instances.value.map(i => ({
    id: i.id,
    flowNo: i.processCode || 'INS-' + i.id,
    title: i.processName || '流程#' + i.id,
    type: '流程',
    submitTime: fmtTime(i.submitTime || i.createTime),
    currentNode: i.currentNode || statusName(i.status),
    status: statusName(i.status)
  }))
)

// 抄送我的 / 我的收藏 / 待学习制度：后端无对应实体，使用真实空态（不灌假数据）
const ccList = ref([])
const favoritesList = ref([])
const learningList = ref([])

// 最近更新：由实例+审批记录按 updateTime 聚合取前 10
const recentUpdates = computed(() => {
  const flowRows = instances.value.map(i => ({
    id: 'f' + i.id,
    type: 'flow',
    name: i.processName || '流程#' + i.id,
    code: i.processCode || '-',
    version: '-',
    action: statusName(i.status),
    operator: i.applicant || '-',
    sortTime: i.updateTime || i.createTime || '',
    time: fmtTime(i.updateTime || i.createTime)
  }))
  const apprRows = approvals.value.map(a => ({
    id: 'a' + a.id,
    type: 'policy',
    name: a.processName || '审批#' + a.id,
    code: a.bizType || '-',
    version: '-',
    action: a.status === 'approved' ? '通过' : a.status === 'rejected' ? '驳回' : '待审',
    operator: a.approver || a.applicant || '-',
    sortTime: a.updateTime || a.createTime || '',
    time: fmtTime(a.updateTime || a.createTime)
  }))
  return [...flowRows, ...apprRows]
    .filter(r => r.sortTime)
    .sort((a, b) => (b.sortTime > a.sortTime ? 1 : -1))
    .slice(0, 10)
})

const stats = computed(() => ({
  totalFlows: instances.value.length,
  activeCount: instances.value.filter(i => ['pending', 'processing'].includes(i.status)).length,
  policyCount: 0,
  formCount: 0
}))

const pendingCount = computed(() => pendingList.value.length)
const approvedCount = computed(() => approvedList.value.length)
const initiatedCount = computed(() => initiatedList.value.length)
const ccCount = computed(() => ccList.value.length)
const favoritesCount = computed(() => favoritesList.value.length)
const learningCount = computed(() => learningList.value.length)

const getTypeTagType = (type) => {
  const map = { flow: 'primary', policy: 'success', form: 'info' }
  return map[type] || 'info'
}

const getTypeName = (type) => {
  const map = { flow: '流程', policy: '制度', form: '表单' }
  return map[type] || type
}

const quickAction = (action) => {
  const actions = {
    'new-flow': '/process-mgt/management/main',
    'new-policy': '/process-mgt/policy/designer',
    'design': '/process-mgt/design/designer',
    'import': '/process-mgt/management/main',
    'export': '/process-mgt/management/main',
    'report': '/process-mgt/analysis/performance'
  }
  if (actions[action]) {
    window.location.href = actions[action]
  } else {
    ElMessage.info(`执行操作: ${action}`)
  }
}

const viewAllUpdates = () => {
  ElMessage.info('查看全部更新')
}

function viewDetail (row) {
  ElMessage.info(`查看详情: ${row.title}`)
}

function openApprove (row) {
  approveForm.comment = ''
  approveForm.action = 'pass'
  approveForm.id = row.id
  approveVisible.value = true
}

function doApprove () {
  approveVisible.value = false
  ElMessage.success('审批通过')
}

function doReject () {
  if (!approveForm.comment) {
    ElMessage.warning('请填写审批意见')
    return
  }
  approveVisible.value = false
  ElMessage.success('已驳回')
}

function withdraw (row) {
  ElMessageBox.confirm('确认撤回此流程？', '撤回确认', { type: 'warning' }).then(() => {
    ElMessage.success('已撤回')
  }).catch(() => {})
}

function applyCancel (row) {
  ElMessage.info(`申请作废: ${row.title}`)
}

function urgeItem (row) {
  ElMessage.success(`已发送催办: ${row.title}`)
}

function viewDoc (row) {
  ElMessage.info(`查阅制度: ${row.docName}`)
}

function startLearn (row) {
  ElMessageBox.confirm(`确定要开始学习"${row.docName}"吗？`, '开始学习').then(() => {
    ElMessage.success('已加入学习计划')
  }).catch(() => {})
}

function continueLearn (row) {
  ElMessage.info(`继续学习: ${row.docName}`)
}

function cancelFav (row) {
  ElMessageBox.confirm('确定取消收藏吗？', '提示').then(() => {
    ElMessage.success('已取消收藏')
  }).catch(() => {})
}

function recollect (row) {
  ElMessage.success(`已收藏: ${row.docName}`)
}

function openSettings () {
  settingsVisible.value = true
}

function savePreferences () {
  settingsVisible.value = false
  ElMessage.success('偏好设置已保存')
}

function getProgressStatus (progress) {
  if (progress >= 100) return 'success'
  if (progress >= 50) return 'warning'
  return ''
}

const loadAll = async () => {
  try {
    const [instRes, apprRes] = await Promise.all([
      axios.get('/api/process/instance/list', { params: { pageNum: 1, pageSize: 200 } }),
      axios.get('/api/process/approval/list', { params: { pageNum: 1, pageSize: 200 } })
    ])
    const instPage = instRes.data && instRes.data.data ? instRes.data.data : {}
    const apprPage = apprRes.data && apprRes.data.data ? apprRes.data.data : {}
    instances.value = instPage.records || []
    approvals.value = apprPage.records || []
  } catch (e) {
    console.error('加载工作台数据失败', e)
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; cursor: pointer; }
.stat-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-card.primary .stat-icon { background: #e3f2fd; color: #409eff; }
.stat-card.info .stat-icon { background: #f4f4f5; color: #909399; }
.stat-card.system-stat .stat-icon { background: #f0f9ff; color: #0ea5e9; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 14px; margin-top: 4px; }

.action-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.action-card { display: flex; flex-direction: column; align-items: center; padding: 16px 12px; background: #f8fafc; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.action-card:hover { background: #eff6ff; transform: translateY(-2px); }
.action-card .action-icon { font-size: 24px; color: #3b82f6; margin-bottom: 8px; }
.action-card span { font-size: 13px; color: #333; }

.status-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.status-card { display: flex; align-items: center; gap: 12px; padding: 12px; background: #f8fafc; border-radius: 8px; }
.status-card .status-dot { width: 10px; height: 10px; border-radius: 50%; }
.status-card .status-dot.online { background: #52c41a; }
.status-card .status-dot.warning { background: #faad14; }
.status-card .status-dot.offline { background: #f56c6c; }
.status-card span { font-size: 13px; color: #333; }
.status-card .status-text { color: #999; margin-left: auto; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
</style>
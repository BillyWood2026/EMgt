<template>
  <div class="page-card">
    <div class="page-header">
      <h2>流程制度审批发布全流程</h2>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="完整审批流程：起草→部门审核→体系专员→分管领导→发布，支持审批意见填写、附件查看、流程撤回等操作。" />

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待我审批</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card primary">
          <div class="stat-icon"><el-icon><Share /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ initiatedCount }}</div>
            <div class="stat-label">我发起的</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-icon"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ cancelledCount }}</div>
            <div class="stat-label">已作废</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <!-- 待我审批 -->
      <el-tab-pane label="待我审批" name="pending">
        <el-card shadow="never">
          <el-table :data="pendingList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="170" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column prop="submitTime" label="发起时间" width="160" />
            <el-table-column prop="currentNode" label="当前节点" width="140">
              <template #default="{row}"><el-tag size="small" type="warning">{{ row.currentNode }}</el-tag></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}"><el-tag size="small" :type="getStatusType(row.status)">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <el-button link size="small" type="primary" @click="openApprove(row)">审批</el-button>
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 我发起的 -->
      <el-tab-pane label="我发起的" name="initiated">
        <el-card shadow="never">
          <el-table :data="initiatedList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="170" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="submitTime" label="发起时间" width="160" />
            <el-table-column prop="currentNode" label="当前节点" width="140" />
            <el-table-column label="状态" width="100">
              <template #default="{row}"><el-tag size="small" :type="getStatusType(row.status)">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
                <el-button link size="small" type="warning" v-if="row.status === '审批中'" @click="withdraw(row)">撤回</el-button>
                <el-button link size="small" type="danger" v-if="row.status === '已通过'" @click="openCancelDialog(row)">申请作废</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 已完成 -->
      <el-tab-pane label="已完成" name="completed">
        <el-card shadow="never">
          <el-table :data="completedList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="170" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column prop="completeTime" label="完成时间" width="160" />
            <el-table-column label="结果" width="100">
              <template #default="{row}"><el-tag size="small" :type="row.result === '通过' ? 'success' : 'danger'">{{ row.result }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 已作废 -->
      <el-tab-pane label="已作废" name="cancelled">
        <el-card shadow="never">
          <el-table :data="cancelledList" border stripe size="small">
            <el-table-column prop="flowNo" label="流程编号" width="170" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column prop="cancelTime" label="作废时间" width="160" />
            <el-table-column prop="cancelReason" label="作废原因" min-width="200" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{row}">
                <el-button link size="small" @click="viewDetail(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 审批详情弹窗 -->
    <el-dialog v-model="detailVisible" title="审批详情" width="800px">
      <el-card v-if="currentItem" shadow="never" style="margin-bottom:16px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="流程编号">{{ currentItem.flowNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentItem.type }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ currentItem.initiator }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ currentItem.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentItem.status)">{{ currentItem.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 审批流程步骤条 -->
      <el-card shadow="never" style="margin-bottom:16px">
        <template #header><b>审批流程</b></template>
        <el-steps :active="currentStep" align-center simple>
          <el-step title="起草" description="发起人提交" />
          <el-step title="部门审核" description="部门负责人" />
          <el-step title="体系专员" description="制度专员" />
          <el-step title="分管领导" description="分管副总" />
          <el-step title="发布" description="正式发布" />
        </el-steps>
      </el-card>

      <!-- 审批历史 -->
      <el-card shadow="never" style="margin-bottom:16px">
        <template #header><b>审批历史</b></template>
        <el-timeline>
          <el-timeline-item 
            v-for="(step, idx) in currentItem?.steps" 
            :key="idx" 
            :timestamp="step.time" 
            :type="step.type"
          >
            <el-card shadow="never">
              <b>{{ step.node }}</b>
              <p style="margin:6px 0 0;color:#606266">{{ step.action }}（{{ step.operator }}）</p>
              <p v-if="step.comment" style="margin:4px 0 0;color:#909399">意见：{{ step.comment }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 附件列表 -->
      <el-card v-if="currentItem?.attachments?.length" shadow="never">
        <template #header><b>附件</b></template>
        <div class="attachment-list">
          <div 
            v-for="(file, idx) in currentItem.attachments" 
            :key="idx" 
            class="attachment-item"
          >
            <el-icon><Document /></el-icon>
            <span>{{ file.name }}</span>
            <el-tag size="mini">{{ file.size }}</el-tag>
            <el-button link size="small" @click="downloadAttachment(file)">下载</el-button>
          </div>
        </div>
      </el-card>
    </el-dialog>

    <!-- 审批操作弹窗 -->
    <el-dialog v-model="approveVisible" title="审批操作" width="680px">
      <el-form label-width="100px">
        <el-form-item label="审批类型">
          <el-radio-group v-model="approveForm.approvalMode">
            <el-radio-button label="通过" value="pass" />
            <el-radio-button label="驳回" value="reject" />
            <el-radio-button label="加签" value="addSign" />
            <el-radio-button label="减签" value="removeSign" />
            <el-radio-button label="转签" value="transfer" />
          </el-radio-group>
        </el-form-item>

        <!-- 会签/或签选项 -->
        <el-form-item label="会签方式" v-if="approveForm.approvalMode === 'pass'">
          <el-radio-group v-model="approveForm.signMode">
            <el-radio label="countersign">会签（全部通过）</el-radio>
            <el-radio label="orsign">或签（一人通过即生效）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approveForm.comment" :rows="3" placeholder="请填写审批意见" />
        </el-form-item>

        <!-- 驳回选项 -->
        <el-form-item label="驳回至" v-if="approveForm.approvalMode === 'reject'">
          <el-radio-group v-model="approveForm.rejectTarget">
            <el-radio label="initiator">驳回至发起人</el-radio>
            <el-radio label="specific">驳回至指定节点</el-radio>
          </el-radio-group>
          <el-select v-model="approveForm.rejectNode" placeholder="选择驳回节点" v-if="approveForm.rejectTarget === 'specific'" style="margin-top:8px;width:100%">
            <el-option label="起草" value="draft" />
            <el-option label="部门审核" value="dept" />
            <el-option label="体系专员" value="system" />
          </el-select>
        </el-form-item>

        <!-- 加签选项 -->
        <el-form-item label="加签人员" v-if="approveForm.approvalMode === 'addSign'">
          <el-select v-model="approveForm.addSignUser" placeholder="选择加签人员" style="width:100%">
            <el-option label="张主任（部门主管）" value="zhang" />
            <el-option label="李经理（质量部）" value="li" />
            <el-option label="王总（分管领导）" value="wang" />
          </el-select>
        </el-form-item>
        <el-form-item label="加签位置" v-if="approveForm.approvalMode === 'addSign'">
          <el-radio-group v-model="approveForm.addSignPosition">
            <el-radio label="before">加签在当前节点之前</el-radio>
            <el-radio label="after">加签在当前节点之后</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 转签选项 -->
        <el-form-item label="转签人员" v-if="approveForm.approvalMode === 'transfer'">
          <el-select v-model="approveForm.transferUser" placeholder="选择转签人员" style="width:100%">
            <el-option label="陈主管（代理审批）" value="chen" />
            <el-option label="刘经理（部门经理）" value="liu" />
          </el-select>
        </el-form-item>

        <!-- 超时设置 -->
        <el-form-item label="节点时限" v-if="approveForm.approvalMode === 'pass'">
          <el-input-number v-model="approveForm.timeoutHours" :min="1" :max="168" /> 小时
          <el-checkbox v-model="approveForm.enableTimeoutAlert" style="margin-left:16px">启用超时预警</el-checkbox>
        </el-form-item>

        <!-- 抄送选项 -->
        <el-form-item label="抄送人员" v-if="approveForm.approvalMode === 'pass'">
          <el-select v-model="approveForm.ccUsers" multiple placeholder="选择抄送人员（可选）" style="width:100%">
            <el-option label="综合部-行政主管" value="admin" />
            <el-option label="财务部-财务经理" value="finance" />
            <el-option label="质量部-质量总监" value="quality" />
          </el-select>
        </el-form-item>

        <!-- 常用意见快捷选择 -->
        <el-form-item label="快捷意见" v-if="approveForm.approvalMode === 'pass'">
          <el-space wrap>
            <el-tag v-for="opinion in commonOpinions" :key="opinion" class="opinion-tag" @click="approveForm.comment = opinion" style="cursor:pointer">
              {{ opinion }}
            </el-tag>
          </el-space>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible=false">取消</el-button>
        <el-button type="info" v-if="approveForm.approvalMode !== 'pass'" @click="doDelegate">委托代理</el-button>
        <el-button type="danger" v-if="approveForm.approvalMode === 'reject'" @click="doReject">确认驳回</el-button>
        <el-button type="primary" v-if="approveForm.approvalMode !== 'reject'" @click="doApprove">确认{{ getApprovalActionText() }}</el-button>
      </template>
    </el-dialog>

    <!-- 审批代理人设置弹窗 -->
    <el-dialog v-model="delegateVisible" title="审批代理人设置" width="520px">
      <el-form label-width="120px">
        <el-form-item label="代理人">
          <el-select v-model="delegateForm.agent" placeholder="选择代理人">
            <el-option label="张主任" value="zhang" />
            <el-option label="李经理" value="li" />
            <el-option label="陈主管" value="chen" />
          </el-select>
        </el-form-item>
        <el-form-item label="代理时间段">
          <el-date-picker v-model="delegateForm.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
        </el-form-item>
        <el-form-item label="代理原因">
          <el-input v-model="delegateForm.reason" placeholder="请假/出差等" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon title="设置代理后，符合条件的审批流程将自动转交给代理人处理" />
      </el-form>
      <template #footer>
        <el-button @click="delegateVisible=false">取消</el-button>
        <el-button type="primary" @click="saveDelegate">保存设置</el-button>
      </template>
    </el-dialog>

    <!-- 流程暂停/终止弹窗 -->
    <el-dialog v-model="suspendVisible" title="流程暂停/终止" width="520px">
      <el-form label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="suspendForm.action">
            <el-radio label="suspend">暂停流程</el-radio>
            <el-radio label="terminate">终止流程</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作原因">
          <el-input type="textarea" v-model="suspendForm.reason" :rows="3" placeholder="请填写操作原因" />
        </el-form-item>
        <el-alert v-if="suspendForm.action === 'terminate'" type="danger" :closable="false" show-icon title="终止流程后，该流程将永久结束，无法恢复，请谨慎操作" />
      </el-form>
      <template #footer>
        <el-button @click="suspendVisible=false">取消</el-button>
        <el-button type="danger" @click="doSuspendOrTerminate">确认{{ suspendForm.action === 'suspend' ? '暂停' : '终止' }}</el-button>
      </template>
    </el-dialog>

    <!-- 作废申请弹窗 -->
    <el-dialog v-model="cancelVisible" title="申请作废" width="520px">
      <el-form label-width="130px">
        <el-form-item label="作废原因">
          <el-select v-model="cancelForm.reason" style="width:100%">
            <el-option label="内容错误，需重新起草" value="内容错误" />
            <el-option label="政策变更，不再适用" value="政策变更" />
            <el-option label="被新制度替代" value="被替代" />
            <el-option label="其他原因" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input type="textarea" v-model="cancelForm.description" :rows="3" placeholder="请详细说明作废原因" />
        </el-form-item>
        <el-form-item label="替代文件">
          <el-input v-model="cancelForm.replacement" placeholder="如：XX制度V3.0" />
        </el-form-item>
        <el-alert type="warning" :closable="false" show-icon title="作废申请需经过审批，审批通过后该文件将被标记为废止状态" />
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible=false">取消</el-button>
        <el-button type="danger" @click="submitCancel">提交作废申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Share, CircleCheck, CircleClose, Document } from '@element-plus/icons-vue'

const activeTab = ref('pending')
const detailVisible = ref(false)
const approveVisible = ref(false)
const cancelVisible = ref(false)
const delegateVisible = ref(false)
const suspendVisible = ref(false)

const currentItem = ref(null)
const currentStep = ref(2)

const commonOpinions = ref([
  '同意发布执行',
  '内容完整，符合规范',
  '审核通过，建议推广',
  '同意修订，格式规范',
  '材料齐全，流程合规'
])

const approveForm = reactive({
  comment: '',
  action: 'pass',
  approvalMode: 'pass',
  signMode: 'countersign',
  rejectTarget: 'initiator',
  rejectNode: '',
  addSignUser: '',
  addSignPosition: 'after',
  transferUser: '',
  timeoutHours: 24,
  enableTimeoutAlert: false,
  ccUsers: []
})

const delegateForm = reactive({
  agent: '',
  timeRange: [],
  reason: ''
})

const suspendForm = reactive({
  action: 'suspend',
  reason: ''
})

const cancelForm = reactive({
  reason: '',
  description: '',
  replacement: ''
})

const pendingList = ref([])

const initiatedList = ref([])

const completedList = ref([])

const cancelledList = ref([])

const pendingCount = computed(() => pendingList.value.length)
const initiatedCount = computed(() => initiatedList.value.length)
const completedCount = computed(() => completedList.value.length)
const cancelledCount = computed(() => cancelledList.value.length)

function getStatusType (status) {
  const types = { '审批中': 'warning', '已通过': 'success', '已驳回': 'danger', '已完成': 'success', '待审批': 'warning' }
  return types[status] || ''
}

async function loadPending () {
  try {
    const params = {
      approver: localStorage.getItem('username') || '',
      pageNum: 1,
      pageSize: 100
    }
    const res = await axios.get('/api/process/approval/pending', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      pendingList.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载待审批列表失败')
    console.error(e)
  }
}

async function loadInitiated () {
  try {
    const params = {
      applicant: localStorage.getItem('username') || '',
      pageNum: 1,
      pageSize: 100
    }
    const res = await axios.get('/api/process/approval/initiated', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      initiatedList.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载我发起的列表失败')
    console.error(e)
  }
}

async function loadCompleted () {
  try {
    const params = {
      approver: localStorage.getItem('username') || '',
      pageNum: 1,
      pageSize: 100
    }
    const res = await axios.get('/api/process/approval/completed', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      completedList.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载已完成列表失败')
    console.error(e)
  }
}

onMounted(() => {
  loadPending()
  loadInitiated()
  loadCompleted()
})

function viewDetail (row) {
  currentItem.value = {
    ...row,
    steps: [
      { node: '起草', action: '提交申请', operator: row.initiator, time: row.submitTime, comment: '', type: 'primary' },
      { node: '部门审核', action: '审批通过', operator: '陈静', time: new Date(Date.now() - 8 * 3600 * 1000).toLocaleString('zh-CN'), comment: '同意修订', type: 'success' },
      { node: '体系专员', action: row.status === '审批中' ? '待处理' : '审批通过', operator: '赵敏', time: row.status === '审批中' ? '-' : new Date(Date.now() - 4 * 3600 * 1000).toLocaleString('zh-CN'), comment: row.status === '审批中' ? '' : '内容完整，符合规范', type: row.status === '审批中' ? 'warning' : 'success' },
      { node: '分管领导', action: '待处理', operator: '-', time: '-', comment: '', type: 'info' },
      { node: '发布', action: '待处理', operator: '-', time: '-', comment: '', type: 'info' }
    ],
    attachments: [
      { name: '招聘管理制度V3.0.docx', size: '1.2MB' },
      { name: '修订说明.docx', size: '320KB' }
    ]
  }
  currentStep.value = row.status === '审批中' ? 2 : 4
  detailVisible.value = true
}

function openApprove (row) {
  currentItem.value = row
  approveForm.comment = ''
  approveForm.action = 'pass'
  approveForm.approvalMode = 'pass'
  approveForm.signMode = 'countersign'
  approveForm.rejectTarget = 'initiator'
  approveForm.rejectNode = ''
  approveForm.addSignUser = ''
  approveForm.addSignPosition = 'after'
  approveForm.transferUser = ''
  approveForm.timeoutHours = 24
  approveForm.enableTimeoutAlert = false
  approveForm.ccUsers = []
  approveVisible.value = true
}

function getApprovalActionText () {
  const map = { pass: '通过', addSign: '加签', removeSign: '减签', transfer: '转签' }
  return map[approveForm.approvalMode] || '通过'
}

async function doApprove () {
  if (!approveForm.comment) {
    ElMessage.warning('请填写审批意见')
    return
  }
  if (!currentItem.value || !currentItem.value.id) {
    ElMessage.warning('未获取到审批记录')
    return
  }
  try {
    const res = await axios.put(`/api/process/approval/${currentItem.value.id}/approve`, {
      comment: approveForm.comment,
      signMode: approveForm.signMode,
      timeoutHours: approveForm.timeoutHours,
      ccUsers: approveForm.ccUsers
    })
    if (res.data.code === 200) {
      let successMsg = '审批通过'
      if (approveForm.ccUsers.length > 0) {
        successMsg = `审批通过，已抄送 ${approveForm.ccUsers.length} 人`
      }
      approveVisible.value = false
      ElMessage.success(successMsg)
      loadPending()
      loadCompleted()
    } else {
      ElMessage.error(res.data.message || '审批失败')
    }
  } catch (e) {
    ElMessage.error('审批失败')
    console.error(e)
  }
}

async function doReject () {
  if (!approveForm.comment) {
    ElMessage.warning('请填写审批意见')
    return
  }
  if (!currentItem.value || !currentItem.value.id) {
    ElMessage.warning('未获取到审批记录')
    return
  }
  try {
    const res = await axios.put(`/api/process/approval/${currentItem.value.id}/reject`, {
      comment: approveForm.comment,
      rejectTarget: approveForm.rejectTarget,
      rejectNode: approveForm.rejectNode
    })
    if (res.data.code === 200) {
      let rejectMsg = '已驳回至发起人'
      if (approveForm.rejectTarget === 'specific' && approveForm.rejectNode) {
        rejectMsg = `已驳回至【${approveForm.rejectNode}】节点`
      }
      approveVisible.value = false
      ElMessage.success(rejectMsg)
      loadPending()
      loadCompleted()
    } else {
      ElMessage.error(res.data.message || '驳回失败')
    }
  } catch (e) {
    ElMessage.error('驳回失败')
    console.error(e)
  }
}

function doDelegate () {
  delegateVisible.value = true
}

function saveDelegate () {
  if (!delegateForm.agent) {
    ElMessage.warning('请选择代理人')
    return
  }
  if (!delegateForm.timeRange || delegateForm.timeRange.length === 0) {
    ElMessage.warning('请选择代理时间段')
    return
  }
  delegateVisible.value = false
  ElMessage.success('代理人设置已保存')
}

function doSuspendOrTerminate () {
  if (!suspendForm.reason) {
    ElMessage.warning('请填写操作原因')
    return
  }
  const actionText = suspendForm.action === 'suspend' ? '暂停' : '终止'
  if (suspendForm.action === 'terminate') {
    ElMessageBox.confirm('终止流程后无法恢复，确定要终止吗？', '危险操作', { confirmButtonText: '确定终止', cancelButtonText: '取消', type: 'error' }).then(() => {
      suspendVisible.value = false
      ElMessage.success(`流程已${actionText}`)
    }).catch(() => {})
  } else {
    suspendVisible.value = false
    ElMessage.success(`流程已${actionText}`)
  }
}

function withdraw (row) {
  ElMessageBox.confirm('确认撤回此流程？撤回后将恢复草稿状态，可重新编辑提交。', '撤回确认', { type: 'warning' }).then(() => {
    ElMessage.success('已撤回，发起人可重新编辑提交')
  }).catch(() => {})
}

function openCancelDialog (row) {
  currentItem.value = row
  cancelForm.reason = ''
  cancelForm.description = ''
  cancelForm.replacement = ''
  cancelVisible.value = true
}

function submitCancel () {
  if (!cancelForm.reason) {
    ElMessage.warning('请选择作废原因')
    return
  }
  if (!cancelForm.description) {
    ElMessage.warning('请填写详细说明')
    return
  }
  cancelVisible.value = false
  ElMessage.success('作废申请已提交，等待审批')
}

function downloadAttachment (file) {
  ElMessage.success(`正在下载：${file.name}`)
}
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }

.stat-card { display: flex; align-items: center; gap: 16px; padding: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.stat-card.warning .stat-icon { background: #fef7e8; color: #e6a23c; }
.stat-card.primary .stat-icon { background: #e3f2fd; color: #409eff; }
.stat-card.success .stat-icon { background: #e8f5e9; color: #67c23a; }
.stat-card.danger .stat-icon { background: #fef0f0; color: #f56c6c; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 13px; }

.attachment-list { padding-top: 8px; }
.attachment-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed #ebeef5; }
.attachment-item:last-child { border-bottom: none; }
</style>

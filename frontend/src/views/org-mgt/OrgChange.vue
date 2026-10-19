<template>
  <div class="page-container">
    <div class="page-header">
      <h2>组织变更</h2>
      <el-button type="primary" @click="handleAdd">发起变更</el-button>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="审批中心" name="approval">
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="请输入关键词" style="width: 180px" />
      </el-form-item>
      <el-form-item label="变更类型">
        <el-select v-model="searchForm.changeType" placeholder="选择类型" style="width: 150px" clearable>
          <el-option label="组织新增" value="CREATE" />
          <el-option label="组织删除" value="DELETE" />
          <el-option label="名称修改" value="NAME_CHANGE" />
          <el-option label="层级调整" value="MIGRATE" />
          <el-option label="上级归属变更" value="TRANSFER" />
          <el-option label="负责人变更" value="MANAGER_CHANGE" />
          <el-option label="状态启停" value="DISABLE" />
          <el-option label="业务属性变更" value="UPDATE" />
          <el-option label="合并组织" value="MERGE" />
          <el-option label="拆分组织" value="SPLIT" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px" clearable>
          <el-option label="草稿" value="DRAFT" />
          <el-option label="审批中" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已撤销" value="CANCELLED" />
          <el-option label="已执行" value="EXECUTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="changeNo" label="变更单号" width="160" />
      <el-table-column prop="changeTitle" label="变更标题" width="180" show-overflow-tooltip />
      <el-table-column prop="changeType" label="变更类型" width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="getChangeTypeTag(row.changeType)">{{ getChangeTypeLabel(row.changeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orgName" label="涉及组织" width="140" />
      <el-table-column prop="approvalStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.approvalStatus)" size="small">
            {{ getStatusLabel(row.approvalStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前节点" width="160">
        <template #default="{ row }">
          {{ getNodeLabel(row.currentNode) }}
        </template>
      </el-table-column>
      <el-table-column prop="initiatorName" label="发起人" width="100" />
      <el-table-column label="发起时间" width="160">
        <template #default="{ row }">
          {{ row.submitTime || row.createTime }}
        </template>
      </el-table-column>
      <el-table-column prop="effectiveDate" label="生效日期" width="120" />
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" v-if="row.approvalStatus === 'DRAFT'" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" v-if="row.approvalStatus === 'DRAFT'" @click="handleSubmit(row)">提交审批</el-button>
          <el-button size="small" type="danger" v-if="row.approvalStatus === 'DRAFT'" @click="handleDelete(row)">删除</el-button>
          <el-button size="small" type="success" v-if="row.approvalStatus === 'PENDING'" @click="handleApprove(row)">通过</el-button>
          <el-button size="small" type="danger" v-if="row.approvalStatus === 'PENDING'" @click="handleReject(row)">驳回</el-button>
          <el-button size="small" type="warning" v-if="row.approvalStatus === 'PENDING'" @click="handleRecall(row)">撤回</el-button>
          <el-button size="small" type="info" v-if="row.approvalStatus === 'PENDING'" @click="handleAddSign(row)">加签</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right"
    />
      </el-tab-pane>

      <el-tab-pane label="变更历史台账" name="history">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="historySearch.keyword" placeholder="变更编号/组织/标题" style="width: 180px" />
          </el-form-item>
          <el-form-item label="组织编码">
            <el-input v-model="historySearch.orgCode" placeholder="请输入组织编码" style="width: 140px" />
          </el-form-item>
          <el-form-item label="变更类型">
            <el-select v-model="historySearch.changeType" placeholder="选择类型" style="width: 150px" clearable>
              <el-option label="组织新增" value="CREATE" />
              <el-option label="组织删除" value="DELETE" />
              <el-option label="名称修改" value="NAME_CHANGE" />
              <el-option label="层级调整" value="MIGRATE" />
              <el-option label="上级归属变更" value="TRANSFER" />
              <el-option label="负责人变更" value="MANAGER_CHANGE" />
              <el-option label="状态启停" value="DISABLE" />
              <el-option label="业务属性变更" value="UPDATE" />
              <el-option label="合并组织" value="MERGE" />
              <el-option label="拆分组织" value="SPLIT" />
            </el-select>
          </el-form-item>
          <el-form-item label="审批状态">
            <el-select v-model="historySearch.status" placeholder="选择状态" style="width: 120px" clearable>
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="审批中" value="PENDING" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="historySearch.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadHistory(1)">搜索</el-button>
            <el-button @click="resetHistorySearch">重置</el-button>
            <el-button type="success" @click="exportHistory">导出台账</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
          <el-table-column prop="changeNo" label="变更编号" width="150" />
          <el-table-column prop="orgName" label="变更组织" width="130" />
          <el-table-column prop="changeType" label="变更类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="getChangeTypeTag(row.changeType)">{{ getChangeTypeLabel(row.changeType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="beforeContent" label="变更前内容" min-width="160" show-overflow-tooltip />
          <el-table-column prop="afterContent" label="变更后内容" min-width="160" show-overflow-tooltip />
          <el-table-column prop="initiatorName" label="发起人" width="90" />
          <el-table-column prop="approvalTime" label="变更时间" width="160" show-overflow-tooltip />
          <el-table-column prop="approvalStatus" label="审批状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.approvalStatus)" size="small">
                {{ getStatusLabel(row.approvalStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approvalProcess" label="审批流程" width="130" show-overflow-tooltip />
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewHistoryDetail(row)">详情</el-button>
              <el-button size="small" type="info" @click="viewCompare(row)">对比版本</el-button>
              <el-button size="small" type="success" plain @click="exportHistoryRecord(row)">导出记录</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          @size-change="(val) => { historyPagination.pageSize = val; loadHistory() }"
          @current-change="(val) => loadHistory(val)"
          :current-page="historyPagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="historyPagination.pageSize"
          :total="historyPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="compareVisible" title="版本对比（差异高亮）" width="800px">
      <el-row :gutter="16">
        <el-col :span="12">
          <h4 style="margin-top: 0">变更前</h4>
          <pre class="compare-pane" v-html="diffHtml(compareData.beforeContent, compareData.afterContent, true)"></pre>
        </el-col>
        <el-col :span="12">
          <h4 style="margin-top: 0">变更后</h4>
          <pre class="compare-pane" v-html="diffHtml(compareData.afterContent, compareData.beforeContent, false)"></pre>
        </el-col>
      </el-row>
    </el-dialog>

    <el-dialog v-model="historyDetailVisible" title="变更历史详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="变更编号">{{ historyDetail.changeNo }}</el-descriptions-item>
        <el-descriptions-item label="变更标题">{{ historyDetail.changeTitle }}</el-descriptions-item>
        <el-descriptions-item label="变更组织">{{ historyDetail.orgName }}</el-descriptions-item>
        <el-descriptions-item label="变更类型">{{ getChangeTypeLabel(historyDetail.changeType) }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ historyDetail.initiatorName }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ historyDetail.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(historyDetail.approvalStatus)" size="small">
            {{ getStatusLabel(historyDetail.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ historyDetail.approvalTime }}</el-descriptions-item>
        <el-descriptions-item label="审批流程" :span="2">{{ historyDetail.approvalProcess }}</el-descriptions-item>
        <el-descriptions-item label="变更原因" :span="2">{{ historyDetail.changeReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ historyDetail.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变更单号">
              <el-input v-model="formData.changeNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更类型" required>
              <el-select v-model="formData.changeType" style="width: 100%">
                <el-option label="组织新增" value="CREATE" />
                <el-option label="组织删除" value="DELETE" />
                <el-option label="名称修改" value="NAME_CHANGE" />
                <el-option label="层级调整" value="MIGRATE" />
                <el-option label="上级归属变更" value="TRANSFER" />
                <el-option label="负责人变更" value="MANAGER_CHANGE" />
                <el-option label="状态启停" value="DISABLE" />
                <el-option label="业务属性变更" value="UPDATE" />
                <el-option label="合并组织" value="MERGE" />
                <el-option label="拆分组织" value="SPLIT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="变更标题" required>
          <el-input v-model="formData.changeTitle" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="涉及组织编号">
              <el-input-number v-model="formData.orgId" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织名称">
              <el-input v-model="formData.orgName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期">
              <el-date-picker
                v-model="formData.effectiveDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.approvalStatus" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="审批中" value="PENDING" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="变更内容">
          <el-input v-model="formData.changeContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="formData.changeReason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="组织变更详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="变更单号">{{ detailData.changeNo }}</el-descriptions-item>
        <el-descriptions-item label="变更标题">{{ detailData.changeTitle }}</el-descriptions-item>
        <el-descriptions-item label="变更类型">{{ getChangeTypeLabel(detailData.changeType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.approvalStatus)" size="small">
            {{ getStatusLabel(detailData.approvalStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ getNodeLabel(detailData.currentNode) }}</el-descriptions-item>
        <el-descriptions-item label="涉及组织">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detailData.initiatorName }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ detailData.submitTime || detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="变更内容" :span="2">{{ detailData.changeContent }}</el-descriptions-item>
        <el-descriptions-item label="变更原因" :span="2">{{ detailData.changeReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)

const searchForm = reactive({
  keyword: '',
  changeType: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('发起组织变更')
const formRef = ref(null)

const formData = reactive({
  id: null,
  changeNo: '',
  changeTitle: '',
  changeType: 'CREATE',
  orgId: null,
  orgName: '',
  approvalStatus: 'DRAFT',
  initiatorName: '',
  effectiveDate: '',
  changeContent: '',
  changeReason: '',
  remark: ''
})

const detailData = reactive({})
const list = ref([])

const activeTab = ref('approval')

const historyLoading = ref(false)
const historyList = ref([])
const historySearch = reactive({ keyword: '', orgCode: '', changeType: '', status: '', dateRange: [] })
const historyPagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const compareVisible = ref(false)
const compareData = reactive({ beforeContent: '', afterContent: '' })
const historyDetailVisible = ref(false)
const historyDetail = reactive({})

async function loadHistory(page) {
  if (page) historyPagination.currentPage = page
  historyLoading.value = true
  try {
    const [startTime, endTime] = historySearch.dateRange || []
    const res = await axios.get('/api/org-change/history/list', {
      params: {
        page: historyPagination.currentPage,
        pageSize: historyPagination.pageSize,
        orgCode: historySearch.orgCode || undefined,
        changeType: historySearch.changeType || undefined,
        startTime: startTime || undefined,
        endTime: endTime || undefined
      }
    })
    if (res.data.code === 200) {
      let data = res.data.data.list || res.data.data || []
      if (historySearch.status) data = data.filter(i => i.approvalStatus === historySearch.status)
      if (historySearch.keyword) {
        const kw = historySearch.keyword.toLowerCase()
        data = data.filter(i => (i.changeNo || '').toLowerCase().includes(kw) || (i.orgName || '').toLowerCase().includes(kw) || (i.changeTitle || '').toLowerCase().includes(kw))
      }
      historyList.value = data
      historyPagination.total = data.length
    }
  } catch (e) {
    ElMessage.error('加载变更历史失败')
    console.error(e)
  } finally {
    historyLoading.value = false
  }
}

function resetHistorySearch() {
  Object.assign(historySearch, { keyword: '', orgCode: '', changeType: '', status: '', dateRange: [] })
  loadHistory(1)
}

function diffHtml(source, other, isBefore) {
  if (!source) return '<span style="color:#c0c4cc">（空）</span>'
  const sourceStr = String(source)
  const otherStr = String(other || '')
  // Simple line-based diff: highlight lines that differ
  const srcLines = sourceStr.split('\n')
  const otherLines = otherStr.split('\n')
  return srcLines.map((line, idx) => {
    const changed = otherLines[idx] !== line
    const cls = isBefore ? (changed ? 'diff-removed' : '') : (changed ? 'diff-added' : '')
    const esc = line.replace(/[&<>]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[c]))
    return changed ? `<span class="${cls}">${esc || '(空)'}</span>` : esc
  }).join('\n')
}

function exportHistoryRecord(row) {
  try {
    const content = [
      `变更编号: ${row.changeNo}`,
      `变更组织: ${row.orgName}`,
      `变更类型: ${getChangeTypeLabel(row.changeType)}`,
      `变更前内容:\n${row.beforeContent || ''}`,
      `变更后内容:\n${row.afterContent || ''}`,
      `发起人: ${row.initiatorName}`,
      `审批时间: ${row.approvalTime}`,
      `审批状态: ${getStatusLabel(row.approvalStatus)}`,
      `审批流程: ${row.approvalProcess || ''}`
    ].join('\n')
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `变更记录_${row.changeNo || row.id}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

function handleAddSign(row) {
  ElMessageBox.prompt('请输入加签人姓名', '审批加签', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '加签人'
  }).then(async ({ value }) => {
    if (!value) return
    ElMessage.info(`已向 ${value} 发起加签（流程引擎处理）`)
  }).catch(() => {})
}

async function exportHistory() {
  try {
    const res = await axios.post('/api/org-change/history/export', {
      keyword: historySearch.keyword || undefined,
      changeType: historySearch.changeType || undefined
    }, { responseType: 'blob', timeout: 60000 })
    const blob = res.data
    if (blob.type && blob.type.includes('json')) {
      ElMessage.error('导出失败')
      return
    }
    const url = URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = '组织变更台账.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败：' + (e.message || ''))
  }
}

function viewHistoryDetail(row) {
  Object.keys(historyDetail).forEach(k => delete historyDetail[k])
  Object.assign(historyDetail, row)
  historyDetailVisible.value = true
}

function viewCompare(row) {
  compareData.beforeContent = row.beforeContent
  compareData.afterContent = row.afterContent
  compareVisible.value = true
}

function handleRecall(row) {
  ElMessageBox.confirm('确定要撤回该变更申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-change/approval/${row.id}/recall`)
      if (res.data.code === 200) {
        ElMessage.success('已撤回')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '撤回失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '撤回失败')
    }
  }).catch(() => {})
}

function getChangeTypeLabel(type) {
  const map = {
    CREATE: '组织新增',
    DELETE: '组织删除',
    NAME_CHANGE: '名称修改',
    MIGRATE: '层级调整',
    TRANSFER: '上级归属变更',
    MANAGER_CHANGE: '负责人变更',
    DISABLE: '状态启停',
    UPDATE: '业务属性变更',
    MERGE: '合并组织',
    SPLIT: '拆分组织',
    ADD: '组织新增',
    REMOVE: '组织删除',
    ADJUST: '调整组织'
  }
  return map[type] || type
}

function getChangeTypeTag(type) {
  const map = {
    CREATE: 'success', DELETE: 'danger', NAME_CHANGE: '', MIGRATE: 'warning',
    TRANSFER: 'warning', MANAGER_CHANGE: 'primary', DISABLE: 'info',
    UPDATE: '', MERGE: 'warning', SPLIT: 'warning'
  }
  return map[type] || 'info'
}

function getStatusType(status) {
  const map = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
    EXECUTED: 'success'
  }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = {
    DRAFT: '草稿',
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    CANCELLED: '已撤销',
    EXECUTED: '已执行'
  }
  return map[status] || status
}

function getNodeLabel(node) {
  const map = {
    INITIATE: '发起申请',
    DIRECT_SUPERVISOR: '直属负责人初审',
    HR_REVIEW: '人力组织岗复审',
    LEADER_FINAL: '分管领导终审',
    PARTY_APPROVAL: '党委审批',
    FINAL_APPROVAL: '终审'
  }
  return map[node] || (node ? node : '—')
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/org-change/list', {
      params: {
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword || undefined,
        changeType: searchForm.changeType || undefined,
        status: searchForm.status || undefined
      }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || res.data.data || []
      pagination.total = res.data.data.total || list.value.length
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.changeType = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadList()
}

function handleAdd() {
  dialogTitle.value = '发起组织变更'
  Object.assign(formData, {
    id: null,
    changeNo: '',
    changeTitle: '',
    changeType: 'CREATE',
    orgId: null,
    orgName: '',
    approvalStatus: 'DRAFT',
    initiatorName: '',
    effectiveDate: '',
    changeContent: '',
    changeReason: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑组织变更'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/org-change/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function handleSubmit(row) {
  ElMessageBox.confirm('确定要提交审批吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post(`/api/org-change/${row.id}/submit`)
      if (res.data.code === 200) {
        ElMessage.success('提交成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '提交失败')
      }
    } catch (e) {
      ElMessage.error('提交失败')
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除吗？删除后不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/org-change/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

function handleApprove(row) {
  ElMessageBox.prompt('请输入审批意见（可选）', '通过审批', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '审批意见'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/org-change/approval/${row.id}/approve`, { opinion: value || '' })
      if (res.data.code === 200) {
        ElMessage.success(res.data.data?.message || '审批通过')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '审批失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '审批失败')
    }
  }).catch(() => {})
}

function handleReject(row) {
  ElMessageBox.prompt('请输入驳回原因', '驳回审批', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '驳回原因不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/org-change/approval/${row.id}/reject`, { reason: value })
      if (res.data.code === 200) {
        ElMessage.success('已驳回')
        loadList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '驳回失败')
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '驳回失败')
    }
  }).catch(() => {})
}

async function handleSubmitForm() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/org-change/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/org-change', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '发起组织变更' ? '保存成功' : '修改成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadList()
  loadHistory()
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
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.compare-pane {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  min-height: 200px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
}

.diff-removed {
  background: #fde2e2;
  color: #c0392b;
  padding: 0 4px;
  border-radius: 2px;
}

.diff-added {
  background: #e1f3d8;
  color: #2e7d32;
  padding: 0 4px;
  border-radius: 2px;
}
</style>

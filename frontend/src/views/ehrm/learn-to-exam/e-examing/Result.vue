<template>
  <div class="page-card">
    <div class="page-header">
      <h2>考试结果</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>录入成绩
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
      在线考试成绩与结果管理，记录员工得分、是否及格、考试通过率与证书获取情况，支持成绩复核与证书发放。
    </el-alert>

    <!-- 成绩统计（前端算法：平均分 / 通过率 / 及格人数） -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-item"><span class="stat-num">{{ scoreStats.total }}</span><span class="stat-label">成绩记录数</span></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-item"><span class="stat-num" style="color:#409eff">{{ scoreStats.avgScore }}</span><span class="stat-label">平均分</span></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-item"><span class="stat-num" style="color:#67c23a">{{ scoreStats.passedCount }}</span><span class="stat-label">及格人数</span></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never"><div class="stat-item"><span class="stat-num" style="color:#e6a23c">{{ scoreStats.passRate }}%</span><span class="stat-label">通过率</span></div></el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employee" placeholder="请输入员工姓名" style="width:180px" clearable />
        </el-form-item>
        <el-form-item label="考试名称">
          <el-input v-model="searchForm.examName" placeholder="请输入考试名称" style="width:200px" clearable />
        </el-form-item>
        <el-form-item label="是否及格">
          <el-select v-model="searchForm.passed" placeholder="选择结果" style="width:140px" clearable>
            <el-option label="及格" value="yes" />
            <el-option label="不及格" value="no" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="resultList" border stripe size="small">
        <el-table-column prop="id" label="记录ID" width="110" />
        <el-table-column prop="employee" label="员工姓名" width="110">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employee }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="examName" label="考试名称" min-width="180">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.examName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="成绩" width="90" align="center">
          <template #default="{row}">
            <span :style="{color: row.score >= row.passScore ? '#67c23a' : '#f56c6c', fontWeight: 'bold'}">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="passScore" label="及格分" width="80" align="center" />
        <el-table-column prop="passed" label="结果" width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.passed ? 'success' : 'danger'" size="small">{{ row.passed ? '及格' : '不及格' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="160" align="center">
          <template #default="{row}">
            <el-progress :percentage="row.passRate" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column prop="certStatus" label="证书" width="110" align="center">
          <template #default="{row}">
            <el-tag :type="getCertType(row.certStatus)" size="small">{{ getCertLabel(row.certStatus) }}</el-tag>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：风控日志（切屏次数/设备ID/IP） -->
        <el-table-column label="风控日志" width="130" align="center">
          <template #default="{row}">
            <el-tooltip v-if="row.switchCount > 0" :content="`切屏${row.switchCount}次 / 设备:${row.deviceId || '-'} / IP:${row.ip || '-'}`" placement="top">
              <el-tag type="danger" size="small">切屏{{ row.switchCount }}次</el-tag>
            </el-tooltip>
            <span v-else style="color:#909399">无异常</span>
          </template>
        </el-table-column>
        <el-table-column prop="examTime" label="考试时间" width="150" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" type="warning" @click="reviewScore(row)">复核</el-button>
            <el-button link size="small" type="success" @click="issueCert(row)">发证</el-button>
            <el-button link size="small" type="danger" @click="voidScore(row)">作废</el-button>
            <el-button link size="small" type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; text-align:right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width:100%">
                <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试名称" required>
              <el-input v-model="formData.courseName" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试时间" required>
              <el-date-picker v-model="formData.examTime" type="datetime" placeholder="选择考试时间" style="width:100%" value-format="YYYY-MM-DD HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总分">
              <el-input-number v-model="formData.totalScore" :min="1" :max="200" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格分">
              <el-input-number v-model="formData.passScore" :min="1" :max="200" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实际成绩" required>
              <el-input-number v-model="formData.overallScore" :min="0" :max="200" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证书发放">
              <el-switch v-model="formData.hasCertificate" :active-value="1" :inactive-value="0" active-text="已发证" inactive-text="未发证" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="3" placeholder="请输入成绩备注（如复核说明等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

const searchForm = reactive({ employee: '', examName: '', passed: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('录入成绩')
const formRef = ref(null)

const formData = reactive({
  id: '', employeeName: '', department: '', courseName: '', examTime: '',
  totalScore: 100, passScore: 60, overallScore: 0, hasCertificate: 0, remark: ''
})

const resultList = ref([])

function getCertLabel(value) { const labels = { issued: '已发证', pending: '待发证', none: '无证书', VALID: '已发证', EXPIRED: '已过期', REVOKED: '已撤销' }; return labels[value] || value }
function getCertType(value) { const types = { issued: 'success', pending: 'warning', none: 'info', VALID: 'success', EXPIRED: 'info', REVOKED: 'danger' }; return types[value] || 'info' }

async function loadResults() {
  try {
    const { data } = await request.get('/api/learnexam/evaluation/list', {
      params: { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    })
    const records = data?.records || data?.data || []
    resultList.value = records.map(r => {
      const score = Number(r.overallScore || r.score || 0)
      const passed = score >= Number(r.passScore || 60)
      return {
        id: r.id, employee: r.employeeName || r.employee, department: r.department || '',
        examName: r.courseName || r.examName || '', examTime: r.evaluationTime || r.examTime || '',
        totalScore: r.totalScore || 100, passScore: r.passScore || 60, score, passed,
        passRate: r.passRate || 0, certStatus: r.certificateStatus || (r.hasCertificate ? 'issued' : 'none'), remark: r.improvementSuggestions || r.remark || '',
        // 规格书字段，待后端扩展：风控日志
        switchCount: r.switchCount || 0, deviceId: r.deviceId || '', ip: r.ip || ''
      }
    })
    pagination.total = data?.total || resultList.value.length
  } catch (e) {
    ElMessage.error('成绩列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1; loadResults() }

// 成绩统计算法：平均分 / 及格人数 / 通过率（前端实时计算）
const scoreStats = computed(() => {
  const list = resultList.value
  const total = list.length
  if (!total) return { total: 0, avgScore: 0, passedCount: 0, passRate: 0 }
  const sum = list.reduce((s, r) => s + Number(r.score || 0), 0)
  const passedCount = list.filter(r => r.passed).length
  return {
    total,
    avgScore: (sum / total).toFixed(1),
    passedCount,
    passRate: ((passedCount / total) * 100).toFixed(1)
  }
})
function resetSearch() { searchForm.employee = ''; searchForm.examName = ''; searchForm.passed = ''; loadResults() }
function refreshData() { loadResults(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '录入成绩'
  Object.assign(formData, {
    id: '', employeeName: '', department: '', courseName: '', examTime: '',
    totalScore: 100, passScore: 60, overallScore: 0, hasCertificate: 0, remark: ''
  })
  dialogVisible.value = true
}

function viewDetail(row) { ElMessage.info(`查看成绩详情: ${row.employee} - ${row.examName}`) }

function editItem(row) {
  dialogTitle.value = '编辑成绩'
  Object.assign(formData, {
    id: row.id, employeeName: row.employee, department: row.department, courseName: row.examName,
    examTime: row.examTime, totalScore: row.totalScore, passScore: row.passScore, overallScore: row.score,
    hasCertificate: row.certStatus === 'issued' ? 1 : 0, remark: row.remark
  })
  dialogVisible.value = true
}

function issueCert(row) {
  if (!row.passed) { ElMessage.warning('不及格，无法发证'); return }
  if (row.certStatus === 'issued') { ElMessage.info('该记录已发证'); return }
  ElMessageBox.confirm(`确定为 ${row.employee} 发放证书吗？`, '发证确认', { type: 'warning' })
    .then(async () => {
      try { await request.post(`/api/learnexam/evaluation/${row.id}/issue-certificate`); row.certStatus = 'issued'; ElMessage.success('证书已发放') }
      catch(e){ ElMessage.error('发证失败') }
    })
    .catch(() => {})
}

// 成绩复核：录入复核说明并回写（improvementSuggestions 承载复核结论）
function reviewScore(row) {
  ElMessageBox.prompt(`请输入 ${row.employee} - ${row.examName} 的成绩复核结论`, '成绩复核', {
    confirmButtonText: '提交复核', cancelButtonText: '取消',
    inputPlaceholder: '复核说明（如：复核无误 / 调整为XX分）', inputType: 'textarea'
  }).then(async ({ value }) => {
    try {
      await request.put('/api/learnexam/evaluation', {
        id: row.id, improvementSuggestions: `【复核】${value}`
      })
      ElMessage.success('复核结论已提交')
      loadResults()
    } catch (e) { ElMessage.error('复核提交失败') }
  }).catch(() => {})
}

// 作废成绩：红色二次确认后回写作废标记
function voidScore(row) {
  ElMessageBox.confirm(
    `确定作废 ${row.employee} 的成绩吗？作废后成绩不参与统计与发证，已发证书需人工回收。`,
    '作废成绩确认（高危操作）',
    { type: 'error', confirmButtonText: '确认作废', confirmButtonClass: 'el-button--danger' }
  ).then(async () => {
    try {
      await request.put('/api/learnexam/evaluation', {
        id: row.id, certificateStatus: 'none', hasCertificate: 0, improvementSuggestions: '【成绩作废】该成绩已作废，不参与统计'
      })
      ElMessage.success('成绩已作废')
      loadResults()
    } catch (e) { ElMessage.error('作废失败') }
  }).catch(() => {})
}

function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除 ${row.employee} 的成绩记录吗？`, '删除确认', { type: 'warning' })
    .then(async () => { try { await request.delete(`/api/learnexam/evaluation/${row.id}`); ElMessage.success('成绩记录已删除'); loadResults() } catch(e){ ElMessage.error('删除失败') } })
    .catch(() => {})
}

async function saveData() {
  if (!formData.employeeName) { ElMessage.warning('请填写员工姓名'); return }
  if (formData.overallScore > formData.totalScore) { ElMessage.warning('实际成绩不能大于总分'); return }
  try {
    const payload = {
      ...formData,
      employeeName: formData.employeeName,
      courseName: formData.courseName,
      overallScore: formData.overallScore,
      improvementSuggestions: formData.remark
    }
    if (formData.id) {
      await request.put('/api/learnexam/evaluation', payload)
    } else {
      await request.post('/api/learnexam/evaluation', payload)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '成绩已更新' : '成绩已录入')
    loadResults()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadResults())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-num { font-size: 22px; font-weight: 700; color: #303133; }
.stat-label { font-size: 12px; color: #909399; }
</style>

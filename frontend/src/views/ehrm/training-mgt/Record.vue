<template>
  <div class="page-card">
    <div class="page-header">
      <h2>培训记录</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="recordList"
          file-name="培训记录"
          title="培训记录"
          import-api="/api/training/record"
          :field-map="ioFieldMap"
          @done="loadRecordList"
        />
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建记录
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="培训记录"
      description="记录员工参加培训的情况，包含培训课程、培训时间、完成时长、培训结果与证书发放信息，作为人才发展依据。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.employeeName" placeholder="请输入姓名" style="width: 180px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width: 180px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="请输入课程名称" style="width: 200px" />
        </el-form-item>
        <el-form-item label="培训结果">
          <el-select v-model="searchForm.result" placeholder="选择结果" style="width: 150px" clearable>
            <el-option label="优秀" value="excellent" />
            <el-option label="合格" value="pass" />
            <el-option label="不合格" value="fail" />
            <el-option label="进行中" value="ongoing" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="recordList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="recordNo" label="记录编号" width="140" />
        <el-table-column label="员工姓名" width="110">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.employeeName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="courseName" label="培训课程" min-width="180" />
        <el-table-column prop="trainingDate" label="培训时间" width="120" />
        <el-table-column prop="duration" label="时长(h)" width="100" />
        <el-table-column label="培训结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)" size="small">{{ getResultLabel(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证书" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.certificateNo" type="success" size="small">{{ row.certificateNo }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="institution" label="培训机构" width="140" show-overflow-tooltip />
        <el-table-column prop="credit" label="学分" width="80">
          <template #default="{ row }">{{ row.credit ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.signInStatus === '已签到' ? 'success' : 'info'" size="small">{{ row.signInStatus || '未签到' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="evaluation" label="效果评估" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.evaluation" type="primary" size="small">{{ row.evaluation }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" type="success" @click="handleSignIn(row)">签到</el-button>
            <el-button link size="small" type="warning" @click="openEvaluation(row)">评估</el-button>
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
        @size-change="doSearch"
        @current-change="loadRecordList"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.department" placeholder="选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训课程" required>
              <el-input v-model="formData.courseName" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训时间" required>
              <el-date-picker v-model="formData.trainingDate" type="date" placeholder="选择培训日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时长(h)" required>
              <el-input-number v-model="formData.duration" :min="0.5" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训结果" required>
              <el-select v-model="formData.result" placeholder="选择结果" style="width: 100%">
                <el-option label="优秀" value="excellent" />
                <el-option label="合格" value="pass" />
                <el-option label="不合格" value="fail" />
                <el-option label="进行中" value="ongoing" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="证书编号">
              <el-input v-model="formData.certificateNo" placeholder="如已发证请填写证书编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训机构">
              <el-input v-model="formData.institution" placeholder="请输入培训机构" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训学分">
              <el-input-number v-model="formData.credit" :min="0" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签到状态">
              <el-select v-model="formData.signInStatus" placeholder="选择签到状态" style="width: 100%">
                <el-option label="未签到" value="未签到" />
                <el-option label="已签到" value="已签到" />
                <el-option label="迟到" value="迟到" />
                <el-option label="缺勤" value="缺勤" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="效果评估">
          <el-select v-model="formData.evaluation" placeholder="选择柯氏评估等级" style="width: 100%" clearable>
            <el-option label="一级-反应评估" value="一级-反应评估" />
            <el-option label="二级-学习评估" value="二级-学习评估" />
            <el-option label="三级-行为评估" value="三级-行为评估" />
            <el-option label="四级-结果评估" value="四级-结果评估" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训说明">
          <el-input type="textarea" v-model="formData.remark" :rows="4" placeholder="请输入培训内容、考核情况等说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evalVisible" title="培训效果评估" width="480px">
      <el-form label-width="100px">
        <el-form-item label="员工">
          <el-input :value="evalRow.employeeName" disabled />
        </el-form-item>
        <el-form-item label="培训课程">
          <el-input :value="evalRow.courseName" disabled />
        </el-form-item>
        <el-form-item label="评估等级">
          <el-select v-model="evalValue" placeholder="选择柯氏评估等级" style="width: 100%">
            <el-option label="一级-反应评估" value="一级-反应评估" />
            <el-option label="二级-学习评估" value="二级-学习评估" />
            <el-option label="三级-行为评估" value="三级-行为评估" />
            <el-option label="四级-结果评估" value="四级-结果评估" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEvaluation">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'recordNo', title: '记录编号' },
  { key: 'employeeName', title: '员工姓名' },
  { key: 'department', title: '部门' },
  { key: 'courseName', title: '培训课程' },
  { key: 'trainingDate', title: '培训时间' },
  { key: 'duration', title: '时长(h)' },
  { key: 'certificateNo', title: '证书编号' },
  { key: 'institution', title: '培训机构' },
  { key: 'credit', title: '学分' },
  { key: 'signInStatus', title: '签到状态' },
  { key: 'evaluation', title: '效果评估' }
]
const ioFieldMap = {
  '记录编号': 'recordNo',
  '员工姓名': 'employeeName',
  '部门': 'department',
  '培训课程': 'courseName',
  '课程名称': 'courseName',
  '培训时间': 'trainingDate',
  '培训日期': 'trainingDate',
  '时长(h)': 'duration',
  '时长': 'duration',
  '培训结果': 'result',
  '证书编号': 'certificateNo',
  '培训机构': 'institution',
  '学分': 'credit',
  '培训学分': 'credit',
  '签到状态': 'signInStatus',
  '效果评估': 'evaluation',
  '备注': 'remark'
}

// 部门选项
const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']

// 搜索表单状态
const searchForm = reactive({
  employeeName: '',
  department: '',
  courseName: '',
  result: ''
})

// 分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建培训记录')
const formRef = ref(null)

// 新建/编辑使用的表单数据
const formData = reactive({
  id: '',
  recordNo: '',
  employeeName: '',
  department: '',
  courseName: '',
  trainingDate: '',
  duration: 1,
  result: 'pass',
  certificateNo: '',
  institution: '',
  credit: null,
  signInStatus: '',
  evaluation: '',
  remark: ''
})

// 效果评估弹窗状态
const evalVisible = ref(false)
const evalRow = ref({})
const evalValue = ref('')

// 从后端加载的培训记录（TrainingMgt 8086）
const recordList = ref([])

// 从后端加载培训记录列表
const loadRecordList = async () => {
  try {
    const res = await axios.get('/api/training/record', {
      params: {
        current: pagination.currentPage,
        size: pagination.pageSize,
        employeeName: searchForm.employeeName || undefined,
        department: searchForm.department || undefined,
        courseName: searchForm.courseName || undefined,
        result: searchForm.result || undefined
      }
    })
    const data = res.data?.data || res.data
    recordList.value = data.records || data.list || []
    pagination.total = Number(data.total) || 0
  } catch (error) {
    console.error('加载培训记录失败', error)
    ElMessage.error('加载培训记录失败')
  }
}

onMounted(() => {
  loadRecordList()
})

// 将培训结果码映射为中文标签
function getResultLabel(result) {
  const labels = { excellent: '优秀', pass: '合格', fail: '不合格', ongoing: '进行中' }
  return labels[result] || result
}

// 将培训结果码映射为标签类型
function getResultType(result) {
  const types = { excellent: 'success', pass: 'primary', fail: 'danger', ongoing: 'warning' }
  return types[result] || 'info'
}

// 使用当前筛选条件触发搜索
function doSearch() {
  pagination.currentPage = 1
  loadRecordList()
}

// 重置搜索筛选条件
function resetSearch() {
  searchForm.employeeName = ''
  searchForm.department = ''
  searchForm.courseName = ''
  searchForm.result = ''
  pagination.currentPage = 1
  loadRecordList()
}

// 手动刷新数据
function refreshData() {
  loadRecordList()
  ElMessage.success('培训记录数据已刷新')
}

// 打开新建对话框并重置表单
function openCreate() {
  dialogTitle.value = '新建培训记录'
  Object.assign(formData, {
    id: '',
    recordNo: '',
    employeeName: '',
    department: '',
    courseName: '',
    trainingDate: '',
    duration: 1,
    result: 'pass',
    certificateNo: '',
    institution: '',
    credit: null,
    signInStatus: '',
    evaluation: '',
    remark: ''
  })
  dialogVisible.value = true
}

// 查看培训记录详情
function viewDetail(row) {
  ElMessage.info(`查看 ${row.employeeName} 的培训记录 ${row.recordNo} 详情`)
}

// 打开对话框编辑已有记录
function editItem(row) {
  dialogTitle.value = '编辑培训记录'
  Object.assign(formData, {
    id: row.id,
    recordNo: row.recordNo,
    employeeName: row.employeeName,
    department: row.department,
    courseName: row.courseName,
    trainingDate: row.trainingDate,
    duration: row.duration,
    result: row.result,
    certificateNo: row.certificateNo || '',
    institution: row.institution || '',
    credit: row.credit ?? null,
    signInStatus: row.signInStatus || '',
    evaluation: row.evaluation || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

// 学员签到（更新签到状态）
function handleSignIn(row) {
  ElMessageBox.confirm(`确认为 ${row.employeeName} 签到课程「${row.courseName}」吗？`, '签到确认', { type: 'info' })
    .then(async () => {
      await axios.put('/api/training/record', { ...row, signInStatus: '已签到' })
      ElMessage.success('签到成功')
      loadRecordList()
    })
    .catch(() => {})
}

// 打开效果评估弹窗
function openEvaluation(row) {
  evalRow.value = row
  evalValue.value = row.evaluation || ''
  evalVisible.value = true
}

// 保存效果评估等级
async function saveEvaluation() {
  if (!evalValue.value) {
    ElMessage.warning('请选择评估等级')
    return
  }
  try {
    await axios.put('/api/training/record', { ...evalRow.value, evaluation: evalValue.value })
    ElMessage.success('效果评估已保存')
    evalVisible.value = false
    loadRecordList()
  } catch (e) {
    ElMessage.error('评估保存失败')
  }
}

// 确认后删除培训记录
function deleteItem(row) {
  ElMessageBox.confirm(`确定要删除培训记录 ${row.recordNo} 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await axios.delete(`/api/training/record/${row.id}`)
      ElMessage.success('培训记录已删除')
      loadRecordList()
    })
    .catch(() => {})
}

// 保存表单数据（新建或更新）
async function saveData() {
  if (!formData.employeeName || !formData.department || !formData.courseName || !formData.trainingDate || !formData.result) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (formData.id) {
      await axios.put('/api/training/record', { ...formData })
      ElMessage.success('培训记录已更新')
    } else {
      await axios.post('/api/training/record', { ...formData })
      ElMessage.success('培训记录已新建')
    }
    dialogVisible.value = false
    loadRecordList()
  } catch (error) {
    console.error('保存培训记录失败', error)
    ElMessage.error('保存培训记录失败')
  }
}
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

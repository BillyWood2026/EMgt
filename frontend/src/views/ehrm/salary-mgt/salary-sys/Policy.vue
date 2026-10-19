<template>
  <div class="page-card">
    <div class="page-header">
      <h2>薪资项与政策管理</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="policyList"
          file-name="薪资项政策"
          title="薪资项政策列表"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增政策
        </el-button>
      </div>
    </div>

    <!-- 核心通用薪资算法（规格书第十四章 页面3） -->
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="核心通用薪资算法：日工资基数=基本工资÷21.75；时薪基数=日工资基数÷8；实发工资=应发合计-社保个人-公积金个人-专项扣除-个税-各类考勤扣款" />

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" class="search-form">
        <el-form-item label="政策名称">
          <el-input v-model="searchForm.keyword" placeholder="名称/编号关键词" style="width:180px" clearable />
        </el-form-item>
        <el-form-item label="薪资类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" style="width:140px" clearable>
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" style="width:120px" clearable>
            <el-option label="生效" value="生效" />
            <el-option label="已失效" value="已失效" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="filteredList" border stripe size="small" v-loading="loading">
        <el-table-column prop="policyNo" label="薪资项编码" width="130" />
        <el-table-column prop="name" label="薪资项名称" min-width="180">
          <template #default="{row}">
            <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="薪资类型" width="110">
          <template #default="{row}">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="核算公式" min-width="220" show-overflow-tooltip>
          <template #default="{row}">{{ row.content || '-' }}</template>
        </el-table-column>
        <el-table-column prop="scope" label="适用人群" width="140" />
        <el-table-column prop="effectiveDate" label="生效时间" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === '生效' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link size="small" @click="editPolicy(row)">编辑</el-button>
            <el-button link size="small" type="success" v-if="row.status !== '生效'" @click="enablePolicy(row)">启用</el-button>
            <el-button link size="small" type="danger" v-if="row.status === '生效'" @click="disablePolicy(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗（规格书950px规则配置弹窗） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="薪资项名称" required>
              <el-input v-model="formData.name" placeholder="请输入薪资项名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资类型" required>
              <el-select v-model="formData.type" placeholder="选择类型">
                <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" required>
              <el-date-picker v-model="formData.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择生效日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用人群">
              <el-input v-model="formData.scope" placeholder="例如：全体员工" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="核算公式">
          <el-input type="textarea" v-model="formData.content" :rows="4" placeholder="可视化公式内容，支持引用变量（考勤、绩效、假期、职级），如：基本工资÷21.75×计薪天数+绩效工资×绩效系数" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="取整规则">
              <el-select v-model="formData.roundingRule" placeholder="选择取整规则">
                <el-option label="四舍五入到分" value="四舍五入到分" />
                <el-option label="四舍五入到元" value="四舍五入到元" />
                <el-option label="向上取整" value="向上取整" />
                <el-option label="向下取整" value="向下取整" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="异常兜底值">
              <el-input-number v-model="formData.fallbackValue" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="核算优先级">
              <el-input-number v-model="formData.priority" :min="1" :max="99" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="薪资项政策详情" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="薪资项编码">{{ currentRow.policyNo }}</el-descriptions-item>
        <el-descriptions-item label="薪资项名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="薪资类型">{{ getTypeLabel(currentRow.type) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ currentRow.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="适用人群">{{ currentRow.scope }}</el-descriptions-item>
        <el-descriptions-item label="核算公式" :span="2">{{ currentRow.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取整规则">{{ currentRow.roundingRule || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常兜底值">{{ currentRow.fallbackValue ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="核算优先级">{{ currentRow.priority ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增薪资政策')
const currentRow = ref({})

const typeOptions = [
  { label: '基本工资', value: 'basic' },
  { label: '绩效工资', value: 'performance' },
  { label: '奖金', value: 'bonus' },
  { label: '津贴补贴', value: 'allowance' },
  { label: '扣款', value: 'deduction' },
  { label: '税费', value: 'tax' }
]

const searchForm = reactive({ keyword: '', type: '', status: '' })

const formData = reactive({
  id: null,
  policyNo: '',
  name: '',
  type: '',
  effectiveDate: '',
  scope: '',
  content: '',
  roundingRule: '',
  fallbackValue: 0,
  priority: 1
})

const policyList = ref([])

const ioColumns = [
  { key: 'policyNo', title: '薪资项编码' },
  { key: 'name', title: '薪资项名称' },
  { key: 'type', title: '薪资类型' },
  { key: 'content', title: '核算公式' },
  { key: 'scope', title: '适用人群' },
  { key: 'effectiveDate', title: '生效时间' },
  { key: 'status', title: '状态' }
]

const filteredList = computed(() => {
  return policyList.value.filter(item => {
    if (searchForm.keyword && !((item.name || '').includes(searchForm.keyword) || (item.policyNo || '').includes(searchForm.keyword))) return false
    if (searchForm.type && item.type !== searchForm.type) return false
    if (searchForm.status && item.status !== searchForm.status) return false
    return true
  })
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    const res = await axios.get('/api/salary/salary-item/list', { params })
    const data = res.data
    if (data && data.code === 200) {
      const payload = data.data
      policyList.value = Array.isArray(payload) ? payload : (payload && Array.isArray(payload.records) ? payload.records : [])
    } else if (data && Array.isArray(data.records)) {
      policyList.value = data.records
    } else if (Array.isArray(data)) {
      policyList.value = data
    } else {
      policyList.value = []
    }
  } catch (e) {
    ElMessage.error('加载薪资政策失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function getTypeLabel(type) {
  const hit = typeOptions.find(t => t.value === type)
  return hit ? hit.label : type
}

function getTypeTag(type) {
  const tags = { basic: 'primary', performance: 'success', bonus: 'warning', allowance: 'info', deduction: 'danger', tax: 'danger' }
  return tags[type] || 'info'
}

function handleSearch() {}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
}

function openAddDialog() {
  dialogTitle.value = '新增薪资政策'
  Object.assign(formData, {
    id: null,
    policyNo: 'XZ-' + new Date().getFullYear() + '-' + String(policyList.value.length + 1).padStart(3, '0'),
    name: '', type: '', effectiveDate: '', scope: '', content: '',
    roundingRule: '', fallbackValue: 0, priority: 1
  })
  dialogVisible.value = true
}

function editPolicy(row) {
  dialogTitle.value = '编辑薪资政策'
  Object.assign(formData, {
    id: row.id,
    policyNo: row.policyNo,
    name: row.name,
    type: row.type,
    effectiveDate: row.effectiveDate,
    scope: row.scope,
    content: row.content || '',
    roundingRule: row.roundingRule || '',
    fallbackValue: row.fallbackValue ?? 0,
    priority: row.priority ?? 1
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

async function changeStatus(row, status, actionName) {
  try {
    await ElMessageBox.confirm(`确定要${actionName}薪资政策"${row.name}"吗？`, `${actionName}确认`, { type: 'warning' })
    const res = await axios.put(`/api/salary/salary-item/${row.id}`, { ...row, status })
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(`薪资政策已${actionName}`)
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || `${actionName}失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${actionName}失败`)
      console.error(e)
    }
  }
}

function disablePolicy(row) { changeStatus(row, '已失效', '停用') }
function enablePolicy(row) { changeStatus(row, '生效', '启用') }

async function handleSubmit() {
  if (!formData.name || !formData.type || !formData.effectiveDate) {
    ElMessage.warning('请填写薪资项名称、薪资类型、生效日期')
    return
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/salary/salary-item/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/salary/salary-item', formData)
    }
    if (res.data && (res.data.code === 200 || res.data.code === undefined)) {
      ElMessage.success(dialogTitle.value === '新增薪资政策' ? '薪资政策新增成功' : '薪资政策已更新')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

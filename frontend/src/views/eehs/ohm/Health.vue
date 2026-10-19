<template>
  <div class="page-container">
    <div class="page-header">
      <h2>健康管理（一人一档）</h2>
      <div>
        <el-select v-model="currentRole" size="small" style="width:160px;margin-right:8px" @change="onRoleChange">
          <el-option label="本人" value="self" />
          <el-option label="HR健康管理员" value="hr_health" />
          <el-option label="医务岗" value="medical" />
          <el-option label="其他角色" value="other" />
        </el-select>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>

    <el-alert
      v-if="!canViewFull"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom:12px"
      title="IAM 隐私提示：健康档案含敏感个人信息，仅本人 / HR健康管理员 / 医务岗可查看完整数据，当前角色已脱敏展示。"
    />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="employeeName" label="员工姓名" width="100" />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="工号" width="100">
        <template #default="{row}">
          <span v-if="canViewFull">{{ row.employeeNo || '—' }}</span>
          <span v-else class="masked">******</span>
        </template>
      </el-table-column>
      <el-table-column prop="dept" label="部门" width="110" />
      <el-table-column prop="post" label="岗位" width="120" />
      <el-table-column prop="hazardFactor" label="接触危害因素" min-width="120" />
      <el-table-column prop="examCount" label="检查次数" width="90" />
      <el-table-column prop="lastExamDate" label="最近检查日期" width="120" />
      <!-- 规格书字段，待后端扩展：一人一档明细 -->
      <el-table-column label="危害接触记录" min-width="120">
        <template #default="{row}">
          <span v-if="canViewFull">{{ row.hazardContactRecord || '—' }}</span>
          <span v-else class="masked">无权限</span>
        </template>
      </el-table-column>
      <el-table-column label="体检报告" width="100">
        <template #default="{row}">
          <el-button v-if="canViewFull" link type="primary" size="small">查看</el-button>
          <span v-else class="masked">无权限</span>
        </template>
      </el-table-column>
      <el-table-column label="培训记录" width="100">
        <template #default="{row}">
          <el-button v-if="canViewFull" link type="primary" size="small">查看</el-button>
          <span v-else class="masked">无权限</span>
        </template>
      </el-table-column>
      <el-table-column label="调岗记录" width="100">
        <template #default="{row}">
          <el-button v-if="canViewFull" link type="primary" size="small">查看</el-button>
          <span v-else class="masked">无权限</span>
        </template>
      </el-table-column>
      <el-table-column label="档案状态" width="90">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="员工姓名">
          <el-input v-model="formData.employeeName" placeholder="请输入员工姓名" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="工号">
          <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="formData.dept" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="formData.post" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="formData.gender" placeholder="请选择性别" style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="formData.age" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="接触危害因素">
          <el-input v-model="formData.hazardFactor" placeholder="请输入接触危害因素" />
        </el-form-item>
        <el-form-item label="检查次数">
          <el-input-number v-model="formData.examCount" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="最近检查日期">
          <el-date-picker v-model="formData.lastExamDate" type="date" value-format="YYYY-MM-DD" placeholder="选择最近检查日期" style="width:100%" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：一人一档 -->
        <el-form-item label="危害接触记录">
          <el-input v-model="formData.hazardContactRecord" type="textarea" :rows="2" placeholder="危害接触史、接触岗位、接触时长" />
        </el-form-item>
        <el-form-item label="体检报告">
          <el-input v-model="formData.examReport" placeholder="体检报告编号或链接" />
        </el-form-item>
        <el-form-item label="培训记录">
          <el-input v-model="formData.trainingRecord" placeholder="职业健康培训记录" />
        </el-form-item>
        <el-form-item label="调岗记录">
          <el-input v-model="formData.transferRecord" placeholder="因健康原因调岗记录" />
        </el-form-item>
        <el-form-item label="档案状态">
          <el-select v-model="formData.status" placeholder="请选择档案状态" style="width:100%">
            <el-option label="正常" value="normal" />
            <el-option label="需关注" value="attention" />
            <el-option label="异常" value="abnormal" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { healthApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增健康管理')
const currentRole = ref('hr_health')
const formData = reactive({
  id: null,
  employeeName: "",
  employeeNo: "",
  dept: "",
  post: "",
  gender: "",
  age: null,
  hazardFactor: "",
  examCount: null,
  lastExamDate: "",
  hazardContactRecord: "",
  examReport: "",
  trainingRecord: "",
  transferRecord: "",
  status: "",
  remark: ""
})

// IAM 隐私权限：仅本人 / HR健康管理员 / 医务岗可查看完整健康数据
const canViewFull = computed(() => ['self', 'hr_health', 'medical'].includes(currentRole.value))
function onRoleChange() { /* 触发重渲染 */ }

function statusText(s) {
  return { normal: '正常', attention: '需关注', abnormal: '异常' }[s] || s
}
function statusTagType(s) {
  return { normal: 'success', attention: 'warning', abnormal: 'danger' }[s] || 'info'
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await healthApi.records.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增健康管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑健康管理'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await healthApi.records.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await healthApi.records.update(formData.id, formData)
      : await healthApi.records.save(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.masked { color: #c0c4cc; }
</style>

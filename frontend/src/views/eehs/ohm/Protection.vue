<template>
  <div class="page-container">
    <div class="page-header">
      <h2>防护管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="facilityName" label="设施/用品名称" min-width="140" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="post" label="适用岗位" width="130" />
      <el-table-column prop="standard" label="配备标准" width="120" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="checkCycle" label="检查周期" width="100" />
      <el-table-column prop="lastCheckDate" label="最近检查日期" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="领用校验" width="90">
        <template #default="{row}">
          <el-tag :type="row.issuanceCheck === 'checked' ? 'success' : 'warning'" size="small">
            {{ row.issuanceCheck === 'checked' ? '已校验' : '未校验' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column label="防护配置状态" width="100">
        <template #default="{row}">
          <el-tag :type="configTagType(row.configStatus)" size="small">{{ configStatusText(row.configStatus) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段：高危害岗位强制防护标记（computed） -->
      <el-table-column label="强制防护" width="90">
        <template #default="{row}">
          <el-tag v-if="isHighHazardPost(row.post)" type="danger" size="small">高危害强制</el-tag>
          <span v-else class="muted">—</span>
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
        <el-form-item label="设施/用品名称">
          <el-input v-model="formData.facilityName" placeholder="请输入设施/用品名称" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="formData.category" placeholder="请选择类别" style="width:100%">
            <el-option label="防护设施" value="防护设施" />
            <el-option label="防护用品" value="防护用品" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用岗位">
          <el-input v-model="formData.post" placeholder="请输入适用岗位（高危害岗位将强制防护）" />
        </el-form-item>
        <el-form-item label="配备标准">
          <el-input v-model="formData.standard" placeholder="请输入配备标准" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="formData.quantity" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="检查周期">
          <el-input v-model="formData.checkCycle" placeholder="请输入检查周期" />
        </el-form-item>
        <el-form-item label="最近检查日期">
          <el-date-picker v-model="formData.lastCheckDate" type="date" value-format="YYYY-MM-DD" placeholder="选择最近检查日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="正常" value="normal" />
            <el-option label="维护中" value="maintenance" />
            <el-option label="已报废" value="scrapped" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="领用校验">
          <el-select v-model="formData.issuanceCheck" placeholder="请选择领用校验状态" style="width:100%">
            <el-option label="已校验" value="checked" />
            <el-option label="未校验" value="unchecked" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="防护配置状态">
          <el-select v-model="formData.configStatus" placeholder="请选择防护配置状态" style="width:100%">
            <el-option label="已配置" value="configured" />
            <el-option label="部分配置" value="partial" />
            <el-option label="未配置" value="unconfigured" />
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
import { ref, reactive, onMounted } from 'vue'
import { healthApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增防护管理')
const formData = reactive({
  id: null,
  facilityName: "",
  category: "",
  post: "",
  standard: "",
  quantity: null,
  checkCycle: "",
  lastCheckDate: "",
  status: "",
  issuanceCheck: "unchecked",
  configStatus: "unconfigured",
  remark: ""
})

// 高危害岗位判定：岗位名称包含高危害类型关键词
const HIGH_HAZARD_TYPES = ['噪音', '粉尘', '化学试剂', '辐射', '高温', '冲压', '焊接', '化学品']
function isHighHazardPost(post) {
  if (!post) return false
  return HIGH_HAZARD_TYPES.some(t => post.includes(t))
}

function statusText(s) {
  return { normal: '正常', maintenance: '维护中', scrapped: '已报废' }[s] || s
}
function statusTagType(s) {
  return { normal: 'success', maintenance: 'warning', scrapped: 'info' }[s] || 'info'
}
function configStatusText(s) {
  return { configured: '已配置', partial: '部分配置', unconfigured: '未配置' }[s] || '未配置'
}
function configTagType(s) {
  return { configured: 'success', partial: 'warning', unconfigured: 'danger' }[s] || 'info'
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
  formData.issuanceCheck = "unchecked"
  formData.configStatus = "unconfigured"
}

async function loadData() {
  loading.value = true
  try {
    const res = await healthApi.protections.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增防护管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑防护管理'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await healthApi.protections.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await healthApi.protections.update(formData.id, formData)
      : await healthApi.protections.save(formData)
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
.muted { color: #999; }
</style>

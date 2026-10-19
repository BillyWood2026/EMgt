<template>
  <div class="page-container">
    <div class="page-header">
      <h2>废弃物管理</h2>
      <div>
        <el-tag type="info" effect="plain" v-if="!isEnvOfficer">当前非环保专员，涉密危废信息已脱敏</el-tag>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="wasteCode" label="废弃物编码" />
      <el-table-column prop="wasteName" label="废弃物名称" />
      <el-table-column prop="category" label="类别" />
      <!-- 涉密危废列，IAM 脱敏提示（v-if 模拟，仅环保专员可见） -->
      <el-table-column label="是否涉密危废" width="120">
        <template #default="{row}">
          <el-tag v-if="row.isConfidential === '是'" type="danger" size="small">涉密危废</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量">
        <template #default="{row}">
          <span v-if="row.isConfidential === '是' && !isEnvOfficer">****</span>
          <span v-else>{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="vendor" label="处置单位">
        <template #default="{row}">
          <span v-if="row.isConfidential === '是' && !isEnvOfficer">****</span>
          <span v-else>{{ row.vendor }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="produceDate" label="产生日期" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="废弃物编码">
          <el-input v-model="formData.wasteCode" placeholder="请输入废弃物编码" />
        </el-form-item>
        <el-form-item label="废弃物名称">
          <el-input v-model="formData.wasteName" placeholder="请输入废弃物名称" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="formData.category" placeholder="请选择类别" style="width:100%">
            <el-option label="危险废物" value="危险废物" />
            <el-option label="一般固废" value="一般固废" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否涉密危废"><!-- 规格书字段，待后端扩展 -->
          <el-select v-model="formData.isConfidential" placeholder="请选择" style="width:100%">
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="formData.quantity" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="处置方式">
          <el-input v-model="formData.disposeMethod" placeholder="请输入处置方式" />
        </el-form-item>
        <el-form-item label="处置单位">
          <el-input v-model="formData.vendor" placeholder="请输入处置单位" />
        </el-form-item>
        <el-form-item label="产生日期">
          <el-date-picker v-model="formData.produceDate" type="date" value-format="YYYY-MM-DD" placeholder="选择产生日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="暂存" value="stored" />
            <el-option label="已处置" value="disposed" />
          </el-select>
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
import { envApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增废弃物管理')
const formData = reactive({
  id: null,
  wasteCode: "",
  wasteName: "",
  category: "",
  isConfidential: "",
  quantity: null,
  unit: "",
  disposeMethod: "",
  vendor: "",
  produceDate: "",
  status: ""
})

// IAM 模拟：当前用户角色是否为环保专员（仅环保专员+保密专员可查看涉密危废完整台账）
const userRole = ref(localStorage.getItem('role') || 'ADMIN')
const isEnvOfficer = computed(() => ['ADMIN', 'ENV_OFFICER', 'SECRECY_OFFICER'].includes(userRole.value))

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await envApi.wastes.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增废弃物管理'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑废弃物管理'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await envApi.wastes.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await envApi.wastes.update(formData.id, formData)
      : await envApi.wastes.save(formData)
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
</style>

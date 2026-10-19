<template>
  <div class="page-container">
    <div class="page-header">
      <h2>环境监测</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="monitorPoint" label="监测点位" />
      <el-table-column prop="pollutant" label="污染物因子" />
      <el-table-column prop="monitorValue" label="监测值" />
      <el-table-column prop="standardValue" label="国标阈值" />
      <el-table-column prop="unit" label="单位" />
      <el-table-column prop="monitorTime" label="监测时间" />
      <el-table-column label="超标状态" width="100">
        <template #default="{row}">
          <el-tag :type="isExceeded(row) ? 'danger' : 'success'" size="small">
            {{ isExceeded(row) ? '超标' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警级别" width="100">
        <template #default="{row}">
          <el-tag :type="warningType(warningLevel(row))" size="small" v-if="isExceeded(row)">{{ warningLevel(row) }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="rectifyOwner" label="整改责任人" />
      <el-table-column prop="monitorType" label="监测类型"><!-- 规格书字段，待后端扩展 --></el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="监测点位">
          <el-input v-model="formData.monitorPoint" placeholder="请输入监测点位" />
        </el-form-item>
        <el-form-item label="污染物因子">
          <el-input v-model="formData.pollutant" placeholder="请输入污染物因子" />
        </el-form-item>
        <el-form-item label="监测值">
          <el-input-number v-model="formData.monitorValue" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="国标阈值">
          <el-input-number v-model="formData.standardValue" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="formData.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="监测时间">
          <el-date-picker v-model="formData.monitorTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择监测时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="整改责任人">
          <el-input v-model="formData.rectifyOwner" placeholder="请输入整改责任人" /><!-- 规格书字段，待后端扩展 -->
        </el-form-item>
        <el-form-item label="监测类型">
          <el-input v-model="formData.monitorType" placeholder="如：废水/废气/噪音/粉尘" /><!-- 规格书字段，待后端扩展 -->
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="正常" value="normal" />
            <el-option label="超标" value="exceeded" />
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
import { envApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增环境监测')
const formData = reactive({
  id: null,
  monitorPoint: "",
  pollutant: "",
  monitorValue: null,
  standardValue: null,
  unit: "",
  monitorTime: "",
  status: "",
  remark: "",
  rectifyOwner: "",
  monitorType: ""
})

// 算法：监测值 > 国标阈值 判定为超标
function isExceeded(row) {
  return Number(row.monitorValue) > Number(row.standardValue)
}

// 算法：预警级别 = 差值/阈值 分级（<10% 轻微，10%-30% 一般，>30% 严重）
function warningLevel(row) {
  const v = Number(row.monitorValue)
  const s = Number(row.standardValue)
  if (!s || v <= s) return ''
  const ratio = (v - s) / s
  if (ratio < 0.1) return '轻微'
  if (ratio < 0.3) return '一般'
  return '严重'
}

function warningType(level) {
  if (level === '严重') return 'danger'
  if (level === '一般') return 'warning'
  if (level === '轻微') return 'info'
  return 'success'
}

// 全表超标次数统计（computed 演示算法联动）
const exceedCount = computed(() => tableData.value.filter(r => isExceeded(r)).length)

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await envApi.monitors.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增环境监测'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑环境监测'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await envApi.monitors.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await envApi.monitors.update(formData.id, formData)
      : await envApi.monitors.save(formData)
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

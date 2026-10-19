<template>
  <div class="page-container">
    <div class="page-header">
      <h2>能源分析</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="analysisPeriod" label="分析周期" width="110" />
      <el-table-column prop="energyType" label="能源类型" width="90" />
      <el-table-column prop="consumption" label="消耗量" width="100" />
      <el-table-column label="同比%" width="90">
        <template #default="{row}">
          <span :class="rateClass(row.yoyRate)">{{ row.yoyRate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="环比%" width="90">
        <template #default="{row}">
          <span :class="rateClass(row.momRate)">{{ row.momRate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="能耗趋势" width="90">
        <template #default="{row}">
          <el-tag :type="trendTagType(row)" size="small">{{ trendText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unitOutputConsumption" label="单位产值能耗" width="110" />
      <el-table-column prop="targetValue" label="目标值" width="90" />
      <!-- 规格书字段：能效对标（computed） -->
      <el-table-column label="能效对标" width="90">
        <template #default="{row}">
          <el-tag :type="benchmarkTagType(row)" size="small">{{ benchmarkText(row) }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展：峰谷能耗分析 -->
      <el-table-column label="峰谷分析" min-width="120">
        <template #default="{row}">{{ row.peakValleyAnalysis || '—' }}</template>
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
        <el-form-item label="分析周期">
          <el-input v-model="formData.analysisPeriod" placeholder="请输入分析周期" />
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="formData.energyType" placeholder="请选择能源类型" style="width:100%">
            <el-option label="水" value="水" />
            <el-option label="电" value="电" />
            <el-option label="气" value="气" />
            <el-option label="暖" value="暖" />
            <el-option label="压缩空气" value="压缩空气" />
          </el-select>
        </el-form-item>
        <el-form-item label="消耗量">
          <el-input-number v-model="formData.consumption" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="同比(%)">
          <el-input-number v-model="formData.yoyRate" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="环比(%)">
          <el-input-number v-model="formData.momRate" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="单位产值能耗">
          <el-input-number v-model="formData.unitOutputConsumption" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="目标值">
          <el-input-number v-model="formData.targetValue" controls-position="right" style="width:100%" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="峰谷能耗分析">
          <el-input v-model="formData.peakValleyAnalysis" type="textarea" :rows="2" placeholder="峰段/谷段能耗占比分析" />
        </el-form-item>
        <el-form-item label="分析结论">
          <el-input v-model="formData.conclusion" type="textarea" :rows="3" placeholder="请输入分析结论" />
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
import { energyApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增能源分析')
const formData = reactive({
  id: null,
  analysisPeriod: "",
  energyType: "",
  consumption: null,
  yoyRate: null,
  momRate: null,
  unitOutputConsumption: null,
  targetValue: null,
  peakValleyAnalysis: "",
  conclusion: ""
})

// 能耗趋势算法：同比>0 且 环比>0 → 上升（红）；同比<0 且 环比<0 → 下降（绿）；否则 波动（橙）
function trendText(row) {
  const yoy = Number(row.yoyRate)
  const mom = Number(row.momRate)
  if (yoy > 0 && mom > 0) return '上升'
  if (yoy < 0 && mom < 0) return '下降'
  return '波动'
}
function trendTagType(row) {
  const t = trendText(row)
  return { 上升: 'danger', 下降: 'success', 波动: 'warning' }[t] || 'info'
}

// 能效对标：消耗量 ≤ 目标值 → 达标（绿）；>目标值 → 超标（红）
function benchmarkText(row) {
  const tv = Number(row.targetValue)
  if (!tv) return '未对标'
  return Number(row.consumption) <= tv ? '达标' : '超标'
}
function benchmarkTagType(row) {
  const t = benchmarkText(row)
  return { 达标: 'success', 超标: 'danger', 未对标: 'info' }[t] || 'info'
}

// 同比/环比着色：正值红，负值绿
function rateClass(rate) {
  const r = Number(rate)
  if (r > 0) return 'text-danger'
  if (r < 0) return 'text-success'
  return ''
}

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await energyApi.analyses.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增能源分析'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑能源分析'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await energyApi.analyses.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await energyApi.analyses.update(formData.id, formData)
      : await energyApi.analyses.save(formData)
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
.text-danger { color: #f5222d; font-weight: bold; }
.text-success { color: #52c41a; }
</style>

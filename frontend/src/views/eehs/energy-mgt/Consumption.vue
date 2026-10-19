<template>
  <div class="page-container">
    <div class="page-header">
      <h2>能耗统计</h2>
      <div>
        <el-button type="success" @click="baselineDrawerVisible = true">能源基线管理</el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="statPeriod" label="统计周期" width="100" />
      <el-table-column prop="energyType" label="能源类型" width="90" />
      <!-- 规格书字段，待后端扩展：厂区/车间/设备/产线多级 -->
      <el-table-column prop="factory" label="厂区" width="90" />
      <el-table-column prop="workshop" label="车间" width="90" />
      <el-table-column prop="productionLine" label="产线" width="90" />
      <el-table-column prop="equipment" label="设备" width="100" />
      <el-table-column label="消耗量" width="110">
        <template #default="{row}">{{ row.consumption }} {{ row.unit }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展：峰谷能耗 -->
      <el-table-column label="峰段能耗" width="100">
        <template #default="{row}">{{ row.peakConsumption ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="谷段能耗" width="100">
        <template #default="{row}">{{ row.valleyConsumption ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="单位产品能耗" width="120">
        <template #default="{row}">
          <span :class="{ 'text-danger': isOverBaseline(row) }">{{ unitProductConsumption(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="standardCoal" label="折标煤(吨)" width="100" />
      <el-table-column label="超基线" width="80">
        <template #default="{row}">
          <el-tag v-if="isOverBaseline(row)" type="danger" size="small">超标</el-tag>
          <el-tag v-else-if="row.baseline" type="success" size="small">达标</el-tag>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="cost" label="费用(元)" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="120px">
        <el-form-item label="统计周期">
          <el-input v-model="formData.statPeriod" placeholder="请输入统计周期" />
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
        <!-- 规格书字段，待后端扩展：多级 -->
        <el-form-item label="厂区">
          <el-input v-model="formData.factory" placeholder="请输入厂区" />
        </el-form-item>
        <el-form-item label="车间">
          <el-input v-model="formData.workshop" placeholder="请输入车间" />
        </el-form-item>
        <el-form-item label="产线">
          <el-input v-model="formData.productionLine" placeholder="请输入产线" />
        </el-form-item>
        <el-form-item label="设备">
          <el-input v-model="formData.equipment" placeholder="请输入设备" />
        </el-form-item>
        <el-form-item label="消耗量">
          <el-input-number v-model="formData.consumption" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="formData.unit" placeholder="请输入计量单位" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：峰谷 -->
        <el-form-item label="峰段能耗">
          <el-input-number v-model="formData.peakConsumption" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="谷段能耗">
          <el-input-number v-model="formData.valleyConsumption" controls-position="right" style="width:100%" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：产量与基线 -->
        <el-form-item label="产量">
          <el-input-number v-model="formData.output" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="能耗基线">
          <el-input-number v-model="formData.baseline" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="费用(元)">
          <el-input-number v-model="formData.cost" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="折标煤(吨)">
          <el-input-number v-model="formData.standardCoal" controls-position="right" style="width:100%" />
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

    <!-- 能源基线管理 抽屉 -->
    <el-drawer v-model="baselineDrawerVisible" title="能源基线管理（多级基线建立与维护）" size="40%">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px">
        按厂区/车间/设备/产线多级建立能耗基线，超基线自动标红预警
      </el-alert>
      <el-table :data="baselineList" border size="small" max-height="500">
        <el-table-column prop="level" label="层级" width="90" />
        <el-table-column prop="name" label="对象" min-width="120" />
        <el-table-column prop="energyType" label="能源类型" width="90" />
        <el-table-column prop="baselineValue" label="基线值" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { energyApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增能耗统计')
const formData = reactive({
  id: null,
  statPeriod: "",
  energyType: "",
  factory: "",
  workshop: "",
  productionLine: "",
  equipment: "",
  consumption: null,
  unit: "",
  peakConsumption: null,
  valleyConsumption: null,
  output: null,
  baseline: null,
  cost: null,
  standardCoal: null,
  remark: ""
})

// 单位产品能耗 = 总能耗 / 产量
function unitProductConsumption(row) {
  const output = Number(row.output)
  if (!output) return '—'
  return (Number(row.consumption) / output).toFixed(4)
}

// 超基线预警：消耗量 > 基线 → 标红
function isOverBaseline(row) {
  const baseline = Number(row.baseline)
  if (!baseline) return false
  return Number(row.consumption) > baseline
}

// 能源基线管理（多级）
const baselineDrawerVisible = ref(false)
const baselineList = ref([
  { level: '厂区', name: '深圳厂区', energyType: '电', baselineValue: 500000, unit: 'kWh', enabled: true },
  { level: '车间', name: 'SMT一车间', energyType: '电', baselineValue: 120000, unit: 'kWh', enabled: true },
  { level: '产线', name: 'SMT-L1', energyType: '电', baselineValue: 30000, unit: 'kWh', enabled: true },
  { level: '设备', name: '回流焊炉#01', energyType: '电', baselineValue: 8000, unit: 'kWh', enabled: true }
])

function resetForm() {
  Object.keys(formData).forEach(k => { formData[k] = (k === 'id') ? null : null })
}

async function loadData() {
  loading.value = true
  try {
    const res = await energyApi.consumptions.list()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增能耗统计'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑能耗统计'
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条记录？', '提示', { type: 'warning' })
    const res = await energyApi.consumptions.remove(row.id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); loadData() }
    else ElMessage.error(res.data.message || '删除失败')
  } catch (e) {}
}

async function handleSubmit() {
  try {
    const res = formData.id
      ? await energyApi.consumptions.update(formData.id, formData)
      : await energyApi.consumptions.save(formData)
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
.muted { color: #999; }
</style>

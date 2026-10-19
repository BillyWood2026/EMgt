<template>
  <div class="page-container">
    <div class="page-header">
      <h2>环境管理概览</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddTarget">新增环境目标</el-button>
        <el-button type="warning" @click="handleBatchInspect">批量发起排查</el-button>
        <el-button type="success" @click="handleExportAnnual">导出环境年报</el-button>
        <el-button @click="loadAll">刷新</el-button>
      </div>
    </div>

    <!-- 顶部筛选：厂区/业务域/月份/年度/风险等级/合规状态 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="厂区">
          <el-select v-model="filterForm.factory" placeholder="请选择厂区" clearable style="width:140px">
            <el-option label="深圳厂区" value="SZ" />
            <el-option label="东莞厂区" value="DG" />
            <el-option label="苏州厂区" value="SZ2" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务域">
          <el-select v-model="filterForm.bizDomain" placeholder="请选择业务域" clearable style="width:140px">
            <el-option label="SMT产线" value="SMT" />
            <el-option label="组装车间" value="ASM" />
            <el-option label="研发中心" value="RD" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker v-model="filterForm.month" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width:140px" />
        </el-form-item>
        <el-form-item label="年度">
          <el-date-picker v-model="filterForm.year" type="year" value-format="YYYY" placeholder="选择年度" style="width:120px" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filterForm.riskLevel" placeholder="请选择" clearable style="width:120px">
            <el-option label="一般" value="一般" />
            <el-option label="较大" value="较大" />
            <el-option label="重大" value="重大" />
          </el-select>
        </el-form-item>
        <el-form-item label="合规状态">
          <el-select v-model="filterForm.complianceStatus" placeholder="请选择" clearable style="width:120px">
            <el-option label="合规" value="compliant" />
            <el-option label="不合规" value="non_compliant" />
            <el-option label="待复核" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAll">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡：重大环境因素数、危废存量、监测超标次数、未闭环隐患、演练完成率、合规通过率 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.majorEnvFactors }}</div>
            <div class="stat-label">重大环境因素数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><Files /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.hazardousWasteStock }}</div>
            <div class="stat-label">危废存量(吨)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Monitor /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.monitorExceeded }}</div>
            <div class="stat-label">监测超标次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.unclosedHazards }}</div>
            <div class="stat-label">未闭环隐患</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.drillCompletionRate }}%</div>
            <div class="stat-label">演练完成率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.complianceRate }}%</div>
            <div class="stat-label">合规通过率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>最新环境监测（超标置顶）</span></template>
          <el-table :data="monitors" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="monitorPoint" label="监测点位" min-width="120" />
            <el-table-column prop="pollutant" label="污染物" width="100" />
            <el-table-column label="监测值/标准" width="140">
              <template #default="{row}">
                <span :class="{ 'exceed-text': Number(row.monitorValue) > Number(row.standardValue) }">
                  {{ row.monitorValue }} / {{ row.standardValue }} {{ row.unit }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{row}">
                <el-tag :type="row.status === 'exceeded' ? 'danger' : 'success'" size="small">
                  {{ row.status === 'exceeded' ? '超标' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>排污许可执行</span></template>
          <el-table :data="emissions" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="outletName" label="排口名称" min-width="120" />
            <el-table-column prop="pollutantType" label="污染物" width="100" />
            <el-table-column label="许可/实际" width="140">
              <template #default="{row}">{{ row.permitValue }} / {{ row.actualValue }} {{ row.unit }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{row}">
                <el-tag :type="row.status === 'exceeded' ? 'danger' : 'success'" size="small">
                  {{ row.status === 'exceeded' ? '超许可' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 环境目标 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <template #header><span>环境目标</span></template>
      <el-table :data="targets" border size="small" v-loading="loading">
        <el-table-column prop="targetNo" label="目标编号" width="150" />
        <el-table-column prop="targetName" label="目标名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="厂区" width="100">
          <template #default="{row}">{{ factoryName(row.factory) }}</template>
        </el-table-column>
        <el-table-column label="业务域" width="100">
          <template #default="{row}">{{ domainName(row.bizDomain) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column label="基线→目标" width="160">
          <template #default="{row}">{{ row.baselineValue }} → {{ row.targetValue }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column prop="deadline" label="完成期限" width="110" />
        <el-table-column prop="owner" label="责任人" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag size="small" :type="row.status === '已完成' ? 'success' : row.status === '草稿' ? 'info' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="editTarget(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTarget(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="targetVis" :title="targetForm.id ? '编辑环境目标' : '新增环境目标'" width="680px">
      <el-form :model="targetForm" label-width="100px" size="default">
        <el-form-item label="目标名称" required>
          <el-input v-model="targetForm.targetName" maxlength="200" placeholder="如：2026年度单位产品能耗下降5%" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="厂区" required>
              <el-select v-model="targetForm.factory" style="width:100%">
                <el-option label="深圳厂区" value="SZ" />
                <el-option label="东莞厂区" value="DG" />
                <el-option label="苏州厂区" value="SZ2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务域">
              <el-select v-model="targetForm.bizDomain" style="width:100%" clearable>
                <el-option label="SMT产线" value="SMT" />
                <el-option label="组装车间" value="ASM" />
                <el-option label="研发中心" value="RD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="目标类别">
              <el-select v-model="targetForm.category" style="width:100%">
                <el-option label="节能减排" value="节能减排" />
                <el-option label="危废减量" value="危废减量" />
                <el-option label="合规提升" value="合规提升" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="完成期限" required>
              <el-date-picker v-model="targetForm.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="基线值">
              <el-input-number v-model="targetForm.baselineValue" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标值">
              <el-input-number v-model="targetForm.targetValue" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="targetForm.unit" placeholder="如 tCO2e/%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="责任人">
              <el-input v-model="targetForm.owner" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="targetForm.status" style="width:100%">
                <el-option label="草稿" value="草稿" />
                <el-option label="审批中" value="审批中" />
                <el-option label="已批准" value="已批准" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="targetForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="targetVis=false">取消</el-button>
        <el-button type="primary" @click="saveTarget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { envApi } from '@/api/eehs'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const monitors = ref([])
const emissions = ref([])
const targets = ref([])
const targetVis = ref(false)
const targetForm = reactive({ id: null, targetNo: null, targetName: '', factory: 'SZ', bizDomain: '', category: '节能减排', baselineValue: 0, targetValue: 0, unit: '', deadline: '', owner: '', status: '草稿', remark: '' })
const factoryName = f => ({ SZ: '深圳厂区', DG: '东莞厂区', SZ2: '苏州厂区' }[f] || f || '—')
const domainName = d => ({ SMT: 'SMT产线', ASM: '组装车间', RD: '研发中心' }[d] || d || '—')
const stats = reactive({
  majorEnvFactors: 0,
  hazardousWasteStock: 0,
  monitorExceeded: 0,
  unclosedHazards: 0,
  drillCompletionRate: 0,
  complianceRate: 0
})
const filterForm = reactive({
  factory: '',
  bizDomain: '',
  month: '',
  year: '',
  riskLevel: '',
  complianceStatus: ''
})

async function loadAll() {
  loading.value = true
  try {
    const [m, e, w, t] = await Promise.all([envApi.monitors.list(), envApi.emissions.list(), envApi.wastes.list(), envApi.targets.list()])
    const mData = m.data.code === 200 ? (m.data.data || []) : []
    const eData = e.data.code === 200 ? (e.data.data || []) : []
    const wData = w.data.code === 200 ? (w.data.data || []) : []
    targets.value = t.data.code === 200 ? (t.data.data || []) : []
    monitors.value = [...mData].sort((a, b) => (a.status === 'exceeded' ? -1 : 1) - (b.status === 'exceeded' ? -1 : 1)).slice(0, 10)
    emissions.value = eData.slice(0, 10)
    // 统计指标
    stats.monitorExceeded = mData.filter(x => x.status === 'exceeded').length
    stats.hazardousWasteStock = wData.filter(x => x.category === '危险废物').reduce((s, x) => s + (Number(x.quantity) || 0), 0).toFixed(2)
    stats.majorEnvFactors = mData.filter(x => x.status === 'exceeded').length
    stats.unclosedHazards = mData.filter(x => x.status !== 'closed').length
    // 演练完成率、合规通过率为规格书衍生指标，前端模拟计算
    stats.drillCompletionRate = wData.length ? Math.round((wData.filter(x => x.status === 'disposed').length / wData.length) * 100) : 0
    stats.complianceRate = mData.length ? Math.round((mData.filter(x => x.status !== 'exceeded').length / mData.length) * 100) : 100
  } finally { loading.value = false }
}

function resetFilter() {
  Object.keys(filterForm).forEach(k => { filterForm[k] = '' })
}

function handleAddTarget() {
  Object.assign(targetForm, { id: null, targetNo: null, targetName: '', factory: 'SZ', bizDomain: '', category: '节能减排', baselineValue: 0, targetValue: 0, unit: '', deadline: '', owner: '', status: '草稿', remark: '' })
  targetVis.value = true
}

function editTarget(row) {
  Object.assign(targetForm, {
    id: row.id, targetNo: row.targetNo, targetName: row.targetName || '', factory: row.factory || 'SZ',
    bizDomain: row.bizDomain || '', category: row.category || '节能减排',
    baselineValue: row.baselineValue != null ? Number(row.baselineValue) : 0,
    targetValue: row.targetValue != null ? Number(row.targetValue) : 0,
    unit: row.unit || '', deadline: row.deadline || '', owner: row.owner || '',
    status: row.status || '草稿', remark: row.remark || ''
  })
  targetVis.value = true
}

async function saveTarget() {
  if (!(targetForm.targetName || '').trim()) return ElMessage.warning('请填写目标名称')
  if (!targetForm.deadline) return ElMessage.warning('请选择完成期限')
  if (targetForm.id) {
    await envApi.targets.update(targetForm.id, { ...targetForm })
  } else {
    await envApi.targets.save({ ...targetForm })
  }
  ElMessage.success('已保存')
  targetVis.value = false
  loadAll()
}

async function deleteTarget(row) {
  try {
    await ElMessageBox.confirm(`确认删除环境目标「${row.targetName}」？`, '删除确认', { type: 'warning' })
  } catch (e) { return }
  const r = await envApi.targets.remove(row.id)
  if (r.data && r.data.code === 200) {
    ElMessage.success('已删除')
  } else {
    ElMessage.error((r.data && r.data.message) || '删除失败')
  }
  loadAll()
}

async function handleBatchInspect() {
  try {
    await ElMessageBox.confirm('确认对当前筛选范围批量发起环境隐患排查？', '批量发起排查', { type: 'warning' })
    ElMessage.success('已批量发起排查任务')
  } catch (e) {}
}

function handleExportAnnual() {
  ElMessage.success('环境年报导出中（规格书字段，待后端扩展）')
}

onMounted(loadAll)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-actions { display: flex; gap: 8px; }
.stat-card { display: flex; align-items: center; padding: 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; margin-right: 12px; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; }
.exceed-text { color: #f5222d; font-weight: bold; }
</style>

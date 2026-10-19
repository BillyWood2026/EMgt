<template>
  <div class="page-container">
    <div class="page-header">
      <h2>执行跟踪</h2>
      <el-button type="primary" @click="handleAdd">新增填报</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="偏差 = 实际进度 − 计划进度（后端自动计算）；预警分级：偏差≥0 蓝（正常），-10%~0 黄（关注），-20%~-10% 橙（预警），<-20% 红（严重滞后，需填写风险与整改措施）。" />

    <div class="filter-bar">
      <el-input-number v-model="queryPlanId" :min="0" controls-position="right" placeholder="按计划ID筛选" style="width:160px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="queryPlanId=null;loadData()">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="执行跟踪" title="执行跟踪导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="planId" label="计划ID" width="90" />
      <el-table-column prop="taskId" label="任务ID" width="90" />
      <el-table-column prop="trackPeriod" label="跟踪周期" width="110" />
      <el-table-column prop="plannedProgress" label="计划进度%" width="100" />
      <el-table-column prop="actualProgress" label="实际进度%" width="100" />
      <el-table-column prop="deviation" label="偏差" width="90">
        <template #default="{row}">
          <span :style="{color: devColor(row.deviation), fontWeight: 'bold'}">{{ row.deviation ?? 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="预警等级" width="95">
        <template #default="{row}">
          <el-tag :type="warnLevel(row.deviation).type" size="small">{{ warnLevel(row.deviation).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="completionDesc" label="完成说明" min-width="150" show-overflow-tooltip />
      <el-table-column prop="riskDesc" label="风险说明" min-width="130" show-overflow-tooltip />
      <el-table-column prop="rectifyMeasure" label="整改措施" min-width="130" show-overflow-tooltip />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="blocker" label="当前卡点" width="110" show-overflow-tooltip>
        <template #default="{row}">{{ row.blocker || '—' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="resourceSupport" label="资源支撑" width="110" show-overflow-tooltip>
        <template #default="{row}">{{ row.resourceSupport || '—' }}</template>
      </el-table-column>
      <el-table-column prop="reporter" label="填报人" width="90" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="formData" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计划ID" required>
              <el-input-number v-model="formData.planId" :min="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务ID">
              <el-input-number v-model="formData.taskId" :min="0" controls-position="right" style="width:100%" placeholder="关联WBS任务" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="跟踪周期" required>
          <el-input v-model="formData.trackPeriod" placeholder="如 2026-01 / 2026-W05" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="计划进度%">
              <el-input-number v-model="formData.plannedProgress" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际进度%">
              <el-input-number v-model="formData.actualProgress" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="偏差预览">
              <el-tag :type="warnLevel(previewDeviation).type" style="width:100%;text-align:center">
                {{ previewDeviation }}（{{ warnLevel(previewDeviation).label }}）
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="完成说明">
          <el-input v-model="formData.completionDesc" type="textarea" :rows="2" placeholder="请输入完成说明" />
        </el-form-item>
        <el-form-item label="风险说明">
          <el-input v-model="formData.riskDesc" type="textarea" :rows="2" placeholder="偏差<-10%时建议填写" />
        </el-form-item>
        <el-form-item label="整改措施">
          <el-input v-model="formData.rectifyMeasure" type="textarea" :rows="2" placeholder="偏差<-20%时必填" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="当前卡点">
          <el-input v-model="formData.blocker" placeholder="影响进度的关键卡点" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="资源支撑">
          <el-input v-model="formData.resourceSupport" placeholder="需要的资源/协调支撑" />
        </el-form-item>
        <el-form-item label="填报人">
          <el-input v-model="formData.reporter" placeholder="请输入填报人" />
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
import { workplanApi } from '@/api/strategy'
import { ElMessage } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const tableData = ref([])
const queryPlanId = ref(null)

const ioColumns = [
  { key: 'planId', title: '计划ID' },
  { key: 'taskId', title: '任务ID' },
  { key: 'trackPeriod', title: '跟踪周期' },
  { key: 'plannedProgress', title: '计划进度%' },
  { key: 'actualProgress', title: '实际进度%' },
  { key: 'deviation', title: '偏差' },
  { key: 'completionDesc', title: '完成说明' },
  { key: 'riskDesc', title: '风险说明' },
  { key: 'rectifyMeasure', title: '整改措施' },
  { key: 'reporter', title: '填报人' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增执行跟踪')
const formData = reactive({
  id: null, planId: null, taskId: null, trackPeriod: '', plannedProgress: null, actualProgress: null,
  completionDesc: '', riskDesc: '', rectifyMeasure: '', blocker: '', resourceSupport: '', reporter: ''
})

const previewDeviation = computed(() => (Number(formData.actualProgress) || 0) - (Number(formData.plannedProgress) || 0))

function devColor(d) {
  const v = Number(d) || 0
  if (v >= 0) return '#67c23a'
  if (v >= -10) return '#e6a23c'
  if (v >= -20) return '#ff7d00'
  return '#f56c6c'
}
function warnLevel(d) {
  const v = Number(d) || 0
  if (v >= 0) return { type: 'primary', label: '正常' }
  if (v >= -10) return { type: 'warning', label: '关注' }
  if (v >= -20) return { type: 'warning', label: '预警' }
  return { type: 'danger', label: '严重滞后' }
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listTracks(queryPlanId.value || undefined)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增执行跟踪'
  Object.assign(formData, { id: null, planId: null, taskId: null, trackPeriod: '', plannedProgress: null, actualProgress: null, completionDesc: '', riskDesc: '', rectifyMeasure: '', blocker: '', resourceSupport: '', reporter: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑执行跟踪'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formData.planId) { ElMessage.warning('请填写计划ID'); return }
  if (!formData.trackPeriod) { ElMessage.warning('请填写跟踪周期'); return }
  if (previewDeviation.value < -20 && !formData.rectifyMeasure) { ElMessage.warning('偏差小于-20%，整改措施必填'); return }
  try {
    const res = await workplanApi.saveTrack(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else ElMessage.error(res.data.message || '保存失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { display: flex; gap: 10px; margin-top: 14px; align-items: center; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>考核打分</h2>
      <el-button type="primary" @click="handleAdd">新增打分</el-button>
    </div>

    <el-alert type="info" :closable="false" style="margin-top:12px"
      title="考核算法：总分 = 基础分（按完成率自动折算）× 70% + 人工评分 × 30%；逾期任务按模板扣分标准扣分。打分后可在下方实时查询计划总分。" />

    <div class="filter-bar">
      <el-input-number v-model="queryPlanId" :min="0" controls-position="right" placeholder="按计划ID筛选" style="width:160px" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="warning" :disabled="!queryPlanId" @click="showTotalScore">计划总分</el-button>
      <el-button @click="queryPlanId=null;loadData()">重置</el-button>
    </div>

    <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="考核打分" title="考核打分导入导出" style="margin-top:12px" @done="loadData" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="planId" label="计划ID" width="90" />
      <el-table-column prop="taskId" label="任务ID" width="90" />
      <el-table-column prop="scoreItem" label="评分项" width="150" show-overflow-tooltip />
      <el-table-column prop="scoreWeight" label="权重%" width="90" />
      <el-table-column prop="scoreValue" label="得分" width="90">
        <template #default="{row}">
          <span :style="{color: Number(row.scoreValue) >= 80 ? '#67c23a' : Number(row.scoreValue) >= 60 ? '#e6a23c' : '#f56c6c', fontWeight:'bold'}">{{ row.scoreValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="加权分" width="90">
        <template #default="{row}">{{ weighted(row) }}</template>
      </el-table-column>
      <el-table-column prop="scorer" label="评分人" width="100" />
      <el-table-column prop="scoreOpinion" label="评分意见" min-width="160" show-overflow-tooltip />
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="deductDetail" label="扣分明细" width="120" show-overflow-tooltip>
        <template #default="{row}">{{ row.deductDetail || '—' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="adjustReason" label="调整原因" width="120" show-overflow-tooltip>
        <template #default="{row}">{{ row.adjustReason || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
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
        <el-form-item label="评分项" required>
          <el-input v-model="formData.scoreItem" placeholder="如 完成质量/及时性/协同配合" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="权重%">
              <el-input-number v-model="formData.scoreWeight" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="得分">
              <el-input-number v-model="formData.scoreValue" :min="0" :max="100" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="加权预览">
          <el-tag type="info" style="width:100%;text-align:center">
            加权分 = {{ formData.scoreValue || 0 }} × {{ formData.scoreWeight || 0 }}% = {{ weightedPreview }}
          </el-tag>
        </el-form-item>
        <el-form-item label="评分人">
          <el-input v-model="formData.scorer" placeholder="请输入评分人" />
        </el-form-item>
        <el-form-item label="评分意见">
          <el-input v-model="formData.scoreOpinion" type="textarea" :rows="2" placeholder="得分低于60分时建议填写" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="扣分明细">
          <el-input v-model="formData.deductDetail" placeholder="逾期/质量扣分明细" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="调整原因">
          <el-input v-model="formData.adjustReason" placeholder="人工调整分数时必填" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const tableData = ref([])
const queryPlanId = ref(null)

const ioColumns = [
  { key: 'planId', title: '计划ID' },
  { key: 'taskId', title: '任务ID' },
  { key: 'scoreItem', title: '评分项' },
  { key: 'scoreWeight', title: '权重%' },
  { key: 'scoreValue', title: '得分' },
  { key: 'scorer', title: '评分人' },
  { key: 'scoreOpinion', title: '评分意见' },
  { key: 'deductDetail', title: '扣分明细' },
  { key: 'adjustReason', title: '调整原因' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('新增考核打分')
const formData = reactive({
  id: null, planId: null, taskId: null, scoreItem: '', scoreWeight: null, scoreValue: null,
  scorer: '', scoreOpinion: '', deductDetail: '', adjustReason: ''
})

const weightedPreview = computed(() => ((Number(formData.scoreValue) || 0) * (Number(formData.scoreWeight) || 0) / 100).toFixed(2))

function weighted(row) {
  return ((Number(row.scoreValue) || 0) * (Number(row.scoreWeight) || 0) / 100).toFixed(2)
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listScores(queryPlanId.value || undefined)
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增考核打分'
  Object.assign(formData, { id: null, planId: null, taskId: null, scoreItem: '', scoreWeight: null, scoreValue: null, scorer: '', scoreOpinion: '', deductDetail: '', adjustReason: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑考核打分'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function showTotalScore() {
  try {
    const res = await workplanApi.totalScore(queryPlanId.value)
    if (res.data.code === 200) {
      const d = res.data.data
      const score = (d && (d.totalScore ?? d.score ?? d)) ?? 0
      ElMessageBox.alert(`计划 #${queryPlanId.value} 考核总分：${score}（基础分×70% + 人工评分×30%）`, '计划总分', { confirmButtonText: '知道了' })
    } else ElMessage.error(res.data.message || '查询失败')
  } catch (e) { ElMessage.error(e.response?.data?.message || '查询失败') }
}

async function handleSubmit() {
  if (!formData.planId) { ElMessage.warning('请填写计划ID'); return }
  if (!formData.scoreItem) { ElMessage.warning('请填写评分项'); return }
  if (formData.scoreValue != null && formData.scoreValue < 60 && !formData.scoreOpinion) {
    ElMessage.warning('得分低于60分，评分意见必填')
    return
  }
  try {
    const res = await workplanApi.saveScore(formData)
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

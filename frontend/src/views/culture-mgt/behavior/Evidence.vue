<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">个人行为举证：记录正向/反向行为，部门初审+文化复核后入账积分</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" class="mt">
      <el-tab-pane label="我的举证" name="mine">
        <div class="bar">
          <el-button size="small" type="primary" @click="openCreate">新建举证</el-button>
          <el-button size="small" @click="loadMine">刷新</el-button>
        </div>
        <el-table :data="mineRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="evidenceNo" label="编号" width="150" />
          <el-table-column prop="valueDimension" label="价值观维度" width="100" align="center" />
          <el-table-column prop="behaviorType" label="类型" width="70" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.behaviorType === '正向' ? 'success' : 'danger'">{{ row.behaviorType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scene" label="场景" width="120" show-overflow-tooltip />
          <el-table-column prop="deedDesc" label="事迹描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="selfScore" label="自评分" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '草稿' || row.status === '已驳回'" size="small" link @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === '草稿' || row.status === '已驳回'" size="small" type="primary" link @click="doSubmit(row)">提交</el-button>
              <el-button size="small" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="can('C04','C99')" label="部门初审" name="first">
        <div class="bar">
          <el-button size="small" @click="loadTodo">刷新</el-button>
        </div>
        <el-table :data="todoRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="evidenceNo" label="编号" width="150" />
          <el-table-column prop="contributor" label="举证员工" width="100" />
          <el-table-column prop="dept" label="部门" width="140" show-overflow-tooltip />
          <el-table-column prop="valueDimension" label="价值观维度" width="100" align="center" />
          <el-table-column prop="behaviorType" label="类型" width="70" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.behaviorType === '正向' ? 'success' : 'danger'">{{ row.behaviorType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deedDesc" label="事迹描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="selfScore" label="自评分" width="80" align="center" />
          <el-table-column prop="createTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" link @click="openApprove(row, 'first')">初审</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="can('C02','C03','C99')" label="文化复核" name="second">
        <div class="bar">
          <el-button size="small" @click="loadTodo">刷新</el-button>
        </div>
        <el-table :data="todoRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="evidenceNo" label="编号" width="150" />
          <el-table-column prop="contributor" label="举证员工" width="100" />
          <el-table-column prop="dept" label="部门" width="140" show-overflow-tooltip />
          <el-table-column prop="valueDimension" label="价值观维度" width="100" align="center" />
          <el-table-column prop="deedDesc" label="事迹描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="selfScore" label="自评分" width="80" align="center" />
          <el-table-column label="审批日志" width="120">
            <template #default="{row}">
              <el-popover v-if="row.approvalLog && row.approvalLog !== '[]'" trigger="click" width="400">
                <template #reference><el-button size="small" link>查看日志</el-button></template>
                <pre class="log-pre">{{ formatLog(row.approvalLog) }}</pre>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" link @click="openApprove(row, 'second')">复核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="can('C02','C03','C99')" label="全部举证" name="all">
        <div class="bar">
          <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:120px" @change="loadAll">
            <el-option label="草稿" value="草稿" />
            <el-option label="待初审" value="待初审" />
            <el-option label="待复核" value="待复核" />
            <el-option label="已入账" value="已入账" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
          <el-select v-model="filter.dimension" placeholder="维度" clearable size="small" style="width:120px" @change="loadAll">
            <el-option label="客户导向" value="客户导向" />
            <el-option label="奋斗担当" value="奋斗担当" />
            <el-option label="艰苦奋斗" value="艰苦奋斗" />
            <el-option label="自我批判" value="自我批判" />
          </el-select>
          <el-input v-model="filter.keyword" placeholder="关键词" clearable size="small" style="width:160px" @change="loadAll" />
          <span style="flex:1"></span>
          <el-button size="small" @click="loadAll">刷新</el-button>
        </div>
        <el-table :data="allRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="evidenceNo" label="编号" width="150" />
          <el-table-column prop="contributor" label="员工" width="90" />
          <el-table-column prop="dept" label="部门" width="130" show-overflow-tooltip />
          <el-table-column prop="valueDimension" label="维度" width="90" align="center" />
          <el-table-column prop="behaviorType" label="类型" width="70" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.behaviorType === '正向' ? 'success' : 'danger'">{{ row.behaviorType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deedDesc" label="事迹" min-width="180" show-overflow-tooltip />
          <el-table-column prop="selfScore" label="自评分" width="70" align="center" />
          <el-table-column prop="finalScore" label="终评分" width="70" align="center" />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建/编辑举证弹窗 -->
    <el-dialog v-model="editVis" :title="editRow.id ? '编辑举证' : '新建举证'" width="640px" :close-on-click-modal="false">
      <el-form label-width="100px" size="small">
        <el-form-item label="价值观维度" required>
          <el-select v-model="editRow.valueDimension" placeholder="选择维度" style="width:100%">
            <el-option label="客户导向" value="客户导向" />
            <el-option label="奋斗担当" value="奋斗担当" />
            <el-option label="艰苦奋斗" value="艰苦奋斗" />
            <el-option label="自我批判" value="自我批判" />
          </el-select>
        </el-form-item>
        <el-form-item label="行为类型" required>
          <el-radio-group v-model="editRow.behaviorType">
            <el-radio value="正向">正向</el-radio>
            <el-radio value="反向">反向</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发生时间" required>
          <el-date-picker v-model="editRow.occurTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" :disabled-date="disabledFuture" />
        </el-form-item>
        <el-form-item label="场景" required>
          <el-input v-model="editRow.scene" placeholder="如 客户现场/月度复盘" />
        </el-form-item>
        <el-form-item label="事迹描述" required>
          <el-input v-model="editRow.deedDesc" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="不少于30字" />
        </el-form-item>
        <el-form-item label="佐证附件" required>
          <el-input v-model="editRow.attachments" placeholder='JSON数组，如 [{"name":"照片.jpg","url":"/files/a.jpg"}]' />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-input v-model="editRow.relatedProject" placeholder="可选" />
        </el-form-item>
        <el-form-item label="自评分" required>
          <el-input-number v-model="editRow.selfScore" :min="0" :max="10" :step="0.5" controls-position="right" />
          <span class="tip">0-10分，作为复核参考</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="apVis" :title="apStage === 'first' ? '部门初审' : '文化复核'" width="500px">
      <el-form label-width="80px" size="small">
        <el-form-item label="审批结果">
          <el-radio-group v-model="apPass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="apOpinion" type="textarea" :rows="3" placeholder="驳回时不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="apVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="approving" @click="doApprove">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVis" title="举证详情" size="560px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ detailRow.evidenceNo }}</el-descriptions-item>
          <el-descriptions-item label="员工">{{ detailRow.contributor }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailRow.dept }}</el-descriptions-item>
          <el-descriptions-item label="维度">{{ detailRow.valueDimension }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailRow.behaviorType }}</el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ detailRow.occurTime }}</el-descriptions-item>
          <el-descriptions-item label="场景">{{ detailRow.scene }}</el-descriptions-item>
          <el-descriptions-item label="自评分">{{ detailRow.selfScore }}</el-descriptions-item>
          <el-descriptions-item label="终评分">{{ detailRow.finalScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="statusType(detailRow.status)">{{ detailRow.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="mt"><strong>事迹描述：</strong>{{ detailRow.deedDesc }}</div>
        <div class="mt"><strong>审批日志：</strong><pre class="log-pre">{{ formatLog(detailRow.approvalLog) }}</pre></div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const tab = ref('mine')
const loading = ref(false)
const mineRows = ref([])
const todoRows = ref([])
const allRows = ref([])
const filter = ref({ status: '', dimension: '', keyword: '' })

const editVis = ref(false)
const saving = ref(false)
const editRow = ref({})

const apVis = ref(false)
const approving = ref(false)
const apRow = ref({})
const apStage = ref('first')
const apPass = ref(true)
const apOpinion = ref('')

const detailVis = ref(false)
const detailRow = ref(null)

function statusType(s) {
  if (s === '已入账') return 'success'
  if (s === '已驳回') return 'danger'
  if (s === '待初审' || s === '待复核') return 'warning'
  return 'info'
}
function fmt(t) { return t ? t.substring(0, 16) : '-' }
function formatLog(log) {
  try { return JSON.stringify(JSON.parse(log), null, 2) } catch { return log }
}
function disabledFuture(d) { return d && d.getTime() > Date.now() }

async function loadMine() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/evidence/mine'); mineRows.value = r.data.data || [] } catch { mineRows.value = [] } finally { loading.value = false }
}
async function loadTodo() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/evidence/todo'); todoRows.value = r.data.data || [] } catch { todoRows.value = [] } finally { loading.value = false }
}
async function loadAll() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/evidence/list', { params: filter.value }); allRows.value = r.data.data || [] } catch { allRows.value = [] } finally { loading.value = false }
}

function openCreate() {
  editRow.value = { valueDimension: '', behaviorType: '正向', occurTime: '', scene: '', deedDesc: '', attachments: '', relatedProject: '', selfScore: 8 }
  editVis.value = true
}
function openEdit(row) {
  editRow.value = { ...row }
  editVis.value = true
}
async function doSave() {
  saving.value = true
  try {
    const url = editRow.value.id ? `/api/culture/behavior/evidence/${editRow.value.id}` : '/api/culture/behavior/evidence'
    const method = editRow.value.id ? 'put' : 'post'
    await axios[method](url, editRow.value)
    ElMessage.success('保存成功')
    editVis.value = false
    loadMine()
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}
async function doSubmit(row) {
  try {
    await ElMessageBox.confirm('提交后将进入部门初审，确认提交？', '提示', { type: 'warning' })
    await axios.post(`/api/culture/behavior/evidence/${row.id}/submit`)
    ElMessage.success('已提交')
    loadMine()
  } catch { /* cancelled */ }
}
function openApprove(row, stage) {
  apRow.value = row
  apStage.value = stage
  apPass.value = true
  apOpinion.value = ''
  apVis.value = true
}
async function doApprove() {
  approving.value = true
  try {
    await axios.post(`/api/culture/behavior/evidence/${apRow.value.id}/approve`, null, { params: { pass: apPass.value, opinion: apOpinion.value } })
    ElMessage.success('审批完成')
    apVis.value = false
    loadTodo()
    loadMine()
  } catch (e) { ElMessage.error(e.response?.data?.message || '审批失败') } finally { approving.value = false }
}
function openDetail(row) {
  detailRow.value = row
  detailVis.value = true
}

onMounted(() => {
  loadMine()
  if (can('C04','C99') || can('C02','C03','C99')) loadTodo()
})
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 10px; }
.mb { margin-bottom: 10px; }
.tip { color: #999; font-size: 12px; margin-left: 8px; }
.log-pre { font-size: 12px; background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 300px; overflow: auto; }
</style>

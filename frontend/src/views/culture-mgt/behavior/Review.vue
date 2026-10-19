<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">价值观评议：4维度加权(客户导向25%/奋斗担当30%/艰苦奋斗25%/自我批判20%)，窗口期内匿名/实名评议</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" class="mt">
      <el-tab-pane v-if="can('C02','C03','C99')" label="评议配置" name="config">
        <div class="bar">
          <el-button size="small" type="primary" @click="openConfig">新增配置</el-button>
          <el-button size="small" @click="loadConfig">刷新</el-button>
        </div>
        <el-table :data="configRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column label="权重配置" min-width="260">
            <template #default="{row}">
              <el-tag v-for="(v,k) in parseWeight(row.weightJson)" :key="k" size="small" class="w-tag">{{ k }}:{{ v }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetScope" label="参评对象" width="120" show-overflow-tooltip />
          <el-table-column label="互评" width="60" align="center">
            <template #default="{row}">{{ row.allowPeer === 1 ? '允许' : '否' }}</template>
          </el-table-column>
          <el-table-column label="评议窗口" width="220">
            <template #default="{row}">{{ fmt(row.startTime) }} ~ {{ fmt(row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.status === '已发布' ? 'success' : row.status === '已结束' ? 'info' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publisher" label="发布人" width="90" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '草稿'" size="small" type="primary" link @click="openConfig(row)">编辑</el-button>
              <el-button v-if="row.status === '草稿'" size="small" type="success" link @click="doPublish(row)">发布</el-button>
              <el-button v-if="row.status === '已发布'" size="small" type="info" link @click="doFinish(row)">结束</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我待评议" name="todo">
        <div class="bar">
          <el-button size="small" @click="loadTodo">刷新</el-button>
        </div>
        <el-table :data="todoRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="evaluatee" label="被评人" width="90" />
          <el-table-column prop="relation" label="关系" width="70" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{row}">
              <el-tag size="small" type="info">{{ row.pushed === 1 ? '已评' : '未评' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" link @click="openReview(row)">评议</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我已评议" name="given">
        <el-table :data="givenRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="evaluatee" label="被评人" width="90" />
          <el-table-column prop="relation" label="关系" width="70" align="center" />
          <el-table-column prop="totalScore" label="总分" width="70" align="center" />
          <el-table-column prop="levelTag" label="等级" width="60" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="levelType(row.levelTag)">{{ row.levelTag }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评语" min-width="200" show-overflow-tooltip />
          <el-table-column prop="submitTime" label="评议时间" width="160" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我收到的评议" name="received">
        <el-table :data="receivedRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="evaluator" label="评议人" width="90" />
          <el-table-column prop="relation" label="关系" width="70" align="center" />
          <el-table-column prop="totalScore" label="总分" width="70" align="center" />
          <el-table-column prop="levelTag" label="等级" width="60" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="levelType(row.levelTag)">{{ row.levelTag }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评语" min-width="200" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 配置弹窗 -->
    <el-dialog v-model="cfgVis" :title="cfgRow.id ? '编辑评议配置' : '新增评议配置'" width="560px" :close-on-click-modal="false">
      <el-form label-width="100px" size="small">
        <el-form-item label="绩效周期" required>
          <el-input v-model="cfgRow.periodTag" placeholder="如 2026Q3" />
        </el-form-item>
        <el-form-item label="权重配置" required>
          <div class="weight-line">
            <span>客户导向</span><el-input-number v-model="weights.客户导向" :min="0" :max="100" controls-position="right" />
            <span>奋斗担当</span><el-input-number v-model="weights.奋斗担当" :min="0" :max="100" controls-position="right" />
            <span>艰苦奋斗</span><el-input-number v-model="weights.艰苦奋斗" :min="0" :max="100" controls-position="right" />
            <span>自我批判</span><el-input-number v-model="weights.自我批判" :min="0" :max="100" controls-position="right" />
          </div>
          <div class="tip">合计：{{ weightSum }}%（须=100）</div>
        </el-form-item>
        <el-form-item label="参评对象" required>
          <el-input v-model="cfgRow.targetScope" placeholder="如 集团总部及各分公司" />
        </el-form-item>
        <el-form-item label="允许互评">
          <el-switch v-model="cfgRow.allowPeer" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="评议窗口" required>
          <el-date-picker v-model="cfgRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束"
                          value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm" range-separator="至" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="cfgVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSaveConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 评议弹窗 -->
    <el-dialog v-model="rvVis" title="价值观评议" width="520px" :close-on-click-modal="false">
      <el-form label-width="100px" size="small">
        <el-form-item label="被评人">
          <el-input :model-value="rvRow.evaluatee" disabled />
        </el-form-item>
        <el-form-item label="评议关系">
          <el-radio-group v-model="rvForm.relation">
            <el-radio value="上级">上级</el-radio>
            <el-radio value="互评">互评</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider>4维度评分(0-100)</el-divider>
        <el-form-item label="客户导向">
          <el-input-number v-model="rvForm.scoreCustomer" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="奋斗担当">
          <el-input-number v-model="rvForm.scoreStrive" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="艰苦奋斗">
          <el-input-number v-model="rvForm.scoreHardship" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="自我批判">
          <el-input-number v-model="rvForm.scoreReflect" :min="0" :max="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="评语" required>
          <el-input v-model="rvForm.comment" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="不少于10字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="rvVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doReview">提交评议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const tab = ref(can('C02','C03','C99') ? 'config' : 'todo')
const loading = ref(false)
const configRows = ref([])
const todoRows = ref([])
const givenRows = ref([])
const receivedRows = ref([])

const cfgVis = ref(false)
const saving = ref(false)
const cfgRow = ref({})
const cfgRange = ref([])
const weights = ref({ '客户导向': 25, '奋斗担当': 30, '艰苦奋斗': 25, '自我批判': 20 })
const weightSum = computed(() => Object.values(weights.value).reduce((a, b) => a + b, 0))

const rvVis = ref(false)
const rvRow = ref({})
const rvForm = ref({ relation: '互评', scoreCustomer: 80, scoreStrive: 80, scoreHardship: 80, scoreReflect: 80, comment: '' })

function levelType(l) { if (l === 'S' || l === 'A') return 'success'; if (l === 'B') return 'warning'; return 'danger' }
function fmt(t) { return t ? t.substring(0, 16) : '-' }
function parseWeight(json) { try { return JSON.parse(json) } catch { return {} } }

async function loadConfig() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/review/config/list'); configRows.value = r.data.data || [] } catch { configRows.value = [] } finally { loading.value = false }
}
async function loadTodo() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/review/my-todo'); todoRows.value = r.data.data || [] } catch { todoRows.value = [] } finally { loading.value = false }
}
async function loadGiven() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/review/my-given'); givenRows.value = r.data.data || [] } catch { givenRows.value = [] } finally { loading.value = false }
}
async function loadReceived() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/review/my-received'); receivedRows.value = r.data.data || [] } catch { receivedRows.value = [] } finally { loading.value = false }
}

function openConfig(row) {
  if (row) {
    cfgRow.value = { ...row }
    cfgRange.value = [row.startTime, row.endTime]
    const w = parseWeight(row.weightJson)
    weights.value = { '客户导向': w['客户导向']||25, '奋斗担当': w['奋斗担当']||30, '艰苦奋斗': w['艰苦奋斗']||25, '自我批判': w['自我批判']||20 }
  } else {
    cfgRow.value = { periodTag: '', targetScope: '', allowPeer: 0 }
    cfgRange.value = []
    weights.value = { '客户导向': 25, '奋斗担当': 30, '艰苦奋斗': 25, '自我批判': 20 }
  }
  cfgVis.value = true
}
async function doSaveConfig() {
  if (weightSum.value !== 100) { ElMessage.warning('权重合计须=100%'); return }
  saving.value = true
  try {
    cfgRow.value.weightJson = JSON.stringify(weights.value)
    if (cfgRange.value && cfgRange.value.length === 2) { cfgRow.value.startTime = cfgRange.value[0]; cfgRow.value.endTime = cfgRange.value[1] }
    await axios.post('/api/culture/behavior/review/config', cfgRow.value)
    ElMessage.success('保存成功')
    cfgVis.value = false
    loadConfig()
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}
async function doPublish(row) {
  try {
    await ElMessageBox.confirm('发布后配置将锁定不可修改，确认发布？', '提示', { type: 'warning' })
    await axios.post(`/api/culture/behavior/review/config/${row.id}/publish`)
    ElMessage.success('已发布')
    loadConfig()
  } catch { /* cancelled */ }
}
async function doFinish(row) {
  try {
    await ElMessageBox.confirm('结束后该评议周期关闭，确认结束？', '提示', { type: 'warning' })
    await axios.post(`/api/culture/behavior/review/config/${row.id}/finish`)
    ElMessage.success('已结束')
    loadConfig()
  } catch { /* cancelled */ }
}

function openReview(row) {
  rvRow.value = row
  rvForm.value = { configId: row.configId, evaluatee: row.evaluatee, relation: '互评', scoreCustomer: 80, scoreStrive: 80, scoreHardship: 80, scoreReflect: 80, comment: '' }
  rvVis.value = true
}
async function doReview() {
  saving.value = true
  try {
    await axios.post('/api/culture/behavior/review/submit', { ...rvForm.value, configId: rvRow.value.configId })
    ElMessage.success('评议已提交')
    rvVis.value = false
    loadTodo()
    loadGiven()
  } catch (e) { ElMessage.error(e.response?.data?.message || '提交失败') } finally { saving.value = false }
}

onMounted(() => {
  if (can('C02','C03','C99')) loadConfig()
  loadTodo()
  loadGiven()
  loadReceived()
})
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 10px; }
.tip { color: #999; font-size: 12px; margin-left: 8px; }
.weight-line { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.w-tag { margin-right: 6px; margin-bottom: 4px; }
</style>

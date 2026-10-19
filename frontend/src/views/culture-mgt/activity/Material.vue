<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">物料库存：出入库写台账并回写结余；库存≤安全阈值红色预警；出库超库存拦截</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option label="正常" value="正常" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-select v-model="q.type" placeholder="类型" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增物料</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt" max-height="500">
        <el-table-column prop="matName" label="物料名称" min-width="170" show-overflow-tooltip>
          <template #default="{row}">
            <span>{{ row.matName }}</span>
            <el-tag v-if="isLow(row)" size="small" type="danger" class="ml4">低于安全库存</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matType" label="类型" width="80" />
        <el-table-column prop="spec" label="规格" width="100" show-overflow-tooltip />
        <el-table-column label="库存" width="110" align="center">
          <template #default="{row}">
            <span :class="{ low: isLow(row) }">{{ row.stock }}</span> {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="90" align="center" />
        <el-table-column prop="keeper" label="保管人" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='正常' ? 'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99') && row.status==='正常'" size="small" type="success" @click="openFlow(row, '入库')">入库</el-button>
            <el-button v-if="can('C02','C03','C99') && row.status==='正常'" size="small" type="warning" @click="openFlow(row, '出库')">出库</el-button>
            <el-button size="small" @click="openFlows(row)">台账</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑物料' : '新增物料'" width="560px">
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="物料名称" required><el-input v-model="form.matName" maxlength="100" show-word-limit placeholder="2-100字" /></el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="物料类型">
              <el-select v-model="form.matType" style="width:100%">
                <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号"><el-input v-model="form.spec" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="单位"><el-input v-model="form.unit" placeholder="件/条/套" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前库存"><el-input-number v-model="form.stock" :min="0" style="width:100%" controls-position="right" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="安全库存"><el-input-number v-model="form.safetyStock" :min="0" style="width:100%" controls-position="right" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="保管人"><el-input v-model="form.keeper" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="flowVis" :title="`${flowType} — ${flowRow.matName}`" width="480px">
      <el-form label-width="90px" size="small">
        <el-form-item :label="(flowType==='入库'?'入库':'出库') + '数量'" required>
          <el-input-number v-model="flowForm.qty" :min="1" style="width:100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="关联活动">
          <el-select v-model="flowForm.activityId" clearable filterable style="width:100%" placeholder="选填">
            <el-option v-for="a in actOptions" :key="a.id" :label="a.actName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="事由"><el-input v-model="flowForm.reason" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <div class="tip">当前库存 {{ flowRow.stock }} {{ flowRow.unit }}<span v-if="flowType==='出库'">，出库后结余 {{ (flowRow.stock||0) - (flowForm.qty||0) }}</span></div>
      <template #footer>
        <el-button size="small" @click="flowVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doFlow">确认{{ flowType }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="ledgerVis" size="600px" title="出入库台账">
      <el-table :data="flows" size="small" border v-loading="ledgerLoading" max-height="520">
        <el-table-column label="时间" width="150">
          <template #default="{row}">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="70">
          <template #default="{row}">
            <el-tag size="small" :type="row.flowType==='入库' ? 'success':'warning'">{{ row.flowType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qty" label="数量" width="70" align="center" />
        <el-table-column prop="balanceAfter" label="结余" width="70" align="center" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="reason" label="事由" min-width="130" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const TYPES = ['海报', '横幅', '展架', '文创', '书籍', '设备']
const rows = ref([])
const actOptions = ref([])
const loading = ref(false)
const q = reactive({ status: '', type: '' })
const formVis = ref(false)
const flowVis = ref(false)
const ledgerVis = ref(false)
const ledgerLoading = ref(false)
const flows = ref([])
const form = reactive({ id: null, matName: '', matType: '海报', spec: '', unit: '件', stock: 0, safetyStock: 0, keeper: '', remark: '' })
const flowRow = ref({})
const flowType = ref('入库')
const flowForm = reactive({ qty: 1, activityId: null, reason: '' })

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function isLow(row) { return row.status === '正常' && (row.stock || 0) <= (row.safetyStock || 0) }
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/material/list', { params: { status: q.status || undefined, type: q.type || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
async function loadActs() {
  const r = await axios.get('/api/culture/activity/list')
  actOptions.value = r.data.data || []
}
function openCreate() {
  Object.assign(form, { id: null, matName: '', matType: '海报', spec: '', unit: '件', stock: 0, safetyStock: 0, keeper: '', remark: '' })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, matName: row.matName, matType: row.matType, spec: row.spec, unit: row.unit, stock: row.stock, safetyStock: row.safetyStock, keeper: row.keeper, remark: row.remark })
  formVis.value = true
}
async function doSave() {
  if ((form.matName || '').length < 2) return ElMessage.warning('物料名称需2-100字')
  await axios({ url: form.id ? '/api/culture/material/' + form.id : '/api/culture/material', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
function openFlow(row, type) {
  flowRow.value = row
  flowType.value = type
  flowForm.qty = 1
  flowForm.activityId = null
  flowForm.reason = ''
  flowVis.value = true
}
async function doFlow() {
  if (!flowForm.qty || flowForm.qty < 1) return ElMessage.warning('数量须为正整数')
  await axios.post(`/api/culture/material/${flowRow.value.id}/flow`, {
    flowType: flowType.value, qty: flowForm.qty, activityId: flowForm.activityId, reason: flowForm.reason
  })
  ElMessage.success(flowType.value + '完成')
  flowVis.value = false
  load()
}
async function openFlows(row) {
  ledgerVis.value = true
  ledgerLoading.value = true
  try {
    const r = await axios.get('/api/culture/material/flows', { params: { materialId: row.id } })
    flows.value = r.data.data || []
  } finally { ledgerLoading.value = false }
}
onMounted(() => { load(); loadActs() })
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.ml4 { margin-left:4px; }
.tip { color:#909399; font-size:12px; }
.low { color:var(--el-color-danger); font-weight:700; }
</style>

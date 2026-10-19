<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">干部文化履职填报：季度5项（宣贯/活动/案例/谈心/整改），部门负责人填报→文化岗审核</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" class="mt">
      <el-tab-pane v-if="can('C04','C99')" label="我的履职" name="mine">
        <div class="bar">
          <el-button size="small" type="primary" @click="openCreate">新建履职</el-button>
          <el-button size="small" @click="loadMine">刷新</el-button>
        </div>
        <el-table :data="mineRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="officer" label="干部" width="90" />
          <el-table-column prop="dept" label="部门" width="140" show-overflow-tooltip />
          <el-table-column label="履职项" min-width="200">
            <template #default="{row}">
              <div v-for="(item,i) in parseItems(row.dutyItems)" :key="i" class="duty-line">
                <el-tag size="small" :type="item.done ? 'success' : 'info'">{{ item.done ? '已完成' : '未完成' }}</el-tag>
                <span class="duty-key">{{ item.key }}</span>
                <span class="duty-note">{{ item.note }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="weakness" label="短板" min-width="140" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.status === '已归档' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewOpinion" label="审核意见" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{row}">
              <el-button v-if="row.status === '草稿' || row.status === '已驳回'" size="small" link @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status === '草稿' || row.status === '已驳回'" size="small" type="primary" link @click="doSubmit(row)">提交</el-button>
              <el-button size="small" link @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="can('C02','C03','C99')" label="待审核" name="todo">
        <div class="bar">
          <el-button size="small" @click="loadTodo">刷新</el-button>
        </div>
        <el-table :data="todoRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="officer" label="干部" width="90" />
          <el-table-column prop="dept" label="部门" width="140" show-overflow-tooltip />
          <el-table-column label="履职项" min-width="200">
            <template #default="{row}">
              <div v-for="(item,i) in parseItems(row.dutyItems)" :key="i" class="duty-line">
                <el-tag size="small" :type="item.done ? 'success' : 'info'">{{ item.done ? '已完成' : '未完成' }}</el-tag>
                <span class="duty-key">{{ item.key }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="weakness" label="短板" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" link @click="openReview(row)">审核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="can('C02','C03','C99')" label="全部履职" name="all">
        <div class="bar">
          <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:120px" @change="loadAll">
            <el-option label="草稿" value="草稿" />
            <el-option label="待审核" value="待审核" />
            <el-option label="已归档" value="已归档" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
          <el-input v-model="filter.officer" placeholder="干部" clearable size="small" style="width:120px" @change="loadAll" />
          <el-input v-model="filter.period" placeholder="周期" clearable size="small" style="width:120px" @change="loadAll" />
          <span style="flex:1"></span>
          <el-button size="small" @click="loadAll">刷新</el-button>
        </div>
        <el-table :data="allRows" size="small" border v-loading="loading" class="mt">
          <el-table-column prop="periodTag" label="周期" width="100" />
          <el-table-column prop="officer" label="干部" width="90" />
          <el-table-column prop="dept" label="部门" width="140" show-overflow-tooltip />
          <el-table-column label="完成度" width="90" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="doneRate(row.dutyItems) === '100%' ? 'success' : 'warning'">{{ doneRate(row.dutyItems) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{row}">
              <el-tag size="small" :type="row.status === '已归档' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="editVis" :title="editRow.id ? '编辑履职' : '新建履职'" width="660px" :close-on-click-modal="false">
      <el-form label-width="90px" size="small">
        <el-form-item label="周期" required>
          <el-input v-model="editRow.periodTag" placeholder="如 2026Q3" />
        </el-form-item>
        <el-form-item label="部门" required>
          <el-input v-model="editRow.dept" placeholder="部门名称" />
        </el-form-item>
        <el-divider>5项履职</el-divider>
        <el-form-item v-for="(item,i) in dutyForm" :key="i" :label="item.key">
          <div class="duty-edit">
            <el-checkbox v-model="item.done" :true-value="1" :false-value="0">已完成</el-checkbox>
            <el-input v-model="item.note" placeholder="说明" class="duty-input" />
            <el-input v-model="item.attachments" placeholder='附件JSON(可选)' class="duty-input" />
          </div>
        </el-form-item>
        <el-form-item label="文化短板">
          <el-input v-model="editRow.weakness" type="textarea" :rows="2" maxlength="300" show-word-limit placeholder="发现的文化短板" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="reviewVis" title="审核履职" width="500px">
      <el-form label-width="80px" size="small">
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewPass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="reviewOpinion" type="textarea" :rows="3" placeholder="驳回时不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="reviewVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doReview">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVis" title="履职详情" size="520px">
      <template v-if="detailRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="周期">{{ detailRow.periodTag }}</el-descriptions-item>
          <el-descriptions-item label="干部">{{ detailRow.officer }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ detailRow.dept }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag size="small" :type="detailRow.status === '已归档' ? 'success' : detailRow.status === '已驳回' ? 'danger' : 'warning'">{{ detailRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detailRow.reviewer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detailRow.reviewTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt"><strong>5项履职：</strong></div>
        <div v-for="(item,i) in parseItems(detailRow.dutyItems)" :key="i" class="duty-line mt">
          <el-tag size="small" :type="item.done ? 'success' : 'info'">{{ item.done ? '已完成' : '未完成' }}</el-tag>
          <span class="duty-key">{{ item.key }}</span>
          <span class="duty-note">{{ item.note }}</span>
        </div>
        <div class="mt"><strong>短板：</strong>{{ detailRow.weakness }}</div>
        <div class="mt" v-if="detailRow.reviewOpinion"><strong>审核意见：</strong>{{ detailRow.reviewOpinion }}</div>
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

const tab = ref(can('C04','C99') ? 'mine' : 'todo')
const loading = ref(false)
const mineRows = ref([])
const todoRows = ref([])
const allRows = ref([])
const filter = ref({ status: '', officer: '', period: '' })

const editVis = ref(false)
const saving = ref(false)
const editRow = ref({})
const dutyForm = ref([{key:'宣贯',done:0,note:'',attachments:''},{key:'活动',done:0,note:'',attachments:''},{key:'案例',done:0,note:'',attachments:''},{key:'谈心',done:0,note:'',attachments:''},{key:'整改',done:0,note:'',attachments:''}])

const reviewVis = ref(false)
const reviewPass = ref(true)
const reviewOpinion = ref('')
const reviewRow = ref({})
const submitting = ref(false)

const detailVis = ref(false)
const detailRow = ref(null)

function parseItems(json) { try { return JSON.parse(json) } catch { return [] } }
function doneRate(json) {
  const items = parseItems(json); if (!items.length) return '-'
  const done = items.filter(i => i.done).length
  return Math.round(done / items.length * 100) + '%'
}

async function loadMine() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/duty/mine'); mineRows.value = r.data.data || [] } catch { mineRows.value = [] } finally { loading.value = false }
}
async function loadTodo() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/duty/todo'); todoRows.value = r.data.data || [] } catch { todoRows.value = [] } finally { loading.value = false }
}
async function loadAll() {
  loading.value = true
  try { const r = await axios.get('/api/culture/behavior/duty/list', { params: filter.value }); allRows.value = r.data.data || [] } catch { allRows.value = [] } finally { loading.value = false }
}

function openCreate() {
  editRow.value = { periodTag: '', dept: '', weakness: '' }
  dutyForm.value = [{key:'宣贯',done:0,note:'',attachments:''},{key:'活动',done:0,note:'',attachments:''},{key:'案例',done:0,note:'',attachments:''},{key:'谈心',done:0,note:'',attachments:''},{key:'整改',done:0,note:'',attachments:''}]
  editVis.value = true
}
function openEdit(row) {
  editRow.value = { ...row }
  dutyForm.value = parseItems(row.dutyItems)
  editVis.value = true
}
async function doSave() {
  saving.value = true
  try {
    editRow.value.dutyItems = JSON.stringify(dutyForm.value)
    const url = editRow.value.id ? `/api/culture/behavior/duty/${editRow.value.id}` : '/api/culture/behavior/duty'
    const method = editRow.value.id ? 'put' : 'post'
    await axios[method](url, editRow.value)
    ElMessage.success('保存成功')
    editVis.value = false
    loadMine()
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') } finally { saving.value = false }
}
async function doSubmit(row) {
  try {
    await ElMessageBox.confirm('提交后将进入文化岗审核，确认提交？', '提示', { type: 'warning' })
    await axios.post(`/api/culture/behavior/duty/${row.id}/submit`)
    ElMessage.success('已提交')
    loadMine()
  } catch { /* cancelled */ }
}

function openReview(row) {
  reviewRow.value = row
  reviewPass.value = true
  reviewOpinion.value = ''
  reviewVis.value = true
}
async function doReview() {
  submitting.value = true
  try {
    await axios.post(`/api/culture/behavior/duty/${reviewRow.value.id}/review`, null, { params: { pass: reviewPass.value, opinion: reviewOpinion.value } })
    ElMessage.success('审核完成')
    reviewVis.value = false
    loadTodo()
    loadAll()
  } catch (e) { ElMessage.error(e.response?.data?.message || '审核失败') } finally { submitting.value = false }
}

function openDetail(row) {
  detailRow.value = row
  detailVis.value = true
}

onMounted(() => {
  if (can('C04','C99')) loadMine()
  if (can('C02','C03','C99')) { loadTodo(); loadAll() }
})
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.mt { margin-top: 10px; }
.duty-line { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.duty-key { font-weight: 600; min-width: 48px; }
.duty-note { color: #666; font-size: 12px; }
.duty-edit { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.duty-input { width: 200px; }
</style>

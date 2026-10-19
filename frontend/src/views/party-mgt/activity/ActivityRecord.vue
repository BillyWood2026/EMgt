<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">模板化要素（主持/记录人/时间/地点/应到实到/议程/发言摘要）缺一不可归档 · 归档自动入四本台账 · 补录须填原因 · 决议按责任人+时限跟踪超期预警</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.orgId" placeholder="全部支部" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-select v-model="f.activityType" placeholder="全部类型" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="f.status" placeholder="全部状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in ['编辑中','已归档']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input-number v-model="f.year" :min="2020" :max="2030" size="small" controls-position="right" style="width:110px" />
        <el-button size="small" @click="load">查询</el-button>
        <el-button size="small" type="danger" plain @click="checkOverdue">决议超期检查</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02','R07','R08')" size="small" type="primary" @click="edit()">新建记录</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="orgName" label="党支部" width="110" show-overflow-tooltip />
        <el-table-column prop="activityType" label="类型" width="100" />
        <el-table-column prop="title" label="主题" min-width="170" show-overflow-tooltip />
        <el-table-column prop="holdDate" label="召开日期" width="100" />
        <el-table-column label="应/实到" width="90">
          <template #default="{ row }">{{ row.expectCount ?? '—' }}/{{ row.attendCount ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="补录" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isBackfill===1" size="small" type="warning">补录</el-tag>
            <span v-else class="muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已归档'?'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R07','R08') && row.status!=='已归档'" size="small" link @click="edit(row)">编辑</el-button>
            <el-button v-if="can('R02','R07','R08') && row.status!=='已归档'" size="small" link type="success" @click="archive(row)">归档</el-button>
            <el-button size="small" link type="primary" @click="openRes(row)">决议跟踪</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑活动记录' : '新建活动记录'" width="720px" top="6vh">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="党支部" required>
              <el-select v-model="form.orgId" style="width:100%" @change="onOrgChange">
                <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" required>
              <el-select v-model="form.activityType" style="width:100%">
                <el-option v-for="t in types" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="会议主题" required><el-input v-model="form.title" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="召开时间" required><el-date-picker v-model="form.holdDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="会议地点" required><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="主持人" required><el-input v-model="form.host" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="记录人" required><el-input v-model="form.recorder" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="关联活动ID"><el-input-number v-model="form.activityId" :min="1" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="应到人数" required><el-input-number v-model="form.expectCount" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实到人数" required><el-input-number v-model="form.attendCount" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="计入台账"><el-switch v-model="form.inLedger" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="党课主讲人"><el-input v-model="form.lecturer" placeholder="主讲人姓名" /></el-form-item>
        <el-form-item label="主讲人身份">
          <el-select v-model="form.lecturerRole" clearable style="width:260px">
            <el-option v-for="r in ['机关党委书记','班子成员','党支部书记','其他党员']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="会议议程" required><el-input v-model="form.agenda" type="textarea" :rows="2" placeholder="一、… 二、…" /></el-form-item>
        <el-form-item label="发言摘要/决议" required><el-input v-model="form.speeches" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="会议内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="照片附件"><el-input v-model="form.attachments" placeholder="附件URL，多个以逗号分隔" /></el-form-item>
        <el-form-item label="补录(追记)">
          <el-switch v-model="form.isBackfill" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="form.isBackfill===1" label="补录原因" required>
          <el-input v-model="form.backfillReason" type="textarea" :rows="2" placeholder="补录必须说明原因，否则阻断归档" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="resVisible" title="决议事项落实跟踪" size="560px">
      <div class="bar">
        <el-tag size="small" type="info">{{ resRecord && resRecord.title }}</el-tag>
        <el-button v-if="can('R02','R07','R08')" size="small" type="primary" @click="resForm={recordId:resRecord.id,status:'待落实'}; resDialog=true">新增决议</el-button>
      </div>
      <el-table :data="resList" size="small" border>
        <el-table-column prop="title" label="决议事项" min-width="160" show-overflow-tooltip />
        <el-table-column prop="owner" label="责任人" width="90" />
        <el-table-column label="时限" width="100">
          <template #default="{ row }">
            <span :class="{ overdue: row.status!=='已落实' && row.deadline < today }">{{ row.deadline }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='已落实'?'success':(row.status==='落实中'?'warning':'danger')">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="row.status!=='已落实'" size="small" link type="success" @click="finishRes(row)">落实</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="resDialog" title="新增决议事项" width="440px">
      <el-form label-width="90px">
        <el-form-item label="决议事项" required><el-input v-model="resForm.title" /></el-form-item>
        <el-form-item label="责任人" required><el-input v-model="resForm.owner" /></el-form-item>
        <el-form-item label="落实时限" required><el-date-picker v-model="resForm.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="resForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resDialog=false">取消</el-button>
        <el-button type="primary" @click="saveRes">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/activity/record'
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]
const types = ['支部党员大会', '支部委员会', '党小组会', '党课', '主题党日']

const loading = ref(false)
const rows = ref([])
const f = reactive({ orgId: null, activityType: null, status: null, year: new Date().getFullYear() })
const formVisible = ref(false)
const form = ref({})
const resVisible = ref(false)
const resRecord = ref(null)
const resList = ref([])
const resDialog = ref(false)
const resForm = ref({})
const today = new Date().toISOString().slice(0, 10)

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { ...f } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
function onOrgChange(id) { form.value.orgName = (orgs.find(o => o.id === id) || {}).name }
function edit(row) {
  form.value = row ? { ...row } : { orgId: 1, activityType: '支部党员大会', inLedger: 1, isBackfill: 0, status: '编辑中' }
  formVisible.value = true
}
async function save() {
  try {
    await axios.post(api, form.value)
    ElMessage.success('记录已保存')
    formVisible.value = false
    load()
  } catch {}
}
async function archive(row) {
  try {
    await axios.post(`${api}/${row.id}/archive`)
    ElMessage.success('归档成功，已自动归集三会一课台账')
    load()
  } catch {}
}
async function openRes(row) {
  resRecord.value = row
  resVisible.value = true
  const res = await axios.get(`${api}/resolution/list/${row.id}`)
  if (res.data.code === 200) resList.value = res.data.data
}
async function saveRes() {
  try {
    await axios.post(`${api}/resolution`, resForm.value)
    ElMessage.success('决议已登记')
    resDialog.value = false
    openRes(resRecord.value)
  } catch {}
}
async function finishRes(row) {
  try {
    await axios.post(`${api}/resolution/${row.id}/finish`, null, { params: { remark: '已完成落实' } })
    ElMessage.success('已标记落实')
    openRes(resRecord.value)
  } catch {}
}
async function checkOverdue() {
  try {
    await axios.post(`${api}/resolution/check-overdue`)
    ElMessage.success('无超期未落实决议')
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.muted { color: #c0c4cc; }
.overdue { color: #f56c6c; font-weight: 600; }
</style>

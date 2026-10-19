<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">频次：党员大会/党课季度≥1 · 支委会/党小组会/主题党日月≥1 · 同日冲突提示 · 上级组织部门审批后发布</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.orgId" placeholder="全部支部" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-select v-model="f.activityType" placeholder="全部类型" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="f.approveStatus" placeholder="审批状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in ['待审批','通过','退回']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="f.publishStatus" placeholder="发布状态" clearable size="small" style="width:110px" @change="load">
          <el-option v-for="s in ['未发布','已发布']" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input-number v-model="f.year" :min="2020" :max="2030" size="small" controls-position="right" style="width:110px" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-input-number v-model="checkQ" :min="1" :max="4" size="small" controls-position="right" style="width:96px" placeholder="季度" />
        <el-button size="small" type="warning" @click="checkFrequency">频次合规校验</el-button>
        <el-button v-if="can('R02','R07','R08')" size="small" type="primary" @click="edit()">编制计划</el-button>
      </div>

      <el-table :data="rows" size="small" border v-loading="loading">
        <el-table-column prop="orgName" label="党支部" width="120" show-overflow-tooltip />
        <el-table-column prop="activityType" label="类型" width="110" />
        <el-table-column prop="title" label="活动主题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="planDate" label="计划日期" width="100" />
        <el-table-column prop="host" label="主持人" width="90" />
        <el-table-column prop="expectCount" label="应参加" width="70" />
        <el-table-column label="冲突" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.conflictFlag===1" size="small" type="danger">冲突</el-tag>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="审批" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.approveStatus==='通过'?'success':(row.approveStatus==='退回'?'danger':'warning')">{{ row.approveStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.publishStatus==='已发布'?'success':'info'">{{ row.publishStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R07','R08')" size="small" link @click="edit(row)">编辑</el-button>
            <el-button v-if="can('R02','R03','R04') && row.approveStatus==='待审批'" size="small" link type="success" @click="openApprove(row,true)">通过</el-button>
            <el-button v-if="can('R02','R03','R04') && row.approveStatus==='待审批'" size="small" link type="danger" @click="openApprove(row,false)">退回</el-button>
            <el-button v-if="can('R02','R07','R08') && row.approveStatus==='通过' && row.publishStatus!=='已发布'" size="small" link type="primary" @click="publish(row)">发布</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑计划' : '编制计划'" width="620px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="党支部" required>
          <el-select v-model="form.orgId" style="width:100%" @change="onOrgChange">
            <el-option v-for="o in orgs" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动类型" required>
          <el-select v-model="form.activityType" style="width:100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动主题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="计划日期" required>
          <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="活动地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="主持人" required><el-input v-model="form.host" /></el-form-item>
        <el-form-item label="参加范围" required><el-input v-model="form.participantScope" placeholder="如：全体党员" /></el-form-item>
        <el-form-item label="应参加人数"><el-input-number v-model="form.expectCount" :min="1" /></el-form-item>
        <el-form-item label="签到方式">
          <el-select v-model="form.signMethod" style="width:100%">
            <el-option v-for="m in ['现场扫码','定位','人脸','人工代签']" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="主讲人(党课)"><el-input v-model="form.lecturer" /></el-form-item>
        <el-form-item label="主讲人身份">
          <el-select v-model="form.lecturerRole" clearable style="width:100%">
            <el-option v-for="r in ['机关党委书记','班子成员','党支部书记']" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveVisible" title="计划审批" width="440px">
      <el-form label-width="80px">
        <el-form-item label="活动">{{ current && current.title }}</el-form-item>
        <el-form-item label="审批意见"><el-input v-model="approveOpinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" @click="doApprove(false)">退回</el-button>
        <el-button type="success" @click="doApprove(true)">审批通过</el-button>
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

const api = '/api/party/activity/plan'
const orgs = [{ id: 1, name: '第一党支部' }, { id: 2, name: '第二党支部' }]
const types = ['支部党员大会', '支部委员会', '党小组会', '党课', '主题党日']

const loading = ref(false)
const rows = ref([])
const f = reactive({ orgId: null, activityType: null, approveStatus: null, publishStatus: null, year: new Date().getFullYear() })
const checkQ = ref(Math.floor(new Date().getMonth() / 3) + 1)
const formVisible = ref(false)
const form = ref({})
const approveVisible = ref(false)
const current = ref(null)
const approveOpinion = ref('')

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { ...f } })
    if (res.data.code === 200) rows.value = res.data.data
  } finally { loading.value = false }
}
function onOrgChange(id) { form.value.orgName = (orgs.find(o => o.id === id) || {}).name }
function edit(row) {
  form.value = row ? { ...row } : { orgId: 1, activityType: '支部党员大会', inLedger: 1, signMethod: '现场扫码' }
  formVisible.value = true
}
async function save() {
  try {
    await axios.post(api, form.value)
    ElMessage.success('已保存（同日冲突已自动标记）')
    formVisible.value = false
    load()
  } catch {}
}
async function checkFrequency() {
  try {
    await axios.post(`${api}/check-frequency`, null, { params: { orgId: f.orgId || 1, year: f.year, quarter: checkQ.value } })
    ElMessage.success(`${f.year}年第${checkQ.value}季度频次合规`)
  } catch {}
}
function openApprove(row) { current.value = row; approveOpinion.value = ''; approveVisible.value = true }
async function doApprove(passed) {
  try {
    await axios.post(`${api}/${current.value.id}/approve`, null, { params: { passed, opinion: approveOpinion.value } })
    ElMessage.success(passed ? '已审批通过' : '已退回')
    approveVisible.value = false
    load()
  } catch {}
}
async function publish(row) {
  try {
    await axios.post(`${api}/${row.id}/publish`)
    ElMessage.success('活动已发布，报名签到通道开启')
    load()
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.muted { color: #c0c4cc; }
</style>

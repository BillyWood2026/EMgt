<template>
  <div class="page-container">
    <RoleBar />
    <el-card shadow="never">
      <el-tabs v-model="tab">
        <!-- ============ 会议台账 ============ -->
        <el-tab-pane label="会议台账" name="meeting">
          <div class="bar">
            <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:230px" @change="loadMeetings">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filter.meetingType" placeholder="类型" clearable size="small" style="width:130px" @change="loadMeetings">
              <el-option label="民主生活会" value="民主生活会" />
              <el-option label="组织生活会" value="组织生活会" />
            </el-select>
            <el-select v-model="filter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadMeetings">
              <el-option v-for="y in [2024,2025,2026,2027]" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:110px" @change="loadMeetings">
              <el-option label="计划中" value="计划中" /><el-option label="已召开" value="已召开" /><el-option label="已归档" value="已归档" />
            </el-select>
            <el-button v-if="can('R02','R04','R07')" size="small" type="primary" @click="openMeeting()">发起/补录会议</el-button>
          </div>

          <el-table :data="meetings" size="small" border v-loading="loading">
            <el-table-column prop="meetingType" label="类型" width="100" />
            <el-table-column prop="topic" label="主题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="orgName" label="组织" width="180" show-overflow-tooltip />
            <el-table-column prop="meetingDate" label="时间" width="110" />
            <el-table-column prop="host" label="主持人" width="90" />
            <el-table-column label="方式" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.supplement" size="small" type="warning">补录</el-tag>
                <span v-else>正常</span>
              </template>
            </el-table-column>
            <el-table-column label="材料" width="70">
              <template #default="{ row }">
                <el-tag size="small" :type="row.preMaterialsReady ? 'success' : 'info'">
                  {{ row.preMaterialsReady ? '齐' : '缺' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status !== '已召开' && row.status !== '已归档' && can('R02','R04','R07')"
                           size="small" link type="success" @click="openRecord(row)">登记召开</el-button>
                <el-button v-if="can('R02','R04','R07')" size="small" link @click="openMeeting(row)">编辑</el-button>
                <el-button size="small" link type="primary" @click="openRectifications(row)">整改清单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 整改台账 ============ -->
        <el-tab-pane label="整改台账" name="rect">
          <div class="bar">
            <span class="tip">到期前7天预警、超期升级、超期30天告警；销号必须填写整改结果并附佐证。</span>
            <el-button v-if="can('R02','R04','R07')" size="small" type="primary" @click="openRectForm()">新增整改项</el-button>
          </div>
          <el-table :data="rects" size="small" border v-loading="rectLoading">
            <el-table-column prop="problem" label="问题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="ownerName" label="责任人" width="90" />
            <el-table-column prop="deadline" label="期限" width="110" />
            <el-table-column label="提醒级别" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="levelType(row.reminderLevel)">{{ row.reminderLevel || '正常' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '已销号' ? 'success' : 'warning'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="closeDate" label="销号日期" width="110" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button v-if="row.status !== '已销号' && can('R02','R04','R07')"
                           size="small" link type="success" @click="openClose(row)">销号</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 年度统计 ============ -->
        <el-tab-pane label="年度统计" name="stats">
          <div class="bar">
            <el-select v-model="statsYear" size="small" style="width:110px" @change="loadStats">
              <el-option v-for="y in [2024,2025,2026,2027]" :key="y" :label="y + '年'" :value="y" />
            </el-select>
          </div>
          <el-row :gutter="12" class="stat-cards">
            <el-col :span="6"><el-card shadow="never"><div class="num">{{ stats.orgCount }}</div><div class="lbl">党组织数</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="never"><div class="num">{{ stats.requiredSessions }}</div><div class="lbl">应召开(次)</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="never"><div class="num">{{ stats.heldSessions }}</div><div class="lbl">已召开(次)</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="never"><div class="num red">{{ stats.holdRate }}%</div><div class="lbl">召开/达标率</div></el-card></el-col>
          </el-row>
          <el-table :data="stats.details || []" size="small" border style="margin-top:12px">
            <el-table-column prop="orgName" label="党组织" min-width="200" show-overflow-tooltip />
            <el-table-column prop="民主生活会" label="民主生活会(已开)" width="140" />
            <el-table-column prop="组织生活会" label="组织生活会(已开)" width="140" />
            <el-table-column label="预警">
              <template #default="{ row }">
                <el-tag v-for="(v, k) in row.alerts" :key="k" size="small"
                        :type="v.includes('告警') ? 'danger' : 'warning'" class="alert-tag">
                  {{ k }}：{{ v }}
                </el-tag>
                <span v-if="!Object.keys(row.alerts || {}).length" class="tip">达标</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 发起/编辑/补录会议 -->
    <el-dialog v-model="meetingVisible" :title="meetingForm.id ? '编辑会议' : '发起/补录会议'" width="720px">
      <el-form :model="meetingForm" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="组织" required>
              <el-select v-model="meetingForm.orgId" filterable style="width:100%" @change="onOrgPick">
                <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会议类型" required>
              <el-radio-group v-model="meetingForm.meetingType">
                <el-radio value="民主生活会">民主生活会</el-radio>
                <el-radio value="组织生活会">组织生活会</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="14"><el-form-item label="主题" required><el-input v-model="meetingForm.topic" maxlength="100" show-word-limit /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="主持人" required><el-input v-model="meetingForm.host" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="会议时间" required><el-date-picker v-model="meetingForm.meetingDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="会议地点"><el-input v-model="meetingForm.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="参会党员"><el-input v-model="meetingForm.participants" placeholder="逗号分隔，或JSON" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="列席人员"><el-input v-model="meetingForm.attendeesNonvoting" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="事后补录">
              <el-switch v-model="meetingForm.supplement" :active-value="1" :inactive-value="0" />
              <span v-if="meetingForm.supplement" class="tip">补录必须注明原因</span>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="meetingForm.supplement">
            <el-form-item label="补录原因" required><el-input v-model="meetingForm.supplementReason" type="textarea" :rows="2" /></el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="议程与记录"><el-input v-model="meetingForm.agendaContent" type="textarea" :rows="3" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="meetingVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMeeting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 登记召开：四类材料 + 缺席原因 -->
    <el-dialog v-model="recordVisible" title="登记会议召开（会前准备核验）" width="720px">
      <p class="tip">四类会前材料必须齐备，缺席人员必须登记原因，否则不能归档。</p>
      <el-checkbox-group v-model="materialTypes" class="material-grid">
        <el-checkbox v-for="m in REQUIRED_MATERIALS" :key="m" :value="m" :label="m">{{ m }}</el-checkbox>
      </el-checkbox-group>
      <el-table :data="absences" size="small" border style="margin-top:10px">
        <el-table-column label="缺席人员" width="170">
          <template #default="{ row }"><el-input v-model="row.memberName" size="small" placeholder="姓名" /></template>
        </el-table-column>
        <el-table-column label="缺席原因（必填）">
          <template #default="{ row }"><el-input v-model="row.reason" size="small" placeholder="如：出差/病假，已请假" /></template>
        </el-table-column>
        <el-table-column width="70">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="absences.splice($index,1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button size="small" class="add-btn" @click="absences.push({ memberName: '', reason: '' })">添加缺席人员</el-button>
      <template #footer>
        <el-button @click="recordVisible = false">取消</el-button>
        <el-button type="success" @click="submitHold">核验通过并登记召开</el-button>
      </template>
    </el-dialog>

    <!-- 整改项表单 -->
    <el-dialog v-model="rectFormVisible" title="整改问题" width="560px">
      <el-form :model="rectForm" label-width="90px">
        <el-form-item label="所属会议">
          <el-select v-model="rectForm.meetingId" clearable filterable style="width:100%">
            <el-option v-for="m in meetings" :key="m.id" :label="m.meetingType + '·' + m.topic" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题" required><el-input v-model="rectForm.problem" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="整改措施"><el-input v-model="rectForm.measure" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="rectForm.ownerName" /></el-form-item>
        <el-form-item label="完成期限"><el-date-picker v-model="rectForm.deadline" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rectFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRectForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 销号 -->
    <el-dialog v-model="closeVisible" title="整改销号" width="520px">
      <el-form label-width="90px">
        <el-form-item label="问题">{{ closeRow.problem }}</el-form-item>
        <el-form-item label="整改结果" required><el-input v-model="closeResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="佐证材料"><el-input v-model="closeEvidence" placeholder="附件链接或说明" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeVisible = false">取消</el-button>
        <el-button type="success" @click="submitClose">确认销号</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/org/life-meeting'
const REQUIRED_MATERIALS = ['方案', '学习记录', '征求意见汇总', '谈心谈话记录']

const tab = ref('meeting')
const loading = ref(false)
const meetings = ref([])
const rects = ref([])
const rectLoading = ref(false)
const orgFlat = ref([])
const filter = reactive({ orgId: null, meetingType: '', year: null, status: '' })

async function loadOrgs() {
  const res = await axios.get('/api/party/org/organization/tree')
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}

async function loadMeetings() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: {
      orgId: filter.orgId || undefined, meetingType: filter.meetingType || undefined,
      year: filter.year || undefined, status: filter.status || undefined
    } })
    if (res.data.code === 200) meetings.value = res.data.data
  } finally { loading.value = false }
}

function statusType(s) {
  return { '计划中': 'info', '已召开': 'success', '已归档': '' }[s] || 'info'
}
function levelType(l) {
  return { '正常': 'success', '临近到期': 'warning', '已超期': 'danger', '超期告警': 'danger' }[l] || 'success'
}

// ---------- 会议表单 ----------
const meetingVisible = ref(false)
const meetingForm = reactive({})

function resetMeeting() {
  Object.keys(meetingForm).forEach(k => delete meetingForm[k])
  Object.assign(meetingForm, { meetingType: '组织生活会', supplement: 0, status: '计划中' })
}
function onOrgPick(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) meetingForm.orgName = o.name
}
function openMeeting(row) {
  resetMeeting()
  if (row) Object.assign(meetingForm, JSON.parse(JSON.stringify(row)))
  meetingVisible.value = true
}
async function submitMeeting() {
  try {
    if (meetingForm.id) await axios.put(API, meetingForm)
    else await axios.post(API, meetingForm)
    ElMessage.success('保存成功')
    meetingVisible.value = false
    loadMeetings()
  } catch { /* 拦截器提示 */ }
}

// ---------- 登记召开 ----------
const recordVisible = ref(false)
const materialTypes = ref([])
const absences = ref([])
let recordRow = null

async function openRecord(row) {
  recordRow = row
  materialTypes.value = []
  absences.value = []
  recordVisible.value = true
}

async function submitHold() {
  if (materialTypes.value.length < 4) { ElMessage.error('四类会前材料必须齐备（E-MEET-02）'); return }
  try {
    await axios.post(`${API}/materials/${recordRow.id}`,
      materialTypes.value.map(t => ({ materialType: t, fileName: t + '.docx', fileUrl: '' })))
    if (absences.value.some(a => a.memberName)) {
      await axios.post(`${API}/absences/${recordRow.id}`,
        absences.value.filter(a => a.memberName))
    }
    await axios.post(`${API}/hold/${recordRow.id}`)
    ElMessage.success('已登记召开')
    recordVisible.value = false
    loadMeetings()
  } catch { /* 拦截器提示缺料/缺原因 */ }
}

// ---------- 整改 ----------
const rectFormVisible = ref(false)
const rectForm = reactive({})
const closeVisible = ref(false)
const closeResult = ref('')
const closeEvidence = ref('')
let closeRow = null

async function loadRects(meetingId) {
  rectLoading.value = true
  try {
    const res = await axios.get(`${API}/rectifications`, { params: { meetingId: meetingId || undefined } })
    if (res.data.code === 200) rects.value = res.data.data
  } finally { rectLoading.value = false }
}

function openRectifications(row) {
  tab.value = 'rect'
  loadRects(row.id)
}
function openRectForm() {
  Object.keys(rectForm).forEach(k => delete rectForm[k])
  rectFormVisible.value = true
}
async function submitRectForm() {
  if (!rectForm.problem) { ElMessage.warning('问题不能为空'); return }
  try {
    await axios.post(`${API}/rectifications`, rectForm)
    ElMessage.success('已保存')
    rectFormVisible.value = false
    loadRects()
  } catch { /* 拦截器提示 */ }
}
function openClose(row) {
  closeRow = row
  closeResult.value = row.result || ''
  closeEvidence.value = row.evidenceUrl || ''
  closeVisible.value = true
}
async function submitClose() {
  if (!closeResult.value) { ElMessage.error('销号必须填写整改结果'); return }
  try {
    await axios.post(`${API}/rectifications/close/${closeRow.id}`,
      { result: closeResult.value, evidenceUrl: closeEvidence.value })
    ElMessage.success('已销号')
    closeVisible.value = false
    loadRects()
  } catch { /* 拦截器提示 */ }
}

// ---------- 统计 ----------
const statsYear = ref(2026)
const stats = ref({})
async function loadStats() {
  const res = await axios.get(`${API}/stats`, { params: { year: statsYear.value } })
  if (res.data.code === 200) stats.value = res.data.data
}

onMounted(() => {
  loadOrgs().then(loadMeetings)
  loadRects()
  loadStats()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.add-btn { margin-top: 8px; }
.material-grid { display: flex; flex-direction: column; gap: 6px; }
.stat-cards .num { font-size: 26px; font-weight: 700; color: #409eff; }
.stat-cards .num.red { color: #f56c6c; }
.stat-cards .lbl { color: #909399; font-size: 13px; margin-top: 4px; }
.alert-tag { margin: 2px 6px 2px 0; }
</style>

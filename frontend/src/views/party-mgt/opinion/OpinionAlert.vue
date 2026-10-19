<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">四色阈值矩阵 · 夜间静默22:00-7:00 · 同事件合并 · 红色30分钟未认领升级</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="预警记录" name="rec">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="f.level" placeholder="全部级别" clearable size="small" style="width:110px" @change="loadRec">
              <el-option v-for="l in levels" :key="l.v" :label="l.t" :value="l.v" />
            </el-select>
            <el-select v-model="f.status" placeholder="全部状态" clearable size="small" style="width:110px" @change="loadRec">
              <el-option v-for="s in ['待处置','处置中','已解除']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="f.keyword" placeholder="标题" size="small" style="width:150px" clearable @keyup.enter="loadRec" />
            <el-button size="small" @click="loadRec">查询</el-button>
          </div>
          <el-table :data="recs" size="small" border v-loading="loading">
            <el-table-column prop="title" label="舆情标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="alertLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="levelType(row.alertLevel)" effect="dark">{{ row.alertLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="情感/热度/关联" width="130">
              <template #default="{ row }">{{ row.sentiment }}/{{ row.heatLevel }}/{{ row.relevanceLevel }}</template>
            </el-table-column>
            <el-table-column prop="alertReason" label="预警原因" min-width="180" show-overflow-tooltip />
            <el-table-column prop="notifyStatus" label="推送" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.notifyStatus==='已推送'?'success':(row.notifyStatus==='推送失败'?'danger':'warning')">{{ row.notifyStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="claimStatus" label="认领" width="80" />
            <el-table-column prop="handleDeadline" label="处置时限" width="160" />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04','R14') && row.claimStatus==='未认领'" size="small" link type="success" @click="claim(row)">认领</el-button>
                <el-button v-if="can('R02','R04','R14')" size="small" link type="warning" @click="escalate(row)">未认领升级</el-button>
                <el-button v-if="can('R02','R04','R14') && row.analysisId && row.status!=='已解除'" size="small" link type="primary" @click="toHandle(row)">生成工单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="预警规则配置" name="rule">
        <el-card shadow="never">
          <div class="bar">
            <span class="hint">表30 阈值矩阵：红=负面+高热+高关联或重大突发事件(24h)；橙=负面+热度中高(48h)；黄=负面+热度一般(72h)；蓝=纳入监测</span>
            <el-button v-if="can('R02')" size="small" type="primary" @click="editRule()">新增规则</el-button>
          </div>
          <el-table :data="rules" size="small" border v-loading="loading">
            <el-table-column prop="ruleName" label="规则" width="110" />
            <el-table-column prop="level" label="级别" width="70">
              <template #default="{ row }"><el-tag size="small" :type="levelType(row.level)" effect="dark">{{ row.level }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="sentimentCond" label="情感条件" width="90" />
            <el-table-column prop="heatCond" label="热度条件" width="90" />
            <el-table-column prop="relevanceCond" label="关联条件" width="90" />
            <el-table-column prop="deadlineHours" label="时限(小时)" width="90" />
            <el-table-column prop="notifyWays" label="通知方式" min-width="170" show-overflow-tooltip />
            <el-table-column prop="claimTimeoutMin" label="认领超时(分)" width="100" />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link @click="editRule(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="del(`${api}/rule`, row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notify">
        <el-card shadow="never">
          <div class="bar">
            <el-button v-if="can('R02')" size="small" type="primary" @click="editNotify()">新增通知</el-button>
          </div>
          <el-table :data="notifies" size="small" border v-loading="loading">
            <el-table-column prop="notifyType" label="类型" width="100" />
            <el-table-column prop="levelScope" label="适用级别" width="120" />
            <el-table-column prop="receivers" label="接收人" min-width="200" show-overflow-tooltip />
            <el-table-column label="启用" width="80">
              <template #default="{ row }"><el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link @click="editNotify(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="del(`${api}/notify`, row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="ruleVisible" :title="rule.id ? '编辑规则' : '新增规则'" width="540px">
      <el-form :model="rule" label-width="110px">
        <el-form-item label="规则名称" required><el-input v-model="rule.ruleName" /></el-form-item>
        <el-form-item label="级别" required>
          <el-select v-model="rule.level" style="width:100%">
            <el-option v-for="l in levels" :key="l.v" :label="l.t" :value="l.v" />
          </el-select>
        </el-form-item>
        <el-form-item label="情感条件"><el-input v-model="rule.sentimentCond" placeholder="如：负面" /></el-form-item>
        <el-form-item label="热度条件"><el-input v-model="rule.heatCond" placeholder="如：高" /></el-form-item>
        <el-form-item label="关联度条件"><el-input v-model="rule.relevanceCond" placeholder="如：高" /></el-form-item>
        <el-form-item label="时限(小时)"><el-input-number v-model="rule.deadlineHours" :min="0" /></el-form-item>
        <el-form-item label="通知方式"><el-input v-model="rule.notifyWays" placeholder="短信+APP+站内" /></el-form-item>
        <el-form-item label="认领超时(分)"><el-input-number v-model="rule.claimTimeoutMin" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleVisible=false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="notifyVisible" :title="notify.id ? '编辑通知' : '新增通知'" width="460px">
      <el-form :model="notify" label-width="90px">
        <el-form-item label="类型" required>
          <el-select v-model="notify.notifyType" style="width:100%">
            <el-option v-for="t in ['短信','APP','站内']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用级别"><el-input v-model="notify.levelScope" placeholder="红,橙 / 全部" /></el-form-item>
        <el-form-item label="接收人"><el-input v-model="notify.receivers" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="notify.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="notifyVisible=false">取消</el-button>
        <el-button type="primary" @click="saveNotify">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/opinion/alert'
const levels = [{ v: '红色', t: '红色' }, { v: '橙色', t: '橙色' }, { v: '黄色', t: '黄色' }, { v: '蓝色', t: '蓝色' }]

const tab = ref('rec')
const loading = ref(false)
const recs = ref([]); const rules = ref([]); const notifies = ref([])
const f = reactive({ level: null, status: null, keyword: '' })
const ruleVisible = ref(false); const rule = ref({})
const notifyVisible = ref(false); const notify = ref({})

function levelType(l) { return { 红: 'danger', 红色: 'danger', 橙: 'warning', 橙色: 'warning', 黄: 'warning', 黄色: 'warning', 蓝: 'info', 蓝色: 'info' }[l] || 'info' }

async function loadRec() {
  const res = await axios.get(`${api}/list`, { params: { ...f, keyword: f.keyword || undefined } })
  if (res.data.code === 200) recs.value = res.data.data
}
async function loadRule() {
  const res = await axios.get(`${api}/rule/list`)
  if (res.data.code === 200) rules.value = res.data.data
}
async function loadNotify() {
  const res = await axios.get(`${api}/notify/list`)
  if (res.data.code === 200) notifies.value = res.data.data
}
function load() {
  loading.value = true
  const fn = { rec: loadRec, rule: loadRule, notify: loadNotify }[tab.value]
  Promise.resolve(fn()).finally(() => { loading.value = false })
}

async function claim(row) {
  try { await axios.post(`${api}/${row.id}/claim`); ElMessage.success('已认领'); loadRec() } catch {}
}
async function escalate(row) {
  try {
    const res = await axios.post(`${api}/${row.id}/escalate`, null, { params: { force: true } })
    if (res.data.code === 200) { ElMessage.success('已升级通知'); loadRec() }
  } catch {}
}
async function toHandle(row) {
  try {
    const res = await axios.post(`/api/party/opinion/handle/create/${row.id}`)
    if (res.data.code === 200) {
      ElMessage.success('处置工单已生成')
      try { await ElMessageBox.confirm('已生成处置工单，是否跳转处置闭环页？', '提示', { type: 'success' }).catch(() => 'cancel') } catch {}
    }
  } catch {}
}
function editRule(row) {
  rule.value = row ? { ...row } : { ruleName: '', level: '红', sentimentCond: '负面', heatCond: '', relevanceCond: '', deadlineHours: 72, notifyWays: '站内+APP推送', claimTimeoutMin: 120, status: '启用' }
  ruleVisible.value = true
}
async function saveRule() {
  try { await axios.post(`${api}/rule`, rule.value); ElMessage.success('已保存'); ruleVisible.value = false; loadRule() } catch {}
}
function editNotify(row) {
  notify.value = row ? { ...row } : { notifyType: '站内', levelScope: '全部', receivers: '', enabled: 1 }
  notifyVisible.value = true
}
async function saveNotify() {
  try { await axios.post(`${api}/notify`, notify.value); ElMessage.success('已保存'); notifyVisible.value = false; loadNotify() } catch {}
}
async function del(apiPath, id) {
  try { await axios.delete(`${apiPath}/${id}`); ElMessage.success('已删除'); load() } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.hint { font-size: 12px; color: #909399; margin-right: auto; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">消息通知：模板配置 / 发送记录 · 夜间22:00-07:00静默延迟非预警 · 重试上限3次（E-SY-11）· 写操作仅 R02</el-tag>
    </RoleBar>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="模板配置" name="template">
        <el-card shadow="never">
          <div class="bar">
            <span style="flex:1"></span>
            <el-button v-if="can('R02')" size="small" type="primary" @click="openTplEdit(null)">新增模板</el-button>
          </div>
          <el-table :data="templates" size="small" border v-loading="loading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="tplCode" label="编码" width="120" />
            <el-table-column prop="tplName" label="名称" width="140" show-overflow-tooltip />
            <el-table-column prop="msgType" label="消息类型" width="90" />
            <el-table-column prop="channels" label="渠道" min-width="140" show-overflow-tooltip />
            <el-table-column label="夜间静默" width="80">
              <template #default="{ row }"><el-tag size="small" :type="row.nightSilent ? 'warning' : 'info'">{{ row.nightSilent ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link type="primary" @click="openTplEdit(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="delTpl(row)">删除</el-button>
                <el-button size="small" link type="success" @click="openSend(row)">发送</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="发送记录" name="record">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="q.status" placeholder="全部状态" clearable size="small" style="width:120px">
              <el-option label="发送成功" value="发送成功" />
              <el-option label="发送失败" value="发送失败" />
              <el-option label="待发送" value="待发送" />
            </el-select>
            <el-select v-model="q.msgType" placeholder="全部类型" clearable size="small" style="width:120px">
              <el-option label="通知" value="通知" />
              <el-option label="预警" value="预警" />
            </el-select>
            <el-input v-model="q.receiverLike" placeholder="接收人" size="small" clearable style="width:140px" @keyup.enter="loadRecords" />
            <el-button size="small" @click="loadRecords">查询</el-button>
          </div>
          <el-table :data="records" size="small" border v-loading="loading" max-height="480">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="tplCode" label="模板" width="110" />
            <el-table-column prop="title" label="标题" width="140" show-overflow-tooltip />
            <el-table-column prop="receiver" label="接收人" width="110" />
            <el-table-column prop="channel" label="渠道" width="90" />
            <el-table-column prop="msgType" label="类型" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '发送成功' ? 'success' : row.status === '发送失败' ? 'danger' : 'warning'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试" width="60" />
            <el-table-column label="计划时间" width="150">
              <template #default="{ row }">{{ fmt(row.scheduledTime) }}</template>
            </el-table-column>
            <el-table-column prop="failReason" label="失败原因" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === '发送失败'" size="small" link type="primary" @click="resend(row)">重发</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :total="total" layout="total, prev, pager, next" size="small" style="margin-top:10px; justify-content:flex-end" @current-change="loadRecords" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="tplVisible" :title="tplForm.id ? '编辑模板' : '新增模板'" width="520px" destroy-on-close>
      <el-form :model="tplForm" label-width="90px" size="small">
        <el-form-item label="编码" required><el-input v-model="tplForm.tplCode" :disabled="!!tplForm.id" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="tplForm.tplName" /></el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="tplForm.msgType" style="width:160px">
            <el-option label="通知" value="通知" />
            <el-option label="预警" value="预警" />
          </el-select>
        </el-form-item>
        <el-form-item label="渠道"><el-input v-model="tplForm.channels" placeholder="站内信,短信,邮件（逗号分隔）" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="tplForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="夜间静默"><el-switch v-model="tplForm.nightSilent" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="tplForm.status" style="width:160px">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="tplVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="saveTpl">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sendVisible" title="发送消息" width="480px" destroy-on-close>
      <el-form :model="sendForm" label-width="90px" size="small">
        <el-form-item label="模板"><el-input v-model="sendForm.tplCode" disabled /></el-form-item>
        <el-form-item label="接收人" required><el-input v-model="sendForm.receiver" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="sendForm.title" /></el-form-item>
        <el-form-item label="内容" required><el-input v-model="sendForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="模拟失败"><el-switch v-model="sendForm.failDemo" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="sendVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="doSend">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/system/message'
const activeTab = ref('template')
const templates = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const q = reactive({ status: '', msgType: '', receiverLike: '', pageNum: 1, pageSize: 10 })

const tplVisible = ref(false)
const sendVisible = ref(false)
const tplEmpty = { id: null, tplCode: '', tplName: '', msgType: '通知', channels: '站内信', content: '', nightSilent: 0, status: '启用' }
const tplForm = reactive({ ...tplEmpty })
const sendForm = reactive({ tplCode: '', receiver: '', title: '', content: '', failDemo: false })

const fmt = t => (t || '').replace('T', ' ').slice(0, 19) || '—'

async function loadTemplates() {
  loading.value = true
  try { const res = await axios.get(`${api}/template/list`); templates.value = res.data.data || [] } finally { loading.value = false }
}
async function loadRecords() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/record/list`, { params: { ...q, status: q.status || undefined, msgType: q.msgType || undefined, receiverLike: q.receiverLike || undefined } })
    records.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } finally { loading.value = false }
}
watch(activeTab, v => { if (v === 'template') loadTemplates(); else loadRecords() })

function openTplEdit(row) { Object.assign(tplForm, tplEmpty, row || {}); tplVisible.value = true }
async function saveTpl() {
  saving.value = true
  try {
    if (tplForm.id) { await axios.put(`${api}/template/${tplForm.id}`, tplForm); ElMessage.success('已保存') }
    else { await axios.post(`${api}/template`, tplForm); ElMessage.success('已创建') }
    tplVisible.value = false; loadTemplates()
  } catch {} finally { saving.value = false }
}
async function delTpl(row) {
  await axios.delete(`${api}/template/${row.id}`)
  ElMessage.success('已删除'); loadTemplates()
}
function openSend(row) { Object.assign(sendForm, { tplCode: row.tplCode, receiver: '', title: '', content: row.content || '', failDemo: false }); sendVisible.value = true }
async function doSend() {
  saving.value = true
  try {
    const res = await axios.post(`${api}/send`, { ...sendForm })
    if (res.data.data.nightSilent) ElMessage.info('夜间静默生效，消息已延至次日7点发送')
    else ElMessage.success('已发送')
    sendVisible.value = false; loadRecords()
  } catch {} finally { saving.value = false }
}
async function resend(row) {
  const res = await axios.post(`${api}/${row.id}/resend`)
  if (res.data.data.warn) ElMessage.warning(res.data.data.warn)
  else ElMessage.success('已重发')
  loadRecords()
}
onMounted(loadTemplates)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
</style>

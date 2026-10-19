<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">情感分值-1~1 · 热度指数(加权+时间衰减) · 关联度评分 · 事件聚类</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="舆情列表" name="coll">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="f1.sourcePlatform" placeholder="全部信源" clearable size="small" style="width:150px" @change="loadColl">
              <el-option v-for="t in srcTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="f1.status" placeholder="全部状态" clearable size="small" style="width:110px" @change="loadColl">
              <el-option v-for="s in ['待分析','已分析','已预警']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="f1.keyword" placeholder="标题/关键词" size="small" style="width:160px" clearable @keyup.enter="loadColl" />
            <el-button size="small" @click="loadColl">查询</el-button>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="openColl()">登记舆情</el-button>
          </div>
          <el-table :data="colls" size="small" border v-loading="loading" @selection-change="selColl = $event">
            <el-table-column type="selection" width="40" />
            <el-table-column prop="title" label="舆情标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="sourcePlatform" label="信源" width="100" />
            <el-table-column prop="sentimentHint" label="情感预判" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="sentType(row.sentimentHint)">{{ row.sentimentHint }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="互动量(转/评/赞/阅)" width="150">
              <template #default="{ row }">{{ row.fwdCount }}/{{ row.cmtCount }}/{{ row.likeCount }}/{{ row.readCount }}</template>
            </el-table-column>
            <el-table-column prop="clusterId" label="聚类" width="120" show-overflow-tooltip />
            <el-table-column prop="collectTime" label="采集时间" width="160" />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="showColl(row)">详情</el-button>
                <el-button v-if="can('R02','R14') && row.status==='待分析'" size="small" link type="success" @click="analyze(row)">自动分析</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="聚类分析" name="ana">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="f2.sentiment" placeholder="全部情感" clearable size="small" style="width:110px" @change="loadAna">
              <el-option v-for="s in ['正面','负面','中性']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-select v-model="f2.heatLevel" placeholder="全部热度" clearable size="small" style="width:110px" @change="loadAna">
              <el-option v-for="s in ['高','中','低']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="f2.keyword" placeholder="标题" size="small" style="width:150px" clearable @keyup.enter="loadAna" />
            <el-button size="small" @click="loadAna">查询</el-button>
            <el-button v-if="can('R02','R14') && selAna.length" size="small" type="warning" @click="batchMark('属实')">批量属实</el-button>
            <el-button v-if="can('R02','R14') && selAna.length" size="small" @click="batchMark('不属实')">批量不属实</el-button>
            <el-button v-if="can('R02','R14') && selAna.length" size="small" @click="batchMark('待核实')">批量待核实</el-button>
          </div>
          <el-table :data="anas" size="small" border v-loading="loading" @selection-change="selAna = $event">
            <el-table-column type="selection" width="40" />
            <el-table-column prop="title" label="舆情标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="情感(分值)" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="sentType(row.sentiment)">{{ row.sentiment }} {{ row.sentimentScore }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="热度(指数/级)" width="120">
              <template #default="{ row }">{{ row.heatValue }} / {{ row.heatLevel }}</template>
            </el-table-column>
            <el-table-column label="关联度" width="110">
              <template #default="{ row }">{{ row.relevanceScore }}分 / {{ row.relevanceLevel }}</template>
            </el-table-column>
            <el-table-column prop="eventCluster" label="事件聚类" width="130" show-overflow-tooltip />
            <el-table-column prop="markResult" label="标注" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.markResult==='属实'?'success':(row.markResult==='不属实'?'danger':'info')">{{ row.markResult }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="showAna(row)">详情</el-button>
                <el-button v-if="can('R02','R14')" size="small" link type="warning" @click="openRejudge(row)">改判</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="collVisible" title="登记舆情" width="560px">
      <el-form :model="collForm" label-width="90px">
        <el-form-item label="标题" required><el-input v-model="collForm.title" /></el-form-item>
        <el-form-item label="信源平台">
          <el-select v-model="collForm.sourcePlatform" style="width:100%">
            <el-option v-for="t in srcTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="原文链接"><el-input v-model="collForm.url" placeholder="URL将按指纹MD5去重" /></el-form-item>
        <el-form-item label="发布者"><el-input v-model="collForm.author" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="collForm.keywords" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="collForm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="重大事件"><el-switch v-model="collForm.majorEvent" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collVisible=false">取消</el-button>
        <el-button type="primary" @click="saveColl">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejudgeVisible" title="人工改判情感（理由必填）" width="460px">
      <el-form label-width="80px">
        <el-form-item label="改判为">
          <el-select v-model="rejudgeForm.sentiment" style="width:100%">
            <el-option v-for="s in ['正面','负面','中性']" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="理由" required><el-input v-model="rejudgeForm.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejudgeVisible=false">取消</el-button>
        <el-button type="primary" @click="doRejudge">提交改判</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="舆情详情" size="45%">
      <template v-if="detail">
        <template v-if="detail.kind === 'coll'">
          <h4>{{ detail.row.title }}</h4>
          <p>原文链接：<a :href="detail.row.url" target="_blank">{{ detail.row.url }}</a></p>
          <p>信源：{{ detail.row.sourcePlatform }}　发布者：{{ detail.row.author }}　地区：{{ detail.row.region }}</p>
          <p>关键词：{{ detail.row.keywords }}</p>
          <p>传播路径：采集({{ detail.row.collectTime }}) → 转发{{ detail.row.fwdCount }} → 评论{{ detail.row.cmtCount }} → 点赞{{ detail.row.likeCount }} → 阅读{{ detail.row.readCount }}</p>
          <p>发布时间线：采集于 {{ detail.row.collectTime }}，状态 {{ detail.row.status }}</p>
          <p>{{ detail.row.content }}</p>
        </template>
        <template v-else>
          <h4>{{ detail.row.title }}</h4>
          <p>情感：{{ detail.row.sentiment }}（分值 {{ detail.row.sentimentScore }}，阈值 &lt; -0.2 判负面）</p>
          <p>热度指数：{{ detail.row.heatValue }}（等级 {{ detail.row.heatLevel }}）</p>
          <p>关联度：{{ detail.row.relevanceScore }} 分（{{ detail.row.relevanceLevel }}）</p>
          <p>事件聚类：{{ detail.row.eventCluster }}</p>
          <p>分析人：{{ detail.row.analyst }}　时间：{{ detail.row.analyzeTime }}</p>
          <p v-if="detail.row.manualSentiment">人工改判：{{ detail.row.manualSentiment }}（{{ detail.row.judgeReason }}）</p>
          <p>{{ detail.row.summary }}</p>
        </template>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/opinion'
const srcTypes = ['新闻网站','论坛','微博','微信','短视频平台','本地媒体']

const tab = ref('coll')
const loading = ref(false)
const colls = ref([]); const anas = ref([])
const f1 = reactive({ sourcePlatform: null, status: null, keyword: '' })
const f2 = reactive({ sentiment: null, heatLevel: null, keyword: '' })
const selColl = ref([]); const selAna = ref([])
const collVisible = ref(false); const collForm = ref({})
const rejudgeVisible = ref(false); const rejudgeForm = ref({ id: null, sentiment: '中性', reason: '' })
const detailVisible = ref(false); const detail = ref(null)

function sentType(s) { return { 负面: 'danger', 正面: 'success', 中性: 'info' }[s] || 'info' }

async function loadColl() {
  const res = await axios.get(`${API}/collection/list`, { params: { ...f1, keyword: f1.keyword || undefined } })
  if (res.data.code === 200) colls.value = res.data.data
}
async function loadAna() {
  const res = await axios.get(`${API}/analysis/list`, { params: { ...f2, keyword: f2.keyword || undefined } })
  if (res.data.code === 200) anas.value = res.data.data
}
function load() {
  loading.value = true
  Promise.resolve(tab.value === 'coll' ? loadColl() : loadAna()).finally(() => { loading.value = false })
}

function openColl() {
  collForm.value = { title: '', sourcePlatform: '论坛', url: '', author: '', keywords: '', content: '', majorEvent: 0 }
  collVisible.value = true
}
async function saveColl() {
  try {
    await axios.post(`${API}/collection`, collForm.value)
    ElMessage.success('已登记'); collVisible.value = false; loadColl()
  } catch {}
}
async function analyze(row) {
  try {
    const res = await axios.post(`${API}/analysis/analyze/${row.id}`)
    if (res.data.code === 200) {
      const d = res.data.data
      ElMessage.success(`分析完成：${d.sentiment}(${d.sentimentScore}) 热度${d.heatValue}/${d.heatLevel} 关联${d.relevanceLevel} 聚类${d.eventCluster}`)
      loadColl()
    }
  } catch {}
}
function openRejudge(row) {
  rejudgeForm.value = { id: row.id, sentiment: row.sentiment, reason: '' }
  rejudgeVisible.value = true
}
async function doRejudge() {
  if (!rejudgeForm.value.reason) { ElMessage.warning('改判理由必填'); return }
  try {
    await axios.post(`${API}/analysis/${rejudgeForm.value.id}/rejudge`, null,
      { params: { sentiment: rejudgeForm.value.sentiment, reason: rejudgeForm.value.reason } })
    ElMessage.success('已改判'); rejudgeVisible.value = false; loadAna()
  } catch {}
}
async function batchMark(mark) {
  try {
    await axios.post(`${API}/analysis/mark`, { ids: selAna.value.map(r => r.id), markResult: mark })
    ElMessage.success(`已批量标注为「${mark}」`); loadAna()
  } catch {}
}
function showColl(row) { detail.value = { kind: 'coll', row }; detailVisible.value = true }
function showAna(row) { detail.value = { kind: 'ana', row }; detailVisible.value = true }
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
</style>

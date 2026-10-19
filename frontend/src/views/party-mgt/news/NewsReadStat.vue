<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">阅读排行 · 渠道分布 · 防刷去重</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane label="阅读排行榜" name="ranking">
          <div class="bar">
            <el-date-picker v-model="range" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" @change="loadRanking" />
            <el-input-number v-model="limit" :min="1" :max="100" size="small" placeholder="TOP N" />
            <el-button size="small" @click="loadRanking">查询</el-button>
          </div>
          <el-table :data="ranking" size="small" border>
            <el-table-column label="排名" width="70">
              <template #default="{ $index }">
                <el-tag size="small" :type="$index < 3 ? 'warning' : 'info'">{{ $index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="articleTitle" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="pv" label="PV" width="90" sortable />
            <el-table-column prop="uv" label="UV" width="90" />
            <el-table-column prop="likeCount" label="点赞" width="80" />
            <el-table-column prop="commentCount" label="评论" width="80" />
            <el-table-column prop="shareCount" label="转发" width="80" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="渠道分布" name="channel">
          <div class="bar">
            <el-date-picker v-model="channelDate" type="date" placeholder="选择日期" size="small" />
            <el-button size="small" @click="loadChannel">查询</el-button>
          </div>
          <el-row :gutter="16">
            <el-col :span="8" v-for="(pv, ch) in channels" :key="ch">
              <el-card shadow="hover" class="channel-card">
                <div class="ch-name">{{ ch }}</div>
                <div class="ch-pv">{{ pv }}</div>
                <div class="ch-label">PV</div>
              </el-card>
            </el-col>
          </el-row>
          <el-descriptions v-if="channelData.totalPv != null" :column="1" border size="small" style="margin-top:12px">
            <el-descriptions-item label="日期">{{ channelData.date }}</el-descriptions-item>
            <el-descriptions-item label="总PV">{{ channelData.totalPv }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import RoleBar from '../PartyRoleBar.vue'

const API = '/api/party/news/read-stat'
const tab = ref('ranking')
const ranking = ref([])
const range = ref([])
const limit = ref(20)
const channelDate = ref(new Date())
const channelData = reactive({})
const channels = ref({})

async function loadRanking() {
  const params = { limit: limit.value }
  if (range.value && range.value.length === 2) {
    params.startDate = fmt(range.value[0])
    params.endDate = fmt(range.value[1])
  }
  const res = await axios.get(`${API}/ranking`, { params })
  if (res.data.code === 200) ranking.value = res.data.data
}
async function loadChannel() {
  if (!channelDate.value) return
  const res = await axios.get(`${API}/hourly`, { params: { date: fmt(channelDate.value) } })
  if (res.data.code === 200) {
    Object.assign(channelData, res.data.data)
    channels.value = res.data.data.channels || {}
  }
}
function fmt(d) {
  if (!d) return null
  const dt = new Date(d)
  return `${dt.getFullYear()}-${String(dt.getMonth()+1).padStart(2,'0')}-${String(dt.getDate()).padStart(2,'0')}`
}
onMounted(() => { loadRanking(); loadChannel() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.channel-card { text-align: center; }
.ch-name { font-size: 16px; font-weight: 600; color: #303133; }
.ch-pv { font-size: 32px; font-weight: 700; color: #409eff; margin: 8px 0; }
.ch-label { color: #909399; font-size: 13px; }
</style>

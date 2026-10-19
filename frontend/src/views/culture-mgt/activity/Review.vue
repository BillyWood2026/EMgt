<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">活动复盘：结束后归档素材（照片/视频/纪要≥1个）并填写总结（≥10字）方可闭环；闭环后前台可见成效</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="待复盘" name="待复盘" />
        <el-tab-pane label="已闭环" name="已闭环" />
      </el-tabs>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="500">
        <el-table-column prop="actName" label="活动名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="actType" label="类型" width="110" />
        <el-table-column prop="organizerDept" label="主办部门" width="150" show-overflow-tooltip />
        <el-table-column label="活动时间" width="150">
          <template #default="{row}">{{ fmt(row.planStart) }}</template>
        </el-table-column>
        <el-table-column label="签到/应参与" width="100" align="center">
          <template #default="{row}">{{ row.checkedCount }}/{{ row.shouldCount }}</template>
        </el-table-column>
        <el-table-column label="参与率" width="90" align="center">
          <template #default="{row}"><span class="rate">{{ partRate(row) }}%</span></template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status==='待复盘' && can('C02','C03','C99')" size="small" type="primary" @click="openClose(row)">复盘闭环</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="closeVis" title="复盘闭环" width="640px">
      <div class="tip mb">{{ closeRow.actName }}</div>
      <el-form label-width="90px" size="small">
        <el-form-item label="复盘总结" required>
          <el-input v-model="closeForm.summary" type="textarea" :rows="4" maxlength="2000" show-word-limit placeholder="不少于10字：组织情况、亮点、不足" />
        </el-form-item>
        <el-form-item label="成效评估">
          <el-input v-model="closeForm.resultEval" type="textarea" :rows="2" maxlength="500" placeholder="参与率、合格率、满意度等量化结论" />
        </el-form-item>
        <el-form-item label="归档素材" required>
          <div class="att-box">
            <div v-for="(f,i) in fileArr" :key="i" class="att-row">
              <el-input v-model="fileArr[i]" size="small" placeholder="素材地址（照片/视频/纪要，至少1个）" />
              <el-button size="small" link type="danger" @click="fileArr.splice(i,1)">删除</el-button>
            </div>
            <el-button size="small" @click="fileArr.push('')">+ 添加素材</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="closeVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doClose">确认闭环</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVis" size="500px" :title="cur.actName || '活动详情'">
      <div v-if="cur.id">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="状态">{{ cur.status }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ cur.actType }}</el-descriptions-item>
          <el-descriptions-item label="签到/应参与">{{ cur.checkedCount }}/{{ cur.shouldCount }}</el-descriptions-item>
          <el-descriptions-item label="参与率">{{ partRate(cur) }}%</el-descriptions-item>
        </el-descriptions>
        <div class="sec-title">简介</div>
        <div class="d-content">{{ cur.intro }}</div>
        <template v-if="cur.summary">
          <div class="sec-title">复盘总结</div>
          <div class="d-content">{{ cur.summary }}</div>
          <div class="sec-title">成效评估</div>
          <div class="d-content">{{ cur.resultEval || '—' }}</div>
          <div class="sec-title">归档素材</div>
          <div v-for="(f,i) in fileList(cur.archiveFiles)" :key="i" class="att">{{ f }}</div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const rows = ref([])
const loading = ref(false)
const tab = ref('待复盘')
const closeVis = ref(false)
const detailVis = ref(false)
const cur = ref({})
const closeRow = ref({})
const closeForm = reactive({ summary: '', resultEval: '' })
const fileArr = ref([])

function fmt(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function fileList(json) { try { const v = JSON.parse(json || '[]'); return Array.isArray(v) ? v : [] } catch { return [] } }
function partRate(row) {
  if (!row.shouldCount) return 0
  return Math.round((row.checkedCount || 0) / row.shouldCount * 1000) / 10
}
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/activity/list', { params: { status: tab.value } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openDetail(row) { cur.value = row; detailVis.value = true }
function openClose(row) {
  closeRow.value = row
  closeForm.summary = row.summary || ''
  closeForm.resultEval = row.resultEval || ''
  fileArr.value = fileList(row.archiveFiles)
  if (!fileArr.value.length) fileArr.value = ['']
  closeVis.value = true
}
async function doClose() {
  if ((closeForm.summary || '').trim().length < 10) return ElMessage.warning('复盘总结不少于10字')
  const files = fileArr.value.map(s => s.trim()).filter(Boolean)
  if (!files.length) return ElMessage.warning('至少归档1个素材')
  await axios.post(`/api/culture/activity/${closeRow.value.id}/close`, {
    summary: closeForm.summary, resultEval: closeForm.resultEval, archiveFiles: JSON.stringify(files)
  })
  ElMessage.success('复盘闭环完成')
  closeVis.value = false
  load()
}
onMounted(load)
</script>

<style scoped>
.mb { margin-bottom:10px; }
.tip { color:#909399; font-size:12px; }
.rate { font-weight:700; color:var(--el-color-success); }
.att-box { width:100%; }
.att-row { display:flex; gap:8px; margin-bottom:6px; }
.sec-title { font-weight:600; margin:14px 0 8px; }
.d-content { white-space:pre-wrap; line-height:1.8; background:#f7f9fc; padding:10px; border-radius:6px; }
.att { font-size:13px; color:var(--el-color-primary); padding:2px 0; }
</style>

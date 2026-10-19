<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">日报/周报/月报自动生成 · 闭环率与环比 · 数据缺失标注说明</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="舆情报告" name="rep">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="f.reportType" placeholder="全部类型" clearable size="small" style="width:110px" @change="load">
              <el-option v-for="t in ['日报','周报','月报','专报']" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="f.status" placeholder="全部状态" clearable size="small" style="width:110px" @change="load">
              <el-option v-for="s in ['草稿','已提交','已发布']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="gen('日报')">生成日报</el-button>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="gen('周报')">生成周报</el-button>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="gen('月报')">生成月报</el-button>
          </div>
          <el-table :data="list" size="small" border v-loading="loading">
            <el-table-column prop="reportNo" label="报告编号" width="170" />
            <el-table-column prop="title" label="报告标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="reportType" label="类型" width="80" />
            <el-table-column prop="periodStart" label="统计开始" width="110" />
            <el-table-column prop="periodEnd" label="统计结束" width="110" />
            <el-table-column prop="totalCount" label="总量" width="70" />
            <el-table-column prop="negativeCount" label="负面" width="70">
              <template #default="{ row }"><span :class="{ neg: row.negativeCount > 0 }">{{ row.negativeCount }}</span></template>
            </el-table-column>
            <el-table-column label="闭环率" width="90">
              <template #default="{ row }">{{ row.closeRate == null ? '-' : row.closeRate + '%' }}</template>
            </el-table-column>
            <el-table-column label="环比" width="90">
              <template #default="{ row }">
                <span v-if="row.momChange == null">-</span>
                <span v-else :class="row.momChange > 0 ? 'neg' : 'green'">{{ row.momChange > 0 ? '+' : '' }}{{ row.momChange }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status==='已发布' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="show(row)">详情</el-button>
                <el-button size="small" link type="primary" @click="exportRow(row)">导出</el-button>
                <el-button v-if="can('R02','R03') && row.status!=='已发布'" size="small" link type="success" @click="publish(row)">发布</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="del(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="报告模板配置" name="tpl">
        <el-card shadow="never">
          <div class="bar">
            <el-button v-if="can('R02')" size="small" type="primary" @click="editTpl()">新增模板</el-button>
          </div>
          <el-table :data="templates" size="small" border v-loading="loading">
            <el-table-column prop="name" label="模板名称" min-width="140" />
            <el-table-column prop="reportType" label="类型" width="90" />
            <el-table-column prop="sections" label="章节配置" min-width="300" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link @click="editTpl(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="delTpl(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="tplVisible" :title="tpl.id ? '编辑模板' : '新增模板'" width="520px">
      <el-form :model="tpl" label-width="90px">
        <el-form-item label="模板名称" required><el-input v-model="tpl.name" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="tpl.reportType" style="width:100%">
            <el-option v-for="t in ['日报','周报','月报','专报']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="章节配置"><el-input v-model="tpl.sections" type="textarea" :rows="3" placeholder="逗号分隔章节" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tplVisible=false">取消</el-button>
        <el-button type="primary" @click="saveTpl">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="报告详情" size="46%">
      <template v-if="cur">
        <h4>{{ cur.title }}（{{ cur.reportNo }}）</h4>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="统计周期">{{ cur.periodStart }} 至 {{ cur.periodEnd }}</el-descriptions-item>
          <el-descriptions-item label="报告人">{{ cur.reporter }}</el-descriptions-item>
          <el-descriptions-item label="舆情总量">{{ cur.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="负面量">{{ cur.negativeCount }}</el-descriptions-item>
          <el-descriptions-item label="应处置">{{ cur.dueCount }}</el-descriptions-item>
          <el-descriptions-item label="已归档">{{ cur.closedCount }}</el-descriptions-item>
          <el-descriptions-item label="处置闭环率">{{ cur.closeRate == null ? '-' : cur.closeRate + '%' }}</el-descriptions-item>
          <el-descriptions-item label="环比变化">{{ cur.momChange == null ? '-' : cur.momChange + '%' }}</el-descriptions-item>
        </el-descriptions>
        <el-alert v-if="cur.dataNote" type="warning" :closable="false" :title="'数据源说明：' + cur.dataNote" style="margin-top:10px" />
        <h5>热点TOP10</h5>
        <el-table :data="topList" size="small" border>
          <el-table-column prop="rank" label="排名" width="70" />
          <el-table-column prop="title" label="舆情标题" min-width="240" show-overflow-tooltip />
          <el-table-column prop="heat" label="热度" width="90" />
        </el-table>
        <h5>正文</h5>
        <p>{{ cur.content }}</p>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/opinion/report'

const tab = ref('rep')
const loading = ref(false)
const list = ref([]); const templates = ref([])
const f = reactive({ reportType: null, status: null })
const tplVisible = ref(false); const tpl = ref({})
const detailVisible = ref(false); const cur = ref(null)

const topList = computed(() => {
  if (!cur.value || !cur.value.top10) return []
  return cur.value.top10.split(';').map(s => {
    const m = s.match(/^(\d+)\.(.+)\(热度(.+?)\)$/)
    return m ? { rank: m[1], title: m[2], heat: m[3] } : { rank: '-', title: s, heat: '-' }
  })
})

async function load() {
  loading.value = true
  try {
    if (tab.value === 'rep') {
      const res = await axios.get(`${API}/list`, { params: { ...f } })
      if (res.data.code === 200) list.value = res.data.data
    } else {
      const res = await axios.get(`${API}/template/list`)
      if (res.data.code === 200) templates.value = res.data.data
    }
  } finally { loading.value = false }
}

async function gen(type) {
  try {
    const res = await axios.post(`${API}/generate`, null, { params: { reportType: type } })
    if (res.data.code === 200) {
      ElMessage.success(`${type}已自动生成（草稿）`)
      if (res.data.data.dataNote) ElMessage.warning('存在采集失败/限流时段，报告已标注数据源说明')
      load()
    }
  } catch {}
}
function show(row) { cur.value = row; detailVisible.value = true }
async function publish(row) {
  try { await axios.post(`${API}/${row.id}/publish`); ElMessage.success('已发布'); load() } catch {}
}
async function del(row) {
  try { await axios.delete(`${API}/${row.id}`); ElMessage.success('已删除'); load() } catch {}
}
function exportRow(row) {
  const lines = [
    row.title, `编号：${row.reportNo}　类型：${row.reportType}　周期：${row.periodStart}至${row.periodEnd}`,
    `总量：${row.totalCount}　负面：${row.negativeCount}　应处置：${row.dueCount}　已归档：${row.closedCount}　闭环率：${row.closeRate}%　环比：${row.momChange == null ? '-' : row.momChange + '%'}`,
    `热点TOP10：${row.top10 || '无'}`,
    row.dataNote ? `数据源说明：${row.dataNote}` : '',
    '', row.content
  ].filter(Boolean)
  const blob = new Blob([lines.join('\n')], { type: 'application/msword' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${row.reportNo}.doc`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('已导出Word')
}
function editTpl(row) {
  tpl.value = row ? { ...row } : { name: '', reportType: '日报', sections: '总量与负面量,热点TOP10,预警情况,处置进展', status: '启用' }
  tplVisible.value = true
}
async function saveTpl() {
  try { await axios.post(`${API}/template`, tpl.value); ElMessage.success('已保存'); tplVisible.value = false; load() } catch {}
}
async function delTpl(row) {
  try { await axios.delete(`${API}/template/${row.id}`); ElMessage.success('已删除'); load() } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.neg { color: #f56c6c; font-weight: 600; }
.green { color: #67c23a; }
</style>

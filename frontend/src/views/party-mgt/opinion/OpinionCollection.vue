<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">指纹去重 · 失败重试3次 · 配额限流 · 命中风暴检测</el-tag>
    </RoleBar>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane label="关键词库" name="kw">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="kwGroup" placeholder="全部分组" clearable size="small" style="width:140px" @change="loadKw">
              <el-option v-for="g in ['企业','品牌','人物','业务']" :key="g" :label="g" :value="g" />
            </el-select>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="editKw()">新增关键词</el-button>
          </div>
          <el-table :data="kws" size="small" border v-loading="loading">
            <el-table-column prop="name" label="关键词" min-width="140" />
            <el-table-column prop="groupName" label="分组" width="90" />
            <el-table-column prop="weight" label="权重" width="70" />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R14')" size="small" link @click="editKw(row)">编辑</el-button>
                <el-button v-if="can('R02','R14')" size="small" link type="danger" @click="del(apiKw, row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="信源配置" name="src">
        <el-card shadow="never">
          <div class="bar">
            <el-select v-model="srcType" placeholder="全部类型" clearable size="small" style="width:160px" @change="loadSrc">
              <el-option v-for="t in srcTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="editSrc()">新增信源</el-button>
          </div>
          <el-table :data="srcs" size="small" border v-loading="loading">
            <el-table-column prop="name" label="信源名称" min-width="120" />
            <el-table-column prop="type" label="类型" width="110" />
            <el-table-column prop="url" label="地址" min-width="180" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R14')" size="small" link @click="editSrc(row)">编辑</el-button>
                <el-button v-if="can('R02','R14')" size="small" link type="danger" @click="del(apiSrc, row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="采集任务" name="task">
        <el-card shadow="never">
          <div class="bar">
            <el-button v-if="can('R02','R14')" size="small" type="primary" @click="editTask()">新建任务</el-button>
            <span class="hint">算法：按关键词AND/OR组合匹配 · URL指纹MD5去重 · 失败重试3次告警 · 配额超限限流 · 1小时命中>50触发风暴限流合并</span>
          </div>
          <el-table :data="tasks" size="small" border v-loading="loading">
            <el-table-column prop="name" label="任务名称" min-width="130" />
            <el-table-column prop="keywordGroup" label="关键词组" min-width="150" show-overflow-tooltip />
            <el-table-column prop="sources" label="信源" min-width="140" show-overflow-tooltip />
            <el-table-column prop="matchMode" label="匹配" width="70" />
            <el-table-column prop="frequency" label="频率" width="90" />
            <el-table-column prop="quota" label="配额" width="70" />
            <el-table-column prop="collectCount" label="累计采集" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status==='运行中'?'success':(row.status==='异常'?'danger':'info')">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R14')" size="small" link type="success" @click="runTask(row)">执行采集</el-button>
                <el-button v-if="can('R02','R14')" size="small" link type="warning" @click="runTask(row, true)">模拟失败</el-button>
                <el-button v-if="can('R02','R14')" size="small" link @click="editTask(row)">编辑</el-button>
                <el-button v-if="can('R02','R14')" size="small" link type="danger" @click="del(apiTask, row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="采集日志" name="log">
        <el-card shadow="never">
          <el-table :data="logs" size="small" border v-loading="loading">
            <el-table-column prop="taskName" label="任务" min-width="120" />
            <el-table-column prop="collectTime" label="采集时间" width="170" />
            <el-table-column prop="hitCount" label="命中" width="70" />
            <el-table-column prop="newCount" label="新增" width="70" />
            <el-table-column prop="retryCount" label="重试" width="70" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status==='成功'?'success':(row.status==='失败'?'danger':'warning')">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="formVisible" :title="formTitle" width="520px">
      <el-form :model="form" label-width="90px">
        <template v-if="formType==='kw'">
          <el-form-item label="关键词" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="分组">
            <el-select v-model="form.groupName" style="width:100%">
              <el-option v-for="g in ['企业','品牌','人物','业务']" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>
          <el-form-item label="权重(1-10)"><el-input-number v-model="form.weight" :min="1" :max="10" /></el-form-item>
        </template>
        <template v-else-if="formType==='src'">
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="类型" required>
            <el-select v-model="form.type" style="width:100%">
              <el-option v-for="t in srcTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="地址"><el-input v-model="form.url" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="任务名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="关键词组"><el-input v-model="form.keywordGroup" placeholder="逗号分隔" /></el-form-item>
          <el-form-item label="信源"><el-input v-model="form.sources" placeholder="逗号分隔" /></el-form-item>
          <el-form-item label="匹配模式">
            <el-select v-model="form.matchMode" style="width:100%">
              <el-option label="OR 任一命中" value="OR" /><el-option label="AND 全部命中" value="AND" />
            </el-select>
          </el-form-item>
          <el-form-item label="频率">
            <el-select v-model="form.frequency" style="width:100%">
              <el-option v-for="f in ['每30分钟','每小时','每天']" :key="f" :label="f" :value="f" />
            </el-select>
          </el-form-item>
          <el-form-item label="单次配额"><el-input-number v-model="form.quota" :min="1" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const apiKw = '/api/party/opinion/keyword'
const apiSrc = '/api/party/opinion/source'
const apiTask = '/api/party/opinion/task'
const srcTypes = ['新闻网站','论坛','微博','微信','短视频平台','本地媒体']

const tab = ref('kw')
const loading = ref(false)
const kws = ref([]); const srcs = ref([]); const tasks = ref([]); const logs = ref([])
const kwGroup = ref(null); const srcType = ref(null)
const formVisible = ref(false); const formTitle = ref(''); const formType = ref('kw'); const form = ref({})

async function loadKw() {
  const res = await axios.get(`${apiKw}/list`, { params: { group: kwGroup.value || undefined } })
  if (res.data.code === 200) kws.value = res.data.data
}
async function loadSrc() {
  const res = await axios.get(`${apiSrc}/list`, { params: { type: srcType.value || undefined } })
  if (res.data.code === 200) srcs.value = res.data.data
}
async function loadTask() {
  const res = await axios.get(`${apiTask}/list`)
  if (res.data.code === 200) tasks.value = res.data.data
}
async function loadLog() {
  const res = await axios.get(`${apiTask}/log/list`)
  if (res.data.code === 200) logs.value = res.data.data
}
function load() {
  loading.value = true
  const fn = { kw: loadKw, src: loadSrc, task: loadTask, log: loadLog }[tab.value]
  Promise.resolve(fn()).finally(() => { loading.value = false })
}

function editKw(row) {
  formType.value = 'kw'; formTitle.value = row ? '编辑关键词' : '新增关键词'
  form.value = row ? { ...row } : { name: '', groupName: '企业', weight: 5, status: '启用' }
  formVisible.value = true
}
function editSrc(row) {
  formType.value = 'src'; formTitle.value = row ? '编辑信源' : '新增信源'
  form.value = row ? { ...row } : { name: '', type: '新闻网站', url: '', status: '启用' }
  formVisible.value = true
}
function editTask(row) {
  formType.value = 'task'; formTitle.value = row ? '编辑任务' : '新建任务'
  form.value = row ? { ...row } : { name: '', keywordGroup: '', sources: '', matchMode: 'OR', frequency: '每小时', quota: 100, status: '运行中' }
  formVisible.value = true
}
async function save() {
  const api = { kw: apiKw, src: apiSrc, task: apiTask }[formType.value]
  try {
    await axios.post(api, form.value)
    ElMessage.success('已保存'); formVisible.value = false; load()
  } catch {}
}
async function del(api, id) {
  try { await axios.delete(`${api}/${id}`); ElMessage.success('已删除'); load() } catch {}
}
async function runTask(row, fail = false) {
  try {
    const res = await axios.post(`${apiTask}/${row.id}/run`, null, { params: fail ? { fail: true } : {} })
    if (res.data.code === 200) {
      ElMessage.success(`采集完成：命中${res.data.data.hitCount}条，新增${res.data.data.newCount}条，去重合并${res.data.data.dedupCount}条`)
      loadTask(); loadLog()
    }
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.hint { font-size: 12px; color: #909399; }
</style>

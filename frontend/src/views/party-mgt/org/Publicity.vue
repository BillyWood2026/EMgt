<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">范围权限矩阵 · 敏感信息脱敏校验 · 默认期限与到期处理</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <!-- ============ 公开事项台账 ============ -->
        <el-tab-pane label="公开事项台账" name="list">
          <div class="bar">
            <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                       style="width:200px" @change="loadList">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="filter.category" placeholder="类别" clearable size="small" filterable
                       style="width:150px" @change="loadList">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
            <el-select v-model="filter.status" placeholder="状态" clearable size="small" style="width:110px" @change="loadList">
              <el-option v-for="s in ['草稿','待审核','已发布','已下架','已归档']" :key="s" :label="s" :value="s" />
            </el-select>
            <el-select v-model="filter.year" placeholder="年度" clearable size="small" style="width:100px" @change="loadList">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R04','R07')" size="small" type="primary" @click="openForm()">新建公开事项</el-button>
          </div>

          <el-table :data="list" size="small" border v-loading="loading">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="category" label="类别" width="120" show-overflow-tooltip />
            <el-table-column label="公开范围" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="scopeType(row.publicScope)">{{ row.publicScope }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="公开期限" width="200">
              <template #default="{ row }">
                <span>{{ row.periodStart || '—' }} 至 {{ row.periodEnd || '—' }}</span>
                <el-tag v-if="isExpired(row)" size="small" type="danger" class="mx">已到期</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="approver" label="批准人" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="pubStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status !== '已发布' && can('R02','R04','R07')"
                           size="small" link type="success" @click="publish(row)">发布</el-button>
                <el-button v-if="can('R02','R04','R07')" size="small" link @click="openForm(row)">编辑</el-button>
                <el-button v-if="can('R02','R04')" size="small" link type="danger" @click="remove(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 到期处理 ============ -->
        <el-tab-pane label="到期处理" name="expire">
          <div class="bar">
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="processExpired">执行到期处理</el-button>
            <el-button size="small" @click="loadExpire">刷新</el-button>
            <span class="tip">按事项设定的到期方式（自动下架/归档/续期）批量处理已过截止日的已发布事项。</span>
          </div>

          <h4 class="sec-title">已发布且到期（待处理）</h4>
          <el-table :data="dueList" size="small" border>
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="category" label="类别" width="120" />
            <el-table-column prop="periodEnd" label="截止日" width="120" />
            <el-table-column label="到期方式" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="{ 自动下架: 'info', 归档: '', 续期: 'warning' }[row.expireAction] || 'info'">
                  {{ row.expireAction || '自动下架' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <h4 class="sec-title">超期未发布提醒（草稿/待审核超过3天）</h4>
          <el-table :data="overdueList" size="small" border>
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="category" label="类别" width="120" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column label="创建时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04','R07')" size="small" link type="primary" @click="tab='list'; openForm(row)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 默认期限配置 ============ -->
        <el-tab-pane label="默认期限配置" name="config">
          <div class="bar">
            <el-button v-if="can('R02')" size="small" type="primary" @click="openConfig()">新增类别配置</el-button>
            <span class="tip">未配置的类别默认公开期限为 30 天。</span>
          </div>
          <el-table :data="configs" size="small" border v-loading="configLoading">
            <el-table-column prop="category" label="事项类别" width="160" />
            <el-table-column prop="defaultScope" label="默认范围" width="110" />
            <el-table-column prop="defaultMethods" label="默认方式" min-width="180" show-overflow-tooltip />
            <el-table-column label="默认期限" width="100">
              <template #default="{ row }">{{ row.durationDays }} 天</template>
            </el-table-column>
            <el-table-column label="按工作日" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.workdayFlag ? 'warning' : 'info'">{{ row.workdayFlag ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link @click="openConfig(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 事项表单 ============ -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑公开事项' : '新建公开事项'" width="780px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="党组织" required>
              <el-select v-model="form.orgId" filterable style="width:100%" @change="onOrg">
                <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别" required>
              <el-select v-model="form.category" filterable allow-create style="width:100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="标题" required><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="公开内容"><el-input v-model="form.content" type="textarea" :rows="5" placeholder="禁止出现未脱敏身份证号、手机号，否则发布时阻断（E-PUB-02）" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="公开范围" required>
              <el-radio-group v-model="form.publicScope">
                <el-radio value="全体党员">全体党员</el-radio>
                <el-radio value="全体职工">全体职工</el-radio>
                <el-radio value="社会公众">社会公众</el-radio>
              </el-radio-group>
              <el-tag size="small" :type="scopePublishAllowed ? 'success' : 'danger'" class="mx">
                {{ scopePublishAllowed ? '当前角色可发布此范围' : '当前角色无权发布此范围（E-PUB-04）' }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公开方式">
              <el-checkbox-group v-model="methodList">
                <el-checkbox v-for="m in methodOptions" :key="m" :value="m" :label="m">{{ m }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="发布人"><el-input v-model="form.publisher" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="审核批准人" required><el-input v-model="form.approver" placeholder="发布前必填" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="到期方式">
              <el-select v-model="form.expireAction" style="width:100%">
                <el-option label="自动下架" value="自动下架" /><el-option label="归档" value="归档" /><el-option label="续期" value="续期" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公开起始"><el-date-picker v-model="form.periodStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公开截止"><el-date-picker v-model="form.periodEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="留空按类别默认期限" /></el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="附件"><el-input v-model="form.attachments" placeholder="附件链接，多个用逗号分隔" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- ============ 配置表单 ============ -->
    <el-dialog v-model="configVisible" :title="configForm.id ? '编辑期限配置' : '新增类别期限配置'" width="560px">
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="事项类别" required>
          <el-select v-model="configForm.category" filterable allow-create style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认范围">
          <el-select v-model="configForm.defaultScope" clearable style="width:100%">
            <el-option label="全体党员" value="全体党员" /><el-option label="全体职工" value="全体职工" /><el-option label="社会公众" value="社会公众" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认方式">
          <el-input v-model="configForm.defaultMethods" placeholder="逗号分隔，如：系统公示栏,门户" />
        </el-form-item>
        <el-form-item label="默认期限" required>
          <el-input-number v-model="configForm.durationDays" :min="1" :max="3650" /> 天
        </el-form-item>
        <el-form-item label="按工作日">
          <el-switch v-model="configForm.workdayFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="configForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/org/publicity'
const ORG_API = '/api/party/org/organization'
const years = [2024, 2025, 2026, 2027]
const categories = ['党费收缴使用', '民主评议', '评优表彰', '发展党员', '组织设置', '换届选举', '党风廉政', '其他']
const methodOptions = ['系统公示栏', '门户', '公众号', '会议通报']

/** 范围权限矩阵：社会公众仅R02；全体职工R02+R04；全体党员R02+R04+R07。 */
const SCOPE_ROLES = { 社会公众: ['R02'], 全体职工: ['R02', 'R04'], 全体党员: ['R02', 'R04', 'R07'] }

const tab = ref('list')
const orgFlat = ref([])

async function loadOrgs() {
  const res = await axios.get(`${ORG_API}/tree`)
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}
function scopeType(s) {
  return { 社会公众: 'danger', 全体职工: 'warning', 全体党员: 'success' }[s] || 'info'
}
function pubStatusType(s) {
  return { 草稿: 'info', 待审核: 'warning', 已发布: 'success', 已下架: 'info', 已归档: '' }[s] || 'info'
}
function isExpired(row) {
  return row.status === '已发布' && row.periodEnd && row.periodEnd < new Date().toISOString().slice(0, 10)
}
function formatTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '—' }

// ---------- 台账 ----------
const loading = ref(false)
const list = ref([])
const filter = reactive({ orgId: null, category: '', status: '', year: null })

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, { params: {
      orgId: filter.orgId || undefined, category: filter.category || undefined,
      status: filter.status || undefined, year: filter.year || undefined
    } })
    if (res.data.code === 200) list.value = res.data.data
  } finally { loading.value = false }
}

const formVisible = ref(false)
const form = reactive({})
const methodList = ref([])
const scopePublishAllowed = computed(() => {
  const allow = SCOPE_ROLES[form.publicScope]
  return !allow ? false : can(...allow)
})

function resetForm() {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, { expireAction: '自动下架' })
  methodList.value = []
}
function onOrg(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) form.orgName = o.name
}
function openForm(row) {
  resetForm()
  if (row) {
    Object.assign(form, JSON.parse(JSON.stringify(row)))
    try { methodList.value = JSON.parse(row.methods || '[]') } catch { methodList.value = [] }
  }
  formVisible.value = true
}
async function submitForm() {
  if (!form.orgId) { ElMessage.error('请选择党组织'); return }
  if (!form.title || !form.category) { ElMessage.error('标题与类别必填（E-PUB-01）'); return }
  form.methods = JSON.stringify(methodList.value)
  try {
    await axios(form.id ? { method: 'put', url: API, data: form }
                      : { method: 'post', url: API, data: form })
    ElMessage.success('已保存')
    formVisible.value = false
    loadList()
  } catch { /* 拦截器提示 */ }
}
async function publish(row) {
  try {
    await ElMessageBox.confirm(
      `发布《${row.title}》？系统将校验公开范围权限（${row.publicScope || '未选择'}）、敏感信息脱敏、审核批准人，并按类别默认期限补齐公开截止日。`,
      '发布确认', { type: 'warning', confirmButtonText: '确认发布' })
  } catch { return }
  try {
    const res = await axios.post(`${API}/publish/${row.id}`)
    const d = res.data.data || {}
    ElMessage.success(`已发布，公开至 ${d.periodEnd || ''}`)
    loadList()
  } catch { /* 拦截器提示 E-PUB-02/03/04 */ }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除《${row.title}》？`, '删除', { type: 'warning' })
    await axios.delete(`${API}/${row.id}`)
    ElMessage.success('已删除')
    loadList()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 到期处理 ----------
const dueList = ref([])
const overdueList = ref([])
async function loadExpire() {
  const [pubRes, odRes] = await Promise.all([
    axios.get(`${API}/list`, { params: { status: '已发布' } }),
    axios.get(`${API}/overdue`)
  ])
  if (pubRes.data.code === 200) {
    const today = new Date().toISOString().slice(0, 10)
    dueList.value = pubRes.data.data.filter(r => r.periodEnd && r.periodEnd < today)
  }
  if (odRes.data.code === 200) overdueList.value = odRes.data.data
}
async function processExpired() {
  try {
    const res = await axios.post(`${API}/expire/process`)
    const d = res.data.data || {}
    ElMessage.success(`处理完成：共 ${d.total || 0} 项，下架 ${d.takenOff || 0}，归档 ${d.archived || 0}，续期 ${d.renewed || 0}`)
    loadExpire()
    if (tab.value === 'list') loadList()
  } catch { /* 拦截器提示 */ }
}

// ---------- 配置 ----------
const configLoading = ref(false)
const configs = ref([])
async function loadConfigs() {
  configLoading.value = true
  try {
    const res = await axios.get(`${API}/config/list`)
    if (res.data.code === 200) configs.value = res.data.data
  } finally { configLoading.value = false }
}
const configVisible = ref(false)
const configForm = reactive({})
function openConfig(row) {
  Object.keys(configForm).forEach(k => delete configForm[k])
  if (row) Object.assign(configForm, JSON.parse(JSON.stringify(row)))
  else Object.assign(configForm, { durationDays: 30, workdayFlag: 0 })
  configVisible.value = true
}
async function submitConfig() {
  if (!configForm.category) { ElMessage.error('事项类别必填'); return }
  if (!configForm.durationDays || configForm.durationDays < 1) { ElMessage.error('默认公开期限必须≥1天'); return }
  try {
    await axios(configForm.id ? { method: 'put', url: `${API}/config`, data: configForm }
                          : { method: 'post', url: `${API}/config`, data: configForm })
    ElMessage.success('配置已保存')
    configVisible.value = false
    loadConfigs()
  } catch { /* 拦截器提示 */ }
}

onMounted(() => { loadOrgs().then(() => { loadList(); loadConfigs() }); loadExpire() })
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.mx { margin-left: 8px; }
.sec-title { margin: 14px 0 8px; font-size: 14px; color: #303133; }
</style>

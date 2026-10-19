<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">敏感字段脱敏 · 党龄自动计算 · 结构统计</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="filter.orgId" placeholder="全部组织" clearable size="small" filterable
                   style="width:230px" @change="loadList">
          <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-select v-model="filter.politicalStatus" placeholder="政治面貌" clearable size="small"
                   style="width:130px" @change="loadList">
          <el-option v-for="p in politicalStatuses" :key="p" :label="p" :value="p" />
        </el-select>
        <el-select v-model="filter.status" placeholder="状态" clearable size="small"
                   style="width:120px" @change="loadList">
          <el-option v-for="s in memberStatuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="filter.keyword" placeholder="姓名/编号/电话" clearable size="small"
                  style="width:180px" @keyup.enter="loadList" />
        <el-button size="small" type="primary" @click="loadList">查询</el-button>
        <div class="bar-right">
          <el-button size="small" @click="openStatistics">结构统计</el-button>
          <el-button v-if="can('R02','R04','R07','R08')" size="small" @click="openImport">导入</el-button>
          <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openEdit()">新增党员</el-button>
        </div>
      </div>

      <el-table :data="list" size="small" border v-loading="loading">
        <el-table-column prop="name" label="姓名" width="90" fixed />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="birthDate" label="出生日期" width="110" />
        <el-table-column label="政治面貌" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.politicalStatus === '正式党员' ? 'success' : (row.politicalStatus === '预备党员' ? 'warning' : 'info')">
              {{ row.politicalStatus || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinDate" label="入党日期" width="110" />
        <el-table-column prop="fullDate" label="转正日期" width="110" />
        <el-table-column label="党龄" width="80">
          <template #default="{ row }">{{ partyAgeText(row.fullDate) }}</template>
        </el-table-column>
        <el-table-column prop="orgName" label="组织" min-width="180" show-overflow-tooltip />
        <el-table-column prop="position" label="岗位" width="110" show-overflow-tooltip />
        <el-table-column label="联系电话" width="130">
          <template #default="{ row }">
            <el-tooltip v-if="isMasked(row.phone)" content="已脱敏，需申请解锁查看" placement="top">
              <span class="masked">{{ row.phone }}</span>
            </el-tooltip>
            <span v-else>{{ row.phone || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="档案" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.archiveComplete === 1 ? 'success' : 'warning'">
              {{ row.archiveComplete === 1 ? '完整' : '缺失' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('R02','R04','R07')" size="small" link type="warning" @click="openUnlock(row)">解锁申请</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ============ 详情 ============ -->
    <el-dialog v-model="detailVisible" :title="`党员档案 · ${detail.name || ''}`" width="980px" top="4vh">
      <div class="detail-head">
        <el-tag :type="detail.politicalStatus === '正式党员' ? 'success' : 'warning'">{{ detail.politicalStatus || '—' }}</el-tag>
        <el-tag size="small" type="info">{{ detail.memberStatus || '—' }}</el-tag>
        <el-tag size="small">党龄 {{ detail.partyAge != null ? detail.partyAge + '年' : '—' }}</el-tag>
        <el-tag v-if="!detail.photoUrl" size="small" type="warning">缺照片</el-tag>
        <el-tag v-if="!detail.phone || detail.phone === '***'" size="small" type="warning">缺联系方式</el-tag>
        <el-tag v-if="detail.archiveComplete !== 1" size="small" type="warning">档案材料缺失</el-tag>
      </div>

      <el-descriptions :column="3" border size="small" class="detail-desc">
        <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="党员编号">{{ detail.memberNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.gender || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detail.birthDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ detail.ethnicity || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ detail.education || '—' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">
          <el-tooltip v-if="isMasked(detail.idCard)" content="已脱敏，需申请解锁查看" placement="top">
            <span class="masked">{{ detail.idCard }}</span>
          </el-tooltip>
          <span v-else>{{ detail.idCard || '—' }}</span>
          <el-tag v-if="detail.unlockedFields && detail.unlockedFields.idCard" size="small" type="success" class="mx">已解锁</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          <el-tooltip v-if="isMasked(detail.phone)" content="已脱敏，需申请解锁查看" placement="top">
            <span class="masked">{{ detail.phone }}</span>
          </el-tooltip>
          <span v-else>{{ detail.phone || '—' }}</span>
          <el-tag v-if="detail.unlockedFields && detail.unlockedFields.phone" size="small" type="success" class="mx">已解锁</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="籍贯">{{ detail.nativePlace || '—' }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ detail.orgName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detail.position || '—' }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ detail.employeeNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入党日期">{{ detail.joinDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="转正日期">{{ detail.fullDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="流动状态">{{ detail.floatStatus || '—' }}</el-descriptions-item>
        <el-descriptions-item label="家庭住址" :span="2">
          <el-tooltip v-if="isMasked(detail.address)" content="已脱敏，需申请解锁查看" placement="top">
            <span class="masked">{{ detail.address }}</span>
          </el-tooltip>
          <span v-else>{{ detail.address || '—' }}</span>
          <el-tag v-if="detail.unlockedFields && detail.unlockedFields.address" size="small" type="success" class="mx">已解锁</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="mat-head">
        <b>档案材料</b>
        <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openMaterial">上传材料</el-button>
      </div>
      <el-table :data="materials" size="small" border max-height="260">
        <el-table-column prop="materialType" label="类型" width="110" />
        <el-table-column prop="materialName" label="材料名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="uploadBy" label="上传人" width="90" />
        <el-table-column prop="uploadTime" label="上传时间" width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '在库' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="borrower" label="借阅人" width="90" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04','R07','R08') && row.status === '在库'"
                       size="small" link type="primary" @click="borrowMaterial(row)">借阅</el-button>
            <el-button v-if="can('R02','R04','R07','R08') && row.status === '借阅中'"
                       size="small" link type="success" @click="returnMaterial(row)">归还</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- ============ 上传材料 ============ -->
    <el-dialog v-model="materialVisible" title="上传档案材料" width="480px" append-to-body>
      <el-form :model="materialForm" label-width="90px">
        <el-form-item label="材料类型" required>
          <el-select v-model="materialForm.materialType" filterable allow-create style="width:100%">
            <el-option v-for="t in materialTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="材料名称" required>
          <el-input v-model="materialForm.materialName" placeholder="如：入党志愿书扫描件" />
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="materialForm.filePath" placeholder="模拟路径，如 /files/xxx.pdf" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="materialVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMaterial">上传</el-button>
      </template>
    </el-dialog>

    <!-- ============ 编辑/新增 ============ -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑党员' : '新增党员'" width="760px" top="4vh">
      <el-form :model="editForm" label-width="96px" class="edit-form">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="editForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党员编号">
              <el-input v-model="editForm.memberNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="editForm.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="editForm.birthDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="editForm.idCard" maxlength="18" placeholder="18 位身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="editForm.ethnicity" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="editForm.education" clearable style="width:100%">
                <el-option v-for="e in educations" :key="e" :label="e" :value="e" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.phone" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌" required>
              <el-select v-model="editForm.politicalStatus" style="width:100%">
                <el-option v-for="p in politicalStatuses" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="党员状态">
              <el-select v-model="editForm.memberStatus" clearable style="width:100%">
                <el-option v-for="s in memberStatuses" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属组织" required>
              <el-select v-model="editForm.orgId" filterable style="width:100%" @change="onEditOrg">
                <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位">
              <el-input v-model="editForm.position" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号">
              <el-input v-model="editForm.employeeNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="editForm.nativePlace" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入党日期">
              <el-date-picker v-model="editForm.joinDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转正日期">
              <el-date-picker v-model="editForm.fullDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流动状态">
              <el-select v-model="editForm.floatStatus" clearable style="width:100%">
                <el-option v-for="f in ['在岗','流动','失联']" :key="f" :label="f" :value="f" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="照片地址">
              <el-input v-model="editForm.photoUrl" placeholder="照片 URL（模拟）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="家庭住址">
              <el-input v-model="editForm.address" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 解锁申请 ============ -->
    <el-dialog v-model="unlockVisible" title="敏感字段解锁申请" width="520px">
      <el-form label-width="90px">
        <el-form-item label="党员">{{ unlockForm.memberName }}</el-form-item>
        <el-form-item label="解锁字段" required>
          <el-radio-group v-model="unlockForm.fieldName">
            <el-radio value="idCard">身份证号</el-radio>
            <el-radio value="phone">联系电话</el-radio>
            <el-radio value="address">家庭住址</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请理由" required>
          <el-input v-model="unlockForm.reason" type="textarea" :rows="3"
                    placeholder="解锁后 24 小时内可查看原文，需上级党委审批" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unlockVisible = false">取消</el-button>
        <el-button type="warning" @click="submitUnlock">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ============ 结构统计 ============ -->
    <el-dialog v-model="statVisible" title="党员结构统计" width="860px" top="5vh" @opened="loadStatistics">
      <div class="stat-total">
        <b>{{ statistics.total || 0 }}</b><span>党员总数</span>
        <el-tag size="small" type="info" class="mx">{{ statOrgName }}</el-tag>
      </div>
      <el-row :gutter="10">
        <el-col :span="12">
          <div class="stat-block">
            <div class="stat-title">年龄分布</div>
            <el-table :data="distRows(statistics.ageDistribution)" size="small" border>
              <el-table-column prop="label" label="年龄段" />
              <el-table-column prop="value" label="人数" width="90" />
            </el-table>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-block">
            <div class="stat-title">学历分布</div>
            <el-table :data="distRows(statistics.educationDistribution)" size="small" border>
              <el-table-column prop="label" label="学历" />
              <el-table-column prop="value" label="人数" width="90" />
            </el-table>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-block">
            <div class="stat-title">岗位分布</div>
            <el-table :data="distRows(statistics.positionDistribution)" size="small" border>
              <el-table-column prop="label" label="岗位" />
              <el-table-column prop="value" label="人数" width="90" />
            </el-table>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="stat-block">
            <div class="stat-title">党龄分布</div>
            <el-table :data="distRows(statistics.partyAgeDistribution)" size="small" border>
              <el-table-column prop="label" label="党龄段" />
              <el-table-column prop="value" label="人数" width="90" />
            </el-table>
          </div>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- ============ 导入 ============ -->
    <el-dialog v-model="importVisible" title="党员数据导入" width="860px" top="5vh">
      <el-alert type="info" :closable="false" class="import-tip"
                title="粘贴 JSON 数组（字段：name/idCard/phone/joinDate/fullDate），先预览校验，再提交入库。" />
      <el-input v-model="importText" type="textarea" :rows="6"
                placeholder='[{"name":"张三","idCard":"110101199001011234","phone":"13900000901","joinDate":"2020-06-01","fullDate":"2021-06-01"}]' />
      <div class="import-actions">
        <el-button size="small" @click="previewImport">预览校验</el-button>
        <el-button size="small" type="primary" :disabled="!previewRows.length" @click="submitImport">提交入库</el-button>
        <span v-if="importResult" class="tip">
          成功 {{ importResult.success }} 条，失败 {{ importResult.fail }} 条
        </span>
      </div>
      <el-table v-if="previewRows.length" :data="previewRows" size="small" border max-height="300" class="preview-table">
        <el-table-column prop="rowNum" label="行号" width="70" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="idCard" label="身份证号" width="180" show-overflow-tooltip />
        <el-table-column prop="joinDate" label="入党日期" width="110" />
        <el-table-column prop="fullDate" label="转正日期" width="110" />
        <el-table-column label="校验" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.valid" size="small" type="success">通过</el-tag>
            <el-tag v-for="e in row.errors" :key="e" size="small" type="danger" class="mx">{{ e }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/member/info'
const ORG_API = '/api/party/org/organization'

const politicalStatuses = ['正式党员', '预备党员', '共青团员', '群众']
const memberStatuses = ['正式党员', '预备党员', '流动党员', '停止党籍']
const educations = ['博士', '硕士', '本科', '大专', '高中', '中专', '初中及以下']
const materialTypes = ['入党申请书', '入党志愿书', '政审材料', '转正申请书', '思想汇报', '其他']

const loading = ref(false)
const list = ref([])
const orgFlat = ref([])
const filter = reactive({ orgId: null, politicalStatus: null, status: null, keyword: '' })

async function loadOrgs() {
  const res = await axios.get(`${ORG_API}/tree`)
  if (res.data.code === 200) {
    const flat = []
    const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
    walk(res.data.data)
    orgFlat.value = flat
  }
}
async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: {
        orgId: filter.orgId || undefined,
        politicalStatus: filter.politicalStatus || undefined,
        status: filter.status || undefined,
        keyword: filter.keyword || undefined
      }
    })
    if (res.data.code === 200) list.value = res.data.data
  } finally { loading.value = false }
}

function isMasked(v) {
  return typeof v === 'string' && v.includes('*')
}
function calcPartyAge(fullDate) {
  if (!fullDate) return null
  const d = new Date(fullDate)
  if (isNaN(d.getTime())) return null
  const now = new Date()
  let y = now.getFullYear() - d.getFullYear()
  const m = now.getMonth() - d.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < d.getDate())) y--
  return y < 0 ? 0 : y
}
function partyAgeText(fullDate) {
  const y = calcPartyAge(fullDate)
  return y == null ? '—' : `${y}年`
}

// ---------- 详情 ----------
const detailVisible = ref(false)
const detail = ref({})
const materials = ref([])

async function openDetail(row) {
  try {
    const res = await axios.get(`${API}/${row.id}`)
    if (res.data.code === 200) {
      detail.value = res.data.data
      detailVisible.value = true
      loadMaterials()
    }
  } catch { /* 拦截器提示 */ }
}
async function loadMaterials() {
  const res = await axios.get(`${API}/materials`, { params: { memberId: detail.value.id } })
  if (res.data.code === 200) materials.value = res.data.data
}

// ---------- 材料 ----------
const materialVisible = ref(false)
const materialForm = reactive({})

function openMaterial() {
  Object.keys(materialForm).forEach(k => delete materialForm[k])
  Object.assign(materialForm, { materialType: null, materialName: '', filePath: '' })
  materialVisible.value = true
}
async function submitMaterial() {
  if (!materialForm.materialType) { ElMessage.warning('请选择材料类型'); return }
  if (!materialForm.materialName?.trim()) { ElMessage.warning('请填写材料名称'); return }
  try {
    await axios.post(`${API}/materials`, {
      memberId: detail.value.id,
      materialType: materialForm.materialType,
      materialName: materialForm.materialName,
      filePath: materialForm.filePath
    })
    ElMessage.success('材料已上传')
    materialVisible.value = false
    loadMaterials()
    loadList()
  } catch { /* 拦截器提示 */ }
}
async function borrowMaterial(row) {
  try {
    const { value } = await ElMessageBox.prompt(`借阅《${row.materialName}》，请输入借阅人：`, '借阅登记', {
      confirmButtonText: '确认借阅', cancelButtonText: '取消',
      inputValidator: v => !!v?.trim() || '借阅人不能为空'
    })
    await axios.post(`${API}/materials/${row.id}/borrow`, null, { params: { borrower: value } })
    ElMessage.success('借阅已登记')
    loadMaterials()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}
async function returnMaterial(row) {
  try {
    await axios.post(`${API}/materials/${row.id}/return`)
    ElMessage.success('材料已归还')
    loadMaterials()
  } catch { /* 拦截器提示 */ }
}

// ---------- 编辑/新增 ----------
const editVisible = ref(false)
const editForm = reactive({})

function onEditOrg(id) {
  const o = orgFlat.value.find(x => x.id === id)
  if (o) editForm.orgName = o.name
}
function openEdit(row) {
  Object.keys(editForm).forEach(k => delete editForm[k])
  if (row) {
    Object.assign(editForm, JSON.parse(JSON.stringify(row)))
    // 脱敏值不回填，留空表示不修改
    if (isMasked(editForm.idCard)) editForm.idCard = ''
    if (isMasked(editForm.phone)) editForm.phone = ''
    if (isMasked(editForm.address)) editForm.address = ''
  } else {
    Object.assign(editForm, { politicalStatus: '正式党员', gender: '男' })
  }
  editVisible.value = true
}
async function submitEdit() {
  if (!editForm.name?.trim()) { ElMessage.warning('姓名必填'); return }
  if (!editForm.politicalStatus) { ElMessage.warning('请选择政治面貌'); return }
  if (!editForm.orgId) { ElMessage.warning('请选择所属组织'); return }
  if (editForm.idCard && !/^\d{17}[\dXx]$/.test(editForm.idCard)) {
    ElMessage.error('身份证号须为 18 位（末位可为 X）')
    return
  }
  if (editForm.joinDate && editForm.fullDate && editForm.fullDate < editForm.joinDate) {
    ElMessage.error('转正日期不能早于入党日期')
    return
  }
  const body = { id: editForm.id }
  ;['memberNo','name','gender','birthDate','ethnicity','education','orgId','orgName','position',
    'employeeNo','nativePlace','joinDate','fullDate','memberStatus','politicalStatus','floatStatus',
    'photoUrl','idCard','phone','address'].forEach(k => {
    if (editForm[k] !== undefined && editForm[k] !== '') body[k] = editForm[k]
  })
  try {
    await axios.post(API, body)
    ElMessage.success('党员档案已保存')
    editVisible.value = false
    loadList()
  } catch { /* 拦截器提示 */ }
}

// ---------- 解锁申请 ----------
const unlockVisible = ref(false)
const unlockForm = reactive({})

function openUnlock(row) {
  Object.keys(unlockForm).forEach(k => delete unlockForm[k])
  Object.assign(unlockForm, { memberId: row.id, memberName: row.name, fieldName: 'idCard', reason: '' })
  unlockVisible.value = true
}
async function submitUnlock() {
  if (!unlockForm.reason?.trim()) { ElMessage.warning('请填写申请理由'); return }
  try {
    await axios.post(`${API}/${unlockForm.memberId}/unlock`, null, {
      params: { fieldName: unlockForm.fieldName, reason: unlockForm.reason }
    })
    ElMessage.success('解锁申请已提交，待上级党委审批')
    unlockVisible.value = false
  } catch { /* 拦截器提示 */ }
}

// ---------- 结构统计 ----------
const statVisible = ref(false)
const statistics = ref({})
const statOrgName = computed(() => {
  if (!filter.orgId) return '全部组织'
  const o = orgFlat.value.find(x => x.id === filter.orgId)
  return o ? o.name : '全部组织'
})

function openStatistics() {
  statistics.value = {}
  statVisible.value = true
}
async function loadStatistics() {
  try {
    const res = await axios.get(`${API}/statistics`, { params: { orgId: filter.orgId || undefined } })
    if (res.data.code === 200) statistics.value = res.data.data
  } catch { /* 拦截器提示 */ }
}
function distRows(dist) {
  if (!dist) return []
  return Object.entries(dist).map(([label, value]) => ({ label, value }))
}

// ---------- 导入 ----------
const importVisible = ref(false)
const importText = ref('')
const previewRows = ref([])
const importResult = ref(null)

function openImport() {
  importText.value = ''
  previewRows.value = []
  importResult.value = null
  importVisible.value = true
}
function parseImportRows() {
  let rows
  try {
    rows = JSON.parse(importText.value)
  } catch {
    ElMessage.error('JSON 格式错误，请检查输入')
    return null
  }
  if (!Array.isArray(rows) || !rows.length) {
    ElMessage.warning('请输入非空 JSON 数组')
    return null
  }
  return rows
}
async function previewImport() {
  const rows = parseImportRows()
  if (!rows) return
  try {
    const res = await axios.post(`${API}/import/preview`, rows)
    if (res.data.code === 200) {
      previewRows.value = res.data.data
      importResult.value = null
    }
  } catch { /* 拦截器提示 */ }
}
async function submitImport() {
  const rows = parseImportRows()
  if (!rows) return
  try {
    const res = await axios.post(`${API}/import/submit`, rows)
    if (res.data.code === 200) {
      importResult.value = res.data.data
      ElMessage.success(`导入完成：成功 ${importResult.value.success} 条，失败 ${importResult.value.fail} 条`)
      loadList()
    }
  } catch { /* 拦截器提示 */ }
}

onMounted(() => { loadOrgs().then(loadList) })
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.bar-right { margin-left: auto; display: flex; gap: 8px; }
.tip { color: #909399; font-size: 12px; }
.mx { margin-left: 8px; }
.masked { color: #c0a060; cursor: help; }
.detail-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.detail-desc { margin-bottom: 14px; }
.mat-head { display: flex; align-items: center; justify-content: space-between; margin: 4px 0 8px; }
.stat-total { display: flex; align-items: baseline; gap: 8px; margin-bottom: 12px; }
.stat-total b { font-size: 26px; color: #409eff; }
.stat-total span { font-size: 13px; color: #909399; }
.stat-block { margin-bottom: 12px; }
.stat-title { font-weight: 600; margin-bottom: 6px; color: #5a6a7e; }
.import-tip { margin-bottom: 10px; }
.import-actions { display: flex; align-items: center; gap: 10px; margin: 10px 0; }
.preview-table { margin-top: 6px; }
</style>

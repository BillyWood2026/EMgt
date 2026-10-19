<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">计划 · 课程库 · 在线学习 · 学时统计 · 师资库</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ============ 培训计划 ============ -->
        <el-tab-pane label="培训计划" name="plans">
          <div class="bar">
            <el-select v-model="planFilter.orgId" placeholder="全部组织" clearable filterable size="small"
                       style="width:230px" @change="loadPlans">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="planFilter.year" placeholder="年度" clearable size="small" style="width:110px" @change="loadPlans">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openPlan()">新增计划</el-button>
          </div>
          <el-table :data="plans" size="small" border v-loading="planLoading">
            <el-table-column prop="planName" label="计划名称" min-width="220" show-overflow-tooltip />
            <el-table-column prop="trainYear" label="年度" width="80" />
            <el-table-column prop="targetCount" label="目标人数" width="90" />
            <el-table-column prop="form" label="培训形式" width="110" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="planStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="primary" @click="openCompletion(row)">完成率</el-button>
                <el-button v-if="can('R02','R04')" size="small" link @click="openPlan(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 课程库 ============ -->
        <el-tab-pane label="课程库" name="courses">
          <div class="bar">
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openCourse()">新增课程</el-button>
            <el-button size="small" @click="loadCourses">刷新</el-button>
          </div>
          <el-table :data="courses" size="small" border v-loading="courseLoading">
            <el-table-column prop="courseName" label="课程名称" min-width="220" show-overflow-tooltip />
            <el-table-column prop="category" label="课程类别" width="110" />
            <el-table-column label="课程时长" width="100">
              <template #default="{ row }">{{ row.hours }} 学时</template>
            </el-table-column>
            <el-table-column prop="lecturer" label="讲师" width="110" />
            <el-table-column label="是否启用" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.enabled === 1 ? 'success' : 'danger'">
                  {{ row.enabled === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04')" size="small" link @click="openCourse(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 师资库 ============ -->
        <el-tab-pane label="师资库" name="teachers">
          <div class="bar">
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="openTeacher()">新增师资</el-button>
            <el-button size="small" @click="loadTeachers">刷新</el-button>
          </div>
          <el-table :data="teachers" size="small" border v-loading="teacherLoading">
            <el-table-column prop="teacherName" label="姓名" width="110" />
            <el-table-column prop="title" label="职称" width="140" show-overflow-tooltip />
            <el-table-column prop="specialty" label="擅长领域" min-width="180" show-overflow-tooltip />
            <el-table-column prop="phone" label="联系电话" width="130" />
            <el-table-column label="是否启用" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.enabled === 1 ? 'success' : 'danger'">
                  {{ row.enabled === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04')" size="small" link @click="openTeacher(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 在线学习 ============ -->
        <el-tab-pane label="在线学习" name="study">
          <div class="bar">
            <el-select v-model="studyFilter.memberId" placeholder="全部党员" clearable filterable size="small"
                       style="width:160px" @change="loadStudies">
              <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
            <el-select v-model="studyFilter.courseId" placeholder="全部课程" clearable filterable size="small"
                       style="width:220px" @change="loadStudies">
              <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
            </el-select>
            <el-select v-model="studyFilter.year" placeholder="年度" clearable size="small" style="width:110px" @change="loadStudies">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R04','R07','R08')" size="small" type="primary" @click="openStudy()">登记学习</el-button>
          </div>
          <el-table :data="studies" size="small" border v-loading="studyLoading">
            <el-table-column prop="memberName" label="党员" width="100" />
            <el-table-column prop="courseName" label="课程" min-width="200" show-overflow-tooltip />
            <el-table-column prop="studyDate" label="学习日期" width="110" />
            <el-table-column label="学习时长" width="100">
              <template #default="{ row }">{{ row.studyMinutes }} 分钟</template>
            </el-table-column>
            <el-table-column label="进度" width="140">
              <template #default="{ row }">
                <el-progress :percentage="pctNum(row.progress)" :stroke-width="10"
                             :status="Number(row.progress) >= 100 ? 'success' : ''" />
              </template>
            </el-table-column>
            <el-table-column label="是否计入学时" width="110">
              <template #default="{ row }">
                <el-tag size="small" :type="row.validFlag === 1 ? 'success' : 'danger'">
                  {{ row.validFlag === 1 ? '计入' : '不计入' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学时" width="90">
              <template #default="{ row }">{{ row.scoreHours }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 学时统计 ============ -->
        <el-tab-pane label="学时统计" name="hours">
          <div class="bar">
            <el-select v-model="hoursFilter.orgId" placeholder="全部组织" clearable filterable size="small"
                       style="width:230px" @change="loadHours">
              <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
            </el-select>
            <el-select v-model="hoursFilter.year" placeholder="年度" size="small" style="width:110px" @change="loadHours">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button v-if="can('R02','R04')" size="small" type="primary" @click="generateHours">生成统计</el-button>
          </div>
          <el-table :data="hoursStats" size="small" border v-loading="hoursLoading">
            <el-table-column prop="memberName" label="党员" width="100" />
            <el-table-column prop="orgName" label="组织" min-width="200" show-overflow-tooltip />
            <el-table-column prop="totalHours" label="总学时" width="90" />
            <el-table-column prop="targetHours" label="目标学时" width="90" />
            <el-table-column label="完成率" width="160">
              <template #default="{ row }">
                <el-progress :percentage="pctNum(row.completionRate)" :stroke-width="10"
                             :status="Number(row.completionRate) >= 100 ? 'success' : ''" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '达标' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 学时预警 ============ -->
        <el-tab-pane label="学时预警" name="warning">
          <div class="bar">
            <el-select v-model="warningFilter.year" placeholder="年度" size="small" style="width:110px" @change="loadWarnings">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
            <el-button size="small" @click="loadWarnings">刷新</el-button>
            <span class="tip">展示年度学时未达标的党员（目标默认 32 学时）</span>
          </div>
          <el-table :data="warnings" size="small" border v-loading="warningLoading">
            <el-table-column prop="memberName" label="党员" width="100" />
            <el-table-column prop="orgName" label="组织" min-width="200" show-overflow-tooltip />
            <el-table-column prop="totalHours" label="总学时" width="90" />
            <el-table-column prop="targetHours" label="目标学时" width="90" />
            <el-table-column label="完成率" width="160">
              <template #default="{ row }">
                <el-progress :percentage="pctNum(row.completionRate)" :stroke-width="10" status="exception" />
              </template>
            </el-table-column>
            <el-table-column label="预警信息" min-width="180">
              <template #default="{ row }">
                <el-tag size="small" type="danger">{{ row.message }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============ 培训计划表单 ============ -->
    <el-dialog v-model="planVisible" :title="planForm.id ? '编辑培训计划' : '新增培训计划'" width="640px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="计划名称" required>
          <el-input v-model="planForm.planName" placeholder="如：2026年度党员教育培训计划" />
        </el-form-item>
        <el-form-item label="培训组织" required>
          <el-select v-model="planForm.orgId" filterable style="width:100%">
            <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训年度" required>
          <el-select v-model="planForm.trainYear" style="width:160px">
            <el-option v-for="y in years" :key="y" :label="y + '年度'" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标人数">
          <el-input-number v-model="planForm.targetCount" :min="0" :max="100000" />
        </el-form-item>
        <el-form-item label="培训形式">
          <el-select v-model="planForm.form" style="width:200px" allow-create filterable>
            <el-option v-for="f in ['集中轮训','在线学习','专题讲座','实践教学']" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标学时">
          <el-input-number v-model="planForm.targetHours" :min="0" :max="1000" :precision="1" /> 学时
        </el-form-item>
        <el-form-item label="起止日期">
          <el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="开始日期" style="width:160px" />
          <span class="mx">至</span>
          <el-date-picker v-model="planForm.endDate" type="date" value-format="YYYY-MM-DD"
                          placeholder="结束日期" style="width:160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="planForm.status">
            <el-radio value="草稿">草稿</el-radio>
            <el-radio value="进行中">进行中</el-radio>
            <el-radio value="已结束">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 计划完成率 ============ -->
    <el-dialog v-model="compVisible" title="计划完成率" width="480px">
      <div class="comp-box">
        <div class="comp-title">{{ completion.planName }}</div>
        <el-progress :percentage="pctNum(completion.completionRate)" :stroke-width="16"
                     :status="Number(completion.completionRate) >= 100 ? 'success' : ''" />
        <div class="comp-meta">
          <span>目标人数：{{ completion.targetCount }} 人</span>
          <span>已完成：{{ completion.completedCount }} 人次</span>
          <span>完成率：{{ completion.completionRate }}%</span>
        </div>
        <el-tag size="small" :type="planStatusType(completion.status)">计划状态：{{ completion.status }}</el-tag>
      </div>
      <template #footer>
        <el-button @click="compVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 课程表单 ============ -->
    <el-dialog v-model="courseVisible" :title="courseForm.id ? '编辑课程' : '新增课程'" width="600px">
      <el-form :model="courseForm" label-width="100px">
        <el-form-item label="课程名称" required>
          <el-input v-model="courseForm.courseName" />
        </el-form-item>
        <el-form-item label="课程类别">
          <el-select v-model="courseForm.category" style="width:200px" allow-create filterable>
            <el-option v-for="c in ['理论教育','党性教育','业务能力','知识技能']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程时长">
          <el-input-number v-model="courseForm.hours" :min="0" :max="1000" :precision="1" /> 学时
        </el-form-item>
        <el-form-item label="讲师">
          <el-input v-model="courseForm.lecturer" style="width:200px" />
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input v-model="courseForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="courseForm.enabled" :active-value="1" :inactive-value="0"
                     active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCourse">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 师资表单 ============ -->
    <el-dialog v-model="teacherVisible" :title="teacherForm.id ? '编辑师资' : '新增师资'" width="600px">
      <el-form :model="teacherForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="teacherForm.teacherName" style="width:200px" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="teacherForm.title" style="width:200px" placeholder="如：教授 / 高级政工师" />
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="teacherForm.specialty" placeholder="如：马克思主义理论、党史党建" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-select v-model="teacherForm.orgId" filterable clearable style="width:100%">
            <el-option v-for="o in orgFlat" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="teacherForm.phone" style="width:200px" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="teacherForm.enabled" :active-value="1" :inactive-value="0"
                     active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teacherVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTeacher">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============ 登记学习 ============ -->
    <el-dialog v-model="studyVisible" title="登记在线学习" width="600px">
      <el-form :model="studyForm" label-width="100px">
        <el-form-item label="党员" required>
          <el-select v-model="studyForm.memberId" filterable style="width:100%" @change="onStudyMember">
            <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" required>
          <el-select v-model="studyForm.courseId" filterable style="width:100%" @change="onStudyCourse">
            <el-option v-for="c in courses" :key="c.id" :label="`${c.courseName}（${c.hours}学时）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学习日期" required>
          <el-date-picker v-model="studyForm.studyDate" type="date" value-format="YYYY-MM-DD" style="width:160px" />
        </el-form-item>
        <el-form-item label="学习时长">
          <el-input-number v-model="studyForm.studyMinutes" :min="0" :max="10000" /> 分钟
          <span class="tip mx">课程总时长 {{ studyForm.courseMinutes || 0 }} 分钟</span>
        </el-form-item>
        <el-form-item label="学习进度" required>
          <el-input-number v-model="studyForm.progress" :min="0" :max="100" /> %
          <el-tag size="small" class="mx" :type="studyForm.progress >= 80 ? 'success' : 'danger'">
            {{ studyForm.progress >= 80 ? '计入学时' : '进度<80%，不计入学时' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="预计学时">
          <el-tag size="small" type="info">{{ studyPreviewHours }} 学时</el-tag>
          <span class="tip mx">同人同课程同日重复登记将自动去重（E-MB-14）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="studyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStudy">保存</el-button>
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

const API = '/api/party/member/education'
const ORG_API = '/api/party/org/organization'
const MEMBER_API = '/api/party/member/info'
const years = [2024, 2025, 2026, 2027]
const CUR_YEAR = 2026

const activeTab = ref('plans')
const orgFlat = ref([])
const members = ref([])

async function loadOrgs() {
  try {
    const res = await axios.get(`${ORG_API}/tree`)
    if (res.data.code === 200) {
      const flat = []
      const walk = nodes => nodes.forEach(n => { flat.push(n); if (n.children) walk(n.children) })
      walk(res.data.data)
      orgFlat.value = flat
    }
  } catch { /* 拦截器提示 */ }
}
async function loadMembers() {
  try {
    const res = await axios.get(`${MEMBER_API}/list`)
    if (res.data.code === 200) members.value = res.data.data || []
  } catch { /* 拦截器提示 */ }
}

function pctNum(v) {
  const n = Number(v)
  if (isNaN(n)) return 0
  return Math.min(Math.max(n, 0), 100)
}
function planStatusType(s) {
  return { 草稿: 'info', 进行中: 'warning', 已结束: 'success' }[s] || 'info'
}

function onTabChange(name) {
  if (name === 'plans') loadPlans()
  else if (name === 'courses') loadCourses()
  else if (name === 'teachers') loadTeachers()
  else if (name === 'study') { loadCourses(); loadStudies() }
  else if (name === 'hours') loadHours()
  else if (name === 'warning') loadWarnings()
}

// ---------- 培训计划 ----------
const plans = ref([])
const planLoading = ref(false)
const planFilter = reactive({ orgId: null, year: null })

async function loadPlans() {
  planLoading.value = true
  try {
    const res = await axios.get(`${API}/plans`, {
      params: { orgId: planFilter.orgId || undefined, year: planFilter.year || undefined }
    })
    if (res.data.code === 200) plans.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { planLoading.value = false }
}

const planVisible = ref(false)
const planForm = reactive({})
function openPlan(row) {
  Object.keys(planForm).forEach(k => delete planForm[k])
  if (row) {
    Object.assign(planForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(planForm, { trainYear: CUR_YEAR, targetCount: null, targetHours: 32, status: '草稿', form: '集中轮训' })
  }
  planVisible.value = true
}
async function submitPlan() {
  if (!planForm.planName?.trim()) { ElMessage.warning('计划名称必填'); return }
  if (!planForm.orgId) { ElMessage.warning('请选择培训组织'); return }
  if (!planForm.trainYear) { ElMessage.warning('请选择培训年度'); return }
  try {
    await axios.post(`${API}/plans`, planForm)
    ElMessage.success('计划已保存')
    planVisible.value = false
    loadPlans()
  } catch { /* 拦截器提示 */ }
}

// ---------- 计划完成率 ----------
const compVisible = ref(false)
const completion = ref({})
async function openCompletion(row) {
  try {
    const res = await axios.get(`${API}/plan-completion/${row.id}`)
    if (res.data.code === 200) {
      completion.value = res.data.data || {}
      compVisible.value = true
    }
  } catch { /* 拦截器提示 */ }
}

// ---------- 课程库 ----------
const courses = ref([])
const courseLoading = ref(false)

async function loadCourses() {
  courseLoading.value = true
  try {
    const res = await axios.get(`${API}/courses`)
    if (res.data.code === 200) courses.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { courseLoading.value = false }
}

const courseVisible = ref(false)
const courseForm = reactive({})
function openCourse(row) {
  Object.keys(courseForm).forEach(k => delete courseForm[k])
  if (row) {
    Object.assign(courseForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(courseForm, { category: '理论教育', hours: 1, enabled: 1 })
  }
  courseVisible.value = true
}
async function submitCourse() {
  if (!courseForm.courseName?.trim()) { ElMessage.warning('课程名称必填'); return }
  try {
    await axios.post(`${API}/courses`, courseForm)
    ElMessage.success('课程已保存')
    courseVisible.value = false
    loadCourses()
  } catch { /* 拦截器提示 */ }
}

// ---------- 师资库 ----------
const teachers = ref([])
const teacherLoading = ref(false)

async function loadTeachers() {
  teacherLoading.value = true
  try {
    const res = await axios.get(`${API}/teachers`)
    if (res.data.code === 200) teachers.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { teacherLoading.value = false }
}

const teacherVisible = ref(false)
const teacherForm = reactive({})
function openTeacher(row) {
  Object.keys(teacherForm).forEach(k => delete teacherForm[k])
  if (row) {
    Object.assign(teacherForm, JSON.parse(JSON.stringify(row)))
  } else {
    Object.assign(teacherForm, { enabled: 1 })
  }
  teacherVisible.value = true
}
async function submitTeacher() {
  if (!teacherForm.teacherName?.trim()) { ElMessage.warning('姓名必填'); return }
  try {
    await axios.post(`${API}/teachers`, teacherForm)
    ElMessage.success('师资已保存')
    teacherVisible.value = false
    loadTeachers()
  } catch { /* 拦截器提示 */ }
}

// ---------- 在线学习 ----------
const studies = ref([])
const studyLoading = ref(false)
const studyFilter = reactive({ memberId: null, courseId: null, year: null })

async function loadStudies() {
  studyLoading.value = true
  try {
    const res = await axios.get(`${API}/study-records`, {
      params: {
        memberId: studyFilter.memberId || undefined,
        courseId: studyFilter.courseId || undefined,
        year: studyFilter.year || undefined
      }
    })
    if (res.data.code === 200) studies.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { studyLoading.value = false }
}

const studyVisible = ref(false)
const studyForm = reactive({})
const studyPreviewHours = computed(() => {
  if (studyForm.progress == null || studyForm.progress < 80) return '0.00'
  return ((Number(studyForm.studyMinutes) || 0) / 60).toFixed(2)
})
function openStudy() {
  Object.keys(studyForm).forEach(k => delete studyForm[k])
  const today = new Date()
  const pad = n => String(n).padStart(2, '0')
  Object.assign(studyForm, {
    memberId: null, memberName: '', orgId: null, orgName: '',
    courseId: null, courseName: '', studyDate: `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`,
    studyMinutes: 0, courseMinutes: 0, progress: 100
  })
  studyVisible.value = true
}
function onStudyMember(id) {
  const m = members.value.find(x => x.id === id)
  if (m) {
    studyForm.memberName = m.name
    studyForm.orgId = m.orgId
    studyForm.orgName = m.orgName || ''
  }
}
function onStudyCourse(id) {
  const c = courses.value.find(x => x.id === id)
  if (c) {
    studyForm.courseName = c.courseName
    studyForm.courseMinutes = Math.round(Number(c.hours || 0) * 60)
    studyForm.studyMinutes = studyForm.courseMinutes
  }
}
async function submitStudy() {
  if (!studyForm.memberId) { ElMessage.warning('请选择党员'); return }
  if (!studyForm.courseId) { ElMessage.warning('请选择课程'); return }
  if (!studyForm.studyDate) { ElMessage.warning('请选择学习日期'); return }
  if (studyForm.progress == null) { ElMessage.warning('请填写学习进度'); return }
  try {
    await axios.post(`${API}/study-records`, studyForm)
    ElMessage.success(studyForm.progress >= 80 ? '学习已登记，计入学时' : '学习已登记，进度<80%不计入学时')
    studyVisible.value = false
    loadStudies()
  } catch (e) {
    if (e?.response?.data?.data?.errorCode === 'E-MB-14') {
      ElMessageBox.alert('同课程同日期已存在学习记录，重复学习已去重。', '重复学习去重', { type: 'warning' })
    }
  }
}

// ---------- 学时统计 ----------
const hoursStats = ref([])
const hoursLoading = ref(false)
const hoursFilter = reactive({ orgId: null, year: CUR_YEAR })

async function loadHours() {
  hoursLoading.value = true
  try {
    const res = await axios.get(`${API}/hours-stat`, {
      params: { orgId: hoursFilter.orgId || undefined, year: hoursFilter.year || undefined }
    })
    if (res.data.code === 200) hoursStats.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { hoursLoading.value = false }
}
async function generateHours() {
  if (!hoursFilter.year) { ElMessage.warning('请先选择统计年度'); return }
  try {
    await ElMessageBox.confirm(`将按 ${hoursFilter.year} 年度有效学习记录重新汇总学时统计，是否继续？`, '生成学时统计', { type: 'info' })
    await axios.post(`${API}/hours-stat/generate`, null, { params: { year: hoursFilter.year } })
    ElMessage.success('学时统计已生成')
    loadHours()
  } catch (e) { if (e !== 'cancel') { /* 业务错误已提示 */ } }
}

// ---------- 学时预警 ----------
const warnings = ref([])
const warningLoading = ref(false)
const warningFilter = reactive({ year: CUR_YEAR })

async function loadWarnings() {
  warningLoading.value = true
  try {
    const res = await axios.get(`${API}/training-warning`, { params: { year: warningFilter.year } })
    if (res.data.code === 200) warnings.value = res.data.data || []
  } catch { /* 拦截器提示 */ } finally { warningLoading.value = false }
}

onMounted(() => {
  loadOrgs()
  loadMembers()
  loadPlans()
  loadCourses()
  loadTeachers()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.bar { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.tip { color: #909399; font-size: 12px; }
.mx { margin-left: 8px; }
.comp-box { padding: 4px 2px; }
.comp-title { font-weight: 600; margin-bottom: 12px; }
.comp-meta { display: flex; gap: 18px; margin: 12px 0; color: #606266; font-size: 13px; }
</style>

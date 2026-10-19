<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">我的调研：在有效期内的已发布问卷可作答；匿名问卷可重复提交，实名问卷每人限交一次</el-tag>
    </RoleBar>

    <el-row :gutter="14" v-loading="loading">
      <el-col v-for="q in rows" :key="q.id" :span="8">
        <el-card shadow="hover" class="q-card">
          <div class="q-title">{{ q.title }}</div>
          <div class="q-tags">
            <el-tag size="small">{{ q.periodType }}</el-tag>
            <el-tag size="small" :type="q.anonymousFlag === 1 ? 'success' : 'warning'">
              {{ q.anonymousFlag === 1 ? '匿名调研' : '实名调研' }}
            </el-tag>
            <el-tag size="small" :type="q.submitted ? 'info' : q.inWindow ? 'danger' : 'info'" effect="plain">
              {{ q.submitted ? '已提交' : q.inWindow ? '待作答' : '已过作答期' }}
            </el-tag>
          </div>
          <div class="q-line">参与组织：{{ q.targetOrg || '全员' }}</div>
          <div class="q-line">有效期：{{ fmt(q.startTime) }} ~ {{ fmt(q.endTime) }}</div>
          <div class="q-foot">
            <el-button size="small" type="primary" :disabled="q.submitted || !q.inWindow" @click="openAnswer(q)">
              {{ q.submitted ? '已提交' : !q.inWindow ? '已过作答期' : '参与调研' }}
            </el-button>
            <span v-if="q.anonymousFlag === 1" class="q-anon">匿名提交，结果不关联个人</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" v-if="!loading && !rows.length">
        <el-empty description="暂无可参与的调研问卷" />
      </el-col>
    </el-row>

    <el-dialog v-model="ansVis" :title="cur && cur.title" width="760px" :close-on-click-modal="false">
      <template v-if="cur">
        <el-alert type="info" :closable="false" class="mb">
          {{ cur.anonymousFlag === 1 ? '本问卷为匿名调研，请放心如实作答。' : '本问卷为实名调研，提交后不可重复作答。' }}
          请填写您所在部门用于分部门氛围诊断：
          <el-input v-model="userDept" size="small" style="width:200px; margin-left:8px" placeholder="如 客户服务部" />
        </el-alert>
        <el-form label-position="top" size="small">
          <el-card v-for="(item, i) in qDefs" :key="i" shadow="never" class="q-item">
            <div class="q-stem">
              {{ i + 1 }}. {{ item.stem }}
              <span v-if="item.required === 1 || item.required === true" class="req">*</span>
              <el-tag size="small" effect="plain" class="ml">{{ typeLabel(item.type) }}</el-tag>
              <el-tag v-if="item.type === '量表' && item.dimension" size="small" type="success" effect="plain" class="ml">{{ item.dimension }}</el-tag>
            </div>
            <div class="q-body">
              <el-radio-group v-if="item.type === '单选'" v-model="answers[item.qid]">
                <el-radio v-for="opt in item.options" :key="opt" :value="opt">{{ opt }}</el-radio>
              </el-radio-group>
              <el-checkbox-group v-else-if="item.type === '多选'" v-model="multiAnswers[item.qid]">
                <el-checkbox v-for="opt in item.options" :key="opt" :value="opt">{{ opt }}</el-checkbox>
              </el-checkbox-group>
              <div v-else-if="item.type === '量表'" class="rate-line">
                <el-rate v-model="answers[item.qid]" :max="item.maxScale || 5" />
                <span class="rate-tip">1 分最低 ~ {{ item.maxScale || 5 }} 分最高，当前 {{ answers[item.qid] || 0 }} 分</span>
              </div>
              <el-input v-else v-model="answers[item.qid]" type="textarea" :rows="3" maxlength="500" show-word-limit
                        placeholder="请输入您的看法或建议" />
            </div>
          </el-card>
        </el-form>
      </template>
      <template #footer>
        <el-button size="small" @click="ansVis = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="doSubmit">提交答卷</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const rows = ref([])
const loading = ref(false)
const ansVis = ref(false)
const submitting = ref(false)
const cur = ref(null)
const qDefs = ref([])
const answers = ref({})
const multiAnswers = ref({})
const userDept = ref(localStorage.getItem('surveyDept') || '')

function fmt(s) { return s ? String(s).replace('T', ' ').slice(0, 16) : '-' }
function typeLabel(t) { return { 单选: '单选题', 多选: '多选题', 量表: '量表打分', 文本: '开放题' }[t] || t }

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/survey-mine/list')
    rows.value = r.data.data || []
  } finally { loading.value = false }
}

function openAnswer(q) {
  cur.value = q
  let defs = []
  try { defs = JSON.parse(q.questions || '[]') } catch (e) { defs = [] }
  qDefs.value = defs.map((d, i) => ({ ...d, qid: d.qid || 'q' + (i + 1) }))
  answers.value = {}
  multiAnswers.value = {}
  qDefs.value.forEach(d => {
    if (d.type === '多选') multiAnswers.value[d.qid] = []
  })
  ansVis.value = true
}

async function doSubmit() {
  if (!userDept.value.trim()) return ElMessage.warning('请先填写所在部门')
  const payloadAnswers = { ...answers.value }
  for (const d of qDefs.value) {
    const required = d.required === 1 || d.required === true
    if (d.type === '多选') {
      const arr = multiAnswers.value[d.qid] || []
      if (required && !arr.length) return ElMessage.warning(`第${qDefs.value.indexOf(d) + 1}题为必答题`)
      if (arr.length) payloadAnswers[d.qid] = arr.join(',')
      else delete payloadAnswers[d.qid]
    } else if (required && (payloadAnswers[d.qid] === undefined || payloadAnswers[d.qid] === '' || payloadAnswers[d.qid] === 0)) {
      return ElMessage.warning(`第${qDefs.value.indexOf(d) + 1}题为必答题`)
    }
  }
  submitting.value = true
  try {
    await axios.post(`/api/culture/survey-mine/${cur.value.id}/submit`, {
      answers: payloadAnswers,
      userDept: userDept.value.trim()
    })
    localStorage.setItem('surveyDept', userDept.value.trim())
    ElMessage.success('答卷提交成功')
    ansVis.value = false
    load()
  } finally { submitting.value = false }
}

onMounted(load)
</script>

<style scoped>
.q-card { margin-bottom: 14px; }
.q-title { font-size: 15px; font-weight: 600; margin-bottom: 10px; }
.q-tags { display: flex; gap: 6px; margin-bottom: 10px; flex-wrap: wrap; }
.q-line { color: #666; font-size: 12.5px; line-height: 1.9; }
.q-foot { margin-top: 8px; display: flex; align-items: center; gap: 10px; }
.q-anon { color: #888; font-size: 12px; }
.q-item { margin-bottom: 10px; }
.q-stem { font-weight: 600; margin-bottom: 8px; }
.q-body { padding-left: 4px; }
.req { color: #f56c6c; margin-left: 2px; }
.ml { margin-left: 8px; }
.mb { margin-bottom: 12px; }
.rate-line { display: flex; align-items: center; gap: 14px; }
.rate-tip { color: #909399; font-size: 12.5px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">三级审核 · 角色分离 · 互斥校验 · 紧急通道</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane label="待审稿件" name="pending">
          <div class="bar">
            <span class="tip">待审稿件列表（按当前审核级别分级处理）</span>
            <el-button v-if="can('R02','R03')" size="small" type="danger" @click="openEmergency">紧急发布</el-button>
          </div>
          <el-table :data="pending" size="small" border v-loading="loading">
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="栏目" width="110"><template #default="{ row }">{{ colName(row.columnId) }}</template></el-table-column>
            <el-table-column prop="author" label="作者" width="90" />
            <el-table-column prop="currentReviewLevel" label="当前审核" width="100">
              <template #default="{ row }"><el-tag size="small" type="warning">{{ row.currentReviewLevel }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="160" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02','R04','R03')" size="small" link type="primary" @click="openReview(row)">审核</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="审核记录" name="history">
          <div class="bar">
            <el-select v-model="selArticleId" placeholder="选择稿件" filterable size="small" style="width:280px" @change="loadHistory">
              <el-option v-for="a in allArticles" :key="a.id" :label="a.title" :value="a.id" />
            </el-select>
          </div>
          <el-table :data="history" size="small" border>
            <el-table-column prop="reviewType" label="审核级别" width="100" />
            <el-table-column prop="reviewer" label="审核人" width="100" />
            <el-table-column label="结果" width="90">
              <template #default="{ row }"><el-tag size="small" :type="row.result==='通过'?'success':'danger'">{{ row.result }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="opinion" label="意见" min-width="200" show-overflow-tooltip />
            <el-table-column prop="reviewTime" label="时间" width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="reviewVisible" title="稿件审核" width="560px">
      <el-form label-width="100px">
        <el-form-item label="标题">{{ reviewForm.title }}</el-form-item>
        <el-form-item label="作者">{{ reviewForm.author }}</el-form-item>
        <el-form-item label="当前级别"><el-tag type="warning">{{ reviewForm.currentReviewLevel }}</el-tag></el-form-item>
        <el-form-item label="审核级别" required>
          <el-select v-model="reviewForm.reviewType" style="width:100%">
            <el-option label="初审" value="初审" />
            <el-option label="复审" value="复审" />
            <el-option label="终审" value="终审" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="reviewForm.pass">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" required>
          <el-input v-model="reviewForm.opinion" type="textarea" :rows="3" placeholder="E-NEWS-09：审核意见不能为空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="emergencyVisible" title="紧急发布" width="480px">
      <el-form label-width="100px">
        <el-form-item label="选择稿件">
          <el-select v-model="emergencyForm.articleId" filterable style="width:100%">
            <el-option v-for="a in pending" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急原因" required>
          <el-input v-model="emergencyForm.reason" type="textarea" :rows="3" placeholder="E-NEWS-13：紧急发布原因必填，事后补审" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emergencyVisible=false">取消</el-button>
        <el-button type="danger" @click="submitEmergency">紧急发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/news/review'
const ART_API = '/api/party/news/article'
const COL_API = '/api/party/news/column'
const tab = ref('pending')
const pending = ref([])
const history = ref([])
const allArticles = ref([])
const columns = ref([])
const loading = ref(false)
const selArticleId = ref(null)
const reviewVisible = ref(false)
const reviewForm = reactive({})
const emergencyVisible = ref(false)
const emergencyForm = reactive({})

function colName(id) { const c = columns.value.find(x => x.id === id); return c ? c.name : '-' }

async function loadPending() {
  loading.value = true
  try {
    const res = await axios.get(`${ART_API}/list`, { params: { status: '待审' } })
    if (res.data.code === 200) pending.value = res.data.data
  } finally { loading.value = false }
}
async function loadColumns() {
  const res = await axios.get(`${COL_API}/list`)
  if (res.data.code === 200) columns.value = res.data.data
}
async function loadAllArticles() {
  const res = await axios.get(`${ART_API}/list`)
  if (res.data.code === 200) allArticles.value = res.data.data
}
async function loadHistory() {
  if (!selArticleId.value) { history.value = []; return }
  const res = await axios.get(`${API}/list`, { params: { articleId: selArticleId.value } })
  if (res.data.code === 200) history.value = res.data.data
}
function openReview(row) {
  Object.keys(reviewForm).forEach(k => delete reviewForm[k])
  Object.assign(reviewForm, { id: row.id, title: row.title, author: row.author, currentReviewLevel: row.currentReviewLevel, reviewType: row.currentReviewLevel, pass: true, opinion: '' })
  reviewVisible.value = true
}
async function submitReview() {
  if (!reviewForm.opinion) { ElMessage.warning('审核意见不能为空'); return }
  try {
    await axios.post(`${API}/${reviewForm.id}`, null, { params: { reviewType: reviewForm.reviewType, pass: reviewForm.pass, opinion: reviewForm.opinion } })
    ElMessage.success('审核已提交')
    reviewVisible.value = false
    loadPending()
  } catch {}
}
function openEmergency() {
  Object.keys(emergencyForm).forEach(k => delete emergencyForm[k])
  Object.assign(emergencyForm, { articleId: null, reason: '' })
  emergencyVisible.value = true
}
async function submitEmergency() {
  if (!emergencyForm.articleId || !emergencyForm.reason) { ElMessage.warning('稿件和原因必填'); return }
  try {
    await axios.post(`${API}/${emergencyForm.articleId}/emergency`, null, { params: { reason: emergencyForm.reason } })
    ElMessage.success('紧急发布成功')
    emergencyVisible.value = false
    loadPending()
  } catch {}
}
onMounted(() => { loadColumns(); loadPending(); loadAllArticles() })
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.tip { color: #909399; font-size: 13px; }
</style>

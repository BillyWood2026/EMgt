<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">审批待办：按当前节点角色过滤 · 超72小时未审红色预警 · 驳回意见必填留痕</el-tag>
    </RoleBar>

    <el-alert v-if="overdue.length" type="error" :closable="false" class="mt"
      :title="`预警：${overdue.length} 条稿件提交已超72小时未审批，请尽快处理`" show-icon />

    <el-table :data="rows" size="small" border v-loading="loading" class="mt">
      <el-table-column prop="newsNo" label="编号" width="150" />
      <el-table-column label="标题" min-width="200" show-overflow-tooltip>
        <template #default="{row}">
          <el-tag v-if="isOverdue(row)" size="small" type="danger" effect="dark" style="margin-right:4px">超72h</el-tag>
          <span>{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="newsType" label="类型" width="90" align="center">
        <template #default="{row}"><el-tag size="small" type="info">{{ row.newsType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="sourceOrg" label="来源组织" width="110" />
      <el-table-column label="当前节点" width="150">
        <template #default="{row}"><span class="node">{{ stageNode(row) }}</span></template>
      </el-table-column>
      <el-table-column prop="creator" label="撰稿人" width="90" />
      <el-table-column prop="submitTime" label="提交时间" width="150" />
      <el-table-column label="停留时长" width="110" align="center">
        <template #default="{row}">
          <span :class="isOverdue(row) ? 'overdue' : 'wait'">{{ waitText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="success" link @click="doApprove(row)">通过</el-button>
          <el-button size="small" type="danger" link @click="openReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !rows.length" description="当前节点暂无待审稿件" />

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showReject" title="驳回稿件" width="480px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="稿件" :span="2">{{ rejectRow?.title }}</el-form-item>
        <el-form-item label="驳回意见" required>
          <el-input v-model="rejectOpinion" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="必填20-500字，将随审批记录永久留痕" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="showReject = false">取消</el-button>
        <el-button size="small" type="danger" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const loading = ref(false)
const rows = ref([])

const overdue = computed(() => rows.value.filter(isOverdue))

const showReject = ref(false)
const rejectRow = ref(null)
const rejectOpinion = ref('')

function stageNode(row) {
  const hq = row.sourceOrg === '集团总部'
  const s = row.stage || 0
  if (hq) return s === 1 ? '集团文化管理员审核' : '分管高管终审'
  return ['子公司负责人初审', '集团文化管理员复核', '集团高管终审'][s - 1] || ''
}

function submitTs(row) {
  return row.submitTime ? new Date(row.submitTime.replace(' ', 'T')).getTime() : 0
}

function isOverdue(row) {
  return submitTs(row) > 0 && (Date.now() - submitTs(row)) > 72 * 3600 * 1000
}

function waitText(row) {
  const h = Math.floor((Date.now() - submitTs(row)) / 3600000)
  if (h < 1) return '不足1小时'
  if (h < 24) return h + ' 小时'
  return Math.floor(h / 24) + ' 天 ' + (h % 24) + ' 小时'
}

function load() {
  loading.value = true
  axios.get('/api/culture/news/todo')
    .then(r => { rows.value = r.data.data || [] })
    .catch(e => ElMessage.error(e.response?.data?.message || '加载失败'))
    .finally(() => { loading.value = false })
}

function doApprove(row) {
  ElMessageBox.confirm(`确认在节点【${stageNode(row)}】通过《${row.title}》？`, '审批确认', { type: 'success' })
    .then(() => axios.post(`/api/culture/news/${row.id}/approve?pass=true`))
    .then(r => {
      const d = r.data.data
      ElMessage.success(d.status === '已发布' ? '终审通过，稿件已发布' : '已通过，流转至下一节点')
      load()
    })
    .catch(e => { if (e?.response) ElMessage.error(e.response?.data?.message || '操作失败') })
}

function openReject(row) {
  rejectRow.value = row
  rejectOpinion.value = ''
  showReject.value = true
}

function doReject() {
  const len = rejectOpinion.value.trim().length
  if (len < 20 || len > 500) { ElMessage.warning('驳回意见必填且需20-500字'); return }
  axios.post(`/api/culture/news/${rejectRow.value.id}/approve?pass=false&opinion=${encodeURIComponent(rejectOpinion.value.trim())}`)
    .then(() => { ElMessage.success('已驳回退回草稿，意见已留痕'); showReject.value = false; load() })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.node { color: #409eff; font-size: 12px; }
.overdue { color: #f56c6c; font-weight: 600; }
.wait { color: #909399; }
</style>

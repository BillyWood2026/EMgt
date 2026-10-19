<template>
  <div class="page-container">
    <RoleBar />
    <div class="page-header">
      <h2>党费公示管理</h2>
    </div>

    <div class="toolbar">
      <el-input-number v-model="filter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
      <el-input-number v-model="filter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:150px" />
      <el-button type="primary" @click="loadList">查询</el-button>
      <el-button v-if="can('R02','R12')" type="primary" style="margin-left:auto" @click="openCreateDialog">新建公示</el-button>
    </div>

    <el-table :data="list" border stripe size="small" v-loading="loading">
      <el-table-column prop="orgName" label="组织名称" min-width="160" />
      <el-table-column prop="feeYear" label="费属年度" width="100" align="center" />
      <el-table-column prop="period" label="公示周期" width="110" align="center" />
      <el-table-column label="公示时段" min-width="200">
        <template #default="{ row }">{{ row.periodStart }} ~ {{ row.periodEnd }}</template>
      </el-table-column>
      <el-table-column prop="totalCollected" label="收缴合计" width="110" align="right" />
      <el-table-column prop="totalTurnedOver" label="上缴合计" width="110" align="right" />
      <el-table-column prop="totalUsed" label="使用合计" width="110" align="right" />
      <el-table-column prop="balance" label="结余" width="110" align="right" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishDate" label="发布日期" width="110" align="center" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="can('R02','R12') && row.status !== 'PUBLISHED'"
            size="small"
            type="primary"
            link
            @click="publish(row)"
          >发布</el-button>
        </template>
      </el-table-column>
    </el-table>

    <h3 class="section-title">未公示支部</h3>
    <el-table :data="todoList" border stripe size="small" v-loading="todoLoading">
      <el-table-column prop="orgId" label="组织ID" width="100" align="center" />
      <el-table-column prop="orgName" label="支部名称" min-width="180" />
      <el-table-column prop="message" label="提示信息" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.message || `${filter.year} 年度党费收支尚未公示（E-DU-10）` }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建公示弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建党费公示" width="560px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="110px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="createForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model="createForm.orgName" />
        </el-form-item>
        <el-form-item label="费属年度" required>
          <el-input-number v-model="createForm.feeYear" :min="2000" :max="2100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="公示开始" required>
          <el-date-picker v-model="createForm.periodStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="公示结束" required>
          <el-date-picker v-model="createForm.periodEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="公示范围">
          <el-input v-model="createForm.publicScope" placeholder="默认：全体党员" />
        </el-form-item>
        <el-form-item label="附件">
          <el-input v-model="createForm.attachment" placeholder="公示材料附件" />
        </el-form-item>
        <el-form-item>
          <span class="form-tip">保存后金额自动从该组织当年度收支汇总（year-summary）取值。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/membership/dues-publication'

const filter = reactive({ year: new Date().getFullYear(), orgId: null })
const list = ref([])
const loading = ref(false)
const todoList = ref([])
const todoLoading = ref(false)

const createDialogVisible = ref(false)
const createForm = reactive({
  orgId: null,
  orgName: '',
  feeYear: new Date().getFullYear(),
  periodStart: '',
  periodEnd: '',
  publicScope: '全体党员',
  attachment: ''
})

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get(`${API}/list`, {
      params: { year: filter.year || undefined, orgId: filter.orgId || undefined }
    })
    list.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
  loadTodo()
}

async function loadTodo() {
  todoLoading.value = true
  try {
    const res = await axios.get(`${API}/todo`, { params: { year: filter.year || undefined } })
    todoList.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    todoLoading.value = false
  }
}

function openCreateDialog() {
  Object.assign(createForm, {
    orgId: filter.orgId,
    orgName: '',
    feeYear: filter.year || new Date().getFullYear(),
    periodStart: '',
    periodEnd: '',
    publicScope: '全体党员',
    attachment: ''
  })
  createDialogVisible.value = true
}

async function submitCreate() {
  try {
    await axios.post(API, {
      orgId: createForm.orgId,
      orgName: createForm.orgName,
      feeYear: createForm.feeYear,
      periodStart: createForm.periodStart || undefined,
      periodEnd: createForm.periodEnd || undefined,
      publicScope: createForm.publicScope || undefined,
      attachment: createForm.attachment || undefined
    })
    ElMessage.success('新建公示成功')
    createDialogVisible.value = false
    loadList()
  } catch (e) {
    console.error(e)
  }
}

async function publish(row) {
  try {
    await ElMessageBox.confirm(
      `确定发布「${row.orgName} ${row.feeYear} 年度」党费公示吗？同年同组织仅允许一条已发布记录。`,
      '发布确认',
      { confirmButtonText: '发布', cancelButtonText: '取消', type: 'warning' }
    )
    await axios.post(`${API}/publish/${row.id}`)
    ElMessage.success('发布成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function statusLabel(s) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }
  return map[s] || s || ''
}
function statusTagType(s) {
  const map = { DRAFT: 'info', PUBLISHED: 'success', ARCHIVED: 'info' }
  return map[s] || 'info'
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  color: #303133;
}

.form-tip {
  color: #909399;
  font-size: 12px;
}
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>计划模板与考核规则配置</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar :columns="ioColumns" :rows="tableData" file-name="计划模板" title="计划模板与考核规则" />
        <el-button type="primary" @click="handleAdd">新增模板</el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-top:12px"
      title="模板统一维护集团级计划标准：计划周期模板、任务拆解规则、计划权重、完成率核算公式、逾期扣分规则、考核分值标准；权重合计超100%拦截保存，未配置考核扣分标准禁止模板生效" />

    <el-table :data="tableData" border stripe size="small" v-loading="loading" style="margin-top:16px">
      <el-table-column prop="templateCode" label="模板编号" width="130" />
      <el-table-column prop="templateName" label="模板名称" min-width="180" />
      <el-table-column prop="planType" label="计划周期类型" width="110">
        <template #default="{row}">
          <el-tag size="small">{{ row.planType }}</el-tag>
        </template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="applyOrg" label="适用组织" width="130">
        <template #default="{row}">{{ row.applyOrg || '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="applySequence" label="适用岗位序列" width="120">
        <template #default="{row}">{{ row.applySequence || '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="maxLevel" label="任务层级数" width="90">
        <template #default="{row}">{{ row.maxLevel ?? '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="totalWeight" label="总分权重" width="90">
        <template #default="{row}">{{ row.totalWeight != null ? row.totalWeight + '%' : '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="overdueDeduct" label="逾期扣分标准" width="110">
        <template #default="{row}">{{ row.overdueDeduct || '-' }}</template>
      </el-table-column>
      <!-- 规格书字段，待后端扩展 -->
      <el-table-column prop="nodeCheckOn" label="节点考核" width="90">
        <template #default="{row}">
          <el-tag v-if="row.nodeCheckOn != null" size="small" :type="row.nodeCheckOn ? 'success' : 'info'">{{ row.nodeCheckOn ? '开' : '关' }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="规则生效状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{row}">
          <el-button link size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link size="small" @click="viewRule(row)">查看规则</el-button>
          <el-button link size="small" type="warning" @click="copyTemplate(row)">复制模板</el-button>
          <el-button link size="small" type="danger" v-if="row.status === '启用'" @click="disableTemplate(row)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 模板编辑弹窗（规格书950px） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="950px">
      <el-form :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="模板编号" required>
              <el-input v-model="formData.templateCode" placeholder="请输入模板编号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板名称" required>
              <el-input v-model="formData.templateName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划周期类型" required>
              <el-select v-model="formData.planType" placeholder="请选择计划类型" style="width:100%">
                <el-option label="年度" value="年度" />
                <el-option label="季度" value="季度" />
                <el-option label="月度" value="月度" />
                <el-option label="周" value="周" />
                <el-option label="专项" value="专项" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="适用组织">
              <el-input v-model="formData.applyOrg" placeholder="例如：集团总部" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="适用岗位序列">
              <el-select v-model="formData.applySequence" placeholder="选择序列" style="width:100%">
                <el-option label="管理序列" value="管理序列" />
                <el-option label="技术研发序列" value="技术研发序列" />
                <el-option label="职能支撑序列" value="职能支撑序列" />
                <el-option label="业务营销序列" value="业务营销序列" />
                <el-option label="生产操作序列" value="生产操作序列" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="任务层级数">
              <el-input-number v-model="formData.maxLevel" :min="1" :max="5" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="总分权重%">
              <el-input-number v-model="formData.totalWeight" :min="0" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="逾期扣分标准">
              <el-input v-model="formData.overdueDeduct" placeholder="例如：战略任务逾期扣3分" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 规格书字段，待后端扩展 -->
            <el-form-item label="节点考核开关">
              <el-switch v-model="formData.nodeCheckOn" active-text="开" inactive-text="关" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="模板结构">
          <el-input v-model="formData.templateContent" type="textarea" :rows="3" placeholder="计划层级拆解规则：最大拆解层级、子任务权重分配规则、前置依赖校验规则、里程碑节点必填项" />
        </el-form-item>
        <el-form-item label="默认WBS">
          <el-input v-model="formData.defaultWbs" type="textarea" :rows="3" placeholder="默认WBS任务结构" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width:100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- EL-R01/R02 规则查看弹窗 -->
    <el-dialog v-model="ruleVisible" title="模板规则明细" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="模板编号">{{ currentRow.templateCode }}</el-descriptions-item>
        <el-descriptions-item label="模板名称">{{ currentRow.templateName }}</el-descriptions-item>
        <el-descriptions-item label="计划周期类型">{{ currentRow.planType }}</el-descriptions-item>
        <el-descriptions-item label="适用组织">{{ currentRow.applyOrg || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适用岗位序列">{{ currentRow.applySequence || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务层级数">{{ currentRow.maxLevel ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="总分权重">{{ currentRow.totalWeight != null ? currentRow.totalWeight + '%' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="逾期扣分标准">{{ currentRow.overdueDeduct || '-' }}</el-descriptions-item>
        <el-descriptions-item label="节点考核开关">{{ currentRow.nodeCheckOn ? '开' : '关' }}</el-descriptions-item>
        <el-descriptions-item label="生效状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="模板结构" :span="2">{{ currentRow.templateContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="默认WBS" :span="2">{{ currentRow.defaultWbs || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="ruleVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { workplanApi } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const ruleVisible = ref(false)
const dialogTitle = ref('新增计划模板')
const currentRow = ref({})
const formData = reactive({
  id: null,
  templateCode: "",
  templateName: "",
  planType: "年度",
  applyOrg: "",
  applySequence: "",
  maxLevel: 3,
  totalWeight: 100,
  overdueDeduct: "",
  nodeCheckOn: true,
  templateContent: "",
  defaultWbs: "",
  status: "启用",
})

const ioColumns = [
  { key: 'templateCode', title: '模板编号' },
  { key: 'templateName', title: '模板名称' },
  { key: 'planType', title: '计划周期类型' },
  { key: 'applyOrg', title: '适用组织' },
  { key: 'applySequence', title: '适用岗位序列' },
  { key: 'maxLevel', title: '任务层级数' },
  { key: 'totalWeight', title: '总分权重' },
  { key: 'overdueDeduct', title: '逾期扣分标准' },
  { key: 'status', title: '规则生效状态' },
  { key: 'createTime', title: '创建时间' }
]

function resetForm() {
  Object.assign(formData, {
    id: null, templateCode: "", templateName: "", planType: "年度",
    applyOrg: "", applySequence: "", maxLevel: 3, totalWeight: 100,
    overdueDeduct: "", nodeCheckOn: true, templateContent: "", defaultWbs: "", status: "启用"
  })
}

async function loadData() {
  loading.value = true
  try {
    const res = await workplanApi.listTemplates()
    if (res.data.code === 200) tableData.value = res.data.data || []
    else ElMessage.error(res.data.message || '加载失败')
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增计划模板'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑计划模板'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function viewRule(row) {
  currentRow.value = row
  ruleVisible.value = true
}

// 复制模板二次确认（规格书弹窗校验）
async function copyTemplate(row) {
  try {
    await ElMessageBox.confirm('复制模板将保留全部规则配置、清空任务数据，是否继续？', '复制模板确认', { type: 'warning' })
    const payload = { ...row, id: null, templateCode: row.templateCode + '-COPY', templateName: row.templateName + '（副本）' }
    const res = await workplanApi.saveTemplate(payload)
    if (res.data.code === 200) {
      ElMessage.success('模板已复制')
      loadData()
    } else {
      ElMessage.error(res.data.message || '复制失败')
    }
  } catch (e) {}
}

async function disableTemplate(row) {
  try {
    await ElMessageBox.confirm(`确定要作废模板「${row.templateName}」吗？作废后不可用于新计划编制。`, '作废确认', { type: 'warning' })
    const res = await workplanApi.updateTemplate(row.id, { ...row, status: '停用' })
    if (res.data.code === 200) {
      ElMessage.success('模板已作废')
      loadData()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {}
}

async function handleSubmit() {
  if (!formData.templateCode || !formData.templateName) {
    ElMessage.warning('请填写模板编号与模板名称')
    return
  }
  // 权重合计校验：超100%拦截保存
  if (formData.totalWeight != null && formData.totalWeight > 100) {
    ElMessage.error('权重配置合计超100%，禁止保存')
    return
  }
  // 未配置考核扣分标准禁止模板生效
  if (formData.status === '启用' && !formData.overdueDeduct) {
    ElMessage.error('未配置考核扣分标准，禁止模板生效')
    return
  }
  try {
    const res = await workplanApi.saveTemplate(formData)
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (e) { ElMessage.error('保存失败') }
}

onMounted(loadData)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>

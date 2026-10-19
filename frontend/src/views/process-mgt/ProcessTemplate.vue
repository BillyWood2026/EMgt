<template>
  <div class="page-card">
    <div class="page-header">
      <h2>流程模板与制度模板管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddTemplate">
          <el-icon><Plus /></el-icon>新增模板
        </el-button>
      </div>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="制度模板包含9个标准章节（目的/范围/权责/定义/管理内容/流程/考核/附则/附件），流程模板支持流程图设计与节点权责配置。" />

    <el-tabs v-model="activeTab">
      <!-- 制度模板库 -->
      <el-tab-pane label="制度模板库" name="document">
        <el-card shadow="never" style="margin-bottom:12px">
          <el-form :inline="true" :model="search" @submit.prevent>
            <el-form-item label="模板名称"><el-input v-model="search.docName" placeholder="搜索模板名称" clearable style="width:200px" /></el-form-item>
            <el-form-item label="分类">
              <el-select v-model="search.docCategory" clearable placeholder="全部" style="width:160px">
                <el-option label="全部" value="" />
                <el-option label="体系手册" value="体系手册" />
                <el-option label="程序文件" value="程序文件" />
                <el-option label="管理制度" value="管理制度" />
                <el-option label="作业SOP" value="作业SOP" />
                <el-option label="表单模板" value="表单模板" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用体系">
              <el-select v-model="search.docSystem" clearable placeholder="全部" style="width:140px">
                <el-option label="全部" value="" />
                <el-option label="质量管理体系" value="质量管理体系" />
                <el-option label="安全生产体系" value="安全生产体系" />
                <el-option label="环境管理体系" value="环境管理体系" />
                <el-option label="职业健康体系" value="职业健康体系" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="search.docStatus" clearable placeholder="全部" style="width:120px">
                <el-option label="全部" value="" />
                <el-option label="启用" value="启用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doSearch">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
              <el-button type="success" @click="importTemplate">导入模板</el-button>
              <el-button type="info" @click="exportTemplate">导出模板</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never">
          <el-table :data="documentTemplates" border stripe size="small">
            <el-table-column prop="name" label="模板名称" min-width="180" />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{row}"><el-tag size="small" type="primary">{{ row.category }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="system" label="适用体系" width="130" />
            <el-table-column prop="scope" label="适用范围" width="150" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column label="状态" width="80">
              <template #default="{row}"><el-tag size="small" :type="row.status === '启用' ? 'success' : 'warning'">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="useCount" label="使用次数" width="90" align="center" />
            <el-table-column prop="updateTime" label="更新时间" width="160" />
            <el-table-column label="操作" width="320">
              <template #default="{row}">
                <el-button link size="small" @click="viewDocTemplate(row)">查看</el-button>
                <el-button link size="small" type="primary" @click="editDocTemplate(row)">编辑</el-button>
                <el-button link size="small" @click="previewDoc(row)">预览</el-button>
                <el-button link size="small" @click="copyDocTemplate(row)">复制</el-button>
                <el-button link size="small" type="success" @click="useDocTemplate(row)">立即使用</el-button>
                <el-button link size="small" type="danger" @click="deleteDocTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 流程模板库 -->
      <el-tab-pane label="流程模板库" name="flow">
        <el-card shadow="never" style="margin-bottom:12px">
          <el-form :inline="true" :model="flowSearch" @submit.prevent>
            <el-form-item label="模板名称"><el-input v-model="flowSearch.name" placeholder="搜索模板名称" clearable style="width:200px" /></el-form-item>
            <el-form-item label="分类">
              <el-select v-model="flowSearch.category" clearable placeholder="全部" style="width:160px">
                <el-option label="全部" value="" />
                <el-option label="审批流程" value="审批流程" />
                <el-option label="业务流程" value="业务流程" />
                <el-option label="管控流程" value="管控流程" />
                <el-option label="作业流程" value="作业流程" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="flowSearch.status" clearable placeholder="全部" style="width:120px">
                <el-option label="全部" value="" />
                <el-option label="启用" value="启用" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doFlowSearch">查询</el-button>
              <el-button @click="resetFlowSearch">重置</el-button>
              <el-button type="success" @click="importFlowTemplate">导入流程</el-button>
              <el-button type="info" @click="exportFlowTemplate">导出流程</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never">
          <el-table :data="flowTemplates" border stripe size="small">
            <el-table-column prop="name" label="模板名称" min-width="180" />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="{row}"><el-tag size="small" type="primary">{{ row.category }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="nodeCount" label="节点数" width="80" align="center" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column label="状态" width="80">
              <template #default="{row}"><el-tag size="small" :type="row.status === '启用' ? 'success' : 'warning'">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="useCount" label="使用次数" width="90" align="center" />
            <el-table-column prop="updateTime" label="更新时间" width="160" />
            <el-table-column label="操作" width="320">
              <template #default="{row}">
                <el-button link size="small" @click="viewFlowTemplate(row)">查看</el-button>
                <el-button link size="small" type="primary" @click="editFlowTemplate(row)">编辑</el-button>
                <el-button link size="small" @click="previewFlow(row)">预览</el-button>
                <el-button link size="small" @click="copyFlowTemplate(row)">复制</el-button>
                <el-button link size="small" type="success" @click="useFlowTemplate(row)">立即使用</el-button>
                <el-button link size="small" type="danger" @click="deleteFlowTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 流程图模板库 -->
      <el-tab-pane label="流程图模板" name="flowchart">
        <el-card shadow="never">
          <el-row :gutter="20">
            <el-col :span="6" v-for="tmpl in flowchartTemplates" :key="tmpl.id">
              <el-card shadow="hover" class="flowchart-card">
                <div class="flowchart-preview">
                  <el-icon :size="48"><component :is="tmpl.icon" /></el-icon>
                </div>
                <h4>{{ tmpl.name }}</h4>
                <p class="flowchart-desc">{{ tmpl.description }}</p>
                <div class="flowchart-meta">
                  <el-tag size="small" type="primary">{{ tmpl.category }}</el-tag>
                  <span>使用 {{ tmpl.useCount }} 次</span>
                </div>
                <div class="flowchart-actions">
                  <el-button size="small" @click="previewFlowchart(tmpl)">预览</el-button>
                  <el-button size="small" type="primary" @click="useFlowchartTemplate(tmpl)">使用</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 制度模板详情弹窗 -->
    <el-dialog v-model="docDetailVisible" title="制度模板详情" width="800px">
      <el-card v-if="currentDocTemplate" shadow="never" style="margin-bottom:12px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="模板名称">{{ currentDocTemplate.name }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ currentDocTemplate.version }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentDocTemplate.category }}</el-descriptions-item>
          <el-descriptions-item label="适用范围">{{ currentDocTemplate.scope }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDocTemplate.status === '启用' ? 'success' : 'warning'">{{ currentDocTemplate.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentDocTemplate.owner }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDocTemplate.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentDocTemplate.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never">
        <template #header><b>制度章节内容（9个标准章节）</b></template>
        
        <div class="chapter-section" v-for="(chapter, idx) in currentDocTemplate?.chapters" :key="idx">
          <div class="chapter-header" @click="toggleChapter(idx)">
            <el-icon :component="chapterExpanded[idx] ? Minus : Plus" />
            <span><b>{{ chapter.title }}</b></span>
          </div>
          <div v-show="chapterExpanded[idx]" class="chapter-content">
            <p>{{ chapter.content }}</p>
          </div>
        </div>
      </el-card>
    </el-dialog>

    <!-- 流程模板详情弹窗 -->
    <el-dialog v-model="flowDetailVisible" title="流程模板详情" width="800px">
      <el-card v-if="currentFlowTemplate" shadow="never" style="margin-bottom:12px">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="模板名称">{{ currentFlowTemplate.name }}</el-descriptions-item>
          <el-descriptions-item label="版本号">{{ currentFlowTemplate.version }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentFlowTemplate.category }}</el-descriptions-item>
          <el-descriptions-item label="节点数">{{ currentFlowTemplate.nodeCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentFlowTemplate.status === '启用' ? 'success' : 'warning'">{{ currentFlowTemplate.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentFlowTemplate.owner }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" style="margin-bottom:12px">
        <template #header><b>流程图</b></template>
        <div class="flow-diagram">
          <div class="flow-start">开始</div>
          <div class="flow-arrow">→</div>
          <div class="flow-node approve">审批节点1</div>
          <div class="flow-arrow">→</div>
          <div class="flow-node execute">执行节点</div>
          <div class="flow-arrow">→</div>
          <div class="flow-node decision">判断节点</div>
          <div class="flow-arrow">→</div>
          <div class="flow-end">结束</div>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header><b>流程说明</b></template>
        <p>{{ currentFlowTemplate?.description }}</p>
      </el-card>

      <el-card shadow="never">
        <template #header><b>节点权责配置</b></template>
        <el-table :data="currentFlowTemplate?.nodes" border size="small">
          <el-table-column prop="name" label="节点名称" width="120" />
          <el-table-column prop="type" label="节点类型" width="100">
            <template #default="{row}"><el-tag size="small" :type="getNodeTypeColor(row.type)">{{ row.type }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="handler" label="处理人" width="120" />
          <el-table-column prop="role" label="角色权限" width="150" />
          <el-table-column prop="timeout" label="超时时间" width="100" />
          <el-table-column prop="description" label="说明" />
        </el-table>
      </el-card>
    </el-dialog>

    <!-- 新增模板弹窗 -->
    <el-dialog v-model="addTemplateVisible" :title="isEditTemplate ? '编辑模板' : '新增模板'" width="720px">
      <el-form label-width="100px">
        <el-form-item label="模板类型">
          <el-select v-model="templateForm.type" @change="onTemplateTypeChange">
            <el-option label="制度模板" value="document" />
            <el-option label="流程模板" value="flow" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="templateForm.category">
            <el-option v-for="cat in templateCategories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用范围">
          <el-input v-model="templateForm.scope" placeholder="如：公司全体员工" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="templateForm.owner" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="templateForm.version" placeholder="如：V1.0" />
        </el-form-item>
        
        <el-divider v-if="templateForm.type === 'document'" />
        
        <div v-if="templateForm.type === 'document'" style="max-height:400px;overflow-y:auto">
          <el-form-item v-for="(chapter, idx) in templateForm.chapters" :key="idx" :label="chapter.title">
            <el-input type="textarea" v-model="chapter.content" :rows="3" :placeholder="'请输入' + chapter.title" />
          </el-form-item>
        </div>

        <div v-else>
          <el-form-item label="流程描述">
            <el-input type="textarea" v-model="templateForm.description" :rows="3" placeholder="请描述流程说明" />
          </el-form-item>
          <el-form-item label="节点配置">
            <el-table :data="templateForm.nodes" border size="small">
              <el-table-column label="节点名称">
                <template #default="{row}"><el-input v-model="row.name" size="small" /></template>
              </el-table-column>
              <el-table-column label="类型">
                <template #default="{row}">
                  <el-select v-model="row.type" size="small">
                    <el-option label="开始" value="开始" />
                    <el-option label="审批" value="审批" />
                    <el-option label="执行" value="执行" />
                    <el-option label="判断" value="判断" />
                    <el-option label="结束" value="结束" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="处理人">
                <template #default="{row}"><el-input v-model="row.handler" size="small" /></template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default>
                  <el-button link size="small" type="danger">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button size="small" @click="addFlowNode">新增节点</el-button>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="addTemplateVisible=false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'

const activeTab = ref('document')
const search = reactive({ docName: '', docCategory: '', docStatus: '' })
const flowSearch = reactive({ name: '', category: '' })

const docDetailVisible = ref(false)
const flowDetailVisible = ref(false)
const addTemplateVisible = ref(false)
const isEditTemplate = ref(false)
const editingTemplateId = ref(null)

const currentDocTemplate = ref(null)
const currentFlowTemplate = ref(null)

const chapterExpanded = ref([])

const templateCategories = ['人事管理', '行政管理', '财务管理', '生产管理', '审批流程', '业务流程']

const templateForm = reactive({
  type: 'document',
  name: '',
  category: '',
  scope: '',
  owner: '',
  version: 'V1.0',
  description: '',
  chapters: [
    { title: '1. 目的', content: '' },
    { title: '2. 适用范围', content: '' },
    { title: '3. 职责与权限', content: '' },
    { title: '4. 术语定义', content: '' },
    { title: '5. 管理内容', content: '' },
    { title: '6. 工作流程', content: '' },
    { title: '7. 考核与监督', content: '' },
    { title: '8. 附则', content: '' },
    { title: '9. 附件', content: '' }
  ],
  nodes: []
})

const documentTemplates = ref([])

const flowTemplates = ref([])

const flowchartTemplates = ref([
  { id: 1, name: '通用审批流程', category: '审批流程', description: '适用于一般审批场景，包含发起、审批、结束节点', icon: 'Connection', useCount: 256 },
  { id: 2, name: '判断分支流程', category: '审批流程', description: '包含条件判断节点，支持多分支流转', icon: 'Guide', useCount: 189 },
  { id: 3, name: '并行审批流程', category: '审批流程', description: '支持多节点并行审批后汇总', icon: 'Operation', useCount: 145 },
  { id: 4, name: '循环执行流程', category: '业务流程', description: '适用于需要循环执行的业务流程', icon: 'Refresh', useCount: 78 },
  { id: 5, name: '子流程调用', category: '业务流程', description: '支持调用子流程的复合流程', icon: 'Box', useCount: 92 },
  { id: 6, name: '抄送知会流程', category: '管控流程', description: '包含抄送节点的审批流程', icon: 'Message', useCount: 67 },
  { id: 7, name: '会签审批流程', category: '审批流程', description: '多人会签场景，全部通过才能继续', icon: 'User', useCount: 134 },
  { id: 8, name: '或签审批流程', category: '审批流程', description: '一人通过即生效的审批场景', icon: 'Checked', useCount: 156 }
])

function toggleChapter (idx) {
  chapterExpanded.value[idx] = !chapterExpanded.value[idx]
}

function getNodeTypeColor (type) {
  const colors = { '开始': 'success', '审批': 'warning', '执行': 'primary', '判断': 'info', '结束': 'danger' }
  return colors[type] || ''
}

function viewDocTemplate (row) {
  currentDocTemplate.value = row
  chapterExpanded.value = row.chapters.map(() => true)
  docDetailVisible.value = true
}

function viewFlowTemplate (row) {
  currentFlowTemplate.value = row
  flowDetailVisible.value = true
}

function editDocTemplate (row) {
  editingTemplateId.value = row.id
  Object.assign(templateForm, {
    type: 'document',
    name: row.name,
    category: row.category,
    scope: row.scope,
    owner: row.owner,
    version: row.version,
    chapters: row.chapters || templateForm.chapters.map(c => ({ ...c, content: '' }))
  })
  isEditTemplate.value = true
  addTemplateVisible.value = true
}

function editFlowTemplate (row) {
  editingTemplateId.value = row.id
  Object.assign(templateForm, {
    type: 'flow',
    name: row.name,
    category: row.category,
    scope: '',
    owner: row.owner,
    version: row.version,
    description: row.description || '',
    nodes: row.nodes || []
  })
  isEditTemplate.value = true
  addTemplateVisible.value = true
}

function previewDoc (row) {
  ElMessage.info(`预览制度模板: ${row.name}`)
}

function previewFlow (row) {
  ElMessage.info(`预览流程模板: ${row.name}`)
}

function copyDocTemplate (row) {
  ElMessage.success(`已复制模板: ${row.name}`)
}

function copyFlowTemplate (row) {
  ElMessage.success(`已复制模板: ${row.name}`)
}

function deleteDocTemplate (row) {
  ElMessageBox.confirm('确认删除此模板？', '提示', { type: 'danger' }).then(async () => {
    try {
      const res = await axios.delete(`/api/process/template/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadDocTemplates()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }).catch(() => {})
}

function deleteFlowTemplate (row) {
  ElMessageBox.confirm('确认删除此模板？', '提示', { type: 'danger' }).then(async () => {
    try {
      const res = await axios.delete(`/api/process/template/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadFlowTemplates()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }).catch(() => {})
}

function useDocTemplate (row) {
  ElMessageBox.confirm(`确定要使用模板"${row.name}"创建新文档吗？`, '使用模板', { type: 'info' }).then(() => {
    ElMessage.success(`已基于模板"${row.name}"创建新文档，请完善内容`)
  }).catch(() => {})
}

function useFlowTemplate (row) {
  ElMessageBox.confirm(`确定要使用流程模板"${row.name}"创建新流程吗？`, '使用模板', { type: 'info' }).then(() => {
    ElMessage.success(`已基于流程模板"${row.name}"创建新流程，请完善节点配置`)
  }).catch(() => {})
}

function importTemplate () {
  ElMessage.info('打开模板导入对话框')
}

function exportTemplate () {
  ElMessage.info('导出选中模板')
}

function importFlowTemplate () {
  ElMessage.info('打开流程模板导入对话框')
}

function exportFlowTemplate () {
  ElMessage.info('导出选中流程模板')
}

function previewFlowchart (tmpl) {
  ElMessage.info(`预览流程图模板: ${tmpl.name}`)
}

function useFlowchartTemplate (tmpl) {
  ElMessageBox.confirm(`确定要使用流程图模板"${tmpl.name}"创建新流程图吗？`, '使用模板', { type: 'info' }).then(() => {
    ElMessage.success(`已基于流程图模板"${tmpl.name}"创建新流程图`)
  }).catch(() => {})
}

function openAddTemplate () {
  editingTemplateId.value = null
  templateForm.type = 'document'
  templateForm.name = ''
  templateForm.category = ''
  templateForm.scope = ''
  templateForm.owner = ''
  templateForm.version = 'V1.0'
  templateForm.description = ''
  templateForm.chapters = templateForm.chapters.map(c => ({ ...c, content: '' }))
  templateForm.nodes = []
  isEditTemplate.value = false
  addTemplateVisible.value = true
}

function onTemplateTypeChange (val) {
  if (val === 'flow') {
    templateForm.nodes = [
      { name: '开始', type: '开始', handler: '-', role: '-', timeout: '-', description: '' },
      { name: '审批节点', type: '审批', handler: '', role: '', timeout: '', description: '' },
      { name: '结束', type: '结束', handler: '-', role: '-', timeout: '-', description: '' }
    ]
  }
}

function addFlowNode () {
  templateForm.nodes.push({
    name: '新节点',
    type: '审批',
    handler: '',
    role: '',
    timeout: '',
    description: ''
  })
}

async function saveTemplate () {
  if (!templateForm.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  try {
    if (isEditTemplate.value && editingTemplateId.value) {
      const res = await axios.put(`/api/process/template/${editingTemplateId.value}`, templateForm)
      if (res.data.code === 200) {
        addTemplateVisible.value = false
        ElMessage.success('编辑成功')
        templateForm.type === 'document' ? loadDocTemplates() : loadFlowTemplates()
      } else {
        ElMessage.error(res.data.message || '编辑失败')
      }
    } else {
      const res = await axios.post('/api/process/template', templateForm)
      if (res.data.code === 200) {
        addTemplateVisible.value = false
        ElMessage.success('新增成功')
        templateForm.type === 'document' ? loadDocTemplates() : loadFlowTemplates()
      } else {
        ElMessage.error(res.data.message || '新增失败')
      }
    }
  } catch (e) {
    ElMessage.error('保存失败')
    console.error(e)
  }
}

async function loadDocTemplates () {
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (search.docName) params.keyword = search.docName
    if (search.docCategory) params.category = search.docCategory
    if (search.docStatus) params.status = search.docStatus
    const res = await axios.get('/api/process/template/list', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      documentTemplates.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载制度模板失败')
    console.error(e)
  }
}

async function loadFlowTemplates () {
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (flowSearch.name) params.keyword = flowSearch.name
    if (flowSearch.category) params.category = flowSearch.category
    const res = await axios.get('/api/process/template/list', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      flowTemplates.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载流程模板失败')
    console.error(e)
  }
}

function doSearch () { loadDocTemplates() }
function resetSearch () {
  Object.assign(search, { docName: '', docCategory: '', docStatus: '' })
  loadDocTemplates()
}
function doFlowSearch () { loadFlowTemplates() }
function resetFlowSearch () {
  Object.assign(flowSearch, { name: '', category: '' })
  loadFlowTemplates()
}

onMounted(() => {
  loadDocTemplates()
  loadFlowTemplates()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }

.chapter-section { margin-bottom: 8px; }
.chapter-header { display: flex; align-items: center; gap: 8px; padding: 10px; background: #fafbfc; cursor: pointer; border-radius: 4px; }
.chapter-content { padding: 12px 16px; border-left: 3px solid #409eff; margin-top: 4px; }
.chapter-content p { margin: 0; color: #606266; line-height: 1.6; }

.flow-diagram { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 24px; background: #fafbfc; border-radius: 8px; flex-wrap: wrap; }
.flow-start, .flow-end { width: 60px; height: 60px; border-radius: 50%; background: #67c23a; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }
.flow-end { background: #f56c6c; }
.flow-node { padding: 12px 20px; border-radius: 4px; font-size: 13px; }
.flow-node.approve { background: #e6a23c; color: white; }
.flow-node.execute { background: #409eff; color: white; }
.flow-node.decision { background: #909399; color: white; }
.flow-arrow { color: #909399; font-size: 18px; }
</style>

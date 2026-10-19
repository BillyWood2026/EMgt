<template>
  <div class="epros-policy">
    <div class="policy-header">
      <div class="header-left">
        <el-input 
          v-model="policyForm.title" 
          placeholder="制度标题" 
          class="policy-title"
        />
        <el-select v-model="policyForm.category" placeholder="制度分类">
          <el-option label="管理类" value="management" />
          <el-option label="业务类" value="business" />
          <el-option label="技术类" value="technical" />
          <el-option label="合规类" value="compliance" />
        </el-select>
        <el-input 
          v-model="policyForm.code" 
          placeholder="制度编号" 
          class="policy-code"
        />
      </div>
      <div class="header-right">
        <el-button size="small" @click="saveDraft">保存草稿</el-button>
        <el-button size="small" type="primary" @click="submitForReview">提交审核</el-button>
        <el-button size="small" @click="previewPolicy">预览</el-button>
      </div>
    </div>

    <div class="policy-body">
      <div class="content-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="制度内容" name="content">
            <div class="content-editor">
              <div class="editor-toolbar">
                <el-button size="small" icon="EditPen" @click="formatText('bold')">粗体</el-button>
                <el-button size="small" icon="EditPen" @click="formatText('italic')">斜体</el-button>
                <el-button size="small" icon="EditPen" @click="formatText('underline')">下划线</el-button>
                <el-divider direction="vertical" />
                <el-button size="small" icon="Filter" @click="formatText('alignLeft')">左对齐</el-button>
                <el-button size="small" icon="Filter" @click="formatText('alignCenter')">居中</el-button>
                <el-button size="small" icon="Filter" @click="formatText('alignRight')">右对齐</el-button>
                <el-divider direction="vertical" />
                <el-button size="small" icon="List" @click="formatText('list')">列表</el-button>
                <el-button size="small" icon="List" @click="formatText('orderedList')">有序列表</el-button>
                <el-divider direction="vertical" />
                <el-button size="small" icon="Minus" @click="formatText('hr')">分隔线</el-button>
                <el-button size="small" icon="Link" @click="insertLink">链接</el-button>
                <el-button size="small" icon="Picture" @click="insertImage">图片</el-button>
              </div>
              
              <div class="editor-content">
                <textarea 
                  v-model="policyForm.content" 
                  placeholder="请输入制度内容，支持HTML格式"
                  class="content-textarea"
                />
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="流程关联" name="flows">
            <div class="flows-panel">
              <div class="flows-header">
                <span>关联流程</span>
                <el-button size="small" icon="Plus" @click="addFlowRelation">添加关联</el-button>
              </div>
              <el-table :data="flowRelations" border>
                <el-table-column prop="flowCode" label="流程编码" width="120" />
                <el-table-column prop="flowName" label="流程名称" width="200" />
                <el-table-column prop="relationType" label="关联类型" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.relationType === 'main' ? 'primary' : 'info'">
                      {{ row.relationType === 'main' ? '主流程' : '辅助流程' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="bindingNode" label="绑定节点" width="120" />
                <el-table-column prop="actions" label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="small" @click="removeFlowRelation(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="表单绑定" name="forms">
            <div class="forms-panel">
              <div class="forms-header">
                <span>绑定表单</span>
                <el-button size="small" icon="Plus" @click="addFormBinding">添加表单</el-button>
              </div>
              <el-table :data="formBindings" border>
                <el-table-column prop="formCode" label="表单编码" width="120" />
                <el-table-column prop="formName" label="表单名称" width="200" />
                <el-table-column prop="bindingType" label="绑定方式" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.bindingType === 'auto' ? 'success' : 'warning'">
                      {{ row.bindingType === 'auto' ? '自动绑定' : '手动选择' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="small" @click="removeFormBinding(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="合规条款" name="compliance">
            <div class="compliance-panel">
              <div class="compliance-header">
                <span>合规条款绑定</span>
                <el-button size="small" icon="Plus" @click="addCompliance">添加条款</el-button>
              </div>
              <el-table :data="complianceItems" border>
                <el-table-column prop="standard" label="标准体系" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getStandardTagType(row.standard)">
                      {{ getStandardName(row.standard) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="clause" label="条款编号" width="120" />
                <el-table-column prop="description" label="条款内容" width="300" />
                <el-table-column prop="riskLevel" label="风险等级" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getRiskTagType(row.riskLevel)">
                      {{ row.riskLevel }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="small" @click="removeCompliance(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="制度查阅" name="document">
            <div class="document-panel">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
                title="支持全文检索、标题检索、标签检索，按部门、版本、类型多维度组合查询，支持制度借阅、在线学习、学习进度追踪。" />
              <el-card shadow="never" style="margin-bottom:16px">
                <el-form :inline="true" :model="searchForm" @submit.prevent>
                  <el-form-item label="关键词">
                    <el-input v-model="searchForm.keyword" placeholder="搜索制度名称、关键词、内容" clearable style="width:280px" prefix-icon="Search" />
                  </el-form-item>
                  <el-form-item label="分类">
                    <el-select v-model="searchForm.category" clearable placeholder="全部" style="width:140px">
                      <el-option label="全部" value="" />
                      <el-option label="体系手册" value="体系手册" />
                      <el-option label="程序文件" value="程序文件" />
                      <el-option label="管理制度" value="管理制度" />
                      <el-option label="作业SOP" value="作业SOP" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="体系">
                    <el-select v-model="searchForm.system" clearable placeholder="全部" style="width:140px">
                      <el-option label="全部" value="" />
                      <el-option label="质量管理体系" value="质量管理体系" />
                      <el-option label="安全生产体系" value="安全生产体系" />
                      <el-option label="环境管理体系" value="环境管理体系" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="责任部门">
                    <el-select v-model="searchForm.dept" clearable placeholder="全部" style="width:140px">
                      <el-option label="全部" value="" />
                      <el-option label="生产制造中心" value="生产部" />
                      <el-option label="质量管理部" value="质量部" />
                      <el-option label="人力资源部" value="人力资源部" />
                      <el-option label="财务管理部" value="财务部" />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="doSearch">搜索</el-button>
                    <el-button @click="resetSearch">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              <el-tabs v-model="docActiveTab">
                <el-tab-pane label="制度列表" name="list">
                  <el-table :data="documentList" border stripe size="small">
                    <el-table-column prop="docCode" label="制度编码" width="150" />
                    <el-table-column prop="docName" label="制度名称" min-width="200">
                      <template #default="{row}">
                        <el-link type="primary" @click="viewDocument(row)">{{ row.docName }}</el-link>
                      </template>
                    </el-table-column>
                    <el-table-column prop="category" label="分类" width="100">
                      <template #default="{row}"><el-tag size="small">{{ row.category }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="version" label="版本" width="80">
                      <template #default="{row}"><el-tag size="small" type="success">{{ row.version }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="deptName" label="责任部门" width="120" />
                    <el-table-column prop="effectiveDate" label="生效日期" width="120" />
                    <el-table-column label="状态" width="80">
                      <template #default="{row}">
                        <el-tag v-if="row.status === '有效'" size="small" type="success">有效</el-tag>
                        <el-tag v-else size="small" type="info">作废</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="280" fixed="right">
                      <template #default="{row}">
                        <el-button link size="small" type="primary" @click="viewDocument(row)">查阅</el-button>
                        <el-button link size="small" @click="borrowDocument(row)">借阅</el-button>
                        <el-button link size="small" @click="startLearning(row)">开始学习</el-button>
                        <el-button link size="small" @click="collectDocument(row)">收藏</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="我的收藏" name="favorites">
                  <el-table :data="favoriteList" border stripe size="small">
                    <el-table-column prop="docName" label="制度名称" min-width="200">
                      <template #default="{row}">
                        <el-link type="primary" @click="viewDocument(row)">{{ row.docName }}</el-link>
                      </template>
                    </el-table-column>
                    <el-table-column prop="category" label="分类" width="100">
                      <template #default="{row}"><el-tag size="small">{{ row.category }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="collectTime" label="收藏时间" width="160" />
                    <el-table-column label="操作" width="200">
                      <template #default="{row}">
                        <el-button link size="small" type="primary" @click="viewDocument(row)">查阅</el-button>
                        <el-button link size="small" @click="startLearning(row)">学习</el-button>
                        <el-button link size="small" type="danger" @click="cancelCollect(row)">取消</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="借阅记录" name="borrow">
                  <el-table :data="borrowList" border stripe size="small">
                    <el-table-column prop="docName" label="制度名称" min-width="180" />
                    <el-table-column prop="borrowTime" label="借阅时间" width="160" />
                    <el-table-column prop="returnTime" label="归还时间" width="160" />
                    <el-table-column label="状态" width="100">
                      <template #default="{row}">
                        <el-tag v-if="row.status === '借阅中'" type="warning" size="small">借阅中</el-tag>
                        <el-tag v-else type="success" size="small">已归还</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="{row}">
                        <el-button link size="small" type="primary" @click="viewDocument(row)">查阅</el-button>
                        <el-button link size="small" v-if="row.status === '借阅中'" @click="returnDocument(row)">归还</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-tab-pane>

          <el-tab-pane label="流程说明书" name="instruction">
            <div class="instruction-panel">
              <div class="search-bar">
                <el-input v-model="searchKeyword" placeholder="搜索说明书名称/编号" prefix-icon="Search" class="search-input" />
                <el-select v-model="filterDomain" placeholder="选择流程域">
                  <el-option label="全部" value="" />
                  <el-option v-for="d in domainOptions" :key="d.code" :label="d.name" :value="d.code" />
                </el-select>
                <el-select v-model="filterLevel" placeholder="选择流程层级">
                  <el-option label="全部" value="" />
                  <el-option label="一级流程域" value="level1" />
                  <el-option label="二级流程组" value="level2" />
                  <el-option label="三级主流程" value="level3" />
                  <el-option label="四级子流程" value="level4" />
                  <el-option label="五级岗位流程" value="level5" />
                </el-select>
                <el-select v-model="filterStatus" placeholder="选择状态">
                  <el-option label="全部" value="" />
                  <el-option label="草稿" value="draft" />
                  <el-option label="待审核" value="pending" />
                  <el-option label="生效中" value="active" />
                  <el-option label="已废止" value="deprecated" />
                </el-select>
                <el-button icon="Refresh" @click="loadInstructionList">刷新</el-button>
              </div>
              <div class="stats-cards">
                <el-card class="stat-card">
                  <div class="stat-icon blue">📋</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ instructionList.length }}</div>
                    <div class="stat-label">说明书总数</div>
                  </div>
                </el-card>
                <el-card class="stat-card">
                  <div class="stat-icon green">✅</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ activeCount }}</div>
                    <div class="stat-label">生效中</div>
                  </div>
                </el-card>
                <el-card class="stat-card">
                  <div class="stat-icon orange">⏳</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ pendingCount }}</div>
                    <div class="stat-label">待审核</div>
                  </div>
                </el-card>
                <el-card class="stat-card">
                  <div class="stat-icon purple">📊</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ templateList.length }}</div>
                    <div class="stat-label">可用模板</div>
                  </div>
                </el-card>
              </div>
              <el-table :data="filteredList" border stripe>
                <el-table-column prop="code" label="说明书编号" width="180" />
                <el-table-column prop="name" label="说明书名称" width="220" />
                <el-table-column prop="flowCode" label="关联流程" width="140" />
                <el-table-column prop="domainName" label="归属域" width="160" />
                <el-table-column prop="groupName" label="归属组" width="160" />
                <el-table-column prop="level" label="流程层级" width="100">
                  <template #default="{ row }">
                    <el-tag size="small">{{ row.level }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getInstStatusTagType(row.status)">{{ getInstStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="ownerDept" label="归口部门" width="120" />
                <el-table-column prop="updateTime" label="更新时间" width="160" />
                <el-table-column prop="actions" label="操作" width="280" fixed="right">
                  <template #default="{ row }">
                    <el-button size="small" @click="viewDetail(row)">查看</el-button>
                    <el-button size="small" @click="editInstruction(row)">编辑</el-button>
                    <el-button size="small" @click="previewInstruction(row)">预览</el-button>
                    <el-button size="small" type="danger" @click="deleteInstruction(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <div class="policy-footer">
      <div class="footer-info">
        <span>当前版本: {{ policyForm.version }}</span>
        <span>状态: <el-tag :type="getStatusTagType(policyForm.status)">{{ getStatusName(policyForm.status) }}</el-tag></span>
        <span>创建时间: {{ policyForm.createTime }}</span>
        <span>最后修改: {{ policyForm.updateTime }}</span>
      </div>
    </div>

    <el-dialog title="选择流程" v-model="flowSelectVisible" width="600px">
      <el-input v-model="flowSearch" placeholder="搜索流程" class="search-input" />
      <el-table :data="flowOptions" border style="margin-top: 16px;">
        <el-table-column prop="code" label="流程编码" width="140" />
        <el-table-column prop="name" label="流程名称" width="200" />
        <el-table-column prop="level" label="流程层级" width="100" />
        <el-table-column prop="actions" label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="confirmFlowSelect(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import {
  EditPen, Filter, List, Minus, Link, Picture, Plus, Search, Refresh, Star
} from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'

const activeTab = ref('content')

// 当前持久化的制度文档ID
const policyDocId = ref(null)

const policyForm = reactive({
  title: '安全生产管理制度',
  category: 'compliance',
  code: 'ZD-EHS-001',
  version: 'V1.0',
  content: '<h1>安全生产管理制度</h1>\n\n<h2>1. 目的</h2>\n<p>为加强公司安全生产管理，保障员工生命安全和公司财产安全，根据国家相关法律法规，制定本制度。</p>\n\n<h2>2. 适用范围</h2>\n<p>本制度适用于公司所有部门和全体员工。</p>\n\n<h2>3. 职责分工</h2>\n<ul>\n  <li>EHS部：负责安全生产的统筹管理</li>\n  <li>各部门：负责本部门安全生产执行</li>\n  <li>员工：严格遵守安全操作规程</li>\n</ul>\n\n<h2>4. 管理要求</h2>\n<p>4.1 所有员工必须经过安全培训合格后方可上岗...</p>',
  status: 'draft',
  createTime: '2024-01-15 10:00:00',
  updateTime: '2024-01-15 14:30:00'
})

const flowRelations = ref([
  { id: 1, flowCode: 'FP080101', flowName: '安全检查流程', relationType: 'main', bindingNode: '审批节点' },
  { id: 2, flowCode: 'FP080102', flowName: '作业许可流程', relationType: 'auxiliary', bindingNode: '申请节点' }
])

const formBindings = ref([
  { id: 1, formCode: 'FM-EHS-001', formName: '安全检查表', bindingType: 'auto' },
  { id: 2, formCode: 'FM-EHS-002', formName: '作业许可证', bindingType: 'auto' }
])

const complianceItems = ref([
  { id: 1, standard: 'ISO45001', clause: '4.3', description: '组织应确定与职业健康安全管理体系相关的内部和外部议题', riskLevel: '中' },
  { id: 2, standard: 'ISO45001', clause: '5.1', description: '最高管理者应证实其对职业健康安全管理体系的领导作用和承诺', riskLevel: '高' },
  { id: 3, standard: '安全生产标准化', clause: 'AQ/T 9006', description: '企业安全生产标准化基本规范', riskLevel: '高' }
])

const flowSelectVisible = ref(false)
const flowSearch = ref('')

const flowOptions = ref([
  { code: 'FP080101', name: '安全检查流程', level: '三级' },
  { code: 'FP080102', name: '作业许可流程', level: '三级' },
  { code: 'FP080103', name: '隐患整改流程', level: '三级' },
  { code: 'FP080201', name: '环境监测流程', level: '三级' }
])

const getStandardTagType = (standard) => {
  const map = {
    'ISO9001': 'primary',
    'ISO14001': 'success',
    'ISO45001': 'warning',
    'ISO50001': 'info',
    '安全生产标准化': 'danger'
  }
  return map[standard] || 'info'
}

const getStandardName = (standard) => {
  const map = {
    'ISO9001': '质量管理体系',
    'ISO14001': '环境管理体系',
    'ISO45001': '职业健康安全',
    'ISO50001': '能源管理体系',
    '安全生产标准化': '安全生产标准'
  }
  return map[standard] || standard
}

const getRiskTagType = (level) => {
  const map = { '高': 'danger', '中': 'warning', '低': 'success' }
  return map[level] || 'info'
}

const getStatusTagType = (status) => {
  const map = { draft: 'info', pending: 'warning', approved: 'success', deprecated: 'danger' }
  return map[status] || 'info'
}

const getStatusName = (status) => {
  const map = { draft: '草稿', pending: '待审核', approved: '已生效', deprecated: '已废止' }
  return map[status] || status
}

const formatText = (type) => {
  const formatNames = {
    bold: '粗体',
    italic: '斜体',
    underline: '下划线',
    alignLeft: '左对齐',
    alignCenter: '居中对齐',
    alignRight: '右对齐',
    list: '无序列表',
    orderedList: '有序列表',
    hr: '分隔线'
  }
  ElMessage.info(`已应用格式：${formatNames[type] || type}`)
}

const insertLink = () => {
  ElMessage.info('插入链接')
}

const insertImage = () => {
  ElMessage.info('插入图片')
}

const addFlowRelation = () => {
  flowSelectVisible.value = true
}

const confirmFlowSelect = (row) => {
  flowRelations.value.push({
    id: Date.now(),
    flowCode: row.code,
    flowName: row.name,
    relationType: 'auxiliary',
    bindingNode: '默认节点'
  })
  flowSelectVisible.value = false
}

const removeFlowRelation = (row) => {
  flowRelations.value = flowRelations.value.filter(r => r.id !== row.id)
}

const addFormBinding = () => {
  formBindings.value.push({
    id: Date.now(),
    formCode: `FM-${Date.now()}`,
    formName: '新增表单',
    bindingType: 'manual'
  })
}

const removeFormBinding = (row) => {
  formBindings.value = formBindings.value.filter(b => b.id !== row.id)
}

const addCompliance = () => {
  complianceItems.value.push({
    id: Date.now(),
    standard: 'ISO45001',
    clause: '新增条款',
    description: '新增合规条款描述',
    riskLevel: '中'
  })
}

const removeCompliance = (row) => {
  complianceItems.value = complianceItems.value.filter(c => c.id !== row.id)
}

const buildDocPayload = () => ({
  docNo: policyForm.code,
  title: policyForm.title,
  category: policyForm.category,
  version: policyForm.version || 'V1.0',
  content: policyForm.content
})

const applyDocResponse = (d) => {
  if (!d) return
  if (d.id) policyDocId.value = d.id
  policyForm.status = d.status || policyForm.status
  policyForm.version = d.version || policyForm.version
  if (d.updateTime) policyForm.updateTime = String(d.updateTime).replace('T', ' ').slice(0, 19)
  if (d.createTime) policyForm.createTime = String(d.createTime).replace('T', ' ').slice(0, 19)
}

const saveDraft = async () => {
  try {
    const payload = buildDocPayload()
    const res = policyDocId.value
      ? await axios.put(`/api/document/${policyDocId.value}`, payload)
      : await axios.post('/api/document', payload)
    applyDocResponse(res.data && res.data.data ? res.data.data : res.data)
    ElMessage.success('草稿已保存')
  } catch (e) {
    const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message
    ElMessage.error('草稿保存失败：' + msg)
  }
}

const submitForReview = async () => {
  try {
    if (!policyDocId.value) {
      await saveDraft()
    }
    const res = await axios.post(`/api/document/${policyDocId.value}/submit`)
    applyDocResponse(res.data && res.data.data ? res.data.data : res.data)
    ElMessage.success('已提交审核')
  } catch (e) {
    const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message
    ElMessage.error('提交审核失败：' + msg)
  }
}

const previewPolicy = () => {
  const win = window.open('', '_blank')
  if (!win) {
    ElMessage.warning('浏览器拦截了预览窗口，请允许弹出窗口')
    return
  }
  win.document.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>${policyForm.title}</title>
    <style>body{font-family:'微软雅黑',sans-serif;max-width:900px;margin:32px auto;padding:0 24px;line-height:1.8}
    h1,h2{color:#303133}h1{border-bottom:2px solid #409eff;padding-bottom:8px}
    .meta{color:#909399;font-size:13px;margin-bottom:24px}</style></head>
    <body><h1>${policyForm.title}</h1>
    <div class="meta">编号：${policyForm.code} ｜ 版本：${policyForm.version} ｜ 分类：${policyForm.category}</div>
    ${policyForm.content}</body></html>`)
  win.document.close()
}

// ========== 制度查阅相关 (来自 ProcessDocument.vue) ==========
const docActiveTab = ref('list')
const searchForm = reactive({ keyword: '', category: '', system: '', dept: '' })
const pageSize = ref(10)
const currentPage = ref(1)
const totalCount = ref(100)

const documentList = ref([
  { id: 1, docCode: 'ZD-GL-001', docName: '四标一体化管理手册', category: '体系手册', version: 'V2.0', deptName: '综合管理部', status: '有效', effectiveDate: '2026-01-01', nextReview: '2027-01-01', viewCount: 1256, downloadCount: 89, isCollected: false },
  { id: 2, docCode: 'ZD-SC-001', docName: '生产作业控制程序', category: '程序文件', version: 'V3.1', deptName: '生产部', status: '有效', effectiveDate: '2026-03-15', nextReview: '2026-09-15', viewCount: 876, downloadCount: 45, isCollected: false },
  { id: 3, docCode: 'ZD-QA-001', docName: '进料检验作业指导书', category: '作业SOP', version: 'V1.5', deptName: '品质部', status: '有效', effectiveDate: '2026-02-01', nextReview: '2026-08-01', viewCount: 432, downloadCount: 28, isCollected: false },
  { id: 4, docCode: 'ZD-HR-001', docName: '员工培训管理制度', category: '管理制度', version: 'V2.5', deptName: '人力资源部', status: '有效', effectiveDate: '2026-04-01', nextReview: '2026-10-01', viewCount: 789, downloadCount: 56, isCollected: false },
  { id: 5, docCode: 'ZD-EHS-001', docName: '文件控制程序', category: '程序文件', version: 'V2.0', deptName: 'EHS部', status: '有效', effectiveDate: '2026-01-01', nextReview: '2027-01-01', viewCount: 1102, downloadCount: 78, isCollected: false },
  { id: 6, docCode: 'ZD-EHS-002', docName: '隐患排查治理制度', category: '管理制度', version: 'V1.0', deptName: 'EHS部', status: '有效', effectiveDate: '2026-02-01', nextReview: '2026-08-01', viewCount: 654, downloadCount: 42, isCollected: false },
  { id: 7, docCode: 'ZD-PRO-001', docName: '生产计划管理程序', category: '程序文件', version: 'V2.0', deptName: '生产中心', status: '有效', effectiveDate: '2026-01-05', nextReview: '2026-07-05', viewCount: 543, downloadCount: 36, isCollected: false },
  { id: 8, docCode: 'ZD-QA-002', docName: '不良品管理程序', category: '程序文件', version: 'V2.0', deptName: '品质部', status: '有效', effectiveDate: '2026-01-12', nextReview: '2026-07-12', viewCount: 489, downloadCount: 31, isCollected: false },
  { id: 9, docCode: 'ZD-ENE-001', docName: '能源管理程序', category: '程序文件', version: 'V1.0', deptName: '能源管理部', status: '有效', effectiveDate: '2026-02-15', nextReview: '2026-08-15', viewCount: 321, downloadCount: 22, isCollected: false },
  { id: 10, docCode: 'ZD-SMT-001', docName: 'SMT首件检验规程', category: '作业SOP', version: 'V2.0', deptName: 'SMT车间', status: '有效', effectiveDate: '2026-01-08', nextReview: '2026-07-08', viewCount: 567, downloadCount: 38, isCollected: false }
])

const favoriteList = ref([
  { id: 1, docName: '四标一体化管理手册', category: '体系手册', collectTime: '2026-06-10 10:30' },
  { id: 2, docName: '安全生产管理制度', category: '管理制度', collectTime: '2026-06-08 14:20' }
])

const borrowList = ref([
  { id: 1, docName: '生产作业控制程序', borrowTime: '2026-06-15 09:00', returnTime: '', status: '借阅中' },
  { id: 2, docName: '进料检验作业指导书', borrowTime: '2026-06-10 11:00', returnTime: '2026-06-14 16:00', status: '已归还' }
])

const doSearch = () => { ElMessage.success('搜索完成') }
const resetSearch = () => { searchForm.keyword = ''; searchForm.category = ''; searchForm.system = ''; searchForm.dept = '' }
const viewDocument = (row) => { ElMessage.info(`查阅制度: ${row.docName}`) }
const borrowDocument = (row) => { ElMessage.success(`借阅成功: ${row.docName}`) }
const startLearning = (row) => { ElMessage.info(`开始学习: ${row.docName}`) }
const collectDocument = (row) => {
  row.isCollected = !row.isCollected
  ElMessage.success(row.isCollected ? `已收藏: ${row.docName}` : `已取消收藏: ${row.docName}`)
}
const cancelCollect = (row) => { ElMessage.success(`已取消收藏: ${row.docName}`) }
const returnDocument = (row) => { ElMessage.success(`已归还: ${row.docName}`) }

// ========== 流程说明书相关 (来自 ProcessInstruction.vue) ==========
const searchKeyword = ref('')
const filterDomain = ref('')
const filterLevel = ref('')
const filterStatus = ref('')

const domainOptions = ref([
  { code: 'domain1', name: '战略经营流程域' },
  { code: 'domain2', name: '产品研发流程域' },
  { code: 'domain3', name: '供应链采购流程域' },
  { code: 'domain4', name: '生产制造流程域' },
  { code: 'domain5', name: 'SMT工艺制程流程域' },
  { code: 'domain6', name: '品质管控流程域' },
  { code: 'domain7', name: '设备工装流程域' },
  { code: 'domain8', name: '仓储物流流程域' },
  { code: 'domain9', name: 'EHS合规管理流程域' },
  { code: 'domain10', name: '能源管控流程域' }
])

const instructionList = ref([
  { id: 1, code: 'LY-04-01-001', name: '生产订单下达与排程主流程说明书', flowCode: 'FP040101', domainName: '生产制造流程域', groupName: '生产计划管理组', level: '三级主流程', version: 'V2.0', status: 'active', ownerDept: '生产中心', updateTime: '2026-03-15 14:30' },
  { id: 2, code: 'LY-05-02-001', name: 'SMT首件确认主流程说明书', flowCode: 'FP050101', domainName: 'SMT工艺制程流程域', groupName: '贴片制程管控组', level: '三级主流程', version: 'V3.0', status: 'active', ownerDept: 'SMT车间', updateTime: '2026-04-20 09:15' },
  { id: 3, code: 'LY-06-01-001', name: '来料检验判定与放行主流程说明书', flowCode: 'FP060101', domainName: '品质管控流程域', groupName: '来料IQC检验组', level: '三级主流程', version: 'V2.1', status: 'active', ownerDept: '品质部', updateTime: '2026-05-08 16:45' },
  { id: 4, code: 'LY-06-04-001', name: '不良品隔离、返工、报废闭环主流程说明书', flowCode: 'FP060401', domainName: '品质管控流程域', groupName: '不良品处置管控组', level: '三级主流程', version: 'V1.0', status: 'pending', ownerDept: '品质部', updateTime: '2026-06-10 11:00' },
  { id: 5, code: 'LY-09-01-001', name: 'EHS体系文件全生命周期主流程说明书', flowCode: 'FP090101', domainName: 'EHS合规管理流程域', groupName: '体系文件管控组', level: '三级主流程', version: 'V2.0', status: 'active', ownerDept: 'EHS部', updateTime: '2026-03-01 10:00' },
  { id: 6, code: 'LY-09-05-001', name: '隐患排查、整改、验证闭环主流程说明书', flowCode: 'FP090501', domainName: 'EHS合规管理流程域', groupName: '隐患排查整改组', level: '三级主流程', version: 'V1.5', status: 'active', ownerDept: 'EHS部', updateTime: '2026-04-15 13:20' },
  { id: 7, code: 'LY-10-04-001', name: '节能改善项目立项-实施-验证-固化主流程说明书', flowCode: 'FP100401', domainName: '能源管控流程域', groupName: '能耗统计分析组', level: '三级主流程', version: 'V1.0', status: 'draft', ownerDept: '能源管理部', updateTime: '2026-06-05 09:30' },
  { id: 8, code: 'LY-05-05-001', name: 'ESD日常检查与异常处置主流程说明书', flowCode: 'FP050501', domainName: 'SMT工艺制程流程域', groupName: 'ESD静电防护管控组', level: '三级主流程', version: 'V1.0', status: 'active', ownerDept: 'EHS部', updateTime: '2026-02-28 15:00' }
])

const templateList = ref([
  { id: 1, name: '三级主流程说明书模板', version: 'V2.0', usageCount: 156 },
  { id: 2, name: '四级子流程说明书模板', version: 'V1.0', usageCount: 89 },
  { id: 3, name: '五级岗位流程说明书模板', version: 'V1.0', usageCount: 45 }
])

const activeCount = computed(() => instructionList.value.filter(i => i.status === 'active').length)
const pendingCount = computed(() => instructionList.value.filter(i => i.status === 'pending').length)

const filteredList = computed(() => {
  return instructionList.value.filter(item => {
    if (searchKeyword.value && !item.name.includes(searchKeyword.value) && !item.code.includes(searchKeyword.value)) return false
    if (filterDomain.value && !item.domainName.includes(filterDomain.value)) return false
    if (filterLevel.value && !item.level.includes(filterLevel.value.replace('level', '级'))) return false
    if (filterStatus.value && item.status !== filterStatus.value) return false
    return true
  })
})

const getInstStatusTagType = (status) => {
  const map = { draft: 'info', pending: 'warning', active: 'success', deprecated: 'danger', archived: 'info' }
  return map[status] || 'info'
}

const getInstStatusName = (status) => {
  const map = { draft: '草稿', pending: '待审核', active: '生效中', deprecated: '已废止', archived: '已归档' }
  return map[status] || status
}

const loadInstructionList = () => { ElMessage.success('列表已刷新') }
const viewDetail = (row) => { ElMessage.info(`查看说明书: ${row.name}`) }
const editInstruction = (row) => { ElMessage.info(`编辑说明书: ${row.name}`) }
const previewInstruction = (row) => { ElMessage.info(`预览说明书: ${row.name}`) }
const deleteInstruction = (row) => { ElMessage.success(`已删除: ${row.name}`) }
</script>

<style lang="scss" scoped>
.epros-policy {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.policy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  
  .header-left {
    display: flex;
    gap: 12px;
  }
  
  .policy-title {
    width: 300px;
  }
  
  .policy-code {
    width: 150px;
  }
}

.policy-body {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.content-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .el-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .el-tabs__content {
      flex: 1;
      overflow: auto;
    }
  }
}

.content-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  flex-wrap: wrap;
}

.editor-content {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.content-textarea {
  width: 100%;
  height: 100%;
  min-height: 500px;
  border: none;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
}

.flows-panel, .forms-panel, .compliance-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
}

.flows-header, .forms-header, .compliance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  font-weight: 600;
}

.el-table {
  flex: 1;
  overflow: auto;
}

.policy-footer {
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
}

.footer-info {
  display: flex;
  gap: 24px;
  
  span {
    font-size: 13px;
    color: #666;
  }
}

.search-input {
  width: 100%;
}
</style>
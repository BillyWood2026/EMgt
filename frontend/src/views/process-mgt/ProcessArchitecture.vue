<template>
  <div class="epros-architecture">
    <div class="page-header">
      <div class="header-title">
        <h2>五级流程架构管理</h2>
        <p>电子产品行业五级流程制度体系框架</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="openAddModal('level1')">新增流程域</el-button>
        <el-button icon="Download" @click="exportArchitecture">导出架构图</el-button>
        <el-button icon="Refresh" @click="loadArchitecture">刷新</el-button>
      </div>
    </div>

    <div class="architecture-container">
        <div class="level-tabs">
          <el-tabs v-model="activeLevel" type="card" @tab-change="handleLevelChange">
            <el-tab-pane label="体系树形视图" name="tree-view">
              <div class="tree-view-content">
                <div class="tree-page-header">
                  <div class="header-left">
                    <h2>流程制度体系管理</h2>
                    <p class="header-subtitle">公司级与部门级双层架构 · 四级体系标准化管理 · 全生命周期管控</p>
                  </div>
                  <div class="header-actions">
                    <el-input v-model="searchKeyword" placeholder="关键词搜索制度/流程/文件" clearable style="width:260px">
                      <template #prefix><el-icon><Search /></el-icon></template>
                    </el-input>
                    <el-button @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
                    <el-button @click="handleExport"><el-icon><Download /></el-icon>导出架构</el-button>
                    <el-button @click="refreshTree"><el-icon><Refresh /></el-icon>刷新</el-button>
                  </div>
                </div>

                <el-row :gutter="16" class="stats-row">
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card">
                      <div class="stat-value">{{ stats.totalCompany }}</div>
                      <div class="stat-label">公司级体系</div>
                    </el-card>
                  </el-col>
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card">
                      <div class="stat-value">{{ stats.totalDept }}</div>
                      <div class="stat-label">部门级体系</div>
                    </el-card>
                  </el-col>
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card">
                      <div class="stat-value">{{ stats.totalDocuments }}</div>
                      <div class="stat-label">制度文件</div>
                    </el-card>
                  </el-col>
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card">
                      <div class="stat-value">{{ stats.totalFlows }}</div>
                      <div class="stat-label">流程文件</div>
                    </el-card>
                  </el-col>
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card warning">
                      <div class="stat-value">{{ stats.pendingCount }}</div>
                      <div class="stat-label">待审批</div>
                    </el-card>
                  </el-col>
                  <el-col :span="4">
                    <el-card shadow="hover" class="stat-card success">
                      <div class="stat-value">{{ stats.effectiveCount }}</div>
                      <div class="stat-label">有效文件</div>
                    </el-card>
                  </el-col>
                </el-row>

                <el-tabs v-model="treeActiveTab" class="main-tabs">
                  <el-tab-pane label="公司级体系" name="company">
                    <div class="content-container">
                      <el-card shadow="never" class="tree-panel">
                        <template #header>
                          <div class="panel-header">
                            <span><b>制度体系结构（四级架构）</b></span>
                            <div class="header-toolbar">
                              <el-button size="small" type="primary" @click="addNode('system')">
                                <el-icon><Plus /></el-icon>新增体系
                              </el-button>
                              <el-button size="small" @click="expandAll"><el-icon><Rank /></el-icon></el-button>
                              <el-button size="small" @click="collapseAll"><el-icon><Close /></el-icon></el-button>
                            </div>
                          </div>
                        </template>
                        <div class="tree-filter">
                          <el-select v-model="companyFilter.type" placeholder="节点类型" clearable size="small" style="width:120px">
                            <el-option label="体系分类" value="system" />
                            <el-option label="业务模块" value="module" />
                            <el-option label="制度清单" value="document" />
                            <el-option label="版本文件" value="file" />
                          </el-select>
                          <el-select v-model="companyFilter.status" placeholder="状态" clearable size="small" style="width:100px">
                            <el-option label="启用" value="启用" />
                            <el-option label="停用" value="停用" />
                            <el-option label="作废" value="作废" />
                            <el-option label="待审批" value="待审批" />
                          </el-select>
                        </div>
                        <el-tree
                          :data="filteredCompanyTree"
                          :props="treeProps"
                          node-key="id"
                          :default-expand-all="expandAllFlag"
                          :expand-on-click-node="false"
                          @node-click="handleCompanyNodeClick"
                          highlight-current
                        >
                          <template #default="{ node, data }">
                            <span class="tree-node-content">
                              <span class="node-icon"><el-icon :component="getNodeIcon(data.type)" /></span>
                              <span class="node-label" :class="{ 'is-disabled': data.status === '作废' }">{{ node.label }}</span>
                              <el-tag v-if="data.status === '有效'" size="mini" type="success" class="node-tag">有效</el-tag>
                              <el-tag v-else-if="data.status === '待审批'" size="mini" type="danger" class="node-tag">待审批</el-tag>
                              <el-tag v-else-if="data.status === '作废'" size="mini" type="info" class="node-tag">作废</el-tag>
                              <el-tag v-if="data.isLatest && data.type === 'file'" size="mini" type="success" class="node-tag">V{{ data.version }}</el-tag>
                            </span>
                          </template>
                        </el-tree>
                      </el-card>
                      <el-card shadow="never" class="detail-panel">
                        <template #header>
                          <div class="panel-header">
                            <span><b>{{ selectedCompanyNode?.name || '请选择节点查看详情' }}</b></span>
                            <div class="header-toolbar" v-if="selectedCompanyNode">
                              <el-button size="small" type="primary" @click="handleCreateDoc('document')">
                                <el-icon><Plus /></el-icon>新建
                              </el-button>
                              <el-button size="small" @click="handleEditNode"><el-icon><Edit /></el-icon>编辑</el-button>
                              <el-button size="small" type="danger" @click="handleDeleteNode"><el-icon><Delete /></el-icon>删除</el-button>
                            </div>
                          </div>
                        </template>
                        <div v-if="!selectedCompanyNode" class="empty-state">
                          <el-icon :size="64" class="empty-icon"><FolderOpened /></el-icon>
                          <p>从左侧树形目录选择节点</p>
                        </div>
                        <div v-else class="node-detail">
                          <el-descriptions :column="2" border size="small">
                            <el-descriptions-item label="节点类型">
                              <el-tag :type="getTypeTagType(selectedCompanyNode.type)" size="small">{{ getTypeLabel(selectedCompanyNode.type) }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="节点状态">
                              <el-tag :type="getStatusTagType(selectedCompanyNode.status)" size="small">{{ selectedCompanyNode.status }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="负责人">
                              <el-tag size="small">{{ selectedCompanyNode.owner || '-' }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="创建时间">
                              {{ selectedCompanyNode.createTime || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="描述" :span="2">
                              {{ selectedCompanyNode.desc || '-' }}
                            </el-descriptions-item>
                          </el-descriptions>
                          <el-card v-if="['system', 'module', 'document'].includes(selectedCompanyNode.type)" shadow="never">
                            <template #header>
                              <div class="card-header">
                                <span><b>版本文件列表</b></span>
                                <el-button size="small" type="primary" @click="handleUploadNewVersion">
                                  <el-icon><Upload /></el-icon>上传新版本
                                </el-button>
                              </div>
                            </template>
                            <el-table :data="companyVersionFiles" border size="small" max-height="300">
                              <el-table-column prop="version" label="版本号" width="90">
                                <template #default="{ row }">
                                  <el-tag size="small" :type="row.isCurrent ? 'success' : 'info'">V{{ row.version }}</el-tag>
                                </template>
                              </el-table-column>
                              <el-table-column prop="fileName" label="文件名" min-width="200" />
                              <el-table-column prop="fileType" label="类型" width="80" />
                              <el-table-column prop="uploadTime" label="上传时间" width="140" />
                              <el-table-column prop="status" label="状态" width="80">
                                <template #default="{ row }">
                                  <el-tag size="small" :type="getFileStatusType(row.status)">{{ row.status }}</el-tag>
                                </template>
                              </el-table-column>
                              <el-table-column label="操作" width="150">
                                <template #default="{ row }">
                                  <el-button link size="small" @click="handlePreviewFile(row)">预览</el-button>
                                  <el-button link size="small" @click="handleDownloadFile(row)">下载</el-button>
                                </template>
                              </el-table-column>
                            </el-table>
                          </el-card>
                          <el-card shadow="never">
                            <template #header><b>操作日志</b></template>
                            <el-timeline>
                              <el-timeline-item v-for="(log, idx) in nodeOperationLogs" :key="idx" :timestamp="log.time" placement="top">
                                <el-card shadow="never" body-style="padding:8px">
                                  <b>{{ log.operator }}</b> {{ log.action }}
                                  <p>{{ log.detail }}</p>
                                </el-card>
                              </el-timeline-item>
                            </el-timeline>
                          </el-card>
                        </div>
                      </el-card>
                    </div>
                  </el-tab-pane>

                  <el-tab-pane label="部门级体系" name="department">
                    <div class="content-container">
                      <el-card shadow="never" class="tree-panel">
                        <template #header>
                          <div class="panel-header">
                            <span><b>部门制度结构</b></span>
                            <div class="header-toolbar">
                              <el-select v-model="selectedDept" placeholder="选择部门" clearable size="small" style="width:140px">
                                <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
                              </el-select>
                              <el-button size="small" type="primary" @click="addDeptNode">
                                <el-icon><Plus /></el-icon>新增部门制度
                              </el-button>
                            </div>
                          </div>
                        </template>
                        <el-tree
                          :data="filteredDeptTree"
                          :props="treeProps"
                          node-key="id"
                          :default-expand-all="expandAllFlag"
                          @node-click="handleDeptNodeClick"
                        >
                          <template #default="{ data }">
                            <span class="tree-node-content">
                              <span class="node-icon"><el-icon :component="getNodeIcon(data.type)" /></span>
                              <span class="node-label">{{ data.name }}</span>
                              <el-tag v-if="data.status === '有效'" size="mini" type="success" class="node-tag">有效</el-tag>
                              <el-tag v-else-if="data.status === '待审批'" size="mini" type="danger" class="node-tag">待审批</el-tag>
                            </span>
                          </template>
                        </el-tree>
                      </el-card>
                      <el-card shadow="never" class="detail-panel">
                        <template #header>
                          <div class="panel-header">
                            <span><b>{{ selectedDeptNode?.name || '请选择节点查看详情' }}</b></span>
                            <div class="header-toolbar" v-if="selectedDeptNode">
                              <el-button size="small" type="primary" @click="handleCreateDeptDoc">
                                <el-icon><Plus /></el-icon>新建
                              </el-button>
                              <el-button size="small" @click="handleEditDeptNode"><el-icon><Edit /></el-icon>编辑</el-button>
                              <el-button size="small" type="danger" @click="handleDeleteDeptNode"><el-icon><Delete /></el-icon>删除</el-button>
                            </div>
                          </div>
                        </template>
                        <div v-if="!selectedDeptNode" class="empty-state">
                          <el-icon :size="64" class="empty-icon"><OfficeBuilding /></el-icon>
                          <p>从左侧部门树形目录选择节点</p>
                        </div>
                        <div v-else class="node-detail">
                          <el-descriptions :column="2" border size="small">
                            <el-descriptions-item label="所属部门">
                              <el-tag size="small">{{ selectedDeptNode.deptName }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="节点状态">
                              <el-tag :type="getStatusTagType(selectedDeptNode.status)" size="small">{{ selectedDeptNode.status }}</el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="负责人">
                              {{ selectedDeptNode.owner || '-' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="描述" :span="2">
                              {{ selectedDeptNode.desc || '-' }}
                            </el-descriptions-item>
                          </el-descriptions>
                        </div>
                      </el-card>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </el-tab-pane>
          <el-tab-pane label="一级流程域" name="level1">
            <div class="level-content">
              <div class="domain-grid">
                <div 
                  v-for="domain in level1Data" 
                  :key="domain.id" 
                  class="domain-card"
                  :class="{ 'active': selectedDomain?.id === domain.id }"
                  @click="selectDomain(domain)"
                >
                  <div class="domain-icon">{{ getDomainIcon(domain.code) }}</div>
                  <div class="domain-info">
                    <div class="domain-name">{{ domain.name }}</div>
                    <div class="domain-code">{{ domain.code }}</div>
                    <div class="domain-count">二级流程组: {{ domain.childCount }}个</div>
                  </div>
                  <div class="domain-actions">
                    <el-button size="small" @click.stop="editDomain(domain)">编辑</el-button>
                    <el-button size="small" @click.stop="viewDetail(domain)">详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="二级流程组" name="level2">
            <div class="level-content">
              <div v-if="selectedDomain" class="selected-info">
                <span>所属流程域: {{ selectedDomain.name }}</span>
                <el-button size="small" @click="selectedDomain = null">返回</el-button>
              </div>
              <el-table :data="level2Data" border>
                <el-table-column prop="code" label="流程组编码" width="120" />
                <el-table-column prop="name" label="流程组名称" width="200" />
                <el-table-column prop="parentName" label="所属流程域" width="150" />
                <el-table-column prop="description" label="描述" width="200" />
                <el-table-column prop="deptName" label="归口部门" width="120" />
                <el-table-column prop="childCount" label="三级流程数" width="100" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'active' ? 'success' : 'info'">
                      {{ row.status === 'active' ? '生效中' : '规划中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="180">
                  <template #default="{ row }">
                    <el-button size="small" @click="editLevel2(row)">编辑</el-button>
                    <el-button size="small" @click="viewLevel2Detail(row)">详情</el-button>
                    <el-button size="small" @click="drillDown(row)">查看三级流程</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="三级主流程" name="level3">
            <div class="level-content">
              <el-table :data="level3Data" border>
                <el-table-column prop="code" label="流程编码" width="140" />
                <el-table-column prop="name" label="流程名称" width="200" />
                <el-table-column prop="parentName" label="所属流程组" width="150" />
                <el-table-column prop="description" label="流程描述" width="200" />
                <el-table-column prop="deptName" label="主责部门" width="120" />
                <el-table-column prop="version" label="当前版本" width="100" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="200">
                  <template #default="{ row }">
                    <el-button size="small" @click="editLevel3(row)">编辑</el-button>
                    <el-button size="small" @click="designFlow(row)">设计流程</el-button>
                    <el-button size="small" @click="viewLevel3Detail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="四级子流程" name="level4">
            <div class="level-content">
              <el-table :data="level4Data" border>
                <el-table-column prop="code" label="子流程编码" width="140" />
                <el-table-column prop="name" label="子流程名称" width="200" />
                <el-table-column prop="parentName" label="所属主流程" width="180" />
                <el-table-column prop="description" label="描述" width="200" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="160">
                  <template #default="{ row }">
                    <el-button size="small" @click="editLevel4(row)">编辑</el-button>
                    <el-button size="small" @click="designFlow(row)">设计</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="五级岗位流程" name="level5">
            <div class="level-content">
              <el-table :data="level5Data" border>
                <el-table-column prop="code" label="岗位流程编码" width="140" />
                <el-table-column prop="name" label="岗位流程名称" width="200" />
                <el-table-column prop="parentName" label="所属子流程" width="180" />
                <el-table-column prop="postName" label="关联岗位" width="120" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="160">
                  <template #default="{ row }">
                    <el-button size="small" @click="editLevel5(row)">编辑</el-button>
                    <el-button size="small" @click="viewSOP(row)">查看SOP</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-drawer title="流程域详情" v-model="detailDrawerVisible" size="500px">
      <div v-if="selectedDomain" class="domain-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="流程域编码">{{ selectedDomain.code }}</el-descriptions-item>
            <el-descriptions-item label="流程域名称">{{ selectedDomain.name }}</el-descriptions-item>
            <el-descriptions-item label="归口部门">{{ selectedDomain.deptName }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ selectedDomain.owner }}</el-descriptions-item>
            <el-descriptions-item label="建立日期">{{ selectedDomain.createDate }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedDomain.status === 'active' ? '生效中' : '规划中' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <h4>职责范围</h4>
          <div class="rich-text">{{ selectedDomain.responsibility }}</div>
        </div>
        <div class="detail-section">
          <h4>下属流程组</h4>
          <div class="el-list">
            <div class="el-list-item" v-for="group in selectedDomain.groups" :key="group.id">
              {{ group.code }} - {{ group.name }}
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog title="新增流程域" v-model="addModalVisible" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="流程域编码" prop="code">
          <el-input v-model="formData.code" placeholder="如 FD01" />
        </el-form-item>
        <el-form-item label="流程域名称" prop="name">
          <el-input v-model="formData.name" placeholder="如 战略规划" />
        </el-form-item>
        <el-form-item label="归口部门" prop="deptId">
          <el-select v-model="formData.deptId" placeholder="请选择部门">
            <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="formData.owner" />
        </el-form-item>
        <el-form-item label="职责范围" prop="responsibility">
          <el-input v-model="formData.responsibility" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Download, Refresh, OfficeBuilding, Shop, Goods, Document, User, Setting, TrendCharts, Aim, Trophy, Message, Tools, Box, Odometer, Lightning, Search, Edit, Delete, Upload, FolderOpened, Rank, Close, Connection, List, Grid } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

const activeLevel = ref('level1')
const selectedDomain = ref(null)
const detailDrawerVisible = ref(false)
const addModalVisible = ref(false)

// 树形视图相关变量
const treeActiveTab = ref('company')
const searchKeyword = ref('')
const selectedCompanyNode = ref(null)
const selectedDeptNode = ref(null)
const expandAllFlag = ref(true)
const selectedDept = ref('')

const stats = reactive({
  totalCompany: 3,
  totalDept: 8,
  totalDocuments: 45,
  totalFlows: 28,
  pendingCount: 5,
  effectiveCount: 68
})

const companyFilter = reactive({
  type: '',
  status: ''
})

const deptList = ref([
  { id: 'dept1', name: '研发中心' },
  { id: 'dept2', name: '销售部' },
  { id: 'dept3', name: '生产制造中心' },
  { id: 'dept4', name: '人力资源部' }
])

const treeProps = {
  children: 'children',
  label: 'name'
}

const formData = reactive({
  code: '',
  name: '',
  deptId: '',
  owner: '',
  responsibility: ''
})

const formRules = {
  code: [{ required: true, message: '请输入流程域编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入流程域名称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择归口部门', trigger: 'change' }]
}

const formRef = ref(null)

const deptOptions = ref([])

// 公司级树形数据
const companyTree = ref([
  {
    id: 'sys1',
    name: '人事管理制度体系',
    type: 'system',
    status: '有效',
    level: 1,
    owner: '周敏',
    createTime: '2026-01-01 09:00',
    docCount: 15,
    children: [
      {
        id: 'mod1-1',
        name: '招聘与入职管理',
        type: 'module',
        status: '有效',
        level: 2,
        owner: '陈静',
        children: [
          {
            id: 'doc1-1',
            name: '招聘管理制度',
            type: 'document',
            status: '有效',
            level: 3,
            owner: '赵敏',
            children: [
              { id: 'f1-1', name: '招聘管理制度V3.0.docx', type: 'file', status: '生效', level: 4, version: '3.0', isLatest: true, size: '1.2MB', uploader: '赵敏', uploadTime: '2026-06-01 14:30' },
              { id: 'f1-2', name: '招聘管理制度V2.5.docx', type: 'file', status: '历史', level: 4, version: '2.5', size: '1.1MB' },
              { id: 'f1-3', name: '招聘管理制度V2.0.docx', type: 'file', status: '历史', level: 4, version: '2.0', size: '980KB' }
            ]
          },
          { id: 'doc1-2', name: '新员工入职流程SOP', type: 'sop', status: '待审批', level: 3, owner: '赵敏', pendingCount: 1 },
          { id: 'flow1-1', name: '招聘申请审批流程', type: 'flow', status: '有效', level: 3, owner: '陈静' }
        ]
      },
      {
        id: 'mod1-2',
        name: '绩效考核管理',
        type: 'module',
        status: '有效',
        level: 2,
        owner: '陈静',
        children: [
          { id: 'doc1-3', name: '绩效考核管理办法', type: 'document', status: '有效', level: 3, owner: '周敏' },
          { id: 'doc1-4', name: '绩效申诉处理流程', type: 'flow', status: '作废', level: 3, owner: '周敏' }
        ]
      },
      { id: 'mod1-3', name: '培训发展管理', type: 'module', status: '有效', level: 2, owner: '刘洋' }
    ]
  },
  {
    id: 'sys2',
    name: '行政管理规范体系',
    type: 'system',
    status: '有效',
    level: 1,
    owner: '张伟',
    createTime: '2026-01-15 10:00',
    docCount: 12,
    children: [
      { id: 'mod2-1', name: '办公区域管理', type: 'module', status: '有效', level: 2, owner: '李明' },
      { id: 'mod2-2', name: '会议活动管理', type: 'module', status: '有效', level: 2, owner: '李明' }
    ]
  },
  {
    id: 'sys3',
    name: '财务管理制度体系',
    type: 'system',
    status: '有效',
    level: 1,
    owner: '王芳',
    createTime: '2026-02-01 09:00',
    docCount: 18,
    children: [
      {
        id: 'mod3-1',
        name: '费用报销管理',
        type: 'module',
        status: '有效',
        level: 2,
        owner: '王芳',
        children: [{ id: 'doc3-1', name: '费用报销管理规定', type: 'document', status: '有效', level: 3, owner: '王芳' }]
      }
    ]
  }
])

// 部门级树形数据
const deptTree = ref([
  {
    id: 'dept-sys1',
    name: '研发中心制度体系',
    type: 'system',
    status: '有效',
    deptName: '研发中心',
    children: [
      { id: 'dept-doc1', name: '代码审查规范', type: 'document', status: '有效', deptName: '研发中心', scope: '研发中心全体', owner: '孙磊' },
      { id: 'dept-doc2', name: '研发安全规范', type: 'document', status: '有效', deptName: '研发中心', owner: '孙磊' },
      { id: 'dept-doc3', name: '代码提交流程', type: 'flow', status: '有效', deptName: '研发中心', owner: '孙磊' }
    ]
  },
  {
    id: 'dept-sys2',
    name: '销售部制度体系',
    type: 'system',
    status: '有效',
    deptName: '销售部',
    children: [{ id: 'dept-doc4', name: '客户拜访规范', type: 'document', status: '有效', deptName: '销售部', scope: '销售部全体', owner: '李娜' }]
  },
  {
    id: 'dept-sys3',
    name: '生产制造中心制度体系',
    type: 'system',
    status: '有效',
    deptName: '生产制造中心',
    children: [
      { id: 'dept-doc5', name: '安全生产操作规程', type: 'document', status: '有效', deptName: '生产制造中心', owner: '刘洋' },
      { id: 'dept-doc6', name: '设备维护保养规定', type: 'document', status: '作废', deptName: '生产制造中心', owner: '刘洋' }
    ]
  }
])

// 版本文件列表
const companyVersionFiles = computed(() => {
  if (!selectedCompanyNode.value?.children) return []
  const files = []
  selectedCompanyNode.value.children.forEach(child => {
    if (child.type === 'file') files.push(child)
    if (child.children) {
      child.children.forEach(subChild => {
        if (subChild.type === 'file') files.push(subChild)
      })
    }
  })
  return files.length > 0 ? files : [
    { id: 'v1', version: '3.0', fileName: '招聘管理制度V3.0.docx', fileType: 'Word', uploadTime: '2026-06-01 14:30', status: '生效', isCurrent: true },
    { id: 'v2', version: '2.5', fileName: '招聘管理制度V2.5.docx', fileType: 'Word', uploadTime: '2026-03-15 10:20', status: '历史', isCurrent: false },
    { id: 'v3', version: '2.0', fileName: '招聘管理制度V2.0.docx', fileType: 'Word', uploadTime: '2026-01-20 16:45', status: '历史', isCurrent: false }
  ]
})

// 操作日志
const nodeOperationLogs = ref([
  { time: '2026-06-15 14:30', operator: '赵敏', action: '上传新版本', detail: '上传招聘管理制度V3.0版本' },
  { time: '2026-06-10 09:15', operator: '陈静', action: '编辑节点', detail: '修改负责人为赵敏' },
  { time: '2026-05-20 16:45', operator: '周敏', action: '新增子节点', detail: '新增"绩效考核"业务模块' },
  { time: '2026-04-01 10:00', operator: '系统', action: '自动创建', detail: '初始化人事管理制度体系' }
])

// 计算属性
const filteredCompanyTree = computed(() => {
  let tree = companyTree.value
  if (searchKeyword.value) {
    tree = filterTreeByKeyword(tree, searchKeyword.value)
  }
  if (companyFilter.type) {
    tree = filterTreeByType(tree, companyFilter.type)
  }
  if (companyFilter.status) {
    tree = filterTreeByStatus(tree, companyFilter.status)
  }
  return tree
})

const filteredDeptTree = computed(() => {
  let tree = deptTree.value
  if (selectedDept.value) {
    tree = tree.filter(d => d.id === selectedDept.value || d.deptName === selectedDept.value)
  }
  if (searchKeyword.value) {
    tree = filterTreeByKeyword(tree, searchKeyword.value)
  }
  return tree
})

// 树形过滤方法
function filterTreeByKeyword(tree, keyword) {
  const result = []
  tree.forEach(node => {
    if (node.name.includes(keyword)) {
      result.push({ ...node })
    } else if (node.children) {
      const filteredChildren = filterTreeByKeyword(node.children, keyword)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  })
  return result
}

function filterTreeByType(tree, type) {
  const result = []
  tree.forEach(node => {
    if (node.type === type) {
      result.push({ ...node })
    } else if (node.children) {
      const filteredChildren = filterTreeByType(node.children, type)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  })
  return result
}

function filterTreeByStatus(tree, status) {
  const result = []
  tree.forEach(node => {
    if (node.status === status) {
      result.push({ ...node })
    } else if (node.children) {
      const filteredChildren = filterTreeByStatus(node.children, status)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  })
  return result
}

// 获取节点图标
function getNodeIcon(type) {
  const icons = {
    system: FolderOpened,
    module: Document,
    document: Document,
    flow: Connection,
    sop: List,
    file: Document
  }
  return icons[type] || FolderOpened
}

function getTypeLabel(type) {
  const labels = { system: '体系分类', module: '业务模块', document: '制度清单', flow: '流程文件', sop: '作业SOP', file: '版本文件' }
  return labels[type] || type
}

function getTypeTagType(type) {
  const types = { system: 'primary', module: 'success', document: 'warning', flow: 'info', sop: 'info', file: 'info' }
  return types[type] || 'info'
}

function getFileStatusType(status) {
  const types = { '生效': 'success', '当前': 'success', '历史': 'info', '作废': 'danger' }
  return types[status] || 'info'
}

// 树形视图方法
function handleSearch() { ElMessage.info('搜索功能开发中') }
function handleExport() { ElMessage.info('导出功能开发中') }
function refreshTree() { ElMessage.success('树形已刷新') }
function handleCompanyNodeClick(data) { selectedCompanyNode.value = data }
function handleDeptNodeClick(data) { selectedDeptNode.value = data }
function expandAll() { expandAllFlag.value = true }
function collapseAll() { expandAllFlag.value = false }
function addNode(type) { ElMessage.info(`新增${getTypeLabel(type)}节点`) }
function addDeptNode() { ElMessage.info('新增部门制度') }
function handleCreateDoc(type) { ElMessage.info(`创建${getTypeLabel(type)}`) }
function handleCreateDeptDoc() { ElMessage.info('创建部门文档') }
function handleEditNode() { ElMessage.info('编辑节点') }
function handleEditDeptNode() { ElMessage.info('编辑部门节点') }
function handleDeleteNode() { ElMessage.info('删除节点') }
function handleDeleteDeptNode() { ElMessage.info('删除部门节点') }
function handleUploadNewVersion() { ElMessage.info('上传新版本') }
function handlePreviewFile(row) { ElMessage.info(`预览文件: ${row.fileName}`) }
function handleDownloadFile(row) { ElMessage.info(`下载文件: ${row.fileName}`) }

const level1Data = ref([])
const level2Data = ref([])
const level3Data = ref([])
const level4Data = ref([])
const level5Data = ref([])

const getDomainIcon = (code) => {
  const iconMap = {
    'FD01': OfficeBuilding,
    'FD02': Shop,
    'FD03': Goods,
    'FD04': Document,
    'FD05': User,
    'FD06': Setting,
    'FD07': TrendCharts,
    'FD08': Aim,
    'FD09': Trophy,
    'FD10': Tools,
    'FD11': Box,
    'FD12': Odometer,
    'FD13': Lightning
  }
  return iconMap[code] || OfficeBuilding
}

const getStatusName = (status) => {
  const map = { active: '生效中', draft: '草稿', pending: '待审核', deprecated: '已废止', archived: '已归档' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { active: 'success', draft: 'info', pending: 'warning', deprecated: 'danger', archived: 'info' }
  return map[status] || 'info'
}

const handleLevelChange = () => {
  loadLevelData()
}

const selectDomain = (domain) => {
  selectedDomain.value = domain
}

const viewDetail = (domain) => {
  selectedDomain.value = domain
  detailDrawerVisible.value = true
}

const editDomain = (domain) => {
  ElMessage.info(`编辑流程域: ${domain.name}`)
}

const editLevel2 = (row) => {
  ElMessage.info(`编辑流程组: ${row.name}`)
}

const viewLevel2Detail = (row) => {
  ElMessage.info(`查看流程组详情: ${row.name}`)
}

const drillDown = (row) => {
  activeLevel.value = 'level3'
}

const editLevel3 = (row) => {
  ElMessage.info(`编辑主流程: ${row.name}`)
}

const designFlow = (row) => {
  ElMessage.info(`设计流程: ${row.name}`)
}

const viewLevel3Detail = (row) => {
  ElMessage.info(`查看主流程详情: ${row.name}`)
}

const editLevel4 = (row) => {
  ElMessage.info(`编辑子流程: ${row.name}`)
}

const editLevel5 = (row) => {
  ElMessage.info(`编辑岗位流程: ${row.name}`)
}

const viewSOP = (row) => {
  ElMessage.info(`查看SOP: ${row.name}`)
}

const openAddModal = (level) => {
  addModalVisible.value = true
}

const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('新增成功')
      addModalVisible.value = false
      loadArchitecture()
    }
  })
}

const exportArchitecture = () => {
  window.open('/api/epros/export/architecture')
}

const loadArchitecture = async () => {
  level1Data.value = [
    { id: 1, code: 'FD01', name: '战略经营流程域', deptName: '战略管理部', owner: '战略管理部负责人', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '企业战略规划、年度经营计划、投资决策、经营分析', system: '通用体系' },
    { id: 2, code: 'FD02', name: '产品研发流程域', deptName: '研发中心', owner: '研发总监', childCount: 5, status: 'active', createDate: '2024-01-01', responsibility: '产品规划、研发设计、样品验证、技术文档管理', system: '通用体系' },
    { id: 3, code: 'FD03', name: '供应链采购流程域', deptName: '采购部', owner: '采购总监', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '供应商管理、采购执行、合同管理、来料验收', system: '通用体系' },
    { id: 4, code: 'FD04', name: '生产制造流程域', deptName: '生产中心', owner: '生产总监', childCount: 5, status: 'active', createDate: '2024-01-01', responsibility: '生产计划、车间作业、异常处理、生产追溯', system: '通用体系' },
    { id: 5, code: 'FD05', name: 'PCBA制程工艺流程域', deptName: 'PCBA车间', owner: 'PCBA经理', childCount: 6, status: 'active', createDate: '2024-01-01', responsibility: 'SMT上料防错、贴片管控、回流焊、AOI检测、ESD防护', system: '安全生产' },
    { id: 6, code: 'FD06', name: '品质管控流程域', deptName: '品质部', owner: '品质总监', childCount: 6, status: 'active', createDate: '2024-01-01', responsibility: '来料检验、制程巡检、成品检验、不良品处置、持续改进', system: '通用体系' },
    { id: 7, code: 'FD07', name: '设备工装流程域', deptName: '设备部', owner: '设备经理', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '设备维护、工装管理、备件管理、设备点检', system: '安全生产' },
    { id: 8, code: 'FD08', name: '仓储物流流程域', deptName: '仓储部', owner: '仓储经理', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '物料存储、出入库管理、库存盘点、物流配送', system: '通用体系' },
    { id: 9, code: 'FD09', name: 'EHS合规管理流程域', deptName: 'EHS部', owner: 'EHS经理', childCount: 7, status: 'active', createDate: '2024-01-01', responsibility: '体系文件、风险管控、职业健康、隐患排查、应急管理', system: '安全/环境/职业健康' },
    { id: 10, code: 'FD10', name: '五金工艺流程域', deptName: '五金车间', owner: '五金车间经理', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '五金冲压、CNC加工、表面处理、品质检验', system: '安全生产' },
    { id: 11, code: 'FD11', name: '塑料工艺流程域', deptName: '注塑车间', owner: '注塑车间经理', childCount: 4, status: 'active', createDate: '2024-01-01', responsibility: '注塑成型、模具管理、品质管控、物料追溯', system: '安全生产' },
    { id: 12, code: 'FD12', name: '电子装配工艺流程域', deptName: '装配车间', owner: '装配车间经理', childCount: 5, status: 'active', createDate: '2024-01-01', responsibility: '插件装配、焊接、测试调试、成品组装', system: '安全生产' },
    { id: 13, code: 'FD13', name: '能源管控流程域', deptName: '能源管理部', owner: '能源管理员', childCount: 5, status: 'active', createDate: '2024-01-01', responsibility: '能源数据采集、能耗分析、节能改善、能源评审', system: '能源管理' }
  ]
  
  level2Data.value = [
    { id: 101, code: 'FG0401', name: '生产计划管理组', parentName: '生产制造流程域', parentCode: 'FD04', description: '生产订单接收、产能评估、计划排产', deptName: '生产中心', childCount: 3, status: 'active' },
    { id: 102, code: 'FG0402', name: '生产排程与换线组', parentName: '生产制造流程域', parentCode: 'FD04', description: '产线排程、换线准备、换线确认', deptName: '生产中心', childCount: 4, status: 'active' },
    { id: 103, code: 'FG0403', name: '车间作业执行组', parentName: '生产制造流程域', parentCode: 'FD04', description: '作业指导、生产执行、产量统计', deptName: '生产中心', childCount: 5, status: 'active' },
    { id: 104, code: 'FG0404', name: '生产异常处理组', parentName: '生产制造流程域', parentCode: 'FD04', description: '异常报告、原因分析、整改措施', deptName: '生产中心', childCount: 3, status: 'active' },
    { id: 105, code: 'FG0405', name: '生产追溯管理组', parentName: '生产制造流程域', parentCode: 'FD04', description: '批次追溯、条码管理、质量追踪', deptName: '生产中心', childCount: 2, status: 'active' },
    { id: 106, code: 'FG0501', name: 'SMT上料防错组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '物料核对、条码扫描、防错验证', deptName: 'PCBA车间', childCount: 3, status: 'active' },
    { id: 107, code: 'FG0502', name: '贴片制程管控组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '钢网管理、锡膏印刷、贴片操作', deptName: 'PCBA车间', childCount: 4, status: 'active' },
    { id: 108, code: 'FG0503', name: '回流焊制程管控组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '炉温监控、参数调整、品质控制', deptName: 'PCBA车间', childCount: 3, status: 'active' },
    { id: 109, code: 'FG0504', name: 'AOI检测管控组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '自动检测、不良判定、误判复核', deptName: '品质部', childCount: 3, status: 'active' },
    { id: 110, code: 'FG0505', name: 'ESD静电防护管控组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '静电监测、接地检查、防护措施', deptName: 'EHS部', childCount: 2, status: 'active' },
    { id: 111, code: 'FG0506', name: '无尘车间环境管控组', parentName: 'PCBA制程工艺流程域', parentCode: 'FD05', description: '温湿度控制、洁净度监测、环境记录', deptName: 'EHS部', childCount: 2, status: 'active' },
    { id: 112, code: 'FG0601', name: '来料IQC检验组', parentName: '品质管控流程域', parentCode: 'FD06', description: '来料验收、检验判定、不合格处理', deptName: '品质部', childCount: 4, status: 'active' },
    { id: 113, code: 'FG0602', name: '制程IPQC巡检组', parentName: '品质管控流程域', parentCode: 'FD06', description: '首件确认、巡检记录、异常反馈', deptName: '品质部', childCount: 3, status: 'active' },
    { id: 114, code: 'FG0603', name: '成品FQC/OQC检验组', parentName: '品质管控流程域', parentCode: 'FD06', description: '成品检验、出货检验、放行判定', deptName: '品质部', childCount: 4, status: 'active' },
    { id: 115, code: 'FG0604', name: '不良品处置管控组', parentName: '品质管控流程域', parentCode: 'FD06', description: '不良品隔离、返工、报废处理', deptName: '品质部', childCount: 3, status: 'active' },
    { id: 116, code: 'FG0605', name: '客户稽核与客诉组', parentName: '品质管控流程域', parentCode: 'FD06', description: '客户审核、客诉处理、8D报告', deptName: '品质部', childCount: 3, status: 'active' },
    { id: 117, code: 'FG0606', name: '品质持续改进组', parentName: '品质管控流程域', parentCode: 'FD06', description: 'QC改善、六西格玛、QCC活动', deptName: '品质部', childCount: 2, status: 'active' },
    { id: 118, code: 'FG0901', name: '体系文件管控组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '文件编写、审批发布、版本控制', deptName: 'EHS部', childCount: 4, status: 'active' },
    { id: 119, code: 'FG0902', name: '风险与危险源管控组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '风险评估、危险源识别、管控措施', deptName: 'EHS部', childCount: 3, status: 'active' },
    { id: 120, code: 'FG0903', name: '环境因素与合规管控组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '环境因素识别、合规评估、法规跟踪', deptName: 'EHS部', childCount: 2, status: 'active' },
    { id: 121, code: 'FG0904', name: '职业健康管控组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '职业病预防、健康监测、劳保用品', deptName: 'EHS部', childCount: 3, status: 'active' },
    { id: 122, code: 'FG0905', name: '隐患排查整改组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '隐患检查、整改跟踪、验证闭环', deptName: 'EHS部', childCount: 3, status: 'active' },
    { id: 123, code: 'FG0906', name: '应急管理组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '应急预案、演练、应急处置', deptName: 'EHS部', childCount: 2, status: 'active' },
    { id: 124, code: 'FG0907', name: '体系审核与评审组', parentName: 'EHS合规管理流程域', parentCode: 'FD09', description: '内部审核、管理评审、不符合项整改', deptName: 'EHS部', childCount: 3, status: 'active' },
    { id: 125, code: 'FG1301', name: '能源数据采集组', parentName: '能源管控流程域', parentCode: 'FD13', description: '能耗计量、数据采集、抄表核对', deptName: '能源管理部', childCount: 2, status: 'active' },
    { id: 126, code: 'FG1302', name: '能耗统计分析组', parentName: '能源管控流程域', parentCode: 'FD13', description: '能耗统计、趋势分析、报表生成', deptName: '能源管理部', childCount: 2, status: 'active' },
    { id: 127, code: 'FG1303', name: '能耗异常管控组', parentName: '能源管控流程域', parentCode: 'FD13', description: '异常识别、原因分析、纠偏措施', deptName: '能源管理部', childCount: 2, status: 'active' },
    { id: 128, code: 'FG1304', name: '节能改善闭环组', parentName: '能源管控流程域', parentCode: 'FD13', description: '节能项目、效果验证、标准化', deptName: '能源管理部', childCount: 3, status: 'active' },
    { id: 129, code: 'FG1305', name: '能源评审与绩效管控组', parentName: '能源管控流程域', parentCode: 'FD13', description: '年度评审、绩效指标、目标管理', deptName: '能源管理部', childCount: 2, status: 'active' },
    // 五金工艺流程域二级流程组
    { id: 130, code: 'FG1001', name: '五金冲压加工组', parentName: '五金工艺流程域', parentCode: 'FD10', description: '冲压工艺、模具更换、品质检验', deptName: '五金车间', childCount: 3, status: 'active' },
    { id: 131, code: 'FG1002', name: 'CNC加工组', parentName: '五金工艺流程域', parentCode: 'FD10', description: '数控加工、程序管理、精度控制', deptName: '五金车间', childCount: 3, status: 'active' },
    { id: 132, code: 'FG1003', name: '表面处理组', parentName: '五金工艺流程域', parentCode: 'FD10', description: '电镀、喷涂、阳极氧化', deptName: '五金车间', childCount: 2, status: 'active' },
    { id: 133, code: 'FG1004', name: '五金品质检验组', parentName: '五金工艺流程域', parentCode: 'FD10', description: '尺寸测量、外观检验、硬度测试', deptName: '品质部', childCount: 2, status: 'active' },
    // 塑料工艺流程域二级流程组
    { id: 134, code: 'FG1101', name: '注塑成型组', parentName: '塑料工艺流程域', parentCode: 'FD11', description: '注塑工艺、参数设置、成型控制', deptName: '注塑车间', childCount: 4, status: 'active' },
    { id: 135, code: 'FG1102', name: '模具管理组', parentName: '塑料工艺流程域', parentCode: 'FD11', description: '模具维护、保养、更换', deptName: '注塑车间', childCount: 2, status: 'active' },
    { id: 136, code: 'FG1103', name: '塑料品质管控组', parentName: '塑料工艺流程域', parentCode: 'FD11', description: '外观检验、尺寸测量、性能测试', deptName: '品质部', childCount: 2, status: 'active' },
    { id: 137, code: 'FG1104', name: '塑料物料追溯组', parentName: '塑料工艺流程域', parentCode: 'FD11', description: '原料批次、生产记录、追溯管理', deptName: '注塑车间', childCount: 2, status: 'active' },
    // 电子装配工艺流程域二级流程组
    { id: 138, code: 'FG1201', name: '插件装配组', parentName: '电子装配工艺流程域', parentCode: 'FD12', description: '元器件插件、手工焊接、AOI检测', deptName: '装配车间', childCount: 4, status: 'active' },
    { id: 139, code: 'FG1202', name: '波峰焊组', parentName: '电子装配工艺流程域', parentCode: 'FD12', description: '波峰焊工艺、参数控制、品质监控', deptName: '装配车间', childCount: 3, status: 'active' },
    { id: 140, code: 'FG1203', name: '测试调试组', parentName: '电子装配工艺流程域', parentCode: 'FD12', description: '功能测试、性能调试、故障排查', deptName: '装配车间', childCount: 4, status: 'active' },
    { id: 141, code: 'FG1204', name: '成品组装组', parentName: '电子装配工艺流程域', parentCode: 'FD12', description: '整机装配、包装、入库', deptName: '装配车间', childCount: 3, status: 'active' },
    { id: 142, code: 'FG1205', name: '返修重工组', parentName: '电子装配工艺流程域', parentCode: 'FD12', description: '不良品返修、重工处理', deptName: '装配车间', childCount: 2, status: 'active' }
  ]
  
  level3Data.value = [
    { id: 1001, code: 'FP040101', name: '生产订单下达与排程主流程', parentName: '生产计划管理组', parentCode: 'FG0401', description: '从订单接收到生产排程的完整流程', deptName: '生产中心', version: 'V2.0', status: 'active', bpmnRequired: true },
    { id: 1002, code: 'FP040201', name: 'SMT生产换线确认主流程', parentName: '生产排程与换线组', parentCode: 'FG0402', description: '换线准备、确认、首件验证', deptName: 'SMT车间', version: 'V3.0', status: 'active', bpmnRequired: true },
    { id: 1003, code: 'FP050101', name: 'SMT首件确认主流程', parentName: 'SMT上料防错组', parentCode: 'FG0501', description: '首件制作、检验、确认放行', deptName: 'SMT车间', version: 'V2.0', status: 'active', bpmnRequired: true },
    { id: 1004, code: 'FP040401', name: '制程异常停机处置主流程', parentName: '生产异常处理组', parentCode: 'FG0404', description: '异常报告、原因分析、恢复生产', deptName: '生产中心', version: 'V2.0', status: 'active', bpmnRequired: true },
    { id: 1005, code: 'FP060101', name: '来料检验判定与放行主流程', parentName: '来料IQC检验组', parentCode: 'FG0601', description: '来料验收、检验、判定、放行', deptName: '品质部', version: 'V3.0', status: 'active', bpmnRequired: true },
    { id: 1006, code: 'FP060301', name: '成品检验与出货放行主流程', parentName: '成品FQC/OQC检验组', parentCode: 'FG0603', description: '成品检验、出货检验、放行审批', deptName: '品质部', version: 'V2.0', status: 'active', bpmnRequired: true },
    { id: 1007, code: 'FP060401', name: '不良品隔离、返工、报废闭环主流程', parentName: '不良品处置管控组', parentCode: 'FG0604', description: '不良品处理全流程', deptName: '品质部', version: 'V2.0', status: 'active', bpmnRequired: true },
    { id: 1008, code: 'FP050501', name: 'ESD日常检查与异常处置主流程', parentName: 'ESD静电防护管控组', parentCode: 'FG0505', description: '静电防护日常检查与异常处理', deptName: 'EHS部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '安全生产' },
    { id: 1009, code: 'FP050601', name: '无尘车间温湿度/洁净度管控主流程', parentName: '无尘车间环境管控组', parentCode: 'FG0506', description: '环境参数监控与调整', deptName: 'EHS部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '安全生产' },
    { id: 1010, code: 'FP090101', name: 'EHS体系文件全生命周期主流程', parentName: '体系文件管控组', parentCode: 'FG0901', description: '文件编写、审批、发布、废止', deptName: 'EHS部', version: 'V2.0', status: 'active', bpmnRequired: true, system: '安全/环境/职业健康' },
    { id: 1011, code: 'FP090501', name: '隐患排查、整改、验证闭环主流程', parentName: '隐患排查整改组', parentCode: 'FG0905', description: '隐患识别、整改、验证全流程', deptName: 'EHS部', version: 'V2.0', status: 'active', bpmnRequired: true, system: '安全生产' },
    { id: 1012, code: 'FP090701', name: '内外部审核不符合项整改主流程', parentName: '体系审核与评审组', parentCode: 'FG0907', description: '审核发现问题的整改闭环', deptName: 'EHS部', version: 'V2.0', status: 'active', bpmnRequired: true, system: '安全/环境/职业健康' },
    { id: 1013, code: 'FP090702', name: '年度管理评审闭环主流程', parentName: '体系审核与评审组', parentCode: 'FG0907', description: '年度管理评审全过程', deptName: 'EHS部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '安全/环境/职业健康' },
    { id: 1014, code: 'FP130101', name: '能耗数据采集、核对、汇总主流程', parentName: '能源数据采集组', parentCode: 'FG1301', description: '能耗数据的采集与汇总', deptName: '能源管理部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '能源管理' },
    { id: 1015, code: 'FP130301', name: '能耗异常分析与纠偏主流程', parentName: '能耗异常管控组', parentCode: 'FG1303', description: '能耗异常识别与纠正', deptName: '能源管理部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '能源管理' },
    { id: 1016, code: 'FP130401', name: '节能改善项目立项-实施-验证-固化主流程', parentName: '节能改善闭环组', parentCode: 'FG1304', description: '节能项目全生命周期管理', deptName: '能源管理部', version: 'V1.0', status: 'active', bpmnRequired: true, system: '能源管理' }
  ]
  
  level4Data.value = [
    { id: 10001, code: 'FS050101', name: 'SMT物料上料核对子流程', parentName: 'SMT上料防错组', parentCode: 'FG0501', description: '物料条码扫描、批次核对', version: 'V2.0', status: 'active', reusable: true },
    { id: 10002, code: 'FS050301', name: '炉温参数异常调整子流程', parentName: '回流焊制程管控组', parentCode: 'FG0503', description: '炉温监控异常时的调整流程', version: 'V1.0', status: 'active', reusable: true },
    { id: 10003, code: 'FS050401', name: 'AOI误判复核子流程', parentName: 'AOI检测管控组', parentCode: 'FG0504', description: 'AOI检测结果的人工复核', version: 'V1.0', status: 'active', reusable: true },
    { id: 10004, code: 'FS050501', name: '车间静电超标处置子流程', parentName: 'ESD静电防护管控组', parentCode: 'FG0505', description: '静电超标时的应急处理', version: 'V1.0', status: 'active', reusable: true },
    { id: 10005, code: 'FS060101', name: '来料不合格让步接收子流程', parentName: '来料IQC检验组', parentCode: 'FG0601', description: '不合格来料的让步审批', version: 'V2.0', status: 'active', reusable: true },
    { id: 10006, code: 'FS060401', name: '制程不良追溯分析子流程', parentName: '不良品处置管控组', parentCode: 'FG0604', description: '不良品原因追溯分析', version: 'V1.0', status: 'active', reusable: true },
    { id: 10007, code: 'FS090101', name: '文件修订发起子流程', parentName: '体系文件管控组', parentCode: 'FG0901', description: '文件修订申请与审批', version: 'V2.0', status: 'active', reusable: true },
    { id: 10008, code: 'FS090102', name: '文件作废归档子流程', parentName: '体系文件管控组', parentCode: 'FG0901', description: '文件作废与归档', version: 'V1.0', status: 'active', reusable: true },
    { id: 10009, code: 'FS090501', name: '隐患分级判定子流程', parentName: '隐患排查整改组', parentCode: 'FG0905', description: '隐患等级判定标准', version: 'V1.0', status: 'active', reusable: true },
    { id: 10010, code: 'FS090502', name: '整改措施验证子流程', parentName: '隐患排查整改组', parentCode: 'FG0905', description: '整改效果的验证确认', version: 'V1.0', status: 'active', reusable: true },
    { id: 10011, code: 'FS130101', name: '能耗抄表异常复核子流程', parentName: '能源数据采集组', parentCode: 'FG1301', description: '抄表数据异常的复核', version: 'V1.0', status: 'active', reusable: true },
    { id: 10012, code: 'FS130401', name: '节能效果数据验证子流程', parentName: '节能改善闭环组', parentCode: 'FG1304', description: '节能项目效果验证', version: 'V1.0', status: 'active', reusable: true }
  ]
  
  level5Data.value = [
    { id: 100001, code: 'FJ050101', name: 'SMT操作员上料操作流程', parentName: 'SMT物料上料核对子流程', parentCode: 'FS050101', postName: 'SMT操作员', version: 'V2.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100002, code: 'FJ060201', name: 'IPQC首件巡检操作流程', parentName: '制程IPQC巡检组', parentCode: 'FG0602', postName: 'IPQC检验员', version: 'V2.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100003, code: 'FJ070101', name: '设备操作员开机点检流程', parentName: '设备工装流程域', parentCode: 'FD07', postName: '设备操作员', version: 'V1.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100004, code: 'FJ090501', name: '车间安全员日常隐患排查操作流程', parentName: '隐患排查整改组', parentCode: 'FG0905', postName: '车间安全员', version: 'V1.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100005, code: 'FJ090101', name: 'EHS专员文件发布与培训流程', parentName: '体系文件管控组', parentCode: 'FG0901', postName: 'EHS专员', version: 'V2.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100006, code: 'FJ130101', name: '能源管理员抄表与数据核对流程', parentName: '能源数据采集组', parentCode: 'FG1301', postName: '能源管理员', version: 'V1.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100007, code: 'FJ080101', name: '仓管员物料收发合规操作流程', parentName: '仓储物流流程域', parentCode: 'FD08', postName: '仓管员', version: 'V2.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100008, code: 'FJ060401', name: '品质员不良品判定操作流程', parentName: '不良品处置管控组', parentCode: 'FG0604', postName: '品质员', version: 'V2.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100009, code: 'FJ090601', name: '行政应急物资点检操作流程', parentName: '应急管理组', parentCode: 'FG0906', postName: '行政专员', version: 'V1.0', status: 'active', sopRequired: true, formRequired: true },
    { id: 100010, code: 'FJ090401', name: '车间员工职业健康防护操作流程', parentName: '职业健康管控组', parentCode: 'FG0904', postName: '车间员工', version: 'V1.0', status: 'active', sopRequired: true, formRequired: true }
  ]

  // 三/四/五级流程以后端流程架构数据为准（仅已发布 active）
  await syncRemoteLevels()
}

const mapDefToRow = (d, extra) => ({
  id: d.id,
  code: d.code,
  name: d.name,
  parentCode: d.category || '',
  parentName: '',
  description: d.description || '',
  deptName: d.ownerDept || '',
  version: d.version || '',
  status: d.status,
  ...extra
})

const syncRemoteLevels = async () => {
  try {
    const res = await axios.get('/api/process/architecture')
    const payload = res.data && res.data.data ? res.data.data : {}
    const levels = payload.levels || {}
    level3Data.value = (levels.level3 || []).map(d => mapDefToRow(d, { bpmnRequired: true, system: '' }))
    level4Data.value = (levels.level4 || []).map(d => mapDefToRow(d, { reusable: true }))
    level5Data.value = (levels.level5 || []).map(d => ({
      ...mapDefToRow(d, {}),
      postName: d.ownerDept || '',
      sopRequired: true,
      formRequired: true
    }))
  } catch (e) {
    // 后端不可用时保留本地示例数据
    console.warn('流程架构数据加载失败，使用本地示例数据', e)
  }
}

const loadLevelData = () => {
  loadArchitecture()
}

const loadDeptOptions = () => {
  deptOptions.value = [
    { id: 1, name: '战略管理部' },
    { id: 2, name: '供应链中心' },
    { id: 3, name: '生产中心' },
    { id: 4, name: '研发中心' },
    { id: 5, name: '人力资源部' },
    { id: 6, name: '财务部' },
    { id: 7, name: '市场部' },
    { id: 8, name: 'EHS部' },
    { id: 9, name: '品质部' },
    { id: 10, name: 'IT部' }
  ]
}

onMounted(() => {
  loadArchitecture()
  loadDeptOptions()
})
</script>

<style lang="scss" scoped>
.epros-architecture {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: #fff;
  
  .header-title {
    h2 {
      margin: 0 0 4px 0;
      font-size: 18px;
    }
    p {
      margin: 0;
      font-size: 12px;
      opacity: 0.8;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.architecture-container {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.level-tabs {
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

.level-content {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  height: 100%;
}

.domain-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.domain-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }
  
  &.active {
    border-color: #3b82f6;
    background: #eff6ff;
  }
  
  .domain-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    background: linear-gradient(135deg, #3b82f6 0%, #1e3a8a 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
  }
  
  .domain-info {
    flex: 1;
    
    .domain-name {
      font-size: 15px;
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }
    
    .domain-code {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
    }
    
    .domain-count {
      font-size: 12px;
      color: #999;
    }
  }
  
  .domain-actions {
    display: flex;
    gap: 8px;
  }
}

.selected-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #eff6ff;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #1e3a8a;
  font-weight: 500;
}

.domain-detail {
  padding: 12px;
}

.detail-section {
  margin-bottom: 20px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8e8e8;
  }
}

.rich-text {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
  min-height: 80px;
}

/* 树形视图样式 */
.tree-view-content {
  padding: 16px;
  height: 100%;
}

.tree-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  
  .header-left {
    h2 { margin: 0 0 4px 0; }
    .header-subtitle { margin: 0; font-size: 12px; color: #999; }
  }
  
  .header-actions { display: flex; gap: 8px; }
}

.stats-row { margin-bottom: 16px; }

.stat-card {
  text-align: center;
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #333;
  }
  
  .stat-label { font-size: 12px; color: #999; }
  
  &.warning .stat-value { color: #e6a23c; }
  &.success .stat-value { color: #67c23a; }
}

.main-tabs { margin-top: 16px; }

.content-container {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  height: calc(100% - 60px);
}

.tree-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header-toolbar { display: flex; gap: 8px; }
  
  .tree-filter {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e8e8e8;
  }
  
  :deep(.el-tree) {
    flex: 1;
    overflow-y: auto;
  }
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .node-icon { font-size: 16px; color: #666; }
  .node-label { flex: 1; font-size: 14px; }
  .node-label.is-disabled { text-decoration: line-through; color: #999; }
  .node-tag { margin-left: 4px; }
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header-toolbar { display: flex; gap: 8px; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  
  .empty-icon { color: #ccc; margin-bottom: 16px; }
  p { margin: 0; color: #999; }
}

.node-detail {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
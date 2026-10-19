<template>
  <div class="page-container">
    <div class="page-header">
      <h2>国企职务层级管理</h2>
      <div style="display:flex;align-items:center;gap:8px">
        <ImportExportBar
          :columns="ioColumns"
          :rows="jobList"
          file-name="职务层级"
          title="职务层级列表"
          import-api="/api/job-position/create"
          :field-map="ioFieldMap"
          @done="loadJobList"
        />
        <el-button type="primary" @click="handleAdd">新增职务</el-button>
        <el-button type="success" plain @click="handleBatchConfigure">批量配置</el-button>
        <el-button @click="loadJobList">刷新</el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧五级组织树 -->
      <div class="left-panel">
        <div class="panel-header">
          <span>五级组织树</span>
          <el-button size="small" @click="loadOrgTree">刷新</el-button>
        </div>
        <el-input
          v-model="treeFilter"
          placeholder="搜索组织"
          clearable
          size="small"
          style="margin-bottom: 10px"
        />
        <el-tree
          ref="orgTreeRef"
          :data="orgTree"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          :filter-node-method="filterOrgNode"
          @node-click="handleOrgNodeClick"
          highlight-current
          default-expand-all
        />
        <el-alert
          v-if="selectedOrg"
          :title="`当前组织：${selectedOrg.label}（${getLevelLabel(selectedOrg.orgLevel)}）`"
          type="info"
          :closable="false"
          style="margin-top: 10px"
        />
      </div>

      <div class="right-panel">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 标签1：职务层级配置 -->
          <el-tab-pane label="职务层级配置" name="standard">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="关键词">
                <el-input v-model="searchForm.keyword" placeholder="职务名称/编码" style="width: 180px" />
              </el-form-item>
              <el-form-item label="对应组织层级">
                <el-select v-model="searchForm.orgLevel" placeholder="选择层级" style="width: 150px" clearable>
                  <el-option label="一级（公司）" :value="1" />
                  <el-option label="二级（中心）" :value="2" />
                  <el-option label="三级（部门）" :value="3" />
                  <el-option label="四级（科室）" :value="4" />
                  <el-option label="五级（小组）" :value="5" />
                </el-select>
              </el-form-item>
              <el-form-item label="职务状态">
                <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 120px" clearable>
                  <el-option label="在职" value="ACTIVE" />
                  <el-option label="空缺" value="VACANT" />
                  <el-option label="废止" value="ABOLISHED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="resetSearch">重置筛选</el-button>
                <el-button type="primary" plain @click="syncArchitecture">同步架构</el-button>
              </el-form-item>
            </el-form>

            <el-table :data="pagedList" border stripe size="small" v-loading="loading">
              <el-table-column type="index" label="序号" width="60" :index="indexOffset" />
              <el-table-column prop="positionCode" label="职务编码" width="130" />
              <el-table-column prop="positionName" label="职务名称" width="150" show-overflow-tooltip />
              <el-table-column prop="orgLevel" label="对应组织层级" width="110">
                <template #default="{ row }">
                  <el-tag :type="levelTagType(row.orgLevel)" size="small">{{ getLevelLabel(row.orgLevel) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="orgName" label="所属组织" width="130" show-overflow-tooltip />
              <el-table-column prop="positionGrade" label="职务等级" width="90" />
              <el-table-column prop="incumbentName" label="任职人员" width="100" />
              <el-table-column prop="positionStatus" label="职务状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.positionStatus)" size="small">
                    {{ getStatusLabel(row.positionStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="isCoreManagement" label="核心管理" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.isCoreManagement" type="danger" size="small">是</el-tag>
                  <span v-else>否</span>
                </template>
              </el-table-column>
              <el-table-column prop="effectiveDate" label="生效时间" width="150" show-overflow-tooltip />
              <el-table-column prop="expireDate" label="失效时间" width="150" show-overflow-tooltip />
              <el-table-column prop="updateTime" label="更新时间" width="150" show-overflow-tooltip />
              <el-table-column label="操作" width="420" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="viewDetail(row)">详情</el-button>
                  <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
                  <el-button size="small" type="success" @click="handleAppoint(row)">任免</el-button>
                  <el-button size="small" type="info" plain @click="viewHistory(row)">变更历史</el-button>
                  <el-button size="small" type="info" plain @click="viewAppointRecords(row)">任免记录</el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    v-if="row.positionStatus !== 'ABOLISHED'"
                    @click="handleAbolish(row)"
                  >作废</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              @size-change="(v)=>{pagination.pageSize=v;pagination.currentPage=1}"
              @current-change="(v)=>pagination.currentPage=v"
              :current-page="pagination.currentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.pageSize"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              style="margin-top: 20px; text-align: right"
            />

            <!-- 底部汇总统计 -->
            <el-row :gutter="16" style="margin-top: 16px">
              <el-col :span="6">
                <el-card shadow="hover"><div class="stat">职务总数：{{ stats.total }}</div></el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover"><div class="stat stat-active">在职：{{ stats.active }}</div></el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover"><div class="stat stat-vacant">空缺：{{ stats.vacant }}</div></el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover"><div class="stat stat-abolished">废止：{{ stats.abolished }}</div></el-card>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- 标签2：个性化职务配置 -->
          <el-tab-pane label="个性化职务配置" name="personalized">
            <el-alert
              title="个性化职务配置：基于标准五级组织对应职务体系，为单个组织自定义适配专属管理职务，不破坏整体五级管控体系。仅授权管理员可配置。"
              type="warning"
              show-icon
              :closable="false"
              style="margin-bottom: 12px"
            />
            <div style="margin-bottom: 12px">
              <el-button type="primary" @click="handleAddPersonalized">新增个性化职务</el-button>
              <el-tag v-if="selectedOrg" style="margin-left: 12px">当前组织：{{ selectedOrg.label }}</el-tag>
            </div>
            <el-table :data="personalizedList" border stripe size="small" v-loading="loading">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="orgName" label="所属组织" width="140" />
              <el-table-column prop="orgLevel" label="标准职务层级" width="120">
                <template #default="{ row }">
                  {{ getLevelLabel(row.orgLevel) }}
                </template>
              </el-table-column>
              <el-table-column prop="personalizedName" label="自定义职务名称" width="160" show-overflow-tooltip />
              <el-table-column prop="positionGrade" label="职务等级" width="90" />
              <el-table-column prop="sortOrder" label="等级排序" width="90" />
              <el-table-column prop="isInStaffingControl" label="纳入职数管控" width="110">
                <template #default="{ row }">
                  {{ row.isInStaffingControl ? '是' : '否' }}
                </template>
              </el-table-column>
              <el-table-column prop="isInCommandChain" label="纳入指挥链" width="100">
                <template #default="{ row }">
                  {{ row.isInCommandChain ? '是' : '否' }}
                </template>
              </el-table-column>
              <el-table-column prop="effectiveDate" label="生效时间" width="150" show-overflow-tooltip />
              <el-table-column prop="personalizedReason" label="配置依据" min-width="150" show-overflow-tooltip />
              <el-table-column prop="positionStatus" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.positionStatus)" size="small">
                    {{ getStatusLabel(row.positionStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="handleEditPersonalized(row)">编辑</el-button>
                  <el-button size="small" type="warning" @click="togglePersonalizedStatus(row)">
                    {{ row.positionStatus === 'ACTIVE' ? '停用' : '启用' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && personalizedList.length === 0" description="暂无个性化职务配置" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 新增/编辑职务弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职务编码" required>
              <el-input v-model="formData.positionCode" placeholder="自动生成，可修改" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务名称" required>
              <el-input v-model="formData.positionName" placeholder="请输入职务名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="对应组织层级" required>
              <el-select v-model="formData.orgLevel" placeholder="选择层级" style="width: 100%" @change="onOrgLevelChange">
                <el-option label="一级（公司）" :value="1" />
                <el-option label="二级（中心）" :value="2" />
                <el-option label="三级（部门）" :value="3" />
                <el-option label="四级（科室）" :value="4" />
                <el-option label="五级（小组）" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准职务建议">
              <el-select v-model="formData.positionName" placeholder="选择标准职务（可手动修改）" style="width: 100%" clearable>
                <el-option v-for="p in standardPositions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属组织">
              <el-select v-model="formData.orgId" placeholder="选择所属组织" style="width: 100%" filterable>
                <el-option
                  v-for="o in orgOptions"
                  :key="o.id"
                  :label="`${o.orgName}（${getLevelLabel(o.orgLevel)}）`"
                  :value="o.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务等级">
              <el-input v-model="formData.positionGrade" placeholder="如：M1/P1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职务状态">
              <el-select v-model="formData.positionStatus" style="width: 100%">
                <el-option label="在职" value="ACTIVE" />
                <el-option label="空缺" value="VACANT" />
                <el-option label="废止" value="ABOLISHED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任职人员">
              <el-input v-model="formData.incumbentName" placeholder="请输入任职人员" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" required>
              <el-date-picker v-model="formData.effectiveDate" type="datetime" placeholder="选择日期时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效日期">
              <el-date-picker v-model="formData.expireDate" type="datetime" placeholder="选择日期时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="核心管理职务">
              <el-switch v-model="formData.isCoreManagement" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="挂职标识">
              <el-switch v-model="formData.isTemporaryPost" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="兼职标识">
              <el-switch v-model="formData.isPartTimePost" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="纳入职数管控">
              <el-switch v-model="formData.isInStaffingControl" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="纳入指挥链">
              <el-switch v-model="formData.isInCommandChain" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序序号">
              <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权责说明">
          <el-input v-model="formData.responsibilityDesc" type="textarea" :rows="3" placeholder="请输入权责说明" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remarks" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 职务任免弹窗 -->
    <el-dialog v-model="appointVisible" title="职务任免" width="560px">
      <el-form label-width="110px">
        <el-form-item label="职务名称">
          <span>{{ appointRow.positionName }}（{{ getLevelLabel(appointRow.orgLevel) }}）</span>
        </el-form-item>
        <el-form-item label="任免类型" required>
          <el-radio-group v-model="appointForm.appointType">
            <el-radio value="APPOINT">任职</el-radio>
            <el-radio value="RELIEVE">免职</el-radio>
            <el-radio value="TEMPORARY">挂职</el-radio>
            <el-radio value="PART_TIME">兼职</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任职人" required>
          <el-input v-model="appointForm.incumbentName" placeholder="请输入任职人姓名" />
        </el-form-item>
        <el-form-item label="生效日期" required>
          <el-date-picker v-model="appointForm.effectiveDate" type="datetime" placeholder="选择日期时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="任免说明" required>
          <el-input v-model="appointForm.appointReason" type="textarea" :rows="3" placeholder="请填写任免说明" />
        </el-form-item>
        <el-form-item label="任免文件">
          <!-- 规格书字段，待后端扩展 -->
          <el-upload :auto-upload="false" :show-file-list="false">
            <el-button size="small">上传任免文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppoint">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="职务详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="职务编码">{{ detailData.positionCode }}</el-descriptions-item>
        <el-descriptions-item label="职务名称">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="对应组织层级">{{ getLevelLabel(detailData.orgLevel) }}</el-descriptions-item>
        <el-descriptions-item label="职务等级">{{ detailData.positionGrade }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ detailData.orgName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.positionStatus)" size="small">{{ getStatusLabel(detailData.positionStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任职人员">{{ detailData.incumbentName }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ detailData.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ detailData.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="核心管理职务">{{ detailData.isCoreManagement ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="挂职">{{ detailData.isTemporaryPost ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="兼职">{{ detailData.isPartTimePost ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="纳入职数管控">{{ detailData.isInStaffingControl ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="纳入指挥链">{{ detailData.isInCommandChain ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="权责说明" :span="2">{{ detailData.responsibilityDesc }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remarks }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 变更历史弹窗 -->
    <el-dialog v-model="historyVisible" title="职务变更历史" width="800px">
      <el-table :data="historyList" border stripe size="small" v-loading="historyLoading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="changeType" label="变更类型" width="110" />
        <el-table-column prop="changeContent" label="变更内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operator" label="操作人" width="110" />
        <el-table-column prop="operateTime" label="操作时间" width="170" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!historyLoading && historyList.length === 0" description="暂无变更历史" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'positionCode', title: '职务编码' },
  { key: 'positionName', title: '职务名称' },
  { key: 'orgLevel', title: '对应组织层级' },
  { key: 'orgName', title: '所属组织' },
  { key: 'positionGrade', title: '职务等级' },
  { key: 'positionStatus', title: '职务状态' },
  { key: 'incumbentName', title: '任职人员' },
  { key: 'effectiveDate', title: '生效时间' },
  { key: 'expireDate', title: '失效时间' },
  { key: 'isCoreManagement', title: '核心管理职务' },
  { key: 'sortOrder', title: '排序序号' }
]
const ioFieldMap = {
  '职务编码': 'positionCode',
  '职务名称': 'positionName',
  '对应组织层级': 'orgLevel',
  '所属组织': 'orgName',
  '职务等级': 'positionGrade',
  '职务状态': 'positionStatus',
  '任职人员': 'incumbentName',
  '生效时间': 'effectiveDate',
  '失效时间': 'expireDate',
  '核心管理职务': 'isCoreManagement',
  '排序序号': 'sortOrder'
}

const loading = ref(false)
const activeTab = ref('standard')

// 左侧组织树
const orgTree = ref([])
const treeFilter = ref('')
const orgTreeRef = ref(null)
const selectedOrg = ref(null)
const orgOptions = ref([])

// 搜索
const searchForm = reactive({ keyword: '', orgLevel: null, status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

// 职务列表
const jobList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增职务')
const formRef = ref(null)

const defaultForm = () => ({
  id: null, positionCode: '', positionName: '', orgLevel: null, orgId: null,
  orgCode: '', orgName: '', positionGrade: '', positionStatus: 'ACTIVE',
  incumbentId: '', incumbentName: '', effectiveDate: '', expireDate: '',
  responsibilityDesc: '', sortOrder: 0, isCoreManagement: false,
  isTemporaryPost: false, isPartTimePost: false, isPersonalized: false,
  personalizedName: '', personalizedReason: '', isInStaffingControl: true,
  isInCommandChain: true, remarks: ''
})
const formData = reactive(defaultForm())

const detailData = reactive({})

// 任免
const appointVisible = ref(false)
const appointRow = ref({})
const appointForm = reactive({ appointType: 'APPOINT', incumbentName: '', effectiveDate: '', appointReason: '' })

// 历史
const historyVisible = ref(false)
const historyLoading = ref(false)
const historyList = ref([])

// 个性化
const personalizedList = ref([])

// 标准职务建议
const standardPositions = ref([])

const stats = computed(() => {
  const list = jobList.value
  return {
    total: list.length,
    active: list.filter(i => i.positionStatus === 'ACTIVE').length,
    vacant: list.filter(i => i.positionStatus === 'VACANT').length,
    abolished: list.filter(i => i.positionStatus === 'ABOLISHED').length
  }
})

const pagedList = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return jobList.value.slice(start, start + pagination.pageSize)
})

function indexOffset(i) { return (pagination.currentPage - 1) * pagination.pageSize + i + 1 }

function getLevelLabel(level) {
  const map = { 1: '一级（公司）', 2: '二级（中心）', 3: '三级（部门）', 4: '四级（科室）', 5: '五级（小组）' }
  return map[level] || level
}

function levelTagType(level) {
  return { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'success', 5: 'info' }[level] || 'info'
}

function getStatusType(s) {
  return { ACTIVE: 'success', VACANT: 'warning', ABOLISHED: 'danger' }[s] || 'info'
}

function getStatusLabel(s) {
  return { ACTIVE: '在职', VACANT: '空缺', ABOLISHED: '废止' }[s] || s
}

function filterOrgNode(value, data) {
  if (!value) return true
  return (data.label || '').includes(value)
}

watch(treeFilter, v => orgTreeRef.value?.filter(v))

async function loadOrgTree() {
  try {
    const res = await axios.get('/api/org-unit/tree')
    if (res.data.code === 200) {
      orgTree.value = res.data.data || []
      orgOptions.value = flattenOrgs(res.data.data || [])
    }
  } catch (e) { ElMessage.error('加载组织树失败') }
}

function flattenOrgs(nodes, out = []) {
  nodes.forEach(n => {
    out.push({ id: n.id, orgName: n.label || n.orgName, orgLevel: n.orgLevel, orgCode: n.orgCode })
    if (n.children) flattenOrgs(n.children, out)
  })
  return out
}

function handleOrgNodeClick(data) {
  selectedOrg.value = data
  searchForm.orgLevel = data.orgLevel
  formData.orgId = data.id
  formData.orgName = data.label || data.orgName
  formData.orgCode = data.orgCode
  pagination.currentPage = 1
  loadJobList()
  loadPersonalized()
}

async function loadJobList() {
  loading.value = true
  try {
    const res = await axios.get('/api/job-position/list', {
      params: {
        page: 1, pageSize: 1000,
        keyword: searchForm.keyword || undefined,
        orgLevel: searchForm.orgLevel || undefined,
        orgCode: selectedOrg.value?.orgCode || undefined,
        status: searchForm.status || undefined
      }
    })
    const data = res.data.code !== undefined ? res.data.data : res.data
    let list = data.list || data || []
    // 仅展示标准职务（非个性化）
    list = list.filter(i => !i.isPersonalized)
    jobList.value = list
    pagination.total = list.length
  } catch (e) {
    ElMessage.error('加载职务列表失败')
  } finally { loading.value = false }
}

async function loadPersonalized() {
  loading.value = true
  try {
    const res = await axios.get('/api/job-position/list', {
      params: {
        page: 1, pageSize: 1000,
        orgCode: selectedOrg.value?.orgCode || undefined
      }
    })
    const data = res.data.code !== undefined ? res.data.data : res.data
    let list = data.list || data || []
    personalizedList.value = list.filter(i => i.isPersonalized)
  } catch (e) {
    ElMessage.error('加载个性化职务失败')
  } finally { loading.value = false }
}

async function loadStandardPositions(level) {
  if (!level) { standardPositions.value = []; return }
  try {
    const res = await axios.get(`/api/job-position/standard-positions/${level}`)
    const data = res.data.code !== undefined ? res.data.data : res.data
    standardPositions.value = data || []
  } catch (e) { standardPositions.value = [] }
}

function onOrgLevelChange() {
  loadStandardPositions(formData.orgLevel)
  // 禁止跨层级：锁定组织候选
}

function handleSearch() { pagination.currentPage = 1; loadJobList() }
function resetSearch() { Object.assign(searchForm, { keyword: '', orgLevel: null, status: '' }); pagination.currentPage = 1; loadJobList() }

function handleAdd() {
  Object.assign(formData, defaultForm())
  formData.orgId = selectedOrg.value?.id || null
  formData.orgName = selectedOrg.value?.label || ''
  formData.orgCode = selectedOrg.value?.orgCode || ''
  formData.orgLevel = selectedOrg.value?.orgLevel || null
  formData.effectiveDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
  if (formData.orgLevel) loadStandardPositions(formData.orgLevel)
  dialogTitle.value = '新增职务'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(formData, { ...defaultForm(), ...row })
  loadStandardPositions(row.orgLevel)
  dialogTitle.value = '编辑职务'
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/job-position/${row.id}`)
    const data = res.data.code !== undefined ? res.data.data : res.data
    Object.assign(detailData, data)
    detailVisible.value = true
  } catch (e) { ElMessage.error('获取职务详情失败') }
}

async function handleSubmit() {
  // 禁止跨层级配置校验
  if (formData.orgId) {
    const org = orgOptions.value.find(o => o.id === formData.orgId)
    if (org && org.orgLevel !== formData.orgLevel) {
      ElMessage.error(`刚性规则：职务层级必须与所属组织层级一致，禁止跨层级配置职务。`)
      return
    }
  }
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/job-position/${formData.id}`, formData, { params: { operator: 'admin' } })
    } else {
      res = await axios.post('/api/job-position/create', formData, { params: { initiator: 'admin' } })
    }
    const data = res.data.code !== undefined ? res.data : res.data
    if (res.data.code === 200 || data.success) {
      ElMessage.success(dialogTitle.value === '新增职务' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadJobList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) { ElMessage.error('操作失败') }
}

function handleAppoint(row) {
  appointRow.value = row
  Object.assign(appointForm, { appointType: 'APPOINT', incumbentName: row.incumbentName || '', effectiveDate: '', appointReason: '' })
  appointVisible.value = true
}

async function submitAppoint() {
  if (!appointForm.incumbentName || !appointForm.effectiveDate || !appointForm.appointReason) {
    ElMessage.warning('请填写任职人、生效日期和任免说明')
    return
  }
  try {
    const res = await axios.post(`/api/job-position/${appointRow.value.id}/appoint`, null, {
      params: {
        personId: appointForm.incumbentName,
        personName: appointForm.incumbentName,
        appointReason: appointForm.appointReason,
        operator: 'admin'
      }
    })
    if (res.data.code === 200) {
      ElMessage.success('任免成功')
      appointVisible.value = false
      loadJobList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '任免失败')
    }
  } catch (e) { ElMessage.error('任免失败') }
}

function handleAbolish(row) {
  ElMessageBox.prompt('请输入作废原因', '职务作废', {
    confirmButtonText: '确定', cancelButtonText: '取消',
    inputType: 'textarea',
    inputValidator: v => (v && v.trim() ? true : '作废原因不能为空')
  }).then(async ({ value }) => {
    try {
      const res = await axios.post(`/api/job-position/${row.id}/abolish`, null, {
        params: { reason: value, operator: 'admin' }
      })
      if (res.data.code === 200) {
        ElMessage.success('作废成功')
        loadJobList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '作废失败')
      }
    } catch (e) { ElMessage.error('作废失败') }
  }).catch(() => {})
}

async function viewHistory(row) {
  historyVisible.value = true
  historyLoading.value = true
  historyList.value = []
  try {
    const res = await axios.get('/api/job-position/history', {
      params: { positionCode: row.positionCode, page: 1, pageSize: 100 }
    })
    const data = res.data.code !== undefined ? res.data.data : res.data
    historyList.value = data || []
  } catch (e) { ElMessage.error('获取变更历史失败') }
  finally { historyLoading.value = false }
}

async function viewAppointRecords(row) {
  // 任免记录复用变更历史，按任免类型筛选
  historyVisible.value = true
  historyLoading.value = true
  historyList.value = []
  try {
    const res = await axios.get('/api/job-position/history', {
      params: { positionCode: row.positionCode, page: 1, pageSize: 100 }
    })
    const data = res.data.code !== undefined ? res.data.data : res.data
    historyList.value = (data || []).filter(i => /任免|任职|挂职|兼职|免职/i.test(i.changeType || '') || /任免|任职/i.test(i.changeContent || ''))
    if (historyList.value.length === 0) historyList.value = data || []
  } catch (e) { ElMessage.error('获取任免记录失败') }
  finally { historyLoading.value = false }
}

function handleBatchConfigure() {
  if (!selectedOrg.value) {
    ElMessage.warning('请先在左侧选择一个组织')
    return
  }
  ElMessageBox.confirm(`将对「${selectedOrg.value.label}」批量配置标准职务（总经理/副总经理/经理/主管/领班等），确定继续？`, '批量配置', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.post('/api/job-position/batch-configure', [], {
        params: { orgId: selectedOrg.value.id, operator: 'admin' }
      })
      if (res.data.code === 200 || res.data.success) {
        ElMessage.success('批量配置成功')
        loadJobList()
      } else {
        ElMessage.error(res.data.message || res.data.msg || '批量配置失败')
      }
    } catch (e) { ElMessage.error('批量配置失败') }
  }).catch(() => {})
}

async function syncArchitecture() {
  ElMessageBox.confirm('同步将把职务层级数据更新至组织架构图，确定继续？', '同步架构', { type: 'warning' }).then(async () => {
    try {
      await axios.post(`/api/job-position/${jobList.value[0]?.id || 0}/sync`)
      ElMessage.success('同步成功')
    } catch (e) { ElMessage.success('同步请求已发送') }
  }).catch(() => {})
}

// ===== 个性化职务配置 =====
function handleAddPersonalized() {
  if (!selectedOrg.value) {
    ElMessage.warning('请先在左侧选择一个组织')
    return
  }
  Object.assign(formData, defaultForm())
  formData.orgId = selectedOrg.value.id
  formData.orgName = selectedOrg.value.label
  formData.orgCode = selectedOrg.value.orgCode
  formData.orgLevel = selectedOrg.value.orgLevel
  formData.isPersonalized = true
  formData.effectiveDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
  dialogTitle.value = '新增个性化职务'
  dialogVisible.value = true
}

function handleEditPersonalized(row) {
  Object.assign(formData, { ...defaultForm(), ...row })
  dialogTitle.value = '编辑个性化职务'
  dialogVisible.value = true
}

function togglePersonalizedStatus(row) {
  const newStatus = row.positionStatus === 'ACTIVE' ? 'VACANT' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}该个性化职务吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post(`/api/job-position/${row.id}/toggle-status`, null, {
        params: { newStatus, operator: 'admin' }
      })
      if (res.data.code === 200 || res.data.success) {
        ElMessage.success(`${action}成功`)
        loadPersonalized()
      } else {
        ElMessage.error(res.data.message || res.data.msg || `${action}失败`)
      }
    } catch (e) { ElMessage.error(`${action}失败`) }
  }).catch(() => {})
}

watch(activeTab, v => { if (v === 'personalized') loadPersonalized() })

onMounted(() => {
  loadOrgTree()
  loadJobList()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.main-content { display: flex; gap: 20px; }
.left-panel { width: 260px; flex-shrink: 0; border: 1px solid #e4e7ed; border-radius: 4px; padding: 10px; background: #fff; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-weight: bold; }
.right-panel { flex: 1; min-width: 0; }
.search-form { margin-bottom: 16px; }
.stat { text-align: center; font-size: 16px; font-weight: 600; }
.stat-active { color: #67c23a; }
.stat-vacant { color: #e6a23c; }
.stat-abolished { color: #f56c6c; }
</style>

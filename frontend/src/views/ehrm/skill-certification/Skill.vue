<template>
  <div class="page-container">
    <div class="page-header">
      <h2>技能管理</h2>
      <el-button type="primary" @click="handleAdd">新增技能</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="技能名称">
        <el-input v-model="searchForm.name" placeholder="请输入技能名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="技能类别">
        <el-select v-model="searchForm.category" placeholder="选择类别" style="width:150px">
          <el-option label="操作技能" value="operation" />
          <el-option label="专业技能" value="professional" />
          <el-option label="管理技能" value="management" />
          <el-option label="通用技能" value="general" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="searchForm.level" placeholder="选择等级" style="width:120px">
          <el-option label="初级" value="level1" />
          <el-option label="中级" value="level2" />
          <el-option label="高级" value="level3" />
          <el-option label="专家级" value="level4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="skillList" border stripe size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="skillNo" label="技能编号" width="140" />
      <el-table-column prop="name" label="技能名称" min-width="180">
        <template #default="{row}">
          <el-link type="primary" @click="viewDetail(row)">{{ row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="技能类别" width="120">
        <template #default="{row}">
          <el-tag size="small">{{ getCategoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="100">
        <template #default="{row}">
          <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="requirement" label="要求" min-width="150" />
      <el-table-column prop="certification" label="认证方式" width="120">
        <template #default="{row}">{{ row.certification }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">{{ row.status === 'active' ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'active' ? 'danger' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 20px; text-align: right;"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="技能编号" required>
          <el-input v-model="formData.skillNo" placeholder="请输入技能编号" />
        </el-form-item>
        <el-form-item label="技能名称" required>
          <el-input v-model="formData.name" placeholder="请输入技能名称" />
        </el-form-item>
        <el-form-item label="技能类别" required>
          <el-select v-model="formData.category" placeholder="选择技能类别">
            <el-option label="操作技能" value="operation" />
            <el-option label="专业技能" value="professional" />
            <el-option label="管理技能" value="management" />
            <el-option label="通用技能" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" required>
          <el-select v-model="formData.level">
            <el-option label="初级" value="level1" />
            <el-option label="中级" value="level2" />
            <el-option label="高级" value="level3" />
            <el-option label="专家级" value="level4" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入技能描述" />
        </el-form-item>
        <el-form-item label="技能要求">
          <el-input v-model="formData.requirement" type="textarea" :rows="3" placeholder="请输入技能要求" />
        </el-form-item>
        <el-form-item label="认证方式">
          <el-input v-model="formData.certification" placeholder="请输入认证方式" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="技能详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="技能编号">{{ detailData.skillNo }}</el-descriptions-item>
        <el-descriptions-item label="技能名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="技能类别">{{ getCategoryLabel(detailData.category) }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="getLevelType(detailData.level)" size="small">{{ getLevelLabel(detailData.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="技能描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="2">{{ detailData.requirement }}</el-descriptions-item>
        <el-descriptions-item label="认证方式">{{ detailData.certification }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'active' ? 'success' : 'danger'" size="small">{{ detailData.status === 'active' ? '启用' : '禁用' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const searchForm = reactive({
  name: '',
  category: '',
  level: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 30
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增技能')
const formRef = ref(null)

const formData = reactive({
  id: null,
  skillNo: '',
  name: '',
  category: 'operation',
  level: 'level1',
  description: '',
  requirement: '',
  certification: '',
  status: 'active'
})

const detailData = reactive({})

const skillList = ref([
  { id: 1, skillNo: 'SKILL-001', name: 'CNC操作技能', category: 'operation', level: 'level2', description: '掌握CNC机床操作和编程', requirement: '具有2年以上CNC操作经验', certification: '实操考核', status: 'active' },
  { id: 2, skillNo: 'SKILL-002', name: '质量管理体系', category: 'professional', level: 'level3', description: '熟悉ISO9001质量管理体系', requirement: '持有内审员证书', certification: '笔试+面试', status: 'active' },
  { id: 3, skillNo: 'SKILL-003', name: '项目管理', category: 'management', level: 'level3', description: '掌握项目管理知识体系', requirement: 'PMP或同等认证', certification: '笔试+答辩', status: 'active' },
  { id: 4, skillNo: 'SKILL-004', name: '焊接技能', category: 'operation', level: 'level2', description: '掌握各类焊接技术', requirement: '焊工证', certification: '实操考核', status: 'active' },
  { id: 5, skillNo: 'SKILL-005', name: '英语沟通', category: 'general', level: 'level1', description: '基本英语沟通能力', requirement: 'CET-4及以上', certification: '笔试', status: 'active' },
  { id: 6, skillNo: 'SKILL-006', name: 'PLC编程', category: 'professional', level: 'level4', description: '精通PLC编程和自动化控制', requirement: '5年以上经验', certification: '实操+答辩', status: 'active' },
  { id: 7, skillNo: 'SKILL-007', name: '设备维修', category: 'operation', level: 'level3', description: '设备故障诊断和维修', requirement: '机电专业背景', certification: '实操考核', status: 'active' },
  { id: 8, skillNo: 'SKILL-008', name: '数据分析', category: 'professional', level: 'level2', description: '数据分析和报表制作', requirement: 'Excel高级应用', certification: '笔试', status: 'disabled' }
])

function getCategoryLabel(category) {
  const map = { operation: '操作技能', professional: '专业技能', management: '管理技能', general: '通用技能' }
  return map[category] || category
}

function getLevelLabel(level) {
  const map = { level1: '初级', level2: '中级', level3: '高级', level4: '专家级' }
  return map[level] || level
}

function getLevelType(level) {
  const map = { level1: 'info', level2: 'primary', level3: 'warning', level4: 'danger' }
  return map[level] || 'info'
}

function handleSearch() {
  pagination.currentPage = 1
}

function resetSearch() {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.level = ''
  pagination.currentPage = 1
}

function handleSizeChange(val) {
  pagination.pageSize = val
}

function handleCurrentChange(val) {
  pagination.currentPage = val
}

function handleAdd() {
  dialogTitle.value = '新增技能'
  Object.assign(formData, {
    id: null,
    skillNo: '',
    name: '',
    category: 'operation',
    level: 'level1',
    description: '',
    requirement: '',
    certification: '',
    status: 'active'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑技能'
  Object.assign(formData, row)
  dialogVisible.value = true
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

function handleToggleStatus(row) {
  row.status = row.status === 'active' ? 'disabled' : 'active'
  el.message.success(row.status === 'active' ? '技能已启用' : '技能已禁用')
}

function handleSubmit() {
  dialogVisible.value = false
  el.message.success(dialogTitle.value === '新增技能' ? '新增成功' : '修改成功')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理幅度</h2>
      <div>
        <el-button type="info" @click="handleValidate" style="margin-right: 8px">幅度校验</el-button>
        <el-button type="primary" @click="handleInitDefault" :disabled="list.length > 0">初始化默认标准</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="岗位层级">
        <el-select v-model="searchForm.positionLevel" placeholder="选择层级" style="width: 150px">
          <el-option label="一级" value="L1" />
          <el-option label="二级" value="L2" />
          <el-option label="三级" value="L3" />
          <el-option label="四级" value="L4" />
          <el-option label="五级" value="L5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="filteredList" border stripe size="small" v-loading="loading">
      <el-table-column prop="positionLevel" label="岗位层级" width="100" />
      <el-table-column prop="positionName" label="岗位名称" width="160" />
      <el-table-column prop="minSpan" label="最小幅度" width="100" />
      <el-table-column prop="standardSpan" label="标准幅度" width="100" />
      <el-table-column prop="maxSpan" label="最大幅度" width="100" />
      <el-table-column prop="minOrgCount" label="最小组织数" width="110" />
      <el-table-column prop="standardOrgCount" label="标准组织数" width="110" />
      <el-table-column prop="maxOrgCount" label="最大组织数" width="110" />
      <el-table-column prop="minPersonCount" label="最小人数" width="100" />
      <el-table-column prop="standardPersonCount" label="标准人数" width="100" />
      <el-table-column prop="maxPersonCount" label="最大人数" width="100" />
      <el-table-column prop="isStrict" label="严格模式" width="100">
        <template #default="{ row }">
          {{ row.isStrict ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="alertThreshold" label="告警阈值" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位层级" required>
              <el-select v-model="formData.positionLevel" style="width: 100%" :disabled="!!formData.id">
                <el-option label="一级" value="L1" />
                <el-option label="二级" value="L2" />
                <el-option label="三级" value="L3" />
                <el-option label="四级" value="L4" />
                <el-option label="五级" value="L5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称">
              <el-input v-model="formData.positionName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>直接下属数</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小">
              <el-input-number v-model="formData.minSpan" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标准">
              <el-input-number v-model="formData.standardSpan" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大">
              <el-input-number v-model="formData.maxSpan" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>下属组织数</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小">
              <el-input-number v-model="formData.minOrgCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标准">
              <el-input-number v-model="formData.standardOrgCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大">
              <el-input-number v-model="formData.maxOrgCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>下属人数</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最小">
              <el-input-number v-model="formData.minPersonCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标准">
              <el-input-number v-model="formData.standardPersonCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大">
              <el-input-number v-model="formData.maxPersonCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="严格模式">
              <el-switch v-model="formData.isStrict" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="告警阈值">
              <el-input-number v-model="formData.alertThreshold" :min="0" :max="1" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remarks" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="管理幅度标准详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位层级">{{ detailData.positionLevel }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.positionName }}</el-descriptions-item>
        <el-descriptions-item label="最小幅度">{{ detailData.minSpan }}</el-descriptions-item>
        <el-descriptions-item label="标准幅度">{{ detailData.standardSpan }}</el-descriptions-item>
        <el-descriptions-item label="最大幅度">{{ detailData.maxSpan }}</el-descriptions-item>
        <el-descriptions-item label="严格模式">{{ detailData.isStrict ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="告警阈值">{{ detailData.alertThreshold }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remarks }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'

const loading = ref(false)

const searchForm = reactive({
  positionLevel: ''
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('编辑管理幅度标准')
const formRef = ref(null)

const formData = reactive({
  id: null,
  positionLevel: '',
  positionName: '',
  minSpan: 0,
  standardSpan: 0,
  maxSpan: 0,
  minOrgCount: 0,
  standardOrgCount: 0,
  maxOrgCount: 0,
  minPersonCount: 0,
  standardPersonCount: 0,
  maxPersonCount: 0,
  isStrict: false,
  alertThreshold: 0.8,
  remarks: ''
})

const detailData = reactive({})
const list = ref([])

const filteredList = computed(() => {
  if (!searchForm.positionLevel) return list.value
  return list.value.filter((item) => item.positionLevel === searchForm.positionLevel)
})

async function handleValidate() {
  try {
    const res = await axios.post('/api/management-span/validate')
    if (res.data.code === 200) {
      const data = res.data.data
      if (typeof data === 'string') {
        ElMessage.success(data)
      } else if (Array.isArray(data) && data.length > 0) {
        ElMessage.warning(`校验发现 ${data.length} 项异常，请前往管理幅度异常页处理`)
      } else {
        ElMessage.success('幅度校验通过')
      }
    } else {
      ElMessage.error(res.data.message || res.data.msg || '校验失败')
    }
  } catch (e) {
    ElMessage.error('幅度校验失败')
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await axios.get('/api/management-span/all')
    if (res.data.code === 200) {
      list.value = res.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadList()
}

function resetSearch() {
  searchForm.positionLevel = ''
  loadList()
}

function handleEdit(row) {
  dialogTitle.value = '编辑管理幅度标准'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

function viewDetail(row) {
  Object.assign(detailData, row)
  detailVisible.value = true
}

async function handleInitDefault() {
  try {
    const res = await axios.post('/api/management-span/initialize')
    if (res.data.code === 200) {
      ElMessage.success('初始化成功')
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '初始化失败')
    }
  } catch (e) {
    ElMessage.error('初始化失败')
  }
}

async function handleSubmit() {
  try {
    const res = await axios.put(`/api/management-span/${formData.id}`, formData)
    if (res.data.code === 200) {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.message || res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
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
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

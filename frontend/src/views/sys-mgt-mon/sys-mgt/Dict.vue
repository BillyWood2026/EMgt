<template>
  <div class="page-container">
    <div class="page-header">
      <h2>字典管理</h2>
      <el-button type="primary" @click="handleAdd">新增字典</el-button>
    </div>

    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="字典名称">
        <el-input v-model="searchForm.dictName" placeholder="请输入字典名称" style="width:180px" />
      </el-form-item>
      <el-form-item label="字典编码">
        <el-input v-model="searchForm.dictCode" placeholder="请输入字典编码" style="width:180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="dictList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="dictCode" label="字典编码" width="180" />
      <el-table-column prop="dictName" label="字典名称" width="180" />
      <el-table-column prop="description" label="字典描述" min-width="200" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{row}">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="handleItems(row)">字典项</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="字典编码" required>
          <el-input v-model="formData.dictCode" placeholder="请输入字典编码" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="字典名称" required>
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典描述">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入字典描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="字典详情" width="600px">
      <el-form :model="detailData" label-width="100px" :disabled="true">
        <el-form-item label="字典编码">
          <el-input v-model="detailData.dictCode" disabled />
        </el-form-item>
        <el-form-item label="字典名称">
          <el-input v-model="detailData.dictName" disabled />
        </el-form-item>
        <el-form-item label="字典描述">
          <el-input v-model="detailData.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="detailData.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ detailData.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="itemsVisible" title="字典项管理" width="800px">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" size="small" @click="handleAddItem">新增字典项</el-button>
      </div>
      <el-table :data="dictItems" border stripe size="small" v-loading="itemLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="itemLabel" label="字典标签" width="150" />
        <el-table-column prop="itemValue" label="字典值" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="handleEditItem(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="itemDialogTitle" width="500px">
      <el-form ref="itemFormRef" :model="itemFormData" label-width="100px">
        <el-form-item label="字典标签" required>
          <el-input v-model="itemFormData.itemLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" required>
          <el-input v-model="itemFormData.itemValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemFormData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="itemFormData.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemFormData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleItemSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const itemLoading = ref(false)
const searchForm = reactive({
  dictName: '',
  dictCode: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const itemsVisible = ref(false)
const itemDialogVisible = ref(false)
const dialogTitle = ref('新增字典')
const itemDialogTitle = ref('新增字典项')
const formRef = ref(null)
const itemFormRef = ref(null)

const formData = reactive({
  id: null,
  dictCode: '',
  dictName: '',
  description: '',
  status: 'ACTIVE'
})

const detailData = reactive({})

const itemFormData = reactive({
  id: null,
  dictId: null,
  itemLabel: '',
  itemValue: '',
  sortOrder: 0,
  status: 'ACTIVE',
  remark: ''
})

const currentDictId = ref(null)
const dictItems = ref([])
const dictList = ref([])

async function loadDictList() {
  loading.value = true
  try {
    const res = await axios.get('/api/sys/dict/list', {
      params: {
        pageNum: pagination.currentPage,
        pageSize: pagination.pageSize,
        dictName: searchForm.dictName || undefined,
        dictCode: searchForm.dictCode || undefined
      }
    })
    if (res.data.code === 200) {
      dictList.value = res.data.data.list
      pagination.total = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('加载字典列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadDictList()
}

function resetSearch() {
  searchForm.dictName = ''
  searchForm.dictCode = ''
  pagination.currentPage = 1
  loadDictList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  loadDictList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadDictList()
}

function handleAdd() {
  dialogTitle.value = '新增字典'
  Object.assign(formData, {
    id: null,
    dictCode: '',
    dictName: '',
    description: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑字典'
  Object.assign(formData, {
    id: row.id,
    dictCode: row.dictCode,
    dictName: row.dictName,
    description: row.description,
    status: row.status
  })
  dialogVisible.value = true
}

async function viewDetail(row) {
  try {
    const res = await axios.get(`/api/sys/dict/${row.id}`)
    if (res.data.code === 200) {
      Object.assign(detailData, res.data.data)
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取字典详情失败')
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该字典吗？删除后字典项也会被删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/dict/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadDictList()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/sys/dict/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/sys/dict', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value === '新增字典' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadDictList()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

async function loadDictItems(dictId) {
  itemLoading.value = true
  try {
    const res = await axios.get(`/api/sys/dict/${dictId}/items`)
    if (res.data.code === 200) {
      dictItems.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('加载字典项失败')
    console.error(e)
  } finally {
    itemLoading.value = false
  }
}

function handleItems(row) {
  currentDictId.value = row.id
  dictItems.value = []
  itemsVisible.value = true
  loadDictItems(row.id)
}

function handleAddItem() {
  itemDialogTitle.value = '新增字典项'
  Object.assign(itemFormData, {
    id: null,
    dictId: currentDictId.value,
    itemLabel: '',
    itemValue: '',
    sortOrder: 0,
    status: 'ACTIVE',
    remark: ''
  })
  itemDialogVisible.value = true
}

function handleEditItem(row) {
  itemDialogTitle.value = '编辑字典项'
  Object.assign(itemFormData, {
    id: row.id,
    dictId: row.dictId,
    itemLabel: row.itemLabel,
    itemValue: row.itemValue,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark
  })
  itemDialogVisible.value = true
}

function handleDeleteItem(row) {
  ElMessageBox.confirm('确定要删除该字典项吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/dict/item/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadDictItems(currentDictId.value)
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

async function handleItemSubmit() {
  try {
    let res
    if (itemFormData.id) {
      res = await axios.put(`/api/sys/dict/item/${itemFormData.id}`, itemFormData)
    } else {
      res = await axios.post(`/api/sys/dict/${currentDictId.value}/item`, itemFormData)
    }
    if (res.data.code === 200) {
      ElMessage.success(itemDialogTitle.value === '新增字典项' ? '新增成功' : '修改成功')
      itemDialogVisible.value = false
      loadDictItems(currentDictId.value)
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadDictList()
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

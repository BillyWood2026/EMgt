<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ config.title }}</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>

    <el-form v-if="config.search && config.search.length" :inline="true" :model="searchForm" class="search-form">
      <el-form-item v-for="s in config.search" :key="s.prop" :label="s.label">
        <el-input
          v-if="s.type === 'input'"
          v-model="searchForm[paramKey(s)]"
          :placeholder="'请输入' + s.label"
          style="width:170px"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select
          v-else-if="s.type === 'select'"
          v-model="searchForm[paramKey(s)]"
          :placeholder="'请选择' + s.label"
          style="width:160px"
          clearable
        >
          <el-option v-for="o in s.options" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="dataList" border stripe size="small" v-loading="loading">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column
        v-for="col in config.columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :min-width="col.width ? undefined : 140"
        :width="col.width"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <el-tag v-if="col.options" :type="tagType(col, row[col.prop])" size="small" disable-transitions>
            {{ optionLabel(col.options, row[col.prop]) }}
          </el-tag>
          <span v-else>{{ formatValue(row[col.prop]) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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
      class="pagination-bar"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <el-row :gutter="12">
          <el-col v-for="f in config.fields" :key="f.prop" :span="f.type === 'textarea' ? 24 : 12">
            <el-form-item :label="f.label" :required="f.required">
              <el-input
                v-if="f.type === 'input'"
                v-model="formData[f.prop]"
                :placeholder="'请输入' + f.label"
              />
              <el-input
                v-else-if="f.type === 'textarea'"
                v-model="formData[f.prop]"
                type="textarea"
                :rows="2"
                :placeholder="'请输入' + f.label"
              />
              <el-select
                v-else-if="f.type === 'select'"
                v-model="formData[f.prop]"
                :placeholder="'请选择' + f.label"
                style="width:100%"
                clearable
              >
                <el-option v-for="o in f.options" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
              <el-date-picker
                v-else-if="f.type === 'date'"
                v-model="formData[f.prop]"
                type="date"
                value-format="YYYY-MM-DD"
                :placeholder="'请选择' + f.label"
                style="width:100%"
              />
              <el-date-picker
                v-else-if="f.type === 'datetime'"
                v-model="formData[f.prop]"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                :placeholder="'请选择' + f.label"
                style="width:100%"
              />
              <el-input-number
                v-else-if="f.type === 'number'"
                v-model="formData[f.prop]"
                :step="f.step ? Number(f.step) : 1"
                :precision="f.step ? 2 : 0"
                controls-position="right"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  config: { type: Object, required: true }
})

const loading = ref(false)
const dataList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const formRef = ref(null)
const formData = reactive({})

const searchFields = props.config.search || []
const searchForm = reactive({})
searchFields.forEach(s => { searchForm[paramKey(s)] = '' })

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

function paramKey(s) {
  return s.like ? s.prop + 'Like' : s.prop
}

function optionLabel(options, value) {
  const hit = options.find(o => o.value === value)
  return hit ? hit.label : (value ?? '')
}

const TAG_TYPES = ['success', 'warning', 'danger', 'info', 'primary']
function tagType(col, value) {
  const idx = col.options.findIndex(o => o.value === value)
  return TAG_TYPES[idx % TAG_TYPES.length] || 'info'
}

function formatValue(v) {
  if (v === null || v === undefined) return ''
  return String(v).replace('T', ' ')
}

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pagination.currentPage, pageSize: pagination.pageSize }
    searchFields.forEach(s => {
      const v = searchForm[paramKey(s)]
      if (v !== '' && v !== null && v !== undefined) params[paramKey(s)] = v
    })
    const res = await axios.get(`${props.config.apiBase}/list`, { params })
    if (res.data.code === 200) {
      dataList.value = res.data.data.records || []
      pagination.total = res.data.data.total || 0
    } else {
      ElMessage.error(res.data.message || '加载数据失败')
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadData()
}

function resetSearch() {
  searchFields.forEach(s => { searchForm[paramKey(s)] = '' })
  pagination.currentPage = 1
  loadData()
}

function handleSizeChange(v) {
  pagination.pageSize = v
  pagination.currentPage = 1
  loadData()
}

function handleCurrentChange(v) {
  pagination.currentPage = v
  loadData()
}

function resetForm() {
  Object.keys(formData).forEach(k => { delete formData[k] })
}

function handleAdd() {
  dialogTitle.value = `新增${props.config.title}`
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = `编辑${props.config.title}`
  resetForm()
  Object.assign(formData, row)
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除该条${props.config.title}记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`${props.config.apiBase}/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }).catch(() => {})
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`${props.config.apiBase}/${formData.id}`, formData)
    } else {
      res = await axios.post(props.config.apiBase, formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

watch(() => props.config, () => {
  pagination.currentPage = 1
  loadData()
})

onMounted(() => {
  loadData()
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

.search-form {
  margin-bottom: 12px;
}

.pagination-bar {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

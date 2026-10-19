<template>
  <div class="import-export-bar">
    <template v-if="importApi">
      <el-upload
        :show-file-list="false"
        :auto-upload="false"
        accept=".xlsx,.xls,.csv"
        :on-change="handleFileChange"
        style="display:inline-block"
      >
        <el-button size="small" type="warning" :loading="parsing">导入</el-button>
      </el-upload>
    </template>
    <el-dropdown size="small" @command="handleExport" style="margin-left:8px">
      <el-button size="small" type="success" :loading="exporting">
        导出<el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="excel">导出Excel</el-dropdown-item>
          <el-dropdown-item command="word">导出Word</el-dropdown-item>
          <el-dropdown-item command="pdf">导出PDF</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-dialog v-model="previewVisible" title="导入预览" width="80%" top="6vh">
      <div class="preview-meta">
        共解析出 <b>{{ importTotal }}</b> 条记录，下方预览前 {{ previewRows.length }} 条
      </div>
      <el-table :data="previewRows" border stripe size="small" max-height="480">
        <el-table-column
          v-for="col in importColumns"
          :key="col.key"
          :prop="col.key"
          :label="col.title"
          show-overflow-tooltip
        />
      </el-table>
      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  fileName: { type: String, default: '导出数据' },
  title: { type: String, default: '' },
  importApi: { type: String, default: '' },
  fieldMap: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['done'])

const exporting = ref(false)
const parsing = ref(false)
const importing = ref(false)
const previewVisible = ref(false)
const importColumns = ref([])
const importRows = ref([])
const previewRows = ref([])
const importTotal = ref(0)

function guessExt(fmt, blobType) {
  if (fmt === 'excel') return blobType && blobType.includes('csv') ? 'csv' : 'xlsx'
  if (fmt === 'word') return 'docx'
  if (fmt === 'pdf') return 'pdf'
  return 'bin'
}

async function showBlobError(blob, fallback) {
  try {
    const text = await blob.text()
    const json = JSON.parse(text)
    ElMessage.error(json.message || json.msg || fallback)
  } catch (e) {
    ElMessage.error(fallback)
  }
}

async function handleExport(fmt) {
  if (!props.rows || props.rows.length === 0) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }
  exporting.value = true
  try {
    const res = await request.post(`/api/file/io/export/${fmt}`, {
      fileName: props.fileName,
      sheetName: props.title || props.fileName,
      title: props.title || props.fileName,
      columns: props.columns,
      rows: props.rows
    }, { responseType: 'blob', timeout: 60000 })
    const blob = res.data
    if (blob.type && blob.type.includes('json')) {
      await showBlobError(blob, '导出失败')
      return
    }
    const url = URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = `${props.fileName}.${guessExt(fmt, blob.type)}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    const data = e.response && e.response.data
    if (data instanceof Blob && data.type && data.type.includes('json')) {
      await showBlobError(data, '导出失败')
    } else {
      ElMessage.error('导出失败：' + (e.message || ''))
    }
  } finally {
    exporting.value = false
  }
}

async function handleFileChange(uploadFile) {
  const file = uploadFile.raw
  if (!file) return
  parsing.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await request.post('/api/file/io/import/excel', fd, { timeout: 60000 })
    if (res.data && res.data.code === 200) {
      const d = res.data.data || {}
      importColumns.value = d.columns || []
      importRows.value = d.rows || []
      importTotal.value = d.total != null ? d.total : (d.rows || []).length
      previewRows.value = importRows.value.slice(0, 50)
      if (importRows.value.length === 0) {
        ElMessage.warning('未解析到任何数据行')
        return
      }
      previewVisible.value = true
    } else {
      ElMessage.error((res.data && (res.data.message || res.data.msg)) || '文件解析失败')
    }
  } catch (e) {
    ElMessage.error('文件解析失败：' + (e.message || ''))
    console.error(e)
  } finally {
    parsing.value = false
  }
}

function mapRow(row) {
  const out = {}
  const titles = importColumns.value.map(c => c.title)
  const keys = importColumns.value.map(c => c.key)
  titles.forEach((title, i) => {
    const target = props.fieldMap[title]
    const value = row[keys[i]] != null ? row[keys[i]] : row[title]
    if (target) {
      out[target] = value
    } else {
      out[title] = value
    }
  })
  return out
}

async function confirmImport() {
  if (!props.importApi) return
  importing.value = true
  try {
    const payloads = importRows.value.map(mapRow)
    const results = await Promise.allSettled(payloads.map(p => request.post(props.importApi, p)))
    const failed = []
    results.forEach((r, i) => {
      const rejected = r.status === 'rejected'
      const bizFail = r.status === 'fulfilled' && r.value && r.value.data && r.value.data.code && r.value.data.code !== 200
      if (rejected || bizFail) {
        failed.push({ index: i + 1, row: payloads[i], reason: rejected ? (r.reason && r.reason.message) : (r.value.data.message || r.value.data.msg) })
      }
    })
    const ok = results.length - failed.length
    if (failed.length > 0) {
      console.error('导入失败明细：', failed)
      ElMessage.warning(`导入完成：成功 ${ok} 条，失败 ${failed.length} 条（失败明细见控制台）`)
    } else {
      ElMessage.success(`导入完成：成功 ${ok} 条`)
    }
    previewVisible.value = false
    emit('done')
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.import-export-bar {
  display: inline-flex;
  align-items: center;
}
.preview-meta {
  margin-bottom: 12px;
  color: #606266;
  font-size: 13px;
}
</style>

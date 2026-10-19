<template>
  <div class="page-card">
    <div class="page-header">
      <h2>流程制度快速查询</h2>
    </div>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px"
      title="支持关键词搜索、多维度筛选，提供热门制度、常用收藏、最近浏览等快捷入口，支持在线预览和文件下载。" />

    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-input 
            v-model="searchKeyword" 
            placeholder="输入关键词搜索流程制度..." 
            clearable
            style="width:400px"
            @keyup.enter="doSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="doSearch">搜索</el-button>
        </div>
        
        <div class="search-filters">
          <el-form :inline="true" :model="searchFilters">
            <el-form-item label="分类">
              <el-select v-model="searchFilters.category" clearable style="width:140px">
                <el-option label="全部" value="" />
                <el-option label="人事管理" value="人事管理" />
                <el-option label="行政管理" value="行政管理" />
                <el-option label="财务管理" value="财务管理" />
                <el-option label="生产管理" value="生产管理" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchFilters.status" clearable style="width:120px">
                <el-option label="全部" value="" />
                <el-option label="生效中" value="生效中" />
                <el-option label="待审批" value="待审批" />
                <el-option label="已废止" value="已废止" />
              </el-select>
            </el-form-item>
            <el-form-item label="层级">
              <el-select v-model="searchFilters.level" clearable style="width:120px">
                <el-option label="全部" value="" />
                <el-option label="公司级" value="公司级" />
                <el-option label="部门级" value="部门级" />
              </el-select>
            </el-form-item>
            <el-form-item label="部门">
              <el-select v-model="searchFilters.dept" clearable style="width:140px">
                <el-option label="全部" value="" />
                <el-option label="研发中心" value="研发中心" />
                <el-option label="销售部" value="销售部" />
                <el-option label="人力资源部" value="人力资源部" />
                <el-option label="生产制造中心" value="生产制造中心" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker v-model="searchFilters.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" style="width:240px" />
            </el-form-item>
            <el-form-item>
              <el-button @click="resetFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <!-- 快捷入口 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <!-- 热门制度 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><b>🔥 热门制度</b></template>
          <div class="hot-list">
            <div 
              v-for="(item, idx) in hotDocuments" 
              :key="idx" 
              class="hot-item"
              @click="previewDocument(item)"
            >
              <span class="hot-rank">{{ idx + 1 }}</span>
              <span class="hot-title">{{ item.name }}</span>
              <el-tag size="mini" :type="item.status === '生效中' ? 'success' : 'warning'">{{ item.status }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 常用收藏 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <b>⭐ 我的收藏</b>
              <el-button size="small" @click="manageFavorites">管理</el-button>
            </div>
          </template>
          <div class="favorite-list">
            <div 
              v-for="(item, idx) in favorites" 
              :key="idx" 
              class="favorite-item"
              @click="previewDocument(item)"
            >
              <el-icon><Star /></el-icon>
              <span>{{ item.name }}</span>
            </div>
            <div v-if="favorites.length === 0" class="empty-favorites">暂无收藏</div>
          </div>
        </el-card>
      </el-col>

      <!-- 最近浏览 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><b>👀 最近浏览</b></template>
          <div class="history-list">
            <div 
              v-for="(item, idx) in recentViews" 
              :key="idx" 
              class="history-item"
              @click="previewDocument(item)"
            >
              <el-icon><Clock /></el-icon>
              <span>{{ item.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索结果 -->
    <el-card shadow="never">
      <template #header>
        <div class="result-header">
          <b>搜索结果（共 {{ filteredDocuments.length }} 条）</b>
          <div class="result-actions">
            <el-button size="small" @click="exportResults">导出列表</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filteredDocuments" border stripe size="small">
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{row}">
            <span class="doc-title" @click="previewDocument(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{row}"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="90">
          <template #default="{row}"><el-tag size="small" :type="row.level === '公司级' ? 'primary' : 'info'">{{ row.level }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="dept" label="部门" width="120" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag size="small" :type="getStatusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button link size="small" @click="previewDocument(row)">预览</el-button>
            <el-button link size="small" @click="downloadDocument(row)">下载</el-button>
            <el-button link size="small" @click="toggleFavorite(row)">
              <el-icon><Star :class="{ filled: isFavorite(row) }" /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredDocuments.length"
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50]"
          background
        />
      </div>
    </el-card>

    <!-- 文件预览弹窗 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="900px" :close-on-click-modal="false">
      <div class="preview-header">
        <div class="preview-title">{{ currentDocument?.title }}</div>
        <div class="preview-actions">
          <el-button size="small" @click="downloadDocument(currentDocument)">下载</el-button>
          <el-button size="small" @click="toggleFavorite(currentDocument)">
            <el-icon><Star :class="{ filled: isFavorite(currentDocument) }" /></el-icon>
            {{ isFavorite(currentDocument) ? '取消收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
      
      <div class="preview-info">
        <el-tag size="small">{{ currentDocument?.type }}</el-tag>
        <span>{{ currentDocument?.version }}</span>
        <span>|</span>
        <span>{{ currentDocument?.publishTime }}</span>
      </div>

      <div class="preview-content">
        <div v-html="currentDocument?.content"></div>
      </div>

      <template #footer>
        <el-button @click="previewVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import { Search, Star, Clock } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const searchFilters = reactive({
  category: '',
  status: '',
  level: '',
  dept: '',
  dateRange: []
})

const page = reactive({ current: 1, size: 10 })

const previewVisible = ref(false)
const currentDocument = ref(null)

const favorites = ref([
  { id: 1, title: '招聘管理制度', type: '人事管理', version: 'V3.0', publishTime: '2026-06-01' },
  { id: 2, title: '费用报销管理规定', type: '财务管理', version: 'V2.0', publishTime: '2026-04-20' },
  { id: 4, title: '安全生产操作规程', type: '生产管理', version: 'V3.2', publishTime: '2026-06-05' }
])

const hotDocuments = ref([
  { id: 1, name: '招聘管理制度', status: '生效中' },
  { id: 2, name: '员工考勤管理办法', status: '生效中' },
  { id: 3, name: '费用报销管理规定', status: '生效中' },
  { id: 4, name: '会议管理制度', status: '生效中' },
  { id: 5, name: '安全生产操作规程', status: '生效中' }
])

const recentViews = ref([
  { id: 1, name: '招聘管理制度' },
  { id: 2, name: '员工培训管理制度' },
  { id: 3, name: '办公区域管理规范' },
  { id: 4, name: '绩效考核管理办法' }
])

const documents = ref([])

const filteredDocuments = computed(() => {
  return documents.value.filter(doc => {
    if (searchKeyword.value && !doc.title.includes(searchKeyword.value) && !doc.code.includes(searchKeyword.value)) return false
    if (searchFilters.category && doc.type !== searchFilters.category) return false
    if (searchFilters.status && doc.status !== searchFilters.status) return false
    if (searchFilters.level && doc.level !== searchFilters.level) return false
    if (searchFilters.dept && doc.dept !== searchFilters.dept) return false
    if (searchFilters.dateRange.length === 2) {
      if (doc.publishTime < searchFilters.dateRange[0] || doc.publishTime > searchFilters.dateRange[1]) return false
    }
    return true
  })
})

async function loadData () {
  try {
    const params = { pageNum: 1, pageSize: 1000 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchFilters.status) params.status = searchFilters.status
    const res = await axios.get('/api/process/instance/list', { params })
    if (res.data.code === 200) {
      const data = res.data.data
      documents.value = (data && data.records) ? data.records : (data || [])
    }
  } catch (e) {
    ElMessage.error('加载流程实例列表失败')
    console.error(e)
  }
}

function doSearch () {
  page.current = 1
  loadData()
}

function resetFilters () {
  Object.assign(searchFilters, { category: '', status: '', level: '', dept: '', dateRange: [] })
  searchKeyword.value = ''
  page.current = 1
  loadData()
}

function getStatusType (status) {
  const types = { '生效中': 'success', '待审批': 'warning', '已废止': 'info' }
  return types[status] || ''
}

function isFavorite (doc) {
  if (!doc) return false
  return favorites.value.some(f => f.title === doc.title)
}

function toggleFavorite (doc) {
  if (!doc) return
  const idx = favorites.value.findIndex(f => f.title === doc.title)
  if (idx >= 0) {
    favorites.value.splice(idx, 1)
    ElMessage.success('已取消收藏')
  } else {
    favorites.value.push({
      id: doc.id,
      title: doc.title,
      type: doc.type,
      version: doc.version,
      publishTime: doc.publishTime
    })
    ElMessage.success('已收藏')
  }
}

function previewDocument (doc) {
  currentDocument.value = doc
  previewVisible.value = true
}

function downloadDocument (doc) {
  if (!doc) return
  ElMessage.success(`正在下载：${doc.title}`)
}

function manageFavorites () {
  ElMessage.info('收藏管理功能')
}

function exportResults () {
  ElMessage.success('正在导出搜索结果')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }

.search-bar { display: flex; flex-direction: column; gap: 12px; }
.search-input-wrap { display: flex; gap: 8px; }
.search-filters { margin-top: 8px; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

.hot-list, .favorite-list, .history-list { padding-top: 8px; }
.hot-item, .favorite-item, .history-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; cursor: pointer; transition: background 0.2s; }
.hot-item:hover, .favorite-item:hover, .history-item:hover { background: #f5f7fa; padding: 8px; margin: 0 -8px; border-radius: 4px; }
.hot-rank { width: 24px; height: 24px; background: #f56c6c; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }
.hot-title { flex: 1; }
.empty-favorites { color: #909399; text-align: center; padding: 16px; }

.result-header { display: flex; justify-content: space-between; align-items: center; }
.result-actions { display: flex; gap: 8px; }
.doc-title { color: #409eff; cursor: pointer; text-decoration: underline; }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

.preview-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.preview-title { font-size: 16px; font-weight: bold; }
.preview-actions { display: flex; gap: 8px; }
.preview-info { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #ebeef5; color: #909399; font-size: 13px; }
.preview-content { max-height: 500px; overflow-y: auto; padding: 16px; background: #fafbfc; border-radius: 4px; }
.preview-content :deep(h2) { font-size: 16px; margin: 12px 0; }
.preview-content :deep(h3) { font-size: 14px; margin: 10px 0; color: #409eff; }
.preview-content :deep(p) { line-height: 1.8; margin: 8px 0; }

:deep(.el-icon-star.filled) { color: #e6a23c; }
</style>

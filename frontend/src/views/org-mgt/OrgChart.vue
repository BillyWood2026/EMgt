<template>
  <div class="page-container">
    <div class="page-header">
      <h2>组织架构图可视化</h2>
      <div class="header-actions">
        <el-radio-group v-model="chartType" size="default" @change="loadChart">
          <el-radio-button label="organization">组织架构图</el-radio-button>
          <el-radio-button label="position">岗位架构图</el-radio-button>
          <el-radio-button label="personnel">人员架构图</el-radio-button>
        </el-radio-group>
        <el-button @click="loadChart" style="margin-left: 12px">刷新</el-button>
        <el-button @click="expandAll">展开全部</el-button>
        <el-button @click="collapseAll">折叠全部</el-button>
        <el-button type="warning" plain @click="showVacant">空缺岗位</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索组织/岗位/人员名称或编码"
        clearable
        style="width: 320px"
        @input="handleSearch"
      />
      <el-select v-model="filterLevel" placeholder="按层级筛选" style="width: 150px; margin-left: 10px" clearable>
        <el-option label="一级（公司）" :value="1" />
        <el-option label="二级（中心）" :value="2" />
        <el-option label="三级（部门）" :value="3" />
        <el-option label="四级（科室）" :value="4" />
        <el-option label="五级（小组）" :value="5" />
      </el-select>
      <el-tag v-if="chartType === 'position'" type="warning" effect="plain" style="margin-left: 10px">
        展示组织 + 岗位（编制/在岗）
      </el-tag>
      <el-tag v-if="chartType === 'personnel'" type="success" effect="plain" style="margin-left: 10px">
        展示组织 + 岗位 + 在职人员（空缺高亮）
      </el-tag>
    </div>

    <div class="chart-container" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="{ label: 'label', children: 'children' }"
        node-key="id"
        :default-expand-all="true"
        :expand-on-click-node="false"
        :filter-node-method="filterTreeNode"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <el-tooltip placement="right" :show-after="200">
            <template #content>
              <div v-if="data.type === 'ORG'">
                <div><b>{{ data.name }}</b>（{{ getLevelLabel(data.level) }}）</div>
                <div>负责人：{{ data.orgManager || '—' }}</div>
                <div>编制人数：{{ data.staffingCount ?? '—' }}</div>
                <div>在岗人数：{{ data.currentCount ?? '—' }}</div>
                <div>组织状态：{{ data.orgStatus === 'ENABLED' ? '启用' : '停用' }}</div>
              </div>
              <div v-else-if="data.type === 'POSITION'">
                <div><b>{{ data.name }}</b></div>
                <div>岗位编码：{{ data.positionCode }}</div>
                <div>职级：{{ data.positionLevel || '—' }}</div>
                <div>编制数：{{ data.staffingCount ?? 0 }}</div>
                <div>在岗人数：{{ data.currentCount ?? 0 }}</div>
                <div>空缺数：{{ data.vacantCount ?? 0 }}</div>
                <div>状态：{{ data.positionStatus }}</div>
              </div>
              <div v-else-if="data.type === 'PERSON'">
                <div><b>{{ data.name }}</b></div>
                <div>状态：{{ data.status === 'FILLED' ? '在职' : '空缺' }}</div>
              </div>
            </template>
            <div class="tree-node" :class="{ 'node-highlight': isHighlighted(data) }">
              <span class="node-icon" :class="'level-' + (data.level || data.orgLevel)"></span>
              <span class="node-name">{{ data.label || data.name }}</span>
              <el-tag
                v-if="data.type === 'ORG'"
                :type="data.orgStatus === 'ENABLED' ? 'success' : 'danger'"
                size="small"
                style="margin-left: 8px"
              >{{ data.orgStatus === 'ENABLED' ? '启用' : '停用' }}</el-tag>
              <el-tag
                v-else-if="data.type === 'POSITION'"
                :type="(data.vacantCount || 0) > 0 ? 'warning' : 'success'"
                size="small"
                style="margin-left: 8px"
              >
                编{{ data.staffingCount ?? 0 }}/在{{ data.currentCount ?? 0 }}
              </el-tag>
              <el-tag
                v-else-if="data.type === 'PERSON'"
                :type="data.status === 'FILLED' ? 'success' : 'danger'"
                size="small"
                style="margin-left: 8px"
              >{{ data.status === 'FILLED' ? '在职' : '空缺' }}</el-tag>
              <span v-if="data.type === 'ORG'" class="node-code" style="margin-left: 8px">
                {{ data.orgCode }}
              </span>
            </div>
          </el-tooltip>
        </template>
      </el-tree>
      <el-empty v-if="!loading && treeData.length === 0" description="暂无架构数据" />
    </div>

    <el-drawer v-model="drawerVisible" title="节点详情" size="420px">
      <el-descriptions v-if="currentNode" :column="1" border>
        <el-descriptions-item label="名称">{{ currentNode.name || currentNode.label }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeLabel(currentNode.type) }}</el-descriptions-item>
        <el-descriptions-item label="层级">{{ getLevelLabel(currentNode.level || currentNode.orgLevel) }}</el-descriptions-item>
        <template v-if="currentNode.type === 'ORG'">
          <el-descriptions-item label="组织编码">{{ currentNode.orgCode }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentNode.orgManager || '—' }}</el-descriptions-item>
          <el-descriptions-item label="编制人数">{{ currentNode.staffingCount ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="在岗人数">{{ currentNode.currentCount ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentNode.orgStatus === 'ENABLED' ? 'success' : 'danger'" size="small">
              {{ currentNode.orgStatus === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </template>
        <template v-else-if="currentNode.type === 'POSITION'">
          <el-descriptions-item label="岗位编码">{{ currentNode.positionCode }}</el-descriptions-item>
          <el-descriptions-item label="职级">{{ currentNode.positionLevel || '—' }}</el-descriptions-item>
          <el-descriptions-item label="编制数">{{ currentNode.staffingCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="在岗人数">{{ currentNode.currentCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="空缺数">
            <el-tag :type="(currentNode.vacantCount || 0) > 0 ? 'danger' : 'success'" size="small">
              {{ currentNode.vacantCount ?? 0 }}
            </el-tag>
          </el-descriptions-item>
        </template>
        <template v-else-if="currentNode.type === 'PERSON'">
          <el-descriptions-item label="人员状态">
            <el-tag :type="currentNode.status === 'FILLED' ? 'success' : 'danger'" size="small">
              {{ currentNode.status === 'FILLED' ? '在职' : '空缺' }}
            </el-tag>
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="vacantVisible" title="空缺岗位" width="700px">
      <el-table :data="vacantList" border stripe size="small" v-loading="vacantLoading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="positionCode" label="岗位编码" width="130" />
        <el-table-column prop="positionName" label="岗位名称" min-width="150" />
        <el-table-column prop="orgName" label="所属组织" width="150" />
        <el-table-column prop="positionLevel" label="职级" width="90" />
        <el-table-column label="空缺数" width="90">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.vacantCount }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!vacantLoading && vacantList.length === 0" description="暂无空缺岗位" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const chartType = ref('organization')
const searchText = ref('')
const filterLevel = ref(null)
const treeRef = ref(null)
const drawerVisible = ref(false)
const currentNode = ref(null)

const treeData = ref([])
const vacantVisible = ref(false)
const vacantLoading = ref(false)
const vacantList = ref([])

function typeLabel(t) {
  return { ORG: '组织', POSITION: '岗位', PERSON: '人员' }[t] || t
}

function getLevelLabel(level) {
  const map = { 1: '一级（公司）', 2: '二级（中心）', 3: '三级（部门）', 4: '四级（科室）', 5: '五级（小组）' }
  return map[level] || level
}

function isHighlighted(data) {
  if (!searchText.value) return false
  const kw = searchText.value.toLowerCase()
  return (data.name || '').toLowerCase().includes(kw) ||
    (data.orgCode || '').toLowerCase().includes(kw) ||
    (data.positionCode || '').toLowerCase().includes(kw)
}

function filterTreeNode(value, data) {
  if (!value) {
    if (!filterLevel.value) return true
    return (data.level || data.orgLevel) === filterLevel.value
  }
  const kw = value.toLowerCase()
  const match = (data.name || '').toLowerCase().includes(kw) ||
    (data.orgCode || '').toLowerCase().includes(kw) ||
    (data.positionCode || '').toLowerCase().includes(kw)
  if (!match) return false
  if (filterLevel.value) return (data.level || data.orgLevel) === filterLevel.value
  return true
}

async function loadChart() {
  loading.value = true
  treeData.value = []
  try {
    const endpoint = {
      organization: '/api/org/chart/organization',
      position: '/api/org/chart/position',
      personnel: '/api/org/chart/personnel'
    }[chartType.value]
    const res = await axios.get(endpoint)
    if (res.data.code === 200) {
      treeData.value = normalize(res.data.data || [])
    }
  } catch (e) {
    ElMessage.error('加载架构图失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// Ensure each node has a `name` and unique `id` for el-tree, plus `label` for display
function normalize(nodes) {
  return (nodes || []).map(n => {
    const node = { ...n, name: n.name || n.orgName || n.label, label: n.name || n.orgName || n.label }
    if (node.children && node.children.length) node.children = normalize(node.children)
    return node
  })
}

async function showVacant() {
  vacantVisible.value = true
  vacantLoading.value = true
  vacantList.value = []
  try {
    const res = await axios.get('/api/org/chart/vacant-positions')
    const data = res.data.code !== undefined ? res.data.data : res.data
    vacantList.value = data || []
  } catch (e) {
    ElMessage.error('获取空缺岗位失败')
  } finally {
    vacantLoading.value = false
  }
}

function handleNodeClick(data) {
  currentNode.value = data
  drawerVisible.value = true
}

function handleSearch() {
  if (treeRef.value) treeRef.value.filter(searchText.value)
}

function expandAll() {
  if (treeRef.value) {
    treeRef.value.store._getAllNodes().forEach(n => { n.expanded = true })
  }
}

function collapseAll() {
  if (treeRef.value) {
    treeRef.value.store._getAllNodes().forEach(n => { n.expanded = false })
  }
}

onMounted(() => {
  loadChart()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-actions { display: flex; align-items: center; }
.search-bar { margin-bottom: 20px; display: flex; align-items: center; }
.chart-container {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  min-height: 500px;
  max-height: 70vh;
  overflow-y: auto;
}
.tree-node {
  display: flex;
  align-items: center;
  flex: 1;
  padding: 4px 6px;
  border-radius: 4px;
}
.node-highlight {
  background: #fff3cd;
  font-weight: 600;
}
.node-icon {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
}
.level-1 { background: #f56c6c; }
.level-2 { background: #e6a23c; }
.level-3 { background: #409eff; }
.level-4 { background: #67c23a; }
.level-5 { background: #909399; }
.node-name { font-weight: 500; }
.node-code { font-size: 12px; color: #909399; }
</style>

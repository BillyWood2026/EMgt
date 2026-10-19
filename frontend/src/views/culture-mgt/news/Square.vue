<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">新闻宣传首页：置顶优先展示 · 价值观标签分类 · 移动端同步</el-tag>
    </RoleBar>

    <div class="bar mt">
      <el-radio-group v-model="query.newsType" size="small" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button v-for="t in TYPES" :key="t" :value="t">{{ t }}</el-radio-button>
      </el-radio-group>
      <el-select v-model="query.tag" size="small" placeholder="价值观标签" clearable style="width:150px" @change="load">
        <el-option v-for="t in tags" :key="t.id" :label="t.tagName" :value="t.tagName" />
      </el-select>
      <el-input v-model="query.keyword" size="small" placeholder="搜索标题/摘要" clearable style="width:200px" @change="load" />
      <el-button size="small" @click="load">查询</el-button>
    </div>

    <div v-loading="loading" class="mt">
      <el-empty v-if="!rows.length" description="暂无已发布新闻" />
      <el-row :gutter="12">
        <el-col v-for="n in rows" :key="n.id" :xs="24" :sm="12" :md="8" style="margin-bottom:12px">
          <el-card shadow="hover" :class="['news-card', { top: n.isTop === 1 }]" @click="openDetail(n)">
            <div class="card-head">
              <el-tag v-if="n.isTop === 1" size="small" type="danger" effect="dark">置顶</el-tag>
              <el-tag size="small" type="info">{{ n.newsType }}</el-tag>
              <span class="org">{{ n.sourceOrg }}</span>
            </div>
            <div class="title">{{ n.title }}</div>
            <div class="summary">{{ n.summary }}</div>
            <div class="card-foot">
              <span>
                <el-tag v-for="t in parseTags(n.valueTags)" :key="t" size="small" effect="plain" style="margin-right:4px">{{ t }}</el-tag>
              </span>
              <span class="meta">{{ n.publishTime }}</span>
            </div>
            <div class="stats-line">👁 {{ n.readCount }} · 📱 {{ n.mobileReadCount }} · 👍 {{ n.likeCount }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="showDetail" :title="detail ? detail.title : '新闻详情'" size="520px">
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="类型"><el-tag size="small" type="info">{{ detail.newsType }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="来源组织">{{ detail.sourceOrg }}</el-descriptions-item>
          <el-descriptions-item label="价值观标签">
            <el-tag v-for="t in parseTags(detail.valueTags)" :key="t" size="small" effect="plain" style="margin-right:4px">{{ t }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detail.publishTime }}</el-descriptions-item>
        </el-descriptions>
        <div class="content mt" v-html="detail.content"></div>
        <div class="actions mt">
          <el-button size="small" type="primary" plain @click="doLike">👍 点赞（{{ detail.likeCount }}）</el-button>
          <el-button size="small" @click="doMobileView">📱 移动端预览</el-button>
        </div>
        <div class="meta-line mt">阅读 {{ detail.readCount }} · 移动端 {{ detail.mobileReadCount }} · 点赞 {{ detail.likeCount }}</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'

const TYPES = ['文化动态', '榜样事迹', '高管寄语', '文化专题', '活动报道']

const loading = ref(false)
const rows = ref([])
const tags = ref([])
const query = reactive({ newsType: '', tag: '', keyword: '' })

const showDetail = ref(false)
const detail = ref(null)
const liked = ref({})

function parseTags(json) {
  try { return JSON.parse(json || '[]') } catch { return [] }
}

function load() {
  loading.value = true
  const params = new URLSearchParams()
  if (query.newsType) params.set('newsType', query.newsType)
  if (query.tag) params.set('tag', query.tag)
  if (query.keyword) params.set('keyword', query.keyword)
  axios.get(`/api/culture/news/public?${params}`)
    .then(r => { rows.value = r.data.data || [] })
    .finally(() => { loading.value = false })
}

function loadTags() {
  axios.get('/api/culture/news/tag/list')
    .then(r => { tags.value = (r.data.data || []).filter(t => t.enabled === 1) })
}

function openDetail(n) {
  axios.post(`/api/culture/news/${n.id}/view`)
    .then(r => {
      Object.assign(n, r.data.data)
      detail.value = n
      showDetail.value = true
    })
}

function doLike() {
  if (liked.value[detail.value.id]) { ElMessage.warning('已点过赞啦'); return }
  axios.post(`/api/culture/news/${detail.value.id}/like`)
    .then(r => {
      Object.assign(detail.value, r.data.data)
      liked.value[detail.value.id] = true
      const hit = rows.value.find(x => x.id === detail.value.id)
      if (hit) Object.assign(hit, r.data.data)
      ElMessage.success('点赞成功')
    })
    .catch(e => ElMessage.error(e.response?.data?.message || '操作失败'))
}

function doMobileView() {
  axios.post(`/api/culture/news/${detail.value.id}/view?channel=mobile`)
    .then(r => {
      Object.assign(detail.value, r.data.data)
      const hit = rows.value.find(x => x.id === detail.value.id)
      if (hit) Object.assign(hit, r.data.data)
      ElMessage.success('移动端阅读+1（三端同步计数）')
    })
}

onMounted(() => { load(); loadTags() })
</script>

<style scoped>
.page-container { padding: 12px; }
.mt { margin-top: 12px; }
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.news-card { cursor: pointer; border-left: 3px solid transparent; }
.news-card.top { border-left: 3px solid #f56c6c; background: #fffaf5; }
.card-head { display: flex; gap: 6px; align-items: center; margin-bottom: 6px; }
.org { color: #909399; font-size: 12px; margin-left: auto; }
.title { font-weight: 600; font-size: 14px; margin-bottom: 4px; }
.summary { color: #606266; font-size: 12px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.meta { color: #909399; font-size: 12px; }
.stats-line { color: #909399; font-size: 12px; margin-top: 6px; }
.detail-body { padding: 4px; }
.content { line-height: 1.8; font-size: 13px; }
.actions { display: flex; gap: 8px; }
.meta-line { color: #909399; font-size: 12px; }
</style>

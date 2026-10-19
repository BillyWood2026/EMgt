<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="success" effect="plain">前台只读 · 文化全景聚合生效数据</el-tag>
    </RoleBar>

    <el-row :gutter="12" class="mb">
      <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-t">生效纲领</div><div class="kpi-v">{{ creed ? creed.version : '—' }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-t">发展里程碑</div><div class="kpi-v">{{ milestones.length }} 个</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="kpi kpi-warn"><div class="kpi-t">生效红线(重大)</div><div class="kpi-v">{{ redlines.length }} 条 / 重大 {{ levelCount['重大'] || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="kpi"><div class="kpi-t">生效制度 / 释义</div><div class="kpi-v">{{ institutions.length }} / {{ glossaries.length }}</div></el-card></el-col>
    </el-row>

    <el-card v-if="creed" shadow="never" class="mb">
      <template #header>
        <div class="bar">
          <span style="font-weight:600">{{ creed.creedName }}（{{ creed.version }}）</span>
          <el-tag size="small" type="success">生效中</el-tag>
          <span style="flex:1"></span>
          <span style="color:#909399;font-size:12px">适用：{{ creed.applyOrg }} ｜ 生效 {{ creed.effectDate }}</span>
        </div>
      </template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="企业使命">{{ creed.mission }}</el-descriptions-item>
        <el-descriptions-item label="企业愿景">{{ creed.vision }}</el-descriptions-item>
        <el-descriptions-item label="企业精神 / 口号">{{ creed.spirit }} ｜ {{ creed.slogan }}</el-descriptions-item>
        <el-descriptions-item label="核心价值观">
          <el-row :gutter="10">
            <el-col v-for="g in valueGroups" :key="g.title" :span="12" class="mb">
              <el-card shadow="never" class="vg">
                <div class="vg-t">{{ g.title }}</div>
                <ul><li v-for="(c,i) in groupClauses(g)" :key="i">{{ c }}</li></ul>
              </el-card>
            </el-col>
          </el-row>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-empty v-else description="暂无生效纲领" />

    <el-card shadow="never" class="mb">
      <template #header><span style="font-weight:600">发展里程碑</span></template>
      <el-timeline v-if="milestones.length">
        <el-timeline-item v-for="m in milestones" :key="m.id" :timestamp="m.occurDate" placement="top" type="success">
          <div style="font-weight:600">{{ m.title }}</div>
          <div style="color:#606266">{{ m.content }}</div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无里程碑" :image-size="70" />
    </el-card>

    <el-card shadow="never" class="mb">
      <template #header>
        <div class="bar">
          <span style="font-weight:600">文化红线禁令</span>
          <el-tag size="small" type="danger">重大红线 = 评优/晋升/调薪一票否决</el-tag>
        </div>
      </template>
      <el-table :data="redlines" size="small" border>
        <el-table-column prop="code" label="编号" width="110" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="description" label="违规情形" min-width="260" show-overflow-tooltip />
        <el-table-column label="等级" width="80">
          <template #default="{row}"><el-tag size="small" :type="levelType(row.violationLevel)">{{ row.violationLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="一票否决" width="90">
          <template #default="{row}"><el-tag v-if="row.vetoFlag===1" size="small" type="danger">否决锁定</el-tag><span v-else>—</span></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-card shadow="never" class="mb">
          <template #header><span style="font-weight:600">生效制度与合规覆盖</span></template>
          <div v-for="d in institutions" :key="d.id" class="inst">
            <div class="bar">
              <span>{{ d.docName }} <span style="color:#909399;font-size:12px">{{ d.version }} · {{ d.docType }}</span></span>
              <span :style="{color: rateOf(d)>=80?'#67c23a':'#e6a23c'}">{{ rateOf(d) }}%</span>
            </div>
            <el-progress :percentage="rateOf(d)" :stroke-width="8" :status="rateOf(d)>=80?'success':''" />
          </div>
          <el-empty v-if="!institutions.length" description="暂无生效制度" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="mb">
          <template #header><span style="font-weight:600">价值观释义</span></template>
          <el-collapse>
            <el-collapse-item v-for="g in glossaries" :key="g.id" :title="g.valueTag" :name="g.id">
              <p><b>正向：</b>{{ g.positiveText }}</p>
              <p v-if="g.negativeText"><b>反面：</b>{{ g.negativeText }}</p>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import RoleBar from '../CultureRoleBar.vue'

const data = ref({})
const creed = computed(() => data.value.activeCreed || null)
const milestones = computed(() => data.value.milestones || [])
const redlines = computed(() => data.value.redlines || [])
const institutions = computed(() => data.value.institutions || [])
const glossaries = computed(() => data.value.glossaries || [])
const levelCount = computed(() => data.value.redlineLevelCount || {})

const valueGroups = computed(() => {
  try { return JSON.parse(creed.value.valueGroups || '[]') } catch { return [] }
})
function groupClauses(g) {
  return Array.isArray(g.clauses) ? g.clauses : []
}
function levelType(lv) {
  if (lv === '重大') return 'danger'
  if (lv === '严重') return 'warning'
  if (lv === '较重') return 'warning'
  return 'info'
}
function rateOf(d) {
  if (!d.shouldCount || d.shouldCount <= 0) return 0
  return Math.round(d.learnedCount * 10000 / d.shouldCount) / 100
}

async function load() {
  const r = await axios.get('/api/culture/foundation/panorama/overview')
  data.value = r.data.data || {}
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mb { margin-bottom:12px; }
.kpi { text-align:center; }
.kpi-t { color:#909399; font-size:12px; }
.kpi-v { font-size:22px; font-weight:700; color:#2f9e44; margin-top:4px; }
.kpi-warn .kpi-v { color:#e6a23c; font-size:18px; }
.vg { background:#f6ffed; border-color:#b7eb8f; }
.vg-t { font-weight:600; color:#2f9e44; margin-bottom:4px; }
.vg ul { margin:0; padding-left:18px; color:#606266; }
.inst { margin-bottom:10px; }
</style>

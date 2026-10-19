<template>
  <div class="page-container">
    <div class="page-header">
      <h2>职业健康概览</h2>
      <el-button type="primary" @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.hazardPostCount }}</div>
            <div class="stat-label">职业危害岗位数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.highHazardPostCount }}</div>
            <div class="stat-label">高危害岗位数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon orange"><el-icon><FirstAidKit /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.abnormalExamCount }}</div>
            <div class="stat-label">体检异常人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon red"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.suspectedDiseaseCount }}</div>
            <div class="stat-label">职业病疑似病例</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon green"><el-icon><Box /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ protectionIssuanceRate }}%</div>
            <div class="stat-label">防护用品领用率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon blue"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ healthComplianceRate }}%</div>
            <div class="stat-label">健康合规率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>近期职业健康检查</span></template>
          <el-table :data="exams" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="employeeName" label="员工" width="90" />
            <el-table-column prop="dept" label="部门" width="110" />
            <el-table-column prop="examType" label="体检类型" width="90" />
            <el-table-column prop="examDate" label="检查日期" width="110" />
            <el-table-column label="结论" min-width="100">
              <template #default="{row}">
                <span :class="{ 'text-danger': isAbnormal(row.result) }">{{ row.result }}</span>
              </template>
            </el-table-column>
            <el-table-column label="异常" width="70">
              <template #default="{row}">
                <el-tag v-if="isAbnormal(row.result)" type="danger" size="small">异常</el-tag>
                <el-tag v-else type="success" size="small">正常</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>需关注健康档案</span></template>
          <el-table :data="attentionRecords" border size="small" v-loading="loading" max-height="380">
            <el-table-column prop="employeeName" label="员工" width="90" />
            <el-table-column prop="post" label="岗位" width="120" />
            <el-table-column prop="hazardFactor" label="危害因素" min-width="120" />
            <el-table-column label="状态" width="90">
              <template #default="{row}">
                <el-tag :type="row.status === 'abnormal' ? 'danger' : 'warning'" size="small">
                  {{ row.status === 'abnormal' ? '异常' : '需关注' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { healthApi } from '@/api/eehs'

const loading = ref(false)
const exams = ref([])
const records = ref([])
const protections = ref([])
const attentionRecords = ref([])
const stats = reactive({
  hazardPostCount: 0,
  highHazardPostCount: 0,
  abnormalExamCount: 0,
  suspectedDiseaseCount: 0
})

// 高危害因素集合（噪音/粉尘/化学试剂/辐射）
const HIGH_HAZARD_TYPES = ['噪音', '粉尘', '化学试剂', '辐射']

function isAbnormal(result) {
  return !!result && (result.includes('异常') || result.includes('职业病'))
}

// 防护用品领用率 = 已领用(状态正常)的防护用品数 / 防护用品总数 × 100%
const protectionIssuanceRate = computed(() => {
  const items = protections.value.filter(p => p.category === '防护用品')
  if (items.length === 0) return '0.00'
  const issued = items.filter(p => p.status === 'normal').length
  return ((issued / items.length) * 100).toFixed(1)
})

// 健康合规率 = 档案状态正常数 / 档案总数 × 100%
const healthComplianceRate = computed(() => {
  if (records.value.length === 0) return '0.00'
  const normal = records.value.filter(r => r.status === 'normal').length
  return ((normal / records.value.length) * 100).toFixed(1)
})

async function loadAll() {
  loading.value = true
  try {
    const [r, e, p] = await Promise.all([
      healthApi.records.list(),
      healthApi.examinations.list(),
      healthApi.protections.list()
    ])
    const rData = r.data.code === 200 ? (r.data.data || []) : []
    const eData = e.data.code === 200 ? (e.data.data || []) : []
    const pData = p.data.code === 200 ? (p.data.data || []) : []
    records.value = rData
    protections.value = pData
    exams.value = eData.slice(0, 10)
    attentionRecords.value = rData.filter(x => x.status === 'abnormal' || x.status === 'attention').slice(0, 10)

    // 职业危害岗位数：有危害因素的去重岗位
    const hazardPosts = new Set(rData.filter(x => x.hazardFactor).map(x => x.post).filter(Boolean))
    stats.hazardPostCount = hazardPosts.size
    // 高危害岗位数：危害因素含高危害类型
    const highHazardPosts = new Set(
      rData.filter(x => x.hazardFactor && HIGH_HAZARD_TYPES.some(t => x.hazardFactor.includes(t)))
        .map(x => x.post).filter(Boolean)
    )
    stats.highHazardPostCount = highHazardPosts.size
    // 体检异常人数
    stats.abnormalExamCount = eData.filter(x => isAbnormal(x.result)).length
    // 职业病疑似病例
    stats.suspectedDiseaseCount = eData.filter(x => x.result && (x.result.includes('疑似职业病') || x.result.includes('职业病疑似'))).length
  } finally { loading.value = false }
}

onMounted(loadAll)
</script>
<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 16px; }
.stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; }
.stat-icon.blue { background-color: #e6f7ff; color: #1890ff; }
.stat-icon.green { background-color: #f6ffed; color: #52c41a; }
.stat-icon.orange { background-color: #fff7e6; color: #fa8c16; }
.stat-icon.red { background-color: #fff1f0; color: #f5222d; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
.text-danger { color: #f5222d; font-weight: bold; }
</style>

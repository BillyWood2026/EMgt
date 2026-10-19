<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">定量线性映射：score = min(完成率, 满分) · 定性去极值均值：去掉最高最低后取平均</el-tag>
    </RoleBar>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>线性映射计算器（定量指标）</b></template>
          <el-form label-width="110px">
            <el-form-item label="完成率/达成值">
              <el-input-number v-model="lm.ratio" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
            <el-form-item label="满分">
              <el-input-number v-model="lm.maxScore" :precision="2" :min="0" style="width:100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="calcLinear">计算得分</el-button>
            </el-form-item>
          </el-form>
          <div v-if="lm.result != null" class="result-box">
            <div class="num">{{ lm.result }}</div>
            <div class="hint">得分 = min({{ lm.ratio }}, {{ lm.maxScore }})</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>去极值均值计算器（定性指标）</b></template>
          <el-form label-width="110px">
            <el-form-item label="评委分">
              <el-input v-model="tm.values" placeholder="逗号分隔，如 85,90,88,92,78" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="calcTrim">计算均值</el-button>
            </el-form-item>
          </el-form>
          <div v-if="tm.result != null" class="result-box">
            <div class="num">{{ tm.result }}</div>
            <div class="hint">已去掉 1 个最高分与 1 个最低分后取均值</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:12px">
      <template #header><b>算法说明</b></template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="定量指标">由数据源模块（三会一课/党员积分/党费收缴等）自动取数，按线性映射换算得分，不超过指标满分。</el-descriptions-item>
        <el-descriptions-item label="定性指标">多名评委独立打分，系统自动去掉一个最高分与一个最低分后取算术平均，降低极端分影响。</el-descriptions-item>
        <el-descriptions-item label="加减分指标">在总分基础上直接加减，受方案「调整分上限」约束。</el-descriptions-item>
        <el-descriptions-item label="一票否决">标记为一票否决的指标若核查命中，则该考核对象直接判定为不合格。</el-descriptions-item>
        <el-descriptions-item label="等级判定">总分 ≥ 优秀线→优秀；≥ 良好线→良好；≥ 合格线→合格；否则不合格。</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import axios from '@/api/axiosConfig'
import RoleBar from '../PartyRoleBar.vue'

const lm = reactive({ ratio: 85, maxScore: 100, result: null })
const tm = reactive({ values: '85,90,88,92,78', result: null })

async function calcLinear() {
  const res = await axios.get('/api/party/assessment/algorithm/linear-map', { params: { ratio: lm.ratio, maxScore: lm.maxScore } })
  lm.result = res.data.data.score
}
async function calcTrim() {
  const res = await axios.get('/api/party/assessment/algorithm/trim-mean', { params: { values: tm.values } })
  tm.result = res.data.data.mean
}
</script>

<style scoped>
.page-container { padding: 12px; }
.result-box { text-align: center; padding: 18px; background: #f4f7fb; border-radius: 6px; }
.num { font-size: 32px; font-weight: 600; color: #2f6fdd; }
.hint { color: #8a97a8; font-size: 12px; margin-top: 6px; }
</style>

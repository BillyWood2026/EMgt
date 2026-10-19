<template>
  <div class="page-container">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2>技能认证管理</h2>
        <span class="subtitle">央企五级标准 · 技能全生命周期管理 · 证书分级认定</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleRouter('/hr/skill-certification/plan')">
          <el-icon><Calendar /></el-icon> 认证计划
        </el-button>
        <el-button type="success" @click="handleRouter('/hr/skill-certification/apply')">
          <el-icon><Edit /></el-icon> 技能申报
        </el-button>
        <el-button type="warning" @click="handleRouter('/hr/skill-certification/review')">
          <el-icon><Document /></el-icon> 评审打分
        </el-button>
        <el-button @click="handleRouter('/hr/skill-certification/profile')">
          <el-icon><User /></el-icon> 我的档案
        </el-button>
        <el-button @click="handleRouter('/hr/skill-certification/certificate')">
          <el-icon><Medal /></el-icon> 证书台账
        </el-button>
        <el-button type="danger" @click="handleRouter('/hr/skill-certification/warning')">
          <el-icon><Warning /></el-icon> 预警台账
        </el-button>
        <el-button type="info" @click="handleRouter('/hr/skill-certification/standard')">
          <el-icon><Setting /></el-icon> 标准配置
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon blue"><User /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.totalCertified }}</div>
          <div class="stat-label">持证总人数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><Medal /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.totalCertificates }}</div>
          <div class="stat-label">有效证书数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><Clock /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.expiringCount }}</div>
          <div class="stat-label">即将到期</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><TrendCharts /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.pendingApply }}</div>
          <div class="stat-label">申报中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red"><Warning /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.expiredCount }}</div>
          <div class="stat-label">已过期</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cyan"><DataAnalysis /></div>
        <div class="stat-content">
          <div class="stat-value">{{ statsData.thisYearPass }}</div>
          <div class="stat-label">本年通过</div>
        </div>
      </div>
    </div>

    <!-- Tab切换区 -->
    <div class="main-content">
      <el-tabs v-model="activeTab" class="skill-tabs">
        <el-tab-pane label="认证计划" name="plan">
          <SkillPlanList />
        </el-tab-pane>
        <el-tab-pane label="我的申报" name="my-apply">
          <SkillMyApply />
        </el-tab-pane>
        <el-tab-pane label="认证评审" name="review" :lazy="true">
          <SkillReviewList />
        </el-tab-pane>
        <el-tab-pane label="我的档案" name="profile" :lazy="true">
          <SkillProfileCard />
        </el-tab-pane>
        <el-tab-pane label="证书台账" name="certificate" :lazy="true">
          <SkillCertificateLedger />
        </el-tab-pane>
        <el-tab-pane label="预警台账" name="warning" :lazy="true">
          <SkillWarningLedger />
        </el-tab-pane>
        <el-tab-pane label="标准配置" name="standard" :lazy="true">
          <SkillStandardConfig />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Calendar, Edit, Document, User, Medal, Warning, Setting, Clock, TrendCharts, DataAnalysis } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

// 子组件导入
import SkillPlanList from './SkillPlan.vue';
import SkillMyApply from './SkillApply.vue';
import SkillReviewList from './SkillReview.vue';
import SkillProfileCard from './SkillProfile.vue';
import SkillCertificateLedger from './SkillCertificate.vue';
import SkillWarningLedger from './SkillWarning.vue';
import SkillStandardConfig from './SkillStandard.vue';

const router = useRouter();
const activeTab = ref('plan');

// 统计数据
const statsData = reactive({
  totalCertified: 0,
  totalCertificates: 0,
  expiringCount: 0,
  pendingApply: 0,
  expiredCount: 0,
  thisYearPass: 0
});

// 路由跳转
const handleRouter = (path: string) => {
  router.push(path);
};

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await axios.get('/api/skill-cert/ledger/statistics');
    const data = res.data?.data || res.data;
    Object.assign(statsData, data);
  } catch (error) {
    console.error('加载统计数据失败', error);
  }
};

onMounted(() => {
  loadStats();
});
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  font-size: 12px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.header-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid;
}

.stat-card:nth-child(1) { border-left-color: #409eff; }
.stat-card:nth-child(2) { border-left-color: #67c23a; }
.stat-card:nth-child(3) { border-left-color: #e6a23c; }
.stat-card:nth-child(4) { border-left-color: #9c27b0; }
.stat-card:nth-child(5) { border-left-color: #f56c6c; }
.stat-card:nth-child(6) { border-left-color: #00bcd4; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.blue { background: linear-gradient(135deg, #409eff, #66b1ff); }
.stat-icon.green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-icon.orange { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.stat-icon.purple { background: linear-gradient(135deg, #9c27b0, #b366d9); }
.stat-icon.red { background: linear-gradient(135deg, #f56c6c, #f78989); }
.stat-icon.cyan { background: linear-gradient(135deg, #00bcd4, #26c6da); }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 主内容区 */
.main-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px;
}

.skill-tabs {
  margin-top: 0;
}

@media (max-width: 1400px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

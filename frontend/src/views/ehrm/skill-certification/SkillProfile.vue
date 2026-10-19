<template>
  <div class="profile-container">
    <!-- 技能等级分布 -->
    <div class="level-distribution">
      <h3>技能等级分布</h3>
      <div class="level-cards">
        <div v-for="level in levelStats" :key="level.level" class="level-card" :class="`level-${level.level}`">
          <div class="level-badge">{{ level.level }}</div>
          <div class="level-name">{{ level.levelName }}</div>
          <div class="level-count">{{ level.count }} 人</div>
          <div class="level-percent">{{ level.percent }}%</div>
        </div>
      </div>
    </div>

    <!-- 技能档案列表 -->
    <div class="profile-list">
      <div class="section-header">
        <h3>我的技能档案</h3>
        <el-button type="primary" plain @click="handleExportProfile">
          <el-icon><Download /></el-icon> 导出档案
        </el-button>
      </div>

      <el-table :data="profileList" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="skillTypeName" label="技能工种" min-width="120" />
        <el-table-column prop="highestLevel" label="最高等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.highestLevel" :type="getLevelTagType(row.highestLevel)" size="small">
              {{ row.highestLevel }}
            </el-tag>
            <span v-else class="text-muted">未获证</span>
          </template>
        </el-table-column>
        <el-table-column prop="certCount" label="证书数量" width="90" align="center" />
        <el-table-column prop="latestCertDate" label="最近获证" width="120" />
        <el-table-column prop="validCertCount" label="有效证书" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.validCertCount > 0, 'text-danger': row.validCertCount === 0 }">
              {{ row.validCertCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="expiringDays" label="最近到期" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.expiringDays > 0" :class="{ 'text-warning': row.expiringDays <= 30 }">
              {{ row.expiringDays }}天
            </span>
            <span v-else-if="row.expiringDays === 0" class="text-danger">已到期</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyCount" label="申报次数" width="90" align="center" />
        <el-table-column prop="passCount" label="通过次数" width="90" align="center" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 技能详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="技能档案详情" width="900px" destroy-on-close>
      <el-tabs v-model="detailTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工种名称">{{ currentProfile.skillTypeName }}</el-descriptions-item>
            <el-descriptions-item label="最高等级">
              <el-tag v-if="currentProfile.highestLevel" :type="getLevelTagType(currentProfile.highestLevel)">
                {{ currentProfile.highestLevel }} - {{ currentProfile.highestLevelName }}
              </el-tag>
              <span v-else class="text-muted">未获证</span>
            </el-descriptions-item>
            <el-descriptions-item label="从业年限">{{ currentProfile.workYears }} 年</el-descriptions-item>
            <el-descriptions-item label="累计培训学时">{{ currentProfile.totalTrainHours }} 学时</el-descriptions-item>
            <el-descriptions-item label="证书总数">{{ currentProfile.certCount }}</el-descriptions-item>
            <el-descriptions-item label="有效证书">{{ currentProfile.validCertCount }}</el-descriptions-item>
            <el-descriptions-item label="申报次数">{{ currentProfile.applyCount }}</el-descriptions-item>
            <el-descriptions-item label="通过次数">{{ currentProfile.passCount }}</el-descriptions-item>
            <el-descriptions-item label="认证通过率">{{ currentProfile.passRate }}%</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 证书记录 -->
        <el-tab-pane label="证书记录" name="certificates">
          <el-table :data="certList" border stripe size="small">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="certNo" label="证书编号" min-width="150" />
            <el-table-column prop="certName" label="证书名称" min-width="120" />
            <el-table-column prop="certLevel" label="等级" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ row.certLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="certType" label="证书类型" width="120" align="center">
              <template #default="{ row }">
                {{ row.certType === 'INTERNAL' ? '内部认证' : '社会证书' }}
              </template>
            </el-table-column>
            <el-table-column prop="issueDate" label="发证日期" width="120" />
            <el-table-column prop="validUntil" label="有效期至" width="120" />
            <el-table-column prop="certStatus" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getCertStatusType(row.certStatus)" size="small">{{ row.certStatusText }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 认证历史 -->
        <el-tab-pane label="认证历史" name="history">
          <el-timeline>
            <el-timeline-item v-for="(record, index) in historyList" :key="index" :timestamp="record.createTime" placement="top">
              <el-card shadow="never">
                <div class="history-item">
                  <div class="history-header">
                    <el-tag :type="record.result === 'PASS' ? 'success' : 'danger'" size="small">{{ record.resultText }}</el-tag>
                    <span class="history-level">{{ record.applyLevel }}</span>
                    <span class="history-plan">{{ record.planName }}</span>
                  </div>
                  <div class="history-scores">
                    <span>理论：{{ record.theoryScore ?? '-' }}</span>
                    <span>实操：{{ record.practiceScore ?? '-' }}</span>
                    <span>素养：{{ record.qualityScore ?? '-' }}</span>
                    <span>总分：<strong>{{ record.finalScore ?? '-' }}</strong></span>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <!-- 补考记录 -->
        <el-tab-pane label="补考记录" name="retake">
          <el-table :data="retakeList" border stripe size="small">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="originalApplyTime" label="原认证时间" width="160" />
            <el-table-column prop="failReason" label="不合格原因" min-width="150" />
            <el-table-column prop="retakeTime" label="补考时间" width="160" />
            <el-table-column prop="retakeScore" label="补考成绩" width="90" align="center" />
            <el-table-column prop="retakeResult" label="结果" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.retakeResult === 'PASS' ? 'success' : 'danger'" size="small">{{ row.retakeResultText }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="retakeList.length === 0" description="无补考记录" :image-size="80" />
        </el-tab-pane>

        <!-- 成果记录 -->
        <el-tab-pane label="成果记录" name="achievements">
          <el-table :data="achievementList" border stripe size="small">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="achievementType" label="成果类型" width="120" align="center" />
            <el-table-column prop="achievementName" label="成果名称" min-width="150" />
            <el-table-column prop="achievementLevel" label="级别" width="100" align="center" />
            <el-table-column prop="completeTime" label="完成时间" width="120" />
            <el-table-column prop="remark" label="备注" min-width="150" />
          </el-table>
          <el-empty v-if="achievementList.length === 0" description="无成果记录" :image-size="80" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Download } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const profileList = ref([]);
const detailDialogVisible = ref(false);
const detailTab = ref('basic');
const currentProfile = ref<any>({});

// 证书列表
const certList = ref([]);
// 认证历史
const historyList = ref([]);
// 补考记录
const retakeList = ref([]);
// 成果记录
const achievementList = ref([]);

// 等级统计
const levelStats = ref([
  { level: 'L1', levelName: '初级工', count: 456, percent: 36.3 },
  { level: 'L2', levelName: '中级工', count: 389, percent: 31.0 },
  { level: 'L3', levelName: '高级工', count: 234, percent: 18.6 },
  { level: 'L4', levelName: '技师', count: 89, percent: 7.1 },
  { level: 'L5', levelName: '高级技师', count: 88, percent: 7.0 }
]);

// 获取等级标签类型
const getLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    L1: 'info',
    L2: 'primary',
    L3: 'warning',
    L4: 'danger',
    L5: 'success'
  };
  return typeMap[level] || 'info';
};

// 获取证书状态类型
const getCertStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    VALID: 'success',
    EXPIRING: 'warning',
    EXPIRED: 'danger',
    REVOKED: 'info'
  };
  return typeMap[status] || 'info';
};

// 加载档案列表
const loadProfileList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/ledger/my');
    const data = res.data?.data || res.data;
    profileList.value = Array.isArray(data) ? data : (data.list || data.records || []);
  } catch (error) {
    console.error('加载档案列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 查看详情
const handleViewDetail = async (row: any) => {
  currentProfile.value = row;
  detailTab.value = 'basic';

  try {
    const res = await axios.get(`/api/skill-cert/ledger/employee/${row.id}`);
    const data = res.data?.data || res.data;
    certList.value = data.certificates || data.certList || [];
    historyList.value = data.history || data.historyList || [];
    retakeList.value = data.retakes || data.retakeList || [];
    achievementList.value = data.achievements || data.achievementList || [];
  } catch (error) {
    console.error('加载详情失败', error);
  }

  detailDialogVisible.value = true;
};

// 导出档案
const handleExportProfile = () => {
  ElMessage.info('正在导出个人技能档案...');
  window.open('/api/skill-cert/ledger/export');
};

onMounted(() => {
  loadProfileList();
});
</script>

<style scoped>
.profile-container {
  padding: 0;
}

.level-distribution {
  margin-bottom: 24px;
}

.level-distribution h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.level-cards {
  display: flex;
  gap: 16px;
}

.level-card {
  flex: 1;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.level-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.level-L1::before { background: #909399; }
.level-L2::before { background: #409eff; }
.level-L3::before { background: #e6a23c; }
.level-L4::before { background: #f56c6c; }
.level-L5::before { background: #67c23a; }

.level-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin-bottom: 8px;
}

.level-L1 .level-badge { background: #909399; }
.level-L2 .level-badge { background: #409eff; }
.level-L3 .level-badge { background: #e6a23c; }
.level-L4 .level-badge { background: #f56c6c; }
.level-L5 .level-badge { background: #67c23a; }

.level-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.level-count {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.level-percent {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.profile-list {
  background: white;
  padding: 16px;
  border-radius: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.history-item {
  padding: 8px 0;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.history-level {
  font-weight: 600;
  color: #303133;
}

.history-plan {
  color: #606266;
  font-size: 13px;
}

.history-scores {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.history-scores strong {
  color: #67c23a;
}

.text-muted {
  color: #909399;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.text-warning {
  color: #e6a23c;
}
</style>

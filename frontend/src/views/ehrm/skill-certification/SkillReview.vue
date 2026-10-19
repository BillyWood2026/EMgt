<template>
  <div class="review-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterForm.reviewStatus" placeholder="评审状态" clearable style="width: 140px;">
        <el-option label="待评审" value="PENDING" />
        <el-option label="已评分" value="SCORED" />
        <el-option label="已完成" value="COMPLETED" />
      </el-select>
      <el-select v-model="filterForm.planId" placeholder="认证计划" clearable style="width: 200px;">
        <el-option v-for="plan in planOptions" :key="plan.id" :label="plan.planName" :value="plan.id" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 查询
      </el-button>
      <el-button @click="handleReset">
        <el-icon><RefreshRight /></el-icon> 重置
      </el-button>
    </div>

    <!-- 评审列表 -->
    <el-table :data="reviewList" border stripe v-loading="loading" @row-click="handleRowClick">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="userName" label="员工姓名" width="90" />
      <el-table-column prop="orgName" label="所属组织" min-width="120" />
      <el-table-column prop="skillTypeName" label="申报工种" width="100" />
      <el-table-column prop="applyLevel" label="申报等级" width="80" align="center">
        <template #default="{ row }">
          <el-tag type="warning" size="small">{{ row.applyLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="planName" label="认证计划" min-width="160" show-overflow-tooltip />
      <el-table-column prop="theoryScore" label="理论成绩" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.theoryScore !== undefined">{{ row.theoryScore }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="practiceScore" label="实操成绩" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.practiceScore !== undefined">{{ row.practiceScore }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="qualityScore" label="素养得分" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.qualityScore !== undefined">{{ row.qualityScore }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="finalScore" label="综合得分" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.finalScore" :class="{ 'text-success': row.finalScore >= 80, 'text-danger': row.finalScore < 80 }">{{ row.finalScore }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="评审状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.reviewStatus)" size="small">{{ row.reviewStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewerName" label="评委" width="90" align="center" />
      <el-table-column prop="reviewTime" label="评审时间" width="160" />
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 评审打分弹窗 -->
    <el-dialog v-model="scoreDialogVisible" title="认证评审打分" width="850px" :close-on-click-modal="false" destroy-on-close>
      <div class="score-header">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="员工姓名">{{ currentRecord.userName }}</el-descriptions-item>
          <el-descriptions-item label="所属组织">{{ currentRecord.orgName }}</el-descriptions-item>
          <el-descriptions-item label="申报工种">{{ currentRecord.skillTypeName }}</el-descriptions-item>
          <el-descriptions-item label="申报等级">
            <el-tag type="warning" size="small">{{ currentRecord.applyLevel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="理论成绩">
            <span :class="{ 'text-success': currentRecord.theoryScore >= 70, 'text-danger': currentRecord.theoryScore < 70 }">
              {{ currentRecord.hasExternalCert ? '免考(100)' : (currentRecord.theoryScore ?? '-') }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="持证情况">
            <el-tag v-if="currentRecord.hasExternalCert" type="success" size="small">有外部证书(免理论)</el-tag>
            <span v-else class="text-muted">无</span>
          </el-descriptions-item>
        </el-descriptions>
        <!-- 评审进度 -->
        <el-steps :active="reviewStep" finish-status="success" style="margin-top: 16px;" size="small">
          <el-step title="评委一打分" />
          <el-step title="评委二打分" />
          <el-step title="部门复核" />
          <el-step title="人力终审" />
        </el-steps>
      </div>

      <el-divider content-position="left">实操考核打分</el-divider>
      <el-form :model="scoreForm" :rules="scoreRules" ref="scoreFormRef" label-width="100px">
        <el-table :data="practiceItems" border size="small">
          <el-table-column prop="itemName" label="考核项目" min-width="120" />
          <el-table-column prop="maxScore" label="满分" width="70" align="center" />
          <el-table-column prop="score" label="得分" width="120" align="center">
            <template #default="{ row, $index }">
              <el-form-item :prop>`practiceItems[${$index}].score`" :rules="[{ required: true, message: '请打分', trigger: 'blur' }]" style="margin-bottom: 0;">
                <el-input-number v-model="row.score" :min="0" :max="row.maxScore" size="small" style="width: 90px;" />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注">
            <template #default="{ row, $index }">
              <el-input v-model="row.remark" placeholder="扣分说明" size="small" />
            </template>
          </el-table-column>
        </el-table>

        <el-row :gutter="20" style="margin-top: 16px;">
          <el-col :span="8">
            <el-form-item label="实操总分">
              <el-input :value="calculatePracticeTotal" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单科判定">
              <el-tag :type="checkSingleScorePass.practicePass ? 'success' : 'danger'" size="small">
                {{ checkSingleScorePass.practicePass ? '达标' : '未达标' }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">过程素养评分</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="培训出勤" prop="attendanceScore">
              <el-input-number v-model="scoreForm.attendanceScore" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="作业规范" prop="standardScore">
              <el-input-number v-model="scoreForm.standardScore" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="带教配合" prop="mentoringScore">
              <el-input-number v-model="scoreForm.mentoringScore" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="日常绩效" prop="performanceScore">
              <el-input-number v-model="scoreForm.performanceScore" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合规记录" prop="complianceScore">
              <el-input-number v-model="scoreForm.complianceScore" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="素养总分">
              <el-input :value="calculateQualityTotal" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">综合评定</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="综合得分">
              <el-input :value="calculateFinalScore" disabled style="font-size: 20px; font-weight: bold;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合格判定">
              <el-tag :type="isPass ? 'success' : 'danger'" size="large">{{ isPass ? '合格' : '不合格' }}</el-tag>
              <span class="pass-threshold">（L1/L2≥80，L3≥85，L4/L5≥90）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 单科合格明细 -->
        <el-alert v-if="!isPass && passDetail.length > 0" type="error" :closable="false" style="margin-bottom: 16px;">
          <template #title>
            <span>不合格原因：{{ passDetail.join('；') }}</span>
          </template>
        </el-alert>

        <!-- 补考入口（L4/L5不支持单纯补考） -->
        <el-row v-if="!isPass && !['L4', 'L5'].includes(currentRecord.applyLevel)" :gutter="20" style="margin-top: 16px;">
          <el-col :span="24">
            <el-button type="warning" @click="handleRetake">
              <el-icon><RefreshRight /></el-icon>发起单科补考
            </el-button>
            <span class="retake-tip">（L4/L5高级别不支持单纯补考，总分不达标需重新参与完整认证）</span>
          </el-col>
        </el-row>

        <el-form-item label="评审结论" prop="conclusion">
          <el-radio-group v-model="scoreForm.conclusion">
            <el-radio label="PASS">通过</el-radio>
            <el-radio label="FAIL">不通过</el-radio>
            <el-radio label="SUPPLEMENT">待补充材料</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="扣分说明">
          <el-input v-model="scoreForm.deductRemark" type="textarea" :rows="3" placeholder="请说明扣分原因及改进建议" />
        </el-form-item>

        <el-form-item label="评委签字">
          <el-input v-model="scoreForm.reviewerSign" placeholder="请输入评委姓名确认签字" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitScore">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, RefreshRight } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const reviewList = ref([]);
const scoreDialogVisible = ref(false);
const scoreFormRef = ref();
const currentRecord = ref<any>({});
const reviewStep = ref(0); // 0-评委一, 1-评委二, 2-部门复核, 3-人力终审

// 筛选表单
const filterForm = reactive({
  reviewStatus: '',
  planId: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 计划选项
const planOptions = ref([
  { id: '1', planName: '2026年度第一批次技能认证' },
  { id: '2', planName: '2025年度第二批次技能认证' }
]);

// 实操考核项目
const practiceItems = ref([
  { itemName: '现场操作规范', maxScore: 25, score: 0, remark: '' },
  { itemName: '工艺流程执行', maxScore: 25, score: 0, remark: '' },
  { itemName: '故障处理能力', maxScore: 25, score: 0, remark: '' },
  { itemName: '质量成果展示', maxScore: 25, score: 0, remark: '' }
]);

// 评分表单
const scoreForm = reactive({
  practiceScores: [] as number[],
  attendanceScore: 0,
  standardScore: 0,
  mentoringScore: 0,
  performanceScore: 0,
  complianceScore: 0,
  conclusion: 'PASS',
  deductRemark: '',
  reviewerSign: ''
});

// 评分规则
const scoreRules = {
  conclusion: [{ required: true, message: '请选择评审结论', trigger: 'change' }],
  reviewerSign: [{ required: true, message: '请签字确认', trigger: 'blur' }]
};

// 计算实操总分（双评委取均值场景，实际由后端计算）
const calculatePracticeTotal = computed(() => {
  return practiceItems.value.reduce((sum, item) => sum + (item.score || 0), 0);
});

// 计算素养总分（5项相加）
const calculateQualityTotal = computed(() => {
  return (
    (scoreForm.attendanceScore || 0) +
    (scoreForm.standardScore || 0) +
    (scoreForm.mentoringScore || 0) +
    (scoreForm.performanceScore || 0) +
    (scoreForm.complianceScore || 0)
  );
});

// 计算最终得分 - 加权算法
// 最终得分 = 理论(40%) + 实操(50%) + 素养(10%)
const calculateFinalScore = computed(() => {
  const theoryWeight = 0.4;
  const practiceWeight = 0.5;
  const qualityWeight = 0.1;

  // 持证免考：外部证书持有者理论成绩自动满分100
  const theoryScore = currentRecord.value.hasExternalCert ? 100 : (currentRecord.value.theoryScore || 0);
  const practiceScore = calculatePracticeTotal.value;
  // 素养取平均分后加权
  const qualityAvgScore = calculateQualityTotal.value / 5;

  return Math.round((theoryScore * theoryWeight + practiceScore * practiceWeight + qualityAvgScore * qualityWeight) * 100) / 100;
});

// 单科最低分校验
const checkSingleScorePass = computed(() => {
  const theoryScore = currentRecord.value.hasExternalCert ? 100 : (currentRecord.value.theoryScore || 0);
  const practiceScore = calculatePracticeTotal.value;
  const qualityAvgScore = calculateQualityTotal.value / 5;
  const level = currentRecord.value.applyLevel;

  // 各等级单科最低分要求
  const singleMinScores: Record<string, { theory: number; practice: number; quality: number }> = {
    L1: { theory: 70, practice: 70, quality: 60 },
    L2: { theory: 70, practice: 70, quality: 60 },
    L3: { theory: 75, practice: 75, quality: 65 },
    L4: { theory: 80, practice: 80, quality: 70 },
    L5: { theory: 80, practice: 80, quality: 70 }
  };

  const minScores = singleMinScores[level] || singleMinScores.L1;
  return {
    theoryPass: theoryScore >= minScores.theory,
    practicePass: practiceScore >= minScores.practice,
    qualityPass: qualityAvgScore >= minScores.quality
  };
});

// 是否合格 - 综合判定
const isPass = computed(() => {
  const score = calculateFinalScore.value;
  const level = currentRecord.value.applyLevel;
  const { theoryPass, practicePass, qualityPass } = checkSingleScorePass.value;

  // 1. 先检查单科是否全部达标
  if (!theoryPass || !practicePass || !qualityPass) {
    return false;
  }

  // 2. 再检查总分是否达标
  if (level === 'L1' || level === 'L2') return score >= 80;
  if (level === 'L3') return score >= 85;
  return score >= 90;
});

// 合格详情
const passDetail = computed(() => {
  const { theoryPass, practicePass, qualityPass } = checkSingleScorePass.value;
  const reasons: string[] = [];
  if (!theoryPass) reasons.push('理论成绩未达单科最低分');
  if (!practicePass) reasons.push('实操成绩未达单科最低分');
  if (!qualityPass) reasons.push('素养成绩未达单科最低分');
  return reasons;
});

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    SCORED: 'primary',
    COMPLETED: 'success'
  };
  return typeMap[status] || 'info';
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadReviewList();
};

// 重置
const handleReset = () => {
  filterForm.reviewStatus = '';
  filterForm.planId = '';
  handleSearch();
};

// 发起补考
const handleRetake = async () => {
  if (!currentRecord.value.id) return;
  // L4/L5不支持单纯补考
  if (['L4', 'L5'].includes(currentRecord.value.applyLevel)) {
    ElMessage.warning('L4/L5高级别不支持单纯补考，总分不达标需重新参与当期完整认证流程');
    return;
  }
  try {
    await axios.post('/api/skill-cert/assessment/retake', { id: currentRecord.value.id });
    ElMessage.success('已发起补考申请，请等待审核安排补考时间');
    loadReviewList();
    scoreDialogVisible.value = false;
  } catch (error) {
    console.error('发起补考失败', error);
    ElMessage.error('发起补考失败');
  }
};

// 行点击
const handleRowClick = (row: any) => {
  currentRecord.value = row;
  // 重置评分表单
  scoreForm.attendanceScore = 85;
  scoreForm.standardScore = 85;
  scoreForm.mentoringScore = 85;
  scoreForm.performanceScore = 85;
  scoreForm.complianceScore = 85;
  scoreForm.conclusion = 'PASS';
  scoreForm.deductRemark = '';
  scoreForm.reviewerSign = '';
  practiceItems.value.forEach(item => {
    item.score = 0;
    item.remark = '';
  });
  scoreDialogVisible.value = true;
};

// 提交评分
const handleSubmitScore = async () => {
  try {
    await scoreFormRef.value.validate();

    if (!scoreForm.reviewerSign) {
      ElMessage.warning('请签字确认');
      return;
    }

    await axios.post('/api/skill-cert/assessment/score', {
      id: currentRecord.value.id,
      practiceScores: practiceItems.value,
      ...scoreForm
    });

    ElMessage.success('评分提交成功');
    scoreDialogVisible.value = false;
    loadReviewList();
  } catch (error) {
    console.error('提交失败', error);
    ElMessage.error('提交失败');
  }
};

// 加载评审列表
const loadReviewList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/assessment/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...filterForm
      }
    });
    const data = res.data?.data || res.data;
    reviewList.value = data.records || data.list || [];
    pagination.total = data.total || 0;
  } catch (error) {
    console.error('加载评审列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadReviewList();
};

const handleCurrentChange = (page: number) => {
  pagination.current = page;
  loadReviewList();
};

onMounted(() => {
  loadReviewList();
});
</script>

<style scoped>
.review-container {
  padding: 0;
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.score-header {
  margin-bottom: 20px;
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

.pass-threshold {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.retake-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.el-divider {
  margin: 16px 0 8px;
}
</style>

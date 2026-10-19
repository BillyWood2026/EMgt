<template>
  <div class="apply-container">
    <!-- 我的申报记录 -->
    <div class="section-header">
      <h3>我的技能申报记录</h3>
      <el-button type="primary" @click="handleNewApply" :disabled="!canApply">
        <el-icon><Plus /></el-icon> 新增申报
      </el-button>
    </div>

    <!-- 申报记录列表 -->
    <el-table :data="applyList" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="planName" label="认证计划" min-width="160" show-overflow-tooltip />
      <el-table-column prop="skillTypeName" label="申报工种" width="120" />
      <el-table-column prop="applyLevel" label="申报等级" width="90" align="center">
        <template #default="{ row }">
          <el-tag type="warning" size="small">{{ row.applyLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentLevel" label="现有等级" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.currentLevel">{{ row.currentLevel }}</span>
          <span v-else class="text-muted">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="workYears" label="从业年限" width="90" align="center" />
      <el-table-column prop="trainHours" label="培训学时" width="90" align="center" />
      <el-table-column prop="applyStatus" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.applyStatus)" size="small">{{ row.applyStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="finalScore" label="最终得分" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.finalScore">{{ row.finalScore }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="result" label="结果" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.result === 'PASS'" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="row.result === 'FAIL'" type="danger" size="small">不通过</el-tag>
          <el-tag v-else-if="row.result === 'SUPPLEMENT'" type="warning" size="small">待补充</el-tag>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申报时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleViewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
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

    <!-- 新增申报弹窗 -->
    <el-dialog v-model="applyDialogVisible" title="技能认证申报" width="800px" :close-on-click-modal="false" destroy-on-close>
      <el-steps :active="applyStep" finish-status="success" align-center style="margin-bottom: 30px;">
        <el-step title="选择计划" />
        <el-step title="填写信息" />
        <el-step title="提交申报" />
      </el-steps>

      <!-- 步骤1：选择认证计划 -->
      <div v-if="applyStep === 0" class="step-content">
        <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
          <template #title>
            <span>请选择您要申报的认证计划。每个员工同一批次只能申报一个工种的一个等级。</span>
          </template>
        </el-alert>
        <el-table :data="availablePlans" border @selection-change="handlePlanSelect">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="planName" label="计划名称" />
          <el-table-column prop="skillTypes" label="开放工种">
            <template #default="{ row }">
              {{ row.skillTypes?.join('、') }}
            </template>
          </el-table-column>
          <el-table-column prop="skillLevels" label="开放等级" width="120">
            <template #default="{ row }">
              <el-tag v-for="level in row.skillLevels" :key="level" size="small" style="margin-right: 4px;">{{ level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applyEndTime" label="申报截止" width="120" />
        </el-table>
      </div>

      <!-- 步骤2：填写申报信息 -->
      <div v-if="applyStep === 1" class="step-content">
        <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="130px">
          <el-divider content-position="left">基本信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="申报工种" prop="skillTypeId">
                <el-select v-model="applyForm.skillTypeId" placeholder="请选择" style="width: 100%;" @change="handleSkillTypeChange">
                  <el-option v-for="type in selectedPlanSkillTypes" :key="type.id" :label="type.skillTypeName" :value="type.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="申报等级" prop="applyLevel">
                <el-select v-model="applyForm.applyLevel" placeholder="请选择" style="width: 100%;" :disabled="!applyForm.skillTypeId" @change="handleLevelChange">
                  <el-option v-for="level in availableLevels" :key="level" :label="`${level} - ${getLevelName(level)}`" :value="level" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="现有技能等级">
                <el-input :value="applyForm.currentLevel || '无'" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="从业年限(年)">
                <el-input-number v-model="applyForm.workYears" :min="0" :max="50" disabled style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="培训学时">
                <el-input-number v-model="applyForm.trainHours" :min="0" :max="500" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="绩效等级">
                <el-select v-model="applyForm.performanceLevel" placeholder="请选择" style="width: 100%;">
                  <el-option label="A（优秀）" value="A" />
                  <el-option label="B（良好）" value="B" />
                  <el-option label="C（合格）" value="C" />
                  <el-option label="D（待改进）" value="D" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 申报条件校验结果 -->
          <el-alert v-if="qualificationResult" :type="qualificationResult.pass ? 'success' : 'error'" :closable="false" style="margin: 16px 0;">
            <template #title>
              <span v-if="qualificationResult.pass">恭喜！您已满足 {{ applyForm.applyLevel }} 的申报条件</span>
              <span v-else>不满足申报条件：{{ qualificationResult.reasons?.join('；') }}</span>
            </template>
          </el-alert>

          <el-divider content-position="left">外部证书（如有）</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="证书类型">
                <el-select v-model="applyForm.externalCertType" placeholder="请选择" clearable style="width: 100%;">
                  <el-option label="国家职业资格证书" value="NATIONAL" />
                  <el-option label="职业技能等级证书" value="SKILL" />
                  <el-option label="行业权威协会认证" value="INDUSTRY" />
                  <el-option label="央企行业专项认证" value="SOE" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证书编号">
                <el-input v-model="applyForm.externalCertNo" placeholder="请输入" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="证书附件">
            <el-upload action="#" :auto-upload="false" :file-list="certFileList" list-type="picture">
              <el-button>上传证书</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 jpg/png/pdf 文件，单文件不超过 5MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-divider content-position="left">项目/成果材料（如有）</el-divider>
          <el-form-item label="项目成果">
            <el-input v-model="applyForm.projectResult" type="textarea" :rows="3" placeholder="请简述您参与的项目、技改成果、带教经历等" />
          </el-form-item>
          <el-form-item label="成果附件">
            <el-upload action="#" :auto-upload="false" :file-list="projectFileList" list-type="picture">
              <el-button>上传附件</el-button>
            </el-upload>
          </el-form-item>

          <el-divider content-position="left">申报说明</el-divider>
          <el-form-item label="个人申报说明">
            <el-input v-model="applyForm.applyRemark" type="textarea" :rows="4" placeholder="请简要说明您的申报理由、工作表现、特长优势等" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：确认提交 -->
      <div v-if="applyStep === 2" class="step-content">
        <el-alert type="success" :closable="false" style="margin-bottom: 20px;">
          <template #title>
            <span>请确认您的申报信息，提交后将进入资格审核阶段。</span>
          </template>
        </el-alert>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="认证计划">{{ applyForm.planName }}</el-descriptions-item>
          <el-descriptions-item label="申报时间">{{ new Date().toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="申报工种">{{ applyForm.skillTypeName }}</el-descriptions-item>
          <el-descriptions-item label="申报等级">{{ applyForm.applyLevel }} - {{ getLevelName(applyForm.applyLevel) }}</el-descriptions-item>
          <el-descriptions-item label="现有等级">{{ applyForm.currentLevel || '无' }}</el-descriptions-item>
          <el-descriptions-item label="从业年限">{{ applyForm.workYears }} 年</el-descriptions-item>
          <el-descriptions-item label="培训学时">{{ applyForm.trainHours }} 学时</el-descriptions-item>
          <el-descriptions-item label="绩效等级">{{ applyForm.performanceLevel }}</el-descriptions-item>
          <el-descriptions-item label="外部证书" :span="2">{{ applyForm.externalCertType ? `${applyForm.externalCertType} - ${applyForm.externalCertNo}` : '无' }}</el-descriptions-item>
          <el-descriptions-item label="项目成果" :span="2">{{ applyForm.projectResult || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button v-if="applyStep > 0" @click="applyStep--">上一步</el-button>
        <el-button v-if="applyStep < 2" type="primary" @click="handleNextStep" :disabled="!canNextStep">下一步</el-button>
        <el-button v-if="applyStep === 2" type="primary" @click="handleSubmitApply" :loading="submitting">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="申报详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="认证计划">{{ currentApply.planName }}</el-descriptions-item>
        <el-descriptions-item label="申报时间">{{ currentApply.createTime }}</el-descriptions-item>
        <el-descriptions-item label="申报工种">{{ currentApply.skillTypeName }}</el-descriptions-item>
        <el-descriptions-item label="申报等级">{{ currentApply.applyLevel }}</el-descriptions-item>
        <el-descriptions-item label="现有等级">{{ currentApply.currentLevel || '无' }}</el-descriptions-item>
        <el-descriptions-item label="从业年限">{{ currentApply.workYears }} 年</el-descriptions-item>
        <el-descriptions-item label="培训学时">{{ currentApply.trainHours }} 学时</el-descriptions-item>
        <el-descriptions-item label="申报状态">
          <el-tag :type="getStatusType(currentApply.applyStatus)">{{ currentApply.applyStatusText }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 考核成绩（如果已有） -->
      <template v-if="currentApply.theoryScore !== undefined">
        <el-divider content-position="left">考核成绩</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="理论考试">{{ currentApply.theoryScore }}分（权重40%）</el-descriptions-item>
          <el-descriptions-item label="实操考核">{{ currentApply.practiceScore }}分（权重50%）</el-descriptions-item>
          <el-descriptions-item label="过程素养">{{ currentApply.qualityScore }}分（权重10%）</el-descriptions-item>
          <el-descriptions-item label="最终得分">{{ currentApply.finalScore }}分</el-descriptions-item>
          <el-descriptions-item label="认证结果" :span="2">
            <el-tag v-if="currentApply.result === 'PASS'" type="success" size="small">通过</el-tag>
            <el-tag v-else-if="currentApply.result === 'FAIL'" type="danger" size="small">不通过</el-tag>
            <el-tag v-else type="warning" size="small">待补充</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const applyList = ref([]);
const applyDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const applyStep = ref(0);
const submitting = ref(false);
const applyFormRef = ref();
const canApply = ref(true);

// 申报步骤表单
const applyForm = reactive({
  planId: '',
  planName: '',
  skillTypeId: '',
  skillTypeName: '',
  applyLevel: '',
  currentLevel: '',
  workYears: 0,
  trainHours: 0,
  performanceLevel: '',
  externalCertType: '',
  externalCertNo: '',
  projectResult: '',
  applyRemark: ''
});

// 条件校验结果
const qualificationResult = ref<{ pass: boolean; reasons?: string[] } | null>(null);

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 可用计划
const availablePlans = ref([
  { id: '1', planName: '2026年度第一批次技能认证', skillTypes: ['数控车工', '焊接工', '钳工'], skillLevels: ['L1', 'L2', 'L3'], applyEndTime: '2026-03-15' },
  { id: '2', planName: '2026年度第二批次技能认证', skillTypes: ['电工', '车工'], skillLevels: ['L3', 'L4'], applyEndTime: '2026-06-30' }
]);

// 选中计划
const selectedPlan = ref<any>(null);

// 已选工种类型
const selectedPlanSkillTypes = computed(() => {
  if (!selectedPlan.value) return [];
  return selectedPlan.value.skillTypes.map((name: string) => ({ id: name, skillTypeName: name }));
});

// 可用等级
const availableLevels = computed(() => {
  return selectedPlan.value?.skillLevels || [];
});

// 文件列表
const certFileList = ref<any[]>([]);
const projectFileList = ref<any[]>([]);

// 申报规则
const applyRules = {
  skillTypeId: [{ required: true, message: '请选择申报工种', trigger: 'change' }],
  applyLevel: [{ required: true, message: '请选择申报等级', trigger: 'change' }]
};

// 获取等级名称
const getLevelName = (level: string) => {
  const names: Record<string, string> = {
    L1: '初级工',
    L2: '中级工',
    L3: '高级工',
    L4: '技师',
    L5: '高级技师'
  };
  return names[level] || level;
};

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    EXAMINED: 'primary',
    PASS: 'success',
    FAIL: 'danger'
  };
  return typeMap[status] || 'info';
};

// 下一步是否可用
const canNextStep = computed(() => {
  if (applyStep.value === 0) {
    return !!selectedPlan.value;
  }
  if (applyStep.value === 1) {
    return !!applyForm.skillTypeId && !!applyForm.applyLevel && (!qualificationResult.value || qualificationResult.value.pass);
  }
  return true;
});

// 选择计划
const handlePlanSelect = (selection: any[]) => {
  if (selection.length > 0) {
    selectedPlan.value = selection[0];
    applyForm.planId = selection[0].id;
    applyForm.planName = selection[0].planName;
  } else {
    selectedPlan.value = null;
    applyForm.planId = '';
    applyForm.planName = '';
  }
};

// 工种变更
const handleSkillTypeChange = () => {
  const type = selectedPlanSkillTypes.value.find((t: any) => t.id === applyForm.skillTypeId);
  applyForm.skillTypeName = type?.skillTypeName || '';
  applyForm.applyLevel = '';
  qualificationResult.value = null;
};

// 等级变更 - 触发申报条件校验
const handleLevelChange = async () => {
  if (!applyForm.applyLevel) {
    qualificationResult.value = null;
    return;
  }

  // 完整申报资格校验逻辑
  const reasons: string[] = [];
  const level = applyForm.applyLevel;
  const currentLevel = applyForm.currentLevel;
  const workYears = applyForm.workYears;
  const trainHours = applyForm.trainHours;
  const hasProjectResult = applyForm.projectResult?.trim().length > 0;

  // 1. 逐级申报校验 - 不可跨级申报
  const levelOrder = ['L1', 'L2', 'L3', 'L4', 'L5'];
  const applyLevelIndex = levelOrder.indexOf(level);
  const currentLevelIndex = currentLevel ? levelOrder.indexOf(currentLevel) : -1;

  if (applyLevelIndex > 0 && currentLevelIndex < 0) {
    // 无等级人员只能申报L1
    if (level !== 'L1') {
      reasons.push(`需逐级申报，当前无前置等级，不可直接申报${level}`);
    }
  } else if (applyLevelIndex - currentLevelIndex > 1) {
    reasons.push(`需逐级申报，不可跨级（当前${currentLevel}，需先获${levelOrder[currentLevelIndex + 1]}）`);
  }

  // 2. 在岗年限校验
  const minWorkYearsMap: Record<string, number> = {
    L1: 0, L2: 1, L3: 2, L4: 3, L5: 3
  };
  const requiredWorkYears = minWorkYearsMap[level] || 0;
  if (workYears < requiredWorkYears) {
    reasons.push(`${level}需在岗满${requiredWorkYears}年，当前${workYears}年`);
  }

  // 3. 培训学时校验
  const minTrainHoursMap: Record<string, number> = {
    L1: 40, L2: 60, L3: 80, L4: 100, L5: 120
  };
  const requiredTrainHours = minTrainHoursMap[level] || 0;
  if (trainHours < requiredTrainHours) {
    reasons.push(`${level}需培训${requiredTrainHours}学时，当前${trainHours}学时`);
  }

  // 4. L4/L5需项目/技改成果材料
  if (['L4', 'L5'].includes(level) && !hasProjectResult) {
    reasons.push(`${level}及以上等级需上传项目/技改/带教成果材料`);
  }

  // 5. 前置等级持有年限校验（L4需L3满3年，L5需L4满3年）
  // 实际项目中应从后端获取证书持有年限，此处模拟
  if (level === 'L4' && currentLevel === 'L3') {
    reasons.push('L4需持有L3证书满3年');
  }
  if (level === 'L5' && currentLevel === 'L4') {
    reasons.push('L5需持有L4证书满3年');
  }

  qualificationResult.value = {
    pass: reasons.length === 0,
    reasons
  };
};

// 新增申报
const handleNewApply = () => {
  applyStep.value = 0;
  selectedPlan.value = null;
  Object.assign(applyForm, {
    planId: '',
    planName: '',
    skillTypeId: '',
    skillTypeName: '',
    applyLevel: '',
    currentLevel: 'L2',
    workYears: 5,
    trainHours: 120,
    performanceLevel: 'B',
    externalCertType: '',
    externalCertNo: '',
    projectResult: '',
    applyRemark: ''
  });
  qualificationResult.value = null;
  applyDialogVisible.value = true;
};

// 下一步
const handleNextStep = () => {
  if (applyStep.value === 0 && !selectedPlan.value) {
    ElMessage.warning('请选择认证计划');
    return;
  }
  if (applyStep.value === 1 && !applyForm.skillTypeId) {
    ElMessage.warning('请选择申报工种和等级');
    return;
  }
  applyStep.value++;
};

// 提交申报
const handleSubmitApply = async () => {
  try {
    submitting.value = true;
    await axios.post('/api/skill-cert/application', applyForm);
    ElMessage.success('申报提交成功，请等待资格审核');
    applyDialogVisible.value = false;
    loadApplyList();
  } catch (error) {
    ElMessage.error('提交失败');
  } finally {
    submitting.value = false;
  }
};

// 查看详情
const handleViewDetail = (row: any) => {
  currentApply.value = row;
  detailDialogVisible.value = true;
};

// 加载申报列表
const loadApplyList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/application/list', {
      params: {
        current: pagination.current,
        size: pagination.size
      }
    });
    const data = res.data?.data || res.data;
    applyList.value = data.records || data.list || [];
    pagination.total = data.total || 0;
  } catch (error) {
    console.error('加载申报列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadApplyList();
};

const handleCurrentChange = (page: number) => {
  pagination.current = page;
  loadApplyList();
};

// 当前详情
const currentApply = ref<any>({});

onMounted(() => {
  loadApplyList();
});
</script>

<style scoped>
.apply-container {
  padding: 0;
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.step-content {
  min-height: 300px;
}

.text-muted {
  color: #909399;
}
</style>

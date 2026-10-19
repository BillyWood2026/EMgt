<template>
  <div class="standard-container">
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="standard-tabs">
      <el-tab-pane label="工种配置" name="skill-types">
        <template #label>
          <el-icon><Setting /></el-icon> 工种配置
        </template>
      </el-tab-pane>
      <el-tab-pane label="等级标准配置" name="level-standards">
        <template #label>
          <el-icon><Medal /></el-icon> 等级标准
        </template>
      </el-tab-pane>
      <el-tab-pane label="考核权重配置" name="score-weights">
        <template #label>
          <el-icon><DataAnalysis /></el-icon> 考核权重
        </template>
      </el-tab-pane>
      <el-tab-pane label="评委组配置" name="reviewer-groups">
        <template #label>
          <el-icon><User /></el-icon> 评委组
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 工种配置 -->
    <div v-if="activeTab === 'skill-types'" class="tab-content">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="filterForm.certType" placeholder="工种分类" clearable style="width: 140px;">
            <el-option label="技术类" value="TECH" />
            <el-option label="操作类" value="OPERATE" />
            <el-option label="专业职能类" value="PROFESSIONAL" />
          </el-select>
          <el-select v-model="filterForm.status" placeholder="状态" clearable style="width: 120px;">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
          <el-select v-model="filterForm.skillLevel" placeholder="技能等级" clearable style="width: 120px;">
            <el-option label="L1 初级工" value="L1" />
            <el-option label="L2 中级工" value="L2" />
            <el-option label="L3 高级工" value="L3" />
            <el-option label="L4 技师" value="L4" />
            <el-option label="L5 高级技师" value="L5" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAddSkillType">
            <el-icon><Plus /></el-icon> 新增工种
          </el-button>
        </div>
      </div>

      <el-table :data="skillTypeList" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="skillTypeName" label="工种名称" min-width="120" />
        <el-table-column prop="skillCategory" label="工种分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ getCategoryText(row.skillCategory) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabledLevels" label="开放等级" width="200">
          <template #default="{ row }">
            <el-tag v-for="level in row.enabledLevels" :key="level" size="small" style="margin-right: 4px;">{{ level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyStatus" label="申报状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.applyStatus === 1 ? 'success' : 'info'" size="small">{{ row.applyStatus === 1 ? '开启' : '关闭' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certValidMonth" label="证书有效期" width="100" align="center">
          <template #default="{ row }">{{ row.certValidMonth }}个月</template>
        </el-table-column>
        <el-table-column prop="warnDays" label="预警天数" width="90" align="center">
          <template #default="{ row }">{{ row.warnDays }}天</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展：是否强制持证（岗位准入硬校验） -->
        <el-table-column label="强制持证" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.forceCert === 1 ? 'danger' : 'info'" size="small">{{ row.forceCert === 1 ? '强制' : '非强制' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEditSkillType(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDeleteSkillType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 等级标准配置 -->
    <div v-if="activeTab === 'level-standards'" class="tab-content">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>以下为各等级的核心能力标准和申报前置条件配置，这些是系统硬编码规则，不可随意修改。如需调整请联系集团管理员。</span>
        </template>
      </el-alert>
      <el-table :data="levelStandards" border stripe>
        <el-table-column prop="level" label="等级" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="等级名称" width="100" align="center" />
        <el-table-column prop="coreAbility" label="核心能力标准" min-width="200" show-overflow-tooltip />
        <el-table-column prop="prerequisites" label="申报前置条件" min-width="180" />
        <el-table-column prop="passScore" label="合格总分" width="90" align="center" />
        <el-table-column prop="passSingleScore" label="单科最低分" width="100" align="center" />
        <el-table-column prop="minWorkYears" label="最低在岗年限" width="110" align="center" />
        <el-table-column prop="trainHours" label="所需培训学时" width="110" align="center" />
      </el-table>
    </div>

    <!-- 考核权重配置 -->
    <div v-if="activeTab === 'score-weights'" class="tab-content">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>考核权重全局统一配置：理论40%、实操50%、素养10%。如需微调请联系集团管理员。</span>
        </template>
      </el-alert>
      <el-form :model="weightForm" label-width="140px" style="max-width: 600px;">
        <el-form-item label="理论成绩权重">
          <el-input-number v-model="weightForm.theoryWeight" :min="0" :max="100" :step="5" disabled style="width: 200px;" />
          <span class="weight-suffix">%</span>
        </el-form-item>
        <el-form-item label="实操成绩权重">
          <el-input-number v-model="weightForm.practiceWeight" :min="0" :max="100" :step="5" disabled style="width: 200px;" />
          <span class="weight-suffix">%</span>
        </el-form-item>
        <el-form-item label="过程素养权重">
          <el-input-number v-model="weightForm.qualityWeight" :min="0" :max="100" :step="5" disabled style="width: 200px;" />
          <span class="weight-suffix">%</span>
        </el-form-item>
        <el-form-item label="权重合计">
          <el-input :value="weightForm.theoryWeight + weightForm.practiceWeight + weightForm.qualityWeight + '%'" disabled />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">各等级合格分数线</el-divider>
      <el-table :data="passScoreTable" border stripe style="max-width: 600px;">
        <el-table-column prop="level" label="等级" width="100" align="center" />
        <el-table-column prop="levelName" label="等级名称" width="120" align="center" />
        <el-table-column prop="passTotalScore" label="总分合格线" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.passTotalScore" :min="0" :max="100" size="small" disabled style="width: 80px;" />
          </template>
        </el-table-column>
        <el-table-column prop="passSingleScore" label="单科最低分" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.passSingleScore" :min="0" :max="100" size="small" disabled style="width: 80px;" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 评委组配置 -->
    <div v-if="activeTab === 'reviewer-groups'" class="tab-content">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="filterForm.orgId" placeholder="所属组织" clearable style="width: 160px;">
            <el-option v-for="org in orgList" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAddReviewerGroup">
            <el-icon><Plus /></el-icon> 新增评委组
          </el-button>
        </div>
      </div>

      <el-table :data="reviewerGroups" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="groupName" label="评委组名称" min-width="150" />
        <el-table-column prop="orgName" label="所属组织" min-width="120" />
        <el-table-column prop="skillTypeName" label="负责工种" width="100" />
        <el-table-column prop="members" label="评委成员" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="(member, index) in row.members" :key="index" size="small" style="margin-right: 4px;">{{ member }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="组长" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEditReviewerGroup(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDeleteReviewerGroup(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑工种弹窗 -->
    <el-dialog v-model="skillTypeDialogVisible" :title="isEditSkillType ? '编辑工种' : '新增工种'" width="600px" destroy-on-close>
      <el-form :model="skillTypeForm" :rules="skillTypeRules" ref="skillTypeFormRef" label-width="120px">
        <el-form-item label="工种名称" prop="skillTypeName">
          <el-input v-model="skillTypeForm.skillTypeName" placeholder="如：数控车工、焊接工" />
        </el-form-item>
        <el-form-item label="工种分类" prop="skillCategory">
          <el-radio-group v-model="skillTypeForm.skillCategory">
            <el-radio label="TECH">技术类</el-radio>
            <el-radio label="OPERATE">操作类</el-radio>
            <el-radio label="PROFESSIONAL">专业职能类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开放等级" prop="enabledLevels">
          <el-checkbox-group v-model="skillTypeForm.enabledLevels">
            <el-checkbox label="L1">L1 初级工</el-checkbox>
            <el-checkbox label="L2">L2 中级工</el-checkbox>
            <el-checkbox label="L3">L3 高级工</el-checkbox>
            <el-checkbox label="L4">L4 技师</el-checkbox>
            <el-checkbox label="L5">L5 高级技师</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="申报状态">
          <el-switch v-model="skillTypeForm.applyStatus" :active-value="1" :inactive-value="0" />
          <span class="status-text">{{ skillTypeForm.applyStatus === 1 ? '开启' : '关闭' }}</span>
        </el-form-item>
        <el-form-item label="证书有效期(月)">
          <el-input-number v-model="skillTypeForm.certValidMonth" :min="12" :max="60" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="预警提前天数">
          <el-input-number v-model="skillTypeForm.warnDays" :min="30" :max="180" style="width: 200px;" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展：是否强制持证（央企中船持证合规：强制工种无证不得上岗） -->
        <el-form-item label="强制持证">
          <el-switch v-model="skillTypeForm.forceCert" :active-value="1" :inactive-value="0" />
          <span class="status-text">{{ skillTypeForm.forceCert === 1 ? '强制（无证禁上岗）' : '非强制' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skillTypeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSkillType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑评委组弹窗 -->
    <el-dialog v-model="reviewerGroupDialogVisible" :title="isEditReviewerGroup ? '编辑评委组' : '新增评委组'" width="600px" destroy-on-close>
      <el-form :model="reviewerGroupForm" :rules="reviewerGroupRules" ref="reviewerGroupFormRef" label-width="120px">
        <el-form-item label="评委组名称" prop="groupName">
          <el-input v-model="reviewerGroupForm.groupName" placeholder="如：制造一部评委组" />
        </el-form-item>
        <el-form-item label="所属组织" prop="orgId">
          <el-select v-model="reviewerGroupForm.orgId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="org in orgList" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责工种" prop="skillTypeId">
          <el-select v-model="reviewerGroupForm.skillTypeId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="type in skillTypeList" :key="type.id" :label="type.skillTypeName" :value="type.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评委成员" prop="members">
          <el-select v-model="reviewerGroupForm.memberIds" multiple placeholder="请选择" style="width: 100%;">
            <el-option v-for="user in userList" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="组长">
          <el-select v-model="reviewerGroupForm.leaderId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="user in userList" :key="user.id" :label="user.name" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="reviewerGroupForm.status" :active-value="1" :inactive-value="0" />
          <span class="status-text">{{ reviewerGroupForm.status === 1 ? '启用' : '停用' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewerGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveReviewerGroup">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus, Setting, Medal, DataAnalysis, User } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const activeTab = ref('skill-types');
const isEditSkillType = ref(false);
const isEditReviewerGroup = ref(false);
const skillTypeDialogVisible = ref(false);
const reviewerGroupDialogVisible = ref(false);
const skillTypeFormRef = ref();
const reviewerGroupFormRef = ref();

// 筛选表单
const filterForm = reactive({
  certType: '',
  status: '',
  skillLevel: '',
  orgId: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 工种列表
const skillTypeList = ref([]);

// 等级标准
const levelStandards = ref([
  { level: 'L1', levelName: '初级工', coreAbility: '掌握基础理论、标准操作流程，可在指导下完成常规基础工作', prerequisites: '转正合格、岗前培训通关、无违纪记录', passScore: '≥80', passSingleScore: '≥70', minWorkYears: '0年', trainHours: '40' },
  { level: 'L2', levelName: '中级工', coreAbility: '熟练掌握本岗位全套流程，可独立完成常规任务，处理一般性故障', prerequisites: '持有L1证书在岗满1年/本工种工龄≥2年', passScore: '≥80', passSingleScore: '≥70', minWorkYears: '1年', trainHours: '60' },
  { level: 'L3', levelName: '高级工', coreAbility: '精通岗位专业技能，可处理复杂问题、质量优化、效率提升，具备带教能力', prerequisites: '持有L2证书在岗满2年/本工种工龄≥4年，近周期绩效达标', passScore: '≥85', passSingleScore: '≥75', minWorkYears: '2年', trainHours: '80' },
  { level: 'L4', levelName: '技师', coreAbility: '具备专项技术攻关、工艺优化、问题复盘、方案设计能力，可承担专项项目', prerequisites: '持有L3证书在岗满3年，具备带教/技改/项目经历', passScore: '≥90', passSingleScore: '≥80', minWorkYears: '3年', trainHours: '100' },
  { level: 'L5', levelName: '高级技师', coreAbility: '掌握体系化技术能力，主导技术革新、标准制定、疑难攻坚、人才培养', prerequisites: '持有L4证书在岗满3年，具备公司级及以上技术成果', passScore: '≥90', passSingleScore: '≥80', minWorkYears: '3年', trainHours: '120' }
]);

// 考核权重
const weightForm = reactive({
  theoryWeight: 40,
  practiceWeight: 50,
  qualityWeight: 10
});

// 合格分数线表
const passScoreTable = ref([
  { level: 'L1', levelName: '初级工', passTotalScore: 80, passSingleScore: 70 },
  { level: 'L2', levelName: '中级工', passTotalScore: 80, passSingleScore: 70 },
  { level: 'L3', levelName: '高级工', passTotalScore: 85, passSingleScore: 75 },
  { level: 'L4', levelName: '技师', passTotalScore: 90, passSingleScore: 80 },
  { level: 'L5', levelName: '高级技师', passTotalScore: 90, passSingleScore: 80 }
]);

// 评委组列表
const reviewerGroups = ref([
  {
    id: '1',
    groupName: '制造一部评委组',
    orgName: '制造一部',
    skillTypeName: '数控车工',
    members: ['王评委', '李评委', '张评委'],
    leaderName: '王评委',
    status: 1
  }
]);

// 组织列表
const orgList = ref([
  { id: '1', name: '制造一部' },
  { id: '2', name: '制造二部' },
  { id: '3', name: '技术中心' }
]);

// 用户列表
const userList = ref([
  { id: '1', name: '王评委' },
  { id: '2', name: '李评委' },
  { id: '3', name: '张评委' }
]);

// 工种表单
const skillTypeForm = reactive({
  id: '',
  skillTypeName: '',
  skillCategory: 'TECH',
  enabledLevels: ['L1', 'L2', 'L3'],
  applyStatus: 1,
  certValidMonth: 36,
  warnDays: 90,
  forceCert: 0 // 规格书字段，待后端扩展
});

// 工种规则
const skillTypeRules = {
  skillTypeName: [{ required: true, message: '请输入工种名称', trigger: 'blur' }],
  skillCategory: [{ required: true, message: '请选择工种分类', trigger: 'change' }],
  enabledLevels: [{ required: true, message: '请选择开放等级', trigger: 'change' }]
};

// 评委组表单
const reviewerGroupForm = reactive({
  id: '',
  groupName: '',
  orgId: '',
  skillTypeId: '',
  memberIds: [] as string[],
  leaderId: '',
  status: 1
});

// 评委组规则
const reviewerGroupRules = {
  groupName: [{ required: true, message: '请输入评委组名称', trigger: 'blur' }],
  orgId: [{ required: true, message: '请选择所属组织', trigger: 'change' }],
  skillTypeId: [{ required: true, message: '请选择负责工种', trigger: 'change' }],
  memberIds: [{ required: true, message: '请选择评委成员', trigger: 'change' }]
};

// 获取分类文本
const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    TECH: '技术类',
    OPERATE: '操作类',
    PROFESSIONAL: '专业职能类'
  };
  return map[category] || category;
};

// 加载工种列表
const loadSkillTypeList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/standard/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        certType: filterForm.certType,
        status: filterForm.status,
        skillLevel: filterForm.skillLevel
      }
    });
    const data = res.data?.data || res.data;
    skillTypeList.value = data.records || data.list || [];
    pagination.total = data.total || 0;
  } catch (error) {
    console.error('加载工种列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadSkillTypeList();
};

// 新增工种
const handleAddSkillType = () => {
  isEditSkillType.value = false;
  Object.assign(skillTypeForm, {
    id: '',
    skillTypeName: '',
    skillCategory: 'TECH',
    enabledLevels: ['L1', 'L2', 'L3'],
    applyStatus: 1,
    certValidMonth: 36,
    warnDays: 90,
    forceCert: 0
  });
  skillTypeDialogVisible.value = true;
};

// 编辑工种
const handleEditSkillType = (row: any) => {
  isEditSkillType.value = true;
  Object.assign(skillTypeForm, row);
  skillTypeDialogVisible.value = true;
};

// 保存工种
const handleSaveSkillType = async () => {
  try {
    await skillTypeFormRef.value.validate();
    if (isEditSkillType.value) {
      await axios.put(`/api/skill-cert/standard/${skillTypeForm.id}`, skillTypeForm);
    } else {
      await axios.post('/api/skill-cert/standard', skillTypeForm);
    }
    ElMessage.success('保存成功');
    skillTypeDialogVisible.value = false;
    loadSkillTypeList();
  } catch (error) {
    console.error('保存失败', error);
    ElMessage.error('保存失败');
  }
};

// 删除工种
const handleDeleteSkillType = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该工种吗？', '删除确认', { type: 'warning' });
    await axios.delete(`/api/skill-cert/standard/${row.id}`);
    ElMessage.success('删除成功');
    loadSkillTypeList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error);
      ElMessage.error('删除失败');
    }
  }
};

// 新增评委组
const handleAddReviewerGroup = () => {
  isEditReviewerGroup.value = false;
  Object.assign(reviewerGroupForm, {
    id: '',
    groupName: '',
    orgId: '',
    skillTypeId: '',
    memberIds: [],
    leaderId: '',
    status: 1
  });
  reviewerGroupDialogVisible.value = true;
};

// 编辑评委组
const handleEditReviewerGroup = (row: any) => {
  isEditReviewerGroup.value = true;
  Object.assign(reviewerGroupForm, row);
  reviewerGroupDialogVisible.value = true;
};

// 保存评委组
const handleSaveReviewerGroup = async () => {
  try {
    await reviewerGroupFormRef.value.validate();
    // await axios.post('/api/hr/skill/reviewer-group/save', reviewerGroupForm);
    ElMessage.success('保存成功');
    reviewerGroupDialogVisible.value = false;
    // 重新加载列表
  } catch (error) {
    console.error('保存失败', error);
  }
};

// 删除评委组
const handleDeleteReviewerGroup = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评委组吗？', '删除确认', { type: 'warning' });
    // await axios.delete(`/api/hr/skill/reviewer-group/${row.id}`);
    ElMessage.success('删除成功');
    // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error);
    }
  }
};

onMounted(() => {
  loadSkillTypeList();
});
</script>

<style scoped>
.standard-container {
  padding: 0;
}

.standard-tabs {
  margin-bottom: 16px;
}

.tab-content {
  min-height: 400px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.weight-suffix {
  margin-left: 8px;
  color: #909399;
}

.status-text {
  margin-left: 8px;
  color: #606266;
}

.el-divider {
  margin: 24px 0 16px;
}
</style>

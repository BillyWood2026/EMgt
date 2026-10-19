<template>
  <div class="plan-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select v-model="filterForm.planStatus" placeholder="计划状态" clearable style="width: 140px;">
          <el-option label="未开始" value="NOT_STARTED" />
          <el-option label="申报中" value="APPLYING" />
          <el-option label="考核中" value="EXAMINING" />
          <el-option label="公示中" value="PUBLIC" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
        <el-select v-model="filterForm.planYear" placeholder="认证年度" clearable style="width: 120px;">
          <el-option v-for="year in yearOptions" :key="year" :label="`${year}年`" :value="year" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshRight /></el-icon> 重置
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon> 新增计划
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </div>
    </div>

    <!-- 计划列表 -->
    <el-table :data="planList" border v-loading="loading" stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="planName" label="计划名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="planYear" label="认证年度" width="90" align="center" />
      <el-table-column prop="skillTypes" label="覆盖工种" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.skillTypes?.join('、') || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="skillLevels" label="覆盖等级" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-for="level in row.skillLevels" :key="level" size="small" style="margin-right: 4px;">{{ level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申报时间" min-width="160">
        <template #default="{ row }">
          {{ row.applyStartTime }} ~ {{ row.applyEndTime }}
        </template>
      </el-table-column>
      <el-table-column prop="examTime" label="考核时间" min-width="120" />
      <el-table-column prop="joinNum" label="参与人数" width="90" align="center" />
      <el-table-column prop="passNum" label="通过人数" width="90" align="center" />
      <el-table-column prop="planStatus" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.planStatus)" size="small">{{ getStatusText(row.planStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="managerName" label="负责人" width="90" align="center" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
          <el-button v-if="row.planStatus === 'NOT_STARTED'" type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.planStatus === 'APPLYING'" type="warning" size="small" link @click="handleTerminate(row)">终止</el-button>
          <el-button v-if="['EXAMINING', 'PUBLIC'].includes(row.planStatus)" type="success" size="small" link @click="handleArchive(row)">归档</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑认证计划' : '新增认证计划'" width="700px" :close-on-click-modal="false">
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="120px">
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="如：2026年度第一批次技能认证" />
        </el-form-item>
        <el-form-item label="认证年度" prop="planYear">
          <el-select v-model="planForm.planYear" placeholder="请选择">
            <el-option v-for="year in yearOptions" :key="year" :label="`${year}年`" :value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="覆盖工种" prop="skillTypeIds">
          <el-select v-model="planForm.skillTypeIds" multiple placeholder="请选择" style="width: 100%;">
            <el-option v-for="type in skillTypeOptions" :key="type.id" :label="type.skillTypeName" :value="type.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="覆盖等级" prop="skillLevels">
          <el-checkbox-group v-model="planForm.skillLevels">
            <el-checkbox label="L1">L1 初级工</el-checkbox>
            <el-checkbox label="L2">L2 中级工</el-checkbox>
            <el-checkbox label="L3">L3 高级工</el-checkbox>
            <el-checkbox label="L4">L4 技师</el-checkbox>
            <el-checkbox label="L5">L5 高级技师</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申报开始时间" prop="applyStartTime">
              <el-date-picker v-model="planForm.applyStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报结束时间" prop="applyEndTime">
              <el-date-picker v-model="planForm.applyEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考核时间" prop="examTime">
          <el-date-picker v-model="planForm.examTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="负责人" prop="managerUserId">
          <el-select v-model="planForm.managerUserId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.userName" :value="user.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="认证计划详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划名称">{{ currentPlan.planName }}</el-descriptions-item>
        <el-descriptions-item label="认证年度">{{ currentPlan.planYear }}</el-descriptions-item>
        <el-descriptions-item label="覆盖工种">{{ currentPlan.skillTypes?.join('、') }}</el-descriptions-item>
        <el-descriptions-item label="覆盖等级">
          <el-tag v-for="level in currentPlan.skillLevels" :key="level" size="small" style="margin-right: 4px;">{{ level }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申报时间">{{ currentPlan.applyStartTime }} ~ {{ currentPlan.applyEndTime }}</el-descriptions-item>
        <el-descriptions-item label="考核时间">{{ currentPlan.examTime }}</el-descriptions-item>
        <el-descriptions-item label="参与人数">{{ currentPlan.joinNum }}</el-descriptions-item>
        <el-descriptions-item label="通过人数">{{ currentPlan.passNum }}</el-descriptions-item>
        <el-descriptions-item label="计划状态">
          <el-tag :type="getStatusType(currentPlan.planStatus)">{{ getStatusText(currentPlan.planStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentPlan.managerName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 申报人员列表 -->
      <el-divider content-position="left">申报人员 ({{ applyList.length }})</el-divider>
      <el-table :data="applyList" border size="small" max-height="300">
        <el-table-column prop="userName" label="姓名" />
        <el-table-column prop="orgName" label="组织" />
        <el-table-column prop="skillType" label="申报工种" />
        <el-table-column prop="applyLevel" label="申报等级" />
        <el-table-column prop="applyStatus" label="状态">
          <template #default="{ row }">
            <el-tag :type="getApplyStatusType(row.applyStatus)" size="small">{{ row.applyStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finalScore" label="最终得分" />
        <el-table-column prop="result" label="结果">
          <template #default="{ row }">
            <el-tag v-if="row.result === 'PASS'" type="success" size="small">通过</el-tag>
            <el-tag v-else-if="row.result === 'FAIL'" type="danger" size="small">不通过</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, RefreshRight, Plus, Download } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const planList = ref([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const isEdit = ref(false);
const planFormRef = ref();
const currentPlan = ref<any>({});
const applyList = ref([]);

// 筛选表单
const filterForm = reactive({
  planStatus: '',
  planYear: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 年份选项
const yearOptions = [2026, 2025, 2024, 2023];

// 工种选项
const skillTypeOptions = ref([
  { id: '1', skillTypeName: '数控车工' },
  { id: '2', skillTypeName: '焊接工' },
  { id: '3', skillTypeName: '电工' },
  { id: '4', skillTypeName: '钳工' },
  { id: '5', skillTypeName: '车工' }
]);

// 用户选项
const userOptions = ref([
  { id: '1', userName: '张三' },
  { id: '2', userName: '李四' }
]);

// 计划表单
const planForm = reactive({
  planName: '',
  planYear: new Date().getFullYear(),
  skillTypeIds: [],
  skillLevels: [],
  applyStartTime: '',
  applyEndTime: '',
  examTime: '',
  managerUserId: ''
});

// 表单校验
const planRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planYear: [{ required: true, message: '请选择认证年度', trigger: 'change' }],
  skillTypeIds: [{ required: true, message: '请选择覆盖工种', trigger: 'change' }],
  skillLevels: [{ required: true, message: '请选择覆盖等级', trigger: 'change' }],
  applyStartTime: [{ required: true, message: '请选择申报开始时间', trigger: 'change' }],
  applyEndTime: [{ required: true, message: '请选择申报结束时间', trigger: 'change' }],
  examTime: [{ required: true, message: '请选择考核时间', trigger: 'change' }],
  managerUserId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
};

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    NOT_STARTED: '未开始',
    APPLYING: '申报中',
    EXAMINING: '考核中',
    PUBLIC: '公示中',
    ARCHIVED: '已归档'
  };
  return statusMap[status] || status;
};

// 获取状态标签类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    NOT_STARTED: 'info',
    APPLYING: 'primary',
    EXAMINING: 'warning',
    PUBLIC: 'success',
    ARCHIVED: 'info'
  };
  return typeMap[status] || 'info';
};

// 获取申报状态标签类型
const getApplyStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    EXAMINED: 'primary'
  };
  return typeMap[status] || 'info';
};

// 加载计划列表
const loadPlanList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/plan/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...filterForm
      }
    });
    const data = res.data?.data || res.data;
    planList.value = data.records || data.list || [];
    pagination.total = data.total || 0;
  } catch (error) {
    console.error('加载计划列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadPlanList();
};

// 重置
const handleReset = () => {
  filterForm.planStatus = '';
  filterForm.planYear = '';
  handleSearch();
};

// 新增
const handleCreate = () => {
  isEdit.value = false;
  Object.assign(planForm, {
    planName: '',
    planYear: new Date().getFullYear(),
    skillTypeIds: [],
    skillLevels: [],
    applyStartTime: '',
    applyEndTime: '',
    examTime: '',
    managerUserId: ''
  });
  dialogVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  isEdit.value = true;
  Object.assign(planForm, row);
  dialogVisible.value = true;
};

// 保存
const handleSave = async () => {
  try {
    await planFormRef.value.validate();
    if (isEdit.value) {
      await axios.put(`/api/skill-cert/plan/${planForm.id}`, planForm);
    } else {
      await axios.post('/api/skill-cert/plan', planForm);
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功');
    dialogVisible.value = false;
    loadPlanList();
  } catch (error) {
    console.error('保存失败', error);
    ElMessage.error('保存失败');
  }
};

// 查看
const handleView = async (row: any) => {
  currentPlan.value = row;
  // 加载申报人员列表
  applyList.value = [
    { userName: '王五', orgName: '制造一部', skillType: '数控车工', applyLevel: 'L2', applyStatus: 'APPROVED', applyStatusText: '已审核', finalScore: 85, result: 'PASS' },
    { userName: '赵六', orgName: '制造二部', skillType: '焊接工', applyLevel: 'L3', applyStatus: 'EXAMINED', applyStatusText: '已考核', finalScore: '-', result: '' }
  ];
  detailVisible.value = true;
};

// 终止
const handleTerminate = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要终止该认证计划吗？终止后将关闭申报入口', '提示', { type: 'warning' });
    await axios.put(`/api/skill-cert/plan/${row.id}/terminate`);
    ElMessage.success('计划已终止');
    loadPlanList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('终止失败', error);
      ElMessage.error('终止失败');
    }
  }
};

// 归档
const handleArchive = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要归档该认证计划吗？归档后将生成最终证书', '提示', { type: 'warning' });
    await axios.put(`/api/skill-cert/plan/${row.id}/archive`);
    ElMessage.success('计划已归档');
    loadPlanList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归档失败', error);
      ElMessage.error('归档失败');
    }
  }
};

// 导出
const handleExport = () => {
  ElMessage.info('正在导出...');
  window.open('/api/skill-cert/plan/export');
};

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadPlanList();
};

const handleCurrentChange = (page: number) => {
  pagination.current = page;
  loadPlanList();
};

onMounted(() => {
  loadPlanList();
});
</script>

<style scoped>
.plan-container {
  padding: 0;
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>

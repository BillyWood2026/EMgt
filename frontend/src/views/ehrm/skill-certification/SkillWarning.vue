<template>
  <div class="warning-container">
    <!-- 预警类型Tab -->
    <el-tabs v-model="activeWarningType" class="warning-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="证书即将到期" name="CERT_EXPIRING">
        <template #label>
          <span>证书即将到期 <el-badge :value="warningCounts.certExpiring" :max="99" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="等级未按期晋级" name="LEVEL_NOT_PROMOTED">
        <template #label>
          <span>等级未按期晋级 <el-badge :value="warningCounts.levelNotPromoted" :max="99" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="认证逾期未参与" name="CERT_OVERDUE">
        <template #label>
          <span>认证逾期未参与 <el-badge :value="warningCounts.certOverdue" :max="99" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="补考未完成" name="RETAKE_INCOMPLETE">
        <template #label>
          <span>补考未完成 <el-badge :value="warningCounts.retakeIncomplete" :max="99" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="资质已失效" name="QUALIFICATION_EXPIRED">
        <template #label>
          <span>资质已失效 <el-badge :value="warningCounts.qualificationExpired" :max="99" type="danger" /></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-select v-model="filterForm.orgId" placeholder="所属组织" clearable style="width: 160px;">
          <el-option v-for="org in orgList" :key="org.id" :label="org.name" :value="org.id" />
        </el-select>
        <el-select v-model="filterForm.skillTypeId" placeholder="技能工种" clearable style="width: 140px;">
          <el-option v-for="type in skillTypeList" :key="type.id" :label="type.name" :value="type.id" />
        </el-select>
        <el-input v-model="filterForm.keyword" placeholder="搜索员工姓名/工号" clearable style="width: 160px;" />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshRight /></el-icon> 重置
        </el-button>
      </div>
      <div class="filter-right">
        <el-button @click="handleBatchProcess" :disabled="selectedWarnings.length === 0">
          <el-icon><Setting /></el-icon> 批量处理
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon> 导出预警
        </el-button>
      </div>
    </div>

    <!-- 预警列表 -->
    <el-table :data="warningList" border stripe v-loading="loading" @selection-change="handleSelectionChange" @row-click="handleRowClick">
      <el-table-column type="selection" width="50" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="warningType" label="预警类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getWarningTypeTag(row.warningType)" size="small">{{ row.warningTypeText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="staffNo" label="工号" width="100" />
      <el-table-column prop="userName" label="姓名" width="90" />
      <el-table-column prop="orgName" label="所属组织" min-width="120" />
      <el-table-column prop="skillTypeName" label="技能工种" width="100" />
      <el-table-column prop="currentLevel" label="当前等级" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.currentLevel" size="small">{{ row.currentLevel }}</el-tag>
          <span v-else class="text-muted">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="targetLevel" label="目标等级" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.targetLevel" class="text-target">{{ row.targetLevel }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="warningDays" label="预警级别" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.warningDays <= 30" type="danger" size="small">紧急</el-tag>
          <el-tag v-else-if="row.warningDays <= 60" type="warning" size="small">预警</el-tag>
          <el-tag v-else type="info" size="small">提醒</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warningContent" label="预警内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="deadline" label="截止日期" width="120" />
      <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.handleStatus)" size="small">{{ row.handleStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handlerName" label="处理人" width="90" align="center" />
      <el-table-column prop="handleTime" label="处理时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleView(row)">详情</el-button>
          <el-button v-if="row.handleStatus === 'PENDING'" type="success" size="small" link @click="handleProcess(row)">处理</el-button>
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
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="processDialogVisible" title="预警处理" width="600px" destroy-on-close>
      <el-alert :type="currentWarning.handleStatus === 'PROCESSED' ? 'success' : 'warning'" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>{{ currentWarning.warningTypeText }}：{{ currentWarning.warningContent }}</span>
        </template>
      </el-alert>

      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="员工姓名">{{ currentWarning.userName }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ currentWarning.orgName }}</el-descriptions-item>
        <el-descriptions-item label="技能工种">{{ currentWarning.skillTypeName }}</el-descriptions-item>
        <el-descriptions-item label="当前等级">{{ currentWarning.currentLevel || '无' }}</el-descriptions-item>
        <el-descriptions-item label="预警时间">{{ currentWarning.createTime }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ currentWarning.deadline }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
        <el-form-item label="处理方式" prop="processType">
          <el-radio-group v-model="processForm.processType">
            <el-radio label="NOTIFY">发送通知</el-radio>
            <el-radio label="CERTIFY">确认认证</el-radio>
            <el-radio label="EXEMPT">申请豁免</el-radio>
            <el-radio label="OTHER">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="processRemark">
          <el-input v-model="processForm.processRemark" type="textarea" :rows="4" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess">提交处理</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="预警详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预警类型">
          <el-tag :type="getWarningTypeTag(currentWarning.warningType)" size="small">{{ currentWarning.warningTypeText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警级别">
          <el-tag v-if="currentWarning.warningDays <= 30" type="danger">紧急</el-tag>
          <el-tag v-else-if="currentWarning.warningDays <= 60" type="warning">预警</el-tag>
          <el-tag v-else type="info">提醒</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ currentWarning.userName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ currentWarning.staffNo }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ currentWarning.orgName }}</el-descriptions-item>
        <el-descriptions-item label="技能工种">{{ currentWarning.skillTypeName }}</el-descriptions-item>
        <el-descriptions-item label="当前等级">{{ currentWarning.currentLevel || '无' }}</el-descriptions-item>
        <el-descriptions-item label="目标等级">{{ currentWarning.targetLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预警时间">{{ currentWarning.createTime }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ currentWarning.deadline }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTag(currentWarning.handleStatus)">{{ currentWarning.handleStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警内容" :span="2">{{ currentWarning.warningContent }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, RefreshRight, Download, Setting } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const warningList = ref([]);
const selectedWarnings = ref<any[]>([]);
const processDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const currentWarning = ref<any>({});
const processFormRef = ref();
const activeWarningType = ref('CERT_EXPIRING');

// 筛选表单
const filterForm = reactive({
  orgId: '',
  skillTypeId: '',
  keyword: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 预警数量统计（来源 /api/skill-cert/ledger/statistics）
const warningCounts = reactive({
  certExpiring: 0,
  levelNotPromoted: 0,
  certOverdue: 0,
  retakeIncomplete: 0,
  qualificationExpired: 0
});

// 组织列表（来源 /api/org-unit/tree，扁平化）
const orgList = ref<any[]>([]);

// 技能工种列表（来源 /api/skill-cert/skill/list）
const skillTypeList = ref<any[]>([]);

// 加载组织列表
const loadOrgList = async () => {
  try {
    const res = await axios.get('/api/org-unit/tree');
    const tree = res.data?.data || res.data || [];
    const flat: any[] = [];
    const walk = (nodes: any[]) => {
      for (const n of nodes || []) {
        if (n && n.id != null) flat.push({ id: String(n.id), name: n.orgName || n.name || '' });
        if (n?.children?.length) walk(n.children);
      }
    };
    walk(tree);
    orgList.value = flat;
  } catch (error) {
    console.error('加载组织列表失败', error);
  }
};

// 加载技能工种列表
const loadSkillTypeList = async () => {
  try {
    const res = await axios.get('/api/skill-cert/skill/list', { params: { current: 1, size: 200 } });
    const data = res.data?.data || res.data;
    const records = data.records || data.list || [];
    skillTypeList.value = records.map((r: any) => ({ id: String(r.id), name: r.skillName || r.name || '' }));
  } catch (error) {
    console.error('加载技能工种列表失败', error);
  }
};

// 加载预警统计
const loadWarningCounts = async () => {
  try {
    const res = await axios.get('/api/skill-cert/ledger/statistics');
    const data = res.data?.data || res.data || {};
    warningCounts.certExpiring = Number(data.expiringCount) || 0;
    warningCounts.certOverdue = Number(data.expiredCount) || 0;
    warningCounts.retakeIncomplete = Number(data.pendingApply) || 0;
  } catch (error) {
    console.error('加载预警统计失败', error);
  }
};

// 处理表单
const processForm = reactive({
  processType: 'NOTIFY',
  processRemark: ''
});

// 处理规则
const processRules = {
  processType: [{ required: true, message: '请选择处理方式', trigger: 'change' }]
};

// 加载预警列表
const loadWarningList = async () => {
  loading.value = true;
  try {
    let apiUrl = '/api/skill-cert/ledger/warning-list';
    if (activeWarningType.value === 'CERT_EXPIRING') {
      apiUrl = '/api/skill-cert/ledger/expiring';
    }
    const res = await axios.get(apiUrl, {
      params: {
        current: pagination.current,
        size: pagination.size,
        warningType: activeWarningType.value,
        ...filterForm
      }
    });
    const data = res.data?.data || res.data;
    warningList.value = data.records || data.list || [];
    pagination.total = data.total || 0;
  } catch (error) {
    console.error('加载预警列表失败', error);
  } finally {
    loading.value = false;
  }
};

// Tab切换
const handleTabChange = () => {
  pagination.current = 1;
  loadWarningList();
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadWarningList();
};

// 重置
const handleReset = () => {
  filterForm.orgId = '';
  filterForm.skillTypeId = '';
  filterForm.keyword = '';
  handleSearch();
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedWarnings.value = selection;
};

// 行点击
const handleRowClick = (row: any) => {
  handleView(row);
};

// 获取预警类型标签
const getWarningTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    CERT_EXPIRING: 'warning',
    LEVEL_NOT_PROMOTED: 'primary',
    CERT_OVERDUE: 'danger',
    RETAKE_INCOMPLETE: 'warning',
    QUALIFICATION_EXPIRED: 'danger'
  };
  return typeMap[type] || 'info';
};

// 获取状态标签
const getStatusTag = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    PROCESSED: 'success',
    EXEMPTED: 'info'
  };
  return typeMap[status] || 'info';
};

// 查看
const handleView = (row: any) => {
  currentWarning.value = row;
  detailDialogVisible.value = true;
};

// 处理
const handleProcess = (row: any) => {
  currentWarning.value = row;
  processForm.processType = 'NOTIFY';
  processForm.processRemark = '';
  processDialogVisible.value = true;
};

// 提交处理
const handleSubmitProcess = async () => {
  try {
    await processFormRef.value.validate();
    await axios.put(`/api/skill-cert/ledger/${currentWarning.value.id}/process`, processForm);
    ElMessage.success('处理成功');
    processDialogVisible.value = false;
    loadWarningList();
  } catch (error) {
    console.error('处理失败', error);
    ElMessage.error('处理失败');
  }
};

// 批量处理
const handleBatchProcess = () => {
  ElMessage.info(`已选中 ${selectedWarnings.value.length} 条预警记录，请稍后处理`);
};

// 导出
const handleExport = () => {
  ElMessage.info('正在导出预警数据...');
};

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadWarningList();
};

const handleCurrentChange = (page: number) => {
  pagination.current = page;
  loadWarningList();
};

onMounted(() => {
  loadWarningList();
  loadOrgList();
  loadSkillTypeList();
  loadWarningCounts();
});
</script>

<style scoped>
.warning-container {
  padding: 0;
}

.warning-tabs {
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-left,
.filter-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #909399;
}

.text-target {
  color: #67c23a;
  font-weight: 600;
}
</style>

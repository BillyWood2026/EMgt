<template>
  <div class="certificate-container">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select v-model="filterForm.certStatus" placeholder="证书状态" clearable style="width: 130px;">
          <el-option label="有效" value="VALID" />
          <el-option label="即将到期" value="EXPIRING" />
          <el-option label="已过期" value="EXPIRED" />
          <el-option label="已作废" value="REVOKED" />
        </el-select>
        <el-select v-model="filterForm.certType" placeholder="证书类型" clearable style="width: 140px;">
          <el-option label="内部认证" value="INTERNAL" />
          <el-option label="社会证书" value="EXTERNAL" />
        </el-select>
        <el-select v-model="filterForm.certLevel" placeholder="证书等级" clearable style="width: 120px;">
          <el-option label="L1 初级工" value="L1" />
          <el-option label="L2 中级工" value="L2" />
          <el-option label="L3 高级工" value="L3" />
          <el-option label="L4 技师" value="L4" />
          <el-option label="L5 高级技师" value="L5" />
        </el-select>
        <el-input v-model="filterForm.keyword" placeholder="搜索证书编号/姓名" clearable style="width: 180px;" />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshRight /></el-icon> 重置
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon> 导出台账
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-item">
        <div class="stat-value">{{ statsData.totalCerts }}</div>
        <div class="stat-label">证书总数</div>
      </div>
      <div class="stat-item valid">
        <div class="stat-value">{{ statsData.validCerts }}</div>
        <div class="stat-label">有效证书</div>
      </div>
      <div class="stat-item expiring">
        <div class="stat-value">{{ statsData.expiringCerts }}</div>
        <div class="stat-label">即将到期</div>
      </div>
      <div class="stat-item expired">
        <div class="stat-value">{{ statsData.expiredCerts }}</div>
        <div class="stat-label">已过期</div>
      </div>
      <div class="stat-item external">
        <div class="stat-value">{{ statsData.externalCerts }}</div>
        <div class="stat-label">社会证书</div>
      </div>
    </div>

    <!-- 证书列表 -->
    <el-table :data="certList" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="certNo" label="证书编号" min-width="150" />
      <el-table-column prop="certName" label="证书名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="certLevel" label="等级" width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.certLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="certType" label="证书类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.certType === 'INTERNAL' ? 'primary' : 'success'" size="small">
            {{ row.certType === 'INTERNAL' ? '内部认证' : '社会证书' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="持证人员" width="90" />
      <el-table-column prop="orgName" label="所属组织" min-width="120" show-overflow-tooltip />
      <el-table-column prop="skillTypeName" label="技能工种" width="100" />
      <el-table-column prop="issueDate" label="发证日期" width="120" />
      <el-table-column prop="validUntil" label="有效期至" width="120" />
      <el-table-column prop="expiringDays" label="剩余天数" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.expiringDays > 0" :class="{ 'text-warning': row.expiringDays <= 30 }">
            {{ row.expiringDays }}天
          </span>
          <span v-else-if="row.expiringDays === 0" class="text-danger">今日到期</span>
          <span v-else class="text-expired">已过期{{ Math.abs(row.expiringDays) }}天</span>
        </template>
      </el-table-column>
      <el-table-column prop="certStatus" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.certStatus)" size="small">{{ row.certStatusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleView(row)">查看</el-button>
          <el-button v-if="row.certStatus === 'VALID'" type="warning" size="small" link @click="handleRevoke(row)">作废</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="证书详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="证书编号">{{ currentCert.certNo }}</el-descriptions-item>
        <el-descriptions-item label="证书名称">{{ currentCert.certName }}</el-descriptions-item>
        <el-descriptions-item label="证书等级">
          <el-tag size="small">{{ currentCert.certLevel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="证书类型">
          <el-tag :type="currentCert.certType === 'INTERNAL' ? 'primary' : 'success'" size="small">
            {{ currentCert.certType === 'INTERNAL' ? '内部认证' : '社会证书' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="持证人员">{{ currentCert.userName }}</el-descriptions-item>
        <el-descriptions-item label="所属组织">{{ currentCert.orgName }}</el-descriptions-item>
        <el-descriptions-item label="技能工种">{{ currentCert.skillTypeName }}</el-descriptions-item>
        <el-descriptions-item label="发证机构">{{ currentCert.issueOrg }}</el-descriptions-item>
        <el-descriptions-item label="发证日期">{{ currentCert.issueDate }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ currentCert.validUntil }}</el-descriptions-item>
        <el-descriptions-item label="证书状态">
          <el-tag :type="getStatusType(currentCert.certStatus)">{{ currentCert.certStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认定类型" :span="2">
          {{ currentCert.recognizeType === 'INTERNAL' ? '内部认证' : '外部证书互认' }}
          <span v-if="currentCert.externalCertNo">（原证书号：{{ currentCert.externalCertNo }}）</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">附件存档</el-divider>
      <div class="attachment-list">
        <el-link v-if="currentCert.attachmentUrl" type="primary" :href="currentCert.attachmentUrl" target="_blank">
          <el-icon><Document /></el-icon> 证书附件.pdf
        </el-link>
        <span v-else class="text-muted">暂无附件</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, RefreshRight, Download, Document } from '@element-plus/icons-vue';
import axios from '@/api/axiosConfig';

const loading = ref(false);
const certList = ref([]);
const detailVisible = ref(false);
const currentCert = ref<any>({});

// 筛选表单
const filterForm = reactive({
  certStatus: '',
  certType: '',
  certLevel: '',
  keyword: ''
});

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
});

// 统计数据
const statsData = reactive({
  totalCerts: 0,
  validCerts: 0,
  expiringCerts: 0,
  expiredCerts: 0,
  externalCerts: 0
});

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    VALID: 'success',
    EXPIRING: 'warning',
    EXPIRED: 'danger',
    REVOKED: 'info'
  };
  return typeMap[status] || 'info';
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadCertList();
};

// 重置
const handleReset = () => {
  filterForm.certStatus = '';
  filterForm.certType = '';
  filterForm.certLevel = '';
  filterForm.keyword = '';
  handleSearch();
};

// 加载证书列表
const loadCertList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/skill-cert/annual-review/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...filterForm
      }
    });
    const data = res.data?.data || res.data;
    certList.value = data.records || data.list || [];
    pagination.total = data.total || 0;

    if (data.stats) {
      Object.assign(statsData, data.stats);
    }
  } catch (error) {
    console.error('加载证书列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 查看详情
const handleView = (row: any) => {
  currentCert.value = row;
  detailVisible.value = true;
};

// 作废/吊销
const handleRevoke = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要吊销该证书吗？吊销后证书将失效', '证书吊销', { type: 'warning' });
    await axios.put(`/api/skill-cert/annual-review/${row.id}/revoke`);
    ElMessage.success('证书已吊销');
    loadCertList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('吊销失败', error);
      ElMessage.error('吊销失败');
    }
  }
};

// 导出
const handleExport = () => {
  ElMessage.info('正在导出证书台账...');
  window.open('/api/skill-cert/annual-review/export');
};

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadCertList();
};

const handleCurrentChange = (page: number) => {
  pagination.current = page;
  loadCertList();
};

onMounted(() => {
  loadCertList();
});
</script>

<style scoped>
.certificate-container {
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

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  padding: 16px;
  background: white;
  border-radius: 8px;
  text-align: center;
  border-left: 4px solid;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.stat-item.valid { border-left-color: #67c23a; }
.stat-item.expiring { border-left-color: #e6a23c; }
.stat-item.expired { border-left-color: #f56c6c; }
.stat-item.external { border-left-color: #409eff; }

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.attachment-list {
  padding: 8px 0;
}

.text-muted {
  color: #909399;
}

.text-warning {
  color: #e6a23c;
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.text-expired {
  color: #f56c6c;
}
</style>

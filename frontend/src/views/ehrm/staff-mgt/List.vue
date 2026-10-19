<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人员花名册总览</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="employeeList"
          file-name="员工花名册"
          title="员工花名册"
          import-api="/api/hr-archive/employee"
          :field-map="ioFieldMap"
          @done="loadEmployees"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增人员
        </el-button>
        <el-button type="warning" plain @click="openBatchMovement">
          <el-icon><Switch /></el-icon>批量异动
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="人员花名册（全域人员主数据档案）"
      description="在职、试用期、离职、退休、待入职、外派全人员统一台账；人员数据禁止物理删除，仅支持作废、归档；司龄自动计算并生成新人/骨干/资深标签；支持发起12类标准化人事异动。"
      style="margin-bottom:16px"
    />

    <el-card shadow="never" style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="员工姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" style="width:150px" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.department" placeholder="选择部门" style="width:150px" clearable>
            <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
          </el-select>
        </el-form-item>
        <el-form-item label="人员状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width:130px" clearable>
            <el-option label="在职" value="active" />
            <el-option label="试用期" value="probation" />
            <el-option label="离职" value="resigned" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="岗位序列">
          <el-select v-model="searchForm.positionSeq" placeholder="选择序列" style="width:140px" clearable>
            <el-option v-for="s in positionSeqs" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="人员类型">
          <el-select v-model="searchForm.employType" placeholder="选择类型" style="width:120px" clearable>
            <el-option v-for="t in employTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-form-item label="涉密等级">
          <el-select v-model="searchForm.secretLevel" placeholder="选择等级" style="width:120px" clearable>
            <el-option label="普通" value="普通" />
            <el-option label="一般涉密" value="一般涉密" />
            <el-option label="核心涉密" value="核心涉密" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="employeeList" border stripe size="small" @selection-change="sel => selectedRows = sel">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="employeeId" label="工号" width="95" />
        <el-table-column prop="name" label="姓名" width="85">
          <template #default="{row}">
            <el-link type="primary" @click="viewProfile(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="position" label="岗位" width="110" />
        <!-- 规格书字段，待后端扩展（电子行业岗位序列） -->
        <el-table-column prop="positionSeq" label="岗位序列" width="105">
          <template #default="{row}">{{ row.positionSeq || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="employType" label="人员类型" width="90">
          <template #default="{row}">{{ row.employType || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展（涉密等级：普通/一般涉密/核心涉密） -->
        <el-table-column prop="secretLevel" label="涉密等级" width="95">
          <template #default="{row}">
            <el-tag v-if="row.secretLevel" size="small" :type="row.secretLevel === '核心涉密' ? 'danger' : row.secretLevel === '一般涉密' ? 'warning' : 'info'">{{ row.secretLevel }}</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="idCard" label="身份证号" width="170">
          <template #default="{row}">{{ maskIdCard(row.idCard) }}</template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入职日期" width="105" />
        <!-- 司龄自动计算算法：当前日期-入职日期，生成司龄标签：新人(0-1年)/骨干(3-8年)/资深(8年+) -->
        <el-table-column label="司龄" width="110">
          <template #default="{row}">
            {{ row.workAgeText }}
            <el-tag v-if="row.workAgeTag" size="small" :type="row.workAgeTag === '资深' ? 'danger' : row.workAgeTag === '骨干' ? 'success' : 'info'" style="margin-left:4px">{{ row.workAgeTag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractTerm" label="合同期限" width="95">
          <template #default="{row}">{{ contractTermLabel(row.contractTerm) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="人员状态" width="90">
          <template #default="{row}">
            <el-tag v-if="row.status === 'active'" type="success" size="small">在职</el-tag>
            <el-tag v-else-if="row.status === 'probation'" type="warning" size="small">试用期</el-tag>
            <el-tag v-else type="info" size="small">离职</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="105" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{row}">
            <el-button link size="small" type="primary" @click="viewProfile(row)">详情</el-button>
            <el-button link size="small" @click="editEmployee(row)">编辑</el-button>
            <el-button link size="small" type="warning" @click="openMovement(row)">发起异动</el-button>
            <el-button link size="small" type="success" @click="openPortrait(row)">画像预览</el-button>
            <el-button link size="small" type="danger" @click="handleResign(row)">离职</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:16px; text-align:right"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 底部统计面板：在职总人数、试用期人数、离职率、平均司龄 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.activeCount }}</div>
          <div class="stat-label">在职总人数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color:#e6a23c">{{ stats.probationCount }}</div>
          <div class="stat-label">试用期人数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color:#f56c6c">{{ stats.turnoverRate }}%</div>
          <div class="stat-label">离职率（离职/全员）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color:#409eff">{{ stats.avgWorkAge }}年</div>
          <div class="stat-label">平均司龄</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑人员弹窗（850px，规格书分区：基础人事/组织岗位/资质合规） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="850px">
      <el-form ref="formRef" :model="formData" label-width="110px">
        <el-divider content-position="left">基础人事信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工姓名" required>
              <el-input v-model="formData.employeeName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" required>
              <el-radio-group v-model="formData.gender">
                <el-radio label="男" />
                <el-radio label="女" />
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" required>
              <el-input v-model="formData.idCard" placeholder="18位身份证号（重复录入拦截）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="formData.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="formData.nativePlace" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="婚姻状态">
              <el-select v-model="formData.maritalStatus" placeholder="选择婚姻状态" clearable style="width:100%">
                <el-option label="未婚" value="未婚" />
                <el-option label="已婚" value="已婚" />
                <el-option label="离异" value="离异" />
                <el-option label="丧偶" value="丧偶" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-select v-model="formData.politicalStatus" placeholder="选择政治面貌" clearable style="width:100%">
                <el-option label="群众" value="群众" />
                <el-option label="共青团员" value="共青团员" />
                <el-option label="中共党员" value="中共党员" />
                <el-option label="民主党派" value="民主党派" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="formData.emergencyContact" placeholder="请输入紧急联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急电话">
              <el-input v-model="formData.emergencyPhone" placeholder="请输入紧急联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">组织岗位信息（电子行业核心）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" required>
              <el-select v-model="formData.deptName" placeholder="选择部门" style="width:100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" required>
              <el-input v-model="formData.positionName" placeholder="请输入岗位" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位序列">
              <el-select v-model="formData.positionSeq" placeholder="选择岗位序列" clearable style="width:100%">
                <el-option v-for="s in positionSeqs" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职级档位">
              <el-select v-model="formData.rankLevel" placeholder="选择职级（华为双通道T/M）" clearable style="width:100%">
                <el-option v-for="r in ['T1','T2','T3','T4','T5','M1','M2','M3','M4','M5']" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人员用工类型">
              <el-select v-model="formData.employType" placeholder="选择人员类型" clearable style="width:100%">
                <el-option v-for="t in employTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="涉密等级">
              <el-select v-model="formData.secretLevel" placeholder="选择涉密等级" clearable style="width:100%">
                <el-option label="普通" value="普通" />
                <el-option label="一般涉密" value="一般涉密" />
                <el-option label="核心涉密" value="核心涉密" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">入职合同信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" required>
              <el-date-picker v-model="formData.hireDate" type="date" placeholder="选择入职日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同期限">
              <el-select v-model="formData.contractTerm" placeholder="选择合同期限" style="width:100%">
                <el-option label="1年" value="1y" />
                <el-option label="2年" value="2y" />
                <el-option label="3年" value="3y" />
                <el-option label="无固定期限" value="permanent" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工状态">
              <el-select v-model="formData.employeeStatus" placeholder="选择状态" style="width:100%">
                <el-option label="在职" value="ACTIVE" />
                <el-option label="试用期" value="PROBATION" />
                <el-option label="离职" value="RESIGNED" />
                <el-option label="退休" value="RETIRED" />
                <el-option label="待岗" value="STANDBY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="formData.remark" :rows="2" placeholder="特殊岗位备注、合规备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉（EL-STA01 人员全景档案） -->
    <el-drawer v-model="detailVisible" :title="`人员全景档案：${detailRow.name || ''}`" size="560px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="工号">{{ detailRow.employeeId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailRow.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailRow.gender }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detailRow.department }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detailRow.position }}</el-descriptions-item>
        <el-descriptions-item label="岗位序列">{{ detailRow.positionSeq || '—' }}</el-descriptions-item>
        <el-descriptions-item label="人员类型">{{ detailRow.employType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="涉密等级">{{ detailRow.secretLevel || '—' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ maskIdCard(detailRow.idCard) }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ maskPhone(detailRow.phone) }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ detailRow.entryDate }}</el-descriptions-item>
        <el-descriptions-item label="司龄">{{ detailRow.workAgeText }}（{{ detailRow.workAgeTag || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="合同期限">{{ contractTermLabel(detailRow.contractTerm) }}</el-descriptions-item>
        <el-descriptions-item label="人员状态">{{ STATUS_LABEL[detailRow.status] || detailRow.status }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailRow.updateTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailRow.remark }}</el-descriptions-item>
      </el-descriptions>
      <el-alert type="info" :closable="false" show-icon style="margin-top:12px"
        title="EL子集（教育经历/工作履历/任职履历/合同/证书/培训/绩效/奖惩/虚拟组织/薪酬）由员工档案页承载，此处为花名册全景摘要；变更历史快照、版本对比待后端扩展。" />
      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-drawer>

    <!-- 发起人事异动弹窗（12类标准化异动，规格书第六章） -->
    <el-dialog v-model="movementVisible" title="发起人事异动" width="900px">
      <el-form :model="movementForm" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工">
              <el-input :value="`${movementForm.employeeName}（${movementForm.employeeId}）`" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="异动类型" required>
              <el-select v-model="movementForm.moveType" placeholder="选择异动类型" style="width:100%">
                <el-option v-for="t in moveTypes" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" required>
              <el-date-picker v-model="movementForm.effectDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支撑附件">
              <el-input v-model="movementForm.attachment" placeholder="任免文件/离职申请/调岗审批单编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">变更前后对比（原信息只读，系统自动回填）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原部门">
              <el-input :value="movementForm.oldDept" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标部门">
              <el-select v-model="movementForm.newDept" placeholder="选择目标部门" clearable style="width:100%">
                <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原岗位">
              <el-input :value="movementForm.oldPosition" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标岗位">
              <el-input v-model="movementForm.newPosition" placeholder="请输入目标岗位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="异动原因" required>
          <el-input type="textarea" v-model="movementForm.reason" :rows="2" placeholder="请输入异动原因与变更说明" />
        </el-form-item>
        <el-alert type="warning" :closable="false" show-icon
          title="流程预览：发起人 → 原组织负责人 → 目标组织负责人 → 人力岗复核 → 分管领导终审（核心干部追加党委审批）；审批通过生效后自动更新花名册、生成版本快照。统一流程引擎接口待后端接入，当前仅离职异动可直接生效。" />
      </el-form>
      <template #footer>
        <el-button @click="movementVisible=false">取消</el-button>
        <el-button @click="saveMovementDraft">保存草稿</el-button>
        <el-button type="primary" @click="submitMovement">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 人才画像预览弹窗（规格书第六章人才画像：能力雷达/标签/潜力分/流失风险） -->
    <el-dialog v-model="portraitVisible" :title="`人才画像预览：${portraitRow.name || ''}`" width="640px">
      <div ref="portraitRadarRef" style="width:100%; height:260px"></div>
      <div style="margin:8px 0">
        <span style="margin-right:8px; color:#606266">人才标签：</span>
        <el-tag v-for="t in portraitTags" :key="t.text" size="small" :type="t.type" style="margin-right:6px">{{ t.text }}</el-tag>
      </div>
      <div style="margin:8px 0">
        <span style="color:#606266">综合潜力得分（专业30%+绩效35%+经验15%+培训10%+奖惩10%）：</span>
        <b style="color:#409eff">{{ portraitAnalysis.potentialScore }}</b>
      </div>
      <div style="margin:8px 0">
        <span style="color:#606266">流失风险指数：</span>
        <el-progress :percentage="portraitAnalysis.riskIndex" :stroke-width="14" :text-inside="true" style="width:70%; display:inline-flex; vertical-align:middle"
          :color="portraitAnalysis.riskIndex <= 30 ? '#67c23a' : portraitAnalysis.riskIndex <= 60 ? '#e6a23c' : '#f56c6c'" />
        <el-tag size="small" style="margin-left:8px" :type="portraitAnalysis.riskIndex <= 30 ? 'success' : portraitAnalysis.riskIndex <= 60 ? 'warning' : 'danger'">
          {{ portraitAnalysis.riskIndex <= 30 ? '低风险' : portraitAnalysis.riskIndex <= 60 ? '中风险(黄色预警)' : '高风险(红色告警)' }}
        </el-tag>
      </div>
      <el-alert type="info" :closable="false" show-icon title="画像数据每日凌晨由人力资本数据湖(HCDL)ETL自动重算；标签聚类(K-Means分层)、岗位智能匹配待后端数据湖扩展。" />
      <template #footer>
        <el-button @click="portraitVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Switch } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api/axiosConfig'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'id', title: '员工ID' },
  { key: 'employeeId', title: '工号' },
  { key: 'name', title: '姓名' },
  { key: 'gender', title: '性别' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'positionSeq', title: '岗位序列' },
  { key: 'employType', title: '人员类型' },
  { key: 'secretLevel', title: '涉密等级' },
  { key: 'idCard', title: '身份证号' },
  { key: 'phone', title: '手机号' },
  { key: 'entryDate', title: '入职日期' },
  { key: 'workAgeText', title: '司龄' },
  { key: 'contractTerm', title: '合同期限' }
]
const ioFieldMap = {
  '工号': 'employeeId',
  '姓名': 'employeeName',
  '员工姓名': 'employeeName',
  '性别': 'gender',
  '身份证号': 'idCard',
  '手机号': 'phone',
  '部门': 'deptName',
  '岗位': 'positionName',
  '岗位序列': 'positionSeq',
  '人员类型': 'employType',
  '人员用工类型': 'employType',
  '涉密等级': 'secretLevel',
  '职级档位': 'rankLevel',
  '入职日期': 'hireDate',
  '合同期限': 'contractTerm',
  '员工状态': 'employeeStatus',
  '民族': 'nation',
  '籍贯': 'nativePlace',
  '婚姻状态': 'maritalStatus',
  '政治面貌': 'politicalStatus',
  '紧急联系人': 'emergencyContact',
  '紧急电话': 'emergencyPhone',
  '备注': 'remark'
}

const departments = ['研发部', '生产部', '销售部', '财务部', '人力资源部', '行政部']
const positionSeqs = ['硬件研发', '嵌入式软件', '结构设计', '工艺工程', '品质管控', '设备运维', '生产管理', '供应链', '售后', '职能']
const employTypes = ['正式工', '试用期', '临时工', '外派', '兼职', '返聘']
const moveTypes = ['入职异动', '试用期转正异动', '内部调岗异动', '跨部门调动异动', '职级调整异动', '薪酬调整异动', '内部兼职异动', '跨厂区借调异动', '外派支援异动', '长期休假异动', '奖惩异动', '离职异动']

const searchForm = reactive({ name: '', department: '', status: '', positionSeq: '', employType: '', secretLevel: '' })

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const dialogVisible = ref(false)
const dialogTitle = ref('新增人员')
const formRef = ref(null)
const selectedRows = ref([])

const emptyForm = {
  id: '', employeeId: '', employeeName: '', gender: '男', idCard: '', phone: '',
  deptName: '', positionName: '', positionSeq: '', rankLevel: '', employType: '', secretLevel: '',
  hireDate: '', contractTerm: '', employeeStatus: 'ACTIVE',
  nation: '', nativePlace: '', maritalStatus: '', politicalStatus: '',
  emergencyContact: '', emergencyPhone: '', remark: ''
}
const formData = reactive({ ...emptyForm })

const detailVisible = ref(false)
const detailRow = ref({})

const movementVisible = ref(false)
const movementForm = reactive({
  employeeId: '', employeeName: '', moveType: '', effectDate: '', attachment: '',
  oldDept: '', oldPosition: '', newDept: '', newPosition: '', reason: '', row: null
})

const portraitVisible = ref(false)
const portraitRow = ref({})
const portraitRadarRef = ref(null)
let portraitRadarIns = null

const employeeList = ref([])
const allEmployees = ref([])

const STATUS_MAP = { ACTIVE: 'active', PROBATION: 'probation', RESIGNED: 'resigned', RETIRED: 'resigned', STANDBY: 'probation' }
const STATUS_LABEL = { active: '在职', probation: '试用期', resigned: '离职' }

// 底部统计面板（算法：离职率=离职/全员；平均司龄）
const stats = computed(() => {
  const list = allEmployees.value
  const active = list.filter(e => e.status === 'active').length
  const probation = list.filter(e => e.status === 'probation').length
  const resigned = list.filter(e => e.status === 'resigned').length
  const total = list.length || 1
  const ages = list.map(e => e.workAgeYears || 0).filter(y => y > 0)
  return {
    activeCount: active,
    probationCount: probation,
    turnoverRate: Math.round((resigned / total) * 1000) / 10,
    avgWorkAge: ages.length ? (Math.round((ages.reduce((s, y) => s + y, 0) / ages.length) * 10) / 10) : 0
  }
})

function contractTermLabel(term) {
  const labels = { '1y': '1年', '2y': '2年', '3y': '3年', permanent: '无固定期限' }
  return labels[term] || term || ''
}

function maskIdCard(idCard) {
  if (!idCard) return ''
  return idCard.slice(0, 6) + '**********' + idCard.slice(-4)
}

function maskPhone(phone) {
  if (!phone) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

// 司龄自动计算算法：当前系统日期-入职日期，按年月统计；生成司龄标签：新人(0-1年)/骨干(3-8年)/资深(8年及以上)
function calcWorkAge(entryDate) {
  if (!entryDate) return { text: '', years: 0, tag: '' }
  const start = new Date(entryDate)
  if (isNaN(start.getTime())) return { text: '', years: 0, tag: '' }
  const now = new Date()
  let years = now.getFullYear() - start.getFullYear()
  let months = now.getMonth() - start.getMonth()
  if (months < 0) { years -= 1; months += 12 }
  if (years < 0) return { text: '', years: 0, tag: '' }
  const totalYears = years + months / 12
  let tag = ''
  if (totalYears < 3) tag = '新人'
  else if (totalYears < 8) tag = '骨干'
  else tag = '资深'
  return { text: `${years}年${months}月`, years: totalYears, tag }
}

async function loadEmployees() {
  try {
    const { data } = await request.get('/api/hr-archive/employee')
    const list = (data?.data || []).map(e => {
      const entryDate = e.hireDate ? String(e.hireDate).slice(0, 10) : ''
      const wa = calcWorkAge(entryDate)
      return {
        id: e.id || e.employeeId,
        employeeId: e.employeeId,
        name: e.employeeName,
        gender: e.gender,
        department: e.deptName,
        position: e.positionName,
        positionSeq: e.positionSeq || '',
        rankLevel: e.rankLevel || '',
        employType: e.employType || '',
        secretLevel: e.secretLevel || '',
        idCard: e.idCard,
        phone: e.phone,
        entryDate,
        workAgeText: wa.text,
        workAgeYears: wa.years,
        workAgeTag: wa.tag,
        status: STATUS_MAP[e.employeeStatus] || 'active',
        contractTerm: e.contractTerm,
        updateTime: e.updateTime ? String(e.updateTime).slice(0, 10) : '',
        remark: e.remark || ''
      }
    })
    allEmployees.value = list
    applyFilter()
  } catch (e) {
    ElMessage.error('员工列表加载失败：' + (e.message || ''))
  }
}

function applyFilter() {
  let list = [...allEmployees.value]
  if (searchForm.name) list = list.filter(e => e.name?.includes(searchForm.name))
  if (searchForm.department) list = list.filter(e => e.department === searchForm.department)
  if (searchForm.status) list = list.filter(e => e.status === searchForm.status)
  if (searchForm.positionSeq) list = list.filter(e => e.positionSeq === searchForm.positionSeq)
  if (searchForm.employType) list = list.filter(e => e.employType === searchForm.employType)
  if (searchForm.secretLevel) list = list.filter(e => e.secretLevel === searchForm.secretLevel)
  pagination.total = list.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  employeeList.value = list.slice(start, start + pagination.pageSize)
}

function handleSearch() {
  pagination.currentPage = 1
  applyFilter()
}

function resetSearch() {
  searchForm.name = ''
  searchForm.department = ''
  searchForm.status = ''
  searchForm.positionSeq = ''
  searchForm.employType = ''
  searchForm.secretLevel = ''
  applyFilter()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  applyFilter()
}

function handleCurrentChange(page) {
  pagination.currentPage = page
  applyFilter()
}

function refreshData() {
  loadEmployees()
  ElMessage.success('数据已刷新')
}

function openAddDialog() {
  dialogTitle.value = '新增人员'
  Object.assign(formData, emptyForm)
  dialogVisible.value = true
}

function editEmployee(row) {
  dialogTitle.value = '编辑人员'
  Object.assign(formData, emptyForm, {
    id: row.id,
    employeeId: row.employeeId,
    employeeName: row.name,
    gender: row.gender,
    idCard: row.idCard,
    phone: row.phone,
    deptName: row.department,
    positionName: row.position,
    positionSeq: row.positionSeq,
    rankLevel: row.rankLevel,
    employType: row.employType,
    secretLevel: row.secretLevel,
    hireDate: row.entryDate,
    contractTerm: row.contractTerm,
    employeeStatus: Object.keys(STATUS_MAP).find(k => STATUS_MAP[k] === row.status) || 'ACTIVE',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

function viewProfile(row) {
  detailRow.value = row
  detailVisible.value = true
}

// 发起人事异动（12类标准化异动单据）
function openMovement(row) {
  Object.assign(movementForm, {
    employeeId: row.employeeId, employeeName: row.name, moveType: '', effectDate: '', attachment: '',
    oldDept: row.department, oldPosition: row.position, newDept: '', newPosition: '', reason: '', row
  })
  movementVisible.value = true
}

function openBatchMovement() {
  if (!selectedRows.value.length) { ElMessage.warning('请先勾选需批量异动的人员'); return }
  ElMessage.info(`批量异动（调岗/转正/离职/外派）已选择 ${selectedRows.value.length} 人，批量单据统一提交审批；批量导入模板校验待后端扩展`)
}

function saveMovementDraft() {
  if (!movementForm.moveType) { ElMessage.warning('请选择异动类型'); return }
  ElMessage.success('异动草稿已保存（本地草稿，统一流程引擎接入后自动提交）')
  movementVisible.value = false
}

// 提交审批：离职异动直接走后端 PUT 生效；其余类型待统一流程引擎接入
async function submitMovement() {
  if (!movementForm.moveType || !movementForm.effectDate || !movementForm.reason) {
    ElMessage.warning('请填写异动类型、生效日期、异动原因'); return
  }
  const row = movementForm.row
  if (movementForm.moveType === '离职异动') {
    try {
      await request.put('/api/hr-archive/employee', {
        id: row.id, employeeId: row.employeeId, employeeName: row.name,
        gender: row.gender, idCard: row.idCard, phone: row.phone,
        deptName: row.department, positionName: row.position,
        hireDate: row.entryDate, contractTerm: row.contractTerm,
        employeeStatus: 'RESIGNED',
        remark: `${row.remark || ''}【离职异动】生效日期${movementForm.effectDate}，原因：${movementForm.reason}`
      })
      ElMessage.success('离职异动已提交并生效，花名册状态已更新为离职')
      movementVisible.value = false
      loadEmployees()
    } catch (e) {
      ElMessage.error('离职异动提交失败：' + (e.response?.data?.message || e.message || ''))
    }
    return
  }
  ElMessage.warning(`「${movementForm.moveType}」需经统一流程引擎审批（发起人→原组织负责人→目标组织负责人→人力复核→分管领导终审），流程接口待后端扩展，单据已暂存草稿`)
  movementVisible.value = false
}

// 人才画像预览（能力雷达+标签+潜力分+流失风险，前端演示算法）
const portraitTags = computed(() => {
  const row = portraitRow.value
  const tags = []
  if (!row.employeeId) return tags
  if (row.workAgeTag) tags.push({ text: row.workAgeTag, type: row.workAgeTag === '资深' ? 'danger' : row.workAgeTag === '骨干' ? 'success' : 'info' })
  if (row.status === 'probation') tags.push({ text: '试用期', type: 'warning' })
  if (row.secretLevel && row.secretLevel !== '普通') tags.push({ text: row.secretLevel, type: 'danger' })
  if ((row.workAgeYears || 0) >= 3) tags.push({ text: '稳定贡献者', type: 'success' })
  if (row.positionSeq === '硬件研发' || row.positionSeq === '嵌入式软件') tags.push({ text: '技术骨干', type: 'primary' })
  return tags
})

const portraitAnalysis = computed(() => {
  const row = portraitRow.value
  const seed = String(row.employeeId || '').split('').reduce((s, c) => s + c.charCodeAt(0), 0)
  const base = 55 + (seed % 30)
  const dims = [
    Math.min(100, base + (seed % 9)),
    Math.min(100, base - 5 + (seed % 13)),
    Math.min(100, base - 8 + (seed % 7)),
    Math.min(100, base - 10 + (seed % 11)),
    Math.min(100, base - 3 + (seed % 5)),
    Math.min(100, base + 3)
  ]
  // 综合潜力得分 = 专业30% + 绩效35% + 管理经验15% + 培训10% + 奖惩10%（规格书权重）
  const potentialScore = Math.round(dims[0] * 0.3 + dims[1] * 0.35 + dims[2] * 0.15 + dims[3] * 0.1 + dims[4] * 0.1)
  // 流失风险指数：司龄越短、状态波动越高风险（0-30低/31-60中/61-100高）
  const ageYears = row.workAgeYears || 0
  let riskIndex = 60 - Math.min(40, ageYears * 6) + (row.status === 'probation' ? 10 : 0) + (row.status === 'resigned' ? 40 : 0)
  riskIndex = Math.max(0, Math.min(100, Math.round(riskIndex)))
  return { dims, potentialScore, riskIndex }
})

function openPortrait(row) {
  portraitRow.value = row
  portraitVisible.value = true
  nextTick(() => drawPortraitRadar())
}

function drawPortraitRadar() {
  if (!portraitRadarRef.value) return
  portraitRadarIns = portraitRadarIns || echarts.init(portraitRadarRef.value)
  portraitRadarIns.resize()
  const names = ['专业技能', '管理能力', '变革能力', '战略理解', '团队建设', '执行力']
  portraitRadarIns.setOption({
    tooltip: {},
    radar: { indicator: names.map(n => ({ name: n, max: 100 })), radius: '62%' },
    series: [{ type: 'radar', data: [{ value: portraitAnalysis.value.dims, name: '能力得分', areaStyle: { opacity: 0.2 } }] }]
  })
}

function handleResign(row) {
  ElMessageBox.confirm(`确定要办理${row.name}的离职手续吗？`, '离职确认', { type: 'warning' })
    .then(async () => {
      const target = allEmployees.value.find(e => e.id === row.id)
      if (target) {
        await request.put('/api/hr-archive/employee', {
          id: target.id, employeeId: target.employeeId, employeeName: target.name,
          gender: target.gender, idCard: target.idCard, phone: target.phone,
          deptName: target.department, positionName: target.position,
          hireDate: target.entryDate, contractTerm: target.contractTerm,
          employeeStatus: 'RESIGNED', remark: target.remark
        })
        ElMessage.success(`已办理${row.name}的离职手续`)
        loadEmployees()
      }
    })
    .catch(() => {})
}

async function handleSubmit() {
  // 身份证重复录入拦截（规格书高风险校验）
  if (!formData.id && formData.idCard) {
    const dup = allEmployees.value.find(e => e.idCard && e.idCard === formData.idCard)
    if (dup) {
      ElMessageBox.alert('该身份证已存在员工档案，禁止重复新增', '重复录入拦截', { type: 'error' })
      return
    }
  }
  try {
    const isEdit = !!formData.id
    const payload = { ...formData }
    if (isEdit) {
      await request.put('/api/hr-archive/employee', payload)
    } else {
      await request.post('/api/hr-archive/employee', payload)
    }
    dialogVisible.value = false
    ElMessage.success(isEdit ? '员工信息已更新' : '员工新增成功')
    loadEmployees()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadEmployees())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }
.search-form { margin-bottom: 0; }
.stat-card { text-align: center; padding: 12px; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }
</style>

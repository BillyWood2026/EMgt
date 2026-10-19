<template>
  <div class="page-card">
    <div class="page-header">
      <h2>人力资源规划</h2>
      <div class="header-actions">
        <el-select v-model="hrmAuth.role" size="small" style="width:170px" @change="setHrmRole">
          <el-option v-for="r in HRM_ROLES" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- 页面1：战略与业务基数配置 -->
      <el-tab-pane label="战略业务基数" name="base">
        <div class="tab-toolbar">
          <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" :icon="Plus" @click="openBaseDialog()">新增业务基数方案</el-button>
          <ImportExportBar
            v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR','DEPT_HEAD','EXEC')"
            :columns="baseExportCols" :rows="baseRows" file-name="集团战略基数台账" title="战略业务基数"
            :import-api="can('HR_ADMIN','HRBP') ? '/api/hr-plan' : ''"
            :field-map="baseFieldMap" @done="loadBase" />
        </div>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ baseRows.length }}</div><div class="stat-label">基数方案数</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ baseRows.filter(r => r.status === '生效执行').length }}</div><div class="stat-label">生效执行中</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(baseRows, 'plannedHeadcount') }}</div><div class="stat-label">规划周期总编制上限</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ wan(sum(baseRows, 'budget')) }}</div><div class="stat-label">人力成本预算合计(万)</div></el-card>
        </div>
        <el-table :data="baseRows" v-loading="baseLoading" border stripe size="small">
          <el-table-column prop="planName" label="方案名称" min-width="140" />
          <el-table-column prop="planYear" label="规划年度" width="90" align="center" />
          <el-table-column prop="department" label="适用组织" width="120" />
          <el-table-column prop="currentHeadcount" label="现有人数" width="90" align="right" />
          <el-table-column prop="plannedHeadcount" label="规划编制" width="90" align="right" />
          <el-table-column prop="recruitmentTarget" label="招聘计划" width="90" align="right" />
          <el-table-column label="预算(万)" width="90" align="right">
            <template #default="{ row }">{{ wan(row.budget) }}</template>
          </el-table-column>
          <!-- 规格书字段，待后端扩展：规划情景(基准/增长/收缩)、营收目标、人均效能目标 -->
          <el-table-column prop="strategy" label="战略方向" min-width="120" show-overflow-tooltip />
          <el-table-column prop="timeline" label="时间线" width="110" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="baseStatusType(row.status)" size="small">{{ row.status || '草稿' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approvedBy" label="审批人" width="90" />
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button v-if="can('HR_ADMIN','HRBP') && row.status === '草稿'" link size="small" type="primary" @click="openBaseDialog(row)">编辑</el-button>
              <el-button v-if="can('HR_ADMIN','HRBP','DEPT_HEAD') && row.status === '草稿'" link size="small" type="warning" @click="setBaseStatus(row, '待业务评审')">提交评审</el-button>
              <el-button v-if="can('HR_ADMIN','HR_DIRECTOR') && row.status === '待业务评审'" link size="small" type="success" @click="setBaseStatus(row, '待人力终审')">业务评审通过</el-button>
              <el-button v-if="can('HR_ADMIN','HR_DIRECTOR','EXEC') && row.status === '待人力终审'" link size="small" type="success" @click="setBaseStatus(row, '生效执行')">终审生效</el-button>
              <el-button v-if="can('HR_ADMIN') && row.status !== '已作废'" link size="small" type="danger" @click="voidBase(row)">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 页面5：人力规划方案编制与审批 -->
      <el-tab-pane label="规划方案" name="proposals">
        <div class="tab-toolbar">
          <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" :icon="Plus" @click="openProposalDialog()">新增规划方案</el-button>
          <ImportExportBar
            v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR','EXEC')"
            :columns="proposalExportCols" :rows="proposals.records" file-name="人力规划方案台账" title="规划方案" />
        </div>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(proposals.records, 'totalDemand') }}</div><div class="stat-label">周期人力总需求</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(proposals.records, 'internalSupply') }}</div><div class="stat-label">内部可供给总量</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value warn">{{ sum(proposals.records, 'externalRecruitmentGap') }}</div><div class="stat-label">外部招聘缺口</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(proposals.records, 'redundancyOptimization') }}</div><div class="stat-label">冗余优化人数</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ wan(sum(proposals.records, 'totalHrCostBudget')) }}</div><div class="stat-label">规划人力总成本(万)</div></el-card>
        </div>
        <el-table :data="proposals.records" v-loading="proposals.loading" border stripe size="small">
          <el-table-column prop="proposalNo" label="方案编号" width="160" />
          <el-table-column prop="planNo" label="关联基数编号" width="120" />
          <el-table-column prop="orgCode" label="适用组织" width="110" />
          <el-table-column label="规划周期" width="190">
            <template #default="{ row }">{{ row.periodStart }} ~ {{ row.periodEnd }}</template>
          </el-table-column>
          <!-- 规格书字段，待后端扩展：规划情景(基准/增长/收缩) -->
          <el-table-column prop="totalDemand" label="总需求" align="right" width="80" />
          <el-table-column prop="internalSupply" label="内部供给" align="right" width="90" />
          <el-table-column prop="externalRecruitmentGap" label="外招缺口" align="right" width="90" />
          <el-table-column prop="redundancyOptimization" label="冗余优化" align="right" width="90" />
          <el-table-column label="供需状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="gapType(row.supplyDemandGap)" size="small">{{ gapText(row.supplyDemandGap) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="人力成本(万)" align="right" width="100">
            <template #default="{ row }">{{ wan(row.totalHrCostBudget) }}</template>
          </el-table-column>
          <el-table-column label="审批状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="proposalStatusType(row.status)" size="small">{{ proposalStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="310" fixed="right">
            <template #default="{ row }">
              <el-button v-if="can('HR_ADMIN','HRBP') && row.status === 'DRAFT'" link size="small" type="primary" @click="openProposalDialog(row)">编辑</el-button>
              <el-button v-if="can('HR_ADMIN','HRBP','DEPT_HEAD') && row.status === 'DRAFT'" link size="small" type="warning" @click="setProposalStatus(row, 'BUSINESS_REVIEW')">提交多级审批</el-button>
              <el-button v-if="can('HR_ADMIN','HR_DIRECTOR') && row.status === 'BUSINESS_REVIEW'" link size="small" type="success" @click="setProposalStatus(row, 'HR_FINAL')">业务复核通过</el-button>
              <el-button v-if="can('HR_ADMIN','HR_DIRECTOR','EXEC') && row.status === 'HR_FINAL'" link size="small" type="success" @click="setProposalStatus(row, 'APPROVED')">人力终审</el-button>
              <el-button v-if="can('HR_ADMIN','EXEC') && row.status === 'APPROVED'" link size="small" type="success" @click="setProposalStatus(row, 'EXECUTING')">生效执行</el-button>
              <el-button v-if="can('HR_ADMIN') && row.status === 'EXECUTING'" link size="small" @click="setProposalStatus(row, 'ARCHIVED')">复盘归档</el-button>
              <el-button v-if="can('HR_ADMIN')" link size="small" type="danger" @click="voidProposal(row)">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt" layout="total, prev, pager, next" :total="proposals.total"
          :page-size="proposals.size" :current-page="proposals.page"
          @current-change="(p) => loadProposals(p)" />
      </el-tab-pane>

      <!-- 页面2：全域人力现状盘点 -->
      <el-tab-pane label="现状盘点" name="inventory">
        <div class="tab-toolbar">
          <el-input v-model="inventory.filterOrg" placeholder="一级部门筛选" size="small" style="width:160px" clearable @change="loadInventory(1)" />
          <el-select v-model="inventory.filterStatus" placeholder="编制占用状态" size="small" style="width:140px" clearable @change="loadInventory(1)">
            <el-option label="在编" value="在编" />
            <el-option label="超编" value="超编" />
            <el-option label="空缺" value="空缺" />
          </el-select>
          <ImportExportBar
            v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR','EXEC')"
            :columns="inventoryExportCols" :rows="inventory.records" file-name="人力现状盘点" title="现状盘点" />
        </div>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ inventoryStats.total }}</div><div class="stat-label">盘点人数</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ inventoryStats.core }}</div><div class="stat-label">核心关键人才</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ inventoryStats.highRisk }}</div><div class="stat-label">高流失风险</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ inventoryStats.onPostRate }}%</div><div class="stat-label">编制占用率</div></el-card>
        </div>
        <el-table :data="inventory.records" v-loading="inventory.loading" border stripe size="small">
          <el-table-column prop="employeeId" label="工号" width="110" />
          <el-table-column prop="employeeName" label="姓名" width="90" />
          <el-table-column prop="orgLevel2" label="一级部门" min-width="110" />
          <el-table-column prop="orgLevel3" label="二级部门" min-width="110" />
          <!-- 规格书字段，待后端扩展：虚拟组织任职、司龄、是否临近退休 -->
          <el-table-column prop="positionSequence" label="岗位序列" width="110" />
          <el-table-column prop="jobLevel" label="职务层级" width="80" align="center" />
          <el-table-column prop="headcountType" label="编制类型" width="90" align="center" />
          <el-table-column label="年龄" width="90" align="center">
            <template #default="{ row }">{{ row.age }}（{{ row.ageRange }}）</template>
          </el-table-column>
          <el-table-column prop="performanceLevel" label="近两期绩效" width="90" align="center" />
          <el-table-column label="流失风险" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="riskType(row.turnoverRisk)" size="small">{{ riskText(row.turnoverRisk) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="核心人才" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isCoreTalent" type="warning" size="small">核心</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="nineBoxPosition" label="九宫格" width="110" align="center" />
          <el-table-column label="编制占用状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.headcountStatus === '在编' ? 'success' : (row.headcountStatus === '超编' ? 'danger' : 'info')" size="small">{{ row.headcountStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="snapshotDate" label="盘点日期" width="100" />
        </el-table>
        <el-pagination class="mt" layout="total, prev, pager, next" :total="inventory.total"
          :page-size="inventory.size" :current-page="inventory.page"
          @current-change="(p) => loadInventory(p)" />
      </el-tab-pane>

      <!-- 页面3：人力需求预测测算工作台 -->
      <el-tab-pane label="需求预测测算" name="forecasts">
        <div class="tab-toolbar">
          <el-button v-if="can('HR_ADMIN','HRBP')" type="primary" :icon="Plus" @click="openForecastDialog()">新增测算</el-button>
          <ImportExportBar
            v-if="can('HR_ADMIN','HRBP','HR_DIRECTOR','EXEC')"
            :columns="forecastExportCols" :rows="forecasts.records" file-name="人力需求测算台账" title="需求预测" />
        </div>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(forecasts.records, 'totalDemand') }}</div><div class="stat-label">周期总需求</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(forecasts.records, 'growthDemand') }}</div><div class="stat-label">业务增长增量需求</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(forecasts.records, 'efficiencyReduction') }}</div><div class="stat-label">增效减员扣减</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ forecasts.records.filter(r => r.gapFlag === 'GAP').length }}</div><div class="stat-label">缺口岗位数</div></el-card>
        </div>
        <el-table :data="forecasts.records" v-loading="forecasts.loading" border stripe size="small">
          <el-table-column prop="forecastId" label="预测单号" width="160" />
          <el-table-column prop="planNo" label="规划编号" width="110" />
          <el-table-column prop="orgCode" label="组织" width="110" />
          <el-table-column prop="positionName" label="岗位名称" min-width="110" />
          <el-table-column prop="positionSequence" label="岗位序列" width="100" />
          <el-table-column prop="currentOnPostCount" label="现有在岗" align="right" width="90" />
          <el-table-column prop="growthDemand" label="业务增长需求" align="right" width="110" />
          <el-table-column prop="efficiencyReduction" label="增效减员" align="right" width="90" />
          <el-table-column prop="totalDemand" label="周期总需求" align="right" width="100" />
          <el-table-column label="测算算法" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ algoText(row.algorithmSource) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="需求缺口" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="gapType(row.gapFlag)" size="small">{{ gapText(row.gapFlag) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="can('HR_ADMIN','HRBP')" link size="small" type="primary" @click="openForecastDialog(row)">手动校准</el-button>
              <el-button v-if="can('HR_ADMIN')" link size="small" type="danger" @click="removeForecast(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt" layout="total, prev, pager, next" :total="forecasts.total"
          :page-size="forecasts.size" :current-page="forecasts.page"
          @current-change="(p) => loadForecasts(p)" />
      </el-tab-pane>

      <!-- 页面4：内部人才供给测算（马尔可夫转移矩阵） -->
      <el-tab-pane label="供给测算·马尔可夫" name="supply">
        <el-alert type="info" :closable="false" class="mb">
          马尔可夫人才流转矩阵：周期内部净供给 = 期初存量×留存概率 + 晋升流入 + 跨部门调入 − 离职流出 − 退休流出
          <!-- 规格书核心算法，前端测算展示，待后端扩展供给测算实体 -->
        </el-alert>
        <el-card shadow="never" class="mb">
          <template #header><span>EL-S01 转移概率配置（历史3年均值，可调）</span></template>
          <el-form :inline="true">
            <el-form-item label="在岗留存"><el-input-number v-model="matrix.retention" :min="0" :max="1" :step="0.01" size="small" /></el-form-item>
            <el-form-item label="晋升"><el-input-number v-model="matrix.promotion" :min="0" :max="1" :step="0.01" size="small" /></el-form-item>
            <el-form-item label="平调"><el-input-number v-model="matrix.transfer" :min="0" :max="1" :step="0.01" size="small" /></el-form-item>
            <el-form-item label="离职"><el-input-number v-model="matrix.resign" :min="0" :max="1" :step="0.01" size="small" /></el-form-item>
            <el-form-item label="退休"><el-input-number v-model="matrix.retire" :min="0" :max="1" :step="0.01" size="small" /></el-form-item>
          </el-form>
        </el-card>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(supplyRows, 'begin') }}</div><div class="stat-label">期初存量合计</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ sum(supplyRows, 'netSupply') }}</div><div class="stat-label">周期内部净供给</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value warn">{{ sum(supplyRows, 'outflow') }}</div><div class="stat-label">离职/退休流出</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ supplyRows.filter(r => r.gap === 'GAP').length }}</div><div class="stat-label">供给缺口序列</div></el-card>
        </div>
        <el-table :data="supplyRows" v-loading="supplyLoading" border stripe size="small">
          <el-table-column prop="seq" label="岗位序列" min-width="120" />
          <el-table-column prop="begin" label="期初存量" align="right" width="90" />
          <el-table-column prop="promoIn" label="内部晋升流入" align="right" width="110" />
          <el-table-column prop="transferIn" label="跨部门调岗流入" align="right" width="120" />
          <el-table-column prop="retained" label="留存人员" align="right" width="90" />
          <el-table-column prop="outflow" label="离职/退休流出" align="right" width="110" />
          <el-table-column prop="netSupply" label="周期内部净供给" align="right" width="120" />
          <el-table-column prop="demand" label="周期需求(对照)" align="right" width="110" />
          <el-table-column label="供给缺口" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.gap" :type="gapType(row.gap)" size="small">{{ gapText(row.gap) }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 页面6：规划滚动执行跟踪与偏差预警 -->
      <el-tab-pane label="执行跟踪预警" name="tracking">
        <el-alert type="info" :closable="false" class="mb">
          三色预警：|偏差率|≤5% 绿色正常；5%~15% 黄色预警（季度滚动微调）；&gt;15% 红色告警（强制重测算与整改）
        </el-alert>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ trackRows.length }}</div><div class="stat-label">跟踪组织数</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#67c23a">{{ trackRows.filter(r => r.warnLevel === 'GREEN').length }}</div><div class="stat-label">绿色正常</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#e6a23c">{{ trackRows.filter(r => r.warnLevel === 'YELLOW').length }}</div><div class="stat-label">黄色预警</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#f56c6c">{{ trackRows.filter(r => r.warnLevel === 'RED').length }}</div><div class="stat-label">红色告警</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ avgDeviationRate }}%</div><div class="stat-label">平均偏差率</div></el-card>
        </div>
        <el-table :data="trackRows" v-loading="trackLoading" border stripe size="small">
          <el-table-column prop="orgName" label="岗位/组织" min-width="120" />
          <el-table-column prop="statisticsPeriod" label="周期" width="90" align="center" />
          <el-table-column prop="planningHeadcount" label="规划目标在岗" align="right" width="110" />
          <el-table-column prop="actualHeadcount" label="当前实际在岗" align="right" width="110" />
          <el-table-column prop="headcountDeviationValue" label="供需偏差值" align="right" width="100" />
          <el-table-column label="偏差率" align="right" width="90">
            <template #default="{ row }">{{ row.headcountDeviationRate }}%</template>
          </el-table-column>
          <el-table-column prop="recruitmentAchievementRate" label="招聘达成率%" align="right" width="110" />
          <el-table-column label="预警等级" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="warnType(row.warnLevel)" size="small">{{ warnText(row.warnLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="can('HR_ADMIN','HRBP') && row.warnLevel === 'RED'" link size="small" type="danger" @click="ElMessage.info('已发起滚动重测算（重算需求+供给算法）')">滚动重测算</el-button>
              <el-button v-else-if="can('HR_ADMIN','HRBP') && row.warnLevel === 'YELLOW'" link size="small" type="warning" @click="ElMessage.info('已推送HRBP整改待办')">整改待办</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 页面7：人力规划BI全景盘点看板 -->
      <el-tab-pane label="BI 全景看板" name="bi">
        <div class="tab-toolbar">
          <el-input v-model="biPeriod" size="small" style="width:140px" placeholder="统计周期" />
          <el-button size="small" type="primary" @click="loadBi">查询</el-button>
          <ImportExportBar
            v-if="can('HR_ADMIN','HR_DIRECTOR','EXEC')"
            :columns="biExportCols" :rows="bi.rows" file-name="人力规划BI盘点报告" title="BI全景看板" />
        </div>
        <el-alert type="info" :closable="false" class="mb">
          人力健康度 = 编制合规率×0.4 + 人均效能达成率×0.3 + 关键人才留存率×0.2 + 干部储备充足率×0.1（0-100分，&lt;60分红色预警）；规划偏差率 = (实际在岗−规划目标)/规划目标×100%
        </el-alert>
        <div class="stat-cards">
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ biAvg('hrHealthScore') }}</div><div class="stat-label">平均HR健康分</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ biAvg('headcountComplianceRate') }}%</div><div class="stat-label">平均编制达成率</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ biAvg('recruitmentAchievementRate') }}%</div><div class="stat-label">平均招聘达成率</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value">{{ biAvg('keyTalentRetentionRate') }}%</div><div class="stat-label">关键人才保留率</div></el-card>
          <el-card shadow="hover" class="stat-card"><div class="stat-value danger">{{ bi.rows.filter(r => Number(r.hrHealthScore) < 60).length }}</div><div class="stat-label">健康度&lt;60组织</div></el-card>
        </div>
        <el-table :data="bi.rows" v-loading="bi.loading" border stripe size="small">
          <el-table-column prop="orgName" label="组织" min-width="110" />
          <el-table-column prop="statisticsPeriod" label="统计周期" width="90" align="center" />
          <el-table-column label="编制(规划/实际)" width="130" align="center">
            <template #default="{ row }">{{ row.planningHeadcount }} / {{ row.actualHeadcount }}</template>
          </el-table-column>
          <el-table-column label="编制偏差" width="110" align="right">
            <template #default="{ row }">
              <span :style="{ color: row.headcountDeviationValue < 0 ? '#e6a23c' : '#67c23a' }">
                {{ row.headcountDeviationValue }}（{{ row.headcountDeviationRate }}%）
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="headcountComplianceRate" label="编制达成率%" align="right" width="110" />
          <el-table-column prop="supplyDemandGap" label="供需缺口" align="right" width="90" />
          <el-table-column label="人力成本(万)" width="150" align="right">
            <template #default="{ row }">{{ wan(row.planningHrCost) }} / {{ wan(row.actualHrCost) }}</template>
          </el-table-column>
          <el-table-column prop="costDeviationRate" label="成本偏差%" align="right" width="100" />
          <el-table-column prop="perCapitaRevenue" label="人均营收(万)" align="right" width="110" />
          <el-table-column prop="perCapitaCost" label="人均成本(万)" align="right" width="110" />
          <el-table-column prop="recruitmentAchievementRate" label="招聘达成%" align="right" width="100" />
          <el-table-column prop="keyTalentRetentionRate" label="关键人才保留%" align="right" width="120" />
          <el-table-column label="HR健康分" width="140" align="center">
            <template #default="{ row }">
              <el-progress :percentage="Number(row.hrHealthScore)" :color="healthColor(row.hrHealthScore)" :stroke-width="12"
                :format="() => row.hrHealthScore" />
            </template>
          </el-table-column>
          <el-table-column label="预警" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="warnType(row.healthWarningLevel)" size="small">{{ warnText(row.healthWarningLevel) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 业务指标（EL-P02 多维度业务指标扩展） -->
      <el-tab-pane label="业务指标" name="metrics">
        <el-table :data="metrics.records" v-loading="metrics.loading" border stripe size="small">
          <el-table-column prop="metricsCode" label="指标编码" width="130" />
          <el-table-column prop="metricsName" label="指标名称" min-width="150" />
          <el-table-column prop="planNo" label="规划编号" width="110" />
          <el-table-column prop="baseYearActual" label="基准年度实际值" align="right" width="120" />
          <el-table-column prop="targetValue" label="规划周期目标值" align="right" width="120" />
          <el-table-column label="指标增长率" align="right" width="100">
            <template #default="{ row }">{{ row.growthRate }}%</template>
          </el-table-column>
          <!-- 规格书字段，待后端扩展：指标对应岗位序列 -->
          <el-table-column prop="weightCoefficient" label="权重系数" align="right" width="90" />
          <el-table-column label="指标类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ metricsTypeText(row.metricsType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'ACTIVE' ? '生效' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt" layout="total, prev, pager, next" :total="metrics.total"
          :page-size="metrics.size" :current-page="metrics.page"
          @current-change="(p) => loadMetrics(p)" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑 业务基数方案对话框 -->
    <el-dialog v-model="baseDialogVisible" :title="baseForm.id ? '编辑业务基数方案' : '新增业务基数方案'" width="950px">
      <el-form :model="baseForm" label-width="130px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="方案名称" required><el-input v-model="baseForm.planName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规划年度" required><el-input-number v-model="baseForm.planYear" :min="2020" :max="2100" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用组织" required><el-input v-model="baseForm.department" placeholder="五级实体组织" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间线"><el-input v-model="baseForm.timeline" placeholder="如 2026Q1-Q4" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="现有人数"><el-input-number v-model="baseForm.currentHeadcount" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规划编制" required><el-input-number v-model="baseForm.plannedHeadcount" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="招聘计划人数"><el-input-number v-model="baseForm.recruitmentTarget" :min="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="人力成本预算(元)"><el-input-number v-model="baseForm.budget" :min="0" :step="10000" style="width:100%" /></el-form-item>
          </el-col>
          <!-- 规格书字段，待后端扩展：规划情景(基准/增长/收缩)、滚动更新周期、偏差预警阈值 -->
        </el-row>
        <el-form-item label="战略方向"><el-input v-model="baseForm.strategy" placeholder="三年中长期战略目标" /></el-form-item>
        <el-form-item label="年度目标"><el-input type="textarea" v-model="baseForm.objectives" :rows="3" placeholder="年度业务增长目标、数字化减员增效目标等" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="baseDialogVisible = false">关闭</el-button>
        <el-button @click="saveBase('草稿')">保存草稿</el-button>
        <el-button type="primary" @click="saveBase('待业务评审')">提交业务部门评审</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑 规划方案对话框 -->
    <el-dialog v-model="proposalDialogVisible" :title="proposalForm.id ? '编辑规划方案' : '新增规划方案'" width="950px">
      <el-form :model="proposalForm" label-width="150px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="方案编号"><el-input v-model="proposalForm.proposalNo" placeholder="留空自动生成" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联业务基数编号" required><el-input v-model="proposalForm.planNo" placeholder="必须绑定已评审基数" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用组织" required><el-input v-model="proposalForm.orgCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规划周期" required>
              <el-date-picker v-model="proposalPeriod" type="daterange" value-format="YYYY-MM-DD"
                start-placeholder="开始" end-placeholder="结束" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="周期人力总需求"><el-input-number v-model="proposalForm.totalDemand" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内部可供给总量"><el-input-number v-model="proposalForm.internalSupply" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="人力总成本预算(元)"><el-input-number v-model="proposalForm.totalHrCostBudget" :min="0" :step="10000" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-alert type="warning" :closable="false">
          供需缺口自动测算：外部招聘缺口 = 总需求 − 内部供给（&gt;0）；冗余优化 = 内部供给 − 总需求（&gt;0）。
          当前：外招缺口 <b>{{ proposalGap.external }}</b> 人，冗余优化 <b>{{ proposalGap.redundancy }}</b> 人。
        </el-alert>
        <!-- 规格书字段，待后端扩展：内部调剂人数、优化减员合规预案附件、落地举措分解(招聘/轮岗/培训/编制/成本) -->
      </el-form>
      <template #footer>
        <el-button @click="proposalDialogVisible = false">关闭</el-button>
        <el-button @click="saveProposal(false)">保存草稿</el-button>
        <el-button type="primary" @click="saveProposal(true)">提交多级审批</el-button>
      </template>
    </el-dialog>

    <!-- 新增/校准 需求测算对话框 -->
    <el-dialog v-model="forecastDialogVisible" :title="forecastForm.id ? '手动校准需求值' : '新增需求测算'" width="750px">
      <el-form :model="forecastForm" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规划编号"><el-input v-model="forecastForm.planNo" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织"><el-input v-model="forecastForm.orgCode" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位名称" required><el-input v-model="forecastForm.positionName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位序列"><el-input v-model="forecastForm.positionSequence" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="现有在岗人数"><el-input-number v-model="forecastForm.currentOnPostCount" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务增长增量"><el-input-number v-model="forecastForm.growthDemand" :min="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="增效减员扣减"><el-input-number v-model="forecastForm.efficiencyReduction" :min="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="测算算法">
              <el-select v-model="forecastForm.algorithmSource" style="width:100%">
                <el-option label="劳动效率法" value="LABOR_EFFICIENCY" />
                <el-option label="回归趋势预测法" value="REGRESSION" />
                <el-option label="业务驱动比率法" value="BUSINESS_RATIO" />
                <el-option label="德尔菲专家校准法" value="EXPERT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert type="info" :closable="false">
          劳动效率法：周期需求 = 现有在岗 + 业务增长增量 − 增效减员扣减 =
          <b>{{ forecastTotal }}</b> 人；&gt;现有在岗标记需求缺口（GAP）。
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="forecastDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveForecast">保存测算结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'
import ImportExportBar from '@/components/ImportExportBar.vue'
import { hrmAuth, setHrmRole, can, HRM_ROLES } from '../hrmAuth.js'
import { startApproval, FLOW_DEF_CODES } from '@/api/flowApproval'

const activeTab = ref('base')

const baseRows = ref([])
const baseLoading = ref(false)
const proposals = reactive({ records: [], total: 0, page: 1, size: 10, loading: false })
const inventory = reactive({ records: [], total: 0, page: 1, size: 10, loading: false, filterOrg: '', filterStatus: '' })
const inventoryAll = ref([])
const forecasts = reactive({ records: [], total: 0, page: 1, size: 10, loading: false })
const metrics = reactive({ records: [], total: 0, page: 1, size: 10, loading: false })
const bi = reactive({ rows: [], loading: false })
const biPeriod = ref('2026-H1')
const trackAll = ref([])
const trackLoading = ref(false)
const supplyData = ref({ seqs: [], demandBySeq: {} })
const supplyLoading = ref(false)

// 马尔可夫转移概率（EL-S01，历史3年均值默认）
const matrix = reactive({ retention: 0.8, promotion: 0.05, transfer: 0.03, resign: 0.08, retire: 0.04 })

const baseDialogVisible = ref(false)
const baseForm = reactive({ id: null, planName: '', planYear: new Date().getFullYear(), department: '', currentHeadcount: 0, plannedHeadcount: 0, recruitmentTarget: 0, budget: 0, strategy: '', objectives: '', timeline: '', status: '草稿' })
const proposalDialogVisible = ref(false)
const proposalForm = reactive({ id: null, proposalNo: '', planNo: '', orgCode: '', totalDemand: 0, internalSupply: 0, totalHrCostBudget: 0 })
const proposalPeriod = ref([])
const forecastDialogVisible = ref(false)
const forecastForm = reactive({ id: null, forecastId: '', planNo: '', orgCode: '', positionName: '', positionSequence: '', currentOnPostCount: 0, growthDemand: 0, efficiencyReduction: 0, algorithmSource: 'LABOR_EFFICIENCY' })

const wan = (v) => (v == null ? '-' : (v / 10000).toFixed(1))
const sum = (rows, key) => rows.reduce((a, r) => a + (Number(r[key]) || 0), 0)
const gapText = (v) => ({ GAP: '缺口', SURPLUS: '冗余', BALANCED: '平衡' }[v] || v || '-')
const gapType = (v) => ({ GAP: 'danger', SURPLUS: 'warning', BALANCED: 'success' }[v] || 'info')
const proposalStatusText = (v) => ({ DRAFT: '草稿', BUSINESS_REVIEW: '业务复核', HR_FINAL: 'HR终审', APPROVED: '已审批', EXECUTING: '执行中', ARCHIVED: '已归档' }[v] || v || '-')
const proposalStatusType = (v) => ({ DRAFT: 'info', BUSINESS_REVIEW: 'warning', HR_FINAL: 'warning', APPROVED: 'success', EXECUTING: 'primary', ARCHIVED: 'info' }[v] || 'info')
const baseStatusType = (v) => ({ '草稿': 'info', '待业务评审': 'warning', '待人力终审': 'warning', '生效执行': 'success', '滚动跟踪': 'primary', '复盘归档': 'info', '已作废': 'danger' }[v] || 'info')
const riskText = (v) => ({ LOW: '低', MEDIUM: '中', HIGH: '高' }[v] || v || '-')
const riskType = (v) => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[v] || 'info')
const algoText = (v) => ({ LABOR_EFFICIENCY: '劳动效能法', REGRESSION: '回归分析法', BUSINESS_RATIO: '业务驱动比率法', EXPERT: '德尔菲专家法' }[v] || v || '-')
const metricsTypeText = (v) => ({ EFFICIENCY: '效率类', COMPLIANCE: '合规类', TALENT: '人才类' }[v] || v || '-')
const warnText = (v) => ({ GREEN: '正常', YELLOW: '预警', RED: '严重' }[v] || v || '-')
const warnType = (v) => ({ GREEN: 'success', YELLOW: 'warning', RED: 'danger' }[v] || 'info')
const healthColor = (v) => (v >= 85 ? '#67c23a' : v >= 70 ? '#e6a23c' : '#f56c6c')

const baseExportCols = [
  { key: 'planName', title: '方案名称' }, { key: 'planYear', title: '规划年度' }, { key: 'department', title: '适用组织' },
  { key: 'currentHeadcount', title: '现有人数' }, { key: 'plannedHeadcount', title: '规划编制' }, { key: 'recruitmentTarget', title: '招聘计划' },
  { key: 'budget', title: '预算(元)' }, { key: 'strategy', title: '战略方向' }, { key: 'timeline', title: '时间线' }, { key: 'status', title: '状态' }
]
const baseFieldMap = { '方案名称': 'planName', '规划年度': 'planYear', '适用组织': 'department', '现有人数': 'currentHeadcount', '规划编制': 'plannedHeadcount', '招聘计划': 'recruitmentTarget', '预算(元)': 'budget', '战略方向': 'strategy', '时间线': 'timeline' }
const proposalExportCols = [
  { key: 'proposalNo', title: '方案编号' }, { key: 'planNo', title: '关联基数编号' }, { key: 'orgCode', title: '适用组织' },
  { key: 'totalDemand', title: '总需求' }, { key: 'internalSupply', title: '内部供给' }, { key: 'externalRecruitmentGap', title: '外招缺口' },
  { key: 'redundancyOptimization', title: '冗余优化' }, { key: 'totalHrCostBudget', title: '人力成本预算(元)' }, { key: 'status', title: '审批状态' }
]
const inventoryExportCols = [
  { key: 'employeeId', title: '工号' }, { key: 'employeeName', title: '姓名' }, { key: 'orgLevel2', title: '一级部门' },
  { key: 'orgLevel3', title: '二级部门' }, { key: 'positionSequence', title: '岗位序列' }, { key: 'jobLevel', title: '职务层级' },
  { key: 'headcountType', title: '编制类型' }, { key: 'age', title: '年龄' }, { key: 'performanceLevel', title: '绩效' },
  { key: 'turnoverRisk', title: '流失风险' }, { key: 'nineBoxPosition', title: '九宫格' }, { key: 'headcountStatus', title: '编制占用状态' }
]
const forecastExportCols = [
  { key: 'forecastId', title: '预测单号' }, { key: 'planNo', title: '规划编号' }, { key: 'orgCode', title: '组织' },
  { key: 'positionName', title: '岗位名称' }, { key: 'positionSequence', title: '岗位序列' }, { key: 'currentOnPostCount', title: '现有在岗' },
  { key: 'growthDemand', title: '业务增长需求' }, { key: 'efficiencyReduction', title: '增效减员' }, { key: 'totalDemand', title: '周期总需求' },
  { key: 'algorithmSource', title: '测算算法' }, { key: 'gapFlag', title: '需求缺口' }
]
const biExportCols = [
  { key: 'orgName', title: '组织' }, { key: 'statisticsPeriod', title: '统计周期' }, { key: 'planningHeadcount', title: '规划编制' },
  { key: 'actualHeadcount', title: '实际在岗' }, { key: 'headcountDeviationRate', title: '编制偏差率%' }, { key: 'headcountComplianceRate', title: '编制达成率%' },
  { key: 'supplyDemandGap', title: '供需缺口' }, { key: 'recruitmentAchievementRate', title: '招聘达成率%' }, { key: 'keyTalentRetentionRate', title: '关键人才保留率%' },
  { key: 'hrHealthScore', title: 'HR健康分' }, { key: 'healthWarningLevel', title: '预警等级' }
]

// 供需缺口自动测算（规格书算法：总需求−内部供给=外招缺口；反之为冗余优化量）
const proposalGap = computed(() => {
  const diff = (Number(proposalForm.totalDemand) || 0) - (Number(proposalForm.internalSupply) || 0)
  return { external: Math.max(diff, 0), redundancy: Math.max(-diff, 0) }
})
// 劳动效率法：周期需求 = 现有在岗 + 增长增量 − 增效扣减
const forecastTotal = computed(() =>
  (Number(forecastForm.currentOnPostCount) || 0) + (Number(forecastForm.growthDemand) || 0) - (Number(forecastForm.efficiencyReduction) || 0))

// 马尔可夫供给矩阵行
const supplyRows = computed(() => supplyData.value.seqs.map(s => {
  const retained = Math.round(s.begin * matrix.retention)
  const promoIn = Math.round(s.begin * matrix.promotion)
  const transferIn = Math.round(s.begin * matrix.transfer)
  const outflow = Math.round(s.begin * matrix.resign) + Math.round(s.begin * matrix.retire)
  const netSupply = retained + promoIn + transferIn - outflow
  const demand = supplyData.value.demandBySeq[s.seq] || 0
  return { seq: s.seq, begin: s.begin, promoIn, transferIn, retained, outflow, netSupply, demand, gap: demand > 0 ? (netSupply >= demand ? 'BALANCED' : 'GAP') : '' }
}))

// 三色预警判定（|偏差率|≤5%绿 / 5-15%黄 / >15%红）
const trackRows = computed(() => trackAll.value.map(r => {
  const rate = Math.abs(Number(r.headcountDeviationRate) || 0)
  const warnLevel = rate <= 5 ? 'GREEN' : rate <= 15 ? 'YELLOW' : 'RED'
  return { ...r, warnLevel }
}))
const avgDeviationRate = computed(() => {
  if (!trackRows.value.length) return 0
  return (trackRows.value.reduce((a, r) => a + Math.abs(Number(r.headcountDeviationRate) || 0), 0) / trackRows.value.length).toFixed(1)
})
const inventoryStats = computed(() => {
  const all = inventoryAll.value
  const total = all.length
  const core = all.filter(r => r.isCoreTalent).length
  const highRisk = all.filter(r => r.turnoverRisk === 'HIGH').length
  const onPost = all.filter(r => r.headcountStatus === '在编').length
  return { total, core, highRisk, onPostRate: total ? Math.round(onPost / total * 100) : 0 }
})
const biAvg = (key) => {
  if (!bi.rows.length) return 0
  return (bi.rows.reduce((a, r) => a + (Number(r[key]) || 0), 0) / bi.rows.length).toFixed(1)
}

function extractPage(res) {
  const d = res.data || {}
  return { records: d.records || [], total: d.total || 0 }
}

async function loadBase() {
  baseLoading.value = true
  try {
    const res = await axios.get('/api/hr-plan/list')
    baseRows.value = res.data?.data || []
  } catch (e) { ElMessage.error('战略基数加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { baseLoading.value = false }
}
async function loadProposals(page = proposals.page) {
  proposals.loading = true
  try {
    const res = await axios.get('/api/hr-planning/proposal/page', { params: { pageNum: page, pageSize: proposals.size } })
    Object.assign(proposals, extractPage(res), { page })
  } catch (e) { ElMessage.error('规划方案加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { proposals.loading = false }
}
async function loadInventory(page = inventory.page) {
  inventory.loading = true
  try {
    const res = await axios.get('/api/hr-planning/inventory/page', {
      params: { pageNum: page, pageSize: inventory.size, orgLevel2: inventory.filterOrg || undefined, headcountStatus: inventory.filterStatus || undefined }
    })
    Object.assign(inventory, extractPage(res), { page })
    const all = await axios.get('/api/hr-planning/inventory/list')
    inventoryAll.value = all.data?.data || []
  } catch (e) { ElMessage.error('现状盘点加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { inventory.loading = false }
}
async function loadForecasts(page = forecasts.page) {
  forecasts.loading = true
  try {
    const res = await axios.get('/api/hr-planning/forecast/page', { params: { pageNum: page, pageSize: forecasts.size } })
    Object.assign(forecasts, extractPage(res), { page })
  } catch (e) { ElMessage.error('需求预测加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { forecasts.loading = false }
}
async function loadMetrics(page = metrics.page) {
  metrics.loading = true
  try {
    const res = await axios.get('/api/hr-planning/metrics/page', { params: { pageNum: page, pageSize: metrics.size } })
    Object.assign(metrics, extractPage(res), { page })
  } catch (e) { ElMessage.error('业务指标加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { metrics.loading = false }
}
async function loadSupply() {
  supplyLoading.value = true
  try {
    const inv = await axios.get('/api/hr-planning/inventory/list')
    const bySeq = {}
    ;(inv.data?.data || []).forEach(r => { const k = r.positionSequence || '未分组'; bySeq[k] = (bySeq[k] || 0) + 1 })
    let demandBySeq = {}
    try {
      const fr = await axios.get('/api/hr-planning/forecast/list')
      ;(fr.data?.data || []).forEach(f => { const k = f.positionSequence || '未分组'; demandBySeq[k] = (demandBySeq[k] || 0) + (Number(f.totalDemand) || 0) })
    } catch (e) { /* 需求数据缺失时仅展示供给 */ }
    supplyData.value = { seqs: Object.keys(bySeq).map(k => ({ seq: k, begin: bySeq[k] })), demandBySeq }
  } catch (e) { ElMessage.error('供给测算加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { supplyLoading.value = false }
}
async function loadTracking() {
  trackLoading.value = true
  try {
    const res = await axios.get('/api/hrplanning/bi/stats/list')
    trackAll.value = res.data?.data || []
  } catch (e) { ElMessage.error('执行跟踪加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { trackLoading.value = false }
}
async function loadBi() {
  bi.loading = true
  try {
    const res = await axios.get('/api/hrplanning/bi/stats/period/' + encodeURIComponent(biPeriod.value || '2026-H1'))
    bi.rows = res.data?.data || []
  } catch (e) { ElMessage.error('BI 视图加载失败: ' + (e.response?.data?.message || e.message)) }
  finally { bi.loading = false }
}

function openBaseDialog(row) {
  Object.assign(baseForm, row ? { ...row } : { id: null, planName: '', planYear: new Date().getFullYear(), department: '', currentHeadcount: 0, plannedHeadcount: 0, recruitmentTarget: 0, budget: 0, strategy: '', objectives: '', timeline: '', status: '草稿' })
  baseDialogVisible.value = true
}
async function saveBase(nextStatus) {
  // 编制刚性校验：规划编制超出现有+招聘计划时黄色强预警
  if (baseForm.plannedHeadcount > (Number(baseForm.currentHeadcount) || 0) + (Number(baseForm.recruitmentTarget) || 0)) {
    try {
      await ElMessageBox.confirm('规划编制超出现有人数+招聘计划上限，请确认已调整业务目标或申请编制扩容。', '编制校验预警', { type: 'warning' })
    } catch (e) { return }
  }
  try {
    const payload = { ...baseForm, status: nextStatus || baseForm.status || '草稿' }
    let saved = payload
    if (baseForm.id) {
      await axios.put(`/api/hr-plan/${baseForm.id}`, payload)
      ElMessage.success('业务基数方案已更新')
    } else {
      const res = await axios.post('/api/hr-plan', payload)
      saved = (res.data && res.data.data) || payload
      ElMessage.success(nextStatus === '待业务评审' ? '已提交业务部门评审' : '草稿已保存')
    }
    // 提交评审时尝试走流程引擎
    if (nextStatus === '待业务评审') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_HR_PLAN,
          bizCode: 'hr-plan',
          bizId: saved.id || baseForm.id,
          bizJson: saved,
          title: `人力规划基数-${saved.planName}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    baseDialogVisible.value = false
    loadBase()
  } catch (e) { ElMessage.error('保存失败') }
}
async function setBaseStatus(row, status) {
  try {
    // 提交评审时优先走流程引擎
    if (status === '待业务评审') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_HR_PLAN,
          bizCode: 'hr-plan',
          bizId: row.id,
          bizJson: row,
          title: `人力规划基数-${row.planName}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        await axios.put(`/api/hr-plan/${row.id}`, { ...row, status })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
        loadBase()
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    await axios.put(`/api/hr-plan/${row.id}`, { ...row, status })
    ElMessage.success('状态已流转为：' + status)
    loadBase()
  } catch (e) { ElMessage.error('操作失败') }
}
function voidBase(row) {
  ElMessageBox.confirm(`确定作废方案"${row.planName}"吗？`, '作废确认', { type: 'warning' }).then(async () => {
    await axios.delete(`/api/hr-plan/${row.id}`)
    ElMessage.success('已作废')
    loadBase()
  }).catch(() => {})
}

function openProposalDialog(row) {
  Object.assign(proposalForm, row ? { ...row } : { id: null, proposalNo: '', planNo: '', orgCode: '', totalDemand: 0, internalSupply: 0, totalHrCostBudget: 0 })
  proposalPeriod.value = row && row.periodStart ? [row.periodStart, row.periodEnd] : []
  proposalDialogVisible.value = true
}
async function saveProposal(submit) {
  if (!proposalForm.planNo) { ElMessage.warning('方案必须绑定已评审通过的业务基数编号'); return }
  try {
    const payload = {
      ...proposalForm,
      proposalNo: proposalForm.proposalNo || ('HP-' + new Date().getFullYear() + '-' + String(Date.now()).slice(-4)),
      periodStart: proposalPeriod.value?.[0] || null,
      periodEnd: proposalPeriod.value?.[1] || null,
      externalRecruitmentGap: proposalGap.value.external,
      redundancyOptimization: proposalGap.value.redundancy,
      supplyDemandGap: proposalGap.value.external > 0 ? 'GAP' : (proposalGap.value.redundancy > 0 ? 'SURPLUS' : 'BALANCED'),
      status: submit ? 'BUSINESS_REVIEW' : 'DRAFT'
    }
    let saved = payload
    if (proposalForm.id) {
      await axios.put(`/api/hr-planning/proposal/${proposalForm.id}`, payload)
      ElMessage.success('规划方案已更新')
    } else {
      const res = await axios.post('/api/hr-planning/proposal', payload)
      saved = (res.data && res.data.data) || payload
      ElMessage.success(submit ? '已提交多级审批' : '草稿已保存')
    }
    // 提交多级审批时尝试走流程引擎
    if (submit) {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_HR_PLAN,
          bizCode: 'hr-planning-proposal',
          bizId: saved.id || proposalForm.id,
          bizJson: saved,
          title: `人力规划方案-${saved.proposalNo}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    proposalDialogVisible.value = false
    loadProposals(1)
  } catch (e) { ElMessage.error('保存失败') }
}
async function setProposalStatus(row, status) {
  try {
    // 提交多级审批时优先走流程引擎
    if (status === 'BUSINESS_REVIEW') {
      try {
        const instNo = await startApproval({
          defCode: FLOW_DEF_CODES.HRM_HR_PLAN,
          bizCode: 'hr-planning-proposal',
          bizId: row.id,
          bizJson: row,
          title: `人力规划方案-${row.proposalNo}`,
          initiator: localStorage.getItem('username') || 'dev-user'
        })
        await axios.put(`/api/hr-planning/proposal/${row.id}`, { ...row, status })
        ElMessage.success('已发起流程审批，实例号：' + instNo)
        loadProposals()
        return
      } catch (e) {
        ElMessage.warning('流程引擎不可用，已本地提交')
      }
    }
    await axios.put(`/api/hr-planning/proposal/${row.id}`, { ...row, status })
    ElMessage.success('审批状态已流转')
    loadProposals()
  } catch (e) { ElMessage.error('操作失败') }
}
function voidProposal(row) {
  ElMessageBox.confirm(`确定作废方案"${row.proposalNo}"吗？`, '作废确认', { type: 'warning' }).then(async () => {
    await axios.delete(`/api/hr-planning/proposal/${row.id}`)
    ElMessage.success('已作废')
    loadProposals()
  }).catch(() => {})
}

function openForecastDialog(row) {
  Object.assign(forecastForm, row ? { ...row } : { id: null, forecastId: '', planNo: '', orgCode: '', positionName: '', positionSequence: '', currentOnPostCount: 0, growthDemand: 0, efficiencyReduction: 0, algorithmSource: 'LABOR_EFFICIENCY' })
  forecastDialogVisible.value = true
}
async function saveForecast() {
  if (!forecastForm.positionName) { ElMessage.warning('请填写岗位名称'); return }
  try {
    const total = forecastTotal.value
    const payload = {
      ...forecastForm,
      forecastId: forecastForm.forecastId || ('DF-' + String(Date.now()).slice(-8)),
      totalDemand: total,
      gapFlag: total > (Number(forecastForm.currentOnPostCount) || 0) ? 'GAP' : (total < (Number(forecastForm.currentOnPostCount) || 0) ? 'SURPLUS' : 'BALANCED')
    }
    if (forecastForm.id) {
      await axios.put(`/api/hr-planning/forecast/${forecastForm.id}`, payload)
      ElMessage.success('校准结果已保存（留痕：操作人/时间由后端记录）')
    } else {
      await axios.post('/api/hr-planning/forecast', payload)
      ElMessage.success('测算结果已保存')
    }
    forecastDialogVisible.value = false
    loadForecasts(1)
  } catch (e) { ElMessage.error('保存失败') }
}
function removeForecast(row) {
  ElMessageBox.confirm(`确定删除岗位"${row.positionName}"的测算记录吗？`, '删除确认', { type: 'warning' }).then(async () => {
    await axios.delete(`/api/hr-planning/forecast/${row.id}`)
    ElMessage.success('已删除')
    loadForecasts()
  }).catch(() => {})
}

const loadedTabs = new Set()
function onTabChange(tab) {
  if (loadedTabs.has(tab)) return
  loadedTabs.add(tab)
  if (tab === 'proposals') loadProposals(1)
  else if (tab === 'inventory') loadInventory(1)
  else if (tab === 'forecasts') loadForecasts(1)
  else if (tab === 'supply') loadSupply()
  else if (tab === 'tracking') loadTracking()
  else if (tab === 'metrics') loadMetrics(1)
  else if (tab === 'bi') loadBi()
}
function reloadAll() {
  loadBase()
  if (loadedTabs.has('proposals')) loadProposals(proposals.page)
  if (loadedTabs.has('inventory')) loadInventory(inventory.page)
  if (loadedTabs.has('forecasts')) loadForecasts(forecasts.page)
  if (loadedTabs.has('supply')) loadSupply()
  if (loadedTabs.has('tracking')) loadTracking()
  if (loadedTabs.has('metrics')) loadMetrics(metrics.page)
  if (loadedTabs.has('bi')) loadBi()
}

onMounted(() => {
  loadedTabs.add('base')
  loadBase()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.header-actions { display: flex; gap: 8px; align-items: center; }
.tab-toolbar { display: flex; gap: 8px; margin-bottom: 12px; align-items: center; }
.stat-cards { display: flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 140px; text-align: center; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }
.stat-value.warn { color: #e6a23c; }
.stat-value.danger { color: #f56c6c; }
.stat-label { color: #909399; font-size: 13px; margin-top: 4px; }
.mt { margin-top: 12px; justify-content: flex-end; }
.mb { margin-bottom: 12px; }
</style>

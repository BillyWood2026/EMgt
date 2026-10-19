<template>
  <div class="page-container">
    <RoleBar />
    <div class="page-header">
      <h2>党费收缴管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 1. 计算基数 ==================== -->
      <el-tab-pane label="计算基数" name="base">
        <div class="toolbar">
          <el-input-number v-model="baseFilterOrgId" :min="0" controls-position="right" placeholder="组织ID" style="width:160px" />
          <el-button type="primary" @click="loadBaseList">查询</el-button>
          <el-button v-if="can('R02','R08')" type="primary" @click="openBaseDialog">新建基数</el-button>
        </div>
        <el-table :data="baseList" border stripe size="small" v-loading="baseLoading">
          <el-table-column prop="memberName" label="党员姓名" min-width="110" />
          <el-table-column prop="memberCategory" label="人员类别" min-width="110" />
          <el-table-column prop="baseAmount" label="计算基数" width="110" align="right" />
          <el-table-column prop="fixedAmount" label="固定金额" width="100" align="right" />
          <el-table-column prop="ratio" label="比例" width="90" align="right">
            <template #default="{ row }">{{ row.ratio != null ? (row.ratio * 100).toFixed(2) + '%' : '' }}</template>
          </el-table-column>
          <el-table-column prop="auditStatus" label="审批状态" width="100">
            <template #default="{ row }">
              <el-tag :type="auditTagType(row.auditStatus)" size="small">{{ auditLabel(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <template v-if="can('R03','R04') && row.auditStatus === 'PENDING'">
                <el-button size="small" type="success" link @click="auditBase(row, true)">通过</el-button>
                <el-button size="small" type="danger" link @click="auditBase(row, false)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 2. 月度账单 ==================== -->
      <el-tab-pane label="月度账单" name="bills">
        <div class="toolbar">
          <el-input-number v-model="billFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-input-number v-model="billFilter.month" :min="1" :max="12" controls-position="right" placeholder="月份" style="width:110px" />
          <el-input-number v-model="billFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:140px" />
          <el-select v-model="billFilter.status" placeholder="状态" clearable style="width:130px">
            <el-option label="未缴" value="UNPAID" />
            <el-option label="已登记" value="REGISTERED" />
            <el-option label="已确认" value="CONFIRMED" />
          </el-select>
          <el-button type="primary" @click="loadBills">查询</el-button>
          <el-button v-if="can('R02','R08','R12')" type="warning" @click="openGenerateDialog">生成账单</el-button>
          <el-button @click="exportCsv">导出 CSV</el-button>
        </div>
        <el-table :data="bills" border stripe size="small" v-loading="billsLoading">
          <el-table-column prop="billNo" label="账单编号" min-width="170" />
          <el-table-column prop="memberName" label="党员姓名" width="110" />
          <el-table-column prop="memberCategory" label="人员类别" width="110" />
          <el-table-column prop="baseAmount" label="基数" width="100" align="right" />
          <el-table-column prop="ratio" label="比例" width="90" align="right">
            <template #default="{ row }">{{ row.ratio != null ? (row.ratio * 100).toFixed(2) + '%' : '' }}</template>
          </el-table-column>
          <el-table-column prop="dueAmount" label="应缴金额" width="100" align="right" />
          <el-table-column prop="paidAmount" label="已缴金额" width="100" align="right" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="billStatusTagType(row.status)" size="small">{{ billStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 3. 收缴登记 ==================== -->
      <el-tab-pane label="收缴登记" name="register">
        <div class="toolbar">
          <el-input v-model="registerKeyword" placeholder="按账单编号 / 党员姓名过滤" clearable style="width:260px" />
          <el-button type="primary" @click="loadBills">刷新</el-button>
          <el-button v-if="can('R12')" type="success" :disabled="!selectedBills.length" @click="confirmPayments">
            确认入账（已选 {{ selectedBills.length }}）
          </el-button>
        </div>
        <el-table
          :data="filteredRegisterBills"
          border
          stripe
          size="small"
          v-loading="billsLoading"
          @selection-change="selectedBills = $event"
        >
          <el-table-column type="selection" width="45" />
          <el-table-column prop="billNo" label="账单编号" min-width="170" />
          <el-table-column prop="memberName" label="党员姓名" width="110" />
          <el-table-column prop="dueAmount" label="应缴金额" width="100" align="right" />
          <el-table-column prop="paidAmount" label="已缴金额" width="100" align="right" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="billStatusTagType(row.status)" size="small">{{ billStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="can('R02','R08','R12') && row.status === 'UNPAID'"
                size="small"
                type="primary"
                link
                @click="openPayDialog(row)"
              >登记</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 4. 催缴预警 ==================== -->
      <el-tab-pane label="催缴预警" name="remind">
        <div class="toolbar">
          <el-input-number v-model="remindFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:150px" />
          <el-input-number v-model="remindFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-input-number v-model="remindFilter.month" :min="1" :max="12" controls-position="right" placeholder="月份" style="width:110px" />
          <el-button type="primary" @click="loadRemind">查询</el-button>
          <span v-if="remind.asOf" class="as-of">截至日期：{{ remind.asOf }}</span>
        </div>

        <h3 class="section-title">连续 3 个月未缴（预警）</h3>
        <el-table :data="remind.warn3" border stripe size="small" v-loading="remindLoading">
          <el-table-column prop="memberName" label="党员姓名" width="120" />
          <el-table-column prop="orgName" label="所在组织" min-width="150" />
          <el-table-column prop="unpaidMonths" label="未缴月数" width="100" align="right" />
          <el-table-column prop="dueAmount" label="累计应缴" width="110" align="right" />
        </el-table>

        <h3 class="section-title">连续 6 个月未缴（警报）</h3>
        <el-table :data="remind.alarm6" border stripe size="small" v-loading="remindLoading">
          <el-table-column prop="memberName" label="党员姓名" width="120" />
          <el-table-column prop="orgName" label="所在组织" min-width="150" />
          <el-table-column prop="unpaidMonths" label="未缴月数" width="100" align="right" />
          <el-table-column prop="dueAmount" label="累计应缴" width="110" align="right" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="can('R02','R07')"
                size="small"
                type="danger"
                link
                @click="deserterWorkflow(row)"
              >自行脱党联动</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 5. 减免补缴 ==================== -->
      <el-tab-pane label="减免补缴" name="reduce">
        <div class="toolbar">
          <el-input-number v-model="reduceFilter.memberId" :min="0" controls-position="right" placeholder="党员ID" style="width:140px" />
          <el-input-number v-model="reduceFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:140px" />
          <el-button type="primary" @click="loadReduceBackpay">查询</el-button>
          <el-button v-if="can('R02','R07','R08')" type="primary" @click="openReduceDialog">新建减免</el-button>
          <el-button v-if="can('R02','R08','R12')" type="warning" @click="openBackpayDialog">新建补缴</el-button>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <h3 class="section-title">减免单</h3>
            <el-table :data="reduceList" border stripe size="small" v-loading="reduceLoading">
              <el-table-column prop="memberName" label="党员" width="100" />
              <el-table-column prop="reduceType" label="减免类型" width="110" />
              <el-table-column prop="amount" label="金额" width="90" align="right" />
              <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
              <el-table-column prop="auditStatus" label="审批状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="auditTagType(row.auditStatus)" size="small">{{ auditLabel(row.auditStatus) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <template v-if="can('R03') && row.auditStatus === 'PENDING'">
                    <el-button size="small" type="success" link @click="auditReduce(row, true)">通过</el-button>
                    <el-button size="small" type="danger" link @click="auditReduce(row, false)">驳回</el-button>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col :span="12">
            <h3 class="section-title">补缴单</h3>
            <el-table :data="backpayList" border stripe size="small" v-loading="backpayLoading">
              <el-table-column prop="memberName" label="党员" width="100" />
              <el-table-column prop="fromYearmonth" label="起始年月" width="100" />
              <el-table-column prop="toYearmonth" label="截止年月" width="100" />
              <el-table-column prop="amount" label="金额" width="90" align="right" />
              <el-table-column prop="overSixMonth" label="超6月" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.overSixMonth" type="danger" size="small">是</el-tag>
                  <el-tag v-else type="info" size="small">否</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
            </el-table>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== 6. 统计上缴 ==================== -->
      <el-tab-pane label="统计上缴" name="stats">
        <div class="toolbar">
          <el-input-number v-model="statsFilter.year" :min="2000" :max="2100" controls-position="right" placeholder="年份" style="width:120px" />
          <el-input-number v-model="statsFilter.orgId" :min="0" controls-position="right" placeholder="组织ID" style="width:140px" />
          <el-button type="primary" @click="loadStats">查询</el-button>
          <el-button v-if="can('R02','R12')" type="primary" @click="openYearlyRemitDialog">新建年度上缴</el-button>
          <el-button v-if="can('R02','R12')" type="warning" @click="openLargeRemitDialog">大额上缴</el-button>
        </div>

        <el-row :gutter="12" class="stats-cards">
          <el-col :span="4"><el-card shadow="hover"><div class="stat-label">应缴合计</div><div class="stat-value">{{ stats.totalDue ?? '-' }}</div></el-card></el-col>
          <el-col :span="4"><el-card shadow="hover"><div class="stat-label">已缴合计</div><div class="stat-value">{{ stats.totalPaid ?? '-' }}</div></el-card></el-col>
          <el-col :span="4"><el-card shadow="hover"><div class="stat-label">待缴</div><div class="stat-value">{{ stats.totalUnpaid ?? '-' }}</div></el-card></el-col>
          <el-col :span="4"><el-card shadow="hover"><div class="stat-label">减免</div><div class="stat-value">{{ stats.totalReduced ?? '-' }}</div></el-card></el-col>
          <el-col :span="4"><el-card shadow="hover"><div class="stat-label">大额</div><div class="stat-value">{{ stats.largeCount ?? '-' }}</div></el-card></el-col>
        </el-row>

        <h3 class="section-title">上缴单</h3>
        <el-table :data="remittanceList" border stripe size="small" v-loading="remitLoading">
          <el-table-column prop="remitNo" label="上缴单号" min-width="160" />
          <el-table-column prop="remitType" label="类型" width="110" />
          <el-table-column prop="amount" label="金额" width="110" align="right" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="receiptNo" label="回执号" min-width="130" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button v-if="can('R02','R12') && !row.receiptNo" size="small" type="primary" link @click="openReceiptDialog(row)">回执登记</el-button>
            </template>
          </el-table-column>
        </el-table>

        <h3 class="section-title">党费账户</h3>
        <div class="toolbar">
          <el-button v-if="can('R02','R12')" size="small" type="primary" @click="openAccountDialog">新建账户</el-button>
          <el-button size="small" @click="loadAccounts">刷新</el-button>
        </div>
        <el-table :data="accounts" border stripe size="small" v-loading="accountLoading">
          <el-table-column prop="orgName" label="组织名称" min-width="140" />
          <el-table-column prop="bankName" label="开户行" min-width="140" />
          <el-table-column prop="accountNo" label="账号" min-width="160" />
          <el-table-column prop="accountName" label="户名" min-width="140" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建基数弹窗 -->
    <el-dialog v-model="baseDialogVisible" title="新建基数" width="560px" :close-on-click-modal="false">
      <el-form :model="baseForm" label-width="110px">
        <el-form-item label="党员ID" required>
          <el-input-number v-model="baseForm.memberId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="人员类别" required>
          <el-select v-model="baseForm.memberCategory" style="width:100%" @change="previewCalc">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="计算基数">
          <el-input-number v-model="baseForm.baseAmount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="previewCalc" />
        </el-form-item>
        <el-form-item label="固定金额">
          <el-input-number v-model="baseForm.fixedAmount" :min="0" :precision="2" controls-position="right" style="width:100%" @change="previewCalc" />
        </el-form-item>
        <el-form-item label="生效年月" required>
          <el-date-picker v-model="baseForm.effectiveYearmonth" type="month" value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="calcPreview" label="试算结果">
          <span>比例：{{ (calcPreview.ratio * 100).toFixed(2) }}%，月应缴：{{ calcPreview.due }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="baseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBase">确定</el-button>
      </template>
    </el-dialog>

    <!-- 生成账单弹窗 -->
    <el-dialog v-model="generateDialogVisible" title="生成月度账单" width="480px" :close-on-click-modal="false">
      <el-form :model="generateForm" label-width="90px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="generateForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="年份" required>
          <el-input-number v-model="generateForm.year" :min="2000" :max="2100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="月份" required>
          <el-input-number v-model="generateForm.month" :min="1" :max="12" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateBills">确定</el-button>
      </template>
    </el-dialog>

    <!-- 收缴登记弹窗 -->
    <el-dialog v-model="payDialogVisible" title="收缴登记" width="560px" :close-on-click-modal="false">
      <el-form :model="payForm" label-width="110px">
        <el-form-item label="账单编号">
          <el-input :model-value="payForm.billNo" disabled />
        </el-form-item>
        <el-form-item label="实缴金额" required>
          <el-input-number v-model="payForm.paidAmount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="缴费方式" required>
          <el-select v-model="payForm.payMethod" style="width:100%">
            <el-option label="现金" value="现金" />
            <el-option label="银行转账" value="银行转账" />
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
          </el-select>
        </el-form-item>
        <el-form-item label="凭证号">
          <el-input v-model="payForm.voucherNo" />
        </el-form-item>
        <el-form-item label="缴费日期">
          <el-date-picker v-model="payForm.payDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="差额说明">
          <el-input v-model="payForm.diffReason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPay">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建减免弹窗 -->
    <el-dialog v-model="reduceDialogVisible" title="新建减免" width="560px" :close-on-click-modal="false">
      <el-form :model="reduceForm" label-width="110px">
        <el-form-item label="党员ID" required>
          <el-input-number v-model="reduceForm.memberId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="费属年月" required>
          <el-date-picker v-model="reduceForm.yearmonth" type="month" value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item label="减免类型" required>
          <el-select v-model="reduceForm.reduceType" style="width:100%">
            <el-option label="全额减免" value="全额减免" />
            <el-option label="部分减免" value="部分减免" />
          </el-select>
        </el-form-item>
        <el-form-item label="减免金额" required>
          <el-input-number v-model="reduceForm.amount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="减免原因" required>
          <el-input v-model="reduceForm.reason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="支部记录">
          <el-input v-model="reduceForm.branchRecord" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reduceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReduce">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建补缴弹窗 -->
    <el-dialog v-model="backpayDialogVisible" title="新建补缴" width="560px" :close-on-click-modal="false">
      <el-form :model="backpayForm" label-width="110px">
        <el-form-item label="党员ID" required>
          <el-input-number v-model="backpayForm.memberId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="起始年月" required>
          <el-date-picker v-model="backpayForm.fromYearmonth" type="month" value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item label="截止年月" required>
          <el-date-picker v-model="backpayForm.toYearmonth" type="month" value-format="YYYY-MM" style="width:100%" />
        </el-form-item>
        <el-form-item label="补缴金额" required>
          <el-input-number v-model="backpayForm.amount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="补缴原因" required>
          <el-input v-model="backpayForm.reason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="支部同意">
          <el-switch v-model="backpayForm.branchAgreed" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="backpayDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBackpay">确定</el-button>
      </template>
    </el-dialog>

    <!-- 年度上缴弹窗 -->
    <el-dialog v-model="yearlyRemitDialogVisible" title="新建年度上缴单" width="480px" :close-on-click-modal="false">
      <el-form :model="yearlyRemitForm" label-width="90px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="yearlyRemitForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="年份" required>
          <el-input-number v-model="yearlyRemitForm.year" :min="2000" :max="2100" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="yearlyRemitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitYearlyRemit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 大额上缴弹窗 -->
    <el-dialog v-model="largeRemitDialogVisible" title="大额上缴单" width="480px" :close-on-click-modal="false">
      <el-form :model="largeRemitForm" label-width="90px">
        <el-form-item label="缴费ID" required>
          <el-input-number v-model="largeRemitForm.paymentId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="largeRemitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLargeRemit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 回执登记弹窗 -->
    <el-dialog v-model="receiptDialogVisible" title="回执登记" width="480px" :close-on-click-modal="false">
      <el-form :model="receiptForm" label-width="90px">
        <el-form-item label="回执号" required>
          <el-input v-model="receiptForm.receiptNo" />
        </el-form-item>
        <el-form-item label="回执日期" required>
          <el-date-picker v-model="receiptForm.receiptDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReceipt">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建账户弹窗 -->
    <el-dialog v-model="accountDialogVisible" title="新建党费账户" width="560px" :close-on-click-modal="false">
      <el-form :model="accountForm" label-width="90px">
        <el-form-item label="组织ID" required>
          <el-input-number v-model="accountForm.orgId" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="组织名称" required>
          <el-input v-model="accountForm.orgName" />
        </el-form-item>
        <el-form-item label="开户行" required>
          <el-input v-model="accountForm.bankName" />
        </el-form-item>
        <el-form-item label="账号" required>
          <el-input v-model="accountForm.accountNo" />
        </el-form-item>
        <el-form-item label="户名" required>
          <el-input v-model="accountForm.accountName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="accountDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAccount">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/membership/dues'

const activeTab = ref('base')

// ==================== 人员类别 ====================
const categories = ref([])
async function loadCategories() {
  try {
    const res = await axios.get(`${API}/categories`)
    categories.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  }
}

// ==================== 1. 计算基数 ====================
const baseFilterOrgId = ref(null)
const baseList = ref([])
const baseLoading = ref(false)
const baseDialogVisible = ref(false)
const baseForm = reactive({ memberId: null, memberCategory: '', baseAmount: null, fixedAmount: null, effectiveYearmonth: '' })
const calcPreview = ref(null)

async function loadBaseList() {
  baseLoading.value = true
  try {
    const res = await axios.get(`${API}/base/list`, { params: { orgId: baseFilterOrgId.value || undefined } })
    baseList.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    baseLoading.value = false
  }
}

function openBaseDialog() {
  Object.assign(baseForm, { memberId: null, memberCategory: '', baseAmount: null, fixedAmount: null, effectiveYearmonth: '' })
  calcPreview.value = null
  baseDialogVisible.value = true
}

async function previewCalc() {
  if (!baseForm.memberCategory) return
  try {
    const res = await axios.get(`${API}/calc-preview`, {
      params: { category: baseForm.memberCategory, base: baseForm.baseAmount, fixed: baseForm.fixedAmount }
    })
    calcPreview.value = res.data?.data || res.data
  } catch (e) {
    console.error(e)
  }
}

async function submitBase() {
  try {
    const [feeYear, feeMonth] = (baseForm.effectiveYearmonth || '').split('-')
    await axios.post(`${API}/base`, {
      memberId: baseForm.memberId,
      memberCategory: baseForm.memberCategory,
      baseAmount: baseForm.baseAmount,
      fixedAmount: baseForm.fixedAmount,
      feeYear: feeYear ? Number(feeYear) : undefined,
      feeMonth: feeMonth ? Number(feeMonth) : undefined
    })
    ElMessage.success('新建基数成功')
    baseDialogVisible.value = false
    loadBaseList()
  } catch (e) {
    console.error(e)
  }
}

async function auditBase(row, pass) {
  try {
    await axios.post(`${API}/base/${row.id}/audit`, { pass })
    ElMessage.success(pass ? '已通过' : '已驳回')
    loadBaseList()
  } catch (e) {
    console.error(e)
  }
}

function auditLabel(s) {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[s] || s || ''
}
function auditTagType(s) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}

// ==================== 2. 月度账单 ====================
const billFilter = reactive({ year: new Date().getFullYear(), month: new Date().getMonth() + 1, orgId: null, status: '' })
const bills = ref([])
const billsLoading = ref(false)
const generateDialogVisible = ref(false)
const generateForm = reactive({ orgId: null, year: new Date().getFullYear(), month: new Date().getMonth() + 1 })

async function loadBills() {
  billsLoading.value = true
  try {
    const res = await axios.get(`${API}/bills`, {
      params: {
        year: billFilter.year,
        month: billFilter.month,
        orgId: billFilter.orgId || undefined,
        status: billFilter.status || undefined
      }
    })
    bills.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    billsLoading.value = false
  }
}

function openGenerateDialog() {
  Object.assign(generateForm, { orgId: billFilter.orgId, year: billFilter.year, month: billFilter.month })
  generateDialogVisible.value = true
}

async function generateBills() {
  try {
    const res = await axios.post(`${API}/bills/generate`, generateForm)
    const d = res.data?.data || res.data || {}
    ElMessage.success(`已生成 ${d.createdCount ?? 0} 条账单`)
    generateDialogVisible.value = false
    loadBills()
  } catch (e) {
    console.error(e)
  }
}

function exportCsv() {
  const params = new URLSearchParams()
  if (billFilter.year) params.append('year', billFilter.year)
  if (billFilter.month) params.append('month', billFilter.month)
  if (billFilter.orgId) params.append('orgId', billFilter.orgId)
  window.open(`http://localhost:8150${API}/export?${params.toString()}`, '_blank')
}

function billStatusLabel(s) {
  const map = { UNPAID: '未缴', REGISTERED: '已登记', CONFIRMED: '已确认' }
  return map[s] || s || ''
}
function billStatusTagType(s) {
  const map = { UNPAID: 'danger', REGISTERED: 'warning', CONFIRMED: 'success' }
  return map[s] || 'info'
}

// ==================== 3. 收缴登记 ====================
const registerKeyword = ref('')
const selectedBills = ref([])
const payDialogVisible = ref(false)
const payForm = reactive({ billId: null, billNo: '', memberId: null, paidAmount: null, payMethod: '现金', voucherNo: '', payDate: '', diffReason: '' })

const filteredRegisterBills = computed(() => {
  const kw = registerKeyword.value.trim()
  if (!kw) return bills.value
  return bills.value.filter(b => (b.billNo || '').includes(kw) || (b.memberName || '').includes(kw))
})

function openPayDialog(row) {
  Object.assign(payForm, {
    billId: row.id,
    billNo: row.billNo,
    memberId: row.memberId,
    paidAmount: row.dueAmount,
    payMethod: '现金',
    voucherNo: '',
    payDate: '',
    diffReason: ''
  })
  payDialogVisible.value = true
}

async function submitPay() {
  try {
    await axios.post(`${API}/payments/register`, {
      billId: payForm.billId,
      memberId: payForm.memberId,
      feeYear: billFilter.year,
      feeMonth: billFilter.month,
      paidAmount: payForm.paidAmount,
      payMethod: payForm.payMethod,
      voucherNo: payForm.voucherNo || undefined,
      payDate: payForm.payDate || undefined,
      diffReason: payForm.diffReason || undefined
    })
    ElMessage.success('登记成功')
    payDialogVisible.value = false
    loadBills()
  } catch (e) {
    console.error(e)
  }
}

async function confirmPayments() {
  try {
    await ElMessageBox.confirm(`确定要确认入账选中的 ${selectedBills.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.post(`${API}/payments/confirm`, { ids: selectedBills.value.map(b => b.id) })
    ElMessage.success('确认入账成功')
    loadBills()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// ==================== 4. 催缴预警 ====================
const remindFilter = reactive({ orgId: null, year: new Date().getFullYear(), month: new Date().getMonth() + 1 })
const remind = reactive({ asOf: '', warn3: [], alarm6: [] })
const remindLoading = ref(false)

async function loadRemind() {
  remindLoading.value = true
  try {
    const res = await axios.get(`${API}/remind`, {
      params: {
        orgId: remindFilter.orgId || undefined,
        year: remindFilter.year,
        month: remindFilter.month
      }
    })
    const d = res.data?.data || res.data || {}
    remind.asOf = d.asOf || ''
    remind.warn3 = d.warn3 || []
    remind.alarm6 = d.alarm6 || []
  } catch (e) {
    console.error(e)
  } finally {
    remindLoading.value = false
  }
}

async function deserterWorkflow(row) {
  try {
    await ElMessageBox.confirm(`确定对「${row.memberName}」发起自行脱党联动流程吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.post(`${API}/deserter-workflow`, { memberIds: [row.memberId] })
    ElMessage.success('已发起自行脱党联动')
    loadRemind()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// ==================== 5. 减免补缴 ====================
const reduceFilter = reactive({ memberId: null, orgId: null })
const reduceList = ref([])
const backpayList = ref([])
const reduceLoading = ref(false)
const backpayLoading = ref(false)
const reduceDialogVisible = ref(false)
const backpayDialogVisible = ref(false)
const reduceForm = reactive({ memberId: null, yearmonth: '', reduceType: '', amount: null, reason: '', branchRecord: '' })
const backpayForm = reactive({ memberId: null, fromYearmonth: '', toYearmonth: '', amount: null, reason: '', branchAgreed: false })

async function loadReduceBackpay() {
  reduceLoading.value = true
  backpayLoading.value = true
  try {
    const params = { memberId: reduceFilter.memberId || undefined, orgId: reduceFilter.orgId || undefined }
    const [r1, r2] = await Promise.all([
      axios.get(`${API}/reduce/list`, { params }),
      axios.get(`${API}/backpay/list`, { params })
    ])
    reduceList.value = r1.data?.data || r1.data || []
    backpayList.value = r2.data?.data || r2.data || []
  } catch (e) {
    console.error(e)
  } finally {
    reduceLoading.value = false
    backpayLoading.value = false
  }
}

function openReduceDialog() {
  Object.assign(reduceForm, { memberId: null, yearmonth: '', reduceType: '', amount: null, reason: '', branchRecord: '' })
  reduceDialogVisible.value = true
}

async function submitReduce() {
  try {
    const [feeYear, feeMonth] = (reduceForm.yearmonth || '').split('-')
    await axios.post(`${API}/reduce`, {
      memberId: reduceForm.memberId,
      feeYear: feeYear ? Number(feeYear) : undefined,
      feeMonth: feeMonth ? Number(feeMonth) : undefined,
      reduceType: reduceForm.reduceType,
      amount: reduceForm.amount,
      reason: reduceForm.reason,
      branchRecord: reduceForm.branchRecord
    })
    ElMessage.success('新建减免成功')
    reduceDialogVisible.value = false
    loadReduceBackpay()
  } catch (e) {
    console.error(e)
  }
}

async function auditReduce(row, pass) {
  try {
    await axios.post(`${API}/reduce/${row.id}/audit`, { pass })
    ElMessage.success(pass ? '已通过' : '已驳回')
    loadReduceBackpay()
  } catch (e) {
    console.error(e)
  }
}

function openBackpayDialog() {
  Object.assign(backpayForm, { memberId: null, fromYearmonth: '', toYearmonth: '', amount: null, reason: '', branchAgreed: false })
  backpayDialogVisible.value = true
}

async function submitBackpay() {
  try {
    await axios.post(`${API}/backpay`, {
      memberId: backpayForm.memberId,
      fromYearmonth: backpayForm.fromYearmonth,
      toYearmonth: backpayForm.toYearmonth,
      amount: backpayForm.amount,
      reason: backpayForm.reason,
      branchAgreed: backpayForm.branchAgreed
    })
    ElMessage.success('新建补缴成功')
    backpayDialogVisible.value = false
    loadReduceBackpay()
  } catch (e) {
    console.error(e)
  }
}

// ==================== 6. 统计上缴 ====================
const statsFilter = reactive({ year: new Date().getFullYear(), orgId: null })
const stats = reactive({})
const remittanceList = ref([])
const remitLoading = ref(false)
const yearlyRemitDialogVisible = ref(false)
const yearlyRemitForm = reactive({ orgId: null, year: new Date().getFullYear() })
const largeRemitDialogVisible = ref(false)
const largeRemitForm = reactive({ paymentId: null })
const receiptDialogVisible = ref(false)
const receiptForm = reactive({ id: null, receiptNo: '', receiptDate: '' })
const accounts = ref([])
const accountLoading = ref(false)
const accountDialogVisible = ref(false)
const accountForm = reactive({ orgId: null, orgName: '', bankName: '', accountNo: '', accountName: '' })

async function loadStats() {
  try {
    const res = await axios.get(`${API}/stats`, {
      params: { year: statsFilter.year, orgId: statsFilter.orgId || undefined }
    })
    Object.assign(stats, res.data?.data || res.data || {})
  } catch (e) {
    console.error(e)
  }
  loadRemittances()
}

async function loadRemittances() {
  remitLoading.value = true
  try {
    const res = await axios.get(`${API}/remittance/list`, {
      params: { orgId: statsFilter.orgId || undefined, year: statsFilter.year }
    })
    remittanceList.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    remitLoading.value = false
  }
}

function openYearlyRemitDialog() {
  Object.assign(yearlyRemitForm, { orgId: statsFilter.orgId, year: statsFilter.year })
  yearlyRemitDialogVisible.value = true
}

async function submitYearlyRemit() {
  try {
    await axios.post(`${API}/remittance/yearly`, {
      orgId: yearlyRemitForm.orgId, year: yearlyRemitForm.year
    })
    ElMessage.success('年度上缴单已生成')
    yearlyRemitDialogVisible.value = false
    loadRemittances()
  } catch (e) {
    console.error(e)
  }
}

function openLargeRemitDialog() {
  largeRemitForm.paymentId = null
  largeRemitDialogVisible.value = true
}

async function submitLargeRemit() {
  try {
    await axios.post(`${API}/remittance/large`, { paymentId: largeRemitForm.paymentId })
    ElMessage.success('大额上缴单已生成')
    largeRemitDialogVisible.value = false
    loadRemittances()
  } catch (e) {
    console.error(e)
  }
}

function openReceiptDialog(row) {
  Object.assign(receiptForm, { id: row.id, receiptNo: '', receiptDate: '' })
  receiptDialogVisible.value = true
}

async function submitReceipt() {
  try {
    await axios.put(`${API}/remittance/${receiptForm.id}/receipt`, {
      receiptNo: receiptForm.receiptNo,
      receiptDate: receiptForm.receiptDate
    })
    ElMessage.success('回执登记成功')
    receiptDialogVisible.value = false
    loadRemittances()
  } catch (e) {
    console.error(e)
  }
}

async function loadAccounts() {
  accountLoading.value = true
  try {
    const res = await axios.get(`${API}/account/list`)
    accounts.value = res.data?.data || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    accountLoading.value = false
  }
}

function openAccountDialog() {
  Object.assign(accountForm, { orgId: null, orgName: '', bankName: '', accountNo: '', accountName: '' })
  accountDialogVisible.value = true
}

async function submitAccount() {
  try {
    await axios.post(`${API}/account`, accountForm)
    ElMessage.success('新建账户成功')
    accountDialogVisible.value = false
    loadAccounts()
  } catch (e) {
    console.error(e)
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  loadCategories()
  loadBaseList()
  loadBills()
  loadRemind()
  loadReduceBackpay()
  loadStats()
  loadAccounts()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.section-title {
  margin: 16px 0 10px;
  font-size: 15px;
  color: #303133;
}

.as-of {
  color: #909399;
  font-size: 13px;
}

.stats-cards {
  margin-bottom: 16px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>

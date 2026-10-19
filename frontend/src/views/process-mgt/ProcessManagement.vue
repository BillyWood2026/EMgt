<template>
  <div class="epros-management">
    <div class="management-header">
      <div class="header-title">
        <h2>流程管理工具</h2>
        <p>制定流程、修改流程、废止流程全生命周期管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="createNewFlow">制定新流程</el-button>
        <el-button icon="Upload" @click="importFlow">导入流程</el-button>
        <el-button icon="Download" @click="exportFlows">导出流程</el-button>
      </div>
    </div>

    <div class="management-body">
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <!-- 流程列表 Tab -->
        <el-tab-pane label="流程列表" name="flowList">
          <div class="search-bar">
            <el-input v-model="searchKey" placeholder="搜索流程名称或编码" class="search-input" />
            <el-select v-model="filterLevel" placeholder="流程层级">
              <el-option label="全部层级" value="" />
              <el-option label="三级主流程" value="level3" />
              <el-option label="四级子流程" value="level4" />
              <el-option label="五级岗位流程" value="level5" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="流程状态">
              <el-option label="全部状态" value="" />
              <el-option label="草稿" value="draft" />
              <el-option label="待审核" value="pending" />
              <el-option label="已生效" value="active" />
              <el-option label="已废止" value="deprecated" />
              <el-option label="已归档" value="archived" />
            </el-select>
            <el-button icon="Search" @click="doSearch">搜索</el-button>
          </div>

          <div class="flow-list">
            <el-table :data="flowList" border>
              <el-table-column type="selection" width="50" />
              <el-table-column prop="code" label="流程编码" width="140" />
              <el-table-column prop="name" label="流程名称" width="200" />
              <el-table-column prop="level" label="流程层级" width="100">
                <template #default="{ row }">
                  <el-tag :type="getLevelTagType(row.level)">{{ getLevelName(row.level) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="category" label="流程分类" width="120" />
              <el-table-column prop="version" label="当前版本" width="100" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ownerDept" label="主责部门" width="120" />
              <el-table-column prop="createTime" label="创建时间" width="140" />
              <el-table-column prop="updateTime" label="更新时间" width="140" />
              <el-table-column prop="actions" label="操作" width="280">
                <template #default="{ row }">
                  <el-button size="small" @click="viewFlow(row)">查看</el-button>
                  <el-button size="small" @click="editFlow(row)" :disabled="row.status === 'deprecated'">修改</el-button>
                  <el-button size="small" @click="designFlow(row)" :disabled="row.status === 'deprecated'">设计</el-button>
                  <el-button size="small" v-if="row.status === 'active'" @click="deprecateFlow(row)" type="danger">废止</el-button>
                  <el-button size="small" v-if="row.status === 'deprecated'" @click="archiveFlow(row)">归档</el-button>
                  <el-button size="small" @click="viewVersions(row)">版本</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pageSize"
                :total="totalCount"
                layout="total, sizes, prev, pager, next, jumper"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 流程定义 Tab (整合自FlowDefinition.vue) -->
        <el-tab-pane label="流程定义" name="flowDefinition">
          <div class="search-bar">
            <el-form :inline="true" :model="defQuery" @submit.prevent>
              <el-form-item label="流程名称"><el-input v-model="defQuery.name" clearable placeholder="请输入流程名称" /></el-form-item>
              <el-form-item label="流程分类">
                <el-select v-model="defQuery.category" clearable placeholder="全部" style="width:160px">
                  <el-option label="项目管理" value="项目管理" />
                  <el-option label="EHS管理" value="EHS管理" />
                  <el-option label="人力资源" value="人力资源" />
                  <el-option label="采购管理" value="采购管理" />
                  <el-option label="财务管理" value="财务管理" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="defQuery.status" clearable placeholder="全部" style="width:120px">
                  <el-option label="已发布" value="published" />
                  <el-option label="草稿" value="draft" />
                  <el-option label="已停用" value="disabled" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadDefData">查询</el-button>
                <el-button @click="resetDefQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="defPageList" v-loading="defLoading" border stripe>
            <el-table-column prop="code" label="流程编码" width="140" />
            <el-table-column prop="name" label="流程名称" min-width="200" />
            <el-table-column prop="category" label="流程分类" width="120" />
            <el-table-column prop="version" label="版本号" width="90" />
            <el-table-column prop="nodeCount" label="节点数" width="80" />
            <el-table-column prop="creator" label="创建人" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column prop="instanceCount" label="运行实例" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getDefStatusType(row.status)" size="small">{{ getDefStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleDefEdit(row)">编辑</el-button>
                <el-button link type="primary" @click="handleDefDesign(row)">设计</el-button>
                <el-button link type="primary" @click="handleDefView(row)">预览</el-button>
                <el-button link type="primary" @click="handleDefPublish(row)" v-if="row.status === 'draft'">发布</el-button>
                <el-popconfirm title="确认删除？" @confirm="handleDefRemove(row)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination v-model:current-page="defQuery.page" v-model:page-size="defQuery.size" :total="defTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
          </div>
        </el-tab-pane>

        <!-- 表单管理 Tab (整合自FlowForm.vue) -->
        <el-tab-pane label="表单管理" name="formManagement">
          <div class="search-bar">
            <el-form :inline="true" :model="formQuery" @submit.prevent>
              <el-form-item label="表单名称"><el-input v-model="formQuery.name" clearable placeholder="请输入表单名称" /></el-form-item>
              <el-form-item label="分类">
                <el-select v-model="formQuery.category" clearable placeholder="全部" style="width:140px">
                  <el-option label="项目类" value="项目类" />
                  <el-option label="人事类" value="人事类" />
                  <el-option label="财务类" value="财务类" />
                  <el-option label="EHS类" value="EHS类" />
                </el-select>
              </el-form-item>
              <el-form-item><el-button type="primary" @click="loadFormData">查询</el-button><el-button @click="resetFormQuery">重置</el-button></el-form-item>
            </el-form>
          </div>

          <el-table :data="formPageList" v-loading="formLoading" border stripe>
            <el-table-column prop="code" label="表单编码" width="140" />
            <el-table-column prop="name" label="表单名称" min-width="200" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="fieldCount" label="字段数" width="90" />
            <el-table-column prop="creator" label="创建人" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '已启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleFormEdit(row)">编辑</el-button>
                <el-button link type="primary" @click="handleFormDesign(row)">设计</el-button>
                <el-button link type="primary" @click="handleFormPreview(row)">预览</el-button>
                <el-button link type="danger" @click="handleFormRemove(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination v-model:current-page="formQuery.page" v-model:page-size="formQuery.size" :total="formTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
          </div>
        </el-tab-pane>

        <!-- 分类管理 Tab (整合自FlowCategory.vue) -->
        <el-tab-pane label="分类管理" name="categoryManagement">
          <div class="category-stats">
            <el-row :gutter="12" class="stats-row">
              <el-col :span="3"><div class="stat-card blue"><div class="stat-value">{{ totalGroups }}</div><div class="stat-label">一级分类</div></div></el-col>
              <el-col :span="3"><div class="stat-card green"><div class="stat-value">{{ totalSub }}</div><div class="stat-label">子分类</div></div></el-col>
              <el-col :span="3"><div class="stat-card orange"><div class="stat-value">{{ totalFlows }}</div><div class="stat-label">流程总数</div></div></el-col>
              <el-col :span="3"><div class="stat-card purple"><div class="stat-value">{{ totalInstances }}</div><div class="stat-label">实例总数</div></div></el-col>
            </el-row>
          </div>

          <el-table :data="categoryTreeData" row-key="id" border :default-expand-all="allExpanded" :tree-props="{children:'children'}" :expand-row-keys="expandedKeys" @expand-change="handleExpandChange">
            <el-table-column prop="name" label="分类名称" min-width="240">
              <template #default="{ row }">
                <el-tag :type="row.level === 1 ? 'primary' : row.level === 2 ? 'success' : 'info'" size="small" style="margin-right:8px;">
                  {{ row.level === 1 ? '一级' : row.level === 2 ? '二级' : '三级' }}
                </el-tag>
                <span>{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="分类编码" width="160" />
            <el-table-column prop="flowCount" label="流程数量" width="100" align="center">
              <template #default="{ row }"><el-tag size="small">{{ row.flowCount }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="manager" label="负责人" width="100" />
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }"><el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleAddChild(row)" v-if="row.level < 3">新增子项</el-button>
                <el-button link type="primary" @click="handleCategoryEdit(row)">编辑</el-button>
                <el-button link type="success" @click="handleViewCategoryFlows(row)">查看流程</el-button>
                <el-button link type="warning" @click="handleToggleCategoryStatus(row)">{{ row.status === '启用' ? '停用' : '启用' }}</el-button>
                <el-popconfirm title="确认删除该分类？" @confirm="handleCategoryRemove(row)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 规则配置 Tab (整合自FlowRule.vue) -->
        <el-tab-pane label="规则配置" name="ruleConfig">
          <el-tabs v-model="ruleActiveTab">
            <el-tab-pane label="审批规则" name="approve">
              <el-table :data="approveRules" border stripe>
                <el-table-column prop="code" label="规则编码" width="140" />
                <el-table-column prop="name" label="规则名称" min-width="200" />
                <el-table-column prop="condition" label="触发条件" min-width="200" />
                <el-table-column prop="action" label="执行动作" min-width="160" />
                <el-table-column prop="priority" label="优先级" width="90" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }"><el-switch v-model="row.enabled" /></template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleEditRule(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleRemoveRule(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="超时规则" name="timeout">
              <el-table :data="timeoutRules" border stripe>
                <el-table-column prop="code" label="规则编码" width="140" />
                <el-table-column prop="name" label="规则名称" min-width="200" />
                <el-table-column prop="timeoutHours" label="超时时间(小时)" width="120" />
                <el-table-column prop="action" label="超时处理" min-width="160" />
                <el-table-column prop="notifyCount" label="通知次数" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }"><el-switch v-model="row.enabled" /></template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleEditRule(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleRemoveRule(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="通知规则" name="notify">
              <el-table :data="notifyRules" border stripe>
                <el-table-column prop="code" label="规则编码" width="140" />
                <el-table-column prop="name" label="规则名称" min-width="200" />
                <el-table-column prop="eventType" label="触发事件" width="140" />
                <el-table-column prop="notifyType" label="通知方式" width="120" />
                <el-table-column prop="notifyTarget" label="通知对象" width="140" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }"><el-switch v-model="row.enabled" /></template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleEditRule(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleRemoveRule(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 人员指派 Tab (整合自FlowAssignment.vue) -->
        <el-tab-pane label="人员指派" name="assignment">
          <el-tabs v-model="assignActiveTab">
            <el-tab-pane label="审批人员指派" name="approver">
              <el-table :data="approverList" border stripe>
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="nodeName" label="节点名称" min-width="160" />
                <el-table-column prop="approveType" label="指派类型" width="100">
                  <template #default="{ row }">
                    <el-tag :type="{指定人员:'primary',部门主管:'success',岗位角色:'warning',动态选择:'info'}[row.approveType] || 'primary'" size="small">{{ row.approveType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="approvers" label="审批人员" min-width="240" />
                <el-table-column prop="approveMode" label="审批方式" width="120" />
                <el-table-column prop="timeoutHours" label="超时时间" width="100" />
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleApproverEdit(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleApproverRemove(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="代理人设置" name="agent">
              <el-table :data="agentList" border stripe>
                <el-table-column prop="name" label="被代理人" width="120" />
                <el-table-column prop="department" label="部门" width="160" />
                <el-table-column prop="agent" label="代理人" width="120" />
                <el-table-column prop="startDate" label="生效日期" width="120" />
                <el-table-column prop="endDate" label="失效日期" width="120" />
                <el-table-column prop="scope" label="代理范围" min-width="200" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }"><el-tag :type="row.enabled?'success':'info'" size="small">{{ row.enabled?'启用':'停用' }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleAgentEdit(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleAgentRemove(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="岗位角色定义" name="role">
              <el-table :data="roleList" border stripe>
                <el-table-column prop="code" label="角色编码" width="140" />
                <el-table-column prop="name" label="角色名称" min-width="180" />
                <el-table-column prop="type" label="角色类型" width="120" />
                <el-table-column prop="members" label="包含人员" min-width="240" />
                <el-table-column prop="memberCount" label="人数" width="80" />
                <el-table-column label="操作" width="160">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleRoleEdit(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleRoleRemove(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 待办任务 Tab (整合自FlowTodo.vue) -->
        <el-tab-pane label="我的待办" name="todo">
          <div class="todo-stats">
            <el-row :gutter="12" class="stats-row">
              <el-col :span="4"><div class="stat-card blue" @click="activeTodoTab='all'" style="cursor:pointer"><div class="stat-value">{{ todoCount }}</div><div class="stat-label">全部待办</div></div></el-col>
              <el-col :span="4"><div class="stat-card red" @click="activeTodoTab='urgent'" style="cursor:pointer"><div class="stat-value">{{ urgentCount }}</div><div class="stat-label">紧急任务</div></div></el-col>
              <el-col :span="4"><div class="stat-card orange" @click="activeTodoTab='overdue'" style="cursor:pointer"><div class="stat-value">{{ overdueCount }}</div><div class="stat-label">已逾期</div></div></el-col>
              <el-col :span="4"><div class="stat-card green" @click="activeTodoTab='new'" style="cursor:pointer"><div class="stat-value">{{ todayCount }}</div><div class="stat-label">今日新增</div></div></el-col>
              <el-col :span="4"><div class="stat-card purple" style="cursor:pointer"><div class="stat-value">{{ approvedCount }}</div><div class="stat-label">今日已办</div></div></el-col>
              <el-col :span="4"><div class="stat-card cyan" style="cursor:pointer"><div class="stat-value">{{ delegateCount }}</div><div class="stat-label">已委托</div></div></el-col>
            </el-row>
          </div>

          <el-tabs v-model="activeTodoTab">
            <el-tab-pane label="待办列表" name="all">
              <div class="search-bar">
                <el-form :inline="true" :model="todoQuery" @submit.prevent>
                  <el-form-item label="流程名称"><el-input v-model="todoQuery.flowName" clearable placeholder="请输入流程名称" /></el-form-item>
                  <el-form-item label="紧急程度">
                    <el-select v-model="todoQuery.priority" clearable placeholder="全部" style="width:120px">
                      <el-option label="紧急" value="紧急" />
                      <el-option label="一般" value="一般" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="发起时间">
                    <el-date-picker v-model="todoQuery.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="loadTodoData">查询</el-button>
                    <el-button @click="resetTodoQuery">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <el-table :data="todoPageList" v-loading="todoLoading" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="nodeName" label="当前节点" width="140" />
                <el-table-column prop="initiator" label="发起人" width="100" />
                <el-table-column prop="receiveTime" label="到达时间" width="160" />
                <el-table-column prop="deadline" label="截止时间" width="160" />
                <el-table-column label="紧急度" width="90">
                  <template #default="{ row }"><el-tag :type="row.priority === '紧急' ? 'danger' : 'info'" size="small">{{ row.priority }}</el-tag></template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row }"><el-tag :type="isTodoOverdue(row) ? 'danger' : 'success'" size="small">{{ isTodoOverdue(row) ? '已逾期' : '待处理' }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleTodoApprove(row)">审批</el-button>
                    <el-button link type="primary" @click="handleTodoDelegate(row)">委托</el-button>
                    <el-button link type="primary" @click="handleTodoComment(row)">评论</el-button>
                    <el-button link type="primary" @click="handleTodoDetail(row)">详情</el-button>
                    <el-button link type="primary" @click="handleTodoFlow(row)">流程图</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination-container">
                <el-pagination v-model:current-page="todoQuery.page" v-model:page-size="todoQuery.size" :total="todoTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
              </div>
            </el-tab-pane>

            <el-tab-pane :label="'紧急任务 (' + urgentCount + ')'" name="urgent">
              <el-table :data="urgentList" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="nodeName" label="当前节点" width="140" />
                <el-table-column prop="deadline" label="截止时间" width="160" />
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleTodoApprove(row)">审批</el-button>
                    <el-button link type="primary" @click="handleTodoDetail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane :label="'已逾期 (' + overdueCount + ')'" name="overdue">
              <el-table :data="overdueList" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="nodeName" label="当前节点" width="140" />
                <el-table-column prop="deadline" label="截止时间" width="160" />
                <el-table-column label="逾期天数" width="100">
                  <template #default="{ row }"><el-tag type="danger" size="small">逾期 {{ getTodoOverdueDays(row) }} 天</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleTodoApprove(row)">立即处理</el-button>
                    <el-button link type="primary" @click="handleTodoDetail(row)">详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 已办任务 Tab (整合自FlowDone.vue) -->
        <el-tab-pane label="我的已办" name="done">
          <div class="done-stats">
            <el-row :gutter="12" class="stats-row">
              <el-col :span="4"><div class="stat-card blue"><div class="stat-value">{{ totalDone }}</div><div class="stat-label">已办总数</div></div></el-col>
              <el-col :span="4"><div class="stat-card green"><div class="stat-value">{{ approvedDoneCount }}</div><div class="stat-label">已通过</div></div></el-col>
              <el-col :span="4"><div class="stat-card red"><div class="stat-value">{{ rejectedDoneCount }}</div><div class="stat-label">已驳回</div></div></el-col>
              <el-col :span="4"><div class="stat-card orange"><div class="stat-value">{{ returnedDoneCount }}</div><div class="stat-label">已退回</div></div></el-col>
              <el-col :span="4"><div class="stat-card purple"><div class="stat-value">{{ todayDone }}</div><div class="stat-label">今日处理</div></div></el-col>
            </el-row>
          </div>

          <el-tabs v-model="activeDoneTab">
            <el-tab-pane label="全部已办" name="all">
              <div class="search-bar">
                <el-form :inline="true" :model="doneQuery" @submit.prevent>
                  <el-form-item label="流程名称"><el-input v-model="doneQuery.flowName" clearable placeholder="请输入流程名称" /></el-form-item>
                  <el-form-item label="审批结果">
                    <el-select v-model="doneQuery.result" clearable placeholder="全部" style="width:120px">
                      <el-option label="通过" value="通过" />
                      <el-option label="驳回" value="驳回" />
                      <el-option label="退回" value="退回" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="审批时间">
                    <el-date-picker v-model="doneQuery.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="loadDoneData">查询</el-button>
                    <el-button @click="resetDoneQuery">重置</el-button>
                    <el-button type="success" @click="handleDoneExport">导出</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <el-table :data="donePageList" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="nodeName" label="审批节点" width="140" />
                <el-table-column prop="initiator" label="发起人" width="100" />
                <el-table-column prop="approveTime" label="审批时间" width="160" />
                <el-table-column label="审批结果" width="100">
                  <template #default="{ row }"><el-tag :type="getDoneResultType(row.result)" size="small">{{ row.result }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="duration" label="处理时长" width="110" />
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleDoneDetail(row)">详情</el-button>
                    <el-button link type="primary" @click="handleDoneFlow(row)">流程图</el-button>
                    <el-button link type="primary" @click="handleDoneCopy(row)">抄送他人</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination-container">
                <el-pagination v-model:current-page="doneQuery.page" v-model:page-size="doneQuery.size" :total="doneTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
              </div>
            </el-tab-pane>

            <el-tab-pane :label="'已通过 (' + approvedDoneCount + ')'" name="approved">
              <el-table :data="approvedDoneList" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="approveTime" label="审批时间" width="160" />
                <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleDoneDetail(row)">详情</el-button>
                    <el-button link type="primary" @click="handleDoneFlow(row)">流程图</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane :label="'已驳回 (' + rejectedDoneCount + ')'" name="rejected">
              <el-table :data="rejectedDoneList" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="220" />
                <el-table-column prop="approveTime" label="审批时间" width="160" />
                <el-table-column label="驳回原因" min-width="200">
                  <template #default="{ row }"><el-tooltip :content="row.comment" placement="top"><span>{{ row.comment }}</span></el-tooltip></template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleDoneDetail(row)">详情</el-button>
                    <el-button link type="warning" @click="handleReSubmit(row)">重新发起</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 流程实例 Tab (整合自FlowInstance.vue) -->
        <el-tab-pane label="流程实例" name="instance">
          <div class="instance-stats">
            <el-row :gutter="12" class="stats-row">
              <el-col :span="6"><div class="stat-card blue"><div class="stat-value">{{ runningCount }}</div><div class="stat-label">运行中</div></div></el-col>
              <el-col :span="6"><div class="stat-card green"><div class="stat-value">{{ completedCount }}</div><div class="stat-label">已完成</div></div></el-col>
              <el-col :span="6"><div class="stat-card orange"><div class="stat-value">{{ suspendedCount }}</div><div class="stat-label">已暂停</div></div></el-col>
              <el-col :span="6"><div class="stat-card red"><div class="stat-value">{{ rejectedInstanceCount }}</div><div class="stat-label">已驳回</div></div></el-col>
            </el-row>
          </div>

          <div class="search-bar">
            <el-form :inline="true" :model="instanceQuery" @submit.prevent>
              <el-form-item label="流程名称"><el-input v-model="instanceQuery.flowName" clearable placeholder="请输入流程名称" /></el-form-item>
              <el-form-item label="发起人"><el-input v-model="instanceQuery.initiator" clearable placeholder="请输入发起人" /></el-form-item>
              <el-form-item label="状态">
                <el-select v-model="instanceQuery.status" clearable placeholder="全部" style="width:120px">
                  <el-option label="运行中" value="running" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已暂停" value="suspended" />
                  <el-option label="已驳回" value="rejected" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围">
                <el-date-picker v-model="instanceQuery.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px" />
              </el-form-item>
              <el-form-item><el-button type="primary" @click="loadInstanceData">查询</el-button><el-button @click="resetInstanceQuery">重置</el-button></el-form-item>
            </el-form>
          </div>

          <el-table :data="instancePageList" v-loading="instanceLoading" border stripe>
            <el-table-column prop="instanceNo" label="实例编号" width="160" />
            <el-table-column prop="flowName" label="流程名称" min-width="180" />
            <el-table-column prop="category" label="流程分类" width="120" />
            <el-table-column prop="initiator" label="发起人" width="100" />
            <el-table-column prop="currentNode" label="当前节点" width="140" />
            <el-table-column prop="currentApprover" label="当前审批人" width="100" />
            <el-table-column prop="startTime" label="开始时间" width="160" />
            <el-table-column prop="duration" label="处理时长" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getInstanceStatusType(row.status)" size="small">{{ getInstanceStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleInstanceDetail(row)">详情</el-button>
                <el-button link type="primary" @click="handleInstanceFlow(row)">流程图</el-button>
                <el-button link type="warning" v-if="row.status === 'running'" @click="handleInstanceSuspend(row)">暂停</el-button>
                <el-button link type="danger" v-if="row.status === 'running'" @click="handleInstanceTerminate(row)">终止</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination v-model:current-page="instanceQuery.page" v-model:page-size="instanceQuery.size" :total="instanceTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
          </div>
        </el-tab-pane>

        <!-- 我的工作台 Tab (整合自ProcessWorkbench.vue) -->
        <el-tab-pane label="我的工作台" name="workbench">
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card warning">
                <div class="stat-value">{{ workbenchStats.pendingCount }}</div>
                <div class="stat-label">待我审批</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card success">
                <div class="stat-value">{{ workbenchStats.approvedCount }}</div>
                <div class="stat-label">我已审批</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card primary">
                <div class="stat-value">{{ workbenchStats.initiatedCount }}</div>
                <div class="stat-label">我发起的</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card info">
                <div class="stat-value">{{ workbenchStats.ccCount }}</div>
                <div class="stat-label">抄送我的</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ workbenchStats.favoritesCount }}</div>
                <div class="stat-label">我的收藏</div>
              </el-card>
            </el-col>
            <el-col :span="4">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ workbenchStats.learningCount }}</div>
                <div class="stat-label">待学习</div>
              </el-card>
            </el-col>
          </el-row>
          <el-tabs v-model="workbenchTab">
            <el-tab-pane label="待我审批" name="pending">
              <el-table :data="workbenchPending" border stripe size="small">
                <el-table-column prop="flowNo" label="流程编号" width="180" />
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="initiator" label="发起人" width="100" />
                <el-table-column prop="submitTime" label="发起时间" width="160" />
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="handleWorkbenchApprove(row)">审批</el-button>
                    <el-button link size="small" @click="handleWorkbenchView(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="我发起的" name="initiated">
              <el-table :data="workbenchInitiated" border stripe size="small">
                <el-table-column prop="flowNo" label="流程编号" width="180" />
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="submitTime" label="发起时间" width="160" />
                <el-table-column label="状态" width="100">
                  <template #default="{row}"><el-tag size="small" :type="row.status === '审批中' ? 'warning' : 'success'">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{row}">
                    <el-button link size="small" @click="handleWorkbenchView(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="我的收藏" name="favorites">
              <el-table :data="workbenchFavorites" border stripe size="small">
                <el-table-column prop="docName" label="制度名称" min-width="200" />
                <el-table-column prop="category" label="分类" width="100" />
                <el-table-column prop="collectTime" label="收藏时间" width="160" />
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="handleWorkbenchDoc(row)">查阅</el-button>
                    <el-button link size="small" type="danger" @click="handleWorkbenchCancelFav(row)">取消</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 流程制度树形管理 Tab (整合自ProcessTree.vue) -->
        <el-tab-pane label="制度树形管理" name="treeManagement">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-card shadow="never">
                <template #header>
                  <b>流程制度树形架构</b>
                  <el-space style="float:right">
                    <el-select v-model="treeScope" size="small" style="width:100px">
                      <el-option label="公司级" value="company" />
                      <el-option label="部门级" value="dept" />
                    </el-select>
                    <el-button size="small" @click="expandAllTree">展开</el-button>
                    <el-button size="small" @click="collapseAllTree">收起</el-button>
                  </el-space>
                </template>
                <el-tree ref="processTreeRef" :data="processTreeData" node-key="id" :props="treeProps" default-expand-all @node-click="handleTreeNodeClick">
                  <template #default="{ node, data }">
                    <span class="tree-node">
                      <el-icon><component :is="getTreeNodeIcon(data.type)" /></el-icon>
                      <span>{{ node.label }}</span>
                      <el-tag v-if="data.status === '有效'" size="small" type="success" style="margin-left:4px">有效</el-tag>
                      <el-tag v-else-if="data.status === '待更新'" size="small" type="warning" style="margin-left:4px">待更新</el-tag>
                    </span>
                  </template>
                </el-tree>
              </el-card>
            </el-col>
            <el-col :span="18">
              <el-card shadow="never">
                <template #header><b>节点详情</b></template>
                <el-descriptions v-if="selectedTreeNode" :column="2" border>
                  <el-descriptions-item label="节点名称">{{ selectedTreeNode.name }}</el-descriptions-item>
                  <el-descriptions-item label="节点类型">{{ getTreeNodeTypeName(selectedTreeNode.type) }}</el-descriptions-item>
                  <el-descriptions-item label="状态"><el-tag type="success">{{ selectedTreeNode.status }}</el-tag></el-descriptions-item>
                  <el-descriptions-item label="版本">V{{ selectedTreeNode.version || '1.0' }}</el-descriptions-item>
                  <el-descriptions-item label="责任部门">{{ selectedTreeNode.dept }}</el-descriptions-item>
                  <el-descriptions-item label="更新时间">{{ selectedTreeNode.updateTime }}</el-descriptions-item>
                </el-descriptions>
                <el-empty v-else description="请选择左侧树形节点" />
              </el-card>
              <el-card shadow="never" style="margin-top:12px">
                <template #header><b>操作</b></template>
                <el-space>
                  <el-button type="primary" @click="createTreeNode">新增节点</el-button>
                  <el-button @click="editTreeNode">编辑节点</el-button>
                  <el-button type="success" @click="viewTreeNodeFlow">查看流程图</el-button>
                  <el-button type="warning" @click="reviseTreeNode">发起修订</el-button>
                </el-space>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 流程图设计器 Tab (整合自ProcessDesigner.vue) -->
        <el-tab-pane label="流程图设计器" name="flowDesigner">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px" title="BPMN2.0标准可视化流程设计器，支持拖拽建模、节点配置、模板复用、版本同步。" />
          <el-button type="primary" @click="createNewFlowchart">新建流程图</el-button>
          <el-button @click="openFlowchartTemplate">从模板创建</el-button>
          <el-button type="success" @click="saveFlowchartTemplate">保存为模板</el-button>
          <el-dropdown style="margin-left:8px">
            <el-button>批量样式<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="applyEnterpriseStyle">应用企业样式</el-dropdown-item>
                <el-dropdown-item @click="customizeStyle">自定义样式</el-dropdown-item>
                <el-dropdown-item @click="resetStyle">重置为默认</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="info" @click="exportFlowchart">导出流程图</el-button>
          <el-button type="warning" @click="bindPolicy">绑定制度</el-button>
          <el-card shadow="never" style="margin-top:12px">
            <template #header><b>流程图画布</b></template>
            <div class="designer-canvas">
              <el-empty description="请选择或新建流程图进行编辑" />
            </div>
          </el-card>
        </el-tab-pane>

        <!-- 版本管理 Tab (整合自ProcessVersion.vue) -->
        <el-tab-pane label="版本管理" name="versionManagement">
          <el-tabs v-model="versionTab">
            <el-tab-pane label="版本生命周期" name="lifecycle">
              <el-table :data="versionLifecycle" border stripe size="small">
                <el-table-column prop="flowCode" label="流程编码" width="140" />
                <el-table-column prop="flowName" label="流程名称" min-width="200" />
                <el-table-column prop="currentVersion" label="当前版本" width="100" />
                <el-table-column prop="totalVersions" label="版本数" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="{row}">
                    <el-tag v-if="row.status === 'active'" type="success" size="small">有效</el-tag>
                    <el-tag v-else-if="row.status === 'deprecated'" type="danger" size="small">作废</el-tag>
                    <el-tag v-else type="info" size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="lastUpdate" label="最后更新" width="160" />
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" @click="viewVersionDetail(row)">详情</el-button>
                    <el-button link size="small" @click="compareVersion(row)">版本对比</el-button>
                    <el-button link size="small" type="warning" @click="restoreVersion(row)">恢复</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="版本对比" name="versionCompare">
              <el-space>
                <el-select v-model="compareOldVersion" placeholder="选择旧版本" />
                <span>→</span>
                <el-select v-model="compareNewVersion" placeholder="选择新版本" />
                <el-button type="primary" @click="doVersionCompare">对比</el-button>
              </el-space>
              <el-card shadow="never" style="margin-top:12px" v-if="versionCompareResult">
                <el-descriptions :column="3" border>
                  <el-descriptions-item label="新增"><el-tag type="success">{{ versionCompareResult.added }}</el-tag></el-descriptions-item>
                  <el-descriptions-item label="修改"><el-tag type="warning">{{ versionCompareResult.modified }}</el-tag></el-descriptions-item>
                  <el-descriptions-item label="删除"><el-tag type="danger">{{ versionCompareResult.deleted }}</el-tag></el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 模板库 Tab (整合自ProcessTemplate.vue) -->
        <el-tab-pane label="模板库" name="templateLibrary">
          <el-tabs v-model="templateTab">
            <el-tab-pane label="制度模板" name="docTemplate">
              <el-table :data="docTemplates" border stripe size="small">
                <el-table-column prop="name" label="模板名称" min-width="180" />
                <el-table-column prop="category" label="分类" width="100" />
                <el-table-column prop="system" label="适用体系" width="130" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column label="状态" width="80">
                  <template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="useCount" label="使用次数" width="90" />
                <el-table-column label="操作" width="280">
                  <template #default="{row}">
                    <el-button link size="small" @click="viewTemplate(row)">查看</el-button>
                    <el-button link size="small" type="primary" @click="editTemplate(row)">编辑</el-button>
                    <el-button link size="small" type="success" @click="useTemplate(row)">使用</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="流程模板" name="flowTemplate">
              <el-table :data="flowTemplates" border stripe size="small">
                <el-table-column prop="name" label="模板名称" min-width="180" />
                <el-table-column prop="category" label="分类" width="100" />
                <el-table-column prop="nodeCount" label="节点数" width="80" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column label="状态" width="80">
                  <template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" @click="viewFlowTemplate(row)">查看</el-button>
                    <el-button link size="small" type="primary" @click="editFlowTemplate(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="流程图模板" name="flowchartTemplate">
              <el-row :gutter="16">
                <el-col :span="6" v-for="tmpl in flowchartTemplates" :key="tmpl.id">
                  <el-card shadow="hover">
                    <h4>{{ tmpl.name }}</h4>
                    <p>{{ tmpl.description }}</p>
                    <el-space>
                      <el-button size="small" @click="previewFlowchartTemplate(tmpl)">预览</el-button>
                      <el-button size="small" type="primary" @click="useFlowchartTemplate(tmpl)">使用</el-button>
                    </el-space>
                  </el-card>
                </el-col>
              </el-row>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 审批管理 Tab (整合自ProcessApproval.vue) -->
        <el-tab-pane label="审批管理" name="approvalManagement">
          <el-tabs v-model="approvalTab">
            <el-tab-pane label="待我审批" name="pending">
              <el-table :data="approvalPending" border stripe size="small">
                <el-table-column prop="flowNo" label="流程编号" width="170" />
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="initiator" label="发起人" width="100" />
                <el-table-column prop="submitTime" label="发起时间" width="160" />
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="openApproval(row)">审批</el-button>
                    <el-button link size="small" @click="viewApproval(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="我发起的" name="myInitiated">
              <el-table :data="approvalInitiated" border stripe size="small">
                <el-table-column prop="flowNo" label="流程编号" width="170" />
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="submitTime" label="发起时间" width="160" />
                <el-table-column label="状态" width="100">
                  <template #default="{row}"><el-tag size="small" type="warning">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{row}">
                    <el-button link size="small" @click="viewApproval(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="已完成" name="approvalCompleted">
              <el-table :data="approvalCompleted" border stripe size="small">
                <el-table-column prop="flowNo" label="流程编号" width="170" />
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="completeTime" label="完成时间" width="160" />
                <el-table-column label="结果" width="100">
                  <template #default="{row}"><el-tag size="small" :type="row.result === '通过' ? 'success' : 'danger'">{{ row.result }}</el-tag></template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 数据台账 Tab (整合自ProcessLedger.vue) -->
        <el-tab-pane label="数据台账" name="dataLedger">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px" title="智能化数据台账：统计制度总数、有效/作废/待审数量、月度新增/修订数量、各部门制度覆盖率、流程审批通过率等核心指标。" />
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card primary">
                <div class="stat-value">{{ ledgerStats.totalDocs }}</div>
                <div class="stat-label">制度总数</div>
                <div class="stat-trend up">↑ 12%</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card success">
                <div class="stat-value">{{ ledgerStats.activeDocs }}</div>
                <div class="stat-label">有效制度</div>
                <div class="stat-sub">{{ ledgerStats.activeRate }}%</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card warning">
                <div class="stat-value">{{ ledgerStats.pendingDocs }}</div>
                <div class="stat-label">待审批</div>
                <div class="stat-sub">较上月 -5</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card danger">
                <div class="stat-value">{{ ledgerStats.deprecatedDocs }}</div>
                <div class="stat-label">已作废</div>
                <div class="stat-sub">归档留存</div>
              </el-card>
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ ledgerStats.totalVersions }}</div>
                <div class="stat-label">版本总数</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ ledgerStats.approvalRate }}%</div>
                <div class="stat-label">审批通过率</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value">{{ ledgerStats.avgApprovalTime }}h</div>
                <div class="stat-label">平均审批时长</div>
              </el-card>
            </el-col>
          </el-row>
          <el-tabs v-model="ledgerTab">
            <el-tab-pane label="制度分布统计" name="distribution">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-card shadow="never">
                    <template #header><b>制度分类分布</b></template>
                    <div ref="categoryChartRef" style="height:300px"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never">
                    <template #header><b>各部门制度数量</b></template>
                    <div ref="deptChartRef" style="height:300px"></div>
                  </el-card>
                </el-col>
              </el-row>
              <el-row :gutter="16" style="margin-top:16px">
                <el-col :span="12">
                  <el-card shadow="never">
                    <template #header><b>制度体系分布</b></template>
                    <div ref="systemChartRef" style="height:300px"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="never">
                    <template #header><b>月度新增趋势</b></template>
                    <div ref="trendChartRef" style="height:300px"></div>
                  </el-card>
                </el-col>
              </el-row>
            </el-tab-pane>
            <el-tab-pane label="流程时效统计" name="timeliness">
              <el-card shadow="never">
                <template #header><b>审批时效分析</b></template>
                <el-row :gutter="20">
                  <el-col :span="6">
                    <el-statistic title="今日审批" :value="todayStats.total" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="通过" :value="todayStats.approved" :value-style="{ color: '#67c23a' }" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="驳回" :value="todayStats.rejected" :value-style="{ color: '#f56c6c' }" />
                  </el-col>
                  <el-col :span="6">
                    <el-statistic title="超时" :value="todayStats.overtime" :value-style="{ color: '#e6a23c' }" />
                  </el-col>
                </el-row>
              </el-card>
              <el-card shadow="never" style="margin-top:16px">
                <template #header><b>超时流程预警</b></template>
                <el-table :data="overtimeFlows" border stripe size="small">
                  <el-table-column prop="flowNo" label="流程编号" width="180" />
                  <el-table-column prop="title" label="标题" min-width="200" />
                  <el-table-column prop="currentNode" label="当前节点" width="120" />
                  <el-table-column prop="overtimeHours" label="超时时长" width="100">
                    <template #default="{row}"><el-tag type="danger">{{ row.overtimeHours }}h</el-tag></template>
                  </el-table-column>
                  <el-table-column prop="deadline" label="截止时间" width="160" />
                  <el-table-column label="操作" width="100">
                    <template #default="{row}">
                      <el-button link size="small" type="primary" @click="urgeFlow(row)">催办</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="合规性统计" name="compliance">
              <el-card shadow="never">
                <template #header><b>部门体系合规统计</b></template>
                <el-table :data="complianceData" border stripe size="small">
                  <el-table-column prop="deptName" label="部门" width="150" />
                  <el-table-column prop="docCount" label="制度数量" width="100" align="center" />
                  <el-table-column prop="coverage" label="体系覆盖率" width="120">
                    <template #default="{row}">
                      <el-progress :percentage="row.coverage" :color="getCoverageColor(row.coverage)" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="updateRate" label="版本更新率" width="120">
                    <template #default="{row}">
                      <el-progress :percentage="row.updateRate" color="success" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="learningComplete" label="学习完成率" width="120">
                    <template #default="{row}">
                      <el-progress :percentage="row.learningComplete" color="primary" />
                    </template>
                  </el-table-column>
                  <el-table-column prop="exceptionRate" label="异常率" width="100">
                    <template #default="{row}">
                      <el-tag :type="row.exceptionRate > 10 ? 'danger' : 'success'" size="small">{{ row.exceptionRate }}%</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{row}">
                      <el-button link size="small" @click="viewComplianceDetail(row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="制度台账明细" name="ledger">
              <el-card shadow="never" style="margin-bottom:12px">
                <el-form :inline="true" :model="ledgerSearchForm" @submit.prevent>
                  <el-form-item label="制度名称"><el-input v-model="ledgerSearchForm.name" placeholder="搜索制度名称" clearable /></el-form-item>
                  <el-form-item label="分类">
                    <el-select v-model="ledgerSearchForm.category" clearable placeholder="全部" style="width:120px">
                      <el-option label="全部" value="" />
                      <el-option label="体系手册" value="体系手册" />
                      <el-option label="程序文件" value="程序文件" />
                      <el-option label="管理制度" value="管理制度" />
                      <el-option label="作业SOP" value="作业SOP" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="状态">
                    <el-select v-model="ledgerSearchForm.status" clearable placeholder="全部" style="width:100px">
                      <el-option label="全部" value="" />
                      <el-option label="有效" value="有效" />
                      <el-option label="待更新" value="待更新" />
                      <el-option label="作废" value="作废" />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="doLedgerSearch">查询</el-button>
                    <el-button @click="resetLedgerSearch">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              <el-card shadow="never">
                <el-table :data="ledgerDetail" border stripe size="small">
                  <el-table-column prop="docCode" label="制度编码" width="150" />
                  <el-table-column prop="docName" label="制度名称" min-width="180" />
                  <el-table-column prop="category" label="分类" width="100">
                    <template #default="{row}"><el-tag size="small">{{ row.category }}</el-tag></template>
                  </el-table-column>
                  <el-table-column prop="version" label="版本" width="80" />
                  <el-table-column prop="deptName" label="责任部门" width="120" />
                  <el-table-column label="状态" width="80">
                    <template #default="{row}">
                      <el-tag v-if="row.status === '有效'" size="small" type="success">有效</el-tag>
                      <el-tag v-else-if="row.status === '待更新'" size="small" type="warning">待更新</el-tag>
                      <el-tag v-else size="small" type="info">作废</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="effectiveDate" label="生效日期" width="120" />
                  <el-table-column prop="nextReview" label="下次评审" width="120" />
                  <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
                  <el-table-column prop="downloadCount" label="下载" width="70" align="center" />
                  <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{row}">
                      <el-button link size="small" @click="viewDoc(row)">查看</el-button>
                      <el-button link size="small" @click="borrowDoc(row)">借阅</el-button>
                      <el-button link size="small" type="primary" @click="editDoc(row)">修订</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination style="margin-top:16px" background layout="total, sizes, prev, pager, next" :total="100" :page-sizes="[10, 20, 50, 100]" />
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="版本统计" name="versions">
              <el-card shadow="never">
                <template #header><b>版本分布统计</b></template>
                <el-row :gutter="20">
                  <el-col :span="4" v-for="item in versionStats" :key="item.label">
                    <el-statistic :title="item.label" :value="item.value" />
                  </el-col>
                </el-row>
              </el-card>
              <el-card shadow="never" style="margin-top:16px">
                <template #header><b>最近版本更新记录</b></template>
                <el-table :data="versionHistory" border stripe size="small">
                  <el-table-column prop="docName" label="制度名称" min-width="180" />
                  <el-table-column prop="oldVersion" label="旧版本" width="100" />
                  <el-table-column prop="newVersion" label="新版本" width="100">
                    <template #default="{row}"><el-tag type="success">{{ row.newVersion }}</el-tag></template>
                  </el-table-column>
                  <el-table-column prop="changeType" label="变更类型" width="100">
                    <template #default="{row}">
                      <el-tag v-if="row.changeType === '新建'" type="primary" size="small">新建</el-tag>
                      <el-tag v-else-if="row.changeType === '修订'" type="warning" size="small">修订</el-tag>
                      <el-tag v-else type="info" size="small">作废</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="operator" label="操作人" width="100" />
                  <el-table-column prop="updateTime" label="更新时间" width="160" />
                </el-table>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 资产绑定 Tab (整合自ProcessAsset.vue) -->
        <el-tab-pane label="资产绑定" name="assetBinding">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px" title="流程-制度-表单-作业文件四位一体绑定管理" />
          <div class="asset-stats">
            <div class="stat-card">
              <div class="stat-icon flow-icon">
                <el-icon>Guide</el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ assetStats.totalFlows }}</div>
                <div class="stat-label">流程数量</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon policy-icon">
                <el-icon>Document</el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ assetStats.totalPolicies }}</div>
                <div class="stat-label">制度文件</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon form-icon">
                <el-icon>Edit</el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ assetStats.totalForms }}</div>
                <div class="stat-label">绑定表单</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon sop-icon">
                <el-icon>List</el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ assetStats.totalSOPs }}</div>
                <div class="stat-label">作业文件</div>
              </div>
            </div>
          </div>
          <el-tabs v-model="assetActiveTab" type="card">
            <el-tab-pane label="四体绑定视图" name="binding">
              <div class="binding-panel">
                <div class="binding-header">
                  <span>流程-制度-表单-作业文件 四位一体关联</span>
                  <el-select v-model="assetViewMode" placeholder="视图模式">
                    <el-option label="流程主导" value="flow" />
                    <el-option label="制度主导" value="policy" />
                    <el-option label="表单主导" value="form" />
                    <el-option label="作业文件主导" value="sop" />
                  </el-select>
                </div>
                <div class="binding-list">
                  <div v-for="asset in bindingAssets" :key="asset.id" class="binding-card">
                    <div class="card-header">
                      <div class="asset-type">
                        <el-tag :type="getAssetTagType(asset.type)">{{ getAssetTypeName(asset.type) }}</el-tag>
                      </div>
                      <div class="asset-title">{{ asset.name }}</div>
                      <div class="asset-code">{{ asset.code }}</div>
                      <el-tag :type="getStatusTagType(asset.status)">{{ getStatusName(asset.status) }}</el-tag>
                    </div>
                    <div class="card-body">
                      <div v-if="asset.relations" class="relations-grid">
                        <div v-if="asset.relations.policies?.length" class="relation-item">
                          <div class="relation-label">关联制度</div>
                          <div class="relation-list">
                            <span v-for="policy in asset.relations.policies" :key="policy.id" class="relation-tag">
                              {{ policy.code }} - {{ policy.name }}
                            </span>
                          </div>
                        </div>
                        <div v-if="asset.relations.forms?.length" class="relation-item">
                          <div class="relation-label">绑定表单</div>
                          <div class="relation-list">
                            <span v-for="form in asset.relations.forms" :key="form.id" class="relation-tag">
                              {{ form.code }} - {{ form.name }}
                            </span>
                          </div>
                        </div>
                        <div v-if="asset.relations.sops?.length" class="relation-item">
                          <div class="relation-label">作业文件</div>
                          <div class="relation-list">
                            <span v-for="sop in asset.relations.sops" :key="sop.id" class="relation-tag">
                              {{ sop.code }} - {{ sop.name }}
                            </span>
                          </div>
                        </div>
                        <div v-if="asset.relations.flows?.length" class="relation-item">
                          <div class="relation-label">关联流程</div>
                          <div class="relation-list">
                            <span v-for="flow in asset.relations.flows" :key="flow.id" class="relation-tag">
                              {{ flow.code }} - {{ flow.name }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="card-footer">
                      <span class="system-tags">
                        <el-tag v-for="sys in asset.systems" :key="sys" size="small">{{ getSystemName(sys) }}</el-tag>
                      </span>
                      <div class="card-actions">
                        <el-button size="small" @click="viewAssetDetail(asset)">查看详情</el-button>
                        <el-button size="small" @click="editAssetBinding(asset)">编辑绑定</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="多体系融合视图" name="system">
              <div class="system-panel">
                <el-tabs v-model="activeSystem" type="border-card">
                  <el-tab-pane label="通用流程" name="common">
                    <div class="system-content">
                      <div class="system-header">
                        <span>跨体系通用流程（一体化）</span>
                        <el-button size="small" icon="Plus" @click="addCommonFlow">添加通用流程</el-button>
                      </div>
                      <el-table :data="commonFlows" border>
                        <el-table-column prop="code" label="流程编码" width="140" />
                        <el-table-column prop="name" label="流程名称" width="200" />
                        <el-table-column prop="description" label="描述" width="250" />
                        <el-table-column prop="systems" label="覆盖体系" width="150">
                          <template #default="{ row }">
                            <el-tag v-for="sys in row.systems" :key="sys" size="small">{{ getSystemName(sys) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="status" label="状态" width="80">
                          <template #default="{ row }">
                            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="actions" label="操作" width="120">
                          <template #default="{ row }">
                            <el-button size="small" @click="viewSystemFlow(row)">详情</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="安全管理体系" name="safety">
                    <div class="system-content">
                      <div class="system-header">
                        <span>安全管理专属流程</span>
                        <el-tag type="danger">专属流程</el-tag>
                      </div>
                      <el-table :data="safetyFlows" border>
                        <el-table-column prop="code" label="流程编码" width="140" />
                        <el-table-column prop="name" label="流程名称" width="200" />
                        <el-table-column prop="description" label="描述" width="250" />
                        <el-table-column prop="clause" label="合规条款" width="150" />
                        <el-table-column prop="status" label="状态" width="80">
                          <template #default="{ row }">
                            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="actions" label="操作" width="120">
                          <template #default="{ row }">
                            <el-button size="small" @click="viewSystemFlow(row)">详情</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="环境管理体系" name="environment">
                    <div class="system-content">
                      <div class="system-header">
                        <span>环境管理专属流程</span>
                        <el-tag type="success">专属流程</el-tag>
                      </div>
                      <el-table :data="envFlows" border>
                        <el-table-column prop="code" label="流程编码" width="140" />
                        <el-table-column prop="name" label="流程名称" width="200" />
                        <el-table-column prop="description" label="描述" width="250" />
                        <el-table-column prop="clause" label="合规条款" width="150" />
                        <el-table-column prop="status" label="状态" width="80">
                          <template #default="{ row }">
                            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="actions" label="操作" width="120">
                          <template #default="{ row }">
                            <el-button size="small" @click="viewSystemFlow(row)">详情</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="职业健康体系" name="health">
                    <div class="system-content">
                      <div class="system-header">
                        <span>职业健康专属流程</span>
                        <el-tag type="warning">专属流程</el-tag>
                      </div>
                      <el-table :data="healthFlows" border>
                        <el-table-column prop="code" label="流程编码" width="140" />
                        <el-table-column prop="name" label="流程名称" width="200" />
                        <el-table-column prop="description" label="描述" width="250" />
                        <el-table-column prop="clause" label="合规条款" width="150" />
                        <el-table-column prop="status" label="状态" width="80">
                          <template #default="{ row }">
                            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="actions" label="操作" width="120">
                          <template #default="{ row }">
                            <el-button size="small" @click="viewSystemFlow(row)">详情</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                  <el-tab-pane label="能源管理体系" name="energy">
                    <div class="system-content">
                      <div class="system-header">
                        <span>能源管理专属流程</span>
                        <el-tag type="info">专属流程</el-tag>
                      </div>
                      <el-table :data="energyFlows" border>
                        <el-table-column prop="code" label="流程编码" width="140" />
                        <el-table-column prop="name" label="流程名称" width="200" />
                        <el-table-column prop="description" label="描述" width="250" />
                        <el-table-column prop="clause" label="合规条款" width="150" />
                        <el-table-column prop="status" label="状态" width="80">
                          <template #default="{ row }">
                            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                          </template>
                        </el-table-column>
                        <el-table-column prop="actions" label="操作" width="120">
                          <template #default="{ row }">
                            <el-button size="small" @click="viewSystemFlow(row)">详情</el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </el-tab-pane>
            <el-tab-pane label="资产台账" name="ledger">
              <div class="ledger-panel">
                <div class="ledger-header">
                  <span>流程资产总台账</span>
                  <el-button size="small" icon="Refresh" @click="refreshAssetLedger">刷新</el-button>
                </div>
                <el-table :data="assetLedgerData" border>
                  <el-table-column type="selection" width="50" />
                  <el-table-column prop="assetType" label="资产类型" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getAssetTagType(row.assetType)">{{ getAssetTypeName(row.assetType) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="code" label="编码" width="140" />
                  <el-table-column prop="name" label="名称" width="200" />
                  <el-table-column prop="version" label="版本" width="80" />
                  <el-table-column prop="ownerDept" label="主责部门" width="120" />
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="140" />
                  <el-table-column prop="updateTime" label="更新时间" width="140" />
                  <el-table-column prop="actions" label="操作" width="120">
                    <template #default="{ row }">
                      <el-button size="small" @click="viewAssetLedgerDetail(row)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 制度查阅 Tab (整合自ProcessDocument.vue) -->
        <el-tab-pane label="制度查阅" name="documentAccess">
          <el-card shadow="never" style="margin-bottom:12px">
            <el-form :inline="true" :model="docSearch" @submit.prevent>
              <el-form-item label="关键词"><el-input v-model="docSearch.keyword" placeholder="搜索制度名称" clearable style="width:200px" /></el-form-item>
              <el-form-item label="分类">
                <el-select v-model="docSearch.category" clearable placeholder="全部" style="width:120px">
                  <el-option label="体系手册" value="体系手册" />
                  <el-option label="程序文件" value="程序文件" />
                  <el-option label="管理制度" value="管理制度" />
                  <el-option label="作业SOP" value="作业SOP" />
                </el-select>
              </el-form-item>
              <el-form-item><el-button type="primary" @click="searchDocument">搜索</el-button></el-form-item>
            </el-form>
          </el-card>
          <el-table :data="documentList" border stripe size="small">
            <el-table-column prop="docCode" label="制度编码" width="150" />
            <el-table-column prop="docName" label="制度名称" min-width="200" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="deptName" label="责任部门" width="120" />
            <el-table-column label="状态" width="80">
              <template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{row}">
                <el-button link size="small" type="primary" @click="viewDocument(row)">查阅</el-button>
                <el-button link size="small" @click="borrowDocument(row)">借阅</el-button>
                <el-button link size="small" @click="collectDocument(row)">收藏</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 五级流程架构管理 Tab (新增) -->
        <el-tab-pane label="五级流程架构" name="fiveLevelArch">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"
            title="五级流程架构：一级流程域→二级流程组→三级主流程→四级子流程→五级岗位SOP，实行「上下级联动、层级锁定」管控。" />
          <el-tabs v-model="archLevelTab">
            <!-- 一级流程域 -->
            <el-tab-pane label="一级流程域" name="level1">
              <el-table :data="level1Data" border stripe size="small">
                <el-table-column prop="code" label="域编码" width="120" />
                <el-table-column prop="name" label="流程域名称" min-width="180" />
                <el-table-column prop="dept" label="归口部门" width="120" />
                <el-table-column prop="desc" label="管控定位" min-width="200" />
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small" type="success">有效</el-tag></template></el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="viewLevelDetail(row)">查看</el-button>
                    <el-button link size="small" @click="editLevelDetail(row)">编辑</el-button>
                    <el-button link size="small" type="warning" @click="viewLevelChildren(row)">下级</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <!-- 二级流程组 -->
            <el-tab-pane label="二级流程组" name="level2">
              <el-table :data="level2Data" border stripe size="small">
                <el-table-column prop="code" label="组编码" width="120" />
                <el-table-column prop="name" label="流程组名称" min-width="180" />
                <el-table-column prop="parentDomain" label="归属流程域" width="120" />
                <el-table-column prop="dept" label="归口部门" width="120" />
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small" type="success">有效</el-tag></template></el-table-column>
                <el-table-column label="操作" width="180">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="viewLevelDetail(row)">查看</el-button>
                    <el-button link size="small" @click="editLevelDetail(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <!-- 三级主流程 -->
            <el-tab-pane label="三级主流程" name="level3">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom:8px" title="三级核心主流程（端到端管控主线），需绑定流程图+流程说明书。" />
              <el-table :data="level3Data" border stripe size="small">
                <el-table-column prop="code" label="流程编码" width="160" />
                <el-table-column prop="name" label="主流程名称" min-width="220" />
                <el-table-column prop="domain" label="归属域" width="100" />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column prop="hasFlowchart" label="流程图" width="80">
                  <template #default="{row}"><el-tag v-if="row.hasFlowchart" type="success" size="small">有</el-tag><el-tag v-else type="danger" size="small">缺</el-tag></template>
                </el-table-column>
                <el-table-column prop="hasDoc" label="说明书" width="80">
                  <template #default="{row}"><el-tag v-if="row.hasDoc" type="success" size="small">有</el-tag><el-tag v-else type="danger" size="small">缺</el-tag></template>
                </el-table-column>
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column label="操作" width="240">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="designLevel3Flow(row)">设计</el-button>
                    <el-button link size="small" @click="viewLevelDetail(row)">详情</el-button>
                    <el-button link size="small" @click="editLevelDoc(row)">说明书</el-button>
                    <el-button link size="small" type="warning" @click="reviseLevel3(row)">修订</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <!-- 四级子流程 -->
            <el-tab-pane label="四级子流程" name="level4">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom:8px" title="四级可复用分支流程，明确与主流程联动关系。" />
              <el-table :data="level4Data" border stripe size="small">
                <el-table-column prop="code" label="子流程编码" width="160" />
                <el-table-column prop="name" label="子流程名称" min-width="200" />
                <el-table-column prop="parentFlow" label="归属主流程" width="160" />
                <el-table-column prop="reuseCount" label="复用次数" width="80" align="center" />
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="designLevel4Flow(row)">设计</el-button>
                    <el-button link size="small" @click="viewLevelDetail(row)">详情</el-button>
                    <el-button link size="small" type="warning" @click="reviseLevel4(row)">修订</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <!-- 五级岗位SOP -->
            <el-tab-pane label="五级岗位SOP" name="level5">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom:8px" title="五级岗位操作流程/岗位SOP，贴合现场落地执行标准。" />
              <el-table :data="level5Data" border stripe size="small">
                <el-table-column prop="code" label="SOP编码" width="140" />
                <el-table-column prop="name" label="岗位SOP名称" min-width="200" />
                <el-table-column prop="position" label="适用岗位" width="120" />
                <el-table-column prop="parentSubFlow" label="归属子流程" width="160" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag size="small" type="success">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column label="操作" width="200">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="editLevel5SOP(row)">编辑</el-button>
                    <el-button link size="small" @click="viewLevelDetail(row)">详情</el-button>
                    <el-button link size="small" type="warning" @click="reviseLevel5(row)">修订</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 流程说明书管理 Tab (新增) -->
        <el-tab-pane label="流程说明书" name="processDoc">
          <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"
            title="流程说明书与BPMN流程图一一对应、版本同步、状态同步，实行「一流程一说明书、同版同效、联动迭代」管理机制。" />
          <el-card shadow="never" style="margin-bottom:12px">
            <el-form :inline="true" :model="docQuery" @submit.prevent>
              <el-form-item label="流程编号"><el-input v-model="docQuery.flowCode" placeholder="搜索流程编号" clearable style="width:180px" /></el-form-item>
              <el-form-item label="流程名称"><el-input v-model="docQuery.flowName" placeholder="搜索流程名称" clearable style="width:180px" /></el-form-item>
              <el-form-item label="层级">
                <el-select v-model="docQuery.level" clearable placeholder="全部" style="width:120px">
                  <el-option label="三级主流程" value="3" />
                  <el-option label="四级子流程" value="4" />
                  <el-option label="五级岗位SOP" value="5" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="docQuery.status" clearable placeholder="全部" style="width:100px">
                  <el-option label="有效" value="有效" />
                  <el-option label="待更新" value="待更新" />
                  <el-option label="作废" value="作废" />
                </el-select>
              </el-form-item>
              <el-form-item><el-button type="primary" @click="searchDoc">查询</el-button><el-button @click="resetDocQuery">重置</el-button></el-form-item>
            </el-form>
          </el-card>
          <el-tabs v-model="processDocTab">
            <el-tab-pane label="待编制" name="pending">
              <el-table :data="pendingDocList" border stripe size="small">
                <el-table-column prop="flowCode" label="流程编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="200" />
                <el-table-column prop="level" label="层级" width="100" />
                <el-table-column prop="hasFlowchart" label="流程图" width="80">
                  <template #default="{row}"><el-tag v-if="row.hasFlowchart" type="success" size="small">✓</el-tag><el-tag v-else type="danger" size="small">✗</el-tag></template>
                </el-table-column>
                <el-table-column label="说明书状态" width="100">
                  <template #default="{row}"><el-tag type="warning" size="small">待编制</el-tag></template>
                </el-table-column>
                <el-table-column label="操作" width="160">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="createProcessDoc(row)">创建说明书</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="已编制" name="created">
              <el-table :data="createdDocList" border stripe size="small">
                <el-table-column prop="flowCode" label="流程编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="200" />
                <el-table-column prop="level" label="层级" width="100" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column label="状态" width="80"><template #default="{row}"><el-tag type="success" size="small">{{ row.status }}</el-tag></template></el-table-column>
                <el-table-column prop="effectiveDate" label="生效日期" width="120" />
                <el-table-column label="操作" width="280">
                  <template #default="{row}">
                    <el-button link size="small" type="primary" @click="viewProcessDoc(row)">查看</el-button>
                    <el-button link size="small" @click="editProcessDoc(row)">编辑</el-button>
                    <el-button link size="small" @click="downloadProcessDoc(row)">下载</el-button>
                    <el-button link size="small" type="warning" @click="reviseProcessDoc(row)">修订</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="模板查看" name="template">
              <el-card shadow="never">
                <template #header><b>流程说明书标准模板（13章节）</b></template>
                <el-steps direction="vertical" :space="80" active="0">
                  <el-step title="一、基础信息" description="流程编号、名称、层级、归属架构、归口部门、配合部门、适用范围、版本号、生效/作废日期、修订记录" />
                  <el-step title="二、流程目的" description="流程管控目标、解决的管理痛点、管控价值" />
                  <el-step title="三、合规依据" description="ISO体系条款、行业标准、公司管理制度、国标规范" />
                  <el-step title="四、流程边界与适用场景" description="流程起点、终点、上下游关联流程、适用业务场景" />
                  <el-step title="五、流程整体概述" description="端到端业务闭环整体说明" />
                  <el-step title="六、岗位权责细则" description="各岗位执行职责、审批职责、配合职责、禁止行为" />
                  <el-step title="七、核心流转规则" description="分支条件、审批规则、驳回规则、跳转规则、超时规则" />
                  <el-step title="八、节点时限与权限管控" description="标准工时、预警机制、字段权限、涉密管控" />
                  <el-step title="九、风险点与防控措施" description="全流程风险点、失效后果、前置防控、过程管控" />
                  <el-step title="十、异常处置规范" description="业务异常、流程异常、数据异常的标准化处置" />
                  <el-step title="十一、配套资产清单" description="配套制度、表单、SOP/作业文件、证据留存要求" />
                  <el-step title="十二、考核与内审迭代要求" description="流程考核指标、违规标准、内审频次、复盘优化机制" />
                  <el-step title="十三、附件" description="BPMN流程图、配套表单模板、相关标准文件" />
                </el-steps>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 归档管理 Tab (整合自FlowArchive.vue) -->
        <el-tab-pane label="归档管理" name="archive">
          <div class="archive-stats">
            <el-row :gutter="12" class="stats-row">
              <el-col :span="4"><div class="stat-card blue"><div class="stat-value">{{ totalArchiveCount }}</div><div class="stat-label">归档总数</div></div></el-col>
              <el-col :span="4"><div class="stat-card green"><div class="stat-value">{{ monthArchiveCount }}</div><div class="stat-label">本月归档</div></div></el-col>
              <el-col :span="4"><div class="stat-card orange"><div class="stat-value">{{ pendingArchiveCount }}</div><div class="stat-label">待归档</div></div></el-col>
              <el-col :span="4"><div class="stat-card cyan"><div class="stat-value">{{ storageSize }}GB</div><div class="stat-label">存储大小</div></div></el-col>
              <el-col :span="4"><div class="stat-card purple"><div class="stat-value">{{ retentionYears }}年</div><div class="stat-label">存储年限</div></div></el-col>
            </el-row>
          </div>

          <el-tabs v-model="activeArchiveTab">
            <el-tab-pane label="已归档实例" name="archived">
              <div class="search-bar">
                <el-form :inline="true" :model="archiveQuery" @submit.prevent>
                  <el-form-item label="流程名称"><el-input v-model="archiveQuery.flowName" clearable /></el-form-item>
                  <el-form-item label="业务单号"><el-input v-model="archiveQuery.businessKey" clearable /></el-form-item>
                  <el-form-item label="归档时间"><el-date-picker v-model="archiveQuery.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" style="width:240px" /></el-form-item>
                  <el-form-item><el-button type="primary" @click="loadArchiveData">查询</el-button><el-button @click="resetArchiveQuery">重置</el-button></el-form-item>
                </el-form>
              </div>

              <el-table :data="archiveList" v-loading="archiveLoading" border stripe>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="flowName" label="流程名称" min-width="180" />
                <el-table-column prop="businessTitle" label="业务标题" min-width="200" />
                <el-table-column prop="businessKey" label="业务单号" width="160" />
                <el-table-column prop="initiator" label="发起人" width="100" />
                <el-table-column prop="finishTime" label="完成时间" width="160" />
                <el-table-column prop="archiveTime" label="归档时间" width="160" />
                <el-table-column prop="fileSize" label="归档大小" width="100" />
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="handleArchiveView(row)">查看</el-button>
                    <el-button link type="primary" @click="handleArchiveDownload(row)">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination-container">
                <el-pagination v-model:current-page="archiveQuery.page" v-model:page-size="archiveQuery.size" :total="archiveTotal" layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50]" />
              </div>
            </el-tab-pane>

            <el-tab-pane label="归档策略" name="policy">
              <el-table :data="archivePolicyList" border stripe>
                <el-table-column prop="code" label="策略编码" width="140" />
                <el-table-column prop="name" label="策略名称" min-width="200" />
                <el-table-column prop="scope" label="适用范围" width="160" />
                <el-table-column prop="archiveDays" label="完成后天数" width="110" />
                <el-table-column prop="retentionYears" label="保留年限" width="100" />
                <el-table-column prop="storageLocation" label="存储位置" min-width="200" />
                <el-table-column label="状态" width="100"><template #default="{ row }"><el-switch v-model="row.enabled" /></template></el-table-column>
                <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="handleEditPolicy(row)">编辑</el-button></template></el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="归档日志" name="log">
              <el-table :data="archiveLogList" border stripe>
                <el-table-column prop="time" label="操作时间" width="160" />
                <el-table-column prop="type" label="操作类型" width="120"><template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template></el-table-column>
                <el-table-column prop="instanceNo" label="实例编号" width="160" />
                <el-table-column prop="operator" label="操作人" width="100" />
                <el-table-column label="操作结果" width="100"><template #default="{ row }"><el-tag :type="row.result==='成功'?'success':'danger'" size="small">{{ row.result }}</el-tag></template></el-table-column>
                <el-table-column prop="detail" label="详细信息" min-width="200" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog title="制定新流程" v-model="createModalVisible" width="500px">
      <el-form :model="newFlowForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="流程编码" prop="code">
          <el-input v-model="newFlowForm.code" placeholder="如 FP000001" />
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input v-model="newFlowForm.name" placeholder="流程名称" />
        </el-form-item>
        <el-form-item label="流程层级" prop="level">
          <el-select v-model="newFlowForm.level">
            <el-option label="三级主流程" value="level3" />
            <el-option label="四级子流程" value="level4" />
            <el-option label="五级岗位流程" value="level5" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程分类" prop="category">
          <el-input v-model="newFlowForm.category" placeholder="如 生产管理" />
        </el-form-item>
        <el-form-item label="主责部门" prop="ownerDept">
          <el-select v-model="newFlowForm.ownerDept">
            <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流程描述" prop="description">
          <el-input v-model="newFlowForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog title="废止确认" v-model="deprecateModalVisible" width="400px">
      <div class="deprecate-warning">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>确定要废止流程 "<span class="flow-name">{{ deprecateFlowData?.name }}</span>" 吗？</p>
        <p class="hint">废止后流程将不再生效，相关审批将停止流转</p>
      </div>
      <el-form :model="deprecateForm" label-width="100px">
        <el-form-item label="废止原因">
          <el-input v-model="deprecateForm.reason" type="textarea" :rows="3" placeholder="请输入废止原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deprecateModalVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDeprecate">确认废止</el-button>
      </template>
    </el-dialog>

    <el-drawer title="流程详情" v-model="detailDrawerVisible" size="600px">
      <div v-if="selectedFlow" class="flow-detail">
        <div class="detail-header">
          <div class="flow-title">{{ selectedFlow.name }}</div>
          <div class="flow-code">{{ selectedFlow.code }}</div>
          <el-tag :type="getStatusTagType(selectedFlow.status)">{{ getStatusName(selectedFlow.status) }}</el-tag>
        </div>
        
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="流程层级">{{ getLevelName(selectedFlow.level) }}</el-descriptions-item>
            <el-descriptions-item label="流程分类">{{ selectedFlow.category }}</el-descriptions-item>
            <el-descriptions-item label="当前版本">{{ selectedFlow.version }}</el-descriptions-item>
            <el-descriptions-item label="主责部门">{{ selectedFlow.ownerDeptName }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ selectedFlow.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ selectedFlow.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h4>流程描述</h4>
          <div class="rich-text">{{ selectedFlow.description }}</div>
        </div>
        
        <div class="detail-section">
          <h4>关联制度</h4>
          <div class="el-list">
            <div class="el-list-item" v-for="policy in selectedFlow.policies" :key="policy.id">
              {{ policy.code }} - {{ policy.name }}
            </div>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>关联表单</h4>
          <div class="el-list">
            <div class="el-list-item" v-for="form in selectedFlow.forms" :key="form.id">
              {{ form.code }} - {{ form.name }}
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 合规详情弹窗 (来自ProcessLedger.vue) -->
    <el-dialog v-model="complianceDetailVisible" title="部门合规详情" width="700px">
      <el-card shadow="never" v-if="currentCompliance">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="部门名称">{{ currentCompliance.deptName }}</el-descriptions-item>
          <el-descriptions-item label="制度数量">{{ currentCompliance.docCount }}</el-descriptions-item>
          <el-descriptions-item label="体系覆盖率">{{ currentCompliance.coverage }}%</el-descriptions-item>
          <el-descriptions-item label="版本更新率">{{ currentCompliance.updateRate }}%</el-descriptions-item>
          <el-descriptions-item label="学习完成率">{{ currentCompliance.learningComplete }}%</el-descriptions-item>
          <el-descriptions-item label="异常率">{{ currentCompliance.exceptionRate }}%</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card shadow="never" style="margin-top:12px">
        <template #header><b>缺失制度清单</b></template>
        <el-table :data="missingDocs" border size="small">
          <el-table-column prop="docName" label="制度名称" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="requiredBy" label="要求来源" width="150" />
        </el-table>
      </el-card>
    </el-dialog>

    <!-- 资产详情抽屉 (来自ProcessAsset.vue) -->
    <el-drawer title="资产详情" v-model="assetDetailDrawerVisible" size="600px">
      <div v-if="selectedAsset" class="asset-detail">
        <div class="detail-header">
          <div class="asset-info">
            <el-tag :type="getAssetTagType(selectedAsset.type)">{{ getAssetTypeName(selectedAsset.type) }}</el-tag>
            <span class="asset-name">{{ selectedAsset.name }}</span>
            <span class="asset-code">{{ selectedAsset.code }}</span>
          </div>
          <el-tag :type="getStatusTagType(selectedAsset.status)">{{ getStatusName(selectedAsset.status) }}</el-tag>
        </div>
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="版本">{{ selectedAsset.version }}</el-descriptions-item>
            <el-descriptions-item label="主责部门">{{ selectedAsset.ownerDept }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ selectedAsset.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ selectedAsset.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <h4>关联资产</h4>
          <div class="relation-detail">
            <div v-if="selectedAsset.relations?.policies?.length" class="relation-group">
              <div class="relation-title">关联制度</div>
              <div v-for="policy in selectedAsset.relations.policies" :key="policy.id" class="relation-item">
                <span class="item-code">{{ policy.code }}</span>
                <span class="item-name">{{ policy.name }}</span>
                <span class="item-version">{{ policy.version }}</span>
              </div>
            </div>
            <div v-if="selectedAsset.relations?.forms?.length" class="relation-group">
              <div class="relation-title">绑定表单</div>
              <div v-for="form in selectedAsset.relations.forms" :key="form.id" class="relation-item">
                <span class="item-code">{{ form.code }}</span>
                <span class="item-name">{{ form.name }}</span>
              </div>
            </div>
            <div v-if="selectedAsset.relations?.sops?.length" class="relation-group">
              <div class="relation-title">作业文件</div>
              <div v-for="sop in selectedAsset.relations.sops" :key="sop.id" class="relation-item">
                <span class="item-code">{{ sop.code }}</span>
                <span class="item-name">{{ sop.name }}</span>
                <span class="item-version">{{ sop.version }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="detail-section">
          <h4>合规条款</h4>
          <div v-if="selectedAsset.compliance?.length" class="compliance-list">
            <div v-for="item in selectedAsset.compliance" :key="item.id" class="compliance-item">
              <el-tag :type="getStandardTagType(item.standard)">{{ getStandardName(item.standard) }}</el-tag>
              <span>{{ item.clause }} - {{ item.description }}</span>
              <el-tag :type="getRiskTagType(item.riskLevel)" size="small">{{ item.riskLevel }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download, Search, Warning, Refresh, Document, CircleCheck, CircleClose, Clock, Guide, Edit, List, ArrowDown } from '@element-plus/icons-vue'

// 主要变量
const activeTab = ref('flowList')
const searchKey = ref('')
const filterLevel = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(100)

const createModalVisible = ref(false)
const deprecateModalVisible = ref(false)
const detailDrawerVisible = ref(false)

const selectedFlow = ref(null)
const deprecateFlowData = ref(null)

const formRef = ref(null)

const newFlowForm = reactive({
  code: '',
  name: '',
  level: '',
  category: '',
  ownerDept: '',
  description: ''
})

const deprecateForm = reactive({
  reason: ''
})

const formRules = {
  code: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择流程层级', trigger: 'change' }],
  ownerDept: [{ required: true, message: '请选择主责部门', trigger: 'change' }]
}

const deptOptions = ref([
  { id: 1, name: '生产中心' },
  { id: 2, name: 'EHS部' },
  { id: 3, name: '品质部' },
  { id: 4, name: '研发中心' },
  { id: 5, name: '人力资源部' },
  { id: 6, name: '财务部' }
])

const flowList = ref([])

// ========== 流程定义相关 (来自 FlowDefinition.vue) ==========
const defQuery = reactive({ name: '', category: '', status: '', page: 1, size: 10 })
const defTotal = ref(50)
const defLoading = ref(false)
const defPageList = ref([])
const defDialog = reactive({ visible: false, title: '', form: {} })

const getDefStatusType = (status) => ({ published: 'success', draft: 'info', disabled: 'warning' }[status] || 'info')
const getDefStatusText = (status) => ({ published: '已发布', draft: '草稿', disabled: '已停用' }[status] || status)

const loadDefData = () => {
  defLoading.value = true
  setTimeout(() => {
    defPageList.value = [
      { code: 'FLOW001', name: '请假申请流程', category: '人力资源', version: 'v1.0', nodeCount: 5, creator: '管理员', createTime: '2024-01-01', instanceCount: 120, status: 'published' },
      { code: 'FLOW002', name: '费用报销流程', category: '财务管理', version: 'v1.0', nodeCount: 4, creator: '管理员', createTime: '2024-01-02', instanceCount: 85, status: 'published' }
    ]
    defLoading.value = false
  }, 500)
}

const resetDefQuery = () => { defQuery.name = ''; defQuery.category = ''; defQuery.status = '' }
const handleDefEdit = (row) => { ElMessage.info(`编辑流程定义: ${row.name}`) }
const handleDefDesign = (row) => { ElMessage.info(`设计流程: ${row.name}`) }
const handleDefView = (row) => { ElMessage.info(`预览流程: ${row.name}`) }
const handleDefPublish = (row) => { ElMessage.success(`发布流程: ${row.name}`) }
const handleDefRemove = (row) => { ElMessage.success(`删除流程: ${row.name}`) }

// ========== 工作台相关 (来自 ProcessWorkbench.vue) ==========
const workbenchTab = ref('pending')
const workbenchStats = reactive({
  pendingCount: 3, approvedCount: 12, initiatedCount: 8, ccCount: 5, favoritesCount: 10, learningCount: 3
})
const workbenchPending = ref([
  { flowNo: 'LC-ZD-2026-0621-001', title: '招聘管理制度V3.0修订', type: '制度修订', initiator: '周敏', submitTime: '2026-06-21 09:30' },
  { flowNo: 'LC-ZD-2026-0620-002', title: '设备维护保养规程V2.0', type: '制度新建', initiator: '刘洋', submitTime: '2026-06-20 14:15' }
])
const workbenchInitiated = ref([
  { flowNo: 'LC-ZD-2026-0621-001', title: '招聘管理制度V3.0修订', type: '制度修订', submitTime: '2026-06-21 09:30', status: '审批中' }
])
const workbenchFavorites = ref([
  { docName: '四标一体化管理手册', category: '体系手册', collectTime: '2026-06-15 10:30' }
])
const handleWorkbenchApprove = (row) => { ElMessage.info(`审批: ${row.title}`) }
const handleWorkbenchView = (row) => { ElMessage.info(`查看: ${row.title}`) }
const handleWorkbenchDoc = (row) => { ElMessage.info(`查阅: ${row.docName}`) }
const handleWorkbenchCancelFav = (row) => { ElMessage.success('已取消收藏') }

// ========== 树形管理相关 (来自 ProcessTree.vue) ==========
const treeScope = ref('company')
const treeProps = { label: 'name', children: 'children', type: 'type' }
const processTreeRef = ref(null)
const selectedTreeNode = ref(null)
const processTreeData = ref([
  { id: 1, name: '公司级流程', type: 'root', children: [
    { id: 11, name: '体系手册', type: 'folder', status: '有效', children: [
      { id: 111, name: '四标一体化管理手册', type: 'file', status: '有效', version: '2.0', dept: '综合管理部', updateTime: '2026-06-15' }
    ]},
    { id: 12, name: '程序文件', type: 'folder', status: '有效', children: [
      { id: 121, name: '生产作业控制程序', type: 'file', status: '有效', version: '3.1', dept: '生产部', updateTime: '2026-06-10' }
    ]}
  ]}
])
const getTreeNodeIcon = (type) => type === 'folder' ? 'Folder' : type === 'file' ? 'Document' : 'FolderOpened'
const getTreeNodeTypeName = (type) => ({ root: '根节点', folder: '文件夹', file: '文件' }[type] || type)
const handleTreeNodeClick = (data) => { selectedTreeNode.value = data }
const expandAllTree = () => {}
const collapseAllTree = () => {}
const createTreeNode = () => { ElMessage.info('新增节点') }
const editTreeNode = () => { ElMessage.info('编辑节点') }
const viewTreeNodeFlow = () => { ElMessage.info('查看流程图') }
const reviseTreeNode = () => { ElMessage.info('发起修订') }

// ========== 流程图设计器相关 (来自 ProcessDesigner.vue) ==========
const createNewFlowchart = () => { ElMessage.info('新建流程图') }
const openFlowchartTemplate = () => { ElMessage.info('从模板创建') }
const saveFlowchartTemplate = () => { ElMessage.success('已保存为模板') }
const applyEnterpriseStyle = () => { ElMessage.success('已应用企业样式') }
const customizeStyle = () => { ElMessage.info('自定义样式') }
const resetStyle = () => { ElMessage.success('已重置为默认') }
const exportFlowchart = () => { ElMessage.info('导出流程图') }
const bindPolicy = () => { ElMessage.info('绑定制度') }

// ========== 版本管理相关 (来自 ProcessVersion.vue) ==========
const versionTab = ref('lifecycle')
const versionLifecycle = ref([
  { flowCode: 'FLOW001', flowName: '请假申请流程', currentVersion: 'v2.0', totalVersions: 3, status: 'active', lastUpdate: '2026-06-15' }
])
const compareOldVersion = ref('')
const compareNewVersion = ref('')
const versionCompareResult = ref(null)
const viewVersionDetail = (row) => { ElMessage.info(`查看版本详情: ${row.flowName}`) }
const compareVersion = (row) => { ElMessage.info(`版本对比: ${row.flowName}`) }
const restoreVersion = (row) => { ElMessage.success(`恢复版本: ${row.flowName}`) }
const doVersionCompare = () => { versionCompareResult.value = { added: 3, modified: 5, deleted: 1 } }

// ========== 模板库相关 (来自 ProcessTemplate.vue) ==========
const templateTab = ref('docTemplate')
const docTemplates = ref([
  { name: '制度标准模板', category: '管理制度', system: '质量管理体系', version: '1.0', status: '启用', useCount: 45 }
])
const flowTemplates = ref([
  { name: '标准审批流程', category: '审批流程', nodeCount: 5, version: '1.0', status: '启用' }
])
const flowchartTemplates = ref([
  { id: 1, name: '串行审批流程', description: '适用于简单串行审批场景' },
  { id: 2, name: '并行审批流程', description: '适用于多部门并行审批场景' }
])
const viewTemplate = (row) => { ElMessage.info(`查看模板: ${row.name}`) }
const editTemplate = (row) => { ElMessage.info(`编辑模板: ${row.name}`) }
const useTemplate = (row) => { ElMessage.success(`使用模板: ${row.name}`) }
const viewFlowTemplate = (row) => { ElMessage.info(`查看流程模板: ${row.name}`) }
const editFlowTemplate = (row) => { ElMessage.info(`编辑流程模板: ${row.name}`) }
const previewFlowchartTemplate = (tmpl) => { ElMessage.info(`预览: ${tmpl.name}`) }
const useFlowchartTemplate = (tmpl) => { ElMessage.success(`使用流程图模板: ${tmpl.name}`) }

// ========== 审批管理相关 (来自 ProcessApproval.vue) ==========
const approvalTab = ref('pending')
const approvalPending = ref([
  { flowNo: 'LC-ZD-2026-0621-001', title: '招聘管理制度V3.0修订', type: '制度修订', initiator: '周敏', submitTime: '2026-06-21 09:30' }
])
const approvalInitiated = ref([
  { flowNo: 'LC-ZD-2026-0621-001', title: '招聘管理制度V3.0修订', type: '制度修订', submitTime: '2026-06-21 09:30', status: '审批中' }
])
const approvalCompleted = ref([
  { flowNo: 'LC-ZD-2026-0615-001', title: '会议管理制度V2.0', completeTime: '2026-06-16 10:00', result: '通过' }
])
const openApproval = (row) => { ElMessage.info(`审批: ${row.title}`) }
const viewApproval = (row) => { ElMessage.info(`查看: ${row.title}`) }

// ========== 数据台账相关 (来自 ProcessLedger.vue) ==========
const ledgerTab = ref('distribution')
const complianceDetailVisible = ref(false)
const currentCompliance = ref(null)
const categoryChartRef = ref(null)
const deptChartRef = ref(null)
const systemChartRef = ref(null)
const trendChartRef = ref(null)
const ledgerSearchForm = reactive({ name: '', category: '', status: '' })
const ledgerStats = reactive({
  totalDocs: 286, activeDocs: 245, activeRate: 85.7, pendingDocs: 18, deprecatedDocs: 23,
  totalVersions: 1256, approvalRate: 92.5, avgApprovalTime: 4.2
})
const todayStats = reactive({ total: 45, approved: 38, rejected: 5, overtime: 2 })
const overtimeFlows = ref([
  { flowNo: 'LC-ZD-2026-0620-001', title: '安全生产管理制度V3.0修订', currentNode: '分管领导', overtimeHours: 48, deadline: '2026-06-19 17:00' },
  { flowNo: 'LC-ZD-2026-0618-003', title: '采购审批流程优化', currentNode: '体系专员', overtimeHours: 24, deadline: '2026-06-18 12:00' }
])
const complianceData = ref([
  { deptName: '生产制造中心', docCount: 45, coverage: 95, updateRate: 88, learningComplete: 92, exceptionRate: 3 },
  { deptName: '质量管理部', docCount: 38, coverage: 92, updateRate: 85, learningComplete: 96, exceptionRate: 2 },
  { deptName: '人力资源部', docCount: 28, coverage: 88, updateRate: 90, learningComplete: 98, exceptionRate: 1 },
  { deptName: '财务管理部', docCount: 22, coverage: 85, updateRate: 82, learningComplete: 94, exceptionRate: 4 },
  { deptName: '综合管理部', docCount: 35, coverage: 90, updateRate: 87, learningComplete: 91, exceptionRate: 3 }
])
const missingDocs = ref([
  { docName: '特种设备安全操作规程', category: '作业SOP', requiredBy: '安全生产体系' },
  { docName: '危险废弃物管理程序', category: '程序文件', requiredBy: '环境管理体系' }
])
const ledgerDetail = ref([
  { id: 1, docCode: 'ZD-GL-001', docName: '四标一体化管理手册', category: '体系手册', version: 'V2.0', deptName: '管理者代表', status: '有效', effectiveDate: '2026-01-01', nextReview: '2027-01-01', viewCount: 1256, downloadCount: 89 },
  { id: 2, docCode: 'ZD-SC-001', docName: '生产作业控制程序', category: '程序文件', version: 'V3.1', deptName: '生产部', status: '有效', effectiveDate: '2026-03-15', nextReview: '2026-09-15', viewCount: 876, downloadCount: 45 },
  { id: 3, docCode: 'ZD-SC-002', docName: '设备维护保养规程', category: '作业SOP', version: 'V2.0', deptName: '设备部', status: '待更新', effectiveDate: '2025-06-01', nextReview: '2026-06-01', viewCount: 654, downloadCount: 32 },
  { id: 4, docCode: 'ZD-QA-001', docName: '进料检验作业指导书', category: '作业SOP', version: 'V1.5', deptName: '质量部', status: '有效', effectiveDate: '2026-02-01', nextReview: '2026-08-01', viewCount: 432, downloadCount: 28 },
  { id: 5, docCode: 'ZD-HR-001', docName: '员工培训管理制度', category: '管理制度', version: 'V2.5', deptName: '人力资源部', status: '有效', effectiveDate: '2026-04-01', nextReview: '2026-10-01', viewCount: 789, downloadCount: 56 }
])
const versionStats = ref([
  { label: 'V1.0', value: 156 }, { label: 'V1.x', value: 423 }, { label: 'V2.0', value: 287 },
  { label: 'V2.x', value: 198 }, { label: 'V3.0+', value: 192 }
])
const versionHistory = ref([
  { docName: '招聘管理制度', oldVersion: 'V2.0', newVersion: 'V3.0', changeType: '修订', operator: '周敏', updateTime: '2026-06-15 14:30' },
  { docName: '安全生产操作规程', oldVersion: 'V3.1', newVersion: 'V3.2', changeType: '修订', operator: '刘洋', updateTime: '2026-06-10 09:15' },
  { docName: '设备巡检作业流程', oldVersion: '-', newVersion: 'V1.0', changeType: '新建', operator: '李明', updateTime: '2026-06-08 11:00' },
  { docName: '旧版考勤办法', oldVersion: 'V1.0', newVersion: '-', changeType: '作废', operator: '张伟', updateTime: '2026-06-05 16:30' }
])
const getCoverageColor = (value) => {
  if (value >= 90) return '#67c23a'
  if (value >= 70) return '#e6a23c'
  return '#f56c6c'
}
const doLedgerSearch = () => { ElMessage.success('搜索完成') }
const resetLedgerSearch = () => { ledgerSearchForm.name = ''; ledgerSearchForm.category = ''; ledgerSearchForm.status = '' }
const viewDoc = (row) => { ElMessage.info(`查看制度: ${row.docName}`) }
const borrowDoc = (row) => { ElMessage.info(`借阅制度: ${row.docName}`) }
const editDoc = (row) => { ElMessage.info(`修订制度: ${row.docName}`) }
const viewComplianceDetail = (row) => { currentCompliance.value = row; complianceDetailVisible.value = true }
const urgeFlow = (row) => { ElMessage.success(`已发送催办通知: ${row.title}`) }

// ========== 资产绑定相关 (来自 ProcessAsset.vue) ==========
const assetActiveTab = ref('binding')
const activeSystem = ref('common')
const assetViewMode = ref('flow')
const assetDetailDrawerVisible = ref(false)
const selectedAsset = ref(null)
const assetStats = ref({ totalFlows: 86, totalPolicies: 156, totalForms: 234, totalSOPs: 189 })
const bindingAssets = ref([
  {
    id: 1, type: 'flow', code: 'FP090101', name: 'EHS体系文件全生命周期主流程', status: 'active',
    systems: ['safety', 'environment', 'health'],
    relations: {
      policies: [{ id: 1, code: 'ZD-EHS-001', name: '文件控制程序', version: 'V2.0' }, { id: 2, code: 'ZD-EHS-002', name: '记录管理程序', version: 'V1.0' }],
      forms: [{ id: 1, code: 'FM-EHS-001', name: '文件审批单' }, { id: 2, code: 'FM-EHS-002', name: '文件发放记录表' }],
      sops: [{ id: 1, code: 'SOP-EHS-001', name: '文件编写规范', version: 'V1.0' }]
    },
    ownerDept: 'EHS部', createTime: '2024-01-15', updateTime: '2024-03-20', version: 'V2.0'
  },
  {
    id: 2, type: 'flow', code: 'FP090501', name: '隐患排查、整改、验证闭环主流程', status: 'active',
    systems: ['safety'],
    relations: {
      policies: [{ id: 3, code: 'ZD-EHS-003', name: '隐患排查治理制度', version: 'V1.0' }],
      forms: [{ id: 3, code: 'FM-EHS-003', name: '隐患排查表' }, { id: 4, code: 'FM-EHS-004', name: '整改通知单' }],
      sops: [{ id: 2, code: 'SOP-EHS-002', name: '隐患排查作业指导书', version: 'V1.0' }]
    },
    ownerDept: 'EHS部', createTime: '2024-02-01', updateTime: '2024-04-01', version: 'V2.0'
  },
  {
    id: 3, type: 'policy', code: 'ZD-EHS-001', name: '文件控制程序', status: 'active',
    systems: ['safety', 'environment', 'health', 'energy'],
    relations: { flows: [{ id: 1, code: 'FP090101', name: 'EHS体系文件全生命周期主流程' }], forms: [{ id: 1, code: 'FM-EHS-001', name: '文件审批单' }] },
    ownerDept: 'EHS部', createTime: '2024-01-01', updateTime: '2024-03-15', version: 'V2.0'
  },
  {
    id: 4, type: 'flow', code: 'FP100401', name: '节能改善项目立项-实施-验证-固化主流程', status: 'active',
    systems: ['energy'],
    relations: {
      policies: [{ id: 4, code: 'ZD-ENE-001', name: '能源管理程序', version: 'V1.0' }],
      forms: [{ id: 5, code: 'FM-ENE-001', name: '节能项目立项表' }, { id: 6, code: 'FM-ENE-002', name: '节能效果验证表' }],
      sops: [{ id: 3, code: 'SOP-ENE-001', name: '节能项目管理规范', version: 'V1.0' }]
    },
    ownerDept: '能源管理部', createTime: '2024-03-01', updateTime: '2024-04-15', version: 'V1.0'
  },
  {
    id: 5, type: 'flow', code: 'FP060101', name: '来料检验判定与放行主流程', status: 'active',
    systems: ['common'],
    relations: {
      policies: [{ id: 5, code: 'ZD-QA-001', name: '来料检验规程', version: 'V3.0' }],
      forms: [{ id: 7, code: 'FM-QA-001', name: '来料检验报告' }, { id: 8, code: 'FM-QA-002', name: '让步接收申请单' }],
      sops: [{ id: 4, code: 'SOP-QA-001', name: 'IQC检验作业指导书', version: 'V2.0' }]
    },
    ownerDept: '品质部', createTime: '2024-01-10', updateTime: '2024-03-28', version: 'V3.0'
  },
  {
    id: 6, type: 'flow', code: 'FP040101', name: '生产订单下达与排程主流程', status: 'active',
    systems: ['common'],
    relations: {
      policies: [{ id: 6, code: 'ZD-PRO-001', name: '生产计划管理程序', version: 'V2.0' }],
      forms: [{ id: 9, code: 'FM-PRO-001', name: '生产计划表' }, { id: 10, code: 'FM-PRO-002', name: '生产工单' }],
      sops: [{ id: 5, code: 'SOP-PRO-001', name: '生产排程作业指导书', version: 'V1.0' }]
    },
    ownerDept: '生产中心', createTime: '2024-01-05', updateTime: '2024-03-15', version: 'V2.0'
  },
  {
    id: 7, type: 'flow', code: 'FP050101', name: 'SMT首件确认主流程', status: 'active',
    systems: ['safety'],
    relations: {
      policies: [{ id: 7, code: 'ZD-SMT-001', name: 'SMT首件检验规程', version: 'V2.0' }],
      forms: [{ id: 11, code: 'FM-SMT-001', name: '首件检验报告' }, { id: 12, code: 'FM-SMT-002', name: '首件确认单' }],
      sops: [{ id: 6, code: 'SOP-SMT-001', name: 'SMT首件检验作业指导书', version: 'V2.0' }]
    },
    ownerDept: 'SMT车间', createTime: '2024-01-08', updateTime: '2024-03-20', version: 'V2.0'
  },
  {
    id: 8, type: 'flow', code: 'FP060401', name: '不良品隔离、返工、报废闭环主流程', status: 'active',
    systems: ['common'],
    relations: {
      policies: [{ id: 8, code: 'ZD-QA-002', name: '不良品管理程序', version: 'V2.0' }],
      forms: [{ id: 13, code: 'FM-QA-003', name: '不良品处置单' }, { id: 14, code: 'FM-QA-004', name: '返工记录表' }],
      sops: [{ id: 7, code: 'SOP-QA-002', name: '不良品判定作业指导书', version: 'V1.0' }]
    },
    ownerDept: '品质部', createTime: '2024-01-12', updateTime: '2024-03-25', version: 'V2.0'
  }
])
const commonFlows = ref([
  { id: 1, code: 'FP-COM-001', name: '文件控制流程', description: '跨体系通用文件管理流程', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 2, code: 'FP-COM-002', name: '培训管理流程', description: '员工培训计划与实施', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 3, code: 'FP-COM-003', name: '内部审核流程', description: '体系内部审核管理', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 4, code: 'FP-COM-004', name: '不符合项整改流程', description: '审核不符合项整改闭环', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 5, code: 'FP-COM-005', name: '管理评审流程', description: '年度管理评审', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 6, code: 'FP-COM-006', name: '持续改进流程', description: 'QC改善、提案管理', systems: ['safety', 'environment', 'health', 'energy'], status: 'active' },
  { id: 7, code: 'FP040101', name: '生产订单下达与排程主流程', description: '从订单接收到生产排程的完整流程', systems: ['common'], status: 'active' },
  { id: 8, code: 'FP060101', name: '来料检验判定与放行主流程', description: '来料验收、检验、判定、放行', systems: ['common'], status: 'active' },
  { id: 9, code: 'FP060301', name: '成品检验与出货放行主流程', description: '成品检验、出货检验、放行审批', systems: ['common'], status: 'active' },
  { id: 10, code: 'FP060401', name: '不良品隔离、返工、报废闭环主流程', description: '不良品处理全流程', systems: ['common'], status: 'active' }
])
const safetyFlows = ref([
  { id: 1, code: 'FP-SAF-001', name: '作业许可流程', description: '特殊作业安全许可', clause: 'ISO45001-8.1.3', status: 'active' },
  { id: 2, code: 'FP090501', name: '隐患排查、整改、验证闭环主流程', description: '安全隐患识别与整改', clause: 'ISO45001-6.1.2', status: 'active' },
  { id: 3, code: 'FP-SAF-003', name: '事故报告调查流程', description: '安全事故报告与调查', clause: 'ISO45001-8.2', status: 'active' },
  { id: 4, code: 'FP-SAF-004', name: '安全检查流程', description: '日常安全巡检', clause: 'ISO45001-9.2', status: 'active' },
  { id: 5, code: 'FP050501', name: 'ESD日常检查与异常处置主流程', description: '静电防护日常检查与异常处理', clause: 'IEC61340-5-1', status: 'active' },
  { id: 6, code: 'FP050601', name: '无尘车间温湿度/洁净度管控主流程', description: '环境参数监控与调整', clause: 'ISO14644', status: 'active' },
  { id: 7, code: 'FP050101', name: 'SMT首件确认主流程', description: '首件制作、检验、确认放行', clause: 'IPC-A-610', status: 'active' },
  { id: 8, code: 'FP090101', name: 'EHS体系文件全生命周期主流程', description: '文件编写、审批、发布、废止', clause: 'ISO45001-7.5', status: 'active' }
])
const envFlows = ref([
  { id: 1, code: 'FP-ENV-001', name: '环境因素识别流程', description: '环境因素识别与评价', clause: 'ISO14001-6.1.2', status: 'active' },
  { id: 2, code: 'FP-ENV-002', name: '废弃物管理流程', description: '废弃物分类与处置', clause: 'ISO14001-8.1', status: 'active' },
  { id: 3, code: 'FP-ENV-003', name: '环境监测流程', description: '环境指标监测', clause: 'ISO14001-9.1', status: 'active' },
  { id: 4, code: 'FP-ENV-004', name: '化学品管理流程', description: '危险化学品管控', clause: 'ISO14001-8.1', status: 'active' },
  { id: 5, code: 'FP090101', name: 'EHS体系文件全生命周期主流程', description: '文件编写、审批、发布、废止', clause: 'ISO14001-7.5', status: 'active' },
  { id: 6, code: 'FP090701', name: '内外部审核不符合项整改主流程', description: '审核发现问题的整改闭环', clause: 'ISO14001-9.2', status: 'active' }
])
const healthFlows = ref([
  { id: 1, code: 'FP-HLT-001', name: '职业病危害识别流程', description: '职业病危害因素识别', clause: 'ISO45001-6.1.2', status: 'active' },
  { id: 2, code: 'FP-HLT-002', name: '职业健康体检流程', description: '员工职业健康检查', clause: 'ISO45001-8.1.4', status: 'active' },
  { id: 3, code: 'FP-HLT-003', name: '劳保用品管理流程', description: '劳动防护用品发放与使用', clause: 'ISO45001-8.1.3', status: 'active' },
  { id: 4, code: 'FP-HLT-004', name: '健康监护流程', description: '职业健康监护管理', clause: 'ISO45001-8.1.4', status: 'active' },
  { id: 5, code: 'FP090101', name: 'EHS体系文件全生命周期主流程', description: '文件编写、审批、发布、废止', clause: 'ISO45001-7.5', status: 'active' },
  { id: 6, code: 'FP090702', name: '年度管理评审闭环主流程', description: '年度管理评审全过程', clause: 'ISO45001-9.3', status: 'active' }
])
const energyFlows = ref([
  { id: 1, code: 'FP-ENE-001', name: '能源评审流程', description: '年度能源评审', clause: 'ISO50001-4.4.3', status: 'active' },
  { id: 2, code: 'FP100101', name: '能耗数据采集、核对、汇总主流程', description: '能耗数据的采集与汇总', clause: 'ISO50001-6.4', status: 'active' },
  { id: 3, code: 'FP100401', name: '节能改善项目立项-实施-验证-固化主流程', description: '节能措施实施与验证', clause: 'ISO50001-6.5', status: 'active' },
  { id: 4, code: 'FP-ENE-004', name: '能源计量管理流程', description: '能源计量器具管理', clause: 'ISO50001-6.4.1', status: 'active' },
  { id: 5, code: 'FP100301', name: '能耗异常分析与纠偏主流程', description: '能耗异常识别与纠正', clause: 'ISO50001-6.4', status: 'active' }
])
const assetLedgerData = ref([
  { id: 1, assetType: 'flow', code: 'FP090101', name: 'EHS体系文件全生命周期主流程', version: 'V2.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-01-15', updateTime: '2024-03-20' },
  { id: 2, assetType: 'policy', code: 'ZD-EHS-001', name: '文件控制程序', version: 'V2.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-01-01', updateTime: '2024-03-15' },
  { id: 3, assetType: 'form', code: 'FM-EHS-001', name: '文件审批单', version: 'V1.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-01-05', updateTime: '2024-01-05' },
  { id: 4, assetType: 'sop', code: 'SOP-EHS-001', name: '文件编写规范', version: 'V1.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-01-10', updateTime: '2024-01-10' },
  { id: 5, assetType: 'flow', code: 'FP090501', name: '隐患排查、整改、验证闭环主流程', version: 'V2.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-02-01', updateTime: '2024-04-01' },
  { id: 6, assetType: 'flow', code: 'FP100401', name: '节能改善项目立项-实施-验证-固化主流程', version: 'V1.0', ownerDept: '能源管理部', status: 'active', createTime: '2024-03-01', updateTime: '2024-04-15' },
  { id: 7, assetType: 'policy', code: 'ZD-ENE-001', name: '能源管理程序', version: 'V1.0', ownerDept: '能源管理部', status: 'active', createTime: '2024-02-15', updateTime: '2024-02-15' },
  { id: 8, assetType: 'form', code: 'FM-ENE-001', name: '节能项目立项表', version: 'V1.0', ownerDept: '能源管理部', status: 'active', createTime: '2024-03-05', updateTime: '2024-03-05' },
  { id: 9, assetType: 'flow', code: 'FP040101', name: '生产订单下达与排程主流程', version: 'V2.0', ownerDept: '生产中心', status: 'active', createTime: '2024-01-05', updateTime: '2024-03-15' },
  { id: 10, assetType: 'flow', code: 'FP050101', name: 'SMT首件确认主流程', version: 'V2.0', ownerDept: 'SMT车间', status: 'active', createTime: '2024-01-08', updateTime: '2024-03-20' },
  { id: 11, assetType: 'flow', code: 'FP060101', name: '来料检验判定与放行主流程', version: 'V3.0', ownerDept: '品质部', status: 'active', createTime: '2024-01-10', updateTime: '2024-03-28' },
  { id: 12, assetType: 'flow', code: 'FP060301', name: '成品检验与出货放行主流程', version: 'V2.0', ownerDept: '品质部', status: 'active', createTime: '2024-01-12', updateTime: '2024-03-25' },
  { id: 13, assetType: 'flow', code: 'FP060401', name: '不良品隔离、返工、报废闭环主流程', version: 'V2.0', ownerDept: '品质部', status: 'active', createTime: '2024-01-12', updateTime: '2024-03-25' },
  { id: 14, assetType: 'flow', code: 'FP050501', name: 'ESD日常检查与异常处置主流程', version: 'V1.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-02-05', updateTime: '2024-03-10' },
  { id: 15, assetType: 'flow', code: 'FP050601', name: '无尘车间温湿度/洁净度管控主流程', version: 'V1.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-02-10', updateTime: '2024-03-15' },
  { id: 16, assetType: 'flow', code: 'FP090701', name: '内外部审核不符合项整改主流程', version: 'V2.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-02-15', updateTime: '2024-04-01' },
  { id: 17, assetType: 'flow', code: 'FP090702', name: '年度管理评审闭环主流程', version: 'V1.0', ownerDept: 'EHS部', status: 'active', createTime: '2024-02-20', updateTime: '2024-04-05' },
  { id: 18, assetType: 'flow', code: 'FP100101', name: '能耗数据采集、核对、汇总主流程', version: 'V1.0', ownerDept: '能源管理部', status: 'active', createTime: '2024-02-25', updateTime: '2024-04-10' },
  { id: 19, assetType: 'flow', code: 'FP100301', name: '能耗异常分析与纠偏主流程', version: 'V1.0', ownerDept: '能源管理部', status: 'active', createTime: '2024-02-28', updateTime: '2024-04-12' },
  { id: 20, assetType: 'policy', code: 'ZD-QA-001', name: '来料检验规程', version: 'V3.0', ownerDept: '品质部', status: 'active', createTime: '2024-01-08', updateTime: '2024-03-20' }
])
const getAssetTagType = (type) => ({ flow: 'primary', policy: 'success', form: 'info', sop: 'warning' }[type] || 'info')
const getAssetTypeName = (type) => ({ flow: '流程', policy: '制度', form: '表单', sop: '作业文件' }[type] || type)
const getSystemName = (system) => ({
  safety: '安全管理', environment: '环境管理', health: '职业健康', energy: '能源管理', common: '通用流程'
}[system] || system)
const getStandardTagType = (standard) => ({
  'ISO9001': 'primary', 'ISO14001': 'success', 'ISO45001': 'warning', 'ISO50001': 'info', '安全生产标准化': 'danger'
}[standard] || 'info')
const getStandardName = (standard) => ({
  'ISO9001': '质量管理体系', 'ISO14001': '环境管理体系', 'ISO45001': '职业健康安全',
  'ISO50001': '能源管理体系', '安全生产标准化': '安全生产标准'
}[standard] || standard)
const getRiskTagType = (level) => ({ '高': 'danger', '中': 'warning', '低': 'success' }[level] || 'info')
const viewAssetDetail = (asset) => {
  selectedAsset.value = { ...asset, compliance: [
    { id: 1, standard: 'ISO45001', clause: '4.4', description: '文件控制', riskLevel: '中' },
    { id: 2, standard: 'ISO14001', clause: '4.4.5', description: '文件控制', riskLevel: '中' }
  ]}
  assetDetailDrawerVisible.value = true
}
const editAssetBinding = (asset) => { ElMessage.info(`编辑绑定: ${asset.name}`) }
const addCommonFlow = () => { ElMessage.info('添加通用流程') }
const viewSystemFlow = (row) => { ElMessage.info(`查看流程详情: ${row.name}`) }
const refreshAssetLedger = () => { ElMessage.success('台账已刷新') }
const viewAssetLedgerDetail = (row) => { ElMessage.info(`查看台账详情: ${row.name}`) }

// ========== 制度查阅相关 (来自 ProcessDocument.vue) ==========
const docSearch = reactive({ keyword: '', category: '' })
const documentList = ref([
  { id: 1, docCode: 'ZD-GL-001', docName: '四标一体化管理手册', category: '体系手册', version: 'V2.0', deptName: '综合管理部', status: '有效', effectiveDate: '2026-01-01', nextReview: '2027-01-01', viewCount: 1256, downloadCount: 89, isCollected: false },
  { id: 2, docCode: 'ZD-SC-001', docName: '生产作业控制程序', category: '程序文件', version: 'V3.1', deptName: '生产部', status: '有效', effectiveDate: '2026-03-15', nextReview: '2026-09-15', viewCount: 876, downloadCount: 45, isCollected: false },
  { id: 3, docCode: 'ZD-QA-001', docName: '进料检验作业指导书', category: '作业SOP', version: 'V1.5', deptName: '品质部', status: '有效', effectiveDate: '2026-02-01', nextReview: '2026-08-01', viewCount: 432, downloadCount: 28, isCollected: false },
  { id: 4, docCode: 'ZD-HR-001', docName: '员工培训管理制度', category: '管理制度', version: 'V2.5', deptName: '人力资源部', status: '有效', effectiveDate: '2026-04-01', nextReview: '2026-10-01', viewCount: 789, downloadCount: 56, isCollected: false },
  { id: 5, docCode: 'ZD-EHS-001', docName: '文件控制程序', category: '程序文件', version: 'V2.0', deptName: 'EHS部', status: '有效', effectiveDate: '2026-01-01', nextReview: '2027-01-01', viewCount: 1102, downloadCount: 78, isCollected: false },
  { id: 6, docCode: 'ZD-EHS-002', docName: '隐患排查治理制度', category: '管理制度', version: 'V1.0', deptName: 'EHS部', status: '有效', effectiveDate: '2026-02-01', nextReview: '2026-08-01', viewCount: 654, downloadCount: 42, isCollected: false },
  { id: 7, docCode: 'ZD-PRO-001', docName: '生产计划管理程序', category: '程序文件', version: 'V2.0', deptName: '生产中心', status: '有效', effectiveDate: '2026-01-05', nextReview: '2026-07-05', viewCount: 543, downloadCount: 36, isCollected: false },
  { id: 8, docCode: 'ZD-QA-002', docName: '不良品管理程序', category: '程序文件', version: 'V2.0', deptName: '品质部', status: '有效', effectiveDate: '2026-01-12', nextReview: '2026-07-12', viewCount: 489, downloadCount: 31, isCollected: false },
  { id: 9, docCode: 'ZD-ENE-001', docName: '能源管理程序', category: '程序文件', version: 'V1.0', deptName: '能源管理部', status: '有效', effectiveDate: '2026-02-15', nextReview: '2026-08-15', viewCount: 321, downloadCount: 22, isCollected: false },
  { id: 10, docCode: 'ZD-SMT-001', docName: 'SMT首件检验规程', category: '作业SOP', version: 'V2.0', deptName: 'SMT车间', status: '有效', effectiveDate: '2026-01-08', nextReview: '2026-07-08', viewCount: 567, downloadCount: 38, isCollected: false }
])
const searchDocument = () => { ElMessage.info('搜索制度') }
const viewDocument = (row) => { ElMessage.info(`查阅: ${row.docName}`) }
const borrowDocument = (row) => { ElMessage.success(`借阅成功: ${row.docName}`) }
const collectDocument = (row) => {
  row.isCollected = !row.isCollected
  ElMessage.success(row.isCollected ? `已收藏: ${row.docName}` : `已取消收藏: ${row.docName}`)
}

// ========== 五级流程架构相关 (对标杰成EPROS十域架构) ==========
const archLevelTab = ref('level1')
const level1Data = ref([
  { code: 'DOMAIN-01', name: '01-战略经营流程域', dept: '总经理', desc: '企业战略规划、经营决策、目标管控', status: '有效', flowCount: 12 },
  { code: 'DOMAIN-02', name: '02-产品研发流程域', dept: '研发部', desc: '产品规划、设计、开发、验证、上市管理', status: '有效', flowCount: 18 },
  { code: 'DOMAIN-03', name: '03-供应链采购流程域', dept: '采购部', desc: '供应商管理、采购执行、仓储物流', status: '有效', flowCount: 15 },
  { code: 'DOMAIN-04', name: '04-生产制造流程域', dept: '生产部', desc: '生产计划、排程、车间作业、生产异常', status: '有效', flowCount: 25 },
  { code: 'DOMAIN-05', name: '05-SMT工艺制程流程域', dept: 'SMT部', desc: 'SMT上料、贴片、回流焊、AOI、ESD管控', status: '有效', flowCount: 20 },
  { code: 'DOMAIN-06', name: '06-品质管控流程域', dept: '品质部', desc: 'IQC/IPQC/FQC/OQC、不良品处置、客诉', status: '有效', flowCount: 22 },
  { code: 'DOMAIN-07', name: '07-设备工装流程域', dept: '设备部', desc: '设备点检、维护、保养、工装夹具管理', status: '有效', flowCount: 14 },
  { code: 'DOMAIN-08', name: '08-仓储物流流程域', dept: '仓储部', desc: '物料收发、库存管理、出入库管控', status: '有效', flowCount: 16 },
  { code: 'DOMAIN-09', name: '09-EHS合规管理流程域', dept: 'EHS部', desc: '环境/健康/安全/应急/体系审核合规', status: '有效', flowCount: 28 },
  { code: 'DOMAIN-10', name: '10-能源管控流程域', dept: '设备部', desc: '能耗监测、异常分析、节能改善管控', status: '有效', flowCount: 12 }
])
const level2Data = ref([
  // 04-生产制造流程域
  { code: 'GRP-04-01', name: '生产计划管理组', parentDomain: '04-生产制造流程域', dept: '生产部', status: '有效' },
  { code: 'GRP-04-02', name: '生产排程与换线组', parentDomain: '04-生产制造流程域', dept: '生产部', status: '有效' },
  { code: 'GRP-04-03', name: '车间作业执行组', parentDomain: '04-生产制造流程域', dept: '车间', status: '有效' },
  { code: 'GRP-04-04', name: '生产异常处理组', parentDomain: '04-生产制造流程域', dept: '生产部', status: '有效' },
  { code: 'GRP-04-05', name: '生产追溯管理组', parentDomain: '04-生产制造流程域', dept: '品质部', status: '有效' },
  // 05-SMT工艺制程流程域
  { code: 'GRP-05-01', name: 'SMT上料防错组', parentDomain: '05-SMT工艺制程流程域', dept: 'SMT部', status: '有效' },
  { code: 'GRP-05-02', name: '贴片制程管控组', parentDomain: '05-SMT工艺制程流程域', dept: 'SMT部', status: '有效' },
  { code: 'GRP-05-03', name: '回流焊制程管控组', parentDomain: '05-SMT工艺制程流程域', dept: 'SMT部', status: '有效' },
  { code: 'GRP-05-04', name: 'AOI检测管控组', parentDomain: '05-SMT工艺制程流程域', dept: 'SMT部', status: '有效' },
  { code: 'GRP-05-05', name: 'ESD静电防护管控组', parentDomain: '05-SMT工艺制程流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-05-06', name: '无尘车间环境管控组', parentDomain: '05-SMT工艺制程流程域', dept: 'EHS部', status: '有效' },
  // 06-品质管控流程域
  { code: 'GRP-06-01', name: '来料IQC检验组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  { code: 'GRP-06-02', name: '制程IPQC巡检组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  { code: 'GRP-06-03', name: '成品FQC/OQC检验组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  { code: 'GRP-06-04', name: '不良品处置管控组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  { code: 'GRP-06-05', name: '客户稽核与客诉组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  { code: 'GRP-06-06', name: '品质持续改进组', parentDomain: '06-品质管控流程域', dept: '品质部', status: '有效' },
  // 09-EHS合规管理流程域
  { code: 'GRP-09-01', name: '体系文件管控组', parentDomain: '09-EHS合规管理流程域', dept: '综合管理部', status: '有效' },
  { code: 'GRP-09-02', name: '风险与危险源管控组', parentDomain: '09-EHS合规管理流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-09-03', name: '环境因素与合规管控组', parentDomain: '09-EHS合规管理流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-09-04', name: '职业健康管控组', parentDomain: '09-EHS合规管理流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-09-05', name: '隐患排查整改组', parentDomain: '09-EHS合规管理流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-09-06', name: '应急管理组', parentDomain: '09-EHS合规管理流程域', dept: 'EHS部', status: '有效' },
  { code: 'GRP-09-07', name: '体系审核与评审组', parentDomain: '09-EHS合规管理流程域', dept: '综合管理部', status: '有效' },
  // 10-能源管控流程域
  { code: 'GRP-10-01', name: '能源数据采集组', parentDomain: '10-能源管控流程域', dept: '设备部', status: '有效' },
  { code: 'GRP-10-02', name: '能耗统计分析组', parentDomain: '10-能源管控流程域', dept: '设备部', status: '有效' },
  { code: 'GRP-10-03', name: '能耗异常管控组', parentDomain: '10-能源管控流程域', dept: '设备部', status: '有效' }
])
const level3Data = ref([
  { code: 'LY-04-01-001', name: '生产订单下达与排程主流程', domain: '04-生产制造流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-04-02-001', name: '生产计划编制与下发主流程', domain: '04-生产制造流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-05-02-001', name: 'SMT生产换线确认主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-05-02-002', name: 'SMT首件确认主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: true, hasDoc: false, status: '待更新' },
  { code: 'LY-05-03-001', name: '回流焊温度参数管控主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-05-04-001', name: 'AOI检测与误判复核主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: true, hasDoc: false, status: '待更新' },
  { code: 'LY-04-04-001', name: '制程异常停机处置主流程', domain: '04-生产制造流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-04-05-001', name: '生产追溯与批次管控主流程', domain: '04-生产制造流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-06-01-001', name: '来料检验判定与放行主流程', domain: '06-品质管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-06-02-001', name: '制程巡检IPQC管控主流程', domain: '06-品质管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-06-03-001', name: '成品检验与出货放行主流程', domain: '06-品质管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-06-04-001', name: '不良品隔离、返工、报废闭环主流程', domain: '06-品质管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: false, status: '待更新' },
  { code: 'LY-06-05-001', name: '客户稽核与客诉处理主流程', domain: '06-品质管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-05-05-001', name: 'ESD日常检查与异常处置主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-05-06-001', name: '无尘车间温湿度/洁净度管控主流程', domain: '05-SMT工艺制程流程域', type: '核心主流程', hasFlowchart: false, hasDoc: false, status: '草稿' },
  { code: 'LY-09-01-001', name: 'EHS体系文件全生命周期主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-09-02-001', name: '风险源辨识与管控主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-09-05-001', name: '隐患排查、整改、验证闭环主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-09-06-001', name: '应急响应与处置主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-09-07-001', name: '内外部审核不符合项整改主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: false, status: '待更新' },
  { code: 'LY-09-07-002', name: '年度管理评审闭环主流程', domain: '09-EHS合规管理流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-10-01-001', name: '能耗数据采集、核对、汇总主流程', domain: '10-能源管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-10-02-001', name: '能耗异常分析与纠偏主流程', domain: '10-能源管控流程域', type: '核心主流程', hasFlowchart: true, hasDoc: false, status: '待更新' },
  { code: 'LY-10-03-001', name: '节能改善项目立项-实施-验证-固化主流程', domain: '10-能源管控流程域', type: '核心主流程', hasFlowchart: false, hasDoc: false, status: '草稿' },
  { code: 'LY-07-01-001', name: '设备日常点检与开机确认主流程', domain: '07-设备工装流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-07-01-002', name: '设备维护保养计划执行主流程', domain: '07-设备工装流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-08-01-001', name: '物料入库检验与入库主流程', domain: '08-仓储物流流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' },
  { code: 'LY-08-01-002', name: '成品入库与出库管控主流程', domain: '08-仓储物流流程域', type: '核心主流程', hasFlowchart: true, hasDoc: true, status: '有效' }
])
const level4Data = ref([
  { code: 'LY-05-02-001-01', name: 'SMT物料上料核对子流程', parentFlow: 'SMT生产换线确认主流程', domain: '05-SMT工艺制程流程域', reuseCount: 8, status: '有效' },
  { code: 'LY-05-03-001-01', name: '炉温参数异常调整子流程', parentFlow: '回流焊温度参数管控主流程', domain: '05-SMT工艺制程流程域', reuseCount: 5, status: '有效' },
  { code: 'LY-05-04-001-01', name: 'AOI误判复核子流程', parentFlow: 'AOI检测与误判复核主流程', domain: '05-SMT工艺制程流程域', reuseCount: 6, status: '有效' },
  { code: 'LY-05-05-001-01', name: '车间静电超标处置子流程', parentFlow: 'ESD日常检查与异常处置主流程', domain: '05-SMT工艺制程流程域', reuseCount: 9, status: '有效' },
  { code: 'LY-06-01-001-01', name: '来料不合格让步接收子流程', parentFlow: '来料检验判定与放行主流程', domain: '06-品质管控流程域', reuseCount: 4, status: '有效' },
  { code: 'LY-06-04-001-01', name: '不良品返工确认子流程', parentFlow: '不良品隔离、返工、报废闭环主流程', domain: '06-品质管控流程域', reuseCount: 7, status: '有效' },
  { code: 'LY-06-04-001-02', name: '不良品报废审批子流程', parentFlow: '不良品隔离、返工、报废闭环主流程', domain: '06-品质管控流程域', reuseCount: 6, status: '有效' },
  { code: 'LY-04-04-001-01', name: '制程不良追溯分析子流程', parentFlow: '制程异常停机处置主流程', domain: '04-生产制造流程域', reuseCount: 10, status: '有效' },
  { code: 'LY-09-01-001-01', name: '文件修订发起子流程', parentFlow: 'EHS体系文件全生命周期主流程', domain: '09-EHS合规管理流程域', reuseCount: 15, status: '有效' },
  { code: 'LY-09-01-001-02', name: '文件作废归档子流程', parentFlow: 'EHS体系文件全生命周期主流程', domain: '09-EHS合规管理流程域', reuseCount: 12, status: '有效' },
  { code: 'LY-09-02-001-01', name: '危险源辨识评价子流程', parentFlow: '风险源辨识与管控主流程', domain: '09-EHS合规管理流程域', reuseCount: 8, status: '有效' },
  { code: 'LY-09-05-001-01', name: '隐患分级判定子流程', parentFlow: '隐患排查、整改、验证闭环主流程', domain: '09-EHS合规管理流程域', reuseCount: 11, status: '有效' },
  { code: 'LY-09-05-001-02', name: '整改措施验证子流程', parentFlow: '隐患排查、整改、验证闭环主流程', domain: '09-EHS合规管理流程域', reuseCount: 9, status: '有效' },
  { code: 'LY-10-01-001-01', name: '能耗抄表异常复核子流程', parentFlow: '能耗数据采集、核对、汇总主流程', domain: '10-能源管控流程域', reuseCount: 5, status: '有效' },
  { code: 'LY-10-03-001-01', name: '节能效果数据验证子流程', parentFlow: '节能改善项目立项-实施-验证-固化主流程', domain: '10-能源管控流程域', reuseCount: 3, status: '有效' },
  { code: 'LY-07-01-002-01', name: '设备保养计划编制子流程', parentFlow: '设备维护保养计划执行主流程', domain: '07-设备工装流程域', reuseCount: 4, status: '有效' },
  { code: 'LY-08-01-001-01', name: '物料标签打印与粘贴子流程', parentFlow: '物料入库检验与入库主流程', domain: '08-仓储物流流程域', reuseCount: 7, status: '有效' }
])
const level5Data = ref([
  { code: 'SOP-05-02-001-01-01', name: 'SMT操作员上料操作流程', position: 'SMT操作员', parentSubFlow: 'SMT物料上料核对子流程', domain: '05-SMT工艺制程流程域', version: '2.0', status: '有效' },
  { code: 'SOP-05-02-001-01-02', name: 'SMT物料员物料核对操作流程', position: '物料员', parentSubFlow: 'SMT物料上料核对子流程', domain: '05-SMT工艺制程流程域', version: '1.8', status: '有效' },
  { code: 'SOP-05-03-001-01-01', name: '炉温曲线设定与调校操作流程', position: '工艺工程师', parentSubFlow: '炉温参数异常调整子流程', domain: '05-SMT工艺制程流程域', version: '2.1', status: '有效' },
  { code: 'SOP-06-02-001-01-01', name: 'IPQC首件巡检操作流程', position: 'IPQC', parentSubFlow: '制程巡检IPQC管控主流程', domain: '06-品质管控流程域', version: '1.5', status: '有效' },
  { code: 'SOP-06-02-001-01-02', name: 'IPQC巡检记录填写操作流程', position: 'IPQC', parentSubFlow: '制程巡检IPQC管控主流程', domain: '06-品质管控流程域', version: '1.5', status: '有效' },
  { code: 'SOP-04-04-001-01-01', name: '设备操作员开机点检流程', position: '设备操作员', parentSubFlow: '制程不良追溯分析子流程', domain: '04-生产制造流程域', version: '3.0', status: '有效' },
  { code: 'SOP-04-04-001-01-02', name: '生产异常快速响应操作流程', position: '班组长', parentSubFlow: '制程不良追溯分析子流程', domain: '04-生产制造流程域', version: '2.5', status: '有效' },
  { code: 'SOP-09-05-001-01-01', name: '车间安全员日常隐患排查操作流程', position: '车间安全员', parentSubFlow: '隐患分级判定子流程', domain: '09-EHS合规管理流程域', version: '2.1', status: '有效' },
  { code: 'SOP-09-05-001-02-01', name: '整改责任人整改执行操作流程', position: '整改责任人', parentSubFlow: '整改措施验证子流程', domain: '09-EHS合规管理流程域', version: '1.8', status: '有效' },
  { code: 'SOP-09-01-001-01-01', name: 'EHS专员文件发布与培训流程', position: 'EHS专员', parentSubFlow: '文件修订发起子流程', domain: '09-EHS合规管理流程域', version: '1.8', status: '有效' },
  { code: 'SOP-10-01-001-01-01', name: '能源管理员抄表与数据核对流程', position: '能源管理员', parentSubFlow: '能耗抄表异常复核子流程', domain: '10-能源管控流程域', version: '2.0', status: '有效' },
  { code: 'SOP-06-04-001-01-01', name: '品质员不良品判定操作流程', position: '品质员', parentSubFlow: '不良品返工确认子流程', domain: '06-品质管控流程域', version: '2.2', status: '有效' },
  { code: 'SOP-06-01-001-01-01', name: '仓管员物料收发合规操作流程', position: '仓管员', parentSubFlow: '来料不合格让步接收子流程', domain: '08-仓储物流流程域', version: '1.5', status: '有效' },
  { code: 'SOP-09-06-001-01-01', name: '行政应急物资点检操作流程', position: '行政人员', parentSubFlow: '应急响应与处置主流程', domain: '09-EHS合规管理流程域', version: '1.0', status: '有效' },
  { code: 'SOP-09-04-001-01-01', name: '车间员工职业健康防护操作流程', position: '车间员工', parentSubFlow: '隐患排查、整改、验证闭环主流程', domain: '09-EHS合规管理流程域', version: '2.0', status: '有效' },
  { code: 'SOP-07-01-001-01-01', name: '设备操作员设备点检标准操作流程', position: '设备操作员', parentSubFlow: '设备日常点检与开机确认主流程', domain: '07-设备工装流程域', version: '2.3', status: '有效' }
])
const viewLevelDetail = (row) => { ElMessage.info(`查看详情: ${row.name}`) }
const editLevelDetail = (row) => { ElMessage.info(`编辑: ${row.name}`) }
const viewLevelChildren = (row) => {
  ElMessage.info(`查看下级: ${row.name}`)
  archLevelTab.value = 'level2'
}
const designLevel3Flow = (row) => { ElMessage.info(`设计三级流程: ${row.name}`) }
const editLevelDoc = (row) => { ElMessage.info(`编辑说明书: ${row.name}`) }
const reviseLevel3 = (row) => { ElMessage.info(`修订三级流程: ${row.name}`) }
const designLevel4Flow = (row) => { ElMessage.info(`设计四级子流程: ${row.name}`) }
const reviseLevel4 = (row) => { ElMessage.info(`修订四级子流程: ${row.name}`) }
const editLevel5SOP = (row) => { ElMessage.info(`编辑五级SOP: ${row.name}`) }
const reviseLevel5 = (row) => { ElMessage.info(`修订五级SOP: ${row.name}`) }

// ========== 四位一体资产绑定管理 (新增) ==========
const assetBindTab = ref('bindList')
const assetBindData = ref([
  { flowCode: 'LY-04-01-001', flowName: '生产订单下达与排程主流程', level: '三级主流程', bindDoc: true, bindForm: true, bindSOP: true, bindRisk: true, completeness: '100%' },
  { flowCode: 'LY-05-02-001', flowName: 'SMT生产换线确认主流程', level: '三级主流程', bindDoc: true, bindForm: true, bindSOP: false, bindRisk: true, completeness: '75%' },
  { flowCode: 'LY-06-01-001', flowName: '来料检验判定与放行主流程', level: '三级主流程', bindDoc: true, bindForm: false, bindSOP: true, bindRisk: true, completeness: '75%' },
  { flowCode: 'LY-09-05-001', flowName: '隐患排查、整改、验证闭环主流程', level: '三级主流程', bindDoc: true, bindForm: true, bindSOP: true, bindRisk: true, completeness: '100%' }
])
const getBindStatus = (complete) => complete === '100%' ? 'success' : complete === '75%' ? 'warning' : 'danger'
const viewAssetBind = (row) => { ElMessage.info(`查看资产绑定: ${row.flowName}`) }
const editAssetBind = (row) => { ElMessage.info(`编辑资产绑定: ${row.flowName}`) }

// ========== 流程合规与风险管控 (新增) ==========
const riskCtrlTab = ref('riskList')
const riskLevelData = ref([
  { level: '红色风险', count: 2, desc: '重大风险，需立即整改', color: 'danger' },
  { level: '橙色风险', count: 5, desc: '较大风险，需重点管控', color: 'warning' },
  { level: '黄色风险', count: 12, desc: '一般风险，需常规管控', color: '' },
  { level: '蓝色风险', count: 25, desc: '低风险，需关注', color: 'info' }
])
const riskPointList = ref([
  { flowCode: 'LY-05-02-001', flowName: 'SMT生产换线确认主流程', riskPoint: '上料错误导致批量不良', riskLevel: '红色',防控措施: '双人核对+扫码确认', status: '已管控' },
  { flowCode: 'LY-05-03-001', flowName: '回流焊温度参数管控主流程', riskPoint: '温度超标导致虚焊', riskLevel: '橙色',防控措施: '实时监控+超温报警', status: '已管控' },
  { flowCode: 'LY-09-05-001', flowName: '隐患排查、整改、验证闭环主流程', riskPoint: '整改不到位引发事故', riskLevel: '红色',防控措施: '分级管控+验证闭环', status: '已管控' }
])
const viewRiskDetail = (row) => { ElMessage.info(`查看风险详情: ${row.riskPoint}`) }

// ========== 流程绩效考核 (新增) ==========
const kpiTab = ref('kpiOverview')
const kpiOverviewData = ref([
  { dept: 'SMT部', timelyRate: 96.5, rejectRate: 2.1, overdueCount: 3, formCompliance: 98.5 },
  { dept: '品质部', timelyRate: 94.2, rejectRate: 3.5, overdueCount: 5, formCompliance: 97.2 },
  { dept: '生产部', timelyRate: 92.8, rejectRate: 4.2, overdueCount: 8, formCompliance: 95.8 },
  { dept: 'EHS部', timelyRate: 98.1, rejectRate: 1.2, overdueCount: 1, formCompliance: 99.5 }
])
const kpiTrendData = ref([
  { month: '1月', timelyRate: 92.5, rejectRate: 4.5 },
  { month: '2月', timelyRate: 93.2, rejectRate: 4.1 },
  { month: '3月', timelyRate: 94.8, rejectRate: 3.8 },
  { month: '4月', timelyRate: 95.5, rejectRate: 3.2 },
  { month: '5月', timelyRate: 96.2, rejectRate: 2.8 },
  { month: '6月', timelyRate: 96.8, rejectRate: 2.5 }
])

// ========== 流程说明书相关 (新增) ==========
const processDocTab = ref('pending')
const docQuery = reactive({ flowCode: '', flowName: '', level: '', status: '' })
const pendingDocList = ref([
  { flowCode: 'LY-01-02-002', flowName: 'SMT首件确认主流程', level: '三级主流程', hasFlowchart: true },
  { flowCode: 'LY-02-03-001', flowName: '不良品隔离、返工、报废闭环主流程', level: '三级主流程', hasFlowchart: true },
  { flowCode: 'LY-03-02-001', flowName: '无尘车间温湿度/洁净度管控主流程', level: '三级主流程', hasFlowchart: false }
])
const createdDocList = ref([
  { flowCode: 'LY-01-01-001', flowName: '生产订单下达与排程主流程', level: '三级主流程', version: 'V2.0', status: '有效', effectiveDate: '2026-01-01' },
  { flowCode: 'LY-01-02-001', flowName: 'SMT生产换线确认主流程', level: '三级主流程', version: 'V3.0', status: '有效', effectiveDate: '2026-03-15' },
  { flowCode: 'LY-01-03-001', flowName: '制程异常停机处置主流程', level: '三级主流程', version: 'V1.5', status: '有效', effectiveDate: '2026-02-20' },
  { flowCode: 'LY-02-01-001', flowName: '来料检验判定与放行主流程', level: '三级主流程', version: 'V2.1', status: '有效', effectiveDate: '2026-04-01' }
])
const searchDoc = () => { ElMessage.info('搜索流程说明书') }
const resetDocQuery = () => { docQuery.flowCode = ''; docQuery.flowName = ''; docQuery.level = ''; docQuery.status = '' }
const createProcessDoc = (row) => { ElMessage.info(`创建说明书: ${row.flowName}`) }
const viewProcessDoc = (row) => { ElMessage.info(`查看说明书: ${row.flowName}`) }
const editProcessDoc = (row) => { ElMessage.info(`编辑说明书: ${row.flowName}`) }
const downloadProcessDoc = (row) => { ElMessage.success(`下载说明书: ${row.flowName}`) }
const reviseProcessDoc = (row) => { ElMessage.info(`修订说明书: ${row.flowName}`) }

// ========== 表单管理相关 (来自 FlowForm.vue) ==========
const formQuery = reactive({ name: '', category: '', page: 1, size: 10 })
const formTotal = ref(30)
const formLoading = ref(false)
const formPageList = ref([])

const loadFormData = () => {
  formLoading.value = true
  setTimeout(() => {
    formPageList.value = [
      { code: 'FORM001', name: '请假申请单', category: '人事类', fieldCount: 12, creator: '管理员', createTime: '2024-01-01', status: '已启用' },
      { code: 'FORM002', name: '费用报销单', category: '财务类', fieldCount: 15, creator: '管理员', createTime: '2024-01-02', status: '已启用' }
    ]
    formLoading.value = false
  }, 500)
}

const resetFormQuery = () => { formQuery.name = ''; formQuery.category = '' }
const handleFormEdit = (row) => { ElMessage.info(`编辑表单: ${row.name}`) }
const handleFormDesign = (row) => { ElMessage.info(`设计表单: ${row.name}`) }
const handleFormPreview = (row) => { ElMessage.info(`预览表单: ${row.name}`) }
const handleFormRemove = (row) => { ElMessage.success(`删除表单: ${row.name}`) }

// ========== 分类管理相关 (来自 FlowCategory.vue) ==========
const allExpanded = ref(true)
const expandedKeys = ref([])
const categoryTreeData = ref([
  { id: 1, level: 1, name: '生产管理', code: 'SC', flowCount: 10, manager: '张三', sort: 1, status: '启用', children: [
    { id: 11, level: 2, name: 'SMT生产', code: 'SC-SMT', flowCount: 5, manager: '李四', sort: 1, status: '启用' }
  ]},
  { id: 2, level: 1, name: 'EHS管理', code: 'EH', flowCount: 8, manager: '王五', sort: 2, status: '启用', children: [] },
  { id: 3, level: 1, name: '人力资源', code: 'HR', flowCount: 12, manager: '赵六', sort: 3, status: '启用', children: [] }
])
const totalGroups = ref(3)
const totalSub = ref(1)
const totalFlows = ref(30)
const totalInstances = ref(205)

const handleExpandChange = () => {}
const handleAddChild = (row) => { ElMessage.info(`新增子分类: ${row.name}`) }
const handleCategoryEdit = (row) => { ElMessage.info(`编辑分类: ${row.name}`) }
const handleViewCategoryFlows = (row) => { ElMessage.info(`查看分类流程: ${row.name}`) }
const handleToggleCategoryStatus = (row) => { ElMessage.success(`切换分类状态: ${row.name}`) }
const handleCategoryRemove = (row) => { ElMessage.success(`删除分类: ${row.name}`) }

// ========== 规则配置相关 (来自 FlowRule.vue) ==========
const ruleActiveTab = ref('approve')
const approveRules = ref([
  { code: 'RULE001', name: '金额超限审批', condition: '金额 > 50000', action: '升级到总经理', priority: 1, enabled: true },
  { code: 'RULE002', name: '紧急流程加速', condition: '优先级 = 紧急', action: '跳过中间节点', priority: 2, enabled: true }
])
const timeoutRules = ref([
  { code: 'T001', name: '普通审批超时', timeoutHours: 48, action: '发送提醒', notifyCount: 3, enabled: true },
  { code: 'T002', name: '紧急审批超时', timeoutHours: 24, action: '升级处理', notifyCount: 5, enabled: true }
])
const notifyRules = ref([
  { code: 'N001', name: '新任务通知', eventType: '任务到达', notifyType: '站内信', notifyTarget: '审批人', enabled: true },
  { code: 'N002', name: '超时提醒', eventType: '审批超时', notifyType: '短信', notifyTarget: '审批人', enabled: true }
])

const handleEditRule = (row) => { ElMessage.info(`编辑规则: ${row.name}`) }
const handleRemoveRule = (row) => { ElMessage.success(`删除规则: ${row.name}`) }

// ========== 人员指派相关 (来自 FlowAssignment.vue) ==========
const assignActiveTab = ref('approver')
const approverList = ref([
  { flowName: '请假申请', nodeName: '部门审批', approveType: '部门主管', approvers: '张三、李四', approveMode: '单人审批', timeoutHours: 24 },
  { flowName: '费用报销', nodeName: '财务审批', approveType: '指定人员', approvers: '财务专员', approveMode: '单人审批', timeoutHours: 48 }
])
const agentList = ref([
  { name: '张三', department: '研发中心', agent: '李四', startDate: '2024-01-01', endDate: '2024-12-31', scope: '全部流程', enabled: true },
  { name: '王五', department: '生产部', agent: '赵六', startDate: '2024-03-01', endDate: '2024-06-30', scope: '生产相关', enabled: true }
])
const roleList = ref([
  { code: 'ROLE001', name: '部门经理', type: '管理角色', members: '张三、李四、王五', memberCount: 3 },
  { code: 'ROLE002', name: '财务审批员', type: '职能角色', members: '赵六、钱七', memberCount: 2 }
])

const handleApproverEdit = (row) => { ElMessage.info(`编辑审批人员: ${row.flowName}`) }
const handleApproverRemove = (row) => { ElMessage.success(`删除审批人员: ${row.flowName}`) }
const handleAgentEdit = (row) => { ElMessage.info(`编辑代理人: ${row.name}`) }
const handleAgentRemove = (row) => { ElMessage.success(`删除代理人: ${row.name}`) }
const handleRoleEdit = (row) => { ElMessage.info(`编辑角色: ${row.name}`) }
const handleRoleRemove = (row) => { ElMessage.success(`删除角色: ${row.name}`) }

// ========== 待办任务相关 (来自 FlowTodo.vue) ==========
const activeTodoTab = ref('all')
const todoQuery = reactive({ flowName: '', priority: '', dateRange: [], page: 1, size: 10 })
const todoTotal = ref(25)
const todoLoading = ref(false)
const todoPageList = ref([])
const todoCount = ref(15)
const urgentCount = ref(3)
const overdueCount = ref(2)
const todayCount = ref(5)
const approvedCount = ref(8)
const delegateCount = ref(1)
const urgentList = ref([])
const overdueList = ref([])

const loadTodoData = () => {
  todoLoading.value = true
  setTimeout(() => {
    todoPageList.value = [
      { instanceNo: 'INS20240101', flowName: '请假申请', businessTitle: '张三-年假申请', nodeName: '部门审批', initiator: '张三', receiveTime: '2024-01-15 09:00', deadline: '2024-01-16 09:00', priority: '紧急' },
      { instanceNo: 'INS20240102', flowName: '费用报销', businessTitle: '李四-差旅费报销', nodeName: '财务审批', initiator: '李四', receiveTime: '2024-01-15 10:30', deadline: '2024-01-17 10:30', priority: '一般' }
    ]
    todoLoading.value = false
  }, 500)
}

const resetTodoQuery = () => { todoQuery.flowName = ''; todoQuery.priority = ''; todoQuery.dateRange = [] }
const isTodoOverdue = (row) => new Date(row.deadline) < new Date()
const getTodoOverdueDays = (row) => Math.floor((new Date() - new Date(row.deadline)) / (1000 * 60 * 60 * 24))
const handleTodoApprove = (row) => { ElMessage.info(`审批任务: ${row.businessTitle}`) }
const handleTodoDelegate = (row) => { ElMessage.info(`委托任务: ${row.businessTitle}`) }
const handleTodoComment = (row) => { ElMessage.info(`评论任务: ${row.businessTitle}`) }
const handleTodoDetail = (row) => { ElMessage.info(`查看详情: ${row.businessTitle}`) }
const handleTodoFlow = (row) => { ElMessage.info(`查看流程图: ${row.businessTitle}`) }

// ========== 已办任务相关 (来自 FlowDone.vue) ==========
const activeDoneTab = ref('all')
const doneQuery = reactive({ flowName: '', result: '', dateRange: [], page: 1, size: 10 })
const doneTotal = ref(100)
const donePageList = ref([])
const totalDone = ref(100)
const approvedDoneCount = ref(75)
const rejectedDoneCount = ref(15)
const returnedDoneCount = ref(10)
const todayDone = ref(8)
const approvedDoneList = ref([])
const rejectedDoneList = ref([])

const loadDoneData = () => {
  donePageList.value = [
    { instanceNo: 'INS20231201', flowName: '请假申请', businessTitle: '王五-事假申请', nodeName: '部门审批', initiator: '王五', approveTime: '2024-01-10 15:30', result: '通过', duration: '2小时' },
    { instanceNo: 'INS20231202', flowName: '费用报销', businessTitle: '赵六-招待费', nodeName: '财务审批', initiator: '赵六', approveTime: '2024-01-11 11:20', result: '驳回', duration: '3小时' }
  ]
}

const resetDoneQuery = () => { doneQuery.flowName = ''; doneQuery.result = ''; doneQuery.dateRange = [] }
const getDoneResultType = (result) => ({ 通过: 'success', 驳回: 'danger', 退回: 'warning' }[result] || 'info')
const handleDoneDetail = (row) => { ElMessage.info(`查看已办详情: ${row.businessTitle}`) }
const handleDoneFlow = (row) => { ElMessage.info(`查看流程图: ${row.businessTitle}`) }
const handleDoneCopy = (row) => { ElMessage.info(`抄送他人: ${row.businessTitle}`) }
const handleDoneExport = () => { ElMessage.info('导出已办记录') }
const handleReSubmit = (row) => { ElMessage.info(`重新发起: ${row.businessTitle}`) }

// ========== 流程实例相关 (来自 FlowInstance.vue) ==========
const instanceQuery = reactive({ flowName: '', initiator: '', status: '', dateRange: [], page: 1, size: 10 })
const instanceTotal = ref(50)
const instanceLoading = ref(false)
const instancePageList = ref([])
const runningCount = ref(20)
const completedCount = ref(25)
const suspendedCount = ref(3)
const rejectedInstanceCount = ref(2)

const loadInstanceData = () => {
  instanceLoading.value = true
  setTimeout(() => {
    instancePageList.value = [
      { instanceNo: 'INS20240120', flowName: '请假申请', category: '人力资源', initiator: '张三', currentNode: '部门审批', currentApprover: '李四', startTime: '2024-01-20 09:00', duration: '进行中', status: 'running' },
      { instanceNo: 'INS20240119', flowName: '费用报销', category: '财务管理', initiator: '李四', currentNode: '已完成', currentApprover: '-', startTime: '2024-01-19 14:00', duration: '1天', status: 'completed' }
    ]
    instanceLoading.value = false
  }, 500)
}

const resetInstanceQuery = () => { instanceQuery.flowName = ''; instanceQuery.initiator = ''; instanceQuery.status = ''; instanceQuery.dateRange = [] }
const getInstanceStatusType = (status) => ({ running: 'primary', completed: 'success', suspended: 'warning', rejected: 'danger' }[status] || 'info')
const getInstanceStatusText = (status) => ({ running: '运行中', completed: '已完成', suspended: '已暂停', rejected: '已驳回' }[status] || status)
const handleInstanceDetail = (row) => { ElMessage.info(`查看实例详情: ${row.instanceNo}`) }
const handleInstanceFlow = (row) => { ElMessage.info(`查看流程图: ${row.instanceNo}`) }
const handleInstanceSuspend = (row) => { ElMessage.success(`暂停实例: ${row.instanceNo}`) }
const handleInstanceTerminate = (row) => { ElMessage.success(`终止实例: ${row.instanceNo}`) }

// ========== 归档管理相关 (来自 FlowArchive.vue) ==========
const activeArchiveTab = ref('archived')
const archiveQuery = reactive({ flowName: '', businessKey: '', dateRange: [], page: 1, size: 10 })
const archiveTotal = ref(200)
const archiveLoading = ref(false)
const archiveList = ref([])
const archivePolicyList = ref([])
const archiveLogList = ref([])
const totalArchiveCount = ref(200)
const monthArchiveCount = ref(15)
const pendingArchiveCount = ref(5)
const storageSize = ref(2.5)
const retentionYears = ref(5)

const loadArchiveData = () => {
  archiveLoading.value = true
  setTimeout(() => {
    archiveList.value = [
      { instanceNo: 'INS20231201', flowName: '请假申请', businessTitle: '张三-年假', businessKey: 'LEAVE2023001', initiator: '张三', finishTime: '2023-12-01 17:00', archiveTime: '2023-12-02 00:00', fileSize: '2.5MB' },
      { instanceNo: 'INS20231205', flowName: '费用报销', businessTitle: '李四-差旅费', businessKey: 'EXP2023005', initiator: '李四', finishTime: '2023-12-05 16:30', archiveTime: '2023-12-06 00:00', fileSize: '3.2MB' }
    ]
    archiveLoading.value = false
  }, 500)
}

const resetArchiveQuery = () => { archiveQuery.flowName = ''; archiveQuery.businessKey = ''; archiveQuery.dateRange = [] }
const handleArchiveView = (row) => { ElMessage.info(`查看归档: ${row.instanceNo}`) }
const handleArchiveDownload = (row) => { ElMessage.info(`下载归档: ${row.instanceNo}`) }
const handleEditPolicy = (row) => { ElMessage.info(`编辑归档策略: ${row.name}`) }

// ========== Tab切换处理 ==========
const handleTabChange = (tabName) => {
  // 根据切换的Tab加载对应数据
  if (tabName === 'flowDefinition' && defPageList.value.length === 0) loadDefData()
  if (tabName === 'formManagement' && formPageList.value.length === 0) loadFormData()
  if (tabName === 'todo' && todoPageList.value.length === 0) loadTodoData()
  if (tabName === 'done' && donePageList.value.length === 0) loadDoneData()
  if (tabName === 'instance' && instancePageList.value.length === 0) loadInstanceData()
  if (tabName === 'archive' && archiveList.value.length === 0) loadArchiveData()
}

// ========== 原有的方法 ==========

const getLevelTagType = (level) => {
  const map = { level3: 'primary', level4: 'success', level5: 'info' }
  return map[level] || 'info'
}

const getLevelName = (level) => {
  const map = { level3: '三级主流程', level4: '四级子流程', level5: '五级岗位流程' }
  return map[level] || level
}

const getStatusTagType = (status) => {
  const map = { draft: 'info', pending: 'warning', active: 'success', deprecated: 'danger', archived: 'info' }
  return map[status] || 'info'
}

const getStatusName = (status) => {
  const map = { draft: '草稿', pending: '待审核', active: '已生效', deprecated: '已废止', archived: '已归档' }
  return map[status] || status
}

const loadFlowList = async () => {
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchKey.value) params.keyword = searchKey.value
    if (filterLevel.value) params.level = filterLevel.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await axios.get('/api/process/def/list', { params })
    if (res.data.code === 200) {
      const pageData = res.data.data || {}
      flowList.value = pageData.records || []
      totalCount.value = pageData.total || 0
    } else {
      ElMessage.error(res.data.message || '加载流程列表失败')
    }
  } catch (e) {
    ElMessage.error('加载流程列表失败')
    console.error(e)
  }
}

const doSearch = () => {
  currentPage.value = 1
  loadFlowList()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadFlowList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadFlowList()
}

const createNewFlow = () => {
  createModalVisible.value = true
}

const submitCreate = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post('/api/process/def', newFlowForm)
        if (res.data.code === 200) {
          ElMessage.success('流程创建成功')
          createModalVisible.value = false
          loadFlowList()
        } else {
          ElMessage.error(res.data.message || '创建失败')
        }
      } catch (e) {
        ElMessage.error('创建失败')
        console.error(e)
      }
    }
  })
}

const importFlow = () => {
  ElMessage.info('导入流程')
}

const exportFlows = () => {
  window.open('/api/epros/export/flows')
}

const viewFlow = (row) => {
  selectedFlow.value = {
    ...row,
    ownerDeptName: row.ownerDept,
    description: '这是一个详细的流程描述，说明流程的目的、范围和主要步骤。',
    policies: [{ id: 1, code: 'ZD-001', name: '生产管理制度' }],
    forms: [{ id: 1, code: 'FM-001', name: '生产任务单' }]
  }
  detailDrawerVisible.value = true
}

const editFlow = (row) => {
  ElMessage.info(`编辑流程: ${row.name}`)
}

const designFlow = (row) => {
  window.location.href = `/epros/designer?id=${row.id}`
}

const deprecateFlow = (row) => {
  deprecateFlowData.value = row
  deprecateModalVisible.value = true
}

const confirmDeprecate = async () => {
  try {
    const res = await axios.put(`/api/process/def/${deprecateFlowData.value.id}/deprecate`, { reason: deprecateForm.reason })
    if (res.data.code === 200) {
      ElMessage.success(`流程 "${deprecateFlowData.value.name}" 已废止`)
      deprecateModalVisible.value = false
      loadFlowList()
    } else {
      ElMessage.error(res.data.message || '废止失败')
    }
  } catch (e) {
    ElMessage.error('废止失败')
    console.error(e)
  }
}

const archiveFlow = async (row) => {
  try {
    const res = await axios.put(`/api/process/def/${row.id}/archive`)
    if (res.data.code === 200) {
      ElMessage.success(`流程 "${row.name}" 已归档`)
      loadFlowList()
    } else {
      ElMessage.error(res.data.message || '归档失败')
    }
  } catch (e) {
    ElMessage.error('归档失败')
    console.error(e)
  }
}

const viewVersions = (row) => {
  ElMessage.info(`查看版本历史: ${row.name}`)
}

onMounted(() => {
  loadFlowList()
})
</script>

<style lang="scss" scoped>
.epros-management {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  color: #fff;
  
  .header-title {
    h2 {
      margin: 0 0 4px 0;
      font-size: 18px;
    }
    p {
      margin: 0;
      font-size: 12px;
      opacity: 0.8;
    }
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.management-body {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .search-input {
    flex: 1;
    max-width: 300px;
  }
}

.flow-list {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  .el-table {
    flex: 1;
  }
}

.pagination-container {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
}

.deprecate-warning {
  padding: 20px;
  text-align: center;
  background: #fef7f0;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .warning-icon {
    font-size: 48px;
    color: #faad14;
    margin-bottom: 12px;
  }
  
  .flow-name {
    font-weight: 600;
    color: #f56c6c;
  }
  
  .hint {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
  }
}

.flow-detail {
  padding: 12px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 16px;
  
  .flow-title {
    font-size: 18px;
    font-weight: 600;
  }
  
  .flow-code {
    font-size: 12px;
    color: #666;
    background: #f5f5f5;
    padding: 4px 8px;
    border-radius: 4px;
  }
}

.detail-section {
  margin-bottom: 20px;
  
  h4 {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8e8e8;
  }
}

.rich-text {
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}
</style>
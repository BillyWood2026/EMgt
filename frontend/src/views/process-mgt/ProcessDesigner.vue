<template>
  <div class="epros-designer">
    <div class="designer-header">
      <div class="header-left">
        <el-tag type="primary">流程EPROS</el-tag>
        <el-input 
          v-model="flowName" 
          placeholder="流程名称（动词+名词+场景）" 
          class="flow-name-input"
        />
        <el-input 
          v-model="flowCode" 
          placeholder="流程编码（系统自动生成）" 
          class="flow-code-input"
          :disabled="true"
        />
        <el-tag :type="flowStatus === 'effective' ? 'success' : flowStatus === 'pending' ? 'warning' : flowStatus === 'void' ? 'danger' : 'info'">
          {{ flowStatusOptions.find(s => s.value === flowStatus)?.label || '草稿' }}
        </el-tag>
        <el-tag type="primary">v{{ currentVersion }}</el-tag>
        <el-button size="small" @click="showFlowProperties = true" icon="Setting">流程属性</el-button>
      </div>
      <div class="header-right">
        <el-button size="small" @click="showVersionHistory = true" icon="Clock">版本历史</el-button>
        <el-button size="small" @click="createNewVersion('minor')" icon="Plus">新版本</el-button>
        <el-button size="small" @click="undo" icon="RefreshLeft">撤销</el-button>
        <el-button size="small" @click="redo" icon="RefreshRight">重做</el-button>
        <el-button size="small" @click="validateFlow" icon="CircleCheck">BPMN校验</el-button>
        <el-button size="small" @click="autoLayout" icon="Grid">一键规整</el-button>
        <el-button size="small" @click="batchDelete" icon="Delete" :disabled="selectedNodes.length === 0">批量删除</el-button>
        <el-button size="small" @click="batchCopy" icon="DocumentCopy" :disabled="selectedNodes.length === 0">批量复制</el-button>
        <el-button size="small" @click="saveDraft" icon="DocumentAdd">保存草稿</el-button>
        <el-button size="small" type="warning" @click="submitForReview" icon="Promotion">提交审核</el-button>
        <el-button size="small" type="success" @click="publishFlow" icon="Check">发布</el-button>
        <el-button size="small" @click="showExportMenu = true" icon="Download">导出</el-button>
        <el-button size="small" @click="showSampleDialog = true" icon="Picture">示例外</el-button>
        <el-dropdown size="small" @command="handleTemplateCommand">
          <el-button size="small" icon="DocumentCopy">模板库</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="save">保存为模板</el-dropdown-item>
              <el-dropdown-item command="load">从模板加载</el-dropdown-item>
              <el-dropdown-item command="manage">管理我的模板</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown size="small" @command="handleStyleCommand">
          <el-button size="small" icon="Brush">批量样式</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="apply">应用企业样式</el-dropdown-item>
              <el-dropdown-item command="customize">自定义样式</el-dropdown-item>
              <el-dropdown-item command="reset">重置为默认</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="designer-tabs">
      <el-tabs v-model="designerActiveTab" type="card" @tab-change="handleDesignerTabChange">
        <el-tab-pane label="流程设计" name="flow-design">
          <div class="designer-body">
      <div class="toolbar-panel">
        <div class="toolbar-section">
          <h4>BPMN事件节点</h4>
          <div class="tool-items">
            <div class="tool-item" data-node-type="bpmn-start" @mousedown="startDragNode('bpmn-start')" title="开始事件">
              <div class="tool-icon-wrapper start-icon">
                <el-icon>CirclePlus</el-icon>
              </div>
              <span>开始</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-end" @mousedown="startDragNode('bpmn-end')" title="结束事件">
              <div class="tool-icon-wrapper end-icon">
                <el-icon>CircleClose</el-icon>
              </div>
              <span>结束</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-intermediate" @mousedown="startDragNode('bpmn-intermediate')" title="中间事件">
              <div class="tool-icon-wrapper event-icon">
                <el-icon>Clock</el-icon>
              </div>
              <span>中间事件</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-timer" @mousedown="startDragNode('bpmn-timer')" title="定时事件">
              <div class="tool-icon-wrapper timer-icon">
                <el-icon>Timer</el-icon>
              </div>
              <span>定时</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-message" @mousedown="startDragNode('bpmn-message')" title="消息事件">
              <div class="tool-icon-wrapper message-icon">
                <el-icon>Message</el-icon>
              </div>
              <span>消息</span>
            </div>
          </div>
        </div>
        
        <div class="toolbar-section">
          <h4>BPMN任务节点</h4>
          <div class="tool-items">
            <div class="tool-item" data-node-type="bpmn-task" @mousedown="startDragNode('bpmn-task')" title="人工任务">
              <div class="tool-icon-wrapper task-icon">
                <el-icon>User</el-icon>
              </div>
              <span>人工任务</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-approval" @mousedown="startDragNode('bpmn-approval')" title="审批任务">
              <div class="tool-icon-wrapper approval-icon">
                <el-icon>Checked</el-icon>
              </div>
              <span>审批</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-service" @mousedown="startDragNode('bpmn-service')" title="服务任务">
              <div class="tool-icon-wrapper service-icon">
                <el-icon>Cpu</el-icon>
              </div>
              <span>服务调用</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-script" @mousedown="startDragNode('bpmn-script')" title="脚本任务">
              <div class="tool-icon-wrapper script-icon">
                <el-icon>Edit</el-icon>
              </div>
              <span>脚本</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-subprocess" @mousedown="startDragNode('bpmn-subprocess')" title="子流程">
              <div class="tool-icon-wrapper subprocess-icon">
                <el-icon>FolderOpened</el-icon>
              </div>
              <span>子流程</span>
            </div>
          </div>
        </div>
        
        <div class="toolbar-section">
          <h4>BPMN网关</h4>
          <div class="tool-items">
            <div class="tool-item" data-node-type="bpmn-gateway-exclusive" @mousedown="startDragNode('bpmn-gateway-exclusive')" title="排他网关">
              <div class="tool-icon-wrapper gateway-exclusive">
                <el-icon>Refresh</el-icon>
              </div>
              <span>排他网关</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-gateway-parallel" @mousedown="startDragNode('bpmn-gateway-parallel')" title="并行网关">
              <div class="tool-icon-wrapper gateway-parallel">
                <el-icon>Connection</el-icon>
              </div>
              <span>并行网关</span>
            </div>
            <div class="tool-item" data-node-type="bpmn-gateway-inclusive" @mousedown="startDragNode('bpmn-gateway-inclusive')" title="包容网关">
              <div class="tool-icon-wrapper gateway-inclusive">
                <el-icon>Refresh</el-icon>
              </div>
              <span>包容网关</span>
            </div>
          </div>
        </div>
        
        <div class="toolbar-section">
          <h4>连线与工具</h4>
          <div class="tool-items">
            <div class="tool-item" :class="{ active: drawMode === 'line' }" @click="toggleDrawMode('line')">
              <div class="tool-icon-wrapper line-icon">
                <el-icon>Minus</el-icon>
              </div>
              <span>连线</span>
            </div>
            <div class="tool-item" :class="{ active: drawMode === 'delete' }" @click="toggleDrawMode('delete')">
              <div class="tool-icon-wrapper delete-icon">
                <el-icon>Delete</el-icon>
              </div>
              <span>删除</span>
            </div>
            <div class="tool-item" @click="clearCanvas">
              <div class="tool-icon-wrapper clear-icon">
                <el-icon>RefreshLeft</el-icon>
              </div>
              <span>清空</span>
            </div>
            <div class="tool-item" @click="zoomOut">
              <div class="tool-icon-wrapper zoom-icon">
                <el-icon>ZoomOut</el-icon>
              </div>
              <span>缩小</span>
            </div>
            <div class="tool-item" @click="zoomIn">
              <div class="tool-icon-wrapper zoom-icon">
                <el-icon>ZoomIn</el-icon>
              </div>
              <span>放大</span>
            </div>
            <div class="tool-item" @click="fitCanvas">
              <div class="tool-icon-wrapper fit-icon">
                <el-icon>FullScreen</el-icon>
              </div>
              <span>适应</span>
            </div>
          </div>
        </div>
        
        <div class="toolbar-section">
          <h4>对齐工具</h4>
          <div class="tool-items">
            <div class="tool-item" @click="alignLeft">
              <el-icon>DArrowLeft</el-icon>
              <span>左对齐</span>
            </div>
            <div class="tool-item" @click="alignCenter">
              <el-icon>Sort</el-icon>
              <span>居中</span>
            </div>
            <div class="tool-item" @click="alignRight">
              <el-icon>DArrowRight</el-icon>
              <span>右对齐</span>
            </div>
            <div class="tool-item" @click="alignTop">
              <el-icon>Top</el-icon>
              <span>顶对齐</span>
            </div>
            <div class="tool-item" @click="alignMiddle">
              <el-icon>ScaleToOriginal</el-icon>
              <span>垂直居中</span>
            </div>
            <div class="tool-item" @click="alignBottom">
              <el-icon>Bottom</el-icon>
              <span>底对齐</span>
            </div>
          </div>
        </div>
      </div>

      <div class="canvas-panel">
        <div class="canvas-header">
          <span>BPMN画布</span>
          <div class="canvas-info">
            <span>节点: {{ nodeCount }}</span>
            <span>|</span>
            <span>连线: {{ edgeCount }}</span>
            <span>|</span>
            <span>{{ zoomLevel }}%</span>
          </div>
          <div class="layer-controls">
            <el-button size="small" icon="ZoomIn" @click="zoomIn" title="放大" />
            <el-button size="small" icon="ZoomOut" @click="zoomOut" title="缩小" />
            <el-button size="small" icon="FullScreen" @click="fitGraph" title="适应画布" />
            <el-button size="small" icon="View" @click="toggleLayers">图层管理</el-button>
          </div>
        </div>
        <div class="canvas-container">
          <!-- X6 Graph 画布容器 -->
          <div :id="graphContainerId" class="x6-canvas" ref="canvasContainer"></div>
          <!-- X6 Minimap 缩略图 -->
          <div id="x6-minimap" class="x6-minimap"></div>
        </div>
        
        <div class="canvas-footer">
          <div class="history-info">
            <span>操作步骤: {{ historyIndex + 1 }}/{{ history.length }}</span>
          </div>
        </div>
      </div>

      <div class="properties-panel">
        <div class="panel-header">
          <span v-if="selectedNode">节点属性配置</span>
          <span v-else-if="selectedConnection">连线属性</span>
          <span v-else>属性面板</span>
          <el-button size="mini" v-if="selectedNode" @click="deleteSelectedNode" icon="Delete">删除</el-button>
        </div>
        
        <div v-if="selectedNode" class="properties-content">
          <el-tabs v-model="activePropertyTab" type="card">
            <el-tab-pane label="基本信息" name="basic">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="节点名称">
                  <el-input v-model="nodeProperties.label" @change="updateNodeLabel" />
                </el-form-item>
                <el-form-item label="节点类型">
                  <el-tag :type="getNodeTypeTagType(selectedNode.type)">{{ getNodeTypeName(selectedNode.type) }}</el-tag>
                </el-form-item>
                <el-form-item label="节点描述">
                  <el-input type="textarea" v-model="nodeProperties.description" :rows="3" />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="权责配置" name="assignee">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="审批类型">
                  <el-select v-model="nodeProperties.approvalType" placeholder="选择审批类型">
                    <el-option label="岗位审批" value="post" />
                    <el-option label="角色审批" value="role" />
                    <el-option label="指定人员" value="user" />
                    <el-option label="部门审批" value="dept" />
                    <el-option label="动态发起人" value="initiator" />
                    <el-option label="上级主管" value="superior" />
                    <el-option label="归口部门" value="responsibleDept" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批方式">
                  <el-select v-model="nodeProperties.approvalMode">
                    <el-option label="顺序审批" value="sequence" />
                    <el-option label="或签(任意通过)" value="or" />
                    <el-option label="会签(全部通过)" value="and" />
                    <el-option label="自由流转" value="free" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批人配置">
                  <el-input v-model="nodeProperties.approvalConfig" placeholder="选择具体审批人/岗位/角色" />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="时限配置" name="timing">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="处理时限">
                  <el-input-number v-model="nodeProperties.timeLimit" :min="0" />
                  <span class="text-gray">小时</span>
                </el-form-item>
                <el-form-item label="超时处理">
                  <el-select v-model="nodeProperties.timeoutAction">
                    <el-option label="自动通过" value="auto-pass" />
                    <el-option label="自动驳回" value="auto-reject" />
                    <el-option label="升级审批" value="escalate" />
                    <el-option label="仅提醒" value="remind" />
                  </el-select>
                </el-form-item>
                <el-form-item label="超时提醒">
                  <el-select v-model="nodeProperties.remindType">
                    <el-option label="不提醒" value="none" />
                    <el-option label="超时前1小时" value="before-1h" />
                    <el-option label="超时前2小时" value="before-2h" />
                    <el-option label="超时后立即" value="after-0" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="执行配置" name="execution">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="审批人类型">
                  <el-select v-model="nodeProperties.assigneeType">
                    <el-option label="岗位" value="position" />
                    <el-option label="角色" value="role" />
                    <el-option label="部门" value="department" />
                    <el-option label="人员" value="person" />
                    <el-option label="动态发起人" value="initiator" />
                    <el-option label="上级主管" value="superior" />
                    <el-option label="归口部门" value="owning" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批人配置">
                  <el-select v-model="nodeProperties.assignee" placeholder="选择审批人">
                    <el-option label="岗位:仓库管理员" value="position:warehouse" />
                    <el-option label="岗位:生产班长" value="position:supervisor" />
                    <el-option label="岗位:SMT操作员" value="position:smt-operator" />
                    <el-option label="岗位:IPQC检验员" value="position:ipqc" />
                    <el-option label="角色:部门负责人" value="role:dept-head" />
                    <el-option label="部门:生产部" value="dept:production" />
                    <el-option label="人员:张三" value="person:zhangsan" />
                  </el-select>
                </el-form-item>
                <el-form-item label="审批模式">
                  <el-select v-model="nodeProperties.approvalMode">
                    <el-option label="顺序签" value="sequence" />
                    <el-option label="或签(任意通过)" value="or-sign" />
                    <el-option label="会签(全部通过)" value="and-sign" />
                    <el-option label="自由流转" value="free" />
                  </el-select>
                </el-form-item>
                <el-form-item label="会签比例">
                  <el-input-number v-model="nodeProperties.approvalRatio" :min="0" :max="100" />%
                </el-form-item>
                <el-form-item label="超时预警">
                  <el-input-number v-model="nodeProperties.timeoutWarning" :min="0" :max="100" />%
                  <span class="text-gray">处理时限的百分比</span>
                </el-form-item>
                <el-form-item label="督办人">
                  <el-select v-model="nodeProperties.supervisor" placeholder="选择督办人">
                    <el-option label="上级主管" value="superior" />
                    <el-option label="指定人员" value="person" />
                  </el-select>
                </el-form-item>
                <el-form-item label="自动跳过空节点">
                  <el-switch v-model="nodeProperties.skipEmpty" />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="字段权限" name="fields">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="可编辑字段">
                  <el-select v-model="nodeProperties.editableFields" multiple style="width:100%">
                    <el-option label="申请人" value="applicant" />
                    <el-option label="申请时间" value="applyTime" />
                    <el-option label="申请事由" value="reason" />
                    <el-option label="附件" value="attachment" />
                    <el-option label="金额" value="amount" />
                    <el-option label="备注" value="remark" />
                    <el-option label="审批意见" value="comment" />
                  </el-select>
                </el-form-item>
                <el-form-item label="只读字段">
                  <el-select v-model="nodeProperties.readonlyFields" multiple style="width:100%">
                    <el-option label="申请人" value="applicant" />
                    <el-option label="申请时间" value="applyTime" />
                    <el-option label="申请事由" value="reason" />
                  </el-select>
                </el-form-item>
                <el-form-item label="隐藏字段">
                  <el-select v-model="nodeProperties.hiddenFields" multiple style="width:100%">
                    <el-option label="申请人" value="applicant" />
                    <el-option label="金额" value="amount" />
                    <el-option label="审批意见" value="comment" />
                  </el-select>
                </el-form-item>
                <el-form-item label="必填字段">
                  <el-select v-model="nodeProperties.requiredFields" multiple style="width:100%">
                    <el-option label="申请事由" value="reason" />
                    <el-option label="附件" value="attachment" />
                    <el-option label="审批意见" value="comment" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="节点输入输出" name="io">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="节点输入">
                  <el-input type="textarea" v-model="nodeProperties.inputs" :rows="2" placeholder="上游单据、数据、条件、物料、记录" />
                </el-form-item>
                <el-form-item label="节点输出">
                  <el-input type="textarea" v-model="nodeProperties.outputs" :rows="2" placeholder="产出表单、结论、台账、报告、状态" />
                </el-form-item>
                <el-form-item label="证据留存要求">
                  <el-select v-model="nodeProperties.evidenceRequirements" multiple>
                    <el-option label="照片" value="photo" />
                    <el-option label="记录" value="record" />
                    <el-option label="签字" value="signature" />
                    <el-option label="数据留存" value="data" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="风险管控" name="risk">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="节点风险点">
                  <el-input type="textarea" v-model="nodeProperties.riskPoints" :rows="2" placeholder="违规风险、失效风险、质量风险、安全能耗风险" />
                </el-form-item>
                <el-form-item label="管控措施">
                  <el-input type="textarea" v-model="nodeProperties.controlMeasures" :rows="2" placeholder="预防、检查、整改措施" />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="驳回规则" name="reject">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="驳回方式">
                  <el-select v-model="nodeProperties.rejectRule">
                    <el-option label="驳回至上一级" value="previous" />
                    <el-option label="驳回至任意节点" value="arbitrary" />
                    <el-option label="驳回至发起人" value="initiator" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="nodeProperties.rejectRule === 'arbitrary'" label="目标节点">
                  <el-select v-model="nodeProperties.rejectTarget">
                    <el-option v-for="n in nodes.value.filter(n => n.type !== 'bpmn-start' && n.type !== 'bpmn-end')" :key="n.id" :label="n.label" :value="n.id" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="七何要素" name="sevenWhat">
              <div class="seven-what">
                <div class="what-item">
                  <span class="what-label">何事：</span>
                  <el-input v-model="nodeProperties.what" placeholder="做什么" />
                </div>
                <div class="what-item">
                  <span class="what-label">何因：</span>
                  <el-input v-model="nodeProperties.why" placeholder="为什么做" />
                </div>
                <div class="what-item">
                  <span class="what-label">何人：</span>
                  <el-input v-model="nodeProperties.who" placeholder="谁来做" />
                </div>
                <div class="what-item">
                  <span class="what-label">何时：</span>
                  <el-input v-model="nodeProperties.when" placeholder="什么时候" />
                </div>
                <div class="what-item">
                  <span class="what-label">何地：</span>
                  <el-input v-model="nodeProperties.where" placeholder="在哪里做" />
                </div>
                <div class="what-item">
                  <span class="what-label">何规：</span>
                  <el-input v-model="nodeProperties.rule" placeholder="依据什么" />
                </div>
                <div class="what-item">
                  <span class="what-label">何果：</span>
                  <el-input v-model="nodeProperties.result" placeholder="预期结果" />
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="节点事件" name="events">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="进入事件">
                  <el-select v-model="nodeProperties.enterEvent" placeholder="选择事件">
                    <el-option label="无" value="none" />
                    <el-option label="推送消息通知" value="notify" />
                    <el-option label="生成台账记录" value="ledger" />
                    <el-option label="触发子流程" value="subflow" />
                    <el-option label="更新数据" value="update" />
                  </el-select>
                </el-form-item>
                <el-form-item label="完成事件">
                  <el-select v-model="nodeProperties.completeEvent" placeholder="选择事件">
                    <el-option label="无" value="none" />
                    <el-option label="推送消息通知" value="notify" />
                    <el-option label="生成台账记录" value="ledger" />
                    <el-option label="触发子流程" value="subflow" />
                    <el-option label="发送邮件" value="email" />
                  </el-select>
                </el-form-item>
                <el-form-item label="驳回事件">
                  <el-select v-model="nodeProperties.rejectEvent" placeholder="选择事件">
                    <el-option label="无" value="none" />
                    <el-option label="推送消息通知" value="notify" />
                    <el-option label="记录驳回原因" value="record" />
                    <el-option label="触发预警" value="alert" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="资产绑定" name="assets">
              <el-form :model="nodeProperties" label-width="100px">
                <el-form-item label="制度条款">
                  <el-select v-model="nodeProperties.policy" placeholder="选择关联制度">
                    <el-option label="文件控制程序" value="ZD-EHS-001" />
                    <el-option label="隐患排查治理制度" value="ZD-EHS-003" />
                    <el-option label="设备维护管理制度" value="ZD-EM-001" />
                    <el-option label="质量控制程序" value="ZD-QC-001" />
                  </el-select>
                </el-form-item>
                <el-form-item label="作业指导书">
                  <el-select v-model="nodeProperties.sop" placeholder="选择作业指导书">
                    <el-option label="隐患排查作业指导书" value="SOP-EHS-002" />
                    <el-option label="设备点检作业指导书" value="SOP-EM-001" />
                    <el-option label="SMT贴片作业指导书" value="SOP-PM-001" />
                  </el-select>
                </el-form-item>
                <el-form-item label="配套表单">
                  <el-select v-model="nodeProperties.form" placeholder="选择表单">
                    <el-option label="隐患排查表" value="FM-EHS-003" />
                    <el-option label="设备点检记录表" value="FM-EM-001" />
                    <el-option label="生产日报表" value="FM-PM-001" />
                  </el-select>
                </el-form-item>
                <el-form-item label="合规依据">
                  <el-select v-model="nodeProperties.compliance" multiple style="width:100%">
                    <el-option label="ISO45001-6.1.2" value="ISO45001-6.1.2" />
                    <el-option label="ISO14001-4.4.5" value="ISO14001-4.4.5" />
                    <el-option label="ISO9001-8.5.1" value="ISO9001-8.5.1" />
                    <el-option label="GB/T 23331-2020" value="GB/T-23331" />
                  </el-select>
                </el-form-item>
                <el-form-item label="风险提示">
                  <el-input type="textarea" v-model="nodeProperties.riskTip" :rows="2" placeholder="输入风险提示信息" />
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div v-else-if="selectedConnection" class="properties-content">
          <el-form :model="connectionProperties" label-width="100px">
            <el-form-item label="连线标签">
              <el-input v-model="connectionProperties.label" />
            </el-form-item>
            <el-form-item label="条件表达式">
              <el-input type="textarea" v-model="connectionProperties.condition" :rows="2" placeholder="如: amount > 1000" />
            </el-form-item>
            <el-form-item label="可视化条件生成">
              <div class="condition-builder">
                <div class="condition-row">
                  <el-select v-model="conditionBuilder.field" class="condition-field">
                    <el-option label="金额" value="amount" />
                    <el-option label="数量" value="quantity" />
                    <el-option label="状态" value="status" />
                    <el-option label="申请人" value="applicant" />
                    <el-option label="部门" value="department" />
                  </el-select>
                  <el-select v-model="conditionBuilder.operator" class="condition-operator">
                    <el-option label="等于" value="==" />
                    <el-option label="不等于" value="!=" />
                    <el-option label="大于" value=">" />
                    <el-option label="小于" value="<" />
                    <el-option label="大于等于" value=">=" />
                    <el-option label="小于等于" value="<=" />
                    <el-option label="包含" value="contains" />
                  </el-select>
                  <el-input v-model="conditionBuilder.value" class="condition-value" placeholder="输入值" />
                </div>
                <div class="condition-actions">
                  <el-button size="small" type="primary" @click="buildCondition">生成条件</el-button>
                  <el-button size="small" @click="showConditionPresets = !showConditionPresets">常用模板</el-button>
                </div>
                <div v-if="showConditionPresets" class="condition-presets">
                  <el-button size="mini" @click="applyPreset('amount > 1000')">金额>1000</el-button>
                  <el-button size="mini" @click="applyPreset('amount > 5000')">金额>5000</el-button>
                  <el-button size="mini" @click="applyPreset('status == 审批中')">审批中</el-button>
                  <el-button size="mini" @click="applyPreset('department == 生产部')">生产部</el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="优先级">
              <el-input-number v-model="connectionProperties.priority" :min="1" :max="10" />
            </el-form-item>
          </el-form>
        </div>
        
        <div v-else class="empty-properties">
          <div class="empty-icon">
            <el-icon>Pointer</el-icon>
          </div>
          <p>请选择一个节点或连线</p>
          <p class="hint">点击画布上的元素查看和编辑属性</p>
        </div>
      </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="制度文档" name="policy-doc">
          <div class="policy-doc-body">
            <el-card shadow="never" class="toolbar-card" style="margin:12px">
              <div class="toolbar">
                <div class="toolbar-group">
                  <span class="toolbar-label">字体</span>
                  <el-select v-model="editorStyle.font" style="width:120px">
                    <el-option label="宋体" value="宋体" />
                    <el-option label="微软雅黑" value="微软雅黑" />
                    <el-option label="黑体" value="黑体" />
                    <el-option label="楷体" value="楷体" />
                  </el-select>
                  <el-select v-model="editorStyle.fontSize" style="width:80px">
                    <el-option label="12px" value="12px" />
                    <el-option label="14px" value="14px" />
                    <el-option label="16px" value="16px" />
                    <el-option label="18px" value="18px" />
                    <el-option label="20px" value="20px" />
                  </el-select>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">段落</span>
                  <el-button size="small" @click="formatText('bold')"><el-icon><EditPen /></el-icon></el-button>
                  <el-button size="small" @click="formatText('italic')"><el-icon><Edit /></el-icon></el-button>
                  <el-button size="small" @click="formatText('underline')"><el-icon><DataLine /></el-icon></el-button>
                  <el-button size="small" @click="formatText('strikethrough')"><el-icon><Delete /></el-icon></el-button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">对齐</span>
                  <el-button size="small" @click="formatText('alignLeft')"><el-icon><DArrowLeft /></el-icon></el-button>
                  <el-button size="small" @click="formatText('alignCenter')"><el-icon><Grid /></el-icon></el-button>
                  <el-button size="small" @click="formatText('alignRight')"><el-icon><DArrowRight /></el-icon></el-button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">列表</span>
                  <el-button size="small" @click="formatText('orderedList')"><el-icon><CollectionTag /></el-icon></el-button>
                  <el-button size="small" @click="formatText('unorderedList')"><el-icon><List /></el-icon></el-button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">标题</span>
                  <el-select v-model="editorStyle.heading" style="width:100px">
                    <el-option label="正文" value="" />
                    <el-option label="一级标题" value="h1" />
                    <el-option label="二级标题" value="h2" />
                    <el-option label="三级标题" value="h3" />
                  </el-select>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">插入</span>
                  <el-button size="small" @click="insertImage"><el-icon><Picture /></el-icon></el-button>
                  <el-button size="small" @click="insertTable"><el-icon><Grid /></el-icon></el-button>
                  <el-button size="small" @click="insertAttachment"><el-icon><Link /></el-icon></el-button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <span class="toolbar-label">模板</span>
                  <el-select v-model="selectedTemplate" @change="applyTemplate" style="width:150px">
                    <el-option label="选择模板" value="" />
                    <el-option label="招聘管理制度" value="recruit" />
                    <el-option label="员工培训制度" value="training" />
                    <el-option label="费用报销规定" value="expense" />
                  </el-select>
                </div>
              </div>
            </el-card>

            <div style="display: flex; gap: 12px; padding: 0 12px; height: calc(100% - 80px);">
              <el-card shadow="never" style="width: 240px;">
                <template #header><b>目录大纲</b></template>
                <el-tree
                  :data="outlineTree"
                  :props="treeProps"
                  node-key="id"
                  :default-expand-all="true"
                  @node-click="jumpToSection"
                >
                  <template #default="{ node, data }">
                    <span class="outline-node">
                      <span :class="'heading-' + data.level">{{ node.label }}</span>
                      <span class="outline-page">P{{ data.page }}</span>
                    </span>
                  </template>
                </el-tree>
                <div style="margin-top:12px;text-align:right">
                  <el-button size="small" @click="refreshOutline">刷新大纲</el-button>
                </div>
              </el-card>

              <el-card shadow="never" style="flex: 1;">
                <template #header>
                  <div class="editor-header">
                    <span><b>富文本编辑区</b></span>
                    <div class="editor-stats">
                      <span>字数：{{ wordCount }}</span>
                      <span>|</span>
                      <span>段落：{{ paragraphCount }}</span>
                    </div>
                  </div>
                </template>
                <div class="editor-content" contenteditable="true" @input="onEditorInput">
                  <h1>{{ flowName }}</h1>
                  <h2>目的</h2>
                  <p>为规范公司流程管理，保证流程执行质量，提高运营效率，特制定本流程。</p>
                  <h2>适用范围</h2>
                  <p>本流程适用于公司相关业务环节的管理。</p>
                  <h2>职责与权限</h2>
                  <p><strong>主责部门：</strong></p>
                  <ul>
                    <li>负责流程的制定与修订</li>
                    <li>组织流程培训与宣贯</li>
                  </ul>
                  <h2>术语定义</h2>
                  <p>明确流程相关术语的定义。</p>
                  <h2>管理内容</h2>
                  <ol>
                    <li>流程启动条件</li>
                    <li>流程执行步骤</li>
                    <li>流程结束条件</li>
                  </ol>
                  <h2>工作流程</h2>
                  <p>具体流程详见流程图（流程设计Tab）。</p>
                  <h2>考核与监督</h2>
                  <p>流程执行的考核指标和监督机制。</p>
                  <h2>附则</h2>
                  <p>本流程由主责部门负责解释和修订。</p>
                </div>
              </el-card>

              <el-card shadow="never" style="width: 400px;">
                <template #header><b>流程图预览</b></template>
                <div class="flow-canvas-preview" style="height: 400px; background: #fafbfc; border: 1px dashed #d9d9d9; display: flex; align-items: center; justify-content: center;">
                  <div v-if="nodes.length > 0" class="flow-preview-content">
                    <div v-for="(node, idx) in nodes" :key="idx" class="preview-node" :class="node.type">
                      <el-icon :component="getNodeIconByType(node.type)" :size="18" />
                      <span>{{ node.label }}</span>
                    </div>
                  </div>
                  <div v-else class="empty-flow">
                    <el-icon :size="48" style="color:#ccc"><TrendCharts /></el-icon>
                    <p>请在流程设计Tab中绘制流程图</p>
                  </div>
                </div>
                <div style="padding: 12px; border-top: 1px solid #ebeef5; display: flex; justify-content: flex-end; gap: 8px;">
                  <el-button size="small" @click="doDocPreview">预览文档</el-button>
                  <el-button size="small" type="primary" @click="submitDocApproval">提交审批</el-button>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="docPreviewVisible" title="文档预览" width="800px">
      <div class="preview-content" v-html="previewContent"></div>
    </el-dialog>

    <el-dialog title="流程规范智能校验" v-model="validateModalVisible" width="700px">
      <div v-if="validateResult">
        <div v-if="validateResult.success" class="validate-success">
          <el-icon class="success-icon">CheckCircle</el-icon>
          <p>校验通过！流程符合BPMN2.0标准及企业规范</p>
          <div class="validate-stats">
            <span>节点数: {{ validateResult.nodeCount }}</span>
            <span>连线数: {{ validateResult.edgeCount }}</span>
            <span>网关数: {{ validateResult.gatewayCount }}</span>
            <span>任务节点: {{ validateResult.taskCount }}</span>
          </div>
        </div>
        <div v-else class="validate-error">
          <el-icon class="error-icon">CircleCloseFilled</el-icon>
          <p>校验失败，发现以下问题：</p>
          <div class="error-tabs">
            <div 
              v-for="type in ['BPMN语法', '业务完整性', '文件配套']" 
              :key="type"
              class="error-tab"
              :class="{ active: activeErrorType === type }"
              @click="activeErrorType = type"
            >
              {{ type }} 
              <span class="error-count">{{ getErrorCountByType(type) }}</span>
            </div>
          </div>
          <div class="error-list">
            <div 
              v-for="(error, index) in getErrorsByType(activeErrorType)" 
              :key="index" 
              class="error-item"
              :class="`error-${error.type}`"
            >
              <el-icon class="error-dot" :class="`error-${error.type}`">AlertCircle</el-icon>
              <div class="error-content">
                <span class="error-message">{{ error.message }}</span>
                <span class="error-location">位置: {{ error.location }}</span>
                <span class="error-suggestion">整改建议: {{ error.suggestion }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dropdown-menu v-if="showExportMenu" class="export-menu">
      <el-dropdown-item @click="exportBPMN">导出BPMN源文件</el-dropdown-item>
      <el-dropdown-item @click="exportSVG">导出SVG流程图</el-dropdown-item>
      <el-dropdown-item @click="exportPNG">导出PNG流程图</el-dropdown-item>
      <el-dropdown-item @click="exportPDF">导出作业手册</el-dropdown-item>
    </el-dropdown-menu>

    <el-dialog title="版本历史" v-model="showVersionHistory" width="800px">
      <el-tabs>
        <el-tab-pane label="版本记录">
          <el-table :data="versionHistory" style="width: 100%">
            <el-table-column prop="version" label="版本号" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'effective' ? 'success' : row.status === 'pending' ? 'warning' : 'info'" size="small">
                  {{ flowStatusOptions.find(s => s.value === row.status)?.label || '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="savedAt" label="保存时间" width="180" />
            <el-table-column prop="savedBy" label="操作人" width="100" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="mini" type="primary" @click="compareWithVersion(row)">版本对比</el-button>
                <el-button size="mini" @click="loadVersion(row)">加载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="操作日志">
          <el-timeline>
            <el-timeline-item 
              v-for="(log, index) in operationLogs" 
              :key="index" 
              :timestamp="log.timestamp"
            >
              <el-card>
                <h4>{{ log.action }}</h4>
                <p>{{ log.description }}</p>
                <p class="log-operator">操作人: {{ log.operator }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog title="版本对比" v-model="showVersionCompare" width="900px">
      <div v-if="compareVersion" class="version-compare">
        <div class="compare-header">
          <span>对比版本: {{ compareVersion.version }}</span>
          <span>当前版本: {{ currentVersion }}</span>
        </div>
        <div class="compare-content">
          <div class="compare-section">
            <h4>变更的节点 ({{ getChangedNodes.length }}个)</h4>
            <el-tag v-for="node in getChangedNodes" :key="node" size="small" type="warning">{{ node }}</el-tag>
          </div>
          <div class="compare-section">
            <h4>变更的连线 ({{ getChangedConnections.length }}条)</h4>
            <el-tag v-for="conn in getChangedConnections" :key="conn" size="small" type="danger">{{ conn }}</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="流程发布" v-model="showPublishDialog" width="500px">
      <el-form :model="publishForm" label-width="120px">
        <el-form-item label="发布类型">
          <el-radio-group v-model="publishForm.publishType">
            <el-radio label="effective">立即生效</el-radio>
            <el-radio label="pending">待生效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="自动公示">
          <el-switch v-model="publishForm.notify" />
          <span class="form-hint">发布后自动公示到流程台账</span>
        </el-form-item>
        <el-form-item label="推送培训任务">
          <el-switch v-model="publishForm.createTraining" />
          <span class="form-hint">自动推送培训任务并统计覆盖率</span>
        </el-form-item>
        <el-form-item label="发布说明">
          <el-input type="textarea" :rows="3" placeholder="输入发布说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>

    <el-dialog title="流程属性配置" v-model="showFlowProperties" width="800px">
      <el-tabs>
        <el-tab-pane label="基础信息">
          <el-form :model="flowProperties" label-width="140px">
            <el-form-item label="流程编码">
              <el-input v-model="flowProperties.flowCode" disabled />
              <span class="text-gray">系统自动生成：LY-域码-组码-序号-版本</span>
            </el-form-item>
            <el-form-item label="流程名称">
              <el-input v-model="flowProperties.flowName" placeholder="动词+名词+场景" />
              <span class="text-gray">如：SMT贴片首件检验流程</span>
            </el-form-item>
            <el-form-item label="流程层级">
              <el-select v-model="flowProperties.flowLevel">
                <el-option label="一级流程域" value="level1" />
                <el-option label="二级流程组" value="level2" />
                <el-option label="三级主流程" value="level3" />
                <el-option label="四级子流程" value="level4" />
                <el-option label="五级岗位流程" value="level5" />
              </el-select>
            </el-form-item>
            <el-form-item label="归属体系">
              <el-select v-model="flowProperties.systems" multiple>
                <el-option label="安全生产" value="safety" />
                <el-option label="环境管理" value="environment" />
                <el-option label="职业健康" value="health" />
                <el-option label="能源管理" value="energy" />
                <el-option label="通用体系" value="general" />
              </el-select>
            </el-form-item>
            <el-form-item label="归口部门">
              <el-select v-model="flowProperties.owningDepartment" placeholder="选择主责部门">
                <el-option label="生产部" value="production" />
                <el-option label="品质部" value="quality" />
                <el-option label="设备部" value="equipment" />
                <el-option label="EHS部" value="ehs" />
                <el-option label="研发部" value="rd" />
              </el-select>
            </el-form-item>
            <el-form-item label="配合部门">
              <el-select v-model="flowProperties.cooperatingDepartments" multiple>
                <el-option label="生产部" value="production" />
                <el-option label="品质部" value="quality" />
                <el-option label="设备部" value="equipment" />
                <el-option label="仓储部" value="warehouse" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="详细配置">
          <el-form :model="flowProperties" label-width="140px">
            <el-form-item label="适用场景">
              <el-select v-model="flowProperties.applicableScenario">
                <el-option label="SMT制程" value="smt" />
                <el-option label="组装" value="assembly" />
                <el-option label="测试" value="testing" />
                <el-option label="仓储" value="warehouse" />
                <el-option label="品质" value="quality" />
                <el-option label="设备" value="equipment" />
                <el-option label="EHS" value="ehs" />
                <el-option label="能耗" value="energy" />
                <el-option label="研发" value="rd" />
                <el-option label="采购" value="procurement" />
              </el-select>
            </el-form-item>
            <el-form-item label="流程目标">
              <el-input type="textarea" v-model="flowProperties.flowObjective" :rows="2" />
            </el-form-item>
            <el-form-item label="合规依据">
              <el-select v-model="flowProperties.complianceBasis" multiple>
                <el-option label="ISO9001-8.5.1" value="ISO9001-8.5.1" />
                <el-option label="ISO14001-4.4.5" value="ISO14001-4.4.5" />
                <el-option label="ISO45001-6.1.2" value="ISO45001-6.1.2" />
                <el-option label="GB/T 23331-2020" value="GB/T-23331" />
              </el-select>
            </el-form-item>
            <el-form-item label="风险等级">
              <el-radio-group v-model="flowProperties.riskLevel">
                <el-radio label="高" value="high" />
                <el-radio label="中" value="medium" />
                <el-radio label="低" value="low" />
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生效日期">
              <el-date-picker v-model="flowProperties.effectiveDate" type="date" />
            </el-form-item>
            <el-form-item label="评审周期">
              <el-select v-model="flowProperties.reviewCycle">
                <el-option label="年度评审" value="yearly" />
                <el-option label="季度评审" value="quarterly" />
                <el-option label="月度评审" value="monthly" />
                <el-option label="按需评审" value="on-demand" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showFlowProperties = false">取消</el-button>
        <el-button type="primary" @click="saveFlowProperties">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 流程示例对话框 -->
    <el-dialog title="流程示例 - 采购申请审批流程" v-model="showSampleDialog" width="1200px" fullscreen>
      <div class="sample-container">
        <div class="sample-header">
          <el-alert type="info" :closable="false">
            <template #title>
              <span><strong>流程说明：</strong>采购申请审批流程 - 三级主流程（端到端核心业务流程）</span>
            </template>
          </el-alert>
          <div class="sample-meta">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="流程编号">LY-CG-01-001-V1.0</el-descriptions-item>
              <el-descriptions-item label="流程层级">三级主流程</el-descriptions-item>
              <el-descriptions-item label="归口部门">采购部</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <el-tabs type="card">
          <el-tab-pane label="流程图预览">
            <div class="sample-flowchart">
              <div class="flow-node start-node">
                <div class="node-icon">●</div>
                <div class="node-label">开始事件</div>
                <div class="node-name">业务需求产生</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node task-node">
                <div class="node-icon">□</div>
                <div class="node-label">人工任务</div>
                <div class="node-name">需求部门发起采购申请</div>
                <div class="node-desc">填写《采购申请单》、上传依据</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node task-node">
                <div class="node-icon">□</div>
                <div class="node-label">人工任务</div>
                <div class="node-name">部门负责人初审</div>
                <div class="node-desc">核对需求合理性、预算</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node task-node">
                <div class="node-icon">□</div>
                <div class="node-label">人工任务</div>
                <div class="node-name">采购部专员复核</div>
                <div class="node-desc">核对物料规格、供应商适配</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node gateway-node">
                <div class="node-icon">◇</div>
                <div class="node-label">排他网关</div>
                <div class="node-name">金额/物料类型判定</div>
              </div>
            </div>
            
            <div class="flow-branches">
              <div class="branch-item small-branch">
                <div class="branch-header">
                  <el-tag type="success" size="small">分支1：小额常规物料</el-tag>
                  <span class="branch-condition">≤5000元常规物料</span>
                </div>
                <div class="branch-content">
                  <div class="flow-node task-node">
                    <div class="node-icon">□</div>
                    <div class="node-name">采购部经理终审</div>
                  </div>
                  <div class="flow-arrow">→</div>
                </div>
              </div>
              <div class="branch-item big-branch">
                <div class="branch-header">
                  <el-tag type="warning" size="small">分支2：大额/专项物料</el-tag>
                  <span class="branch-condition">＞5000元/设备/核心物料</span>
                </div>
                <div class="branch-content">
                  <div class="flow-node task-node">
                    <div class="node-icon">□</div>
                    <div class="node-name">财务部预算审核</div>
                  </div>
                  <div class="flow-arrow">→</div>
                  <div class="flow-node task-node">
                    <div class="node-icon">□</div>
                    <div class="node-name">总经理终审</div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="flow-merge">
              <div class="flow-arrow">→</div>
              <div class="flow-node gateway-node">
                <div class="node-icon">◇</div>
                <div class="node-label">合并网关</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node end-node success">
                <div class="node-icon">●</div>
                <div class="node-label">结束事件</div>
                <div class="node-name">审批通过-采购立项办结</div>
              </div>
            </div>
            
            <div class="flow-reject">
              <div class="reject-header">
                <el-tag type="danger" size="small">驳回分支</el-tag>
                <span>任意节点可驳回</span>
              </div>
              <div class="flow-arrow reject-arrow">↓</div>
              <div class="flow-node gateway-node reject">
                <div class="node-icon">◇</div>
                <div class="node-label">驳回判定</div>
              </div>
              <div class="flow-arrow">→</div>
              <div class="flow-node end-node reject">
                <div class="node-icon">●</div>
                <div class="node-label">结束事件</div>
                <div class="node-name">驳回终止-修订重提</div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="节点明细">
            <el-table :data="sampleNodeDetails" border size="small" max-height="500">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="type" label="节点类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.typeTag">{{ row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="id" label="节点ID" width="140" />
              <el-table-column prop="name" label="节点名称" width="180" />
              <el-table-column prop="description" label="节点描述" />
              <el-table-column prop="post" label="责任岗位" width="120" />
              <el-table-column prop="output" label="输出成果" width="160" />
              <el-table-column prop="timeLimit" label="时限" width="80" />
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="BPMN源码">
            <div class="bpmn-source">
              <el-input type="textarea" :rows="20" v-model="sampleBpmnXml" readonly class="bpmn-textarea" />
              <el-button type="primary" @click="copyBpmnXml">复制BPMN源码</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 模板保存对话框 -->
    <el-dialog title="保存为流程模板" v-model="showSaveTemplateDialog" width="600px">
      <el-form :model="templateForm" label-width="120px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" placeholder="给模板起个名称" />
        </el-form-item>
        <el-form-item label="模板分类">
          <el-select v-model="templateForm.category" placeholder="选择模板分类">
            <el-option label="行政管理类" value="admin" />
            <el-option label="人事管理类" value="hr" />
            <el-option label="财务管理类" value="finance" />
            <el-option label="生产质量类" value="production" />
            <el-option label="设备管理类" value="equipment" />
            <el-option label="EHS安全类" value="ehs" />
            <el-option label="供应链类" value="supply" />
            <el-option label="研发管理类" value="rd" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用场景">
          <el-input v-model="templateForm.scenario" placeholder="如：通用审批流程" />
        </el-form-item>
        <el-form-item label="模板说明">
          <el-input type="textarea" v-model="templateForm.description" :rows="3" placeholder="描述模板的用途和特点" />
        </el-form-item>
        <el-form-item label="包含节点样式">
          <el-switch v-model="templateForm.includeStyles" />
          <span class="text-gray" style="margin-left: 8px">保留节点颜色、边框等样式</span>
        </el-form-item>
        <el-form-item label="设为公共模板">
          <el-switch v-model="templateForm.isPublic" />
          <span class="text-gray" style="margin-left: 8px">其他用户可以使用此模板</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSaveTemplateDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveTemplate">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 模板加载对话框 -->
    <el-dialog title="从模板加载" v-model="showLoadTemplateDialog" width="900px">
      <div class="template-load-header">
        <el-input v-model="templateSearchKeyword" placeholder="搜索模板名称" style="width: 300px" clearable />
        <el-select v-model="templateFilterCategory" placeholder="筛选分类" style="width: 150px" clearable>
          <el-option label="行政管理类" value="admin" />
          <el-option label="人事管理类" value="hr" />
          <el-option label="生产质量类" value="production" />
          <el-option label="设备管理类" value="equipment" />
          <el-option label="EHS安全类" value="ehs" />
        </el-select>
      </div>
      <el-table :data="filteredTemplates" style="width: 100%; margin-top: 16px" max-height="400">
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scenario" label="适用场景" width="150" />
        <el-table-column prop="nodeCount" label="节点数" width="80" />
        <el-table-column prop="creator" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="loadTemplate(row)">加载</el-button>
            <el-button size="small" @click="previewTemplate(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 样式替换对话框 -->
    <el-dialog title="批量样式替换" v-model="showStyleReplaceDialog" width="700px">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <template #title>
          <span>批量替换选中节点的样式，统一企业流程视觉规范</span>
        </template>
      </el-alert>
      <el-form :model="styleReplaceForm" label-width="120px">
        <el-form-item label="已选节点">
          <el-tag v-for="nodeId in selectedNodesForStyle" :key="nodeId" size="small" style="margin-right: 8px">
            {{ getNodeLabel(nodeId) }}
          </el-tag>
          <span class="text-gray">共 {{ selectedNodesForStyle.length }} 个节点</span>
        </el-form-item>
        <el-form-item label="节点形状">
          <el-radio-group v-model="styleReplaceForm.shape">
            <el-radio label="rounded">圆角矩形</el-radio>
            <el-radio label="rectangle">矩形</el-radio>
            <el-radio label="diamond">菱形</el-radio>
            <el-radio label="ellipse">椭圆形</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="边框颜色">
          <el-color-picker v-model="styleReplaceForm.borderColor" />
          <el-select v-model="styleReplaceForm.borderColorPreset" placeholder="预设颜色" style="width: 120px; margin-left: 8px">
            <el-option label="绿色-开始" value="#67c23a" />
            <el-option label="红色-结束" value="#f56c6c" />
            <el-option label="蓝色-任务" value="#409eff" />
            <el-option label="橙色-审批" value="#e6a23c" />
            <el-option label="灰色-网关" value="#909399" />
          </el-select>
        </el-form-item>
        <el-form-item label="背景颜色">
          <el-color-picker v-model="styleReplaceForm.bgColor" />
        </el-form-item>
        <el-form-item label="字体大小">
          <el-input-number v-model="styleReplaceForm.fontSize" :min="10" :max="20" />
        </el-form-item>
        <el-form-item label="边框宽度">
          <el-slider v-model="styleReplaceForm.borderWidth" :min="1" :max="5" show-stops />
        </el-form-item>
        <el-form-item label="应用阴影">
          <el-switch v-model="styleReplaceForm.shadow" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStyleReplaceDialog = false">取消</el-button>
        <el-button @click="resetStyleReplace">重置</el-button>
        <el-button type="primary" @click="confirmStyleReplace">确认应用</el-button>
      </template>
    </el-dialog>

    <!-- 制度关联对话框 -->
    <el-dialog title="关联制度文件" v-model="showPolicyBindDialog" width="800px">
      <el-tabs v-model="policyBindTab">
        <el-tab-pane label="绑定已有制度" name="existing">
          <div class="policy-search">
            <el-input v-model="policySearchKeyword" placeholder="搜索制度名称或编号" style="width: 300px" clearable />
            <el-select v-model="policyFilterType" placeholder="制度类型" style="width: 150px" clearable>
              <el-option label="程序文件" value="procedure" />
              <el-option label="管理制度" value="management" />
              <el-option label="作业SOP" value="sop" />
              <el-option label="表单记录" value="form" />
            </el-select>
            <el-select v-model="policyFilterSystem" placeholder="所属体系" style="width: 150px" clearable>
              <el-option label="质量管理体系" value="quality" />
              <el-option label="安全生产体系" value="safety" />
              <el-option label="环境管理体系" value="environment" />
              <el-option label="职业健康体系" value="health" />
            </el-select>
          </div>
          <el-table :data="filteredPolicies" style="width: 100%; margin-top: 16px" max-height="350">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="code" label="制度编号" width="140" />
            <el-table-column prop="name" label="制度名称" />
            <el-table-column prop="system" label="所属体系" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.system }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" @click="bindPolicy(row)">绑定</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="创建新制度" name="create">
          <el-form :model="newPolicyForm" label-width="140px">
            <el-form-item label="制度编号">
              <el-input v-model="newPolicyForm.code" placeholder="系统自动生成" disabled />
            </el-form-item>
            <el-form-item label="制度名称" required>
              <el-input v-model="newPolicyForm.name" placeholder="输入制度名称" />
            </el-form-item>
            <el-form-item label="所属体系">
              <el-select v-model="newPolicyForm.system">
                <el-option label="质量管理体系" value="quality" />
                <el-option label="安全生产体系" value="safety" />
                <el-option label="环境管理体系" value="environment" />
                <el-option label="职业健康体系" value="health" />
              </el-select>
            </el-form-item>
            <el-form-item label="制度类型">
              <el-select v-model="newPolicyForm.type">
                <el-option label="程序文件" value="procedure" />
                <el-option label="管理制度" value="management" />
                <el-option label="作业SOP" value="sop" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用场景">
              <el-input v-model="newPolicyForm.scenario" placeholder="描述适用场景" />
            </el-form-item>
          </el-form>
          <div style="text-align: right; margin-top: 16px">
            <el-button type="primary" @click="createAndBindPolicy">创建并绑定</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 版本同步设置对话框 -->
    <el-dialog title="版本同步设置" v-model="showVersionSyncDialog" width="600px">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <template #title>
          <span>流程图与制度文件版本联动：流程图修改后自动触发对应制度待复核状态</span>
        </template>
      </el-alert>
      <el-form :model="versionSyncForm" label-width="140px">
        <el-form-item label="关联制度">
          <el-select v-model="versionSyncForm.policyCode" placeholder="选择关联制度" style="width: 100%">
            <el-option label="隐患排查治理流程 V2.0 - ZD-EHS-003" value="ZD-EHS-003" />
            <el-option label="设备维护管理流程 V1.5 - ZD-EM-001" value="ZD-EM-001" />
            <el-option label="质量控制程序 V3.0 - ZD-QC-001" value="ZD-QC-001" />
          </el-select>
        </el-form-item>
        <el-form-item label="同步模式">
          <el-radio-group v-model="versionSyncForm.mode">
            <el-radio label="auto">自动同步（流程变更自动更新制度版本）</el-radio>
            <el-radio label="manual">手动确认（流程变更需确认后同步）</el-radio>
            <el-radio label="none">不同步（独立版本管理）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="版本映射规则">
          <el-select v-model="versionSyncForm.mappingRule" placeholder="选择版本映射规则">
            <el-option label="流程大版本 = 制度大版本" value="major" />
            <el-option label="流程任意变更 = 制度小版本" value="any" />
            <el-option label="手动指定映射关系" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更自动提醒">
          <el-switch v-model="versionSyncForm.autoNotify" />
          <span class="text-gray" style="margin-left: 8px">流程变更后自动提醒制度负责人</span>
        </el-form-item>
        <el-form-item label="当前同步状态">
          <el-tag :type="versionSyncForm.synced ? 'success' : 'warning'">
            {{ versionSyncForm.synced ? '已同步' : '未同步' }}
          </el-tag>
          <span class="text-gray" style="margin-left: 8px">流程v1.2.0 对比 制度v1.1.0</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVersionSyncDialog = false">取消</el-button>
        <el-button type="primary" @click="saveVersionSync">保存设置</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue';
import { CirclePlus, CircleClose, Checked, Refresh, FolderOpened, Clock, Lightning, Minus, Delete, RefreshLeft, DArrowLeft, Sort, DArrowRight, Top, ScaleToOriginal, Bottom, Plus, FullScreen, CircleCheck, CircleCloseFilled, Timer, Message, User, Cpu, Connection, ZoomOut, ZoomIn, RefreshRight, DocumentAdd, View, Grid, Warning, Aim, DocumentChecked, Calendar, WarnTriangleFilled, DocumentCopy, Promotion, Check, Setting, OfficeBuilding, EditPen, Edit, DataLine, CollectionTag, List, Picture, Link, TrendCharts } from '@element-plus/icons-vue';

// ========== AntV X6 核心模块导入（对标杰成EPROS技术底座）==========
import { Graph } from '@antv/x6'
import axios from '@/api/axiosConfig';
const flowName = ref('SMT贴片流程');
const flowCode = ref('FP030202');
const flowLevel = ref('level3');
const flowDomain = ref('FD05');
const flowStatus = ref('draft');
const currentVersion = ref('1.0.0');
// 当前已持久化的流程定义ID（保存草稿后回填，新版本切换时更新）
const currentDefId = ref(null);
// 后端状态 -> 设计器状态
const backendStatusToUi = (s) => ({ active: 'effective', deprecated: 'void' }[s] || s);
const uiVersionToBackend = (v) => 'V' + v.split('.').slice(0, 2).join('.');
const backendVersionToUi = (v) => {
  if (!v) return currentVersion.value;
  const m = /^V?(\d+)\.(\d+)$/.exec(v);
  return m ? `${m[1]}.${m[2]}.0` : v;
};
const buildDefPayload = () => ({
  code: flowCode.value,
  name: flowName.value,
  level: flowLevel.value,
  category: flowDomain.value,
  version: uiVersionToBackend(currentVersion.value),
  ownerDept: '',
  description: '',
  content: JSON.stringify({ nodes: nodes.value, connections: connections.value, bpmnXml: sampleBpmnXml.value })
});
// 持久化草稿：无ID则新建，有ID则更新
const persistDefinition = async () => {
  const payload = buildDefPayload();
  const res = currentDefId.value
    ? await axios.put(`/api/process/def/${currentDefId.value}`, payload)
    : await axios.post('/api/process/def', payload);
  const saved = res.data && res.data.data ? res.data.data : res.data;
  if (saved && saved.id) currentDefId.value = saved.id;
  return saved;
};
const flowStatusOptions = [
 { label: '草稿', value: 'draft', color: 'info' },
 { label: '待审核', value: 'pending', color: 'warning' },
 { label: '生效', value: 'effective', color: 'success' },
 { label: '作废', value: 'void', color: 'danger' },
 { label: '归档', value: 'archived', color: '' }
];
const drawMode = ref('');
const zoomLevel = ref(100);
const canvasContainer = ref(null);
const canvasGrid = ref(null);
const designerActiveTab = ref('flow-design');
const docPreviewVisible = ref(false);
const previewContent = ref('');

// 富文本编辑器相关变量
const editorStyle = reactive({
  font: '微软雅黑',
  fontSize: '14px',
  heading: ''
});
const selectedTemplate = ref('');
const outlineTree = ref([
  { id: 1, label: '目的', level: 1, page: 1 },
  { id: 2, label: '适用范围', level: 1, page: 1 },
  { id: 3, label: '职责与权限', level: 1, page: 2 },
  { id: 4, label: '术语定义', level: 1, page: 3 },
  { id: 5, label: '管理内容', level: 1, page: 4 },
  { id: 6, label: '工作流程', level: 1, page: 5 },
  { id: 7, label: '考核与监督', level: 1, page: 6 },
  { id: 8, label: '附则', level: 1, page: 7 }
]);
const treeProps = {
  children: 'children',
  label: 'label'
};
const wordCount = ref(500);
const paragraphCount = ref(8);

// ========== AntV X6 Graph 实例变量（对标杰成EPROS画布引擎）==========
let graph = null; // X6 Graph 主实例
const graphContainerId = 'x6-flow-canvas'; // X6 画布容器 ID
const graphDnd = ref(null); // Dnd 拖拽插件实例
const graphSelection = ref(null); // Selection 选择插件实例
const graphHistory = ref(null); // History 历史插件实例
const graphSnapline = ref(null); // Snapline 对齐线插件实例
const activePropertyTab = ref('basic');
const showExportMenu = ref(false);
const showSaveTemplateDialog = ref(false);
const showLoadTemplateDialog = ref(false);
const showStyleReplaceDialog = ref(false);
const showPolicyBindDialog = ref(false);
const showVersionSyncDialog = ref(false);
const policyBindTab = ref('existing');
const templateSearchKeyword = ref('');
const templateFilterCategory = ref('');
const policySearchKeyword = ref('');
const policyFilterType = ref('');
const policyFilterSystem = ref('');
const selectedNodesForStyle = ref([]);
const templateForm = reactive({
  name: '',
  category: '',
  scenario: '',
  description: '',
  includeStyles: true,
  isPublic: false
});
const styleReplaceForm = reactive({
  shape: 'rounded',
  borderColor: '#409eff',
  borderColorPreset: '',
  bgColor: '#ffffff',
  fontSize: 12,
  borderWidth: 2,
  shadow: true
});
const newPolicyForm = reactive({
  name: '',
  system: 'quality',
  type: 'procedure',
  scenario: ''
});
const versionSyncForm = reactive({
  policyCode: '',
  mode: 'auto',
  mappingRule: 'any',
  autoNotify: true,
  synced: false
});
const templateList = ref([
  { id: 1, name: '标准审批流程', category: 'admin', scenario: '通用审批', nodeCount: 5, creator: '系统管理员', createTime: '2026-01-15' },
  { id: 2, name: '采购申请流程', category: 'supply', scenario: '采购管理', nodeCount: 8, creator: '张伟', createTime: '2026-02-20' },
  { id: 3, name: '设备点检流程', category: 'equipment', scenario: '设备维护', nodeCount: 6, creator: '李明', createTime: '2026-03-10' },
  { id: 4, name: '安全生产流程', category: 'ehs', scenario: '安全管理', nodeCount: 10, creator: '刘洋', createTime: '2026-04-05' }
]);
const policyList = ref([
  { id: 1, code: 'ZD-QC-001', name: '质量控制程序', system: '质量管理体系', version: 'V3.0', type: 'procedure' },
  { id: 2, code: 'ZD-EHS-003', name: '隐患排查治理制度', system: '安全生产体系', version: 'V2.0', type: 'management' },
  { id: 3, code: 'ZD-EM-001', name: '设备维护管理制度', system: '设备管理', version: 'V1.5', type: 'management' },
  { id: 4, code: 'SOP-PM-001', name: 'SMT贴片作业指导书', system: '生产质量', version: 'V2.1', type: 'sop' }
]);
const showNodeDetails = ref(true);
const validateModalVisible = ref(false);
const validateResult = ref(null);
const selectedConnection = ref(null);
const drawingLine = ref(null);
const history = ref([]);
const historyIndex = ref(-1);
const selectedNodes = ref([]);
const isSelecting = ref(false);
const selectBox = ref(null);
const conditionBuilderVisible = ref(false);
const versionHistory = ref([]);
const showVersionHistory = ref(false);
const showVersionCompare = ref(false);
const compareVersion = ref(null);
const showPublishDialog = ref(false);
const showFlowProperties = ref(false);
const showSampleDialog = ref(false);
const publishForm = reactive({
 notify: true,
 createTraining: true,
 publishType: 'effective'
});
const operationLogs = ref([]);
const flowProperties = reactive({
 flowCode: '',
 flowName: '',
 flowLevel: 'level3',
 flowDomain: 'FD05',
 systems: [],
 owningDepartment: '',
 cooperatingDepartments: [],
 applicableScenario: '',
 flowObjective: '',
 complianceBasis: [],
 riskLevel: 'medium',
 effectiveDate: '',
 voidDate: '',
 reviewCycle: 'yearly',
 reviewDate: ''
});
const nodes = ref([
 { id: 1, type: 'bpmn-start', x: 200, y: 80, label: '开始', assignee: '', approvalMode: '', timeLimit: 0 },
 { id: 2, type: 'bpmn-task', x: 200, y: 180, label: '领料准备', assignee: '岗位:仓库管理员', approvalMode: 'sequence', timeLimit: 2 },
 { id: 3, type: 'bpmn-approval', x: 200, y: 300, label: '班长审批', assignee: '岗位:生产班长', approvalMode: 'sequence', timeLimit: 4 },
 { id: 4, type: 'bpmn-task', x: 200, y: 420, label: '贴片操作', assignee: '岗位:SMT操作员', approvalMode: 'sequence', timeLimit: 8 },
 { id: 5, type: 'bpmn-approval', x: 200, y: 540, label: '质检确认', assignee: '岗位:IPQC检验员', approvalMode: 'sequence', timeLimit: 2 },
 { id: 6, type: 'bpmn-end', x: 200, y: 660, label: '结束', assignee: '', approvalMode: '', timeLimit: 0 }
]);
const connections = ref([
 { from: 1, to: 2, label: '', condition: '', priority: 1 },
 { from: 2, to: 3, label: '', condition: '', priority: 1 },
 { from: 3, to: 4, label: '', condition: '', priority: 1 },
 { from: 4, to: 5, label: '', condition: '', priority: 1 },
 { from: 5, to: 6, label: '', condition: '', priority: 1 }
]);
const selectedNode = ref(null);
const nodeProperties = reactive({
 label: '',
 description: '',
 approvalType: '',
 approvalMode: 'sequence',
 approvalConfig: '',
 assigneeType: 'position',
 assignee: '',
 timeLimit: 0,
 timeUnit: 'hour',
 timeoutAction: 'remind',
 timeoutWarning: 50,
 supervisor: '',
 skipEmpty: false,
 editableFields: [],
 readonlyFields: [],
 hiddenFields: [],
 requiredFields: [],
 what: '',
 why: '',
 who: '',
 when: '',
 where: '',
 rule: '',
 result: '',
 inputs: '',
 outputs: '',
 policy: '',
 sop: '',
 form: '',
 compliance: [],
 evidenceRequirements: [],
 riskPoints: '',
 controlMeasures: '',
 rejectRule: 'previous',
 rejectTarget: '',
 flowCondition: '',
 events: [],
 enterEvent: 'none',
 completeEvent: 'none',
 rejectEvent: 'none',
 riskTip: ''
});
const connectionProperties = reactive({
 label: '',
 condition: '',
 priority: 1
});
const conditionBuilder = reactive({
 field: '',
 operator: '',
 value: ''
});
const showConditionPresets = ref(false);
const activeErrorType = ref('BPMN语法');
const buildCondition = () => {
 if (conditionBuilder.field && conditionBuilder.operator && conditionBuilder.value) {
 connectionProperties.condition = `${conditionBuilder.field} ${conditionBuilder.operator} ${conditionBuilder.value}`;
 }
};
const applyPreset = (preset) => {
 connectionProperties.condition = preset;
 showConditionPresets.value = false;
};
const getErrorCountByType = (type) => {
 return validateResult.value?.errors?.filter(e => e.type === type).length || 0;
};
const getErrorsByType = (type) => {
 return validateResult.value?.errors?.filter(e => e.type === type) || [];
};
const nodeCount = computed(() => nodes.value.length);
const edgeCount = computed(() => connections.value.length);
const filteredTemplates = computed(() => {
  let result = templateList.value;
  if (templateSearchKeyword.value) {
    result = result.filter(t => t.name.includes(templateSearchKeyword.value));
  }
  if (templateFilterCategory.value) {
    result = result.filter(t => t.category === templateFilterCategory.value);
  }
  return result;
});
const filteredPolicies = computed(() => {
  let result = policyList.value;
  if (policySearchKeyword.value) {
    result = result.filter(p => p.name.includes(policySearchKeyword.value) || p.code.includes(policySearchKeyword.value));
  }
  if (policyFilterType.value) {
    result = result.filter(p => p.type === policyFilterType.value);
  }
  if (policyFilterSystem.value) {
    result = result.filter(p => p.system === policyFilterSystem.value);
  }
  return result;
});
const getNodeIcon = (type) => {
 const icons = {
 'bpmn-start': CirclePlus,
 'bpmn-end': CircleClose,
 'bpmn-intermediate': Clock,
 'bpmn-timer': Timer,
 'bpmn-message': Message,
 'bpmn-task': User,
 'bpmn-approval': Checked,
 'bpmn-service': Cpu,
 'bpmn-script': Cpu,
 'bpmn-subprocess': FolderOpened,
 'bpmn-gateway-exclusive': Refresh,
 'bpmn-gateway-parallel': Connection,
 'bpmn-gateway-inclusive': Refresh
 };
 return icons[type] || Grid;
};
const getNodeTypeName = (type) => {
 const names = {
 'bpmn-start': '开始事件',
 'bpmn-end': '结束事件',
 'bpmn-intermediate': '中间事件',
 'bpmn-timer': '定时事件',
 'bpmn-message': '消息事件',
 'bpmn-task': '人工任务',
 'bpmn-approval': '审批任务',
 'bpmn-service': '服务任务',
 'bpmn-script': '脚本任务',
 'bpmn-subprocess': '子流程',
 'bpmn-gateway-exclusive': '排他网关',
 'bpmn-gateway-parallel': '并行网关',
 'bpmn-gateway-inclusive': '包容网关'
 };
 return names[type] || type;
};
const getNodeTypeTagType = (type) => {
 const types = {
 'bpmn-start': 'success',
 'bpmn-end': 'danger',
 'bpmn-intermediate': 'warning',
 'bpmn-timer': 'warning',
 'bpmn-message': 'info',
 'bpmn-task': 'primary',
 'bpmn-approval': 'success',
 'bpmn-service': 'info',
 'bpmn-script': 'info',
 'bpmn-subprocess': 'warning',
 'bpmn-gateway-exclusive': 'danger',
 'bpmn-gateway-parallel': 'primary',
 'bpmn-gateway-inclusive': 'warning'
 };
 return types[type] || 'info';
};
const getConnectionPath = (conn) => {
 const fromNode = nodes.value.find(n => n.id === conn.from);
 const toNode = nodes.value.find(n => n.id === conn.to);
 if (!fromNode || !toNode)
 return '';
 const fromX = fromNode.x + 60;
 const fromY = fromNode.y + 40;
 const toX = toNode.x;
 const toY = toNode.y + 40;
 if (fromX === toX) {
 return `M ${fromX} ${fromY} L ${toX} ${toY}`;
 }
 const midY = (fromY + toY) / 2;
 return `M ${fromX} ${fromY} L ${fromX} ${midY} L ${toX} ${midY} L ${toX} ${toY}`;
};
const getConnectionColor = (conn) => {
 if (conn.condition && conn.condition.trim()) {
 return '#e6a23c';
 }
 return '#666';
};
const getConnectionArrow = (conn) => {
 if (conn.condition && conn.condition.trim()) {
 return 'arrowhead-green';
 }
 return 'arrowhead';
};
const startDragNode = (type) => {
 // 如果 X6 Graph 已初始化，使用 X6 添加节点
 if (graph) {
 // 计算画布中心位置
 const container = document.getElementById(graphContainerId);
 const centerX = container ? container.offsetWidth / 2 : 400;
 const centerY = container ? container.offsetHeight / 2 : 300;
 
 const nodeConfig = {
 'bpmn-start': { shape: 'bpmn-start', label: '开始', width: 50, height: 50 },
 'bpmn-end': { shape: 'bpmn-end', label: '结束', width: 50, height: 50 },
 'bpmn-intermediate': { shape: 'bpmn-task', label: '中间事件', width: 120, height: 60 },
 'bpmn-timer': { shape: 'bpmn-task', label: '定时事件', width: 120, height: 60 },
 'bpmn-message': { shape: 'bpmn-task', label: '消息事件', width: 120, height: 60 },
 'bpmn-task': { shape: 'bpmn-task', label: '任务节点', width: 120, height: 60 },
 'bpmn-approval': { shape: 'bpmn-approval', label: '审批节点', width: 120, height: 60 },
 'bpmn-service': { shape: 'bpmn-task', label: '服务调用', width: 120, height: 60 },
 'bpmn-script': { shape: 'bpmn-task', label: '脚本任务', width: 120, height: 60 },
 'bpmn-subprocess': { shape: 'bpmn-subprocess', label: '子流程', width: 120, height: 80 },
 'bpmn-gateway-exclusive': { shape: 'bpmn-gateway-exclusive', label: '排他网关', width: 60, height: 60 },
 'bpmn-gateway-parallel': { shape: 'bpmn-gateway-parallel', label: '并行网关', width: 60, height: 60 },
 'bpmn-gateway-inclusive': { shape: 'bpmn-gateway-inclusive', label: '包容网关', width: 60, height: 60 }
 };
 
 const config = nodeConfig[type] || { shape: 'bpmn-task', label: '任务节点', width: 120, height: 60 };
 const newId = `node_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
 
 graph.addNode({
 id: newId,
 shape: config.shape,
 x: centerX - config.width / 2 + Math.random() * 100 - 50,
 y: centerY - config.height / 2 + Math.random() * 100 - 50,
 width: config.width,
 height: config.height,
 attrs: {
 text: { text: config.label }
 },
 data: {
 what: '',
 why: '',
 who: '',
 when: '',
 where: '',
 rule: '',
 result: ''
 }
 });
 
 // 同步到 Vue 数据
 syncGraphToNodes();
 saveHistory();
 ElMessage.success(`已添加 ${config.label}`);
 } else {
 // 兼容旧版本：使用 Vue 数据
 const newId = Date.now();
 const nodeLabels = {
 'bpmn-start': '开始',
 'bpmn-end': '结束',
 'bpmn-intermediate': '中间事件',
 'bpmn-timer': '定时',
 'bpmn-message': '消息',
 'bpmn-task': '任务',
 'bpmn-approval': '审批',
 'bpmn-service': '服务',
 'bpmn-script': '脚本',
 'bpmn-subprocess': '子流程',
 'bpmn-gateway-exclusive': '排他网关',
 'bpmn-gateway-parallel': '并行网关',
 'bpmn-gateway-inclusive': '包容网关'
 };
 nodes.value.push({
 id: newId,
 type,
 x: 300 + Math.random() * 100,
 y: 200 + Math.random() * 100,
 label: nodeLabels[type],
 assignee: type === 'bpmn-approval' ? '岗位审批' : '',
 approvalMode: type === 'bpmn-approval' ? 'sequence' : '',
 timeLimit: 0
 });
 saveHistory();
 }
};

// ========== 工具栏拖拽初始化（X6 Dnd 插件）==========
const initDragAndDrop = () => {
 nextTick(() => {
 // 获取工具栏项目
 const toolItems = document.querySelectorAll('.tool-item');
 toolItems.forEach(item => {
 item.draggable = true;
 
 item.addEventListener('dragstart', (e) => {
 const nodeType = item.getAttribute('data-node-type') || extractNodeType(item);
 if (nodeType) {
 e.dataTransfer.setData('nodeType', nodeType);
 e.dataTransfer.effectAllowed = 'copy';
 }
 });
 
 item.addEventListener('dragend', (e) => {
 e.dataTransfer.clearData();
 });
 });
 
 // 初始化 X6 Dnd 插件
 if (graph && !graphDnd.value) {
 const dnd = new Dnd({
 target: graph,
 })
 graphDnd.value = dnd;
 
 // 为画布容器添加拖拽放下事件
 const canvasContainer = document.getElementById(graphContainerId);
 if (canvasContainer) {
 canvasContainer.addEventListener('dragover', (e) => {
 e.preventDefault();
 e.dataTransfer.dropEffect = 'copy';
 });
 
 canvasContainer.addEventListener('drop', (e) => {
 e.preventDefault();
 const nodeType = e.dataTransfer.getData('nodeType');
 if (nodeType && graph) {
 const rect = canvasContainer.getBoundingClientRect();
 const x = e.clientX - rect.left;
 const y = e.clientY - rect.top;
 addNodeToGraph(nodeType, x, y);
 }
 });
 }
 
 console.log('X6 Dnd 拖拽初始化完成');
 }
 });
};

// 从 tool-item 中提取节点类型
const extractNodeType = (element) => {
 const title = element.getAttribute('title') || '';
 if (title.includes('开始')) return 'bpmn-start';
 if (title.includes('结束')) return 'bpmn-end';
 if (title.includes('任务') || title.includes('人工')) return 'bpmn-task';
 if (title.includes('审批')) return 'bpmn-approval';
 if (title.includes('服务')) return 'bpmn-service';
 if (title.includes('脚本')) return 'bpmn-script';
 if (title.includes('子流程')) return 'bpmn-subprocess';
 if (title.includes('排他')) return 'bpmn-gateway-exclusive';
 if (title.includes('并行')) return 'bpmn-gateway-parallel';
 if (title.includes('包容')) return 'bpmn-gateway-inclusive';
 if (title.includes('中间')) return 'bpmn-intermediate';
 if (title.includes('定时')) return 'bpmn-timer';
 if (title.includes('消息')) return 'bpmn-message';
 return null;
};

const selectNode = (node, event) => {
 if (event?.shiftKey && selectedNodes.value.length > 0) {
 if (!selectedNodes.value.includes(node.id)) {
 selectedNodes.value.push(node.id);
 }
 } else if (event?.ctrlKey || event?.metaKey) {
 const idx = selectedNodes.value.indexOf(node.id);
 if (idx > -1) {
 selectedNodes.value.splice(idx, 1);
 } else {
 selectedNodes.value.push(node.id);
 }
 } else {
 selectedNodes.value = [node.id];
 }
 selectedNode.value = node;
 selectedConnection.value = null;
 Object.assign(nodeProperties, {
 label: node.label,
 description: node.description || '',
 approvalType: '',
 approvalMode: node.approvalMode || 'sequence',
 approvalConfig: node.assignee || '',
 timeLimit: node.timeLimit || 0,
 timeoutAction: 'remind',
 remindType: 'none',
 editableFields: [],
 readonlyFields: [],
 hiddenFields: [],
 what: '',
 why: '',
 who: '',
 when: '',
 where: '',
 rule: '',
 result: '',
 policy: '',
 sop: '',
 form: '',
 compliance: [],
 events: node.events || []
 });
};
const batchDelete = () => {
 if (selectedNodes.value.length === 0) return;
 ElMessageBox.confirm(`确定删除选中的 ${selectedNodes.value.length} 个节点吗？`, '提示', {
 confirmButtonText: '确定',
 cancelButtonText: '取消',
 type: 'warning'
 }).then(() => {
 selectedNodes.value.forEach(id => {
 connections.value = connections.value.filter(c => c.from !== id && c.to !== id);
 });
 nodes.value = nodes.value.filter(n => !selectedNodes.value.includes(n.id));
 selectedNodes.value = [];
 selectedNode.value = null;
 saveHistory();
 ElMessage.success(`已删除 ${selectedNodes.value.length} 个节点`);
 }).catch(() => {
 ElMessage.info('已取消');
 });
};
const batchCopy = () => {
 if (selectedNodes.value.length === 0) return;
 const copiedNodes = nodes.value.filter(n => selectedNodes.value.includes(n.id));
 const offset = 50;
 const newNodes = copiedNodes.map((node, idx) => ({
 ...JSON.parse(JSON.stringify(node)),
 id: Date.now() + idx,
 x: node.x + offset * (idx % 3),
 y: node.y + offset * Math.floor(idx / 3)
 }));
 const newNodeMap = {};
 copiedNodes.forEach((original, idx) => {
 newNodeMap[original.id] = newNodes[idx].id;
 });
 const newConnections = [];
 connections.value.forEach(conn => {
 if (selectedNodes.value.includes(conn.from) && selectedNodes.value.includes(conn.to)) {
 newConnections.push({
 ...conn,
 from: newNodeMap[conn.from],
 to: newNodeMap[conn.to],
 id: Date.now() + idx
 });
 }
 });
 nodes.value.push(...newNodes);
 connections.value.push(...newConnections);
 selectedNodes.value = newNodes.map(n => n.id);
 saveHistory();
 ElMessage.success(`已复制 ${newNodes.length} 个节点`);
};
const batchUpdateProperties = (props) => {
 selectedNodes.value.forEach(id => {
 const node = nodes.value.find(n => n.id === id);
 if (node) {
 Object.assign(node, props);
 }
 });
 saveHistory();
 ElMessage.success('批量修改成功');
};
const updateNodeLabel = () => {
 if (selectedNode.value) {
 selectedNode.value.label = nodeProperties.label;
 saveHistory();
 }
};
const deleteSelectedNode = () => {
 if (selectedNode.value) {
 const index = nodes.value.findIndex(n => n.id === selectedNode.value.id);
 if (index > -1) {
 nodes.value.splice(index, 1);
 connections.value = connections.value.filter(c => c.from !== selectedNode.value.id && c.to !== selectedNode.value.id);
 selectedNode.value = null;
 saveHistory();
 ElMessage.success('节点已删除');
 }
 }
};
const selectConnection = (conn, index) => {
 selectedConnection.value = { connection: conn, index };
 selectedNode.value = null;
 Object.assign(connectionProperties, {
 label: conn.label || '',
 condition: conn.condition || '',
 priority: conn.priority || 1
 });
};
const toggleDrawMode = (mode) => {
 drawMode.value = drawMode.value === mode ? '' : mode;
 if (drawMode.value === 'line' && selectedNode.value) {
 const node = selectedNode.value;
 drawingLine.value = {
 startX: node.x + 60,
 startY: node.y + 40,
 endX: node.x + 60,
 endY: node.y + 40
 };
 }
};
const startDrag = (node, event) => {
 if (drawMode.value === 'delete') {
 deleteSelectedNode();
 return;
 }
 if (drawMode.value === 'line') {
 drawingLine.value = {
 startX: node.x + 60,
 startY: node.y + 40,
 endX: node.x + 60,
 endY: node.y + 40
 };
 return;
 }
 const startX = event.clientX;
 const startY = event.clientY;
 const startNodeX = node.x;
 const startNodeY = node.y;
 const handleMouseMove = (e) => {
 node.x = startNodeX + e.clientX - startX;
 node.y = startNodeY + e.clientY - startY;
 };
 const handleMouseUp = () => {
 document.removeEventListener('mousemove', handleMouseMove);
 document.removeEventListener('mouseup', handleMouseUp);
 saveHistory();
 };
 document.addEventListener('mousemove', handleMouseMove);
 document.addEventListener('mouseup', handleMouseUp);
};
const handleCanvasClick = (event) => {
 if (event.target === canvasGrid.value || event.target.classList.contains('canvas-grid')) {
 selectedNode.value = null;
 selectedConnection.value = null;
 if (drawMode.value === 'line' && drawingLine.value) {
 drawingLine.value = null;
 }
 }
};
const handleDrop = (event) => {
 const data = event.dataTransfer.getData('nodeType');
 if (data) {
 const rect = canvasGrid.value.getBoundingClientRect();
 const x = event.clientX - rect.left - 60;
 const y = event.clientY - rect.top - 40;
 addDroppedNode(data, x, y);
 }
};
const addDroppedNode = (type, x, y) => {
 const newId = Date.now();
 const nodeLabels = {
 'bpmn-start': '开始',
 'bpmn-end': '结束',
 'bpmn-intermediate': '中间事件',
 'bpmn-timer': '定时',
 'bpmn-message': '消息',
 'bpmn-task': '任务',
 'bpmn-approval': '审批',
 'bpmn-service': '服务',
 'bpmn-script': '脚本',
 'bpmn-subprocess': '子流程',
 'bpmn-gateway-exclusive': '排他网关',
 'bpmn-gateway-parallel': '并行网关',
 'bpmn-gateway-inclusive': '包容网关'
 };
 nodes.value.push({
 id: newId,
 type,
 x,
 y,
 label: nodeLabels[type],
 assignee: type === 'bpmn-approval' ? '岗位审批' : '',
 approvalMode: type === 'bpmn-approval' ? 'sequence' : '',
 timeLimit: 0
 });
 saveHistory();
};
const createPort = (node, position) => {
 if (drawMode.value === 'line' && drawingLine.value) {
 const toX = node.x + (position === 'left' ? 0 : position === 'right' ? 120 : 60);
 const toY = node.y + (position === 'top' ? 0 : position === 'bottom' ? 80 : 40);
 if (!connections.value.find(c => c.from === drawingLine.value.fromNodeId && c.to === node.id)) {
 connections.value.push({
 from: drawingLine.value.fromNodeId,
 to: node.id,
 label: '',
 condition: '',
 priority: 1
 });
 }
 drawingLine.value = null;
 drawMode.value = '';
 saveHistory();
 }
};
const clearCanvas = () => {
 ElMessageBox.confirm('确定要清空画布吗？', '提示', {
 confirmButtonText: '确定',
 cancelButtonText: '取消',
 type: 'warning'
 }).then(() => {
 nodes.value = [];
 connections.value = [];
 selectedNode.value = null;
 saveHistory();
 ElMessage.success('画布已清空');
 }).catch(() => {
 ElMessage.info('已取消');
 });
};
const fitCanvas = () => {
 if (nodes.value.length === 0) {
 zoomLevel.value = 100;
 return;
 }
 const minX = Math.min(...nodes.value.map(n => n.x));
 const maxX = Math.max(...nodes.value.map(n => n.x + 120));
 const minY = Math.min(...nodes.value.map(n => n.y));
 const maxY = Math.max(...nodes.value.map(n => n.y + 80));
 const canvasWidth = canvasContainer.value?.clientWidth || 800;
 const canvasHeight = canvasContainer.value?.clientHeight || 600;
 const scaleX = (canvasWidth - 50) / (maxX - minX + 100);
 const scaleY = (canvasHeight - 50) / (maxY - minY + 100);
 zoomLevel.value = Math.min(Math.floor(Math.min(scaleX, scaleY) * 100), 100);
};
const alignLeft = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const minX = Math.min(...selected.map(n => n.x));
 selected.forEach(n => {
 n.x = minX;
 });
 saveHistory();
};
const alignCenter = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const minX = Math.min(...selected.map(n => n.x));
 const maxX = Math.max(...selected.map(n => n.x));
 const centerX = (minX + maxX) / 2;
 selected.forEach(n => {
 n.x = centerX - 60;
 });
 saveHistory();
};
const alignRight = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const maxX = Math.max(...selected.map(n => n.x));
 selected.forEach(n => {
 n.x = maxX;
 });
 saveHistory();
};
const alignTop = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const minY = Math.min(...selected.map(n => n.y));
 selected.forEach(n => {
 n.y = minY;
 });
 saveHistory();
};
const alignMiddle = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const minY = Math.min(...selected.map(n => n.y));
 const maxY = Math.max(...selected.map(n => n.y));
 const centerY = (minY + maxY) / 2;
 selected.forEach(n => {
 n.y = centerY - 40;
 });
 saveHistory();
};
const alignBottom = () => {
 const selected = nodes.value.filter(n => n.selected);
 if (selected.length < 2)
 return;
 const maxY = Math.max(...selected.map(n => n.y));
 selected.forEach(n => {
 n.y = maxY;
 });
 saveHistory();
};
const autoLayout = () => {
 if (nodes.value.length === 0)
 return;
 const sortedNodes = [...nodes.value].sort((a, b) => {
 const typeOrder = { 'bpmn-start': 0, 'bpmn-end': 100, 'bpmn-gateway-exclusive': 30, 'bpmn-gateway-parallel': 31, 'bpmn-gateway-inclusive': 32 };
 return (typeOrder[a.type] || 50) - (typeOrder[b.type] || 50);
 });
 const nodeWidth = 120;
 const nodeHeight = 80;
 const hGap = 180;
 const vGap = 140;
 const startX = 120;
 const startY = 100;
 const maxNodesPerRow = 5;
 const rows = Math.ceil(sortedNodes.length / maxNodesPerRow);
 sortedNodes.forEach((node, index) => {
 const col = index % maxNodesPerRow;
 const row = Math.floor(index / maxNodesPerRow);
 node.x = startX + col * hGap;
 node.y = startY + row * vGap;
 });
 const existingConns = [...connections.value];
 connections.value = [];
 for (let i = 0; i < sortedNodes.length - 1; i++) {
 const existing = existingConns.find(c => c.from === sortedNodes[i].id && c.to === sortedNodes[i + 1].id);
 if (existing) {
 connections.value.push(existing);
 } else {
 connections.value.push({
 from: sortedNodes[i].id,
 to: sortedNodes[i + 1].id,
 label: '',
 condition: '',
 priority: 1
 });
 }
 }
 existingConns.forEach(conn => {
 if (!connections.value.find(c => c.from === conn.from && c.to === conn.to)) {
 connections.value.push(conn);
 }
 });
 saveHistory();
 ElMessage.success('流程图已自动规整（企业级标准化布局）');
};
const validateFlow = () => {
 const errors = [];
 const startNodes = nodes.value.filter(n => n.type === 'bpmn-start');
 const endNodes = nodes.value.filter(n => n.type === 'bpmn-end');
 const gateways = nodes.value.filter(n => n.type.includes('gateway'));
 const tasks = nodes.value.filter(n => n.type === 'bpmn-task' || n.type === 'bpmn-approval');
 if (startNodes.length !== 1) {
 errors.push({
 type: 'BPMN语法',
 message: `开始节点数量应为1个，当前${startNodes.length}个`,
 location: '画布全局',
 suggestion: '确保只有一个开始节点'
 });
 }
 if (endNodes.length !== 1) {
 errors.push({
 type: 'BPMN语法',
 message: `结束节点数量应为1个，当前${endNodes.length}个`,
 location: '画布全局',
 suggestion: '确保只有一个结束节点'
 });
 }
 gateways.forEach(gateway => {
 const incoming = connections.value.filter(c => c.to === gateway.id).length;
 const outgoing = connections.value.filter(c => c.from === gateway.id).length;
 if (incoming !== 1) {
 errors.push({
 type: 'BPMN语法',
 message: `网关${gateway.label}入度错误: 应为1，实际${incoming}`,
 location: `节点: ${gateway.label}`,
 suggestion: '网关只能有一个入连线'
 });
 }
 if (gateway.type === 'bpmn-gateway-exclusive' && outgoing < 2) {
 errors.push({
 type: 'BPMN语法',
 message: `排他网关${gateway.label}出度错误: 至少2个，实际${outgoing}`,
 location: `节点: ${gateway.label}`,
 suggestion: '排他网关需要至少2条出连线'
 });
 }
 });
 nodes.value.forEach(node => {
 const incoming = connections.value.filter(c => c.to === node.id).length;
 const outgoing = connections.value.filter(c => c.from === node.id).length;
 if (node.type === 'bpmn-start' && incoming > 0) {
 errors.push({
 type: 'BPMN语法',
 message: `开始节点不应有入连线`,
 location: `节点: ${node.label}`,
 suggestion: '移除开始节点的入连线'
 });
 }
 if (node.type === 'bpmn-end' && outgoing > 0) {
 errors.push({
 type: 'BPMN语法',
 message: `结束节点不应有出连线`,
 location: `节点: ${node.label}`,
 suggestion: '移除结束节点的出连线'
 });
 }
 });
 tasks.forEach(task => {
 if (!task.assignee) {
 errors.push({
 type: '业务完整性',
 message: `任务节点${task.label}未配置负责人（权责缺失）`,
 location: `节点: ${task.label}`,
 suggestion: '为任务节点配置审批人/岗位/角色'
 });
 }
 if (!task.what) {
 errors.push({
 type: '业务完整性',
 message: `任务节点${task.label}七何要素缺失：缺少"何事"`,
 location: `节点: ${task.label}`,
 suggestion: '完善七何要素配置'
 });
 }
 if (!task.who) {
 errors.push({
 type: '业务完整性',
 message: `任务节点${task.label}七何要素缺失：缺少"何人"`,
 location: `节点: ${task.label}`,
 suggestion: '完善七何要素配置'
 });
 }
 if (!task.rule) {
 errors.push({
 type: '业务完整性',
 message: `任务节点${task.label}缺少合规依据`,
 location: `节点: ${task.label}`,
 suggestion: '绑定相关制度条款或合规标准'
 });
 }
 });
 connections.value.forEach((conn, index) => {
 const fromNode = nodes.value.find(n => n.id === conn.from);
 const toNode = nodes.value.find(n => n.id === conn.to);
 if (!fromNode) {
 errors.push({
 type: 'BPMN语法',
 message: `连线${index + 1}源节点不存在`,
 location: `连线#${index + 1}`,
 suggestion: '检查连线配置'
 });
 }
 if (!toNode) {
 errors.push({
 type: 'BPMN语法',
 message: `连线${index + 1}目标节点不存在`,
 location: `连线#${index + 1}`,
 suggestion: '检查连线配置'
 });
 }
 });
 const hasGatewayWithConditional = connections.value.some(c => c.condition && c.condition.trim());
 const gatewayWithMultipleOut = gateways.some(g => connections.value.filter(c => c.from === g.id).length > 1);
 if (gatewayWithMultipleOut && !hasGatewayWithConditional) {
 errors.push({
 type: 'BPMN语法',
 message: '存在多路分支网关但未配置条件表达式',
 location: '画布全局',
 suggestion: '为网关分支连线配置条件表达式'
 });
 }
 validateResult.value = {
 success: errors.length === 0,
 errors,
 nodeCount: nodes.value.length,
 edgeCount: connections.value.length,
 gatewayCount: gateways.length,
 taskCount: tasks.length
 };
 validateModalVisible.value = true;
};
const saveHistory = () => {
 const state = {
 nodes: JSON.parse(JSON.stringify(nodes.value)),
 connections: JSON.parse(JSON.stringify(connections.value))
 };
 history.value = history.value.slice(0, historyIndex.value + 1);
 history.value.push(state);
 historyIndex.value = history.value.length - 1;
 if (history.value.length > 50) {
 history.value.shift();
 historyIndex.value--;
 }
};
const undo = () => {
 if (historyIndex.value > 0) {
 historyIndex.value--;
 const state = history.value[historyIndex.value];
 nodes.value = state.nodes;
 connections.value = state.connections;
 selectedNode.value = null;
 }
};
const redo = () => {
 if (historyIndex.value < history.value.length - 1) {
 historyIndex.value++;
 const state = history.value[historyIndex.value];
 nodes.value = state.nodes;
 connections.value = state.connections;
 selectedNode.value = null;
 }
};
const saveDraft = async () => {
 try {
 const saved = await persistDefinition();
 const timestamp = new Date().toISOString();
 const flowData = {
 name: flowName.value,
 code: flowCode.value,
 level: flowLevel.value,
 domain: flowDomain.value,
 version: currentVersion.value,
 status: 'draft',
 nodes: nodes.value,
 connections: connections.value,
 savedAt: timestamp,
 savedBy: '当前用户'
 };
 addOperationLog('保存草稿', `流程 ${flowName.value} 已保存为草稿`);
 versionHistory.value.unshift({
 version: currentVersion.value,
 status: 'draft',
 savedAt: timestamp,
 savedBy: '当前用户',
 data: JSON.parse(JSON.stringify(flowData))
 });
 flowStatus.value = backendStatusToUi(saved && saved.status ? saved.status : 'draft');
 ElMessage.success('流程草稿已保存');
 return saved;
 } catch (e) {
 const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message;
 ElMessage.error('草稿保存失败：' + msg);
 throw e;
 }
};
const submitForReview = () => {
 validateFlow();
 setTimeout(() => {
 if (validateResult.value?.success) {
 ElMessageBox.confirm('确定提交审核吗？流程将进入待审核状态。', '提交审核', {
 confirmButtonText: '确定提交',
 cancelButtonText: '取消',
 type: 'info'
 }).then(async () => {
 try {
 if (!currentDefId.value) {
 await saveDraft();
 }
 const res = await axios.post(`/api/process/def/${currentDefId.value}/submit`);
 const d = res.data && res.data.data ? res.data.data : {};
 flowStatus.value = backendStatusToUi(d.status || 'pending');
 addOperationLog('提交审核', `流程 ${flowName.value} 提交审核`);
 addVersionHistory();
 ElMessage.success('流程已提交审核');
 } catch (e) {
 const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message;
 ElMessage.error('提交审核失败：' + msg);
 }
 }).catch(() => {
 ElMessage.info('已取消提交');
 });
 } else {
 ElMessage.error('校验未通过，请先修复所有问题');
 }
 }, 500);
};
const publishFlow = () => {
 validateFlow();
 setTimeout(() => {
 if (!validateResult.value?.success) {
 ElMessage.error('校验未通过，无法发布流程');
 return;
 }
 const hasUnboundTasks = nodes.value.some(n => 
 (n.type === 'bpmn-task' || n.type === 'bpmn-approval') && 
 (!n.policy || !n.form)
 );
 if (hasUnboundTasks) {
 ElMessage.error('存在未绑定制度/表单的任务节点，请先完善资产绑定');
 return;
 }
 showPublishDialog.value = true;
 }, 500);
};
const confirmPublish = async () => {
 try {
 if (!currentDefId.value) {
 await saveDraft();
 }
 // 后端流转：评审通过(approved) -> 发布生效(active)
 await axios.post(`/api/process/def/${currentDefId.value}/approve`, { comment: '设计器发布前评审通过' });
 const pubRes = await axios.post(`/api/process/def/${currentDefId.value}/publish`);
 const d = pubRes.data && pubRes.data.data ? pubRes.data.data : {};
 const oldVersion = versionHistory.value.find(v => v.status === 'effective');
 if (oldVersion) {
 oldVersion.status = 'archived';
 oldVersion.archivedAt = new Date().toISOString();
 addOperationLog('版本归档', `旧版本 ${oldVersion.version} 已归档`);
 }
 flowStatus.value = backendStatusToUi(d.status || 'active');
 addOperationLog('流程发布', `流程 ${flowName.value} v${currentVersion.value} 已${publishForm.publishType === 'effective' ? '生效' : '发布'}`);
 addVersionHistory();
 if (publishForm.notify) {
 addOperationLog('发布公示', '流程已自动公示到流程台账');
 }
 if (publishForm.createTraining) {
 addOperationLog('培训任务', '已自动推送培训任务');
 }
 showPublishDialog.value = false;
 ElMessage.success('流程发布成功');
 } catch (e) {
 const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message;
 ElMessage.error('发布失败：' + msg);
 }
};

// 流程示例数据 - 采购申请审批流程
const sampleNodeDetails = ref([
  { type: '开始事件', typeTag: 'info', id: 'StartEvent_1', name: '开始', description: '业务需求产生、库存不足、生产计划需求触发采购申请', post: '-', output: '-', timeLimit: '瞬时' },
  { type: '人工任务', typeTag: '', id: 'Task_Apply', name: '需求部门发起采购申请', description: '填写《采购申请单》，明确物料名称、规格、数量、用途、需求日期、预算金额，附技术图纸、物料标准', post: '需求部门申购人', output: '《采购申请单》、物料技术附件', timeLimit: '按需' },
  { type: '人工任务', typeTag: '', id: 'Task_DeptCheck', name: '部门负责人初审', description: '审核需求真实性、必要性、合理性，核对是否贴合生产计划、部门工作需求', post: '部门负责人', output: '初审通过/驳回结论', timeLimit: '24h' },
  { type: '人工任务', typeTag: '', id: 'Task_PurchaseCheck', name: '采购部专员复核', description: '复核物料规格是否匹配电子生产标准，核对物料分类、历史采购价格、供应商适配性', post: '采购部专员', output: '采购复核意见', timeLimit: '24h' },
  { type: '排他网关', typeTag: 'warning', id: 'Gateway_AmountCheck', name: '金额/物料类型判定', description: '系统自动判定：≤5000元常规物料走简易审批；＞5000元/核心物料走多级审批', post: '系统自动', output: '流程分支判定结果', timeLimit: '瞬时' },
  { type: '人工任务', typeTag: '', id: 'Task_PurchaseMgrCheck', name: '采购部经理终审（小额）', description: '终审小额常规采购需求，复核采购可行性、交付周期、成本合理性', post: '采购部经理', output: '终审审批结论', timeLimit: '48h' },
  { type: '人工任务', typeTag: '', id: 'Task_FinanceCheck', name: '财务部预算审核（大额）', description: '核对年度/月度预算额度，核查费用科目归类准确性', post: '财务部', output: '预算审核结论', timeLimit: '48h' },
  { type: '人工任务', typeTag: '', id: 'Task_BossCheck', name: '总经理终审（大额）', description: '对大额物资、专项设备采购进行最终风险与成本把控', post: '总经理', output: '最终审批结论', timeLimit: '48h' },
  { type: '排他网关', typeTag: 'warning', id: 'Gateway_EndMerge', name: '流程合并', description: '合并小额与大额分支路径', post: '系统自动', output: '-', timeLimit: '瞬时' },
  { type: '结束事件', typeTag: 'success', id: 'EndEvent_Success', name: '审批通过-采购立项办结', description: '审批全部通过，系统自动归档，更新采购台账，流转至采购下单环节', post: '系统自动', output: '完整流程归档台账', timeLimit: '瞬时' },
  { type: '排他网关', typeTag: 'danger', id: 'Gateway_Reject', name: '审批驳回判定', description: '判定驳回后的处理路径', post: '系统自动', output: '-', timeLimit: '瞬时' },
  { type: '结束事件', typeTag: 'danger', id: 'EndEvent_Reject', name: '驳回终止-修订重提', description: '驳回至申请人修订后重新发起流程', post: '申请人', output: '驳回整改记录', timeLimit: '24h' }
]);

const sampleBpmnXml = ref(`<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_01" targetNamespace="http://bpmn.io/bpmn" exporter="AntV X6/XFlow" exporterVersion="2.0">
  <bpmn:process id="Process_PurchaseApply" name="采购申请审批流程" isExecutable="true">
    <!-- 开始事件 -->
    <bpmn:startEvent id="StartEvent_1" name="开始"></bpmn:startEvent>
    
    <!-- 人工任务节点 -->
    <bpmn:userTask id="Task_Apply" name="需求部门发起采购申请"></bpmn:userTask>
    <bpmn:userTask id="Task_DeptCheck" name="部门负责人初审"></bpmn:userTask>
    <bpmn:userTask id="Task_PurchaseCheck" name="采购部专员复核"></bpmn:userTask>
    
    <!-- 分级审批排他网关 -->
    <bpmn:exclusiveGateway id="Gateway_AmountCheck" name="金额/物料类型判定"></bpmn:exclusiveGateway>
    
    <!-- 小额流程分支 -->
    <bpmn:userTask id="Task_PurchaseMgrCheck" name="采购部经理终审"></bpmn:userTask>
    
    <!-- 大额流程分支 -->
    <bpmn:userTask id="Task_FinanceCheck" name="财务部预算审核"></bpmn:userTask>
    <bpmn:userTask id="Task_BossCheck" name="总经理终审"></bpmn:userTask>
    
    <!-- 合并网关 -->
    <bpmn:exclusiveGateway id="Gateway_EndMerge" name="流程合并"></bpmn:exclusiveGateway>
    
    <!-- 结束事件 -->
    <bpmn:endEvent id="EndEvent_Success" name="审批通过-采购立项办结"></bpmn:endEvent>
    
    <!-- 驳回分支节点 -->
    <bpmn:exclusiveGateway id="Gateway_Reject" name="审批驳回判定"></bpmn:exclusiveGateway>
    <bpmn:endEvent id="EndEvent_Reject" name="驳回终止-修订重提"></bpmn:endEvent>
    
    <!-- 主线流转连线 -->
    <bpmn:sequenceFlow id="Flow_Start2Apply" sourceRef="StartEvent_1" targetRef="Task_Apply"/>
    <bpmn:sequenceFlow id="Flow_Apply2Dept" sourceRef="Task_Apply" targetRef="Task_DeptCheck"/>
    <bpmn:sequenceFlow id="Flow_Dept2Purchase" sourceRef="Task_DeptCheck" targetRef="Task_PurchaseCheck"/>
    <bpmn:sequenceFlow id="Flow_Purchase2Gateway" sourceRef="Task_PurchaseCheck" targetRef="Gateway_AmountCheck"/>
    
    <!-- 小额分支连线 -->
    <bpmn:sequenceFlow id="Flow_Gateway2Small" name="≤5000元常规物料" sourceRef="Gateway_AmountCheck" targetRef="Task_PurchaseMgrCheck"/>
    <bpmn:sequenceFlow id="Flow_Small2Merge" sourceRef="Task_PurchaseMgrCheck" targetRef="Gateway_EndMerge"/>
    
    <!-- 大额分支连线 -->
    <bpmn:sequenceFlow id="Flow_Gateway2Big" name="＞5000元/核心物料/设备" sourceRef="Gateway_AmountCheck" targetRef="Task_FinanceCheck"/>
    <bpmn:sequenceFlow id="Flow_Finance2Boss" sourceRef="Task_FinanceCheck" targetRef="Task_BossCheck"/>
    <bpmn:sequenceFlow id="Flow_Boss2Merge" sourceRef="Task_BossCheck" targetRef="Gateway_EndMerge"/>
    
    <!-- 办结流转 -->
    <bpmn:sequenceFlow id="Flow_Merge2Success" sourceRef="Gateway_EndMerge" targetRef="EndEvent_Success"/>
    
    <!-- 驳回通用分支 -->
    <bpmn:sequenceFlow id="Flow_All2Reject" name="任意节点驳回" sourceRef="Gateway_EndMerge" targetRef="Gateway_Reject"/>
    <bpmn:sequenceFlow id="Flow_Reject2End" sourceRef="Gateway_Reject" targetRef="EndEvent_Reject"/>
  </bpmn:process>
</bpmn:definitions>`);

const copyBpmnXml = () => {
  navigator.clipboard.writeText(sampleBpmnXml.value).then(() => {
    ElMessage.success('BPMN源码已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};

const addVersionHistory = () => {
 const timestamp = new Date().toISOString();
 versionHistory.value.unshift({
 version: currentVersion.value,
 status: flowStatus.value,
 savedAt: timestamp,
 savedBy: '当前用户',
 data: JSON.parse(JSON.stringify({
 nodes: nodes.value,
 connections: connections.value,
 flowName: flowName.value,
 flowCode: flowCode.value
 }))
 });
};
const addOperationLog = (action, description) => {
 operationLogs.value.unshift({
 action,
 description,
 operator: '当前用户',
 timestamp: new Date().toLocaleString()
 });
};
const compareWithVersion = (version) => {
 compareVersion.value = version;
 showVersionCompare.value = true;
};
const getChangedNodes = computed(() => {
 if (!compareVersion.value || !compareVersion.value.data) return [];
 const current = nodes.value.map(n => `${n.id}-${n.label}-${n.type}`);
 const old = compareVersion.value.data.nodes.map(n => `${n.id}-${n.label}-${n.type}`);
 return current.filter(id => !old.includes(id)).concat(old.filter(id => !current.includes(id)));
});
const getChangedConnections = computed(() => {
 if (!compareVersion.value || !compareVersion.value.data) return [];
 const current = connections.value.map(c => `${c.from}-${c.to}`);
 const old = compareVersion.value.data.connections.map(c => `${c.from}-${c.to}`);
 return current.filter(conn => !old.includes(conn)).concat(old.filter(conn => !current.includes(conn)));
});
const createNewVersion = async (type) => {
 try {
 if (currentDefId.value) {
 const res = await axios.post(`/api/process/def/${currentDefId.value}/new-version`);
 const d = res.data && res.data.data ? res.data.data : {};
 if (d.id) {
 currentDefId.value = d.id;
 currentVersion.value = backendVersionToUi(d.version || currentVersion.value);
 flowStatus.value = backendStatusToUi(d.status || 'draft');
 addOperationLog('版本升级', `已创建新版本 ${currentVersion.value}`);
 ElMessage.success(`已创建新版本 ${currentVersion.value}`);
 return;
 }
 }
 // 未持久化的流程仅做本地版本号升级
 const [major, minor, patch] = currentVersion.value.split('.').map(Number);
 if (type === 'major') {
 currentVersion.value = `${major + 1}.0.0`;
 } else if (type === 'minor') {
 currentVersion.value = `${major}.${minor + 1}.0`;
 } else {
 currentVersion.value = `${major}.${minor}.${patch + 1}`;
 }
 flowStatus.value = 'draft';
 addOperationLog('版本升级', `创建新版本 ${currentVersion.value}`);
 ElMessage.success(`已创建新版本 ${currentVersion.value}`);
 } catch (e) {
 const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message;
 ElMessage.error('创建新版本失败：' + msg);
 }
};
const exportBPMN = () => {
 const bpmnData = {
 name: flowName.value,
 code: flowCode.value,
 version: currentVersion.value,
 status: flowStatus.value,
 nodes: nodes.value,
 connections: connections.value,
 exportTime: new Date().toISOString(),
 exportedBy: '当前用户'
 };
 const blob = new Blob([JSON.stringify(bpmnData, null, 2)], { type: 'application/json' });
 const url = URL.createObjectURL(blob);
 const a = document.createElement('a');
 a.href = url;
 a.download = `${flowCode.value}_v${currentVersion.value}.bpmn`;
 a.click();
 URL.revokeObjectURL(url);
 addOperationLog('导出BPMN', `导出流程文件 ${flowCode.value}_v${currentVersion.value}.bpmn`);
 ElMessage.success('BPMN源文件已导出');
};
const exportPDF = () => {
 addOperationLog('导出PDF', '导出作业手册PDF');
 const docContent = generateWorkManualContent();
 const blob = new Blob([docContent], { type: 'application/msword' });
 const url = URL.createObjectURL(blob);
 const a = document.createElement('a');
 a.href = url;
 a.download = `${flowCode.value}_作业手册_v${currentVersion.value}.docx`;
 a.click();
 URL.revokeObjectURL(url);
 ElMessage.success('作业手册已导出');
};
const generateSVGContent = () => {
 let svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1920" height="1080" viewBox="0 0 1920 1080">`;
 svg += `<rect width="100%" height="100%" fill="white"/>`;
 svg += `<text x="20" y="30" font-size="20" font-weight="bold">${flowName.value} (${flowCode.value})</text>`;
 svg += `<text x="20" y="55" font-size="14">版本: ${currentVersion.value} | 状态: ${flowStatusOptions.find(s => s.value === flowStatus.value)?.label || flowStatus.value}</text>`;
 nodes.value.forEach(node => {
 const colors = {
 'bpmn-start': '#67c23a',
 'bpmn-end': '#f56c6c',
 'bpmn-task': '#409eff',
 'bpmn-approval': '#e6a23c'
 };
 const color = colors[node.type] || '#909399';
 svg += `<rect x="${node.x}" y="${node.y}" width="120" height="80" rx="8" fill="${color}" stroke="#333" stroke-width="2"/>`;
 svg += `<text x="${node.x + 60}" y="${node.y + 45}" text-anchor="middle" fill="white" font-size="12">${node.label}</text>`;
 });
 connections.value.forEach(conn => {
 const fromNode = nodes.value.find(n => n.id === conn.from);
 const toNode = nodes.value.find(n => n.id === conn.to);
 if (fromNode && toNode) {
 const x1 = fromNode.x + 60;
 const y1 = fromNode.y + 80;
 const x2 = toNode.x + 60;
 const y2 = toNode.y;
 svg += `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="#666" stroke-width="2" marker-end="url(#arrow)"/>`;
 }
 });
 svg += `<defs><marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto"><path d="M0,0 L0,6 L9,3 z" fill="#666"/></marker></defs>`;
 svg += `</svg>`;
 return svg;
};
const generateWorkManualContent = () => {
 let content = `${flowName.value} 作业手册\n`;
 content += `流程编码: ${flowCode.value}\n`;
 content += `版本: ${currentVersion.value}\n`;
 content += `状态: ${flowStatusOptions.find(s => s.value === flowStatus.value)?.label || flowStatus.value}\n`;
 content += `生成时间: ${new Date().toLocaleString()}\n\n`;
 content += `一、流程概述\n`;
 const levelNames = { level1: '一级', level2: '二级', level3: '三级', level4: '四级', level5: '五级' };
 content += `${flowName.value}适用于${flowDomain.value}流程域的${levelNames[flowLevel.value] || flowLevel.value}流程。\n\n`;
 content += `二、流程节点\n`;
 nodes.value.forEach((node, index) => {
 content += `${index + 1}. ${node.label} (${getNodeTypeName(node.type)})\n`;
 if (node.assignee) content += `   负责人: ${node.assignee}\n`;
 if (node.timeLimit) content += `   处理时限: ${node.timeLimit}小时\n`;
 if (node.policy) content += `   制度依据: ${node.policy}\n`;
 if (node.form) content += `   配套表单: ${node.form}\n`;
 content += `\n`;
 });
 content += `三、流程步骤说明\n`;
 let step = 1;
 nodes.value.filter(n => n.type === 'bpmn-task' || n.type === 'bpmn-approval').forEach(node => {
 content += `第${step}步: ${node.label}\n`;
 content += `   何事: ${node.what || '待补充'}\n`;
 content += `   何人: ${node.who || node.assignee || '待指定'}\n`;
 content += `   何规: ${node.rule || '待补充'}\n\n`;
 step++;
 });
 return content;
};

// 模板相关方法
const handleTemplateCommand = (command) => {
 if (command === 'save') {
 if (nodes.value.length === 0) {
 ElMessage.warning('请先绘制流程图');
 return;
 }
 templateForm.name = flowName.value;
 templateForm.category = '';
 templateForm.scenario = '';
 templateForm.description = '';
 showSaveTemplateDialog.value = true;
 } else if (command === 'load') {
 showLoadTemplateDialog.value = true;
 } else if (command === 'manage') {
 ElMessage.info('模板管理功能开发中');
 }
};

const confirmSaveTemplate = () => {
 if (!templateForm.name) {
 ElMessage.warning('请输入模板名称');
 return;
 }
 const template = {
 id: Date.now(),
 name: templateForm.name,
 category: templateForm.category,
 scenario: templateForm.scenario,
 description: templateForm.description,
 nodeCount: nodes.value.length,
 creator: '当前用户',
 createTime: new Date().toISOString().split('T')[0],
 data: {
 nodes: templateForm.includeStyles ? JSON.parse(JSON.stringify(nodes.value)) : nodes.value.map(n => ({ id: n.id, type: n.type, label: n.label })),
 connections: JSON.parse(JSON.stringify(connections.value))
 }
 };
 templateList.value.unshift(template);
 showSaveTemplateDialog.value = false;
 ElMessage.success('模板已保存');
 addOperationLog('保存模板', `保存了模板：${templateForm.name}`);
};

const loadTemplate = (template) => {
 if (!template.data) {
 ElMessage.error('模板数据无效');
 return;
 }
 nodes.value = JSON.parse(JSON.stringify(template.data.nodes));
 connections.value = JSON.parse(JSON.stringify(template.data.connections));
 showLoadTemplateDialog.value = false;
 saveHistory();
 ElMessage.success(`已加载模板：${template.name}`);
 addOperationLog('加载模板', `加载了模板：${template.name}`);
};

const previewTemplate = (template) => {
 ElMessage.info(`预览模板：${template.name}`);
};

const getCategoryLabel = (category) => {
 const labels = {
 admin: '行政管理类',
 hr: '人事管理类',
 finance: '财务管理类',
 production: '生产质量类',
 equipment: '设备管理类',
 ehs: 'EHS安全类',
 supply: '供应链类',
 rd: '研发管理类'
 };
 return labels[category] || category;
};

// 样式替换相关方法
const handleStyleCommand = (command) => {
 if (selectedNodes.value.length === 0 && selectedNodesForStyle.value.length === 0) {
 ElMessage.warning('请先选择要修改样式的节点（Ctrl+点击多选）');
 return;
 }
 selectedNodesForStyle.value = selectedNodes.value.length > 0 ? [...selectedNodes.value] : selectedNodesForStyle.value;
 styleReplaceForm.shape = 'rounded';
 styleReplaceForm.borderColor = '#409eff';
 styleReplaceForm.bgColor = '#ffffff';
 styleReplaceForm.fontSize = 12;
 styleReplaceForm.borderWidth = 2;
 styleReplaceForm.shadow = true;
 showStyleReplaceDialog.value = true;
};

const getNodeLabel = (nodeId) => {
 const node = nodes.value.find(n => n.id === nodeId);
 return node ? node.label : `节点${nodeId}`;
};

const confirmStyleReplace = () => {
 selectedNodesForStyle.value.forEach(nodeId => {
 const node = nodes.value.find(n => n.id === nodeId);
 if (node) {
 node.style = {
 shape: styleReplaceForm.shape,
 borderColor: styleReplaceForm.borderColor || styleReplaceForm.borderColorPreset,
 bgColor: styleReplaceForm.bgColor,
 fontSize: styleReplaceForm.fontSize,
 borderWidth: styleReplaceForm.borderWidth,
 shadow: styleReplaceForm.shadow
 };
 }
 });
 showStyleReplaceDialog.value = false;
 saveHistory();
 ElMessage.success(`已更新 ${selectedNodesForStyle.value.length} 个节点的样式`);
 addOperationLog('批量样式替换', `替换了 ${selectedNodesForStyle.value.length} 个节点的样式`);
};

const resetStyleReplace = () => {
 styleReplaceForm.shape = 'rounded';
 styleReplaceForm.borderColor = '#409eff';
 styleReplaceForm.bgColor = '#ffffff';
 styleReplaceForm.fontSize = 12;
 styleReplaceForm.borderWidth = 2;
 styleReplaceForm.shadow = true;
};

// 制度关联相关方法
const bindPolicy = (policy) => {
 if (selectedNode.value) {
 selectedNode.value.policy = policy.code;
 selectedNode.value.policyName = policy.name;
 showPolicyBindDialog.value = false;
 ElMessage.success(`已绑定制度：${policy.name}`);
 addOperationLog('绑定制度', `节点 ${selectedNode.value.label} 绑定了制度 ${policy.code}`);
 }
};

const createAndBindPolicy = () => {
 if (!newPolicyForm.name) {
 ElMessage.warning('请输入制度名称');
 return;
 }
 const newCode = `ZD-${Date.now().toString(36).toUpperCase()}`;
 ElMessage.success(`制度 ${newPolicyForm.name} 已创建并绑定`);
 showPolicyBindDialog.value = false;
};

// 版本同步相关方法
const saveVersionSync = () => {
 if (!versionSyncForm.policyCode) {
 ElMessage.warning('请选择关联制度');
 return;
 }
 showVersionSyncDialog.value = false;
 ElMessage.success('版本同步设置已保存');
 addOperationLog('版本同步设置', `关联制度：${versionSyncForm.policyCode}，模式：${versionSyncForm.mode}`);
};

const toggleLayers = () => {
 ElMessage.info('图层管理功能开发中');
};
const saveFlowProperties = () => {
 flowName.value = flowProperties.flowName || flowName.value;
 flowCode.value = flowProperties.flowCode || generateFlowCode();
 flowLevel.value = flowProperties.flowLevel;
 flowDomain.value = flowProperties.flowDomain;
 addOperationLog('保存流程属性', `流程属性已更新`);
 showFlowProperties.value = false;
 ElMessage.success('流程属性配置已保存');
};
const generateFlowCode = () => {
 const domainCode = flowDomain.value.replace('FD', '');
 const levelCode = flowLevel.value.replace('level', '');
 return `LY-${domainCode}-${levelCode}-${Date.now().toString(36).toUpperCase()}`;
};
const loadVersion = (version) => {
 if (version.data) {
 nodes.value = JSON.parse(JSON.stringify(version.data.nodes || []));
 connections.value = JSON.parse(JSON.stringify(version.data.connections || []));
 if (version.data.flowName) flowName.value = version.data.flowName;
 if (version.data.flowCode) flowCode.value = version.data.flowCode;
 }
 flowStatus.value = version.status;
 currentVersion.value = version.version;
 addOperationLog('加载版本', `加载了版本 ${version.version}`);
 showVersionHistory.value = false;
 ElMessage.success(`已加载版本 ${version.version}`);
 saveHistory();
};

// ========== AntV X6 Graph 初始化（对标杰成EPROS画布引擎）==========
const initX6Graph = () => {
 // 等待 DOM 渲染完成
 nextTick(() => {
 const container = document.getElementById(graphContainerId);
 if (!container) {
 console.error('X6 画布容器未找到');
 return;
 }

 // 创建 X6 Graph 实例
 graph = new Graph({
 container: container,
 width: container.offsetWidth || 1200,
 height: container.offsetHeight || 800,
 // 画布背景与网格
 background: { color: '#ffffff' },
 grid: {
 size: 20,
 visible: true,
 type: 'mesh',
 args: {
 color: '#e0e0e0',
 thickness: 1
 }
 },
 // 鼠标滚轮缩放
 mousewheel: {
 enabled: true,
 modifiers: ['ctrl', 'meta'],
 factor: 1.1,
 maxScale: 2,
 minScale: 0.3
 },
 // 连线基础配置
 defaultEdge: {
 attrs: {
 line: {
 stroke: '#67c23a',
 strokeWidth: 2,
 targetMarker: {
 name: 'classic',
 size: 8
 }
 }
 },
 connector: 'routing',
 router: {
 name: 'manhattan',
 args: {
 padding: 20
 }
 }
 },
 // 节点基础配置
 defaultNode: {
 attrs: {
 body: {
 fill: '#ffffff',
 stroke: '#409eff',
 strokeWidth: 2,
 rx: 8,
 ry: 8
 },
 label: {
 fill: '#303133',
 fontSize: 12
 }
 }
 },
 // 锚点配置
 ports: {
 groups: {
 in: {
 attrs: {
 circle: {
 r: 5,
 magnet: true,
 stroke: '#409eff',
 fill: '#ffffff',
 strokeWidth: 2
 }
 },
 position: 'top'
 },
 out: {
 attrs: {
 circle: {
 r: 5,
 magnet: true,
 stroke: '#67c23a',
 fill: '#ffffff',
 strokeWidth: 2
 }
 },
 position: 'bottom'
 }
 }
 },
 // 历史记录
 history: {
 enabled: true
 },
 // 对齐线
 snapline: {
 enabled: true,
 decimals: 1
 },
 // 多选
 selecting: {
 enabled: true,
 multiple: true,
 showNodeSelectionBox: true,
 rubberEdge: true,
 rubberNode: true,
 modifiers: ['shift']
 },
 // 连接线
 connecting: {
 anchor: 'center',
 connectionPoint: 'anchor',
 allowBlank: false,
 allowLoop: false,
 highlight: true,
 snap: {
 radius: 20
 },
 createEdge() {
 return new Graph.Edge({
 attrs: {
 line: {
 stroke: '#67c23a',
 strokeWidth: 2,
 targetMarker: {
 name: 'classic',
 size: 8
 }
 }
 }
 });
 },
 validateEdge({ edge }) {
 // 禁止开始节点作为目标
 const sourceNode = edge.getSourceNode();
 const targetNode = edge.getTargetNode();
 if (targetNode && targetNode.isStart()) {
 ElMessage.warning('开始节点不能作为连线目标');
 return false;
 }
 // 禁止结束节点作为源
 if (sourceNode && sourceNode.isEnd()) {
 ElMessage.warning('结束节点不能作为连线起点');
 return false;
 }
 return true;
 }
 },
 // 节点移动
 moving: {
 enabled: true,
 deltaX: 10,
 deltaY: 10
 },
 // 键盘快捷键
 keyboard: {
 enabled: true,
 delete: true,
 copy: true,
 paste: true
 },
 // DND 拖拽
 dnd: {
 enabled: true
 }
 });

 // 注册 BPMN 形状（开始/结束事件）
 Graph.registerNode('bpmn-start', {
 inherit: 'circle',
 attrs: {
 body: {
 fill: '#e8f5e9',
 stroke: '#67c23a',
 strokeWidth: 3,
 r: 25
 },
 text: {
 text: '开始',
 fill: '#276678',
 fontSize: 12,
 fontWeight: 'bold'
 }
 },
 portGroups: ['out']
 }, true);

 Graph.registerNode('bpmn-end', {
 inherit: 'circle',
 attrs: {
 body: {
 fill: '#ffebee',
 stroke: '#f56c6c',
 strokeWidth: 3,
 r: 25
 },
 text: {
 text: '结束',
 fill: '#c62828',
 fontSize: 12,
 fontWeight: 'bold'
 }
 },
 portGroups: ['in']
 }, true);

 // 注册任务节点
 Graph.registerNode('bpmn-task', {
 inherit: 'rect',
 width: 120,
 height: 60,
 attrs: {
 body: {
 fill: '#e3f2fd',
 stroke: '#409eff',
 strokeWidth: 2,
 rx: 8,
 ry: 8
 },
 text: {
 text: '任务节点',
 fill: '#1565c0',
 fontSize: 12
 }
 },
 portGroups: ['in', 'out']
 }, true);

 // 注册审批节点
 Graph.registerNode('bpmn-approval', {
 inherit: 'rect',
 width: 120,
 height: 60,
 attrs: {
 body: {
 fill: '#e8f5e9',
 stroke: '#2e7d32',
 strokeWidth: 2,
 rx: 8,
 ry: 8
 },
 text: {
 text: '审批节点',
 fill: '#2e7d32',
 fontSize: 12
 }
 },
 portGroups: ['in', 'out']
 }, true);

 // 注册排他网关
 Graph.registerNode('bpmn-gateway-exclusive', {
 inherit: 'diamond',
 width: 60,
 height: 60,
 attrs: {
 body: {
 fill: '#ffebee',
 stroke: '#f56c6c',
 strokeWidth: 3
 },
 text: {
 text: 'X',
 fill: '#c62828',
 fontSize: 16,
 fontWeight: 'bold'
 }
 }
 }, true);

 // 注册并行网关
 Graph.registerNode('bpmn-gateway-parallel', {
 inherit: 'diamond',
 width: 60,
 height: 60,
 attrs: {
 body: {
 fill: '#e3f2fd',
 stroke: '#409eff',
 strokeWidth: 3
 },
 text: {
 text: '+',
 fill: '#1565c0',
 fontSize: 16,
 fontWeight: 'bold'
 }
 }
 }, true);

 // 注册包容网关
 Graph.registerNode('bpmn-gateway-inclusive', {
 inherit: 'diamond',
 width: 60,
 height: 60,
 attrs: {
 body: {
 fill: '#fff3e0',
 stroke: '#e65100',
 strokeWidth: 3
 },
 text: {
 text: 'O',
 fill: '#e65100',
 fontSize: 16,
 fontWeight: 'bold'
 }
 }
 }, true);

 // 注册子流程
 Graph.registerNode('bpmn-subprocess', {
 inherit: 'rect',
 width: 120,
 height: 80,
 attrs: {
 body: {
 fill: '#f5f5f5',
 stroke: '#909399',
 strokeWidth: 2,
 rx: 4,
 ry: 4
 },
 text: {
 text: '子流程',
 fill: '#546e7a',
 fontSize: 12
 }
 },
 portGroups: ['in', 'out']
 }, true);

 // 安装插件
 graph.use(new Snapline({ enabled: true }));
 graph.use(new Selection({
 enabled: true,
 multiple: true,
 rubberEdge: true,
 rubberNode: true
 }));
 graph.use(new History({ enabled: true }));
 graph.use(new Keyboard({ enabled: true }));
 graph.use(new Clipboard({ enabled: true }));
 graph.use(new Export());
 graph.use(new Transform({ enabled: true, resizing: true, rotating: true }));
 graph.use(new Scroller({ enabled: true, pannable: true }));
 graph.use(new Minimap({ enabled: true, container: document.getElementById('x6-minimap') }));

 // 监听节点点击事件
 graph.on('node:click', ({ node }) => {
 selectedNode.value = {
 id: node.id,
 type: node.shape,
 label: node.getLabel()?.text || node.attr('text/text') || '',
 x: node.position().x,
 y: node.position().y,
 width: node.size().width,
 height: node.size().height,
 ...node.getData()
 };
 selectedConnection.value = null;
 });

 // 监听边点击事件
 graph.on('edge:click', ({ edge }) => {
 selectedConnection.value = {
 id: edge.id,
 source: edge.getSource(),
 target: edge.getTarget(),
 sourceCell: edge.getSourceCell()?.id,
 targetCell: edge.getTargetCell()?.id,
 label: edge.getLabel()?.text || '',
 ...edge.getData()
 };
 selectedNode.value = null;
 });

 // 监听画布点击
 graph.on('blank:click', () => {
 selectedNode.value = null;
 selectedConnection.value = null;
 });

 // 监听节点移动
 graph.on('node:moved', () => {
 updateNodeCount();
 saveHistory();
 });

 // 监听边添加
 graph.on('edge:added', () => {
 updateEdgeCount();
 saveHistory();
 });

 // 监听边删除
 graph.on('edge:removed', () => {
 updateEdgeCount();
 saveHistory();
 });

 // 监听节点添加
 graph.on('node:added', () => {
 updateNodeCount();
 saveHistory();
 });

 // 监听节点删除
 graph.on('node:removed', () => {
 updateNodeCount();
 saveHistory();
 });

 // 监听缩放变化
 graph.on('scale', () => {
 zoomLevel.value = Math.round(graph.zoom() * 100);
 });

 // 同步历史数据到 X6
 syncNodesToGraph();
 syncConnectionsToGraph();

 console.log('X6 Graph 初始化完成');
 });
};

// 同步 nodes 数据到 X6 画布
const syncNodesToGraph = () => {
 if (!graph || !nodes.value) return;
 // 清空现有节点
 graph.resetCells(nodes.value.map(node => ({
 id: node.id,
 shape: node.type,
 x: node.x,
 y: node.y,
 width: node.width || 120,
 height: node.height || 60,
 attrs: {
 body: {
 fill: getNodeColor(node.type),
 stroke: getNodeStrokeColor(node.type)
 },
 text: {
 text: node.label
 }
 },
 data: {
 what: node.what,
 why: node.why,
 who: node.who,
 when: node.when,
 where: node.where,
 rule: node.rule,
 result: node.result,
 assignee: node.assignee,
 policy: node.policy,
 policyName: node.policyName
 }
 })));
};

// 同步 connections 数据到 X6 画布
const syncConnectionsToGraph = () => {
 if (!graph || !connections.value) return;
 connections.value.forEach(conn => {
 graph.addEdge({
 source: conn.from,
 target: conn.to,
 attrs: {
 line: {
 stroke: '#67c23a',
 strokeWidth: 2,
 targetMarker: {
 name: 'classic',
 size: 8
 }
 }
 },
 data: {
 condition: conn.condition,
 label: conn.label
 }
 });
 });
};

// 获取节点填充颜色
const getNodeColor = (type) => {
 const colors = {
 'bpmn-start': '#e8f5e9',
 'bpmn-end': '#ffebee',
 'bpmn-task': '#e3f2fd',
 'bpmn-approval': '#e8f5e9',
 'bpmn-gateway-exclusive': '#ffebee',
 'bpmn-gateway-parallel': '#e3f2fd',
 'bpmn-gateway-inclusive': '#fff3e0',
 'bpmn-subprocess': '#f5f5f5'
 };
 return colors[type] || '#ffffff';
};

// 获取节点边框颜色
const getNodeStrokeColor = (type) => {
 const colors = {
 'bpmn-start': '#67c23a',
 'bpmn-end': '#f56c6c',
 'bpmn-task': '#409eff',
 'bpmn-approval': '#2e7d32',
 'bpmn-gateway-exclusive': '#f56c6c',
 'bbpmn-gateway-parallel': '#409eff',
 'bpmn-gateway-inclusive': '#e65100',
 'bpmn-subprocess': '#909399'
 };
 return colors[type] || '#409eff';
};

// 更新节点数量
const updateNodeCount = () => {
 if (graph) {
 nodeCount.value = graph.getNodes().length;
 }
};

// 更新连线数量
const updateEdgeCount = () => {
 if (graph) {
 edgeCount.value = graph.getEdges().length;
 }
};

// 将 X6 节点数据同步回 nodes ref
const syncGraphToNodes = () => {
 if (!graph) return;
 const x6Nodes = graph.getNodes();
 nodes.value = x6Nodes.map(node => ({
 id: node.id,
 type: node.shape,
 label: node.getLabel()?.text || '',
 x: node.position().x,
 y: node.position().y,
 width: node.size().width,
 height: node.size().height,
 ...node.getData()
 }));
};

// 将 X6 边数据同步回 connections ref
const syncGraphToConnections = () => {
 if (!graph) return;
 const x6Edges = graph.getEdges();
 connections.value = x6Edges.map(edge => ({
 from: edge.getSource()?.cell,
 to: edge.getTarget()?.cell,
 condition: edge.getData()?.condition,
 label: edge.getLabel()?.text || ''
 }));
};

// 添加工具栏拖拽的节点到画布
const addNodeToGraph = (type, x, y) => {
 if (!graph) return;
 const nodeConfig = {
 'bpmn-start': { shape: 'bpmn-start', label: '开始' },
 'bpmn-end': { shape: 'bpmn-end', label: '结束' },
 'bpmn-task': { shape: 'bpmn-task', label: '任务节点' },
 'bpmn-approval': { shape: 'bpmn-approval', label: '审批节点' },
 'bpmn-gateway-exclusive': { shape: 'bpmn-gateway-exclusive', label: '排他网关' },
 'bpmn-gateway-parallel': { shape: 'bpmn-gateway-parallel', label: '并行网关' },
 'bpmn-gateway-inclusive': { shape: 'bpmn-gateway-inclusive', label: '包容网关' },
 'bpmn-subprocess': { shape: 'bpmn-subprocess', label: '子流程' }
 };
 const config = nodeConfig[type] || { shape: 'bpmn-task', label: '任务' };
 const id = `node_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
 graph.addNode({
 id,
 shape: config.shape,
 x,
 y,
 attrs: {
 text: { text: config.label }
 },
 data: {
 what: '',
 why: '',
 who: '',
 when: '',
 where: '',
 rule: '',
 result: ''
 }
 });
 // 同步到 Vue 数据
 syncGraphToNodes();
 saveHistory();
};

// 删除选中节点
const deleteSelectedNodes = () => {
 if (!graph) return;
 const selected = graph.getSelectedCells();
 if (selected.length > 0) {
 graph.removeCells(selected);
 syncGraphToNodes();
 syncGraphToConnections();
 saveHistory();
 ElMessage.success(`已删除 ${selected.length} 个元素`);
 }
};

// 复制选中节点
const copySelectedNodes = () => {
 if (!graph) return;
 const selected = graph.getSelectedCells();
 if (selected.length > 0) {
 graph.copy(selected);
 ElMessage.success('已复制选中的元素');
 }
};

// 粘贴节点
const pasteNodes = () => {
 if (!graph) return;
 graph.paste({ offset: 20 });
 syncGraphToNodes();
 syncGraphToConnections();
 saveHistory();
 ElMessage.success('已粘贴元素');
};

// 一键规整布局
const autoLayoutGraph = () => {
 if (!graph) return;
 // 使用 Dagre 算法进行自动布局
 graph.layout({
 determinant: 'dagre',
 rankdir: 'TB',
 nodesep: 50,
 ranksep: 80
 });
 syncGraphToNodes();
 saveHistory();
 ElMessage.success('流程图已自动规整');
};

// 适应画布
const fitGraph = () => {
 if (!graph) return;
 graph.fit({
 padding: 50,
 preserveDimension: true,
 maxScale: 1
 });
};

// 放大
const zoomIn = () => {
 if (!graph) return;
 graph.zoom(0.1);
 zoomLevel.value = Math.round(graph.zoom() * 100);
};

// 缩小
const zoomOut = () => {
 if (!graph) return;
 graph.zoom(-0.1);
 zoomLevel.value = Math.round(graph.zoom() * 100);
};

// 销毁 X6 Graph 实例
const destroyGraph = () => {
 if (graph) {
 graph.dispose();
 graph = null;
 }
};

// 导出为 BPMN XML（完整BPMN2.0标准）
const exportBPMNXml = () => {
 if (!graph) return;
 
 // 获取所有节点和边
 const nodes = graph.getNodes();
 const edges = graph.getEdges();
 
 // BPMN元素类型映射
 const getBpmnElementType = (shape) => {
 const typeMap = {
 'bpmn-start': 'startEvent',
 'bpmn-end': 'endEvent',
 'bpmn-intermediate': 'intermediateThrowEvent',
 'bpmn-timer': 'intermediateCatchEvent',
 'bpmn-message': 'intermediateThrowEvent',
 'bpmn-task': 'task',
 'bpmn-approval': 'userTask',
 'bpmn-service': 'serviceTask',
 'bpmn-script': 'scriptTask',
 'bpmn-subprocess': 'subprocess',
 'bpmn-gateway-exclusive': 'exclusiveGateway',
 'bpmn-gateway-parallel': 'parallelGateway',
 'bpmn-gateway-inclusive': 'inclusiveGateway'
 };
 return typeMap[shape] || 'task';
 };
 
 // 生成节点 BPMN XML
 const generateNodeXml = (node) => {
 const shape = node.shape;
 const type = getBpmnElementType(shape);
 const id = node.id;
 const name = node.getLabel()?.text || node.attr('text/text') || node.data?.label || '';
 const x = node.position().x;
 const y = node.position().y;
 const width = node.size().width;
 const height = node.size().height;
 
 let elementXml = '';
 
 switch (type) {
 case 'startEvent':
 case 'endEvent':
 case 'intermediateThrowEvent':
 case 'intermediateCatchEvent':
 elementXml = ` <bpmn:${type} id="${id}" name="${name}"/>`;
 break;
 case 'exclusiveGateway':
 case 'parallelGateway':
 case 'inclusiveGateway':
 elementXml = ` <bpmn:${type} id="${id}" name="${name}" gatewayDirection="Unspecified"/>`;
 break;
 case 'subprocess':
 elementXml = ` <bpmn:subProcess id="${id}" name="${name}">`;
 elementXml += `
 <bpmn:incoming>Flow_${id}_in</bpmn:incoming>
 <bpmn:outgoing>Flow_${id}_out</bpmn:outgoing>
 </bpmn:subProcess>`;
 break;
 default:
 elementXml = ` <bpmn:${type} id="${id}" name="${name}">
 <bpmn:incoming>Flow_${id}_in</bpmn:incoming>
 <bpmn:outgoing>Flow_${id}_out</bpmn:outgoing>
 </bpmn:${type}>`;
 }
 
 return elementXml;
 };
 
 // 生成图形信息 XML
 const generateShapeXml = (node) => {
 const id = node.id;
 const x = node.position().x;
 const y = node.position().y;
 const width = node.size().width;
 const height = node.size().height;
 return ` <bpmndi:BPMNShape id="BPMNShape_${id}" bpmnElement="${id}">
 <dc:Bounds x="${x}" y="${y}" width="${width}" height="${height}"/>
 </bpmndi:BPMNShape>`;
 };
 
 // 生成连线 XML
 const generateEdgeXml = (edge) => {
 const id = edge.id;
 const sourceId = edge.getSource()?.cell;
 const targetId = edge.getTarget()?.cell;
 const sourceX = edge.getSource()?.x || 0;
 const sourceY = edge.getSource()?.y || 0;
 const targetX = edge.getTarget()?.x || 0;
 const targetY = edge.getTarget()?.y || 0;
 const label = edge.getLabel()?.text || '';
 
 return ` <bpmn:sequenceFlow id="${id}" sourceRef="${sourceId}" targetRef="${targetId}" name="${label}">
 <bpmn:conditionExpression>${label ? `<![CDATA[${label}]]>` : ''}</bpmn:conditionExpression>
 </bpmn:sequenceFlow>
 <bpmndi:BPMNEdge id="BPMNEdge_${id}" bpmnElement="${id}">
 <di:waypoint x="${sourceX}" y="${sourceY}"/>
 <di:waypoint x="${targetX}" y="${targetY}"/>
 </bpmndi:BPMNEdge>`;
 };
 
 // 构建完整 BPMN XML
 const processElements = nodes.map(node => generateNodeXml(node)).join('\n');
 const diagramShapes = nodes.map(node => generateShapeXml(node)).join('\n');
 const diagramEdges = edges.map(edge => generateEdgeXml(edge)).join('\n');
 
 const BPMN_XML_TEMPLATE = `<?xml version="1.0" encoding="UTF-8"?>
 <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
 xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
 xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
 xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
 xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
 id="Definitions_${Date.now()}"
 targetNamespace="http://bpmn.io/bpmn"
 exporter="EPROS-X6-Designer" exporterVersion="2.0">
 <bpmn:process id="Process_${flowCode.value}" name="${flowName.value}" isExecutable="true">
 ${processElements}
 ${edges.map(edge => ` <bpmn:sequenceFlow id="${edge.id}" sourceRef="${edge.getSource()?.cell}" targetRef="${edge.getTarget()?.cell}"/>`).join('\n')}
 </bpmn:process>
 <bpmndi:BPMNDiagram id="BPMNDiagram_1" name="${flowName.value}">
 <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_${flowCode.value}">
 ${diagramShapes}
 ${diagramEdges}
 </bpmndi:BPMNPlane>
 </bpmndi:BPMNDiagram>
 </bpmn:definitions>`;
 
 const blob = new Blob([BPMN_XML_TEMPLATE], { type: 'application/xml' });
 const url = URL.createObjectURL(blob);
 const link = document.createElement('a');
 link.href = url;
 link.download = `${flowCode.value}_${flowName.value}.bpmn`;
 link.click();
 URL.revokeObjectURL(url);
 ElMessage.success('BPMN 2.0 文件已导出');
};

// 导出为 SVG
const exportSVG = () => {
 if (!graph) return;
 const svg = graph.toSVG();
 const blob = new Blob([svg], { type: 'image/svg+xml' });
 const url = URL.createObjectURL(blob);
 const link = document.createElement('a');
 link.href = url;
 link.download = `${flowCode.value}_${flowName.value}.svg`;
 link.click();
 URL.revokeObjectURL(url);
 ElMessage.success('SVG 文件已导出');
};

// 导出为 PNG
const exportPNG = () => {
 if (!graph) return;
 graph.toPNG((dataUri) => {
 const link = document.createElement('a');
 link.href = dataUri;
 link.download = `${flowCode.value}_${flowName.value}.png`;
 link.click();
 ElMessage.success('PNG 文件已导出');
 }, {
 backgroundColor: '#ffffff',
 pixelRatio: 2
 });
};

// 制度文档编辑器相关方法
const handleDesignerTabChange = (tab) => {
 designerActiveTab.value = tab;
};
const formatText = (action) => {
 const actionNames = {
 bold: '粗体',
 italic: '斜体',
 underline: '下划线',
 strikethrough: '删除线',
 alignLeft: '左对齐',
 alignCenter: '居中对齐',
 alignRight: '右对齐',
 orderedList: '有序列表',
 unorderedList: '无序列表'
 };
 ElMessage.info(`执行格式操作: ${actionNames[action] || action}`);
};
const insertImage = () => {
 ElMessage.info('插入图片功能开发中');
};
const insertTable = () => {
 ElMessage.info('插入表格功能开发中');
};
const insertAttachment = () => {
 ElMessage.info('插入附件功能开发中');
};
const applyTemplate = () => {
 if (selectedTemplate.value) {
 ElMessage.success('已应用模板');
 }
};
const jumpToSection = (data) => {
 ElMessage.info(`跳转到: ${data.label}`);
};
const refreshOutline = () => {
 ElMessage.success('大纲已刷新');
};
const onEditorInput = () => {
 wordCount.value = Math.floor(Math.random() * 500) + 300;
 paragraphCount.value = Math.floor(Math.random() * 10) + 5;
};
const getNodeIconByType = (type) => {
 const icons = {
 'bpmn-start': CirclePlus,
 'bpmn-end': CircleClose,
 'bpmn-task': Cpu,
 'bpmn-approval': Checked,
 'bpmn-gateway-exclusive': Timer,
 'bpmn-gateway-parallel': Connection
 };
 return icons[type] || Cpu;
};
const doDocPreview = () => {
 previewContent.value = `<h1>${flowName.value}</h1><p>文档预览内容...</p>`;
 docPreviewVisible.value = true;
};
const submitDocApproval = () => {
 ElMessage.success('文档已提交审批');
};

onMounted(() => {
 // 初始化 X6 Graph
 initX6Graph();
 // 初始化拖拽
 initDragAndDrop();
 saveHistory();
 addOperationLog('打开流程', `打开了流程 ${flowName.value}`);
 addVersionHistory();
});

onBeforeUnmount(() => {
 // 销毁 X6 Graph 实例
 destroyGraph();
});
</script>

<style lang="scss" scoped>
.epros-designer {
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  
  .header-left {
    display: flex;
    gap: 12px;
    align-items: center;
    
    .flow-name-input {
      width: 200px;
    }
    
    .flow-code-input {
      width: 150px;
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.designer-body {
  flex: 1;
  display: flex;
  gap: 12px;
  padding: 12px;
  overflow: hidden;
}

.toolbar-panel {
  width: 180px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  overflow-y: auto;
  
  .toolbar-section {
    margin-bottom: 20px;
    
    h4 {
      font-size: 12px;
      color: #606266;
      margin: 0 0 10px 0;
      padding-bottom: 6px;
      border-bottom: 1px solid #f0f0f0;
    }
    
    .tool-items {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
    
    .tool-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 72px;
      height: 60px;
      cursor: pointer;
      border: 1px solid #e8e8e8;
      border-radius: 6px;
      transition: all 0.2s;
      font-size: 11px;
      color: #666;
      
      &:hover {
        border-color: #409eff;
        background: #f0f5ff;
      }
      
      &.active {
        border-color: #409eff;
        background: #409eff;
        color: #fff;
      }
      
      .tool-icon-wrapper {
        width: 32px;
        height: 32px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 4px;
        font-size: 18px;
        
        &.start-icon { background: #e8f5e9; color: #276678; }
        &.end-icon { background: #ffebee; color: #c62828; }
        &.event-icon { background: #fff3e0; color: #e65100; }
        &.timer-icon { background: #fff3e0; color: #e65100; }
        &.message-icon { background: #e3f2fd; color: #1565c0; }
        &.task-icon { background: #e3f2fd; color: #1565c0; }
        &.approval-icon { background: #e8f5e9; color: #2e7d32; }
        &.service-icon { background: #f3e5f5; color: #7b1fa2; }
        &.script-icon { background: #f3e5f5; color: #7b1fa2; }
        &.subprocess-icon { background: #f5f5f5; color: #546e7a; }
        &.gateway-exclusive { background: #ffebee; color: #c62828; }
        &.gateway-parallel { background: #e3f2fd; color: #1565c0; }
        &.gateway-inclusive { background: #fff3e0; color: #e65100; }
        &.line-icon { background: #f5f5f5; color: #666; }
        &.delete-icon { background: #ffebee; color: #c62828; }
        &.clear-icon { background: #f5f5f5; color: #666; }
        &.zoom-icon { background: #f5f5f5; color: #666; }
        &.fit-icon { background: #f5f5f5; color: #666; }
      }
    }
  }
}

.canvas-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  
  .canvas-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    border-bottom: 1px solid #ebeef5;
    font-weight: 600;
    
    .canvas-info {
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: #666;
      font-weight: normal;
    }
    
    .layer-controls {
      display: flex;
      gap: 8px;
    }
  }
  
  .canvas-container {
    flex: 1;
    overflow: hidden;
    background: #f5f7fa;
    position: relative;
    
    // X6 Graph 画布样式
    .x6-canvas {
      width: 100%;
      height: 100%;
      
      // X6 节点样式覆盖
      .x6-node {
        &.selected {
          box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
        }
      }
    }
    
    // X6 Minimap 缩略图
    .x6-minimap {
      position: absolute;
      bottom: 16px;
      right: 16px;
      width: 200px;
      height: 150px;
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      overflow: hidden;
    }
  }
  
  .canvas-footer {
    padding: 8px 16px;
    border-top: 1px solid #ebeef5;
    font-size: 12px;
    color: #666;
  }
}

.flow-node {
  position: absolute;
  width: 120px;
  background: #fff;
  border: 2px solid #d9d9d9;
  border-radius: 8px;
  cursor: move;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  
  &.selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
  
  &.node-bpmn-start {
    border-color: #67c23a;
    .node-header { background: #e8f5e9; }
  }
  
  &.node-bpmn-end {
    border-color: #f56c6c;
    .node-header { background: #ffebee; }
  }
  
  &.node-bpmn-task, &.node-bpmn-approval {
    border-color: #409eff;
    .node-header { background: #e3f2fd; }
  }
  
  &.node-bpmn-subprocess {
    border-color: #909399;
    .node-header { background: #f5f5f5; }
  }
  
  &.node-bpmn-gateway-exclusive {
    border-color: #f56c6c;
    .node-header { background: #fff0f0; }
  }
  
  &.node-bpmn-gateway-parallel {
    border-color: #409eff;
    .node-header { background: #e3f2fd; }
  }
  
  &.node-bpmn-gateway-inclusive {
    border-color: #e6a23c;
    .node-header { background: #fffbe6; }
  }
  
  &.node-bpmn-timer, &.node-bpmn-intermediate, &.node-bpmn-message {
    border-color: #e6a23c;
    .node-header { background: #fffbe6; }
  }
  
  .node-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 8px;
    border-bottom: 1px solid #e8e8e8;
    
    .node-icon {
      font-size: 16px;
    }
    
    .node-title {
      font-size: 12px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .node-body {
    padding: 6px;
    
    .node-property {
      display: flex;
      justify-content: space-between;
      font-size: 10px;
      color: #666;
      margin-bottom: 2px;
      
      .prop-label { color: #999; }
      .prop-value { max-width: 70px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }
  }
  
  .node-ports {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    
    .port {
      position: absolute;
      width: 10px;
      height: 10px;
      background: #fff;
      border: 2px solid #409eff;
      border-radius: 50%;
      opacity: 0;
      transition: opacity 0.2s;
      cursor: crosshair;
      
      &:hover {
        background: #409eff;
      }
      
      &.port-top { top: -5px; left: 50%; transform: translateX(-50%); }
      &.port-right { right: -5px; top: 50%; transform: translateY(-50%); }
      &.port-bottom { bottom: -5px; left: 50%; transform: translateX(-50%); }
      &.port-left { left: -5px; top: 50%; transform: translateY(-50%); }
    }
    
    &:hover .port {
      opacity: 1;
    }
  }
}

.connections {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  
  path {
    pointer-events: stroke;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      stroke-width: 3;
    }
    
    &.selected {
      stroke-width: 3;
      stroke: #409eff;
    }
  }
}

.drawing-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.properties-panel {
  width: 320px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;
    font-weight: 600;
  }
  
  .properties-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    
    .el-tabs {
      height: 100%;
      
      .el-tabs__content {
        height: calc(100% - 40px);
        overflow-y: auto;
      }
    }
  }
  
  .empty-properties {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
    
    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
      color: #d9d9d9;
    }
    
    .hint {
      font-size: 12px;
      margin-top: 8px;
    }
  }
}

.seven-what {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  .what-item {
    display: flex;
    gap: 8px;
    
    .what-label {
      width: 40px;
      font-weight: 500;
      color: #666;
    }
    
    .el-input {
      flex: 1;
    }
  }
}

.text-gray {
  color: #909399;
  margin-left: 8px;
}

.validate-success, .validate-error {
  padding: 20px;
  text-align: center;
  
  .success-icon, .error-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  .success-icon { color: #67c23a; }
  .error-icon { color: #f56c6c; }
  
  p {
    font-size: 14px;
    color: #666;
  }
}

.validate-success {
  .validate-stats {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 16px;
    font-size: 12px;
    color: #999;
  }
}

.validate-error {
  text-align: left;
  
  .error-list {
    margin-top: 16px;
  }
  
  .error-item {
    display: flex;
    flex-direction: column;
    padding: 12px;
    background: #fef0f0;
    border-radius: 6px;
    margin-bottom: 10px;
    
    .error-dot {
      color: #f56c6c;
      margin-right: 8px;
    }
    
    .error-message {
      font-weight: 500;
      color: #f56c6c;
    }
    
    .error-location, .error-suggestion {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.export-menu {
  position: fixed;
  top: 60px;
  right: 20px;
  z-index: 999;
}

.version-compare {
  .compare-header {
    display: flex;
    justify-content: space-between;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 6px;
    margin-bottom: 16px;
    font-weight: 500;
  }
  
  .compare-content {
    .compare-section {
      margin-bottom: 20px;
      
      h4 {
        margin-bottom: 12px;
        color: #606266;
      }
      
      .el-tag {
        margin-right: 8px;
        margin-bottom: 8px;
      }
    }
  }
}

.form-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.log-operator {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.batch-selected {
  box-shadow: 0 0 0 2px #409eff !important;
}

.error-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  
  .error-tab {
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    background: #f5f7fa;
    
    &:hover {
      background: #e9eef3;
    }
    
    &.active {
      background: #409eff;
      color: #fff;
    }
    
    .error-count {
      margin-left: 4px;
      padding: 2px 6px;
      background: rgba(255,255,255,0.3);
      border-radius: 10px;
      font-size: 11px;
    }
  }
}

.condition-builder {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  
  .condition-row {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
    
    .condition-field {
      width: 100px;
    }
    
    .condition-operator {
      width: 100px;
    }
    
    .condition-value {
      flex: 1;
    }
  }
  
  .condition-actions {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .condition-presets {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

/* 流程示例对话框样式 */
.sample-container {
  .sample-header {
    margin-bottom: 16px;
    
    .sample-meta {
      margin-top: 12px;
    }
  }
}

.sample-flowchart {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  min-height: 200px;
  
  .flow-node {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 20px;
    border-radius: 8px;
    min-width: 160px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .node-icon {
      font-size: 24px;
      margin-bottom: 6px;
    }
    
    .node-label {
      font-size: 11px;
      color: #909399;
      margin-bottom: 4px;
    }
    
    .node-name {
      font-size: 13px;
      font-weight: 500;
      color: #303133;
    }
    
    .node-desc {
      font-size: 11px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  .start-node {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
    color: white;
    
    .node-label {
      color: rgba(255, 255, 255, 0.8);
    }
    
    .node-name {
      color: white;
    }
  }
  
  .task-node {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: white;
    
    .node-label {
      color: rgba(255, 255, 255, 0.8);
    }
    
    .node-name {
      color: white;
    }
  }
  
  .gateway-node {
    background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
    color: white;
    transform: rotate(45deg);
    
    .node-icon {
      transform: rotate(-45deg);
    }
    
    .node-label,
    .node-name {
      transform: rotate(-45deg);
    }
    
    .node-label {
      color: rgba(255, 255, 255, 0.8);
    }
    
    .node-name {
      color: white;
      font-size: 12px;
    }
  }
  
  .end-node {
    min-width: 180px;
    
    .node-icon {
      font-size: 28px;
    }
    
    .node-label {
      color: #909399;
    }
    
    .node-name {
      font-size: 12px;
    }
    
    &.success {
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      
      .node-label,
      .node-name {
        color: white;
      }
    }
    
    &.reject {
      background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
      
      .node-label,
      .node-name {
        color: white;
      }
    }
  }
  
  .flow-arrow {
    font-size: 24px;
    color: #409eff;
    margin: 0 8px;
    display: flex;
    align-items: center;
  }
}

.flow-branches {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin: 20px 0;
  
  .branch-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .branch-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;
      
      .branch-condition {
        font-size: 12px;
        color: #606266;
      }
    }
    
    .branch-content {
      display: flex;
      align-items: center;
    }
  }
}

.flow-merge {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}

.flow-reject {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 2px dashed #f56c6c;
  
  .reject-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
    position: absolute;
    top: 50%;
    left: 20px;
    
    span {
      font-size: 12px;
      color: #f56c6c;
    }
  }
  
  .reject-arrow {
    margin: 0 8px;
  }
}

.bpmn-source {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .bpmn-textarea {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    background: #1e1e1e;
    color: #d4d4d4;
    
    :deep(.el-textarea__inner) {
      background: #1e1e1e;
      color: #d4d4d4;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }
  }
}
</style>

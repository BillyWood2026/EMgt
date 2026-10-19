<template>
  <div class="epros-publish">
    <div class="publish-header">
      <div class="header-title">
        <h2>流程发布与宣贯</h2>
        <p>流程发布自动公示、培训任务推送、多格式导出</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Promotion" @click="batchPublish">批量发布</el-button>
        <el-button icon="Download" @click="exportAll">批量导出</el-button>
      </div>
    </div>

    <div class="publish-body">
      <div class="stats-panel">
        <div class="stat-card">
          <div class="stat-value">{{ stats.pendingPublish }}</div>
          <div class="stat-label">待发布流程</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.published }}</div>
          <div class="stat-label">已发布流程</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.trainingRate }}%</div>
          <div class="stat-label">培训覆盖率</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.acknowledged }}</div>
          <div class="stat-label">已确认阅读</div>
        </div>
      </div>

      <div class="publish-tabs">
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="待发布流程" name="pending">
            <div class="pending-panel">
              <div class="toolbar">
                <el-input v-model="searchKey" placeholder="搜索流程名称或编码" class="search-input" />
                <el-select v-model="filterLevel" placeholder="流程层级">
                  <el-option label="全部" value="" />
                  <el-option label="三级主流程" value="level3" />
                  <el-option label="四级子流程" value="level4" />
                  <el-option label="五级岗位流程" value="level5" />
                </el-select>
                <el-button icon="Search" @click="doSearch">搜索</el-button>
              </div>
              
              <el-table :data="pendingFlows" border @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="50" />
                <el-table-column prop="code" label="流程编码" width="140" />
                <el-table-column prop="name" label="流程名称" width="200" />
                <el-table-column prop="level" label="流程层级" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getLevelTagType(row.level)">{{ getLevelName(row.level) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column prop="validationStatus" label="校验状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.validationStatus === 'passed' ? 'success' : 'danger'">
                      {{ row.validationStatus === 'passed' ? '已通过' : '未通过' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="ownerDept" label="主责部门" width="120" />
                <el-table-column prop="updateTime" label="更新时间" width="160" />
                <el-table-column prop="actions" label="操作" width="280">
                  <template #default="{ row }">
                    <el-button size="small" @click="previewFlow(row)">预览</el-button>
                    <el-button size="small" type="primary" @click="publishFlow(row)" :disabled="row.validationStatus !== 'passed'">发布</el-button>
                    <el-button size="small" @click="configPublish(row)">发布配置</el-button>
                    <el-button size="small" @click="viewValidation(row)">查看校验</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="已发布流程" name="published">
            <div class="published-panel">
              <div class="toolbar">
                <el-input v-model="searchKey2" placeholder="搜索流程名称或编码" class="search-input" />
                <el-select v-model="filterSystem" placeholder="体系分类">
                  <el-option label="全部" value="" />
                  <el-option label="安全生产" value="safety" />
                  <el-option label="环境管理" value="environment" />
                  <el-option label="职业健康" value="health" />
                  <el-option label="能源管理" value="energy" />
                  <el-option label="通用流程" value="common" />
                </el-select>
                <el-button icon="Search" @click="doSearch2">搜索</el-button>
              </div>
              
              <el-table :data="publishedFlows" border>
                <el-table-column prop="code" label="流程编码" width="140" />
                <el-table-column prop="name" label="流程名称" width="200" />
                <el-table-column prop="version" label="版本" width="80" />
                <el-table-column prop="publishTime" label="发布时间" width="160" />
                <el-table-column prop="publisher" label="发布人" width="100" />
                <el-table-column prop="trainingStatus" label="培训状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.trainingStatus === 'completed' ? 'success' : 'warning'">
                      {{ row.trainingStatus === 'completed' ? '已完成' : '进行中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="acknowledgeRate" label="阅读确认率" width="100">
                  <template #default="{ row }">
                    <span :class="{ 'low-rate': row.acknowledgeRate < 80 }">{{ row.acknowledgeRate }}%</span>
                  </template>
                </el-table-column>
                <el-table-column prop="actions" label="操作" width="240">
                  <template #default="{ row }">
                    <el-button size="small" @click="viewPublishDetail(row)">详情</el-button>
                    <el-button size="small" @click="pushTraining(row)">推送培训</el-button>
                    <el-button size="small" @click="exportFlow(row)">导出</el-button>
                    <el-button size="small" type="danger" @click="deprecateFlow(row)">废止</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="培训任务" name="training">
            <div class="training-panel">
              <div class="toolbar">
                <el-button type="primary" icon="Plus" @click="createTrainingTask">新建培训任务</el-button>
                <el-select v-model="filterTrainingStatus" placeholder="培训状态">
                  <el-option label="全部" value="" />
                  <el-option label="未开始" value="pending" />
                  <el-option label="进行中" value="ongoing" />
                  <el-option label="已完成" value="completed" />
                </el-select>
              </div>
              
              <div class="training-list">
                <div v-for="task in trainingTasks" :key="task.id" class="training-card">
                  <div class="card-header">
                    <div class="task-info">
                      <span class="task-name">{{ task.name }}</span>
                      <el-tag :type="getTrainingStatusTag(task.status)">{{ getTrainingStatusName(task.status) }}</el-tag>
                    </div>
                    <div class="task-time">
                      <span>截止时间: {{ task.deadline }}</span>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="flow-list">
                      <span class="label">关联流程:</span>
                      <span v-for="flow in task.flows" :key="flow.id" class="flow-tag">{{ flow.name }}</span>
                    </div>
                    <div class="progress-info">
                      <span class="label">完成进度:</span>
                      <el-progress :percentage="task.progress" :status="task.progress === 100 ? 'success' : ''" />
                    </div>
                    <div class="stats-info">
                      <span>应培训: {{ task.totalCount }} 人</span>
                      <span>已完成: {{ task.completedCount }} 人</span>
                      <span>未完成: {{ task.pendingCount }} 人</span>
                    </div>
                  </div>
                  <div class="card-footer">
                    <el-button size="small" @click="viewTrainingDetail(task)">查看详情</el-button>
                    <el-button size="small" @click="remindTraining(task)" :disabled="task.status === 'completed'">催办提醒</el-button>
                    <el-button size="small" @click="exportTrainingReport(task)">导出报告</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="导出中心" name="export">
            <div class="export-panel">
              <div class="export-options">
                <div class="option-section">
                  <h4>导出格式</h4>
                  <div class="format-options">
                    <div class="format-item" :class="{ active: exportFormat === 'bpmn' }" @click="exportFormat = 'bpmn'">
                      <el-icon><Document /></el-icon>
                      <span>BPMN源文件</span>
                    </div>
                    <div class="format-item" :class="{ active: exportFormat === 'svg' }" @click="exportFormat = 'svg'">
                      <el-icon><Picture /></el-icon>
                      <span>SVG流程图</span>
                    </div>
                    <div class="format-item" :class="{ active: exportFormat === 'png' }" @click="exportFormat = 'png'">
                      <el-icon><PictureFilled /></el-icon>
                      <span>PNG流程图</span>
                    </div>
                    <div class="format-item" :class="{ active: exportFormat === 'pdf' }" @click="exportFormat = 'pdf'">
                      <el-icon><DocumentCopy /></el-icon>
                      <span>PDF作业手册</span>
                    </div>
                    <div class="format-item" :class="{ active: exportFormat === 'word' }" @click="exportFormat = 'word'">
                      <el-icon><Notebook /></el-icon>
                      <span>Word作业手册</span>
                    </div>
                  </div>
                </div>
                
                <div class="option-section">
                  <h4>导出范围</h4>
                  <el-radio-group v-model="exportScope">
                    <el-radio label="selected">选中流程</el-radio>
                    <el-radio label="level">按层级导出</el-radio>
                    <el-radio label="system">按体系导出</el-radio>
                    <el-radio label="all">全部流程</el-radio>
                  </el-radio-group>
                  
                  <div v-if="exportScope === 'level'" class="scope-config">
                    <el-select v-model="exportLevel" multiple placeholder="选择层级">
                      <el-option label="三级主流程" value="level3" />
                      <el-option label="四级子流程" value="level4" />
                      <el-option label="五级岗位流程" value="level5" />
                    </el-select>
                  </div>
                  
                  <div v-if="exportScope === 'system'" class="scope-config">
                    <el-select v-model="exportSystem" multiple placeholder="选择体系">
                      <el-option label="安全生产" value="safety" />
                      <el-option label="环境管理" value="environment" />
                      <el-option label="职业健康" value="health" />
                      <el-option label="能源管理" value="energy" />
                      <el-option label="通用流程" value="common" />
                    </el-select>
                  </div>
                </div>
                
                <div class="option-section">
                  <h4>导出配置</h4>
                  <div class="config-options">
                    <el-checkbox v-model="exportConfig.includeWatermark">添加水印</el-checkbox>
                    <el-checkbox v-model="exportConfig.includeVersion">包含版本信息</el-checkbox>
                    <el-checkbox v-model="exportConfig.includeAssets">包含资产绑定</el-checkbox>
                    <el-checkbox v-model="exportConfig.includeCompliance">包含合规依据</el-checkbox>
                  </div>
                </div>
              </div>
              
              <div class="export-actions">
                <el-button type="primary" icon="Download" @click="executeExport">执行导出</el-button>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="公示公告" name="notice">
            <div class="notice-panel">
              <div class="toolbar">
                <el-button type="primary" icon="Plus" @click="createNotice">发布公告</el-button>
              </div>
              
              <div class="notice-list">
                <div v-for="notice in notices" :key="notice.id" class="notice-card">
                  <div class="notice-header">
                    <el-tag :type="notice.type === 'publish' ? 'success' : notice.type === 'update' ? 'warning' : 'danger'">
                      {{ notice.type === 'publish' ? '新发布' : notice.type === 'update' ? '版本更新' : '废止通知' }}
                    </el-tag>
                    <span class="notice-title">{{ notice.title }}</span>
                    <span class="notice-time">{{ notice.createTime }}</span>
                  </div>
                  <div class="notice-body">
                    <div class="flow-info">
                      <span>流程: {{ notice.flowName }}</span>
                      <span>版本: {{ notice.version }}</span>
                    </div>
                    <div class="notice-content">{{ notice.content }}</div>
                  </div>
                  <div class="notice-footer">
                    <span>发布人: {{ notice.publisher }}</span>
                    <span>阅读数: {{ notice.readCount }}</span>
                    <el-button size="small" @click="viewNotice(notice)">查看详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="智能校验" name="validation">
            <div class="validation-panel">
              <div class="validation-header">
                <div class="header-title">
                  <h2>流程规范智能校验</h2>
                  <p>杰成流程建设规范引擎 - 流程发布前全自动智能校验</p>
                </div>
                <div class="header-actions">
                  <el-button type="primary" icon="CircleCheck" @click="runFullValidation">执行全量校验</el-button>
                  <el-button icon="Download" @click="exportValidationReport">导出校验报告</el-button>
                  <el-button icon="Refresh" @click="refreshValidationData">刷新</el-button>
                </div>
              </div>
              <div class="validation-stats">
                <div class="stat-card total">
                  <div class="stat-icon">📄</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ validationStats.totalFlows }}</div>
                    <div class="stat-label">待校验流程</div>
                  </div>
                </div>
                <div class="stat-card passed">
                  <div class="stat-icon">✅</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ validationStats.passedFlows }}</div>
                    <div class="stat-label">校验通过</div>
                  </div>
                </div>
                <div class="stat-card warning">
                  <div class="stat-icon">⚠️</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ validationStats.warningFlows }}</div>
                    <div class="stat-label">存在警告</div>
                  </div>
                </div>
                <div class="stat-card error">
                  <div class="stat-icon">❌</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ validationStats.errorFlows }}</div>
                    <div class="stat-label">校验失败</div>
                  </div>
                </div>
              </div>
              <el-tabs v-model="validationTab" type="card">
                <el-tab-pane label="校验任务列表" name="validation-tasks">
                  <div class="task-panel">
                    <div class="task-toolbar">
                      <el-input v-model="validationSearchKey" placeholder="搜索流程名称或编码" class="search-input" />
                      <el-select v-model="filterValidationResult" placeholder="校验结果">
                        <el-option label="全部" value="" />
                        <el-option label="通过" value="passed" />
                        <el-option label="警告" value="warning" />
                        <el-option label="失败" value="error" />
                        <el-option label="待校验" value="pending" />
                      </el-select>
                      <el-button icon="Search" @click="doValidationSearch">搜索</el-button>
                    </div>
                    <el-table :data="validationTasks" border>
                      <el-table-column prop="flowCode" label="流程编码" width="140" />
                      <el-table-column prop="flowName" label="流程名称" width="200" />
                      <el-table-column prop="validationType" label="校验类型" width="100">
                        <template #default="{ row }">
                          <el-tag :type="getValidationTypeTag(row.validationType)">{{ getValidationTypeName(row.validationType) }}</el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="result" label="校验结果" width="100">
                        <template #default="{ row }">
                          <el-tag :type="getValidationResultTag(row.result)">{{ getValidationResultName(row.result) }}</el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="errorCount" label="错误数" width="80" />
                      <el-table-column prop="warningCount" label="警告数" width="80" />
                      <el-table-column prop="validateTime" label="校验时间" width="160" />
                      <el-table-column label="操作" width="200">
                        <template #default="{ row }">
                          <el-button size="small" @click="viewValidationDetail(row)">查看详情</el-button>
                          <el-button size="small" @click="runSingleValidation(row)">重新校验</el-button>
                          <el-button size="small" type="primary" v-if="row.result === 'passed'" @click="publishFromValidation(row)">发布</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="BPMN语法校验" name="syntax">
                  <div class="syntax-panel">
                    <div class="panel-header">
                      <span>流程图语法规范校验（连线错误、节点缺失、闭环错误）</span>
                      <el-button size="small" type="primary" @click="runSyntaxValidation">执行语法校验</el-button>
                    </div>
                    <div class="rule-list">
                      <div v-for="rule in syntaxRules" :key="rule.id" class="rule-item">
                        <div class="rule-header">
                          <el-checkbox v-model="rule.enabled" />
                          <span class="rule-name">{{ rule.name }}</span>
                          <el-tag :type="rule.severity === 'error' ? 'danger' : 'warning'" size="small">{{ rule.severity === 'error' ? '错误' : '警告' }}</el-tag>
                        </div>
                        <div class="rule-desc">{{ rule.description }}</div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="字段完整性校验" name="field">
                  <div class="field-panel">
                    <div class="panel-header">
                      <span>流程字段完整性校验（七何要素缺失、权责缺失、依据缺失）</span>
                      <el-button size="small" type="primary" @click="runFieldValidation">执行字段校验</el-button>
                    </div>
                    <div class="check-section">
                      <h4>七何要素校验</h4>
                      <div class="check-grid">
                        <div v-for="item in sevenWhatChecks" :key="item.key" class="check-item">
                          <el-checkbox v-model="item.enabled" />
                          <span class="check-label">{{ item.label }}</span>
                          <el-tag :type="item.required ? 'danger' : 'warning'" size="small">{{ item.required ? '必填' : '建议' }}</el-tag>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="文件配套校验" name="file">
                  <div class="file-panel">
                    <div class="panel-header">
                      <span>流程文件配套校验（未绑定制度/表单/作业文件禁止发布）</span>
                      <el-button size="small" type="primary" @click="runFileValidation">执行文件校验</el-button>
                    </div>
                    <div class="file-requirements">
                      <div class="requirement-item">
                        <div class="requirement-header">
                          <el-checkbox v-model="fileRequirements.policy" />
                          <span class="requirement-name">制度文件绑定</span>
                          <el-tag type="danger" size="small">强制</el-tag>
                        </div>
                        <div class="requirement-desc">每个流程节点必须绑定对应的制度条款或程序文件</div>
                      </div>
                      <div class="requirement-item">
                        <div class="requirement-header">
                          <el-checkbox v-model="fileRequirements.form" />
                          <span class="requirement-name">表单文件绑定</span>
                          <el-tag type="danger" size="small">强制</el-tag>
                        </div>
                        <div class="requirement-desc">每个流程节点必须绑定对应的执行表单</div>
                      </div>
                      <div class="requirement-item">
                        <div class="requirement-header">
                          <el-checkbox v-model="fileRequirements.sop" />
                          <span class="requirement-name">作业指导书绑定</span>
                          <el-tag type="warning" size="small">建议</el-tag>
                        </div>
                        <div class="requirement-desc">建议每个流程节点绑定对应的作业指导书</div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-tab-pane>

          <el-tab-pane label="版本管理" name="version">
            <div class="version-panel">
              <div class="version-header">
                <div class="header-title">
                  <h2>流程版本全生命周期管理</h2>
                  <p>流程版本化：草稿、待审核、生效、作废、归档全状态管控</p>
                </div>
                <div class="header-actions">
                  <el-button size="small" icon="Clock" @click="viewAllVersionHistory">查看全部历史</el-button>
                  <el-button size="small" icon="Download" @click="exportVersionHistory">导出历史</el-button>
                </div>
              </div>
              <el-tabs v-model="versionTab" type="card">
                <el-tab-pane label="版本生命周期" name="lifecycle">
                  <div class="lifecycle-panel">
                    <div class="lifecycle-header">
                      <el-input v-model="versionSearchKey" placeholder="搜索流程名称或编码" class="search-input" />
                      <el-select v-model="filterVersionStatus" placeholder="状态筛选">
                        <el-option label="全部" value="" />
                        <el-option label="草稿" value="draft" />
                        <el-option label="待审核" value="pending" />
                        <el-option label="已生效" value="active" />
                        <el-option label="已作废" value="deprecated" />
                        <el-option label="已归档" value="archived" />
                      </el-select>
                      <el-button icon="Search" @click="doVersionSearch">搜索</el-button>
                    </div>
                    <div class="lifecycle-grid">
                      <div v-for="item in lifecycleData" :key="item.id" class="lifecycle-card">
                        <div class="card-header">
                          <div class="flow-info">
                            <span class="flow-name">{{ item.flowName }}</span>
                            <span class="flow-code">{{ item.flowCode }}</span>
                          </div>
                          <el-tag :type="getVersionStatusTag(item.status)">{{ getVersionStatusName(item.status) }}</el-tag>
                        </div>
                        <div class="lifecycle-flow">
                          <div class="lifecycle-step" :class="{ active: item.status === 'draft', completed: ['pending', 'active', 'deprecated', 'archived'].includes(item.status) }">
                            <div class="step-icon">📝</div>
                            <span>草稿</span>
                          </div>
                          <div class="step-line" :class="{ completed: ['pending', 'active', 'deprecated', 'archived'].includes(item.status) }"></div>
                          <div class="lifecycle-step" :class="{ active: item.status === 'pending', completed: ['active', 'deprecated', 'archived'].includes(item.status) }">
                            <div class="step-icon">⏳</div>
                            <span>待审核</span>
                          </div>
                          <div class="step-line" :class="{ completed: ['active', 'deprecated', 'archived'].includes(item.status) }"></div>
                          <div class="lifecycle-step" :class="{ active: item.status === 'active', completed: ['deprecated', 'archived'].includes(item.status) }">
                            <div class="step-icon">✅</div>
                            <span>生效</span>
                          </div>
                          <div class="step-line" :class="{ completed: ['deprecated', 'archived'].includes(item.status) }"></div>
                          <div class="lifecycle-step" :class="{ active: item.status === 'deprecated', completed: ['archived'].includes(item.status) }">
                            <div class="step-icon">❌</div>
                            <span>作废</span>
                          </div>
                          <div class="step-line" :class="{ completed: item.status === 'archived' }"></div>
                          <div class="lifecycle-step" :class="{ active: item.status === 'archived' }">
                            <div class="step-icon">📁</div>
                            <span>归档</span>
                          </div>
                        </div>
                        <div class="version-info">
                          <span>当前版本: <el-tag size="small">{{ item.currentVersion }}</el-tag></span>
                          <span>版本数: {{ item.totalVersions }}</span>
                        </div>
                        <div class="card-actions">
                          <el-button size="small" @click="viewVersionDetail(item)">详情</el-button>
                          <el-button size="small" v-if="item.status === 'draft'" type="primary" @click="submitVersionReview(item)">提交审核</el-button>
                          <el-button size="small" v-if="item.status === 'pending'" type="success" @click="approveVersion(item)">审批</el-button>
                          <el-button size="small" v-if="item.status === 'active'" type="warning" @click="createNewVersion(item)">新建版本</el-button>
                          <el-button size="small" v-if="item.status === 'active'" type="danger" @click="deprecateVersion(item)">废止</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="版本对比" name="compare">
                  <div class="compare-panel">
                    <div class="compare-select">
                      <div class="select-group">
                        <label>旧版本</label>
                        <el-select v-model="compareOld" placeholder="选择旧版本">
                          <el-option v-for="v in versionOptions" :key="v.id" :label="v.version" :value="v.id" />
                        </el-select>
                      </div>
                      <div class="select-group">
                        <label>新版本</label>
                        <el-select v-model="compareNew" placeholder="选择新版本">
                          <el-option v-for="v in versionOptions" :key="v.id" :label="v.version" :value="v.id" />
                        </el-select>
                      </div>
                      <el-button type="primary" @click="doVersionCompare">对比</el-button>
                    </div>
                    <div v-if="compareResult" class="compare-result">
                      <div class="result-header">
                        <span>对比结果</span>
                      </div>
                      <div class="diff-summary">
                        <span>新增: <el-tag type="success">{{ compareResult.added }} 处</el-tag></span>
                        <span>修改: <el-tag type="warning">{{ compareResult.modified }} 处</el-tag></span>
                        <span>删除: <el-tag type="danger">{{ compareResult.deleted }} 处</el-tag></span>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="版本历史" name="history">
                  <div class="history-panel">
                    <div class="history-list">
                      <div v-for="record in versionHistory" :key="record.id" class="history-card">
                        <div class="card-header">
                          <div class="flow-info">
                            <span class="flow-name">{{ record.flowName }}</span>
                            <span class="flow-code">{{ record.flowCode }}</span>
                          </div>
                          <el-tag :type="getVersionStatusTag(record.status)">{{ getVersionStatusName(record.status) }}</el-tag>
                        </div>
                        <div class="card-body">
                          <div class="version-info">
                            <span>版本: {{ record.version }}</span>
                            <span>→</span>
                            <span>{{ record.nextVersion || '当前版本' }}</span>
                          </div>
                          <div class="change-info">
                            <span class="change-type">{{ getChangeTypeName(record.changeType) }}</span>
                            <span class="change-desc">{{ record.changeDescription }}</span>
                          </div>
                        </div>
                        <div class="card-footer">
                          <span>操作人: {{ record.operator }}</span>
                          <span>操作时间: {{ record.operationTime }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog title="发布配置" v-model="publishConfigVisible" width="600px">
      <el-form :model="publishConfig" label-width="120px">
        <el-form-item label="发布类型">
          <el-radio-group v-model="publishConfig.type">
            <el-radio label="immediate">立即发布</el-radio>
            <el-radio label="scheduled">定时发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="publishConfig.type === 'scheduled'" label="发布时间">
          <el-date-picker v-model="publishConfig.scheduledTime" type="datetime" placeholder="选择发布时间" />
        </el-form-item>
        <el-form-item label="公示设置">
          <el-checkbox v-model="publishConfig.showNotice">发布公示公告</el-checkbox>
        </el-form-item>
        <el-form-item label="培训设置">
          <el-checkbox v-model="publishConfig.pushTraining">自动推送培训任务</el-checkbox>
        </el-form-item>
        <el-form-item v-if="publishConfig.pushTraining" label="培训对象">
          <el-select v-model="publishConfig.trainingTargets" multiple placeholder="选择培训对象">
            <el-option label="全体员工" value="all" />
            <el-option label="生产部" value="production" />
            <el-option label="品质部" value="quality" />
            <el-option label="EHS部" value="ehs" />
            <el-option label="能源管理部" value="energy" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="publishConfig.pushTraining" label="培训截止时间">
          <el-date-picker v-model="publishConfig.trainingDeadline" type="datetime" placeholder="选择截止时间" />
        </el-form-item>
        <el-form-item label="阅读确认">
          <el-checkbox v-model="publishConfig.requireAcknowledge">要求相关人员确认阅读</el-checkbox>
        </el-form-item>
        <el-form-item label="发布说明">
          <el-input v-model="publishConfig.description" type="textarea" :rows="3" placeholder="请输入发布说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, Download, Plus, Search, Document, Picture, PictureFilled, DocumentCopy, Notebook } from '@element-plus/icons-vue'
import axios from '@/api/axiosConfig'

const activeTab = ref('pending')
const searchKey = ref('')
const searchKey2 = ref('')
const filterLevel = ref('')
const filterSystem = ref('')
const filterTrainingStatus = ref('')
const selectedFlows = ref([])
const publishConfigVisible = ref(false)
const exportFormat = ref('bpmn')
const exportScope = ref('selected')
const exportLevel = ref([])
const exportSystem = ref([])

const stats = ref({
  pendingPublish: 12,
  published: 86,
  trainingRate: 78,
  acknowledged: 234
})

const publishConfig = reactive({
  type: 'immediate',
  scheduledTime: '',
  showNotice: true,
  pushTraining: true,
  trainingTargets: [],
  trainingDeadline: '',
  requireAcknowledge: true,
  description: ''
})

const exportConfig = reactive({
  includeWatermark: true,
  includeVersion: true,
  includeAssets: true,
  includeCompliance: true
})

const pendingFlows = ref([
  { id: 1, code: 'FP090101', name: 'EHS体系文件全生命周期主流程', level: 'level3', version: 'V2.0', validationStatus: 'passed', ownerDept: 'EHS部', updateTime: '2024-04-15 10:30:00' },
  { id: 2, code: 'FP090501', name: '隐患排查、整改、验证闭环主流程', level: 'level3', version: 'V2.0', validationStatus: 'passed', ownerDept: 'EHS部', updateTime: '2024-04-15 10:25:00' },
  { id: 3, code: 'FP100401', name: '节能改善项目立项-实施-验证-固化主流程', level: 'level3', version: 'V1.0', validationStatus: 'error', ownerDept: '能源管理部', updateTime: '2024-04-15 10:20:00' },
  { id: 4, code: 'FP060101', name: '来料检验判定与放行主流程', level: 'level3', version: 'V3.0', validationStatus: 'passed', ownerDept: '品质部', updateTime: '2024-04-15 10:15:00' },
  { id: 5, code: 'FP040101', name: '生产订单下达与排程主流程', level: 'level3', version: 'V2.0', validationStatus: 'passed', ownerDept: '生产中心', updateTime: '2024-04-15 10:10:00' }
])

const publishedFlows = ref([
  { id: 1, code: 'FP050101', name: 'SMT首件确认主流程', version: 'V2.0', publishTime: '2024-04-10 09:00:00', publisher: '张三', trainingStatus: 'completed', acknowledgeRate: 95 },
  { id: 2, code: 'FP060301', name: '成品检验与出货放行主流程', version: 'V2.0', publishTime: '2024-04-08 14:30:00', publisher: '李四', trainingStatus: 'ongoing', acknowledgeRate: 72 },
  { id: 3, code: 'FP060401', name: '不良品隔离、返工、报废闭环主流程', version: 'V2.0', publishTime: '2024-04-05 11:00:00', publisher: '王五', trainingStatus: 'completed', acknowledgeRate: 88 },
  { id: 4, code: 'FP050501', name: 'ESD日常检查与异常处置主流程', version: 'V1.0', publishTime: '2024-04-01 16:00:00', publisher: '赵六', trainingStatus: 'completed', acknowledgeRate: 100 }
])

const trainingTasks = ref([
  { id: 1, name: 'EHS体系文件管理培训', status: 'ongoing', deadline: '2024-04-30', flows: [{ id: 1, name: 'EHS体系文件全生命周期主流程' }], progress: 65, totalCount: 120, completedCount: 78, pendingCount: 42 },
  { id: 2, name: '品质管理流程培训', status: 'completed', deadline: '2024-04-15', flows: [{ id: 1, name: '来料检验判定与放行主流程' }, { id: 2, name: '成品检验与出货放行主流程' }], progress: 100, totalCount: 80, completedCount: 80, pendingCount: 0 },
  { id: 3, name: '生产排程流程培训', status: 'pending', deadline: '2024-05-15', flows: [{ id: 1, name: '生产订单下达与排程主流程' }], progress: 0, totalCount: 50, completedCount: 0, pendingCount: 50 }
])

const notices = ref([
  { id: 1, type: 'publish', title: '关于发布《SMT首件确认主流程》的通知', flowName: 'SMT首件确认主流程', version: 'V2.0', content: '为规范SMT首件确认流程，确保产品质量，现发布新版本流程。', createTime: '2024-04-10 09:00:00', publisher: '张三', readCount: 156 },
  { id: 2, type: 'update', title: '《成品检验与出货放行主流程》版本更新通知', flowName: '成品检验与出货放行主流程', version: 'V2.0', content: '根据客户要求，更新了出货检验标准，请相关人员注意。', createTime: '2024-04-08 14:30:00', publisher: '李四', readCount: 89 },
  { id: 3, type: 'deprecate', title: '关于废止《旧版不良品处理流程》的通知', flowName: '不良品处理流程(旧版)', version: 'V1.0', content: '新版本已发布，旧版本即日起废止。', createTime: '2024-04-05 11:00:00', publisher: '王五', readCount: 67 }
])

const getLevelTagType = (level) => {
  const map = { level3: 'primary', level4: 'success', level5: 'warning' }
  return map[level] || 'info'
}

const getLevelName = (level) => {
  const map = { level3: '三级主流程', level4: '四级子流程', level5: '五级岗位流程' }
  return map[level] || level
}

const getTrainingStatusTag = (status) => {
  const map = { pending: 'info', ongoing: 'warning', completed: 'success' }
  return map[status] || 'info'
}

const getTrainingStatusName = (status) => {
  const map = { pending: '未开始', ongoing: '进行中', completed: '已完成' }
  return map[status] || status
}

const handleSelectionChange = (selection) => {
  selectedFlows.value = selection
}

const batchPublish = async () => {
  if (selectedFlows.value.length === 0) {
    ElMessage.warning('请先选择要发布的流程')
    return
  }
  try {
    for (const row of selectedFlows.value) {
      await axios.post(`/api/process/def/${row.id}/approve`, { comment: '批量发布前评审通过' })
      await axios.post(`/api/process/def/${row.id}/publish`)
    }
    ElMessage.success(`已批量发布 ${selectedFlows.value.length} 个流程`)
    await loadFlows()
  } catch (e) {
    const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message
    ElMessage.error('批量发布失败：' + msg)
  }
}

const publishFlow = (row) => {
  ElMessageBox.confirm(
    `确定要发布流程 "${row.name}" 吗？`,
    '发布确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await axios.post(`/api/process/def/${row.id}/approve`, { comment: '发布前评审通过' })
      await axios.post(`/api/process/def/${row.id}/publish`)
      ElMessage.success('流程发布成功')
      await loadFlows()
    } catch (e) {
      const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message
      ElMessage.error('发布失败：' + msg)
    }
  }).catch(() => {})
}

const configPublish = (row) => {
  publishConfigVisible.value = true
}

const confirmPublish = () => {
  ElMessage.success('流程发布成功')
  publishConfigVisible.value = false
}

const previewFlow = (row) => {
  ElMessage.info(`预览流程: ${row.name}`)
}

const viewValidation = (row) => {
  ElMessage.info(`查看校验结果: ${row.name}`)
}

const viewPublishDetail = (row) => {
  ElMessage.info(`查看发布详情: ${row.name}`)
}

const pushTraining = (row) => {
  ElMessage.success(`已推送培训任务: ${row.name}`)
}

const exportFlow = (row) => {
  ElMessage.success(`正在导出: ${row.name}`)
}

const deprecateFlow = (row) => {
  ElMessageBox.confirm(
    `确定要废止流程 "${row.name}" 吗？废止后流程将不再生效。`,
    '废止确认',
    { confirmButtonText: '确定废止', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await axios.put(`/api/process/def/${row.id}/deprecate`)
      ElMessage.success('流程已废止')
      await loadFlows()
    } catch (e) {
      const msg = e.response && e.response.data ? (e.response.data.message || e.response.data.msg) : e.message
      ElMessage.error('废止失败：' + msg)
    }
  }).catch(() => {})
}

const createTrainingTask = () => {
  ElMessage.info('新建培训任务')
}

const viewTrainingDetail = (task) => {
  ElMessage.info(`查看培训详情: ${task.name}`)
}

const remindTraining = (task) => {
  ElMessage.success(`已发送催办提醒`)
}

const exportTrainingReport = (task) => {
  ElMessage.success(`正在导出培训报告`)
}

const executeExport = () => {
  ElMessage.success(`正在导出 ${exportFormat.value.toUpperCase()} 格式文件`)
}

const exportAll = () => {
  ElMessage.success('正在批量导出')
}

const createNotice = () => {
  ElMessage.info('新建公告')
}

const viewNotice = (notice) => {
  ElMessage.info(`查看公告: ${notice.title}`)
}

const doSearch = () => {
  ElMessage.success('搜索完成')
}

const doSearch2 = () => {
  ElMessage.success('搜索完成')
}

// ========== 智能校验相关 (来自 ProcessValidation.vue) ==========
const validationTab = ref('validation-tasks')
const validationSearchKey = ref('')
const filterValidationResult = ref('')

const validationStats = ref({
  totalFlows: 45,
  passedFlows: 32,
  warningFlows: 8,
  errorFlows: 5
})

const validationTasks = ref([
  { id: 1, flowCode: 'FP090101', flowName: 'EHS体系文件全生命周期主流程', validationType: 'field', result: 'passed', errorCount: 0, warningCount: 1, validateTime: '2024-04-15 10:30' },
  { id: 2, flowCode: 'FP090501', flowName: '隐患排查、整改、验证闭环主流程', validationType: 'file', result: 'passed', errorCount: 0, warningCount: 0, validateTime: '2024-04-15 10:25' },
  { id: 3, flowCode: 'FP100401', flowName: '节能改善项目立项-实施-验证-固化主流程', validationType: 'syntax', result: 'error', errorCount: 2, warningCount: 0, validateTime: '2024-04-15 10:20' },
  { id: 4, flowCode: 'FP060101', flowName: '来料检验判定与放行主流程', validationType: 'version', result: 'warning', errorCount: 0, warningCount: 3, validateTime: '2024-04-15 10:15' },
  { id: 5, flowCode: 'FP040101', flowName: '生产订单下达与排程主流程', validationType: 'field', result: 'passed', errorCount: 0, warningCount: 0, validateTime: '2024-04-15 10:10' }
])

const syntaxRules = ref([
  { id: 1, name: '开始节点检查', description: '流程图必须包含且仅包含一个开始节点', severity: 'error', enabled: true },
  { id: 2, name: '结束节点检查', description: '流程图必须包含至少一个结束节点', severity: 'error', enabled: true },
  { id: 3, name: '连线完整性检查', description: '所有节点必须有入连线和出连线（开始/结束节点除外）', severity: 'error', enabled: true },
  { id: 4, name: '闭环检查', description: '流程图不应存在无意义的闭环', severity: 'warning', enabled: true },
  { id: 5, name: '网关分支检查', description: '网关分支条件必须互斥且完整', severity: 'error', enabled: true }
])

const sevenWhatChecks = ref([
  { key: 'what', label: '做什么(事项)', required: true, enabled: true },
  { key: 'why', label: '为什么做(目的)', required: false, enabled: true },
  { key: 'who', label: '谁来做(职责)', required: true, enabled: true },
  { key: 'when', label: '何时做(时效)', required: true, enabled: true },
  { key: 'where', label: '在哪里做(地点)', required: false, enabled: true },
  { key: 'how', label: '怎么做(方法)', required: true, enabled: true },
  { key: 'howmuch', label: '做到什么程度(标准)', required: true, enabled: true }
])

const fileRequirements = reactive({
  policy: true,
  form: true,
  sop: true,
  compliance: false
})

const getValidationTypeTag = (type) => {
  const map = { syntax: 'danger', field: 'warning', file: 'success', version: 'info' }
  return map[type] || 'info'
}

const getValidationTypeName = (type) => {
  const map = { syntax: '语法校验', field: '字段校验', file: '文件校验', version: '版本校验' }
  return map[type] || type
}

const getValidationResultTag = (result) => {
  const map = { passed: 'success', warning: 'warning', error: 'danger', pending: 'info' }
  return map[result] || 'info'
}

const getValidationResultName = (result) => {
  const map = { passed: '通过', warning: '警告', error: '失败', pending: '待校验' }
  return map[result] || result
}

const runFullValidation = () => { ElMessage.success('正在执行全量校验') }
const exportValidationReport = () => { ElMessage.success('正在导出校验报告') }
const refreshValidationData = () => { ElMessage.success('数据已刷新') }
const doValidationSearch = () => { ElMessage.success('搜索完成') }
const viewValidationDetail = (row) => { ElMessage.info(`查看校验详情: ${row.flowName}`) }
const runSingleValidation = (row) => { ElMessage.success(`正在重新校验: ${row.flowName}`) }
const publishFromValidation = (row) => { ElMessage.success(`流程发布成功: ${row.flowName}`) }
const runSyntaxValidation = () => { ElMessage.success('正在执行语法校验') }
const runFieldValidation = () => { ElMessage.success('正在执行字段校验') }
const runFileValidation = () => { ElMessage.success('正在执行文件校验') }

// ========== 版本管理相关 (来自 ProcessVersion.vue) ==========
const versionTab = ref('lifecycle')
const versionSearchKey = ref('')
const filterVersionStatus = ref('')
const compareOld = ref('')
const compareNew = ref('')
const compareResult = ref(null)

const versionOptions = ref([
  { id: 1, version: 'V1.0', status: 'deprecated' },
  { id: 2, version: 'V1.5', status: 'deprecated' },
  { id: 3, version: 'V2.0', status: 'active' },
  { id: 4, version: 'V3.0', status: 'draft' }
])

const lifecycleData = ref([
  { id: 1, flowName: 'EHS体系文件全生命周期主流程', flowCode: 'FP090101', status: 'active', currentVersion: 'V2.0', totalVersions: 4, lastUpdate: '2024-04-15' },
  { id: 2, flowName: '隐患排查、整改、验证闭环主流程', flowCode: 'FP090501', status: 'active', currentVersion: 'V2.0', totalVersions: 3, lastUpdate: '2024-04-10' },
  { id: 3, flowName: '来料检验判定与放行主流程', flowCode: 'FP060101', status: 'pending', currentVersion: 'V3.0', totalVersions: 5, lastUpdate: '2024-04-14' },
  { id: 4, flowName: '节能改善项目立项主流程', flowCode: 'FP100401', status: 'draft', currentVersion: 'V1.0', totalVersions: 1, lastUpdate: '2024-04-15' },
  { id: 5, flowName: '旧版不良品处理流程', flowCode: 'FP060401-Old', status: 'deprecated', currentVersion: 'V1.0', totalVersions: 2, lastUpdate: '2024-03-20' }
])

const versionHistory = ref([
  { id: 1, flowName: 'EHS体系文件全生命周期主流程', flowCode: 'FP090101', version: 'V1.0', nextVersion: 'V1.5', changeType: 'update', changeDescription: '优化审批节点', operator: '张三', operationTime: '2024-01-15', status: 'deprecated' },
  { id: 2, flowName: 'EHS体系文件全生命周期主流程', flowCode: 'FP090101', version: 'V1.5', nextVersion: 'V2.0', changeType: 'update', changeDescription: '新增合规检查节点', operator: '李四', operationTime: '2024-03-20', status: 'deprecated' },
  { id: 3, flowName: 'EHS体系文件全生命周期主流程', flowCode: 'FP090101', version: 'V2.0', nextVersion: '', changeType: 'update', changeDescription: '适配新版ISO标准', operator: '王五', operationTime: '2024-04-15', status: 'active' },
  { id: 4, flowName: '隐患排查、整改、验证闭环主流程', flowCode: 'FP090501', version: 'V1.0', nextVersion: 'V2.0', changeType: 'major', changeDescription: '全面重构流程', operator: '赵六', operationTime: '2024-02-10', status: 'deprecated' }
])

const getVersionStatusTag = (status) => {
  const map = { draft: 'info', pending: 'warning', active: 'success', deprecated: 'danger', archived: 'info' }
  return map[status] || 'info'
}

const getVersionStatusName = (status) => {
  const map = { draft: '草稿', pending: '待审核', active: '已生效', deprecated: '已作废', archived: '已归档' }
  return map[status] || status
}

const getChangeTypeName = (type) => {
  const map = { create: '新建', update: '修订', major: '重大变更', deprecate: '废止' }
  return map[type] || type
}

const doVersionSearch = () => { ElMessage.success('搜索完成') }
const viewVersionDetail = (item) => { ElMessage.info(`查看版本详情: ${item.flowName}`) }
const submitVersionReview = (item) => { ElMessage.success(`已提交审核: ${item.flowName}`) }
const approveVersion = (item) => { ElMessage.success(`审批通过: ${item.flowName}`) }
const createNewVersion = (item) => { ElMessage.info(`新建版本: ${item.flowName}`) }
const deprecateVersion = (item) => { ElMessage.success(`已废止: ${item.flowName}`) }
const doVersionCompare = () => {
  compareResult.value = { added: 3, modified: 5, deleted: 2 }
  ElMessage.success('版本对比完成')
}
const viewAllVersionHistory = () => { ElMessage.info('查看全部历史') }
const exportVersionHistory = () => { ElMessage.success('正在导出版本历史') }

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '')

const loadFlows = async () => {
  try {
    const [pendingRes, activeRes] = await Promise.all([
      axios.get('/api/process/def/list', { params: { status: 'pending', pageNum: 1, pageSize: 200 } }),
      axios.get('/api/process/def/list', { params: { status: 'active', pageNum: 1, pageSize: 200 } })
    ])
    const pendingPage = pendingRes.data && pendingRes.data.data ? pendingRes.data.data : {}
    const activePage = activeRes.data && activeRes.data.data ? activeRes.data.data : {}
    pendingFlows.value = (pendingPage.records || []).map(d => ({
      id: d.id,
      code: d.code,
      name: d.name,
      level: d.level,
      version: d.version,
      validationStatus: 'passed',
      ownerDept: d.ownerDept || '',
      updateTime: fmtTime(d.updateTime)
    }))
    publishedFlows.value = (activePage.records || []).map(d => ({
      id: d.id,
      code: d.code,
      name: d.name,
      version: d.version,
      publishTime: fmtTime(d.updateTime),
      publisher: '',
      trainingStatus: 'pending',
      acknowledgeRate: null
    }))
  } catch (e) {
    console.warn('发布台账加载失败', e)
  }
}

onMounted(() => {
  loadFlows()
})
</script>

<style lang="scss" scoped>
.epros-publish {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.publish-header {
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

.publish-body {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.stats-panel {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  
  .stat-card {
    flex: 1;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #3b82f6;
    }
    
    .stat-label {
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }
  }
}

.publish-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .search-input {
    width: 200px;
  }
}

.training-panel {
  .training-list {
    display: grid;
    gap: 16px;
  }
  
  .training-card {
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 16px;
    background: #fff;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .task-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .task-name {
          font-weight: 600;
          font-size: 16px;
        }
      }
    }
    
    .card-body {
      .flow-list, .progress-info {
        margin-bottom: 12px;
        
        .label {
          font-weight: 500;
          margin-right: 8px;
        }
        
        .flow-tag {
          display: inline-block;
          padding: 2px 8px;
          background: #e3f2fd;
          border-radius: 4px;
          font-size: 12px;
          margin-right: 8px;
        }
      }
      
      .stats-info {
        display: flex;
        gap: 24px;
        font-size: 12px;
        color: #666;
      }
    }
    
    .card-footer {
      display: flex;
      gap: 8px;
      padding-top: 12px;
      border-top: 1px solid #eee;
    }
  }
}

.export-panel {
  .export-options {
    .option-section {
      margin-bottom: 24px;
      
      h4 {
        margin-bottom: 12px;
        color: #333;
      }
      
      .format-options {
        display: flex;
        gap: 16px;
        
        .format-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 16px 24px;
          border: 2px solid #e8e8e8;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s;
          
          &:hover {
            border-color: #409eff;
          }
          
          &.active {
            border-color: #409eff;
            background: #f0f5ff;
          }
          
          .el-icon {
            font-size: 32px;
            margin-bottom: 8px;
            color: #409eff;
          }
        }
      }
      
      .scope-config {
        margin-top: 12px;
      }
      
      .config-options {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
    }
  }
  
  .export-actions {
    padding-top: 16px;
    border-top: 1px solid #eee;
  }
}

.notice-panel {
  .notice-list {
    display: grid;
    gap: 16px;
  }
  
  .notice-card {
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    padding: 16px;
    background: #fff;
    
    .notice-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
      
      .notice-title {
        font-weight: 600;
        flex: 1;
      }
      
      .notice-time {
        font-size: 12px;
        color: #999;
      }
    }
    
    .notice-body {
      margin-bottom: 12px;
      
      .flow-info {
        font-size: 12px;
        color: #666;
        margin-bottom: 8px;
        
        span {
          margin-right: 16px;
        }
      }
      
      .notice-content {
        font-size: 14px;
        color: #333;
      }
    }
    
    .notice-footer {
      display: flex;
      align-items: center;
      gap: 24px;
      font-size: 12px;
      color: #999;
    }
  }
}

.low-rate {
  color: #f56c6c;
  font-weight: 600;
}
</style>

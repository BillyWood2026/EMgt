<template>
  <div class="page-card">
    <div class="page-header">
      <h2>测评题库与测评模型配置</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新建模型
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
      title="测评题库与测评模型配置（对标北森六大测评体系）"
      description="内置CATA认知能力、GPI职场人格、职业锚、价值观动机、岗位胜任力、人才风险六大标准测评体系，支持维度权重、题库、CATA自适应规则、风控防作弊、岗位匹配阈值、报告模板全套EL扩展配置。"
      style="margin-bottom: 16px"
    />

    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="模型名称">
          <el-input v-model="searchForm.modelName" placeholder="请输入模型名称" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item label="测评类型">
          <el-select v-model="searchForm.assessType" placeholder="选择测评类型" style="width: 180px" clearable>
            <el-option v-for="t in assessTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用岗位序列">
          <el-select v-model="searchForm.position" placeholder="选择岗位序列" style="width: 160px" clearable>
            <el-option v-for="pos in positions" :key="pos" :label="pos" :value="pos" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 130px" clearable>
            <el-option label="启用" value="active" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="pagedList" border stripe size="small">
        <el-table-column prop="id" label="模型编码" width="120" />
        <el-table-column prop="modelName" label="模型名称" min-width="170">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row)">{{ row.modelName }}</el-link>
          </template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="assessType" label="测评类型" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="getAssessTypeColor(row.assessType)">{{ row.assessType || '岗位胜任力' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dimensions" label="测评维度" width="200">
          <template #default="{ row }">
            <el-tag v-for="dim in row.dimensions" :key="dim" :type="getDimensionType(dim)" size="small" style="margin-right: 4px">
              {{ dim }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="适用岗位序列" width="110" />
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="duration" label="测评时长" width="90">
          <template #default="{ row }">{{ row.duration ? row.duration + '分钟' : '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="questionCount" label="题目数量" width="90">
          <template #default="{ row }">{{ row.questionCount || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="scoringAlgo" label="计分算法" width="120">
          <template #default="{ row }">{{ row.scoringAlgo || '—' }}</template>
        </el-table-column>
        <!-- 规格书字段，待后端扩展 -->
        <el-table-column prop="antiCheatLevel" label="防作弊等级" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.antiCheatLevel === '高' ? 'danger' : row.antiCheatLevel === '中' ? 'warning' : 'info'">{{ row.antiCheatLevel || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalWeight" label="权重配置" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.totalWeight" :stroke-width="14" :text-inside="true" :status="row.totalWeight === 100 ? 'success' : 'exception'" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="110" />
        <el-table-column prop="status" label="模型状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">{{ row.status === 'active' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="viewDetail(row)">EL详情</el-button>
            <el-button link size="small" @click="editItem(row)">编辑</el-button>
            <el-button link size="small" @click="copyItem(row)">复制</el-button>
            <el-button link size="small" :type="row.status === 'active' ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
      />
    </el-card>

    <!-- 新增/编辑模型弹窗（规格书：920px，六标签页） -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="920px">
      <el-tabs v-model="dialogTab">
        <el-tab-pane label="基础信息" name="basic">
          <el-form :model="formData" label-width="110px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="模型编码">
                  <el-input v-model="formData.standardCode" placeholder="自动生成" readonly />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="模型名称" required>
                  <el-input v-model="formData.modelName" placeholder="请输入模型名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <!-- 规格书字段，待后端扩展 -->
                <el-form-item label="测评类型" required>
                  <el-select v-model="formData.assessType" placeholder="选择测评类型" style="width: 100%">
                    <el-option v-for="t in assessTypes" :key="t" :label="t" :value="t" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="适用岗位序列" required>
                  <el-select v-model="formData.position" placeholder="选择适用岗位序列" style="width: 100%">
                    <el-option v-for="pos in positions" :key="pos" :label="pos" :value="pos" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="测评维度" required>
                  <el-select v-model="formData.dimensions" multiple placeholder="选择测评维度" style="width: 100%">
                    <el-option v-for="d in dimensionOptions" :key="d" :label="d" :value="d" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评估周期">
                  <el-select v-model="formData.period" placeholder="选择评估周期" style="width: 100%">
                    <el-option label="半年度" value="半年度" />
                    <el-option label="年度" value="年度" />
                    <el-option label="季度" value="季度" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <!-- 规格书字段，待后端扩展 -->
              <el-col :span="8">
                <el-form-item label="测评时长(分)">
                  <el-input-number v-model="formData.duration" :min="5" :max="180" style="width: 100%" />
                </el-form-item>
              </el-col>
              <!-- 规格书字段，待后端扩展 -->
              <el-col :span="8">
                <el-form-item label="题目数量">
                  <el-input-number v-model="formData.questionCount" :min="1" :max="500" style="width: 100%" />
                </el-form-item>
              </el-col>
              <!-- 规格书字段，待后端扩展 -->
              <el-col :span="8">
                <el-form-item label="防作弊等级">
                  <el-select v-model="formData.antiCheatLevel" style="width: 100%">
                    <el-option label="高" value="高" />
                    <el-option label="中" value="中" />
                    <el-option label="低" value="低" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <!-- 规格书字段，待后端扩展 -->
              <el-col :span="12">
                <el-form-item label="计分算法类型">
                  <el-select v-model="formData.scoringAlgo" style="width: 100%">
                    <el-option label="CATA自适应(IRT)" value="CATA自适应(IRT)" />
                    <el-option label="GPI标准化百分制" value="GPI标准化百分制" />
                    <el-option label="迫选防伪装计分" value="迫选防伪装计分" />
                    <el-option label="维度加权计分" value="维度加权计分" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="创建人">
                  <el-input v-model="formData.creator" placeholder="请输入创建人" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="模型说明">
              <el-input type="textarea" v-model="formData.description" :rows="3" placeholder="请输入模型说明" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- EL-S01 测评维度权重配置（规格书字段，待后端扩展） -->
        <el-tab-pane label="维度权重配置" name="dimension">
          <el-table :data="formData.dimensionRows" border size="small">
            <el-table-column prop="name" label="维度名称" width="140">
              <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
            </el-table-column>
            <el-table-column prop="definition" label="维度定义" min-width="160">
              <template #default="{ row }"><el-input v-model="row.definition" size="small" /></template>
            </el-table-column>
            <el-table-column prop="fullScore" label="满分值" width="90">
              <template #default="{ row }"><el-input-number v-model="row.fullScore" :min="1" :max="100" size="small" style="width: 100%" /></template>
            </el-table-column>
            <el-table-column prop="weight" label="权重占比(%)" width="120">
              <template #default="{ row }"><el-input-number v-model="row.weight" :min="0" :max="100" size="small" style="width: 100%" /></template>
            </el-table-column>
            <el-table-column prop="coreVeto" label="核心否决项" width="90">
              <template #default="{ row }"><el-switch v-model="row.coreVeto" size="small" /></template>
            </el-table-column>
            <el-table-column prop="inMatch" label="纳入岗位匹配" width="100">
              <template #default="{ row }"><el-switch v-model="row.inMatch" size="small" /></template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template #default="{ $index }">
                <el-button link size="small" type="danger" @click="formData.dimensionRows.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 10px; display: flex; align-items: center; gap: 16px">
            <el-button size="small" @click="formData.dimensionRows.push({ name: '', definition: '', fullScore: 100, weight: 0, coreVeto: false, inMatch: true })">新增维度</el-button>
            <span :style="{ color: weightSum === 100 ? '#67c23a' : '#f56c6c' }">权重合计：{{ weightSum }}%（校验规则：权重总和非100%拦截提交）</span>
          </div>
        </el-tab-pane>

        <!-- EL-S02 题库配置（规格书字段，待后端扩展） -->
        <el-tab-pane label="题库配置" name="question">
          <el-form label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="支持题型">
                  <el-checkbox-group v-model="formData.questionTypes">
                    <el-checkbox label="单选" /><el-checkbox label="多选" />
                    <el-checkbox label="迫选" /><el-checkbox label="情景题" />
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="难度系数区间">
                  <el-input v-model="formData.difficultyRange" placeholder="如 0.3-0.9（超区间拦截）" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="迫选防伪装">
                  <el-switch v-model="formData.forcedChoice" active-text="启用二难迫选（北森同款，无绝对正确答案）" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="题目启用校验">
                  <el-switch v-model="formData.questionAudit" active-text="题目上线前审核" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- EL-S03 CATA自适应规则（规格书字段，待后端扩展） -->
        <el-tab-pane label="CATA自适应规则" name="cata">
          <el-form label-width="160px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="初始难度">
                  <el-input-number v-model="formData.cataInitDifficulty" :min="0" :max="1" :step="0.1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="答对难度递增系数">
                  <el-input-number v-model="formData.cataUpFactor" :min="0" :max="1" :step="0.05" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="答错难度递减系数">
                  <el-input-number v-model="formData.cataDownFactor" :min="0" :max="1" :step="0.05" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最短作答时长(秒)">
                  <el-input-number v-model="formData.cataMinDuration" :min="1" :max="300" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="题目自适应数量">
                  <el-input-number v-model="formData.cataQuestionCount" :min="5" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="终止测评置信度阈值">
                  <el-input-number v-model="formData.cataStopThreshold" :min="0.5" :max="0.99" :step="0.01" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-alert type="info" :closable="false" show-icon title="算法逻辑：系统实时根据正确率动态出题，高能力推送难题、低能力推送基础题，达到置信度阈值即终止测评，短时间精准定位能力区间。" />
          </el-form>
        </el-tab-pane>

        <!-- EL-S04 风控防作弊规则（规格书字段，待后端扩展） -->
        <el-tab-pane label="风控规则" name="risk">
          <el-form label-width="160px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="允许切屏次数">
                  <el-input-number v-model="formData.maxScreenSwitch" :min="0" :max="20" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最小单题时长(秒)">
                  <el-input-number v-model="formData.minQuestionSeconds" :min="1" :max="120" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最大连续秒答题数">
                  <el-input-number v-model="formData.maxFlashAnswers" :min="1" :max="50" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="异常作答判定阈值">
                  <el-input-number v-model="formData.abnormalThreshold" :min="1" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8"><el-form-item label="IP异地风控"><el-switch v-model="formData.ipRiskControl" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="设备唯一校验"><el-switch v-model="formData.deviceUniqueCheck" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="规律作答识别"><el-switch v-model="formData.patternDetect" /></el-form-item></el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- EL-S05 岗位匹配标准阈值（规格书字段，待后端扩展） -->
        <el-tab-pane label="匹配阈值" name="threshold">
          <el-form label-width="140px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="各维度合格分数线">
                  <el-input-number v-model="formData.passScore" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="优秀分数线">
                  <el-input-number v-model="formData.excellentScore" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="风险红线">
                  <el-input-number v-model="formData.riskRedLine" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="高潜判定规则">
                  <el-input v-model="formData.highPotentialRule" placeholder="如 综合分≥85 且 无风险红线" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- EL-S06 测评报告模板配置（规格书字段，待后端扩展） -->
        <el-tab-pane label="报告模板" name="report">
          <el-form label-width="160px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="报告版式">
                  <el-select v-model="formData.reportLayout" style="width: 100%">
                    <el-option label="标准版" value="标准版" />
                    <el-option label="专业版(含维度雷达)" value="专业版" />
                    <el-option label="高管摘要版" value="高管摘要版" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="维度雷达图开关">
                  <el-switch v-model="formData.reportRadar" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="风险提示文案">
              <el-input v-model="formData.reportRiskText" placeholder="风险提示默认文案" />
            </el-form-item>
            <el-form-item label="发展建议模板">
              <el-input type="textarea" v-model="formData.reportSuggestTpl" :rows="2" placeholder="发展建议模板" />
            </el-form-item>
            <el-form-item label="展示原始作答记录">
              <el-switch v-model="formData.reportShowRaw" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>

    <!-- EL详情抽屉：维度权重雷达图 + EL子集摘要 -->
    <el-drawer v-model="drawerVisible" :title="`EL模型配置详情：${detailRow.modelName || ''}`" size="520px">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 12px">
        <el-descriptions-item label="模型编码">{{ detailRow.id }}</el-descriptions-item>
        <el-descriptions-item label="测评类型">{{ detailRow.assessType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="测评时长">{{ detailRow.duration ? detailRow.duration + '分钟' : '—' }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ detailRow.questionCount || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计分算法">{{ detailRow.scoringAlgo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="防作弊等级">{{ detailRow.antiCheatLevel || '—' }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 8px 0">维度权重雷达图（权重参与综合匹配得分、潜力得分、风险指数计算）</h4>
      <div ref="radarChartRef" style="width: 100%; height: 300px"></div>
      <el-alert type="info" :closable="false" show-icon style="margin-top: 8px"
        title="EL-S07至EL-S12六类测评专项维度扩展（认知能力/人格特质/职业锚/价值观动机/胜任力/风险）支持企业个性化新增维度，规格书字段，待后端扩展。" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/api/axiosConfig'

const assessTypes = ['CATA认知能力', 'GPI职场人格', '职业倾向与职业锚', '工作价值观与动机', '岗位胜任力专项', '人才风险']
const positions = ['硬件研发', '嵌入式软件', '结构设计', '工艺工程', '品质管控', '设备运维', '生产管理', '供应链', '售后', '职能']
const dimensionOptions = ['业绩', '能力', '潜力', '价值观', '领导力', '言语理解', '数理逻辑', '抽象推理', '思维敏捷度', '情绪稳定', '尽责自律', '外向协作', '开放创新', '抗压韧性', '执行力', '沟通协作', '风险把控', '客户导向', '艰苦奋斗']

const searchForm = reactive({ modelName: '', assessType: '', position: '', status: '' })
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新建模型')
const dialogTab = ref('basic')
const drawerVisible = ref(false)
const detailRow = ref({})
const radarChartRef = ref(null)
let radarIns = null

const defaultForm = {
  id: '', standardCode: '', modelName: '', assessType: '岗位胜任力专项', dimensions: [], totalWeight: 100,
  position: '', period: '年度', creator: '', createTime: '', description: '', status: 'active',
  duration: 45, questionCount: 60, scoringAlgo: '维度加权计分', antiCheatLevel: '中',
  dimensionRows: [], questionTypes: ['单选'], difficultyRange: '0.3-0.9', forcedChoice: false, questionAudit: true,
  cataInitDifficulty: 0.5, cataUpFactor: 0.15, cataDownFactor: 0.15, cataMinDuration: 10, cataQuestionCount: 30, cataStopThreshold: 0.9,
  maxScreenSwitch: 3, minQuestionSeconds: 5, maxFlashAnswers: 5, abnormalThreshold: 10,
  ipRiskControl: true, deviceUniqueCheck: true, patternDetect: true,
  passScore: 60, excellentScore: 85, riskRedLine: 70, highPotentialRule: '',
  reportLayout: '标准版', reportRadar: true, reportRiskText: '', reportSuggestTpl: '', reportShowRaw: false
}
const formData = reactive({ ...defaultForm, dimensionRows: [], questionTypes: ['单选'] })
const modelList = ref([])

const weightSum = computed(() => formData.dimensionRows.reduce((s, r) => s + (Number(r.weight) || 0), 0))

const filteredList = computed(() => {
  let list = [...modelList.value]
  if (searchForm.modelName) list = list.filter(m => m.modelName?.includes(searchForm.modelName))
  if (searchForm.assessType) list = list.filter(m => m.assessType === searchForm.assessType)
  if (searchForm.position) list = list.filter(m => m.position === searchForm.position)
  if (searchForm.status) list = list.filter(m => m.status === searchForm.status)
  return list
})
const pagedList = computed(() => {
  pagination.total = filteredList.value.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

function getDimensionType(dim) { const types = { '业绩': 'danger', '能力': 'primary', '潜力': 'success', '价值观': 'warning', '领导力': 'info' }; return types[dim] || 'info' }
function getAssessTypeColor(t) {
  const types = { 'CATA认知能力': 'primary', 'GPI职场人格': 'success', '职业倾向与职业锚': 'warning', '工作价值观与动机': 'danger', '岗位胜任力专项': '', '人才风险': 'info' }
  return types[t] || ''
}

function parseDimensionRows(r) {
  // 后端暂无维度权重子集字段，从扩展字段(dimWeightJson)解析；无则按维度均分权重生成展示行
  if (r.dimWeightJson) {
    try { return JSON.parse(r.dimWeightJson) } catch (e) { /* 忽略解析失败 */ }
  }
  const dims = r.positionSequences ? r.positionSequences.split(',') : []
  if (!dims.length) return []
  const w = Math.floor(100 / dims.length)
  return dims.map((d, i) => ({ name: d, definition: '', fullScore: 100, weight: i === dims.length - 1 ? 100 - w * (dims.length - 1) : w, coreVeto: false, inMatch: true }))
}

async function loadModels() {
  try {
    const { data } = await request.get('/api/evaluation/standard/search')
    const list = data?.data || data || []
    modelList.value = list.map(r => {
      const dimRows = parseDimensionRows(r)
      return {
        id: r.standardCode || r.id,
        standardCode: r.standardCode || '',
        modelName: r.standardName || '',
        assessType: r.assessType || '',
        dimensions: r.positionSequences ? r.positionSequences.split(',') : [],
        dimensionRows: dimRows,
        totalWeight: dimRows.length ? dimRows.reduce((s, x) => s + (Number(x.weight) || 0), 0) : 100,
        position: r.positionFamily || '',
        period: r.periodType || '年度',
        creator: r.creator || '',
        createTime: r.createTime ? String(r.createTime).slice(0, 10) : '',
        description: r.description || '',
        duration: r.duration, questionCount: r.questionCount, scoringAlgo: r.scoringAlgo, antiCheatLevel: r.antiCheatLevel,
        status: r.status === 'ACTIVE' ? 'active' : 'disabled'
      }
    })
    pagination.total = modelList.value.length
  } catch (e) {
    ElMessage.error('模型列表加载失败：' + (e.message || ''))
  }
}

function doSearch() { pagination.currentPage = 1 }
function resetSearch() { searchForm.modelName = ''; searchForm.assessType = ''; searchForm.position = ''; searchForm.status = ''; pagination.currentPage = 1 }
function refreshData() { loadModels(); ElMessage.success('数据已刷新') }

function openCreate() {
  dialogTitle.value = '新建模型'
  dialogTab.value = 'basic'
  Object.assign(formData, JSON.parse(JSON.stringify(defaultForm)), { standardCode: 'MDL' + String(Date.now()).slice(-6), id: '' })
  dialogVisible.value = true
}

function copyItem(row) {
  dialogTitle.value = '复制模型'
  dialogTab.value = 'basic'
  Object.assign(formData, JSON.parse(JSON.stringify({ ...defaultForm, ...row })), {
    id: '', standardCode: 'MDL' + String(Date.now()).slice(-6), modelName: row.modelName + '-副本', status: 'disabled'
  })
  dialogVisible.value = true
}

function viewDetail(row) {
  detailRow.value = row
  drawerVisible.value = true
  nextTick(() => drawRadar(row))
}

function drawRadar(row) {
  if (!radarChartRef.value) return
  radarIns = radarIns || echarts.init(radarChartRef.value)
  radarIns.resize()
  const dims = (row.dimensionRows && row.dimensionRows.length)
    ? row.dimensionRows
    : (row.dimensions || []).map(d => ({ name: d, weight: Math.round(100 / (row.dimensions.length || 1)) }))
  if (!dims.length) { radarIns.clear(); return }
  radarIns.setOption({
    tooltip: {},
    radar: {
      indicator: dims.map(d => ({ name: d.name, max: Math.max(60, ...dims.map(x => x.weight || 0)) })),
      radius: '62%'
    },
    series: [{
      type: 'radar',
      data: [{ value: dims.map(d => d.weight || 0), name: '权重占比(%)', areaStyle: { opacity: 0.2 } }]
    }]
  })
}

function editItem(row) {
  dialogTitle.value = '编辑模型'
  dialogTab.value = 'basic'
  Object.assign(formData, JSON.parse(JSON.stringify({ ...defaultForm, ...row })), { standardCode: row.id })
  dialogVisible.value = true
}

async function toggleStatus(row) {
  const target = row.status === 'active' ? 'INACTIVE' : 'ACTIVE'
  try {
    await request.put('/api/evaluation/standard', {
      standardCode: row.id, standardName: row.modelName, positionFamily: row.position,
      positionSequences: (row.dimensions || []).join(','), periodType: row.period,
      description: row.description, status: target
    })
    ElMessage.success(target === 'ACTIVE' ? '模型已启用' : '模型已停用')
    loadModels()
  } catch (e) {
    ElMessage.error('状态切换失败：' + (e.response?.data?.message || e.message || ''))
  }
}

async function saveData() {
  if (!formData.modelName || !formData.position) { ElMessage.warning('请填写模型名称和适用岗位序列'); return }
  if (formData.dimensionRows.length && weightSum.value !== 100) {
    ElMessage.error('维度权重合计必须为100%，当前为' + weightSum.value + '%，已拦截提交'); return
  }
  try {
    const payload = {
      standardCode: formData.standardCode,
      standardName: formData.modelName,
      positionFamily: formData.position,
      positionSequences: (formData.dimensions || []).join(','),
      periodType: formData.period,
      description: formData.description,
      creator: formData.creator,
      status: formData.status === 'active' ? 'ACTIVE' : 'INACTIVE',
      // 规格书字段，待后端扩展（后端多字段不报错，随提交携带）
      assessType: formData.assessType,
      duration: formData.duration,
      questionCount: formData.questionCount,
      scoringAlgo: formData.scoringAlgo,
      antiCheatLevel: formData.antiCheatLevel,
      dimWeightJson: JSON.stringify(formData.dimensionRows || [])
    }
    if (formData.id && formData.id !== formData.standardCode) {
      await request.put('/api/evaluation/standard', payload)
    } else if (formData.id) {
      await request.put('/api/evaluation/standard', payload)
    } else {
      await request.post('/api/evaluation/standard', payload)
    }
    dialogVisible.value = false
    ElMessage.success(formData.id ? '评估模型已更新' : '评估模型新增成功')
    loadModels()
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadModels())
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; gap: 8px; }
.search-form { margin-bottom: 0; }
</style>

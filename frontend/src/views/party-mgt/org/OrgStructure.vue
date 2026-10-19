<template>
  <div class="page-container">
    <RoleBar>
      <el-tag v-if="alerts.near?.length" type="warning" size="small">临近届满 {{ alerts.near.length }}</el-tag>
      <el-tag v-if="alerts.expired?.length" type="danger" size="small">超期未换 {{ alerts.expired.length }}</el-tag>
    </RoleBar>

    <el-row :gutter="12">
      <!-- 左：组织树 -->
      <el-col :span="9" :xs="24">
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div class="tree-toolbar">
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button value="tree">组织树</el-radio-button>
                <el-radio-button value="list">同级列表</el-radio-button>
              </el-radio-group>
              <div>
                <el-button v-if="can('R02')" size="small" type="primary" @click="openForm()">新建</el-button>
                <el-button v-if="can('R02','R03')" size="small" @click="downloadExport">导出</el-button>
                <el-button v-if="can('R02')" size="small" @click="importVisible = true">导入</el-button>
                <el-button v-if="can('R03')" size="small" type="warning" @click="openPending">待审批
                  <el-badge v-if="pendingCount" :value="pendingCount" class="pending-badge" />
                </el-button>
              </div>
            </div>
            <div class="tree-filter">
              <el-input v-model="keyword" size="small" placeholder="搜索组织名称/编码" clearable
                        style="width:55%" @keyup.enter="reloadTree">
                <template #append><el-button @click="reloadTree">搜索</el-button></template>
              </el-input>
              <el-select v-model="orgType" size="small" placeholder="类型" clearable
                         style="width:42%" @change="reloadTree">
                <el-option v-for="t in ORG_TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </div>
          </template>

          <el-tree
            v-if="viewMode === 'tree'"
            :key="treeKey"
            :data="treeData"
            :props="{ label: 'name', children: 'children', isLeaf: d => !d.hasChildren }"
            node-key="id"
            lazy
            :load="loadNode"
            :expand-on-click-node="false"
            :draggable="can('R02')"
            :allow-drop="allowDrop"
            @node-click="onNodeClick"
            @node-drop="onNodeDrop"
            highlight-current
          >
            <template #default="{ data }">
              <span class="tree-node">
                <span class="node-icon">{{ typeIcon(data.orgType) }}</span>
                <span class="node-name">{{ data.name }}</span>
                <el-tag size="small" effect="plain" class="node-count">{{ data.memberCount || 0 }}人</el-tag>
                <el-tooltip v-if="data.termStatus === 'NEAR'" content="临近届满（≤180天）" placement="top">
                  <span class="dot dot-warning" />
                </el-tooltip>
                <el-tooltip v-else-if="data.termStatus === 'EXPIRED'" content="超期未换届" placement="top">
                  <span class="dot dot-danger" />
                </el-tooltip>
                <el-tooltip v-if="data.verifyFlag === 1" content="数据预警：信息待核实" placement="top">
                  <span class="dot dot-info" />
                </el-tooltip>
                <el-tag v-if="data.status === '筹备'" size="small" type="info">筹备</el-tag>
                <el-tag v-if="data.status === '撤销'" size="small" type="danger">撤销</el-tag>
              </span>
            </template>
          </el-tree>

          <div v-else>
            <el-table :data="siblingRows" size="small" border @row-click="onNodeClick"
                      highlight-current-row v-loading="listLoading" height="480">
              <el-table-column prop="orgCode" label="编码" width="90" />
              <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="orgType" label="类型" width="90" />
              <el-table-column prop="memberCount" label="党员" width="60" />
              <el-table-column label="状态" width="70">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.status === '正常' ? 'success' : 'info'">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination small layout="prev, pager, next" :total="siblingTotal"
                           :page-size="listPageSize" :current-page="listPageNum"
                           @current-change="onListPage" class="list-pager" />
          </div>
        </el-card>
      </el-col>

      <!-- 右：详情 -->
      <el-col :span="15" :xs="24">
        <el-card v-if="!detail" shadow="never" class="detail-empty">
          <el-empty description="请选择左侧党组织查看详情" />
        </el-card>
        <el-card v-else shadow="never">
          <template #header>
            <div class="detail-header">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item v-for="(b, i) in detail.breadcrumb" :key="b.id">
                  <a v-if="i < detail.breadcrumb.length - 1" @click="loadDetail(b.id)">{{ b.name }}</a>
                  <span v-else>{{ b.name }}</span>
                </el-breadcrumb-item>
              </el-breadcrumb>
              <div>
                <el-button v-if="can('R03') && detail.org.auditStatus === 'PENDING'"
                           size="small" type="warning" @click="openApprove">批复</el-button>
                <el-button v-if="can('R02')" size="small" @click="openForm(detail.org)">编辑</el-button>
                <el-button v-if="can('R02','R07')" size="small" @click="groupsVisible = true">党小组设置</el-button>
                <el-button v-if="can('R02') && detail.org.status !== '撤销'" size="small" type="danger" plain
                           @click="openMerge">合并</el-button>
                <el-button v-if="can('R02') && detail.org.status !== '撤销'" size="small" type="danger"
                           @click="openRevoke">撤销</el-button>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="base">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="组织编码">{{ detail.org.orgCode }}</el-descriptions-item>
                <el-descriptions-item label="组织类型">{{ detail.org.orgType }}</el-descriptions-item>
                <el-descriptions-item label="简称">{{ detail.org.shortName }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag size="small" :type="detail.org.status === '正常' ? 'success' : 'info'">{{ detail.org.status }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="书记">{{ detail.org.secretary }}</el-descriptions-item>
                <el-descriptions-item label="副书记">{{ detail.org.deputySecretary }}</el-descriptions-item>
                <el-descriptions-item label="委员" :span="2">{{ detail.org.committeeMembers }}</el-descriptions-item>
                <el-descriptions-item label="成立日期">{{ detail.org.establishedDate }}</el-descriptions-item>
                <el-descriptions-item label="批复文号">{{ detail.org.approvalNo }}</el-descriptions-item>
                <el-descriptions-item label="本届任期">
                  第{{ detail.org.termNo || '—' }}届 {{ detail.org.termStart || '—' }} ~ {{ detail.org.termEnd || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="届满状态">
                  <el-tag size="small" :type="termTagType(detail.org.termStatus)">
                    {{ termLabel(detail.org.termStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="是否临时支部">{{ detail.org.isFloating ? '是' : '否' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ detail.org.contactPhone }}</el-descriptions-item>
                <el-descriptions-item label="地址" :span="2">{{ detail.org.address }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ detail.org.remark }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <el-tab-pane :label="`党员(${detail.memberCount})`" name="members">
              <el-table :data="detail.members" size="small" border max-height="420">
                <el-table-column prop="name" label="姓名" width="90" />
                <el-table-column prop="memberStatus" label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.memberStatus === '正式党员' ? 'success' : 'warning'">
                      {{ row.memberStatus }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="gender" label="性别" width="60" />
                <el-table-column prop="phone" label="电话" width="130" />
                <el-table-column prop="joinDate" label="入党日期" width="120" />
                <el-table-column label="表决权" width="80">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.votingRightSuspended ? 'danger' : 'success'">
                      {{ row.votingRightSuspended ? '暂停' : '有' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <p class="tab-tip">正式党员 {{ detail.formalCount }} 人；表决人数自动排除表决权暂停人员。</p>
            </el-tab-pane>

            <el-tab-pane label="班子与党小组" name="leader">
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="书记">{{ detail.org.secretary || '—' }}</el-descriptions-item>
                <el-descriptions-item label="副书记">{{ detail.org.deputySecretary || '—' }}</el-descriptions-item>
                <el-descriptions-item label="委员">{{ detail.org.committeeMembers || '—' }}</el-descriptions-item>
                <el-descriptions-item label="党小组设置">
                  <span style="white-space:pre-wrap">{{ detail.org.partyGroups || '未设置' }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <el-tab-pane label="最近换届" name="election">
              <el-descriptions v-if="detail.latestElection" :column="2" border size="small">
                <el-descriptions-item label="选举方式">{{ detail.latestElection.electionMode }}</el-descriptions-item>
                <el-descriptions-item label="阶段">{{ stageLabel(detail.latestElection.stage) }}</el-descriptions-item>
                <el-descriptions-item label="应到会">{{ detail.latestElection.shouldVoteCount }}</el-descriptions-item>
                <el-descriptions-item label="实到会">{{ detail.latestElection.actualCount }}</el-descriptions-item>
                <el-descriptions-item label="批复文号">{{ detail.latestElection.approvalNo }}</el-descriptions-item>
                <el-descriptions-item label="备案日期">{{ detail.latestElection.recordDate }}</el-descriptions-item>
              </el-descriptions>
              <el-empty v-else description="暂无换届记录" :image-size="80" />
            </el-tab-pane>

            <el-tab-pane label="组织生活(本年)" name="life">
              <el-table :data="lifeRows" size="small" border>
                <el-table-column prop="type" label="会议类型" />
                <el-table-column prop="required" label="年度要求" width="100" />
                <el-table-column prop="held" label="已召开" width="100" />
                <el-table-column label="状态">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.held >= row.required ? 'success' : 'danger'">
                      {{ row.held >= row.required ? '达标' : '应开未开' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="历史沿革" name="history">
              <el-timeline>
                <el-timeline-item v-for="h in detail.histories" :key="h.id"
                                  :timestamp="`${h.changeDate || ''} ${h.changeType || ''}`" placement="top">
                  <span style="white-space:pre-wrap">{{ h.changeContent }}</span>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-if="!detail.histories?.length" description="暂无沿革记录" :image-size="80" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建/编辑组织 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑党组织' : '新建党组织'" width="720px"
               :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="组织名称" required><el-input v-model="form.name" maxlength="50" show-word-limit /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织类型" required>
              <el-select v-model="form.orgType" style="width:100%">
                <el-option v-for="t in ORG_TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上级组织">
              <el-tree-select v-model="form.parentId" :data="parentOptions" :props="{ label: 'name', value: 'id' }"
                              check-strictly clearable style="width:100%" placeholder="不选则为顶级党委" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期" required>
              <el-date-picker v-model="form.establishedDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="书记"><el-input v-model="form.secretary" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="副书记"><el-input v-model="form.deputySecretary" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="临时支部">
              <el-switch v-model="form.isFloating" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="委员名单"><el-input v-model="form.committeeMembers" placeholder="多人用顿号/逗号分隔" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任期开始"><el-date-picker v-model="form.termStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任期结束"><el-date-picker v-model="form.termEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24" v-if="!form.id">
            <el-form-item label="直接生效">
              <el-switch v-model="directApprove" active-text="勾选后直接生效（无需上级批复）" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="warnings.length">
            <el-alert v-for="(w, i) in warnings" :key="i" :title="w" type="warning" :closable="false" show-icon class="warn-item" />
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批复 -->
    <el-dialog v-model="approveVisible" title="上级批复" width="420px">
      <el-form label-width="90px">
        <el-form-item label="批复文号" required><el-input v-model="approvalNo" placeholder="如：××党发〔2026〕12号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">批复生效</el-button>
      </template>
    </el-dialog>

    <!-- 党小组设置 -->
    <el-dialog v-model="groupsVisible" title="党小组设置" width="520px">
      <el-input v-model="groupsText" type="textarea" :rows="6"
                placeholder="每行一个党小组，如：&#10;第一党小组（组长：××，成员：××、××）" />
      <template #footer>
        <el-button @click="groupsVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGroups">保存</el-button>
      </template>
    </el-dialog>

    <!-- 合并 -->
    <el-dialog v-model="mergeVisible" title="党组织合并影响分析" width="640px">
      <el-form label-width="100px">
        <el-form-item label="被合并组织">{{ impact.sourceOrg?.name }}</el-form-item>
        <el-form-item label="并入目标组织" required>
          <el-tree-select v-model="mergeTargetId" :data="parentOptions" :props="{ label: 'name', value: 'id' }"
                          check-strictly style="width:100%" @change="runMergeAnalyze" />
        </el-form-item>
        <el-form-item label="合并原因"><el-input v-model="mergeReason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <el-descriptions v-if="impact" :column="2" border size="small">
        <el-descriptions-item label="影响党员">{{ impact.affectedMembers }} 人（将整体迁移）</el-descriptions-item>
        <el-descriptions-item label="下级组织">{{ impact.childOrgCount }} 个</el-descriptions-item>
        <el-descriptions-item label="在办事项">{{ impact.pendingFlows }} 项</el-descriptions-item>
        <el-descriptions-item label="未结党费">{{ impact.unpaidDues }} 人次</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="impact?.blocked" type="error" show-icon :closable="false" class="warn-item"
                :title="'存在阻断项：' + (impact.blockerDetails || []).join('；')" />
      <template #footer>
        <el-button @click="mergeVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!mergeTargetId || impact?.blocked" @click="executeMerge">执行合并</el-button>
      </template>
    </el-dialog>

    <!-- 撤销 -->
    <el-dialog v-model="revokeVisible" title="党组织撤销影响分析" width="640px">
      <el-form label-width="100px">
        <el-form-item label="撤销组织">{{ impact.sourceOrg?.name }}</el-form-item>
        <el-form-item label="撤销原因" required><el-input v-model="revokeReason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <el-descriptions v-if="impact" :column="2" border size="small">
        <el-descriptions-item label="在册党员">{{ impact.affectedMembers }} 人（须先转出）</el-descriptions-item>
        <el-descriptions-item label="下级组织">{{ impact.childOrgCount }} 个</el-descriptions-item>
        <el-descriptions-item label="在办事项">{{ impact.pendingFlows }} 项</el-descriptions-item>
        <el-descriptions-item label="未结党费">{{ impact.unpaidDues }} 人次</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="impact?.blocked" type="error" show-icon :closable="false" class="warn-item"
                :title="'存在阻断项：' + (impact.blockerDetails || []).join('；')" />
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button type="danger" :disabled="impact?.blocked || !revokeReason" @click="executeRevoke">执行撤销</el-button>
      </template>
    </el-dialog>

    <!-- 导入 -->
    <el-dialog v-model="importVisible" title="批量导入党组织" width="820px">
      <div class="import-bar">
        <el-button size="small" @click="downloadTemplate">下载导入模板</el-button>
        <el-upload :show-file-list="false" :before-upload="beforeUpload" accept=".xlsx">
          <el-button size="small" type="primary">选择 Excel 预览</el-button>
        </el-upload>
      </div>
      <el-table :data="previewRows" size="small" border max-height="380" class="import-table">
        <el-table-column label="#" type="index" width="44" />
        <el-table-column prop="name" label="组织名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="orgType" label="类型" width="90" />
        <el-table-column prop="parentCode" label="上级编码" width="90" />
        <el-table-column prop="establishedDate" label="成立日期" width="110" />
        <el-table-column label="校验">
          <template #default="{ row }">
            <el-tag v-if="!row.errors || !row.errors.length" size="small" type="success">通过</el-tag>
            <el-tooltip v-else :content="row.errors.join('；')">
              <el-tag size="small" type="danger">错误{{ row.errors.length }}</el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <p class="tab-tip" v-if="previewRows.length">
        共 {{ previewRows.length }} 行，错误 {{ previewRows.filter(r => r.errors?.length).length }} 行；仅提交校验通过行。
      </p>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!validPreviewCount" @click="commitImport">
          提交导入（{{ validPreviewCount }} 行）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const API = '/api/party/org/organization'
const ORG_TYPES = ['党委', '党总支', '党支部', '临时党支部']

const treeData = ref([])
const treeKey = ref(0)
const keyword = ref('')
const orgType = ref('')
const viewMode = ref('tree')
const detail = ref(null)
const activeTab = ref('base')
const alerts = ref({ near: [], expired: [] })
const pendingCount = ref(0)

// ---------- 树 ----------
async function loadNode(node, resolve) {
  const parentId = node.level === 0 ? undefined : node.data.id
  try {
    const res = await axios.get(`${API}/tree`, {
      params: { parentId, keyword: keyword.value || undefined, orgType: orgType.value || undefined }
    })
    resolve(res.data.code === 200 ? res.data.data : [])
  } catch {
    resolve([])
  }
}

function reloadTree() {
  treeKey.value++
}

async function onNodeClick(data) {
  if (data.id) await loadDetail(data.id)
}

function allowDrop(dragging, dropNode, type) {
  // 仅允许挂入某节点内部；不允许同级排序式移动歧义
  return type === 'inner' && dropNode.data.status !== '撤销'
}

async function onNodeDrop(draggingNode, dropNode, dropType) {
  const id = draggingNode.data.id
  const newParentId = dropType === 'inner' ? dropNode.data.id : dropNode.data.parentId
  try {
    await ElMessageBox.confirm(
      `确认将「${draggingNode.data.name}」移动到「${dropType === 'inner' ? dropNode.data.name : '同级'}」下？组织编码将自动重编。`,
      '移动组织确认', { type: 'warning' })
    await axios.post(`${API}/move`, { id, newParentId: newParentId ?? null })
    ElMessage.success('移动成功，编码已重编')
    reloadTree(); if (detail.value) loadDetail(id)
  } catch {
    reloadTree()
  }
}

// ---------- 同级列表 ----------
const siblingRows = ref([])
const siblingTotal = ref(0)
const listPageNum = ref(1)
const listPageSize = ref(20)
const listLoading = ref(false)

async function loadSiblings() {
  if (viewMode.value !== 'list') return
  listLoading.value = true
  try {
    const parentId = detail.value ? detail.org.parentId : undefined
    const res = await axios.get(`${API}/list-page`, {
      params: { parentId: parentId ?? null, pageNum: listPageNum.value, pageSize: listPageSize.value,
                 keyword: keyword.value || undefined }
    })
    if (res.data.code === 200) {
      siblingRows.value = res.data.data.records || []
      siblingTotal.value = res.data.data.total || 0
    }
  } finally {
    listLoading.value = false
  }
}

function onListPage(p) {
  listPageNum.value = p
  loadSiblings()
}

// ---------- 详情 ----------
async function loadDetail(id) {
  const res = await axios.get(`${API}/detail/${id}`)
  if (res.data.code === 200) {
    detail.value = res.data.data
    if (viewMode.value === 'list') loadSiblings()
  }
}

const lifeRows = computed(() => {
  const s = detail.value?.lifeStats || {}
  return ['民主生活会', '组织生活会'].map(t => ({
    type: t, required: s[t + '_要求'] || 1, held: s[t + '_已开'] || 0
  }))
})

function termLabel(s) {
  return { NORMAL: '正常', NEAR: '临近届满', EXPIRED: '超期未换' }[s] || '正常'
}
function termTagType(s) {
  return { NORMAL: 'success', NEAR: 'warning', EXPIRED: 'danger' }[s] || 'success'
}
function stageLabel(s) {
  return { PLAN: '方案', CANDIDATE: '候选人', MEETING: '会议计票', FILED: '已备案' }[s] || (s || '方案')
}
function typeIcon(t) {
  return { '党委': '🏛', '党总支': '🏢', '党支部': '🏠', '临时党支部': '⛺' }[t] || '🏢'
}

// ---------- 新建/编辑 ----------
const formVisible = ref(false)
const form = reactive({})
const directApprove = ref(true)
const warnings = ref([])
const parentOptions = ref([])

function resetForm() {
  Object.keys(form).forEach(k => delete form[k])
  warnings.value = []
}

async function openForm(org) {
  resetForm()
  const tree = await axios.get(`${API}/tree`)
  parentOptions.value = tree.data.code === 200 ? tree.data.data : []
  if (org) {
    Object.assign(form, JSON.parse(JSON.stringify(org)))
    form.isFloating = org.isFloating || 0
  } else {
    form.parentId = detail.value ? detail.org.id : null
    form.orgType = '党支部'
    form.isFloating = 0
    directApprove.value = true
  }
  formVisible.value = true
}

async function submitForm() {
  try {
    if (form.id) {
      const res = await axios.put(`${API}/update`, form)
      if (res.data.code === 200) {
        ElMessage.success('修改成功')
        formVisible.value = false
        reloadTree(); loadDetail(form.id)
      }
    } else {
      const res = await axios.post(`${API}/save?directApprove=${directApprove.value}`, form)
      if (res.data.code === 200) {
        warnings.value = res.data.data.warnings || []
        if (warnings.value.length) {
          ElMessageBox.alert(warnings.value.join('\n'), '数据预警（已保存待核实）', { type: 'warning' })
        } else {
          ElMessage.success(directApprove.value ? '新建成功，已生效' : '新建成功，待上级批复')
        }
        formVisible.value = false
        reloadTree()
        loadPendingCount()
      }
    }
  } catch { /* 拦截器已提示 */ }
}

// ---------- 批复 ----------
const approveVisible = ref(false)
const approvalNo = ref('')
function openApprove() {
  approvalNo.value = ''
  approveVisible.value = true
}
async function submitApprove() {
  if (!approvalNo.value) { ElMessage.warning('请填写批复文号'); return }
  await axios.post(`${API}/approve/${detail.value.org.id}`, { approvalNo: approvalNo.value })
  ElMessage.success('批复成功，组织已生效')
  approveVisible.value = false
  reloadTree(); loadDetail(detail.value.org.id); loadPendingCount()
}

async function loadPendingCount() {
  if (!can('R03')) return
  const res = await axios.get(`${API}/pending`)
  pendingCount.value = res.data.code === 200 ? res.data.data.length : 0
}

async function openPending() {
  const res = await axios.get(`${API}/pending`)
  const list = res.data.data || []
  if (!list.length) { ElMessage.info('暂无待审批组织'); return }
  await ElMessageBox.alert(list.map(o => `${o.orgCode} ${o.name}`).join('\n'), '待审批党组织')
  if (detail.value) loadDetail(detail.value.org.id)
}

// ---------- 党小组 ----------
const groupsVisible = ref(false)
const groupsText = ref('')
watch(groupsVisible, v => {
  if (v && detail.value) groupsText.value = detail.value.org.partyGroups || ''
})
async function submitGroups() {
  await axios.put(`${API}/party-groups/${detail.value.org.id}`, { partyGroups: groupsText.value })
  ElMessage.success('党小组设置已保存')
  groupsVisible.value = false
  loadDetail(detail.value.org.id)
}

// ---------- 合并 / 撤销 ----------
const mergeVisible = ref(false)
const revokeVisible = ref(false)
const mergeTargetId = ref(null)
const mergeReason = ref('')
const revokeReason = ref('')
const impact = ref(null)

async function openMerge() {
  impact.value = null
  mergeTargetId.value = null
  mergeReason.value = ''
  const tree = await axios.get(`${API}/tree`)
  parentOptions.value = tree.data.code === 200 ? tree.data.data : []
  mergeVisible.value = true
}

async function runMergeAnalyze() {
  if (!mergeTargetId.value) return
  const res = await axios.post(`${API}/merge/analyze`, {
    sourceOrgId: detail.value.org.id, targetOrgId: mergeTargetId.value
  })
  impact.value = res.data.data
}

async function executeMerge() {
  await axios.post(`${API}/merge/execute`, {
    sourceOrgId: detail.value.org.id, targetOrgId: mergeTargetId.value, reason: mergeReason.value
  })
  ElMessage.success('合并完成，党员已迁移，组织已撤销')
  mergeVisible.value = false
  detail.value = null
  reloadTree()
}

async function openRevoke() {
  impact.value = null
  revokeReason.value = ''
  const res = await axios.post(`${API}/revoke/analyze`, { orgId: detail.value.org.id })
  impact.value = res.data.data
  revokeVisible.value = true
}

async function executeRevoke() {
  await axios.post(`${API}/revoke/execute`, { orgId: detail.value.org.id, reason: revokeReason.value })
  ElMessage.success('撤销完成，编码保留不重用')
  revokeVisible.value = false
  detail.value = null
  reloadTree()
}

// ---------- 导入导出 ----------
const importVisible = ref(false)
const previewRows = ref([])
const validPreviewCount = computed(() => previewRows.value.filter(r => !r.errors?.length).length)

async function downloadExport() {
  const resp = await axios.get(`${API}/export`, { responseType: 'blob' })
  triggerBlob(resp, '党组织列表.csv')
}

async function downloadTemplate() {
  const resp = await axios.get(`${API}/import/template`, { responseType: 'blob' })
  triggerBlob(resp, '党组织导入模板.xlsx')
}

function triggerBlob(resp, fallback) {
  const dispo = resp.headers['content-disposition'] || ''
  const m = /filename\*=UTF-8''([^;]+)/.exec(dispo)
  const name = m ? decodeURIComponent(m[1]) : fallback
  const url = URL.createObjectURL(resp.data)
  const a = document.createElement('a')
  a.href = url; a.download = name; a.click()
  URL.revokeObjectURL(url)
}

async function beforeUpload(file) {
  const fd = new FormData()
  fd.append('file', file)
  const res = await axios.post(`${API}/import/preview`, fd)
  if (res.data.code === 200) {
    previewRows.value = res.data.data.rows || []
    if (!previewRows.value.length) ElMessage.warning('未解析到数据行')
  }
  return false
}

async function commitImport() {
  const rows = previewRows.value.filter(r => !r.errors?.length)
  await axios.post(`${API}/import/commit`, { rows })
  ElMessage.success(`成功导入 ${rows.length} 个组织`)
  importVisible.value = false
  previewRows.value = []
  reloadTree()
}

// ---------- 届满提醒 ----------
async function loadAlerts() {
  const res = await axios.get(`${API}/term-alerts`)
  if (res.data.code === 200) alerts.value = res.data.data
}

onMounted(() => {
  loadAlerts()
  loadPendingCount()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.tree-card :deep(.el-card__header) { padding: 10px 12px; }
.tree-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; flex-wrap: wrap; gap: 6px; }
.tree-filter { display: flex; justify-content: space-between; gap: 6px; }
.tree-node { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; }
.node-icon { margin-right: 2px; }
.node-count { transform: scale(.85); }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-left: 2px; }
.dot-warning { background: #e6a23c; }
.dot-danger { background: #f56c6c; }
.dot-info { background: #409eff; }
.pending-badge { margin-left: 4px; }
.detail-header { display: flex; justify-content: space-between; align-items: center; gap: 10px; flex-wrap: wrap; }
.detail-empty { min-height: 400px; display: flex; align-items: center; justify-content: center; }
.tab-tip { color: #909399; font-size: 12px; margin: 8px 0 0; }
.warn-item { margin-bottom: 6px; }
.import-bar { margin-bottom: 10px; display: flex; gap: 10px; }
.list-pager { margin-top: 8px; justify-content: flex-end; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">三级指标树 · 编码唯一 · 有子级禁止删除 · 一票否决指标命中即不合格</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="f.level" placeholder="全部层级" clearable size="small" style="width:110px" @change="load">
          <el-option :value="1" label="一级指标" /><el-option :value="2" label="二级指标" /><el-option :value="3" label="三级指标" />
        </el-select>
        <el-select v-model="f.targetType" placeholder="考核对象" clearable size="small" style="width:120px" @change="load">
          <el-option v-for="t in targetTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="f.status" placeholder="状态" clearable size="small" style="width:100px" @change="load">
          <el-option label="启用" value="启用" /><el-option label="停用" value="停用" />
        </el-select>
        <el-input v-model="f.keyword" placeholder="名称/编码" size="small" clearable style="width:160px" @keyup.enter="load" />
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('R02','R04')" size="small" type="primary" @click="edit()">新增指标</el-button>
      </div>

      <el-table :data="tree" size="small" border row-key="indicatorCode" v-loading="loading"
                :tree-props="{ children: 'children' }" default-expand-all>
        <el-table-column prop="indicatorName" label="指标名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="indicatorCode" label="编码" width="110" />
        <el-table-column label="层级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="['info','warning','success'][row.level-1]">{{ row.level }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="考核对象" width="100" />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="weight" label="权重%" width="80" />
        <el-table-column label="评分方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.scoreMode" size="small" :type="row.scoreMode==='定量'?'primary':(row.scoreMode==='定性'?'warning':'info')" effect="plain">{{ row.scoreMode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSource" label="数据源" width="120" show-overflow-tooltip />
        <el-table-column label="一票否决" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.vetoFlag===1" size="small" type="danger">否决</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='启用'?'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="can('R02','R04')" size="small" link @click="edit(row)">编辑</el-button>
            <el-button v-if="can('R02','R04') && row.level<3" size="small" link type="primary" @click="addChild(row)">加子级</el-button>
            <el-button v-if="can('R02','R04')" size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑指标' : '新增指标'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="指标编码" required>
          <el-input v-model="form.indicatorCode" :disabled="!!form.id" placeholder="唯一编码，如 A01" />
        </el-form-item>
        <el-form-item label="指标名称" required><el-input v-model="form.indicatorName" /></el-form-item>
        <el-form-item label="层级" required>
          <el-radio-group v-model="form.level" :disabled="!!form.id">
            <el-radio-button :value="1">一级</el-radio-button>
            <el-radio-button :value="2" :disabled="!form.parentCode">二级</el-radio-button>
            <el-radio-button :value="3" :disabled="!form.parentCode">三级</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级指标" v-if="form.level>1">
          <el-select v-model="form.parentCode" style="width:100%" placeholder="选择上级指标">
            <el-option v-for="p in parentCandidates" :key="p.indicatorCode" :label="`${p.indicatorCode} ${p.indicatorName}`" :value="p.indicatorCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="考核对象" required>
          <el-select v-model="form.targetType" style="width:100%">
            <el-option v-for="t in targetTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值"><el-input-number v-model="form.score" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="权重%"><el-input-number v-model="form.weight" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="评分方式">
          <el-radio-group v-model="form.scoreMode">
            <el-radio-button value="定量">定量</el-radio-button>
            <el-radio-button value="定性">定性</el-radio-button>
            <el-radio-button value="加减分">加减分</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据源">
          <el-select v-model="form.dataSource" clearable style="width:100%" placeholder="定量指标取数来源">
            <el-option v-for="s in dataSources" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分标准"><el-input v-model="form.scoringStandard" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="一票否决"><el-switch v-model="form.vetoFlag" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button value="启用">启用</el-radio-button>
            <el-radio-button value="停用">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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

const api = '/api/party/assessment/indicator'
const targetTypes = ['党组织', '党组织书记', '党员']
const dataSources = ['三会一课', '党员积分', '党费收缴', '民主评议', '新闻宣传', '舆情监控', '人工填报']

const loading = ref(false)
const list = ref([])
const f = reactive({ level: null, targetType: null, status: null, keyword: '' })
const formVisible = ref(false)
const form = ref({})

const tree = computed(() => {
  const map = {}
  list.value.forEach(i => { map[i.indicatorCode] = { ...i, children: [] } })
  const roots = []
  list.value.forEach(i => {
    const node = map[i.indicatorCode]
    if (i.parentCode && map[i.parentCode]) map[i.parentCode].children.push(node)
    else roots.push(node)
  })
  const sortRec = arr => { arr.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)); arr.forEach(x => sortRec(x.children)) }
  sortRec(roots)
  return roots
})

const parentCandidates = computed(() => list.value.filter(i => i.level === (form.value.level || 1) - 1))

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${api}/list`, { params: { ...f } })
    if (res.data.code === 200) list.value = res.data.data
  } finally { loading.value = false }
}
function edit(row) {
  form.value = row ? { ...row } : { level: 1, targetType: '党组织', scoreMode: '定量', vetoFlag: 0, status: '启用', sortOrder: 0 }
  formVisible.value = true
}
function addChild(parent) {
  form.value = { level: parent.level + 1, parentCode: parent.indicatorCode, targetType: parent.targetType, scoreMode: '定量', vetoFlag: 0, status: '启用', sortOrder: 0 }
  formVisible.value = true
}
async function save() {
  try {
    await axios.post(api, form.value)
    ElMessage.success('已保存')
    formVisible.value = false
    load()
  } catch {}
}
async function remove(row) {
  if (row.children && row.children.length) {
    ElMessage.warning('存在子级指标，禁止删除')
    return
  }
  await ElMessageBox.confirm(`确认删除指标「${row.indicatorName}」？`, '提示', { type: 'warning' })
  try {
    await axios.delete(`${api}/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch {}
}
onMounted(load)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
.muted { color: #c0c4cc; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">荣誉项目配置：C03/C07 维护；评选周期 月度/季度/半年度/年度；勾选自动同步后表彰事迹自动生成公开案例</el-tag>
    </RoleBar>

    <el-card shadow="never" class="mb">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:120px" @change="load">
          <el-option label="已启用" value="已启用" />
          <el-option label="已停用" value="已停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C03', 'C07')" size="small" type="primary" @click="openCreate">新增荣誉项目</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" max-height="520">
        <el-table-column prop="projectName" label="项目名称" min-width="140" />
        <el-table-column prop="periodType" label="评选周期" width="90" />
        <el-table-column label="申报类型" width="80">
          <template #default="{row}">
            <el-tag size="small" :type="row.applyType === '个人' ? '' : 'success'">{{ row.applyType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orgScope" label="参评组织范围" min-width="150" show-overflow-tooltip />
        <el-table-column label="申报起止" width="190">
          <template #default="{row}">{{ row.applyStart }} ~ {{ row.applyEnd }}</template>
        </el-table-column>
        <el-table-column label="自动同步案例库" width="120" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.autoSyncCase === 1 ? 'success' : 'info'">{{ row.autoSyncCase === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag size="small" :type="row.status === '已启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="维护人" width="90" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <template v-if="can('C03', 'C07')">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status === '已启用' ? 'danger' : 'success'" plain @click="doToggle(row)">
                {{ row.status === '已启用' ? '停用' : '启用' }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑荣誉项目' : '新增荣誉项目'" width="600px">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.projectName" maxlength="100" placeholder="如：年度奋斗者、文化践行标兵" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="评选周期" required>
              <el-select v-model="form.periodType" style="width:100%">
                <el-option v-for="p in PERIODS" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报类型" required>
              <el-radio-group v-model="form.applyType">
                <el-radio label="个人">个人</el-radio>
                <el-radio label="团队">团队</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参评组织范围">
          <el-input v-model="form.orgScope" placeholder="组织名称，逗号分隔；留空表示全集团" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="申报开始" required>
              <el-date-picker v-model="form.applyStart" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报截止" required>
              <el-date-picker v-model="form.applyEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="自动同步案例库">
          <el-switch v-model="form.autoSyncCase" :active-value="1" :inactive-value="0" />
          <span class="tip">开启后终审表彰时勾选同步，事迹自动生成公开案例写入案例库</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis = false">取消</el-button>
        <el-button size="small" type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const PERIODS = ['月度', '季度', '半年度', '年度']
const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '' })
const formVis = ref(false)
const form = reactive({ id: null, projectName: '', periodType: '年度', applyType: '个人', orgScope: '', applyStart: '', applyEnd: '', autoSyncCase: 0 })

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/honor-project/list', { params: { status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, projectName: '', periodType: '年度', applyType: '个人', orgScope: '', applyStart: '', applyEnd: '', autoSyncCase: 0 })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, {
    id: row.id, projectName: row.projectName, periodType: row.periodType, applyType: row.applyType,
    orgScope: row.orgScope, applyStart: row.applyStart, applyEnd: row.applyEnd, autoSyncCase: row.autoSyncCase
  })
  formVis.value = true
}
async function doSave() {
  await axios({ url: form.id ? '/api/culture/honor-project/' + form.id : '/api/culture/honor-project', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/honor-project/${row.id}/toggle`)
  ElMessage.success('已切换状态')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display: flex; align-items: center; gap: 8px; }
.mb { margin-bottom: 12px; }
.tip { font-size: 12px; color: #909399; margin-left: 8px; }
</style>

<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">文化阵地台账：展厅/书屋/活动中心/线上阵地等；内容更新周期到期未更新显示橙警，点击"更新登记"刷新日期</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option label="正常" value="正常" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-select v-model="q.type" placeholder="类型" clearable size="small" style="width:130px" @change="load">
          <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增阵地</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt" max-height="500">
        <el-table-column prop="posName" label="阵地名称" min-width="170" show-overflow-tooltip>
          <template #default="{row}">
            <span>{{ row.posName }}</span>
            <el-tag v-if="isOverdue(row)" size="small" type="warning" class="ml4">超期{{ overdueDays(row) }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="posType" label="类型" width="100" />
        <el-table-column prop="ownerDept" label="责任部门" width="160" show-overflow-tooltip />
        <el-table-column prop="manager" label="管理员" width="100" />
        <el-table-column label="更新周期" width="90" align="center">
          <template #default="{row}">{{ row.updateCycleDays }}天</template>
        </el-table-column>
        <el-table-column label="最近更新" width="110" align="center">
          <template #default="{row}">{{ row.lastUpdateDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='正常' ? 'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" type="success" @click="doTouch(row)">更新登记</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" :type="row.status==='正常' ? 'warning':''" @click="doToggle(row)">
              {{ row.status==='正常' ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">橙警规则：最近更新日期 + 更新周期 &lt; 今天（停用阵地不告警）</div>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑阵地' : '新增阵地'" width="560px">
      <el-form :model="form" label-width="100px" size="small">
        <el-form-item label="阵地名称" required><el-input v-model="form.posName" maxlength="100" show-word-limit placeholder="2-100字" /></el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="阵地类型">
              <el-select v-model="form.posType" style="width:100%">
                <el-option v-for="t in TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任部门"><el-input v-model="form.ownerDept" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="管理员"><el-input v-model="form.manager" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新周期(天)"><el-input-number v-model="form.updateCycleDays" :min="1" :max="365" style="width:100%" controls-position="right" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="formVis=false">取消</el-button>
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

const TYPES = ['职工书屋', '文化展厅', '活动中心', '荣誉墙', '线上阵地']
const rows = ref([])
const loading = ref(false)
const q = reactive({ status: '', type: '' })
const formVis = ref(false)
const form = reactive({ id: null, posName: '', posType: '文化展厅', ownerDept: '', manager: '', updateCycleDays: 30, remark: '' })

function overdueDays(row) {
  if (!row.lastUpdateDate) return null
  const last = new Date(String(row.lastUpdateDate).slice(0, 10) + 'T00:00:00')
  const diff = Math.floor((Date.now() - last.getTime()) / 86400000) - (row.updateCycleDays || 0)
  return diff > 0 ? diff : 0
}
function isOverdue(row) { return row.status === '正常' && overdueDays(row) > 0 }
async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/position/list', { params: { status: q.status || undefined, type: q.type || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, posName: '', posType: '文化展厅', ownerDept: '', manager: '', updateCycleDays: 30, remark: '' })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, posName: row.posName, posType: row.posType, ownerDept: row.ownerDept, manager: row.manager, updateCycleDays: row.updateCycleDays, remark: row.remark })
  formVis.value = true
}
async function doSave() {
  if ((form.posName || '').length < 2) return ElMessage.warning('阵地名称需2-100字')
  await axios({ url: form.id ? '/api/culture/position/' + form.id : '/api/culture/position', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doTouch(row) {
  await axios.post(`/api/culture/position/${row.id}/touch`)
  ElMessage.success('已登记内容更新')
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/position/${row.id}/toggle`)
  ElMessage.success(row.status === '正常' ? '已停用' : '已启用')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.ml4 { margin-left:4px; }
.tip { color:#909399; font-size:12px; }
</style>

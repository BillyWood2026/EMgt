<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">数据字典：左类型列表 + 右字典项表格 · 被引用项删除阻断（E-SY-10）可引导停用 · 写操作仅 R02</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <el-row :gutter="12">
        <el-col :span="6">
          <div class="bar">
            <b>字典类型</b>
            <el-button v-if="can('R02')" size="small" type="primary" @click="openTypeEdit(null)">新增</el-button>
          </div>
          <el-table :data="types" size="small" border highlight-current-row @current-change="onSelectType" v-loading="typeLoading" height="420">
            <el-table-column prop="typeCode" label="编码" width="100" show-overflow-tooltip />
            <el-table-column prop="typeName" label="名称" min-width="110" show-overflow-tooltip />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link type="primary" @click.stop="openTypeEdit(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click.stop="delType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="18">
          <div class="bar">
            <b>字典项</b>
            <span style="color:#909399; font-size:12px">{{ curType ? curType.typeCode + ' ' + curType.typeName : '请先选择类型' }}</span>
            <span style="flex:1"></span>
            <el-button v-if="can('R02') && curType" size="small" type="primary" @click="openItemEdit(null)">新增字典项</el-button>
          </div>
          <el-table :data="items" size="small" border v-loading="itemLoading" height="420">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="itemValue" label="值" width="110" />
            <el-table-column prop="itemLabel" label="标签" min-width="130" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column prop="version" label="版本" width="70" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button v-if="can('R02')" size="small" link type="primary" @click="openItemEdit(row)">编辑</el-button>
                <el-button v-if="can('R02')" size="small" link type="warning" @click="disableItem(row)">停用</el-button>
                <el-button v-if="can('R02')" size="small" link type="danger" @click="delItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="typeVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="420px" destroy-on-close>
      <el-form :model="typeForm" label-width="90px" size="small">
        <el-form-item label="编码" required><el-input v-model="typeForm.typeCode" :disabled="!!typeForm.id" /></el-form-item>
        <el-form-item label="名称" required><el-input v-model="typeForm.typeName" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="typeVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemVisible" :title="itemForm.id ? '编辑字典项' : '新增字典项'" width="460px" destroy-on-close>
      <el-form :model="itemForm" label-width="90px" size="small">
        <el-form-item label="字典值" required><el-input v-model="itemForm.itemValue" /></el-form-item>
        <el-form-item label="显示标签" required><el-input v-model="itemForm.itemLabel" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" :min="0" style="width:120px" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="itemForm.status" style="width:160px">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="itemVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../PartyRoleBar.vue'
import { can } from '../partyAuth'

const api = '/api/party/system/dict'
const types = ref([])
const items = ref([])
const curType = ref(null)
const typeLoading = ref(false)
const itemLoading = ref(false)
const saving = ref(false)

const typeVisible = ref(false)
const itemVisible = ref(false)
const typeEmpty = { id: null, typeCode: '', typeName: '', remark: '' }
const itemEmpty = { id: null, typeCode: '', itemValue: '', itemLabel: '', sortOrder: 0, status: '启用' }
const typeForm = reactive({ ...typeEmpty })
const itemForm = reactive({ ...itemEmpty })

async function loadTypes() {
  typeLoading.value = true
  try {
    const res = await axios.get(`${api}/type/list`)
    types.value = res.data.data || []
    if (types.value.length && !curType.value) { curType.value = types.value[0]; loadItems() }
  } finally { typeLoading.value = false }
}
async function loadItems() {
  if (!curType.value) return
  itemLoading.value = true
  try {
    const res = await axios.get(`${api}/item/list`, { params: { typeCode: curType.value.typeCode } })
    items.value = res.data.data || []
  } finally { itemLoading.value = false }
}
function onSelectType(row) { curType.value = row; loadItems() }

function openTypeEdit(row) { Object.assign(typeForm, typeEmpty, row || {}); typeVisible.value = true }
async function saveType() {
  saving.value = true
  try {
    if (typeForm.id) { await axios.put(`${api}/type/${typeForm.id}`, typeForm); ElMessage.success('已保存') }
    else { await axios.post(`${api}/type`, typeForm); ElMessage.success('已创建') }
    typeVisible.value = false; loadTypes()
  } catch {} finally { saving.value = false }
}
async function delType(row) {
  await ElMessageBox.confirm(`确认删除类型「${row.typeName}」？`, '提示', { type: 'warning' })
  await axios.delete(`${api}/type/${row.id}`)
  ElMessage.success('已删除'); loadTypes()
}

function openItemEdit(row) { Object.assign(itemForm, itemEmpty, row || {}, { typeCode: curType.value?.typeCode }); itemVisible.value = true }
async function saveItem() {
  saving.value = true
  try {
    if (itemForm.id) { await axios.put(`${api}/item/${itemForm.id}`, itemForm); ElMessage.success('已保存') }
    else { await axios.post(`${api}/item`, itemForm); ElMessage.success('已创建') }
    itemVisible.value = false; loadItems()
  } catch {} finally { saving.value = false }
}
async function disableItem(row) {
  await ElMessageBox.confirm(`确认停用字典项「${row.itemLabel}」？`, '提示', { type: 'warning' })
  await axios.post(`${api}/item/${row.id}/disable`)
  ElMessage.success('已停用'); loadItems()
}
async function delItem(row) {
  await ElMessageBox.confirm(`确认删除字典项「${row.itemLabel}」？若被引用将阻断（E-SY-10），可改用停用。`, '提示', { type: 'warning' })
  await axios.delete(`${api}/item/${row.id}`)
  ElMessage.success('已删除'); loadItems()
}
onMounted(loadTypes)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center; }
</style>

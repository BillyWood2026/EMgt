<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">课程元数据：视频与存储复用培训中台，CCMS 仅维护元数据（中台课程ID/学时0-40/组织范围/是否必修）；分类=新人必修|年度复训|价值观专题|红线禁令专题</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-select v-model="q.category" placeholder="分类" clearable size="small" style="width:140px" @change="load">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="q.status" placeholder="状态" clearable size="small" style="width:110px" @change="load">
          <el-option label="启用" value="启用" />
          <el-option label="停用" value="停用" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="openCreate">新增课程</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="courseName" label="课程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="courseCategory" label="分类" width="130" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="catType(row.courseCategory)">{{ row.courseCategory }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="midCourseId" label="中台课程ID" width="140" />
        <el-table-column prop="creditHours" label="学时" width="70" align="center" />
        <el-table-column prop="orgScope" label="组织范围" min-width="140" show-overflow-tooltip />
        <el-table-column prop="requiredFlag" label="必修" width="70" align="center">
          <template #default="{row}">
            <el-tag v-if="row.requiredFlag===1" size="small" type="danger">必修</el-tag>
            <el-tag v-else size="small" type="info">选修</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{row}">
            <el-tag size="small" :type="row.status==='启用'?'success':'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="维护人" width="110" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{row}">
            <el-button v-if="can('C02','C03','C99')" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" :type="row.status==='启用'?'warning':''" @click="doToggle(row)">
              {{ row.status==='启用' ? '停用' : '启用' }}
            </el-button>
            <el-button v-if="can('C02','C03','C99')" size="small" type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="tip mt">删除保护：被学习任务或考试引用的课程禁止删除（E-TR-14），可先停用</div>
    </el-card>

    <el-dialog v-model="formVis" :title="form.id ? '编辑课程' : '新增课程'" width="560px">
      <el-form :model="form" label-width="110px" size="small">
        <el-form-item label="课程名称" required>
          <el-input v-model="form.courseName" maxlength="100" show-word-limit placeholder="2-100字" />
        </el-form-item>
        <el-form-item label="课程分类" required>
          <el-select v-model="form.courseCategory" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="中台课程ID" required>
          <el-input v-model="form.midCourseId" placeholder="如 MID-C2026005" />
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-form-item label="学时" required>
              <el-input-number v-model="form.creditHours" :min="0.5" :max="40" :step="0.5" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必修">
              <el-switch v-model="form.requiredFlag" :active-value="1" :inactive-value="0" active-text="必修" inactive-text="选修" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="组织范围" required>
          <el-input v-model="form.orgScope" placeholder="如 全员 / 集团本部 / 新员工" />
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleBar from '../CultureRoleBar.vue'
import { can } from '../cultureAuth'

const categories = ['新人必修', '年度复训', '价值观专题', '红线禁令专题']
const rows = ref([])
const loading = ref(false)
const q = reactive({ category: '', status: '' })
const formVis = ref(false)
const form = reactive({ id: null, courseName: '', courseCategory: '新人必修', midCourseId: '', creditHours: 2, orgScope: '全员', requiredFlag: 0 })

function catType(c) {
  if (c === '新人必修') return 'success'
  if (c === '年度复训') return ''
  if (c === '价值观专题') return 'warning'
  return 'danger'
}

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/course/list', { params: { category: q.category || undefined, status: q.status || undefined } })
    rows.value = r.data.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, courseName: '', courseCategory: '新人必修', midCourseId: '', creditHours: 2, orgScope: '全员', requiredFlag: 0 })
  formVis.value = true
}
function openEdit(row) {
  Object.assign(form, { id: row.id, courseName: row.courseName, courseCategory: row.courseCategory, midCourseId: row.midCourseId, creditHours: Number(row.creditHours), orgScope: row.orgScope, requiredFlag: row.requiredFlag })
  formVis.value = true
}
async function doSave() {
  if (!form.courseName || form.courseName.length < 2) return ElMessage.warning('课程名称需2-100字')
  if (!form.midCourseId) return ElMessage.warning('中台课程ID必填')
  await axios({ url: form.id ? '/api/culture/course/' + form.id : '/api/culture/course', method: form.id ? 'put' : 'post', data: { ...form } })
  ElMessage.success('已保存')
  formVis.value = false
  load()
}
async function doToggle(row) {
  await axios.post(`/api/culture/course/${row.id}/toggle`)
  ElMessage.success(row.status === '启用' ? '已停用' : '已启用')
  load()
}
async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该课程？被任务/考试引用将无法删除。', '删除确认', { type: 'warning' })
  await axios.delete(`/api/culture/course/${row.id}`)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.tip { color:#909399; font-size:12px; }
</style>

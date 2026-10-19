<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">栏目树 · 编码唯一 · 删除稿件阻断</el-tag>
    </RoleBar>

    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="bar">
            <span class="title">栏目树</span>
            <el-button v-if="can('R02','R04','R13')" size="small" type="primary" @click="openForm()">新增根栏目</el-button>
          </div>
          <el-tree
            :data="tree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            @node-click="onNodeClick"
            default-expand-all
            highlight-current />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <div class="bar">
            <span class="title">{{ current ? '编辑栏目' : '请选择栏目' }}</span>
            <div v-if="current">
              <el-button v-if="can('R02','R04','R13')" size="small" type="primary" @click="openForm(current)">编辑</el-button>
              <el-button v-if="can('R02','R04','R13')" size="small" @click="openForm(null, current.id)">新增子栏目</el-button>
              <el-button v-if="can('R02','R04')" size="small" type="danger" @click="removeCol(current)">删除</el-button>
            </div>
          </div>
          <el-descriptions v-if="current" :column="2" border size="small">
            <el-descriptions-item label="栏目名称">{{ current.name }}</el-descriptions-item>
            <el-descriptions-item label="栏目编码">{{ current.code }}</el-descriptions-item>
            <el-descriptions-item label="上级栏目">{{ parentName }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ current.sortOrder }}</el-descriptions-item>
            <el-descriptions-item label="可见范围">
              <el-tag size="small">{{ current.visible }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="允许投稿">
              <el-tag size="small" :type="current.allowContribution ? 'success' : 'info'">
                {{ current.allowContribution ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审批级别">{{ current.approveLevel }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="点击左侧栏目查看详情" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑栏目' : (form.parentId ? '新增子栏目' : '新增栏目')" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="栏目名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="栏目编码" required>
          <el-input v-model="form.code" placeholder="唯一编码，如 GROUP_NEWS" />
        </el-form-item>
        <el-form-item label="上级栏目">
          <el-tree-select v-model="form.parentId" :data="tree" :props="{ label: 'name', value: 'id', children: 'children' }"
                          check-strictly clearable placeholder="无（顶级栏目）" style="width:100%" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="可见范围" required>
          <el-select v-model="form.visible" style="width:100%">
            <el-option label="公开" value="公开" />
            <el-option label="全体党员" value="全体党员" />
            <el-option label="内部" value="内部" />
          </el-select>
        </el-form-item>
        <el-form-item label="允许投稿">
          <el-switch v-model="form.allowContribution" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="审批级别" required>
          <el-select v-model="form.approveLevel" style="width:100%">
            <el-option label="一审" value="一审" />
            <el-option label="二审" value="二审" />
            <el-option label="三审" value="三审" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
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

const API = '/api/party/news/column'
const tree = ref([])
const current = ref(null)
const formVisible = ref(false)
const form = reactive({})

const parentName = computed(() => {
  if (!current.value || !current.value.parentId) return '无（顶级）'
  const find = (nodes) => {
    for (const n of nodes) {
      if (n.id === current.value.parentId) return n.name
      if (n.children) { const r = find(n.children); if (r) return r }
    }
    return null
  }
  return find(tree.value) || '-'
})

async function loadTree() {
  const res = await axios.get(`${API}/tree`)
  if (res.data.code === 200) tree.value = res.data.data
}
function onNodeClick(data) { current.value = data }
function openForm(row, parentId) {
  Object.keys(form).forEach(k => delete form[k])
  if (row) { Object.assign(form, JSON.parse(JSON.stringify(row))) }
  else { Object.assign(form, { parentId: parentId || null, visible: '公开', allowContribution: 1, approveLevel: '一审', sortOrder: 0 }) }
  formVisible.value = true
}
async function submit() {
  if (!form.name || !form.code) { ElMessage.warning('名称和编码必填'); return }
  try {
    await axios.post(API, form)
    ElMessage.success('保存成功')
    formVisible.value = false
    loadTree()
  } catch {}
}
async function removeCol(row) {
  try {
    await ElMessageBox.confirm(`确认删除栏目「${row.name}」？`, '提示', { type: 'warning' })
    await axios.delete(`${API}/${row.id}`)
    ElMessage.success('已删除')
    current.value = null
    loadTree()
  } catch {}
}
onMounted(loadTree)
</script>

<style scoped>
.page-container { padding: 12px; }
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.title { font-weight: 600; font-size: 15px; }
</style>

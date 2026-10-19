<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <h2>菜单管理</h2>
      <div>
        <el-button type="primary" @click="handleAdd">新增菜单</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <el-tree
      :data="menuTree"
      :props="treeProps"
      node-key="id"
      default-expand-all
      :expand-on-click-node="false"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <span>
            <el-icon v-if="data.icon" style="margin-right: 5px;">
              <component :is="data.icon" />
            </el-icon>
            {{ node.label }}
            <el-tag size="small" style="margin-left: 8px;" :type="data.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ data.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </span>
          <span class="node-actions">
            <el-button size="small" @click.stop="handleAddChild(data)">添加子菜单</el-button>
            <el-button size="small" type="primary" @click.stop="handleEdit(data)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="handleDelete(data)">删除</el-button>
          </span>
        </span>
      </template>
    </el-tree>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item label="菜单名称" required>
          <el-input v-model="formData.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单编码" required>
          <el-input v-model="formData.menuCode" placeholder="请输入菜单编码" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-select v-model="formData.menuType" placeholder="选择菜单类型" style="width: 100%;">
            <el-option label="目录" value="CATALOG" />
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuTree"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            placeholder="选择上级菜单"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="formData.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="菜单图标">
          <el-input v-model="formData.icon" placeholder="请输入菜单图标" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '@/api/axiosConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'menuName'
}

const formData = reactive({
  id: null,
  parentId: null,
  menuCode: '',
  menuName: '',
  menuType: 'MENU',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  status: 'ACTIVE'
})

const menuTree = ref([])

async function loadMenuTree() {
  loading.value = true
  try {
    const res = await axios.get('/api/sys/menu/tree')
    if (res.data.code === 200) {
      menuTree.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('加载菜单树失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialogTitle.value = '新增菜单'
  Object.assign(formData, {
    id: null,
    parentId: null,
    menuCode: '',
    menuName: '',
    menuType: 'MENU',
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

function handleAddChild(data) {
  dialogTitle.value = '新增子菜单'
  Object.assign(formData, {
    id: null,
    parentId: data.id,
    menuCode: '',
    menuName: '',
    menuType: 'MENU',
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

async function handleEdit(data) {
  try {
    const res = await axios.get(`/api/sys/menu/${data.id}`)
    if (res.data.code === 200) {
      dialogTitle.value = '编辑菜单'
      Object.assign(formData, res.data.data)
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取菜单详情失败')
  }
}

function handleDelete(data) {
  ElMessageBox.confirm('确定要删除该菜单吗？删除后子菜单也会被删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/sys/menu/${data.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadMenuTree()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

function handleRefresh() {
  loadMenuTree()
  ElMessage.success('刷新成功')
}

async function handleSubmit() {
  try {
    let res
    if (formData.id) {
      res = await axios.put(`/api/sys/menu/${formData.id}`, formData)
    } else {
      res = await axios.post('/api/sys/menu', formData)
    }
    if (res.data.code === 200) {
      ElMessage.success(dialogTitle.value.includes('新增') ? '新增成功' : '修改成功')
      dialogVisible.value = false
      loadMenuTree()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

onMounted(() => {
  loadMenuTree()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-actions {
  display: none;
}

.el-tree-node:hover > .el-tree-node__content > .custom-tree-node > .node-actions {
  display: inline-block;
}
</style>

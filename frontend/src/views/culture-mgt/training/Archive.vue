<template>
  <div class="page-container">
    <RoleBar>
      <el-tag size="small" type="info" effect="plain">员工文化培训档案（EL-CUL-009）：学习记录/考试成绩/补考记录/证书/合规状态；考试不合格自动冻结评优/晋升/调薪并写入人事档案，补考通过自动解冻；非本人且非文化岗不可查看</el-tag>
    </RoleBar>

    <el-card shadow="never">
      <div class="bar">
        <el-input v-model="q.keyword" placeholder="姓名" clearable size="small" style="width:160px" @keyup.enter="load" />
        <el-select v-model="q.frozen" placeholder="资格状态" clearable size="small" style="width:130px" @change="load">
          <el-option label="已冻结" :value="1" />
          <el-option label="正常" :value="0" />
        </el-select>
        <el-button size="small" @click="load">查询</el-button>
        <span style="flex:1"></span>
        <el-button v-if="can('C02','C03','C99')" size="small" type="primary" @click="certVis=true">颁发认证</el-button>
      </div>
      <el-table :data="rows" size="small" border v-loading="loading" class="mt">
        <el-table-column prop="userName" label="姓名" width="130" />
        <el-table-column prop="dept" label="部门" min-width="130" show-overflow-tooltip />
        <el-table-column label="学习进度" width="120" align="center">
          <template #default="{row}">{{ row.studyDone }}/{{ row.studyTotal }}</template>
        </el-table-column>
        <el-table-column prop="examCount" label="考试次数" width="90" align="center" />
        <el-table-column prop="bestScore" label="最高分" width="80" align="center" />
        <el-table-column prop="retakeCount" label="补考次数" width="90" align="center" />
        <el-table-column prop="certCount" label="证书数" width="80" align="center" />
        <el-table-column label="合规状态" width="110" align="center">
          <template #default="{row}">
            <el-tag v-if="row.frozen===1" size="small" type="danger">已冻结</el-tag>
            <el-tag v-else size="small" type="success">合规</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="primary" plain @click="openDetail(row)">档案抽屉</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVis" :title="'文化培训档案：' + detailData.userName" size="62%">
      <template v-if="detailData.userName">
        <div class="mb">
          <el-tag :type="detailData.frozen ? 'danger' : 'success'">
            合规状态：{{ detailData.compliance }}
          </el-tag>
          <el-tag v-if="detailData.frozen" type="warning" class="ml4">解冻条件：任一考试补考通过</el-tag>
        </div>

        <div class="sec">学习记录</div>
        <el-table :data="detailData.studyRecords || []" size="small" border max-height="200">
          <el-table-column prop="taskName" label="任务" min-width="150" show-overflow-tooltip />
          <el-table-column prop="courseName" label="课程" min-width="150" show-overflow-tooltip />
          <el-table-column prop="progress" label="进度%" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="80" align="center" />
          <el-table-column prop="finishTime" label="完成时间" min-width="150" />
        </el-table>

        <div class="sec">考试与补考记录</div>
        <el-table :data="detailData.examInstances || []" size="small" border max-height="220">
          <el-table-column prop="examName" label="考试" min-width="150" show-overflow-tooltip />
          <el-table-column prop="status" label="结果" width="140" align="center" />
          <el-table-column prop="score" label="得分" width="70" align="center">
            <template #default="{row}">{{ row.score === null ? '-' : row.score }}</template>
          </el-table-column>
          <el-table-column prop="attempt" label="第几次" width="70" align="center" />
          <el-table-column prop="switchCount" label="切屏" width="70" align="center" />
          <el-table-column prop="startTime" label="开始时间" min-width="150" />
          <el-table-column prop="submitTime" label="交卷时间" min-width="150" />
        </el-table>

        <div class="sec">文化认证证书（编号 CT+年份+4位）</div>
        <el-table :data="detailData.certs || []" size="small" border max-height="200">
          <el-table-column prop="certNo" label="证书编号" width="130" />
          <el-table-column prop="certType" label="类型" min-width="150" />
          <el-table-column prop="issueDate" label="颁发日期" width="110" />
          <el-table-column prop="issuer" label="颁发人" width="110" />
          <el-table-column prop="reason" label="颁发说明" min-width="180" show-overflow-tooltip />
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="certVis" title="颁发文化认证证书" width="520px">
      <el-form :model="certForm" label-width="100px" size="small">
        <el-form-item label="员工姓名" required>
          <el-input v-model="certForm.userName" placeholder="如 张三" />
        </el-form-item>
        <el-form-item label="证书类型" required>
          <el-select v-model="certForm.certType" style="width:100%">
            <el-option label="优秀文化践行者认证" value="优秀文化践行者认证" />
            <el-option label="年度文化合格认证" value="年度文化合格认证" />
          </el-select>
        </el-form-item>
        <el-form-item label="颁发理由" required>
          <el-input v-model="certForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="不少于5字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="certVis=false">取消</el-button>
        <el-button size="small" type="primary" @click="doCert">颁发</el-button>
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

const rows = ref([])
const loading = ref(false)
const q = reactive({ keyword: '', frozen: null })

const detailVis = ref(false)
const detailData = reactive({})

const certVis = ref(false)
const certForm = reactive({ userName: '', certType: '优秀文化践行者认证', reason: '' })

async function load() {
  loading.value = true
  try {
    const r = await axios.get('/api/culture/archive/list', { params: { keyword: q.keyword || undefined, frozen: q.frozen } })
    rows.value = r.data.data || []
  } catch (e) { rows.value = [] } finally { loading.value = false }
}
async function openDetail(row) {
  const r = await axios.get(`/api/culture/archive/${encodeURIComponent(row.userName)}`)
  Object.assign(detailData, r.data.data)
  detailVis.value = true
}
async function doCert() {
  if (!certForm.userName) return ElMessage.warning('请填写员工姓名')
  if (!certForm.reason || certForm.reason.length < 5) return ElMessage.warning('颁发理由不少于5字（E-TR-13）')
  const r = await axios.post('/api/culture/archive/cert', { ...certForm })
  ElMessage.success(`已颁发证书 ${r.data.data.certNo}`)
  certVis.value = false
  certForm.reason = ''
  load()
}
onMounted(load)
</script>

<style scoped>
.bar { display:flex; align-items:center; gap:8px; }
.mt { margin-top:10px; }
.mb { margin-bottom:10px; }
.ml4 { margin-left:4px; }
.sec { font-weight:bold; margin:14px 0 6px; font-size:13px; }
</style>

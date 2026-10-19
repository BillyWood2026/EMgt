<template>
  <div class="page-card">
    <div class="page-header">
      <h2>员工档案</h2>
      <div class="header-actions">
        <ImportExportBar
          :columns="ioColumns"
          :rows="ioRows"
          file-name="员工人事档案"
          title="员工人事档案"
          import-api="/api/hr-archive/employee"
          :field-map="ioFieldMap"
        />
        <el-button type="primary" @click="editProfile">
          <el-icon><Edit /></el-icon>编辑档案
        </el-button>
        <el-button @click="printArchive">
          <el-icon><Printer /></el-icon>打印档案
        </el-button>
        <el-button @click="versionVisible = true">
          <el-icon><Clock /></el-icon>版本记录
        </el-button>
        <el-button @click="loadProfile">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :span="6">
        <el-card shadow="hover" class="profile-card">
          <div class="profile-avatar">
            <el-avatar :size="120" style="background:#1890ff; font-size:48px">{{ profile.name?.charAt(0) }}</el-avatar>
          </div>
          <div class="profile-info">
            <h3>{{ profile.name }}</h3>
            <p>{{ profile.position }}</p>
            <p>{{ profile.department }}</p>
            <el-tag :type="profile.status === 'active' ? 'success' : 'warning'" size="small" style="margin-top:8px">
              {{ profile.status === 'active' ? '在职' : '试用期' }}
            </el-tag>
          </div>
          <div style="margin-top:16px; text-align:left">
            <div style="font-size:13px; color:#606266; margin-bottom:4px">
              档案完整度
              <el-tag size="small" :type="completeness >= 90 ? 'success' : completeness >= 60 ? 'warning' : 'danger'" style="margin-left:4px">{{ completeness }}%</el-tag>
            </div>
            <el-progress :percentage="completeness" :stroke-width="10" :show-text="false"
              :color="completeness >= 90 ? '#67c23a' : completeness >= 60 ? '#e6a23c' : '#f56c6c'" />
            <div v-if="missingFields.length" style="font-size:12px; color:#f56c6c; margin-top:6px">
              缺失字段：{{ missingFields.join('、') }}
            </div>
            <div style="font-size:12px; color:#909399; margin-top:6px">档案版本号：V{{ profile.archiveVersion || 1 }}　更新：{{ profile.updateTime || '—' }}</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="quick-links">
          <h4 class="card-title">快捷操作</h4>
          <el-button type="primary" block style="margin-bottom:8px" @click="handleContract">合同管理</el-button>
          <el-button block style="margin-bottom:8px" @click="handleSalary">薪资查询</el-button>
          <el-button block @click="handleLeave">请假申请</el-button>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-card shadow="never">
              <el-form label-width="120px" :model="profile">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="员工编号">
                      <el-input :value="profile.id" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="姓名">
                      <el-input :value="profile.name" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-input :value="profile.gender" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="身份证号">
                      <el-input :value="maskIdCard(profile.idCard)" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="手机号">
                      <el-input :value="maskPhone(profile.phone)" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="邮箱">
                      <el-input :value="profile.email" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="出生日期">
                      <el-input :value="profile.birthDate" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="籍贯">
                      <el-input :value="profile.nativePlace" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展（页面2基础信息档案完整字段清单） -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="曾用名">
                      <el-input :value="profile.formerName" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="婚姻状况">
                      <el-input :value="profile.maritalStatus" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="户籍地址">
                      <el-input :value="profile.hukouAddress" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="户籍性质">
                      <el-input :value="profile.hukouType" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="政治面貌">
                      <el-input :value="profile.politicalStatus" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="民族">
                      <el-input :value="profile.nation" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="学历">
                      <el-input :value="profile.education" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="专业">
                      <el-input :value="profile.major" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="毕业院校">
                      <el-input :value="profile.school" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="毕业时间">
                      <el-input :value="profile.graduationDate" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="学位">
                      <el-input :value="profile.degree" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="外语等级">
                      <el-input :value="profile.foreignLangLevel" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="计算机等级">
                      <el-input :value="profile.computerLevel" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="紧急联系人">
                      <el-input :value="profile.emergencyContact" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="紧急联系电话">
                      <el-input :value="profile.emergencyPhone" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="联系地址">
                  <el-input :value="profile.address" disabled />
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="工作信息" name="work">
            <el-card shadow="never">
              <el-form label-width="120px" :model="profile">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="部门">
                      <el-input :value="profile.department" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="岗位">
                      <el-input :value="profile.position" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="入职日期">
                      <el-input :value="profile.entryDate" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="转正日期">
                      <el-input :value="profile.regularDate" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="合同期限">
                      <el-input :value="profile.contractTerm" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="合同到期日">
                      <el-input :value="profile.contractEndDate" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="编制类型">
                      <el-input :value="profile.staffingType" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="用工形式">
                      <el-input :value="profile.employmentType" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="工龄(年)">
                      <el-input :value="profile.workYears" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="既往单位">
                      <el-input :value="profile.previousCompany" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展（职级/薪级/职族/在岗状态/档案状态） -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="职级">
                      <el-input :value="profile.rankLevel" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="薪级">
                      <el-input :value="profile.salaryGrade" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="职族">
                      <el-input :value="profile.jobFamily" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="岗位序列">
                      <el-input :value="profile.positionSeq" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
                <!-- 规格书字段，待后端扩展 -->
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="在岗状态">
                      <el-input :value="profile.onJobStatus" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="档案状态">
                      <el-input :value="profile.archiveStatus || '正常'" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="技能证书" name="skill">
            <el-card shadow="never">
              <el-form label-width="120px" :model="profile">
                <el-form-item label="专业技能">
                  <el-input :value="profile.skills" disabled type="textarea" :rows="3" />
                </el-form-item>
                <el-form-item label="资质证书">
                  <el-input :value="profile.certifications" disabled type="textarea" :rows="3" />
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="工作经历" name="experience">
            <el-card shadow="never">
              <el-table :data="workExperience" border stripe size="small">
                <el-table-column prop="startDate" label="开始日期" width="120" />
                <el-table-column prop="endDate" label="结束日期" width="120" />
                <el-table-column prop="company" label="公司名称" width="180" />
                <el-table-column prop="position" label="职位" width="150" />
                <el-table-column prop="description" label="工作描述" />
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="培训记录" name="training">
            <el-card shadow="never">
              <el-table :data="trainingRecords" border stripe size="small">
                <el-table-column prop="courseName" label="课程名称" min-width="200" />
                <el-table-column prop="trainingDate" label="培训日期" width="120" />
                <el-table-column prop="duration" label="时长" width="80" />
                <el-table-column prop="result" label="考核结果" width="100">
                  <template #default="{row}">
                    <el-tag :type="row.result === '合格' ? 'success' : 'danger'" size="small">{{ row.result }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="奖惩记录" name="reward">
            <el-card shadow="never">
              <el-table :data="rewardRecords" border stripe size="small">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="type" label="类型" width="80">
                  <template #default="{row}">
                    <el-tag :type="row.type === '奖励' ? 'success' : 'danger'" size="small">{{ row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="200" />
                <el-table-column prop="description" label="说明" />
              </el-table>
            </el-card>
          </el-tab-pane>

          <!-- 规格书第七章页面3：证照材料数字化影像档案（EL-FILE，OCR识别/有效期预警待后端扩展） -->
          <el-tab-pane label="证照材料" name="certificate">
            <el-card shadow="never">
              <div style="margin-bottom:12px">
                <el-button size="small" type="primary" @click="ElMessage.info('OCR识别录入待后端影像服务扩展')">OCR识别录入</el-button>
                <el-button size="small" @click="ElMessage.info('批量上传影像附件待后端扩展')">批量上传</el-button>
                <el-button size="small" type="warning" plain @click="ElMessage.info('影像在线预览待后端扩展')">影像在线预览</el-button>
              </div>
              <el-table :data="certificates" border stripe size="small">
                <el-table-column prop="certType" label="证照类型" width="110" />
                <el-table-column prop="certName" label="证照名称" min-width="150" />
                <el-table-column prop="certNo" label="证照编号" width="150" />
                <el-table-column prop="issuer" label="发证机关" width="130" />
                <el-table-column prop="issueDate" label="发证日期" width="105" />
                <el-table-column prop="expiryDate" label="有效期截止" width="105" />
                <el-table-column label="有效期状态" width="100">
                  <template #default="{row}">
                    <el-tag :type="certStatus(row).type" size="small">{{ certStatus(row).text }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <!-- 规格书EL-BASE-02：家庭关系明细扩展页（待后端扩展） -->
          <el-tab-pane label="家庭关系" name="family">
            <el-card shadow="never">
              <el-table :data="familyMembers" border stripe size="small">
                <el-table-column prop="name" label="家庭成员" width="110" />
                <el-table-column prop="relation" label="关系" width="90" />
                <el-table-column prop="workUnit" label="工作单位" min-width="180" />
                <el-table-column prop="phone" label="联系方式" width="130" />
                <el-table-column prop="hukou" label="户籍信息" min-width="150" />
              </el-table>
            </el-card>
          </el-tab-pane>

          <!-- 规格书EL-ARCH-01：档案全生命周期快照页 -->
          <el-tab-pane label="变更快照" name="snapshot">
            <el-card shadow="never">
              <el-table :data="versionSnapshots" border stripe size="small">
                <el-table-column prop="version" label="版本号" width="80">
                  <template #default="{row}">V{{ row.version }}</template>
                </el-table-column>
                <el-table-column prop="time" label="操作时间" width="170" />
                <el-table-column prop="operator" label="操作人" width="100" />
                <el-table-column prop="changeType" label="变更类型" width="130" />
                <el-table-column prop="summary" label="修改内容摘要" min-width="200" />
              </el-table>
              <el-alert type="info" :closable="false" show-icon style="margin-top:12px"
                title="版本快照规则：每次新增/编辑/变更/审批/红冲调整自动生成终身版本快照，版本号递增、永不覆盖、永不删除；跨版本差异对比高亮与红冲调整单待后端扩展。" />
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <el-dialog v-model="editVisible" title="编辑员工档案" width="720px">
      <el-form :model="editForm" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历">
              <el-input v-model="editForm.education" placeholder="请输入学历" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毕业院校">
              <el-input v-model="editForm.school" placeholder="请输入毕业院校" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="editForm.major" placeholder="请输入专业" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毕业时间">
              <el-date-picker v-model="editForm.graduationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择毕业时间" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工龄(年)">
              <el-input-number v-model="editForm.workYears" :min="0" :max="50" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="既往单位">
              <el-input v-model="editForm.previousCompany" placeholder="请输入既往单位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="editForm.emergencyContact" placeholder="请输入紧急联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急电话">
              <el-input v-model="editForm.emergencyPhone" placeholder="请输入紧急联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系地址">
          <el-input v-model="editForm.address" placeholder="请输入联系地址" />
        </el-form-item>
        <el-form-item label="专业技能">
          <el-input type="textarea" v-model="editForm.skills" :rows="2" placeholder="请输入专业技能" />
        </el-form-item>
        <el-form-item label="资质证书">
          <el-input type="textarea" v-model="editForm.certifications" :rows="2" placeholder="请输入资质证书" />
        </el-form-item>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="民族">
              <el-input v-model="editForm.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-input v-model="editForm.politicalStatus" placeholder="请输入政治面貌" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="曾用名">
              <el-input v-model="editForm.formerName" placeholder="请输入曾用名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="婚姻状况">
              <el-select v-model="editForm.maritalStatus" clearable placeholder="选择婚姻状况" style="width:100%">
                <el-option label="未婚" value="未婚" />
                <el-option label="已婚" value="已婚" />
                <el-option label="离异" value="离异" />
                <el-option label="丧偶" value="丧偶" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户籍地址">
              <el-input v-model="editForm.hukouAddress" placeholder="请输入户籍地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户籍性质">
              <el-select v-model="editForm.hukouType" clearable placeholder="选择户籍性质" style="width:100%">
                <el-option label="城镇" value="城镇" />
                <el-option label="农村" value="农村" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学位">
              <el-input v-model="editForm.degree" placeholder="请输入学位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="外语等级">
              <el-input v-model="editForm.foreignLangLevel" placeholder="如 CET-6" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 规格书字段，待后端扩展 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计算机等级">
              <el-input v-model="editForm.computerLevel" placeholder="如 国家二级" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职级">
              <el-input v-model="editForm.rankLevel" placeholder="如 T3 / M2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- EL-ARCH-01 档案版本记录弹窗 -->
    <el-dialog v-model="versionVisible" title="档案版本记录（全生命周期快照）" width="720px">
      <el-table :data="versionSnapshots" border stripe size="small">
        <el-table-column prop="version" label="版本号" width="80">
          <template #default="{row}">V{{ row.version }}</template>
        </el-table-column>
        <el-table-column prop="time" label="操作时间" width="170" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="changeType" label="变更类型" width="130" />
        <el-table-column prop="summary" label="修改内容摘要" min-width="200" />
      </el-table>
      <template #footer>
        <el-button @click="versionVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Refresh, Printer, Clock } from '@element-plus/icons-vue'
import request from '@/api/axiosConfig'
import ImportExportBar from '@/components/ImportExportBar.vue'

const ioColumns = [
  { key: 'id', title: '员工编号' },
  { key: 'name', title: '姓名' },
  { key: 'gender', title: '性别' },
  { key: 'idCard', title: '身份证号' },
  { key: 'phone', title: '手机号' },
  { key: 'email', title: '邮箱' },
  { key: 'education', title: '学历' },
  { key: 'school', title: '毕业院校' },
  { key: 'major', title: '专业' },
  { key: 'workYears', title: '工龄(年)' },
  { key: 'department', title: '部门' },
  { key: 'position', title: '岗位' },
  { key: 'entryDate', title: '入职日期' },
  { key: 'emergencyContact', title: '紧急联系人' },
  { key: 'emergencyPhone', title: '紧急电话' }
]
const ioFieldMap = {
  '员工编号': 'employeeId',
  '姓名': 'employeeName',
  '性别': 'gender',
  '身份证号': 'idCard',
  '手机号': 'phone',
  '邮箱': 'email',
  '学历': 'education',
  '毕业院校': 'school',
  '专业': 'major',
  '毕业时间': 'graduationDate',
  '工龄': 'workYears',
  '既往单位': 'previousCompany',
  '联系地址': 'address',
  '紧急联系人': 'emergencyContact',
  '紧急电话': 'emergencyPhone',
  '专业技能': 'skills',
  '资质证书': 'certifications',
  '部门': 'deptName',
  '岗位': 'positionName',
  '入职日期': 'hireDate'
}
const ioRows = computed(() => [{ ...profile }])

const activeTab = ref('basic')

const profile = reactive({
  id: 'EMP001',
  name: '张三',
  gender: '男',
  idCard: '110101199001011234',
  phone: '13800138001',
  email: 'zhangsan@example.com',
  birthDate: '1990-01-01',
  nativePlace: '北京市',
  education: '本科',
  major: '计算机科学',
  department: '研发部',
  position: '高级工程师',
  entryDate: '2020-03-15',
  regularDate: '2020-06-15',
  contractTerm: '3年',
  contractEndDate: '2026-03-15',
  staffingType: '正式编制',
  employmentType: '全日制',
  school: '',
  graduationDate: '',
  workYears: null,
  previousCompany: '',
  skills: '',
  certifications: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  nation: '',
  politicalStatus: '',
  // 规格书字段，待后端扩展
  formerName: '',
  maritalStatus: '',
  hukouAddress: '',
  hukouType: '',
  degree: '',
  foreignLangLevel: '',
  computerLevel: '',
  rankLevel: '',
  salaryGrade: '',
  jobFamily: '',
  positionSeq: '',
  onJobStatus: '',
  archiveStatus: '正常',
  archiveVersion: 1,
  updateTime: '',
  profileId: null,
  status: 'active'
})

const workExperience = ref([
  { id: 1, startDate: '2020-03-15', endDate: '至今', company: 'eOpt企业运营平台', position: '高级工程师', description: '负责系统架构设计和核心模块开发' },
  { id: 2, startDate: '2017-07-01', endDate: '2020-03-14', company: '某科技有限公司', position: '软件工程师', description: '参与多个项目的开发和维护' },
  { id: 3, startDate: '2015-07-01', endDate: '2017-06-30', company: '某互联网公司', position: '初级工程师', description: '前端开发和技术支持' }
])

const trainingRecords = ref([
  { id: 1, courseName: 'Java架构师培训', trainingDate: '2025-03-10', duration: '5天', result: '合格' },
  { id: 2, courseName: '项目管理PMP培训', trainingDate: '2024-08-15', duration: '7天', result: '合格' },
  { id: 3, courseName: '安全意识培训', trainingDate: '2024-01-20', duration: '1天', result: '合格' }
])

const rewardRecords = ref([
  { id: 1, date: '2025-01-15', type: '奖励', title: '年度优秀员工', description: '2024年度优秀员工' },
  { id: 2, date: '2024-06-20', type: '奖励', title: '项目突出贡献奖', description: '某项目按期交付' },
  { id: 3, date: '2023-12-10', type: '惩罚', title: '迟到警告', description: '月度迟到超过3次' }
])

// 证照材料数字化影像档案（规格书页面3，示例数据，OCR识别与影像服务待后端扩展）
const certificates = ref([
  { id: 1, certType: '身份证', certName: '居民身份证', certNo: '110101199001011234', issuer: '北京市公安局', issueDate: '2015-03-01', expiryDate: '2035-03-01' },
  { id: 2, certType: '学历证', certName: '本科毕业证', certNo: 'XL-2015-0662', issuer: '某大学', issueDate: '2015-07-01', expiryDate: '' },
  { id: 3, certType: '职业资格证', certName: 'PMP项目管理认证', certNo: 'PMP-882134', issuer: 'PMI', issueDate: '2024-08-15', expiryDate: '2026-11-01' },
  { id: 4, certType: '技能等级证', certName: '高级电工证', certNo: 'JN-330521', issuer: '人社部门', issueDate: '2023-05-10', expiryDate: '2026-05-09' }
])

// 家庭关系明细（规格书EL-BASE-02，示例数据，待后端扩展）
const familyMembers = ref([
  { id: 1, name: '张父', relation: '父亲', workUnit: '已退休', phone: '139****0001', hukou: '北京市海淀区' },
  { id: 2, name: '李母', relation: '母亲', workUnit: '已退休', phone: '139****0002', hukou: '北京市海淀区' }
])

// 档案完整性校验（规格书EL-ARCH-02算法：关键字段缺失标红预警，完整度降权）
const ARCHIVE_KEY_FIELDS = [
  ['name', '姓名'], ['idCard', '身份证号'], ['phone', '手机号'], ['education', '学历'],
  ['school', '毕业院校'], ['major', '专业'], ['entryDate', '入职日期'],
  ['emergencyContact', '紧急联系人'], ['emergencyPhone', '紧急联系电话'], ['address', '联系地址']
]
const missingFields = computed(() =>
  ARCHIVE_KEY_FIELDS.filter(([k]) => !profile[k] && profile[k] !== 0).map(([, label]) => label)
)
const completeness = computed(() =>
  Math.round(((ARCHIVE_KEY_FIELDS.length - missingFields.value.length) / ARCHIVE_KEY_FIELDS.length) * 100)
)

// 证照有效期状态（规格书算法：过期自动标红；90天内即将过期预警）
function certStatus(row) {
  if (!row.expiryDate) return { text: '长期有效', type: 'info' }
  const now = new Date()
  const expiry = new Date(row.expiryDate)
  if (expiry < now) return { text: '已过期', type: 'danger' }
  const days = (expiry - now) / (1000 * 60 * 60 * 24)
  if (days <= 90) return { text: '即将过期', type: 'warning' }
  return { text: '有效', type: 'success' }
}

const versionVisible = ref(false)

// 版本快照台账（基于后端 version/updateTime 生成，历史版本永久归档）
const versionSnapshots = computed(() => {
  const snaps = []
  const v = profile.archiveVersion || 1
  for (let i = v; i >= 1; i--) {
    snaps.push({
      version: i,
      time: i === v ? (profile.updateTime || '—') : '—',
      operator: 'HR专员',
      changeType: i === 1 ? '建档' : '档案信息变更',
      summary: i === v ? '当前生效版本' : '历史版本（只读归档）'
    })
  }
  return snaps
})

// 打印档案（规格书：打印档案按钮，导出二次确认）
function printArchive() {
  ElMessageBox.confirm('确认打印当前员工人事档案？敏感字段将按脱敏规则输出。', '打印档案二次确认', { type: 'warning' })
    .then(() => window.print())
    .catch(() => {})
}

function maskIdCard(idCard) {
  if (!idCard) return ''
  return idCard.slice(0, 6) + '**********' + idCard.slice(-4)
}

function maskPhone(phone) {
  if (!phone) return ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const editVisible = ref(false)
const editForm = reactive({
  education: '', school: '', major: '', graduationDate: '', workYears: null,
  previousCompany: '', skills: '', certifications: '', address: '',
  emergencyContact: '', emergencyPhone: '', nation: '', politicalStatus: '',
  formerName: '', maritalStatus: '', hukouAddress: '', hukouType: '',
  degree: '', foreignLangLevel: '', computerLevel: '', rankLevel: ''
})

async function loadProfile() {
  try {
    const { data } = await request.get('/api/employee/profile')
    const list = data?.data || []
    if (list.length) {
      const p = list[0]
      Object.assign(profile, {
        profileId: p.id,
        education: p.education || '',
        school: p.school || '',
        major: p.major || '',
        graduationDate: p.graduationDate ? String(p.graduationDate).slice(0, 10) : '',
        workYears: p.workYears,
        previousCompany: p.previousCompany || '',
        skills: p.skills || '',
        certifications: p.certifications || '',
        address: p.address || '',
        emergencyContact: p.emergencyContact || '',
        emergencyPhone: p.emergencyPhone || '',
        nation: p.nation || '',
        politicalStatus: p.politicalStatus || '',
        formerName: p.formerName || '',
        maritalStatus: p.maritalStatus || '',
        hukouAddress: p.hukouAddress || '',
        hukouType: p.hukouType || '',
        degree: p.degree || '',
        foreignLangLevel: p.foreignLangLevel || '',
        computerLevel: p.computerLevel || '',
        rankLevel: p.rankLevel || '',
        archiveVersion: p.version || 1,
        updateTime: p.updateTime ? String(p.updateTime).slice(0, 16).replace('T', ' ') : ''
      })
    }
  } catch (e) {
    ElMessage.warning('人事档案加载失败，展示本地示例数据')
  }
}

function editProfile() {
  Object.assign(editForm, {
    education: profile.education, school: profile.school, major: profile.major,
    graduationDate: profile.graduationDate, workYears: profile.workYears,
    previousCompany: profile.previousCompany, skills: profile.skills,
    certifications: profile.certifications, address: profile.address,
    emergencyContact: profile.emergencyContact, emergencyPhone: profile.emergencyPhone,
    nation: profile.nation, politicalStatus: profile.politicalStatus,
    formerName: profile.formerName, maritalStatus: profile.maritalStatus,
    hukouAddress: profile.hukouAddress, hukouType: profile.hukouType,
    degree: profile.degree, foreignLangLevel: profile.foreignLangLevel,
    computerLevel: profile.computerLevel, rankLevel: profile.rankLevel
  })
  editVisible.value = true
}

async function saveProfile() {
  try {
    const payload = { ...editForm }
    if (profile.profileId) {
      await request.put(`/api/employee/profile/${profile.profileId}`, payload)
    } else {
      const { data } = await request.post('/api/employee/profile', payload)
      profile.profileId = data?.data?.id || null
    }
    Object.assign(profile, editForm)
    profile.archiveVersion = (profile.archiveVersion || 1) + 1
    profile.updateTime = new Date().toLocaleString('zh-CN', { hour12: false })
    editVisible.value = false
    ElMessage.success('员工档案已保存，已生成新版本快照 V' + profile.archiveVersion)
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || ''))
  }
}

onMounted(() => loadProfile())

function handleContract() {
  ElMessage.info('合同管理')
}

function handleSalary() {
  ElMessage.info('薪资查询')
}

function handleLeave() {
  ElMessage.info('请假申请')
}
</script>

<style scoped>
.page-card { padding: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-actions { display: flex; align-items: center; gap: 8px; }

.profile-card { text-align: center; padding: 24px; }
.profile-avatar { margin-bottom: 16px; }
.profile-info h3 { margin: 0 0 8px; font-size: 20px; }
.profile-info p { margin: 4px 0; color: #666; }

.card-title { margin: 0 0 16px; font-size: 14px; font-weight: 600; }
</style>
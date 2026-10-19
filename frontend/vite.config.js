import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 穿透测试用代理配置：按后端服务真实部署端口映射
export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.vue', '.js', '.jsx', '.json', '.scss', '.css'],
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {}
    }
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    timeout: 60000,
    proxy: {
      // ---- IAM 8081 ----
      '/api/auth': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/sys-mon': { target: 'http://localhost:8112', changeOrigin: true },
      '/api/sys/menu': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/sys/dict': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/sys/config': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/sys/file': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/sys/notice': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/sys': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/hrm': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/iam': { target: 'http://localhost:8081', changeOrigin: true },
      // ---- 模块配置中心 9000 ----
      '/api/module-config': { target: 'http://localhost:9000', changeOrigin: true },
      // ---- 统一中台：数据中台8200 / 文件中台8201 / 消息中心8202 / IOT 8203 / BI 8204 / 大屏 8205 ----
      '/api/data': { target: 'http://localhost:8200', changeOrigin: true },
      '/api/file': { target: 'http://localhost:8201', changeOrigin: true },
      '/api/message': { target: 'http://localhost:8202', changeOrigin: true },
      '/api/iot': { target: 'http://localhost:8203', changeOrigin: true },
      '/api/bi': { target: 'http://localhost:8204', changeOrigin: true },
      '/api/screen': { target: 'http://localhost:8205', changeOrigin: true },
      // ---- 统一流程引擎BPMN 8206（独立中台，非 ProcessMgt） ----
      '/api/flow': { target: 'http://localhost:8206', changeOrigin: true },
      // ---- OrgMgt 8082 ----
      '/api/org-unit': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/org-mgt': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/org-change': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/org-permission': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/org-personalized-quota': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/personalized-quota': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/org': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/position-hierarchy': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/position': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/job-position': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/job-description': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/staffing-standard': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/management-span-anomaly': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/management-span': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/virtual-org': { target: 'http://localhost:8082', changeOrigin: true },
      // ---- PerfMgt 8085 ----
      '/api/perf': { target: 'http://localhost:8085', changeOrigin: true },
      // ---- QualificationMgt 8090（等级评定/review、等级标准/standard） ----
      '/api/review-calibration': { target: 'http://localhost:8090', changeOrigin: true },
      '/api/review': { target: 'http://localhost:8090', changeOrigin: true },
      '/api/standard': { target: 'http://localhost:8090', changeOrigin: true },
      '/api/qualification': { target: 'http://localhost:8090', changeOrigin: true },
      '/api/employee-record': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/architecture': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/application-validation': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/application': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/batch-process': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/bi-report': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/dashboard': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/snapshot': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/headcount': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/process-callback': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/salary-adjustment': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/salary-rule': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/el/': { target: 'http://localhost:8084', changeOrigin: true },
      // ---- TalentDevepPrj 8095 (must be before /api/training catch-all) ----
      '/api/training-projects': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/training-evaluations': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/training-monitoring': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/training-progress': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/training-standards': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/training-students': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/talent-ledger': { target: 'http://localhost:8095', changeOrigin: true },
      // ---- TrainingMgt 8087 ----
      '/api/training': { target: 'http://localhost:8087', changeOrigin: true },
      // ---- SkillCertMgt 8093（技能认证） ----
      '/api/skill-cert': { target: 'http://localhost:8093', changeOrigin: true },
      // ---- RecruitmentMgt 8086 ----
      '/api/recruitment': { target: 'http://localhost:8086', changeOrigin: true },
      '/api/recruiting': { target: 'http://localhost:8086', changeOrigin: true },
      '/api/recruit': { target: 'http://localhost:8086', changeOrigin: true },
      // ---- EmployeeMgt 8083 ----
      '/api/employee': { target: 'http://localhost:8083', changeOrigin: true },
      // ---- 人才发展拆分：评审8191 / 继任8192 / 培养8193（须在 /api/talent catch-all 之前） ----
      '/api/talent/review': { target: 'http://localhost:8191', changeOrigin: true },
      '/api/talent/succession': { target: 'http://localhost:8192', changeOrigin: true },
      '/api/talent/dev-project': { target: 'http://localhost:8193', changeOrigin: true },
      '/api/talent/tracking': { target: 'http://localhost:8193', changeOrigin: true },
      // 8085 人才池前缀与 /api/talent catch-all 冲突，必须在前
      '/api/talent-pool': { target: 'http://localhost:8085', changeOrigin: true },
      // ---- TalentMgt 8094（人才测评：assessment/model/list） ----
      '/api/talent': { target: 'http://localhost:8094', changeOrigin: true },
      // ---- SalaryMgt 8092 ----
      '/api/salary': { target: 'http://localhost:8092', changeOrigin: true },
      // ---- EmployeeMgt 8083（员工/合同，前端沿用 /api/hr-archive 前缀） ----
      '/api/hr-archive': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      // ---- 战略管理板块：5 个微服务 8101-8105（前端统一 /api/strategy 前缀，按二级路径分流） ----
      '^/api/strategy/blm(?=/|$)': { target: 'http://localhost:8101', changeOrigin: true },
      '^/api/strategy/dste(?=/|$)': { target: 'http://localhost:8102', changeOrigin: true },
      '^/api/strategy/sixs(?=/|$)': { target: 'http://localhost:8103', changeOrigin: true },
      '^/api/strategy/budget(?=/|$)': { target: 'http://localhost:8104', changeOrigin: true },
      '^/api/strategy/workplan(?=/|$)': { target: 'http://localhost:8105', changeOrigin: true },
      '^/api/strategy/ping': { target: 'http://localhost:8101', changeOrigin: true },
      // ---- HolidayMgt 8190（假期管理，原属 AttendanceMgt 拆分） ----
      '/api/holiday': { target: 'http://localhost:8190', changeOrigin: true },
      '/api/leave-balance': { target: 'http://localhost:8190', changeOrigin: true },
      '/api/leave-policy': { target: 'http://localhost:8190', changeOrigin: true },
      // ---- ImproveMgt(改善) 8142 ----
      '/api/improve': { target: 'http://localhost:8142', changeOrigin: true },
      '/api/kaizen': { target: 'http://localhost:8142', changeOrigin: true },
      // ---- ProcessMgt(流程制度) 8143 ----
      '/api/process': { target: 'http://localhost:8143', changeOrigin: true },
      '/api/document': { target: 'http://localhost:8143', changeOrigin: true },
      '/api/doc': { target: 'http://localhost:8143', changeOrigin: true },
      // ---- HRPlanningMgt 8095(人力规划) ----
      '/api/hr-planning': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/hrplanning': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/hrplan': { target: 'http://localhost:8095', changeOrigin: true },
      '/api/hr-plan': { target: 'http://localhost:8095', changeOrigin: true },
      // ---- AuditMgt(审计) 8145 ----
      '/api/audit': { target: 'http://localhost:8145', changeOrigin: true },
      // ---- DisciplineMgt(纪检监督) 8196 ----
      '/api/discipline': { target: 'http://localhost:8196', changeOrigin: true },
      // ---- PartyAffairsMgt(党务管理) 8150 ----
      '/api/party': { target: 'http://localhost:8150', changeOrigin: true },
      // ---- CultureMgt(企业文化 CCMS) 8160 ----
      '/api/culture': { target: 'http://localhost:8160', changeOrigin: true },
      // ---- TalentMgt 8094(人才测评/评估) ----
      '/api/talent-assessment': { target: 'http://localhost:8094', changeOrigin: true },
      '/api/talent-evaluation': { target: 'http://localhost:8094', changeOrigin: true },
      '/api/evaluation/special-approval': { target: 'http://localhost:8191', changeOrigin: true },
      '/api/evaluation': { target: 'http://localhost:8094', changeOrigin: true },
      // ---- ApiGateway 8080 ----
      '/api/gateway': { target: 'http://localhost:8080', changeOrigin: true },
      // ---- SysMgt 8111 ----
      '/api/user-mgt': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/role-mgt': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/menu-mgt': { target: 'http://localhost:8111', changeOrigin: true },
      '/api/dict-mgt': { target: 'http://localhost:8111', changeOrigin: true },
      // ---- SysMonitor 8112（/api/sys-mon 已在前面注册，此处仅保留 log-mgt） ----
      '/api/log-mgt': { target: 'http://localhost:8112', changeOrigin: true },
      // ---- MovementMgt 8094 (异动/调动) ----
      '/api/movement': { target: 'http://localhost:8094', changeOrigin: true },
      '/api/transfer': { target: 'http://localhost:8094', changeOrigin: true },
      // ---- SuccessPlanMgt 8085 (继任/ succession plan) ----
      '/api/success-plan': { target: 'http://localhost:8085', changeOrigin: true },
      '/api/succession': { target: 'http://localhost:8085', changeOrigin: true },
      // ---- 培训学考拆分：在线学习8194 / 在线考试8195（exams 须在 learn catch-all 之前） ----
      '/api/learn-to-exam': { target: 'http://localhost:8087', changeOrigin: true },
      '/api/exam': { target: 'http://localhost:8087', changeOrigin: true },
      '/api/learning': { target: 'http://localhost:8087', changeOrigin: true },
      '/api/learn/exams': { target: 'http://localhost:8195', changeOrigin: true },
      '/api/learnexam': { target: 'http://localhost:8195', changeOrigin: true },
      '/api/learn': { target: 'http://localhost:8194', changeOrigin: true },
      // ---- AttendanceMgt 8084 (排班/考勤) ----
      '/api/hr-roaster': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/roster': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/schedule': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/attendance': { target: 'http://localhost:8084', changeOrigin: true },
      // ---- ProjectMgt 8091 (项目管理) ----
      '/api/project': { target: 'http://localhost:8091', changeOrigin: true },
      // ---- EEHS 板块：4 个微服务 8106-8109（前端统一 /api/eehs 前缀，按二级路径分流） ----
      '^/api/eehs/env(?=/|$)': { target: 'http://localhost:8106', changeOrigin: true },
      '^/api/eehs/safety(?=/|$)': { target: 'http://localhost:8107', changeOrigin: true },
      '^/api/eehs/health(?=/|$)': { target: 'http://localhost:8108', changeOrigin: true },
      '^/api/eehs/energy(?=/|$)': { target: 'http://localhost:8109', changeOrigin: true }
    }
  },
  optimizeDeps: {
    include: ['@antv/x6', 'element-plus', 'echarts'],
    esbuildOptions: { target: 'es2020' }
  }
})

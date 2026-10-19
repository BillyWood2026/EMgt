<template>
  <div class="home-container">
    <div class="main-content">
      <div class="welcome-section">
        <h1>企业管理平台</h1>
        <p class="description">一体化企业管理系统，涵盖组织管理、人力资源、流程管控、审计管理、系统监控、改进项目、EEHS等核心业务领域</p>
      </div>

      <div class="modules-grid">
        <div class="module-card" @click="navigateTo('/org-mgt/structure')">
          <div class="module-icon org-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-160-280h320v-64h-320v64zm0-128h320v-64h-320v64zm0-128h320v-64h-320v64z"/>
            </svg>
          </div>
          <h3>组织管理</h3>
          <p>组织架构、岗位管理、编制标准、管理幅度等组织功能</p>
          <div class="module-stats">
            <span class="stat">13个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/ehrm/skill-management/index')">
          <div class="module-icon ehrm-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm0-640c-52.94 0-96 43.06-96 96s43.06 96 96 96 96-43.06 96-96-43.06-96-96-96zm0 128c-17.67 0-32-14.33-32-32s14.33-32 32-32 32 14.33 32 32-14.33 32-32 32zm0 256c-52.94 0-96 43.06-96 96v128h192v-128c0-52.94-43.06-96-96-96zm0 128c17.67 0 32 14.33 32 32v64h-64v-64c0-17.67 14.33-32 32-32z"/>
            </svg>
          </div>
          <h3>人力资源管理</h3>
          <p>技能认证、人才评估、招聘管理、培训发展等HR功能</p>
          <div class="module-stats">
            <span class="stat">20+个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/audit-mgt/plan')">
          <div class="module-icon audit-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-160-280h320v-64h-320v64zm0-128h320v-64h-320v64z"/>
            </svg>
          </div>
          <h3>审计管理</h3>
          <p>审计计划、项目管理、问题发现、审计报告等功能</p>
          <div class="module-stats">
            <span class="stat">4个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/sys-mgt-mon/sys-mgt/user')">
          <div class="module-icon sys-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-160-320h320v-64h-320v64zm0-128h320v-64h-320v64zm0-128h320v-64h-320v64z"/>
            </svg>
          </div>
          <h3>系统管理监控</h3>
          <p>用户管理、角色权限、系统监控、模块配置等管理功能</p>
          <div class="module-stats">
            <span class="stat">10个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/process-mgt/workbench/index')">
          <div class="module-icon process-mgt-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-320-384l160 128-160 128v-256zm320 384v-256l160 128-160 128z"/>
            </svg>
          </div>
          <h3>流程管理</h3>
          <p>流程设计、制度管理、审批监控、绩效评估全流程管控</p>
          <div class="module-stats">
            <span class="stat">11个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/ops/improve/proposal')">
          <div class="module-icon kaizen-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-160-384h320v64h-320v-64zm0 128h320v64h-320v-64zm0 128h192v64h-192v-64z"/>
            </svg>
          </div>
          <h3>改进项目管理</h3>
          <p>改善提案、改善实施、改善成果、项目管理全流程</p>
          <div class="module-stats">
            <span class="stat">7个功能</span>
          </div>
        </div>

        <div class="module-card" @click="navigateTo('/eehs/safety/inspection')">
          <div class="module-icon eehs-icon">
            <svg viewBox="0 0 1024 1024" fill="currentColor" width="48" height="48">
              <path d="M512 1024C229.25 1024 0 794.75 0 512S229.25 0 512 0s512 229.25 512 512-229.25 512-512 512zm0-96c229.69 0 416-186.31 416-416S741.69 96 512 96 96 282.31 96 512s186.31 416 416 416zm-256-256h512v-64h-512v64zm0 128h512v-64h-512v64zm0 128h384v-64h-384v64z"/>
            </svg>
          </div>
          <h3>EEHS管理</h3>
          <p>安全管理、环境管理、职业健康、能源管理一体化平台</p>
          <div class="module-stats">
            <span class="stat">17个功能</span>
          </div>
        </div>
      </div>

      <div class="quick-stats">
        <div class="stat-item">
          <div class="stat-value">8</div>
          <div class="stat-label">核心模块</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">100+</div>
          <div class="stat-label">功能页面</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">236</div>
          <div class="stat-label">路由地址</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">admin</div>
          <div class="stat-label">当前用户</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.main-content {
  padding: 40px 60px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 50px;
}

.welcome-section h1 {
  font-size: 36px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 15px;
}

.welcome-section .description {
  font-size: 16px;
  color: #666;
  max-width: 800px;
  margin: 0 auto;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 25px;
  max-width: 1400px;
  margin: 0 auto 50px;
}

.module-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  border-top: 4px solid transparent;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.12);
}

.module-card:nth-child(1) {
  border-top-color: #667eea;
}

.module-card:nth-child(2) {
  border-top-color: #f093fb;
}

.module-card:nth-child(3) {
  border-top-color: #4facfe;
}

.module-card:nth-child(4) {
  border-top-color: #43e97b;
}

.module-card:nth-child(5) {
  border-top-color: #fa709a;
}

.module-card:nth-child(6) {
  border-top-color: #fee140;
}

.module-card:nth-child(7) {
  border-top-color: #9c88ff;
}

.module-card:nth-child(8) {
  border-top-color: #00cec9;
}

.module-card:nth-child(9) {
  border-top-color: #fd79a8;
}

.module-icon {
  width: 70px;
  height: 70px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.org-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.ehrm-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.process-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.audit-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.sys-icon {
  background: linear-gradient(135deg, #fee140 0%, #fa709a 100%);
  color: white;
}

.process-mgt-icon {
  background: linear-gradient(135deg, #9c88ff 0%, #81ecec 100%);
  color: white;
}

.kaizen-icon {
  background: linear-gradient(135deg, #00cec9 0%, #81ecec 100%);
  color: white;
}

.eehs-icon {
  background: linear-gradient(135deg, #fd79a8 0%, #fab1a0 100%);
  color: white;
}

.module-card h3 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.module-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.module-stats {
  display: flex;
  gap: 15px;
}

.stat {
  font-size: 12px;
  color: #999;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 12px;
}

.quick-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>
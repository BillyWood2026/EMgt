<template>
  <div class="cul-placeholder">
    <CultureRoleBar />
    <el-card shadow="never" class="ph-card">
      <el-result icon="info" :title="title" sub-title="该页面属于 CCMS 企业文化管理系统，框架已就位，将按子模块分步骤实现">
        <template #extra>
          <el-tag size="small" type="success" effect="plain">{{ $route.path }}</el-tag>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import CultureRoleBar from './CultureRoleBar.vue'

const $route = useRoute()
const title = computed(() => $route.meta?.title || '页面建设中')
</script>

<style scoped>
.cul-placeholder { padding: 12px; }
.ph-card { min-height: 420px; display: flex; align-items: center; justify-content: center; }
</style>

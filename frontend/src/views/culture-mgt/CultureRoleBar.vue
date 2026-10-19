<template>
  <div class="culture-role-bar">
    <span class="role-label">当前企业文化角色（演示切换）：</span>
    <el-radio-group v-model="current" size="small" @change="onChange">
      <el-radio-button v-for="r in CULTURE_ROLES" :key="r.value" :value="r.value">{{ r.label }}</el-radio-button>
    </el-radio-group>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CULTURE_ROLES, cultureAuth, setCultureRole } from './cultureAuth'

const current = computed({
  get: () => cultureAuth.role,
  set: v => setCultureRole(v)
})

function onChange(v) {
  setCultureRole(v)
}
</script>

<style scoped>
.culture-role-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f3f8f5;
  border: 1px dashed #b9d8c6;
  border-radius: 6px;
  flex-wrap: wrap;
}
.role-label {
  color: #4a6b58;
  font-size: 13px;
  white-space: nowrap;
}
</style>

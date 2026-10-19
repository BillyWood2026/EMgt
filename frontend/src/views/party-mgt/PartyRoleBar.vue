<template>
  <div class="party-role-bar">
    <span class="role-label">当前党务角色（演示切换）：</span>
    <el-radio-group v-model="current" size="small" @change="onChange">
      <el-radio-button v-for="r in PARTY_ROLES" :key="r.value" :value="r.value">{{ r.label }}</el-radio-button>
    </el-radio-group>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PARTY_ROLES, partyAuth, setPartyRole } from './partyAuth'

const current = computed({
  get: () => partyAuth.role,
  set: v => setPartyRole(v)
})

function onChange(v) {
  setPartyRole(v)
}
</script>

<style scoped>
.party-role-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f4f7fb;
  border: 1px dashed #c6d2e1;
  border-radius: 6px;
  flex-wrap: wrap;
}
.role-label {
  color: #5a6a7e;
  font-size: 13px;
  white-space: nowrap;
}
</style>

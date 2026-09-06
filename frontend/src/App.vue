<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div class="brand">测发模式仿真验证</div>
      <nav>
        <RouterLink to="/flows">流程管理</RouterLink>
        <RouterLink to="/simulation-history">仿真记录</RouterLink>
      </nav>
    </header>
    <main class="page-container">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
defineProps({ src: { type: String, required: true } })
</script>

<template>
  <iframe class="legacy-frame" :src="src" title="原版页面" />
</template>

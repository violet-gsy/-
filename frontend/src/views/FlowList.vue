<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getActivityDiagrams, getLaunchModes, updateLaunchMode } from '../api/activityDiagram'

const router = useRouter()
const diagrams = ref([])
const modes = ref([])
const keyword = ref('')
const modeFilter = ref('')
const loading = ref(false)
const message = ref('')

const filtered = computed(() => diagrams.value.filter(item => {
  const name = item.activityDiagramName || item.name || ''
  return (!keyword.value || name.toLowerCase().includes(keyword.value.toLowerCase())) &&
    (!modeFilter.value || item.launchModeId === modeFilter.value)
}))

async function load() {
  loading.value = true
  try {
    const [page, enabledModes] = await Promise.all([getActivityDiagrams(), getLaunchModes()])
    diagrams.value = page?.records || page?.data?.records || page?.data || []
    modes.value = enabledModes?.data || enabledModes || []
  } catch (error) { message.value = error.message || '加载失败' } finally { loading.value = false }
}

async function saveMode(item, event) {
  try {
    await updateLaunchMode(item.activityDiagramId || item.id, event.target.value)
    item.launchModeId = event.target.value
    message.value = '测发模式已保存'
  } catch (error) { message.value = error.message || '保存失败' }
}

onMounted(load)
</script>

<template>
  <section class="card">
    <div class="page-title"><div><h1>测发流程管理</h1><p>Vue 版流程列表与测发模式关联</p></div><button class="primary" @click="load">刷新</button></div>
    <div class="toolbar"><input v-model="keyword" placeholder="搜索流程名称" /><select v-model="modeFilter"><option value="">全部模式</option><option v-for="mode in modes" :key="mode.launchModeId" :value="mode.launchModeId">{{ mode.launchModeName }}</option></select><span class="muted">共 {{ filtered.length }} 条</span></div>
    <div v-if="message" class="notice">{{ message }}</div>
    <div v-if="loading" class="empty">正在加载...</div>
    <table v-else><thead><tr><th>流程名称</th><th>测发模式</th><th>更新时间</th><th>操作</th></tr></thead><tbody><tr v-for="item in filtered" :key="item.activityDiagramId || item.id"><td>{{ item.activityDiagramName || item.name }}</td><td><select :value="item.launchModeId || ''" @change="saveMode(item, $event)"><option value="">未设置</option><option v-for="mode in modes" :key="mode.launchModeId" :value="mode.launchModeId">{{ mode.launchModeName }}</option></select></td><td>{{ item.updateTime || '-' }}</td><td><button @click="router.push(`/viewer/${item.activityDiagramId || item.id}`)">打开看板</button></td></tr><tr v-if="!filtered.length"><td colspan="4" class="empty">暂无流程数据</td></tr></tbody></table>
  </section>
</template>

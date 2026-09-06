<script setup>
import { computed, onMounted, ref } from 'vue'
import { getNodeRecords, getSimulationRuns } from '../api/simulation'

const runs = ref([]); const selected = ref(null); const compare = ref([]); const keyword = ref(''); const tab = ref('detail'); const loading = ref(false)
const filtered = computed(() => runs.value.filter(run => !keyword.value || (run.diagramName || run.runName || '').includes(keyword.value)))
async function load() { loading.value = true; try { const page = await getSimulationRuns(); runs.value = page?.records || page?.data?.records || page?.data || [] } finally { loading.value = false } }
async function selectRun(run) { selected.value = { ...run, nodes: await getNodeRecords(run.id || run.runId) } }
function toggle(run) { const id = run.id || run.runId; compare.value = compare.value.some(x => (x.id || x.runId) === id) ? compare.value.filter(x => (x.id || x.runId) !== id) : [...compare.value, run] }
onMounted(load)
</script>

<template><section class="card"><div class="page-title"><div><h1>仿真记录与多方案对比</h1><p>查看运行结果、节点记录并进行方案对比</p></div><button class="primary" @click="load">刷新</button></div><div class="toolbar"><input v-model="keyword" placeholder="搜索流程或运行名称" /><span class="muted">{{ filtered.length }} 条记录</span></div><div class="history-layout"><aside class="run-list"><div v-if="loading" class="empty">正在加载...</div><button v-for="run in filtered" :key="run.id || run.runId" class="run-item" @click="selectRun(run)"><span>{{ run.runName || run.diagramName || run.id }}</span><small>{{ run.status || '-' }}</small><input type="checkbox" :checked="compare.some(x => (x.id || x.runId) === (run.id || run.runId))" @click.stop="toggle(run)" /></button><div v-if="!filtered.length" class="empty">暂无记录</div></aside><main class="history-detail"><div class="tabs"><button :class="{active: tab === 'detail'}" @click="tab = 'detail'">运行详情</button><button :class="{active: tab === 'compare'}" @click="tab = 'compare'">方案对比（{{ compare.length }}）</button></div><pre v-if="tab === 'detail'">{{ selected ? JSON.stringify(selected, null, 2) : '请选择一条运行记录' }}</pre><pre v-else>{{ JSON.stringify(compare, null, 2) }}</pre></main></div></section></template>

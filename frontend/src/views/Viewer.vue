<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import DiagramCanvas from '../components/DiagramCanvas.vue'
import { useWebSocket } from '../composables/useWebSocket'
import { startSimulation } from '../api/simulation'

const route = useRoute(); const diagram = ref({}); const selectedNode = ref(null); const run = ref(null); const busy = ref(false); const message = ref('')
const diagramId = computed(() => route.params.diagramId)
const { connected, lastError } = useWebSocket(event => { message.value = `收到实时消息：${event.type || 'update'}` })
async function start() { busy.value = true; try { run.value = await startSimulation({ diagramId: diagramId.value, diagramName: route.query.name || '' }); message.value = '仿真已启动' } catch (error) { message.value = error.message || '启动失败' } finally { busy.value = false } }
</script>

<template><section class="viewer card"><div class="page-title"><div><h1>{{ route.query.name || '流程仿真看板' }}</h1><p>流程 ID：{{ diagramId }}</p></div><div class="actions"><span :class="['status', connected ? 'online' : 'offline']">{{ connected ? 'WebSocket 已连接' : 'WebSocket 未连接' }}</span><button class="primary" :disabled="busy" @click="start">{{ busy ? '启动中...' : '开始仿真' }}</button></div></div><div v-if="message || lastError" class="notice">{{ lastError || message }}</div><div class="viewer-grid"><div class="diagram-wrap"><DiagramCanvas :model="diagram" @select="selectedNode = $event" /></div><aside class="node-panel"><h3>节点信息</h3><pre>{{ selectedNode ? JSON.stringify(selectedNode, null, 2) : '点击流程节点查看详情' }}</pre><h3 v-if="run">当前运行</h3><pre v-if="run">{{ JSON.stringify(run, null, 2) }}</pre></aside></div></section></template>

<script setup>
import * as d3 from 'd3'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({ model: { type: Object, default: () => ({}) } })
const emit = defineEmits(['select'])
const svgRef = ref(null)
let zoomBehavior

function render() {
  if (!svgRef.value) return
  const svg = d3.select(svgRef.value)
  svg.selectAll('*').remove()
  const width = svgRef.value.clientWidth || 900
  const height = svgRef.value.clientHeight || 580
  const nodes = props.model.nodes || props.model.children || []
  const links = props.model.links || props.model.edges || []
  const group = svg.append('g')
  zoomBehavior = d3.zoom().scaleExtent([0.1, 6]).on('zoom', event => group.attr('transform', event.transform))
  svg.call(zoomBehavior)
  group.selectAll('line').data(links).enter().append('line')
    .attr('x1', d => d.source?.x ?? 80).attr('y1', d => d.source?.y ?? 80)
    .attr('x2', d => d.target?.x ?? 260).attr('y2', d => d.target?.y ?? 180)
    .attr('stroke', '#94a3b8')
  const node = group.selectAll('g.node').data(nodes).enter().append('g').attr('class', 'node')
    .attr('transform', (d, i) => `translate(${d.x ?? 100 + (i % 5) * 150},${d.y ?? 100 + Math.floor(i / 5) * 100})`)
    .style('cursor', 'pointer').on('click', (_, d) => emit('select', d))
  node.append('rect').attr('x', -58).attr('y', -24).attr('width', 116).attr('height', 48).attr('rx', 8)
    .attr('fill', '#eff6ff').attr('stroke', '#3b82f6')
  node.append('text').attr('text-anchor', 'middle').attr('dy', 5).attr('fill', '#1e3a8a')
    .text(d => d.name || d.nodeName || d.id || '节点')
  if (!nodes.length) {
    svg.append('text').attr('x', width / 2).attr('y', height / 2).attr('text-anchor', 'middle').attr('fill', '#64748b').text('暂无流程图数据')
  }
}

watch(() => props.model, render, { deep: true })
onMounted(() => nextTick(render))
onBeforeUnmount(() => { if (svgRef.value && zoomBehavior) d3.select(svgRef.value).on('.zoom', null) })
</script>

<template><svg ref="svgRef" class="diagram-canvas" viewBox="0 0 1000 650" preserveAspectRatio="xMidYMid meet" /></template>

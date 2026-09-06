import { createRouter, createWebHistory } from 'vue-router'
import FlowList from '../views/FlowList.vue'
import SimulationHistory from '../views/SimulationHistory.vue'
import Viewer from '../views/Viewer.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/flows' },
    { path: '/flows', component: FlowList },
    { path: '/simulation-history', component: SimulationHistory },
    { path: '/viewer/:diagramId', component: Viewer, props: true }
  ]
})

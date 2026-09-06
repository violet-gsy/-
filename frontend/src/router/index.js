import { createRouter, createWebHistory } from 'vue-router'
import FlowList from '../views/FlowList.vue'
import SimulationHistory from '../views/SimulationHistory.vue'
import Viewer from '../views/Viewer.vue'
import LegacyPage from '../views/LegacyPage.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/flows' },
    { path: '/flows', component: LegacyPage, props: { src: '/legacy/index.html' } },
    { path: '/simulation-history', component: LegacyPage, props: { src: '/legacy/simulation-history.html' } },
    {
      path: '/viewer/:diagramId',
      component: LegacyPage,
      props: route => ({
        src: `/legacy/viewer.html?${new URLSearchParams({
          diagramId: route.params.diagramId,
          ...route.query
        }).toString()}`
      })
    },
    { path: '/vue/flows', component: FlowList },
    { path: '/vue/simulation-history', component: SimulationHistory },
    { path: '/vue/viewer/:diagramId', component: Viewer, props: true }
  ]
})

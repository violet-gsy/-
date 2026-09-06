import http, { unwrap } from './http'

export async function getSimulationRuns(params = {}) {
  return unwrap(await http.get('/simulationRun/page', {
    params: { current: 1, size: 500, ...params }
  }))
}

export async function getNodeRecords(runId) {
  return unwrap(await http.get(`/simulationNode/records/${encodeURIComponent(runId)}`))
}

export async function startSimulation(payload) {
  return unwrap(await http.post('/simulationRun/start', payload))
}

export async function endSimulation(id) {
  return unwrap(await http.post('/simulationRun/end', null, { params: { id } }))
}

import http, { unwrap } from './http'

export async function getActivityDiagrams(params = {}) {
  return unwrap(await http.get('/activity-diagram/page', {
    params: { current: 1, size: 500, ...params }
  }))
}

export async function getLaunchModes() {
  return unwrap(await http.get('/launchMode/enabled'))
}

export async function updateLaunchMode(diagramId, modeId) {
  return unwrap(await http.put('/activity-diagram/update-launch-mode', null, {
    params: { diagramId, modeId: modeId || undefined }
  }))
}

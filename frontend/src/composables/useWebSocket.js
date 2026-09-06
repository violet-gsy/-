import { onBeforeUnmount, onMounted, ref } from 'vue'

export function useWebSocket(onMessage) {
  const connected = ref(false)
  const lastError = ref('')
  let socket

  function connect() {
    const url = import.meta.env.VITE_WS_URL || 'ws://127.0.0.1:8765'
    socket = new WebSocket(url)
    socket.onopen = () => { connected.value = true; lastError.value = '' }
    socket.onmessage = event => {
      try { onMessage(JSON.parse(event.data)) } catch { onMessage(event.data) }
    }
    socket.onerror = () => { lastError.value = 'WebSocket 连接失败' }
    socket.onclose = () => { connected.value = false }
  }

  function send(message) {
    if (socket?.readyState === WebSocket.OPEN) socket.send(JSON.stringify(message))
  }

  onMounted(connect)
  onBeforeUnmount(() => socket?.close())

  return { connected, lastError, send }
}

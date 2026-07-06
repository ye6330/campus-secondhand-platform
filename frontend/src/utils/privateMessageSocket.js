export function createPrivateMessageSocket(token, onPrivateMessage) {
  if (!token) return null
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const socketUrl = `${protocol}//${window.location.host}/ws/private?token=${encodeURIComponent(token)}`
  const socket = new WebSocket(socketUrl)

  socket.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload?.type === 'private_message' && payload.data) {
        onPrivateMessage?.(payload.data)
      }
    } catch (e) {
      // ignore malformed push payload
    }
  }

  return socket
}

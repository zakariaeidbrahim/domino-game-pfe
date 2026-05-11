import { Client } from '@stomp/stompjs'
import type { GameState } from './types'
import { config } from '../config'

export function connectGameTopic(gameId: number, token: string, onState: (s: GameState) => void) {
  const client = new Client({
    brokerURL: config.wsUrl,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 1000,
    onConnect: () => {
      client.subscribe(`/topic/games/${gameId}`, (msg) => {
        try {
          onState(JSON.parse(msg.body) as GameState)
        } catch {
          // ignore bad payload
        }
      })
    },
  })

  client.activate()
  return () => client.deactivate()
}


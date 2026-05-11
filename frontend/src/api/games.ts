import { api } from './http'
import type { CreateGameResponse, GameState } from './types'

export async function createGame() {
  return api<CreateGameResponse>('/api/games', { method: 'POST', body: '{}' })
}

export async function joinGame(joinCode: string) {
  return api<CreateGameResponse>('/api/games/join', {
    method: 'POST',
    body: JSON.stringify({ joinCode }),
  })
}

export async function getGame(gameId: number) {
  return api<GameState>(`/api/games/${gameId}`)
}

export async function playMove(gameId: number, handIndex: number, side?: 'LEFT' | 'RIGHT') {
  return api<GameState>(`/api/games/${gameId}/play`, {
    method: 'POST',
    body: JSON.stringify({ handIndex, side }),
  })
}

export async function drawOrPass(gameId: number) {
  return api<GameState>(`/api/games/${gameId}/draw`, { method: 'POST', body: '{}' })
}


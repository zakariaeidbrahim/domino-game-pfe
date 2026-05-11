export type DominoTile = { a: number; b: number }

export type GameState = {
  joinCode: string
  player1: string
  player2?: string | null
  currentTurn?: string | null
  stock: DominoTile[]
  chain: DominoTile[]
  hand1: DominoTile[]
  hand2: DominoTile[]
  leftEnd?: number | null
  rightEnd?: number | null
  score1: number
  score2: number
  finished: boolean
  winner?: string | null
  finishReason?: string | null
}

export type AuthResponse = { token: string; username: string }
export type CreateGameResponse = { gameId: number; joinCode: string }


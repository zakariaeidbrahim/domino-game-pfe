import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import type { GameState } from '../api/types'
import { drawOrPass, getGame, playMove } from '../api/games'
import { connectGameTopic } from '../api/ws'

function Tile({ a, b }: { a: number; b: number }) {
  return (
    <span className="tile">
      <strong>{a}</strong> | <strong>{b}</strong>
    </span>
  )
}

export default function GamePage() {
  const { gameId } = useParams()
  const id = Number(gameId)
  const nav = useNavigate()
  const [params] = useSearchParams()
  const joinCode = params.get('code')

  const [state, setState] = useState<GameState | null>(null)
  const [error, setError] = useState<string | null>(null)
  const token = useMemo(() => localStorage.getItem('token') || '', [])
  const username = localStorage.getItem('username') || ''

  useEffect(() => {
    let stop: (() => void) | null = null
    ;(async () => {
      try {
        setState(await getGame(id))
        stop = connectGameTopic(id, token, (s) => setState(s))
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Erreur')
      }
    })()
    return () => {
      if (stop) stop()
    }
  }, [id, token])

  const myHand = useMemo(() => {
    if (!state) return []
    return username === state.player1 ? state.hand1 : state.hand2
  }, [state, username])

  const canPlay = state?.currentTurn === username && !state?.finished

  async function onPlay(idx: number, side?: 'LEFT' | 'RIGHT') {
    setError(null)
    try {
      const s = await playMove(id, idx, side)
      setState(s)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onDraw() {
    setError(null)
    try {
      const s = await drawOrPass(id)
      setState(s)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  function back() {
    nav('/lobby')
  }

  return (
    <div className="container">
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="title" style={{ margin: 0 }}>Partie</h1>
          <div className="muted">
            Game #{id} {joinCode ? `— Code: ${joinCode}` : null}
          </div>
        </div>
        <button onClick={back}>Retour lobby</button>
      </div>

      {!state ? (
        <p className="muted" style={{ marginTop: 12 }}>Chargement...</p>
      ) : (
        <div className="grid" style={{ marginTop: 12 }}>
          <div className="card">
            <h2 style={{ marginTop: 0 }}>État</h2>
            <p className="muted" style={{ marginTop: 0 }}>
              Joueurs: <strong>{state.player1}</strong> vs <strong>{state.player2 ?? '...en attente'}</strong>
            </p>
            <p className="muted">
              Tour: <strong>{state.currentTurn ?? '-'}</strong>
            </p>
            <p className="muted">Stock: {state.stock.length} dominos</p>

            {state.finished ? (
              <div>
                <p>
                  Résultat: <strong>{state.winner}</strong> ({state.finishReason})
                </p>
              </div>
            ) : null}

            <div className="row" style={{ marginTop: 12 }}>
              <button onClick={onDraw} disabled={!canPlay}>
                Piocher / Pass
              </button>
            </div>
          </div>

          <div className="card">
            <h2 style={{ marginTop: 0 }}>Chaîne</h2>
            <div className="row">
              {state.chain.length === 0 ? <span className="muted">Vide</span> : null}
              {state.chain.map((t, i) => (
                <Tile key={i} a={t.a} b={t.b} />
              ))}
            </div>
            <p className="muted" style={{ marginTop: 12 }}>
              Extrémités: {state.leftEnd ?? '-'} / {state.rightEnd ?? '-'}
            </p>
          </div>

          <div className="card" style={{ gridColumn: '1 / -1' }}>
            <h2 style={{ marginTop: 0 }}>Ma main</h2>
            <p className="muted" style={{ marginTop: 0 }}>
              Clique un domino pour jouer. Si possible, tu peux choisir LEFT/RIGHT.
            </p>
            <div className="row">
              {myHand.map((t, idx) => (
                <span className="tile" key={idx}>
                  <Tile a={t.a} b={t.b} />
                  <button onClick={() => onPlay(idx)} disabled={!canPlay}>
                    Jouer
                  </button>
                  <button onClick={() => onPlay(idx, 'LEFT')} disabled={!canPlay}>
                    LEFT
                  </button>
                  <button onClick={() => onPlay(idx, 'RIGHT')} disabled={!canPlay}>
                    RIGHT
                  </button>
                </span>
              ))}
            </div>
          </div>
        </div>
      )}

      {error ? (
        <p style={{ color: '#ffb4b4', marginTop: 12 }}>{error}</p>
      ) : null}
    </div>
  )
}


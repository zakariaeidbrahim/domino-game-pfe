import { useState } from 'react'
import { createGame, joinGame } from '../api/games'
import { useNavigate } from 'react-router-dom'

export default function LobbyPage() {
  const [joinCode, setJoinCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const nav = useNavigate()
  const username = localStorage.getItem('username')

  async function onCreate() {
    setError(null)
    try {
      const res = await createGame()
      nav(`/game/${res.gameId}?code=${res.joinCode}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  async function onJoin() {
    setError(null)
    try {
      const res = await joinGame(joinCode)
      nav(`/game/${res.gameId}?code=${res.joinCode}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    nav('/login')
  }

  return (
    <div className="container">
      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 className="title" style={{ margin: 0 }}>Lobby</h1>
        <div className="row" style={{ alignItems: 'center' }}>
          <span className="muted">Connecté: {username}</span>
          <button onClick={logout}>Logout</button>
        </div>
      </div>

      <div className="grid" style={{ marginTop: 12 }}>
        <div className="card">
          <h2 style={{ marginTop: 0 }}>Créer une partie</h2>
          <p className="muted">Tu obtiens un code pour inviter un ami.</p>
          <button onClick={onCreate}>Créer</button>
        </div>

        <div className="card">
          <h2 style={{ marginTop: 0 }}>Rejoindre une partie</h2>
          <p className="muted">Entre le code de la partie.</p>
          <div className="row">
            <input value={joinCode} onChange={(e) => setJoinCode(e.target.value)} placeholder="ex: A1B2C3" />
            <button onClick={onJoin}>Rejoindre</button>
          </div>
        </div>
      </div>

      {error ? (
        <p style={{ color: '#ffb4b4', marginTop: 12 }}>{error}</p>
      ) : null}
    </div>
  )
}


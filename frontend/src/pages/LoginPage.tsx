import { useState } from 'react'
import { login, register } from '../api/auth'
import { useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const nav = useNavigate()

  async function submit() {
    setError(null)
    try {
      const fn = mode === 'login' ? login : register
      const res = await fn(username, password)
      localStorage.setItem('token', res.token)
      localStorage.setItem('username', res.username)
      nav('/lobby')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erreur')
    }
  }

  return (
    <div className="container">
      <div className="card" style={{ maxWidth: 520, margin: '0 auto' }}>
        <h1 className="title">DominoGame</h1>
        <p className="muted">PFE2 + PFE3 (Spring Boot + MySQL + React)</p>

        <div className="row" style={{ marginTop: 12 }}>
          <button onClick={() => setMode('login')} disabled={mode === 'login'}>
            Connexion
          </button>
          <button onClick={() => setMode('register')} disabled={mode === 'register'}>
            Inscription
          </button>
        </div>

        <div className="row" style={{ marginTop: 12 }}>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="username"
            style={{ flex: 1, minWidth: 220 }}
          />
          <input
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="password"
            type="password"
            style={{ flex: 1, minWidth: 220 }}
          />
        </div>

        <div className="row" style={{ marginTop: 12 }}>
          <button onClick={submit}>{mode === 'login' ? 'Se connecter' : "S'inscrire"}</button>
        </div>

        {error ? (
          <p style={{ color: '#ffb4b4', marginTop: 12 }}>
            {error}
          </p>
        ) : null}
      </div>
    </div>
  )
}


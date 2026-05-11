import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import LobbyPage from './pages/LobbyPage'
import GamePage from './pages/GamePage'

function isAuthed() {
  return Boolean(localStorage.getItem('token'))
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to={isAuthed() ? '/lobby' : '/login'} replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/lobby" element={isAuthed() ? <LobbyPage /> : <Navigate to="/login" replace />} />
      <Route path="/game/:gameId" element={isAuthed() ? <GamePage /> : <Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}


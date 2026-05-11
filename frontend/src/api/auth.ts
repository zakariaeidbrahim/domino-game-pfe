import { api } from './http'
import type { AuthResponse } from './types'

export async function register(username: string, password: string) {
  return api<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export async function login(username: string, password: string) {
  return api<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}


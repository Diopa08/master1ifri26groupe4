import axios from 'axios'
import type { LoginRequest, LoginResponse } from '../types'

// Auth utilise axios directement (pas d'intercepteur JWT sur /login)
export const login = (data: LoginRequest) =>
  axios.post<LoginResponse>('/api/auth/login', data).then((r) => r.data)

export const register = (data: { email: string; password: string }) =>
  axios.post('/api/auth/register', data).then((r) => r.data)

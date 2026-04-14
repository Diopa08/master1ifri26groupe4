import api from './client'

export const getUsers = () =>
  api.get('/auth/users').then(r => r.data)

/** Création d'un utilisateur avec rôle — réservé admin */
export const createUser = (data: { email: string; password: string; role: string }) =>
  api.post('/auth/users', data).then(r => r.data)

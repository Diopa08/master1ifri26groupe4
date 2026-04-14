import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import type { ReactNode } from 'react'

interface Props {
  roles: string[]
  children: ReactNode
}

export default function RoleRoute({ roles, children }: Props) {
  const { isAuthenticated, hasRole } = useAuth()

  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (!roles.some(r => hasRole(r))) return <Navigate to="/" replace />

  return <>{children}</>
}

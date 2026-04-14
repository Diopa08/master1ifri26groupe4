import api from './client'

export interface Notification {
  id: number
  type: string
  title: string
  message: string
  targetRole: string
  read: boolean
  referenceId?: number
  referenceType?: string
  createdAt: string
}

export const getNotifications = () =>
  api.get<Notification[]>('/notifications').then(r => r.data)

export const getUnreadCount = () =>
  api.get<{ count: number }>('/notifications/unread/count').then(r => r.data.count)

export const markAsRead = (id: number) =>
  api.put<Notification>(`/notifications/${id}/read`).then(r => r.data)

export const markAllAsRead = () =>
  api.put('/notifications/read-all').then(r => r.data)

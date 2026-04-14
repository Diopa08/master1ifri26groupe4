import api from './client'

export type ProductionStatus = 'PLANNED' | 'IN_PROGRESS' | 'QUALITY_CHECK' | 'COMPLETED' | 'CANCELLED'

export interface ProductionOrder {
  id: number
  referenceNumber: string
  productId: number
  productName: string
  quantityRequested: number
  quantityProduced: number
  status: ProductionStatus
  priority: string
  plannedStartDate?: string
  actualStartDate?: string
  completedDate?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface CreateProductionRequest {
  productId: number
  productName: string
  quantityRequested: number
  priority?: string
  plannedStartDate?: string
  notes?: string
}

export const getProductionOrders = () =>
  api.get<ProductionOrder[]>('/production/orders').then(r => r.data)

export const createProductionOrder = (data: CreateProductionRequest) =>
  api.post<ProductionOrder>('/production/orders', data).then(r => r.data)

export const startProduction = (id: number) =>
  api.put<ProductionOrder>(`/production/orders/${id}/start`).then(r => r.data)

export const qualityCheck = (id: number) =>
  api.put<ProductionOrder>(`/production/orders/${id}/quality-check`).then(r => r.data)

export const completeProduction = (id: number, quantityProduced?: number) =>
  api.put<ProductionOrder>(`/production/orders/${id}/complete`, { quantityProduced }).then(r => r.data)

export const cancelProduction = (id: number, reason?: string) =>
  api.put<ProductionOrder>(`/production/orders/${id}/cancel`, { reason }).then(r => r.data)

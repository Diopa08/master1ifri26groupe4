import client from './client'
import type { Order, CreateOrderRequest, ApiResponse } from '../types'

export const getOrders = () => client.get<ApiResponse<Order[]>>('/orders').then(r => r.data.data)
export const getOrder = (id: number) => client.get<ApiResponse<Order>>(`/orders/${id}`).then(r => r.data.data)
export const getOrdersByClient = (clientId: number) => client.get<ApiResponse<Order[]>>(`/orders/client/${clientId}`).then(r => r.data.data)
export const createOrder = (data: CreateOrderRequest) => client.post<ApiResponse<Order>>('/orders', data).then(r => r.data.data)
export const validateOrder = (id: number) => client.put<ApiResponse<Order>>(`/orders/${id}/validate`).then(r => r.data.data)
export const updateOrderStatus = (id: number, newStatus: string, reason?: string) =>
  client.put<ApiResponse<Order>>(`/orders/${id}/status`, { newStatus, reason }).then(r => r.data.data)
export const cancelOrder = (id: number, reason: string) =>
  client.delete<ApiResponse<Order>>(`/orders/${id}/cancel`, { params: { reason } }).then(r => r.data.data)

import client from './client'
import type { Stock, StockRequest, StockUpdateRequest, StockCheckResponse, StockMovement } from '../types'

export const getStocks = () => client.get<Stock[]>('/inventory/stocks').then(r => r.data)
export const getStock = (id: number) => client.get<Stock>(`/inventory/stocks/${id}`).then(r => r.data)
export const getByProduct = (productId: number) => client.get<Stock[]>(`/inventory/stocks/product/${productId}`).then(r => r.data)
export const getByWarehouse = (warehouseId: number) => client.get<Stock[]>(`/inventory/stocks/warehouse/${warehouseId}`).then(r => r.data)
export const createStock = (data: StockRequest) => client.post<Stock>('/inventory/stocks', data).then(r => r.data)
export const updateStock = (id: number, data: StockUpdateRequest) => client.put<Stock>(`/inventory/stocks/${id}/update`, data).then(r => r.data)
export const checkAvailability = (productId: number, warehouseId: number, quantity: number) =>
  client.get<StockCheckResponse>('/inventory/check', { params: { productId, warehouseId, quantity } }).then(r => r.data)
export const getMovements = (stockId: number) => client.get<StockMovement[]>(`/inventory/movements/${stockId}`).then(r => r.data)

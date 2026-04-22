// ─── Auth ─────────────────────────────────────────────
export interface LoginRequest { email: string; password: string }
export interface LoginResponse { token: string }

// ─── Product ──────────────────────────────────────────
export interface Product {
  id: number
  name: string
  description: string
  category: string
  unitPrice: number
  unit: string
}
export interface ProductRequest {
  name: string
  description: string
  category: string
  unitPrice: number
  unit: string
}

// ─── Stock / Inventory ────────────────────────────────
export interface Stock {
  id: number
  productId: number
  warehouseId: number
  quantity: number
  threshold: number
}
export interface StockRequest {
  productId: number
  warehouseId: number
  quantity: number
  threshold: number
}
export interface StockUpdateRequest {
  quantity: number
  reason: string
  type: 'IN' | 'OUT'
}
export interface StockCheckResponse {
  available: boolean
  currentQuantity: number
  requestedQuantity: number
}
export interface StockMovement {
  id: number
  stockId: number
  type: 'IN' | 'OUT'
  quantity: number
  reason: string
  date: string
}

// ─── Order ────────────────────────────────────────────
export type OrderStatus = 'PENDING' | 'VALIDATED' | 'IN_PRODUCTION' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'

export interface OrderItem {
  id?: number
  productId: number
  productName: string
  quantity: number
  unitPrice: number
  subtotal?: number
}
export interface Order {
  id: number
  orderNumber: string
  clientId: number
  status: OrderStatus
  items: OrderItem[]
  totalAmount: number
  shippingAddress: string
  notes?: string
  createdAt: string
  updatedAt: string
}
export interface CreateOrderRequest {
  clientId: number
  shippingAddress: string
  notes?: string
  items: Omit<OrderItem, 'id' | 'subtotal'>[]
}

// ─── Invoice / Billing ────────────────────────────────
export type InvoiceStatus = 'UNPAID' | 'PAID' | 'PARTIAL' | 'OVERDUE' | 'CANCELLED'
export type PaymentMethod = 'CASH' | 'BANK_TRANSFER' | 'CHECK' | 'MOBILE_MONEY'

export interface Invoice {
  id: number
  invoiceNumber: string
  orderId: number
  orderNumber: string
  clientId: number
  totalAmount: number
  netAmount: number
  taxAmount: number
  status: InvoiceStatus
  paymentMethod?: PaymentMethod
  dueDate?: string
  paidAt?: string
  notes?: string
  createdAt: string
}
export interface RecordPaymentRequest {
  amount: number
  paymentMethod: PaymentMethod
  notes?: string
}

// ─── API Generic ──────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

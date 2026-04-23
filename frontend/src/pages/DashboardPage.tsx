import { useEffect, useState } from 'react'
import { getProducts } from '../api/products'
import { getOrders } from '../api/orders'
import { getInvoices } from '../api/billing'
import { getStocks } from '../api/inventory'
import { Package, ShoppingCart, FileText, Warehouse, TrendingUp, AlertTriangle } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Order, Invoice, Stock } from '../types'

interface Stats {
  products: number
  orders: number
  invoices: number
  stocks: number
  revenue: number
  pendingOrders: number
  lowStock: number
  unpaidInvoices: number
}

function StatCard({ label, value, icon: Icon, color, to }: {
  label: string; value: number | string; icon: React.ElementType; color: string; to: string
}) {
  return (
    <Link to={to} className="bg-white rounded-xl shadow-sm p-6 flex items-center gap-4 hover:shadow-md transition-shadow">
      <div className={`p-3 rounded-xl ${color}`}>
        <Icon size={24} className="text-white" />
      </div>
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className="text-2xl font-bold text-gray-800">{value}</p>
      </div>
    </Link>
  )
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats | null>(null)
  const [recentOrders, setRecentOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.allSettled([getProducts(), getOrders(), getInvoices(), getStocks()])
      .then(([p, o, i, s]) => {
        const products = p.status === 'fulfilled' ? p.value : []
        const orders   = o.status === 'fulfilled' ? o.value : []
        const invoices = i.status === 'fulfilled' ? i.value : []
        const stocks   = s.status === 'fulfilled' ? s.value : []

        const revenue = (invoices as Invoice[]).filter(inv => inv.status === 'PAID').reduce((acc, inv) => acc + inv.totalAmount, 0)
        const pendingOrders = (orders as Order[]).filter(ord => ord.status === 'PENDING').length
        const lowStock = (stocks as Stock[]).filter(st => st.quantity <= st.threshold).length
        const unpaidInvoices = (invoices as Invoice[]).filter(inv => inv.status === 'UNPAID' || inv.status === 'OVERDUE').length

        setStats({
          products: products.length,
          orders: orders.length,
          invoices: invoices.length,
          stocks: stocks.length,
          revenue,
          pendingOrders,
          lowStock,
          unpaidInvoices,
        })
        setRecentOrders((orders as Order[]).slice(0, 5))
      })
      .finally(() => setLoading(false))
  }, [])

  const statusColors: Record<string, string> = {
    PENDING:       'bg-yellow-100 text-yellow-800',
    VALIDATED:     'bg-blue-100 text-blue-800',
    IN_PRODUCTION: 'bg-purple-100 text-purple-800',
    SHIPPED:       'bg-indigo-100 text-indigo-800',
    DELIVERED:     'bg-green-100 text-green-800',
    CANCELLED:     'bg-red-100 text-red-800',
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
    </div>
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Tableau de bord</h1>
        <p className="text-gray-500 text-sm mt-1">Vue d'ensemble de l'activité</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Produits"   value={stats?.products ?? 0}  icon={Package}      color="bg-blue-500"   to="/products" />
        <StatCard label="Commandes"  value={stats?.orders ?? 0}    icon={ShoppingCart} color="bg-purple-500" to="/orders" />
        <StatCard label="Factures"   value={stats?.invoices ?? 0}  icon={FileText}     color="bg-green-500"  to="/billing" />
        <StatCard label="Stocks"     value={stats?.stocks ?? 0}    icon={Warehouse}    color="bg-orange-500" to="/inventory" />
      </div>

      {/* Secondary metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border-l-4 border-green-500">
          <div className="flex items-center gap-2 text-green-600 mb-1">
            <TrendingUp size={18} />
            <span className="text-sm font-medium">Revenu total</span>
          </div>
          <p className="text-xl font-bold text-gray-800">
            {(stats?.revenue ?? 0).toLocaleString('fr-FR')} FCFA
          </p>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border-l-4 border-yellow-500">
          <div className="flex items-center gap-2 text-yellow-600 mb-1">
            <ShoppingCart size={18} />
            <span className="text-sm font-medium">Commandes en attente</span>
          </div>
          <p className="text-xl font-bold text-gray-800">{stats?.pendingOrders ?? 0}</p>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border-l-4 border-red-500">
          <div className="flex items-center gap-2 text-red-600 mb-1">
            <AlertTriangle size={18} />
            <span className="text-sm font-medium">Stocks bas</span>
          </div>
          <p className="text-xl font-bold text-gray-800">{stats?.lowStock ?? 0}</p>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border-l-4 border-orange-500">
          <div className="flex items-center gap-2 text-orange-600 mb-1">
            <FileText size={18} />
            <span className="text-sm font-medium">Factures impayées</span>
          </div>
          <p className="text-xl font-bold text-gray-800">{stats?.unpaidInvoices ?? 0}</p>
        </div>
      </div>

      {/* Recent orders */}
      <div className="bg-white rounded-xl shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-800">Commandes récentes</h2>
          <Link to="/orders" className="text-sm text-blue-600 hover:underline">Voir tout</Link>
        </div>
        {recentOrders.length === 0 ? (
          <p className="text-gray-400 text-sm text-center py-8">Aucune commande</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-3 font-medium">N° Commande</th>
                  <th className="pb-3 font-medium">Client</th>
                  <th className="pb-3 font-medium">Montant</th>
                  <th className="pb-3 font-medium">Statut</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {recentOrders.map(order => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="py-3 font-mono text-xs">{order.orderNumber}</td>
                    <td className="py-3">Client #{order.client.email}</td>
                    <td className="py-3 font-medium">{order.totalAmount.toLocaleString('fr-FR')} FCFA</td>
                    <td className="py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[order.status]}`}>
                        {order.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

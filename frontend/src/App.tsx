import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import RoleRoute from './components/RoleRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ProductsPage from './pages/ProductsPage'
import InventoryPage from './pages/InventoryPage'
import OrdersPage from './pages/OrdersPage'
import BillingPage from './pages/BillingPage'
import UsersPage from './pages/UsersPage'
import ProductionPage from './pages/ProductionPage'
import NotificationsPage from './pages/NotificationsPage'
import ShopPage from './pages/ShopPage'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route path="/" element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }>
          {/* Tous les rôles */}
          <Route index element={<DashboardPage />} />

          {/* Admin + Opérateur */}
          <Route path="products" element={
            <RoleRoute roles={['ROLE_ADMIN', 'ROLE_OPERATOR']}>
              <ProductsPage />
            </RoleRoute>
          } />
          <Route path="inventory" element={
            <RoleRoute roles={['ROLE_ADMIN', 'ROLE_OPERATOR']}>
              <InventoryPage />
            </RoleRoute>
          } />

          {/* Catalogue produits pour ROLE_USER */}
          <Route path="shop" element={<ShopPage />} />

          {/* Tous les rôles */}
          <Route path="orders" element={<OrdersPage />} />

          {/* Admin + User */}
          <Route path="billing" element={
            <RoleRoute roles={['ROLE_ADMIN', 'ROLE_USER']}>
              <BillingPage />
            </RoleRoute>
          } />

          {/* Admin + Opérateur */}
          <Route path="production" element={
            <RoleRoute roles={['ROLE_ADMIN', 'ROLE_OPERATOR']}>
              <ProductionPage />
            </RoleRoute>
          } />
          <Route path="notifications" element={
            <RoleRoute roles={['ROLE_ADMIN', 'ROLE_OPERATOR']}>
              <NotificationsPage />
            </RoleRoute>
          } />

          {/* Admin uniquement */}
          <Route path="users" element={
            <RoleRoute roles={['ROLE_ADMIN']}>
              <UsersPage />
            </RoleRoute>
          } />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

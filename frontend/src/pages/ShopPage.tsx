import { useEffect, useState } from 'react'
import { getProducts } from '../api/products'
import { createOrder } from '../api/orders'
import { useAuth } from '../contexts/AuthContext'
import type { Product, CreateOrderRequest } from '../types'
import {
  ShoppingCart, Plus, Minus, Trash2, X, Loader2,
  Package, CheckCircle, Search
} from 'lucide-react'

interface CartItem {
  product: Product
  quantity: number
}

export default function ShopPage() {
  const { user } = useAuth()
  const [products, setProducts]   = useState<Product[]>([])
  const [filtered, setFiltered]   = useState<Product[]>([])
  const [cart, setCart]           = useState<CartItem[]>([])
  const [search, setSearch]       = useState('')
  const [category, setCategory]   = useState('all')
  const [loading, setLoading]     = useState(true)
  const [showCart, setShowCart]   = useState(false)
  const [showCheckout, setShowCheckout] = useState(false)
  const [saving, setSaving]       = useState(false)
  const [success, setSuccess]     = useState(false)
  const [error, setError]         = useState('')
  const [shippingAddress, setShippingAddress] = useState('')
  const [notes, setNotes]         = useState('')

  useEffect(() => {
    getProducts()
      .then(p => { setProducts(p); setFiltered(p) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    let result = products
    if (search) result = result.filter(p =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.description?.toLowerCase().includes(search.toLowerCase())
    )
    if (category !== 'all') result = result.filter(p => p.category === category)
    setFiltered(result)
  }, [search, category, products])

  const categories = ['all', ...Array.from(new Set(products.map(p => p.category).filter(Boolean)))]

  const addToCart = (product: Product) => {
    setCart(prev => {
      const existing = prev.find(c => c.product.id === product.id)
      if (existing) return prev.map(c => c.product.id === product.id ? { ...c, quantity: c.quantity + 1 } : c)
      return [...prev, { product, quantity: 1 }]
    })
  }

  const updateQty = (productId: number, delta: number) => {
    setCart(prev => prev
      .map(c => c.product.id === productId ? { ...c, quantity: Math.max(0, c.quantity + delta) } : c)
      .filter(c => c.quantity > 0)
    )
  }

  const removeFromCart = (productId: number) =>
    setCart(prev => prev.filter(c => c.product.id !== productId))

  const cartTotal = cart.reduce((s, c) => s + c.product.unitPrice * c.quantity, 0)
  const cartCount = cart.reduce((s, c) => s + c.quantity, 0)

  const getCartQty = (productId: number) =>
    cart.find(c => c.product.id === productId)?.quantity ?? 0

  const handleOrder = async () => {
    if (!shippingAddress.trim()) { setError("L'adresse de livraison est obligatoire."); return }
    if (cart.length === 0) { setError('Le panier est vide.'); return }
    setError('')
    setSaving(true)

    const req: CreateOrderRequest = {
      shippingAddress,
      notes,
      items: cart.map(c => ({
        productId:   c.product.id,
        productName: c.product.name,
        quantity:    c.quantity,
        unitPrice:   c.product.unitPrice,
      })),
    }

    try {
      await createOrder(req)
      setSuccess(true)
      setCart([])
      setShippingAddress('')
      setNotes('')
      setTimeout(() => { setSuccess(false); setShowCheckout(false); setShowCart(false) }, 3000)
    } catch {
      setError('Erreur lors de la création de la commande. Veuillez réessayer.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
    </div>
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Catalogue produits</h1>
          <p className="text-sm text-gray-500 mt-1">{filtered.length} produit(s) disponible(s)</p>
        </div>
        <button
          onClick={() => setShowCart(true)}
          className="relative flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition"
        >
          <ShoppingCart size={18} />
          Mon panier
          {cartCount > 0 && (
            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
              {cartCount}
            </span>
          )}
        </button>
      </div>

      {/* Filtres */}
      <div className="flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text" placeholder="Rechercher un produit..."
            value={search} onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="flex flex-wrap gap-2">
          {categories.map(cat => (
            <button key={cat}
              onClick={() => setCategory(cat)}
              className={`px-3 py-2 rounded-lg text-sm font-medium transition ${
                category === cat ? 'bg-blue-600 text-white' : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
              }`}
            >
              {cat === 'all' ? 'Tous' : cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grille produits */}
      {filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <Package size={40} className="mx-auto mb-3 opacity-30" />
          <p>Aucun produit trouvé</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map(product => {
            const qty = getCartQty(product.id)
            return (
              <div key={product.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 flex flex-col hover:shadow-md transition-shadow">
                <div className="flex-1">
                  <span className="inline-block text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full font-medium mb-2">
                    {product.category}
                  </span>
                  <h3 className="font-semibold text-gray-900 mb-1">{product.name}</h3>
                  {product.description && (
                    <p className="text-xs text-gray-500 mb-3 line-clamp-2">{product.description}</p>
                  )}
                  <div className="flex items-baseline gap-1 mb-4">
                    <span className="text-xl font-bold text-blue-700">
                      {product.unitPrice.toLocaleString('fr-FR')}
                    </span>
                    <span className="text-sm text-gray-500">FCFA / {product.unit}</span>
                  </div>
                </div>

                {qty === 0 ? (
                  <button
                    onClick={() => addToCart(product)}
                    className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm font-medium transition"
                  >
                    <Plus size={16} /> Ajouter au panier
                  </button>
                ) : (
                  <div className="flex items-center justify-between bg-blue-50 rounded-lg p-2">
                    <button onClick={() => updateQty(product.id, -1)}
                      className="p-1 rounded-md hover:bg-blue-100 text-blue-700 transition">
                      <Minus size={16} />
                    </button>
                    <span className="font-bold text-blue-700 min-w-8 text-center">{qty}</span>
                    <button onClick={() => updateQty(product.id, +1)}
                      className="p-1 rounded-md hover:bg-blue-100 text-blue-700 transition">
                      <Plus size={16} />
                    </button>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {/* ── Panneau panier ── */}
      {showCart && (
        <div className="fixed inset-0 bg-black/50 flex items-end sm:items-center justify-end sm:justify-center z-50">
          <div className="bg-white w-full sm:w-[480px] h-full sm:h-auto sm:max-h-[90vh] sm:rounded-2xl shadow-2xl flex flex-col">
            <div className="flex items-center justify-between px-6 py-4 border-b">
              <h3 className="font-semibold text-gray-900 text-lg flex items-center gap-2">
                <ShoppingCart size={20} /> Mon panier
                {cartCount > 0 && (
                  <span className="bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full font-bold">
                    {cartCount} article(s)
                  </span>
                )}
              </h3>
              <button onClick={() => setShowCart(false)}><X size={20} className="text-gray-400" /></button>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              {cart.length === 0 ? (
                <div className="text-center py-12 text-gray-400">
                  <ShoppingCart size={36} className="mx-auto mb-3 opacity-30" />
                  <p>Votre panier est vide</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {cart.map(c => (
                    <div key={c.product.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-sm text-gray-900 truncate">{c.product.name}</p>
                        <p className="text-xs text-gray-500">{c.product.unitPrice.toLocaleString('fr-FR')} FCFA / {c.product.unit}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <button onClick={() => updateQty(c.product.id, -1)}
                          className="w-6 h-6 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                          <Minus size={12} />
                        </button>
                        <span className="w-6 text-center text-sm font-semibold">{c.quantity}</span>
                        <button onClick={() => updateQty(c.product.id, +1)}
                          className="w-6 h-6 rounded-full bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                          <Plus size={12} />
                        </button>
                      </div>
                      <span className="text-sm font-bold text-blue-700 min-w-20 text-right">
                        {(c.product.unitPrice * c.quantity).toLocaleString('fr-FR')} FCFA
                      </span>
                      <button onClick={() => removeFromCart(c.product.id)} className="text-red-400 hover:text-red-600">
                        <Trash2 size={15} />
                      </button>
                    </div>
                  ))}

                  <div className="border-t pt-3 flex justify-between items-center">
                    <span className="font-semibold text-gray-700">Total</span>
                    <span className="text-xl font-bold text-blue-700">{cartTotal.toLocaleString('fr-FR')} FCFA</span>
                  </div>
                </div>
              )}
            </div>

            {cart.length > 0 && (
              <div className="p-6 border-t bg-gray-50 rounded-b-2xl">
                <div className="bg-blue-50 border border-blue-100 rounded-lg px-4 py-2 text-sm text-blue-800 mb-3">
                  Commande pour : <strong>{user?.email}</strong>
                </div>
                <button
                  onClick={() => { setShowCart(false); setShowCheckout(true) }}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-semibold transition"
                >
                  Passer la commande — {cartTotal.toLocaleString('fr-FR')} FCFA
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── Modal confirmation de commande ── */}
      {showCheckout && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg">
            <div className="flex items-center justify-between px-6 py-4 border-b">
              <h3 className="font-semibold text-gray-900 text-lg">Confirmer la commande</h3>
              <button onClick={() => setShowCheckout(false)}><X size={20} className="text-gray-400" /></button>
            </div>

            {success ? (
              <div className="p-12 text-center">
                <CheckCircle size={48} className="mx-auto mb-4 text-green-500" />
                <h4 className="text-xl font-bold text-gray-800 mb-2">Commande envoyée !</h4>
                <p className="text-gray-500 text-sm">Votre commande a été transmise et sera traitée prochainement.</p>
              </div>
            ) : (
              <div className="p-6 space-y-4">
                <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                  <h4 className="text-sm font-semibold text-gray-700 mb-2">Récapitulatif ({cart.length} produit(s))</h4>
                  {cart.map(c => (
                    <div key={c.product.id} className="flex justify-between text-sm">
                      <span className="text-gray-600">{c.product.name} × {c.quantity}</span>
                      <span className="font-medium">{(c.product.unitPrice * c.quantity).toLocaleString('fr-FR')} FCFA</span>
                    </div>
                  ))}
                  <div className="border-t pt-2 flex justify-between font-bold">
                    <span>Total</span>
                    <span className="text-blue-700">{cartTotal.toLocaleString('fr-FR')} FCFA</span>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Adresse de livraison <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text" value={shippingAddress}
                    onChange={e => setShippingAddress(e.target.value)}
                    placeholder="Ex : Quartier Cadjèhoun, Cotonou, Bénin"
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Notes (optionnel)</label>
                  <input
                    type="text" value={notes}
                    onChange={e => setNotes(e.target.value)}
                    placeholder="Instructions de livraison, référence chantier..."
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                {error && (
                  <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm">{error}</div>
                )}

                <div className="flex justify-end gap-3 pt-2">
                  <button onClick={() => setShowCheckout(false)}
                    className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 transition">
                    Retour
                  </button>
                  <button onClick={handleOrder} disabled={saving}
                    className="px-5 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-60 flex items-center gap-2 transition font-medium">
                    {saving && <Loader2 size={14} className="animate-spin" />}
                    Confirmer la commande
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

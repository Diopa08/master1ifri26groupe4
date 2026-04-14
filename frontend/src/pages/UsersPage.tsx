import { useEffect, useState } from 'react'
import { getUsers, createUser } from '../api/users'
import { Users, Plus, X, Loader2, ShieldCheck } from 'lucide-react'

interface User {
  id: number
  email: string
  username: string
  roles: { id: number; name: string }[]
}

const ROLES = ['ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_USER']

const roleColor: Record<string, string> = {
  ROLE_ADMIN:    'bg-red-100 text-red-700',
  ROLE_OPERATOR: 'bg-yellow-100 text-yellow-700',
  ROLE_USER:     'bg-blue-100 text-blue-700',
}

const roleLabel: Record<string, string> = {
  ROLE_ADMIN:    'Administrateur',
  ROLE_OPERATOR: 'Opérateur',
  ROLE_USER:     'Utilisateur',
}

export default function UsersPage() {
  const [users, setUsers]     = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving]   = useState(false)
  const [form, setForm]       = useState({ email: '', password: '', role: 'ROLE_USER' })

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      setUsers(await getUsers())
    } catch {
      setError('Impossible de charger les utilisateurs.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    try {
      await createUser(form)
      setShowForm(false)
      setForm({ email: '', password: '', role: 'ROLE_USER' })
      await load()
    } catch {
      setError('Erreur lors de la création de l\'utilisateur.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-purple-100 rounded-lg">
            <Users size={24} className="text-purple-600" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Utilisateurs</h1>
            <p className="text-sm text-gray-500">{users.length} compte(s) enregistré(s)</p>
          </div>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition"
        >
          <Plus size={16} /> Nouvel utilisateur
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg p-3 mb-4 text-sm">{error}</div>
      )}

      {/* Modal création */}
      {showForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">Nouvel utilisateur</h2>
              <button onClick={() => setShowForm(false)} className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  type="email" required
                  value={form.email}
                  onChange={e => setForm({ ...form, email: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                  placeholder="utilisateur@sfmc.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Mot de passe</label>
                <input
                  type="password" required minLength={6}
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                  placeholder="••••••••"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Rôle</label>
                <select
                  value={form.role}
                  onChange={e => setForm({ ...form, role: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
                >
                  {ROLES.map(r => <option key={r} value={r}>{roleLabel[r]}</option>)}
                </select>
              </div>
              <div className="flex gap-2 pt-2">
                <button type="button" onClick={() => setShowForm(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition">
                  Annuler
                </button>
                <button type="submit" disabled={saving}
                  className="flex-1 flex items-center justify-center gap-2 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-400 text-white px-4 py-2 rounded-lg text-sm font-medium transition">
                  {saving ? <><Loader2 size={14} className="animate-spin" /> Création...</> : 'Créer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Table */}
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 size={32} className="animate-spin text-purple-500" />
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left font-semibold text-gray-600">#</th>
                <th className="px-6 py-3 text-left font-semibold text-gray-600">Email</th>
                <th className="px-6 py-3 text-left font-semibold text-gray-600">Nom d'utilisateur</th>
                <th className="px-6 py-3 text-left font-semibold text-gray-600">Rôles</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {users.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-gray-400">
                    <ShieldCheck size={32} className="mx-auto mb-2 opacity-40" />
                    Aucun utilisateur trouvé
                  </td>
                </tr>
              ) : users.map(u => (
                <tr key={u.id} className="hover:bg-gray-50 transition">
                  <td className="px-6 py-4 text-gray-400">{u.id}</td>
                  <td className="px-6 py-4 font-medium text-gray-900">{u.email}</td>
                  <td className="px-6 py-4 text-gray-600">{u.username || '—'}</td>
                  <td className="px-6 py-4">
                    <div className="flex flex-wrap gap-1">
                      {u.roles?.map(r => (
                        <span key={r.name} className={`text-xs px-2 py-0.5 rounded-full font-medium ${roleColor[r.name] || 'bg-gray-100 text-gray-600'}`}>
                          {roleLabel[r.name] || r.name}
                        </span>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

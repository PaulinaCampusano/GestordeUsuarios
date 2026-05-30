import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // ── State ──
  const token       = ref(localStorage.getItem('token') || null)
  const user        = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const permisos    = ref(JSON.parse(localStorage.getItem('permisos') || '[]'))
  const loading     = ref(false)

  // ── Getters ──
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() =>
    user.value?.rol === 'ROLE_ADMIN'
  )
  const nombreCompleto = computed(() => user.value?.nombreCompleto || '')
  const initials = computed(() => {
    const name = user.value?.nombreCompleto || ''
    return name
      .split(' ')
      .slice(0, 2)
      .map(n => n[0])
      .join('')
      .toUpperCase() || 'U'
  })

  // ── Actions ──
  async function login(email, password) {
    loading.value = true
    try {
      const { data } = await api.post('/auth/login', { email, password })
      _setSession(data)
      return data
    } finally {
      loading.value = false
    }
  }

  async function register(payload) {
    loading.value = true
    try {
      await api.post('/auth/register', payload)
    } finally {
      loading.value = false
    }
  }

  async function requestPasswordReset(email) {
    await api.post('/auth/reset-password', { email })
  }

  async function resetPassword(resetToken, newPassword) {
    await api.post('/auth/new-password', { token: resetToken, newPassword })
  }

  async function fetchMe() {
    const { data } = await api.get('/auth/me')
    user.value = { ...user.value, ...data }
    localStorage.setItem('user', JSON.stringify(user.value))
    return data
  }

  async function updateMe(payload) {
    await api.put('/auth/me', payload)
    // Refrescar datos del perfil
    await fetchMe()
  }

  async function changePassword(payload) {
    await api.put('/auth/me/password', payload)
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch { /* silenciar — token puede ya ser inválido */ }
    _clearSession()
  }

  function _setSession(data) {
    token.value    = data.token
    user.value     = {
      email:         data.email,
      nombreCompleto:data.nombreCompleto,
      rol:           data.rol,
    }
    permisos.value = data.permisos || []

    localStorage.setItem('token',   data.token)
    localStorage.setItem('user',    JSON.stringify(user.value))
    localStorage.setItem('permisos',JSON.stringify(permisos.value))
  }

  function _clearSession() {
    token.value    = null
    user.value     = null
    permisos.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('permisos')
  }

  return {
    token, user, permisos, loading,
    isAuthenticated, isAdmin, nombreCompleto, initials,
    login, register, requestPasswordReset, resetPassword,
    fetchMe, updateMe, changePassword, logout,
  }
})

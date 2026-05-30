<template>
  <AppLayout>
    <div class="page-header flex justify-between items-center">
      <div>
        <h1 class="page-title">Gestión de Usuarios</h1>
        <p class="page-subtitle">Administra cuentas, roles y estados</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal" id="btn-create-user">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        Nuevo Usuario
      </button>
    </div>

    <!-- Stats cards -->
    <div class="stats-row">
      <div class="stat-card">
        <p class="stat-label">Total usuarios</p>
        <p class="stat-value">{{ users.length }}</p>
      </div>
      <div class="stat-card">
        <p class="stat-label">Activos</p>
        <p class="stat-value text-success">{{ users.filter(u => u.isActive).length }}</p>
      </div>
      <div class="stat-card">
        <p class="stat-label">Inactivos</p>
        <p class="stat-value text-danger">{{ users.filter(u => !u.isActive).length }}</p>
      </div>
      <div class="stat-card">
        <p class="stat-label">Administradores</p>
        <p class="stat-value text-primary">{{ users.filter(u => u.role?.nombre === 'ROLE_ADMIN').length }}</p>
      </div>
    </div>

    <!-- Search bar -->
    <div class="search-bar-wrap">
      <div class="input-wrapper search-input-wrap">
        <svg class="search-icon-left" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          id="input-search-users"
          v-model="searchQuery"
          type="text"
          class="form-input search-input"
          placeholder="Buscar por nombre, email o RUT..."
        />
        <button v-if="searchQuery" class="input-icon" @click="searchQuery = ''" aria-label="Limpiar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <select v-model="filterStatus" class="form-input filter-select" id="select-filter-status">
        <option value="">Todos los estados</option>
        <option value="active">Activos</option>
        <option value="inactive">Inactivos</option>
      </select>
    </div>

    <!-- Table -->
    <div class="card" style="padding: 0;">
      <div v-if="loadingUsers" class="table-loading">
        <div class="spinner" style="width:32px;height:32px;border-width:3px;" />
        <span>Cargando usuarios...</span>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="empty-state">
        <p class="empty-icon">👥</p>
        <p class="empty-title">Sin resultados</p>
        <p class="empty-msg">No se encontraron usuarios con los filtros aplicados</p>
      </div>

      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>RUT</th>
              <th>Teléfono</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Miembro desde</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <!-- Usuario -->
              <td>
                <div class="user-cell">
                  <div class="user-avatar user-avatar-sm">{{ getInitials(user.nombreCompleto) }}</div>
                  <div>
                    <p class="user-cell-name">{{ user.nombreCompleto }}</p>
                    <p class="user-cell-email">{{ user.email }}</p>
                  </div>
                </div>
              </td>
              <td>{{ user.rut || '—' }}</td>
              <td>{{ user.telefono || '—' }}</td>
              <!-- Rol -->
              <td>
                <span class="badge badge-primary">{{ formatRole(user.role?.nombre) }}</span>
              </td>
              <!-- Estado -->
              <td>
                <span class="badge" :class="user.isActive ? 'badge-success' : 'badge-danger'">
                  {{ user.isActive ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <!-- Fecha -->
              <td>{{ formatDate(user.createdAt) }}</td>
              <!-- Acciones -->
              <td>
                <div class="actions-cell">
                  <button
                    class="btn btn-icon btn-ghost"
                    @click="openEditModal(user)"
                    :id="`btn-edit-user-${user.id}`"
                    title="Editar usuario"
                  >
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button
                    class="btn btn-icon"
                    :class="user.isActive ? 'btn-toggle-active' : 'btn-toggle-inactive'"
                    @click="handleToggleStatus(user)"
                    :id="`btn-toggle-status-${user.id}`"
                    :title="user.isActive ? 'Desactivar' : 'Activar'"
                  >
                    <svg v-if="user.isActive" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="1" y="5" width="22" height="14" rx="7" ry="7"/>
                      <circle cx="16" cy="12" r="3" fill="currentColor"/>
                    </svg>
                    <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="1" y="5" width="22" height="14" rx="7" ry="7"/>
                      <circle cx="8" cy="12" r="3" fill="currentColor"/>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal: crear / editar usuario -->
    <Transition name="fade">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-box">
          <div class="modal-header">
            <h2 class="modal-title">{{ isEditing ? 'Editar usuario' : 'Nuevo usuario' }}</h2>
            <button class="modal-close" @click="closeModal" id="btn-close-user-modal" aria-label="Cerrar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="handleSaveUser" id="form-user-modal" novalidate>
              <div class="modal-form-grid">
                <!-- Nombre -->
                <div class="form-group">
                  <label class="form-label" for="modal-nombre">Nombre completo *</label>
                  <input
                    id="modal-nombre"
                    v-model="modalForm.nombreCompleto"
                    type="text"
                    class="form-input"
                    :class="{ error: modalErrors.nombreCompleto }"
                    placeholder="Juan Pérez"
                  />
                  <span v-if="modalErrors.nombreCompleto" class="form-error">⚠ {{ modalErrors.nombreCompleto }}</span>
                </div>

                <!-- Email -->
                <div class="form-group">
                  <label class="form-label" for="modal-email">Correo electrónico *</label>
                  <input
                    id="modal-email"
                    v-model="modalForm.email"
                    type="email"
                    class="form-input"
                    :class="{ error: modalErrors.email }"
                    placeholder="usuario@correo.com"
                  />
                  <span v-if="modalErrors.email" class="form-error">⚠ {{ modalErrors.email }}</span>
                </div>

                <!-- RUT -->
                <div class="form-group">
                  <label class="form-label" for="modal-rut">RUT <span class="text-faint">(opcional)</span></label>
                  <input id="modal-rut" v-model="modalForm.rut" type="text" class="form-input" placeholder="12.345.678-9" />
                </div>

                <!-- Teléfono -->
                <div class="form-group">
                  <label class="form-label" for="modal-telefono">Teléfono <span class="text-faint">(opcional)</span></label>
                  <input id="modal-telefono" v-model="modalForm.telefono" type="tel" class="form-input" placeholder="+56 9 1234 5678" />
                </div>

                <!-- Rol -->
                <div class="form-group">
                  <label class="form-label" for="modal-rol">Rol</label>
                  <select id="modal-rol" v-model="modalForm.rol" class="form-input">
                    <option v-for="role in roles" :key="role.id" :value="role.nombre">
                      {{ formatRole(role.nombre) }}
                    </option>
                  </select>
                </div>

                <!-- Contraseña (solo crear) -->
                <div class="form-group" v-if="!isEditing">
                  <label class="form-label" for="modal-password">Contraseña *</label>
                  <div class="input-wrapper">
                    <input
                      id="modal-password"
                      v-model="modalForm.password"
                      :type="showModalPwd ? 'text' : 'password'"
                      class="form-input"
                      :class="{ error: modalErrors.password }"
                      placeholder="Mín. 8 caracteres"
                      autocomplete="new-password"
                    />
                    <button type="button" class="input-icon" @click="showModalPwd = !showModalPwd">
                      <svg v-if="!showModalPwd" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                      <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                    </button>
                  </div>
                  <span v-if="modalErrors.password" class="form-error">⚠ {{ modalErrors.password }}</span>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button class="btn btn-ghost" @click="closeModal" id="btn-cancel-user-modal">Cancelar</button>
            <button
              class="btn btn-primary"
              @click="handleSaveUser"
              :disabled="savingUser"
              id="btn-save-user-modal"
            >
              <span v-if="savingUser" class="spinner" />
              <span v-else>{{ isEditing ? 'Guardar cambios' : 'Crear usuario' }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Modal: confirmar toggle estado -->
    <Transition name="fade">
      <div v-if="showConfirm" class="modal-overlay" @click.self="showConfirm = false">
        <div class="modal-box" style="max-width: 400px;">
          <div class="modal-header">
            <h2 class="modal-title">Confirmar acción</h2>
            <button class="modal-close" @click="showConfirm = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <p style="font-size: var(--font-size-sm); color: var(--color-text-muted);">
              ¿Deseas <strong style="color: var(--color-text);">{{ confirmUser?.isActive ? 'desactivar' : 'activar' }}</strong>
              la cuenta de <strong style="color: var(--color-text);">{{ confirmUser?.nombreCompleto }}</strong>?
            </p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-ghost" @click="showConfirm = false" id="btn-cancel-confirm">Cancelar</button>
            <button
              class="btn"
              :class="confirmUser?.isActive ? 'btn-danger' : 'btn-success'"
              @click="confirmToggle"
              :disabled="togglingStatus"
              id="btn-confirm-toggle"
            >
              <span v-if="togglingStatus" class="spinner" />
              <span v-else>{{ confirmUser?.isActive ? 'Desactivar' : 'Activar' }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useToast } from 'vue-toastification'
import api from '@/services/api'

const toast = useToast()

// ── Data ──
const users        = ref([])
const roles        = ref([])
const loadingUsers = ref(true)
const searchQuery  = ref('')
const filterStatus = ref('')

// ── Modal ──
const showModal    = ref(false)
const isEditing    = ref(false)
const savingUser   = ref(false)
const showModalPwd = ref(false)
const editingId    = ref(null)

const modalForm = reactive({
  nombreCompleto: '', email: '', rut: '', telefono: '', password: '', rol: 'ROLE_USER',
})
const modalErrors = reactive({
  nombreCompleto: '', email: '', password: '',
})

// ── Confirm modal ──
const showConfirm   = ref(false)
const confirmUser   = ref(null)
const togglingStatus = ref(false)

// ── Computed ──
const filteredUsers = computed(() => {
  let list = users.value
  const q  = searchQuery.value.toLowerCase()

  if (q) {
    list = list.filter(u =>
      u.nombreCompleto?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q) ||
      u.rut?.toLowerCase().includes(q)
    )
  }
  if (filterStatus.value === 'active')   list = list.filter(u => u.isActive)
  if (filterStatus.value === 'inactive') list = list.filter(u => !u.isActive)

  return list
})

// ── Helpers ──
function getInitials(name = '') {
  return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || 'U'
}
function formatRole(nombre) {
  const map = { ROLE_ADMIN: 'Admin', ROLE_USER: 'Usuario', ROLE_SUPERVISOR: 'Supervisor' }
  return map[nombre] || nombre?.replace('ROLE_', '') || '—'
}
function formatDate(date) {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' })
}

// ── Load data ──
async function loadUsers() {
  try {
    const { data } = await api.get('/admin/users')
    users.value = data
  } catch {
    toast.error('Error al cargar usuarios')
  } finally {
    loadingUsers.value = false
  }
}

async function loadRoles() {
  try {
    const { data } = await api.get('/admin/roles')
    roles.value = data
  } catch { /* silenciar */ }
}

onMounted(() => {
  loadUsers()
  loadRoles()
})

// ── Modal actions ──
function resetModal() {
  modalForm.nombreCompleto = ''
  modalForm.email          = ''
  modalForm.rut            = ''
  modalForm.telefono       = ''
  modalForm.password       = ''
  modalForm.rol            = 'ROLE_USER'
  modalErrors.nombreCompleto = ''
  modalErrors.email          = ''
  modalErrors.password       = ''
  showModalPwd.value = false
}

function openCreateModal() {
  isEditing.value = false
  editingId.value = null
  resetModal()
  showModal.value = true
}

function openEditModal(user) {
  isEditing.value              = true
  editingId.value              = user.id
  modalForm.nombreCompleto     = user.nombreCompleto || ''
  modalForm.email              = user.email || ''
  modalForm.rut                = user.rut || ''
  modalForm.telefono           = user.telefono || ''
  modalForm.rol                = user.role?.nombre || 'ROLE_USER'
  modalErrors.nombreCompleto   = ''
  modalErrors.email            = ''
  modalErrors.password         = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function validateModal() {
  let valid = true
  modalErrors.nombreCompleto = ''
  modalErrors.email          = ''
  modalErrors.password       = ''

  if (!modalForm.nombreCompleto.trim()) { modalErrors.nombreCompleto = 'El nombre es obligatorio'; valid = false }
  if (!modalForm.email) { modalErrors.email = 'El correo es obligatorio'; valid = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(modalForm.email)) { modalErrors.email = 'Formato inválido'; valid = false }
  if (!isEditing.value && !modalForm.password) { modalErrors.password = 'La contraseña es obligatoria'; valid = false }
  else if (!isEditing.value && modalForm.password.length < 8) { modalErrors.password = 'Mínimo 8 caracteres'; valid = false }
  return valid
}

async function handleSaveUser() {
  if (!validateModal()) return
  savingUser.value = true
  try {
    if (isEditing.value) {
      // Actualizar datos básicos
      await api.put(`/admin/users/${editingId.value}`, {
        nombreCompleto: modalForm.nombreCompleto,
        email:          modalForm.email,
        rut:            modalForm.rut || undefined,
        telefono:       modalForm.telefono || undefined,
      })
      // Asignar rol si cambió
      if (modalForm.rol) {
        await api.post(`/admin/users/${editingId.value}/roles/${modalForm.rol}`)
      }
      toast.success('Usuario actualizado correctamente')
    } else {
      await api.post('/admin/users', {
        nombreCompleto: modalForm.nombreCompleto,
        email:          modalForm.email,
        password:       modalForm.password,
        rut:            modalForm.rut || undefined,
        telefono:       modalForm.telefono || undefined,
        roles:          modalForm.rol ? [modalForm.rol] : undefined,
      })
      toast.success('Usuario creado correctamente')
    }
    closeModal()
    await loadUsers()
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data?.error || 'Error al guardar usuario'
    toast.error(msg)
  } finally {
    savingUser.value = false
  }
}

// ── Toggle status ──
function handleToggleStatus(user) {
  confirmUser.value = user
  showConfirm.value = true
}

async function confirmToggle() {
  togglingStatus.value = true
  try {
    await api.patch(`/admin/users/${confirmUser.value.id}/toggle-status`)
    const action = confirmUser.value.isActive ? 'desactivado' : 'activado'
    toast.success(`Usuario ${action} correctamente`)
    showConfirm.value = false
    await loadUsers()
  } catch (err) {
    toast.error(err.response?.data?.message || 'Error al cambiar estado')
  } finally {
    togglingStatus.value = false
  }
}
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.stat-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-faint);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: var(--space-2);
}
.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: 800;
  letter-spacing: -0.03em;
}
.text-success { color: var(--color-success); }
.text-danger  { color: var(--color-danger);  }
.text-primary { color: var(--color-primary-h); }

.search-bar-wrap {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
  align-items: center;
}
.search-input-wrap { flex: 1; }
.search-input { padding-left: 2.5rem; }
.search-icon-left {
  position: absolute;
  left: 0.875rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-faint);
  pointer-events: none;
}
.filter-select {
  width: 200px;
  flex-shrink: 0;
  background: rgba(255,255,255,0.05);
  cursor: pointer;
}
.filter-select option { background: var(--color-surface); }

.table-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-16);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.empty-state {
  text-align: center;
  padding: var(--space-16) var(--space-8);
}
.empty-icon  { font-size: 3rem; margin-bottom: var(--space-4); }
.empty-title { font-size: var(--font-size-lg); font-weight: 700; margin-bottom: var(--space-2); }
.empty-msg   { font-size: var(--font-size-sm); color: var(--color-text-muted); }

.user-cell { display: flex; align-items: center; gap: var(--space-3); }
.user-cell-name  { font-weight: 600; font-size: var(--font-size-sm); }
.user-cell-email { font-size: var(--font-size-xs); color: var(--color-text-muted); margin-top: 1px; }

.actions-cell { display: flex; align-items: center; gap: var(--space-2); }

.btn-toggle-active {
  background: rgba(239,68,68,0.1);
  color: var(--color-danger);
  border: 1px solid rgba(239,68,68,0.2);
}
.btn-toggle-active:hover { background: rgba(239,68,68,0.2); }
.btn-toggle-inactive {
  background: rgba(16,185,129,0.1);
  color: var(--color-success);
  border: 1px solid rgba(16,185,129,0.2);
}
.btn-toggle-inactive:hover { background: rgba(16,185,129,0.2); }

.modal-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .stats-row          { grid-template-columns: 1fr 1fr; }
  .search-bar-wrap    { flex-direction: column; }
  .filter-select      { width: 100%; }
  .modal-form-grid    { grid-template-columns: 1fr; }
}
</style>

<template>
  <AppLayout>
    <div class="page-header">
      <h1 class="page-title">Mi Perfil</h1>
      <p class="page-subtitle">Gestiona tu información personal y contraseña</p>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loadingProfile" class="profile-grid">
      <div class="card skeleton-card" v-for="i in 2" :key="i" />
    </div>

    <div v-else class="profile-grid">
      <!-- ── Información personal ── -->
      <section class="card">
        <div class="section-header">
          <div class="section-icon">👤</div>
          <div>
            <h2 class="section-title">Información personal</h2>
            <p class="section-sub">Actualiza tu nombre y teléfono</p>
          </div>
        </div>

        <!-- Avatar grande -->
        <div class="profile-avatar-wrap">
          <div class="profile-avatar">{{ authStore.initials }}</div>
          <div>
            <p class="profile-name">{{ profileData.nombreCompleto }}</p>
            <p class="profile-email">{{ profileData.email }}</p>
            <span class="badge" :class="profileData.activo ? 'badge-success' : 'badge-danger'">
              {{ profileData.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
        </div>

        <!-- Read-only fields -->
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">RUT</span>
            <span class="info-value">{{ profileData.rut || '—' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Rol</span>
            <span class="badge badge-primary">{{ roleLabel }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Miembro desde</span>
            <span class="info-value">{{ formatDate(profileData.fechaCreacion) }}</span>
          </div>
        </div>

        <hr class="section-divider" />

        <!-- Editable form -->
        <form @submit.prevent="handleUpdateProfile" id="form-update-profile" novalidate>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label" for="profile-nombre">Nombre completo</label>
              <input
                id="profile-nombre"
                v-model="editForm.nombreCompleto"
                type="text"
                class="form-input"
                :class="{ error: editErrors.nombreCompleto }"
                placeholder="Tu nombre"
              />
              <span v-if="editErrors.nombreCompleto" class="form-error">⚠ {{ editErrors.nombreCompleto }}</span>
            </div>
            <div class="form-group">
              <label class="form-label" for="profile-telefono">Teléfono</label>
              <input
                id="profile-telefono"
                v-model="editForm.telefono"
                type="tel"
                class="form-input"
                placeholder="+56 9 1234 5678"
              />
            </div>
          </div>
          <button type="submit" class="btn btn-primary" :disabled="savingProfile" id="btn-save-profile">
            <span v-if="savingProfile" class="spinner" />
            <span v-else>Guardar cambios</span>
          </button>
        </form>
      </section>

      <!-- ── Cambiar contraseña ── -->
      <section class="card">
        <div class="section-header">
          <div class="section-icon">🔒</div>
          <div>
            <h2 class="section-title">Cambiar contraseña</h2>
            <p class="section-sub">Usa una contraseña segura y única</p>
          </div>
        </div>

        <form @submit.prevent="handleChangePassword" id="form-change-password" novalidate class="auth-form">
          <!-- Contraseña actual -->
          <div class="form-group">
            <label class="form-label" for="pwd-current">Contraseña actual</label>
            <div class="input-wrapper">
              <input
                id="pwd-current"
                v-model="pwdForm.currentPassword"
                :type="showCurrent ? 'text' : 'password'"
                class="form-input"
                :class="{ error: pwdErrors.currentPassword }"
                placeholder="••••••••"
                autocomplete="current-password"
              />
              <button type="button" class="input-icon" @click="showCurrent = !showCurrent">
                <svg v-if="!showCurrent" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
            <span v-if="pwdErrors.currentPassword" class="form-error">⚠ {{ pwdErrors.currentPassword }}</span>
          </div>

          <!-- Nueva contraseña -->
          <div class="form-group">
            <label class="form-label" for="pwd-new">Nueva contraseña</label>
            <div class="input-wrapper">
              <input
                id="pwd-new"
                v-model="pwdForm.newPassword"
                :type="showNew ? 'text' : 'password'"
                class="form-input"
                :class="{ error: pwdErrors.newPassword }"
                placeholder="Mín. 8 caracteres"
                autocomplete="new-password"
                @input="updateStrength"
              />
              <button type="button" class="input-icon" @click="showNew = !showNew">
                <svg v-if="!showNew" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
            <div class="password-strength" v-if="pwdForm.newPassword">
              <div class="strength-bars">
                <div v-for="i in 4" :key="i" class="strength-bar" :class="i <= strength ? `active-${strength - 1}` : ''" />
              </div>
            </div>
            <span v-if="pwdErrors.newPassword" class="form-error">⚠ {{ pwdErrors.newPassword }}</span>
          </div>

          <!-- Confirmar -->
          <div class="form-group">
            <label class="form-label" for="pwd-confirm">Confirmar nueva contraseña</label>
            <input
              id="pwd-confirm"
              v-model="pwdForm.confirmPassword"
              type="password"
              class="form-input"
              :class="{ error: pwdErrors.confirmPassword }"
              placeholder="Repite la contraseña"
              autocomplete="new-password"
            />
            <span v-if="pwdErrors.confirmPassword" class="form-error">⚠ {{ pwdErrors.confirmPassword }}</span>
          </div>

          <button type="submit" class="btn btn-primary" :disabled="savingPwd" id="btn-change-password">
            <span v-if="savingPwd" class="spinner" />
            <span v-else>Actualizar contraseña</span>
          </button>
        </form>
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const authStore = useAuthStore()
const toast     = useToast()

// ── Profile ──
const loadingProfile = ref(true)
const savingProfile  = ref(false)
const profileData    = ref({})
const editForm       = reactive({ nombreCompleto: '', telefono: '' })
const editErrors     = reactive({ nombreCompleto: '' })

// ── Password ──
const savingPwd   = ref(false)
const showCurrent = ref(false)
const showNew     = ref(false)
const strength    = ref(0)
const pwdForm     = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const pwdErrors   = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const roleMap = {
  ROLE_ADMIN: 'Administrador', ROLE_USER: 'Usuario', ROLE_SUPERVISOR: 'Supervisor',
}
const roleLabel = computed(() => roleMap[profileData.value?.rol] || profileData.value?.rol || '—')

function formatDate(date) {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('es-CL', { day: '2-digit', month: 'long', year: 'numeric' })
}

onMounted(async () => {
  try {
    profileData.value = await authStore.fetchMe()
    editForm.nombreCompleto = profileData.value.nombreCompleto || ''
    editForm.telefono       = profileData.value.telefono || ''
  } catch { toast.error('No se pudo cargar el perfil') }
  finally { loadingProfile.value = false }
})

async function handleUpdateProfile() {
  editErrors.nombreCompleto = ''
  if (!editForm.nombreCompleto.trim()) {
    editErrors.nombreCompleto = 'El nombre es obligatorio'; return
  }
  savingProfile.value = true
  try {
    await authStore.updateMe({
      nombreCompleto: editForm.nombreCompleto,
      telefono: editForm.telefono || undefined,
    })
    profileData.value = await authStore.fetchMe()
    toast.success('Perfil actualizado correctamente')
  } catch (err) {
    toast.error(err.response?.data?.message || 'Error al actualizar perfil')
  } finally { savingProfile.value = false }
}

function updateStrength() {
  const p = pwdForm.newPassword
  let s = 0
  if (p.length >= 8) s++
  if (/[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++
  if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(p)) s++
  strength.value = s
}

async function handleChangePassword() {
  Object.keys(pwdErrors).forEach(k => (pwdErrors[k] = ''))
  let valid = true
  if (!pwdForm.currentPassword) { pwdErrors.currentPassword = 'Ingresa tu contraseña actual'; valid = false }
  if (!pwdForm.newPassword)     { pwdErrors.newPassword = 'Ingresa la nueva contraseña'; valid = false }
  else if (pwdForm.newPassword.length < 8) { pwdErrors.newPassword = 'Mínimo 8 caracteres'; valid = false }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { pwdErrors.confirmPassword = 'Las contraseñas no coinciden'; valid = false }
  if (!valid) return

  savingPwd.value = true
  try {
    await authStore.changePassword({
      currentPassword: pwdForm.currentPassword,
      newPassword:     pwdForm.newPassword,
    })
    toast.success('Contraseña actualizada correctamente')
    pwdForm.currentPassword = ''
    pwdForm.newPassword     = ''
    pwdForm.confirmPassword = ''
    strength.value = 0
  } catch (err) {
    toast.error(err.response?.data?.message || 'Error al cambiar contraseña')
  } finally { savingPwd.value = false }
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-6);
}
@media (max-width: 900px) {
  .profile-grid { grid-template-columns: 1fr; }
}
.skeleton-card {
  height: 400px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
.section-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.section-icon {
  font-size: 1.5rem;
  width: 42px; height: 42px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(99,102,241,0.1);
  border-radius: var(--radius-md);
  flex-shrink: 0;
}
.section-title { font-size: var(--font-size-lg); font-weight: 700; }
.section-sub   { font-size: var(--font-size-xs); color: var(--color-text-muted); margin-top: 2px; }
.profile-avatar-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  padding: var(--space-4);
  background: rgba(255,255,255,0.02);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
.profile-avatar {
  width: 60px; height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  display: flex; align-items: center; justify-content: center;
  font-size: 1.25rem; font-weight: 700; color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 15px rgba(99,102,241,0.3);
}
.profile-name  { font-weight: 700; font-size: var(--font-size-base); }
.profile-email { font-size: var(--font-size-xs); color: var(--color-text-muted); margin: 2px 0 6px; }
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: var(--font-size-xs); color: var(--color-text-faint); text-transform: uppercase; letter-spacing: 0.05em; }
.info-value { font-size: var(--font-size-sm); font-weight: 500; }
.section-divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin-bottom: var(--space-6);
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
@media (max-width: 600px) {
  .form-row  { grid-template-columns: 1fr; }
  .info-grid { grid-template-columns: 1fr 1fr; }
}
</style>

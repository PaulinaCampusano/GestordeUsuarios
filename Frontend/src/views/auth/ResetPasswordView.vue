<template>
  <div class="auth-layout">
    <div class="auth-card card-glass">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="auth-logo-icon">🔐</div>
        <div>
          <p class="auth-title">Nueva contraseña</p>
          <p class="auth-subtitle">El token es válido por 15 minutos</p>
        </div>
      </div>

      <!-- Error: no token -->
      <div v-if="!token" class="error-box">
        <p class="error-msg">⚠️ Enlace inválido o incompleto. Solicita uno nuevo.</p>
        <RouterLink to="/auth/forgot-password" class="btn btn-primary btn-w-full" id="link-request-new-token">
          Solicitar nuevo enlace
        </RouterLink>
      </div>

      <!-- Success -->
      <Transition name="fade">
        <div v-if="success" class="success-box">
          <div class="success-icon">✅</div>
          <h2 class="success-title">¡Contraseña actualizada!</h2>
          <p class="success-msg">Ya puedes iniciar sesión con tu nueva contraseña.</p>
          <RouterLink to="/auth/login" class="btn btn-primary btn-w-full" id="link-go-login-after-reset">
            Ir al Login
          </RouterLink>
        </div>
      </Transition>

      <!-- Form -->
      <form v-if="token && !success" class="auth-form" @submit.prevent="handleSubmit" id="form-reset-password" novalidate>
        <!-- Nueva contraseña -->
        <div class="form-group">
          <label class="form-label" for="new-password">Nueva contraseña</label>
          <div class="input-wrapper">
            <input
              id="new-password"
              v-model="form.newPassword"
              :type="showNew ? 'text' : 'password'"
              class="form-input"
              :class="{ error: errors.newPassword }"
              placeholder="Mín. 8 caracteres"
              autocomplete="new-password"
              @input="updateStrength"
            />
            <button type="button" class="input-icon" @click="showNew = !showNew">
              <svg v-if="!showNew" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <!-- Strength -->
          <div class="password-strength" v-if="form.newPassword">
            <div class="strength-bars">
              <div v-for="i in 4" :key="i" class="strength-bar" :class="i <= strength ? `active-${strength - 1}` : ''" />
            </div>
            <span class="strength-label">{{ strengthLabel }}</span>
          </div>
          <span v-if="errors.newPassword" class="form-error">⚠ {{ errors.newPassword }}</span>
        </div>

        <!-- Policy hint -->
        <div class="policy-hint">
          <p>La contraseña debe contener:</p>
          <ul>
            <li :class="{ met: /[A-Z]/.test(form.newPassword) }">✓ Al menos una mayúscula</li>
            <li :class="{ met: /[a-z]/.test(form.newPassword) }">✓ Al menos una minúscula</li>
            <li :class="{ met: /\d/.test(form.newPassword) }">✓ Al menos un número</li>
            <li :class="{ met: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(form.newPassword) }">✓ Al menos un carácter especial</li>
            <li :class="{ met: form.newPassword.length >= 8 }">✓ Mínimo 8 caracteres</li>
          </ul>
        </div>

        <!-- Confirmar -->
        <div class="form-group">
          <label class="form-label" for="confirm-password">Confirmar contraseña</label>
          <input
            id="confirm-password"
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            :class="{ error: errors.confirmPassword }"
            placeholder="Repite la contraseña"
            autocomplete="new-password"
          />
          <span v-if="errors.confirmPassword" class="form-error">⚠ {{ errors.confirmPassword }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-w-full" :disabled="loading" id="btn-reset-password">
          <span v-if="loading" class="spinner" />
          <span v-else>Actualizar contraseña</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const route     = useRoute()
const authStore = useAuthStore()
const toast     = useToast()

const token   = route.query.token || ''
const loading = ref(false)
const success = ref(false)
const showNew = ref(false)
const strength = ref(0)

const form   = reactive({ newPassword: '', confirmPassword: '' })
const errors = reactive({ newPassword: '', confirmPassword: '' })

const strengthLabel = computed(() => {
  return ['', 'Muy débil', 'Débil', 'Aceptable', 'Fuerte'][strength.value]
})

function updateStrength() {
  const p = form.newPassword
  let s = 0
  if (p.length >= 8) s++
  if (/[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++
  if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(p)) s++
  strength.value = s
}

function validate() {
  errors.newPassword = ''
  errors.confirmPassword = ''
  let valid = true
  if (!form.newPassword) { errors.newPassword = 'Ingresa la nueva contraseña'; valid = false }
  else if (form.newPassword.length < 8) { errors.newPassword = 'Mínimo 8 caracteres'; valid = false }
  if (form.newPassword !== form.confirmPassword) { errors.confirmPassword = 'Las contraseñas no coinciden'; valid = false }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    await authStore.resetPassword(token, form.newPassword)
    success.value = true
  } catch (err) {
    const msg = err.response?.data?.message || 'Token inválido o expirado'
    toast.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.error-box {
  display: flex; flex-direction: column; gap: var(--space-4); text-align: center;
}
.error-msg { color: var(--color-warning); font-size: var(--font-size-sm); }
.success-box {
  display: flex; flex-direction: column; align-items: center;
  text-align: center; gap: var(--space-4); padding: var(--space-4) 0;
}
.success-icon  { font-size: 3rem; }
.success-title { font-size: var(--font-size-xl); font-weight: 700; }
.success-msg   { font-size: var(--font-size-sm); color: var(--color-text-muted); }

.policy-hint {
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}
.policy-hint p { margin-bottom: var(--space-2); font-weight: 500; }
.policy-hint ul { display: flex; flex-direction: column; gap: 4px; }
.policy-hint li { transition: color var(--transition-fast); }
.policy-hint li.met { color: var(--color-success); }
</style>

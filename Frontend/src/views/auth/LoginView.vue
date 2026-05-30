<template>
  <div class="auth-layout">
    <div class="auth-card card-glass">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="auth-logo-icon">🛡️</div>
        <div>
          <p class="auth-title">GestionUsuario</p>
          <p class="auth-subtitle">Inicia sesión en tu cuenta</p>
        </div>
      </div>

      <!-- Form -->
      <form class="auth-form" @submit.prevent="handleSubmit" id="form-login" novalidate>
        <!-- Email -->
        <div class="form-group">
          <label class="form-label" for="input-email">Correo electrónico</label>
          <input
            id="input-email"
            v-model="form.email"
            type="email"
            class="form-input"
            :class="{ error: errors.email }"
            placeholder="tu@correo.com"
            autocomplete="email"
          />
          <span v-if="errors.email" class="form-error">⚠ {{ errors.email }}</span>
        </div>

        <!-- Password -->
        <div class="form-group">
          <div class="flex justify-between items-center">
            <label class="form-label" for="input-password">Contraseña</label>
            <RouterLink to="/auth/forgot-password" class="forgot-link" id="link-forgot-password">
              ¿Olvidaste tu contraseña?
            </RouterLink>
          </div>
          <div class="input-wrapper">
            <input
              id="input-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              :class="{ error: errors.password }"
              placeholder="••••••••"
              autocomplete="current-password"
            />
            <button
              type="button"
              class="input-icon"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            >
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <span v-if="errors.password" class="form-error">⚠ {{ errors.password }}</span>
        </div>

        <!-- Submit -->
        <button
          type="submit"
          class="btn btn-primary btn-w-full"
          :disabled="loading"
          id="btn-login"
        >
          <span v-if="loading" class="spinner" />
          <span v-else>Iniciar sesión</span>
        </button>
      </form>

      <p class="auth-footer-link">
        ¿No tienes cuenta?
        <RouterLink to="/auth/register" id="link-go-register">Regístrate aquí</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const router    = useRouter()
const authStore = useAuthStore()
const toast     = useToast()

const loading      = ref(false)
const showPassword = ref(false)

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })

function validate() {
  errors.email    = ''
  errors.password = ''
  let valid = true

  if (!form.email) {
    errors.email = 'El correo es obligatorio'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = 'Formato de correo inválido'
    valid = false
  }
  if (!form.password) {
    errors.password = 'La contraseña es obligatoria'
    valid = false
  }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    await authStore.login(form.email, form.password)
    toast.success(`¡Bienvenido, ${authStore.nombreCompleto.split(' ')[0]}!`)
    router.push(authStore.isAdmin ? '/admin/users' : '/profile')
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data?.error || 'Credenciales incorrectas'
    toast.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-link {
  font-size: var(--font-size-xs);
  color: var(--color-primary-h);
  transition: color var(--transition-fast);
}
.forgot-link:hover { color: var(--color-accent); }
</style>

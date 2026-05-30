<template>
  <div class="auth-layout">
    <div class="auth-card card-glass">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="auth-logo-icon">🛡️</div>
        <div>
          <p class="auth-title">Crear Cuenta</p>
          <p class="auth-subtitle">Completa el formulario para registrarte</p>
        </div>
      </div>

      <!-- Success state -->
      <Transition name="fade">
        <div v-if="success" class="success-box">
          <div class="success-icon">✅</div>
          <h2 class="success-title">¡Registro exitoso!</h2>
          <p class="success-msg">
            Tu cuenta fue creada. Un administrador debe activarla antes de que puedas ingresar.
          </p>
          <RouterLink to="/auth/login" class="btn btn-primary btn-w-full" id="link-go-login-from-register">
            Ir al Login
          </RouterLink>
        </div>
      </Transition>

      <!-- Form -->
      <form v-if="!success" class="auth-form" @submit.prevent="handleSubmit" id="form-register" novalidate>
        <!-- Nombre completo -->
        <div class="form-group">
          <label class="form-label" for="reg-nombre">Nombre completo *</label>
          <input
            id="reg-nombre"
            v-model="form.nombreCompleto"
            type="text"
            class="form-input"
            :class="{ error: errors.nombreCompleto }"
            placeholder="Juan Pérez García"
            autocomplete="name"
          />
          <span v-if="errors.nombreCompleto" class="form-error">⚠ {{ errors.nombreCompleto }}</span>
        </div>

        <!-- Email -->
        <div class="form-group">
          <label class="form-label" for="reg-email">Correo electrónico *</label>
          <input
            id="reg-email"
            v-model="form.email"
            type="email"
            class="form-input"
            :class="{ error: errors.email }"
            placeholder="tu@correo.com"
            autocomplete="email"
          />
          <span v-if="errors.email" class="form-error">⚠ {{ errors.email }}</span>
        </div>

        <!-- RUT (opcional) -->
        <div class="form-group">
          <label class="form-label" for="reg-rut">RUT <span class="text-faint">(opcional)</span></label>
          <input
            id="reg-rut"
            v-model="form.rut"
            type="text"
            class="form-input"
            placeholder="12.345.678-9"
            autocomplete="off"
          />
        </div>

        <!-- Teléfono (opcional) -->
        <div class="form-group">
          <label class="form-label" for="reg-telefono">Teléfono <span class="text-faint">(opcional)</span></label>
          <input
            id="reg-telefono"
            v-model="form.telefono"
            type="tel"
            class="form-input"
            placeholder="+56 9 1234 5678"
            autocomplete="tel"
          />
        </div>

        <!-- Password -->
        <div class="form-group">
          <label class="form-label" for="reg-password">Contraseña *</label>
          <div class="input-wrapper">
            <input
              id="reg-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              :class="{ error: errors.password }"
              placeholder="Mín. 8 caracteres"
              autocomplete="new-password"
              @input="updateStrength"
            />
            <button type="button" class="input-icon" @click="showPassword = !showPassword">
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <!-- Strength bars -->
          <div class="password-strength" v-if="form.password">
            <div class="strength-bars">
              <div
                v-for="i in 4"
                :key="i"
                class="strength-bar"
                :class="i <= strength ? `active-${strength - 1}` : ''"
              />
            </div>
            <span class="strength-label">{{ strengthLabel }}</span>
          </div>
          <span v-if="errors.password" class="form-error">⚠ {{ errors.password }}</span>
        </div>

        <!-- Confirm password -->
        <div class="form-group">
          <label class="form-label" for="reg-confirm">Confirmar contraseña *</label>
          <input
            id="reg-confirm"
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            :class="{ error: errors.confirmPassword }"
            placeholder="Repite la contraseña"
            autocomplete="new-password"
          />
          <span v-if="errors.confirmPassword" class="form-error">⚠ {{ errors.confirmPassword }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-w-full" :disabled="loading" id="btn-register">
          <span v-if="loading" class="spinner" />
          <span v-else>Crear cuenta</span>
        </button>
      </form>

      <p v-if="!success" class="auth-footer-link">
        ¿Ya tienes cuenta?
        <RouterLink to="/auth/login" id="link-go-login">Inicia sesión</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const authStore = useAuthStore()
const toast     = useToast()

const loading      = ref(false)
const success      = ref(false)
const showPassword = ref(false)
const strength     = ref(0)

const form = reactive({
  nombreCompleto: '',
  email: '',
  rut: '',
  telefono: '',
  password: '',
  confirmPassword: '',
})
const errors = reactive({
  nombreCompleto: '', email: '', password: '', confirmPassword: '',
})

const strengthLabel = computed(() => {
  const labels = ['', 'Muy débil', 'Débil', 'Aceptable', 'Fuerte']
  return labels[strength.value]
})

function updateStrength() {
  const p = form.password
  let s = 0
  if (p.length >= 8)             s++
  if (/[A-Z]/.test(p))          s++
  if (/\d/.test(p))              s++
  if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(p)) s++
  strength.value = s
}

function validate() {
  let valid = true
  Object.keys(errors).forEach(k => (errors[k] = ''))

  if (!form.nombreCompleto.trim()) { errors.nombreCompleto = 'El nombre es obligatorio'; valid = false }
  if (!form.email) { errors.email = 'El correo es obligatorio'; valid = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { errors.email = 'Formato inválido'; valid = false }
  if (!form.password) { errors.password = 'La contraseña es obligatoria'; valid = false }
  else if (form.password.length < 8) { errors.password = 'Mínimo 8 caracteres'; valid = false }
  if (form.password !== form.confirmPassword) { errors.confirmPassword = 'Las contraseñas no coinciden'; valid = false }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    await authStore.register({
      nombreCompleto: form.nombreCompleto,
      email: form.email,
      password: form.password,
      rut: form.rut || undefined,
      telefono: form.telefono || undefined,
    })
    success.value = true
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data?.error || 'Error al registrar usuario'
    toast.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.success-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-4);
  padding: var(--space-4) 0;
}
.success-icon { font-size: 3rem; }
.success-title { font-size: var(--font-size-xl); font-weight: 700; }
.success-msg { font-size: var(--font-size-sm); color: var(--color-text-muted); line-height: 1.6; }
</style>

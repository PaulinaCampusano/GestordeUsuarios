<template>
  <div class="auth-layout">
    <div class="auth-card card-glass">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="auth-logo-icon">🔑</div>
        <div>
          <p class="auth-title">Recuperar acceso</p>
          <p class="auth-subtitle">Te enviaremos un enlace de recuperación</p>
        </div>
      </div>

      <!-- Sent state -->
      <Transition name="fade">
        <div v-if="sent" class="sent-box">
          <div class="sent-icon">📧</div>
          <h2 class="sent-title">Correo enviado</h2>
          <p class="sent-msg">
            Si el correo <strong>{{ sentEmail }}</strong> está registrado, recibirás las instrucciones en los próximos minutos. Revisa tu carpeta de spam.
          </p>
          <RouterLink to="/auth/login" class="btn btn-ghost btn-w-full" id="link-back-to-login">
            Volver al login
          </RouterLink>
        </div>
      </Transition>

      <!-- Form -->
      <form v-if="!sent" class="auth-form" @submit.prevent="handleSubmit" id="form-forgot-password" novalidate>
        <div class="form-group">
          <label class="form-label" for="forgot-email">Correo electrónico</label>
          <input
            id="forgot-email"
            v-model="email"
            type="email"
            class="form-input"
            :class="{ error: emailError }"
            placeholder="tu@correo.com"
            autocomplete="email"
          />
          <span v-if="emailError" class="form-error">⚠ {{ emailError }}</span>
        </div>

        <button type="submit" class="btn btn-primary btn-w-full" :disabled="loading" id="btn-send-reset">
          <span v-if="loading" class="spinner" />
          <span v-else>Enviar enlace</span>
        </button>
      </form>

      <p v-if="!sent" class="auth-footer-link">
        <RouterLink to="/auth/login" id="link-back-login">← Volver al login</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const authStore = useAuthStore()
const toast     = useToast()

const email      = ref('')
const emailError = ref('')
const loading    = ref(false)
const sent       = ref(false)
const sentEmail  = ref('')

async function handleSubmit() {
  emailError.value = ''
  if (!email.value) { emailError.value = 'El correo es obligatorio'; return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    emailError.value = 'Formato de correo inválido'; return
  }

  loading.value = true
  try {
    await authStore.requestPasswordReset(email.value)
    sentEmail.value = email.value
    sent.value = true
  } catch {
    // Por seguridad siempre mostramos el estado enviado
    sentEmail.value = email.value
    sent.value = true
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.sent-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-4);
  padding: var(--space-4) 0;
}
.sent-icon  { font-size: 3rem; }
.sent-title { font-size: var(--font-size-xl); font-weight: 700; }
.sent-msg   {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  line-height: 1.6;
}
.sent-msg strong { color: var(--color-text); }
</style>

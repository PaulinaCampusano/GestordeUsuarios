<template>
  <header class="app-navbar">
    <div class="flex items-center gap-3">
      <!-- Hamburger (mobile) -->
      <button
        class="btn btn-icon btn-ghost mobile-menu-btn"
        @click="emit('toggle-sidebar')"
        id="btn-toggle-sidebar"
        aria-label="Abrir menú"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>
      <div>
        <h1 class="navbar-page-title">{{ pageTitle }}</h1>
      </div>
    </div>

    <div class="flex items-center gap-3">
      <div class="navbar-greeting">
        Hola, <span class="navbar-name">{{ authStore.nombreCompleto.split(' ')[0] }}</span>
      </div>
      <div class="user-avatar user-avatar-sm">{{ authStore.initials }}</div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits(['toggle-sidebar'])
const route     = useRoute()
const authStore = useAuthStore()

const titleMap = {
  Profile:    'Mi Perfil',
  AdminUsers: 'Gestión de Usuarios',
}
const pageTitle = computed(() => titleMap[route.name] || 'Panel de Control')
</script>

<style scoped>
.navbar-page-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
}
.navbar-greeting {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.navbar-name {
  color: var(--color-primary-h);
  font-weight: 600;
}
.user-avatar-sm {
  width: 32px;
  height: 32px;
  font-size: 0.75rem;
}
@media (min-width: 769px) {
  .mobile-menu-btn { display: none; }
}
</style>

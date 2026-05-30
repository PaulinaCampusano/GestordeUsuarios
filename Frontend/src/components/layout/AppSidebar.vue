<template>
  <aside class="sidebar" :class="{ open: isOpen }">
    <!-- Brand -->
    <div class="sidebar-brand">
      <div class="sidebar-brand-icon">🛡️</div>
      <span class="sidebar-brand-name">GestionUsuario</span>
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <p class="nav-section-label">General</p>

      <RouterLink
        v-for="item in generalNav"
        :key="item.to"
        :to="item.to"
        custom
        v-slot="{ navigate, isActive }"
      >
        <div
          class="nav-item"
          :class="{ active: isActive }"
          @click="navigate(); emit('close')"
          :id="`nav-${item.id}`"
        >
          <span class="nav-icon" v-html="item.icon" />
          {{ item.label }}
        </div>
      </RouterLink>

      <template v-if="authStore.isAdmin">
        <p class="nav-section-label">Administración</p>
        <RouterLink
          v-for="item in adminNav"
          :key="item.to"
          :to="item.to"
          custom
          v-slot="{ navigate, isActive }"
        >
          <div
            class="nav-item"
            :class="{ active: isActive }"
            @click="navigate(); emit('close')"
            :id="`nav-${item.id}`"
          >
            <span class="nav-icon" v-html="item.icon" />
            {{ item.label }}
          </div>
        </RouterLink>
      </template>
    </nav>

    <!-- User info + logout -->
    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-avatar">{{ authStore.initials }}</div>
        <div class="user-info">
          <p class="user-name">{{ authStore.nombreCompleto }}</p>
          <p class="user-role">{{ roleLabel }}</p>
        </div>
        <button
          class="btn btn-icon btn-ghost"
          @click="handleLogout"
          title="Cerrar sesión"
          id="btn-logout"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const props = defineProps({ isOpen: Boolean })
const emit  = defineEmits(['close'])

const authStore = useAuthStore()
const router    = useRouter()
const toast     = useToast()

const roleLabel = computed(() => {
  const map = {
    ROLE_ADMIN:      'Administrador',
    ROLE_USER:       'Usuario',
    ROLE_SUPERVISOR: 'Supervisor',
  }
  return map[authStore.user?.rol] || authStore.user?.rol || 'Usuario'
})

const generalNav = [
  {
    id: 'profile', to: '/profile', label: 'Mi Perfil',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>`,
  },
]

const adminNav = [
  {
    id: 'users', to: '/admin/users', label: 'Usuarios',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  },
]

async function handleLogout() {
  await authStore.logout()
  toast.success('Sesión cerrada correctamente')
  router.push('/auth/login')
}
</script>

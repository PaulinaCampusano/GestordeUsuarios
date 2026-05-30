import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ── Redirect raíz ──────────────────────────────────────────
  {
    path: '/',
    redirect: '/auth/login',
  },

  // ── Autenticación (públicas) ────────────────────────────────
  {
    path: '/auth',
    redirect: '/auth/login',
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresGuest: true, title: 'Iniciar Sesión' },
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresGuest: true, title: 'Crear Cuenta' },
  },
  {
    path: '/auth/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: { requiresGuest: true, title: 'Recuperar Contraseña' },
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/auth/ResetPasswordView.vue'),
    meta: { requiresGuest: true, title: 'Nueva Contraseña' },
  },

  // ── App (requieren auth) ────────────────────────────────────
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { requiresAuth: true, title: 'Mi Perfil' },
  },

  // ── Admin ────────────────────────────────────────────────────
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UsersView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, title: 'Gestión de Usuarios' },
  },

  // ── 404 ──────────────────────────────────────────────────────
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// ── Navigation Guards ─────────────────────────────────────────
router.beforeEach((to, _from, next) => {
  // Actualizar título del documento
  document.title = to.meta.title
    ? `${to.meta.title} — GestionUsuario`
    : 'GestionUsuario'

  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next('/auth/login')
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    return next('/profile')
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return next('/profile')
  }

  next()
})

export default router

<template>
  <div class="app-layout">
    <AppSidebar :isOpen="sidebarOpen" @close="sidebarOpen = false" />

    <!-- Overlay mobile -->
    <Transition name="fade">
      <div
        v-if="sidebarOpen"
        class="sidebar-overlay"
        @click="sidebarOpen = false"
      />
    </Transition>

    <div class="app-main">
      <AppNavbar @toggle-sidebar="sidebarOpen = !sidebarOpen" />
      <main class="app-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppNavbar  from './AppNavbar.vue'

const sidebarOpen = ref(false)
</script>

<style scoped>
.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 99;
}
@media (max-width: 768px) {
  .sidebar-overlay { display: block; }
}
</style>

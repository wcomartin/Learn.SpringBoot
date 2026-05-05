<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUser, clearAuth, isAdmin } from '@/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const user = getUser()
const { toasts } = useToast()

const navItems = computed(() => {
  const items = [
    { to: '/', label: 'Dashboard', icon: '📊' },
    { to: '/companies', label: 'Companies', icon: '🏢' },
    { to: '/contacts', label: 'Contacts', icon: '👤' },
    { to: '/products', label: 'Products', icon: '📦' },
    { to: '/orders', label: 'Sales Orders', icon: '🛒' },
    { to: '/purchasing', label: 'Purchasing', icon: '📋' },
    { to: '/shipments', label: 'Shipments', icon: '🚚' },
  ]
  if (isAdmin()) items.push({ to: '/admin', label: 'Admin', icon: '⚙️' })
  return items
})

function logout() {
  clearAuth()
  router.push({ name: 'login' })
}

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex">
    <aside class="w-64 bg-gray-800 border-r border-gray-700 flex flex-col fixed inset-y-0">
      <div class="p-4 border-b border-gray-700">
        <h1 class="text-lg font-bold text-white">📦 Inventory</h1>
      </div>
      <nav class="flex-1 p-3 space-y-1 overflow-y-auto">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          :class="[
            'block px-3 py-2 rounded-lg text-sm font-medium transition-colors',
            isActive(item.to) ? 'bg-indigo-600 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
          ]"
        >
          {{ item.icon }} {{ item.label }}
        </router-link>
      </nav>
      <div class="p-4 border-t border-gray-700">
        <div class="text-sm text-gray-400 mb-2">{{ user?.name }}</div>
        <button @click="logout" class="text-sm text-gray-500 hover:text-white transition-colors">Sign Out</button>
      </div>
    </aside>

    <main class="flex-1 ml-64 p-8 overflow-auto">
      <router-view />
    </main>

    <!-- Global toasts -->
    <div class="fixed top-4 right-4 z-50 space-y-2">
      <transition-group name="toast">
        <div v-for="t in toasts" :key="t.id"
          :class="['px-4 py-3 rounded-lg shadow-lg text-sm font-medium', t.type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white']">
          {{ t.message }}
        </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(1rem); }
</style>

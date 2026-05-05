<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/auth'

const users = ref<any[]>([])

async function fetchUsers() {
  const res = await api('/api/admin/users')
  if (res.ok) users.value = await res.json()
}

async function toggleRole(user: any) {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const res = await api(`/api/admin/users/${user.id}/role`, {
    method: 'PATCH',
    body: JSON.stringify({ role: newRole })
  })
  if (res.ok) await fetchUsers()
}

onMounted(fetchUsers)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-semibold text-white">Admin — Users</h2>
      <span class="text-sm text-gray-400">{{ users.length }} users</span>
    </div>
    <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Name</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Email</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Role</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" class="border-b border-gray-700/50 hover:bg-gray-700/30">
            <td class="px-4 py-3 text-sm text-white font-medium">{{ u.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ u.email }}</td>
            <td class="px-4 py-3">
              <span :class="[
                'px-2 py-0.5 text-xs font-medium rounded-full',
                u.role === 'ADMIN' ? 'bg-purple-900/50 text-purple-400' : 'bg-gray-700 text-gray-400'
              ]">{{ u.role }}</span>
            </td>
            <td class="px-4 py-3 text-right">
              <button
                @click="toggleRole(u)"
                class="text-xs text-indigo-400 hover:text-indigo-300 transition-colors"
              >
                {{ u.role === 'ADMIN' ? 'Demote' : 'Promote' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const router = useRouter()
const { success, error: showError } = useToast()
const { data: companies, loading, fetch, create } = useApi<any>('/api/companies')

const showForm = ref(false)
const search = ref('')
const form = ref({ name: '', email: '', phone: '', website: '', status: 'ACTIVE' })

async function handleCreate() {
  const ok = await create(form.value)
  if (ok) {
    showForm.value = false
    form.value = { name: '', email: '', phone: '', website: '', status: 'ACTIVE' }
    success('Company created')
  } else {
    showError('Failed to create company')
  }
}

function doSearch() {
  const params: Record<string, string> = {}
  if (search.value) params.search = search.value
  fetch(params)
}

onMounted(() => fetch())
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Companies</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">
        + New Company
      </button>
    </div>

    <div class="mb-4">
      <input v-model="search" @input="doSearch" placeholder="Search companies..."
        class="w-full max-w-sm px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500" />
    </div>

    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Name</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Email</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Phone</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in companies" :key="c.id" @click="router.push(`/companies/${c.id}`)"
            class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-white font-medium">{{ c.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ c.email || '—' }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ c.phone || '—' }}</td>
            <td class="px-4 py-3"><StatusBadge :status="c.status" /></td>
          </tr>
          <tr v-if="!companies.length">
            <td colspan="4" class="px-4 py-8 text-center text-gray-500">No companies found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Company" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-400 mb-1">Name *</label>
          <input v-model="form.name" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Email</label>
          <input v-model="form.email" type="email" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Phone</label>
          <input v-model="form.phone" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Website</label>
          <input v-model="form.website" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" />
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Status</label>
          <select v-model="form.status" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="LEAD">Lead</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">
          Create Company
        </button>
      </form>
    </SlideOver>
  </div>
</template>

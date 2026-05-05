<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { api } from '@/auth'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { success, error: showError } = useToast()
const { data: contacts, loading, fetch, create } = useApi<any>('/api/contacts')

const showForm = ref(false)
const search = ref('')
const form = ref({ firstName: '', lastName: '', email: '', phone: '', jobTitle: '', companyId: null as number | null })

// Company search/select
const companies = ref<any[]>([])
const companySearch = ref('')
const companyDropdownOpen = ref(false)
const selectedCompany = ref<any>(null)

const filteredCompanies = computed(() => {
  if (!companySearch.value) return companies.value
  const term = companySearch.value.toLowerCase()
  return companies.value.filter(c => c.name.toLowerCase().includes(term))
})

function selectCompany(company: any) {
  selectedCompany.value = company
  form.value.companyId = company.id
  companySearch.value = company.name
  companyDropdownOpen.value = false
}

function clearCompany() {
  selectedCompany.value = null
  form.value.companyId = null
  companySearch.value = ''
}

async function handleCreate() {
  const body: any = { ...form.value }
  if (!body.companyId) delete body.companyId
  const ok = await create(body)
  if (ok) {
    showForm.value = false
    form.value = { firstName: '', lastName: '', email: '', phone: '', jobTitle: '', companyId: null }
    selectedCompany.value = null
    companySearch.value = ''
    success('Contact created')
  } else { showError('Failed to create contact') }
}

function doSearch() {
  const params: Record<string, string> = {}
  if (search.value) params.search = search.value
  fetch(params)
}

onMounted(async () => {
  fetch()
  const res = await api('/api/companies')
  if (res.ok) companies.value = await res.json()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Contacts</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">+ New Contact</button>
    </div>
    <div class="mb-4">
      <input v-model="search" @input="doSearch" placeholder="Search contacts..."
        class="w-full max-w-sm px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500" />
    </div>
    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Name</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Company</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Email</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Job Title</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Primary</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in contacts" :key="c.id" @click="$router.push(`/contacts/${c.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-white font-medium">{{ c.fullName }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ c.company?.name || '—' }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ c.email || '—' }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ c.jobTitle || '—' }}</td>
            <td class="px-4 py-3">
              <span v-if="c.isPrimary" class="px-2 py-0.5 text-xs font-medium rounded-full bg-indigo-900/50 text-indigo-400">Primary</span>
            </td>
          </tr>
          <tr v-if="!contacts.length"><td colspan="5" class="px-4 py-8 text-center text-gray-500">No contacts found</td></tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Contact" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div><label class="block text-sm text-gray-400 mb-1">First Name *</label><input v-model="form.firstName" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Last Name *</label><input v-model="form.lastName" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Email</label><input v-model="form.email" type="email" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Phone</label><input v-model="form.phone" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Job Title</label><input v-model="form.jobTitle" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>

        <!-- Company search/select -->
        <div class="relative">
          <label class="block text-sm text-gray-400 mb-1">Company</label>
          <div v-if="selectedCompany" class="flex items-center justify-between px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg">
            <span class="text-white text-sm">{{ selectedCompany.name }}</span>
            <button type="button" @click="clearCompany" class="text-gray-400 hover:text-white text-sm">✕</button>
          </div>
          <div v-else>
            <input
              v-model="companySearch"
              @focus="companyDropdownOpen = true"
              @input="companyDropdownOpen = true"
              placeholder="Search companies..."
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500"
            />
            <div v-if="companyDropdownOpen && filteredCompanies.length" class="absolute z-10 mt-1 w-full bg-gray-700 border border-gray-600 rounded-lg shadow-lg max-h-48 overflow-y-auto">
              <button
                v-for="c in filteredCompanies"
                :key="c.id"
                type="button"
                @click="selectCompany(c)"
                class="w-full text-left px-3 py-2 text-sm text-white hover:bg-gray-600 transition-colors"
              >
                {{ c.name }}
              </button>
            </div>
          </div>
        </div>

        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Create Contact</button>
      </form>
    </SlideOver>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/auth'
import { useToast } from '@/composables/useToast'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const route = useRoute()
const router = useRouter()
const { success } = useToast()
const contact = ref<any>(null)
const notes = ref<any[]>([])
const loading = ref(true)
const showEdit = ref(false)
const editForm = ref({ firstName: '', lastName: '', email: '', phone: '', jobTitle: '' })

async function fetchData() {
  const res = await api(`/api/contacts/${route.params.id}`)
  if (res.ok) contact.value = await res.json()
  else router.push('/contacts')

  const n = await api(`/api/contacts/${route.params.id}/notes`)
  if (n.ok) notes.value = await n.json()
  loading.value = false
}

function openEdit() {
  if (!contact.value) return
  editForm.value = {
    firstName: contact.value.firstName,
    lastName: contact.value.lastName,
    email: contact.value.email || '',
    phone: contact.value.phone || '',
    jobTitle: contact.value.jobTitle || ''
  }
  showEdit.value = true
}

async function saveEdit() {
  const res = await api(`/api/contacts/${route.params.id}`, { method: 'PATCH', body: JSON.stringify(editForm.value) })
  if (res.ok) { contact.value = await res.json(); showEdit.value = false; success('Contact updated') }
}

async function deleteContact() {
  const res = await api(`/api/contacts/${route.params.id}`, { method: 'DELETE' })
  if (res.ok) { success('Contact deleted'); router.push('/contacts') }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/contacts')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Contacts</button>

    <Skeleton v-if="loading" />
    <template v-else-if="contact">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white">{{ contact.fullName }}</h2>
            <p v-if="contact.jobTitle" class="text-sm text-gray-400 mt-1">{{ contact.jobTitle }}</p>
            <div class="mt-3 space-y-1 text-sm text-gray-400">
              <p v-if="contact.email">📧 {{ contact.email }}</p>
              <p v-if="contact.phone">📞 {{ contact.phone }}</p>
            </div>
          </div>
          <div class="text-right">
            <span v-if="contact.isPrimary" class="px-2 py-0.5 text-xs font-medium rounded-full bg-indigo-900/50 text-indigo-400">Primary Contact</span>
          </div>
        </div>

        <div class="flex gap-2 mt-4">
          <button @click="openEdit" class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm rounded-lg transition-colors">Edit</button>
          <button @click="deleteContact" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Delete</button>
        </div>

        <div v-if="contact.company" class="mt-4 pt-4 border-t border-gray-700">
          <p class="text-xs text-gray-500 uppercase mb-1">Company</p>
          <router-link :to="`/companies/${contact.company.id}`" class="text-indigo-400 hover:text-indigo-300 text-sm font-medium">
            {{ contact.company.name }}
          </router-link>
        </div>
      </div>

      <h3 class="text-lg font-semibold text-white mb-4">Notes ({{ notes.length }})</h3>
      <div v-if="notes.length" class="space-y-3">
        <div v-for="note in notes" :key="note.id" class="bg-gray-800 rounded-xl border border-gray-700 p-4">
          <div class="flex items-center justify-between mb-2">
            <span class="px-2 py-0.5 text-xs font-medium rounded-full bg-gray-700 text-gray-300">{{ note.kind }}</span>
            <span class="text-xs text-gray-500">{{ new Date(note.createdAt).toLocaleDateString() }}</span>
          </div>
          <p class="text-sm text-gray-300">{{ note.body }}</p>
        </div>
      </div>
      <p v-else class="text-gray-500 text-sm">No notes yet</p>

      <SlideOver :open="showEdit" title="Edit Contact" @close="showEdit = false">
        <form @submit.prevent="saveEdit" class="space-y-4">
          <div><label class="block text-sm text-gray-400 mb-1">First Name</label><input v-model="editForm.firstName" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Last Name</label><input v-model="editForm.lastName" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Email</label><input v-model="editForm.email" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Phone</label><input v-model="editForm.phone" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Job Title</label><input v-model="editForm.jobTitle" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Save</button>
        </form>
      </SlideOver>
    </template>
  </div>
</template>

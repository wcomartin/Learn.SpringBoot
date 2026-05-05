<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/auth'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const route = useRoute()
const router = useRouter()
const { success } = useToast()
const company = ref<any>(null)
const contacts = ref<any[]>([])
const orders = ref<any[]>([])
const shipments = ref<any[]>([])
const loading = ref(true)
const showEdit = ref(false)
const editForm = ref({ name: '', email: '', phone: '', website: '', status: '' })

async function fetchData() {
  const [c, ct, so] = await Promise.all([
    api(`/api/companies/${route.params.id}`),
    api(`/api/contacts?companyId=${route.params.id}`),
    api(`/api/sales/orders?customerId=${route.params.id}`)
  ])
  if (c.ok) company.value = await c.json()
  else router.push('/companies')
  if (ct.ok) contacts.value = await ct.json()
  if (so.ok) {
    orders.value = await so.json()
    // Fetch shipments for all orders
    const orderIds = orders.value.map((o: any) => o.id)
    const shipmentResults = await Promise.all(orderIds.map((id: number) => api(`/api/shipments?salesOrderId=${id}`)))
    const allShipments: any[] = []
    for (const res of shipmentResults) {
      if (res.ok) allShipments.push(...await res.json())
    }
    shipments.value = allShipments
  }
  loading.value = false
}

function openEdit() {
  if (!company.value) return
  editForm.value = { name: company.value.name, email: company.value.email || '', phone: company.value.phone || '', website: company.value.website || '', status: company.value.status }
  showEdit.value = true
}

async function saveEdit() {
  const res = await api(`/api/companies/${route.params.id}`, { method: 'PATCH', body: JSON.stringify(editForm.value) })
  if (res.ok) { company.value = await res.json(); showEdit.value = false; success('Company updated') }
}

async function deleteCompany() {
  const res = await api(`/api/companies/${route.params.id}`, { method: 'DELETE' })
  if (res.ok) { success('Company deleted'); router.push('/companies') }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/companies')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Companies</button>

    <Skeleton v-if="loading" />
    <template v-else-if="company">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white">{{ company.name }}</h2>
            <div class="mt-2 space-y-1 text-sm text-gray-400">
              <p v-if="company.email">📧 {{ company.email }}</p>
              <p v-if="company.phone">📞 {{ company.phone }}</p>
              <p v-if="company.website">🌐 {{ company.website }}</p>
            </div>
          </div>
          <StatusBadge :status="company.status" />
        </div>
        <div class="flex gap-2 mt-4">
          <button @click="openEdit" class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm rounded-lg transition-colors">Edit</button>
          <button @click="deleteCompany" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Delete</button>
        </div>
      </div>

      <!-- Contacts -->
      <h3 class="text-lg font-semibold text-white mb-4">Contacts ({{ contacts.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-700">
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Name</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Email</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Job Title</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Primary</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in contacts" :key="c.id" @click="router.push(`/contacts/${c.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-medium">{{ c.fullName }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ c.email || '—' }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ c.jobTitle || '—' }}</td>
              <td class="px-4 py-3">
                <span v-if="c.isPrimary" class="px-2 py-0.5 text-xs font-medium rounded-full bg-indigo-900/50 text-indigo-400">Primary</span>
              </td>
            </tr>
            <tr v-if="!contacts.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No contacts</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Sales Orders -->
      <h3 class="text-lg font-semibold text-white mb-4">Sales Orders ({{ orders.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-700">
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Order #</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Date</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in orders" :key="o.id" @click="router.push(`/orders/${o.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ o.orderNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ o.orderDate }}</td>
              <td class="px-4 py-3"><StatusBadge :status="o.status" /></td>
              <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ o.total }}</td>
            </tr>
            <tr v-if="!orders.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No sales orders</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Shipments -->
      <h3 class="text-lg font-semibold text-white mb-4">Shipments ({{ shipments.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-700">
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Shipment #</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Carrier</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Tracking</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in shipments" :key="s.id" @click="router.push(`/shipments/${s.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ s.shipmentNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ s.carrierName || '—' }}</td>
              <td class="px-4 py-3"><StatusBadge :status="s.status" /></td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ s.trackingNumber || '—' }}</td>
            </tr>
            <tr v-if="!shipments.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No shipments</td></tr>
          </tbody>
        </table>
      </div>

      <SlideOver :open="showEdit" title="Edit Company" @close="showEdit = false">
        <form @submit.prevent="saveEdit" class="space-y-4">
          <div><label class="block text-sm text-gray-400 mb-1">Name</label><input v-model="editForm.name" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Email</label><input v-model="editForm.email" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Phone</label><input v-model="editForm.phone" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Website</label><input v-model="editForm.website" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Status</label>
            <select v-model="editForm.status" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
              <option value="LEAD">Lead</option><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option>
            </select>
          </div>
          <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Save</button>
        </form>
      </SlideOver>
    </template>
  </div>
</template>

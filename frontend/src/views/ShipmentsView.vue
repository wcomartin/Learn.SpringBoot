<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { api } from '@/auth'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { success, error: showError } = useToast()
const { data: shipments, loading, fetch } = useApi<any>('/api/shipments')
const carriers = ref<any[]>([])
const orders = ref<any[]>([])
const warehouses = ref<any[]>([])

const showForm = ref(false)
const form = ref({ salesOrderId: '', warehouseId: '', carrierId: '' })

async function handleCreate() {
  const body: any = { salesOrderId: Number(form.value.salesOrderId), warehouseId: Number(form.value.warehouseId) }
  if (form.value.carrierId) body.carrierId = Number(form.value.carrierId)
  const res = await api('/api/shipments', { method: 'POST', body: JSON.stringify(body) })
  if (res.ok) {
    showForm.value = false
    form.value = { salesOrderId: '', warehouseId: '', carrierId: '' }
    success('Shipment created')
    fetch()
  } else { showError('Failed to create shipment') }
}

async function updateStatus(id: number, status: string) {
  const res = await api(`/api/shipments/${id}`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { success(`Shipment ${status.toLowerCase()}`); fetch() }
}

onMounted(async () => {
  fetch()
  const [c, o, w] = await Promise.all([api('/api/carriers'), api('/api/sales/orders'), api('/api/warehouses')])
  if (c.ok) carriers.value = await c.json()
  if (o.ok) orders.value = await o.json()
  if (w.ok) warehouses.value = await w.json()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Shipments</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">+ New Shipment</button>
    </div>
    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Shipment #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Carrier</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Tracking</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Est. Delivery</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in shipments" :key="s.id" @click="$router.push(`/shipments/${s.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ s.shipmentNumber }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ s.carrierName || '—' }}</td>
            <td class="px-4 py-3"><StatusBadge :status="s.status" /></td>
            <td class="px-4 py-3 text-sm">
              <a v-if="s.trackingUrl" :href="s.trackingUrl" target="_blank" class="text-indigo-400 hover:text-indigo-300">{{ s.trackingNumber }}</a>
              <span v-else class="text-gray-500">{{ s.trackingNumber || '—' }}</span>
            </td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ s.estimatedDelivery || '—' }}</td>
            <td class="px-4 py-3 text-right space-x-2" @click.stop>
              <button v-if="s.status === 'PENDING'" @click="updateStatus(s.id, 'PICKED')" class="text-xs text-orange-400 hover:text-orange-300">Pick</button>
              <button v-if="s.status === 'PICKED'" @click="updateStatus(s.id, 'PACKED')" class="text-xs text-indigo-400 hover:text-indigo-300">Pack</button>
              <button v-if="s.status === 'PACKED'" @click="updateStatus(s.id, 'SHIPPED')" class="text-xs text-purple-400 hover:text-purple-300">Ship</button>
              <button v-if="s.status === 'SHIPPED'" @click="updateStatus(s.id, 'DELIVERED')" class="text-xs text-green-400 hover:text-green-300">Deliver</button>
            </td>
          </tr>
          <tr v-if="!shipments.length"><td colspan="6" class="px-4 py-8 text-center text-gray-500">No shipments yet</td></tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Shipment" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-400 mb-1">Sales Order *</label>
          <select v-model="form.salesOrderId" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">Select order...</option>
            <option v-for="o in orders" :key="o.id" :value="o.id">{{ o.orderNumber }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Warehouse *</label>
          <select v-model="form.warehouseId" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">Select warehouse...</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Carrier</label>
          <select v-model="form.carrierId" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">None</option>
            <option v-for="c in carriers" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Create Shipment</button>
      </form>
    </SlideOver>
  </div>
</template>

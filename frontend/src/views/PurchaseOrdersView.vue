<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { api } from '@/auth'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { success, error: showError } = useToast()
const { data: orders, loading, fetch } = useApi<any>('/api/purchasing/orders')
const products = ref<any[]>([])
const suppliers = ref<any[]>([])
const warehouses = ref<any[]>([])

const showForm = ref(false)
const form = ref({ supplierId: '', warehouseId: '', lineItems: [{ productId: '', quantity: 1 }] as any[] })

function addLine() { form.value.lineItems.push({ productId: '', quantity: 1 }) }
function removeLine(i: number) { form.value.lineItems.splice(i, 1) }

async function handleCreate() {
  const lines = form.value.lineItems.map(l => {
    const p = products.value.find(pr => pr.id === Number(l.productId))
    return { productId: p.id, productSku: p.sku, productName: p.name, quantity: l.quantity, unitCost: p.costPrice }
  })
  const res = await api('/api/purchasing/orders', {
    method: 'POST',
    body: JSON.stringify({ supplierId: Number(form.value.supplierId), warehouseId: Number(form.value.warehouseId), lineItems: lines })
  })
  if (res.ok) {
    showForm.value = false
    form.value = { supplierId: '', warehouseId: '', lineItems: [{ productId: '', quantity: 1 }] }
    success('Purchase order created')
    fetch()
  } else { showError('Failed to create PO') }
}

async function updateStatus(id: number, status: string) {
  const res = await api(`/api/purchasing/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { success(`PO ${status.toLowerCase()}`); fetch() }
}

onMounted(async () => {
  fetch()
  const [p, s, w] = await Promise.all([api('/api/products'), api('/api/suppliers'), api('/api/warehouses')])
  if (p.ok) products.value = await p.json()
  if (s.ok) suppliers.value = await s.json()
  if (w.ok) warehouses.value = await w.json()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Purchase Orders</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">+ New PO</button>
    </div>
    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">PO #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Supplier</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Date</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Total</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="po in orders" :key="po.id" @click="$router.push(`/purchasing/${po.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ po.poNumber }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ po.supplierName }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ po.orderDate }}</td>
            <td class="px-4 py-3"><StatusBadge :status="po.status" /></td>
            <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ po.total }}</td>
            <td class="px-4 py-3 text-right space-x-2" @click.stop>
              <button v-if="po.status === 'DRAFT'" @click="updateStatus(po.id, 'SUBMITTED')" class="text-xs text-blue-400 hover:text-blue-300">Submit</button>
              <button v-if="po.status === 'SUBMITTED'" @click="updateStatus(po.id, 'ACKNOWLEDGED')" class="text-xs text-cyan-400 hover:text-cyan-300">Acknowledge</button>
              <button v-if="po.status === 'ACKNOWLEDGED'" @click="updateStatus(po.id, 'RECEIVED')" class="text-xs text-green-400 hover:text-green-300">Receive</button>
            </td>
          </tr>
          <tr v-if="!orders.length"><td colspan="6" class="px-4 py-8 text-center text-gray-500">No purchase orders yet</td></tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Purchase Order" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-400 mb-1">Supplier *</label>
          <select v-model="form.supplierId" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">Select supplier...</option>
            <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-1">Destination Warehouse *</label>
          <select v-model="form.warehouseId" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">Select warehouse...</option>
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm text-gray-400 mb-2">Line Items</label>
          <div v-for="(line, i) in form.lineItems" :key="i" class="flex gap-2 mb-2">
            <select v-model="line.productId" required class="flex-1 px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white text-sm focus:outline-none focus:border-indigo-500">
              <option value="">Product...</option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.sku }} — {{ p.name }}</option>
            </select>
            <input v-model.number="line.quantity" type="number" min="1" class="w-20 px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white text-sm focus:outline-none focus:border-indigo-500" />
            <button type="button" @click="removeLine(i)" v-if="form.lineItems.length > 1" class="text-red-400 hover:text-red-300 px-2">✕</button>
          </div>
          <button type="button" @click="addLine" class="text-sm text-indigo-400 hover:text-indigo-300">+ Add line</button>
        </div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Create PO</button>
      </form>
    </SlideOver>
  </div>
</template>

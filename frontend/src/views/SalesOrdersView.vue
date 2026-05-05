<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { api } from '@/auth'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { success, error: showError } = useToast()
const { data: orders, loading, fetch } = useApi<any>('/api/sales/orders')
const products = ref<any[]>([])
const companies = ref<any[]>([])

const showForm = ref(false)
const form = ref({ customerId: '', lineItems: [{ productId: '', quantity: 1 }] as any[] })

function addLine() { form.value.lineItems.push({ productId: '', quantity: 1 }) }
function removeLine(i: number) { form.value.lineItems.splice(i, 1) }

async function handleCreate() {
  const lines = form.value.lineItems.map(l => {
    const p = products.value.find(pr => pr.id === Number(l.productId))
    return { productId: p.id, productSku: p.sku, productName: p.name, quantity: l.quantity, unitPrice: p.unitPrice }
  })
  const res = await api('/api/sales/orders', {
    method: 'POST',
    body: JSON.stringify({ customerId: Number(form.value.customerId), lineItems: lines })
  })
  if (res.ok) {
    showForm.value = false
    form.value = { customerId: '', lineItems: [{ productId: '', quantity: 1 }] }
    success('Sales order created')
    fetch()
  } else { showError('Failed to create order') }
}

async function updateStatus(id: number, status: string) {
  const res = await api(`/api/sales/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { success(`Order ${status.toLowerCase()}`); fetch() }
}

onMounted(async () => {
  fetch()
  const [p, c] = await Promise.all([api('/api/products'), api('/api/companies')])
  if (p.ok) products.value = await p.json()
  if (c.ok) companies.value = await c.json()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Sales Orders</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">+ New Order</button>
    </div>
    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Order #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Date</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Items</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Total</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id" @click="$router.push(`/orders/${o.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ o.orderNumber }}</td>
            <td class="px-4 py-3 text-sm text-gray-300">{{ o.orderDate }}</td>
            <td class="px-4 py-3"><StatusBadge :status="o.status" /></td>
            <td class="px-4 py-3 text-sm text-gray-300 text-right">{{ o.lineItems?.length || 0 }}</td>
            <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ o.total }}</td>
            <td class="px-4 py-3 text-right space-x-2" @click.stop>
              <button v-if="o.status === 'DRAFT'" @click="updateStatus(o.id, 'CONFIRMED')" class="text-xs text-blue-400 hover:text-blue-300">Confirm</button>
              <button v-if="o.status === 'CONFIRMED'" @click="updateStatus(o.id, 'SHIPPED')" class="text-xs text-purple-400 hover:text-purple-300">Ship</button>
              <button v-if="o.status === 'SHIPPED'" @click="updateStatus(o.id, 'DELIVERED')" class="text-xs text-green-400 hover:text-green-300">Deliver</button>
            </td>
          </tr>
          <tr v-if="!orders.length"><td colspan="6" class="px-4 py-8 text-center text-gray-500">No sales orders yet</td></tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Sales Order" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div>
          <label class="block text-sm text-gray-400 mb-1">Customer *</label>
          <select v-model="form.customerId" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
            <option value="">Select company...</option>
            <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.name }}</option>
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
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Create Order</button>
      </form>
    </SlideOver>
  </div>
</template>

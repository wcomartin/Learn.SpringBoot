<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/auth'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const route = useRoute()
const router = useRouter()
const { success } = useToast()
const order = ref<any>(null)
const shipments = ref<any[]>([])
const loading = ref(true)

async function fetchData() {
  const res = await api(`/api/sales/orders/${route.params.id}`)
  if (res.ok) order.value = await res.json()
  else router.push('/orders')

  const s = await api(`/api/shipments?salesOrderId=${route.params.id}`)
  if (s.ok) shipments.value = await s.json()
  loading.value = false
}

async function updateStatus(status: string) {
  const res = await api(`/api/sales/orders/${route.params.id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { order.value = await res.json(); success(`Order ${status.toLowerCase()}`) }
}

async function deleteOrder() {
  const res = await api(`/api/sales/orders/${route.params.id}`, { method: 'DELETE' })
  if (res.ok) { success('Order deleted'); router.push('/orders') }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/orders')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Sales Orders</button>

    <Skeleton v-if="loading" />
    <template v-else-if="order">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white font-mono">{{ order.orderNumber }}</h2>
            <p class="text-sm text-gray-400 mt-1">Placed {{ order.orderDate }}</p>
            <p v-if="order.dueDate" class="text-sm text-gray-400">Due {{ order.dueDate }}</p>
          </div>
          <StatusBadge :status="order.status" />
        </div>
        <div class="grid grid-cols-2 gap-4 mt-6 pt-4 border-t border-gray-700">
          <div>
            <p class="text-xs text-gray-500 uppercase">Customer ID</p>
            <p class="text-sm text-white">{{ order.customerId }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 uppercase">Order Total</p>
            <p class="text-lg font-bold text-white">${{ order.total }}</p>
          </div>
        </div>

        <div class="flex gap-2 mt-4 pt-4 border-t border-gray-700">
          <button v-if="order.status === 'DRAFT'" @click="updateStatus('CONFIRMED')" class="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg transition-colors">Confirm Order</button>
          <button v-if="order.status === 'CONFIRMED'" @click="updateStatus('SHIPPED')" class="px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white text-sm rounded-lg transition-colors">Mark Shipped</button>
          <button v-if="order.status === 'SHIPPED'" @click="updateStatus('DELIVERED')" class="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded-lg transition-colors">Mark Delivered</button>
          <button v-if="order.status === 'DRAFT' || order.status === 'CONFIRMED'" @click="updateStatus('CANCELLED')" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Cancel</button>
          <button v-if="order.status === 'DRAFT'" @click="deleteOrder" class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-300 text-sm rounded-lg transition-colors ml-auto">Delete</button>
        </div>
      </div>

      <h3 class="text-lg font-semibold text-white mb-4">Line Items ({{ order.lineItems.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-700">
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Product</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">SKU</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Qty</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Unit Price</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Line Total</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="line in order.lineItems" :key="line.id" class="border-b border-gray-700/50">
              <td class="px-4 py-3 text-sm text-white font-medium">{{ line.productName }}</td>
              <td class="px-4 py-3 text-sm text-gray-400 font-mono">{{ line.productSku }}</td>
              <td class="px-4 py-3 text-sm text-white text-right">{{ line.quantity }}</td>
              <td class="px-4 py-3 text-sm text-gray-300 text-right">${{ line.unitPrice }}</td>
              <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ line.lineTotal }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="border-t border-gray-600">
              <td colspan="4" class="px-4 py-3 text-sm text-gray-400 text-right font-medium">Total</td>
              <td class="px-4 py-3 text-sm text-white font-bold text-right">${{ order.total }}</td>
            </tr>
          </tfoot>
        </table>
      </div>

      <h3 class="text-lg font-semibold text-white mb-4">Shipments ({{ shipments.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
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
              <td class="px-4 py-3 text-sm text-white font-mono">{{ s.shipmentNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ s.carrierName || '—' }}</td>
              <td class="px-4 py-3"><StatusBadge :status="s.status" /></td>
              <td class="px-4 py-3 text-sm">
                <a v-if="s.trackingUrl" :href="s.trackingUrl" target="_blank" @click.stop class="text-indigo-400 hover:text-indigo-300">{{ s.trackingNumber }}</a>
                <span v-else class="text-gray-500">{{ s.trackingNumber || '—' }}</span>
              </td>
            </tr>
            <tr v-if="!shipments.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No shipments</td></tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

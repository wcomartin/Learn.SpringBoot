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
const loading = ref(true)

async function fetchData() {
  const res = await api(`/api/purchasing/orders/${route.params.id}`)
  if (res.ok) order.value = await res.json()
  else router.push('/purchasing')
  loading.value = false
}

async function updateStatus(status: string) {
  const res = await api(`/api/purchasing/orders/${route.params.id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { order.value = await res.json(); success(`PO ${status.toLowerCase()}`) }
}

async function deleteOrder() {
  const res = await api(`/api/purchasing/orders/${route.params.id}`, { method: 'DELETE' })
  if (res.ok) { success('PO deleted'); router.push('/purchasing') }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/purchasing')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Purchase Orders</button>

    <Skeleton v-if="loading" />
    <template v-else-if="order">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white font-mono">{{ order.poNumber }}</h2>
            <p class="text-sm text-gray-400 mt-1">Ordered {{ order.orderDate }}</p>
            <p v-if="order.expectedDelivery" class="text-sm text-gray-400">Expected {{ order.expectedDelivery }}</p>
          </div>
          <StatusBadge :status="order.status" />
        </div>

        <!-- Actions -->
        <div class="flex gap-2 mt-4">
          <button v-if="order.status === 'DRAFT'" @click="updateStatus('SUBMITTED')" class="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg transition-colors">Submit to Supplier</button>
          <button v-if="order.status === 'SUBMITTED'" @click="updateStatus('ACKNOWLEDGED')" class="px-3 py-1.5 bg-cyan-600 hover:bg-cyan-700 text-white text-sm rounded-lg transition-colors">Mark Acknowledged</button>
          <button v-if="order.status === 'ACKNOWLEDGED'" @click="updateStatus('RECEIVED')" class="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded-lg transition-colors">Mark Received</button>
          <button v-if="order.status === 'DRAFT' || order.status === 'SUBMITTED'" @click="updateStatus('CANCELLED')" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Cancel</button>
          <button v-if="order.status === 'DRAFT'" @click="deleteOrder" class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-300 text-sm rounded-lg transition-colors ml-auto">Delete</button>
        </div>
        <div class="grid grid-cols-3 gap-4 mt-6 pt-4 border-t border-gray-700">
          <div>
            <p class="text-xs text-gray-500 uppercase">Supplier</p>
            <p class="text-sm text-white">{{ order.supplierName }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 uppercase">Warehouse</p>
            <p class="text-sm text-white">ID: {{ order.warehouseId }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 uppercase">Total</p>
            <p class="text-lg font-bold text-white">${{ order.total }}</p>
          </div>
        </div>
      </div>

      <h3 class="text-lg font-semibold text-white mb-4">Line Items ({{ order.lineItems.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="border-b border-gray-700">
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Product</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">SKU</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Ordered</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Received</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Unit Cost</th>
              <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Line Total</th>
              <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="line in order.lineItems" :key="line.id" class="border-b border-gray-700/50">
              <td class="px-4 py-3 text-sm text-white font-medium">{{ line.productName }}</td>
              <td class="px-4 py-3 text-sm text-gray-400 font-mono">{{ line.productSku }}</td>
              <td class="px-4 py-3 text-sm text-white text-right">{{ line.quantity }}</td>
              <td class="px-4 py-3 text-sm text-white text-right">{{ line.receivedQuantity }}</td>
              <td class="px-4 py-3 text-sm text-gray-300 text-right">${{ line.unitCost }}</td>
              <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ line.lineTotal }}</td>
              <td class="px-4 py-3">
                <span v-if="line.fullyReceived" class="px-2 py-0.5 text-xs font-medium rounded-full bg-green-900/50 text-green-400">Complete</span>
                <span v-else-if="line.receivedQuantity > 0" class="px-2 py-0.5 text-xs font-medium rounded-full bg-yellow-900/50 text-yellow-400">Partial</span>
                <span v-else class="px-2 py-0.5 text-xs font-medium rounded-full bg-gray-700 text-gray-400">Pending</span>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="border-t border-gray-600">
              <td colspan="5" class="px-4 py-3 text-sm text-gray-400 text-right font-medium">Total</td>
              <td class="px-4 py-3 text-sm text-white font-bold text-right">${{ order.total }}</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </template>
  </div>
</template>

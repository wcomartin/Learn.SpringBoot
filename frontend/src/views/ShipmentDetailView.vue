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
const shipment = ref<any>(null)
const loading = ref(true)

async function fetchData() {
  const res = await api(`/api/shipments/${route.params.id}`)
  if (res.ok) shipment.value = await res.json()
  else router.push('/shipments')
  loading.value = false
}

async function updateStatus(status: string) {
  const res = await api(`/api/shipments/${route.params.id}`, { method: 'PATCH', body: JSON.stringify({ status }) })
  if (res.ok) { shipment.value = await res.json(); success(`Shipment ${status.toLowerCase()}`) }
}

function formatDate(iso: string | null) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString()
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/shipments')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Shipments</button>

    <Skeleton v-if="loading" />
    <template v-else-if="shipment">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white font-mono">{{ shipment.shipmentNumber }}</h2>
          </div>
          <StatusBadge :status="shipment.status" />
        </div>

        <div class="grid grid-cols-2 gap-6 mt-6 pt-4 border-t border-gray-700">
          <div class="space-y-4">
            <div>
              <p class="text-xs text-gray-500 uppercase">Carrier</p>
              <p class="text-sm text-white">{{ shipment.carrierName || 'Not assigned' }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase">Tracking Number</p>
              <p class="text-sm">
                <a v-if="shipment.trackingUrl" :href="shipment.trackingUrl" target="_blank" class="text-indigo-400 hover:text-indigo-300">{{ shipment.trackingNumber }}</a>
                <span v-else class="text-white">{{ shipment.trackingNumber || '—' }}</span>
              </p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase">Sales Order</p>
              <router-link :to="`/orders/${shipment.salesOrderId}`" class="text-sm text-indigo-400 hover:text-indigo-300">View Order #{{ shipment.salesOrderId }}</router-link>
            </div>
          </div>
          <div class="space-y-4">
            <div>
              <p class="text-xs text-gray-500 uppercase">Estimated Delivery</p>
              <p class="text-sm text-white">{{ shipment.estimatedDelivery || '—' }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase">Shipped At</p>
              <p class="text-sm text-white">{{ formatDate(shipment.shippedAt) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase">Delivered At</p>
              <p class="text-sm text-white">{{ formatDate(shipment.deliveredAt) }}</p>
            </div>
          </div>
        </div>

        <!-- Status timeline -->
        <div class="mt-6 pt-4 border-t border-gray-700">
          <p class="text-xs text-gray-500 uppercase mb-3">Fulfillment Progress</p>
          <div class="flex items-center gap-1">
            <div v-for="step in ['PENDING', 'PICKED', 'PACKED', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED']" :key="step"
              :class="['flex-1 h-2 rounded-full', ['PENDING','PICKED','PACKED','SHIPPED','IN_TRANSIT','DELIVERED'].indexOf(shipment.status) >= ['PENDING','PICKED','PACKED','SHIPPED','IN_TRANSIT','DELIVERED'].indexOf(step) ? 'bg-indigo-500' : 'bg-gray-700']"
            />
          </div>
          <div class="flex justify-between mt-1">
            <span class="text-xs text-gray-500">Pending</span>
            <span class="text-xs text-gray-500">Delivered</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex gap-2 mt-6 pt-4 border-t border-gray-700">
          <button v-if="shipment.status === 'PENDING'" @click="updateStatus('PICKED')" class="px-3 py-1.5 bg-orange-600 hover:bg-orange-700 text-white text-sm rounded-lg transition-colors">Mark Picked</button>
          <button v-if="shipment.status === 'PICKED'" @click="updateStatus('PACKED')" class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm rounded-lg transition-colors">Mark Packed</button>
          <button v-if="shipment.status === 'PACKED'" @click="updateStatus('SHIPPED')" class="px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white text-sm rounded-lg transition-colors">Mark Shipped</button>
          <button v-if="shipment.status === 'SHIPPED' || shipment.status === 'IN_TRANSIT'" @click="updateStatus('DELIVERED')" class="px-3 py-1.5 bg-green-600 hover:bg-green-700 text-white text-sm rounded-lg transition-colors">Mark Delivered</button>
          <button v-if="shipment.status !== 'DELIVERED' && shipment.status !== 'RETURNED'" @click="updateStatus('RETURNED')" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Return</button>
        </div>
      </div>
    </template>
  </div>
</template>

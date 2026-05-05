<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '@/auth'
import StatCard from '@/components/ui/StatCard.vue'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const router = useRouter()
const loading = ref(true)
const stats = ref({ companies: 0, products: 0, openOrders: 0, pendingShipments: 0 })
const lowStock = ref<any[]>([])
const recentOrders = ref<any[]>([])
const pendingShipments = ref<any[]>([])
const openPOs = ref<any[]>([])

async function fetchData() {
  const [c, p, so, sh, ls, po] = await Promise.all([
    api('/api/companies'), api('/api/products'),
    api('/api/sales/orders'), api('/api/shipments'),
    api('/api/stock/low'), api('/api/purchasing/orders')
  ])

  const companies = c.ok ? await c.json() : []
  const products = p.ok ? await p.json() : []
  const orders = so.ok ? await so.json() : []
  const shipments = sh.ok ? await sh.json() : []
  lowStock.value = ls.ok ? await ls.json() : []
  const purchaseOrders = po.ok ? await po.json() : []

  stats.value = {
    companies: companies.length,
    products: products.length,
    openOrders: orders.filter((o: any) => o.status !== 'DELIVERED' && o.status !== 'CANCELLED').length,
    pendingShipments: shipments.filter((s: any) => s.status !== 'DELIVERED').length
  }

  recentOrders.value = orders.slice(0, 8)
  pendingShipments.value = shipments.filter((s: any) => s.status !== 'DELIVERED').slice(0, 8)
  openPOs.value = purchaseOrders.filter((po: any) => po.status !== 'RECEIVED' && po.status !== 'CANCELLED').slice(0, 8)

  loading.value = false
}

onMounted(fetchData)
</script>

<template>
  <div class="h-[calc(100vh-4rem)] flex flex-col">
    <h2 class="text-2xl font-bold text-white mb-4 shrink-0">Dashboard</h2>

    <Skeleton v-if="loading" :rows="8" />
    <template v-else>
      <!-- Stats -->
      <div class="grid grid-cols-4 gap-4 mb-4 shrink-0">
        <StatCard label="Active Companies" :value="stats.companies" icon="🏢" />
        <StatCard label="Products" :value="stats.products" icon="📦" />
        <StatCard label="Open Orders" :value="stats.openOrders" icon="🛒" />
        <StatCard label="Pending Shipments" :value="stats.pendingShipments" icon="🚚" />
      </div>

      <!-- Grid panels -->
      <div class="grid grid-cols-2 grid-rows-2 gap-4 flex-1 min-h-0">
        <!-- Low Stock Alerts -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 p-4 flex flex-col min-h-0">
          <div class="flex items-center justify-between mb-3 shrink-0">
            <h3 class="text-sm font-semibold text-white uppercase tracking-wide">⚠️ Low Stock</h3>
            <span class="text-xs text-red-400 font-medium">{{ lowStock.length }} items</span>
          </div>
          <div class="flex-1 overflow-y-auto space-y-2 min-h-0">
            <div v-for="s in lowStock" :key="s.id" @click="router.push(`/products/${s.productId}`)"
              class="flex items-center justify-between p-2.5 bg-gray-900/50 rounded-lg cursor-pointer hover:bg-gray-700/50 transition-colors">
              <div class="min-w-0">
                <p class="text-sm text-white font-medium truncate">{{ s.productName }}</p>
                <p class="text-xs text-gray-400 truncate">{{ s.warehouseName }}</p>
              </div>
              <div class="text-right shrink-0 ml-2">
                <p class="text-sm font-bold text-red-400">{{ s.quantity }}</p>
                <p class="text-xs text-gray-500">/ {{ s.reorderPoint }}</p>
              </div>
            </div>
            <p v-if="!lowStock.length" class="text-sm text-gray-500">All stock levels OK</p>
          </div>
        </div>

        <!-- Pending Shipments -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 p-4 flex flex-col min-h-0">
          <div class="flex items-center justify-between mb-3 shrink-0">
            <h3 class="text-sm font-semibold text-white uppercase tracking-wide">🚚 Shipments to Fulfill</h3>
            <router-link to="/shipments" class="text-xs text-indigo-400 hover:text-indigo-300">View all</router-link>
          </div>
          <div class="flex-1 overflow-y-auto space-y-2 min-h-0">
            <div v-for="s in pendingShipments" :key="s.id" @click="router.push(`/shipments/${s.id}`)"
              class="flex items-center justify-between p-2.5 bg-gray-900/50 rounded-lg cursor-pointer hover:bg-gray-700/50 transition-colors">
              <div class="min-w-0">
                <p class="text-sm text-white font-mono font-medium">{{ s.shipmentNumber }}</p>
                <p class="text-xs text-gray-400">{{ s.carrierName || 'No carrier' }}</p>
              </div>
              <StatusBadge :status="s.status" />
            </div>
            <p v-if="!pendingShipments.length" class="text-sm text-gray-500">All shipped</p>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 p-4 flex flex-col min-h-0">
          <div class="flex items-center justify-between mb-3 shrink-0">
            <h3 class="text-sm font-semibold text-white uppercase tracking-wide">🛒 Recent Orders</h3>
            <router-link to="/orders" class="text-xs text-indigo-400 hover:text-indigo-300">View all</router-link>
          </div>
          <div class="flex-1 overflow-y-auto space-y-2 min-h-0">
            <div v-for="o in recentOrders" :key="o.id" @click="router.push(`/orders/${o.id}`)"
              class="flex items-center justify-between p-2.5 bg-gray-900/50 rounded-lg cursor-pointer hover:bg-gray-700/50 transition-colors">
              <div class="min-w-0">
                <p class="text-sm text-white font-mono font-medium">{{ o.orderNumber }}</p>
                <p class="text-xs text-gray-400">{{ o.orderDate }}</p>
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <span class="text-sm text-white font-medium">${{ o.total }}</span>
                <StatusBadge :status="o.status" />
              </div>
            </div>
            <p v-if="!recentOrders.length" class="text-sm text-gray-500">No orders</p>
          </div>
        </div>

        <!-- Open POs -->
        <div class="bg-gray-800 rounded-xl border border-gray-700 p-4 flex flex-col min-h-0">
          <div class="flex items-center justify-between mb-3 shrink-0">
            <h3 class="text-sm font-semibold text-white uppercase tracking-wide">📋 Open Purchase Orders</h3>
            <router-link to="/purchasing" class="text-xs text-indigo-400 hover:text-indigo-300">View all</router-link>
          </div>
          <div class="flex-1 overflow-y-auto space-y-2 min-h-0">
            <div v-for="po in openPOs" :key="po.id" @click="router.push(`/purchasing/${po.id}`)"
              class="flex items-center justify-between p-2.5 bg-gray-900/50 rounded-lg cursor-pointer hover:bg-gray-700/50 transition-colors">
              <div class="min-w-0">
                <p class="text-sm text-white font-mono font-medium">{{ po.poNumber }}</p>
                <p class="text-xs text-gray-400 truncate">{{ po.supplierName }}</p>
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <span class="text-sm text-white font-medium">${{ po.total }}</span>
                <StatusBadge :status="po.status" />
              </div>
            </div>
            <p v-if="!openPOs.length" class="text-sm text-gray-500">No open POs</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '@/auth'
import StatCard from '@/components/ui/StatCard.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const stats = ref({ companies: 0, contacts: 0, products: 0, orders: 0, purchaseOrders: 0, shipments: 0, lowStock: 0 })
const loading = ref(true)

async function fetchStats() {
  const [c, ct, p, so, po, sh, ls] = await Promise.all([
    api('/api/companies'), api('/api/contacts'), api('/api/products'),
    api('/api/sales/orders'), api('/api/purchasing/orders'), api('/api/shipments'),
    api('/api/stock/low')
  ])
  stats.value = {
    companies: c.ok ? (await c.json()).length : 0,
    contacts: ct.ok ? (await ct.json()).length : 0,
    products: p.ok ? (await p.json()).length : 0,
    orders: so.ok ? (await so.json()).length : 0,
    purchaseOrders: po.ok ? (await po.json()).length : 0,
    shipments: sh.ok ? (await sh.json()).length : 0,
    lowStock: ls.ok ? (await ls.json()).length : 0,
  }
  loading.value = false
}

onMounted(fetchStats)
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-white mb-6">Dashboard</h2>
    <Skeleton v-if="loading" :rows="3" />
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <StatCard label="Companies" :value="stats.companies" icon="🏢" />
      <StatCard label="Contacts" :value="stats.contacts" icon="👤" />
      <StatCard label="Products" :value="stats.products" icon="📦" />
      <StatCard label="Sales Orders" :value="stats.orders" icon="🛒" />
      <StatCard label="Purchase Orders" :value="stats.purchaseOrders" icon="📋" />
      <StatCard label="Shipments" :value="stats.shipments" icon="🚚" />
      <StatCard label="Low Stock Alerts" :value="stats.lowStock" icon="⚠️" />
    </div>
  </div>
</template>

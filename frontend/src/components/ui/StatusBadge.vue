<script setup lang="ts">
defineProps<{ status: string }>()

const colors: Record<string, string> = {
  ACTIVE: 'bg-green-900/50 text-green-400',
  LEAD: 'bg-yellow-900/50 text-yellow-400',
  INACTIVE: 'bg-gray-700 text-gray-400',
  DRAFT: 'bg-gray-700 text-gray-400',
  CONFIRMED: 'bg-blue-900/50 text-blue-400',
  SUBMITTED: 'bg-blue-900/50 text-blue-400',
  ACKNOWLEDGED: 'bg-cyan-900/50 text-cyan-400',
  SHIPPED: 'bg-purple-900/50 text-purple-400',
  IN_TRANSIT: 'bg-purple-900/50 text-purple-400',
  DELIVERED: 'bg-green-900/50 text-green-400',
  RECEIVED: 'bg-green-900/50 text-green-400',
  CANCELLED: 'bg-red-900/50 text-red-400',
  PENDING: 'bg-yellow-900/50 text-yellow-400',
  PICKED: 'bg-orange-900/50 text-orange-400',
  PACKED: 'bg-indigo-900/50 text-indigo-400',
  DISCONTINUED: 'bg-red-900/50 text-red-400',
  OUT_OF_STOCK: 'bg-red-900/50 text-red-400',
}
</script>

<template>
  <span :class="['px-2 py-0.5 text-xs font-medium rounded-full', colors[status] || 'bg-gray-700 text-gray-400']">
    {{ status }}
  </span>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { success, error: showError } = useToast()
const { data: products, loading, fetch, create } = useApi<any>('/api/products')

const showForm = ref(false)
const form = ref({ sku: '', name: '', unitPrice: '', costPrice: '', reorderPoint: '0' })

async function handleCreate() {
  const ok = await create({ ...form.value, unitPrice: parseFloat(form.value.unitPrice), costPrice: parseFloat(form.value.costPrice), reorderPoint: parseInt(form.value.reorderPoint) })
  if (ok) {
    showForm.value = false
    form.value = { sku: '', name: '', unitPrice: '', costPrice: '', reorderPoint: '0' }
    success('Product created')
  } else { showError('Failed to create product') }
}

onMounted(() => fetch())
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold text-white">Products</h2>
      <button @click="showForm = true" class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg transition-colors">+ New Product</button>
    </div>
    <Skeleton v-if="loading" />
    <div v-else class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">SKU</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Name</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Price</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Cost</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Reorder Pt</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id" @click="$router.push(`/products/${p.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
            <td class="px-4 py-3 text-sm text-gray-400 font-mono">{{ p.sku }}</td>
            <td class="px-4 py-3 text-sm text-white font-medium">{{ p.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-300 text-right">${{ p.unitPrice }}</td>
            <td class="px-4 py-3 text-sm text-gray-300 text-right">${{ p.costPrice }}</td>
            <td class="px-4 py-3 text-sm text-gray-300 text-right">{{ p.reorderPoint }}</td>
            <td class="px-4 py-3"><StatusBadge :status="p.status" /></td>
          </tr>
          <tr v-if="!products.length"><td colspan="6" class="px-4 py-8 text-center text-gray-500">No products yet</td></tr>
        </tbody>
      </table>
    </div>

    <SlideOver :open="showForm" title="New Product" @close="showForm = false">
      <form @submit.prevent="handleCreate" class="space-y-4">
        <div><label class="block text-sm text-gray-400 mb-1">SKU *</label><input v-model="form.sku" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Name *</label><input v-model="form.name" required class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Unit Price</label><input v-model="form.unitPrice" type="number" step="0.01" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Cost Price</label><input v-model="form.costPrice" type="number" step="0.01" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <div><label class="block text-sm text-gray-400 mb-1">Reorder Point</label><input v-model="form.reorderPoint" type="number" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Create Product</button>
      </form>
    </SlideOver>
  </div>
</template>

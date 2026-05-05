<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/auth'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import SlideOver from '@/components/ui/SlideOver.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const route = useRoute()
const router = useRouter()
const { success } = useToast()
const product = ref<any>(null)
const stock = ref<any[]>([])
const salesOrders = ref<any[]>([])
const purchaseOrders = ref<any[]>([])
const shipments = ref<any[]>([])
const loading = ref(true)
const showEdit = ref(false)
const editForm = ref({ name: '', sku: '', unitPrice: '', costPrice: '', reorderPoint: '', status: '' })

async function fetchData() {
  const id = route.params.id
  const [p, s, so, po] = await Promise.all([
    api(`/api/products/${id}`),
    api(`/api/stock?productId=${id}`),
    api(`/api/sales/orders?productId=${id}`),
    api(`/api/purchasing/orders?productId=${id}`)
  ])
  if (p.ok) product.value = await p.json()
  else router.push('/products')
  if (s.ok) stock.value = await s.json()
  if (so.ok) {
    salesOrders.value = await so.json()
    const orderIds = salesOrders.value.map((o: any) => o.id)
    const shipResults = await Promise.all(orderIds.map((oid: number) => api(`/api/shipments?salesOrderId=${oid}`)))
    const all: any[] = []
    for (const res of shipResults) { if (res.ok) all.push(...await res.json()) }
    shipments.value = all
  }
  if (po.ok) purchaseOrders.value = await po.json()
  loading.value = false
}

function openEdit() {
  if (!product.value) return
  editForm.value = { name: product.value.name, sku: product.value.sku, unitPrice: product.value.unitPrice, costPrice: product.value.costPrice, reorderPoint: product.value.reorderPoint, status: product.value.status }
  showEdit.value = true
}

async function saveEdit() {
  const body = { ...editForm.value, unitPrice: parseFloat(editForm.value.unitPrice), costPrice: parseFloat(editForm.value.costPrice), reorderPoint: parseInt(editForm.value.reorderPoint) }
  const res = await api(`/api/products/${route.params.id}`, { method: 'PATCH', body: JSON.stringify(body) })
  if (res.ok) { product.value = await res.json(); showEdit.value = false; success('Product updated') }
}

async function deleteProduct() {
  const res = await api(`/api/products/${route.params.id}`, { method: 'DELETE' })
  if (res.ok) { success('Product deleted'); router.push('/products') }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <button @click="router.push('/products')" class="text-sm text-gray-400 hover:text-white mb-4 inline-block">&larr; Back to Products</button>

    <Skeleton v-if="loading" />
    <template v-else-if="product">
      <div class="bg-gray-800 rounded-xl border border-gray-700 p-6 mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-2xl font-bold text-white">{{ product.name }}</h2>
            <p class="text-sm text-gray-400 font-mono mt-1">{{ product.sku }}</p>
            <p v-if="product.description" class="text-sm text-gray-300 mt-3">{{ product.description }}</p>
          </div>
          <StatusBadge :status="product.status" />
        </div>
        <div class="grid grid-cols-3 gap-4 mt-6 pt-4 border-t border-gray-700">
          <div>
            <p class="text-xs text-gray-500 uppercase">Unit Price</p>
            <p class="text-lg font-semibold text-white">${{ product.unitPrice }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 uppercase">Cost Price</p>
            <p class="text-lg font-semibold text-white">${{ product.costPrice }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-500 uppercase">Reorder Point</p>
            <p class="text-lg font-semibold text-white">{{ product.reorderPoint }}</p>
          </div>
        </div>
        <div class="flex gap-2 mt-4">
          <button @click="openEdit" class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm rounded-lg transition-colors">Edit</button>
          <button @click="deleteProduct" class="px-3 py-1.5 bg-red-600/20 hover:bg-red-600/30 text-red-400 text-sm rounded-lg transition-colors">Delete</button>
        </div>
      </div>

      <!-- Stock Levels -->
      <h3 class="text-lg font-semibold text-white mb-4">Stock Levels</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead><tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Warehouse</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Quantity</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
          </tr></thead>
          <tbody>
            <tr v-for="s in stock" :key="s.id" class="border-b border-gray-700/50">
              <td class="px-4 py-3 text-sm text-white">{{ s.warehouseName }}</td>
              <td class="px-4 py-3 text-sm text-white text-right font-medium">{{ s.quantity }}</td>
              <td class="px-4 py-3">
                <span v-if="s.lowStock" class="px-2 py-0.5 text-xs font-medium rounded-full bg-red-900/50 text-red-400">Low Stock</span>
                <span v-else class="px-2 py-0.5 text-xs font-medium rounded-full bg-green-900/50 text-green-400">OK</span>
              </td>
            </tr>
            <tr v-if="!stock.length"><td colspan="3" class="px-4 py-8 text-center text-gray-500">No stock records</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Sales Orders -->
      <h3 class="text-lg font-semibold text-white mb-4">Sales Orders ({{ salesOrders.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead><tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Order #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Date</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Total</th>
          </tr></thead>
          <tbody>
            <tr v-for="o in salesOrders" :key="o.id" @click="router.push(`/orders/${o.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ o.orderNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ o.orderDate }}</td>
              <td class="px-4 py-3"><StatusBadge :status="o.status" /></td>
              <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ o.total }}</td>
            </tr>
            <tr v-if="!salesOrders.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No sales orders</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Purchase Orders -->
      <h3 class="text-lg font-semibold text-white mb-4">Purchase Orders ({{ purchaseOrders.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead><tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">PO #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Supplier</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-right px-4 py-3 text-xs font-medium text-gray-400 uppercase">Total</th>
          </tr></thead>
          <tbody>
            <tr v-for="po in purchaseOrders" :key="po.id" @click="router.push(`/purchasing/${po.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ po.poNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ po.supplierName }}</td>
              <td class="px-4 py-3"><StatusBadge :status="po.status" /></td>
              <td class="px-4 py-3 text-sm text-white font-medium text-right">${{ po.total }}</td>
            </tr>
            <tr v-if="!purchaseOrders.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No purchase orders</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Shipments (for orders containing this product) -->
      <h3 class="text-lg font-semibold text-white mb-4">Related Shipments ({{ shipments.length }})</h3>
      <div class="bg-gray-800 rounded-xl border border-gray-700 overflow-hidden mb-6">
        <table class="w-full">
          <thead><tr class="border-b border-gray-700">
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Shipment #</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Carrier</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Status</th>
            <th class="text-left px-4 py-3 text-xs font-medium text-gray-400 uppercase">Tracking</th>
          </tr></thead>
          <tbody>
            <tr v-for="s in shipments" :key="s.id" @click="router.push(`/shipments/${s.id}`)" class="border-b border-gray-700/50 hover:bg-gray-700/30 cursor-pointer">
              <td class="px-4 py-3 text-sm text-white font-mono font-medium">{{ s.shipmentNumber }}</td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ s.carrierName || '—' }}</td>
              <td class="px-4 py-3"><StatusBadge :status="s.status" /></td>
              <td class="px-4 py-3 text-sm text-gray-300">{{ s.trackingNumber || '—' }}</td>
            </tr>
            <tr v-if="!shipments.length"><td colspan="4" class="px-4 py-8 text-center text-gray-500">No shipments</td></tr>
          </tbody>
        </table>
      </div>

      <SlideOver :open="showEdit" title="Edit Product" @close="showEdit = false">
        <form @submit.prevent="saveEdit" class="space-y-4">
          <div><label class="block text-sm text-gray-400 mb-1">SKU</label><input v-model="editForm.sku" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Name</label><input v-model="editForm.name" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Unit Price</label><input v-model="editForm.unitPrice" type="number" step="0.01" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Cost Price</label><input v-model="editForm.costPrice" type="number" step="0.01" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Reorder Point</label><input v-model="editForm.reorderPoint" type="number" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500" /></div>
          <div><label class="block text-sm text-gray-400 mb-1">Status</label>
            <select v-model="editForm.status" class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-indigo-500">
              <option value="ACTIVE">Active</option><option value="DISCONTINUED">Discontinued</option><option value="OUT_OF_STOCK">Out of Stock</option>
            </select>
          </div>
          <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">Save</button>
        </form>
      </SlideOver>
    </template>
  </div>
</template>

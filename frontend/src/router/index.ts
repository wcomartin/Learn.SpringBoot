import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/views/AppLayout.vue'),
      meta: { auth: true },
      children: [
        { path: '', name: 'dashboard', component: () => import('@/views/DashboardView.vue') },
        { path: 'companies', name: 'companies', component: () => import('@/views/CompaniesView.vue') },
        { path: 'companies/:id', name: 'company', component: () => import('@/views/CompanyDetailView.vue') },
        { path: 'contacts', name: 'contacts', component: () => import('@/views/ContactsView.vue') },
        { path: 'contacts/:id', name: 'contact', component: () => import('@/views/ContactDetailView.vue') },
        { path: 'products', name: 'products', component: () => import('@/views/ProductsView.vue') },
        { path: 'products/:id', name: 'product', component: () => import('@/views/ProductDetailView.vue') },
        { path: 'orders', name: 'orders', component: () => import('@/views/SalesOrdersView.vue') },
        { path: 'orders/:id', name: 'order', component: () => import('@/views/SalesOrderDetailView.vue') },
        { path: 'purchasing', name: 'purchasing', component: () => import('@/views/PurchaseOrdersView.vue') },
        { path: 'purchasing/:id', name: 'purchaseOrder', component: () => import('@/views/PurchaseOrderDetailView.vue') },
        { path: 'shipments', name: 'shipments', component: () => import('@/views/ShipmentsView.vue') },
        { path: 'shipments/:id', name: 'shipment', component: () => import('@/views/ShipmentDetailView.vue') },
        { path: 'admin', name: 'admin', component: () => import('@/views/AdminView.vue') },
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.meta.auth && !isAuthenticated()) return { name: 'login' }
  if (to.meta.guest && isAuthenticated()) return { name: 'dashboard' }
})

export default router

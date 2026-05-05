import { ref } from 'vue'

export interface ToastItem { id: number; message: string; type: 'success' | 'error' }

const toasts = ref<ToastItem[]>([])
let nextId = 0

export function useToast() {
  function show(message: string, type: 'success' | 'error' = 'success') {
    const id = nextId++
    toasts.value.push({ id, message, type })
    setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 3000)
  }

  function success(message: string) { show(message, 'success') }
  function error(message: string) { show(message, 'error') }

  return { toasts, success, error }
}

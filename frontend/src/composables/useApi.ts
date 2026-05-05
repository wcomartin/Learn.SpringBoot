import { ref } from 'vue'
import { api } from '@/auth'

export function useApi<T>(url: string) {
  const data = ref<T[]>([])
  const loading = ref(false)

  async function fetch(params?: Record<string, string>) {
    loading.value = true
    const query = params ? '?' + new URLSearchParams(params).toString() : ''
    const res = await api(url + query)
    if (res.ok) data.value = await res.json()
    loading.value = false
  }

  async function create(body: any): Promise<boolean> {
    const res = await api(url, { method: 'POST', body: JSON.stringify(body) })
    if (res.ok) await fetch()
    return res.ok
  }

  async function update(id: number, body: any): Promise<boolean> {
    const res = await api(`${url}/${id}`, { method: 'PATCH', body: JSON.stringify(body) })
    if (res.ok) await fetch()
    return res.ok
  }

  async function remove(id: number): Promise<boolean> {
    const res = await api(`${url}/${id}`, { method: 'DELETE' })
    if (res.ok) await fetch()
    return res.ok
  }

  return { data, loading, fetch, create, update, remove }
}

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { setAuth } from '@/auth'

const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const email = ref('')
const password = ref('')
const name = ref('')
const error = ref('')

async function submit() {
  error.value = ''
  const endpoint = mode.value === 'login' ? '/api/auth/login' : '/api/auth/register'
  const body: Record<string, string> = { email: email.value, password: password.value }
  if (mode.value === 'register') body.name = name.value

  const res = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })

  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    error.value = data.message || 'Authentication failed'
    return
  }

  const { token, user } = await res.json()
  setAuth(token, user)
  router.push({ name: 'dashboard' })
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex items-center justify-center px-4">
    <div class="w-full max-w-sm">
      <h1 class="text-2xl font-bold text-white text-center mb-8">
        {{ mode === 'login' ? 'Sign In' : 'Create Account' }}
      </h1>
      <form @submit.prevent="submit" class="space-y-4">
        <input v-if="mode === 'register'" v-model="name" placeholder="Name"
          class="w-full px-4 py-2.5 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500" />
        <input v-model="email" type="email" placeholder="Email" required
          class="w-full px-4 py-2.5 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500" />
        <input v-model="password" type="password" placeholder="Password" required
          class="w-full px-4 py-2.5 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:border-indigo-500" />
        <button type="submit" class="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-colors">
          {{ mode === 'login' ? 'Sign In' : 'Create Account' }}
        </button>
      </form>
      <p v-if="error" class="mt-3 text-sm text-red-400 text-center">{{ error }}</p>
      <p class="mt-6 text-center text-sm text-gray-400">
        <a href="#" @click.prevent="mode = mode === 'login' ? 'register' : 'login'" class="text-indigo-400 hover:text-indigo-300">
          {{ mode === 'login' ? 'Need an account? Register' : 'Already have an account? Sign in' }}
        </a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ label: string; value: string | number; icon: string; color?: string }>()
</script>

<template>
  <div class="bg-gray-800 rounded-xl border border-gray-700 p-5">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-sm text-gray-400">{{ label }}</p>
        <p class="text-2xl font-bold text-white mt-1">{{ value }}</p>
      </div>
      <span class="text-3xl">{{ icon }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ open: boolean; title: string }>()
defineEmits<{ close: [] }>()
</script>

<template>
  <teleport to="body">
    <transition name="panel">
      <div v-if="open" class="fixed inset-0 z-40 flex justify-end">
        <div class="absolute inset-0 bg-black/50" @click="$emit('close')" />
        <div class="relative w-full max-w-md bg-gray-800 border-l border-gray-700 shadow-xl flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-700">
            <h3 class="text-lg font-semibold text-white">{{ title }}</h3>
            <button @click="$emit('close')" class="text-gray-400 hover:text-white text-xl">&times;</button>
          </div>
          <div class="flex-1 overflow-y-auto p-6">
            <slot />
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
.panel-enter-active, .panel-leave-active { transition: all 0.3s ease; }
.panel-enter-from, .panel-leave-to { opacity: 0; }
.panel-enter-from > div:last-child, .panel-leave-to > div:last-child { transform: translateX(100%); }
</style>

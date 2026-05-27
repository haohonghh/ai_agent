<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style>
:root {
  --color-primary: #4f46e5;
  --color-primary-light: #818cf8;
  --color-primary-dark: #3730a3;
  --color-surface: #fafaf9;
  --color-surface-elevated: #ffffff;
  --color-border: #e7e5e4;
  --color-text-primary: #1c1917;
  --color-text-secondary: #78716c;
  --color-text-muted: #a8a29e;
  --color-success: #22c55e;
  --color-warning: #f59e0b;
  --color-danger: #ef4444;
  --color-info: #3b82f6;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.08);
  --shadow-lg: 0 12px 32px rgba(0,0,0,0.12);
  --font-display: 'DM Serif Display', Georgia, serif;
  --font-body: 'DM Sans', system-ui, sans-serif;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  background: var(--color-surface);
  color: var(--color-text-primary);
  font-family: var(--font-body);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

::selection {
  background: var(--color-primary-light);
  color: white;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted);
}
</style>
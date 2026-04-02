import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backend = process.env.VITE_PROXY_TARGET || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/accounts': { target: backend, changeOrigin: true },
      '/items': { target: backend, changeOrigin: true },
      '/images': { target: backend, changeOrigin: true },
      '/carts': { target: backend, changeOrigin: true },
      '/orders': { target: backend, changeOrigin: true },
      '/inventory': { target: backend, changeOrigin: true },
    },
  },
})

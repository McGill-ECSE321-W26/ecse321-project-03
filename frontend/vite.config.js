import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// backend URL
// in production this would be a deployed URL set in the environment variables
// in our case we use localhost:8080
const backend = process.env.VITE_PROXY_TARGET || 'http://localhost:8080'

// documentation: https://vite.dev/config/
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

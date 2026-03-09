import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/token': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/oauth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/error': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/styles': { target: 'http://localhost:8081', changeOrigin: true },
      '/data': { target: 'http://localhost:8081', changeOrigin: true },
      '/fonts': { target: 'http://localhost:8081', changeOrigin: true },
    }
  }
})

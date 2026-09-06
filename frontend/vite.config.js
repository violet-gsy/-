import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8001',
        changeOrigin: true,
        // 活动图控制器本身带 /api 前缀，其他仿真接口没有此前缀。
        rewrite: path => path.startsWith('/api/activity-diagram')
          ? path
          : path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
})

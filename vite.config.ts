import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'https://www.reservauto.net',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // Convert X-WCF-Cookie header to actual Cookie header
            const wcfCookie = req.headers['x-wcf-cookie'];
            if (wcfCookie) {
              proxyReq.setHeader('Cookie', wcfCookie as string);
              proxyReq.removeHeader('x-wcf-cookie');
            }
          });
        },
      }
    }
  }
})

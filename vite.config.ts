import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  base: '/communauto-tools/',
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
            let wcfCookie = req.headers['x-wcf-cookie'];
            if (wcfCookie) {
              // Normalize: handle string arrays, strip leading "cookie:" prefix from DevTools paste
              const raw = Array.isArray(wcfCookie) ? wcfCookie.join('; ') : wcfCookie;
              const cleaned = raw.replace(/^cookie:\s*/i, '').trim();
              if (cleaned) {
                proxyReq.setHeader('Cookie', cleaned);
              }
              proxyReq.removeHeader('x-wcf-cookie');
            }
          });
        },
      }
    }
  }
})

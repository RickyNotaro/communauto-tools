/// <reference types="vite/client" />

interface ImportMetaEnv {
  // no custom env vars needed — production uses corsproxy.io
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

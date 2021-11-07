import { createApp } from 'vue';
import axios from 'axios';
import App from './App.vue';
import router from './router';

createApp(App).use(router, axios).mount('#app');

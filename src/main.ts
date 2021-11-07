import { createApp } from 'vue';
import axios from 'axios';
import App from './App.vue';
import router from './router';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

createApp(App).use(router, axios).mount('#app');

import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
    },
    {
      path: '/vehicules',
      name: 'VehiculeList',
      component: () => import('../views/VehiculeList.vue'),
    },
    {
      path: '/radar',
      name: 'Radar',
      component: () => import('../views/Radar.vue'),
    },
  ],
});

export default router;

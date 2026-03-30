import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router';
import { Capacitor } from '@capacitor/core';
import Home from '../views/Home.vue';

const router = createRouter({
  history: Capacitor.isNativePlatform()
    ? createWebHashHistory()
    : createWebHistory(import.meta.env.BASE_URL),
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
    {
      path: '/signin-callback',
      name: 'SigninCallback',
      component: () => import('../views/SigninCallback.vue'),
    },
    {
      path: '/native-login',
      name: 'NativeLogin',
      component: () => import('../views/NativeLogin.vue'),
    },
  ],
});

export default router;

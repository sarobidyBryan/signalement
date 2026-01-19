import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';
import HomePage from '../views/HomePage.vue';
import LoginVue from '@/views/pages/auth/LoginVue.vue'; // Import statique
import RegisterVue from '@/views/pages/auth/RegisterVue.vue';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: HomePage // ✅ Affiche HomePage
  },
  {
    path: '/login',  
    name: 'Login',
    component: LoginVue 
  },
  {
    path: '/register',  
    name: 'Register',
    component: RegisterVue 
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

export default router;
import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';
import HomePage from '../views/pages/HomePage.vue';
import LoginVue from '@/views/pages/auth/LoginVue.vue'; // Import statique
import RegisterVue from '@/views/pages/auth/RegisterVue.vue';
import { auth } from '@/config/firebase';
import { onAuthStateChanged } from 'firebase/auth';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/'
  },
  {
    path: '',
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
  {
    path: '/list',  
    name: 'List',
    component:() => import('@/views/pages/signalements/ReportsList.vue')  
  },
  {
    path: '/menu',  
    name: 'Menu',
    component: () => import('@/views/pages/MenuPage.vue')
  },
  {
    path: '/map',
    name: 'Carte',  
        component: () => import('@/views/pages/signalements/ReportsOverview.vue')
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// Helper to wait for Firebase auth to initialize and return current user
const getCurrentUser = (): Promise<any> => {
  return new Promise((resolve, reject) => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      unsubscribe();
      resolve(user);
    }, reject);
  });
};

// Navigation guard: redirect to /login when user is not authenticated
router.beforeEach(async (to, from, next) => {
  const publicPaths = ['/login', '/register','/'];

  // Allow public routes without check
  if (publicPaths.includes(to.path)) {
    const user = await getCurrentUser();
    // If logged in and navigating to login, redirect to home
    if (user && to.path === '/login') return next('/map');
    return next();
  }

  // For all other routes, require auth
  const user = await getCurrentUser();
  if (!user) return next('/login');
  return next();
});

export default router;
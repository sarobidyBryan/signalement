<template>
  <ion-app>
    <ion-router-outlet />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet } from '@ionic/vue';
import { onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
let sessionCheckInterval: number | null = null;

onMounted(() => {
  console.log('[App.vue] Démarrage vérification session');
  
  // Vérifier l'expiration de la session toutes les secondes
  sessionCheckInterval = window.setInterval(() => {
    try {
      const expiryStr = localStorage.getItem('sessionExpiry');
      
      // Si pas de session, continuer à vérifier (pour détecter quand une session est créée)
      if (!expiryStr) {
        return;
      }
      
      const expiry = Number(expiryStr);
      const now = Date.now();
      
      console.log('[App.vue] Check:', {
        expiry: new Date(expiry).toLocaleString(),
        now: new Date(now).toLocaleString(),
        expiré: now >= expiry,
        path: router.currentRoute.value.path
      });
      
      if (now >= expiry) {
        console.log('[App.vue] 🔴 Session expirée! Redirection...');
        
        // Session expirée - nettoyer et rediriger
        localStorage.removeItem('uid');
        localStorage.removeItem('user');
        localStorage.removeItem('sessionExpiry');
        localStorage.removeItem('sessionDuration');
        
        // Rediriger vers login si pas déjà sur login
        if (router.currentRoute.value.path !== '/login') {
          console.log('[App.vue] Redirection vers /login');
          
          // Essayer router.replace d'abord
          router.replace({ path: '/login', query: { reason: 'session_expired' } })
            .catch(err => {
              console.warn('[App.vue] router.replace échoué, utilisation de window.location', err);
              // Fallback: redirection forcée
              window.location.href = '/login?reason=session_expired';
            });
        }
      }
    } catch (e) {
      console.warn('Erreur lors de la vérification de session:', e);
    }
  }, 1000); // Vérifier toutes les secondes
});

onUnmounted(() => {
  if (sessionCheckInterval) {
    clearInterval(sessionCheckInterval);
  }
});
</script>

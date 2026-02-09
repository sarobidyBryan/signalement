<template>
  <ion-app>
    <ion-router-outlet />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet } from '@ionic/vue';
import { onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { auth } from '@/config/firebase';
import { signOut } from 'firebase/auth';

const router = useRouter();
let sessionCheckInterval: number | null = null;

function startSessionCheck() {
  if (sessionCheckInterval) return; // déjà démarré
  console.log('[App.vue] Démarrage vérification session');
  sessionCheckInterval = window.setInterval(() => {
    try {
      const expiryStr = localStorage.getItem('sessionExpiry');
      if (!expiryStr) {
        return;
      }

      const expiry = Number(expiryStr);
      const now = Date.now();

      // console.log('[App.vue] Check:', {
      //   expiry: new Date(expiry).toLocaleString(),
      //   now: new Date(now).toLocaleString(),
      //   expiré: now >= expiry,
      //   path: router.currentRoute.value.path
      // });

      if (now >= expiry) {
        console.log('[App.vue] 🔴 Session expirée! Déconnexion et redirection...');

        // Tenter une déconnexion Firebase centrale
        try {
          signOut(auth).catch((e) => console.warn('signOut failed:', e));
        } catch (e) {
          console.warn('Erreur signOut:', e);
        }

        // Session expirée - nettoyer le localStorage
        localStorage.removeItem('uid');
        localStorage.removeItem('user');
        localStorage.removeItem('sessionExpiry');
        localStorage.removeItem('sessionDuration');

        // Stopper la vérification
        stopSessionCheck();

        // Rediriger vers login si pas déjà sur login
        if (router.currentRoute.value.path !== '/login') {
          console.log('[App.vue] Redirection vers /login');
          router.replace({ path: '/login', query: { reason: 'session_expired' } })
            .catch(err => {
              console.warn('[App.vue] router.replace échoué, fallback window.location', err);
              window.location.href = '/login?reason=session_expired';
            });
        }
      }
    } catch (e) {
      console.warn('Erreur lors de la vérification de session:', e);
    }
  }, 1000);
}

function stopSessionCheck() {
  if (sessionCheckInterval) {
    clearInterval(sessionCheckInterval);
    sessionCheckInterval = null;
    console.log('[App.vue] Vérification session stoppée');
  }
}

onMounted(() => {
  startSessionCheck();

  // Écouter les événements personnalisés pour start/stop dans le même onglet
  window.addEventListener('userLoggedOut', stopSessionCheck);
  window.addEventListener('userLoggedIn', startSessionCheck);
});

onUnmounted(() => {
  stopSessionCheck();
  window.removeEventListener('userLoggedOut', stopSessionCheck);
  window.removeEventListener('userLoggedIn', startSessionCheck);
});
</script>

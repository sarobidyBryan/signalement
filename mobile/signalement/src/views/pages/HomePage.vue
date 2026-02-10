<template>
  <ion-page>
    <!-- Header avec navigation -->
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar>
        <!-- Conteneur principal flex en colonne sur mobile -->
        <div class="flex flex-row md:items-center justify-between w-full px-2 sm:px-4 md:px-6 py-2 md:py-0">
          
          <!-- Titre - toujours en première ligne -->
          <div class="flex align-middle justify-center text-black font-bold text-xl w-full md:w-auto text-center md:text-left mb-2 md:mb-0">
            <p>
              Signaleo
            </p>
          </div>
          
          <!-- Boutons - passe à la ligne sur mobile -->
          <div class="flex items-center justify-center md:justify-end space-x-4 w-full md:w-auto">
            <ion-button 
              @click="goToLogin" 
              class="custom-primary-btn-mini"
              fill="outline"
              size="small"
            >
              <span class="text-white font-medium text-sm md:text-base">Connexion</span>
            </ion-button>
          </div>
        </div>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <!-- WRAPPER principal avec Tailwind -->
      <div class="bg-white min-h-full">
        <!-- Hero Section -->
        <section class="pt-8 pb-20 px-4 md:px-8">
          <div class="max-w-7xl mx-auto">
            <div class="md:grid md:grid-cols-2 md:gap-12 md:items-center">
              <!-- Texte principal -->
              <div class="space-y-8">
                <h1 class="text-5xl md:text-6xl font-bold text-gray-900 leading-tight">
                  Votre voix pour des 
                  <span class="text-black">routes plus sûres</span>
                </h1>
                
                <p class="text-xl text-gray-600 leading-relaxed">
                  Signalez en temps réel les problèmes routiers, 
                  contribuez à l'amélioration d'Antananarivo et 
                  faites partie du changement.
                </p>
                
                <!-- Boutons d'action - ICI on utilise des div pour éviter les conflits -->
                <div class="flex flex-col sm:flex-row gap-4 pt-4">
                  <!-- Bouton Register avec wrapper -->
                  <div class="w-full sm:w-auto">
                    <ion-button 
                      @click="goToLogin"
                      class="custom-secondary-btn"
                      expand="block"
                      size="large"
                      fill="outline"
                    >
                      <div class="flex items-center justify-center">
                        <span>Se connecter</span>
                        <ion-icon :icon="arrowForward" class="ml-2"></ion-icon>
                      </div>
                    </ion-button>
                  </div>
                </div>
                
                <!-- Statistiques -->
                <div class="grid grid-cols-3 gap-6 pt-8">
                  <div class="text-center">
                    <div class="text-3xl font-bold text-black">10K+</div>
                    <div class="text-gray-600">Signalements</div>
                  </div>
                  <div class="text-center">
                    <div class="text-3xl font-bold text-black">50+</div>
                    <div class="text-gray-600">Communes</div>
                  </div>
                  <div class="text-center">
                    <div class="text-3xl font-bold text-black">24h</div>
                    <div class="text-gray-600">Traitement moyen</div>
                  </div>
                </div>
              </div>
              
              <!-- Illustration -->
              <div class="relative mt-8 md:mt-0">
                <div class="bg-gradient-to-br from-gray-100 to-white rounded-3xl p-8 shadow-2xl">
                  <div class="space-y-6">
                    <!-- Carte 1 - Utiliser div au lieu de composants Ionic -->
                    <div class="bg-white rounded-2xl p-6 shadow-lg border border-gray-200">
                      <div class="flex items-start space-x-4">
                        <div class="bg-red-100 p-3 rounded-xl">
                          <ion-icon :icon="warning" class="text-red-600 text-2xl"></ion-icon>
                        </div>
                        <div class="flex-1">
                          <div class="flex justify-between items-start">
                            <h3 class="font-bold text-lg text-gray-900">Crevasse</h3>
                            <span class="bg-gray-100 text-gray-700 text-sm font-medium px-3 py-1 rounded-full">
                              En cours
                            </span>
                          </div>
                          <p class="text-gray-600 mt-2">Route Ivato-Aeroport</p>
                          <div class="flex items-center mt-3 text-sm text-gray-500">
                            <ion-icon :icon="time" class="mr-1 text-lg"></ion-icon>
                            <span>Signalé il y a 2h</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- Carte 2 -->
                    <div class="bg-white rounded-2xl p-6 shadow-lg border border-gray-200">
                      <div class="flex items-start space-x-4">
                        <div class="bg-green-100 p-3 rounded-xl">
                          <ion-icon :icon="checkmarkCircle" class="text-green-600 text-2xl"></ion-icon>
                        </div>
                        <div class="flex-1">
                          <div class="flex justify-between items-start">
                            <h3 class="font-bold text-lg text-gray-900">Éclairage réparé</h3>
                            <span class="bg-green-100 text-green-700 text-sm font-medium px-3 py-1 rounded-full">
                              Résolu
                            </span>
                          </div>
                          <p class="text-gray-600 mt-2">Digue Andohatapenaka</p>
                          <div class="flex items-center mt-3 text-sm text-gray-500">
                            <ion-icon :icon="time" class="mr-1 text-lg"></ion-icon>
                            <span>Résolu en 48h</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Features Section -->
        <section class="py-20 bg-gray-50">
          <div class="max-w-7xl mx-auto px-4 md:px-8">
            <div class="text-center mb-16">
              <h2 class="text-4xl font-bold text-gray-900 mb-4">
                Comment ça marche ?
              </h2>
              <p class="text-xl text-gray-600 max-w-3xl mx-auto">
                Une plateforme simple et efficace pour signaler les problèmes routiers dans la ville d'Antananarivo
              </p>
            </div>
            
            <div class="md:grid md:grid-cols-3 md:gap-8">
              <!-- Étape 1 -->
              <div class="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-shadow mb-6 md:mb-0">
                <div class="bg-black text-white w-12 h-12 rounded-xl flex items-center justify-center text-2xl font-bold mb-6">
                  1
                </div>
                <h3 class="text-2xl font-bold text-gray-900 mb-4">Signalez</h3>
                <p class="text-gray-600 mb-6">
                  Identifiez et décrivez le problème routier que vous avez rencontré.
                </p>
                <div class="bg-gray-100 rounded-xl p-4 inline-block">
                  <ion-icon :icon="eye" class="text-3xl text-gray-700"></ion-icon>
                </div>
              </div>
              
              <!-- Étape 2 -->
              <div class="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-shadow mb-6 md:mb-0">
                <div class="bg-black text-white w-12 h-12 rounded-xl flex items-center justify-center text-2xl font-bold mb-6">
                  2
                </div>
                <h3 class="text-2xl font-bold text-gray-900 mb-4">Localisez</h3>
                <p class="text-gray-600 mb-6">
                  Géolocalisez automatiquement le problème pour un traitement rapide.
                </p>
                <div class="bg-gray-100 rounded-xl p-4 inline-block">
                  <ion-icon :icon="location" class="text-3xl text-gray-700"></ion-icon>
                </div>
              </div>
              
              <!-- Étape 3 -->
              <div class="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-shadow">
                <div class="bg-black text-white w-12 h-12 rounded-xl flex items-center justify-center text-2xl font-bold mb-6">
                  3
                </div>
                <h3 class="text-2xl font-bold text-gray-900 mb-4">Suivez</h3>
                <p class="text-gray-600 mb-6">
                  Suivez l'évolution de votre signalement jusqu'à sa résolution.
                </p>
                <div class="bg-gray-100 rounded-xl p-4 inline-block">
                  <ion-icon :icon="notifications" class="text-3xl text-gray-700"></ion-icon>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- CTA Section -->
        <section class="py-20 bg-black">
          <div class="max-w-4xl mx-auto px-4 md:px-8 text-center">
            <h2 class="text-4xl font-bold text-white mb-6">
              Rejoignez la communauté
            </h2>
            <p class="text-xl text-gray-300 mb-10 max-w-2xl mx-auto">
              Des milliers de citoyens contribuent déjà à l'amélioration de leurs routes. 
              Votre signalement fait la différence.
            </p>
            
            <div class="flex flex-col sm:flex-row gap-4 justify-center">
              
              
              <!-- Bouton Login -->
              <div>
                <ion-button 
                  @click="goToLogin"
                  class="cta-secondary-btn"
                  size="large"
                  fill="outline"
                >
                  <span class="text-white font-medium">Accéder à mon compte</span>
                </ion-button>
              </div>
            </div>
            
            <p class="text-gray-400 mt-8 text-sm">
              Aucune carte de crédit requise • Gratuit pour les citoyens
            </p>
          </div>
        </section>

        <!-- Footer -->
        <footer class="bg-gray-900 text-white py-12">
          <div class="max-w-7xl mx-auto px-4 md:px-8">
            <div class="flex flex-col md:flex-row justify-between items-center">
              <div class="mb-8 md:mb-0">
                <h3 class="text-2xl font-bold mb-4">Signaleo</h3>
                <p class="text-gray-400">
                  Construisons ensemble des routes plus sûres
                </p>
              </div>
              
              <div class="flex space-x-6">
                <a href="#" class="text-gray-400 hover:text-white transition-colors">
                  Conditions d'utilisation
                </a>
                <a href="#" class="text-gray-400 hover:text-white transition-colors">
                  Politique de confidentialité
                </a>
                <a href="#" class="text-gray-400 hover:text-white transition-colors">
                  Contact
                </a>
              </div>
            </div>
            
            <div class="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400 text-sm">
              © 2026 Signaleo. Tous droits réservés.
            </div>
          </div>
        </footer>
      </div>
    </ion-content>
  </ion-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { 
  IonPage, 
  IonHeader, 
  IonContent, 
  IonTitle, 
  IonToolbar, 
  IonButton,
  IonIcon 
} from '@ionic/vue';

import { 
  arrowForward, 
  warning, 
  checkmarkCircle, 
  time, 
  camera, 
  location, 
  notifications, 
  eye
} from 'ionicons/icons';

export default defineComponent({
  name: 'LandingPage',
  
  components: {
    IonPage, 
    IonHeader, 
    IonContent, 
    IonTitle, 
    IonToolbar, 
    IonButton,
    IonIcon
  },
  
  data() {
    return {
      arrowForward,
      warning,
      checkmarkCircle,
      time,
      camera,
      location,
      notifications,
      eye
    };
  },
  
  methods: {
    goToLogin() {
      this.$router.push('/login');
    },
    
    
  }
});
</script>

<style scoped>
/* ============================================
   STRATÉGIE : UTILISER LES VARIABLES CSS IONIC
   ============================================ */

/* 1. Pour les composants Ionic, utiliser les variables CSS Ionic */
:deep(.custom-btn) {
  --background: transparent !important;
  --color: #000000 !important;
  --background-hover: rgba(0, 0, 0, 0.05) !important;
  --box-shadow: none !important;
}

:deep(.custom-btn-register) {
  --background: #000000 !important;
  --background-hover: #1a1a1a !important;
  --color: #ffffff !important;
  --border-radius: 12px !important;
  --padding-start: 1.5rem !important;
  --padding-end: 1.5rem !important;
}

/* 2. Pour les boutons principaux */
:deep(.custom-primary-btn) {
  --background: #000000 !important;
  --background-hover: #1a1a1a !important;
  --color: #ffffff !important;
  --border-radius: 12px !important;
  --padding-top: 1rem !important;
  --padding-bottom: 1rem !important;
  height: 56px !important;
}


:deep(.custom-primary-btn-mini) {
  --background: #000000 !important;
  --background-hover: #1a1a1a !important;
  --color: #ffffff !important;
  --border-radius: 8px !important;
  height: 36px !important;
}

:deep(.custom-secondary-btn) {
  --border-color: #000000 !important;
  --border-width: 2px !important;
  --color: #000000 !important;
  --border-radius: 12px !important;
  --padding-top: 1rem !important;
  --padding-bottom: 1rem !important;
  --background-hover: rgba(0, 0, 0, 0.05) !important;
  height: 56px !important;
}

/* 3. Pour les boutons CTA */
:deep(.cta-primary-btn) {
  --background: #ffffff !important;
  --background-hover: #f5f5f5 !important;
  --color: #000000 !important;
  --border-radius: 12px !important;
}

:deep(.cta-secondary-btn) {
  --border-color: #ffffff !important;
  --border-width: 2px !important;
  --color: #ffffff !important;
  --border-radius: 12px !important;
  --background-hover: rgba(255, 255, 255, 0.1) !important;
}

/* ============================================
   CORRECTION DES PROBLÈMES COURANTS
   ============================================ */

/* Force l'application des couleurs */
:deep(ion-title.text-black) {
  color: #000000 !important;
}

/* Correction pour le contenu */
:deep(ion-content) {
  --background: transparent !important;
}

/* Pour les icônes dans les boutons */
:deep(ion-button) ion-icon {
  font-size: 1.25rem !important;
}

/* ============================================
   MODE SOMBRE
   ============================================ */

@media (prefers-color-scheme: dark) {
  /* Surcharger les variables en mode sombre */
  :deep(.custom-btn) {
    --color: #ffffff !important;
    --background-hover: rgba(255, 255, 255, 0.1) !important;
  }
  
  :deep(ion-title.text-black) {
    color: #ffffff !important;
  }
  
  :deep(.custom-primary-btn) {
    --background-hover: #2a2a2a !important;
  }
  
  :deep(.custom-secondary-btn) {
    --border-color: #ffffff !important;
    --color: #ffffff !important;
    --background-hover: rgba(255, 255, 255, 0.1) !important;
  }
}
</style>
<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title class="font-bold text-black">Menu</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="bg-gray-50">
      <!-- Profil utilisateur -->
      <div class="px-4 py-6">
        <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
          <div class="flex items-center space-x-4 mb-6">
            <div class="w-16 h-16 bg-black rounded-full flex items-center justify-center">
              <ion-icon :icon="person" class="text-white text-2xl"></ion-icon>
            </div>
            <div class="flex-1">
              <h2 class="text-xl font-bold text-gray-900">{{ utilisateur.nom }}</h2>
              <p class="text-gray-600">{{ utilisateur.email }}</p>
            </div>
          </div>
          
          <div class="grid grid-cols-3 gap-4 text-center">
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-black mb-1">{{ stats.signalements }}</div>
              <div class="text-sm text-gray-600">Signalements</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-black mb-1">{{ stats.resolus }}</div>
              <div class="text-sm text-gray-600">Résolus</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="text-2xl font-bold text-black mb-1">{{ stats.contributions }}</div>
              <div class="text-sm text-gray-600">Contributions</div>
            </div>
          </div>
        </div>

        <!-- Options du menu -->
        <div class="space-y-3">
          <div 
            v-for="item in menuItems" 
            :key="item.id"
            @click="navigateTo(item.route)"
            class="bg-white rounded-xl p-4 flex items-center justify-between hover:bg-gray-50 cursor-pointer transition-colors"
          >
            <div class="flex items-center space-x-4">
              <div :class="`p-3 rounded-xl ${item.color}`">
                <ion-icon :icon="item.icon" class="text-xl text-gray-700"></ion-icon>
              </div>
              <div>
                <h3 class="font-medium text-gray-900">{{ item.title }}</h3>
                <p class="text-sm text-gray-600">{{ item.description }}</p>
              </div>
            </div>
            <ion-icon :icon="chevronForward" class="text-gray-400"></ion-icon>
          </div>
        </div>

        <!-- Déconnexion -->
        <div class="mt-8">
          <ion-button 
            @click="deconnexion" 
            expand="block" 
            class="custom-logout-btn"
          >
            <ion-icon slot="start" :icon="logOut"></ion-icon>
            Déconnexion
          </ion-button>
        </div>

        <!-- Version -->
        <div class="text-center mt-8 text-gray-500 text-sm">
          Version 1.0.0
        </div>
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
  person,
  settings,
  helpCircle,
  documentText,
  star,
  chevronForward,
  logOut
} from 'ionicons/icons';

export default defineComponent({
  name: 'MenuPage',
  
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
      utilisateur: {
        nom: 'Jean Dupont',
        email: 'jean.dupont@email.com'
      },
      stats: {
        signalements: 12,
        resolus: 8,
        contributions: 45
      },
      menuItems: [
        {
          id: 1,
          title: 'Paramètres',
          description: 'Gérer vos préférences',
          icon: settings,
          color: 'bg-blue-100',
          route: '/parametres'
        },
        {
          id: 2,
          title: 'Aide & Support',
          description: 'FAQ et contact',
          icon: helpCircle,
          color: 'bg-green-100',
          route: '/aide'
        },
        {
          id: 3,
          title: 'Mes signalements',
          description: 'Voir votre historique',
          icon: documentText,
          color: 'bg-purple-100',
          route: '/mes-signalements'
        },
        {
          id: 4,
          title: 'Contributions',
          description: 'Votre impact sur la communauté',
          icon: star,
          color: 'bg-yellow-100',
          route: '/contributions'
        },
        {
          id: 5,
          title: 'Confidentialité',
          description: 'Gérer vos données',
          color: 'bg-gray-100',
          icon: documentText,
          route: '/confidentialite'
        }
      ],
      person,
      settings,
      helpCircle,
      documentText,
      star,
      chevronForward,
      logOut
    };
  },
  
  methods: {
    navigateTo(route: string) {
      console.log('Navigation vers:', route);
      // this.$router.push(route);
    },
    
    deconnexion() {
      console.log('Déconnexion');
      // Logique de déconnexion Firebase
      // this.$router.push('/login');
    }
  }
});
</script>

<style scoped>
/* Styles pour la page menu */
:deep(.custom-logout-btn) {
  --background: #fee2e2;
  --color: #dc2626;
  --border-radius: 12px;
  --background-hover: #fecaca;
}
</style>
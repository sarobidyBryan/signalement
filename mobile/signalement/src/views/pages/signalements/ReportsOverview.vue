<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title class="font-bold text-black">Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="centrerPosition" class="custom-btn">
            <ion-icon slot="icon-only" :icon="locate"></ion-icon>
          </ion-button>
          <ion-button @click="changerTypeCarte" class="custom-btn">
            <ion-icon slot="icon-only" :icon="layers"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="relative">
      <!-- Carte (à remplacer par une vraie carte Google Maps/Leaflet) -->
      <div class="h-full bg-gray-200 relative">
        <!-- Carte fictive -->
        <div class="absolute inset-0 bg-gradient-to-br from-blue-50 to-green-50 flex items-center justify-center">
          <div class="text-center">
            <ion-icon :icon="map" class="text-gray-400 text-8xl mb-6"></ion-icon>
            <h3 class="text-xl font-medium text-gray-900 mb-2">Carte des signalements</h3>
            <p class="text-gray-600 mb-4">Intégrez ici votre solution de cartographie</p>
            <ion-button @click="nouveauSignalement" class="custom-primary-btn">
              <ion-icon slot="start" :icon="add"></ion-icon>
              Signaler ici
            </ion-button>
          </div>
        </div>

        <!-- Marqueurs fictifs -->
        <div 
          v-for="signalement in signalements" 
          :key="signalement.id"
          :style="`position: absolute; top: ${signalement.position.y}%; left: ${signalement.position.x}%;`"
          class="transform -translate-x-1/2 -translate-y-1/2"
        >
          <div 
            :class="`w-8 h-8 rounded-full flex items-center justify-center cursor-pointer ${signalement.status === 'resolu' ? 'bg-green-500' : 'bg-red-500'} shadow-lg`"
            @click="voirSignalementCarte(signalement.id)"
          >
            <ion-icon :icon="signalement.status === 'resolu' ? checkmarkCircle : warning" class="text-white"></ion-icon>
          </div>
        </div>
      </div>

      <!-- Légende -->
      <div class="absolute bottom-20 left-4 right-4 bg-white rounded-xl shadow-lg p-4">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-gray-900">Légende</h4>
          <span class="text-sm text-gray-600">{{ signalements.length }} signalements</span>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div class="flex items-center">
            <div class="w-3 h-3 rounded-full bg-red-500 mr-2"></div>
            <span class="text-sm text-gray-700">En cours</span>
          </div>
          <div class="flex items-center">
            <div class="w-3 h-3 rounded-full bg-green-500 mr-2"></div>
            <span class="text-sm text-gray-700">Résolu</span>
          </div>
        </div>
      </div>
    </ion-content>
    <TabBar active-tab="carte" />
  </ion-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TabBar from '@/views/components/global/TabBar.vue';
import { 
  IonPage, 
  IonHeader, 
  IonContent, 
  IonTitle, 
  IonToolbar,
  IonButton,
  IonButtons,
  IonIcon
} from '@ionic/vue';
import { 
  map,
  locate,
  layers,
  add,
  warning,
  checkmarkCircle
} from 'ionicons/icons';

export default defineComponent({
  name: 'ReportsOverview',
  
  components: {
    IonPage, 
    IonHeader, 
    IonContent, 
    IonTitle, 
    IonToolbar,
    IonButton,
    IonButtons,
    IonIcon,
    TabBar
  },
  
  data() {
    return {
      signalements: [
        {
          id: 1,
          titre: 'Nid-de-poule',
          status: 'en-cours',
          position: { x: 30, y: 40 }
        },
        {
          id: 2,
          titre: 'Éclairage',
          status: 'resolu',
          position: { x: 60, y: 30 }
        },
        {
          id: 3,
          titre: 'Poubelle',
          status: 'en-cours',
          position: { x: 45, y: 60 }
        }
      ],
      map,
      locate,
      layers,
      add,
      warning,
      checkmarkCircle
    };
  },
  
  methods: {
    centrerPosition() {
      console.log('Centrer sur la position actuelle');
    },
    
    changerTypeCarte() {
      console.log('Changer le type de carte');
    },
    
    nouveauSignalement() {
      console.log('Nouveau signalement depuis la carte');
    },
    
    voirSignalementCarte(id: number) {
      console.log('Voir signalement sur carte', id);
    }
  }
});
</script>

<style scoped>
/* Styles pour la page carte */
:deep(.custom-btn) {
  --color: #000000;
  --background: transparent;
}

:deep(.custom-primary-btn) {
  --background: #000000;
  --color: #ffffff;
  --border-radius: 12px;
}
</style>
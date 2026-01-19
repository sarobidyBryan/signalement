<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title class="font-bold text-black">Signalements</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="nouveauSignalement" class="custom-btn">
            <ion-icon slot="icon-only" :icon="add"></ion-icon>
          </ion-button>
          <ion-button @click="rafraichir" class="custom-btn">
            <ion-icon slot="icon-only" :icon="refresh"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="bg-gray-50">
      <!-- Filtres -->
      <div class="px-4 pt-4">
        <div class="flex space-x-2 mb-4">
          <ion-chip :outline="filter !== 'tous'" @click="filter = 'tous'">
            <ion-label>Tous</ion-label>
          </ion-chip>
          <ion-chip :outline="filter !== 'en-cours'" @click="filter = 'en-cours'">
            <ion-label>En cours</ion-label>
          </ion-chip>
          <ion-chip :outline="filter !== 'resolus'" @click="filter = 'resolus'">
            <ion-label>Résolus</ion-label>
          </ion-chip>
        </div>
      </div>

      <!-- Liste des signalements -->
      <div class="px-4 pb-20">
        <div v-if="signalements.length === 0" class="text-center py-12">
          <ion-icon :icon="documentText" class="text-gray-400 text-6xl mb-4"></ion-icon>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun signalement</h3>
          <p class="text-gray-600">Commencez par signaler un problème</p>
          <ion-button @click="nouveauSignalement" class="mt-4 custom-primary-btn">
            Nouveau signalement
          </ion-button>
        </div>

        <div v-else class="space-y-4">
          <div 
            v-for="signalement in signalementsFiltres" 
            :key="signalement.id"
            class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"
            @click="voirSignalement(signalement.id)"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center space-x-3">
                <div :class="`p-2 rounded-lg ${signalement.status === 'resolu' ? 'bg-green-100' : 'bg-red-100'}`">
                  <ion-icon 
                    :icon="signalement.status === 'resolu' ? checkmarkCircle : warning" 
                    :class="signalement.status === 'resolu' ? 'text-green-600' : 'text-red-600'"
                    class="text-xl"
                  ></ion-icon>
                </div>
                <div>
                  <h3 class="font-bold text-gray-900">{{ signalement.titre }}</h3>
                  <p class="text-sm text-gray-600">{{ signalement.categorie }}</p>
                </div>
              </div>
              <span :class="`px-3 py-1 rounded-full text-xs font-medium ${signalement.status === 'resolu' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`">
                {{ signalement.status === 'resolu' ? 'Résolu' : 'En cours' }}
              </span>
            </div>
            
            <p class="text-gray-600 text-sm mb-3">{{ signalement.description }}</p>
            
            <div class="flex items-center justify-between text-sm text-gray-500">
              <div class="flex items-center">
                <ion-icon :icon="location" class="mr-1"></ion-icon>
                <span>{{ signalement.adresse }}</span>
              </div>
              <div class="flex items-center">
                <ion-icon :icon="time" class="mr-1"></ion-icon>
                <span>{{ signalement.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
    <TabBar />
    
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
  IonIcon,
  IonChip,
  IonLabel
} from '@ionic/vue';
import { 
  add, 
  refresh, 
  documentText,
  warning, 
  checkmarkCircle,
  location,
  time
} from 'ionicons/icons';

export default defineComponent({
  name: 'ReportsList',
  
  components: {
    IonPage, 
    IonHeader, 
    IonContent, 
    IonTitle, 
    IonToolbar,
    IonButton,
    IonButtons,
    IonIcon,
    IonChip,
    IonLabel,
    TabBar
  },
  
  data() {
    return {
      filter: 'tous',
      signalements: [
        {
          id: 1,
          titre: 'Nid-de-poule important',
          description: 'Nid-de-poule de 30cm de diamètre sur la chaussée',
          categorie: 'Chaussée',
          adresse: 'Rue de la Paix, Paris',
          date: 'Il y a 2h',
          status: 'en-cours'
        },
        {
          id: 2,
          titre: 'Éclairage défectueux',
          description: 'Lampe publique clignotante',
          categorie: 'Éclairage',
          adresse: 'Avenue des Champs-Élysées',
          date: 'Il y a 1j',
          status: 'resolu'
        },
        {
          id: 3,
          titre: 'Poubelle renversée',
          description: 'Poubelle publique renversée, déchets sur la voie',
          categorie: 'Propreté',
          adresse: 'Boulevard Saint-Germain',
          date: 'Il y a 3h',
          status: 'en-cours'
        }
      ],
      add,
      refresh,
      documentText,
      warning,
      checkmarkCircle,
      location,
      time
    };
  },
  
  computed: {
    signalementsFiltres() {
      if (this.filter === 'tous') return this.signalements;
      return this.signalements.filter(s => s.status === this.filter);
    }
  },
  
  methods: {
    nouveauSignalement() {
      console.log('Nouveau signalement');
      // Navigation vers le formulaire
    },
    
    rafraichir() {
      console.log('Rafraîchir la liste');
      // Recharger les données
    },
    
    voirSignalement(id: number) {
      console.log('Voir signalement', id);
      // Navigation vers le détail
    }
  }
});
</script>

<style scoped>
/* Styles pour la page liste */
:deep(.custom-btn) {
  --color: #000000;
  --background: transparent;
}

:deep(.custom-primary-btn) {
  --background: #000000;
  --color: #ffffff;
  --border-radius: 12px;
}

:deep(ion-chip) {
  --background: transparent;
  --color: #6b7280;
  --border-color: #d1d5db;
}

:deep(ion-chip[outline="false"]) {
  --background: #000000;
  --color: #ffffff;
  --border-color: #000000;
}
</style>
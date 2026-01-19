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
          <ion-chip
            v-for="sf in statusFilters"
            :key="sf.code"
            :outline="filter !== sf.code"
            @click="filter = sf.code"
          >
            <ion-label>{{ sf.label }}</ion-label>
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
                <div :class="`p-2 rounded-lg ${statusPillBg(signalement.status)}`">
                  <ion-icon
                    :icon="statusIcon(signalement.status)"
                    :class="statusIconClass(signalement.status) + ' text-xl'"
                  ></ion-icon>
                </div>
                <div>
                  <h3 class="font-bold text-gray-900">{{ signalement.titre }}</h3>
                  <p class="text-sm text-gray-600">{{ signalement.area }} m2</p>
                </div>
              </div>
              <span :class="statusPillClass(signalement.status)">
                {{ statusLabel(signalement.status) }}
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
import { collection, getDocs, query, orderBy } from 'firebase/firestore';
import { db } from '@/config/firebase';
import { 
  add, 
  refresh, 
  documentText,
  warning, 
  checkmarkCircle,
  closeCircle,
  location,
  time
} from 'ionicons/icons';

type Report = {
  id: any;
  titre: string;
  area: string | null;
  description: string;
  adresse: string;
  date: string;
  status: string;
};

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
      statusFilters: [
        { code: 'SUBMITTED', label: 'Soumis' },
        { code: 'UNDER_REVIEW', label: "En cours d'examen" },
        { code: 'ASSIGNED', label: 'Assigné' },
        { code: 'IN_PROGRESS', label: 'Travaux en cours' },
        { code: 'COMPLETED', label: 'Terminé' },
        { code: 'CANCELLED', label: 'Annulé' },
        { code: 'VERIFIED', label: 'Vérifié' }
      ],
      signalements: [] as Report[],
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
    async loadSignalements() {
      try {
        const q = query(collection(db, 'reports'), orderBy('report_date', 'desc'));
        const snapshot = await getDocs(q);
        const results: Report[] = snapshot.docs.map(doc => {
          const d: any = doc.data();

          const formatDate = (ts: any) => {
            if (!ts) return '';
            const dateObj = ts.toDate ? ts.toDate() : (ts instanceof Date ? ts : new Date(ts));
            return dateObj.toLocaleString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' });
          };

          const titre = d.description ? (d.description.length > 50 ? d.description.slice(0, 50) + '...' : d.description) : `Signalement ${d.id ?? doc.id}`;
          return {
            id: d.id ?? doc.id,
            titre,
            area: d.area ?? null,
            description: d.description ?? '',
            adresse: (d.latitude !== undefined && d.longitude !== undefined) ? `${d.latitude}, ${d.longitude}` : '',
            date: formatDate(d.report_date),
            status: d.status ?? 'SUBMITTED'
          } as Report;
        });
        this.signalements = results;
      } catch (err) {
        console.error('Erreur chargement reports:', err);
      }
    },
    nouveauSignalement() {
      console.log('Nouveau signalement');
      // Navigation vers le formulaire
    },
    
    async rafraichir() {
      console.log('Rafraîchir la liste');
      await this.loadSignalements();
    },
    
    voirSignalement(id: number) {
      console.log('Voir signalement', id);
      // Navigation vers le détail
    }
    ,
    statusLabel(status: string) {
      if (!status) return '';
      switch (status) {
        case 'SUBMITTED':
          return 'Soumis';
        case 'UNDER_REVIEW':
          return "En cours d'examen";
        case 'ASSIGNED':
          return 'Assigné à une entreprise';
        case 'IN_PROGRESS':
          return 'Travaux en cours';
        case 'COMPLETED':
          return 'Terminé';
        case 'CANCELLED':
          return 'Annulé';
        case 'VERIFIED':
          return 'Vérifié et validé';
        default:
          return status;
      }
    },
    statusPillClass(status: string) {
      const color = (() => {
        switch (status) {
          case 'COMPLETED':
          case 'VERIFIED':
            return 'bg-green-100 text-green-700';
          case 'CANCELLED':
            return 'bg-red-100 text-red-700';
          case 'IN_PROGRESS':
          case 'ASSIGNED':
          case 'UNDER_REVIEW':
            return 'bg-yellow-100 text-yellow-700';
          case 'SUBMITTED':
            return 'bg-blue-100 text-blue-700';
          default:
            return 'bg-gray-100 text-gray-700';
        }
      })();
      return `px-3 py-1 rounded-full text-xs font-medium ${color}`;
    },
    statusPillBg(status: string) {
      switch (status) {
        case 'COMPLETED':
        case 'VERIFIED':
          return 'bg-green-100';
        case 'CANCELLED':
          return 'bg-red-100';
        case 'IN_PROGRESS':
        case 'ASSIGNED':
        case 'UNDER_REVIEW':
          return 'bg-yellow-100';
        case 'SUBMITTED':
          return 'bg-blue-100';
        default:
          return 'bg-gray-100';
      }
    },
    statusIcon(status: string) {
      switch (status) {
        case 'COMPLETED':
        case 'VERIFIED':
          return checkmarkCircle;
        case 'CANCELLED':
          return closeCircle;
        case 'IN_PROGRESS':
        case 'ASSIGNED':
        case 'UNDER_REVIEW':
          return warning;
        case 'SUBMITTED':
          return documentText;
        default:
          return documentText;
      }
    },
    statusIconClass(status: string) {
      switch (status) {
        case 'COMPLETED':
        case 'VERIFIED':
          return 'text-green-600';
        case 'CANCELLED':
          return 'text-red-600';
        case 'IN_PROGRESS':
        case 'ASSIGNED':
        case 'UNDER_REVIEW':
          return 'text-yellow-600';
        case 'SUBMITTED':
          return 'text-blue-600';
        default:
          return 'text-gray-600';
      }
    }
  }
  ,
  created() {
    this.loadSignalements();
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
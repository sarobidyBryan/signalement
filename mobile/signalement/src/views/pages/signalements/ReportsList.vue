<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title class="font-bold text-black">Signalements</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="toggleSignalementForm" class="custom-btn">
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
        <div class="flex space-x-2 mb-4 filter-chips">
          <ion-chip :outline="filter !== 'tous'" @click="filter = 'tous'">
            <ion-label>Tous</ion-label>
          </ion-chip>
          <ion-chip
            v-for="sf in statuses"
            :key="sf.status_code"
            :outline="filter !== sf.status_code"
            @click="filter = sf.status_code"
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
          <ion-button @click="toggleSignalementForm" class="mt-4 custom-primary-btn">
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
                  <p class="text-sm text-gray-600" v-if="signalement.area">{{ signalement.area }} m²</p>
                </div>
              </div>
              <span :class="statusPillClass(signalement.status)">
                {{ statusLabel(signalement.status) }}
              </span>
            </div>
            
            <p class="text-gray-600 text-sm mb-2">{{ signalement.description }}</p>
            
            <div class="flex items-center justify-between text-sm text-gray-500">
              <div class="flex items-center" v-if="signalement.adresse">
                <ion-icon :icon="location" class="mr-1"></ion-icon>
                <span>{{ signalement.adresse }}</span>
              </div>
              <div class="flex items-center" v-if="signalement.date">
                <ion-icon :icon="time" class="mr-1"></ion-icon>
                <span>{{ signalement.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
    <ReportForm ref="reportFormRef" v-if="showCreationForm" @close="showCreationForm = false" @submit="handleSubmit"/>
    <TabBar />
    
  </ion-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TabBar from '@/views/components/global/TabBar.vue';
import ReportForm from '@/views/components/global/signalement/ReportForm.vue';
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
import { collection, getDocs, query, orderBy, setDoc, doc, addDoc } from 'firebase/firestore';
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
  budget?: number | null;
  company_name?: string | null;
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
    TabBar,
    ReportForm
  },
  
  data() {
    return {
      showCreationForm : false,
      filter: 'tous',
      statuses: [] as Array<{ id?: number; status_code: string; label: string }>,
      signalements: [] as Report[],
      reportFormRef: null,
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
    async loadStatuses() {
      try {
        const q = query(collection(db, 'status'), orderBy('id'));
        const snap = await getDocs(q);
        const items = snap.docs.map(doc => {
          const d: any = doc.data();
          return {
            id: d.id ?? doc.id,
            status_code: d.status_code ?? d.statusCode ?? d.code,
            label: d.label ?? ''
          };
        });
        this.statuses = items;
      } catch (err) {
        console.error('Erreur chargement statuses:', err);
      }
    },
    async loadSignalements() {
      try {
        // Ne pas trier par report_date si le champ peut être manquant
        const q = query(collection(db, 'reports'));
        const snapshot = await getDocs(q);
        
        const results: Report[] = snapshot.docs.map(doc => {
          const d: any = doc.data();

          const formatDate = (ts: any) => {
            if (!ts) return '';
            try {
              const dateObj = ts.toDate ? ts.toDate() : (ts instanceof Date ? ts : new Date(ts));
              return dateObj.toLocaleString('fr-FR', { 
                day: '2-digit', 
                month: 'long', 
                year: 'numeric', 
                hour: '2-digit', 
                minute: '2-digit' 
              });
            } catch (error) {
              console.warn('Erreur formatage date pour doc', doc.id, error);
              return '';
            }
          };

          const titre = d.description ? 
            (d.description.length > 50 ? d.description.slice(0, 50) + '...' : d.description) : 
            `Signalement ${d.id ?? doc.id}`;
          
          return {
            id: d.id ?? doc.id,
            titre,
            area: d.area ? String(d.area) : null,
            description: d.description ?? '',
            budget: d.budget ?? d.budget_amount ?? null,
            company_name: d.company_name ?? d.companyName ?? null,
            adresse: (d.latitude !== undefined && d.longitude !== undefined) ? 
              `${d.latitude.toFixed(6)}, ${d.longitude.toFixed(6)}` : '',
            date: formatDate(d.report_date ?? d.created_at ?? d.createdAt),
            status: d.status ?? 'SUBMITTED'
          } as Report;
        });
        
        // Trier les résultats en mémoire par date (les plus récents en premier)
        this.signalements = results.sort((a, b) => {
          if (!a.date && !b.date) return 0;
          if (!a.date) return 1;
          if (!b.date) return -1;
          return new Date(b.date).getTime() - new Date(a.date).getTime();
        });
        
        console.log(`${this.signalements.length} signalement(s) chargé(s)`);
      } catch (err) {
        console.error('Erreur chargement reports:', err);
      }
    },

    async handleSubmit(payload) {
      console.log("Nous allons sauvegarder le payload:", payload);
      try {
        const docRef = await addDoc(collection(db, "reports"), payload);
        console.log("Signalement créé avec ID:", docRef.id);
        
        // Reset le formulaire
        if (this.$refs.reportFormRef) {
          this.$refs.reportFormRef.resetForm();
        }
        
        // Fermer le formulaire
        this.showCreationForm = false;
        
        // Recharger la liste des signalements
        await this.loadSignalements();
      } catch (error) {
        console.error("Erreur lors de la création du signalement:", error);
        // Gérer l'erreur (afficher un message, etc.)
      }
    },
    
    toggleSignalementForm() {
      console.log('Nouveau signalement');
      this.showCreationForm = !this.showCreationForm;
      console.log('isShowing',this.showCreationForm);
      // Navigation vers le formulaire
    },
    
    async rafraichir() {
      console.log('Rafraîchir la liste');
      await this.loadSignalements();
    },
    
    voirSignalement(id: number) {
      console.log('Voir signalement', id);
      // Navigation vers le détail
    },
    
    statusLabel(status: string) {
      if (!status) return 'Non défini';
      const found = this.statuses.find((s: any) => s.status_code === status);
      return found?.label ?? status;
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
  },
  
  created() {
    this.loadStatuses().then(() => this.loadSignalements()).catch(() => this.loadSignalements());
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

/* Responsive filter chips */
:deep(.filter-chips) {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 0.25rem;
}

:deep(.filter-chips ion-chip) {
  white-space: nowrap;
}

@media (max-width: 640px) {
  :deep(.filter-chips) {
    gap: 0.35rem;
  }
  :deep(.filter-chips ion-label) {
    font-size: 0.75rem;
  }
  :deep(.filter-chips ion-chip) {
    --padding-top: 4px;
    --padding-bottom: 4px;
    --padding-start: 8px;
    --padding-end: 8px;
  }
}
</style>
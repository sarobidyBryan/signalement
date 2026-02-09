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
          <ion-chip :outline="filter !== 'tous'" @click="filter = 'tous'; currentPage = 1">
            <ion-label>Tous</ion-label>
          </ion-chip>
          <ion-chip
            v-for="sf in statuses"
            :key="sf.statusCode"
            :outline="filter !== sf.statusCode"
            @click="filter = sf.statusCode; currentPage = 1"
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
            :data-report-id="signalement.id"
            :class="[{ 'highlight': signalement.id === highlightedReportId }, 'bg-white rounded-xl shadow-sm border border-gray-200 p-4']"
            tabindex="-1"
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
            <div class="mt-3">
              <div class="flex items-center">
                <ion-button v-if="signalement.image_report && signalement.image_report.length" fill="clear" size="small" @click.stop="openGalleryFor(signalement)" class="p-0">
                  <ion-icon :icon="image" class="mr-2"></ion-icon>
                  <span>Voir photos</span>
                </ion-button>
                <span v-else class="text-sm text-gray-400">Aucune photo</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex items-center justify-between mt-6 pb-4">
          <ion-button
            :disabled="currentPage === 1"
            @click="previousPage"
            fill="solid"
            size="default"
            class="pagination-btn"
            aria-label="Précédent"
          >
            <ion-icon slot="icon-only" :icon="chevronBack" size="large"></ion-icon>
          </ion-button>

          <div class="text-sm text-gray-600 text-center">
            Page {{ currentPage }} / {{ totalPages }}
            <span class="block text-xs text-gray-500">{{ totalFiltered }} signalement(s)</span>
          </div>

          <ion-button
            :disabled="currentPage === totalPages"
            @click="nextPage"
            fill="solid"
            size="default"
            class="pagination-btn"
            aria-label="Suivant"
          >
            <ion-icon slot="icon-only" :icon="chevronForward" size="large"></ion-icon>
          </ion-button>
        </div>
      </div>
    </ion-content>
    <ImageGalleryModal v-model="galleryVisible" :images="galleryImages" :startIndex="galleryStartIndex" />
    <ReportForm ref="reportFormRef" v-if="showCreationForm" @close="showCreationForm = false" @submit="handleSubmit"/>
    <TabBar />
    
  </ion-page>
</template>

<script lang="ts">
import { defineComponent, nextTick } from 'vue';
import TabBar from '@/views/components/global/TabBar.vue';
import ReportForm from '@/views/components/global/signalement/ReportForm.vue';
import ImageGalleryModal from '@/views/components/global/signalement/ImageGalleryModal.vue';
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
import { collection, getDocs, query, orderBy, doc, addDoc, updateDoc } from 'firebase/firestore';
import { db } from '@/config/firebase';
// import { uploadReportImages } from '@/services/supabaseStorage';
import { uploadReportImages } from '@/services/cloudinaryStorage';
import { 
  add, 
  refresh, 
  documentText,
  warning, 
  checkmarkCircle,
  closeCircle,
  chevronBack,
  chevronForward,
  location,
  time,
  image
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
  companyName?: string | null;
  image_report?: any[];
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
    ReportForm,
    ImageGalleryModal
  },
  
  data() {
    return {
      showCreationForm : false,
      filter: 'tous',
      statuses: [] as Array<{ id?: number; statusCode: string; label: string }>,
      signalements: [] as Report[],
      highlightedReportId: null as string | null,
      reportFormRef: null,
      galleryVisible: false,
      galleryImages: [] as string[],
      galleryStartIndex: 0,
      currentPage: 1,
      itemsPerPage: 10,
      chevronBack,
      chevronForward,
      add,
      refresh,
      documentText,
      warning,
      checkmarkCircle,
      image,
      location,
      time
    };
  },
  
  computed: {
    signalementsFiltres() {
      const filtered = this.filter === 'tous' 
        ? this.signalements 
        : this.signalements.filter(s => s.status === this.filter);
      
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return filtered.slice(start, end);
    },
    
    totalPages() {
      const filtered = this.filter === 'tous' 
        ? this.signalements 
        : this.signalements.filter(s => s.status === this.filter);
      return Math.ceil(filtered.length / this.itemsPerPage);
    },
    
    totalFiltered() {
      return this.filter === 'tous' 
        ? this.signalements.length 
        : this.signalements.filter(s => s.status === this.filter).length;
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
            statusCode: d.status_code ?? d.statusCode ?? d.code,
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
          
          // Coords peuvent être des chaînes, ne sont ici qu'affichées sous forme de texte
          const latTxt = d.latitude !== undefined ? String(d.latitude) : '';
          const lngTxt = d.longitude !== undefined ? String(d.longitude) : '';

          return {
            id: d.id ?? doc.id,
            titre,
            area: d.area ? String(d.area) : null,
            description: d.description ?? '',
            image_report: d.image_report ?? [],
            budget: d.assignation?.budget ?? null,
            companyName: d.assignation?.company?.name ?? null,
            adresse: (latTxt && lngTxt) ? `${latTxt}, ${lngTxt}` : '',
            date: formatDate(d.createdAt),
            status: d.status?.statusCode ?? 'SUBMITTED'
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

    async handleSubmit(payload: any) {
      console.log("Nous allons sauvegarder le payload:", payload);
      try {
        // Extraire les photos du payload (elles ne doivent pas être stockées brutes dans Firestore)
        const photos = payload._photos || [];
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { _photos, ...reportData } = payload;

        // 1. Créer le document dans Firestore
        const docRef = await addDoc(collection(db, "reports"), reportData);
        console.log("Signalement créé avec ID:", docRef.id);

        // 2. Upload des images vers Cloudinary (compressées)
        if (photos.length > 0) {
          try {
            console.log(`Upload de ${photos.length} image(s) vers Cloudinary...`);
            const imageReports = await uploadReportImages(photos, docRef.id);

            // 3. Mettre à jour le document Firestore avec les références des images
            if (imageReports.length > 0) {
              await updateDoc(doc(db, "reports", docRef.id), {
                image_report: imageReports, // [{id, id_report, lien}, ...]
              });
              console.log(`${imageReports.length} image(s) enregistrée(s) dans le signalement`);
            }
          } catch (uploadError) {
            console.error("Erreur lors de l'upload des images:", uploadError);
            alert("Le signalement a été créé mais certaines images n'ont pas pu être uploadées.");
          }
        }
        
        // Reset le formulaire
        if (this.$refs.reportFormRef) {
          (this.$refs.reportFormRef as any).resetForm();
        }
        
        // Fermer le formulaire
        this.showCreationForm = false;
        
        // Recharger la liste des signalements
        await this.loadSignalements();
        // Remettre le filtre sur 'tous' pour afficher le nouvel élément
        try {
          this.filter = 'tous';
        } catch (e) { /* no-op */ }
        // Mettre en surbrillance et focus sur le signalement créé
        try {
          if (docRef && docRef.id) {
            this.highlightAndScrollTo(docRef.id);
          }
        } catch (e) { /* no-op */ }
      } catch (error) {
        console.error("Erreur lors de la création du signalement:", error);
        alert("Erreur lors de la création du signalement. Veuillez réessayer.");
      }
    },

    async highlightAndScrollTo(id: string) {
      // Trouver la page où se trouve le signalement
      const filtered = this.filter === 'tous' 
        ? this.signalements 
        : this.signalements.filter(s => s.status === this.filter);
      
      const index = filtered.findIndex(s => s.id === id);
      
      if (index !== -1) {
        // Calculer la page appropriée (les pages commencent à 1)
        const targetPage = Math.floor(index / this.itemsPerPage) + 1;
        this.currentPage = targetPage;
      }
      
      this.highlightedReportId = id;
      // Attendre le rendu après le changement de page
      await nextTick();
      await new Promise(resolve => setTimeout(resolve, 100));

      const tryScroll = () => {
        const el = (this.$el as HTMLElement).querySelector(`[data-report-id="${id}"]`) as HTMLElement | null;
        if (el) {
          try {
            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            // rendre focusable et focus
            (el as HTMLElement).setAttribute('tabindex', '-1');
            (el as HTMLElement).focus();
          } catch (err) {
            console.warn('Impossible de scroller/mettre au focus:', err);
          }
          // Supprimer la surbrillance après quelques secondes
          setTimeout(() => {
            if (this.highlightedReportId === id) this.highlightedReportId = null;
          }, 7000);
          return true;
        }
        return false;
      };

      // Essayer immédiatement, sinon retenter quelques fois (DOM lazy)
      if (!tryScroll()) {
        let attempts = 0;
        const interval = setInterval(() => {
          attempts += 1;
          if (tryScroll() || attempts >= 8) {
            clearInterval(interval);
          }
        }, 250);
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
    
    previousPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.scrollToTop();
      }
    },
    
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.scrollToTop();
      }
    },
    
    scrollToTop() {
      const content = (this.$el as HTMLElement).querySelector('ion-content');
      if (content) {
        (content as any).scrollToTop(300);
      }
    },
    
    voirSignalement(id: number) {
      console.log('Voir signalement', id);
      // Navigation vers le détail
    },

    normalizeImages(imageArr: any[]) {
      if (!imageArr || !Array.isArray(imageArr)) return [];
      return imageArr.map((it: any) => {
        if (!it) return '';
        if (typeof it === 'string') return it;
        return it.lien ?? it.url ?? it.path ?? '';
      }).filter((u: string) => !!u);
    },

    openGalleryFor(signalement: Report, start = 0) {
      const imgs = this.normalizeImages((signalement as any).image_report ?? (signalement as any).raw?.image_report ?? []);
      if (!imgs || imgs.length === 0) {
        alert('Aucune photo pour ce signalement');
        return;
      }
      this.galleryImages = imgs;
      this.galleryStartIndex = start;
      this.galleryVisible = true;
    },
    
    statusLabel(status: string) {
      if (!status) return 'Non défini';
      const found = this.statuses.find((s: any) => s.statusCode === status);
      return found?.label ?? status;
    },
    
    statusPillClass(status: string) {
      const color = (() => {
        switch (status) {
          case 'COMPLETED':
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
/* Highlight recent created report */
.highlight {
  box-shadow: 0 0 0 4px rgba(59,130,246,0.18);
  border-color: #3b82f6 !important;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

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

/* Pagination buttons */
:deep(.pagination-btn) {
  --background: #ffffff;
  --color: #333535;
  --border-radius: 8px;
  --padding-start: 12px;
  --padding-end: 12px;
  min-width: 30px;
  height: 30px;
}

:deep(.pagination-btn[disabled]) {
  --background: #E5E7EB;
  --color: #9CA3AF;
}

:deep(.pagination-btn ion-icon) {
  font-size: 28px !important;
}
</style>
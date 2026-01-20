<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar class="bg-white">
        <ion-title class="font-bold text-black">Carte</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="centrerSurPosition" class="custom-btn">
            <ion-icon slot="icon-only" :icon="locate"></ion-icon>
          </ion-button>
          <ion-button @click="changerTypeCarte" class="custom-btn">
            <ion-icon slot="icon-only" :icon="layers"></ion-icon>
          </ion-button>
          <ion-button @click="toggleFiltres" class="custom-btn">
            <ion-icon slot="icon-only" :icon="filter"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="relative">
      <!-- Conteneur de la carte Leaflet -->
      <div ref="mapContainer" class="h-full w-full z-0"></div>

      <!-- Filtres flottants -->
      <div v-if="showFiltres" class="absolute top-20 left-4 right-4 bg-white rounded-xl shadow-xl p-4 z-10 animate-slideDown">
        <div class="flex items-center justify-between mb-4">
          <h4 class="font-bold text-gray-900">Filtrer les signalements</h4>
          <ion-button fill="clear" @click="toggleFiltres" class="p-0">
            <ion-icon :icon="close" class="text-gray-500"></ion-icon>
          </ion-button>
        </div>
        
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-3 h-3 rounded-full bg-red-500 mr-3"></div>
              <span class="text-gray-700">En cours</span>
            </div>
            <ion-toggle 
              :checked="filtres.enCours" 
              @ionChange="filtres.enCours = $event.detail.checked; filtrerSignalements()"
              color="success"
            ></ion-toggle>
          </div>
          
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-3 h-3 rounded-full bg-green-500 mr-3"></div>
              <span class="text-gray-700">Résolus</span>
            </div>
            <ion-toggle 
              :checked="filtres.resolus" 
              @ionChange="filtres.resolus = $event.detail.checked; filtrerSignalements()"
              color="success"
            ></ion-toggle>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <div class="w-3 h-3 rounded-full bg-gray-400 mr-3"></div>
              <span class="text-gray-700">Mes signalements uniquement</span>
            </div>
            <ion-toggle
              :checked="filtres.onlyMine"
              @ionChange="filtres.onlyMine = $event.detail.checked; filtrerSignalements()"
              color="primary"
            ></ion-toggle>
          </div>

          <!-- Tableau récapitulatif dépendant des filtres -->
          <div class="pt-3">
            <div class="max-h-64 overflow-y-auto border rounded-lg p-2 bg-gray-50">
              <TableauRecapitulatif :signalements="signalementsFiltres" />
            </div>
          </div>

          <div class="pt-2 border-t border-gray-100">
            <ion-button @click="reinitialiserFiltres" expand="block" fill="outline" class="mt-2">
              Réinitialiser
            </ion-button>
          </div>
        </div>
      </div>

      <!-- Légende -->
      <div class="absolute bottom-20 left-4 right-4 bg-white/90 backdrop-blur-sm rounded-xl shadow-lg p-4 z-10">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-gray-900">Signalements</h4>
          <span class="text-sm text-gray-600">{{ signalementsFiltres.length }} visible(s)</span>
        </div>
        <div class="grid grid-cols-2 gap-3">
          <div class="flex items-center">
            <div class="w-3 h-3 rounded-full bg-red-500 mr-2"></div>
            <span class="text-sm text-gray-700">En cours</span>
            <span class="ml-auto text-sm font-medium">{{ compteurEnCours }}</span>
          </div>
          <div class="flex items-center">
            <div class="w-3 h-3 rounded-full bg-green-500 mr-2"></div>
            <span class="text-sm text-gray-700">Résolu</span>
            <span class="ml-auto text-sm font-medium">{{ compteurResolus }}</span>
          </div>
        </div>
      </div>
    </ion-content>
    <TabBar active-tab="carte" />
  </ion-page>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, nextTick } from 'vue';
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
  IonToggle
} from '@ionic/vue';

// Import Leaflet
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Correction pour les icônes Leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

import { 
  locate,
  layers,
  add,
  warning,
  checkmarkCircle,
  filter,
  close
} from 'ionicons/icons';
import TableauRecapitulatif from '@/views/components/global/TableauRecapitulatif.vue';
import { collection, getDocs, query, orderBy } from 'firebase/firestore';
import { onAuthStateChanged } from 'firebase/auth';
import { db, auth } from '@/config/firebase';

// Configuration des icônes Leaflet
const DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// Icônes personnalisées pour nos signalements
const signalementIcon = (color: string) => L.divIcon({
  className: 'custom-marker',
  html: `
    <div class="relative">
      <svg width="32" height="32" viewBox="0 0 32 32">
        <circle cx="16" cy="16" r="14" fill="white" stroke="${color}" stroke-width="2"/>
        <circle cx="16" cy="16" r="8" fill="${color}"/>
      </svg>
    </div>
  `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32]
});

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
    IonToggle,
    TabBar,
    TableauRecapitulatif
  },
  
  setup() {
    const mapContainer = ref<HTMLElement>();
    let map: L.Map | null = null;
    let markers: L.Marker[] = [];
    const isMapInitialized = ref(false);

    // Position par défaut (Antananarivo, Madagascar)
    const defaultCenter: L.LatLngExpression = [-18.8792, 47.5079];
    const defaultZoom = 13;

    // Données des signalements (chargées depuis Firestore)
    const signalements = ref<any[]>([]);

    const showFiltres = ref(false);
    const filtres = ref({
      enCours: true,
      resolus: true,
      onlyMine: false
    });

    const currentUserId = ref<string | null>(auth.currentUser ? auth.currentUser.uid : null);

    const statuses = ref<Array<{ id?: number; status_code: string; label: string }>>([]);

    const signalementsFiltres = ref([...signalements.value]);

    const compteurEnCours = ref(0);
    const compteurResolus = ref(0);

    const isResolved = (status: string) => {
      return status === 'COMPLETED' || status === 'VERIFIED';
    };

    const statusLabel = (status: string) => {
      if (!status) return '';
      const found = statuses.value.find(s => s.status_code === status);
      if (found && found.label) return found.label;
      // fallback
      switch (status) {
        case 'SUBMITTED': return 'Soumis';
        case 'UNDER_REVIEW': return "En cours d'examen";
        case 'ASSIGNED': return 'Assigné à une entreprise';
        case 'IN_PROGRESS': return 'Travaux en cours';
        case 'COMPLETED': return 'Terminé';
        case 'CANCELLED': return 'Annulé';
        case 'VERIFIED': return 'Vérifié et validé';
        default: return status;
      }
    };

    const loadStatuses = async () => {
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
        statuses.value = items;
      } catch (err) {
        console.error('Erreur chargement statuses (map):', err);
      }
    };

    const calculerCompteurs = () => {
      compteurEnCours.value = signalementsFiltres.value.filter(s => !isResolved(s.status)).length;
      compteurResolus.value = signalementsFiltres.value.filter(s => isResolved(s.status)).length;
    };

    const filtrerSignalements = () => {
      signalementsFiltres.value = signalements.value.filter(signalement => {
        const resolved = isResolved(signalement.status);
        if (resolved && !filtres.value.resolus) return false;
        if (!resolved && !filtres.value.enCours) return false;
        // Filtrer par utilisateur connecté si demandé
        if (filtres.value.onlyMine) {
          const owner = signalement.raw && (signalement.raw.user_id ?? signalement.raw.userId ?? signalement.raw.user?.uid ?? null);
          if (!currentUserId.value || owner !== currentUserId.value) return false;
        }
        return true;
      });
      calculerCompteurs();
      updateMarkers();
    };

    const initialiserCarte = async () => {
      if (!mapContainer.value || map) return;

      // Attendre que le DOM soit prêt
      await nextTick();

      try {
        // Créer la carte
        map = L.map(mapContainer.value).setView(defaultCenter, defaultZoom);

        // Ajouter une couche de carte (OpenStreetMap)
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors',
          maxZoom: 19
        }).addTo(map);

        isMapInitialized.value = true;
        console.log('Carte initialisée avec succès');

        // Ajouter les marqueurs initiaux
        updateMarkers();
      } catch (err) {
        console.error('Erreur initialisation carte:', err);
      }
    };

    const statusColor = (status: string) => {
      switch (status) {
        case 'COMPLETED':
        case 'VERIFIED':
          return '#10b981'; // green
        case 'CANCELLED':
          return '#ef4444'; // red
        case 'IN_PROGRESS':
        case 'ASSIGNED':
        case 'UNDER_REVIEW':
          return '#f59e0b'; // yellow
        case 'SUBMITTED':
          return '#3b82f6'; // blue
        default:
          return '#ef4444';
      }
    };

    const updateMarkers = () => {
      // Vérifier que la carte est initialisée
      if (!map || !isMapInitialized.value) {
        console.log('Carte non initialisée, marqueurs non ajoutés');
        return;
      }

      // Supprimer les anciens marqueurs
      markers.forEach(marker => {
        try {
          if (map) map.removeLayer(marker);
        } catch (err) {
          console.warn('Erreur suppression marqueur:', err);
        }
      });
      markers = [];

      // Ajouter les nouveaux marqueurs
      signalementsFiltres.value.forEach(signalement => {
        if (!signalement.position || signalement.position.lat == null || signalement.position.lng == null) {
          console.log('Signalement sans position, ignoré:', signalement.id);
          return;
        }

        try {
          const iconColor = statusColor(signalement.status);
          const marker = L.marker([signalement.position.lat, signalement.position.lng], {
            icon: signalementIcon(iconColor)
          });

          // Préparer affichage company / budget / surface
          const company = signalement.company_name ?? (signalement.raw && (signalement.raw.company_name ?? signalement.raw.companyName)) ?? '—';
          let budgetDisplay = '—';
          if (signalement.budget != null) {
            try {
              budgetDisplay = typeof signalement.budget === 'number'
                ? new Intl.NumberFormat('fr-FR').format(signalement.budget) + ' Ar'
                : String(signalement.budget);
            } catch (e) {
              budgetDisplay = String(signalement.budget);
            }
          }
          let areaDisplay = '—';
          if (signalement.area != null) {
            try {
              areaDisplay = typeof signalement.area === 'number'
                ? new Intl.NumberFormat('fr-FR').format(signalement.area) + ' m²'
                : String(signalement.area) + ' m²';
            } catch (e) {
              areaDisplay = String(signalement.area) + ' m²';
            }
          }

          // Ajouter un popup
          const resolved = isResolved(signalement.status);
          marker.bindPopup(`
            <div class="p-2">
              <h3 class="font-bold text-lg mb-1">${signalement.titre}</h3>
              <p class="text-gray-600 text-sm mb-2">${signalement.description}</p>
              <div class="text-sm text-gray-700 mb-2"><strong>Surface:</strong> ${areaDisplay}</div>
              <div class="text-sm text-gray-700 mb-2"><strong>Entreprise:</strong> ${company}</div>
              <div class="text-sm text-gray-700 mb-2"><strong>Budget:</strong> ${budgetDisplay}</div>
              <div class="flex items-center justify-between">
                <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${resolved ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                  ${resolved ? '✓ ' + statusLabel(signalement.status) : statusLabel(signalement.status)}
                </span>
                <span class="text-xs text-gray-500">${signalement.date}</span>
              </div>
            </div>
          `);
          
          // Ouvrir le popup au survol (desktop) et le fermer au mouseout
          marker.on('mouseover', () => {
            try { marker.openPopup(); } catch (e) { /* no-op */ }
          });
          marker.on('mouseout', () => {
            try { marker.closePopup(); } catch (e) { /* no-op */ }
          });

          // Sur mobile/tactile, le clic ouvrira toujours le popup
          marker.on('click', () => {
            try { marker.openPopup(); } catch (e) { /* no-op */ }
          });

          if (map) {
            marker.addTo(map);
            markers.push(marker);
          }
        } catch (err) {
          console.error('Erreur ajout marqueur pour signalement', signalement.id, err);
        }
      });

      // Ajuster la vue pour montrer tous les marqueurs
      if (markers.length > 0 && map) {
        try {
          const group = L.featureGroup(markers);
          map.fitBounds(group.getBounds().pad(0.1));
        } catch (err) {
          console.warn('Erreur ajustement vue carte:', err);
        }
      }
    };

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
        console.warn('Erreur formatage date:', error);
        return '';
      }
    };

    const loadReports = async () => {
      try {
        // Ne pas trier par report_date si le champ peut être manquant
        const q = query(collection(db, 'reports'));
        const snap = await getDocs(q);
        const items = snap.docs.map(doc => {
          const d: any = doc.data();
          return {
            id: d.id ?? doc.id,
            titre: d.description ? (d.description.length > 50 ? d.description.slice(0, 50) + '...' : d.description) : `Signalement ${d.id ?? doc.id}`,
            description: d.description ?? '',
            status: d.status.statusCode ?? 'SUBMITTED',
            position: (d.latitude !== undefined && d.longitude !== undefined) ? { lat: d.latitude, lng: d.longitude } : null,
            date: formatDate(d.createdAt),
            budget: d.assignation.budget ?? null,
            company_name: d.assignation.company.name ?? null,
            area: d.area ?? null,
            raw: d
          };
        });
        
        // Trier en mémoire par date
        signalements.value = items.sort((a, b) => {
          if (!a.date && !b.date) return 0;
          if (!a.date) return 1;
          if (!b.date) return -1;
          return new Date(b.date).getTime() - new Date(a.date).getTime();
        });
        
        // initialiser le filtré
        signalementsFiltres.value = [...signalements.value];
        calculerCompteurs();
        
        console.log(`${signalements.value.length} signalement(s) chargé(s)`);
        
        // Mettre à jour les marqueurs seulement si la carte est initialisée
        if (isMapInitialized.value) {
          updateMarkers();
        }
      } catch (err) {
        console.error('Erreur chargement reports (map):', err);
      }
    };

    const centrerSurPosition = () => {
      if (navigator.geolocation && map) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            if (map) {
              map.setView([latitude, longitude], 15);
              
              // Ajouter un marqueur pour la position actuelle
              L.marker([latitude, longitude], {
                icon: L.divIcon({
                  className: 'current-location-marker',
                  html: `
                    <div class="relative">
                      <div class="w-6 h-6 bg-blue-600 rounded-full border-2 border-white shadow-lg"></div>
                      <div class="absolute inset-0 animate-ping bg-blue-400 rounded-full"></div>
                    </div>
                  `,
                  iconSize: [24, 24],
                  iconAnchor: [12, 12]
                })
              })
              .bindPopup('Votre position actuelle')
              .addTo(map);
            }
          },
          (error) => {
            console.error('Erreur de géolocalisation:', error);
            // Revenir à la vue par défaut
            if (map) map.setView(defaultCenter, defaultZoom);
          }
        );
      }
    };

    const changerTypeCarte = () => {
      if (!map) return;
      
      // Exemple simple : alterner entre différentes couches
      console.log('Changement de type de carte');
      // Dans une vraie implémentation, vous alterneriez entre différentes couches TileLayer
    };

    const nouveauSignalement = () => {
      if (map) {
        const center = map.getCenter();
        console.log('Nouveau signalement à:', center);
        // Ici, vous pourriez ouvrir un modal ou naviguer vers un formulaire
        // avec les coordonnées pré-remplies
      }
    };

    const toggleFiltres = () => {
      showFiltres.value = !showFiltres.value;
    };

    const reinitialiserFiltres = () => {
      filtres.value = {
        enCours: true,
        resolus: true,
        onlyMine: false
      };
      filtrerSignalements();
    };

    onMounted(async () => {
      // écouter l'auth pour récupérer l'uid courant
      try {
        onAuthStateChanged(auth, (user) => {
          currentUserId.value = user ? user.uid : null;
        });
      } catch (e) {
        console.warn('Impossible d\'écouter l\'état auth:', e);
      }
      
      // Charger d'abord les statuses et les reports
      try {
        await loadStatuses();
        await loadReports();
      } catch (err) {
        console.error('Erreur chargement données:', err);
      }
      
      // Ensuite initialiser la carte
      await initialiserCarte();
    });

    return {
      mapContainer,
      signalements,
      signalementsFiltres,
      showFiltres,
      filtres,
      currentUserId,
      compteurEnCours,
      compteurResolus,
      locate,
      layers,
      add,
      warning,
      checkmarkCircle,
      filter,
      close,
      centrerSurPosition,
      changerTypeCarte,
      nouveauSignalement,
      toggleFiltres,
      filtrerSignalements,
      reinitialiserFiltres
    };
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
  --border-radius: 50%;
}

/* Animation pour les filtres */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slideDown {
  animation: slideDown 0.3s ease-out;
}

/* Styles pour les marqueurs Leaflet */
:deep(.leaflet-marker-icon.custom-marker) {
  background: transparent !important;
  border: none !important;
}

:deep(.current-location-marker) {
  background: transparent !important;
  border: none !important;
}

:deep(.leaflet-popup-content) {
  margin: 0 !important;
  padding: 0 !important;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 12px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  border: 1px solid #e5e7eb !important;
}
</style>
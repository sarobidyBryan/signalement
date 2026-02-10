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
      <div v-if="showFiltres" class="absolute top-20 left-4 right-4 bg-white rounded-xl shadow-xl p-4 z-50 animate-slideDown filters-popup">
        <div class="flex items-center justify-between mb-4">
          <h4 class="font-bold text-gray-900">Filtrer les signalements</h4>
          <ion-button fill="clear" @click="toggleFiltres" class="p-0">
            <ion-icon :icon="close" class="text-gray-500"></ion-icon>
          </ion-button>
        </div>
        
          <div class="space-y-3">
            <div class="mb-2">
              <h5 class="text-sm font-medium text-gray-700 mb-2">Statuts</h5>
              <div class="flex flex-wrap gap-2">
                <ion-chip :outline="selectedStatus !== 'tous'" @click="selectAllStatuses">
                  <ion-label>Tous</ion-label>
                </ion-chip>
                <ion-chip
                  v-for="sf in statuses"
                  :key="sf.statusCode"
                  :outline="selectedStatus !== sf.statusCode"
                  @click="toggleStatus(sf.statusCode)"
                >
                  <ion-label>{{ sf.label }}</ion-label>
                </ion-chip>
              </div>
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
            <ion-button @click="reinitialiserFiltres" expand="block" fill="solid" class="mt-2 reset-btn">
              Réinitialiser
            </ion-button>
          </div>
        </div>
      </div>

      <!-- Légende (masquée si le panneau de filtres est ouvert) -->
      <div v-if="!showFiltres" class="absolute bottom-24 left-4 right-4 bg-white/90 backdrop-blur-sm rounded-xl shadow-lg z-10 legende-container">
        <div class="p-4 pb-2">
          <div class="flex items-center justify-between mb-3">
            <h4 class="font-bold text-gray-900">Signalements</h4>
            <span class="text-sm text-gray-600">{{ signalementsFiltres.length }} visible(s)</span>
          </div>
        </div>
        <div class="px-4 pb-4 max-h-40 overflow-y-auto">
          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="item in compteurParStatus"
              :key="item.statusCode"
              class="flex items-center"
            >
              <div :style="{ background: item.color }" class="w-3 h-3 rounded-full mr-2 flex-shrink-0"></div>
              <span class="text-sm text-gray-700 truncate">{{ item.label }}</span>
              <span class="ml-auto text-sm font-medium flex-shrink-0">{{ item.count }}</span>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
    <ImageGalleryModal v-model="galleryVisible" :images="galleryImages" :startIndex="galleryStartIndex" />
    <TabBar active-tab="carte" />
  </ion-page>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, nextTick, computed } from 'vue';
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
  IonToggle,
  IonChip,
  IonLabel
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
  close,
  image
} from 'ionicons/icons';
import TableauRecapitulatif from '@/views/components/global/TableauRecapitulatif.vue';
import ImageGalleryModal from '@/views/components/global/signalement/ImageGalleryModal.vue';
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
      IonChip,
      IonLabel,
    TabBar,
    TableauRecapitulatif,
    ImageGalleryModal
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
      // selectedStatuses: empty => all statuses
      selectedStatuses: [] as string[],
      onlyMine: false
    });

    const currentUserId = ref<string | null>(auth.currentUser ? auth.currentUser.uid : null);

    const statuses = ref<Array<{ id?: number; statusCode: string; label: string }>>([]);

    const signalementsFiltres = ref([...signalements.value]);

    const compteurEnCours = ref(0);
    const compteurResolus = ref(0);

    const compteurParStatus = computed(() => {
      return statuses.value.map(s => {
        const count = signalementsFiltres.value.filter(sig => sig.status === s.statusCode).length;
        return {
          statusCode: s.statusCode,
          label: s.label,
          color: statusColor(s.statusCode),
          count
        };
      });
    });

    const isResolved = (status: string) => {
      return status === 'COMPLETED' || status === 'VERIFIED';
    };

    // Gallery state
    const galleryVisible = ref(false);
    const galleryImages = ref<string[]>([]);
    const galleryStartIndex = ref(0);
    const pinnedMarkerId = ref<number | null>(null);

    const normalizeImagesArr = (imageArr: any[]) => {
      if (!imageArr || !Array.isArray(imageArr)) return [];
      return imageArr.map((it: any) => {
        if (!it) return '';
        if (typeof it === 'string') return it;
        return it.link ?? it.url ?? it.path ?? '';
      }).filter((u: string) => !!u);
    };

    const openGalleryFromSignalement = (signalement: any, start = 0) => {
      const imgs = normalizeImagesArr(signalement.raw?.imageReport ?? signalement.imageReport ?? []);
      if (!imgs || imgs.length === 0) {
        alert('Aucune photo pour ce signalement');
        return;
      }
      galleryImages.value = imgs;
      galleryStartIndex.value = start;
      galleryVisible.value = true;
    };

    const statusLabel = (status: string) => {
      if (!status) return '';
      const found = statuses.value.find(s => s.statusCode === status);
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
            statusCode: d.status_code ?? d.statusCode ?? d.code,
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
        // Filtrer par utilisateur si demandé
        if (filtres.value.onlyMine) {
          const owner = signalement.raw && (signalement.raw.user?.firebaseUid ?? null);
          if (!currentUserId.value || owner !== currentUserId.value) return false;
        }

        // Filtrer par status si des statuses sont sélectionnés
        const selected = filtres.value.selectedStatuses;
        if (selected && selected.length > 0) {
          return selected.includes(signalement.status);
        }

        return true;
      });
      calculerCompteurs();
      updateMarkers();
    };

    const isStatusSelected = (code: string) => {
      return filtres.value.selectedStatuses && filtres.value.selectedStatuses.includes(code);
    };

    const selectedStatus = computed(() => {
      return (filtres.value.selectedStatuses && filtres.value.selectedStatuses.length > 0)
        ? filtres.value.selectedStatuses[0]
        : 'tous';
    });

    const toggleStatus = (code: string) => {
      const arr = filtres.value.selectedStatuses || [];
      // Make selection exclusive: if clicking currently selected, clear; otherwise select only this one
      if (arr.length === 1 && arr[0] === code) {
        filtres.value.selectedStatuses = [];
      } else {
        filtres.value.selectedStatuses = [code];
      }
      filtrerSignalements();
    };

    const selectAllStatuses = () => {
      filtres.value.selectedStatuses = [];
      filtrerSignalements();
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
          const company = signalement.companyName ?? (signalement.raw && (signalement.raw.companyName ?? signalement.raw.companyName)) ?? '—';
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
            <div class="p-4 space-y-3" style="max-width: 300px;">
              <div>
                <h3 class="font-bold text-base text-gray-900 mb-1">${signalement.titre}</h3>
                <p class="text-gray-600 text-sm leading-relaxed">${signalement.description}</p>
              </div>
              
              <div class="border-t border-gray-200 pt-3">
                <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-2">Priorité</div>
                ${signalement.level != 0 ? `
                  <div class="flex items-center justify-between bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-lg p-3">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium text-amber-900">Niveau de priorité</span>
                    </div>
                    <div class="inline-flex items-center justify-center w-8 h-8 bg-gradient-to-br from-amber-400 to-orange-500 text-white rounded-lg text-sm font-bold shadow-sm">${signalement.level}</div>
                  </div>
                ` : `
                  <div class="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div class="w-2 h-2 bg-slate-400 rounded-full flex-shrink-0"></div>
                    <span class="text-sm text-slate-700 font-medium">Niveau en attente d'assignation</span>
                  </div>
                `}
              </div>
              
              <div class="grid grid-cols-2 gap-3 border-t border-gray-200 pt-3">
                <div>
                  <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Surface</div>
                  <div class="text-sm font-semibold text-gray-900">${areaDisplay}</div>
                </div>
                <div>
                  <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Budget</div>
                  <div class="text-sm font-semibold text-gray-900">${budgetDisplay}</div>
                </div>
              </div>
              
              <div class="border-t border-gray-200 pt-3">
                <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">Entreprise</div>
                <div class="text-sm font-medium text-gray-900">${company}</div>
              </div>
              
              <div class="flex items-center justify-between border-t border-gray-200 pt-3">
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${resolved ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'}">
                  ${resolved ? '✓ ' + statusLabel(signalement.status) : statusLabel(signalement.status)}
                </span>
                <span class="text-xs text-gray-500 font-medium">${signalement.date}</span>
              </div>
              
              <button id="view-photos-${signalement.id}" style="background:#000000;color:#ffffff;padding:10px 12px;border-radius:8px;font-size:13px;font-weight:600;border:none;width:100%;display:block;text-align:center;cursor:pointer;">Voir photos</button>
            </div>
          `);
          
          // Ouvrir le popup au survol (desktop)
          marker.on('mouseover', () => {
            try { marker.openPopup(); } catch (e) { /* no-op */ }
          });

          // Fermer le popup au mouseout seulement si il n'est pas "pinned" par un clic
          // Nous utilisons une variable locale stockée dans closure via pinnedMarkerId
          marker.on('mouseout', () => {
            try {
              if (pinnedMarkerId.value === signalement.id) return;
              marker.closePopup();
            } catch (e) { /* no-op */ }
          });

          // Sur mobile/tactile, le clic fixera (pinné) le popup pour permettre d'interagir avec son contenu
          marker.on('click', () => {
            try {
              pinnedMarkerId.value = signalement.id;
              marker.openPopup();
            } catch (e) { /* no-op */ }
          });

          // Quand le popup se ferme, on débloque le pinned state
          marker.on('popupclose', () => {
            try {
              if (pinnedMarkerId.value === signalement.id) pinnedMarkerId.value = null;
            } catch (e) { /* no-op */ }
          });

          if (map) {
            marker.addTo(map);
            markers.push(marker);

            // Attacher l'écouteur sur l'ouverture du popup pour le bouton 'Voir photos'
            marker.on('popupopen', () => {
              try {
                const el = document.getElementById(`view-photos-${signalement.id}`);
                if (el) {
                  // Remplacer onclick pour éviter doublons
                  (el as any).onclick = () => {
                    try { openGalleryFromSignalement(signalement); } catch (e) { console.warn(e); }
                  };
                }
              } catch (e) {
                console.warn('Erreur liaison bouton photos popup:', e);
              }
            });
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
          // Convertir les coordonnées en nombres si elles sont des chaînes
          let lat: any = d.latitude;
          let lng: any = d.longitude;
          if (typeof lat === 'string') lat = parseFloat(lat);
          if (typeof lng === 'string') lng = parseFloat(lng);
          const hasCoords = lat !== undefined && lng !== undefined && !isNaN(lat) && !isNaN(lng);

          return {
            id: d.id ?? doc.id,
            titre: d.description ? (d.description.length > 50 ? d.description.slice(0, 50) + '...' : d.description) : `Signalement ${d.id ?? doc.id}`,
            description: d.description ?? '',
            status: d.status?.statusCode ?? 'SUBMITTED',
            position: hasCoords ? { lat, lng } : null,
            date: formatDate(d.createdAt),
            budget: d.assignation?.budget ?? null,
            companyName: d.assignation?.company?.name ?? null,
            area: d.area ?? null,
            imageReport: d.imageReport ?? [],
            level: parseInt(d.level) || 0,
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
        selectedStatuses: [],
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
      statuses,
      currentUserId,
      compteurEnCours,
      compteurResolus,
      compteurParStatus,
      locate,
      layers,
      add,
      warning,
      checkmarkCircle,
      filter,
      close,
      image,
      centrerSurPosition,
      changerTypeCarte,
      nouveauSignalement,
      toggleFiltres,
      filtrerSignalements,
      reinitialiserFiltres
      ,selectedStatus,isStatusSelected,toggleStatus,selectAllStatuses
      ,galleryVisible,galleryImages,galleryStartIndex,openGalleryFromSignalement,normalizeImagesArr
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

/* Popup filtres scrollable */
:deep(.filters-popup) {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  padding-bottom: 96px; /* laisse de l'espace pour le TabBar afin que le bouton Réinitialiser soit cliquable */
}

/* Bouton Réinitialiser noir et blanc */
:deep(.reset-btn) {
  --background: #000000;
  --color: #ffffff;
  --border-radius: 12px;
  --padding-top: 10px;
  --padding-bottom: 10px;
}

/* Légende scrollable pour mobile */
.legende-container {
  max-height: calc(100vh - 200px);
}

/* Scrollbar pour la légende sur mobile */
.legende-container ::-webkit-scrollbar {
  width: 4px;
}

.legende-container ::-webkit-scrollbar-track {
  background: transparent;
}

.legende-container ::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

.legende-container ::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
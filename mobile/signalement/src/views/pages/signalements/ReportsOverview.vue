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
import { defineComponent, onMounted, ref, watch } from 'vue';
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
    TabBar
  },
  
  setup() {
    const mapContainer = ref<HTMLElement>();
    let map: L.Map | null = null;
    let markers: L.Marker[] = [];

    // Position par défaut (Paris)
    const defaultCenter: L.LatLngExpression = [48.8566, 2.3522];
    const defaultZoom = 13;

    // Données des signalements
    const signalements = ref([
      {
        id: 1,
        titre: 'Nid-de-poule',
        description: 'Nid-de-poule important sur la chaussée',
        status: 'en-cours',
        position: { lat: 48.8566, lng: 2.3522 },
        date: '2024-01-15',
        categorie: 'route'
      },
      {
        id: 2,
        titre: 'Éclairage public défectueux',
        description: 'Lampadaire ne fonctionnant plus',
        status: 'resolu',
        position: { lat: 48.8584, lng: 2.2945 },
        date: '2024-01-10',
        categorie: 'eclairage'
      },
      {
        id: 3,
        titre: 'Poubelle débordante',
        description: 'Poubelle publique pleine depuis plusieurs jours',
        status: 'en-cours',
        position: { lat: 48.8606, lng: 2.3376 },
        date: '2024-01-12',
        categorie: 'proprete'
      },
      {
        id: 4,
        titre: 'Graffiti',
        description: 'Graffiti sur mur public',
        status: 'en-cours',
        position: { lat: 48.8625, lng: 2.3193 },
        date: '2024-01-14',
        categorie: 'vandalisme'
      },
      {
        id: 5,
        titre: 'Feu tricolore HS',
        description: 'Feu tricolore clignotant en permanence',
        status: 'resolu',
        position: { lat: 48.8534, lng: 2.3488 },
        date: '2024-01-08',
        categorie: 'signalisation'
      }
    ]);

    const showFiltres = ref(false);
    const filtres = ref({
      enCours: true,
      resolus: true
    });

    const signalementsFiltres = ref([...signalements.value]);

    const compteurEnCours = ref(0);
    const compteurResolus = ref(0);

    const calculerCompteurs = () => {
      compteurEnCours.value = signalementsFiltres.value.filter(s => s.status === 'en-cours').length;
      compteurResolus.value = signalementsFiltres.value.filter(s => s.status === 'resolu').length;
    };

    const filtrerSignalements = () => {
      signalementsFiltres.value = signalements.value.filter(signalement => {
        if (signalement.status === 'en-cours' && !filtres.value.enCours) return false;
        if (signalement.status === 'resolu' && !filtres.value.resolus) return false;
        return true;
      });
      calculerCompteurs();
      updateMarkers();
    };

    const initialiserCarte = () => {
      if (!mapContainer.value || map) return;

      // Créer la carte
      map = L.map(mapContainer.value).setView(defaultCenter, defaultZoom);

      // Ajouter une couche de carte (OpenStreetMap)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(map);

      // Ajouter les marqueurs initiaux
      updateMarkers();
    };

    const updateMarkers = () => {
      // Supprimer les anciens marqueurs
      markers.forEach(marker => {
        if (map) map.removeLayer(marker);
      });
      markers = [];

      // Ajouter les nouveaux marqueurs
      signalementsFiltres.value.forEach(signalement => {
        const iconColor = signalement.status === 'resolu' ? '#10b981' : '#ef4444';
        const marker = L.marker([signalement.position.lat, signalement.position.lng], {
          icon: signalementIcon(iconColor)
        });

        // Ajouter un popup
        marker.bindPopup(`
          <div class="p-2">
            <h3 class="font-bold text-lg mb-1">${signalement.titre}</h3>
            <p class="text-gray-600 text-sm mb-2">${signalement.description}</p>
            <div class="flex items-center justify-between">
              <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                signalement.status === 'resolu' 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-red-100 text-red-800'
              }">
                ${signalement.status === 'resolu' ? '✓ Résolu' : 'En cours'}
              </span>
              <span class="text-xs text-gray-500">${signalement.date}</span>
            </div>
          </div>
        `);

        marker.addTo(map!);
        markers.push(marker);
      });

      // Ajuster la vue pour montrer tous les marqueurs
      if (markers.length > 0 && map) {
        const group = L.featureGroup(markers);
        map.fitBounds(group.getBounds().pad(0.1));
      }
    };

    const centrerSurPosition = () => {
      if (navigator.geolocation && map) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
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
      const currentLayer = map.getPane('tilePane');
      // Dans une vraie implémentation, vous alterneriez entre différentes couches TileLayer
      console.log('Changement de type de carte');
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
        resolus: true
      };
      filtrerSignalements();
    };

    onMounted(() => {
      initialiserCarte();
      calculerCompteurs();
    });

    return {
      mapContainer,
      signalements,
      signalementsFiltres,
      showFiltres,
      filtres,
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
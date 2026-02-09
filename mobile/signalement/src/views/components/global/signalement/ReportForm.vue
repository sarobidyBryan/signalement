<template>
  <div class="report-form-overlay">
    <div class="report-form-modal">
      <div class="modal-header">
        <h2>Nouveau Signalement</h2>
        <ion-button 
          fill="clear" 
          size="small" 
          @click="$emit('close')"
          class="close-btn"
          color="dark"
        >
          <ion-icon :icon="closeOutline"></ion-icon>
        </ion-button>
      </div>

      <div class="modal-content">
        <ion-list>
          <ion-item>
            <ion-label position="stacked">Surface endommagée (m²)</ion-label>
            <ion-input 
              type="number" 
              v-model.number="area" 
              placeholder="ex: 12.5"
              :disabled="isSubmitting"
            ></ion-input>
          </ion-item>

          <ion-item>
            <ion-label position="stacked">Coordonnées (latitude, longitude)</ion-label>
            <div class="coords">
              <ion-input 
                v-model="latitude" 
                placeholder="Latitude"
                :disabled="isSubmitting"
                readonly
              ></ion-input>
              <ion-input 
                v-model="longitude" 
                placeholder="Longitude"
                :disabled="isSubmitting"
                readonly
              ></ion-input>
            </div>
          </ion-item>

          <ion-item>
            <ion-label position="stacked">Description</ion-label>
            <ion-textarea 
              v-model="description" 
              rows="3" 
              placeholder="Décrivez les dégâts..."
              :disabled="isSubmitting"
            ></ion-textarea>
          </ion-item>
        </ion-list>

        <div class="map-container">
          <div id="report-map" class="report-map" aria-label="Carte de sélection"></div>
          <div class="map-instruction">
            <small>Cliquez sur la carte pour sélectionner l'emplacement</small>
          </div>
        </div>
      </div>

      <div class="modal-actions">
        <ion-button 
          expand="block" 
          fill="outline"
          color="dark" 
          @click="locateMe" 
          :disabled="isSubmitting"
        >
          <ion-icon :icon="navigateOutline" slot="start"></ion-icon>
          Localiser ma position
        </ion-button>
        
        <div class="action-buttons">
          <ion-button 
            fill="clear" 
            color="medium" 
            @click="$emit('close')"
            :disabled="isSubmitting"
          >
            Annuler
          </ion-button>
          <ion-button 
            expand="block" 
            color="dark" 
            @click="submit" 
            :disabled="isSubmitting"
          >
            <ion-spinner v-if="isSubmitting" name="crescent"></ion-spinner>
            <span v-else>Soumettre le signalement</span>
          </ion-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onBeforeUnmount, ref, defineExpose } from 'vue';
import { 
  IonList, IonItem, IonLabel, IonInput, IonTextarea, IonButton,
  IonIcon, IonSpinner 
} from '@ionic/vue';
import { closeOutline, navigateOutline } from 'ionicons/icons';
import { db } from '@/config/firebase';
import { collection, query, where, getDocs } from 'firebase/firestore';

export default defineComponent({
  name: 'ReportFormModal',
  components: { 
    IonList, IonItem, IonLabel, IonInput, IonTextarea, IonButton,
    IonIcon, IonSpinner 
  },

  emits: ['submit', 'close'],

  setup(_, { emit }) {
    const area = ref<number | null>(null);
    const latitude = ref<number | string>('');
    const longitude = ref<number | string>('');
    const description = ref('');
    const isSubmitting = ref(false);

    let map: any = null;
    let marker: any = null;
    let L: any = null;

    const initMap = async () => {
      // Attendre que le DOM soit mis à jour pour le modal
      await new Promise(resolve => setTimeout(resolve, 50));
      
      // Injecter Leaflet CSS si non présent
      if (!document.querySelector("link[data-leaflet]")) {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
        link.setAttribute('data-leaflet', '1');
        document.head.appendChild(link);
      }

      const mod = await import('leaflet');
      L = (mod as any).default || mod;

      // Créer la carte
      map = L.map('report-map', { dragging: true, tap: false }).setView([-18.8792, 47.5079], 12);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap'
      }).addTo(map);

      map.on('click', (e: any) => {
        if (isSubmitting.value) return;
        const { lat, lng } = e.latlng;
        setCoords(lat, lng);
      });

      // Redimensionner la carte après initialisation
      setTimeout(() => {
        if (map) map.invalidateSize();
      }, 100);
    };

    const setCoords = (lat: number, lng: number) => {
      latitude.value = +lat.toFixed(6);
      longitude.value = +lng.toFixed(6);
      if (!L || !map) return;
      if (!marker) {
        marker = L.marker([lat, lng]).addTo(map);
      } else {
        marker.setLatLng([lat, lng]);
      }
      map.panTo([lat, lng]);
    };

    const locateMe = () => {
      if (!navigator.geolocation || isSubmitting.value) return;
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const { latitude: lat, longitude: lng } = pos.coords;
          setCoords(lat, lng);
        },
        (err) => console.error('Erreur de géolocalisation', err),
        { enableHighAccuracy: true }
      );
    };

    const submit = async () => {
      // Validation côté client
      if (!area.value || !latitude.value || !longitude.value) {
        alert('Veuillez renseigner la surface et sélectionner une position sur la carte.');
        return;
      }
      
      isSubmitting.value = true;
      
      const payload = {
        area: Number(area.value),
        latitude: Number(latitude.value),
        longitude: Number(longitude.value),
        description: description.value || '',
        status: await getStatus("SUBMITTED"),
        user : JSON.parse(localStorage.getItem('user')!) || null ,
        createdAt : new Date()
      };

      // Émettre l'événement au parent
      emit('submit', payload);
      
      // Note: Ne pas réinitialiser isSubmitting ici, le parent doit le faire
      // après avoir traité la soumission
    };

    const getStatus = async (statusCode: string) => {
      try {
        // Essayer le nouveau champ `statusCode` puis tomber en backfill sur `status_code`
        let querySnapshot = await getDocs(query(collection(db, 'status'), where('statusCode', '==', statusCode)));
        if (querySnapshot.empty) {
          querySnapshot = await getDocs(query(collection(db, 'status'), where('status_code', '==', statusCode)));
        }
        if (!querySnapshot.empty) {
          const doc = querySnapshot.docs[0];
          return { id: doc.id, ...doc.data() };
        }
        console.warn(`Aucun statut trouvé pour le code: ${statusCode}`);
        return null;
      } catch (error) {
        console.error('Erreur lors de la récupération du statut:', error);
        return null;
      }
    };

    // Réinitialiser le formulaire
    const resetForm = () => {
      area.value = null;
      latitude.value = '';
      longitude.value = '';
      description.value = '';
      isSubmitting.value = false;
      
      if (marker && L) {
        marker.remove();
        marker = null;
      }
      if (map) {
        map.setView([-18.8792, 47.5079], 12);
      }
    };

    onMounted(() => {
      initMap().catch((e) => console.error('Erreur initialisation carte', e));
    });

    onBeforeUnmount(() => {
      if (map && map.remove) map.remove();
      map = null;
      marker = null;
    });

    defineExpose({ resetForm });

    return {
      area,
      latitude,
      longitude,
      description,
      isSubmitting,
      closeOutline,
      navigateOutline,
      locateMe,
      submit,
      resetForm
    };
  }
});
</script>

<style scoped>
/* Overlay qui couvre tout l'écran */
.report-form-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  padding: 16px;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease-out;
}

/* Modal centré */
.report-form-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  overflow: hidden;
}

/* Header du modal */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--ion-color-light);
  flex-shrink: 0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.close-btn {
  --padding-start: 8px;
  --padding-end: 8px;
  margin: -8px;
}

/* Contenu scrollable */
.modal-content {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  max-height: 60vh;
}

/* Carte */
.map-container {
  margin-top: 20px;
}

.report-map {
  height: 250px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.map-instruction {
  text-align: center;
  margin-top: 8px;
  color: var(--ion-color-medium);
}

.coords {
  display: flex;
  gap: 8px;
  width: 100%;
}

.coords ion-input {
  flex: 1;
}

/* Actions */
.modal-actions {
  padding: 16px 20px 20px;
  border-top: 1px solid var(--ion-color-light);
  display: grid;
  gap: 16px;
  flex-shrink: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.action-buttons ion-button {
  flex: 1;
  max-width: 120px;
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Responsive */
@media (max-width: 576px) {
  .report-form-overlay {
    padding: 8px;
  }
  
  .report-form-modal {
    max-height: 95vh;
  }
  
  .modal-content {
    padding: 16px;
  }
  
  .report-map {
    height: 200px;
  }
  
  .action-buttons {
    flex-direction: column-reverse;
  }
  
  .action-buttons ion-button {
    max-width: 100%;
  }
}
</style>
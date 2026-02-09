<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button fill="clear" @click="goBack" aria-label="Retour" color="dark">
            <ion-icon :icon="arrowBack"></ion-icon>
          </ion-button>
        </ion-buttons>
        <ion-title class="font-bold text-black">Modifier Mes Informations</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="bg-gray-50">
      <div class="px-4 py-6">
        <div class="bg-white rounded-2xl shadow-sm p-6">
          <ion-list>
            <ion-item>
              <ion-label position="stacked">Nom complet</ion-label>
              <ion-input v-model="form.name" placeholder="Votre nom"></ion-input>
            </ion-item>
          </ion-list>
        </div>

        <!-- Actions -->
        <div class="mt-8 space-y-3">
          <ion-button 
            expand="block" 
            color="dark"
            @click="sauvegarder"
            :disabled="isSubmitting"
          >
            <ion-spinner v-if="isSubmitting" name="crescent" class="mr-2"></ion-spinner>
            {{ isSubmitting ? 'Sauvegarde...' : 'Sauvegarder les modifications' }}
          </ion-button>
          <ion-button 
            expand="block" 
            fill="outline"
            @click="goBack"
            color="dark"
          >
            Annuler
          </ion-button>
        </div>

        <!-- Success message -->
        <div v-if="successMessage" class="mt-4 bg-green-50 border border-green-200 rounded-xl p-4 text-green-700 text-center">
          {{ successMessage }}
        </div>

        <!-- Error message -->
        <div v-if="errorMessage" class="mt-4 bg-red-50 border border-red-200 rounded-xl p-4 text-red-700 text-center">
          {{ errorMessage }}
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
  IonButtons,
  IonIcon,
  IonList,
  IonItem,
  IonLabel,
  IonInput,
  IonTextarea,
  IonSpinner
} from '@ionic/vue';
import {
  arrowBack
} from 'ionicons/icons';

import { db } from '@/config/firebase';
import { doc, getDoc, updateDoc } from 'firebase/firestore';

interface FormData {
  name: string;
  email: string;
  phone: string;
  address: string;
}

export default defineComponent({
  name: 'PersonalDataUpdate',

  components: {
    IonPage,
    IonHeader,
    IonContent,
    IonTitle,
    IonToolbar,
    IonButton,
    IonButtons,
    IonIcon,
    IonList,
    IonItem,
    IonLabel,
    IonInput,
    IonTextarea,
    IonSpinner
  },

  data() {
    return {
      form: {
        name: '',
      } as FormData,
      isSubmitting: false,
      successMessage: '',
      errorMessage: '',
      arrowBack
    };
  },

  methods: {
    handleStorageChange(event: StorageEvent) {
      // Vérifier si c'est le localStorage 'user' qui a changé
      if (event.key === 'user' && event.newValue) {
        try {
          const parsedUser = JSON.parse(event.newValue);
          this.form.name = parsedUser.name || this.form.name;
          console.log('Formulaire mis à jour depuis localStorage');
        } catch (e) {
          console.error('Erreur parsing user localStorage:', e);
        }
      }
    },

    handleUserUpdated(event: any) {
      // Gérer l'événement personnalisé déclenché après mise à jour du localStorage
      if (event.detail) {
        try {
          this.form.name = event.detail.name || this.form.name;
          this.form.phone = event.detail.phone || this.form.phone;
          this.form.address = event.detail.address || this.form.address;
          console.log('Formulaire mis à jour depuis événement personnalisé');
        } catch (e) {
          console.error('Erreur lors de la mise à jour depuis événement:', e);
        }
      }
    },

    async chargerDonneesUtilisateur() {
      try {
        const uid = localStorage.getItem('uid');
        if (!uid) {
          console.warn('Aucun UID trouvé dans localStorage');
          return;
        }

        const userRef = doc(db, 'users', uid);
        const userSnap = await getDoc(userRef);

        if (userSnap.exists()) {
          const userData = userSnap.data() as any;
          this.form.name = userData.name || '';
        }
      } catch (error) {
        console.error('Erreur lors du chargement des données:', error);
        this.errorMessage = 'Erreur lors du chargement des données';
      }
    },

    async sauvegarder() {
      // Validation simple
      if (!this.form.name.trim()) {
        this.errorMessage = 'Le nom est obligatoire';
        this.successMessage = '';
        return;
      }

      this.isSubmitting = true;
      this.errorMessage = '';
      this.successMessage = '';

      try {
        const uid = localStorage.getItem('uid');
        if (!uid) {
          throw new Error('UID non trouvé');
        }

        const userRef = doc(db, 'users', uid);
        await updateDoc(userRef, {
          name: this.form.name.trim(),
          updatedAt: new Date()
        });

        const userDoc = await getDoc(doc(db, "users", uid));
        const userData = userDoc.data();
        localStorage.setItem("user", JSON.stringify(userData));

        // Déclencher un événement personnalisé pour notifier les autres composants
        window.dispatchEvent(new CustomEvent('userStorageUpdated', { 
          detail: userData 
        }));

        this.successMessage = 'Informations mises à jour avec succès!';

        // Redirect after success
        setTimeout(() => {
          this.$router.push('/personal-data');
        }, 1500);
      } catch (error) {
        console.error('Erreur lors de la sauvegarde:', error);
        this.errorMessage = 'Erreur lors de la sauvegarde. Veuillez réessayer.';
      } finally {
        this.isSubmitting = false;
      }
    },

    goBack() {
      this.$router.push('/personal-data');
    }
  },

  mounted() {
    this.chargerDonneesUtilisateur();

    // Écouter les changements du localStorage depuis d'autres onglets
    window.addEventListener('storage', this.handleStorageChange);

    // Écouter l'événement personnalisé pour les mises à jour dans le même onglet
    window.addEventListener('userStorageUpdated', this.handleUserUpdated);
  },

  beforeUnmount() {
    // Nettoyer les écouteurs d'événements
    window.removeEventListener('storage', this.handleStorageChange);
    window.removeEventListener('userStorageUpdated', this.handleUserUpdated);
  }
});
</script>

<style scoped>
:deep(.custom-save-btn) {
  --background: #000000;
  --color: #ffffff;
  --border-radius: 12px;
}

:deep(ion-item) {
  --padding-start: 0;
  --padding-end: 0;
  --inner-padding-end: 0;
}

:deep(.ion-input-wrapper) {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}
</style>

<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button fill="clear" @click="goBack" aria-label="Retour" color="dark">
            <ion-icon :icon="arrowBack"></ion-icon>
          </ion-button>
        </ion-buttons>
        <ion-title class="font-bold text-black">Informations Personnelles</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="bg-gray-50">
      <div class="px-4 py-6">
        <!-- Profil utilisateur -->
        <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
          <div class="flex items-center space-x-4 mb-6">
            <div class="w-16 h-16 bg-black rounded-full flex items-center justify-center">
              <ion-icon :icon="person" class="text-white text-2xl"></ion-icon>
            </div>
            <div class="flex-1">
              <h2 class="text-xl font-bold text-gray-900">{{ utilisateur.name }}</h2>
              <p class="text-gray-600">{{ utilisateur.email }}</p>
            </div>
          </div>

          <!-- Information details -->
          <div v-if="isLoading" class="loading-container">
            <ion-spinner name="crescent" class="mb-2"></ion-spinner>
            <div class="loading-text">Chargement des données…</div>
          </div>
          <template v-else>
            <div class="space-y-4">
              <div class="bg-gray-50 rounded-xl p-4">
                <h3 class="text-sm font-medium text-gray-600 mb-1">Nom</h3>
                <p class="text-base text-gray-900">{{ utilisateur.name || '-' }}</p>
              </div>
              <div class="bg-gray-50 rounded-xl p-4">
                <h3 class="text-sm font-medium text-gray-600 mb-1">Email</h3>
                <p class="text-base text-gray-900">{{ utilisateur.email || '-' }}</p>
              </div>
              <div class="bg-gray-50 rounded-xl p-4">
                <h3 class="text-sm font-medium text-gray-600 mb-1">Date d'inscription</h3>
                <p class="text-base text-gray-900">{{ formattedRegistrationDate }}</p>
              </div>
              <div class="bg-gray-50 rounded-xl p-4">
                <h3 class="text-sm font-medium text-gray-600 mb-1">Token FCM</h3>
                <div v-if="fcmToken" class="flex items-center justify-between gap-2">
                  <p class="text-xs text-gray-900 break-all flex-1 font-mono">{{ fcmToken }}</p>
                  <button 
                    @click="copyTokenToClipboard"
                    class="flex-shrink-0 p-2 hover:bg-gray-200 rounded-lg transition-colors"
                    :title="tokenCopied ? 'Copié!' : 'Copier'"
                  >
                    <ion-icon 
                      :icon="tokenCopied ? checkmarkDone : copy" 
                      :style="{color: tokenCopied ? '#22c55e' : '#6b7280'}"
                      class="text-lg"
                    ></ion-icon>
                  </button>
                </div>
                <p v-else class="text-base text-gray-500 italic">Aucun token enregistré</p>
              </div>
            </div>
          </template>
        </div>

        <!-- Bouton modifier -->
        <div class="mt-8">
          <ion-button 
            @click="navigateToUpdate" 
            expand="block" 
            color="dark"
          >
            <ion-icon slot="start" :icon="pencil"></ion-icon>
            Modifier mes informations
          </ion-button>
        </div>

        <!-- Version -->
        <div class="text-center mt-8 text-gray-500 text-sm">
          Version 1.0.0
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
  IonSpinner
} from '@ionic/vue';
import {
  person,
  arrowBack,
  pencil,
  copy,
  checkmarkDone
} from 'ionicons/icons';

import { db } from '@/config/firebase';
import { doc, getDoc } from 'firebase/firestore';

interface UserData {
  name?: string;
  email?: string;
  createdAt?: any;
}

export default defineComponent({
  name: 'PersonalData',

  components: {
    IonPage,
    IonHeader,
    IonContent,
    IonTitle,
    IonToolbar,
    IonButton,
    IonButtons,
    IonIcon,
    IonSpinner
  },

  data() {
    return {
      utilisateur: {
        name: '[Utilisateur]',
        email: '[Email]',
      } as UserData,
      isLoading: false,
      registrationDate: null as any,
      fcmToken: '',
      tokenCopied: false,
      person,
      arrowBack,
      pencil,
      copy,
      checkmarkDone
    };
  },

  computed: {
    formattedRegistrationDate() {
      if (!this.registrationDate) return '-';
      const dateObj = this.registrationDate.toDate ? this.registrationDate.toDate() : (this.registrationDate instanceof Date ? this.registrationDate : new Date(this.registrationDate));
      return dateObj.toLocaleString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
  },

  methods: {
    handleStorageChange(event: StorageEvent) {
      // Vérifier si c'est le localStorage 'user' qui a changé
      if (event.key === 'user' && event.newValue) {
        try {
          const parsedUser = JSON.parse(event.newValue);
          this.utilisateur.name = parsedUser.name || this.utilisateur.name;
          this.utilisateur.email = parsedUser.email || this.utilisateur.email;
          console.log('Données utilisateur mises à jour depuis localStorage');
        } catch (e) {
          console.error('Erreur parsing user localStorage:', e);
        }
      }
    },

    handleUserUpdated(event: any) {
      // Gérer l'événement personnalisé déclenché après mise à jour du localStorage
      if (event.detail) {
        try {
          this.utilisateur.name = event.detail.name || this.utilisateur.name;
          this.utilisateur.email = event.detail.email || this.utilisateur.email;
          console.log('Données utilisateur mises à jour depuis événement personnalisé');
        } catch (e) {
          console.error('Erreur lors de la mise à jour depuis événement:', e);
        }
      }
    },

    async chargerDonneesUtilisateur() {
      this.isLoading = true;
      try {
        const uid = localStorage.getItem('uid');
        if (!uid) {
          console.warn('Aucun UID trouvé dans localStorage');
          this.isLoading = false;
          return;
        }

        const userRef = doc(db, 'users', uid);
        const userSnap = await getDoc(userRef);

        if (userSnap.exists()) {
          const userData = userSnap.data() as UserData;
          if (userData.name) this.utilisateur.name = userData.name;
          if (userData.email) this.utilisateur.email = userData.email;
          if (userData.createdAt) this.registrationDate = userData.createdAt;
        } else {
          console.warn('Document utilisateur non trouvé');
        }
      } catch (error) {
        console.error('Erreur lors du chargement des données utilisateur:', error);
      } finally {
        this.isLoading = false;
      }
    },

    async chargerToken() {
      try {
        const uid = localStorage.getItem('uid');
        if (!uid) {
          console.warn('Aucun UID trouvé dans localStorage');
          return;
        }

        const tokenRef = doc(db, 'userTokens', uid);
        const tokenSnap = await getDoc(tokenRef);

        if (tokenSnap.exists()) {
          const tokenData = tokenSnap.data();
          if (tokenData.fcmToken) {
            this.fcmToken = tokenData.fcmToken;
            console.log('Token FCM chargé avec succès');
          } else {
            console.warn('Aucun token FCM trouvé dans le document');
          }
        } else {
          console.warn('Document userTokens non trouvé');
        }
      } catch (error) {
        console.error('Erreur lors du chargement du token FCM:', error);
      }
    },

    async copyTokenToClipboard() {
      if (!this.fcmToken) return;
      try {
        await navigator.clipboard.writeText(this.fcmToken);
        this.tokenCopied = true;
        console.log('Token copié au presse-papiers');
        
        // Réinitialiser l'état après 2 secondes
        setTimeout(() => {
          this.tokenCopied = false;
        }, 2000);
      } catch (error) {
        console.error('Erreur lors de la copie du token:', error);
      }
    },

    navigateToUpdate() {
      this.$router.push('/personal-data/update');
    },

    goBack() {
      this.$router.push('/menu');
    }
  },

  mounted() {
    this.chargerDonneesUtilisateur();
    this.chargerToken();

    // Fallback: load from localStorage
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const parsedUser = JSON.parse(savedUser);
        this.utilisateur.name = parsedUser.name || this.utilisateur.name;
        this.utilisateur.email = parsedUser.email || this.utilisateur.email;
      } catch (e) {
        console.error('Erreur parsing user localStorage:', e);
      }
    }

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
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.loading-text {
  margin-top: 6px;
  color: #6b7280;
  font-size: 0.95rem;
}

:deep(.custom-edit-btn) {
  --background: #000000;
  --color: #ffffff;
  --border-radius: 12px;
}
</style>

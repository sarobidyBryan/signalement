<script lang="ts">
import { defineComponent } from 'vue';
import { auth, db } from '../../../config/firebase';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';
import { lockClosed, mail, person } from 'ionicons/icons';
import { 
  IonPage, 
  IonContent, 
  IonButton,
  IonInput,
  IonGrid,
  IonRow,
  IonCol,
  IonIcon
} from '@ionic/vue';
import { useRouter } from 'vue-router';
import { doc, setDoc } from 'firebase/firestore';

export default defineComponent({
  name: 'RegisterPage',
  
  components: {
    IonPage,
    IonContent,
    IonInput,
    IonButton,
    IonGrid,
    IonRow,
    IonCol,
    IonIcon
  },
  
  setup() {
    const router = useRouter();
    return {
      router,
      lockClosed,
      mail,
      person
    };
  },
  
  data() {
    return {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      errorMessage: '',
      isLoading: false
    };
  },
  
  methods: {
    async handleRegister() {
      // Réinitialiser les erreurs
      this.errorMessage = '';
      
      // Validation
      if (!this.name || !this.email || !this.password || !this.confirmPassword) {
        this.errorMessage = 'Veuillez remplir tous les champs';
        return;
      }
      
      if (this.password !== this.confirmPassword) {
        this.errorMessage = 'Les mots de passe ne correspondent pas';
        return;
      }
      
      if (this.password.length < 6) {
        this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
        return;
      }
      
      // Validation email basique
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.email)) {
        this.errorMessage = 'Veuillez entrer une adresse email valide';
        return;
      }
      
      this.isLoading = true;
      
      try {
        // Créer l'utilisateur avec email et mot de passe
        const userCredential = await createUserWithEmailAndPassword(
          auth, 
          this.email, 
          this.password
        );
        
        // Mettre à jour le profil avec le nom
        await updateProfile(userCredential.user, {
          displayName: this.name
        });

        await setDoc(doc(db,"users",userCredential.user.uid),{
            name: this.name,
            email: this.email,
            created_at: new Date(),
            status: "ACTIVE"
        });
        
        // Redirection vers la page d'accueil
        this.router.push('/home');
        
      } catch (error: any) {
        console.error('Erreur d\'inscription:', error);
        
        // Messages d'erreur spécifiques Firebase
        switch (error.code) {
          case 'auth/email-already-in-use':
            this.errorMessage = 'Cette adresse email est déjà utilisée';
            break;
          case 'auth/invalid-email':
            this.errorMessage = 'Adresse email invalide';
            break;
          case 'auth/operation-not-allowed':
            this.errorMessage = 'L\'inscription n\'est pas autorisée pour le moment';
            break;
          case 'auth/weak-password':
            this.errorMessage = 'Le mot de passe est trop faible';
            break;
          default:
            this.errorMessage = 'Une erreur est survenue lors de l\'inscription';
        }
      } finally {
        this.isLoading = false;
      }
    },
    
    goToLogin() {
      this.router.push('/login');
    }
  }
});
</script>

<template>
  <ion-page>
    <ion-content :fullscreen="true" class="ion-padding">
      <ion-grid class="h-full">
        <ion-row class="ion-align-items-center ion-justify-content-center h-full">
          <ion-col size="12" size-md="6" size-lg="4">
            <!-- Logo / Titre -->
            <div class="text-center mb-8">
              <div class="h-16 bg-black flex items-center justify-center mx-auto mb-4 rounded-lg">
                <p class="text-2xl font-bold text-white">Signalements</p>
              </div>
              <h1 class="text-3xl font-bold text-gray-900 mb-2">Créer un compte</h1>
              <p class="text-gray-600">Rejoignez notre plateforme</p>
            </div>

            <!-- Message d'erreur -->
            <div v-if="errorMessage" class="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">
              {{ errorMessage }}
            </div>

            <!-- Formulaire d'inscription -->
            <form class="space-y-6" @submit.prevent="handleRegister">
              <!-- Nom complet -->
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Nom complet</label>
                <div class="relative">
                  <ion-icon 
                    :icon="person" 
                    class="absolute left-3 top-1/3 transform -translate-y-1/2 text-gray-400 z-10"
                  ></ion-icon>
                  <ion-input
                    v-model="name"
                    type="text"
                    placeholder="Votre nom complet"
                    class="pl-10"
                    fill="solid"
                    :class="{ 'border-red-500': errorMessage && !name }"
                    required
                  ></ion-input>
                </div>
              </div>

              <!-- Email -->
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Email</label>
                <div class="relative">
                  <ion-icon 
                    :icon="mail" 
                    class="absolute left-3 top-1/3 transform -translate-y-1/2 text-gray-400 z-10"
                  ></ion-icon>
                  <ion-input
                    v-model="email"
                    type="email"
                    placeholder="votre@email.com"
                    class="pl-10"
                    fill="solid"
                    :class="{ 'border-red-500': errorMessage && !email }"
                    required
                  ></ion-input>
                </div>
              </div>

              <!-- Mot de passe -->
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Mot de passe</label>
                <div class="relative">
                  <ion-icon 
                    :icon="lockClosed" 
                    class="absolute left-3 top-1/3 transform -translate-y-1/2 text-gray-400 z-10"
                  ></ion-icon>
                  <ion-input
                    v-model="password"
                    type="password"
                    placeholder="••••••••"
                    class="pl-10"
                    fill="solid"
                    :class="{ 'border-red-500': errorMessage && (!password || password.length < 6) }"
                    required
                  ></ion-input>
                </div>
                <p class="text-xs text-gray-500 mt-1">Minimum 6 caractères</p>
              </div>

              <!-- Confirmation du mot de passe -->
              <div class="space-y-2">
                <label class="text-sm font-medium text-gray-700">Confirmer le mot de passe</label>
                <div class="relative">
                  <ion-icon 
                    :icon="lockClosed" 
                    class="absolute left-3 top-1/3 transform -translate-y-1/2 text-gray-400 z-10"
                  ></ion-icon>
                  <ion-input
                    v-model="confirmPassword"
                    type="password"
                    placeholder="••••••••"
                    class="pl-10"
                    fill="solid"
                    :class="{ 'border-red-500': errorMessage && (!confirmPassword || password !== confirmPassword) }"
                    required
                  ></ion-input>
                </div>
              </div>

              <!-- Bouton d'inscription -->
              <ion-button
                type="submit"
                expand="block"
                class="font-medium h-12"
                color="dark"
                :disabled="isLoading"
              >
                <span v-if="!isLoading">S'inscrire</span>
                <span v-else class="flex items-center justify-center">
                  <svg class="animate-spin h-5 w-5 mr-2 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Création du compte...
                </span>
              </ion-button>

              <!-- Lien vers la connexion -->
              <div class="text-center mt-8">
                <p class="text-gray-600">
                  Déjà un compte? 
                  <a 
                    href="#" 
                    @click.prevent="goToLogin" 
                    class="font-medium text-black hover:underline"
                  >
                    Se connecter
                  </a>
                </p>
              </div>

              <!-- Conditions d'utilisation -->
              <div class="text-center mt-4">
                <p class="text-xs text-gray-500">
                  En vous inscrivant, vous acceptez nos 
                  <a href="#" class="text-gray-700 hover:text-black">Conditions d'utilisation</a>
                  et notre 
                  <a href="#" class="text-gray-700 hover:text-black">Politique de confidentialité</a>
                </p>
              </div>
            </form>
          </ion-col>
        </ion-row>
      </ion-grid>
    </ion-content>
  </ion-page>
</template>

<style scoped>
/* Ajuster la hauteur */
.h-full {
  height: 100%;
}

/* Styles spécifiques pour Ionic dans Vue 3 */
:deep(ion-input) {
  --padding-start: 40px !important;
  --padding-end: 16px !important;
  --padding-top: 12px !important;
  --padding-bottom: 12px !important;
  --border-radius: 12px !important;
}

:deep(.ion-color-dark) {
  --ion-color-base: #000000 !important;
  --ion-color-base-rgb: 0,0,0 !important;
}

:deep(ion-button[disabled]) {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Pour le mode sombre d'Ionic si nécessaire */
@media (prefers-color-scheme: dark) {
  :deep(ion-input) {
    --background: #2a2a2a;
    --color: #ffffff;
  }
  
  .text-gray-900 {
    color: #ffffff;
  }
  
  .text-gray-600 {
    color: #a0a0a0;
  }
  
  .text-gray-500 {
    color: #888888;
  }
}

/* Animation du spinner */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
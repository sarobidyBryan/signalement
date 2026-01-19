<script lang="ts">
import { defineComponent } from 'vue';
import { auth,db } from '../../../config/firebase';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { lockClosed, mail } from 'ionicons/icons';
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
import { doc, getDoc, collection, query, where, getDocs, setDoc, updateDoc, increment, deleteDoc } from 'firebase/firestore';

export default defineComponent({
  name: 'LoginPage',
  
  components: {
    IonPage,
    IonContent,
    IonInput,
    IonButton,
    IonGrid,
    IonRow,
    IonCol,
    IonIcon // Ajouté
  },
  
  data() {
    return {
      email: '',
      password: '',
      errorMessage: ''
    };
  },
  
  setup() {
    return {
      // Exposer les icônes au template
      lockClosed,
      mail
    };
  },
  
  methods: {
    async handleLogin() {
      // Validation basique
      if (!this.email || !this.password) {
        this.errorMessage = 'Veuillez remplir tous les champs';
        return;
      }
      
      try {
        const userCred = await signInWithEmailAndPassword(auth, this.email, this.password);
        const uid = userCred.user.uid;

        // 2. Récupération des données utilisateur depuis Firestore
        const userDoc = await getDoc(doc(db, "users", uid));
        
        // 3. Vérification si le document existe
        if (userDoc.exists()) {
          const userData = userDoc.data();
          console.log("Données utilisateur:", userData);

          // 4. Vérification du statut
          if (userData.status === 'ACTIVE') {
            // Réinitialiser les tentatives de mot de passe après connexion réussie
            try {
              const attemptsRef = doc(db, 'password_attempts', uid);
              const attemptsSnap = await getDoc(attemptsRef);
              if (attemptsSnap.exists()) {
                await updateDoc(attemptsRef, { nb_attempt: 0 });
              } else {
                await setDoc(attemptsRef, { nb_attempt: 0, user_id: uid });
              }
            } catch (e) {
              console.warn('Impossible de réinitialiser password_attempts:', e);
            }

            this.$router.push('/map');
          } else {
            console.log("Cet utilisateur est suspendu");
            alert("Votre compte est suspendu. Contactez l'administrateur.");
          }
        } else {
          console.log("Aucun document utilisateur trouvé");
          // Gérer le cas où le document n'existe pas
        }
      } catch (error: any) {
        console.error('Erreur de connexion:', error);
        
        // Messages d'erreur plus précis
        switch (error.code) {
          case 'auth/invalid-email':
            this.errorMessage = 'Email invalide';
            break;
          case 'auth/user-disabled':
            this.errorMessage = 'Ce compte a été désactivé';
            break;
          case 'auth/invalid-credential':
          case 'auth/wrong-password':
            // Si l'email existe côté application (collection users), incrémenter les tentatives
            try {
              const usersQ = query(collection(db, 'users'), where('email', '==', this.email));
              const usersSnap = await getDocs(usersQ);

              if (!usersSnap.empty) {
                const userDocSnap = usersSnap.docs[0];
                const userId = userDocSnap.id;

                const attemptsRef = doc(db, 'password_attempts', userId);
                const attemptsSnap = await getDoc(attemptsRef);

                // Charger la configuration max_password_attempts
                let maxAttempts = 3; // fallback
                try {
                  const confQ = query(collection(db, 'configurations'), where('key', '==', 'max_password_attempts'));
                  const confSnap = await getDocs(confQ);
                  if (!confSnap.empty) {
                    const conf = confSnap.docs[0].data();
                    if (conf && conf.value) {
                      const parsed = parseInt(conf.value, 10);
                      if (!isNaN(parsed)) maxAttempts = parsed;
                    }
                  }
                } catch (confErr) {
                  console.warn('Impossible de charger la configuration:', confErr);
                }

                const currentAttempts = attemptsSnap.exists() ? (attemptsSnap.data().nb_attempt || 0) : 0;

                // N'incrémenter que si on n'a pas encore atteint la limite
                if (currentAttempts >= maxAttempts) {
                  alert('Nombre maximum de tentatives atteint. Contactez l\'administrateur.');
                  try {
                    const userRef = doc(db, 'users', userId);
                    const userSnap2 = await getDoc(userRef);
                    const currentStatus = userSnap2.exists() ? userSnap2.data().status : null;
                    if (currentStatus !== 'SUSPENDED') {
                      await updateDoc(userRef, { status: 'SUSPENDED' });
                    }
                  } catch (sErr) {
                    console.error('Impossible de suspendre l\'utilisateur:', sErr);
                  }
                } else {
                  // Incrémenter
                  if (attemptsSnap.exists()) {
                    await updateDoc(attemptsRef, { nb_attempt: increment(1) });
                    // augmenter la valeur locale
                    const newAttempts = currentAttempts + 1;
                    if (newAttempts < maxAttempts) {
                      alert('Les informations que vous avez entrées sont incorrectes. Il vous reste ' + (maxAttempts - newAttempts) + ' tentative(s).');
                    } else {
                      alert('Nombre maximum de tentatives atteint. Contactez l\'administrateur.');
                      try {
                        const userRef = doc(db, 'users', userId);
                        const userSnap2 = await getDoc(userRef);
                        const currentStatus = userSnap2.exists() ? userSnap2.data().status : null;
                        if (currentStatus !== 'SUSPENDED') {
                          await updateDoc(userRef, { status: 'SUSPENDED' });
                        }
                      } catch (sErr) {
                        console.error('Impossible de suspendre l\'utilisateur:', sErr);
                      }
                    }
                  } else {
                    await setDoc(attemptsRef, { nb_attempt: 1, user_id: userId });
                    if (1 < maxAttempts) {
                      alert('Les informations que vous avez entrées sont incorrectes. Il vous reste ' + (maxAttempts - 1) + ' tentative(s).');
                    } else {
                      alert('Nombre maximum de tentatives atteint. Contactez l\'administrateur.');
                      try {
                        await updateDoc(doc(db, 'users', userId), { status: 'SUSPENDED' });
                      } catch (sErr) {
                        console.error('Impossible de suspendre l\'utilisateur:', sErr);
                      }
                    }
                  }
                }
              }
            } catch (incErr) {
              console.error('Erreur lors de l\'incrément des tentatives:', incErr);
            }

            this.errorMessage = 'Email ou mot de passe incorrect';
            break;
          case 'auth/user-not-found':
            this.errorMessage = 'Email ou mot de passe incorrect';
            break;
          default:
            this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
        }
      }
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
              <h1 class="text-3xl font-bold text-gray-900 mb-2">Bienvenue</h1>
              <p class="text-gray-600">Connectez-vous à votre compte</p>
            </div>

            <!-- Message d'erreur -->
            <div v-if="errorMessage" class="mb-4 p-4 bg-red-50 text-red-700 rounded-lg">
              {{ errorMessage }}
            </div>

            <!-- Formulaire -->
            <form class="space-y-6" @submit.prevent="handleLogin">
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
                  ></ion-input>
                </div>
              </div>

              <!-- Mot de passe -->
              <div class="space-y-2">
                <div class="flex justify-between items-center">
                  <label class="text-sm font-medium text-gray-700">Mot de passe</label>
                  <a href="#" class="text-sm text-gray-600 hover:text-black">Mot de passe oublié?</a>
                </div>
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
                    :class="{ 'border-red-500': errorMessage && !password }"
                  ></ion-input>
                </div>
              </div>

              <!-- Bouton de connexion -->
              <ion-button
                type="submit"
                expand="block"
                class="font-medium h-12"
                color="dark" 
              >
                Se connecter
              </ion-button>

              <!-- Lien d'inscription -->
              <div class="text-center mt-8">
                <p class="text-gray-600">
                  Pas encore de compte? 
                  <a href="/register" class="font-medium text-black hover:underline">S'inscrire</a>
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
}
</style>
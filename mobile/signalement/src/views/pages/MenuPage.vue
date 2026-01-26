<template>
    <ion-page>
        <ion-header :translucent="true">
            <ion-toolbar>
                <ion-buttons slot="start">
                    <ion-button fill="clear" @click="goBack" aria-label="Retour" color="dark">
                        <ion-icon :icon="arrowBack"></ion-icon>
                    </ion-button>
                </ion-buttons>
                <ion-title class="font-bold text-black">Menu</ion-title>
            </ion-toolbar>
        </ion-header>

        <ion-content :fullscreen="true" class="bg-gray-50">
            <!-- Profil utilisateur -->
            <div class="px-4 py-6">
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

                    <div class="grid grid-cols-3 gap-4 text-center">
                        <div v-if="isLoading" class="col-span-3">
                            <div class="loading-container">
                                <ion-spinner name="crescent" class="mb-2"></ion-spinner>
                                <div class="loading-text">Chargement des données…</div>
                            </div>
                        </div>
                        <template v-else>
                            <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-4">
                                <div class="text-2xl font-bold mb-1">{{ stats.signalements }}</div>
                                <div class="text-sm text-gray-600">Signalements</div>
                            </div>
                            <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-4">
                                <div class="text-2xl font-bold  mb-1">{{ stats.resolus }}</div>
                                <div class="text-sm text-gray-600">Résolus</div>
                            </div>
                            <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-4">
                                <div class="text-2xl font-bold mb-1">{{ stats.contributions }}</div>
                                <div class="text-sm text-gray-600">Contributions</div>
                            </div>
                        </template>
                    </div>
                </div>

                <!-- Options du menu -->
                <div class="space-y-3">
                    <div v-for="item in menuItems" :key="item.id" @click="navigateTo(item.route)"
                        class="bg-white rounded-xl p-4 flex items-center justify-between hover:bg-gray-50 active:bg-gray-100 cursor-pointer transition-colors duration-200 border border-gray-100">
                        <div class="flex items-center space-x-4">
                            <div
                                :class="`p-3 rounded-lg ${item.color} transition-transform duration-200 group-hover:scale-110`">
                                <ion-icon :icon="item.icon" class="text-xl text-gray-700"></ion-icon>
                            </div>
                            <div>
                                <h3 class="font-medium text-gray-900">{{ item.title }}</h3>
                                <p class="text-sm text-gray-600">{{ item.description }}</p>
                            </div>
                        </div>
                        <ion-icon :icon="chevronForward" class="text-gray-400"></ion-icon>
                    </div>
                </div>

                <!-- Déconnexion -->
                <div class="mt-8">
                    <ion-button @click="deconnexion" expand="block" class="custom-logout-btn shadow-sm">
                        <ion-icon slot="start" :icon="logOut"></ion-icon>
                        Déconnexion
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
    IonIcon,
    IonButtons,
    IonSpinner,
} from '@ionic/vue';
import {
    person,
    settings,
    helpCircle,
    documentText,
    star,
    chevronForward,
    logOut,
    shield,
    notifications,
    arrowBack
} from 'ionicons/icons';

// Import Firebase
import { auth, db } from '@/config/firebase'; // Assurez-vous que ce chemin est correct
import { collection, doc, getDoc, getDocs, query, where } from 'firebase/firestore';
import { signOut } from 'firebase/auth';

interface MenuItem {
    id: number;
    title: string;
    description: string;
    icon: any;
    color: string;
    route: string;
}

interface UserData {
    name?: string;
    email?: string;
    stats?: {
        signalements?: number;
        resolus?: number;
        contributions?: number;
    };
}

export default defineComponent({
    name: 'MenuPage',

    components: {
        IonPage,
        IonHeader,
        IonContent,
        IonTitle,
        IonToolbar,
        IonButton,
        IonIcon
    },

    data() {
        return {
            utilisateur: {
                name: '[Utilisateur]',
                email: '[Email]'
            } as UserData,

            isLoading : false ,

            stats: {
                signalements: 0,
                resolus: 0,
                contributions: 0
            },

            menuItems: [
                {
                    id: 1,
                    title: 'Informations personnelles',
                    description: 'Gérer mes informations personnelles',
                    icon: settings,
                    color: 'bg-blue-100',
                    route: '/personal-data'
                },
                {
                    id: 2,
                    title: 'Notifications',
                    description: 'Gérer vos alertes',
                    icon: notifications,
                    color: 'bg-green-100',
                    route: '/notifications'
                },
                {
                    id: 3,
                    title: 'Mes signalements',
                    description: 'Voir votre historique',
                    icon: documentText,
                    color: 'bg-purple-100',
                    route: '/mes-signalements'
                },
                {
                    id: 4,
                    title: 'Contributions',
                    description: 'Votre impact sur la communauté',
                    icon: star,
                    color: 'bg-yellow-100',
                    route: '/contributions'
                },
                {
                    id: 5,
                    title: 'Confidentialité',
                    description: 'Gérer vos données',
                    icon: shield,
                    color: 'bg-gray-100',
                    route: '/confidentialite'
                },
                {
                    id: 6,
                    title: 'Aide & Support',
                    description: 'FAQ et contact',
                    icon: helpCircle,
                    color: 'bg-red-100',
                    route: '/aide'
                }
            ] as MenuItem[],

            // expose icons to template
            person,
            settings,
            helpCircle,
            documentText,
            star,
            arrowBack,
            chevronForward,
            logOut
        };
    },

    methods: {
        async chargerDonneesUtilisateur() {
            this.isLoading = true;
            try {
                const uid = localStorage.getItem('uid');
                if (!uid) {
                    console.warn('Aucun UID trouvé dans localStorage');
                    return;
                }

                const myReportsQuery = query(
                    collection(db, 'reports'),
                    where("user.firebaseUid", "==", uid),
                );

                const myEndedReportsQuery = query(
                    collection(db, 'reports'),
                    where("user.firebaseUid", "==", uid),
                    where("status.statusCode", "==", "COMPLETED")
                );

                const reports = await getDocs(myReportsQuery);
                const endedReports = await getDocs(myEndedReportsQuery);

                console.log("Reports",reports);

                this.stats = {
                    signalements: reports.size || 0,
                    resolus: endedReports.size || 0,
                    contributions: reports.size - endedReports.size || 0
                };

                this.isLoading = false ;

            } catch (error) {
                console.error('Erreur lors du chargement des données utilisateur:', error);
            }
        },

        navigateTo(route: string) {
            this.$router.push(route);
        },

        goBack() {
            // Use router back if available, otherwise navigate to root
            if (this.$router && this.$router.back) {
                this.$router.back();
            } else {
                this.$router.push('/map');
            }
        },

        deconnexion() {
            signOut(auth);
            localStorage.removeItem('uid');
            localStorage.removeItem('user');
            
            this.$router.push('/');
        }
    },

    mounted() {
        this.chargerDonneesUtilisateur();
        const savedUser = localStorage.getItem('user');
        if (savedUser) {
            try {
                const parsedUser = JSON.parse(savedUser);
                console.log("parsed user",parsedUser);
                this.utilisateur.name = parsedUser.name || this.utilisateur.name;
                this.utilisateur.email = parsedUser.email || this.utilisateur.email;
            } catch (e) {
                console.error('Erreur parsing user localStorage:', e);
            }
        }
    }
});
</script>

<style scoped>
/* Styles pour la page menu */
:deep(.custom-logout-btn) {
    --background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
    --color: #dc2626;
    --border-radius: 12px;
    --background-hover: #fecaca;
    --border-width: 1px;
    --border-color: #fca5a5;
    --box-shadow: 0 2px 8px rgba(220, 38, 38, 0.1);
}

/* Animation pour les éléments du menu */
.group-hover\:scale-110 {
    transition: transform 0.2s ease;
}

/* Effet de pression sur mobile */
.active\:bg-gray-100:active {
    background-color: #f3f4f6;
}

/* Loading styles */
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
</style>
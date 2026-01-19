<template>
  <!-- TabBar avec divs -->
  <div class="tab-bar-container">
    <!-- Conteneur principal -->
    <div class="tab-bar">
      <!-- Onglet Liste -->
      <button 
        :class="['tab-button', { 'active': activeTab === 'liste' }]"
        @click="navigateTo('list')"
      >
        <div class="tab-icon-container">
          <ion-icon :icon="activeTab === 'liste' ? list : listOutline" class="tab-icon"></ion-icon>
        </div>
        <span class="tab-label">Liste</span>
      </button>

      <!-- Onglet Carte -->
      <button 
        :class="['tab-button', { 'active': activeTab === 'carte' }]"
        @click="navigateTo('map')"
      >
        <div class="tab-icon-container">
          <ion-icon :icon="activeTab === 'carte' ? map : mapOutline" class="tab-icon"></ion-icon>
        </div>
        <span class="tab-label">Carte</span>
      </button>

      <!-- Onglet Menu -->
      <button 
        :class="['tab-button', { 'active': activeTab === 'menu' }]"
        @click="navigateTo('menu')"
      >
        <div class="tab-icon-container">
          <ion-icon :icon="activeTab === 'menu' ? menu : menuOutline" class="tab-icon"></ion-icon>
        </div>
        <span class="tab-label">Menu</span>
      </button>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { IonIcon } from '@ionic/vue';
import { 
  list,
  listOutline,
  map,
  mapOutline,
  menu,
  menuOutline
} from 'ionicons/icons';

export default defineComponent({
  name: 'TabBar',
  
  components: {
    IonIcon
  },
  
  props: {
    activeTab: {
      type: String,
      default: 'liste'
    }
  },
  
  data() {
    return {
      list,
      listOutline,
      map,
      mapOutline,
      menu,
      menuOutline
    };
  },
  
  methods: {
    navigateTo(tab: string) {
      this.$router.push(`/${tab}`);
    }
  }
});
</script>

<style scoped>
/* ============================================
   STYLES DE LA TAB BAR PERSONNALISÉE
   ============================================ */

.tab-bar-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom);
  background-color: #ffffff;
}

.tab-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  padding: 12px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  height: 70px;
}

/* Boutons d'onglet */
.tab-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  padding: 8px 16px;
  cursor: pointer;
  flex: 1;
  transition: all 0.2s ease;
  position: relative;
}

.tab-button:hover {
  background: rgba(0, 0, 0, 0.02);
}

.tab-button.active {
  color: #000000;
}

/* Indicateur actif */
.tab-button.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: #000000;
  border-radius: 50%;
}

/* Conteneur d'icône */
.tab-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

/* Icône */
.tab-icon {
  font-size: 24px;
  color: #6b7280;
  transition: color 0.2s ease;
}

.tab-button.active .tab-icon {
  color: #000000;
}

/* Label */
.tab-label {
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  transition: color 0.2s ease;
}

.tab-button.active .tab-label {
  color: #000000;
  font-weight: 600;
}

/* ============================================
   ANIMATIONS
   ============================================ */

@keyframes tabPress {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.95);
  }
  100% {
    transform: scale(1);
  }
}

.tab-button:active .tab-icon-container {
  animation: tabPress 0.2s ease;
}

/* ============================================
   MODE SOMBRE
   ============================================ */

@media (prefers-color-scheme: dark) {
  .tab-bar {
    background: #1a1a1a;
    border-top: 1px solid #374151;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.2);
  }
  
  .tab-button:hover {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .tab-button.active {
    color: #ffffff;
  }
  
  .tab-button.active::after {
    background: #ffffff;
  }
  
  .tab-icon {
    color: #9ca3af;
  }
  
  .tab-button.active .tab-icon {
    color: #ffffff;
  }
  
  .tab-label {
    color: #9ca3af;
  }
  
  .tab-button.active .tab-label {
    color: #ffffff;
  }
}

/* ============================================
   RESPONSIVE
   ============================================ */

/* Tablettes et desktop */
@media (min-width: 768px) {
  .tab-bar {
    max-width: 400px;
    margin: 0 auto;
    border-radius: 20px 20px 0 0;
    border-left: 1px solid #e5e7eb;
    border-right: 1px solid #e5e7eb;
    border-bottom: none;
  }
  
  @media (prefers-color-scheme: dark) {
    .tab-bar {
      border-left: 1px solid #374151;
      border-right: 1px solid #374151;
    }
  }
}

/* Grands écrans */
@media (min-width: 1024px) {
  .tab-bar-container {
    left: auto;
    right: 0;
    width: 400px;
    padding-bottom: 0;
  }
  
  .tab-bar {
    border-radius: 0;
    box-shadow: -2px 0 10px rgba(0, 0, 0, 0.05);
  }
}

/* ============================================
   ACCESSIBILITÉ
   ============================================ */

.tab-button:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

.tab-button:focus:not(:focus-visible) {
  outline: none;
}

/* Support des appareils avec notch/safe area */
@supports (padding: max(0px)) {
  .tab-bar-container {
    padding-bottom: max(12px, env(safe-area-inset-bottom));
  }
}
</style>
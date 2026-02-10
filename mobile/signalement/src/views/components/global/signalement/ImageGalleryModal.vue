<template>
  <ion-modal :is-open="internalShow" @didDismiss="onClose">
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button fill="clear" @click="prev" :disabled="images.length<=1">
            <ion-icon :icon="chevBack"></ion-icon>
          </ion-button>
        </ion-buttons>
        <ion-title>{{ currentIndex + 1 }} / {{ images.length }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="next" :disabled="images.length<=1">
            <ion-icon :icon="chevForward"></ion-icon>
          </ion-button>
          <ion-button fill="clear" @click="close">
            <ion-icon :icon="closeIcon"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding gallery-modal">
      <div v-if="images.length === 0" class="text-center py-8 text-gray-600">
        Aucune photo pour ce signalement
      </div>

      <div v-else class="flex flex-col items-center">
        <div class="w-full max-w-3xl h-72 flex items-center justify-center mb-4 cursor-pointer" @click="openFullscreen">
          <img :src="images[currentIndex]" alt="photo" class="max-h-full max-w-full object-contain rounded-lg shadow hover:opacity-90 transition-opacity"/>
        </div>

        <div class="w-full max-w-3xl flex overflow-x-auto space-x-2">
          <button
            v-for="(img, idx) in images"
            :key="idx"
            @click="goTo(idx)"
            class="flex-none border rounded overflow-hidden"
            :class="{'ring-2 ring-black': idx === currentIndex}">
            <img :src="img" alt="thumb" class="h-16 w-24 object-cover"/>
          </button>
        </div>
      </div>
    </ion-content>

    <!-- Fullscreen viewer -->
    <div v-if="showFullscreen" class="fullscreen-overlay" @click="closeFullscreen">
      <div class="fullscreen-header">
        <span class="fullscreen-title">{{ currentIndex + 1 }} / {{ images.length }}</span>
        <button @click.stop="closeFullscreen" class="fullscreen-close-btn">
          <ion-icon :icon="closeIcon" class="text-white text-2xl"></ion-icon>
        </button>
      </div>
      <div class="fullscreen-content">
        <img :src="images[currentIndex]" alt="photo" class="fullscreen-image" @click.stop/>
      </div>
      <div class="fullscreen-nav">
        <button @click.stop="prev" :disabled="images.length<=1" class="fullscreen-nav-btn">
          <ion-icon :icon="chevBack" class="text-white text-3xl"></ion-icon>
        </button>
        <button @click.stop="next" :disabled="images.length<=1" class="fullscreen-nav-btn">
          <ion-icon :icon="chevForward" class="text-white text-3xl"></ion-icon>
        </button>
      </div>
    </div>
  </ion-modal>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue';
import { IonModal, IonHeader, IonToolbar, IonButtons, IonButton, IonTitle, IonContent, IonIcon } from '@ionic/vue';
import { close as closeIcon, chevronBack as chevBack, chevronForward as chevForward } from 'ionicons/icons';

export default defineComponent({
  name: 'ImageGalleryModal',
  components: { IonModal, IonHeader, IonToolbar, IonButtons, IonButton, IonTitle, IonContent, IonIcon },
  props: {
    modelValue: { type: Boolean, required: true },
    images: { type: Array as () => string[], default: () => [] },
    startIndex: { type: Number, default: 0 }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const internalShow = ref(props.modelValue);
    const currentIndex = ref(props.startIndex || 0);
    const showFullscreen = ref(false);

    watch(() => props.modelValue, v => internalShow.value = v);
    watch(internalShow, v => emit('update:modelValue', v));
    watch(() => props.startIndex, (v) => { currentIndex.value = v || 0; });

    const close = () => { internalShow.value = false; };
    const onClose = () => { internalShow.value = false; };
    const prev = () => { if (currentIndex.value > 0) currentIndex.value -= 1; };
    const next = () => { if (currentIndex.value < props.images.length - 1) currentIndex.value += 1; };
    const goTo = (i: number) => { currentIndex.value = i; };
    const openFullscreen = () => { showFullscreen.value = true; };
    const closeFullscreen = () => { showFullscreen.value = false; };

    return { 
      internalShow, 
      currentIndex, 
      showFullscreen,
      close, 
      onClose, 
      prev, 
      next, 
      goTo, 
      openFullscreen,
      closeFullscreen,
      closeIcon, 
      chevBack, 
      chevForward 
    };
  }
});
</script>

<style scoped>
.gallery-modal img { background: #fff; }
.gallery-modal .ring-2 { box-shadow: 0 0 0 2px rgba(0,0,0,0.12); }

/* Thumbnails container: horizontal scroll by default, wraps into rows and becomes vertically scrollable when many items */
.thumbnails-container {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  padding: 6px 2px;
  scroll-snap-type: x mandatory;
}
.thumbnails-container .thumb-btn {
  flex: 0 0 auto;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  display: inline-block;
  scroll-snap-align: center;
}
.thumbnails-container .thumb-img {
  height: 64px;
  width: 96px;
  object-fit: cover;
  display: block;
}
.thumbnails-container .active-thumb {
  box-shadow: 0 0 0 2px rgba(0,0,0,0.12);
  border-color: #000;
}

/* If there are many thumbnails, allow vertical scrolling: set a max-height and enable wrapping */
@media (min-width: 0px) {
  .thumbnails-container.many-rows {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
    gap: 8px;
    max-height: 200px;
    overflow-y: auto;
    overflow-x: hidden;
  }
  .thumbnails-container.many-rows .thumb-btn {
    width: 100%;
  }
}

/* Fullscreen overlay */
.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.fullscreen-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.7), transparent);
  z-index: 10;
}

.fullscreen-title {
  color: white;
  font-size: 1rem;
  font-weight: 600;
}

.fullscreen-close-btn {
  background: transparent;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}

.fullscreen-close-btn:hover {
  opacity: 0.7;
}

.fullscreen-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  overflow: hidden;
}

.fullscreen-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-select: none;
}

.fullscreen-nav {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  padding: 0 1rem;
  pointer-events: none;
  z-index: 10;
}

.fullscreen-nav-btn {
  background: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  pointer-events: auto;
  transition: background 0.2s;
}

.fullscreen-nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.7);
}

.fullscreen-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.cursor-pointer {
  cursor: pointer;
}
</style>

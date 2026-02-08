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
        <div class="w-full max-w-3xl h-72 flex items-center justify-center mb-4">
          <img :src="images[currentIndex]" alt="photo" class="max-h-full max-w-full object-contain rounded-lg shadow"/>
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

    watch(() => props.modelValue, v => internalShow.value = v);
    watch(internalShow, v => emit('update:modelValue', v));
    watch(() => props.startIndex, (v) => { currentIndex.value = v || 0; });

    const close = () => { internalShow.value = false; };
    const onClose = () => { internalShow.value = false; };
    const prev = () => { if (currentIndex.value > 0) currentIndex.value -= 1; };
    const next = () => { if (currentIndex.value < props.images.length - 1) currentIndex.value += 1; };
    const goTo = (i: number) => { currentIndex.value = i; };

    return { internalShow, currentIndex, close, onClose, prev, next, goTo, closeIcon, chevBack, chevForward };
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
</style>

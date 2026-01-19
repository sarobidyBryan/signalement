<template>
  <div class="bg-white/95 backdrop-blur-sm rounded-xl shadow-lg p-4">
    <h4 class="font-bold text-gray-900 mb-2">Tableau récapitulatif</h4>
    <div class="grid grid-cols-2 gap-3 text-sm">
      <div class="flex flex-col">
        <span class="text-gray-600">Nb de points</span>
        <span class="font-medium text-gray-900">{{ nbPoints }}</span>
      </div>

      <div class="flex flex-col">
        <span class="text-gray-600">Surface totale</span>
        <span class="font-medium text-gray-900">{{ totalSurfaceFormatted }}</span>
      </div>

      <div class="flex flex-col">
        <span class="text-gray-600">Avancement moyen</span>
        <span class="font-medium text-gray-900">{{ avancementPercentFormatted }}</span>
      </div>

      <div class="flex flex-col">
        <span class="text-gray-600">Budget total</span>
        <span class="font-medium text-gray-900">{{ totalBudgetFormatted }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';

export default defineComponent({
  name: 'TableauRecapitulatif',
  props: {
    signalements: {
      type: Array as () => any[],
      required: true
    }
  },
  setup(props) {
    const nbPoints = computed(() => props.signalements.length);

    const toNumber = (v: any) => {
      if (v == null) return 0;
      if (typeof v === 'number') return v;
      const n = Number(String(v).replace(/[^0-9.,-]/g, '').replace(',', '.'));
      return Number.isFinite(n) ? n : 0;
    };

    const totalSurface = computed(() => {
      return props.signalements.reduce((acc, s) => {
        let area = 0;
        if (s.area != null) area = toNumber(s.area);
        else if (s.raw && (s.raw.area != null || s.raw.surface != null)) area = toNumber(s.raw.area ?? s.raw.surface);
        return acc + Math.max(0, area);
      }, 0);
    });

    const totalBudget = computed(() => {
      return props.signalements.reduce((acc, s) => {
        let b = 0;
        if (s.budget != null) b = toNumber(s.budget);
        else if (s.raw && s.raw.budget != null) b = toNumber(s.raw.budget);
        return acc + Math.max(0, b);
      }, 0);
    });

    const avancementPercent = computed(() => {
      const arr: number[] = [];
      props.signalements.forEach(s => {
        const area = s.area != null ? toNumber(s.area) : (s.raw ? toNumber(s.raw.area ?? s.raw.surface) : 0);
        const treated = s.raw ? toNumber(s.raw.treated_area ?? s.raw.treatedArea ?? s.treated_area ?? null) : (s.treated_area != null ? toNumber(s.treated_area) : 0);
        if (area > 0) {
          const pct = Math.max(0, Math.min(100, (treated / area) * 100));
          arr.push(pct);
        }
      });
      if (arr.length === 0) return 0;
      const sum = arr.reduce((a, b) => a + b, 0);
      return sum / arr.length;
    });

    const nf = new Intl.NumberFormat('fr-FR');

    const totalSurfaceFormatted = computed(() => nf.format(Math.round(totalSurface.value)) + ' m²');
    const totalBudgetFormatted = computed(() => nf.format(Math.round(totalBudget.value)) + ' Ar');
    const avancementPercentFormatted = computed(() => (Math.round(avancementPercent.value * 10) / 10) + ' %');

    return {
      nbPoints,
      totalSurfaceFormatted,
      totalBudgetFormatted,
      avancementPercentFormatted
    };
  }
});
</script>

<style scoped>
.backdrop-blur-sm { backdrop-filter: blur(4px); }
</style>

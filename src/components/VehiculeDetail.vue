<template>
        <div>
          <h4>Véhicule {{ vehicule.Name}}</h4>
          <div>
            <label><strong>ID:</strong></label> {{ vehicule.Id }}
          </div>
          <div>
            <label><strong>Model Name:</strong></label>
            {{ vehicule.ModelName }}
          </div>
          <div>
            <label><strong>Immatriculation:</strong></label>
            {{ vehicule.Immat }}
          </div>
          <div>
            <label><strong>% Batterie:</strong></label>
            {{ vehicule.EnergyLevel }}
          </div>
          <div>
            <label><strong>Position:</strong></label>
            {{ vehicule.Position.Lat }}, {{ vehicule.Position.Lon }}
          </div>
           <div>
            <label><strong>View on Google Maps:</strong></label>
            <a v-bind:target="'_blank'" v-bind:href="'https://maps.google.com/maps?z=12&t=m&q=loc:' + vehicule.Position.Lat + '+' + vehicule.Position.Lon">Click Here</a>
          </div>
      </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import Vehicule from '@/types/Vehicule';

export default defineComponent({
  name: 'vehicules-details',
  props: {
    vehicule: {
      type: Object as PropType<Vehicule>,
      required: true,
    },
  },
  mounted() {
    console.log(this.vehicule);
  },
});
</script>

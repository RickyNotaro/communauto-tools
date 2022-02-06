<template>
  '<!-- Localhost only API key-->
  <GoogleMap
  api-key="AIzaSyCuQmpLTsBBC3EyJvNkQJNAu8x4HA9XL4E"
  style="width: 100%; height: 800px"
  :center="center"
  :zoom="15"
  >
    <Marker v-for="marker in markers" :options="marker" :key="marker.id" />
  </GoogleMap>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { GoogleMap, Marker } from 'vue3-google-map';
import CommunautoDataService from '@/services/CommunautoDataService';
import Vehicule from '@/types/Vehicule';
import ResponseData from '@/types/ResponseData';

export default defineComponent({
  components: { GoogleMap, Marker },
  setup() {
    const center = { lat: 45.4756595, lng: -73.5884042 };

    return { center };
  },
  data() {
    return {
      vehicules: [] as Vehicule[],
      currentVehicule: {} as Vehicule,
      currentIndex: -1,
      // Not sure what I did here :v
      markers: {} as [key: any],
    };
  },
  methods: {
    retrieveVehicules() {
      CommunautoDataService.getVehicleProposals()
        .then((response: ResponseData) => {
          this.vehicules = response.data.Vehicules;
          // console.log(response.data.Vehicules);

          this.vehicules.forEach((vehicule) => {
            this.markers[vehicule.Id] = {
              position: {
                lat: vehicule.Position.Lat,
                lng: vehicule.Position.Lon,
              },
              title: vehicule.Name,
            };
          });
        })
        .catch((e: Error) => {
          console.log(e);
        });
    },
  },
  mounted() {
    this.retrieveVehicules();
  },
});
</script>

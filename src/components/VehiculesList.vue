<template>
  <div class="list row">
    <div class="col-md-4">
      <h4>Liste de véhicules</h4>
      <ul class="list-group">
        <li
          class="list-group-item"
          :class="{ active: index == currentIndex }"
          v-for="(vehicule, index) in vehicules"
          :key="index"
          @click="setActiveVehicule(vehicule, index)"
        >
          Voiture : {{ vehicule.Name }}
        </li>
      </ul>
    </div>
    <div class="col-md-8">

      <div v-if="currentVehicule.Id">
          <VehiculeDetail :vehicule="this.currentVehicule"/>
      </div>
      <div v-else>
        <br />
        <p>Veuilliez choisir un véhicule...</p>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import CommunautoDataService from '@/services/CommunautoDataService';
import Vehicule from '@/types/Vehicule';
import ResponseData from '@/types/ResponseData';
import VehiculeDetail from '@/components/VehiculeDetail.vue';

export default defineComponent({
  name: 'vehicules-list',
  components: {
    VehiculeDetail,
  },
  data() {
    return {
      vehicules: [] as Vehicule[],
      currentVehicule: {} as Vehicule,
      currentIndex: -1,
    };
  },
  methods: {
    retrieveVehicules() {
      CommunautoDataService.getVehicleProposals()
        .then((response: ResponseData) => {
          this.vehicules = response.data.Vehicules;
          // console.log(response.data.Vehicules);
        })
        .catch((e: Error) => {
          console.log(e);
        });
    },

    refreshList() {
      this.retrieveVehicules();
      this.currentVehicule = {} as Vehicule;
      this.currentIndex = -1;
    },

    setActiveVehicule(vehicule: Vehicule, index = -1) {
      this.currentVehicule = vehicule;
      this.currentIndex = index;
    },
  },
  mounted() {
    this.retrieveVehicules();
  },
});
</script>

<style>
.list {
  text-align: left;
  max-width: 750px;
  margin: auto;
}
</style>

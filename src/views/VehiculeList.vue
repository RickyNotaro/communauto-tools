<template>
  <div class="VehiculeList">
    <vehicule-list></vehicule-list>
  </div>
</template>

<script>

import { Options, Vue } from 'vue-class-component';
import VehiculeList from '@/components/VehiculesList.vue'; // @ is an alias to /src

@Options({
  components: {
    VehiculeList,
  },
})
export default class Home extends Vue {}
</script>

<style>

</style>

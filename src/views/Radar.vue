<template>
  <div class="radar">
    <vehicule-list></vehicule-list>
  </div>
</template>

<script>

import { Options, Vue } from 'vue-class-component';
import Radar from '@/components/Radar.vue'; // @ is an alias to /src

@Options({
  components: {
    Radar,
  },
})
export default class Home extends Vue {}
</script>

<style>

</style>

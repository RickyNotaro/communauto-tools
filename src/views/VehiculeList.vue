<template>
  <div class="container mt-3">
    <h4>Véhicules disponibles</h4>

    <div v-if="locating" class="text-muted mb-2">Localisation en cours...</div>
    <div v-if="locationError" class="alert alert-warning">{{ locationError }} — distances depuis le centre de Montréal</div>

    <div class="row mb-3">
      <div class="col-auto">
        <select v-model="sortKey" class="form-select form-select-sm">
          <option value="distance">Distance</option>
          <option value="name">Numéro</option>
          <option value="energy">Batterie</option>
          <option value="brand">Marque</option>
        </select>
      </div>
      <div class="col-auto">
        <input
          v-model.number="maxDistance"
          type="number"
          class="form-control form-control-sm"
          placeholder="Rayon max (m)"
          style="width: 160px"
        >
      </div>
      <div class="col-auto d-flex align-items-center text-muted">
        {{ filteredVehicles.length }} véhicule{{ filteredVehicles.length > 1 ? 's' : '' }}
      </div>
    </div>

    <div v-if="loading" class="text-muted">Chargement...</div>

    <div class="row">
      <div class="col-md-5">
        <div class="list-group">
          <button
            v-for="v in filteredVehicles"
            :key="v.CarId"
            type="button"
            class="list-group-item list-group-item-action"
            :class="{ active: selected?.CarId === v.CarId }"
            @click="selected = v"
          >
            <div class="d-flex justify-content-between">
              <strong>{{ v.CarNo }} — {{ v.CarBrand }} {{ v.CarModel }}</strong>
              <span class="badge bg-secondary">{{ formatDistance(v.distance) }}</span>
            </div>
            <small>
              {{ v.CarColor }}
              <span v-if="v.IsElectric"> · Électrique</span>
              <span v-if="v.EnergyLevel !== null"> · {{ v.EnergyLevel }}%</span>
            </small>
          </button>
        </div>
      </div>

      <div class="col-md-7">
        <div v-if="selected" class="card">
          <div class="card-body">
            <h5 class="card-title">{{ selected.CarNo }} — {{ selected.CarBrand }} {{ selected.CarModel }}</h5>
            <table class="table table-sm mb-0">
              <tbody>
                <tr><th>VIN</th><td>{{ selected.CarVin }}</td></tr>
                <tr><th>Plaque</th><td>{{ selected.CarPlate }}</td></tr>
                <tr><th>Couleur</th><td>{{ selected.CarColor }}</td></tr>
                <tr><th>Places</th><td>{{ selected.CarSeatNb }}</td></tr>
                <tr><th>Électrique</th><td>{{ selected.IsElectric ? 'Oui' : 'Non' }}</td></tr>
                <tr v-if="selected.EnergyLevel !== null"><th>Batterie</th><td>{{ selected.EnergyLevel }}%</td></tr>
                <tr><th>Distance</th><td>{{ formatDistance(selected.distance) }}</td></tr>
                <tr><th>Dernière utilisation</th><td>{{ formatDate(selected.LastUseDate) }}</td></tr>
                <tr>
                  <th>Position</th>
                  <td>
                    <a
                      target="_blank"
                      :href="`https://maps.google.com/maps?z=15&q=loc:${selected.Latitude}+${selected.Longitude}`"
                    >
                      {{ selected.Latitude.toFixed(5) }}, {{ selected.Longitude.toFixed(5) }}
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div v-else class="text-muted mt-3">
          <p>Sélectionnez un véhicule dans la liste.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useGeolocation } from '@/composables/useGeolocation';
import { useVehicles, type VehicleWithDistance } from '@/composables/useVehicles';

const { userLocation, locationError, locating } = useGeolocation();
const { vehiclesWithDistance, loading, fetchVehicles } = useVehicles(userLocation);

const selected = ref<VehicleWithDistance | null>(null);
const sortKey = ref<'distance' | 'name' | 'energy' | 'brand'>('distance');
const maxDistance = ref<number | null>(null);

const filteredVehicles = computed(() => {
  let list = [...vehiclesWithDistance.value];

  if (maxDistance.value && maxDistance.value > 0) {
    list = list.filter((v) => v.distance <= maxDistance.value!);
  }

  list.sort((a, b) => {
    switch (sortKey.value) {
      case 'distance': return a.distance - b.distance;
      case 'name': return a.CarNo - b.CarNo;
      case 'energy': return (b.EnergyLevel ?? 0) - (a.EnergyLevel ?? 0);
      case 'brand': return `${a.CarBrand} ${a.CarModel}`.localeCompare(`${b.CarBrand} ${b.CarModel}`);
    }
  });

  return list;
});

function formatDistance(meters: number): string {
  return meters < 1000
    ? `${Math.round(meters)} m`
    : `${(meters / 1000).toFixed(1)} km`;
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('fr-CA');
}

onMounted(fetchVehicles);
</script>

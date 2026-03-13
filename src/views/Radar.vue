<template>
  <div class="container-fluid mt-3">
    <div class="row">
      <div class="col-md-3">
        <h5>Radar</h5>

        <div v-if="locationError" class="alert alert-warning alert-sm">{{ locationError }}</div>

        <div class="mb-3">
          <label class="form-label">Rayon : {{ radius }} m</label>
          <input v-model.number="radius" type="range" class="form-range" min="100" max="5000" step="100">
        </div>

        <div class="mb-2 text-muted">
          {{ vehiclesInRadius.length }} véhicule{{ vehiclesInRadius.length > 1 ? 's' : '' }} dans le rayon
        </div>

        <div class="list-group list-group-flush" style="max-height: 60vh; overflow-y: auto">
          <button
            v-for="v in vehiclesInRadius"
            :key="v.CarId"
            type="button"
            class="list-group-item list-group-item-action py-1 px-2"
            :class="{ active: selected?.CarId === v.CarId }"
            @click="selectVehicle(v)"
          >
            <div class="d-flex justify-content-between">
              <small><strong>{{ v.CarNo }}</strong> {{ v.CarBrand }} {{ v.CarModel }}</small>
              <small class="badge bg-secondary">{{ Math.round(v.distance) }} m</small>
            </div>
          </button>
        </div>
      </div>

      <div class="col-md-9">
        <div ref="mapContainer" style="height: 80vh; width: 100%; border-radius: 8px"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useGeolocation } from '@/composables/useGeolocation';
import { useVehicles, type VehicleWithDistance } from '@/composables/useVehicles';

const { userLocation, locationError, locating } = useGeolocation();
const { vehiclesWithDistance, fetchVehicles } = useVehicles(userLocation);

const radius = ref(1000);
const selected = ref<VehicleWithDistance | null>(null);
const mapContainer = ref<HTMLElement | null>(null);

let map: L.Map | null = null;
let userMarker: L.CircleMarker | null = null;
let radiusCircle: L.Circle | null = null;
let vehicleMarkers: L.CircleMarker[] = [];

const vehiclesInRadius = computed(() =>
  vehiclesWithDistance.value
    .filter((v) => v.distance <= radius.value)
    .sort((a, b) => a.distance - b.distance)
);

function initMap() {
  if (!mapContainer.value || map) return;

  map = L.map(mapContainer.value).setView([userLocation.value.lat, userLocation.value.lng], 15);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 19,
  }).addTo(map);

  userMarker = L.circleMarker([userLocation.value.lat, userLocation.value.lng], {
    radius: 8,
    fillColor: '#4285F4',
    color: '#fff',
    weight: 2,
    fillOpacity: 1,
  }).addTo(map).bindPopup('Ma position');

  radiusCircle = L.circle([userLocation.value.lat, userLocation.value.lng], {
    radius: radius.value,
    color: '#4285F4',
    fillColor: '#4285F4',
    fillOpacity: 0.08,
    weight: 1,
  }).addTo(map);
}

function updateMapMarkers() {
  if (!map) return;

  // Clear old vehicle markers
  vehicleMarkers.forEach((m) => m.remove());
  vehicleMarkers = [];

  for (const v of vehiclesWithDistance.value) {
    const inRadius = v.distance <= radius.value;
    const marker = L.circleMarker([v.Latitude, v.Longitude], {
      radius: 6,
      fillColor: inRadius ? '#28a745' : '#aaa',
      color: '#fff',
      weight: 1,
      fillOpacity: inRadius ? 0.9 : 0.4,
    }).addTo(map!);

    marker.bindPopup(
      `<strong>${v.CarNo} — ${v.CarBrand} ${v.CarModel}</strong><br>` +
      `${v.CarColor} · ${v.CarSeatNb} places<br>` +
      `${v.IsElectric ? 'Électrique' : 'Essence'}` +
      (v.EnergyLevel !== null ? ` · ${v.EnergyLevel}%` : '') +
      `<br>${Math.round(v.distance)} m`
    );

    vehicleMarkers.push(marker);
  }
}

function selectVehicle(v: VehicleWithDistance) {
  selected.value = v;
  if (map) {
    map.panTo([v.Latitude, v.Longitude]);
    // Find and open the matching marker popup
    const marker = vehicleMarkers.find((m) => {
      const pos = m.getLatLng();
      return pos.lat === v.Latitude && pos.lng === v.Longitude;
    });
    marker?.openPopup();
  }
}

// Update radius circle when slider changes
watch(radius, (r) => {
  if (radiusCircle) radiusCircle.setRadius(r);
  updateMapMarkers();
});

// Recenter map when user location is resolved
watch(locating, (isLocating) => {
  if (!isLocating && map) {
    const { lat, lng } = userLocation.value;
    map.setView([lat, lng], 15);
    userMarker?.setLatLng([lat, lng]);
    radiusCircle?.setLatLng([lat, lng]);
  }
});

// Redraw vehicle markers when data changes
watch(vehiclesWithDistance, () => updateMapMarkers());

onMounted(async () => {
  await fetchVehicles();
  await nextTick();
  initMap();
  updateMapMarkers();
});
</script>

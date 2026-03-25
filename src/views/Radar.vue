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

        <div class="form-check mb-2">
          <input v-model="showZones" class="form-check-input" type="checkbox" id="showZones">
          <label class="form-check-label" for="showZones">Zones Flex</label>
        </div>

        <!-- Electric filter -->
        <div class="mb-2">
          <label class="form-label small">Type de véhicule</label>
          <div class="btn-group btn-group-sm w-100">
            <button
              class="btn"
              :class="electricFilter === 'all' ? 'btn-primary' : 'btn-outline-secondary'"
              @click="electricFilter = 'all'"
            >Tous</button>
            <button
              class="btn"
              :class="electricFilter === 'electric' ? 'btn-success' : 'btn-outline-secondary'"
              @click="electricFilter = 'electric'"
            >Électrique</button>
            <button
              class="btn"
              :class="electricFilter === 'gas' ? 'btn-warning' : 'btn-outline-secondary'"
              @click="electricFilter = 'gas'"
            >Essence</button>
          </div>
        </div>

        <div class="mb-2 text-muted">
          {{ vehiclesInRadius.length }} véhicule{{ vehiclesInRadius.length > 1 ? 's' : '' }} dans le rayon
        </div>

        <!-- Auto-refresh -->
        <div class="mb-3">
          <label class="form-label small">Rafraîchissement</label>
          <div class="d-flex gap-1 flex-wrap">
            <button
              v-for="s in refreshOptions"
              :key="s"
              class="btn btn-sm"
              :class="refreshInterval === s ? 'btn-primary' : 'btn-outline-secondary'"
              @click="setRefreshInterval(s)"
            >
              {{ s }}s
            </button>
            <button
              class="btn btn-sm"
              :class="refreshInterval === 0 ? 'btn-primary' : 'btn-outline-secondary'"
              @click="setRefreshInterval(0)"
            >
              Off
            </button>
          </div>
          <div v-if="refreshInterval > 0" class="text-muted small mt-1">
            Prochain refresh dans {{ refreshCountdown }}s
          </div>
        </div>

        <!-- Auto-book -->
        <div v-if="isAuthenticated" class="mb-3">
          <button
            v-if="!autoBookActive"
            class="btn btn-warning btn-sm w-100"
            @click="startAutoBook"
          >
            Auto-réserver ({{ radius }} m)
          </button>
          <button
            v-else
            class="btn btn-danger btn-sm w-100"
            @click="stopAutoBook"
          >
            Arrêter auto-réservation
          </button>
          <div v-if="autoBookActive" class="text-muted small mt-1">
            En attente d'un véhicule dans {{ radius }} m...
          </div>
          <div v-if="autoBookError" class="alert alert-danger alert-sm mt-1 mb-0 py-1 px-2 small">{{ autoBookError }}</div>
          <div v-if="autoBookSuccess" class="alert alert-success alert-sm mt-1 mb-0 py-1 px-2 small">{{ autoBookSuccess }}</div>
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
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { useGeolocation } from '@/composables/useGeolocation';
import { useVehicles, type VehicleWithDistance } from '@/composables/useVehicles';
import { useAuth } from '@/composables/useAuth';
import { getLSIZones, createBooking } from '@/services/communautoDataService';

const { userLocation, locationError, locating } = useGeolocation();
const { vehiclesWithDistance, fetchVehicles } = useVehicles(userLocation);
const { isAuthenticated, wcfCookies } = useAuth();

const radius = ref(1000);
const electricFilter = ref<'all' | 'electric' | 'gas'>('all');
const selected = ref<VehicleWithDistance | null>(null);
const mapContainer = ref<HTMLElement | null>(null);

let map: L.Map | null = null;
let userMarker: L.CircleMarker | null = null;
let radiusCircle: L.Circle | null = null;
let vehicleMarkers: L.CircleMarker[] = [];
let zonesLayer: L.GeoJSON | null = null;
const showZones = ref(true);

// --- Auto-refresh ---
const refreshOptions = [15, 30, 60, 90];
const refreshInterval = ref(0);
const refreshCountdown = ref(0);
let refreshTimer: ReturnType<typeof setInterval> | null = null;

function setRefreshInterval(seconds: number) {
  refreshInterval.value = seconds;
  clearRefreshTimer();
  if (seconds > 0) {
    refreshCountdown.value = seconds;
    refreshTimer = setInterval(() => {
      refreshCountdown.value--;
      if (refreshCountdown.value <= 0) {
        refreshCountdown.value = seconds;
        fetchVehicles();
      }
    }, 1000);
  }
}

function clearRefreshTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
}

// --- Auto-book ---
const autoBookActive = ref(false);
const autoBookError = ref<string | null>(null);
const autoBookSuccess = ref<string | null>(null);
let autoBookInProgress = false;

function getUidFromCookies(): number | null {
  const match = wcfCookies.value?.match(/uid=(\d+)/);
  return match ? parseInt(match[1]) : null;
}

async function startAutoBook() {
  autoBookError.value = null;
  autoBookSuccess.value = null;
  autoBookActive.value = true;
  // Enable refresh if not already running
  if (refreshInterval.value === 0) {
    setRefreshInterval(15);
  }
  // Refresh and try immediately
  await fetchVehicles();
  await tryAutoBook();
}

function stopAutoBook() {
  autoBookActive.value = false;
}

async function tryAutoBook() {
  if (!autoBookActive.value || autoBookInProgress) return;
  autoBookInProgress = true;
  try {
    await _tryAutoBookImpl();
  } finally {
    autoBookInProgress = false;
  }
}

async function _tryAutoBookImpl() {
  if (!autoBookActive.value) return;
  const cars = vehiclesInRadius.value;
  if (cars.length === 0) return;

  const uid = getUidFromCookies();
  if (!uid) {
    autoBookError.value = 'uid introuvable dans les cookies';
    autoBookActive.value = false;
    return;
  }

  // Try each car in radius (closest first), skip if booking limit reached
  for (const car of cars) {
    try {
      const res = await createBooking(uid, car.CarId);
      const data = res.data?.d ?? res.data;
      if (data?.Success) {
        autoBookSuccess.value = `Réservé: #${car.CarNo} — ${car.CarBrand} ${car.CarModel} (${Math.round(car.distance)} m)`;
        autoBookActive.value = false;
        return;
      }
      // Booking limit reached on this car — try next one
      if (data?.ErrorType === 3) {
        autoBookError.value = `#${car.CarNo} limite atteinte, essai suivant...`;
        continue;
      }
      // Other error — stop
      autoBookError.value = data?.ErrorMessage || 'Réservation échouée';
      return;
    } catch (e: any) {
      autoBookError.value = e?.message || 'Erreur réseau';
      return;
    }
  }
  // All cars in radius had booking limit reached
  autoBookError.value = 'Limite atteinte sur tous les véhicules dans le rayon';
}

// Attempt auto-book whenever vehicle list updates
watch(vehiclesWithDistance, () => {
  if (autoBookActive.value) tryAutoBook();
});

// --- Map ---
const vehiclesInRadius = computed(() =>
  vehiclesWithDistance.value
    .filter((v) => v.distance <= radius.value)
    .filter((v) => electricFilter.value === 'all' || (electricFilter.value === 'electric' ? v.IsElectric : !v.IsElectric))
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

async function loadZones() {
  if (!map) return;
  try {
    const res = await getLSIZones();
    const geojson = res.data;
    zonesLayer = L.geoJSON(geojson, {
      style: (feature) => {
        const fill = feature?.properties?.style?.fillColor || '4285F4';
        return {
          fillColor: `#${fill}`,
          color: `#${fill}`,
          weight: 1.5,
          fillOpacity: 0.12,
          dashArray: '4 4',
        };
      },
      onEachFeature: (feature, layer) => {
        if (feature.properties?.name) {
          layer.bindTooltip(feature.properties.name, {
            sticky: true,
            className: 'zone-tooltip',
          });
        }
      },
    });
    // Only add to map if the checkbox is still checked
    if (showZones.value) {
      zonesLayer.addTo(map);
    }
  } catch {
    // zones are optional
  }
}

function updateMapMarkers() {
  if (!map) return;

  // Clear old vehicle markers
  vehicleMarkers.forEach((m) => m.remove());
  vehicleMarkers = [];

  const filtered = vehiclesWithDistance.value.filter((v) =>
    electricFilter.value === 'all' || (electricFilter.value === 'electric' ? v.IsElectric : !v.IsElectric)
  );

  for (const v of filtered) {
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

// Redraw vehicle markers when data or filter changes
watch([vehiclesWithDistance, electricFilter], () => updateMapMarkers());

// Toggle zones visibility
watch(showZones, (show) => {
  if (!map || !zonesLayer) return;
  if (show) zonesLayer.addTo(map);
  else zonesLayer.remove();
});

onMounted(async () => {
  await fetchVehicles();
  await nextTick();
  initMap();
  updateMapMarkers();
  loadZones();
});

onUnmounted(() => {
  clearRefreshTimer();
});
</script>

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
                <tr><th>ID</th><td>{{ selected.CarId }}</td></tr>
                <tr><th>Numéro</th><td>{{ selected.CarNo }}</td></tr>
                <tr><th>VIN</th><td>{{ selected.CarVin }}</td></tr>
                <tr><th>Plaque</th><td>{{ selected.CarPlate }}</td></tr>
                <tr><th>Marque</th><td>{{ selected.CarBrand }}</td></tr>
                <tr><th>Modèle</th><td>{{ selected.CarModel }}</td></tr>
                <tr><th>Couleur</th><td>{{ selected.CarColor }}</td></tr>
                <tr><th>Places</th><td>{{ selected.CarSeatNb }}</td></tr>
                <tr><th>Électrique</th><td>{{ selected.IsElectric ? 'Oui' : 'Non' }}</td></tr>
                <tr><th>Flex</th><td>{{ selected.IsVehicleReturnFlex ? 'Oui' : 'Non' }}</td></tr>
                <tr v-if="selected.EnergyLevel !== null"><th>Batterie</th><td>{{ selected.EnergyLevel }}%</td></tr>
                <tr><th>Statut</th><td>{{ bookingStatusLabel(selected.BookingStatus) }}</td></tr>
                <tr><th>Promo</th><td>{{ selected.isPromo ? 'Oui' : 'Non' }}</td></tr>
                <tr v-if="selected.VehiclePromotions?.length">
                  <th>Promotions</th>
                  <td>
                    <div v-for="(p, i) in selected.VehiclePromotions" :key="i">
                      Promo type {{ p.VehiculePromotionType }} (priorité {{ p.PriorityOrder }}{{ p.EndDate ? `, fin: ${formatDate(p.EndDate)}` : '' }})
                    </div>
                  </td>
                </tr>
                <tr><th>Accessoires</th><td>{{ formatAccessories(selected.CarAccessories) }}</td></tr>
                <tr><th>Ordinateur de bord</th><td>{{ selected.BoardComputerType }}</td></tr>
                <tr v-if="selected.CarStationId"><th>Station</th><td>{{ selected.CarStationId }}</td></tr>
                <tr><th>Ville (ID)</th><td>{{ selected.CityID }}</td></tr>
                <tr><th>Dernière utilisation</th><td>{{ formatDate(selected.LastUseDate) }} ({{ selected.LastUse }} min)</td></tr>
                <tr><th>Distance</th><td>{{ formatDistance(selected.distance) }}</td></tr>
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

            <div v-if="isAuthenticated" class="mt-3 d-flex gap-2 align-items-center">
              <button
                class="btn btn-success btn-sm"
                :disabled="booking"
                @click="bookCar(selected!.CarId)"
              >
                {{ booking ? 'Réservation...' : 'Réserver ce véhicule' }}
              </button>
              <button
                v-if="currentBooking"
                class="btn btn-outline-danger btn-sm"
                :disabled="booking"
                @click="cancelCurrentBooking"
              >
                Annuler réservation
              </button>
            </div>
            <div v-if="bookingError" class="alert alert-danger alert-sm mt-2 mb-0 py-1 px-2 small">{{ bookingError }}</div>
            <div v-if="currentBooking" class="alert alert-success alert-sm mt-2 mb-0 py-1 px-2 small">
              Réservation active (Car #{{ currentBooking.CarID || currentBooking.CarId }})
            </div>

            <div v-if="loadingDetails" class="text-muted mt-2">Chargement des détails...</div>
            <div v-if="carDetails" class="mt-3">
              <h6>Détails supplémentaires</h6>
              <table class="table table-sm mb-0">
                <tbody>
                  <tr v-for="(val, key) in carDetails" :key="key">
                    <th>{{ key }}</th>
                    <td>{{ val }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
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
import { ref, computed, onMounted, watch } from 'vue';
import { useGeolocation } from '@/composables/useGeolocation';
import { useVehicles, type VehicleWithDistance } from '@/composables/useVehicles';
import { useAuth } from '@/composables/useAuth';
import { getCarDetails, createBooking, cancelBooking, getCurrentBooking } from '@/services/communautoDataService';

const { userLocation, locationError, locating } = useGeolocation();
const { vehiclesWithDistance, loading, fetchVehicles } = useVehicles(userLocation);
const { isAuthenticated, wcfCookies } = useAuth();

function getUidFromCookies(): number | null {
  const match = wcfCookies.value?.match(/uid=(\d+)/);
  return match ? parseInt(match[1]) : null;
}

const booking = ref(false);
const bookingError = ref<string | null>(null);
const currentBooking = ref<any>(null);

async function bookCar(carId: number) {
  const uid = getUidFromCookies();
  if (!uid) { bookingError.value = 'uid introuvable dans les cookies'; return; }
  booking.value = true;
  bookingError.value = null;
  try {
    const res = await createBooking(uid, carId);
    const data = res.data?.d ?? res.data;
    if (data?.Success) {
      currentBooking.value = data;
    } else {
      bookingError.value = data?.ErrorMessage || 'La réservation a échoué';
    }
  } catch (e: any) {
    bookingError.value = e?.message || 'Erreur réseau';
  } finally {
    booking.value = false;
  }
}

async function cancelCurrentBooking() {
  const uid = getUidFromCookies();
  if (!uid) return;
  booking.value = true;
  bookingError.value = null;
  try {
    const res = await cancelBooking(uid);
    const data = res.data?.d ?? res.data;
    if (data?.Success) {
      currentBooking.value = null;
    } else {
      bookingError.value = data?.ErrorMessage || 'Annulation échouée';
    }
  } catch (e: any) {
    bookingError.value = e?.message || 'Erreur réseau';
  } finally {
    booking.value = false;
  }
}

async function checkCurrentBooking() {
  const uid = getUidFromCookies();
  if (!uid) return;
  try {
    const res = await getCurrentBooking(uid);
    const data = res.data?.d ?? res.data;
    if (data?.Success && data?.CarID) {
      currentBooking.value = data;
    }
  } catch { /* ignore */ }
}

const selected = ref<VehicleWithDistance | null>(null);
const carDetails = ref<any>(null);
const loadingDetails = ref(false);

watch(selected, async (v) => {
  carDetails.value = null;
  if (!v || !isAuthenticated.value) return;
  loadingDetails.value = true;
  try {
    const res = await getCarDetails(v.CarId);
    const data = res.data?.d ?? res.data;
    if (data?.Success && data.Car) {
      carDetails.value = data.Car;
    }
  } catch {
    // ignore — unauthenticated or endpoint unavailable
  } finally {
    loadingDetails.value = false;
  }
});
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

const accessoryMap: Record<number, string> = {
  4: 'Bluetooth',
  16: 'Régulateur de vitesse',
  64: 'Caméra de recul',
  1024: 'Sièges chauffants',
  16384: 'Volant chauffant',
  262144: 'Démarrage à distance',
  2097152: 'Apple CarPlay',
  33554432: 'Android Auto',
  67108864: 'Toit ouvrant',
  268435456: 'Sièges en cuir',
  1073741824: 'Aide au stationnement',
};

function formatAccessories(codes: number[]): string {
  if (!codes?.length) return 'Aucun';
  return codes.map((c) => accessoryMap[c] || `#${c}`).join(', ');
}

function bookingStatusLabel(status: number): string {
  switch (status) {
    case 0: return 'Disponible';
    case 1: return 'Disponible';
    case 2: return 'Réservé';
    case 3: return 'En cours';
    default: return `Inconnu (${status})`;
  }
}

onMounted(() => {
  fetchVehicles();
  if (isAuthenticated.value) checkCurrentBooking();
});
</script>

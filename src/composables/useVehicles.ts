import { ref, computed, type Ref } from 'vue';
import { getAvailableVehicles } from '@/services/communautoDataService';
import type { VehicleV3 } from '@/types/Vehicule';
import { haversineDistance, type LatLng } from './useGeolocation';

export interface VehicleWithDistance extends VehicleV3 {
  distance: number; // meters from user
}

export function useVehicles(userLocation: Ref<LatLng>) {
  const vehicles = ref<VehicleV3[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const vehiclesWithDistance = computed<VehicleWithDistance[]>(() =>
    vehicles.value.map((v) => ({
      ...v,
      distance: haversineDistance(userLocation.value, { lat: v.Latitude, lng: v.Longitude }),
    }))
  );

  async function fetchVehicles() {
    loading.value = true;
    error.value = null;
    try {
      const response = await getAvailableVehicles();
      vehicles.value = response.data.d.Vehicles;
    } catch (e) {
      error.value = 'Erreur lors du chargement des véhicules';
      console.error(e);
    } finally {
      loading.value = false;
    }
  }

  return { vehicles, vehiclesWithDistance, loading, error, fetchVehicles };
}

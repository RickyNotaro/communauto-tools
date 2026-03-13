import { ref, onMounted } from 'vue';

export interface LatLng {
  lat: number;
  lng: number;
}

// Default: Montreal downtown
const DEFAULT_LOCATION: LatLng = { lat: 45.5017, lng: -73.5673 };

export function useGeolocation() {
  const userLocation = ref<LatLng>(DEFAULT_LOCATION);
  const locationError = ref<string | null>(null);
  const locating = ref(true);

  onMounted(() => {
    if (!navigator.geolocation) {
      locationError.value = 'Géolocalisation non supportée par le navigateur';
      locating.value = false;
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        userLocation.value = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };
        locating.value = false;
      },
      (error) => {
        locationError.value = error.message;
        locating.value = false;
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  });

  return { userLocation, locationError, locating };
}

export function haversineDistance(a: LatLng, b: LatLng): number {
  const R = 6371000; // Earth radius in meters
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toRad(b.lat - a.lat);
  const dLng = toRad(b.lng - a.lng);
  const sinLat = Math.sin(dLat / 2);
  const sinLng = Math.sin(dLng / 2);
  const h = sinLat * sinLat + Math.cos(toRad(a.lat)) * Math.cos(toRad(b.lat)) * sinLng * sinLng;
  return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
}

import { ref, computed } from 'vue';
import { Capacitor } from '@capacitor/core';
import { getAvailableVehicles, createBooking } from '@/services/communautoDataService';
import { haversineDistance, type LatLng } from '@/composables/useGeolocation';
import { useAuth } from '@/composables/useAuth';
import {
  requestNotificationPermission,
  showScanningNotification,
  updateScanningNotification,
  clearScanningNotification,
  showBookedNotification,
  showErrorNotification,
} from '@/services/notificationService';
import type { VehicleV3 } from '@/types/Vehicule';

const isNative = Capacitor.isNativePlatform();

// Singleton state — shared across components
const active = ref(false);
const error = ref<string | null>(null);
const success = ref<string | null>(null);
const scanCount = ref(0);

let scanTimer: ReturnType<typeof setInterval> | null = null;
let inProgress = false;

/**
 * Background-capable auto-booking composable.
 *
 * On Android (APK), this shows a persistent notification while scanning
 * and a success notification when a car is booked. The scan continues
 * even when the app is in the background (WebView timers are throttled
 * but still fire on Android).
 */
export function useBackgroundAutoBook() {
  const { wcfCookies } = useAuth();

  function getUidFromCookies(): number | null {
    const match = wcfCookies.value?.match(/uid=(\d+)/);
    return match ? parseInt(match[1]) : null;
  }

  async function start(userLocation: LatLng, radius: number, intervalSeconds: number, electricFilter: 'all' | 'electric' | 'gas') {
    if (active.value) return;

    error.value = null;
    success.value = null;
    scanCount.value = 0;
    active.value = true;

    // Request notification permission on native
    if (isNative) {
      await requestNotificationPermission();
      await showScanningNotification(radius);
    }

    // Run immediately, then on interval
    await scan(userLocation, radius, electricFilter);

    scanTimer = setInterval(() => {
      scan(userLocation, radius, electricFilter);
    }, intervalSeconds * 1000);
  }

  async function stop() {
    active.value = false;
    if (scanTimer) {
      clearInterval(scanTimer);
      scanTimer = null;
    }
    await clearScanningNotification();
  }

  async function scan(userLocation: LatLng, radius: number, electricFilter: 'all' | 'electric' | 'gas') {
    if (!active.value || inProgress) return;
    inProgress = true;

    try {
      scanCount.value++;

      // Fetch vehicles
      const response = await getAvailableVehicles();
      const vehicles: VehicleV3[] = response.data.d.Vehicles;

      // Calculate distances and filter
      const candidates = vehicles
        .map((v) => ({
          ...v,
          distance: haversineDistance(userLocation, { lat: v.Latitude, lng: v.Longitude }),
        }))
        .filter((v) => v.distance <= radius)
        .filter((v) =>
          electricFilter === 'all' ||
          (electricFilter === 'electric' ? v.IsElectric : !v.IsElectric),
        )
        .sort((a, b) => a.distance - b.distance);

      if (isNative && active.value) {
        await updateScanningNotification(
          `Scan #${scanCount.value} — ${candidates.length} véhicule(s) dans ${radius} m`,
        );
      }

      if (candidates.length === 0) {
        // No cars found, will retry on next interval
        return;
      }

      // Try to book
      const uid = getUidFromCookies();
      if (!uid) {
        error.value = 'uid introuvable dans les cookies';
        await stop();
        if (isNative) await showErrorNotification(error.value);
        return;
      }

      for (const car of candidates) {
        try {
          const res = await createBooking(uid, car.CarId);
          const data = res.data?.d ?? res.data;

          if (data?.Success) {
            success.value = `Réservé: #${car.CarNo} — ${car.CarBrand} ${car.CarModel} (${Math.round(car.distance)} m)`;
            active.value = false;
            if (scanTimer) {
              clearInterval(scanTimer);
              scanTimer = null;
            }
            if (isNative) {
              await showBookedNotification(car.CarNo, car.CarBrand, car.CarModel, car.distance);
            }
            return;
          }

          // Booking limit on this car — try next
          if (data?.ErrorType === 3) {
            error.value = `#${car.CarNo} limite atteinte, essai suivant...`;
            continue;
          }

          // Other error — stop
          const errMsg = data?.ErrorMessage || 'Réservation échouée';
          error.value = errMsg;
          await stop();
          if (isNative) await showErrorNotification(errMsg);
          return;
        } catch (e: unknown) {
          const errMsg = e instanceof Error ? e.message : 'Erreur réseau';
          error.value = errMsg;
          await stop();
          if (isNative) await showErrorNotification(errMsg);
          return;
        }
      }

      // All cars had booking limit reached
      error.value = 'Limite atteinte sur tous les véhicules dans le rayon';
    } catch (e: unknown) {
      // Network error during fetch — keep scanning, don't stop
      error.value = e instanceof Error ? e.message : 'Erreur réseau lors du scan';
      if (isNative && active.value) {
        await updateScanningNotification(`Erreur réseau — nouvelle tentative...`);
      }
    } finally {
      inProgress = false;
    }
  }

  return {
    active: computed(() => active.value),
    error: computed(() => error.value),
    success: computed(() => success.value),
    scanCount: computed(() => scanCount.value),
    start,
    stop,
  };
}

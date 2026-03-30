import { Capacitor } from '@capacitor/core';
import { LocalNotifications } from '@capacitor/local-notifications';

const isNative = Capacitor.isNativePlatform();

// Fixed notification IDs
const SCANNING_NOTIFICATION_ID = 9001;
const BOOKED_NOTIFICATION_ID = 9002;

/** Request notification permissions (call once at startup or before first use) */
export async function requestNotificationPermission(): Promise<boolean> {
  if (!isNative) return false;
  const perm = await LocalNotifications.requestPermissions();
  return perm.display === 'granted';
}

/** Show an ongoing notification while scanning for vehicles */
export async function showScanningNotification(radius: number) {
  if (!isNative) return;
  await LocalNotifications.schedule({
    notifications: [
      {
        id: SCANNING_NOTIFICATION_ID,
        title: 'Recherche en cours...',
        body: `Scan automatique dans un rayon de ${radius} m`,
        ongoing: true,
        autoCancel: false,
        smallIcon: 'ic_stat_directions_car',
        largeIcon: 'ic_launcher',
      },
    ],
  });
}

/** Update the scanning notification with current status */
export async function updateScanningNotification(body: string) {
  if (!isNative) return;
  await LocalNotifications.schedule({
    notifications: [
      {
        id: SCANNING_NOTIFICATION_ID,
        title: 'Recherche en cours...',
        body,
        ongoing: true,
        autoCancel: false,
        smallIcon: 'ic_stat_directions_car',
        largeIcon: 'ic_launcher',
      },
    ],
  });
}

/** Remove the scanning notification */
export async function clearScanningNotification() {
  if (!isNative) return;
  await LocalNotifications.cancel({ notifications: [{ id: SCANNING_NOTIFICATION_ID }] });
}

/** Show a notification when a car has been booked */
export async function showBookedNotification(carNo: number | string, carBrand: string, carModel: string, distance: number) {
  if (!isNative) return;
  // Clear the scanning notification first
  await clearScanningNotification();
  await LocalNotifications.schedule({
    notifications: [
      {
        id: BOOKED_NOTIFICATION_ID,
        title: 'Véhicule réservé!',
        body: `#${carNo} — ${carBrand} ${carModel} (${Math.round(distance)} m)`,
        smallIcon: 'ic_stat_directions_car',
        largeIcon: 'ic_launcher',
        sound: 'default',
      },
    ],
  });
}

/** Show an error notification */
export async function showErrorNotification(message: string) {
  if (!isNative) return;
  await clearScanningNotification();
  await LocalNotifications.schedule({
    notifications: [
      {
        id: BOOKED_NOTIFICATION_ID,
        title: 'Auto-réservation arrêtée',
        body: message,
        smallIcon: 'ic_stat_directions_car',
        largeIcon: 'ic_launcher',
      },
    ],
  });
}

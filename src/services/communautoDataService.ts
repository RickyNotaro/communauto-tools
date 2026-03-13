import http from '@/http-common';
import { restApi } from '@/http-common';

// --- Public endpoints (no auth) ---

// https://www.reservauto.net/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles?BranchID=1&LanguageID=1
// BranchID (1=Quebec), LanguageID (1=EN, 2=FR), CityID (e.g. 59=Montreal)
export function getAvailableVehicles(branchId = 1, languageId = 2, cityId?: number) {
  const params: Record<string, number> = { BranchID: branchId, LanguageID: languageId };
  if (cityId !== undefined) {
    params.CityID = cityId;
  }
  return http.get('/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles', { params });
}

// GeoJSON flex zones (no auth)
export function getLSIZones() {
  return http.get('/WCF/Reservauto/Zone/ZonesService.svc/GetLSIZones');
}

// --- Authenticated WCF endpoints (session cookies via proxy) ---

export function getCarDetails(carId: number, languageId = 2) {
  return http.get('/WCF/Reservauto/Car/CarService.svc/Select_Car_V2', {
    params: {
      CarReq: JSON.stringify({ CarID: carId, LanguageID: languageId }),
    },
  });
}

export function createBooking(customerId: number, carId: number) {
  return http.get('/WCF/LSI/LSIBookingServiceV3.svc/CreateBooking', {
    params: { CustomerID: customerId, CarID: carId },
  });
}

export function cancelBooking(customerId: number, branchId = 1) {
  return http.get('/WCF/LSI/LSIBookingServiceV3.svc/CancelBooking', {
    params: { CustomerID: customerId, BranchID: branchId },
  });
}

export function getCurrentBooking(customerId: number) {
  return http.get('/WCF/LSI/LSIBookingServiceV3.svc/GetCurrentBooking', {
    params: { CustomerID: customerId },
  });
}

// --- Authenticated endpoints (REST API with Bearer token) ---

export function getCustomer() {
  return restApi.get('/api/v2/Customer');
}

export function getVehicleAdditionalCost(vehicleId: number, customerId: number, branchId = 1, startDate?: string, endDate?: string) {
  return restApi.get(`/api/v2/Vehicle/${vehicleId}/AdditionalCost`, {
    params: {
      customerId,
      branch: branchId,
      ...(startDate && { startDate }),
      ...(endDate && { endDate }),
    },
  });
}

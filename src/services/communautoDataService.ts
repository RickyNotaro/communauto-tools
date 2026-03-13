import http from '@/http-common';

// https://www.reservauto.net/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles?BranchID=1&LanguageID=1
// BranchID (1=Quebec), LanguageID (1=EN, 2=FR), CityID (e.g. 59=Montreal)
export function getAvailableVehicles(branchId = 1, languageId = 2, cityId?: number) {
  const params: Record<string, number> = { BranchID: branchId, LanguageID: languageId };
  if (cityId !== undefined) {
    params.CityID = cityId;
  }
  return http.get('/WCF/LSI/LSIBookingServiceV3.svc/GetAvailableVehicles', { params });
}

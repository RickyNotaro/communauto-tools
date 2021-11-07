/* eslint-disable class-methods-use-this */
import http from '@/http-common';
// API URL : https://www.reservauto.net/WCF/LSI/Cache/LSIBookingService.svc/GetVehicleProposals?CustomerID=&Longitude=-73.5870264&Latitude=45.4765368&_=1636259690470

class CommunautoDataService {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getVehicleProposals() : Promise<any> {
    return http.get('/WCF/LSI/Cache/LSIBookingService.svc/GetVehicleProposals');
  }
}

export default new CommunautoDataService();

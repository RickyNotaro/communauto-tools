import axios, { AxiosInstance } from 'axios';

const apiClient: AxiosInstance = axios.create({
  baseURL: 'https://www.reservauto.net/',
  withCredentials: false,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});
// API URL : https://www.reservauto.net/WCF/LSI/Cache/LSIBookingService.svc/GetVehicleProposals?CustomerID=&Longitude=-73.5870264&Latitude=45.4765368&_=1636259690470
export default apiClient;

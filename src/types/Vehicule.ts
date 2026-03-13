import type { VehiclePromotion } from './VehiclePromotion';

export interface VehicleV3 {
  CarId: number;
  CarVin: string;
  CarPlate: string;
  CarModel: string;
  CarNo: number;
  CarBrand: string;
  CarColor: string;
  CarSeatNb: number;
  CarAccessories: number[];
  Latitude: number;
  Longitude: number;
  EnergyLevel: number | null;
  IsElectric: boolean;
  LastUseDate: string;
  LastUse: number;
  isPromo: boolean;
  BookingStatus: number;
  BoardComputerType: number;
  CityID: number;
  IsVehicleReturnFlex: boolean;
  CarStationId: number | null;
  VehiclePromotions: VehiclePromotion[] | null;
}

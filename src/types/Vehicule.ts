import VehiclePromotion from './VehiclePromotion';

interface Vehicule {
  HasPromo: boolean;
  VehiculePromotions: Array<VehiclePromotion>;
  ExtensionData: any;
  Id: string;
  Name: string;
  ModelName: number;
  Immat: string;
  EnergyLevel: number;
  Position: {
    ExtensionData: any;
    Latitude: number;
    Longitude: number;
  };
}

export default Vehicule;

/*
"vehicules" : [
  {
  "HasPromo": false,
  "VehiclePromotions": null,
  "ExtensionData": null,
  "Id": "JTDKDTB38G1134944",
  "Name": "3736",
  "ModelName": "Prius C",
  "Immat": "FLP7519",
  "EnergyLevel": 55,
  "Position": {
    "ExtensionData": null,
    "Lat": 46.7954971579063,
    "Lon": -71.2562465922326
    }
  },
  {
      "HasPromo": false,
      "VehiclePromotions": null,
      "ExtensionData": null,
      "Id": "1N4BZ0CP6GC313771",
      "Name": "3866",
      "ModelName": "LEAF",
      "Immat": "FVE2714",
      "EnergyLevel": 85,
      "Position": {
        "ExtensionData": null,
        "Lat": 45.5013921921736,
        "Lon": -73.5707581389326
      }
    },
    {
      "HasPromo": true,
      "VehiclePromotions": [
        {
          "VehiclePromotionType": 1,
          "EndDate": null,
          "PriorityOrder": 1
        }
      ],
      "ExtensionData": null,
      "Id": "JTDKDTB38H1595644",
      "Name": "3948",
      "ModelName": "Prius C",
      "Immat": "FMJ1802",
      "EnergyLevel": 59,
      "Position": {
        "ExtensionData": null,
        "Lat": 45.5870888519433,
        "Lon": -73.509101531248
      }
    }
]
*/

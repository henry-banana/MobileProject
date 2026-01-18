export interface DeliveryAddress {
  street: string;
  ward: string;
  district: string;
  city: string;
  coordinates?: {
    lat: number;
    lng: number;
  };
}

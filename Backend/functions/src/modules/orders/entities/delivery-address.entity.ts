/**
 * Delivery Address Entity - Snapshot format
 * Supports both KTX format (recommended) and legacy format (backward compatibility)
 */
export interface DeliveryAddress {
  // Recommended KTX format (AddressBook style)
  id?: string; // Reference ID if from address book
  label?: string; // "Nhà", "Phòng ký túc xá"
  fullAddress?: string; // "KTX Khu B - Tòa B5"
  building?: string; // "B5"
  room?: string; // "101"
  note?: string; // "Gọi trước khi đến"

  // Legacy format (deprecated but supported)
  street?: string;
  ward?: string;
  district?: string;
  city?: string;
  coordinates?: {
    lat: number;
    lng: number;
  };
}

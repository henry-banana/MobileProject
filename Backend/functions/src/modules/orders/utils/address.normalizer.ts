/**
 * Address Normalization Utilities
 * Ensures delivery addresses are Firestore-safe (no undefined values)
 * Supports both KTX snapshot format and legacy format
 */

import { DeliveryAddress } from '../entities/delivery-address.entity';

/**
 * Remove undefined values recursively from an object
 * Only used for order documents before Firestore save
 * Does not mutate the original object
 */
export function removeUndefinedDeep<T extends Record<string, any>>(obj: T): Partial<T> {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map((item) => removeUndefinedDeep(item)) as any;
  }

  const cleaned: any = {};

  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      const value = obj[key];

      // Skip undefined values entirely
      if (value === undefined) {
        continue;
      }

      // Recursively clean nested objects
      if (value !== null && typeof value === 'object') {
        cleaned[key] = removeUndefinedDeep(value);
      } else {
        cleaned[key] = value;
      }
    }
  }

  return cleaned;
}

/**
 * Normalize delivery address to be Firestore-safe
 * Accepts both KTX snapshot format and legacy format
 * Returns object with only defined values (no undefined keys)
 *
 * RECOMMENDED INPUT (KTX format):
 * { label, fullAddress, building, room, note }
 *
 * LEGACY INPUT (still supported):
 * { street, ward, district, city, coordinates }
 *
 * MIXED (both formats):
 * { label, fullAddress, street, ward, ... }
 * => Only includes fields that are defined
 */
export function normalizeDeliveryAddress(input: DeliveryAddress | undefined): DeliveryAddress {
  if (!input) {
    return {};
  }

  const normalized: DeliveryAddress = {};

  // KTX format fields (preferred, no undefined)
  if (input.id !== undefined) {
    normalized.id = input.id;
  }
  if (input.label !== undefined) {
    normalized.label = input.label;
  }
  if (input.fullAddress !== undefined) {
    normalized.fullAddress = input.fullAddress;
  }
  if (input.building !== undefined) {
    normalized.building = input.building;
  }
  if (input.room !== undefined) {
    normalized.room = input.room;
  }
  if (input.note !== undefined) {
    normalized.note = input.note;
  }

  // Legacy format fields (only if defined, maintain backward compatibility)
  if (input.street !== undefined) {
    normalized.street = input.street;
  }
  if (input.ward !== undefined) {
    normalized.ward = input.ward;
  }
  if (input.district !== undefined) {
    normalized.district = input.district;
  }
  if (input.city !== undefined) {
    normalized.city = input.city;
  }
  if (input.coordinates !== undefined) {
    normalized.coordinates = input.coordinates;
  }

  return normalized;
}

/**
 * Merge delivery note from multiple sources
 * Priority: dto.deliveryNote > address.note > undefined
 */
export function mergeDeliveryNote(
  dtoNote: string | undefined,
  addressNote: string | undefined,
): string | undefined {
  return dtoNote || addressNote;
}

import { Timestamp } from 'firebase-admin/firestore';

/**
 * Timestamp Serialization Utility
 *
 * Converts Firestore Timestamp, Date, string, or null to ISO-8601 string format.
 * Ensures consistent API responses without leaking Firestore Timestamp objects.
 *
 * Usage:
 *   toIsoString(order.createdAt) // Timestamp -> "2026-01-18T15:12:20.059Z"
 *   toIsoString(new Date()) // Date -> "2026-01-18T15:12:20.059Z"
 *   toIsoString("2026-01-18T15:12:20Z") // String -> "2026-01-18T15:12:20Z"
 *   toIsoString(null) // null -> undefined
 */

/**
 * Convert Firestore Timestamp or Date to ISO-8601 string
 *
 * @param timestamp Firestore Timestamp, Date, string, or null/undefined
 * @returns ISO-8601 string or undefined if input is null/undefined
 *
 * Examples:
 *   toIsoString(Timestamp.now()) // "2026-01-18T15:12:20.059Z"
 *   toIsoString(new Date('2026-01-18T10:00:00Z')) // "2026-01-18T10:00:00.000Z"
 *   toIsoString("2026-01-18T10:00:00Z") // "2026-01-18T10:00:00Z"
 *   toIsoString({ _seconds: 1705591340, _nanoseconds: 59000000 }) // "2026-01-18T15:12:20.059Z"
 *   toIsoString(null) // undefined
 *   toIsoString(undefined) // undefined
 */
export function toIsoString(timestamp: Timestamp | Date | string | null | undefined | any): string | undefined {
  if (timestamp === null || timestamp === undefined) {
    return undefined;
  }

  // Firestore Timestamp
  if (timestamp instanceof Timestamp) {
    return timestamp.toDate().toISOString();
  }

  // JavaScript Date
  if (timestamp instanceof Date) {
    return timestamp.toISOString();
  }

  // Already a string
  if (typeof timestamp === 'string') {
    return timestamp;
  }

  // Plain object shaped like Firestore Timestamp: { _seconds: number, _nanoseconds: number }
  if (
    typeof timestamp === 'object' &&
    typeof timestamp._seconds === 'number' &&
    typeof timestamp._nanoseconds === 'number'
  ) {
    // Convert Firestore seconds + nanoseconds to ISO string
    const milliseconds = timestamp._seconds * 1000 + Math.floor(timestamp._nanoseconds / 1000000);
    return new Date(milliseconds).toISOString();
  }

  // Fallback: return undefined for unknown types
  return undefined;
}

/**
 * Recursively convert all timestamp fields in an object to ISO strings
 *
 * Searches for fields ending with "At" or "Timestamp" and converts their values.
 * Mutates the input object in place.
 *
 * @param obj Object to process
 * @returns The modified object with ISO timestamps
 *
 * Example:
 *   const order = {
 *     id: 'order_123',
 *     createdAt: Timestamp.now(),
 *     updatedAt: Timestamp.now(),
 *     items: [
 *       { productId: 'p1', addedAt: Timestamp.now() }
 *     ]
 *   };
 *
 *   serializeTimestamps(order);
 *   // Result:
 *   // {
 *   //   id: 'order_123',
 *   //   createdAt: "2026-01-18T15:12:20.059Z",
 *   //   updatedAt: "2026-01-18T15:12:20.059Z",
 *   //   items: [
 *   //     { productId: 'p1', addedAt: "2026-01-18T15:12:20.059Z" }
 *   //   ]
 *   // }
 */
export function serializeTimestamps<T extends Record<string, any>>(obj: T): T {
  if (!obj || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return (obj.map((item: any) => serializeTimestamps(item)) as unknown) as T;
  }

  const serialized: Record<string, any> = { ...obj };

  for (const key in serialized) {
    if (Object.prototype.hasOwnProperty.call(serialized, key)) {
      const value = serialized[key];

      // Check if field name suggests it's a timestamp
      if (key.endsWith('At') || key.endsWith('Timestamp')) {
        serialized[key] = toIsoString(value);
      }
      // Recursively process nested objects and arrays
      else if (value && typeof value === 'object') {
        if (Array.isArray(value)) {
          serialized[key] = value.map((item: any) =>
            typeof item === 'object' && item !== null ? serializeTimestamps(item) : item
          );
        } else {
          serialized[key] = serializeTimestamps(value);
        }
      }
    }
  }

  return (serialized as unknown) as T;
}

/**
 * Create a class decorator factory for automatic timestamp serialization
 *
 * Usage:
 *   @SerializeTimestamps()
 *   class OrderListItemDto {
 *     createdAt: string;
 *     updatedAt: string;
 *   }
 *
 * When converting to/from JSON, timestamps are automatically serialized.
 */
export function SerializeTimestamps() {
  return function <T extends { new (...args: any[]): {} }>(constructor: T) {
    return class extends constructor {
      toJSON() {
        return serializeTimestamps(this as any);
      }
    };
  };
}

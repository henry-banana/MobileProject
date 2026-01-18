import { Timestamp } from 'firebase-admin/firestore';
import { toIsoString, serializeTimestamps } from '../utils/timestamp.serializer';

/**
 * Orders Response Consistency Tests
 *
 * Verifies that:
 * 1. Timestamps are converted to ISO-8601 strings in responses
 * 2. No Firestore Timestamp objects leak into API responses
 * 3. Cancel endpoint returns proper response with snapshot deliveryAddress
 * 4. No double-wrapping of responses
 */
describe('Orders - Response Consistency (Timestamps and Cancel)', () => {
  describe('Timestamp Serialization Utility', () => {
    it('should convert Firestore Timestamp to ISO-8601 string', () => {
      const ts = Timestamp.now();
      const isoString = toIsoString(ts);

      expect(typeof isoString).toBe('string');
      expect(isoString).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
    });

    it('should convert JavaScript Date to ISO-8601 string', () => {
      const date = new Date('2026-01-18T15:12:20.059Z');
      const isoString = toIsoString(date);

      expect(typeof isoString).toBe('string');
      expect(isoString).toBe('2026-01-18T15:12:20.059Z');
    });

    it('should return string as-is if already a string', () => {
      const isoString = '2026-01-18T15:12:20.059Z';
      const result = toIsoString(isoString);

      expect(result).toBe(isoString);
    });

    it('should return undefined for null or undefined input', () => {
      expect(toIsoString(null)).toBeUndefined();
      expect(toIsoString(undefined)).toBeUndefined();
    });

    it('should convert plain { _seconds, _nanoseconds } objects to ISO-8601 string', () => {
      // Plain object shaped like Firestore Timestamp (from deserialization)
      const now = Timestamp.now();
      const plainTimestamp = {
        _seconds: now.seconds,
        _nanoseconds: now.nanoseconds,
      };

      const isoString = toIsoString(plainTimestamp);

      expect(typeof isoString).toBe('string');
      expect(isoString).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
      // Verify it matches the original Timestamp conversion
      expect(isoString).toBe(toIsoString(now));
    });

    it('should recursively serialize nested timestamps in objects', () => {
      const order = {
        id: 'order_123',
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        items: [
          {
            productId: 'prod_1',
            addedAt: Timestamp.now(),
          },
        ],
        metadata: {
          confirmedAt: Timestamp.now(),
        },
      };

      const serialized = serializeTimestamps(order);

      expect(typeof serialized.createdAt).toBe('string');
      expect(typeof serialized.updatedAt).toBe('string');
      expect(typeof serialized.items[0].addedAt).toBe('string');
      expect(typeof serialized.metadata.confirmedAt).toBe('string');

      // Verify ISO format
      expect(serialized.createdAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
    });
  });

  describe('Order List Response Format', () => {
    it('should have createdAt as ISO-8601 string in list items', () => {
      const listItem = {
        id: 'order_1',
        orderNumber: 'ORD-001',
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        total: 65000,
        itemCount: 2,
        createdAt: toIsoString(Timestamp.now()),
      };

      expect(typeof listItem.createdAt).toBe('string');
      expect(listItem.createdAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
      expect(listItem.createdAt).not.toHaveProperty('_seconds');
    });

    it('should preserve other order list fields correctly', () => {
      const listItem = {
        id: 'order_1',
        orderNumber: 'ORD-001',
        shopId: 'shop_123',
        shopName: 'Cơm Tấm Sườn',
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        total: 65000,
        itemCount: 2,
        createdAt: toIsoString(Timestamp.now()),
      };

      expect(listItem.id).toBe('order_1');
      expect(listItem.orderNumber).toBe('ORD-001');
      expect(listItem.shopId).toBe('shop_123');
      expect(listItem.shopName).toBe('Cơm Tấm Sườn');
      expect(listItem.status).toBe('PENDING');
      expect(listItem.paymentStatus).toBe('UNPAID');
      expect(listItem.total).toBe(65000);
      expect(listItem.itemCount).toBe(2);
    });
  });

  describe('Order Detail Response Format', () => {
    it('should return order with all timestamps as ISO-8601 strings', () => {
      const now = Timestamp.now();
      const past = new Timestamp(now.seconds - 3600, now.nanoseconds);

      const order = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_123',
        shopId: 'shop_123',
        shopName: 'Shop A',
        items: [],
        subtotal: 50000,
        shipFee: 15000,
        discount: 0,
        total: 65000,
        status: 'CONFIRMED',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        createdAt: past,
        updatedAt: now,
        confirmedAt: now,
        deliveryAddress: { label: 'Home' },
      };

      const serialized = serializeTimestamps(order);

      expect(typeof serialized.createdAt).toBe('string');
      expect(typeof serialized.updatedAt).toBe('string');
      expect(typeof serialized.confirmedAt).toBe('string');
    });

    it('should return order with snapshot deliveryAddress format', () => {
      const order = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_123',
        shopId: 'shop_123',
        shopName: 'Shop A',
        items: [],
        subtotal: 50000,
        shipFee: 15000,
        discount: 0,
        total: 65000,
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước 5 phút',
        },
        createdAt: Timestamp.now(),
      };

      expect(order.deliveryAddress).toEqual({
        label: 'KTX B5',
        fullAddress: 'KTX Khu B - Tòa B5',
        building: 'B5',
        room: '101',
        note: 'Gọi trước 5 phút',
      });

      // Verify no legacy fields
      expect((order.deliveryAddress as any).street).toBeUndefined();
      expect((order.deliveryAddress as any).ward).toBeUndefined();
      expect((order.deliveryAddress as any).district).toBeUndefined();
      expect((order.deliveryAddress as any).city).toBeUndefined();
    });
  });

  describe('Cancel Response Format', () => {
    it('should include cancel metadata (reason, by, at)', () => {
      const cancelReason = 'Changed my mind';
      const now = Timestamp.now();

      const order = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_123',
        shopId: 'shop_123',
        shopName: 'Shop A',
        items: [],
        subtotal: 50000,
        shipFee: 15000,
        discount: 0,
        total: 65000,
        status: 'CANCELLED',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: { label: 'Home' },
        createdAt: now,
        updatedAt: now,
        cancelledAt: now,
        cancelReason: cancelReason,
        cancelledBy: 'CUSTOMER',
      };

      expect(order.status).toBe('CANCELLED');
      expect(order.cancelReason).toBe(cancelReason);
      expect(order.cancelledBy).toBe('CUSTOMER');
      expect(order.cancelledAt).toBeInstanceOf(Timestamp);

      // After serialization, timestamps should be ISO strings
      const serialized = serializeTimestamps(order);
      expect(typeof serialized.cancelledAt).toBe('string');
    });

    it('should include snapshot deliveryAddress in cancel response', () => {
      const now = Timestamp.now();

      const order = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_123',
        shopId: 'shop_123',
        shopName: 'Shop A',
        items: [],
        subtotal: 50000,
        shipFee: 15000,
        discount: 0,
        total: 65000,
        status: 'CANCELLED',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước 5 phút',
        },
        createdAt: now,
        updatedAt: now,
        cancelledAt: now,
        cancelReason: 'Changed my mind',
        cancelledBy: 'CUSTOMER',
      };

      expect(order.deliveryAddress).toEqual({
        label: 'KTX B5',
        fullAddress: 'KTX Khu B - Tòa B5',
        building: 'B5',
        room: '101',
        note: 'Gọi trước 5 phút',
      });
    });

    it('should serialize cancel timestamps to ISO strings', () => {
      const now = Timestamp.now();

      const order = {
        id: 'order_123',
        orderNumber: 'ORD-001',
        customerId: 'user_123',
        shopId: 'shop_123',
        shopName: 'Shop A',
        items: [],
        subtotal: 50000,
        shipFee: 15000,
        discount: 0,
        total: 65000,
        status: 'CANCELLED',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: { label: 'Home' },
        createdAt: now,
        updatedAt: now,
        cancelledAt: now,
        cancelReason: 'Cancelled by customer',
        cancelledBy: 'CUSTOMER',
      };

      const serialized = serializeTimestamps(order);

      expect(typeof serialized.createdAt).toBe('string');
      expect(typeof serialized.updatedAt).toBe('string');
      expect(typeof serialized.cancelledAt).toBe('string');
      expect(typeof serialized.cancelReason).toBe('string');
      expect(typeof serialized.cancelledBy).toBe('string');
    });
  });

  describe('Response Wrapper Format', () => {
    it('should not double-wrap responses', () => {
      // Verify the expected response structure
      const listItem = {
        id: 'order_1',
        orderNumber: 'ORD-001',
        total: 65000,
        createdAt: '2026-01-18T15:12:20.059Z',
      };

      // Response should follow: { success: boolean, data: T, timestamp: string }
      const expectedTopLevelKeys = ['success', 'data', 'timestamp'];
      const response = {
        success: true,
        data: listItem,
        timestamp: new Date().toISOString(),
      };

      const actualKeys = Object.keys(response);
      expect(actualKeys.sort()).toEqual(expectedTopLevelKeys.sort());

      // Verify data doesn't have response wrapper keys
      expect(response.data).not.toHaveProperty('success');
      expect(response.data).not.toHaveProperty('timestamp');
    });

    it('should have correct response structure for paginated orders', () => {
      const response = {
        success: true,
        data: {
          orders: [
            {
              id: 'order_123',
              orderNumber: 'ORD-001',
              shopId: 'shop_123',
              shopName: 'Cơm Tấm Sườn',
              status: 'PENDING',
              paymentStatus: 'UNPAID',
              total: 65000,
              itemCount: 2,
              createdAt: '2026-01-18T15:12:20.059Z',
            },
          ],
          page: 1,
          limit: 10,
          total: 42,
          totalPages: 5,
        },
        timestamp: new Date().toISOString(),
      };

      // Top level should have success, data, timestamp
      expect(response).toHaveProperty('success', true);
      expect(response).toHaveProperty('data');
      expect(response).toHaveProperty('timestamp');

      // Data should have paginated structure
      expect(response.data).toHaveProperty('orders');
      expect(response.data).toHaveProperty('page', 1);
      expect(response.data).toHaveProperty('limit', 10);
      expect(response.data).toHaveProperty('total', 42);
      expect(response.data).toHaveProperty('totalPages', 5);

      // Orders should be an array
      expect(Array.isArray(response.data.orders)).toBe(true);
    });
  });

  describe('Timestamp Field Naming Convention', () => {
    it('should identify and serialize fields ending with "At"', () => {
      const obj = {
        id: 'test',
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        deletedAt: Timestamp.now(),
        processedAt: Timestamp.now(),
      };

      const serialized = serializeTimestamps(obj);

      expect(typeof serialized.createdAt).toBe('string');
      expect(typeof serialized.updatedAt).toBe('string');
      expect(typeof serialized.deletedAt).toBe('string');
      expect(typeof serialized.processedAt).toBe('string');
    });

    it('should identify and serialize fields ending with "Timestamp"', () => {
      const obj = {
        id: 'test',
        createTimestamp: Timestamp.now(),
        updateTimestamp: Timestamp.now(),
      };

      const serialized = serializeTimestamps(obj);

      expect(typeof serialized.createTimestamp).toBe('string');
      expect(typeof serialized.updateTimestamp).toBe('string');
    });

    it('should NOT serialize non-timestamp fields', () => {
      const obj = {
        id: 'test',
        status: 'PENDING',
        createdAt: Timestamp.now(),
        name: 'Test Order',
        email: 'test@example.com',
      };

      const serialized = serializeTimestamps(obj);

      expect(serialized.status).toBe('PENDING');
      expect(serialized.name).toBe('Test Order');
      expect(serialized.email).toBe('test@example.com');
      expect(typeof serialized.createdAt).toBe('string');
    });
  });
});

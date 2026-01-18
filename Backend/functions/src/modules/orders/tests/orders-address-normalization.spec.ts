import { normalizeDeliveryAddress, removeUndefinedDeep } from '../utils/address.normalizer';
import { DeliveryAddress } from '../entities';

describe('Orders - Firestore Address Normalization', () => {
  describe('normalizeDeliveryAddress', () => {
    describe('KTX snapshot format (new)', () => {
      it('should accept snapshot with label, fullAddress, building, room, note', () => {
        const snapshot: DeliveryAddress = {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        };

        const normalized = normalizeDeliveryAddress(snapshot);

        expect(normalized).toEqual({
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        });
        expect(normalized.street).toBeUndefined();
        expect(normalized.ward).toBeUndefined();
        expect(normalized.district).toBeUndefined();
        expect(normalized.city).toBeUndefined();
      });

      it('should handle partial snapshot (no optional fields)', () => {
        const snapshot: DeliveryAddress = {
          fullAddress: 'Số 123 Lý Thường Kiệt',
        };

        const normalized = normalizeDeliveryAddress(snapshot);

        expect(normalized).toEqual({
          fullAddress: 'Số 123 Lý Thường Kiệt',
        });
        expect(Object.keys(normalized)).toHaveLength(1);
      });

      it('should handle snapshot with id reference', () => {
        const snapshot: DeliveryAddress = {
          id: 'addr_abc123',
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          room: '101',
        };

        const normalized = normalizeDeliveryAddress(snapshot);

        expect(normalized).toEqual({
          id: 'addr_abc123',
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          room: '101',
        });
      });

      it('should NOT include undefined legacy fields', () => {
        const snapshot: DeliveryAddress = {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          // street, ward, district, city are NOT included
        };

        const normalized = normalizeDeliveryAddress(snapshot);

        // Should only have KTX fields
        expect(normalized).toEqual({
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
        });

        // Verify legacy fields are completely absent (not even as undefined)
        const keys = Object.keys(normalized);
        expect(keys).not.toContain('street');
        expect(keys).not.toContain('ward');
        expect(keys).not.toContain('district');
        expect(keys).not.toContain('city');
      });
    });

    describe('Legacy format (backward compatibility)', () => {
      it('should support legacy street, ward, district, city', () => {
        const legacy: DeliveryAddress = {
          street: '123 Nguyen Hue',
          ward: 'Ben Nghe',
          district: 'District 1',
          city: 'Ho Chi Minh City',
        };

        const normalized = normalizeDeliveryAddress(legacy);

        expect(normalized).toEqual({
          street: '123 Nguyen Hue',
          ward: 'Ben Nghe',
          district: 'District 1',
          city: 'Ho Chi Minh City',
        });
      });

      it('should support legacy with coordinates', () => {
        const legacy: DeliveryAddress = {
          street: '123 Nguyen Hue',
          city: 'Ho Chi Minh City',
          coordinates: { lat: 10.7769, lng: 106.7009 },
        };

        const normalized = normalizeDeliveryAddress(legacy);

        expect(normalized).toEqual({
          street: '123 Nguyen Hue',
          city: 'Ho Chi Minh City',
          coordinates: { lat: 10.7769, lng: 106.7009 },
        });
      });

      it('should NOT include undefined legacy fields', () => {
        const legacy: DeliveryAddress = {
          street: '123 Nguyen Hue',
          // ward, district, city NOT provided
        };

        const normalized = normalizeDeliveryAddress(legacy);

        expect(normalized).toEqual({
          street: '123 Nguyen Hue',
        });

        // Verify missing fields are not present
        const keys = Object.keys(normalized);
        expect(keys).toEqual(['street']);
      });
    });

    describe('Mixed format (both new and legacy)', () => {
      it('should merge new and legacy fields if both provided', () => {
        const mixed: DeliveryAddress = {
          // New fields
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          room: '101',
          // Legacy fields (for backward compatibility)
          street: 'Legacy Street Name',
          ward: 'Ben Nghe',
        };

        const normalized = normalizeDeliveryAddress(mixed);

        expect(normalized).toEqual({
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          room: '101',
          street: 'Legacy Street Name',
          ward: 'Ben Nghe',
        });
      });

      it('should only include defined fields in mixed format', () => {
        const mixed: DeliveryAddress = {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          street: 'Legacy Street',
          // ward is NOT provided
          // district is NOT provided
          city: 'Ho Chi Minh City',
        };

        const normalized = normalizeDeliveryAddress(mixed);

        expect(normalized).toEqual({
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          street: 'Legacy Street',
          city: 'Ho Chi Minh City',
        });

        // Should NOT have ward or district keys at all
        const keys = Object.keys(normalized);
        expect(keys).not.toContain('ward');
        expect(keys).not.toContain('district');
      });
    });

    describe('Empty/undefined input', () => {
      it('should return empty object for undefined input', () => {
        const normalized = normalizeDeliveryAddress(undefined);
        expect(normalized).toEqual({});
      });

      it('should return empty object for null-like input', () => {
        const normalized = normalizeDeliveryAddress(null as any);
        expect(normalized).toEqual({});
      });

      it('should handle object with all undefined fields', () => {
        const allUndefined: DeliveryAddress = {};

        const normalized = normalizeDeliveryAddress(allUndefined);

        expect(normalized).toEqual({});
        expect(Object.keys(normalized)).toHaveLength(0);
      });
    });
  });

  describe('removeUndefinedDeep', () => {
    it('should remove undefined values from order document', () => {
      const order = {
        id: 'order_123',
        customerId: 'user_456',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: undefined, // Should be removed
          street: undefined, // Should be removed
          ward: undefined, // Should be removed
          district: undefined, // Should be removed
          city: undefined, // Should be removed
        },
        deliveryNote: undefined, // Should be removed
        items: [
          {
            productId: 'prod_1',
            productName: 'Cơm',
            quantity: 2,
            price: 25000,
            subtotal: 50000,
          },
        ],
      };

      const cleaned = removeUndefinedDeep(order);

      expect(cleaned).toEqual({
        id: 'order_123',
        customerId: 'user_456',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
        },
        items: [
          {
            productId: 'prod_1',
            productName: 'Cơm',
            quantity: 2,
            price: 25000,
            subtotal: 50000,
          },
        ],
      });

      // Verify specific fields are not present
      expect(cleaned.deliveryNote).toBeUndefined();
      expect((cleaned.deliveryAddress as any)?.street).toBeUndefined();
      expect((cleaned.deliveryAddress as any)?.ward).toBeUndefined();
      expect((cleaned.deliveryAddress as any)?.district).toBeUndefined();
      expect((cleaned.deliveryAddress as any)?.city).toBeUndefined();
    });

    it('should handle arrays of objects', () => {
      const data = {
        items: [
          { name: 'Item 1', value: 100, extra: undefined },
          { name: 'Item 2', value: 200, extra: undefined },
        ],
      };

      const cleaned = removeUndefinedDeep(data);

      expect(cleaned).toEqual({
        items: [
          { name: 'Item 1', value: 100 },
          { name: 'Item 2', value: 200 },
        ],
      });
    });

    it('should handle nested objects', () => {
      const data = {
        user: {
          id: 'user_1',
          profile: {
            name: 'John',
            bio: undefined,
            avatar: undefined,
          },
        },
      };

      const cleaned = removeUndefinedDeep(data);

      expect(cleaned).toEqual({
        user: {
          id: 'user_1',
          profile: {
            name: 'John',
          },
        },
      });
    });

    it('should preserve null values (only remove undefined)', () => {
      const data = {
        name: 'John',
        email: null,
        phone: undefined,
      };

      const cleaned = removeUndefinedDeep(data);

      expect(cleaned).toEqual({
        name: 'John',
        email: null,
      });
      expect(cleaned.phone).toBeUndefined();
    });

    it('should preserve 0, false, empty string', () => {
      const data = {
        count: 0,
        isActive: false,
        description: '',
        note: undefined,
      };

      const cleaned = removeUndefinedDeep(data);

      expect(cleaned).toEqual({
        count: 0,
        isActive: false,
        description: '',
      });
    });

    it('should not mutate original object', () => {
      const original = {
        name: 'John',
        phone: undefined,
        address: {
          street: '123 Main St',
          zip: undefined,
        },
      };

      const cleaned = removeUndefinedDeep(original);

      // Original should still have undefined values
      expect(original.phone).toBeUndefined();
      expect((original.address as any)?.zip).toBeUndefined();

      // Cleaned should not have them
      expect(cleaned.phone).toBeUndefined();
      expect((cleaned.address as any)?.zip).toBeUndefined();

      // But keys should not exist in cleaned
      expect('phone' in cleaned).toBe(false);
      expect('zip' in (cleaned.address as any)).toBe(false);
    });
  });

  describe('Integration - Order document before Firestore save', () => {
    it('should create clean Firestore document with snapshot address', () => {
      const orderEntity = {
        orderNumber: 'ORD-123',
        customerId: 'user_1',
        shopId: 'shop_1',
        shopName: 'Cơm Nhà A',
        items: [
          {
            productId: 'prod_1',
            productName: 'Cơm',
            quantity: 2,
            price: 25000,
            subtotal: 50000,
          },
        ],
        subtotal: 50000,
        shipFee: 5000,
        discount: 0,
        total: 55000,
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: normalizeDeliveryAddress({
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        }),
        deliveryNote: undefined,
      };

      const cleaned = removeUndefinedDeep(orderEntity);

      // Should have no undefined values at any level
      JSON.stringify(cleaned); // This would fail if there were undefined values

      expect(cleaned).toEqual({
        orderNumber: 'ORD-123',
        customerId: 'user_1',
        shopId: 'shop_1',
        shopName: 'Cơm Nhà A',
        items: [
          {
            productId: 'prod_1',
            productName: 'Cơm',
            quantity: 2,
            price: 25000,
            subtotal: 50000,
          },
        ],
        subtotal: 50000,
        shipFee: 5000,
        discount: 0,
        total: 55000,
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: {
          label: 'KTX B5',
          fullAddress: 'KTX Khu B - Tòa B5',
          building: 'B5',
          room: '101',
          note: 'Gọi trước khi đến',
        },
      });

      // Verify no legacy fields in deliveryAddress
      const keys = Object.keys(cleaned.deliveryAddress as any);
      expect(keys).not.toContain('street');
      expect(keys).not.toContain('ward');
      expect(keys).not.toContain('district');
      expect(keys).not.toContain('city');
    });

    it('should create clean document with legacy address', () => {
      const orderEntity = {
        orderNumber: 'ORD-124',
        customerId: 'user_1',
        shopId: 'shop_1',
        shopName: 'Cơm Nhà A',
        items: [],
        subtotal: 50000,
        shipFee: 5000,
        discount: 0,
        total: 55000,
        status: 'PENDING',
        paymentStatus: 'UNPAID',
        paymentMethod: 'COD',
        deliveryAddress: normalizeDeliveryAddress({
          street: '123 Nguyen Hue',
          ward: 'Ben Nghe',
          district: 'District 1',
          city: 'Ho Chi Minh City',
          coordinates: { lat: 10.7769, lng: 106.7009 },
        }),
        deliveryNote: undefined,
      };

      const cleaned = removeUndefinedDeep(orderEntity);

      // Should preserve legacy format
      expect(cleaned.deliveryAddress).toEqual({
        street: '123 Nguyen Hue',
        ward: 'Ben Nghe',
        district: 'District 1',
        city: 'Ho Chi Minh City',
        coordinates: { lat: 10.7769, lng: 106.7009 },
      });

      // Should not have new format fields
      const keys = Object.keys(cleaned.deliveryAddress as any);
      expect(keys).not.toContain('label');
      expect(keys).not.toContain('fullAddress');
      expect(keys).not.toContain('building');
      expect(keys).not.toContain('room');
    });
  });
});

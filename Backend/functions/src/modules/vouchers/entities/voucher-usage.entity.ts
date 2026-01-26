/**
 * Voucher Usage Entity
 * Collection: voucherUsages
 */
export class VoucherUsageEntity {
  id: string; // Deterministic: {voucherId}_{userId}_{orderId}
  voucherId: string;
  shopId: string | null; // Denormalized from voucher for efficient filtering (nullable for future ADMIN vouchers)
  userId: string;
  orderId: string;
  discountAmount: number;
  createdAt: string; // ISO 8601
}

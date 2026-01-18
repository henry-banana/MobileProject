import { Timestamp } from 'firebase-admin/firestore';
import { OrderItem } from './order-item.entity';
import { DeliveryAddress } from './delivery-address.entity';

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PREPARING = 'PREPARING',
  READY = 'READY',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export enum PaymentStatus {
  UNPAID = 'UNPAID',
  PROCESSING = 'PROCESSING',
  PAID = 'PAID',
  REFUNDED = 'REFUNDED',
}

export class OrderEntity {
  id?: string;
  orderNumber: string;

  // Relations
  customerId: string;
  shopId: string;
  shopName: string;
  shipperId?: string;

  // Items (locked from cart)
  items: OrderItem[];

  // Amounts
  subtotal: number;
  shipFee: number;
  discount: number;
  total: number;

  // Status
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: 'COD' | 'ZALOPAY' | 'MOMO' | 'SEPAY';

  // Timestamps
  createdAt?: Timestamp;
  updatedAt?: Timestamp;
  confirmedAt?: Timestamp;
  preparingAt?: Timestamp;
  readyAt?: Timestamp;
  shippingAt?: Timestamp;
  deliveredAt?: Timestamp;
  cancelledAt?: Timestamp;

  // Delivery
  deliveryAddress: DeliveryAddress;
  deliveryNote?: string;

  // Cancellation
  cancelReason?: string;
  cancelledBy?: 'CUSTOMER' | 'OWNER' | 'SYSTEM';

  // Review
  reviewId?: string;
  reviewedAt?: Timestamp;

  // Payout
  paidOut?: boolean;
  paidOutAt?: Timestamp;
}

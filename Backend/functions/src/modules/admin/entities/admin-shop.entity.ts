import { Timestamp } from 'firebase-admin/firestore';
import { IBaseEntity } from '../../../core/database';

/**
 * Shop Status
 * NOTE: Phải match với DTO enum values (UPPERCASE)
 */
export enum ShopStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  BANNED = 'BANNED',
  CLOSED = 'CLOSED',
}

/**
 * Admin Shop Entity - Entity cho admin quản lý shops
 *
 * NOTE: Đây là entity dùng trong Admin module.
 * Khi implement EPIC 04 (Shop), entity đầy đủ sẽ ở modules/shops/
 *
 * Collection: shops
 */
export interface AdminShopEntity extends IBaseEntity {
  /** Owner ID */
  ownerId: string;

  /** Shop name */
  name: string;

  /** Shop description */
  description?: string;

  /** Logo URL */
  logoUrl?: string;

  /** Cover image URL */
  coverUrl?: string;

  /** Shop address */
  address: string;

  /** Phone number */
  phone: string;

  /** Shop status */
  status: ShopStatus;

  /** Is verified by admin */
  isVerified: boolean;

  /** Rating */
  rating: number;
  ratingCount: number;

  /** Order count */
  totalOrders: number;

  /** Revenue */
  totalRevenue: number;

  /** Suspended/Banned info */
  suspendedAt?: Timestamp;
  suspendedBy?: string;
  suspendedReason?: string;

  /** Soft delete */
  deletedAt?: Timestamp;
}

import { Timestamp } from 'firebase-admin/firestore';
import { IBaseEntity } from '../../../core/database';

/**
 * User Role - Theo docs-god/architecture/02_DATA_MODEL.md
 */
export enum UserRole {
  CUSTOMER = 'CUSTOMER',
  OWNER = 'OWNER',
  SHIPPER = 'SHIPPER',
  ADMIN = 'ADMIN',
}

/**
 * User Status
 * NOTE: Phải match với DTO enum values (UPPERCASE)
 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  BANNED = 'BANNED',
}

/**
 * Admin User Entity - Entity cho admin quản lý users
 *
 * NOTE: Đây là entity dùng trong Admin module.
 * Khi implement EPIC 03 (User), entity đầy đủ sẽ ở modules/users/
 *
 * Collection: users
 */
export interface AdminUserEntity extends IBaseEntity {
  /** Firebase Auth UID */
  uid: string;

  /** Email */
  email: string;

  /** Phone */
  phone?: string;

  /** Display name */
  displayName: string;

  /** Avatar URL */
  avatarUrl?: string;

  /** User roles */
  roles: UserRole[];

  /** Account status */
  status: UserStatus;

  /** Is email verified */
  isEmailVerified: boolean;

  /** Banned info */
  bannedAt?: Timestamp;
  bannedBy?: string;
  bannedReason?: string;

  /** Unbanned info */
  unbannedAt?: Timestamp;
  unbannedBy?: string;

  /** Last login */
  lastLoginAt?: Timestamp;

  /** Soft delete */
  deletedAt?: Timestamp;
}

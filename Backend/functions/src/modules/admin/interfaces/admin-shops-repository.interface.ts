import {
  IBaseRepository,
  PaginatedResult,
  QueryOptions,
} from '../../../core/database';
import { AdminShopEntity, ShopStatus } from '../entities';

/**
 * IAdminShopsRepository - Interface cho Admin Shops Repository
 *
 * Dùng trong AdminService để quản lý shops.
 * Implement sẽ ở repositories/firestore-admin-shops.repository.ts
 */
export interface IAdminShopsRepository
  extends IBaseRepository<AdminShopEntity> {
  /**
   * Tìm shops với filter và pagination
   */
  findWithFilters(
    filters: {
      status?: ShopStatus;
      search?: string;
    },
    options?: QueryOptions<AdminShopEntity>,
  ): Promise<PaginatedResult<AdminShopEntity>>;

  /**
   * Update shop status (suspend/ban)
   */
  updateStatus(
    shopId: string,
    status: ShopStatus,
    adminId: string,
    reason?: string,
  ): Promise<AdminShopEntity>;

  /**
   * Count shops by status
   */
  countByStatus(status: ShopStatus): Promise<number>;

  /**
   * Count pending approval shops
   */
  countPendingApproval(): Promise<number>;
}

/**
 * Token để inject repository
 */
export const ADMIN_SHOPS_REPOSITORY_TOKEN = 'IAdminShopsRepository';

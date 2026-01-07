import {
  IBaseRepository,
  PaginatedResult,
  QueryOptions,
} from '../../../core/database';
import { AdminPayoutEntity, PayoutStatus } from '../entities';

/**
 * IAdminPayoutsRepository - Interface cho Admin Payouts Repository
 *
 * Dùng trong AdminService để quản lý payout requests.
 * Implement sẽ ở repositories/firestore-admin-payouts.repository.ts
 */
export interface IAdminPayoutsRepository
  extends IBaseRepository<AdminPayoutEntity> {
  /**
   * Tìm payouts với filter và pagination
   */
  findWithFilters(
    filters: {
      status?: PayoutStatus;
      userId?: string;
    },
    options?: QueryOptions<AdminPayoutEntity>,
  ): Promise<PaginatedResult<AdminPayoutEntity>>;

  /**
   * Approve payout
   */
  approve(payoutId: string, adminId: string): Promise<AdminPayoutEntity>;

  /**
   * Reject payout
   */
  reject(
    payoutId: string,
    adminId: string,
    reason: string,
  ): Promise<AdminPayoutEntity>;

  /**
   * Mark as transferred
   */
  markTransferred(
    payoutId: string,
    adminId: string,
    transferNote: string,
  ): Promise<AdminPayoutEntity>;

  /**
   * Count pending payouts
   */
  countPending(): Promise<number>;

  /**
   * Sum pending payout amounts
   */
  sumPendingAmount(): Promise<number>;
}

/**
 * Token để inject repository
 */
export const ADMIN_PAYOUTS_REPOSITORY_TOKEN = 'IAdminPayoutsRepository';

import {
  IBaseRepository,
  PaginatedResult,
  QueryOptions,
} from '../../../core/database';
import { AdminUserEntity, UserRole, UserStatus } from '../entities';

/**
 * IAdminUsersRepository - Interface cho Admin Users Repository
 *
 * Dùng trong AdminService để quản lý users.
 * Implement sẽ ở repositories/firestore-admin-users.repository.ts
 */
export interface IAdminUsersRepository
  extends IBaseRepository<AdminUserEntity> {
  /**
   * Tìm users với filter và pagination
   */
  findWithFilters(
    filters: {
      role?: UserRole;
      status?: UserStatus;
      search?: string;
    },
    options?: QueryOptions<AdminUserEntity>,
  ): Promise<PaginatedResult<AdminUserEntity>>;

  /**
   * Update user status (ban/unban)
   */
  updateStatus(
    userId: string,
    status: UserStatus,
    adminId: string,
    reason?: string,
  ): Promise<AdminUserEntity>;

  /**
   * Count users by role
   */
  countByRole(role: UserRole): Promise<number>;

  /**
   * Count users by status
   */
  countByStatus(status: UserStatus): Promise<number>;

  /**
   * Count users created after date
   */
  countCreatedAfter(date: Date): Promise<number>;
}

/**
 * Token để inject repository
 */
export const ADMIN_USERS_REPOSITORY_TOKEN = 'IAdminUsersRepository';

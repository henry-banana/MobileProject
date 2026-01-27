import { Injectable, Inject, Logger } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { FirestoreBaseRepository, PaginatedResult, QueryOptions } from '../../../core/database';
import { IAdminUsersRepository } from '../interfaces';
import { AdminUserEntity, UserRole, UserStatus } from '../entities';

/**
 * FirestoreAdminUsersRepository - Firestore implementation của IAdminUsersRepository
 *
 * Collection: users
 *
 * NOTE: Repository này dùng trong Admin module để quản lý users.
 * Logic đầy đủ sẽ implement khi đến EPIC 03 (User).
 */
@Injectable()
export class FirestoreAdminUsersRepository
  extends FirestoreBaseRepository<AdminUserEntity>
  implements IAdminUsersRepository
{
  private readonly logger = new Logger(FirestoreAdminUsersRepository.name);

  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'users');
  }

  /**
   * Tìm users với filter và pagination
   */
  async findWithFilters(
    filters: {
      role?: UserRole;
      status?: UserStatus;
      search?: string;
    },
    options?: QueryOptions<AdminUserEntity>,
  ): Promise<PaginatedResult<AdminUserEntity>> {
    this.logger.log(`Finding users with filters: ${JSON.stringify(filters)}`);

    let query: FirebaseFirestore.Query = this.collection;

    // Filter by role (single value field, not array)
    if (filters.role) {
      query = query.where('role', '==', filters.role);
    }

    // Filter by status
    if (filters.status) {
      query = query.where('status', '==', filters.status);
    }

    // Order by createdAt desc
    query = query.orderBy('createdAt', 'desc');

    // Count total
    const countSnapshot = await query.count().get();
    const total = countSnapshot.data().count;

    // Pagination
    const { page = 1, limit = 20 } = options?.pagination || {};
    const offset = (page - 1) * limit;

    // Get data
    const snapshot = await query.offset(offset).limit(limit).get();
    const data = snapshot.docs.map((doc) => this.mapDocToEntity(doc));

    // TODO: Implement search khi có Algolia hoặc full-text search
    // Firestore không hỗ trợ LIKE query
    // if (filters.search) { ... }

    return {
      data,
      meta: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit),
      },
    };
  }

  /**
   * Update user status (ban/unban)
   */
  async updateStatus(
    userId: string,
    status: UserStatus,
    adminId: string,
    reason?: string,
  ): Promise<AdminUserEntity> {
    this.logger.log(`Updating user ${userId} status to ${status}`);

    const updateData: Record<string, any> = {
      status,
      updatedAt: Timestamp.now(),
    };

    if (status === UserStatus.BANNED) {
      updateData.bannedAt = Timestamp.now();
      updateData.bannedBy = adminId;
      updateData.bannedReason = reason || '';
    } else if (status === UserStatus.ACTIVE) {
      updateData.unbannedAt = Timestamp.now();
      updateData.unbannedBy = adminId;
    }

    await this.collection.doc(userId).update(updateData);

    return this.findByIdOrThrow(userId);
  }

  /**
   * Count users by role
   * NOTE: Users store role as single value field 'role', not 'roles' array
   */
  async countByRole(role: UserRole): Promise<number> {
    const snapshot = await this.collection.where('role', '==', role).count().get();

    return snapshot.data().count;
  }

  /**
   * Count users by status
   */
  async countByStatus(status: UserStatus): Promise<number> {
    const snapshot = await this.collection.where('status', '==', status).count().get();

    return snapshot.data().count;
  }

  /**
   * Count users created after date
   */
  async countCreatedAfter(date: Date): Promise<number> {
    const timestamp = Timestamp.fromDate(date);

    const snapshot = await this.collection.where('createdAt', '>=', timestamp).count().get();

    return snapshot.data().count;
  }
}

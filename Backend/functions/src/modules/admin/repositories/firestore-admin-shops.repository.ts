import { Injectable, Inject, Logger } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { FirestoreBaseRepository, PaginatedResult, QueryOptions } from '../../../core/database';
import { IAdminShopsRepository } from '../interfaces';
import { AdminShopEntity, ShopStatus } from '../entities';

/**
 * FirestoreAdminShopsRepository - Firestore implementation của IAdminShopsRepository
 *
 * Collection: shops
 *
 * NOTE: Repository này dùng trong Admin module để quản lý shops.
 * Logic đầy đủ sẽ implement khi đến EPIC 04 (Shop).
 */
@Injectable()
export class FirestoreAdminShopsRepository
  extends FirestoreBaseRepository<AdminShopEntity>
  implements IAdminShopsRepository
{
  private readonly logger = new Logger(FirestoreAdminShopsRepository.name);

  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'shops');
  }

  /**
   * Tìm shops với filter và pagination
   */
  async findWithFilters(
    filters: {
      status?: ShopStatus;
      search?: string;
    },
    options?: QueryOptions<AdminShopEntity>,
  ): Promise<PaginatedResult<AdminShopEntity>> {
    this.logger.log(`Finding shops with filters: ${JSON.stringify(filters)}`);

    let query: FirebaseFirestore.Query = this.collection;

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

    // TODO: Implement search khi có Algolia

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
   * Update shop status (suspend/ban)
   */
  async updateStatus(
    shopId: string,
    status: ShopStatus,
    adminId: string,
    reason?: string,
  ): Promise<AdminShopEntity> {
    this.logger.log(`Updating shop ${shopId} status to ${status}`);

    const updateData: Record<string, any> = {
      status,
      updatedAt: Timestamp.now(),
    };

    if (status === ShopStatus.SUSPENDED || status === ShopStatus.BANNED) {
      updateData.suspendedAt = Timestamp.now();
      updateData.suspendedBy = adminId;
      updateData.suspendedReason = reason || '';
    }

    await this.collection.doc(shopId).update(updateData);

    return this.findByIdOrThrow(shopId);
  }

  /**
   * Count shops by status
   */
  async countByStatus(status: ShopStatus): Promise<number> {
    const snapshot = await this.collection.where('status', '==', status).count().get();

    return snapshot.data().count;
  }

  /**
   * Count pending approval shops
   */
  async countPendingApproval(): Promise<number> {
    const snapshot = await this.collection.where('status', '==', ShopStatus.PENDING).count().get();

    return snapshot.data().count;
  }
}

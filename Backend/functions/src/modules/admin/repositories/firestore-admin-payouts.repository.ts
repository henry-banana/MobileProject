import { Injectable, Inject, Logger, ConflictException } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import {
  FirestoreBaseRepository,
  PaginatedResult,
  QueryOptions,
} from '../../../core/database';
import { IAdminPayoutsRepository } from '../interfaces';
import { AdminPayoutEntity, PayoutStatus } from '../entities';

/**
 * FirestoreAdminPayoutsRepository - Firestore implementation của IAdminPayoutsRepository
 *
 * Collection: payoutRequests
 *
 * NOTE: Repository này dùng trong Admin module để quản lý payout requests.
 * Logic đầy đủ sẽ implement khi đến EPIC 10 (Wallet).
 */
@Injectable()
export class FirestoreAdminPayoutsRepository
  extends FirestoreBaseRepository<AdminPayoutEntity>
  implements IAdminPayoutsRepository
{
  private readonly logger = new Logger(FirestoreAdminPayoutsRepository.name);

  constructor(@Inject('FIRESTORE') firestore: Firestore) {
    super(firestore, 'payoutRequests');
  }

  /**
   * Tìm payouts với filter và pagination
   */
  async findWithFilters(
    filters: {
      status?: PayoutStatus;
      userId?: string;
    },
    options?: QueryOptions<AdminPayoutEntity>,
  ): Promise<PaginatedResult<AdminPayoutEntity>> {
    this.logger.log(`Finding payouts with filters: ${JSON.stringify(filters)}`);

    let query: FirebaseFirestore.Query = this.collection;

    // Filter by status
    if (filters.status) {
      query = query.where('status', '==', filters.status);
    }

    // Filter by userId
    if (filters.userId) {
      query = query.where('userId', '==', filters.userId);
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
   * Approve payout
   */
  async approve(
    payoutId: string,
    adminId: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Approving payout ${payoutId}`);

    const payout = await this.findByIdOrThrow(payoutId);

    if (payout.status !== PayoutStatus.PENDING) {
      throw new ConflictException('Payout đã được xử lý');
    }

    await this.collection.doc(payoutId).update({
      status: PayoutStatus.APPROVED,
      approvedAt: Timestamp.now(),
      approvedBy: adminId,
      updatedAt: Timestamp.now(),
    });

    return this.findByIdOrThrow(payoutId);
  }

  /**
   * Reject payout
   */
  async reject(
    payoutId: string,
    adminId: string,
    reason: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Rejecting payout ${payoutId}`);

    const payout = await this.findByIdOrThrow(payoutId);

    if (payout.status !== PayoutStatus.PENDING) {
      throw new ConflictException('Payout đã được xử lý');
    }

    await this.collection.doc(payoutId).update({
      status: PayoutStatus.REJECTED,
      rejectedAt: Timestamp.now(),
      rejectedBy: adminId,
      rejectedReason: reason,
      updatedAt: Timestamp.now(),
    });

    // TODO: Return locked funds to available balance
    // Cần WalletService - sẽ implement ở EPIC 10

    return this.findByIdOrThrow(payoutId);
  }

  /**
   * Mark as transferred
   */
  async markTransferred(
    payoutId: string,
    adminId: string,
    transferNote: string,
  ): Promise<AdminPayoutEntity> {
    this.logger.log(`Marking payout ${payoutId} as transferred`);

    const payout = await this.findByIdOrThrow(payoutId);

    if (payout.status !== PayoutStatus.APPROVED) {
      throw new ConflictException('Payout chưa được approve');
    }

    await this.collection.doc(payoutId).update({
      status: PayoutStatus.TRANSFERRED,
      transferredAt: Timestamp.now(),
      transferredBy: adminId,
      transferNote,
      updatedAt: Timestamp.now(),
    });

    return this.findByIdOrThrow(payoutId);
  }

  /**
   * Count pending payouts
   */
  async countPending(): Promise<number> {
    const snapshot = await this.collection
      .where('status', '==', PayoutStatus.PENDING)
      .count()
      .get();

    return snapshot.data().count;
  }

  /**
   * Sum pending payout amounts
   */
  async sumPendingAmount(): Promise<number> {
    // Firestore không hỗ trợ SUM aggregation trực tiếp
    // Phải fetch all pending payouts và sum client-side
    const snapshot = await this.collection
      .where('status', '==', PayoutStatus.PENDING)
      .get();

    let total = 0;
    snapshot.docs.forEach((doc) => {
      const data = doc.data();
      total += data.amount || 0;
    });

    return total;
  }
}

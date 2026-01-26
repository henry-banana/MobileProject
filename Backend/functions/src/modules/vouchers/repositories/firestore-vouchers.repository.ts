import { Injectable, Inject, BadRequestException } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { IVouchersRepository } from '../interfaces';
import { VoucherEntity, VoucherUsageEntity, OwnerType } from '../entities';
import { ErrorCodes } from '../../../shared/constants/error-codes';
import { stripUndefined } from '../utils/firestore.utils';

@Injectable()
export class FirestoreVouchersRepository implements IVouchersRepository {
  private readonly vouchersCollection = 'vouchers';
  private readonly voucherUsagesCollection = 'voucherUsages';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(shopId: string, data: Partial<VoucherEntity>): Promise<VoucherEntity> {
    const voucherRef = this.firestore.collection(this.vouchersCollection).doc();
    const now = Timestamp.now();

    const voucherData: VoucherEntity = {
      id: voucherRef.id,
      code: data.code!.toUpperCase(),
      shopId,
      type: data.type!,
      value: data.value!,
      maxDiscount: data.maxDiscount,
      minOrderAmount: data.minOrderAmount,
      usageLimit: data.usageLimit!,
      usageLimitPerUser: data.usageLimitPerUser!,
      currentUsage: 0,
      validFrom: data.validFrom!,
      validTo: data.validTo!,
      isActive: true,
      ownerType: OwnerType.SHOP,
      name: data.name,
      description: data.description,
      isDeleted: false,
      createdAt: now.toDate().toISOString(),
      updatedAt: now.toDate().toISOString(),
    };

    // Strip undefined values before writing to Firestore
    const cleanedData = stripUndefined(voucherData);

    await voucherRef.set(cleanedData);
    return voucherData;
  }

  async findById(id: string): Promise<VoucherEntity | null> {
    const doc = await this.firestore.collection(this.vouchersCollection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return { id: doc.id, ...doc.data() } as VoucherEntity;
  }

  async findByShopAndCode(shopId: string, code: string): Promise<VoucherEntity | null> {
    const snapshot = await this.firestore
      .collection(this.vouchersCollection)
      .where('shopId', '==', shopId)
      .where('code', '==', code.toUpperCase())
      .where('isDeleted', '==', false)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return { id: doc.id, ...doc.data() } as VoucherEntity;
  }

  async findByShopId(
    shopId: string,
    filters?: {
      isActive?: boolean;
      limit?: number;
      orderBy?: 'createdAt' | 'validTo';
      orderDir?: 'asc' | 'desc';
    },
  ): Promise<VoucherEntity[]> {
    let query = this.firestore
      .collection(this.vouchersCollection)
      .where('shopId', '==', shopId)
      .where('isDeleted', '==', false);

    if (filters?.isActive !== undefined) {
      query = query.where('isActive', '==', filters.isActive);
    }

    const orderBy = filters?.orderBy ?? 'createdAt';
    const orderDir = filters?.orderDir ?? 'desc';
    query = query.orderBy(orderBy, orderDir);

    if (filters?.limit) {
      query = query.limit(filters.limit);
    }

    const snapshot = await query.get();
    return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() }) as VoucherEntity);
  }

  async update(id: string, data: Partial<VoucherEntity>): Promise<void> {
    const voucherRef = this.firestore.collection(this.vouchersCollection).doc(id);
    const now = Timestamp.now();

    const updateData = {
      ...data,
      updatedAt: now.toDate().toISOString(),
    };

    // Strip undefined values before writing to Firestore
    const cleanedData = stripUndefined(updateData);

    await voucherRef.update(cleanedData);
  }

  async delete(id: string): Promise<void> {
    const now = Timestamp.now();
    await this.firestore.collection(this.vouchersCollection).doc(id).update({
      isDeleted: true,
      updatedAt: now.toDate().toISOString(),
    });
  }

  async countUsage(voucherId: string): Promise<number> {
    const snapshot = await this.firestore
      .collection(this.voucherUsagesCollection)
      .where('voucherId', '==', voucherId)
      .count()
      .get();

    return snapshot.data().count;
  }

  async countUsageByUser(voucherId: string, userId: string): Promise<number> {
    const snapshot = await this.firestore
      .collection(this.voucherUsagesCollection)
      .where('voucherId', '==', voucherId)
      .where('userId', '==', userId)
      .count()
      .get();

    return snapshot.data().count;
  }

  /**
   * Count voucher usage per user for multiple vouchers (batch)
   * Chunking for Firestore 'in' operator limit (~10 items per query)
   * @returns Map of voucherId -> usage count for the given user
   */
  async countUsageByUserBatch(
    voucherIds: string[],
    userId: string,
  ): Promise<Record<string, number>> {
    if (voucherIds.length === 0) {
      return {};
    }

    const result: Record<string, number> = {};
    const chunkSize = 10; // Firestore 'in' operator limit

    // Process vouchers in chunks
    for (let i = 0; i < voucherIds.length; i += chunkSize) {
      const chunk = voucherIds.slice(i, i + chunkSize);

      // Query voucherUsages where userId == userId AND voucherId in chunk
      const snapshot = await this.firestore
        .collection(this.voucherUsagesCollection)
        .where('userId', '==', userId)
        .where('voucherId', 'in', chunk)
        .get();

      // Aggregate counts per voucherId in memory
      const counts: Record<string, number> = {};
      for (const doc of snapshot.docs) {
        const data = doc.data();
        const voucherId = data.voucherId;
        counts[voucherId] = (counts[voucherId] ?? 0) + 1;
      }

      // Add to result map
      Object.assign(result, counts);

      // Ensure all queried vouchers are in result (even if count is 0)
      for (const voucherId of chunk) {
        if (!(voucherId in result)) {
          result[voucherId] = 0;
        }
      }
    }

    return result;
  }

  async getUsage(
    voucherId: string,
    userId: string,
    orderId: string,
  ): Promise<VoucherUsageEntity | null> {
    const usageId = `${voucherId}_${userId}_${orderId}`;
    const doc = await this.firestore.collection(this.voucherUsagesCollection).doc(usageId).get();

    if (!doc.exists) {
      return null;
    }

    return { id: doc.id, ...doc.data() } as VoucherUsageEntity;
  }

  async createUsage(data: VoucherUsageEntity): Promise<void> {
    const usageId = `${data.voucherId}_${data.userId}_${data.orderId}`;
    const usageRef = this.firestore.collection(this.voucherUsagesCollection).doc(usageId);

    await usageRef.set({
      ...data,
      id: usageId,
    });
  }

  async applyVoucherAtomic(
    voucherId: string,
    userId: string,
    orderId: string,
    discountAmount: number,
  ): Promise<VoucherEntity> {
    const voucherRef = this.firestore.collection(this.vouchersCollection).doc(voucherId);
    const usageId = `${voucherId}_${userId}_${orderId}`;
    const usageRef = this.firestore.collection(this.voucherUsagesCollection).doc(usageId);

    return await this.firestore.runTransaction(async (transaction) => {
      // 1. Read voucher
      const voucherDoc = await transaction.get(voucherRef);
      if (!voucherDoc.exists) {
        throw new BadRequestException({
          code: ErrorCodes.VOUCHER_NOT_FOUND,
          message: 'Voucher không tồn tại',
          statusCode: 404,
        });
      }

      const voucher = { id: voucherDoc.id, ...voucherDoc.data() } as VoucherEntity;

      // 2. Check if usage already exists (idempotency)
      const usageDoc = await transaction.get(usageRef);
      if (usageDoc.exists) {
        // Already applied - return current voucher state
        return voucher;
      }

      // 3. Validate usage limits
      const newCurrentUsage = voucher.currentUsage + 1;
      if (newCurrentUsage > voucher.usageLimit) {
        throw new BadRequestException({
          code: ErrorCodes.VOUCHER_TOTAL_LIMIT_REACHED,
          message: 'Voucher đã hết lượt sử dụng',
          statusCode: 400,
        });
      }

      // 4. Count user-specific usage
      const userUsageSnapshot = await this.firestore
        .collection(this.voucherUsagesCollection)
        .where('voucherId', '==', voucherId)
        .where('userId', '==', userId)
        .count()
        .get();

      const userUsageCount = userUsageSnapshot.data().count;
      if (userUsageCount >= voucher.usageLimitPerUser) {
        throw new BadRequestException({
          code: ErrorCodes.VOUCHER_USER_LIMIT_REACHED,
          message: 'Bạn đã sử dụng hết lượt cho voucher này',
          statusCode: 400,
        });
      }

      // 5. Create usage record
      const now = Timestamp.now().toDate().toISOString();
      const usageData: VoucherUsageEntity = {
        id: usageId,
        voucherId,
        shopId: voucher.shopId, // Denormalize shopId from voucher for efficient filtering
        userId,
        orderId,
        discountAmount,
        createdAt: now,
      };

      transaction.set(usageRef, usageData);

      // 6. Increment voucher usage
      transaction.update(voucherRef, {
        currentUsage: newCurrentUsage,
        updatedAt: now,
      });

      // 7. Return updated voucher
      return {
        ...voucher,
        currentUsage: newCurrentUsage,
        updatedAt: now,
      };
    });
  }

  /**
   * Get paginated voucher usage history for a user
   * Supports filtering by shopId and date range
   * FIX for VOUCH-009: shopId is now denormalized on usage records for efficient DB-level filtering
   */
  async getUsageHistory(
    userId: string,
    filters?: {
      shopId?: string;
      from?: string;
      to?: string;
    },
    page: number = 1,
    limit: number = 20,
  ): Promise<{ items: VoucherUsageEntity[]; total: number }> {
    // Build query for all usage records of this user
    let query = this.firestore
      .collection(this.voucherUsagesCollection)
      .where('userId', '==', userId);

    // Apply shopId filter at DB level (FIXED: was previously filtered in-memory after pagination)
    if (filters?.shopId) {
      query = query.where('shopId', '==', filters.shopId);
    }

    // Apply date filters if provided
    if (filters?.from) {
      query = query.where('createdAt', '>=', filters.from);
    }
    if (filters?.to) {
      query = query.where('createdAt', '<=', filters.to);
    }

    // Get total count AFTER applying all filters (now accurate)
    const countSnapshot = await query.count().get();
    const total = countSnapshot.data().count;

    // Calculate pagination
    const offset = (page - 1) * limit;

    // Get paginated results, ordered by createdAt descending (newest first)
    const snapshot = await query.orderBy('createdAt', 'desc').offset(offset).limit(limit).get();

    const items: VoucherUsageEntity[] = snapshot.docs.map(
      (doc) => ({ id: doc.id, ...doc.data() }) as VoucherUsageEntity,
    );

    return { items, total };
  }

  /**
   * Get paginated usage records for a specific voucher (owner view)
   * Queries voucherUsages where voucherId matches
   */
  async getVoucherUsageByVoucherId(
    voucherId: string,
    page: number = 1,
    limit: number = 20,
    from?: string,
    to?: string,
  ): Promise<{ items: VoucherUsageEntity[]; total: number }> {
    let query = this.firestore
      .collection(this.voucherUsagesCollection)
      .where('voucherId', '==', voucherId);

    // Apply date filters if provided
    if (from) {
      query = query.where('createdAt', '>=', from);
    }
    if (to) {
      query = query.where('createdAt', '<=', to);
    }

    // Get total count
    const countSnapshot = await query.count().get();
    const total = countSnapshot.data().count;

    // Calculate pagination
    const offset = (page - 1) * limit;

    // Get paginated results, ordered by createdAt descending (newest first)
    const snapshot = await query.orderBy('createdAt', 'desc').offset(offset).limit(limit).get();

    const items: VoucherUsageEntity[] = snapshot.docs.map(
      (doc) => ({ id: doc.id, ...doc.data() }) as VoucherUsageEntity,
    );

    return { items, total };
  }

  /**
   * Get aggregated statistics for a voucher
   * Computes: total uses, total discount amount, unique users, last used time
   */
  async getVoucherStats(
    voucherId: string,
  ): Promise<{
    totalUses: number;
    totalDiscountAmount: number;
    uniqueUsers: number;
    lastUsedAt: string | null;
  }> {
    // Query all usage records for this voucher
    const snapshot = await this.firestore
      .collection(this.voucherUsagesCollection)
      .where('voucherId', '==', voucherId)
      .orderBy('createdAt', 'desc')
      .get();

    const docs = snapshot.docs;
    const totalUses = docs.length;

    // Calculate aggregations in memory
    let totalDiscountAmount = 0;
    const uniqueUserSet = new Set<string>();
    let lastUsedAt: string | null = null;

    for (const doc of docs) {
      const data = doc.data() as VoucherUsageEntity;
      totalDiscountAmount += data.discountAmount;
      uniqueUserSet.add(data.userId);

      // Get the first document's createdAt (already ordered DESC, so first = latest)
      if (!lastUsedAt && data.createdAt) {
        lastUsedAt = data.createdAt;
      }
    }

    return {
      totalUses,
      totalDiscountAmount,
      uniqueUsers: uniqueUserSet.size,
      lastUsedAt,
    };
  }

  /**
   * Mark all active vouchers with validTo < now as isActive=false (expiration sweep)
   * Idempotent: If already inactive, no change
   * 
   * @param now Current timestamp (ISO 8601)
   * @returns { updatedCount: number }
   */
  async expireVouchersBeforeDate(now: string): Promise<{ updatedCount: number }> {
    // Query: Find all ACTIVE vouchers where validTo < now
    const query = this.firestore
      .collection(this.vouchersCollection)
      .where('isActive', '==', true)
      .where('isDeleted', '==', false)
      .where('validTo', '<', now);

    const snapshot = await query.get();
    let updatedCount = 0;

    // Batch update (Firestore allows up to 500 writes per batch)
    const batch = this.firestore.batch();

    for (const doc of snapshot.docs) {
      batch.update(doc.ref, {
        isActive: false,
        updatedAt: Timestamp.now().toDate().toISOString(),
      });
      updatedCount++;
    }

    // Commit batch if there are changes
    if (updatedCount > 0) {
      await batch.commit();
    }

    return { updatedCount };
  }
}


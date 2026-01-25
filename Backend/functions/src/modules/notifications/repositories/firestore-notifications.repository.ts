import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { INotificationsRepository } from '../interfaces';
import { NotificationEntity } from '../entities';

/**
 * Helper: Remove undefined values from object recursively
 * Firestore doesn't support undefined values; this ensures clean documents
 */
function cleanUndefinedValues(obj: any): any {
  if (obj === null || obj === undefined) {
    return undefined;
  }

  if (typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map((item) => cleanUndefinedValues(item)).filter((item) => item !== undefined);
  }

  // Object
  const cleaned: any = {};
  Object.keys(obj).forEach((key) => {
    const value = cleanUndefinedValues(obj[key]);
    if (value !== undefined) {
      cleaned[key] = value;
    }
  });
  return cleaned;
}

@Injectable()
export class FirestoreNotificationsRepository implements INotificationsRepository {
  private readonly collectionName = 'notifications';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(userId: string, data: Partial<NotificationEntity>): Promise<NotificationEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc();
    const now = Timestamp.now();

    // Build entity with clean values (no undefined)
    const entity: any = {
      id: docRef.id,
      userId,
      title: data.title!,
      body: data.body!,
      type: data.type!,
      read: false,
      createdAt: now.toDate().toISOString(),
    };

    // Add optional fields only if defined
    if (data.imageUrl !== undefined) {
      entity.imageUrl = data.imageUrl;
    }
    if (data.data !== undefined) {
      // Deep clean data object to remove any undefined values
      const cleanedData = cleanUndefinedValues(data.data);
      if (Object.keys(cleanedData).length > 0) {
        entity.data = cleanedData;
      }
    }
    if (data.orderId !== undefined) {
      entity.orderId = data.orderId;
    }
    if (data.shopId !== undefined) {
      entity.shopId = data.shopId;
    }
    if (data.sentAt !== undefined) {
      entity.sentAt = data.sentAt;
    }
    if (data.deliveryStatus !== undefined) {
      entity.deliveryStatus = data.deliveryStatus;
    }
    if (data.deliveryErrorCode !== undefined) {
      entity.deliveryErrorCode = data.deliveryErrorCode;
    }
    if (data.deliveryErrorMessage !== undefined) {
      entity.deliveryErrorMessage = data.deliveryErrorMessage;
    }
    if (data.readAt !== undefined) {
      entity.readAt = data.readAt;
    }

    // Write cleaned entity to Firestore (no undefined values)
    await docRef.set(entity);
    
    // Return entity with only defined fields
    return cleanUndefinedValues(entity) as NotificationEntity;
  }

  async findById(userId: string, notificationId: string): Promise<NotificationEntity | null> {
    const doc = await this.firestore.collection(this.collectionName).doc(notificationId).get();

    if (!doc.exists || doc.data()!.userId !== userId) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as NotificationEntity;
  }

  async findByUserId(
    userId: string,
    filters?: {
      read?: boolean;
      limit?: number;
      offset?: number;
    },
  ): Promise<{ items: NotificationEntity[]; total: number }> {
    if (process.env.DEBUG_NOTIF_QUERY === '1') {
      console.log('[FirestoreNotificationsRepository findByUserId]');
      console.log('  filters.read:', filters?.read, 'typeof:', typeof filters?.read);
      console.log('  filters.read === undefined:', filters?.read === undefined);
      console.log('  filters.read !== undefined:', filters?.read !== undefined);
    }

    // Get total count
    let countQuery = this.firestore.collection(this.collectionName).where('userId', '==', userId);

    if (filters?.read !== undefined) {
      if (process.env.DEBUG_NOTIF_QUERY === '1') {
        console.log('[FirestoreNotificationsRepository] Applying read filter:', filters.read);
      }
      countQuery = countQuery.where('read', '==', filters.read);
    } else {
      if (process.env.DEBUG_NOTIF_QUERY === '1') {
        console.log('[FirestoreNotificationsRepository] No read filter, returning all');
      }
    }

    const countSnapshot = await countQuery.count().get();
    const total = countSnapshot.data().count;

    // Get paginated results
    let query = this.firestore.collection(this.collectionName).where('userId', '==', userId);

    if (filters?.read !== undefined) {
      query = query.where('read', '==', filters.read);
    }

    query = query.orderBy('createdAt', 'desc');

    if (filters?.offset) {
      query = query.offset(filters.offset);
    }

    if (filters?.limit) {
      query = query.limit(filters.limit);
    }

    const snapshot = await query.get();

    const items = snapshot.docs.map(
      (doc) =>
        ({
          id: doc.id,
          ...doc.data(),
        }) as NotificationEntity,
    );

    return { items, total };
  }

  async updateReadStatus(userId: string, notificationId: string): Promise<NotificationEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc(notificationId);
    const doc = await docRef.get();

    if (!doc.exists || doc.data()!.userId !== userId) {
      throw new Error('Notification not found or does not belong to user');
    }

    const now = Timestamp.now();
    await docRef.update({
      read: true,
      readAt: now.toDate().toISOString(),
    });

    const updatedDoc = await docRef.get();
    return {
      id: updatedDoc.id,
      ...updatedDoc.data(),
    } as NotificationEntity;
  }

  async markAllAsRead(userId: string): Promise<number> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('userId', '==', userId)
      .where('read', '==', false)
      .get();

    const batch = this.firestore.batch();
    const now = Timestamp.now().toDate().toISOString();

    snapshot.docs.forEach((doc) => {
      batch.update(doc.ref, {
        read: true,
        readAt: now,
      });
    });

    await batch.commit();
    return snapshot.size;
  }
}

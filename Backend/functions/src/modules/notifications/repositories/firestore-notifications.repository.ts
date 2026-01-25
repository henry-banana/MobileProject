import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { INotificationsRepository } from '../interfaces';
import { NotificationEntity } from '../entities';

@Injectable()
export class FirestoreNotificationsRepository implements INotificationsRepository {
  private readonly collectionName = 'notifications';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(userId: string, data: Partial<NotificationEntity>): Promise<NotificationEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc();
    const now = Timestamp.now();

    const entity: NotificationEntity = {
      id: docRef.id,
      userId,
      title: data.title!,
      body: data.body!,
      imageUrl: data.imageUrl,
      type: data.type!,
      data: data.data,
      read: false,
      orderId: data.orderId,
      shopId: data.shopId,
      createdAt: now.toDate().toISOString(),
    };

    await docRef.set(entity);
    return entity;
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
    // Get total count
    let countQuery = this.firestore.collection(this.collectionName).where('userId', '==', userId);

    if (filters?.read !== undefined) {
      countQuery = countQuery.where('read', '==', filters.read);
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

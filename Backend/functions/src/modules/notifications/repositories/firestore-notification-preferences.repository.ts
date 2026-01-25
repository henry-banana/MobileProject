import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { INotificationPreferencesRepository } from '../interfaces';
import { NotificationPreferencesEntity } from '../entities';

@Injectable()
export class FirestoreNotificationPreferencesRepository implements INotificationPreferencesRepository {
  private readonly collectionName = 'notification_preferences';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(
    userId: string,
    data: Partial<NotificationPreferencesEntity>,
  ): Promise<NotificationPreferencesEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc(userId);
    const now = Timestamp.now();

    const entity: NotificationPreferencesEntity = {
      userId,
      transactional: data.transactional ?? true,
      informational: data.informational ?? true,
      marketing: data.marketing ?? false,
      updatedAt: now.toDate().toISOString(),
    };

    await docRef.set(entity);
    return entity;
  }

  async findByUserId(userId: string): Promise<NotificationPreferencesEntity | null> {
    const doc = await this.firestore.collection(this.collectionName).doc(userId).get();

    if (!doc.exists) {
      return null;
    }

    return {
      userId: doc.id,
      ...doc.data(),
    } as NotificationPreferencesEntity;
  }

  async update(
    userId: string,
    data: Partial<NotificationPreferencesEntity>,
  ): Promise<NotificationPreferencesEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc(userId);
    const now = Timestamp.now();

    const updateData: Record<string, unknown> = {
      updatedAt: now.toDate().toISOString(),
    };

    if (data.transactional !== undefined) updateData.transactional = data.transactional;
    if (data.informational !== undefined) updateData.informational = data.informational;
    if (data.marketing !== undefined) updateData.marketing = data.marketing;

    await docRef.set(updateData, { merge: true });

    const updated = await docRef.get();
    return {
      userId: updated.id,
      ...updated.data(),
    } as NotificationPreferencesEntity;
  }
}

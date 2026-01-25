import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { IDeviceTokensRepository } from '../interfaces';
import { DeviceTokenEntity } from '../entities';

@Injectable()
export class FirestoreDeviceTokensRepository implements IDeviceTokensRepository {
  private readonly collectionName = 'device_tokens';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(userId: string, data: Partial<DeviceTokenEntity>): Promise<DeviceTokenEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc();
    const now = Timestamp.now();

    const entity: DeviceTokenEntity = {
      id: docRef.id,
      userId,
      token: data.token!,
      platform: data.platform!,
      deviceInfo: data.deviceInfo,
      createdAt: now.toDate().toISOString(),
      lastUsedAt: now.toDate().toISOString(),
    };

    await docRef.set(entity);
    return entity;
  }

  async findByUserId(userId: string): Promise<DeviceTokenEntity[]> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('userId', '==', userId)
      .get();

    return snapshot.docs.map(
      (doc) =>
        ({
          id: doc.id,
          ...doc.data(),
        }) as DeviceTokenEntity,
    );
  }

  async findByToken(token: string): Promise<DeviceTokenEntity | null> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('token', '==', token)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as DeviceTokenEntity;
  }

  async updateLastUsed(userId: string, token: string): Promise<void> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('userId', '==', userId)
      .where('token', '==', token)
      .limit(1)
      .get();

    if (!snapshot.empty) {
      const doc = snapshot.docs[0];
      await doc.ref.update({
        lastUsedAt: Timestamp.now().toDate().toISOString(),
      });
    }
  }

  async deleteByToken(userId: string, token: string): Promise<void> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('userId', '==', userId)
      .where('token', '==', token)
      .limit(1)
      .get();

    if (!snapshot.empty) {
      const doc = snapshot.docs[0];
      await doc.ref.delete();
    }
  }

  async deleteByUserId(userId: string): Promise<void> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('userId', '==', userId)
      .get();

    const batch = this.firestore.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });

    await batch.commit();
  }
}

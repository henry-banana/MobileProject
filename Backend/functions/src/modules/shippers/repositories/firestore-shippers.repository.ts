import { Injectable, Inject } from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import {
  ShipperApplicationEntity,
  ApplicationStatus,
} from '../entities/shipper-application.entity';
import { ShipperEntity } from '../entities/shipper.entity';
import { IShippersRepository } from './shippers-repository.interface';

@Injectable()
export class FirestoreShippersRepository implements IShippersRepository {
  private readonly applicationsCollection = 'shipperApplications';
  private readonly usersCollection = 'users';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async createApplication(
    data: Partial<ShipperApplicationEntity>,
  ): Promise<ShipperApplicationEntity> {
    const docRef = this.firestore.collection(this.applicationsCollection).doc();

    const applicationData = {
      ...data,
      createdAt: FieldValue.serverTimestamp(),
    };

    await docRef.set(applicationData);

    const doc = await docRef.get();
    const docData = doc.data()!;

    return new ShipperApplicationEntity({
      id: doc.id,
      ...docData,
      createdAt: docData.createdAt?.toDate?.() || docData.createdAt,
    } as ShipperApplicationEntity);
  }

  async findApplicationById(id: string): Promise<ShipperApplicationEntity | null> {
    const doc = await this.firestore.collection(this.applicationsCollection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    const data = doc.data()!;
    return new ShipperApplicationEntity({
      id: doc.id,
      ...data,
      createdAt: data.createdAt?.toDate?.() || data.createdAt,
      reviewedAt: data.reviewedAt?.toDate?.() || data.reviewedAt,
    } as ShipperApplicationEntity);
  }

  async findPendingApplication(
    userId: string,
    shopId: string,
  ): Promise<ShipperApplicationEntity | null> {
    const snapshot = await this.firestore
      .collection(this.applicationsCollection)
      .where('userId', '==', userId)
      .where('shopId', '==', shopId)
      .where('status', '==', ApplicationStatus.PENDING)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    const data = doc.data();
    return new ShipperApplicationEntity({
      id: doc.id,
      ...data,
      createdAt: data.createdAt?.toDate?.() || data.createdAt,
      reviewedAt: data.reviewedAt?.toDate?.() || data.reviewedAt,
    } as ShipperApplicationEntity);
  }

  async findUserApplications(userId: string): Promise<ShipperApplicationEntity[]> {
    const snapshot = await this.firestore
      .collection(this.applicationsCollection)
      .where('userId', '==', userId)
      .orderBy('createdAt', 'desc')
      .get();

    return snapshot.docs.map((doc) => {
      const data = doc.data();
      return new ShipperApplicationEntity({
        id: doc.id,
        ...data,
        createdAt: data.createdAt?.toDate?.() || data.createdAt,
        reviewedAt: data.reviewedAt?.toDate?.() || data.reviewedAt,
      } as ShipperApplicationEntity);
    });
  }

  async findShopApplications(
    shopId: string,
    status?: ApplicationStatus,
  ): Promise<ShipperApplicationEntity[]> {
    let query = this.firestore
      .collection(this.applicationsCollection)
      .where('shopId', '==', shopId);

    if (status) {
      query = query.where('status', '==', status);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();

    return snapshot.docs.map((doc) => {
      const data = doc.data();
      return new ShipperApplicationEntity({
        id: doc.id,
        ...data,
        createdAt: data.createdAt?.toDate?.() || data.createdAt,
        reviewedAt: data.reviewedAt?.toDate?.() || data.reviewedAt,
      } as ShipperApplicationEntity);
    });
  }

  async updateApplicationStatus(
    id: string,
    status: ApplicationStatus,
    reviewedBy: string,
    rejectReason?: string,
  ): Promise<void> {
    const updateData: Record<string, unknown> = {
      status,
      reviewedBy,
      reviewedAt: FieldValue.serverTimestamp(),
    };

    if (rejectReason) {
      updateData.rejectReason = rejectReason;
    }

    await this.firestore.collection(this.applicationsCollection).doc(id).update(updateData);
  }

  async deleteApplication(id: string): Promise<void> {
    await this.firestore.collection(this.applicationsCollection).doc(id).delete();
  }

  async findShippersByShop(shopId: string): Promise<Record<string, unknown>[]> {
    const snapshot = await this.firestore
      .collection(this.usersCollection)
      .where('role', '==', 'SHIPPER')
      .where('shipperInfo.shopId', '==', shopId)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    }));
  }

  /**
   * Find shipper by ID - used by OrdersService to check availability
   * Shippers are stored in the 'users' collection with role: 'SHIPPER'
   */
  async findById(id: string): Promise<ShipperEntity | null> {
    const doc = await this.firestore.collection(this.usersCollection).doc(id).get();

    if (!doc.exists) {
      return null;
    }

    const data = doc.data()!;
    return {
      id: doc.id,
      name: data.name || '',
      phone: data.phone || '',
      avatar: data.avatar || '',
      shipperInfo: data.shipperInfo,
      createdAt: data.createdAt?.toDate?.() || data.createdAt,
      updatedAt: data.updatedAt?.toDate?.() || data.updatedAt,
    } as ShipperEntity;
  }

  /**
   * Update shipper - used by OrdersService to manage shipper availability status
   */
  async update(id: string, updates: Partial<ShipperEntity>): Promise<void> {
    await this.firestore.collection(this.usersCollection).doc(id).update(updates);
  }
}

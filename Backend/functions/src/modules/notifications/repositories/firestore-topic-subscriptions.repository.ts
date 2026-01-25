import { Injectable, Inject } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { TopicSubscriptionEntity } from '../entities/topic-subscription.entity';

export interface ITopicSubscriptionsRepository {
  create(data: { topic: string; userId: string; token: string }): Promise<TopicSubscriptionEntity>;
  findByTopic(topic: string): Promise<TopicSubscriptionEntity[]>;
  findByTopicAndUserId(topic: string, userId: string): Promise<TopicSubscriptionEntity[]>;
  delete(topic: string, token: string): Promise<void>;
  deleteByTopicAndUserId(topic: string, userId: string): Promise<void>;
}

@Injectable()
export class FirestoreTopicSubscriptionsRepository implements ITopicSubscriptionsRepository {
  private readonly collectionName = 'topic_subscriptions';

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {}

  async create(data: {
    topic: string;
    userId: string;
    token: string;
  }): Promise<TopicSubscriptionEntity> {
    const docRef = this.firestore.collection(this.collectionName).doc();
    const now = Timestamp.now();

    const entity: TopicSubscriptionEntity = {
      id: docRef.id,
      topic: data.topic,
      userId: data.userId,
      token: data.token,
      createdAt: now.toDate().toISOString(),
    };

    await docRef.set(entity);
    return entity;
  }

  async findByTopic(topic: string): Promise<TopicSubscriptionEntity[]> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('topic', '==', topic)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as TopicSubscriptionEntity[];
  }

  async findByTopicAndUserId(topic: string, userId: string): Promise<TopicSubscriptionEntity[]> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('topic', '==', topic)
      .where('userId', '==', userId)
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as TopicSubscriptionEntity[];
  }

  async delete(topic: string, token: string): Promise<void> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('topic', '==', topic)
      .where('token', '==', token)
      .get();

    const batch = this.firestore.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });

    await batch.commit();
  }

  async deleteByTopicAndUserId(topic: string, userId: string): Promise<void> {
    const snapshot = await this.firestore
      .collection(this.collectionName)
      .where('topic', '==', topic)
      .where('userId', '==', userId)
      .get();

    const batch = this.firestore.batch();
    snapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });

    await batch.commit();
  }
}

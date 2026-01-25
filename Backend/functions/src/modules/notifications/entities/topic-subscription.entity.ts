/**
 * Topic Subscription Entity
 * Collection: topic_subscriptions
 * Tracks which users/tokens are subscribed to which topics
 */
export class TopicSubscriptionEntity {
  id: string;
  topic: string;
  userId: string;
  token: string;
  createdAt: string; // ISO 8601
}

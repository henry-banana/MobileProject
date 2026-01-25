/**
 * Notification Preferences Entity
 * Collection: notification_preferences
 * User notification preferences (stub implementation)
 */
export class NotificationPreferencesEntity {
  userId: string;

  // Categories
  transactional: boolean; // Order, payment (always true)
  informational: boolean; // Shop updates, daily summary
  marketing: boolean; // Promotions, vouchers

  updatedAt: string; // ISO 8601
}

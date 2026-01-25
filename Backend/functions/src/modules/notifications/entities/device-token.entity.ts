/**
 * Device Token Entity
 * Collection: device_tokens
 * Stores FCM tokens for push notifications
 */
export class DeviceTokenEntity {
  id: string;
  userId: string;
  token: string; // FCM registration token
  platform: 'android' | 'ios';

  deviceInfo?: {
    model?: string;
    osVersion?: string;
  };

  createdAt: string; // ISO 8601
  lastUsedAt: string; // ISO 8601
}

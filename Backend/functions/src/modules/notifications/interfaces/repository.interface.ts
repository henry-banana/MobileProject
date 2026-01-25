import { DeviceTokenEntity, NotificationEntity, NotificationPreferencesEntity } from '../entities';

export interface IDeviceTokensRepository {
  create(userId: string, data: Partial<DeviceTokenEntity>): Promise<DeviceTokenEntity>;
  findByUserId(userId: string): Promise<DeviceTokenEntity[]>;
  findByToken(token: string): Promise<DeviceTokenEntity | null>;
  updateLastUsed(userId: string, token: string): Promise<void>;
  deleteByToken(userId: string, token: string): Promise<void>;
  deleteByUserId(userId: string): Promise<void>;
}

export interface INotificationsRepository {
  create(userId: string, data: Partial<NotificationEntity>): Promise<NotificationEntity>;
  findById(userId: string, notificationId: string): Promise<NotificationEntity | null>;
  findByUserId(
    userId: string,
    filters?: {
      read?: boolean;
      limit?: number;
      offset?: number;
    },
  ): Promise<{ items: NotificationEntity[]; total: number }>;
  updateReadStatus(userId: string, notificationId: string): Promise<NotificationEntity>;
  markAllAsRead(userId: string): Promise<number>;
}

export interface INotificationPreferencesRepository {
  create(
    userId: string,
    data: Partial<NotificationPreferencesEntity>,
  ): Promise<NotificationPreferencesEntity>;
  findByUserId(userId: string): Promise<NotificationPreferencesEntity | null>;
  update(
    userId: string,
    data: Partial<NotificationPreferencesEntity>,
  ): Promise<NotificationPreferencesEntity>;
}

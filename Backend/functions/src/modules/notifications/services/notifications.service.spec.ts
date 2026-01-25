import { Test, TestingModule } from '@nestjs/testing';
import { NotificationsService } from './notifications.service';
import { FCMService } from './fcm.service';
import { DeviceTokenEntity, NotificationEntity } from '../entities';
import { RegisterDeviceTokenDto } from '../dto';
import { NotificationCategory } from '../dto/admin-batch-send.dto';

describe('NotificationsService', () => {
  let service: NotificationsService;
  let mockDeviceTokensRepository: any;
  let mockNotificationsRepository: any;
  let mockFcmService: any;

  beforeEach(async () => {
    // Mock repositories
    mockDeviceTokensRepository = {
      create: jest.fn(),
      findByUserId: jest.fn(),
      findByToken: jest.fn(),
      updateLastUsed: jest.fn(),
      deleteByToken: jest.fn(),
      deleteByUserId: jest.fn(),
    };

    mockNotificationsRepository = {
      create: jest.fn(),
      findById: jest.fn(),
      findByUserId: jest.fn(),
      updateReadStatus: jest.fn(),
      markAllAsRead: jest.fn(),
    };

    mockFcmService = {
      sendToTokens: jest.fn().mockResolvedValue(1),
      sendToToken: jest.fn().mockResolvedValue('message_id'),
      sendToTopic: jest.fn().mockResolvedValue('message_id'),
    };

    const mockNotificationPreferencesRepository = {
      findByUserId: jest.fn().mockResolvedValue(null), // Return null preferences (all enabled)
      getPreferences: jest.fn().mockResolvedValue(null),
      updatePreferences: jest.fn(),
    };

    const mockTopicSubscriptionsRepository = {
      subscribe: jest.fn(),
      unsubscribe: jest.fn(),
      getSubscriptions: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        NotificationsService,
        {
          provide: 'DEVICE_TOKENS_REPOSITORY',
          useValue: mockDeviceTokensRepository,
        },
        {
          provide: 'NOTIFICATIONS_REPOSITORY',
          useValue: mockNotificationsRepository,
        },
        {
          provide: 'NOTIFICATION_PREFERENCES_REPOSITORY',
          useValue: mockNotificationPreferencesRepository,
        },
        {
          provide: 'TOPIC_SUBSCRIPTIONS_REPOSITORY',
          useValue: mockTopicSubscriptionsRepository,
        },
        {
          provide: FCMService,
          useValue: mockFcmService,
        },
      ],
    }).compile();

    service = module.get<NotificationsService>(NotificationsService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('registerDeviceToken', () => {
    it('should create new device token if not exists', async () => {
      const userId = 'user_1';
      const dto: RegisterDeviceTokenDto = {
        token: 'new_token',
        platform: 'android',
        deviceInfo: {
          model: 'Samsung Galaxy S21',
          osVersion: 'Android 12',
        },
      };

      mockDeviceTokensRepository.findByToken.mockResolvedValue(null);
      mockDeviceTokensRepository.create.mockResolvedValue({
        id: 'device_1',
        userId,
        ...dto,
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      });

      const result = await service.registerDeviceToken(userId, dto);

      expect(mockDeviceTokensRepository.findByToken).toHaveBeenCalledWith('new_token');
      expect(mockDeviceTokensRepository.create).toHaveBeenCalledWith(userId, expect.any(Object));
      expect(result.token).toBe('new_token');
    });

    it('should update lastUsedAt if token exists for same user', async () => {
      const userId = 'user_1';
      const dto: RegisterDeviceTokenDto = {
        token: 'existing_token',
        platform: 'android',
      };

      const existingToken: DeviceTokenEntity = {
        id: 'device_1',
        userId,
        token: 'existing_token',
        platform: 'android',
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      };

      mockDeviceTokensRepository.findByToken.mockResolvedValue(existingToken);
      mockDeviceTokensRepository.updateLastUsed.mockResolvedValue(undefined);
      mockDeviceTokensRepository.findByToken.mockResolvedValueOnce(existingToken);

      await service.registerDeviceToken(userId, dto);

      expect(mockDeviceTokensRepository.updateLastUsed).toHaveBeenCalledWith(userId, 'existing_token');
    });

    it('should transfer token if it belongs to another user', async () => {
      const userId = 'user_1';
      const oldUserId = 'user_2';
      const dto: RegisterDeviceTokenDto = {
        token: 'transferred_token',
        platform: 'android',
      };

      mockDeviceTokensRepository.findByToken.mockResolvedValue({
        id: 'device_1',
        userId: oldUserId,
        token: 'transferred_token',
        platform: 'android',
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      });

      mockDeviceTokensRepository.deleteByToken.mockResolvedValue(undefined);
      mockDeviceTokensRepository.create.mockResolvedValue({
        id: 'device_2',
        userId,
        ...dto,
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      });

      await service.registerDeviceToken(userId, dto);

      expect(mockDeviceTokensRepository.deleteByToken).toHaveBeenCalledWith(oldUserId, 'transferred_token');
      expect(mockDeviceTokensRepository.create).toHaveBeenCalledWith(userId, expect.any(Object));
    });
  });

  describe('unregisterDeviceToken', () => {
    it('should delete device token if belongs to user', async () => {
      const userId = 'user_1';
      const token = 'device_token';

      mockDeviceTokensRepository.findByToken.mockResolvedValue({
        id: 'device_1',
        userId,
        token,
        platform: 'android',
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      });

      mockDeviceTokensRepository.deleteByToken.mockResolvedValue(undefined);

      await service.unregisterDeviceToken(userId, token);

      expect(mockDeviceTokensRepository.deleteByToken).toHaveBeenCalledWith(userId, token);
    });

    it('should not delete if token belongs to another user', async () => {
      const userId = 'user_1';
      const token = 'device_token';

      mockDeviceTokensRepository.findByToken.mockResolvedValue({
        id: 'device_1',
        userId: 'user_2',
        token,
        platform: 'android',
        createdAt: new Date().toISOString(),
        lastUsedAt: new Date().toISOString(),
      });

      await service.unregisterDeviceToken(userId, token);

      expect(mockDeviceTokensRepository.deleteByToken).not.toHaveBeenCalled();
    });

    it('should be idempotent if token does not exist', async () => {
      const userId = 'user_1';
      const token = 'nonexistent_token';

      mockDeviceTokensRepository.findByToken.mockResolvedValue(null);

      await service.unregisterDeviceToken(userId, token);

      expect(mockDeviceTokensRepository.deleteByToken).not.toHaveBeenCalled();
    });
  });

  describe('getMyNotifications', () => {
    it('should return paginated notifications with unread count', async () => {
      const userId = 'user_1';
      const notifications: NotificationEntity[] = [
        {
          id: 'notif_1',
          userId,
          title: 'Order confirmed',
          body: 'Your order has been confirmed',
          type: 'ORDER_CONFIRMED' as any,
          read: false,
          createdAt: new Date().toISOString(),
        },
      ];

      mockNotificationsRepository.findByUserId
        .mockResolvedValueOnce({ items: notifications, total: 50 }) // For paginated results
        .mockResolvedValueOnce({ items: [], total: 15 }); // For unread count

      const result = await service.getMyNotifications(userId, undefined, 1, 20);

      expect(result.items).toHaveLength(1);
      expect(result.total).toBe(50);
      expect(result.unreadCount).toBe(15);
      expect(result.page).toBe(1);
      expect(result.limit).toBe(20);
    });

    it('should filter by read=false (unread only)', async () => {
      const userId = 'user_1';
      const unreadNotification: NotificationEntity = {
        id: 'notif_1',
        userId,
        title: 'Unread notification',
        body: 'This is unread',
        type: 'ORDER_CONFIRMED' as any,
        read: false,
        createdAt: new Date().toISOString(),
      };

      mockNotificationsRepository.findByUserId
        .mockResolvedValueOnce({ items: [unreadNotification], total: 1 }) // For read=false filter
        .mockResolvedValueOnce({ items: [unreadNotification], total: 1 }); // For unread count

      const result = await service.getMyNotifications(userId, false, 1, 20);

      expect(result.items).toHaveLength(1);
      expect(result.items[0].read).toBe(false);
      expect(result.total).toBe(1);
      expect(result.unreadCount).toBe(1);
      expect(mockNotificationsRepository.findByUserId).toHaveBeenCalledWith(userId, {
        read: false,
        limit: 20,
        offset: 0,
      });
    });

    it('should filter by read=true (read only)', async () => {
      const userId = 'user_1';
      const readNotification: NotificationEntity = {
        id: 'notif_2',
        userId,
        title: 'Read notification',
        body: 'This is read',
        type: 'ORDER_CONFIRMED' as any,
        read: true,
        createdAt: new Date().toISOString(),
      };

      mockNotificationsRepository.findByUserId
        .mockResolvedValueOnce({ items: [readNotification], total: 5 }) // For read=true filter
        .mockResolvedValueOnce({ items: [], total: 0 }); // For unread count

      const result = await service.getMyNotifications(userId, true, 1, 20);

      expect(result.items).toHaveLength(1);
      expect(result.items[0].read).toBe(true);
      expect(result.total).toBe(5);
      expect(result.unreadCount).toBe(0);
      expect(mockNotificationsRepository.findByUserId).toHaveBeenCalledWith(userId, {
        read: true,
        limit: 20,
        offset: 0,
      });
    });

    it('should return all notifications when read filter is undefined', async () => {
      const userId = 'user_1';

      mockNotificationsRepository.findByUserId
        .mockResolvedValueOnce({ items: [], total: 10 }) // For no filter (all notifications)
        .mockResolvedValueOnce({ items: [], total: 3 }); // For unread count

      const result = await service.getMyNotifications(userId, undefined, 1, 20);

      expect(result.total).toBe(10); // All notifications
      expect(result.unreadCount).toBe(3); // Only unread
      expect(mockNotificationsRepository.findByUserId).toHaveBeenCalledWith(userId, {
        read: undefined,
        limit: 20,
        offset: 0,
      });
    });
  });

  describe('markAsRead', () => {
    it('should mark notification as read', async () => {
      const userId = 'user_1';
      const notificationId = 'notif_1';

      const notification: NotificationEntity = {
        id: notificationId,
        userId,
        title: 'Test',
        body: 'Test',
        type: 'ORDER_CONFIRMED' as any,
        read: false,
        createdAt: new Date().toISOString(),
      };

      mockNotificationsRepository.findById.mockResolvedValue(notification);
      mockNotificationsRepository.updateReadStatus.mockResolvedValue({
        ...notification,
        read: true,
        readAt: new Date().toISOString(),
      });

      const result = await service.markAsRead(userId, notificationId);

      expect(result.read).toBe(true);
      expect(mockNotificationsRepository.updateReadStatus).toHaveBeenCalledWith(userId, notificationId);
    });

    it('should throw error if notification not found', async () => {
      const userId = 'user_1';
      const notificationId = 'nonexistent';

      mockNotificationsRepository.findById.mockResolvedValue(null);

      await expect(service.markAsRead(userId, notificationId)).rejects.toThrow('Notification not found');
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all notifications as read', async () => {
      const userId = 'user_1';

      mockNotificationsRepository.markAllAsRead.mockResolvedValue(10);

      const result = await service.markAllAsRead(userId);

      expect(result).toBe(10);
      expect(mockNotificationsRepository.markAllAsRead).toHaveBeenCalledWith(userId);
    });
  });

  describe('adminBatchSend', () => {
    it('should send MARKETING notification without orderId to multiple users', async () => {
      const userIds = ['user_1', 'user_2'];
      mockNotificationsRepository.create.mockResolvedValue({
        id: 'notif_1',
        userId: userIds[0],
        title: 'Special Offer',
        body: '50% off today!',
        type: 'PROMOTION',
        read: false,
        createdAt: new Date().toISOString(),
        // Note: no orderId in response
      });

      mockDeviceTokensRepository.findByUserId.mockResolvedValue([
        { id: 'device_1', token: 'token_1', userId: userIds[0] },
      ]);

      const result = await service.adminBatchSend({
        userIds,
        title: 'Special Offer',
        body: '50% off today!',
        type: 'PROMOTION' as any,
        category: NotificationCategory.MARKETING,
        data: { promoId: 'promo_123' },
        // No orderId - should not cause Firestore write failures
      });

      expect(result.requestedCount).toBe(2);
      expect(result.successCount).toBeGreaterThan(0);
      expect(mockNotificationsRepository.create).toHaveBeenCalled();
      
      // Verify that the created notification does not have undefined fields
      const createCall = mockNotificationsRepository.create.mock.calls[0];
      const createdData = createCall[1];
      expect(createdData.orderId).toBeUndefined(); // Should not be set, not undefined in the payload
      expect(createdData.shopId).toBeUndefined(); // Should not be set
    });
  });
});


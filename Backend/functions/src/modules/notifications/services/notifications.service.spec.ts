import { Test, TestingModule } from '@nestjs/testing';
import { NotificationsService } from './notifications.service';
import { FCMService } from './fcm.service';
import { IDeviceTokensRepository, INotificationsRepository } from '../interfaces';
import { DeviceTokenEntity, NotificationEntity } from '../entities';
import { RegisterDeviceTokenDto } from '../dto';

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

      const result = await service.registerDeviceToken(userId, dto);

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

      const result = await service.registerDeviceToken(userId, dto);

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

    it('should filter by read status', async () => {
      const userId = 'user_1';

      mockNotificationsRepository.findByUserId
        .mockResolvedValueOnce({ items: [], total: 10 }) // For paginated results
        .mockResolvedValueOnce({ items: [], total: 15 }); // For unread count

      await service.getMyNotifications(userId, false, 1, 20);

      expect(mockNotificationsRepository.findByUserId).toHaveBeenCalledWith(userId, {
        read: false,
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
});

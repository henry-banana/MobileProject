import { Module } from '@nestjs/common';
import { NotificationsService } from './services';
import { FCMService } from './services/fcm.service';
import {
  FirestoreDeviceTokensRepository,
  FirestoreNotificationsRepository,
  FirestoreNotificationPreferencesRepository,
  FirestoreTopicSubscriptionsRepository,
} from './repositories';
import { NotificationsController, NotificationsAdminController } from './controllers';

@Module({
  controllers: [NotificationsController, NotificationsAdminController],
  providers: [
    NotificationsService,
    FCMService,
    {
      provide: 'DEVICE_TOKENS_REPOSITORY',
      useClass: FirestoreDeviceTokensRepository,
    },
    {
      provide: 'NOTIFICATIONS_REPOSITORY',
      useClass: FirestoreNotificationsRepository,
    },
    {
      provide: 'NOTIFICATION_PREFERENCES_REPOSITORY',
      useClass: FirestoreNotificationPreferencesRepository,
    },
    {
      provide: 'TOPIC_SUBSCRIPTIONS_REPOSITORY',
      useClass: FirestoreTopicSubscriptionsRepository,
    },
  ],
  exports: [NotificationsService, FCMService],
})
export class NotificationsModule {}

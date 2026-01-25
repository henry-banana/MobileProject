import { Injectable, Inject, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { NotificationPayload } from '../interfaces';

/**
 * FCM Service
 * Handles Firebase Cloud Messaging operations
 */
@Injectable()
export class FCMService {
  private readonly logger = new Logger(FCMService.name);
  private readonly fcm: admin.messaging.Messaging | null;

  constructor(@Inject('FIREBASE_APP') firebaseApp: admin.app.App) {
    try {
      this.fcm = firebaseApp.messaging();
    } catch (error) {
      this.logger.warn(
        'FCM not available in this environment. Notifications will be stored but not sent.',
        error,
      );
      this.fcm = null;
    }
  }

  /**
   * Send notification to single device
   */
  async sendToToken(token: string, notification: NotificationPayload): Promise<string | null> {
    if (!this.fcm) {
      this.logger.debug('FCM not available, skipping send to token');
      return null;
    }

    try {
      const message: admin.messaging.Message = {
        token,
        notification: {
          title: notification.title,
          body: notification.body,
          imageUrl: notification.imageUrl,
        },
        data: this.stringifyData(notification.data || {}),
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
            clickAction: 'FLUTTER_NOTIFICATION_CLICK',
          },
        },
        apns: {
          payload: {
            aps: {
              sound: 'default',
            },
          },
        },
      };

      const response = await this.fcm.send(message);
      this.logger.log(`FCM message sent: ${response}`);
      return response;
    } catch (err: unknown) {
      const error = err as any;
      this.logger.error(`Failed to send FCM message to token ${token}:`, error);

      // If token is invalid, remove it from database
      if (
        error.code === 'messaging/invalid-registration-token' ||
        error.code === 'messaging/registration-token-not-registered'
      ) {
        this.logger.log(`Removing invalid token: ${token}`);
        // Note: we don't know userId here, so just log it
        // In a real scenario, you might want to clean these up in batch
      }

      return null;
    }
  }

  /**
   * Send notification to multiple devices
   */
  async sendToTokens(tokens: string[], notification: NotificationPayload): Promise<number> {
    if (!this.fcm || tokens.length === 0) {
      this.logger.debug(`FCM not available or no tokens. Skipping batch send.`);
      return 0;
    }

    try {
      let successCount = 0;
      const message = (token: string): admin.messaging.Message => ({
        token,
        notification: {
          title: notification.title,
          body: notification.body,
          imageUrl: notification.imageUrl,
        },
        data: this.stringifyData(notification.data || {}),
        android: {
          priority: 'high',
          notification: {
            sound: 'default',
          },
        },
      });

      // Send to each token
      for (const token of tokens) {
        try {
          await this.fcm.send(message(token));
          successCount++;
        } catch (err) {
          const error = err as any;
          this.logger.error(`Failed to send to ${token}: ${error.code}`);
        }
      }

      this.logger.log(`FCM batch sent: ${successCount}/${tokens.length} successful`);
      return successCount;
    } catch (error) {
      this.logger.error('Failed to send FCM batch:', error);
      return 0;
    }
  }

  /**
   * Send notification to topic
   */
  async sendToTopic(topic: string, notification: NotificationPayload): Promise<string | null> {
    if (!this.fcm) {
      this.logger.debug('FCM not available, skipping send to topic');
      return null;
    }

    try {
      const message: admin.messaging.Message = {
        topic,
        notification: {
          title: notification.title,
          body: notification.body,
          imageUrl: notification.imageUrl,
        },
        data: this.stringifyData(notification.data || {}),
      };

      const response = await this.fcm.send(message);
      this.logger.log(`FCM message sent to topic ${topic}: ${response}`);
      return response;
    } catch (error) {
      this.logger.error(`Failed to send FCM message to topic ${topic}:`, error);
      return null;
    }
  }

  /**
   * Subscribe tokens to topic
   */
  async subscribeToTopic(tokens: string[], topic: string): Promise<void> {
    if (!this.fcm || tokens.length === 0) {
      this.logger.debug('FCM not available or no tokens, skipping topic subscription');
      return;
    }

    try {
      await this.fcm.subscribeToTopic(tokens, topic);
      this.logger.log(`Subscribed ${tokens.length} tokens to topic ${topic}`);
    } catch (error) {
      this.logger.error(`Failed to subscribe to topic ${topic}:`, error);
    }
  }

  /**
   * Unsubscribe tokens from topic
   */
  async unsubscribeFromTopic(tokens: string[], topic: string): Promise<void> {
    if (!this.fcm || tokens.length === 0) {
      this.logger.debug('FCM not available or no tokens, skipping topic unsubscription');
      return;
    }

    try {
      await this.fcm.unsubscribeFromTopic(tokens, topic);
      this.logger.log(`Unsubscribed ${tokens.length} tokens from topic ${topic}`);
    } catch (error) {
      this.logger.error(`Failed to unsubscribe from topic ${topic}:`, error);
    }
  }

  /**
   * Helper: Convert data object to string key-value pairs (FCM requirement)
   */
  private stringifyData(data: Record<string, unknown>): Record<string, string> {
    const result: Record<string, string> = {};

    Object.keys(data).forEach((key) => {
      const value = data[key];
      if (value !== null && value !== undefined) {
        result[key] = typeof value === 'string' ? value : JSON.stringify(value);
      }
    });

    return result;
  }
}

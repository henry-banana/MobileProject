export interface NotificationPayload {
  title: string;
  body: string;
  imageUrl?: string;
  data?: Record<string, unknown>;
}

export interface SendNotificationOptions {
  userId: string;
  title: string;
  body: string;
  type: string;
  data?: Record<string, unknown>;
  imageUrl?: string;
  orderId?: string;
  shopId?: string;
}

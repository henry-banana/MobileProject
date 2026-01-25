import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { NotificationType } from '../entities';

/**
 * Available Notification DTO
 * Response for GET /notifications
 */
export class AvailableNotificationDto {
  @ApiProperty({
    description: 'Notification ID',
    example: 'notif_1',
  })
  id: string;

  @ApiProperty({
    description: 'Notification title',
    example: 'Đơn hàng đã được xác nhận',
  })
  title: string;

  @ApiProperty({
    description: 'Notification body',
    example: 'Đơn hàng #ORD-001 đang được chuẩn bị',
  })
  body: string;

  @ApiPropertyOptional({
    description: 'Notification image URL',
    example: 'https://example.com/image.jpg',
  })
  imageUrl?: string;

  @ApiProperty({
    description: 'Notification type',
    example: 'ORDER_CONFIRMED',
  })
  type: NotificationType;

  @ApiPropertyOptional({
    description: 'Additional data for deep linking',
    example: {
      orderId: 'order_1',
      orderNumber: 'ORD-001',
    },
  })
  data?: Record<string, unknown>;

  @ApiProperty({
    description: 'Whether notification has been read',
    example: false,
  })
  read: boolean;

  @ApiPropertyOptional({
    description: 'When notification was read (ISO 8601)',
    example: '2024-01-08T10:05:00Z',
  })
  readAt?: string;

  @ApiPropertyOptional({
    description: 'Related order ID',
    example: 'order_1',
  })
  orderId?: string;

  @ApiPropertyOptional({
    description: 'Related shop ID',
    example: 'shop_1',
  })
  shopId?: string;

  @ApiProperty({
    description: 'When notification was created (ISO 8601)',
    example: '2024-01-08T10:00:00Z',
  })
  createdAt: string;
}

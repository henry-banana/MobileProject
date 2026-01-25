import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsOptional, IsObject, IsEnum } from 'class-validator';
import { NotificationType } from '../entities';
import { NotificationCategory } from './admin-batch-send.dto';

/**
 * Topic Send DTO
 * Used for POST /admin/notifications/topics/:topic/send
 */
export class TopicSendDto {
  @ApiProperty({
    description: 'Notification title',
    example: 'New Order Available',
  })
  @IsString()
  title: string;

  @ApiProperty({
    description: 'Notification body text',
    example: 'A new order is ready for pickup in your area!',
  })
  @IsString()
  body: string;

  @ApiProperty({
    description: 'Notification type',
    enum: NotificationType,
    example: NotificationType.ORDER_READY,
  })
  @IsEnum(NotificationType)
  type: NotificationType;

  @ApiProperty({
    description: 'Notification category (affects preference filtering)',
    enum: NotificationCategory,
    example: NotificationCategory.INFORMATIONAL,
    required: false,
    default: NotificationCategory.INFORMATIONAL,
  })
  @IsOptional()
  @IsEnum(NotificationCategory)
  category?: NotificationCategory;

  @ApiProperty({
    description: 'Additional data payload (for deep linking, etc.)',
    example: { orderId: 'order_123', shopId: 'shop_456' },
    required: false,
  })
  @IsOptional()
  @IsObject()
  data?: Record<string, any>;

  @ApiProperty({
    description: 'Image URL for rich notification',
    example: 'https://example.com/order.jpg',
    required: false,
  })
  @IsOptional()
  @IsString()
  imageUrl?: string;
}

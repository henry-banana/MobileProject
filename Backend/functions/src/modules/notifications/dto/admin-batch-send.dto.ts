import { ApiProperty } from '@nestjs/swagger';
import { IsArray, IsString, IsOptional, IsObject, IsEnum, ArrayMinSize } from 'class-validator';
import { NotificationType } from '../entities';

export enum NotificationCategory {
  TRANSACTIONAL = 'TRANSACTIONAL',
  INFORMATIONAL = 'INFORMATIONAL',
  MARKETING = 'MARKETING',
}

/**
 * Admin Batch Send DTO
 * Used for POST /admin/notifications/batch
 */
export class AdminBatchSendDto {
  @ApiProperty({
    description: 'Array of user IDs to send notifications to',
    example: ['user_1', 'user_2', 'user_3'],
    type: [String],
  })
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  userIds: string[];

  @ApiProperty({
    description: 'Notification title',
    example: 'Special Promotion',
  })
  @IsString()
  title: string;

  @ApiProperty({
    description: 'Notification body text',
    example: 'Get 50% off on all orders this weekend!',
  })
  @IsString()
  body: string;

  @ApiProperty({
    description: 'Notification type',
    enum: NotificationType,
    example: NotificationType.PROMOTION,
  })
  @IsEnum(NotificationType)
  type: NotificationType;

  @ApiProperty({
    description: 'Notification category (affects preference filtering)',
    enum: NotificationCategory,
    example: NotificationCategory.MARKETING,
    required: false,
    default: NotificationCategory.INFORMATIONAL,
  })
  @IsOptional()
  @IsEnum(NotificationCategory)
  category?: NotificationCategory;

  @ApiProperty({
    description: 'Additional data payload (for deep linking, etc.)',
    example: { promoCode: 'WEEKEND50', url: '/vouchers/promo-123' },
    required: false,
  })
  @IsOptional()
  @IsObject()
  data?: Record<string, any>;

  @ApiProperty({
    description: 'Image URL for rich notification',
    example: 'https://example.com/promo.jpg',
    required: false,
  })
  @IsOptional()
  @IsString()
  imageUrl?: string;
}

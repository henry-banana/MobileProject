import { IsOptional, IsNumber } from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Notification Query DTO
 * Query parameters for GET /notifications
 */
export class NotificationQueryDto {
  @ApiPropertyOptional({
    description: 'Filter by read status',
    example: false,
    required: false,
  })
  @IsOptional()
  @Transform(({ value }) => {
    // Debug logging
    if (process.env.DEBUG_NOTIF_QUERY === '1') {
      console.log(
        '[NotificationQueryDto @Transform] Received value:',
        value,
        'typeof:',
        typeof value,
      );
    }

    // Handle string from query params: "true" -> true, "false" -> false
    if (value === 'true' || value === true || value === 1 || value === '1') {
      if (process.env.DEBUG_NOTIF_QUERY === '1') {
        console.log('[NotificationQueryDto @Transform] Converted to true');
      }
      return true;
    }
    if (value === 'false' || value === false || value === 0 || value === '0') {
      if (process.env.DEBUG_NOTIF_QUERY === '1') {
        console.log('[NotificationQueryDto @Transform] Converted to false');
      }
      return false;
    }
    if (process.env.DEBUG_NOTIF_QUERY === '1') {
      console.log('[NotificationQueryDto @Transform] Returning undefined');
    }
    return undefined; // Return undefined if invalid value, so validation can catch it
  })
  read?: boolean;

  @ApiPropertyOptional({
    description: 'Page number (1-based)',
    example: 1,
    default: 1,
  })
  @IsOptional()
  @IsNumber()
  @Type(() => Number)
  page?: number = 1;

  @ApiPropertyOptional({
    description: 'Items per page',
    example: 20,
    default: 20,
  })
  @IsOptional()
  @IsNumber()
  @Type(() => Number)
  limit?: number = 20;
}

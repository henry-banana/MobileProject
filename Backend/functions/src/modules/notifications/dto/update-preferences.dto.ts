import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsOptional } from 'class-validator';

/**
 * Update Notification Preferences DTO
 * Used for PUT /notifications/preferences
 *
 * Note: transactional is always enabled and cannot be modified
 */
export class UpdatePreferencesDto {
  @ApiProperty({
    description: 'Enable/disable informational notifications (shop updates, daily summaries)',
    example: true,
    required: false,
  })
  @IsOptional()
  @IsBoolean()
  informational?: boolean;

  @ApiProperty({
    description: 'Enable/disable marketing notifications (promotions, vouchers)',
    example: false,
    required: false,
  })
  @IsOptional()
  @IsBoolean()
  marketing?: boolean;
}

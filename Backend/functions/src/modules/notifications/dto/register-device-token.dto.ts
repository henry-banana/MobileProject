import { IsString, IsOptional, IsNotEmpty } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Register Device Token DTO
 * Request body for POST /notifications/tokens
 */
export class RegisterDeviceTokenDto {
  @ApiProperty({
    description: 'FCM registration token',
    example: 'cRLstRFyj7k:APA91bHi8L...',
  })
  @IsString()
  @IsNotEmpty()
  token: string;

  @ApiProperty({
    description: 'Device platform',
    enum: ['android', 'ios'],
    example: 'android',
  })
  @IsString()
  @IsNotEmpty()
  platform: 'android' | 'ios';

  @ApiPropertyOptional({
    description: 'Device information',
    example: {
      model: 'Samsung Galaxy S21',
      osVersion: 'Android 12',
    },
  })
  @IsOptional()
  deviceInfo?: {
    model?: string;
    osVersion?: string;
  };
}

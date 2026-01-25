import { ApiProperty } from '@nestjs/swagger';
import { IsArray, IsString, IsOptional, ArrayMinSize, ValidateIf } from 'class-validator';

/**
 * Topic Subscribe/Unsubscribe DTO
 * Used for POST /admin/notifications/topics/:topic/subscribe and unsubscribe
 *
 * Must provide either userIds or tokens (not both)
 */
export class TopicSubscribeDto {
  @ApiProperty({
    description: 'Array of user IDs to subscribe (will resolve to their device tokens)',
    example: ['user_1', 'user_2'],
    type: [String],
    required: false,
  })
  @IsOptional()
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  @ValidateIf((o) => !o.tokens)
  userIds?: string[];

  @ApiProperty({
    description: 'Array of FCM device tokens to subscribe directly',
    example: ['token_1', 'token_2'],
    type: [String],
    required: false,
  })
  @IsOptional()
  @IsArray()
  @ArrayMinSize(1)
  @IsString({ each: true })
  @ValidateIf((o) => !o.userIds)
  tokens?: string[];
}

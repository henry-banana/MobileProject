import { IsString, IsNotEmpty, IsEnum, IsOptional, MaxLength } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * User Status Enum
 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  BANNED = 'BANNED',
}

/**
 * UpdateUserStatusDto - DTO để ban/unban user
 */
export class UpdateUserStatusDto {
  @ApiProperty({
    description: 'Trạng thái mới của user',
    enum: UserStatus,
    example: UserStatus.BANNED,
  })
  @IsEnum(UserStatus)
  @IsNotEmpty()
  status: UserStatus;

  @ApiPropertyOptional({
    description: 'Lý do (bắt buộc khi ban)',
    example: 'Vi phạm chính sách sử dụng',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  reason?: string;
}

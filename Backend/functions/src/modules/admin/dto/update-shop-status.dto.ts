import { IsString, IsNotEmpty, IsEnum, IsOptional, MaxLength } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Shop Status Enum (Admin perspective)
 */
export enum ShopAdminStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  BANNED = 'BANNED',
}

/**
 * UpdateShopStatusDto - DTO để suspend/ban shop
 */
export class UpdateShopStatusDto {
  @ApiProperty({
    description: 'Trạng thái mới của shop',
    enum: ShopAdminStatus,
    example: ShopAdminStatus.SUSPENDED,
  })
  @IsEnum(ShopAdminStatus)
  @IsNotEmpty()
  status: ShopAdminStatus;

  @ApiPropertyOptional({
    description: 'Lý do (bắt buộc khi suspend/ban)',
    example: 'Nhiều complaint từ khách hàng',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  reason?: string;
}

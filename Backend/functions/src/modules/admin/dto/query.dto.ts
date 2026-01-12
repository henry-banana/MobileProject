import { IsString, IsOptional, IsInt, Min, Max, IsEnum } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { UserStatus } from './update-user-status.dto';
import { PayoutStatus } from './payout.dto';
import { ShopAdminStatus } from './update-shop-status.dto';

/**
 * UserRole Enum for filtering
 */
export enum UserRoleFilter {
  CUSTOMER = 'CUSTOMER',
  OWNER = 'OWNER',
  SHIPPER = 'SHIPPER',
}

/**
 * PaginationQueryDto - Base pagination DTO
 */
export class PaginationQueryDto {
  @ApiPropertyOptional({
    description: 'Số trang',
    default: 1,
    minimum: 1,
  })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @ApiPropertyOptional({
    description: 'Số items mỗi trang',
    default: 20,
    minimum: 1,
    maximum: 100,
  })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  limit?: number = 20;
}

/**
 * ListUsersQueryDto - Query params cho list users
 */
export class ListUsersQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({
    description: 'Lọc theo role',
    enum: UserRoleFilter,
  })
  @IsOptional()
  @IsEnum(UserRoleFilter)
  role?: UserRoleFilter;

  @ApiPropertyOptional({
    description: 'Lọc theo status',
    enum: UserStatus,
  })
  @IsOptional()
  @IsEnum(UserStatus)
  status?: UserStatus;

  @ApiPropertyOptional({
    description: 'Tìm kiếm theo name hoặc email',
  })
  @IsOptional()
  @IsString()
  search?: string;
}

/**
 * ListPayoutsQueryDto - Query params cho list payouts
 */
export class ListPayoutsQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({
    description: 'Lọc theo status',
    enum: PayoutStatus,
    default: PayoutStatus.PENDING,
  })
  @IsOptional()
  @IsEnum(PayoutStatus)
  status?: PayoutStatus = PayoutStatus.PENDING;
}

/**
 * ListShopsQueryDto - Query params cho list shops
 */
export class ListShopsQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({
    description: 'Lọc theo status',
    enum: ShopAdminStatus,
  })
  @IsOptional()
  @IsEnum(ShopAdminStatus)
  status?: ShopAdminStatus;

  @ApiPropertyOptional({
    description: 'Tìm kiếm theo tên shop',
  })
  @IsOptional()
  @IsString()
  search?: string;
}

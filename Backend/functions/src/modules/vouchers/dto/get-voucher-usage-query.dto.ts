import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsInt, Min, Max, IsISO8601, IsString } from 'class-validator';
import { Type } from 'class-transformer';

/**
 * Query DTO for GET /vouchers/me/usage
 * Supports pagination, date filtering, and optional shop filtering
 */
export class GetVoucherUsageQueryDto {
  @ApiPropertyOptional({
    description: 'Page number (1-based)',
    minimum: 1,
    default: 1,
    example: 1,
  })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @ApiPropertyOptional({
    description: 'Items per page',
    minimum: 1,
    maximum: 100,
    default: 20,
    example: 20,
  })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  limit?: number = 20;

  @ApiPropertyOptional({
    description: 'Filter by shop ID (optional, if not provided returns all shops)',
    example: 'shop_123',
  })
  @IsOptional()
  @IsString()
  shopId?: string;

  @ApiPropertyOptional({
    description: 'Start date filter (ISO 8601 format, inclusive)',
    example: '2026-01-01T00:00:00Z',
  })
  @IsOptional()
  @IsISO8601()
  from?: string;

  @ApiPropertyOptional({
    description: 'End date filter (ISO 8601 format, inclusive)',
    example: '2026-01-31T23:59:59Z',
  })
  @IsOptional()
  @IsISO8601()
  to?: string;
}

import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Voucher Usage Record DTO (for owner listing)
 * Similar to VoucherUsageDto but with additional context for owner
 */
export class OwnerVoucherUsageDto {
  @ApiProperty({
    example: 'voucher_summer_2024_user_123_order_456',
    description: 'Deterministic usage record ID',
  })
  id: string;

  @ApiProperty({
    example: 'voucher_summer_2024',
    description: 'Voucher ID',
  })
  voucherId: string;

  @ApiProperty({
    example: 'SUMMER20',
    description: 'Voucher code',
  })
  code: string;

  @ApiProperty({
    example: 'user_123',
    description: 'Customer user ID (anonymized if privacy flag set)',
  })
  userId: string;

  @ApiPropertyOptional({
    example: 'John Doe',
    description: 'Display name of customer (if available)',
  })
  displayName?: string;

  @ApiProperty({
    example: 'order_456',
    description: 'Order ID this voucher was used for',
  })
  orderId: string;

  @ApiProperty({
    example: 7500,
    description: 'Discount amount applied (in VND)',
  })
  discountAmount: number;

  @ApiProperty({
    example: '2026-01-20T15:30:45Z',
    description: 'When the voucher was used (ISO 8601)',
  })
  createdAt: string;
}

/**
 * Paginated Owner Voucher Usage Response
 */
export class PaginatedOwnerVoucherUsageDto {
  @ApiProperty({
    type: () => [OwnerVoucherUsageDto],
    description: 'List of usage records',
  })
  items: OwnerVoucherUsageDto[];

  @ApiProperty({
    example: 1,
    description: 'Current page (1-based)',
  })
  page: number;

  @ApiProperty({
    example: 20,
    description: 'Items per page',
  })
  limit: number;

  @ApiProperty({
    example: 150,
    description: 'Total number of usage records',
  })
  total: number;

  @ApiProperty({
    example: 8,
    description: 'Total number of pages',
  })
  pages: number;

  @ApiProperty({
    example: true,
    description: 'Whether there are more pages',
  })
  hasMore: boolean;
}

/**
 * Aggregated Voucher Statistics DTO
 * Shows performance metrics for a voucher
 */
export class VoucherStatsDto {
  @ApiProperty({
    example: 'voucher_summer_2024',
    description: 'Voucher ID',
  })
  voucherId: string;

  @ApiProperty({
    example: 'SUMMER20',
    description: 'Voucher code',
  })
  code: string;

  @ApiProperty({
    example: 45,
    description: 'Total number of times voucher was used',
  })
  totalUses: number;

  @ApiProperty({
    example: 337500,
    description: 'Total discount amount given out (in VND)',
  })
  totalDiscountAmount: number;

  @ApiProperty({
    example: 38,
    description: 'Number of unique customers who used this voucher',
  })
  uniqueUsers: number;

  @ApiProperty({
    example: '2026-01-25T14:30:00Z',
    description: 'When this voucher was last used (ISO 8601)',
  })
  lastUsedAt: string;

  @ApiProperty({
    example: 45,
    description: 'Usage percentage: (totalUses / usageLimit) * 100',
  })
  usagePercentage: number;

  @ApiProperty({
    example: 'SUMMER20',
    description: 'Voucher code (for reference)',
  })
  voucherCode: string;

  @ApiProperty({
    example: 100,
    description: 'Maximum number of uses for this voucher',
  })
  usageLimit: number;

  @ApiProperty({
    example: 7500,
    description: 'Average discount amount per use',
  })
  averageDiscount: number;
}

/**
 * Response envelope for stats
 */
export class VoucherStatsResponseDto {
  @ApiProperty({
    type: () => VoucherStatsDto,
    description: 'Voucher statistics',
  })
  stats: VoucherStatsDto;

  @ApiProperty({
    example: '2026-01-25T14:30:00Z',
    description: 'When stats were generated',
  })
  generatedAt: string;
}

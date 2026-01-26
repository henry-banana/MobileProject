import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Voucher Usage DTO
 * Returned in GET /vouchers/me/usage endpoint
 */
export class VoucherUsageDto {
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

  @ApiPropertyOptional({
    example: 'SUMMER20',
    description: 'Voucher code (optional if not available)',
  })
  code?: string;

  @ApiProperty({
    example: 'shop_123',
    description: 'Shop ID',
  })
  shopId: string;

  @ApiProperty({
    example: 'order_abc123',
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
 * Paginated Voucher Usage Response
 */
export class PaginatedVoucherUsageDto {
  @ApiProperty({
    type: () => [VoucherUsageDto],
    description: 'List of usage records',
  })
  items: VoucherUsageDto[];

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
    example: 45,
    description: 'Total number of usage records',
  })
  total: number;

  @ApiProperty({
    example: 3,
    description: 'Total number of pages',
  })
  pages: number;

  @ApiProperty({
    example: true,
    description: 'Whether there are more pages',
  })
  hasMore: boolean;
}

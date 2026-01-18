import { ApiProperty } from '@nestjs/swagger';

export class OrderListItemDto {
  @ApiProperty({
    description: 'Order ID',
    example: 'order_123',
  })
  id: string;

  @ApiProperty({
    description: 'Human-readable order number',
    example: 'ORD-001',
  })
  orderNumber: string;

  @ApiProperty({
    description: 'Shop ID',
    example: 'shop_123',
  })
  shopId: string;

  @ApiProperty({
    description: 'Shop name',
    example: 'Cơm Tấm Sườn',
  })
  shopName: string;

  @ApiProperty({
    description: 'Order status',
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    example: 'PENDING',
  })
  status: string;

  @ApiProperty({
    description: 'Payment status',
    enum: ['UNPAID', 'PROCESSING', 'PAID', 'REFUNDED'],
    example: 'UNPAID',
  })
  paymentStatus: string;

  @ApiProperty({
    description: 'Total order amount',
    example: 65000,
  })
  total: number;

  @ApiProperty({
    description: 'Number of items in order',
    example: 2,
  })
  itemCount: number;

  @ApiProperty({
    description: 'Order creation timestamp',
    example: '2024-01-08T10:00:00Z',
  })
  createdAt: any;
}

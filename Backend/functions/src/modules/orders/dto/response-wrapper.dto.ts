import { ApiProperty } from '@nestjs/swagger';

/**
 * Generic response wrapper for success responses
 */
export class ResponseWrapperDto<T> {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Response data',
  })
  data: T;

  @ApiProperty({
    description: 'Response timestamp',
    example: '2024-01-08T10:00:00.000Z',
  })
  timestamp: string;
}

/**
 * Wrapper specifically for OrderEntity responses
 */
export class OrderResponseDto {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Order data',
    example: {
      id: 'order_123',
      orderNumber: 'ORD-001',
      customerId: 'user_123',
      shopId: 'shop_123',
      shopName: 'Cơm Tấm Sườn',
      shipperId: null,
      items: [
        {
          productId: 'prod_123',
          productName: 'Cơm sườn bì chả',
          quantity: 2,
          price: 25000,
          subtotal: 50000,
        },
      ],
      subtotal: 50000,
      shipFee: 15000,
      discount: 0,
      total: 65000,
      status: 'PENDING',
      paymentStatus: 'UNPAID',
      paymentMethod: 'COD',
      deliveryAddress: {
        street: '123 Nguyen Hue',
        ward: 'Ben Nghe',
        district: 'District 1',
        city: 'Ho Chi Minh City',
      },
      deliveryNote: 'Call before delivery',
      createdAt: '2024-01-08T10:00:00.000Z',
    },
  })
  data: any;

  @ApiProperty({
    description: 'Response timestamp',
    example: '2024-01-08T10:00:00.000Z',
  })
  timestamp: string;
}

/**
 * Wrapper for paginated orders responses
 */
export class PaginatedOrdersResponseDto {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Paginated orders data',
    example: {
      orders: [
        {
          id: 'order_123',
          orderNumber: 'ORD-001',
          shopId: 'shop_123',
          shopName: 'Cơm Tấm Sườn',
          status: 'PENDING',
          paymentStatus: 'UNPAID',
          total: 65000,
          itemCount: 2,
          createdAt: '2024-01-08T10:00:00.000Z',
        },
        {
          id: 'order_124',
          orderNumber: 'ORD-002',
          shopId: 'shop_123',
          shopName: 'Cơm Tấm Sườn',
          status: 'SHIPPING',
          paymentStatus: 'PAID',
          total: 85000,
          itemCount: 3,
          createdAt: '2024-01-08T09:30:00.000Z',
        },
      ],
      page: 1,
      limit: 10,
      total: 42,
      totalPages: 5,
    },
  })
  data: any;

  @ApiProperty({
    description: 'Response timestamp',
    example: '2024-01-08T10:00:00.000Z',
  })
  timestamp: string;
}

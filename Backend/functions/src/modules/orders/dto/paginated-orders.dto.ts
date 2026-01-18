import { ApiProperty } from '@nestjs/swagger';
import { OrderListItemDto } from './order-list-item.dto';

export class PaginatedOrdersDto {
  @ApiProperty({
    description: 'List of orders',
    type: [OrderListItemDto],
  })
  orders: OrderListItemDto[];

  @ApiProperty({
    description: 'Current page number',
    example: 1,
  })
  page: number;

  @ApiProperty({
    description: 'Number of items per page',
    example: 10,
  })
  limit: number;

  @ApiProperty({
    description: 'Total number of orders matching filter',
    example: 42,
  })
  total: number;

  @ApiProperty({
    description: 'Total number of pages',
    example: 5,
  })
  totalPages: number;

  @ApiProperty({
    description: 'Whether there is a next page',
    example: true,
  })
  hasNext: boolean;

  @ApiProperty({
    description: 'Whether there is a previous page',
    example: false,
  })
  hasPrev: boolean;
}

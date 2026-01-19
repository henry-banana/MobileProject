import { IsOptional, IsString, IsNumber, Min, Max } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class OrderFilterDto {
  @ApiPropertyOptional({
    description: 'Filter by order status',
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    example: 'PENDING',
  })
  @IsString()
  @IsOptional()
  status?: string;

  @ApiPropertyOptional({
    description: 'Page number (1-indexed)',
    minimum: 1,
    default: 1,
    example: 1,
  })
  @Type(() => Number)
  @IsNumber()
  @IsOptional()
  @Min(1)
  page?: number = 1;

  @ApiPropertyOptional({
    description: 'Number of results per page',
    minimum: 1,
    maximum: 50,
    default: 10,
    example: 10,
  })
  @Type(() => Number)
  @IsNumber()
  @IsOptional()
  @Min(1)
  @Max(50)
  limit?: number = 10;
}

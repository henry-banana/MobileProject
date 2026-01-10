import { IsOptional, IsString, IsNumber, IsEnum, IsBoolean, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';

export enum ProductSortOption {
  NEWEST = 'newest',
  POPULAR = 'popular',
  RATING = 'rating',
  PRICE = 'price',
}

export class ProductFilterDto {
  @ApiProperty({ required: false, example: 'cat_123', description: 'Filter by category' })
  @IsOptional()
  @IsString()
  categoryId?: string;

  @ApiProperty({ required: false, example: 'shop_123', description: 'Filter by shop' })
  @IsOptional()
  @IsString()
  shopId?: string;

  @ApiProperty({ required: false, example: 'phở', description: 'Search keyword' })
  @IsOptional()
  @IsString()
  q?: string;

  @ApiProperty({ required: false, example: 10000, description: 'Minimum price' })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  minPrice?: number;

  @ApiProperty({ required: false, example: 50000, description: 'Maximum price' })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  maxPrice?: number;

  @ApiProperty({
    required: false,
    enum: ProductSortOption,
    example: ProductSortOption.POPULAR,
    description: 'Sort option',
  })
  @IsOptional()
  @IsEnum(ProductSortOption)
  sort?: ProductSortOption;

  @ApiProperty({
    required: false,
    example: true,
    description: 'Filter by availability (owner only)',
  })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isAvailable?: boolean;

  @ApiProperty({ required: false, example: 1, description: 'Page number' })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  page?: number;

  @ApiProperty({ required: false, example: 20, description: 'Items per page' })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  limit?: number;
}

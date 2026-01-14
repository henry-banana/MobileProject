import { IsOptional, IsString, IsInt, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Filter DTO for Owner's product list
 * Simpler than ProductFilterDto (no search, price range, sort)
 */
export class OwnerProductFilterDto {
  @ApiPropertyOptional({ example: 'cat_123', description: 'Filter by category' })
  @IsOptional()
  @IsString()
  categoryId?: string;

  @ApiPropertyOptional({
    example: 'true',
    description: 'Filter by availability (true/false)',
    type: String,
    enum: ['true', 'false'],
  })
  @IsOptional()
  @IsString()
  isAvailable?: string;

  @ApiPropertyOptional({ example: 1, description: 'Page number' })
  @IsOptional()
  @IsInt()
  @Min(1)
  @Type(() => Number)
  page?: number = 1;

  @ApiPropertyOptional({ example: 20, description: 'Items per page' })
  @IsOptional()
  @IsInt()
  @Min(1)
  @Type(() => Number)
  limit?: number = 20;
}

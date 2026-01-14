import { IsString, IsOptional, IsEnum, IsInt, Min, MaxLength, Matches } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { CategoryStatus } from '../entities/category.entity';

/**
 * UpdateCategoryDto - DTO để update category
 *
 * Tất cả fields đều optional
 */
export class UpdateCategoryDto {
  @ApiPropertyOptional({
    description: 'Tên category',
    example: 'Cơm Việt',
    maxLength: 100,
  })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  name?: string;

  @ApiPropertyOptional({
    description: 'Slug (URL-friendly)',
    example: 'com-viet',
    maxLength: 100,
  })
  @IsOptional()
  @IsString()
  @MaxLength(100)
  @Matches(/^[a-z0-9]+(?:-[a-z0-9]+)*$/, {
    message: 'Slug chỉ được chứa chữ thường, số và dấu gạch ngang',
  })
  slug?: string;

  @ApiPropertyOptional({
    description: 'Mô tả category',
    example: 'Các món cơm truyền thống Việt Nam',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  description?: string;

  @ApiPropertyOptional({
    description: 'URL ảnh đại diện',
    example: 'https://storage.googleapis.com/ktx-delivery/categories/com-viet.jpg',
  })
  @IsOptional()
  @IsString()
  imageUrl?: string;

  @ApiPropertyOptional({
    description: 'Icon name (material icons)',
    example: 'restaurant',
  })
  @IsOptional()
  @IsString()
  icon?: string;

  @ApiPropertyOptional({
    description: 'Thứ tự hiển thị (số nhỏ hiển thị trước)',
    example: 2,
    minimum: 0,
  })
  @IsOptional()
  @IsInt()
  @Min(0)
  displayOrder?: number;

  @ApiPropertyOptional({
    description: 'Trạng thái',
    enum: CategoryStatus,
  })
  @IsOptional()
  @IsEnum(CategoryStatus)
  status?: CategoryStatus;
}

import {
  IsString,
  IsNotEmpty,
  IsOptional,
  IsEnum,
  IsInt,
  Min,
  MaxLength,
  Matches,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { CategoryStatus } from '../entities/category.entity';

/**
 * CreateCategoryDto - DTO để tạo category mới
 *
 * Sử dụng bởi Admin để tạo category
 */
export class CreateCategoryDto {
  @ApiProperty({
    description: 'Tên category',
    example: 'Cơm',
    maxLength: 100,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  name: string;

  @ApiPropertyOptional({
    description: 'Slug (URL-friendly). Nếu không cung cấp, sẽ tự động generate từ name',
    example: 'com',
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
    example: 'Các món cơm đa dạng từ khắp nơi',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500)
  description?: string;

  @ApiPropertyOptional({
    description: 'URL ảnh đại diện',
    example: 'https://storage.googleapis.com/ktx-delivery/categories/com.jpg',
  })
  @IsOptional()
  @IsString()
  imageUrl?: string;

  @ApiPropertyOptional({
    description: 'Icon name (material icons)',
    example: 'rice_bowl',
  })
  @IsOptional()
  @IsString()
  icon?: string;

  @ApiPropertyOptional({
    description: 'Thứ tự hiển thị (số nhỏ hiển thị trước). Mặc định là sau category cuối',
    example: 1,
    minimum: 0,
  })
  @IsOptional()
  @IsInt()
  @Min(0)
  displayOrder?: number;

  @ApiPropertyOptional({
    description: 'Trạng thái',
    enum: CategoryStatus,
    default: CategoryStatus.ACTIVE,
  })
  @IsOptional()
  @IsEnum(CategoryStatus)
  status?: CategoryStatus;
}

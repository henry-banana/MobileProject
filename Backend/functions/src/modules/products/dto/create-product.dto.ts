import {
  IsString,
  IsNotEmpty,
  IsNumber,
  Min,
  IsOptional,
  IsUrl,
  MaxLength,
  IsInt,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateProductDto {
  @ApiProperty({ example: 'Cơm sườn nướng', description: 'Product name' })
  @IsString()
  @IsNotEmpty({ message: 'Tên sản phẩm không được để trống' })
  @MaxLength(100, { message: 'Tên sản phẩm tối đa 100 ký tự' })
  name: string;

  @ApiProperty({
    example: 'Cơm sườn nướng mật ong + trứng',
    description: 'Product description',
  })
  @IsString()
  @IsNotEmpty({ message: 'Mô tả không được để trống' })
  @MaxLength(500, { message: 'Mô tả tối đa 500 ký tự' })
  description: string;

  @ApiProperty({ example: 35000, description: 'Price in VND' })
  @IsNumber()
  @Min(1000, { message: 'Giá tối thiểu 1,000đ' })
  price: number;

  @ApiProperty({ example: 'cat_123', description: 'Category ID' })
  @IsString()
  @IsNotEmpty({ message: 'Category không được để trống' })
  categoryId: string;

  @ApiProperty({
    example: 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445',
    description: 'Product image URL',
    required: false,
  })
  @IsOptional()
  @IsUrl({}, { message: 'URL hình ảnh không hợp lệ' })
  imageUrl?: string;

  @ApiProperty({ example: 15, description: 'Preparation time in minutes' })
  @IsInt({ message: 'Thời gian chuẩn bị phải là số nguyên' })
  @Min(5, { message: 'Thời gian chuẩn bị tối thiểu 5 phút' })
  preparationTime: number;
}

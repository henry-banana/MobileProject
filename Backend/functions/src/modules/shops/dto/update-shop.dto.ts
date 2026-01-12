import {
  IsString,
  IsOptional,
  MaxLength,
  MinLength,
  IsNumber,
  Min,
  Matches,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class UpdateShopDto {
  @ApiPropertyOptional({
    example: 'Quán Phở Việt',
    description: 'Tên shop (3-100 ký tự)',
  })
  @IsOptional()
  @IsString()
  @MinLength(3, { message: 'Tên shop phải có ít nhất 3 ký tự' })
  @MaxLength(100, { message: 'Tên shop không được quá 100 ký tự' })
  name?: string;

  @ApiPropertyOptional({
    example: 'Phở ngon nhất KTX, nước dùng ngọt tự nhiên',
    description: 'Mô tả shop (tối đa 500 ký tự)',
  })
  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Mô tả không được quá 500 ký tự' })
  description?: string;

  @ApiPropertyOptional({
    example: 'Tòa A, Tầng 1, KTX ĐHQG',
    description: 'Địa chỉ shop (tối đa 200 ký tự)',
  })
  @IsOptional()
  @IsString()
  @MaxLength(200, { message: 'Địa chỉ không được quá 200 ký tự' })
  address?: string;

  @ApiPropertyOptional({
    example: '0901234567',
    description: 'Số điện thoại (10 chữ số)',
  })
  @IsOptional()
  @IsString()
  @Matches(/^[0-9]{10}$/, { message: 'Số điện thoại phải là 10 chữ số' })
  phone?: string;

  @ApiPropertyOptional({
    example: '07:00',
    description: 'Giờ mở cửa (HH:mm)',
  })
  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ mở cửa phải có định dạng HH:mm (VD: 07:00)',
  })
  openTime?: string;

  @ApiPropertyOptional({
    example: '21:00',
    description: 'Giờ đóng cửa (HH:mm)',
  })
  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ đóng cửa phải có định dạng HH:mm (VD: 21:00)',
  })
  closeTime?: string;

  @ApiPropertyOptional({
    example: 5000,
    description: 'Phí ship mỗi đơn (tối thiểu 3,000đ)',
  })
  @IsOptional()
  @IsNumber()
  @Min(3000, { message: 'Phí ship tối thiểu 3,000đ' })
  shipFeePerOrder?: number;

  @ApiPropertyOptional({
    example: 20000,
    description: 'Đơn tối thiểu (từ 10,000đ)',
  })
  @IsOptional()
  @IsNumber()
  @Min(10000, { message: 'Đơn tối thiểu phải từ 10,000đ' })
  minOrderAmount?: number;

  @ApiPropertyOptional({
    example: 'https://storage.googleapis.com/.../cover.jpg',
    description: 'URL ảnh bìa shop',
  })
  @IsOptional()
  @IsString()
  coverImageUrl?: string;

  @ApiPropertyOptional({
    example: 'https://storage.googleapis.com/.../logo.jpg',
    description: 'URL logo shop',
  })
  @IsOptional()
  @IsString()
  logoUrl?: string;
}

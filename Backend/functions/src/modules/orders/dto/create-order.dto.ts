import { IsString, IsOptional, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { DeliveryAddress } from '../entities';

export class DeliveryAddressDto implements DeliveryAddress {
  @ApiProperty({
    description: 'Street address',
    example: '123 Nguyen Hue',
  })
  @IsString()
  street: string;

  @ApiProperty({
    description: 'Ward name',
    example: 'Ben Nghe',
  })
  @IsString()
  ward: string;

  @ApiProperty({
    description: 'District name',
    example: 'District 1',
  })
  @IsString()
  district: string;

  @ApiProperty({
    description: 'City name',
    example: 'Ho Chi Minh City',
  })
  @IsString()
  city: string;

  @ApiPropertyOptional({
    description: 'GPS coordinates (optional)',
    example: { lat: 10.7769, lng: 106.7009 },
  })
  @IsOptional()
  coordinates?: {
    lat: number;
    lng: number;
  };
}

export class CreateOrderDto {
  @ApiProperty({
    description: 'Shop ID to create order from',
    example: 'shop_123',
  })
  @IsString()
  shopId: string;

  @ApiProperty({
    description: 'Delivery address details',
    type: DeliveryAddressDto,
  })
  @ValidateNested()
  @Type(() => DeliveryAddressDto)
  deliveryAddress: DeliveryAddressDto;

  @ApiPropertyOptional({
    description: 'Additional delivery notes',
    example: 'Call before delivery',
  })
  @IsString()
  @IsOptional()
  deliveryNote?: string;

  @ApiProperty({
    description: 'Payment method',
    enum: ['COD', 'ZALOPAY', 'MOMO', 'SEPAY'],
    example: 'COD',
  })
  @IsString()
  paymentMethod: 'COD' | 'ZALOPAY' | 'MOMO' | 'SEPAY';

  @ApiPropertyOptional({
    description: 'Voucher code to apply (optional)',
    example: 'FREESHIP10',
  })
  @IsString()
  @IsOptional()
  voucherCode?: string;
}

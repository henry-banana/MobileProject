import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsString, Matches } from 'class-validator';

export enum VehicleType {
  MOTORBIKE = 'MOTORBIKE',
  CAR = 'CAR',
  BICYCLE = 'BICYCLE',
}

export class UpdateVehicleDto {
  @ApiProperty({
    enum: VehicleType,
    example: VehicleType.MOTORBIKE,
    description: 'Vehicle type',
  })
  @IsEnum(VehicleType)
  vehicleType: VehicleType;

  @ApiProperty({
    example: '59X1-12345',
    description: 'Vehicle license plate number',
  })
  @IsString()
  @Matches(/^[0-9]{2}[A-Z]{1,2}[-\s]?[0-9]{4,5}$/, {
    message: 'Invalid vehicle number format. Example: 59X1-12345',
  })
  vehicleNumber: string;
}

export class VehicleInfoDto {
  @ApiProperty({ enum: VehicleType, example: VehicleType.MOTORBIKE })
  vehicleType: VehicleType;

  @ApiProperty({ example: '59X1-12345' })
  vehicleNumber: string;

  @ApiPropertyOptional({ example: 'https://storage.googleapis.com/.../license.jpg' })
  driverLicenseUrl?: string;
}

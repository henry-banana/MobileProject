import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Timestamp } from '@google-cloud/firestore';

export enum ApplicationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export enum VehicleType {
  MOTORBIKE = 'MOTORBIKE',
  CAR = 'CAR',
  BICYCLE = 'BICYCLE',
}

export class ShipperApplicationEntity {
  @ApiProperty({ example: 'app_abc123' })
  id: string;

  @ApiProperty({ example: 'uid_123' })
  userId: string;

  @ApiProperty({ example: 'Nguyễn Văn A' })
  userName: string;

  @ApiProperty({ example: '0901234567' })
  userPhone: string;

  @ApiProperty({ example: 'https://...' })
  userAvatar: string;

  @ApiProperty({ example: 'shop_abc' })
  shopId: string;

  @ApiProperty({ example: 'Quán A Mập' })
  shopName: string;

  @ApiProperty({ enum: VehicleType, example: VehicleType.MOTORBIKE })
  vehicleType: VehicleType;

  @ApiProperty({ example: '59X1-12345' })
  vehicleNumber: string;

  @ApiProperty({ example: '079202012345' })
  idCardNumber: string;

  @ApiProperty({ example: 'https://storage.googleapis.com/.../id_front.jpg' })
  idCardFrontUrl: string;

  @ApiProperty({ example: 'https://storage.googleapis.com/.../id_back.jpg' })
  idCardBackUrl: string;

  @ApiProperty({ example: 'https://storage.googleapis.com/.../license.jpg' })
  driverLicenseUrl: string;

  @ApiProperty({ example: 'Tôi muốn làm shipper...' })
  message: string;

  @ApiProperty({ enum: ApplicationStatus, example: ApplicationStatus.PENDING })
  status: ApplicationStatus;

  @ApiPropertyOptional({ example: 'owner_uid_123' })
  reviewedBy?: string;

  @ApiPropertyOptional({ example: '2026-01-13T10:00:00Z' })
  reviewedAt?: Timestamp | Date | string;

  @ApiPropertyOptional({ example: 'Không đủ điều kiện' })
  rejectReason?: string;

  @ApiProperty({ example: '2026-01-13T10:00:00Z' })
  createdAt: Timestamp | Date | string;

  constructor(data: Partial<ShipperApplicationEntity>) {
    Object.assign(this, data);
  }
}

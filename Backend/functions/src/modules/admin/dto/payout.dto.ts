import { IsString, IsNotEmpty, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Payout Status Enum
 */
export enum PayoutStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  TRANSFERRED = 'TRANSFERRED',
}

/**
 * RejectPayoutDto - DTO để reject payout request
 */
export class RejectPayoutDto {
  @ApiProperty({
    description: 'Lý do từ chối',
    example: 'Thông tin tài khoản không chính xác',
    maxLength: 500,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(500)
  reason: string;
}

/**
 * MarkTransferredDto - DTO để đánh dấu đã chuyển khoản
 */
export class MarkTransferredDto {
  @ApiProperty({
    description: 'Mã giao dịch ngân hàng',
    example: 'VCB_20260107_001',
    maxLength: 100,
  })
  @IsString()
  @IsNotEmpty()
  @MaxLength(100)
  transferNote: string;
}

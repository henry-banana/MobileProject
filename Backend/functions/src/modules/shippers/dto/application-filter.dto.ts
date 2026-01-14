import { IsOptional, IsEnum } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export enum ApplicationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export class ApplicationFilterDto {
  @ApiPropertyOptional({
    enum: ApplicationStatus,
    example: ApplicationStatus.PENDING,
    description: 'Filter theo trạng thái đơn',
  })
  @IsOptional()
  @IsEnum(ApplicationStatus, { message: 'Trạng thái không hợp lệ' })
  status?: ApplicationStatus;
}

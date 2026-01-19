import { IsOptional, IsString } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class CancelOrderDto {
  @ApiPropertyOptional({
    description: 'Reason for cancellation',
    example: 'Changed my mind',
  })
  @IsString()
  @IsOptional()
  reason?: string;
}

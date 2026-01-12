import { IsString, IsNotEmpty, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RejectApplicationDto {
  @ApiProperty({
    example: 'Không đủ điều kiện',
    description: 'Lý do từ chối đơn (tối đa 500 ký tự)',
  })
  @IsNotEmpty({ message: 'Lý do từ chối không được để trống' })
  @IsString()
  @MaxLength(500, { message: 'Lý do từ chối không được quá 500 ký tự' })
  reason: string;
}

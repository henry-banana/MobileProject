import { IsBoolean } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ToggleShopStatusDto {
  @ApiProperty({ example: false, description: 'Shop open status' })
  @IsBoolean({ message: 'isOpen phải là boolean' })
  isOpen: boolean;
}

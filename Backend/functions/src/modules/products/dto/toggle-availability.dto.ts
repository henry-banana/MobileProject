import { IsBoolean } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ToggleAvailabilityDto {
  @ApiProperty({ example: false, description: 'Product availability status' })
  @IsBoolean({ message: 'isAvailable phải là boolean' })
  isAvailable: boolean;
}

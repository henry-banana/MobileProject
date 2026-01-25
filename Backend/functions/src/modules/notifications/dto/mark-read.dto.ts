import { IsNotEmpty, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Mark Read DTO
 * Request body for PUT /notifications/:id/read
 */
export class MarkReadDto {
  @ApiProperty({
    description: 'Notification ID to mark as read',
    example: 'notif_1',
  })
  @IsString()
  @IsNotEmpty()
  notificationId: string;
}

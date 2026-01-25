import { Controller, Post, Body, Param, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiParam } from '@nestjs/swagger';
import { NotificationsService } from '../services';
import { AdminBatchSendDto, TopicSubscribeDto, TopicSendDto } from '../dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Admin Notifications Controller
 * Admin-only endpoints for batch sending and topic management
 *
 * Base URL: /admin/notifications
 *
 * Tasks: NOTIF-011 (Batch Send), NOTIF-012 (Topic Management)
 *
 * IMPORTANT: All endpoints require ADMIN role
 */
@ApiTags('Admin - Notifications')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
@Controller('admin/notifications')
export class NotificationsAdminController {
  constructor(private readonly notificationsService: NotificationsService) {}

  // ==================== Admin Batch Send (NOTIF-011) ====================

  /**
   * POST /admin/notifications/batch
   * Send notification to multiple users (admin only)
   *
   * NOTIF-011: Admin Batch Send
   *
   * Rules:
   * - Send to multiple users in one request
   * - Best-effort delivery (don't fail entire batch if one fails)
   * - Persist notification history per user
   * - Return summary of results
   */
  @Post('batch')
  @ApiOperation({
    summary: 'Batch send notifications (admin only)',
    description: 'Send notification to multiple users. Returns summary of success/failure counts.',
  })
  @ApiResponse({
    status: 200,
    description: 'Batch send completed',
    schema: {
      type: 'object',
      properties: {
        requestedCount: { type: 'number', example: 100, description: 'Total users requested' },
        successCount: { type: 'number', example: 95, description: 'Successfully sent' },
        failureCount: { type: 'number', example: 5, description: 'Failed to send' },
        failures: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              userId: { type: 'string', example: 'user_1' },
              reason: { type: 'string', example: 'User not found' },
            },
          },
          description: 'List of failures (if any)',
        },
      },
    },
  })
  async batchSend(@Body() dto: AdminBatchSendDto): Promise<{
    requestedCount: number;
    successCount: number;
    failureCount: number;
    failures?: Array<{ userId: string; reason: string }>;
  }> {
    return await this.notificationsService.adminBatchSend({
      userIds: dto.userIds,
      title: dto.title,
      body: dto.body,
      type: dto.type,
      category: dto.category,
      data: dto.data,
      imageUrl: dto.imageUrl,
    });
  }

  // ==================== Topic Management (NOTIF-012) ====================

  /**
   * POST /admin/notifications/topics/:topic/subscribe
   * Subscribe users/tokens to topic (admin only)
   *
   * NOTIF-012: Topic Management
   *
   * Rules:
   * - Provide either userIds or tokens (not both)
   * - If userIds provided, resolve to device tokens
   * - Subscribe to FCM topic
   * - Track subscriptions in database
   */
  @Post('topics/:topic/subscribe')
  @ApiOperation({
    summary: 'Subscribe to notification topic (admin only)',
    description: 'Subscribe users or tokens to a notification topic for broadcast messages.',
  })
  @ApiParam({
    name: 'topic',
    description: 'Topic name (e.g., "shippers_active", "order_ready")',
    example: 'shippers_active',
  })
  @ApiResponse({
    status: 200,
    description: 'Successfully subscribed',
    schema: {
      type: 'object',
      properties: {
        subscribedCount: {
          type: 'number',
          example: 25,
          description: 'Number of tokens subscribed',
        },
      },
    },
  })
  async subscribeToTopic(
    @Param('topic') topic: string,
    @Body() dto: TopicSubscribeDto,
  ): Promise<{ subscribedCount: number }> {
    return await this.notificationsService.subscribeToTopic({
      topic,
      userIds: dto.userIds,
      tokens: dto.tokens,
    });
  }

  /**
   * POST /admin/notifications/topics/:topic/unsubscribe
   * Unsubscribe users/tokens from topic (admin only)
   *
   * NOTIF-012: Topic Management
   */
  @Post('topics/:topic/unsubscribe')
  @ApiOperation({
    summary: 'Unsubscribe from notification topic (admin only)',
    description: 'Unsubscribe users or tokens from a notification topic.',
  })
  @ApiParam({
    name: 'topic',
    description: 'Topic name',
    example: 'shippers_active',
  })
  @ApiResponse({
    status: 200,
    description: 'Successfully unsubscribed',
    schema: {
      type: 'object',
      properties: {
        unsubscribedCount: {
          type: 'number',
          example: 10,
          description: 'Number of tokens unsubscribed',
        },
      },
    },
  })
  async unsubscribeFromTopic(
    @Param('topic') topic: string,
    @Body() dto: TopicSubscribeDto,
  ): Promise<{ unsubscribedCount: number }> {
    return await this.notificationsService.unsubscribeFromTopic({
      topic,
      userIds: dto.userIds,
      tokens: dto.tokens,
    });
  }

  /**
   * POST /admin/notifications/topics/:topic/send
   * Send notification to topic (admin only)
   *
   * NOTIF-012: Topic Management
   *
   * Rules:
   * - Send to FCM topic (broadcast)
   * - Persist notification history for all subscribed users
   * - Enables shipper broadcast for ORDER_READY, etc.
   */
  @Post('topics/:topic/send')
  @ApiOperation({
    summary: 'Send notification to topic (admin only)',
    description:
      'Broadcast notification to all subscribers of a topic. Also persists history for subscribed users.',
  })
  @ApiParam({
    name: 'topic',
    description: 'Topic name',
    example: 'shippers_active',
  })
  @ApiResponse({
    status: 200,
    description: 'Notification sent to topic',
    schema: {
      type: 'object',
      properties: {
        sentToFCM: { type: 'boolean', example: true, description: 'Whether FCM send succeeded' },
        persistedCount: {
          type: 'number',
          example: 25,
          description: 'Number of user histories persisted',
        },
      },
    },
  })
  async sendToTopic(
    @Param('topic') topic: string,
    @Body() dto: TopicSendDto,
  ): Promise<{ sentToFCM: boolean; persistedCount: number }> {
    return await this.notificationsService.sendToTopic({
      topic,
      title: dto.title,
      body: dto.body,
      type: dto.type,
      category: dto.category,
      data: dto.data,
      imageUrl: dto.imageUrl,
    });
  }
}

import {
  Controller,
  Post,
  Req,
  UseGuards,
  HttpCode,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiOkResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
} from '@nestjs/swagger';
import { OrdersService } from '../services';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Orders Admin Controller
 *
 * Administrative order management endpoints
 * All endpoints require ADMIN authentication
 *
 * Base URL: /api/orders
 *
 * ⚠️ IMPORTANT: Role-Based Access Control
 * - Only ADMIN tokens can access these endpoints
 * - CUSTOMER, OWNER, or SHIPPER tokens will receive 403 Forbidden
 * - Error message: "Access denied. Required roles: ADMIN"
 * - All endpoints in this controller require ADMIN role
 * - See OrdersController for CUSTOMER endpoints, OrdersOwnerController for OWNER endpoints, OrdersShipperController for SHIPPER endpoints
 *
 * Tasks: ADMIN maintenance and data migration tasks
 */
@ApiTags('Orders - Admin')
@Controller('orders')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
export class OrdersAdminController {
  constructor(private ordersService: OrdersService) {}

  /**
   * Backfill shipperId null for pre-fix orders
   *
   * Problem: Orders created before the fix may lack shipperId field.
   * Firestore query .where('shipperId', '==', null) won't match documents
   * where the field is missing entirely.
   *
   * Solution: This endpoint scans orders and adds shipperId: null to those
   * missing this field, making them visible to shippers.
   *
   * Safe: Idempotent (can run multiple times)
   *
   * @param req Express request object with authenticated user info
   * @returns Object with scan statistics: scanned, updated, skipped, errors
   *
   * @example
   * POST /api/orders/admin/backfill-shipperId-null
   *
   * Response:
   * {
   *   success: true,
   *   data: {
   *     scanned: 250,
   *     updated: 45,
   *     skipped: 205,
   *     errors: []
   *   },
   *   timestamp: "2024-01-19T10:30:00Z"
   * }
   */
  @Post('admin/backfill-shipperId-null')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Backfill shipperId field for existing orders',
    description:
      'Scans READY/PENDING/CONFIRMED/PREPARING orders and adds shipperId: null to those missing the field. ' +
      'Required after deploy to fix orders created before this update. Admin-only endpoint.',
  })
  @ApiOkResponse({
    description: 'Backfill completed',
    schema: {
      example: {
        scanned: 250,
        updated: 45,
        skipped: 205,
        errors: [],
      },
    },
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  @ApiForbiddenResponse({ description: 'Must be ADMIN role' })
  async backfillShipperIdNull(@Req() req: any): Promise<any> {
    console.log(
      `[Backfill] Started by admin: ${req.user?.email || req.user?.uid}`,
    );

    // Call backfill service (injected in orders.service)
    return await this.ordersService.backfillShipperIdNull();
  }
}

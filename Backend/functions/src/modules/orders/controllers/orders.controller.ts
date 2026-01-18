import {
  Controller,
  Get,
  Post,
  Put,
  Body,
  Param,
  Req,
  UseGuards,
  Query,
  HttpCode,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiBadRequestResponse,
  ApiNotFoundResponse,
  ApiConflictResponse,
  ApiUnauthorizedResponse,
  ApiForbiddenResponse,
  ApiBody,
  ApiQuery,
  ApiParam,
} from '@nestjs/swagger';
import { OrdersService } from '../services';
import {
  CreateOrderDto,
  OrderFilterDto,
  CancelOrderDto,
  PaginatedOrdersDto,
  OrderResponseDto,
  PaginatedOrdersResponseDto,
} from '../dto';
import { OrderEntity } from '../entities';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Orders Controller - Customer Endpoints
 *
 * Customer-facing order endpoints
 * All endpoints require CUSTOMER authentication
 *
 * Base URL: /api/orders
 *
 * Tasks: ORDER-002 to ORDER-005, ORDER-010 (shop variant)
 */
@ApiTags('Orders')
@Controller('orders')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.CUSTOMER)
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  /**
   * POST /api/orders
   * Create a new order from cart
   *
   * ORDER-002 (MVP)
   * CRITICAL: Uses Firestore transaction for atomic cart clearing
   */
  @Post()
  @HttpCode(201)
  @ApiOperation({
    summary: 'Create a new order',
    description:
      'Creates an order from cart and atomically clears the cart group for the specified shop',
  })
  @ApiBody({
    type: CreateOrderDto,
    examples: {
      cod: {
        summary: 'COD Order',
        value: {
          shopId: 'shop_123',
          deliveryAddress: {
            street: '123 Nguyen Hue',
            ward: 'Ben Nghe',
            district: 'District 1',
            city: 'Ho Chi Minh City',
          },
          deliveryNote: 'Call before delivery',
          paymentMethod: 'COD',
        },
      },
      online: {
        summary: 'Online Payment with Voucher',
        value: {
          shopId: 'shop_456',
          deliveryAddress: {
            street: '456 Le Loi',
            ward: 'Ben Thanh',
            district: 'District 1',
            city: 'Ho Chi Minh City',
            coordinates: { lat: 10.7769, lng: 106.7009 },
          },
          deliveryNote: 'Leave at door',
          paymentMethod: 'ZALOPAY',
          voucherCode: 'FREESHIP10',
        },
      },
    },
  })
  @ApiCreatedResponse({
    description: 'Order created successfully',
    type: OrderResponseDto,
  })
  @ApiBadRequestResponse({ description: 'Invalid input data' })
  @ApiNotFoundResponse({ description: 'Cart or shop not found' })
  @ApiConflictResponse({ description: 'Shop closed or product unavailable' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async createOrder(
    @Req() req: any,
    @Body() dto: CreateOrderDto,
  ): Promise<{ success: boolean; data: OrderEntity; timestamp: string }> {
    const order = await this.ordersService.createOrder(req.user.uid, dto);
    return {
      success: true,
      data: order,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * GET /api/orders
   * List customer's orders with page-based pagination
   *
   * ORDER-003 (MVP)
   */
  @Get()
  @ApiOperation({
    summary: 'Get my orders',
    description: 'Retrieve paginated list of orders for authenticated customer',
  })
  @ApiQuery({
    name: 'status',
    required: false,
    enum: ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING', 'DELIVERED', 'CANCELLED'],
    description: 'Filter by order status',
    example: 'PENDING',
  })
  @ApiQuery({
    name: 'page',
    required: false,
    type: Number,
    description: 'Page number (1-indexed, default: 1)',
    example: 1,
  })
  @ApiQuery({
    name: 'limit',
    required: false,
    type: Number,
    description: 'Number of results per page (default: 10)',
    example: 10,
  })
  @ApiOkResponse({
    description: 'Orders retrieved successfully',
    type: PaginatedOrdersResponseDto,
  })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getMyOrders(
    @Req() req: any,
    @Query('status') status?: string,
    @Query('page') page?: number,
    @Query('limit') limit?: number,
  ): Promise<{
    success: boolean;
    data: PaginatedOrdersDto;
    timestamp: string;
  }> {
    const filter: OrderFilterDto = { status, page, limit };
    const data = await this.ordersService.getMyOrders(req.user.uid, filter);
    return {
      success: true,
      data,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * GET /api/orders/:id
   * Get order detail
   *
   * ORDER-004 (MVP)
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get order detail',
    description: 'Retrieve full details of a specific order',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiOkResponse({
    description: 'Order details retrieved',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized to view this order' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async getOrderDetail(
    @Req() req: any,
    @Param('id') orderId: string,
  ): Promise<{ success: boolean; data: OrderEntity; timestamp: string }> {
    const data = await this.ordersService.getOrderDetail(
      req.user.uid,
      orderId,
    );
    return {
      success: true,
      data,
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * PUT /api/orders/:id/cancel
   * Cancel order (customer)
   *
   * ORDER-005 (MVP)
   */
  @Put(':id/cancel')
  @ApiOperation({
    summary: 'Cancel order',
    description:
      'Cancel an order (only if in PENDING, CONFIRMED, or PREPARING status)',
  })
  @ApiParam({
    name: 'id',
    description: 'Order ID',
    example: 'order_123',
  })
  @ApiBody({
    type: CancelOrderDto,
    examples: {
      withReason: {
        summary: 'Cancel with reason',
        value: {
          reason: 'Changed my mind',
        },
      },
      withoutReason: {
        summary: 'Cancel without reason',
        value: {},
      },
    },
  })
  @ApiOkResponse({
    description: 'Order cancelled successfully',
    type: OrderResponseDto,
  })
  @ApiNotFoundResponse({ description: 'Order not found' })
  @ApiForbiddenResponse({ description: 'Not authorized' })
  @ApiConflictResponse({ description: 'Cannot cancel order in current status' })
  @ApiUnauthorizedResponse({ description: 'Not authenticated' })
  async cancelOrder(
    @Req() req: any,
    @Param('id') orderId: string,
    @Body() dto: CancelOrderDto,
  ): Promise<{ success: boolean; data: OrderEntity; timestamp: string }> {
    const data = await this.ordersService.cancelOrder(
      req.user.uid,
      orderId,
      dto.reason,
    );
    return {
      success: true,
      data,
      timestamp: new Date().toISOString(),
    };
  }
}

import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  ForbiddenException,
} from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import { IShippersRepository } from '../../shippers/repositories/shippers-repository.interface';
import { ShipperStatus } from '../../shippers/entities/shipper.entity';
import {
  OrderEntity,
  OrderStatus,
  PaymentStatus,
} from '../entities';
import {
  CreateOrderDto,
  OrderFilterDto,
  OrderListItemDto,
  PaginatedOrdersDto,
} from '../dto';
import { OrderStateMachineService } from './order-state-machine.service';

@Injectable()
export class OrdersService {
  constructor(
    @Inject(ORDERS_REPOSITORY)
    private readonly ordersRepo: IOrdersRepository,
    private readonly cartService: CartService,
    @Inject('PRODUCTS_REPOSITORY')
    private readonly productsRepo: IProductsRepository,
    @Inject('SHOPS_REPOSITORY')
    private readonly shopsRepo: IShopsRepository,
    @Inject('IShippersRepository')
    private readonly shippersRepo: IShippersRepository,
    private readonly stateMachine: OrderStateMachineService,
  ) {}

  /**
   * ORDER-002: Create a new order from cart
   * CRITICAL: Uses Firestore transaction to atomically create order and clear cart
   * All validation happens in service layer BEFORE transaction
   */
  async createOrder(
    customerId: string,
    dto: CreateOrderDto,
  ): Promise<OrderEntity> {
    // SERVICE LAYER VALIDATION (all pre-transaction checks)
    // This ensures cart state is confirmed before atomic transaction

    // 1. Get cart grouped by shop
    const { groups } = await this.cartService.getCartGrouped(customerId);
    if (!groups || groups.length === 0) {
      throw new NotFoundException({
        code: 'ORDER_001',
        message: 'Cart is empty',
        statusCode: 404,
      });
    }

    // 2. Get the specific shop group
    const shopGroup = groups.find((g) => g.shopId === dto.shopId);
    if (!shopGroup || shopGroup.items.length === 0) {
      throw new NotFoundException({
        code: 'ORDER_002',
        message: `No items found for shop ${dto.shopId} in cart`,
        statusCode: 404,
      });
    }

    // 3. Validate shop exists and is open
    const shop = await this.shopsRepo.findById(dto.shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'ORDER_003',
        message: 'Shop not found',
        statusCode: 404,
      });
    }
    if (!shop.isOpen || shop.status !== 'OPEN') {
      throw new ConflictException({
        code: 'ORDER_004',
        message: 'Shop is currently closed',
        statusCode: 409,
      });
    }

    // 4. Validate all products still available (for business logic only, not price)
    for (const item of shopGroup.items) {
      const product = await this.productsRepo.findById(item.productId);
      if (!product || !product.isAvailable || product.isDeleted) {
        throw new ConflictException({
          code: 'ORDER_005',
          message: `Product ${item.productName} is no longer available`,
          statusCode: 409,
        });
      }
      // NOTE: Do NOT re-fetch product.price here.
      // Prices are LOCKED from cart snapshot (price field).
    }

    // 5. Calculate totals using cart's subtotal (already calculated)
    const subtotal = shopGroup.subtotal;
    const shipFee = shop.shipFeePerOrder || 0;

    // 6. Apply voucher if provided (stub for MVP)
    let discount = 0;
    if (dto.voucherCode) {
      try {
        // TODO: Call voucherService.validateAndApply when available
        // discount = voucher.discountAmount;
      } catch (e) {
        console.warn(`Voucher ${dto.voucherCode} invalid:`, e);
      }
    }

    const total = subtotal + shipFee - discount;

    // 7. Create order entity
    // PRICING LOCK: All item prices are taken from cart snapshot (price field).
    // Product price changes after add-to-cart do NOT affect the order.
    const orderNumber = this.generateOrderNumber();
    const orderEntity: OrderEntity = {
      orderNumber,
      customerId,
      shopId: dto.shopId,
      shopName: shop.name,
      items: shopGroup.items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        price: item.price, // LOCKED: Use cart snapshot price
        subtotal: item.subtotal,
      })),
      subtotal,
      shipFee,
      discount,
      total,
      status: OrderStatus.PENDING,
      paymentStatus: PaymentStatus.UNPAID,
      paymentMethod: dto.paymentMethod,
      deliveryAddress: dto.deliveryAddress,
      deliveryNote: dto.deliveryNote,
    };

    // 8. CRITICAL: Use Firestore transaction
    // Create order + Clear cart group atomically
    const order = await this.ordersRepo.createOrderAndClearCartGroup(
      customerId,
      dto.shopId,
      orderEntity,
    );

    return order;
  }

  /**
   * ORDER-003: Get customer's orders with cursor-based pagination
   */
  async getMyOrders(
    customerId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    const { status, page = 1, limit = 10 } = filter;

    // Validate pagination params
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 10, 50);

    // Get total count using count() - efficient Firestore operation
    const total = await this.ordersRepo.count({
      customerId,
      ...(status && { status }),
    });
    const totalPages = Math.ceil(total / validLimit);

    // Build query for data (no fetch-all, use offset properly)
    let query = this.ordersRepo
      .query()
      .where('customerId', '==', customerId)
      .orderBy('createdAt', 'desc');

    if (status) {
      query = query.where('status', '==', status);
    }

    // Use offset + limit correctly (no over-fetch, no slice)
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // Execute query
    const orders = await this.ordersRepo.findMany(query);

    return {
      orders: orders.map(this.mapToListDto),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
      hasNext: validPage < totalPages,
      hasPrev: validPage > 1,
    };
  }

  /**
   * ORDER-004: Get order detail with ownership check
   */
  async getOrderDetail(
    customerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);

    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Security: Verify order belongs to customer
    if (order.customerId !== customerId) {
      throw new ForbiddenException({
        code: 'ORDER_007',
        message: 'You do not have permission to view this order',
        statusCode: 403,
      });
    }

    return order;
  }

  /**
   * ORDER-005: Cancel order (customer only, with state validation)
   */
  async cancelOrder(
    customerId: string,
    orderId: string,
    reason?: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Security: Verify ownership
    if (order.customerId !== customerId) {
      throw new ForbiddenException({
        code: 'ORDER_007',
        message: 'You do not have permission to cancel this order',
        statusCode: 403,
      });
    }

    // 3. Validate cancellation is allowed for current status
    if (!this.stateMachine.canCancelCustomer(order.status)) {
      throw new ConflictException({
        code: 'ORDER_012',
        message: `Cannot cancel order - order is ${order.status}`,
        statusCode: 409,
      });
    }

    // 4. Update order status
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CANCELLED,
      cancelledAt: Timestamp.now(),
      cancelReason: reason || 'Cancelled by customer',
      cancelledBy: 'CUSTOMER',
    });

    // 5. Trigger refund if already paid (stub for MVP)
    if (order.paymentStatus === PaymentStatus.PAID) {
      // TODO: Call paymentService.initiateRefund(orderId) when available
    }

    // 6. Notify shop owner (stub for MVP)
    // TODO: Call notificationService.notifyOwner(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-006: Owner confirms order
   */
  async confirmOrder(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify owner owns this shop
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'You do not have permission to confirm this order',
        statusCode: 403,
      });
    }

    // 3. Validate payment (COD can be confirmed immediately)
    if (
      order.paymentMethod !== 'COD' &&
      order.paymentStatus !== PaymentStatus.PAID
    ) {
      throw new ConflictException({
        code: 'ORDER_009',
        message: 'Cannot confirm order - payment not completed',
        statusCode: 409,
      });
    }

    // 4. Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.CONFIRMED,
    );

    // 5. Update order
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CONFIRMED,
      confirmedAt: Timestamp.now(),
    });

    // 6. Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-007: Owner marks order as preparing
   */
  async markPreparing(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'Permission denied',
        statusCode: 403,
      });
    }

    // Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.PREPARING,
    );

    // Update
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.PREPARING,
      preparingAt: Timestamp.now(),
    });

    // Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-008: Owner marks order as ready
   */
  async markReady(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'Permission denied',
        statusCode: 403,
      });
    }

    // Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.READY,
    );

    // Update
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.READY,
      readyAt: Timestamp.now(),
    });

    // Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    // Notify available shippers (broadcast) - stub for MVP
    // TODO: Call notificationService.notifyAvailableShippers(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-009: Owner cancels order
   * Can only cancel from CONFIRMED or PREPARING status
   */
  async ownerCancelOrder(
    ownerId: string,
    orderId: string,
    reason?: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'You do not have permission to cancel this order',
        statusCode: 403,
      });
    }

    // 3. Validate cancellation is allowed for current status
    // Owner can only cancel from CONFIRMED or PREPARING
    const cancelableStatuses = [OrderStatus.CONFIRMED, OrderStatus.PREPARING];
    if (!cancelableStatuses.includes(order.status)) {
      throw new ConflictException({
        code: 'ORDER_013',
        message: `Cannot cancel order - order is ${order.status}. Owner can only cancel CONFIRMED or PREPARING orders.`,
        statusCode: 409,
      });
    }

    // 4. Update order status
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CANCELLED,
      cancelledAt: Timestamp.now(),
      cancelReason: reason || 'Cancelled by owner',
      cancelledBy: 'OWNER',
    });

    // 5. Trigger refund if already paid (stub for MVP)
    if (order.paymentStatus === PaymentStatus.PAID) {
      // TODO: Call paymentService.initiateRefund(orderId) when available
    }

    // 6. Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-010: Get shop orders with page-based pagination
   */
  async getShopOrders(
    ownerId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    // 1. Get shop owned by this owner
    const shop = await this.shopsRepo.findByOwnerId(ownerId);
    if (!shop) {
      throw new NotFoundException({
        code: 'ORDER_010',
        message: 'Shop not found for this owner',
        statusCode: 404,
      });
    }

    const shopId = (shop as any).id || (shop as any)._id;

    // 2. Validate pagination params
    const { status, page = 1, limit = 20 } = filter;
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 20, 50);

    // Get total count using count() - efficient Firestore operation
    const total = await this.ordersRepo.count({
      shopId,
      ...(status && { status }),
    });
    const totalPages = Math.ceil(total / validLimit);

    // 3. Build query for data (no fetch-all, use offset properly)
    let query = this.ordersRepo
      .query()
      .where('shopId', '==', shopId)
      .orderBy('createdAt', 'desc');

    if (status) {
      query = query.where('status', '==', status);
    }

    // Use offset + limit correctly (no over-fetch, no slice)
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // 4. Execute query
    const orders = await this.ordersRepo.findMany(query);

    return {
      orders: orders.map(this.mapToListDto),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
      hasNext: validPage < totalPages,
      hasPrev: validPage > 1,
    };
  }

  /**
   * ORDER-013: Accept order for delivery (shipper)
   * PHASE 2
   */
  async acceptOrder(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Validate order is READY
    if (order.status !== OrderStatus.READY) {
      throw new ConflictException({
        code: 'ORDER_014',
        message: 'Order must be READY before accepting',
        statusCode: 409,
      });
    }

    // 3. Validate no shipper assigned yet (first-to-accept wins)
    if (order.shipperId) {
      throw new ConflictException({
        code: 'ORDER_013',
        message: 'Order not available for pickup',
        statusCode: 409,
      });
    }

    // 4. Check shipper availability - must be AVAILABLE
    const shipper = await this.shippersRepo.findById(shipperId);
    if (!shipper) {
      throw new NotFoundException({
        code: 'ORDER_017',
        message: 'Shipper not found',
        statusCode: 404,
      });
    }

    if (shipper.shipperInfo?.status !== ShipperStatus.AVAILABLE) {
      throw new ConflictException({
        code: 'ORDER_018',
        message: 'Shipper is not available',
        statusCode: 409,
      });
    }

    // 5. Validate state transition (READY -> SHIPPING)
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.SHIPPING,
    );

    // 6. Update order: assign shipper and move to SHIPPING
    await this.ordersRepo.update(orderId, {
      shipperId,
      status: OrderStatus.SHIPPING,
      shippingAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });

    // 7. Update shipper status to BUSY
    await this.shippersRepo.update(shipperId, {
      shipperInfo: {
        ...shipper.shipperInfo,
        status: ShipperStatus.BUSY,
      },
    });

    console.log(`✓ Order ${order.orderNumber} accepted by shipper: ${shipperId}`);

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-014: Mark order as shipping (shipper picked up)
   * PHASE 2 - Note: This is redundant with accept in current flow
   * Keeping for API completeness if we later split READY->ASSIGNED->SHIPPING
   */
  async markShipping(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify shipper is assigned to this order
    if (order.shipperId !== shipperId) {
      throw new ForbiddenException({
        code: 'ORDER_016',
        message: 'Shipper not assigned to this order',
        statusCode: 403,
      });
    }

    // 3. Validate state (should be SHIPPING already after accept)
    if (order.status !== OrderStatus.SHIPPING) {
      throw new ConflictException({
        code: 'ORDER_011',
        message: `Order must be SHIPPING, current status: ${order.status}`,
        statusCode: 409,
      });
    }

    // 4. Update shippingAt timestamp if not set
    if (!order.shippingAt) {
      await this.ordersRepo.update(orderId, {
        shippingAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
      });
    }

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-015: Mark order as delivered
   * PHASE 2
   */
  async markDelivered(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify shipper is assigned to this order
    if (order.shipperId !== shipperId) {
      throw new ForbiddenException({
        code: 'ORDER_016',
        message: 'Shipper not assigned to this order',
        statusCode: 403,
      });
    }

    // 3. Validate state transition (SHIPPING -> DELIVERED)
    if (order.status !== OrderStatus.SHIPPING) {
      throw new ConflictException({
        code: 'ORDER_015',
        message: 'Order must be SHIPPING before delivering',
        statusCode: 409,
      });
    }

    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.DELIVERED,
    );

    // 4. Update order status
    const updateData: any = {
      status: OrderStatus.DELIVERED,
      deliveredAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    };

    // 5. For COD orders, mark as PAID upon delivery
    if (order.paymentMethod === 'COD' && order.paymentStatus === PaymentStatus.UNPAID) {
      updateData.paymentStatus = PaymentStatus.PAID;
    }

    await this.ordersRepo.update(orderId, updateData);

    // 6. Update shipper status back to AVAILABLE
    const shipper = await this.shippersRepo.findById(shipperId);
    if (shipper?.shipperInfo) {
      await this.shippersRepo.update(shipperId, {
        shipperInfo: {
          ...shipper.shipperInfo,
          status: ShipperStatus.AVAILABLE,
        },
      });
    }

    console.log(`✓ Order ${order.orderNumber} delivered by shipper: ${shipperId}`);

    // TODO: Trigger payout to shop owner (Phase 2 enhancement)
    // await this.walletService.processOrderPayout(orderId);

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-016: Get shipper's orders with page-based pagination
   * PHASE 2
   */
  async getShipperOrders(
    shipperId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    const { status, page = 1, limit = 10 } = filter;

    // Validate pagination params
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 10, 50);

    // Get total count using count() - efficient Firestore operation
    const total = await this.ordersRepo.count({
      shipperId,
      ...(status && { status }),
    });
    const totalPages = Math.ceil(total / validLimit);

    // Build query for data (no fetch-all, use offset properly)
    let query = this.ordersRepo
      .query()
      .where('shipperId', '==', shipperId)
      .orderBy('createdAt', 'desc');

    if (status) {
      query = query.where('status', '==', status);
    }

    // Use offset + limit correctly (no over-fetch, no slice)
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // Execute query
    const orders = await this.ordersRepo.findMany(query);

    return {
      orders: orders.map(this.mapToListDto),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
      hasNext: validPage < totalPages,
      hasPrev: validPage > 1,
    };
  }

  private mapToListDto(order: OrderEntity): OrderListItemDto {
    return {
      id: order.id!,
      orderNumber: order.orderNumber,
      shopId: order.shopId,
      shopName: order.shopName,
      status: order.status,
      paymentStatus: order.paymentStatus,
      total: order.total,
      itemCount: order.items.length,
      createdAt: order.createdAt,
    };
  }

  private generateOrderNumber(): string {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substr(2, 6).toUpperCase();
    return `ORD-${timestamp}-${random}`;
  }
}

import { Injectable, ConflictException } from '@nestjs/common';
import { OrderStatus } from '../entities';

@Injectable()
export class OrderStateMachineService {
  private readonly transitions: Map<OrderStatus, OrderStatus[]> = new Map([
    [OrderStatus.PENDING, [OrderStatus.CONFIRMED, OrderStatus.CANCELLED]],
    [OrderStatus.CONFIRMED, [OrderStatus.PREPARING, OrderStatus.CANCELLED]],
    [OrderStatus.PREPARING, [OrderStatus.READY, OrderStatus.CANCELLED]],
    [OrderStatus.READY, [OrderStatus.SHIPPING]],
    [OrderStatus.SHIPPING, [OrderStatus.DELIVERED]],
    [OrderStatus.DELIVERED, []],
    [OrderStatus.CANCELLED, []],
  ]);

  /**
   * Validates if a state transition is allowed
   * @param currentStatus Current order status
   * @param newStatus Desired new status
   * @throws ConflictException if transition is invalid
   */
  async validateTransition(currentStatus: OrderStatus, newStatus: OrderStatus): Promise<void> {
    const allowedTransitions = this.transitions.get(currentStatus) || [];

    if (!allowedTransitions.includes(newStatus)) {
      throw new ConflictException({
        code: 'ORDER_011',
        message: `Invalid state transition: ${currentStatus} → ${newStatus}`,
        statusCode: 409,
      });
    }
  }

  /**
   * Checks if an order can be cancelled from current status
   * MVP: Customer can cancel PENDING, CONFIRMED, PREPARING only
   */
  canCancelCustomer(status: OrderStatus): boolean {
    return [OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING].includes(status);
  }

  /**
   * Checks if a status is terminal (no further transitions possible)
   */
  isTerminal(status: OrderStatus): boolean {
    return [OrderStatus.DELIVERED, OrderStatus.CANCELLED].includes(status);
  }

  /**
   * Gets all possible next statuses from current status
   */
  getValidTransitions(status: OrderStatus): OrderStatus[] {
    return this.transitions.get(status) || [];
  }
}

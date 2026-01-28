import { Test, TestingModule } from '@nestjs/testing';
import { ConflictException } from '@nestjs/common';
import { OrderStateMachineService } from './order-state-machine.service';
import { OrderStatus } from '../entities';

describe('OrderStateMachineService', () => {
  let service: OrderStateMachineService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [OrderStateMachineService],
    }).compile();

    service = module.get<OrderStateMachineService>(OrderStateMachineService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('validateTransition', () => {
    it('should allow PENDING → CONFIRMED', async () => {
      await expect(
        service.validateTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED),
      ).resolves.not.toThrow();
    });

    it('should allow PENDING → CANCELLED', async () => {
      await expect(
        service.validateTransition(OrderStatus.PENDING, OrderStatus.CANCELLED),
      ).resolves.not.toThrow();
    });

    it('should allow CONFIRMED → PREPARING', async () => {
      await expect(
        service.validateTransition(OrderStatus.CONFIRMED, OrderStatus.PREPARING),
      ).resolves.not.toThrow();
    });

    it('should allow CONFIRMED → CANCELLED', async () => {
      await expect(
        service.validateTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
      ).resolves.not.toThrow();
    });

    it('should allow PREPARING → READY', async () => {
      await expect(
        service.validateTransition(OrderStatus.PREPARING, OrderStatus.READY),
      ).resolves.not.toThrow();
    });

    it('should allow PREPARING → CANCELLED', async () => {
      await expect(
        service.validateTransition(OrderStatus.PREPARING, OrderStatus.CANCELLED),
      ).resolves.not.toThrow();
    });

    it('should allow READY → SHIPPING', async () => {
      await expect(
        service.validateTransition(OrderStatus.READY, OrderStatus.SHIPPING),
      ).resolves.not.toThrow();
    });

    it('should allow SHIPPING → DELIVERED', async () => {
      await expect(
        service.validateTransition(OrderStatus.SHIPPING, OrderStatus.DELIVERED),
      ).resolves.not.toThrow();
    });

    it('should reject PENDING → PREPARING', async () => {
      await expect(
        service.validateTransition(OrderStatus.PENDING, OrderStatus.PREPARING),
      ).rejects.toThrow(ConflictException);
    });

    it('should reject READY → CANCELLED', async () => {
      await expect(
        service.validateTransition(OrderStatus.READY, OrderStatus.CANCELLED),
      ).rejects.toThrow(ConflictException);
    });

    it('should reject SHIPPING → CANCELLED', async () => {
      await expect(
        service.validateTransition(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
      ).rejects.toThrow(ConflictException);
    });

    it('should reject DELIVERED → any transition', async () => {
      await expect(
        service.validateTransition(OrderStatus.DELIVERED, OrderStatus.PENDING),
      ).rejects.toThrow(ConflictException);
    });

    it('should reject CANCELLED → any transition', async () => {
      await expect(
        service.validateTransition(OrderStatus.CANCELLED, OrderStatus.PENDING),
      ).rejects.toThrow(ConflictException);
    });
  });

  describe('canCancelCustomer', () => {
    it('should allow cancel for PENDING status', () => {
      expect(service.canCancelCustomer(OrderStatus.PENDING)).toBe(true);
    });

    it('should allow cancel for CONFIRMED status', () => {
      expect(service.canCancelCustomer(OrderStatus.CONFIRMED)).toBe(true);
    });

    it('should allow cancel for PREPARING status', () => {
      expect(service.canCancelCustomer(OrderStatus.PREPARING)).toBe(true);
    });

    it('should not allow cancel for READY status', () => {
      expect(service.canCancelCustomer(OrderStatus.READY)).toBe(false);
    });

    it('should not allow cancel for SHIPPING status', () => {
      expect(service.canCancelCustomer(OrderStatus.SHIPPING)).toBe(false);
    });

    it('should not allow cancel for DELIVERED status', () => {
      expect(service.canCancelCustomer(OrderStatus.DELIVERED)).toBe(false);
    });

    it('should not allow cancel for CANCELLED status', () => {
      expect(service.canCancelCustomer(OrderStatus.CANCELLED)).toBe(false);
    });
  });

  describe('isTerminal', () => {
    it('should return true for DELIVERED', () => {
      expect(service.isTerminal(OrderStatus.DELIVERED)).toBe(true);
    });

    it('should return true for CANCELLED', () => {
      expect(service.isTerminal(OrderStatus.CANCELLED)).toBe(true);
    });

    it('should return false for PENDING', () => {
      expect(service.isTerminal(OrderStatus.PENDING)).toBe(false);
    });

    it('should return false for CONFIRMED', () => {
      expect(service.isTerminal(OrderStatus.CONFIRMED)).toBe(false);
    });

    it('should return false for PREPARING', () => {
      expect(service.isTerminal(OrderStatus.PREPARING)).toBe(false);
    });

    it('should return false for READY', () => {
      expect(service.isTerminal(OrderStatus.READY)).toBe(false);
    });

    it('should return false for SHIPPING', () => {
      expect(service.isTerminal(OrderStatus.SHIPPING)).toBe(false);
    });
  });

  describe('getValidTransitions', () => {
    it('should return correct transitions for PENDING', () => {
      const transitions = service.getValidTransitions(OrderStatus.PENDING);
      expect(transitions).toEqual([OrderStatus.CONFIRMED, OrderStatus.CANCELLED]);
    });

    it('should return correct transitions for CONFIRMED', () => {
      const transitions = service.getValidTransitions(OrderStatus.CONFIRMED);
      expect(transitions).toEqual([OrderStatus.PREPARING, OrderStatus.CANCELLED]);
    });

    it('should return correct transitions for READY', () => {
      const transitions = service.getValidTransitions(OrderStatus.READY);
      expect(transitions).toEqual([OrderStatus.SHIPPING]);
    });

    it('should return empty array for terminal status', () => {
      const transitions = service.getValidTransitions(OrderStatus.DELIVERED);
      expect(transitions).toEqual([]);
    });
  });
});

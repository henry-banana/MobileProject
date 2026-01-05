import { Controller, Get } from "@nestjs/common";
import { ClientOrderService } from "./client-order.service";

@Controller("client/orders")
export class ClientOrderController {
  constructor(private readonly orderService: ClientOrderService) {}

  @Get()
  getOrders() {
    return {
      message: this.orderService.getMessage(),
    };
  }
}

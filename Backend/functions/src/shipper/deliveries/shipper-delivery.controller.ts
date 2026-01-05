import { Controller, Get } from "@nestjs/common";
import { ShipperDeliveryService } from "./shipper-delivery.service";

@Controller("shipper/deliveries")
export class ShipperDeliveryController {
  constructor(private readonly deliveryService: ShipperDeliveryService) {}

  @Get()
  getDeliveries() {
    return {
      message: this.deliveryService.getMessage(),
    };
  }
}

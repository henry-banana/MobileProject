import { Module } from "@nestjs/common";
import { ShipperDeliveryController } from "./shipper-delivery.controller";
import { ShipperDeliveryService } from "./shipper-delivery.service";

@Module({
  controllers: [ShipperDeliveryController],
  providers: [ShipperDeliveryService],
})
export class ShipperDeliveryModule {}

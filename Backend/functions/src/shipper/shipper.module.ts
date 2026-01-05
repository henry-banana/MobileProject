import { Module } from "@nestjs/common";
import { ShipperProfileModule } from "./profile/shipper-profile.module";
import { ShipperDeliveryModule } from "./deliveries/shipper-delivery.module";

@Module({
  imports: [ShipperProfileModule, ShipperDeliveryModule],
})
export class ShipperModule {}

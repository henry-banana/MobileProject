import { Module } from "@nestjs/common";
import { ShipperProfileController } from "./shipper-profile.controller";
import { ShipperProfileService } from "./shipper-profile.service";

@Module({
  controllers: [ShipperProfileController],
  providers: [ShipperProfileService],
})
export class ShipperProfileModule {}

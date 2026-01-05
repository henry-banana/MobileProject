import { Module } from "@nestjs/common";
import { ClientOrderController } from "./client-order.controller";
import { ClientOrderService } from "./client-order.service";

@Module({
  controllers: [ClientOrderController],
  providers: [ClientOrderService],
})
export class ClientOrderModule {}

import { Module } from "@nestjs/common";
import { ClientHomeModule } from "./home/client-home.module";
import { ClientOrderModule } from "./orders/client-order.module";

@Module({
  imports: [ClientHomeModule, ClientOrderModule],
})
export class ClientModule {}

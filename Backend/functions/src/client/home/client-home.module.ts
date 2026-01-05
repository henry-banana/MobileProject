import { Module } from "@nestjs/common";
import { ClientHomeController } from "./client-home.controller";
import { ClientHomeService } from "./client-home.service";

@Module({
  controllers: [ClientHomeController],
  providers: [ClientHomeService],
})
export class ClientHomeModule {}

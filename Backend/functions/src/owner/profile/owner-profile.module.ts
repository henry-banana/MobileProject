import { Module } from "@nestjs/common";
import { OwnerProfileController } from "./owner-profile.controller";
import { OwnerProfileService } from "./owner-profile.service";

@Module({
  controllers: [OwnerProfileController],
  providers: [OwnerProfileService],
})
export class OwnerProfileModule {}

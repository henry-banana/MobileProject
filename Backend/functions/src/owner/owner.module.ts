import { Module } from "@nestjs/common";
import { OwnerDashboardModule } from "./dashboard/owner-dashboard.module";
import { OwnerProfileModule } from "./profile/owner-profile.module";

@Module({
  imports: [OwnerDashboardModule, OwnerProfileModule],
})
export class OwnerModule {}

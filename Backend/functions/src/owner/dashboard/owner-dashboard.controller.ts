import { Controller, Get } from "@nestjs/common";
import { OwnerDashboardService } from "./owner-dashboard.service";

@Controller("owner/dashboard")
export class OwnerDashboardController {
  constructor(private readonly dashboardService: OwnerDashboardService) {}

  @Get()
  getDashboard() {
    return {
      message: this.dashboardService.getMessage(),
    };
  }
}

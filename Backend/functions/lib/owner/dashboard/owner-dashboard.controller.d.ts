import { OwnerDashboardService } from "./owner-dashboard.service";
export declare class OwnerDashboardController {
    private readonly dashboardService;
    constructor(dashboardService: OwnerDashboardService);
    getDashboard(): {
        message: string;
    };
}

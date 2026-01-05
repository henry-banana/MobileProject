import { Injectable } from "@nestjs/common";

@Injectable()
export class OwnerDashboardService {
  getMessage(): string {
    return "Hello Owner Dashboard";
  }
}

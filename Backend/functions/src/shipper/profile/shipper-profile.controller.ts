import { Controller, Get } from "@nestjs/common";
import { ShipperProfileService } from "./shipper-profile.service";

@Controller("shipper/profile")
export class ShipperProfileController {
  constructor(private readonly profileService: ShipperProfileService) {}

  @Get()
  getProfile() {
    return {
      message: this.profileService.getMessage(),
    };
  }
}

import { Controller, Get } from "@nestjs/common";
import { OwnerProfileService } from "./owner-profile.service";

@Controller("owner/profile")
export class OwnerProfileController {
  constructor(private readonly profileService: OwnerProfileService) {}

  @Get()
  getProfile() {
    return {
      message: this.profileService.getMessage(),
    };
  }
}

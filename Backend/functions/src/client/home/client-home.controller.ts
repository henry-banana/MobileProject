import { Controller, Get } from "@nestjs/common";
import { ClientHomeService } from "./client-home.service";

@Controller("client/home")
export class ClientHomeController {
  constructor(private readonly homeService: ClientHomeService) {}

  @Get()
  getHome() {
    return {
      message: this.homeService.getMessage(),
    };
  }
}

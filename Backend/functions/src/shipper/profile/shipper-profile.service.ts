import { Injectable } from "@nestjs/common";

@Injectable()
export class ShipperProfileService {
  getMessage(): string {
    return "Hello Shipper Profile";
  }
}

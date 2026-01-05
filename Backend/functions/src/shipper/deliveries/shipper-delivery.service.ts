import { Injectable } from "@nestjs/common";

@Injectable()
export class ShipperDeliveryService {
  getMessage(): string {
    return "Hello Shipper Deliveries";
  }
}

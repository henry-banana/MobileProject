import { Injectable } from "@nestjs/common";

@Injectable()
export class ClientOrderService {
  getMessage(): string {
    return "Hello Client Orders";
  }
}

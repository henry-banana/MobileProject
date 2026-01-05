import { Injectable } from "@nestjs/common";

@Injectable()
export class ClientHomeService {
  getMessage(): string {
    return "Hello Client Home";
  }
}

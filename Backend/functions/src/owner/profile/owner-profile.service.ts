import { Injectable } from "@nestjs/common";

@Injectable()
export class OwnerProfileService {
  getMessage(): string {
    return "Hello Owner Profile";
  }
}

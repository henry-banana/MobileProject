import { Module, Controller, Get } from "@nestjs/common";
import { FirebaseModule } from "./firebase/firebase.module";
import { OwnerModule } from "./owner/owner.module";
import { ShipperModule } from "./shipper/shipper.module";
import { ClientModule } from "./client/client.module";

@Controller()
export class AppController {
  @Get()
  getHello() {
    return {
      success: true,
      message: "NestJS on Firebase Cloud Functions is working! 🚀",
      timestamp: new Date().toISOString(),
    };
  }

  @Get("health")
  healthCheck() {
    return {
      success: true,
      status: "healthy",
      service: "NestJS Firebase Backend",
    };
  }
}

@Module({
  imports: [FirebaseModule, OwnerModule, ShipperModule, ClientModule],
  controllers: [AppController],
})
export class AppModule {}

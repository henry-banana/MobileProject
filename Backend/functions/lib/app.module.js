"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AppModule = exports.AppController = void 0;
const common_1 = require("@nestjs/common");
const firebase_module_1 = require("./firebase/firebase.module");
const owner_module_1 = require("./owner/owner.module");
const shipper_module_1 = require("./shipper/shipper.module");
const client_module_1 = require("./client/client.module");
let AppController = class AppController {
    getHello() {
        return {
            success: true,
            message: "NestJS on Firebase Cloud Functions is working! 🚀",
            timestamp: new Date().toISOString(),
        };
    }
    healthCheck() {
        return {
            success: true,
            status: "healthy",
            service: "NestJS Firebase Backend",
        };
    }
};
exports.AppController = AppController;
__decorate([
    (0, common_1.Get)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AppController.prototype, "getHello", null);
__decorate([
    (0, common_1.Get)("health"),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], AppController.prototype, "healthCheck", null);
exports.AppController = AppController = __decorate([
    (0, common_1.Controller)()
], AppController);
let AppModule = class AppModule {
};
exports.AppModule = AppModule;
exports.AppModule = AppModule = __decorate([
    (0, common_1.Module)({
        imports: [firebase_module_1.FirebaseModule, owner_module_1.OwnerModule, shipper_module_1.ShipperModule, client_module_1.ClientModule],
        controllers: [AppController],
    })
], AppModule);
//# sourceMappingURL=app.module.js.map
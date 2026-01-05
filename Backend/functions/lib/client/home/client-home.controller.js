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
exports.ClientHomeController = void 0;
const common_1 = require("@nestjs/common");
const client_home_service_1 = require("./client-home.service");
let ClientHomeController = class ClientHomeController {
    constructor(homeService) {
        this.homeService = homeService;
    }
    getHome() {
        return {
            message: this.homeService.getMessage(),
        };
    }
};
exports.ClientHomeController = ClientHomeController;
__decorate([
    (0, common_1.Get)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], ClientHomeController.prototype, "getHome", null);
exports.ClientHomeController = ClientHomeController = __decorate([
    (0, common_1.Controller)("client/home"),
    __metadata("design:paramtypes", [client_home_service_1.ClientHomeService])
], ClientHomeController);
//# sourceMappingURL=client-home.controller.js.map
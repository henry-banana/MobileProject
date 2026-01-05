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
exports.ClientOrderController = void 0;
const common_1 = require("@nestjs/common");
const client_order_service_1 = require("./client-order.service");
let ClientOrderController = class ClientOrderController {
    constructor(orderService) {
        this.orderService = orderService;
    }
    getOrders() {
        return {
            message: this.orderService.getMessage(),
        };
    }
};
exports.ClientOrderController = ClientOrderController;
__decorate([
    (0, common_1.Get)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], ClientOrderController.prototype, "getOrders", null);
exports.ClientOrderController = ClientOrderController = __decorate([
    (0, common_1.Controller)("client/orders"),
    __metadata("design:paramtypes", [client_order_service_1.ClientOrderService])
], ClientOrderController);
//# sourceMappingURL=client-order.controller.js.map
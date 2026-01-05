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
exports.ShipperDeliveryController = void 0;
const common_1 = require("@nestjs/common");
const shipper_delivery_service_1 = require("./shipper-delivery.service");
let ShipperDeliveryController = class ShipperDeliveryController {
    constructor(deliveryService) {
        this.deliveryService = deliveryService;
    }
    getDeliveries() {
        return {
            message: this.deliveryService.getMessage(),
        };
    }
};
exports.ShipperDeliveryController = ShipperDeliveryController;
__decorate([
    (0, common_1.Get)(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", []),
    __metadata("design:returntype", void 0)
], ShipperDeliveryController.prototype, "getDeliveries", null);
exports.ShipperDeliveryController = ShipperDeliveryController = __decorate([
    (0, common_1.Controller)("shipper/deliveries"),
    __metadata("design:paramtypes", [shipper_delivery_service_1.ShipperDeliveryService])
], ShipperDeliveryController);
//# sourceMappingURL=shipper-delivery.controller.js.map
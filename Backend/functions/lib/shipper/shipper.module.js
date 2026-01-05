"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ShipperModule = void 0;
const common_1 = require("@nestjs/common");
const shipper_profile_module_1 = require("./profile/shipper-profile.module");
const shipper_delivery_module_1 = require("./deliveries/shipper-delivery.module");
let ShipperModule = class ShipperModule {
};
exports.ShipperModule = ShipperModule;
exports.ShipperModule = ShipperModule = __decorate([
    (0, common_1.Module)({
        imports: [shipper_profile_module_1.ShipperProfileModule, shipper_delivery_module_1.ShipperDeliveryModule],
    })
], ShipperModule);
//# sourceMappingURL=shipper.module.js.map
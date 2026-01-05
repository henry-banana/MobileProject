import { ShipperDeliveryService } from "./shipper-delivery.service";
export declare class ShipperDeliveryController {
    private readonly deliveryService;
    constructor(deliveryService: ShipperDeliveryService);
    getDeliveries(): {
        message: string;
    };
}

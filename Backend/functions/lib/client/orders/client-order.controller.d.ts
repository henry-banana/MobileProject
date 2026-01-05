import { ClientOrderService } from "./client-order.service";
export declare class ClientOrderController {
    private readonly orderService;
    constructor(orderService: ClientOrderService);
    getOrders(): {
        message: string;
    };
}

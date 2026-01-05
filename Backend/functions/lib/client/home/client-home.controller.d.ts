import { ClientHomeService } from "./client-home.service";
export declare class ClientHomeController {
    private readonly homeService;
    constructor(homeService: ClientHomeService);
    getHome(): {
        message: string;
    };
}

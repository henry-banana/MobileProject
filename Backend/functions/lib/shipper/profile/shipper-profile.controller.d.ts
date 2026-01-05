import { ShipperProfileService } from "./shipper-profile.service";
export declare class ShipperProfileController {
    private readonly profileService;
    constructor(profileService: ShipperProfileService);
    getProfile(): {
        message: string;
    };
}

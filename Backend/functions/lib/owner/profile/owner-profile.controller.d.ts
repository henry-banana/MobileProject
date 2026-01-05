import { OwnerProfileService } from "./owner-profile.service";
export declare class OwnerProfileController {
    private readonly profileService;
    constructor(profileService: OwnerProfileService);
    getProfile(): {
        message: string;
    };
}

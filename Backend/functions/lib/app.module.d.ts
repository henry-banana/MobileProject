export declare class AppController {
    getHello(): {
        success: boolean;
        message: string;
        timestamp: string;
    };
    healthCheck(): {
        success: boolean;
        status: string;
        service: string;
    };
}
export declare class AppModule {
}

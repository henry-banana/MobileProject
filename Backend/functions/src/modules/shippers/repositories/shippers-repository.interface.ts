import {
  ShipperApplicationEntity,
  ApplicationStatus,
} from '../entities/shipper-application.entity';

export interface IShippersRepository {
  // Application management
  createApplication(data: Partial<ShipperApplicationEntity>): Promise<ShipperApplicationEntity>;

  findApplicationById(id: string): Promise<ShipperApplicationEntity | null>;

  findPendingApplication(userId: string, shopId: string): Promise<ShipperApplicationEntity | null>;

  findUserApplications(userId: string): Promise<ShipperApplicationEntity[]>;

  findShopApplications(
    shopId: string,
    status?: ApplicationStatus,
  ): Promise<ShipperApplicationEntity[]>;

  updateApplicationStatus(
    id: string,
    status: ApplicationStatus,
    reviewedBy: string,
    rejectReason?: string,
  ): Promise<void>;

  deleteApplication(id: string): Promise<void>;

  // Shipper management
  findShippersByShop(shopId: string): Promise<Record<string, unknown>[]>;
}

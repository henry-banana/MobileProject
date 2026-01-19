import {
  ShipperApplicationEntity,
  ApplicationStatus,
} from '../entities/shipper-application.entity';
import { ShipperEntity } from '../entities/shipper.entity';

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

  // Shipper entity management (for Order module to manage shipper availability)
  findById(id: string): Promise<ShipperEntity | null>;
  update(id: string, updates: Partial<ShipperEntity>): Promise<void>;

  // Shop shipper listing
  findShippersByShop(shopId: string): Promise<Record<string, unknown>[]>;
}

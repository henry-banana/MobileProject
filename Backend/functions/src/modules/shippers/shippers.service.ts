import {
  Injectable,
  ConflictException,
  NotFoundException,
  ForbiddenException,
  Inject,
} from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { IShippersRepository } from './repositories/shippers-repository.interface';
import { UsersService } from '../users/users.service';
import { ShopsService } from '../shops/services/shops.service';
import { ApplyShipperDto } from './dto/apply-shipper.dto';
import { RejectApplicationDto } from './dto/reject-application.dto';
import { ShipperApplicationEntity, ApplicationStatus } from './entities/shipper-application.entity';
import { ShipperEntity, ShipperStatus } from './entities/shipper.entity';

@Injectable()
export class ShippersService {
  constructor(
    @Inject('IShippersRepository')
    private readonly shippersRepository: IShippersRepository,
    private readonly usersService: UsersService,
    private readonly shopsService: ShopsService,
    private readonly firestore: Firestore,
  ) {}

  // SHIP-002: Apply to be Shipper
  async applyShipper(userId: string, dto: ApplyShipperDto): Promise<ShipperApplicationEntity> {
    const user = await this.usersService.findById(userId);

    // Check if already shipper
    if (user.roles?.includes('SHIPPER')) {
      throw new ConflictException('SHIPPER_001: Bạn đã là shipper rồi');
    }

    // Check if already applied (PENDING)
    const existingApp = await this.shippersRepository.findPendingApplication(userId, dto.shopId);
    if (existingApp) {
      throw new ConflictException('SHIPPER_005: Bạn đã nộp đơn cho shop này rồi');
    }

    // Validate shop exists
    const shop = await this.shopsService.getShopById(dto.shopId);

    // Create application
    const application = await this.shippersRepository.createApplication({
      userId,
      userName: user.name,
      userPhone: user.phone,
      userAvatar: user.avatar || '',
      shopId: dto.shopId,
      shopName: shop.name,
      vehicleType: dto.vehicleType,
      vehicleNumber: dto.vehicleNumber,
      idCardNumber: dto.idCardNumber,
      idCardFrontUrl: dto.idCardFrontUrl,
      idCardBackUrl: dto.idCardBackUrl,
      driverLicenseUrl: dto.driverLicenseUrl,
      message: dto.message,
      status: ApplicationStatus.PENDING,
    });

    // TODO: Notify owner
    // await this.notificationService.sendToOwner(dto.shopId, { ... });

    return application;
  }

  // SHIP-003: Get My Applications
  async getMyApplications(userId: string): Promise<ShipperApplicationEntity[]> {
    return this.shippersRepository.findUserApplications(userId);
  }

  // SHIP-004: Cancel Application
  async cancelApplication(userId: string, applicationId: string): Promise<void> {
    const application = await this.shippersRepository.findApplicationById(applicationId);

    if (!application) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Check ownership
    if (application.userId !== userId) {
      throw new ForbiddenException('Bạn không có quyền hủy đơn này');
    }

    // Only PENDING can be cancelled
    if (application.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Chỉ có thể hủy đơn đang chờ duyệt');
    }

    await this.shippersRepository.deleteApplication(applicationId);
  }

  // SHIP-005: List Applications (Owner)
  async listApplications(
    ownerId: string,
    status?: ApplicationStatus,
  ): Promise<ShipperApplicationEntity[]> {
    const owner = await this.usersService.getProfile(ownerId);

    if (!owner.shopId) {
      throw new ForbiddenException('Bạn chưa có shop');
    }

    return this.shippersRepository.findShopApplications(owner.shopId, status);
  }

  // SHIP-006: Approve Application ⭐
  async approveApplication(ownerId: string, applicationId: string): Promise<void> {
    const owner = await this.usersService.findById(ownerId);

    if (!owner.shopId) {
      throw new ForbiddenException('Bạn chưa có shop');
    }

    const app = await this.shippersRepository.findApplicationById(applicationId);

    if (!app) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Validate ownership
    if (app.shopId !== owner.shopId) {
      throw new ForbiddenException('Bạn không có quyền duyệt đơn này');
    }

    // Check status
    if (app.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Đơn đã được xử lý rồi');
    }

    // Transaction: Update application + Update user
    await this.firestore.runTransaction(async (transaction) => {
      const appRef = this.firestore.collection('shipperApplications').doc(applicationId);
      const userRef = this.firestore.collection('users').doc(app.userId);

      // Get current user roles
      const userDoc = await transaction.get(userRef);
      const userData = userDoc.data();
      const currentRoles = userData?.roles || [];

      // Update application
      transaction.update(appRef, {
        status: ApplicationStatus.APPROVED,
        reviewedBy: ownerId,
        reviewedAt: FieldValue.serverTimestamp(),
      });

      // Update user: add SHIPPER role + shipperInfo
      transaction.update(userRef, {
        roles: [...currentRoles, 'SHIPPER'],
        shipperInfo: {
          shopId: app.shopId,
          shopName: app.shopName,
          vehicleType: app.vehicleType,
          vehicleNumber: app.vehicleNumber,
          status: ShipperStatus.AVAILABLE,
          rating: 0,
          totalDeliveries: 0,
          currentOrders: [],
          joinedAt: FieldValue.serverTimestamp(),
        },
        updatedAt: FieldValue.serverTimestamp(),
      });
    });

    // TODO: Notify user
    // await this.notificationService.sendToUser(app.userId, { ... });
  }

  // SHIP-007: Reject Application
  async rejectApplication(
    ownerId: string,
    applicationId: string,
    dto: RejectApplicationDto,
  ): Promise<void> {
    const owner = await this.usersService.getProfile(ownerId);

    if (!owner.shopId) {
      throw new ForbiddenException('Bạn chưa có shop');
    }

    const app = await this.shippersRepository.findApplicationById(applicationId);

    if (!app) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Validate ownership
    if (app.shopId !== owner.shopId) {
      throw new ForbiddenException('Bạn không có quyền từ chối đơn này');
    }

    // Check status
    if (app.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Đơn đã được xử lý rồi');
    }

    await this.shippersRepository.updateApplicationStatus(
      applicationId,
      ApplicationStatus.REJECTED,
      ownerId,
      dto.reason,
    );

    // TODO: Notify user
    // await this.notificationService.sendToUser(app.userId, { ... });
  }

  // SHIP-008: List Shop Shippers
  async listShopShippers(ownerId: string): Promise<ShipperEntity[]> {
    const owner = await this.usersService.getProfile(ownerId);

    if (!owner.shopId) {
      throw new ForbiddenException('Bạn chưa có shop');
    }

    const shippers = await this.shippersRepository.findShippersByShop(owner.shopId);

    return shippers.map(
      (shipper) =>
        new ShipperEntity({
          id: shipper.id,
          name: shipper.name,
          phone: shipper.phone,
          avatar: shipper.avatar,
          shipperInfo: shipper.shipperInfo,
        }),
    );
  }

  // SHIP-009: Remove Shipper
  async removeShipper(ownerId: string, shipperId: string): Promise<void> {
    const owner = await this.usersService.getProfile(ownerId);

    if (!owner.shopId) {
      throw new ForbiddenException('Bạn chưa có shop');
    }

    const shipper = await this.usersService.getProfile(shipperId);

    // Validate ownership
    if (shipper.shipperInfo?.shopId !== owner.shopId) {
      throw new ForbiddenException('Shipper này không thuộc shop của bạn');
    }

    // Check if shipper has active orders
    if (shipper.shipperInfo?.currentOrders && shipper.shipperInfo.currentOrders.length > 0) {
      throw new ConflictException('SHIPPER_003: Shipper đang có đơn chưa hoàn thành');
    }

    // Remove SHIPPER role and clear shipperInfo
    const userRef = this.firestore.collection('users').doc(shipperId);
    await userRef.update({
      roles: FieldValue.arrayRemove('SHIPPER'),
      shipperInfo: FieldValue.delete(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
}

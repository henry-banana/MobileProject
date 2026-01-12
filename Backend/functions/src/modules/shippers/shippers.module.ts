import { Module } from '@nestjs/common';
import { ShippersService } from './shippers.service';
import { ShipperApplicationsController } from './shipper-applications.controller';
import { OwnerShippersController } from './owner-shippers.controller';
import { FirestoreShippersRepository } from './repositories/firestore-shippers.repository';
import { UsersModule } from '../users/users.module';
import { ShopsModule } from '../shops/shops.module';

@Module({
  imports: [UsersModule, ShopsModule],
  controllers: [ShipperApplicationsController, OwnerShippersController],
  providers: [
    ShippersService,
    {
      provide: 'IShippersRepository',
      useClass: FirestoreShippersRepository,
    },
  ],
  exports: [ShippersService],
})
export class ShippersModule {}

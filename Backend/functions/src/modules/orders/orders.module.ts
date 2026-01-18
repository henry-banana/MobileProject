import { Module, forwardRef } from '@nestjs/common';
import { OrderStateMachineService, OrdersService } from './services';
import { FirestoreOrdersRepository } from './repositories';
import { OrdersController, OrdersOwnerController, OrdersShipperController } from './controllers';
import { ProductsModule } from '../products/products.module';
import { ShopsModule } from '../shops/shops.module';
import { CartModule } from '../cart/cart.module';
import { ShippersModule } from '../shippers/shippers.module';
import { ORDERS_REPOSITORY } from './interfaces';

@Module({
  imports: [
    ProductsModule,
    ShopsModule,
    CartModule,
    forwardRef(() => ShippersModule),
  ],
  controllers: [OrdersController, OrdersOwnerController, OrdersShipperController],
  providers: [
    OrderStateMachineService,
    OrdersService,
    {
      provide: ORDERS_REPOSITORY,
      useClass: FirestoreOrdersRepository,
    },
  ],
  exports: [OrderStateMachineService, OrdersService],
})
export class OrdersModule {}

import { Module } from '@nestjs/common';
import { ProductsService } from './services';
import { FirestoreProductsRepository } from './repositories';
import { OwnerProductsController, ProductsController } from './controllers';
import { ShopsModule } from '../shops/shops.module';

@Module({
  imports: [ShopsModule],
  controllers: [OwnerProductsController, ProductsController],
  providers: [
    ProductsService,
    {
      provide: 'PRODUCTS_REPOSITORY',
      useClass: FirestoreProductsRepository,
    },
  ],
  exports: [ProductsService],
})
export class ProductsModule {}

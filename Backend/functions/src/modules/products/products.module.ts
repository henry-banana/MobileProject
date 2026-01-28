import { Module } from '@nestjs/common';
import { ProductsService, ProductSearchService } from './services';
import { FirestoreProductsRepository } from './repositories';
import { OwnerProductsController, ProductsController, SearchController } from './controllers';
import { ShopsModule } from '../shops/shops.module';
import { SharedModule } from '../../shared/shared.module';
import { CategoriesModule } from '../categories/categories.module';

@Module({
  imports: [ShopsModule, SharedModule, CategoriesModule],
  controllers: [OwnerProductsController, ProductsController, SearchController],
  providers: [
    ProductsService,
    ProductSearchService,
    {
      provide: 'PRODUCTS_REPOSITORY',
      useClass: FirestoreProductsRepository,
    },
  ],
  exports: [ProductsService, ProductSearchService, 'PRODUCTS_REPOSITORY'],
})
export class ProductsModule {}

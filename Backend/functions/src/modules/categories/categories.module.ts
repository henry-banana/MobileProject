import { Module } from '@nestjs/common';
import { CategoriesController } from './categories.controller';
import { CategoriesService } from './categories.service';
import { FirestoreCategoriesRepository } from './repositories/firestore-categories.repository';
import { CATEGORIES_REPOSITORY_TOKEN } from './interfaces';

/**
 * Categories Module
 *
 * Module quản lý categories (danh mục sản phẩm)
 *
 * Dependency Injection:
 * - CATEGORIES_REPOSITORY_TOKEN -> FirestoreCategoriesRepository
 * - Nếu muốn đổi DB, chỉ cần đổi useClass
 *
 * Exports:
 * - CategoriesService: để AdminModule có thể sử dụng
 *
 * Features:
 * - Public: GET categories (active only)
 * - Admin: CRUD categories (via AdminModule)
 */
@Module({
  controllers: [CategoriesController],
  providers: [
    // Register repository với interface token
    // Đổi DB: thay FirestoreCategoriesRepository -> MongoCategoriesRepository
    {
      provide: CATEGORIES_REPOSITORY_TOKEN,
      useClass: FirestoreCategoriesRepository,
    },
    CategoriesService,
  ],
  exports: [CategoriesService], // Export để AdminModule sử dụng
})
export class CategoriesModule {}

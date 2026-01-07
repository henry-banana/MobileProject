import { IBaseRepository } from '../../../core/database';
import { CategoryEntity } from '../entities/category.entity';

/**
 * ICategoriesRepository - Interface cho Categories Repository
 *
 * Extend IBaseRepository và thêm các methods đặc thù cho Categories.
 * Service inject interface này, KHÔNG phải concrete implementation.
 *
 * @example
 * ```typescript
 * // Trong CategoriesService
 * constructor(
 *   @Inject(CATEGORIES_REPOSITORY_TOKEN)
 *   private readonly categoriesRepository: ICategoriesRepository,
 * ) {}
 * ```
 */
export interface ICategoriesRepository extends IBaseRepository<CategoryEntity> {
  /**
   * Tìm category theo slug
   */
  findBySlug(slug: string): Promise<CategoryEntity | null>;

  /**
   * Lấy danh sách categories đang active
   */
  findActive(): Promise<CategoryEntity[]>;

  /**
   * Kiểm tra slug đã tồn tại
   * @param slug - Slug cần kiểm tra
   * @param excludeId - ID category cần loại trừ (khi update)
   */
  isSlugExist(slug: string, excludeId?: string): Promise<boolean>;

  /**
   * Lấy displayOrder lớn nhất
   */
  getMaxDisplayOrder(): Promise<number>;

  /**
   * Increment product count
   */
  incrementProductCount(id: string): Promise<void>;

  /**
   * Decrement product count
   */
  decrementProductCount(id: string): Promise<void>;
}

/**
 * Token để inject repository
 * Sử dụng trong module providers và @Inject decorator
 */
export const CATEGORIES_REPOSITORY_TOKEN = 'ICategoriesRepository';

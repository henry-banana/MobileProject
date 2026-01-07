import {
  Injectable,
  Inject,
  ConflictException,
  Logger,
} from '@nestjs/common';
import {
  ICategoriesRepository,
  CATEGORIES_REPOSITORY_TOKEN,
} from './interfaces';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import {
  CategoryEntity,
  CategoryStatus,
} from './entities/category.entity';
import { generateSlug } from '../../shared/utils/string.utils';

/**
 * CategoriesService - Business logic cho categories
 *
 * Responsibilities:
 * - Validation business rules
 * - Generate slug từ name
 * - Đảm bảo slug unique
 * - Orchestrate repository operations
 *
 * Tuân theo:
 * - Single Responsibility Principle
 * - Dependency Inversion (inject repository via interface)
 *
 * NOTE: Inject qua interface token, KHÔNG inject trực tiếp repository class
 */
@Injectable()
export class CategoriesService {
  private readonly logger = new Logger(CategoriesService.name);

  constructor(
    @Inject(CATEGORIES_REPOSITORY_TOKEN)
    private readonly categoriesRepository: ICategoriesRepository,
  ) {}

  /**
   * Tạo category mới
   *
   * Business rules:
   * - Slug phải unique
   * - Nếu không có slug, tự động generate từ name
   * - Nếu không có displayOrder, đặt sau category cuối cùng
   */
  async create(dto: CreateCategoryDto): Promise<CategoryEntity> {
    // Generate slug nếu không có
    let slug = dto.slug || generateSlug(dto.name);

    // Kiểm tra slug unique
    const isSlugExist = await this.categoriesRepository.isSlugExist(slug);
    if (isSlugExist) {
      // Thêm suffix số để unique
      let suffix = 1;
      while (await this.categoriesRepository.isSlugExist(`${slug}-${suffix}`)) {
        suffix++;
      }
      slug = `${slug}-${suffix}`;
    }

    // Get displayOrder nếu không có
    let displayOrder = dto.displayOrder;
    if (displayOrder === undefined) {
      const maxOrder = await this.categoriesRepository.getMaxDisplayOrder();
      displayOrder = maxOrder + 1;
    }

    const categoryData = {
      name: dto.name,
      slug,
      description: dto.description,
      imageUrl: dto.imageUrl,
      icon: dto.icon,
      displayOrder,
      status: dto.status ?? CategoryStatus.ACTIVE,
    };

    const category = await this.categoriesRepository.create(categoryData);

    this.logger.log(`Category created: ${category.id} - ${category.name}`);

    return category;
  }

  /**
   * Lấy category theo ID
   */
  async findById(id: string): Promise<CategoryEntity> {
    return this.categoriesRepository.findByIdOrThrow(id);
  }

  /**
   * Lấy category theo slug
   */
  async findBySlug(slug: string): Promise<CategoryEntity | null> {
    return this.categoriesRepository.findBySlug(slug);
  }

  /**
   * Lấy tất cả categories (cho Admin)
   */
  async findAll(): Promise<CategoryEntity[]> {
    return this.categoriesRepository.findAll();
  }

  /**
   * Lấy categories active (cho Public API)
   */
  async findActive(): Promise<CategoryEntity[]> {
    return this.categoriesRepository.findActive();
  }

  /**
   * Update category
   *
   * Business rules:
   * - Nếu đổi slug, phải unique
   */
  async update(id: string, dto: UpdateCategoryDto): Promise<CategoryEntity> {
    // Kiểm tra category tồn tại
    await this.categoriesRepository.findByIdOrThrow(id);

    // Kiểm tra slug unique nếu đổi slug
    if (dto.slug) {
      const isSlugExist = await this.categoriesRepository.isSlugExist(
        dto.slug,
        id,
      );
      if (isSlugExist) {
        throw new ConflictException(`Slug "${dto.slug}" already exists`);
      }
    }

    const category = await this.categoriesRepository.update(id, dto);

    this.logger.log(`Category updated: ${category.id} - ${category.name}`);

    return category;
  }

  /**
   * Xóa category
   *
   * Business rules:
   * - Không cho xóa nếu có products (future implementation)
   */
  async delete(id: string): Promise<void> {
    // Kiểm tra category tồn tại
    const category = await this.categoriesRepository.findByIdOrThrow(id);

    // TODO: Kiểm tra có products không
    // if (category.productCount && category.productCount > 0) {
    //   throw new ConflictException(
    //     `Cannot delete category with ${category.productCount} products`,
    //   );
    // }

    await this.categoriesRepository.delete(id);

    this.logger.log(`Category deleted: ${id} - ${category.name}`);
  }
}

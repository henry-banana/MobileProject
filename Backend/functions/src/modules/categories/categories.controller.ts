import {
  Controller,
  Get,
  Param,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
} from '@nestjs/swagger';
import { CategoriesService } from './categories.service';

/**
 * Categories Controller - Public API
 *
 * Lấy danh sách categories cho khách hàng/users
 * Không cần xác thực
 *
 * Base URL: /categories
 *
 * Theo docs: docs-god/api/05_CATEGORIES_API.md
 */
@ApiTags('Categories')
@Controller('categories')
export class CategoriesController {
  constructor(private readonly categoriesService: CategoriesService) {}

  /**
   * GET /categories
   * Lấy danh sách categories active
   */
  @Get()
  @ApiOperation({ summary: 'Lấy danh sách categories (chỉ active)' })
  @ApiResponse({
    status: 200,
    description: 'Danh sách categories',
  })
  async findActive() {
    const categories = await this.categoriesService.findActive();
    return {
      success: true,
      data: categories,
    };
  }

  /**
   * GET /categories/:idOrSlug
   * Lấy chi tiết category theo ID hoặc slug
   */
  @Get(':idOrSlug')
  @ApiOperation({ summary: 'Lấy chi tiết category theo ID hoặc slug' })
  @ApiResponse({
    status: 200,
    description: 'Chi tiết category',
  })
  @ApiResponse({ status: 404, description: 'Category không tồn tại' })
  async findOne(@Param('idOrSlug') idOrSlug: string) {
    // Thử tìm theo slug trước
    let category = await this.categoriesService.findBySlug(idOrSlug);

    // Nếu không tìm thấy, thử tìm theo ID
    if (!category) {
      category = await this.categoriesService.findById(idOrSlug);
    }

    return {
      success: true,
      data: category,
    };
  }
}

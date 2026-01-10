import {
  Controller,
  Post,
  Get,
  Put,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { ProductsService } from '../services';
import { CreateProductDto, UpdateProductDto, ToggleAvailabilityDto, ProductFilterDto } from '../dto';
import { FirebaseAuthGuard } from '../../../common/guards/firebase-auth.guard';
import { RolesGuard } from '../../../common/guards/roles.guard';
import { Roles } from '../../../common/decorators/roles.decorator';
import { CurrentUser } from '../../../common/decorators/current-user.decorator';
import { UserRole } from '../../../common/enums/user-role.enum';

/**
 * Owner Products Controller
 *
 * Authenticated endpoints for owners to manage their shop's products
 * Requires Bearer token with OWNER role
 *
 * Base URL: /owner/products
 *
 * Tasks: PROD-001 to PROD-008
 */
@ApiTags('Owner Products')
@ApiBearerAuth()
@UseGuards(FirebaseAuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/products')
export class OwnerProductsController {
  constructor(private readonly productsService: ProductsService) {}

  /**
   * POST /owner/products
   * Create a new product
   *
   * PROD-001
   */
  @Post()
  @ApiOperation({
    summary: 'Create product',
    description: 'Owner creates a new product for their shop',
  })
  @ApiResponse({
    status: 201,
    description: 'Product created successfully',
    schema: {
      example: {
        success: true,
        data: {
          id: 'prod_abc',
          shopId: 'shop_123',
          shopName: 'Quán Phở Việt',
          name: 'Cơm sườn nướng',
          description: 'Cơm sườn nướng mật ong + trứng',
          price: 35000,
          categoryId: 'cat_1',
          categoryName: 'Cơm',
          imageUrl: 'https://...',
          isAvailable: true,
          preparationTime: 15,
          rating: 0,
          totalRatings: 0,
          soldCount: 0,
        },
      },
    },
  })
  async createProduct(@CurrentUser('uid') ownerId: string, @Body() dto: CreateProductDto) {
    return this.productsService.createProduct(ownerId, dto);
  }

  /**
   * GET /owner/products
   * Get all products of owner's shop
   *
   * PROD-002
   */
  @Get()
  @ApiOperation({
    summary: 'Get my products',
    description: "Get all products of owner's shop with filters",
  })
  @ApiQuery({ name: 'categoryId', required: false, type: String })
  @ApiQuery({ name: 'isAvailable', required: false, type: Boolean })
  @ApiQuery({ name: 'page', required: false, type: Number, example: 1 })
  @ApiQuery({ name: 'limit', required: false, type: Number, example: 20 })
  @ApiResponse({
    status: 200,
    description: 'List of products',
    schema: {
      example: {
        success: true,
        data: {
          products: [
            {
              id: 'prod_abc',
              name: 'Cơm sườn nướng',
              price: 35000,
              isAvailable: true,
              soldCount: 50,
            },
          ],
          total: 15,
          page: 1,
          limit: 20,
        },
      },
    },
  })
  async getMyProducts(@CurrentUser('uid') ownerId: string, @Query() filters: ProductFilterDto) {
    return this.productsService.getMyProducts(ownerId, filters);
  }

  /**
   * GET /owner/products/:id
   * Get product detail
   *
   * PROD-003
   */
  @Get(':id')
  @ApiOperation({
    summary: 'Get product detail',
    description: 'Get detailed information of a specific product',
  })
  @ApiResponse({
    status: 200,
    description: 'Product details',
  })
  async getMyProduct(@CurrentUser('uid') ownerId: string, @Param('id') productId: string) {
    return this.productsService.getMyProduct(ownerId, productId);
  }

  /**
   * PUT /owner/products/:id
   * Update product
   *
   * PROD-006 - Price Lock Rule applies
   */
  @Put(':id')
  @ApiOperation({
    summary: 'Update product',
    description: 'Update product information. Cannot change price when shop is open.',
  })
  @ApiResponse({
    status: 200,
    description: 'Product updated successfully',
  })
  @ApiResponse({
    status: 409,
    description: 'Cannot change price when shop is open',
  })
  async updateProduct(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @Body() dto: UpdateProductDto,
  ) {
    await this.productsService.updateProduct(ownerId, productId, dto);
    return { message: 'Cập nhật sản phẩm thành công' };
  }

  /**
   * PUT /owner/products/:id/availability
   * Toggle product availability
   *
   * PROD-007
   */
  @Put(':id/availability')
  @ApiOperation({
    summary: 'Toggle product availability',
    description: 'Mark product as available or unavailable',
  })
  @ApiResponse({
    status: 200,
    description: 'Availability updated successfully',
  })
  async toggleAvailability(
    @CurrentUser('uid') ownerId: string,
    @Param('id') productId: string,
    @Body() dto: ToggleAvailabilityDto,
  ) {
    await this.productsService.toggleAvailability(ownerId, productId, dto);
    return { message: 'Cập nhật trạng thái sản phẩm thành công' };
  }

  /**
   * DELETE /owner/products/:id
   * Delete product (soft delete)
   *
   * PROD-008
   */
  @Delete(':id')
  @ApiOperation({
    summary: 'Delete product',
    description: 'Soft delete a product (set isDeleted = true)',
  })
  @ApiResponse({
    status: 200,
    description: 'Product deleted successfully',
  })
  async deleteProduct(@CurrentUser('uid') ownerId: string, @Param('id') productId: string) {
    await this.productsService.deleteProduct(ownerId, productId);
    return { message: 'Xóa sản phẩm thành công' };
  }
}

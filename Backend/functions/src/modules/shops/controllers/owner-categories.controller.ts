import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
  ForbiddenException,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { ShopsService } from '../services/shops.service';
import { CategoriesService } from '../../categories/categories.service';
import { CreateCategoryDto } from '../../categories/dto/create-category.dto';
import { UpdateCategoryDto } from '../../categories/dto/update-category.dto';

/**
 * Owner Categories Controller
 *
 * Manage Shop-specific Categories (Menu Groups)
 */
@ApiTags('Owner - Categories')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/categories')
export class OwnerCategoriesController {
  constructor(
    private readonly shopsService: ShopsService,
    private readonly categoriesService: CategoriesService,
  ) {}

  @Post()
  @ApiOperation({ summary: 'Create category for shop' })
  @ApiResponse({ status: 201, description: 'Category created' })
  async create(
    @CurrentUser('uid') ownerId: string,
    @Body() dto: CreateCategoryDto,
  ) {
    const shop = await this.shopsService.getMyShop(ownerId);
    
    const category = await this.categoriesService.create(dto, shop.id);
    return { success: true, data: category };
  }

  @Get()
  @ApiOperation({ summary: 'Get all categories of shop' })
  async findAll(@CurrentUser('uid') ownerId: string) {
    const shop = await this.shopsService.getMyShop(ownerId);
    
    // Get shop specific categories
    const categories = await this.categoriesService.findByShopId(shop.id);
    return { success: true, data: categories };
  }

  @Put(':id')
  @ApiOperation({ summary: 'Update category' })
  async update(
    @CurrentUser('uid') ownerId: string,
    @Param('id') id: string,
    @Body() dto: UpdateCategoryDto,
  ) {
    const shop = await this.shopsService.getMyShop(ownerId);
    const category = await this.categoriesService.findById(id);
    
    if (category.shopId !== shop.id) {
      throw new ForbiddenException('Cannot update category of another shop or system category');
    }

    const updated = await this.categoriesService.update(id, dto);
    return { success: true, data: updated };
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete category' })
  async remove(@CurrentUser('uid') ownerId: string, @Param('id') id: string) {
    const shop = await this.shopsService.getMyShop(ownerId);
    const category = await this.categoriesService.findById(id);
    
    if (category.shopId !== shop.id) {
      throw new ForbiddenException('Cannot delete category of another shop or system category');
    }

    await this.categoriesService.delete(id);
    return { success: true, message: 'Deleted successfully' };
  }
}

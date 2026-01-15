import { Controller, Get, Put, Param, Body, Query, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { AuthGuard, AdminGuard } from '../../../core/guards';
import { AdminService } from '../admin.service';
import { ListUsersQueryDto, UpdateUserStatusDto, UserStatus } from '../dto';

/**
 * Admin Users Controller
 *
 * Quản lý users - ADMIN-006, ADMIN-007
 *
 * Base URL: /admin/users
 */
@ApiTags('Admin - Users')
@ApiBearerAuth()
@UseGuards(AuthGuard, AdminGuard)
@Controller('admin/users')
export class AdminUsersController {
  constructor(private readonly adminService: AdminService) {}

  /**
   * GET /admin/users
   * Lấy danh sách tất cả users với pagination và filter
   *
   * ADMIN-006: List All Users
   */
  @Get()
  @ApiOperation({ summary: 'Lấy danh sách users' })
  @ApiQuery({ name: 'page', required: false, type: Number })
  @ApiQuery({ name: 'limit', required: false, type: Number })
  @ApiQuery({ name: 'role', required: false, enum: ['CUSTOMER', 'OWNER', 'SHIPPER'] })
  @ApiQuery({ name: 'status', required: false, enum: UserStatus })
  @ApiQuery({ name: 'search', required: false, type: String })
  @ApiResponse({ status: 200, description: 'Danh sách users' })
  @ApiResponse({ status: 401, description: 'Chưa xác thực' })
  @ApiResponse({ status: 403, description: 'Không có quyền Admin' })
  async listUsers(@Query() query: ListUsersQueryDto) {
    return this.adminService.listUsers(query);
  }

  /**
   * GET /admin/users/:userId
   * Lấy chi tiết user
   */
  @Get(':userId')
  @ApiOperation({ summary: 'Lấy chi tiết user' })
  @ApiResponse({ status: 200, description: 'Chi tiết user' })
  @ApiResponse({ status: 404, description: 'User không tồn tại' })
  async getUser(@Param('userId') userId: string) {
    return this.adminService.getUserById(userId);
  }

  /**
   * PUT /admin/users/:userId/status
   * Ban/Unban user
   *
   * ADMIN-007: Update User Status (Ban/Unban)
   */
  @Put(':userId/status')
  @ApiOperation({ summary: 'Ban/Unban user' })
  @ApiResponse({ status: 200, description: 'Cập nhật status thành công' })
  @ApiResponse({ status: 400, description: 'Dữ liệu không hợp lệ' })
  @ApiResponse({ status: 403, description: 'Không thể ban admin khác' })
  @ApiResponse({ status: 404, description: 'User không tồn tại' })
  async updateUserStatus(
    @Req() req: any,
    @Param('userId') userId: string,
    @Body() dto: UpdateUserStatusDto,
  ) {
    const adminId = req.user.uid;
    await this.adminService.updateUserStatus(adminId, userId, dto);
    return {
      message: dto.status === UserStatus.BANNED ? 'User đã bị ban' : 'User đã được unban',
    };
  }
}

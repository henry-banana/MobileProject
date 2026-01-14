import { Controller, Get, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { AuthGuard, AdminGuard } from '../../../core/guards';
import { AdminService } from '../admin.service';

/**
 * Admin Dashboard Controller
 *
 * Dashboard stats - ADMIN-013
 *
 * Base URL: /admin/dashboard
 */
@ApiTags('Admin - Dashboard')
@ApiBearerAuth()
@UseGuards(AuthGuard, AdminGuard)
@Controller('admin/dashboard')
export class AdminDashboardController {
  constructor(private readonly adminService: AdminService) {}

  /**
   * GET /admin/dashboard
   * Lấy thống kê tổng quan
   *
   * ADMIN-013: Admin Dashboard
   */
  @Get()
  @ApiOperation({ summary: 'Lấy thống kê dashboard' })
  @ApiResponse({
    status: 200,
    description: 'Dashboard stats',
    schema: {
      example: {
        success: true,
        data: {
          users: {
            total: 1000,
            customers: 800,
            owners: 150,
            shippers: 50,
            newToday: 5,
          },
          shops: {
            total: 150,
            active: 120,
            pendingApproval: 5,
          },
          orders: {
            today: 100,
            thisWeek: 500,
            thisMonth: 2000,
          },
          revenue: {
            today: 5000000,
            thisWeek: 25000000,
            thisMonth: 100000000,
          },
          payouts: {
            pending: 10,
            totalPendingAmount: 5000000,
          },
        },
      },
    },
  })
  async getDashboard() {
    const stats = await this.adminService.getDashboardStats();
    return {
      success: true,
      data: stats,
    };
  }

  /**
   * GET /admin/dashboard/users
   * Thống kê users chi tiết
   */
  @Get('users')
  @ApiOperation({ summary: 'Thống kê users chi tiết' })
  @ApiResponse({ status: 200, description: 'User stats' })
  async getUserStats() {
    const stats = await this.adminService.getUserStats();
    return {
      success: true,
      data: stats,
    };
  }

  /**
   * GET /admin/dashboard/orders
   * Thống kê orders chi tiết
   */
  @Get('orders')
  @ApiOperation({ summary: 'Thống kê orders chi tiết' })
  @ApiResponse({ status: 200, description: 'Order stats' })
  async getOrderStats() {
    const stats = await this.adminService.getOrderStats();
    return {
      success: true,
      data: stats,
    };
  }

  /**
   * GET /admin/dashboard/revenue
   * Thống kê revenue chi tiết
   */
  @Get('revenue')
  @ApiOperation({ summary: 'Thống kê revenue chi tiết' })
  @ApiResponse({ status: 200, description: 'Revenue stats' })
  async getRevenueStats() {
    const stats = await this.adminService.getRevenueStats();
    return {
      success: true,
      data: stats,
    };
  }
}

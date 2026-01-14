import { Injectable, CanActivate, ExecutionContext, ForbiddenException } from '@nestjs/common';
import { UserRole } from '../interfaces/user.interface';

/**
 * Admin Guard - Bảo vệ các route chỉ dành cho Admin
 *
 * Guard này kiểm tra custom claims trong Firebase Auth token
 * để xác định người dùng có phải Admin hay không.
 *
 * Phải sử dụng sau AuthGuard (để có user trong request)
 *
 * @example
 * ```typescript
 * @UseGuards(AuthGuard, AdminGuard)
 * @Controller('admin')
 * export class AdminController {}
 * ```
 */
@Injectable()
export class AdminGuard implements CanActivate {
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const user = request.user;

    // Kiểm tra user đã được xác thực (từ AuthGuard)
    if (!user) {
      throw new ForbiddenException('Authentication required');
    }

    // Kiểm tra role từ custom claims
    const role = user.role;

    if (role !== UserRole.ADMIN) {
      throw new ForbiddenException('Access denied. Admin privileges required.');
    }

    return true;
  }
}

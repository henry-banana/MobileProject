import { IBaseEntity } from './base-entity.interface';
import { IReadRepository } from './read-repository.interface';
import { IWriteRepository } from './write-repository.interface';

/**
 * IBaseRepository - Combined interface cho full CRUD
 *
 * Extends cả Read và Write repositories
 * Theo Interface Segregation Principle (ISP)
 *
 * Usage:
 * - Dùng IReadRepository khi chỉ cần read
 * - Dùng IWriteRepository khi chỉ cần write
 * - Dùng IBaseRepository khi cần cả hai
 */
export interface IBaseRepository<T extends IBaseEntity>
  extends IReadRepository<T>,
    IWriteRepository<T> {}

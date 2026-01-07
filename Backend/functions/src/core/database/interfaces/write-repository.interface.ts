import { IBaseEntity } from './base-entity.interface';

/**
 * IWriteRepository - Interface cho write operations
 *
 * Theo Interface Segregation Principle (ISP)
 */
export interface IWriteRepository<T extends IBaseEntity> {
  /**
   * Tạo entity mới
   */
  create(data: Omit<T, 'id' | 'createdAt' | 'updatedAt'>): Promise<T>;

  /**
   * Tạo entity với ID cụ thể
   */
  createWithId(id: string, data: Omit<T, 'id' | 'createdAt' | 'updatedAt'>): Promise<T>;

  /**
   * Update entity
   */
  update(id: string, data: Partial<Omit<T, 'id' | 'createdAt' | 'updatedAt'>>): Promise<T>;

  /**
   * Xóa entity (hard delete)
   */
  delete(id: string): Promise<void>;

  /**
   * Xóa entity (soft delete - set deletedAt)
   */
  softDelete?(id: string): Promise<void>;
}

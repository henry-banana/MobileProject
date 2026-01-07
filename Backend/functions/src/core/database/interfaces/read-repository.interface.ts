import { IBaseEntity, PaginatedResult, QueryOptions } from './base-entity.interface';

/**
 * IReadRepository - Interface cho read operations
 *
 * Theo Interface Segregation Principle (ISP)
 */
export interface IReadRepository<T extends IBaseEntity> {
  /**
   * Tìm entity theo ID
   */
  findById(id: string): Promise<T | null>;

  /**
   * Tìm entity theo ID, throw error nếu không tìm thấy
   */
  findByIdOrThrow(id: string): Promise<T>;

  /**
   * Lấy tất cả entities
   */
  findAll(): Promise<T[]>;

  /**
   * Lấy entities với query options
   */
  findMany(options?: QueryOptions<T>): Promise<T[]>;

  /**
   * Lấy entities với pagination
   */
  findPaginated(options?: QueryOptions<T>): Promise<PaginatedResult<T>>;

  /**
   * Đếm số lượng entities
   */
  count(where?: Partial<Record<keyof T, any>>): Promise<number>;

  /**
   * Kiểm tra entity tồn tại
   */
  exists(id: string): Promise<boolean>;
}

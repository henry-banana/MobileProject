/**
 * Base Entity Interface
 *
 * Tất cả entities phải extend interface này
 */
export interface IBaseEntity {
  id?: string;
  createdAt?: FirebaseFirestore.Timestamp;
  updatedAt?: FirebaseFirestore.Timestamp;
}

/**
 * Pagination Options
 */
export interface PaginationOptions {
  page?: number;
  limit?: number;
}

/**
 * Paginated Result
 */
export interface PaginatedResult<T> {
  data: T[];
  meta: {
    total: number;
    page: number;
    limit: number;
    totalPages: number;
  };
}

/**
 * Query Options for filtering
 */
export interface QueryOptions<T> {
  where?: Partial<Record<keyof T, any>>;
  orderBy?: {
    field: keyof T;
    direction: 'asc' | 'desc';
  };
  pagination?: PaginationOptions;
}

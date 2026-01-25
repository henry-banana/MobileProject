/**
 * Response wrapper for notification endpoints
 * Controllers should return raw payloads only
 * The global TransformInterceptor will wrap with {success, data, timestamp}
 */

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  unreadCount?: number;
}

export interface MarkAllAsReadResponse {
  updated: number;
}

/**
 * Helper to build paginated response
 * Controllers return this, interceptor wraps with {success, data}
 */
export function buildPaginatedResponse<T>(
  items: T[],
  total: number,
  page: number,
  limit: number,
  unreadCount?: number,
): PaginatedResponse<T> {
  return {
    items,
    total,
    page,
    limit,
    ...(unreadCount !== undefined && { unreadCount }),
  };
}

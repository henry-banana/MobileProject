/**
 * Pagination Meta Interface
 */
export interface PaginationMeta {
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

/**
 * Paginated Result Interface
 */
export interface PaginatedResult<T> {
  data: T[];
  meta: PaginationMeta;
}

/**
 * Dashboard Stats Interface
 */
export interface DashboardStats {
  users: {
    total: number;
    customers: number;
    owners: number;
    shippers: number;
    newToday: number;
  };
  shops: {
    total: number;
    active: number;
    pendingApproval: number;
  };
  orders: {
    today: number;
    thisWeek: number;
    thisMonth: number;
  };
  revenue: {
    today: number;
    thisWeek: number;
    thisMonth: number;
  };
  payouts: {
    pending: number;
    totalPendingAmount: number;
  };
}

/**
 * User Stats Interface
 */
export interface UserStats {
  total: number;
  customers: number;
  owners: number;
  shippers: number;
  newToday: number;
}

/**
 * Order Stats Interface
 */
export interface OrderStats {
  today: number;
  thisWeek: number;
  thisMonth: number;
}

/**
 * Revenue Stats Interface
 */
export interface RevenueStats {
  today: number;
  thisWeek: number;
  thisMonth: number;
}

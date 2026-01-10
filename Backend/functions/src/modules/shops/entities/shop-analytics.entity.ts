/**
 * Shop Analytics Entity
 * For owner dashboard
 */
export class ShopAnalyticsEntity {
  today: {
    revenue: number;
    orderCount: number;
    avgOrderValue: number;
    pendingOrders: number;
  };

  thisWeek: {
    revenue: number;
    orderCount: number;
    avgOrderValue: number;
  };

  thisMonth: {
    revenue: number;
    orderCount: number;
  };

  ordersByStatus: {
    [status: string]: number;
  };

  topProducts: Array<{
    id: string;
    name: string;
    soldCount: number;
    revenue: number;
  }>;

  recentOrders: Array<{
    id: string;
    orderNumber: string;
    status: string;
    total: number;
    createdAt: string;
  }>;
}

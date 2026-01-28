import { ReviewEntity } from '../entities';

export interface IReviewsRepository {
  findById(id: string): Promise<ReviewEntity | null>;
  findByOrderId(orderId: string): Promise<ReviewEntity | null>;
  findByShopId(
    shopId: string,
    options?: { page?: number; limit?: number },
  ): Promise<{ reviews: ReviewEntity[]; total: number }>;
  findByCustomerId(customerId: string): Promise<ReviewEntity[]>;
  create(review: Omit<ReviewEntity, 'id'>): Promise<ReviewEntity>;
  update(id: string, updates: Partial<ReviewEntity>): Promise<void>;
  delete(id: string): Promise<void>;
  getShopStats(shopId: string): Promise<{ avgRating: number; totalReviews: number }>;
}

export const REVIEWS_REPOSITORY = 'REVIEWS_REPOSITORY';

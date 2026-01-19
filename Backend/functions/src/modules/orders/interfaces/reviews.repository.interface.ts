import { ReviewEntity } from '../entities';

export interface IReviewsRepository {
  findById(id: string): Promise<ReviewEntity | null>;
  findByOrderId(orderId: string): Promise<ReviewEntity | null>;
  findByShopId(shopId: string): Promise<ReviewEntity[]>;
  create(review: ReviewEntity): Promise<ReviewEntity>;
  update(id: string, updates: Partial<ReviewEntity>): Promise<void>;
  delete(id: string): Promise<void>;
}

export const REVIEWS_REPOSITORY = 'REVIEWS_REPOSITORY';

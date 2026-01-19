export class ReviewEntity {
  id?: string;
  orderId: string;
  customerId: string;
  shopId: string;
  rating: number; // 1-5
  comment?: string;
  ownerReply?: string;
  ownerRepliedAt?: any;
  createdAt?: any;
  updatedAt?: any;
}

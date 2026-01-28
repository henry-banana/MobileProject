export class ReviewEntity {
  id?: string;
  orderId: string;
  customerId: string;
  customerName?: string; // Denormalized for display
  shopId: string;
  rating: number; // 1-5
  comment?: string;
  ownerReply?: string;
  ownerRepliedAt?: any;
  createdAt?: any;
  updatedAt?: any;
}

export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number; // Locked at order creation time
  subtotal: number;
}

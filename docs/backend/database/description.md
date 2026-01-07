# TÀI LIỆU MÔ TẢ DATABASE - KTX Delivery

- **Version**: 2.1
- **Last Updated**: 2026-01-07
- **Database**: Cloud Firestore (NoSQL)
- **Total Collections**: 22

---

## USERS

| Field       | Type      | Description                                   |
| ----------- | --------- | --------------------------------------------- |
| id          | string    | Primary Key (Firebase UID)                    |
| email       | string    | Unique, Email đăng nhập                       |
| displayName | string    | Tên hiển thị                                  |
| phone       | string?   | Số điện thoại VN (10 digits)                  |
| avatarUrl   | string?   | URL avatar                                    |
| role        | enum      | UserRole: CUSTOMER, OWNER, SHIPPER, ADMIN     |
| status      | enum      | UserStatus: PENDING, ACTIVE, SUSPENDED, DELETED |
| fcmTokens   | array     | FCM tokens cho push notification (max 5)      |
| shopId      | string?   | FK đến SHOPS (nếu role = OWNER)               |
| shipperInfo | object?   | Thông tin shipper (nếu role = SHIPPER)        |
| addresses   | [Address] | Embedded: danh sách địa chỉ giao hàng         |
| createdAt   | timestamp | Thời điểm tạo                                 |
| updatedAt   | timestamp | Thời điểm cập nhật cuối                       |

### Embedded: shipperInfo

| Field               | Type      | Description                         |
| ------------------- | --------- | ----------------------------------- |
| shopId              | string    | FK đến shop đang làm việc           |
| shopName            | string    | Denormalized                        |
| status              | enum      | PENDING_APPROVAL, ACTIVE, RESIGNED  |
| isOnline            | boolean   | Đang nhận đơn không                 |
| currentOrders       | array     | Order IDs đang giao                 |
| maxConcurrentOrders | number    | Tối đa đơn cùng lúc (default: 3)    |
| totalDeliveries     | number    | Tổng số đơn đã giao                 |
| rating              | number    | Rating trung bình (0-5)             |
| joinedShopAt        | timestamp | Thời điểm vào shop                  |

### Embedded: Address

| Field       | Type    | Description              |
| ----------- | ------- | ------------------------ |
| id          | string  | UUID                     |
| label       | string  | Tên gợi nhớ ("Tòa A")    |
| fullAddress | string  | Địa chỉ đầy đủ           |
| building    | string? | Tòa nhà                  |
| room        | string? | Số phòng                 |
| note        | string? | Ghi chú thêm             |
| isDefault   | boolean | Địa chỉ mặc định         |

---

## SHOPS

| Field           | Type        | Description                              |
| --------------- | ----------- | ---------------------------------------- |
| id              | string      | Primary Key (UUID)                       |
| ownerId         | string      | FK đến USERS                             |
| name            | string      | Tên shop                                 |
| description     | string?     | Mô tả shop                               |
| coverImageUrl   | string?     | Ảnh bìa                                  |
| logoUrl         | string?     | Logo                                     |
| phone           | string      | SĐT liên hệ                              |
| address         | string      | Địa chỉ shop                             |
| isOpen          | boolean     | Đang mở cửa?                             |
| status          | enum        | ShopStatus: OPEN, CLOSED, SUSPENDED      |
| openTime        | string      | Giờ mở (HH:mm)                           |
| closeTime       | string      | Giờ đóng (HH:mm)                         |
| displayHours    | object?     | Giờ mở cửa theo ngày (JSON)              |
| shipFeePerOrder | number      | Phí ship mỗi đơn (min 3000đ)             |
| minOrderAmount  | number      | Đơn tối thiểu (VND)                      |
| rating          | number      | Rating trung bình (0-5)                  |
| totalRatings    | number      | Số lượt đánh giá                         |
| totalOrders     | number      | Tổng đơn hàng                            |
| subscription    | object      | Thông tin subscription (embedded)        |
| priceLockedSince| timestamp?  | Thời điểm lock giá                       |
| lastOpenedAt    | timestamp?  | Lần mở cửa gần nhất                      |
| lastClosedAt    | timestamp?  | Lần đóng cửa gần nhất                    |
| createdAt       | timestamp   | Thời điểm tạo                            |
| updatedAt       | timestamp   | Thời điểm cập nhật cuối                  |

### Embedded: subscription

| Field          | Type      | Description                    |
| -------------- | --------- | ------------------------------ |
| plan           | enum      | FREE_TRIAL, PREMIUM            |
| status         | enum      | TRIAL, ACTIVE, EXPIRED, CANCELLED |
| trialStartDate | timestamp | Bắt đầu trial                  |
| trialEndDate   | timestamp | Kết thúc trial                 |
| startDate      | timestamp | Bắt đầu subscription           |
| endDate        | timestamp | Kết thúc subscription          |
| autoRenew      | boolean   | Tự động gia hạn                |
| monthlyFee     | number    | 50000 VND                      |

---

## CATEGORIES

| Field       | Type      | Description            |
| ----------- | --------- | ---------------------- |
| id          | string    | Primary Key (UUID)     |
| name        | string    | Tên tiếng Việt         |
| nameEn      | string?   | Tên tiếng Anh          |
| slug        | string    | Unique, URL-friendly   |
| icon        | string?   | Material icon name     |
| description | string?   | Mô tả                  |
| sortOrder   | number    | Thứ tự hiển thị        |
| isActive    | boolean   | Đang hoạt động?        |
| createdAt   | timestamp | Thời điểm tạo          |
| updatedAt   | timestamp | Thời điểm cập nhật cuối|

> ⚠️ **Admin-managed**: Chỉ ADMIN mới có quyền CRUD.

---

## PRODUCTS

| Field        | Type      | Description                              |
| ------------ | --------- | ---------------------------------------- |
| id           | string    | Primary Key (UUID)                       |
| shopId       | string    | FK đến SHOPS                             |
| shopName     | string    | Denormalized                             |
| categoryId   | string    | FK đến CATEGORIES                        |
| categoryName | string    | Denormalized                             |
| name         | string    | Tên sản phẩm                             |
| description  | string?   | Mô tả                                    |
| price        | number    | Giá (VND)                                |
| imageUrl     | string?   | Ảnh chính                                |
| images       | array?    | Ảnh phụ                                  |
| isAvailable  | boolean   | Còn hàng?                                |
| sortOrder    | number    | Thứ tự hiển thị                          |
| rate         | number    | Rating trung bình (0-5)                  |
| totalRate    | number    | Số lượt đánh giá                         |
| priceHistory | array?    | Lịch sử thay đổi giá                     |
| createdAt    | timestamp | Thời điểm tạo                            |
| updatedAt    | timestamp | Thời điểm cập nhật cuối                  |

---

## CARTS

> **Document ID = userId** (1 user = 1 cart)

| Field     | Type       | Description               |
| --------- | ---------- | ------------------------- |
| id        | string     | = userId                  |
| shopId    | string?    | FK đến SHOPS (1 shop/cart)|
| shopName  | string?    | Denormalized              |
| items     | [CartItem] | Embedded: danh sách items |
| updatedAt | timestamp  | Thời điểm cập nhật cuối   |

### Embedded: CartItem

| Field           | Type    | Description                 |
| --------------- | ------- | --------------------------- |
| id              | string  | Item ID (UUID)              |
| productId       | string  | FK đến PRODUCTS             |
| productName     | string  | Denormalized                |
| productImage    | string? | Denormalized                |
| unitPrice       | number  | Giá tại thời điểm thêm      |
| quantity        | number  | Số lượng                    |
| selectedOptions | array?  | Options đã chọn             |
| note            | string? | Ghi chú                     |

---

## ORDERS

| Field           | Type           | Description                              |
| --------------- | -------------- | ---------------------------------------- |
| id              | string         | Primary Key (UUID)                       |
| orderNumber     | string         | Human-readable: ORD-20260107-001         |
| customerId      | string         | FK đến USERS                             |
| customerName    | string         | Denormalized                             |
| customerPhone   | string         | Denormalized                             |
| shopId          | string         | FK đến SHOPS                             |
| shopName        | string         | Denormalized                             |
| shipperId       | string?        | FK đến USERS (shipper)                   |
| shipperName     | string?        | Denormalized                             |
| shipperPhone    | string?        | Denormalized                             |
| items           | [OrderItem]    | Embedded: danh sách items                |
| deliveryAddress | object         | Embedded: địa chỉ giao                   |
| status          | enum           | OrderStatus (see below)                  |
| paymentMethod   | enum           | COD, ZALOPAY, MOMO, SEPAY                |
| paymentStatus   | enum           | PENDING, PAID, FAILED, REFUNDED          |
| subtotal        | number         | Tổng items (VND)                         |
| discountAmount  | number         | Số tiền giảm (voucher)                   |
| shipFee         | number         | Phí ship                                 |
| total           | number         | Tổng thanh toán                          |
| ownerEarnings   | number         | = subtotal - discountAmount - shipFee    |
| shipperEarnings | number         | = shipFee                                |
| voucherId       | string?        | FK đến VOUCHERS                          |
| voucherCode     | string?        | Mã voucher                               |
| note            | string?        | Ghi chú đơn hàng                         |
| cancelledBy     | enum?          | CUSTOMER, OWNER, SYSTEM                  |
| cancelReason    | string?        | Lý do hủy                                |
| proofImageUrl   | string?        | Ảnh chứng minh giao hàng                 |
| statusHistory   | array          | Lịch sử status                           |
| createdAt       | timestamp      | Thời điểm tạo                            |
| confirmedAt     | timestamp?     | Thời điểm xác nhận                       |
| preparedAt      | timestamp?     | Thời điểm chuẩn bị                       |
| readyAt         | timestamp?     | Thời điểm sẵn sàng                       |
| assignedAt      | timestamp?     | Thời điểm assign shipper                 |
| pickedUpAt      | timestamp?     | Thời điểm lấy hàng                       |
| deliveredAt     | timestamp?     | Thời điểm giao xong                      |
| cancelledAt     | timestamp?     | Thời điểm hủy                            |

---

## PAYMENTS

| Field         | Type      | Description                    |
| ------------- | --------- | ------------------------------ |
| id            | string    | Primary Key (UUID)             |
| orderId       | string    | FK đến ORDERS                  |
| orderNumber   | string    | Denormalized                   |
| customerId    | string    | FK đến USERS                   |
| method        | enum      | ZALOPAY, MOMO, SEPAY, COD      |
| amount        | number    | Số tiền (VND)                  |
| status        | enum      | PENDING, PAID, FAILED, EXPIRED, REFUNDED |
| transactionId | string?   | Transaction ID từ payment provider |
| payUrl        | string?   | URL thanh toán                 |
| qrCodeUrl     | string?   | QR code (SePay)                |
| failReason    | string?   | Lý do thất bại                 |
| paidAt        | timestamp?| Thời điểm thanh toán           |
| expiredAt     | timestamp?| Thời điểm hết hạn              |
| createdAt     | timestamp | Thời điểm tạo                  |
| updatedAt     | timestamp | Thời điểm cập nhật cuối        |

---

## WALLETS

> **Document ID = userId**

| Field         | Type      | Description               |
| ------------- | --------- | ------------------------- |
| id            | string    | = userId                  |
| userId        | string    | FK đến USERS              |
| type          | enum      | OWNER, SHIPPER            |
| pending       | number    | Số dư chờ xử lý           |
| available     | number    | Số dư khả dụng            |
| locked        | number    | Số dư bị khóa             |
| totalEarnings | number    | Tổng thu nhập             |
| createdAt     | timestamp | Thời điểm tạo             |
| updatedAt     | timestamp | Thời điểm cập nhật cuối   |

### Subcollection: wallets/{userId}/transactions

| Field         | Type      | Description                   |
| ------------- | --------- | ----------------------------- |
| id            | string    | Primary Key (UUID)            |
| type          | enum      | ORDER_EARNING, PAYOUT, REFUND |
| amount        | number    | Số tiền                       |
| orderId       | string?   | FK đến ORDERS                 |
| orderNumber   | string?   | Denormalized                  |
| description   | string    | Mô tả giao dịch               |
| balanceAfter  | object    | {pending, available, locked}  |
| createdAt     | timestamp | Thời điểm tạo                 |

---

## NOTIFICATIONS

| Field     | Type      | Description              |
| --------- | --------- | ------------------------ |
| id        | string    | Primary Key (UUID)       |
| userId    | string    | FK đến USERS             |
| type      | enum      | NotificationType         |
| title     | string    | Tiêu đề                  |
| body      | string    | Nội dung                 |
| data      | object?   | Extra data               |
| isRead    | boolean   | Đã đọc?                  |
| createdAt | timestamp | Thời điểm tạo            |

---

## USER_FAVORITES

> **Document ID = {userId}_{productId}**

| Field        | Type      | Description          |
| ------------ | --------- | -------------------- |
| id           | string    | = userId_productId   |
| userId       | string    | FK đến USERS         |
| productId    | string    | FK đến PRODUCTS      |
| productName  | string    | Denormalized         |
| productPrice | number    | Denormalized         |
| productImage | string?   | Denormalized         |
| shopId       | string    | Denormalized         |
| shopName     | string    | Denormalized         |
| createdAt    | timestamp | Thời điểm tạo        |

---

## SHIPPER_APPLICATIONS

| Field        | Type      | Description                    |
| ------------ | --------- | ------------------------------ |
| id           | string    | Primary Key (UUID)             |
| shipperId    | string    | FK đến USERS                   |
| shipperName  | string    | Denormalized                   |
| shipperPhone | string    | Denormalized                   |
| shipperEmail | string    | Denormalized                   |
| shopId       | string    | FK đến SHOPS                   |
| shopName     | string    | Denormalized                   |
| status       | enum      | PENDING, APPROVED, REJECTED    |
| note         | string?   | Ghi chú từ shipper             |
| reviewedBy   | string?   | User ID đã review              |
| reviewedAt   | timestamp?| Thời điểm review               |
| rejectReason | string?   | Lý do từ chối                  |
| createdAt    | timestamp | Thời điểm tạo                  |
| updatedAt    | timestamp | Thời điểm cập nhật cuối        |

---

## VOUCHERS

| Field         | Type      | Description                    |
| ------------- | --------- | ------------------------------ |
| id            | string    | Primary Key (UUID)             |
| ownerType     | enum      | ADMIN, SHOP                    |
| shopId        | string?   | FK đến SHOPS (nếu shop voucher)|
| code          | string    | Unique, mã voucher             |
| name          | string    | Tên voucher                    |
| description   | string?   | Mô tả                          |
| discountType  | enum      | PERCENTAGE, FIXED_AMOUNT       |
| discountValue | number    | Giá trị giảm                   |
| maxDiscountAmount | number | Giảm tối đa (VND)             |
| minOrderAmount| number    | Đơn tối thiểu (VND)            |
| targetType    | enum      | ALL, NEW_CUSTOMER, LOYALTY     |
| maxUsesPerUser| number    | Số lần dùng/user               |
| maxTotalUses  | number?   | Tổng số lần dùng               |
| usedCount     | number    | Đã dùng bao nhiêu lần          |
| startDate     | timestamp | Bắt đầu hiệu lực               |
| endDate       | timestamp | Kết thúc hiệu lực              |
| isActive      | boolean   | Đang hoạt động?                |
| createdAt     | timestamp | Thời điểm tạo                  |
| updatedAt     | timestamp | Thời điểm cập nhật cuối        |

---

## ENUMS

### UserRole
- **CUSTOMER**: Khách hàng đặt hàng
- **OWNER**: Chủ shop
- **SHIPPER**: Người giao hàng
- **ADMIN**: Quản trị viên

### UserStatus
- **PENDING**: Chờ xác thực
- **ACTIVE**: Đang hoạt động
- **SUSPENDED**: Bị khóa
- **DELETED**: Đã xóa

### ShopStatus
- **OPEN**: Đang mở cửa
- **CLOSED**: Đóng cửa
- **SUSPENDED**: Bị khóa

### OrderStatus
- **PENDING**: Chờ xác nhận
- **CONFIRMED**: Đã xác nhận
- **PREPARING**: Đang chuẩn bị
- **READY_FOR_PICKUP**: Sẵn sàng giao
- **ASSIGNED**: Shipper đã nhận
- **PICKED_UP**: Đã lấy hàng
- **DELIVERING**: Đang giao
- **DELIVERED**: Đã giao
- **CANCELLED**: Đã hủy

### PaymentMethod
- **COD**: Tiền mặt khi nhận hàng
- **ZALOPAY**: Ví điện tử ZaloPay
- **MOMO**: Ví điện tử MoMo
- **SEPAY**: Chuyển khoản ngân hàng

### PaymentStatus
- **PENDING**: Chờ thanh toán
- **PAID**: Đã thanh toán
- **FAILED**: Thất bại
- **EXPIRED**: Hết hạn
- **REFUNDED**: Đã hoàn tiền

---

## INDEXES

```javascript
// users
users: email ASC (unique)
users: role, status

// shops
shops: ownerId
shops: status, subscription.status

// products
products: shopId, isAvailable, sortOrder ASC
products: categoryId, isAvailable

// orders
orders: customerId, createdAt DESC
orders: shopId, status, createdAt DESC
orders: shipperId, status

// userFavorites
userFavorites: userId, createdAt DESC

// categories
categories: isActive, sortOrder ASC
categories: slug ASC (unique)
```

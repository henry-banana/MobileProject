# Shops Module Guide - KTX Delivery Backend

> **Module:** Shop Management  
> **Base Path:** `/shops`  
> **Status:** ✅ IMPLEMENTED

---

## 1. Overview

Module Shops quản lý toàn bộ thông tin, đăng ký, cập nhật, và vận hành shop trong hệ thống:

| Feature         | Endpoints | Description                       |
|-----------------|-----------|-----------------------------------|
| Register        | 1         | Đăng ký shop mới                  |
| Get/List        | 2         | Lấy thông tin, danh sách shop     |
| Update          | 1         | Cập nhật thông tin shop           |
| Owner Approval  | 1         | Chủ shop duyệt shipper            |
| Revenue         | 1         | Xem doanh thu shop                |
| Products        | N/A       | Quản lý sản phẩm (module riêng)   |

**Total: 5+ endpoints**

---

## 1.1 Data Models

### Shop Entity
```json
{
  "id": "shop_abc123",
  "name": "Hiệp Thập Cẩm",
  "ownerId": "owner_123",
  "address": "Tòa B5, KTX Khu B",
  "phone": "0901234567",
  "status": "ACTIVE", // ACTIVE | INACTIVE | DELETED
  "avatarUrl": "...",
  "rating": 4.8,
  "totalRatings": 120,
  "createdAt": "2026-01-29T10:00:00Z",
  "updatedAt": "2026-01-29T12:00:00Z"
}
```

### Owner Entity (liên kết shop)
```json
{
  "id": "owner_123",
  "displayName": "Nguyễn Văn Chủ",
  "email": "owner1@test.com",
  "role": "OWNER",
  "shopId": "shop_abc123"
}
```

### ShopStatus Enum
| Status   | Ý nghĩa                |
|----------|------------------------|
| ACTIVE   | Shop hoạt động         |
| INACTIVE | Shop tạm ngưng         |

  ## 2. API Endpoints Reference

  ### 2.1. Register Shop (Owner)
  ```http
  POST /api/shops
  Authorization: Bearer <ID_TOKEN>
  Content-Type: application/json

  {
    "name": "Hiệp Thập Cẩm",
    "address": "Tòa B5, KTX Khu B",
    "phone": "0901234567"
  }
  ```
  **Response:**
  ```json
  {
    "success": true,
    "data": {
      "id": "shop_abc123",
      "name": "Hiệp Thập Cẩm",
      "ownerId": "owner_123"
    }
  }
  ```
  **Validation:**
  - Owner chưa có shop
  - Tên shop không trùng
  - Địa chỉ, phone hợp lệ

  ---

  ### 2.2. Get Shop List (Public)
  ```http
  GET /api/shops?page=1&limit=20&search=hiệp
  ```
  **Response:**
  ```json
  {
    "success": true,
    "data": [
      {
        "id": "shop_abc123",
        "name": "Hiệp Thập Cẩm",
        "address": "Tòa B5, KTX Khu B",
        "rating": 4.8
      }
    ],
    "pagination": { "page": 1, "limit": 20, "total": 5 }
  }
  ```

  ---

  ### 2.3. Get Shop Detail (Public)
  ```http
  GET /api/shops/{id}
  ```
  **Response:**
  ```json
  {
    "success": true,
    "data": {
      "id": "shop_abc123",
      "name": "Hiệp Thập Cẩm",
      "address": "Tòa B5, KTX Khu B",
      "products": [...],
      "reviews": [...]
    }
  }
  ```

  ---

  ### 2.4. Update Shop (Owner)
  ```http
  PUT /api/shops/{id}
  Authorization: Bearer <ID_TOKEN>
  Content-Type: application/json
  {
    "name": "Hiệp Thập Cẩm 2",
    "address": "Tòa B6, KTX Khu B",
    "phone": "0901234568"
  }
  ```
  **Validation:**
  - Chỉ owner shop được update
  - Không update khi status = DELETED

  ---

  ### 2.5. Approve Shipper (Owner)
  ```http
  PATCH /api/shipper-applications/{id}/approve
  Authorization: Bearer <ID_TOKEN>
  ```
  **Response:**
  ```json
  {
    "success": true,
    "data": { "shipperId": "shipper_123", "shopId": "shop_abc123" }
  }
  ```

  ---

  ### 2.6. Delete Shop (Owner)
  ```http
  DELETE /api/shops/{id}
  Authorization: Bearer <ID_TOKEN>
  ```
  **Validation:**
  - Không còn đơn hàng đang xử lý
  - Chỉ owner được xóa

  ---

  ### 2.7. Get Shop Revenue (Owner)
  ```http
  GET /api/shops/{id}/revenue?period=month
  Authorization: Bearer <ID_TOKEN>
  ```
  **Response:**
  ```json
  {
    "today": 550000,
    "month": 12000000,
    "year": 25000000,
    "all": 25000000,
    "dailyBreakdown": [ ... ]
  }
  ```

  ---

### Liên kết Product/Order/Review
- Shop có nhiều Product (products[])
- Shop có nhiều Order (orders[])
- Shop có nhiều Review (reviews[])

---

## 1.2 Backend Flow

### Tạo shop mới
1. Owner đăng nhập, gọi POST /shops
2. Backend kiểm tra owner đã có shop chưa
3. Validate tên, địa chỉ, phone, trùng tên
4. Tạo shop, gán shopId cho owner
5. Trả về thông tin shop

### Cập nhật shop
1. Owner gọi PUT /shops/{id}
2. Backend kiểm tra quyền owner, validate input
3. Cập nhật thông tin shop

### Xoá shop (nếu có)
1. Kiểm tra không còn đơn hàng đang xử lý
2. Đánh dấu status = DELETED


  ---

### Chủ shop duyệt shipper

  ### 4.1. Shop Registration Flow
  1. Owner đăng nhập → vào màn hình "Tạo Shop"
  2. Nhập tên, địa chỉ, SĐT → Submit
  3. Backend validate, tạo shop, trả về thông tin
  4. Chuyển sang màn hình quản lý shop

  ### 4.2. Shop Management Flow
  1. Owner vào "Quản lý Shop"
  2. Xem danh sách sản phẩm, đơn hàng, doanh thu
  3. Cập nhật thông tin shop, duyệt shipper
  4. Xem đánh giá, phản hồi khách hàng

  ### 4.3. UI Mockup: Shop Home
  ```
  ┌───────────────────────────────┐
  │  [Tạo Shop]                   │
  │  Nhập tên, địa chỉ, SĐT       │
  │  [Submit]                     │
  │  → [Shop Home]                │
  │  [Cập nhật Shop]              │
  │  [Duyệt Shipper]              │
  │  [Xem doanh thu]              │
  │  [Xem đánh giá]               │
  └───────────────────────────────┘
  ```

  ### 4.4. UI Mockup: Shop List
  ```
  ┌───────────────────────────────┐
  │  Danh sách Shop               │
  │  ───────────────────────────  │
  │  Hiệp Thập Cẩm                │
  │  Tòa B5, KTX Khu B            │
  │  ⭐ 4.8 (120 đánh giá)         │
  │  [Xem chi tiết]               │
  └───────────────────────────────┘
  ```

  ---

1. Owner gọi PATCH /shipper-applications/{id}/approve

  | Code        | Status | Message                        | Mô tả                        |
  |-------------|--------|--------------------------------|------------------------------|
  | SHOP_001    | 409    | Shop name already exists       | Trùng tên shop               |
  | SHOP_002    | 403    | Not owner of this shop         | Không phải chủ shop          |
  | SHOP_003    | 404    | Shop not found                 | Shop không tồn tại           |
  | SHOP_004    | 409    | Shop has active orders         | Không thể xoá shop           |
  | SHOP_005    | 400    | Invalid phone/address/name     | Validate input               |

  **Negative Test Checklist:**
  - [ ] Tạo shop trùng tên
  - [ ] Cập nhật shop không phải owner
  - [ ] Xoá shop khi còn đơn hàng
  - [ ] Gọi API với status = DELETED
  - [ ] Gọi API khi shop INACTIVE

  ---

2. Backend kiểm tra quyền owner, trạng thái đơn

  - [ ] Đăng ký shop mới (role OWNER)
  - [ ] Lấy danh sách shop
  - [ ] Lấy thông tin shop cụ thể
  - [ ] Cập nhật thông tin shop
  - [ ] Chủ shop duyệt shipper
  - [ ] Test cập nhật shop không phải owner (bắt lỗi 403)
  - [ ] Test tạo shop trùng tên (bắt lỗi 409)
  - [ ] Test xóa shop khi còn đơn hàng (bắt lỗi 409)

  ### cURL Example
  ```bash
  # Lấy danh sách shop
  curl -X GET http://localhost:3000/api/shops

  # Đăng ký shop
  curl -X POST http://localhost:3000/api/shops \
    -H "Authorization: Bearer <ID_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{"name":"Hiệp Thập Cẩm","address":"Tòa B5, KTX Khu B","phone":"0901234567"}'

  # Cập nhật shop
  curl -X PUT http://localhost:3000/api/shops/shop_abc123 \
    -H "Authorization: Bearer <ID_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{"name":"Hiệp Thập Cẩm 2","address":"Tòa B6, KTX Khu B","phone":"0901234568"}'
  ```

  ---

3. Gán shipper vào shop

  - Chỉ OWNER mới được tạo/cập nhật shop
  - Một owner chỉ có 1 shop
  - Validate phone, address, name khi tạo/cập nhật
  - Không cho phép xoá shop nếu còn đơn hàng đang xử lý
  - Không cho phép cập nhật shop khi status = DELETED
  - Khi tích hợp frontend:
    - Luôn kiểm tra trạng thái shop, quyền owner
    - Hiển thị rõ các trạng thái: ACTIVE, INACTIVE, DELETED
    - Khi cập nhật shop, validate input phía client trước khi gửi API
    - Khi hiển thị rating, lấy từ trường shop.rating, shop.totalRatings

  ---



  **Q: Một owner có thể tạo nhiều shop không?**
  A: Không. Mỗi owner chỉ có 1 shop.

  **Q: Shop bị xoá có khôi phục được không?**
  A: Không. Shop DELETED sẽ bị ẩn hoàn toàn.

  **Q: Làm sao lấy doanh thu shop?**
  A: Gọi GET /api/shops/{id}/revenue với Bearer token owner.

  **Q: Shop có thể tạm ngưng hoạt động không?**
  A: Có. Đổi status = INACTIVE, khách không đặt được hàng.

  **Q: Làm sao xem đánh giá shop?**
  A: Gọi GET /api/reviews/shop/{shopId} (public).

  **Q: Shop có thể đổi owner không?**
  A: Hiện tại không hỗ trợ chuyển owner.

  **Q: Khi nào shop bị xoá vĩnh viễn?**
  A: Khi owner xoá và không còn đơn hàng active.

  ---

---

  - [PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md](PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md) — Đánh giá shop, quản lý shipper rời shop
  - [USER_GUIDE.md](USER_GUIDE.md) — Quản lý chủ shop
  - [ADMIN_GUIDE.md](ADMIN_GUIDE.md) — Quản trị viên duyệt shop
  - [AUTH_GUIDE.md](AUTH_GUIDE.md) — Đăng nhập, xác thực
  - [SHIPPER_REVENUE_API_GUIDE.md](SHIPPER_REVENUE_API_GUIDE.md) — Doanh thu shipper

  ---


  Gặp vấn đề? Kiểm tra:
  1. Backend logs: Terminal đang chạy `npm start`
  2. Firebase Console: Authentication & Firestore tabs
  3. Swagger docs: http://localhost:3000/api/docs
  4. Issue tracker: GitHub repository
---

## 2. Data Models

### Shop Entity
```json
{
  "id": "shop_abc123",
  "name": "Hiệp Thập Cẩm",
  "ownerId": "owner_123",
  "address": "Tòa B5, KTX Khu B",
  "phone": "0901234567",
  "status": "ACTIVE", // ACTIVE | INACTIVE | DELETED
  "avatarUrl": "...",
  "createdAt": "2026-01-29T10:00:00Z",
  "updatedAt": "2026-01-29T12:00:00Z"
}
```

---

## 3. Authentication

Tất cả endpoints (trừ GET /shops) yêu cầu Firebase ID Token:

```http
Authorization: Bearer <firebase-id-token>
```

---

## 4. API Endpoints Reference

### 4.1. Đăng ký shop mới
```http
POST /api/shops
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "name": "Hiệp Thập Cẩm",
  "address": "Tòa B5, KTX Khu B",
  "phone": "0901234567"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "shop_abc123",
    "name": "Hiệp Thập Cẩm",
    "ownerId": "owner_123"
  }
}
```

### 4.2. Lấy danh sách shop
```http
GET /api/shops
```

**Response:**
```json
{
  "success": true,
  "data": {
    "shops": [
      {
        "id": "shop_abc123",
        "name": "Hiệp Thập Cẩm",
        "address": "Tòa B5, KTX Khu B"
      }
    ]
  }
}
```

### 4.3. Lấy thông tin shop cụ thể
```http
GET /api/shops/{id}
```

### 4.4. Cập nhật thông tin shop
```http
PUT /api/shops/{id}
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "name": "Hiệp Thập Cẩm - Update",
  "address": "Tòa B5, KTX Khu B",
  "phone": "0901234567"
}
```

### 4.5. Chủ shop duyệt shipper
```http
PATCH /api/shipper-applications/{id}/approve
Authorization: Bearer <ID_TOKEN>
```

---

## 5. Owner & Shop Status

- **role: "OWNER"**: Có thể tạo/cập nhật shop, duyệt shipper, xem doanh thu shop.
- **shop.status**:
  - `ACTIVE`: Shop hoạt động bình thường
  - `INACTIVE`: Shop tạm ngưng
  - `DELETED`: Shop đã xoá

---

## 6. Testing Checklist

## 6.1. Testing Checklist

- [ ] Đăng ký shop mới (role OWNER)
- [ ] Lấy danh sách shop
- [ ] Lấy thông tin shop cụ thể
- [ ] Cập nhật thông tin shop
- [ ] Chủ shop duyệt shipper
- [ ] Test cập nhật shop không phải owner (bắt lỗi 403)
- [ ] Test tạo shop trùng tên (bắt lỗi 409)
- [ ] Test xóa shop khi còn đơn hàng (bắt lỗi 409)

## 6.2. Testing với Swagger UI

1. Mở http://localhost:3000/api/docs
2. Click Authorize, nhập Bearer <ID_TOKEN>
3. Test lần lượt các endpoint:
  - POST /shops
  - GET /shops
  - GET /shops/{id}
  - PUT /shops/{id}
  - PATCH /shipper-applications/{id}/approve
4. Xem response, kiểm tra trạng thái, message, error code

## 6.3. Test Accounts (mẫu)

| Email           | Password   | Role   | Status  | Note         |
|-----------------|------------|--------|---------|--------------|
| owner1@test.com | Test123!   | OWNER  | ACTIVE  | Chủ shop     |
| admin1@test.com | Test123!   | ADMIN  | ACTIVE  | Quản trị viên|

> Xem thêm: [TEST_ACCOUNTS.md](TEST_ACCOUNTS.md)

- [ ] Đăng ký shop mới (role OWNER)
- [ ] Lấy danh sách shop
- [ ] Lấy thông tin shop cụ thể
- [ ] Cập nhật thông tin shop
- [ ] Chủ shop duyệt shipper
- [ ] Test cập nhật shop không phải owner (bắt lỗi 403)
- [ ] Test tạo shop trùng tên (bắt lỗi 409)

---

## 7. Testing với cURL

```bash
# Đăng ký shop mới
curl -X POST http://localhost:3000/api/shops \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Hiệp Thập Cẩm","address":"Tòa B5, KTX Khu B","phone":"0901234567"}'

# Lấy danh sách shop
curl -X GET http://localhost:3000/api/shops

# Lấy thông tin shop cụ thể
curl -X GET http://localhost:3000/api/shops/shop_abc123

# Cập nhật shop
curl -X PUT http://localhost:3000/api/shops/shop_abc123 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Hiệp Thập Cẩm - Update","address":"Tòa B5, KTX Khu B","phone":"0901234567"}'
```

---

## 8. Negative Test Cases

## 8.1. Error Codes

| Code        | Status | Message                        | Mô tả                        |
|-------------|--------|--------------------------------|------------------------------|
| SHOP_001    | 409    | Shop name already exists       | Trùng tên shop               |
| SHOP_002    | 403    | Not owner of this shop         | Không phải chủ shop          |
| SHOP_003    | 404    | Shop not found                 | Shop không tồn tại           |
| SHOP_004    | 409    | Shop has active orders         | Không thể xoá shop           |
| SHOP_005    | 400    | Invalid phone/address/name     | Validate input               |

---

- Tạo shop trùng tên → 409 Conflict
- Cập nhật shop không phải owner → 403 Forbidden
- Lấy shop không tồn tại → 404 Not Found

---

## 9. UI/UX Mockup & Logic

### 9.1. UI State Table

| Trạng thái | UI/UX | Quyền truy cập |
|------------|-------|----------------|
| ACTIVE     | Home shop, quản lý    | Đầy đủ quyền owner |
| INACTIVE   | Thông báo tạm ngưng   | Chỉ xem, không chỉnh sửa |
| DELETED    | Ẩn khỏi danh sách     | Không truy cập |

### 9.2. UI Mockup: Shop Home

```
┌───────────────────────────────┐
│  [Tạo Shop]                   │
│  Nhập tên, địa chỉ, SĐT       │
│  [Submit]                     │
│  → [Shop Home]                │
│  [Cập nhật Shop]              │
│  [Duyệt Shipper]              │
│  [Xem doanh thu]              │
└───────────────────────────────┘
```

### 9.3. UI Mockup: Shop List

```
┌───────────────────────────────┐
│  Danh sách Shop               │
│  ───────────────────────────  │
│  Hiệp Thập Cẩm                │
│  Tòa B5, KTX Khu B            │
│  ⭐ 4.8 (120 đánh giá)         │
│  [Xem chi tiết]               │
└───────────────────────────────┘
```

```
┌───────────────────────────────┐
│  [Tạo Shop]                   │
│  Nhập tên, địa chỉ, SĐT       │
│  [Submit]                     │
│  → [Shop Home]                │
│  [Cập nhật Shop]              │
│  [Duyệt Shipper]              │
└───────────────────────────────┘
```

---

## 10. Security & Best Practices

- Chỉ OWNER mới được tạo/cập nhật shop
- Một owner chỉ có 1 shop
- Validate phone, address, name khi tạo/cập nhật
- Không cho phép xoá shop nếu còn đơn hàng đang xử lý
- Không cho phép cập nhật shop khi status = DELETED
- Khi tích hợp frontend:
  - Luôn kiểm tra trạng thái shop, quyền owner
  - Hiển thị rõ các trạng thái: ACTIVE, INACTIVE, DELETED
  - Khi cập nhật shop, validate input phía client trước khi gửi API
  - Khi hiển thị rating, lấy từ trường shop.rating, shop.totalRatings

- Chỉ OWNER mới được tạo/cập nhật shop
- Một owner chỉ có 1 shop
- Validate phone, address, name khi tạo/cập nhật
- Không cho phép xoá shop nếu còn đơn hàng đang xử lý

---

## 11. FAQ

**Q: Một owner có thể có nhiều shop không?**  
A: Không. Mỗi owner chỉ có 1 shop.

**Q: Shop có thể bị xoá không?**  
A: Có, nhưng chỉ khi không còn đơn hàng đang xử lý.

**Q: Có thể đổi owner cho shop không?**  
A: Hiện tại chưa hỗ trợ chuyển quyền owner.

---

## 12. Related Files

- [shops.controller.ts](../../Backend/functions/src/modules/shops/shops.controller.ts)
- [shops.service.ts](../../Backend/functions/src/modules/shops/shops.service.ts)
- [shop.entity.ts](../../Backend/functions/src/modules/shops/entities/shop.entity.ts)
- [owner-shippers.controller.ts](../../Backend/functions/src/modules/shippers/owner-shippers.controller.ts)

- [PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md](PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md) — Đánh giá shop, quản lý shipper rời shop
- [USER_GUIDE.md](USER_GUIDE.md) — Quản lý chủ shop

---

## 13. Support

Gặp vấn đề? Check:
1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Firestore & Authentication
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository

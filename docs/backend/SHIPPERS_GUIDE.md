
# Shippers Module Guide - KTX Delivery Backend

> **Module:** Shipper Registration & Management  
> **Base Path:** `/shipper-applications`  
> **Status:** ✅ IMPLEMENTED

---

## 1. Overview

Module Shippers quản lý toàn bộ quy trình đăng ký, duyệt và vận hành shipper:

| Feature | Endpoints | Description |
|---------|-----------|-------------|
| Register | 1 | Đăng ký tài khoản SHIPPER |
| Apply | 2 | Nộp đơn, xem trạng thái đơn |
| Cancel | 1 | Hủy đơn PENDING |
| Owner Review | 3 | Xem, duyệt, từ chối đơn |
| Notifications | 1 | Nhận thông báo duyệt/từ chối |

**Total: 8 endpoints**

---

## 1.1 Data Models

### Shipper Entity
```json
{
  "id": "user_abc123",
  "displayName": "Nguyễn Văn A",
  "phone": "+84901234567",
  "role": "SHIPPER",
  "shipperInfo": {
    "shopId": "shop_xyz",
    "shopName": "Hiệp Thập Cẩm",
    "vehicleType": "MOTORBIKE",
    "vehicleNumber": "59A1-12345",
    "status": "AVAILABLE", // AVAILABLE | BUSY | OFFLINE
    "isOnline": false,
    "rating": 5.0,
    "totalDeliveries": 0,
    "currentOrders": [],
    "joinedAt": "2026-01-29T10:00:00Z"
  },
  "avatarUrl": "..."
}
```

### Shipper Application Entity
```json
{
  "id": "app_123",
  "userId": "user_abc123",
  "userName": "Nguyễn Văn A",
  "shopId": "shop_xyz",
  "shopName": "Hiệp Thập Cẩm",
  "vehicleType": "MOTORBIKE",
  "vehicleNumber": "59A1-12345",
  "idCardNumber": "012345678901",
  "idCardFrontUrl": "...",
  "idCardBackUrl": "...",
  "driverLicenseUrl": "...",
  "message": "Tôi muốn làm shipper",
  "status": "PENDING", // PENDING | APPROVED | REJECTED
  "createdAt": "2026-01-29T10:00:00Z",
  "reviewedBy": "owner_123",
  "reviewedAt": "2026-01-29T12:00:00Z",
  "rejectionReason": "Thiếu bằng lái xe"
}
```

### Notification Example
```json
{
  "userId": "user_abc123",
  "title": "Application Approved",
  "body": "Your application to be a shipper for Hiệp Thập Cẩm has been approved!",
  "type": "SHIPPER_APPLICATION_APPROVED",
  "data": {
    "applicationId": "app_123",
    "shopId": "shop_xyz"
  },
  "createdAt": "2026-01-29T12:01:00Z"
}
```

### Trip & TripOrder (GPS Module Integration)
```json
{
  "id": "trip_123",
  "shipperId": "user_abc123",
  "orders": [
    {
      "orderId": "order_1",
      "buildingCode": "B5",
      "stopIndex": 1,
      "tripDeliveryStatus": "SHIPPING" // SHIPPING | DELIVERED
    },
    {
      "orderId": "order_2",
      "buildingCode": "A2",
      "stopIndex": 2,
      "tripDeliveryStatus": "PENDING"
    }
  ],
  "route": {
    "distance": 2.5,
    "duration": 18,
    "polyline": "..."
  },
  "status": "STARTED" // PENDING | STARTED | FINISHED | CANCELLED
}
```

> Xem thêm: [GPS_FRONTEND_INTEGRATION_GUIDE.md](GPS_FRONTEND_INTEGRATION_GUIDE.md)

---

## 2. Role & Trạng thái Shipper

- **role: "SHIPPER"**: Có thể truy cập các API dành cho shipper, nhưng chưa chắc đã được gán vào shop nào.
- **shipperInfo.shopId**:
  - `null`: Chưa được duyệt vào shop nào, cần nộp đơn.
  - `"xxx"`: Đã được duyệt, có thể nhận đơn giao hàng.
- **Trạng thái đơn**: `PENDING`, `APPROVED`, `REJECTED`

---

## 3. Authentication

Tất cả endpoints (trừ đăng ký) yêu cầu Firebase ID Token:

```http
Authorization: Bearer <firebase-id-token>
```

---

## 4. Shipper Registration Flow

### 4.1 Đăng ký tài khoản

```http
POST /api/auth/register
Content-Type: application/json
{
  "email": "shipper@example.com",
  "password": "matkhau123",
  "displayName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "SHIPPER"
}
```

> Sau bước này, user có role SHIPPER nhưng chưa được gán vào shop nào.

---

### 4.1.1 Backend Flow: Đăng ký & Apply

1. User đăng ký tài khoản với role SHIPPER
2. User nộp đơn apply vào shop (POST /shipper-applications)
3. Backend kiểm tra:
  - User đã là shipper của shop khác chưa?
  - Đã có đơn PENDING cho shop này chưa?
  - Validate file ảnh, dung lượng, định dạng
4. Upload ảnh lên Firebase Storage
5. Tạo application (status = PENDING)
6. Gửi notification cho chủ shop

### 4.2 Nộp đơn xin làm shipper

```http
POST /api/shipper-applications
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data
```

| Field           | Type   | Required | Description                           |
| --------------- | ------ | -------- | ------------------------------------- |
| `shopId`        | string | ✅       | ID của shop muốn làm việc             |
| `vehicleType`   | enum   | ✅       | `MOTORBIKE`, `BICYCLE`, `WALKING`     |
| `vehicleNumber` | string | ✅       | Biển số xe (VD: 59A1-12345)           |
| `idCardNumber`  | string | ✅       | Số CCCD (12 số)                       |
| `idCardFront`   | file   | ✅       | Ảnh CCCD mặt trước (JPG/PNG, max 5MB) |
| `idCardBack`    | file   | ✅       | Ảnh CCCD mặt sau (JPG/PNG, max 5MB)   |
| `driverLicense` | file   | ✅       | Ảnh Bằng lái xe (JPG/PNG, max 5MB)    |
| `message`       | string | ❌       | Lời nhắn cho chủ shop                 |

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "...",
    "status": "PENDING"
  }
}
```

### 4.3 Xem trạng thái đơn

```http
GET /api/shipper-applications/me
Authorization: Bearer <ID_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "shopName": "...",
      "status": "PENDING"
    }
  ]
}
```

### 4.4 Hủy đơn

```http
DELETE /api/shipper-applications/:id
Authorization: Bearer <ID_TOKEN>
```

---

## 5. Owner Review Flow (OWNER)

### 5.1 Xem danh sách đơn
```http
GET /api/shipper-applications?status=PENDING
Authorization: Bearer <ID_TOKEN>
```

### 5.2 Duyệt đơn
```http
PATCH /api/shipper-applications/:id/approve
Authorization: Bearer <ID_TOKEN>
```
- User được gán vào shop, role chuyển thành SHIPPER, shipperInfo cập nhật.

#### Backend Flow: Approve Application
1. Owner gọi approve API
2. Backend kiểm tra quyền owner, trạng thái đơn
3. Transaction:
  - Update application status = APPROVED
  - Update user: role = SHIPPER, shipperInfo.shopId = ...
  - claimsSyncStatus = PENDING
4. Gọi Firebase setCustomUserClaims (role: SHIPPER)
5. Update claimsSyncStatus = OK (hoặc FAILED nếu lỗi)
6. Gửi notification cho shipper
7. Khởi tạo ví shipper (wallet)

### 5.3 Từ chối đơn
```http
PATCH /api/shipper-applications/:id/reject
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "reason": "Thiếu bằng lái xe"
}
```

---

## 6. Response Format

**Success:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Thành công"
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "SHIPPER_001",
    "message": "Bạn đã là shipper của một shop rồi"
  }
}
```

---

## 7. Error Codes

| Code          | Status | Message                            | Giải thích                                          |
| ------------- | ------ | ---------------------------------- | --------------------------------------------------- |
| SHIPPER_001   | 409    | Bạn đã là shipper của một shop rồi | User đã được gán vào shop (có `shipperInfo.shopId`) |
| SHIPPER_005   | 409    | Bạn đã nộp đơn cho shop này rồi    | Đã có đơn PENDING cho shop này                      |

---

## 8. Testing với cURL

## 8.1. Testing Checklist

- [ ] Đăng ký tài khoản SHIPPER (email, password, role)
- [ ] Nộp đơn vào shop (đủ giấy tờ, đúng định dạng)
- [ ] Xem trạng thái đơn (PENDING, APPROVED, REJECTED)
- [ ] Hủy đơn PENDING
- [ ] Chủ shop duyệt đơn (APPROVE)
- [ ] Chủ shop từ chối đơn (REJECT, nhập lý do)
- [ ] Shipper nhận được notification trạng thái đơn
- [ ] Đăng nhập lại sau khi được duyệt (kiểm tra quyền truy cập)
- [ ] Test upload ảnh sai định dạng/dung lượng (bắt lỗi)
- [ ] Test apply trùng shop (bắt lỗi SHIPPER_005)
- [ ] Test chủ shop tự duyệt mình (bắt lỗi)

## 8.2. Testing với Swagger UI

1. Mở http://localhost:3000/api/docs
2. Click Authorize, nhập Bearer <ID_TOKEN>
3. Test lần lượt các endpoint:
  - POST /auth/register
  - POST /shipper-applications
  - GET /shipper-applications/me
  - DELETE /shipper-applications/{id}
  - PATCH /shipper-applications/{id}/approve
  - PATCH /shipper-applications/{id}/reject
4. Xem response, kiểm tra trạng thái, message, error code

## 8.3. Test Accounts (mẫu)

| Email                  | Password   | Role     | Status  | Note                |
|------------------------|------------|----------|---------|---------------------|
| shipper1@test.com      | Test123!   | SHIPPER  | ACTIVE  | Shipper chưa apply  |
| owner1@test.com        | Test123!   | OWNER    | ACTIVE  | Chủ shop            |
| admin1@test.com        | Test123!   | ADMIN    | ACTIVE  | Quản trị viên       |

> Xem thêm: [TEST_ACCOUNTS.md](TEST_ACCOUNTS.md)

## 8.4. Negative Test Cases (Gợi ý test lỗi)

- Nộp đơn thiếu giấy tờ hoặc sai định dạng ảnh → 400 Bad Request
- Nộp đơn khi đã là shipper của shop khác → 409 SHIPPER_001
- Nộp đơn trùng shop (đã có đơn PENDING) → 409 SHIPPER_005
- Chủ shop tự duyệt mình làm shipper → 400 SHIPPER_BUG
- Duyệt đơn đã bị từ chối hoặc đã duyệt → 409 Conflict
- Hủy đơn không phải của mình → 403 Forbidden

```bash
# Đăng ký tài khoản SHIPPER
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"shipper@example.com","password":"matkhau123","displayName":"Nguyễn Văn A","phone":"0901234567","role":"SHIPPER"}'

# Nộp đơn
curl -X POST http://localhost:3000/api/shipper-applications \
  -H "Authorization: Bearer <token>" \
  -F "shopId=abc123" -F "vehicleType=MOTORBIKE" -F "vehicleNumber=59A1-12345" \
  -F "idCardNumber=012345678901" -F "idCardFront=@cccd_truoc.jpg" -F "idCardBack=@cccd_sau.jpg" -F "driverLicense=@banglai.jpg"

# Xem trạng thái đơn
curl -X GET http://localhost:3000/api/shipper-applications/me \
  -H "Authorization: Bearer <token>"

# Hủy đơn
curl -X DELETE http://localhost:3000/api/shipper-applications/<id> \
  -H "Authorization: Bearer <token>"

# Chủ shop duyệt đơn
curl -X PATCH http://localhost:3000/api/shipper-applications/<id>/approve \
  -H "Authorization: Bearer <token>"

# Chủ shop từ chối đơn
curl -X PATCH http://localhost:3000/api/shipper-applications/<id>/reject \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Thiếu bằng lái xe"}'
```

---

## 9. UI/UX Mockup & Logic

```
┌───────────────────────────────┐
│  Đăng ký Shipper              │
│  ↓                            │
│  Chọn Shop để apply           │
│  ↓                            │
│  Điền thông tin: CCCD, xe...  │
│  ↓                            │
│  [Submit Application]         │
│  ↓                            │
│  [Màn hình "Chờ duyệt"]      │
│  ↓                            │
│  (Owner duyệt)                │
│  ↓                            │
│  [Shipper Home - Xem đơn hàng]│
└───────────────────────────────┘
```

### 9.1 UI State Table

| Trạng thái | UI/UX | Quyền truy cập |
|------------|-------|----------------|
| NOT_SHIPPER | Đăng ký tài khoản | Không truy cập API shipper |
| NEED_TO_APPLY | Nộp đơn vào shop | Không nhận đơn hàng |
| PENDING_APPLICATION | Màn hình chờ duyệt | Không nhận đơn hàng |
| ACTIVE_SHIPPER | Home shipper, nhận đơn | Đầy đủ quyền shipper |

### 9.2 UI Mockup: Trạng thái đơn

```
┌───────────────────────────────┐
│  Đơn xin làm shipper          │
│  ───────────────────────────  │
│  Shop: Hiệp Thập Cẩm          │
│  Trạng thái: PENDING          │
│  [Huỷ đơn]                    │
└───────────────────────────────┘
```
```
┌───────────────────────────────┐
│  Đơn đã được duyệt!           │
│  Shop: Hiệp Thập Cẩm          │
│  [Vào Home Shipper]           │
└───────────────────────────────┘
```
```
┌───────────────────────────────┐
│  Đơn bị từ chối               │
│  Lý do: Thiếu bằng lái xe     │
│  [Nộp đơn lại]                │
└───────────────────────────────┘
```

**Logic kiểm tra trạng thái shipper:**

```javascript
async function checkShipperStatus(user) {
  if (user.role !== "SHIPPER") return "NOT_SHIPPER";
  if (user.shipperInfo?.shopId) return "ACTIVE_SHIPPER";
  const applications = await api.get("/shipper-applications/me");
  const pending = applications.find((a) => a.status === "PENDING");
  if (pending) return "PENDING_APPLICATION";
  return "NEED_TO_APPLY";
}
```

---

## 10. Security & Best Practices

- Ảnh upload chỉ nhận JPG/PNG, tối đa 5MB/file
- Một user chỉ có thể apply 1 shop tại 1 thời điểm
- Chủ shop không thể tự duyệt mình làm shipper
- Sau khi được duyệt, shipper phải re-login để cập nhật quyền

- Khi tích hợp frontend:
  - Luôn kiểm tra trạng thái shipper, trạng thái đơn, trạng thái trip.
  - Hiển thị rõ các trạng thái: PENDING, APPROVED, REJECTED, SHIPPING, DELIVERED.
  - Khi shipper có nhiều đơn, ưu tiên hiển thị theo stopIndex (từ GPS module).
  - Khi cập nhật phương tiện, validate đúng format biển số và loại xe (xem Vehicle API Guide).
  - Khi hiển thị doanh thu, dùng API wallets/revenue để lấy số liệu tổng hợp.

- Khi phát triển/tích hợp:
  - Đảm bảo cấu hình đúng các biến môi trường (.env): FIREBASE_PROJECT_ID, GOOGLE_APPLICATION_CREDENTIALS, GEMINI_API_KEY, ...
  - Đọc kỹ các docs liên quan để tránh lỗi tích hợp.

- Không lưu ảnh giấy tờ trên client, chỉ upload khi cần
- Kiểm tra claimsSyncStatus để debug lỗi phân quyền
- Sử dụng notification để báo trạng thái đơn cho user
- Tối ưu: cache trạng thái đơn, chỉ fetch lại khi có thay đổi

---

## 11. FAQ

**Q: Một user có thể apply nhiều shop cùng lúc không?**  
A: Không. Chỉ được apply 1 shop tại 1 thời điểm.

**Q: Chủ shop có thể tự duyệt mình làm shipper không?**  
A: Không. Hệ thống sẽ chặn thao tác này.

**Q: Sau khi được duyệt, shipper cần làm gì?**  
A: Đăng nhập lại để cập nhật quyền truy cập mới.

**Q: Ảnh upload bị lỗi?**  
A: Kiểm tra định dạng (JPG/PNG) và dung lượng (≤5MB).

**Q: Làm sao debug lỗi không nhận được quyền shipper?**
A: Kiểm tra trường claimsSyncStatus trong user, nếu FAILED thì cần re-login hoặc liên hệ admin để sync lại claims.

**Q: Shipper bị từ chối có thể nộp lại không?**
A: Có, sau khi đơn bị từ chối có thể nộp lại đơn mới cho shop khác hoặc cùng shop.

**Q: Có thể xem lịch sử đơn đã nộp không?**
A: Có, dùng API GET /shipper-applications/me để xem toàn bộ đơn đã nộp và trạng thái.

---

## 12. Related Files

- [shipper-applications.controller.ts](../../Backend/functions/src/modules/shippers/shipper-applications.controller.ts)
- [shippers.service.ts](../../Backend/functions/src/modules/shippers/shippers.service.ts)
- [owner-shippers.controller.ts](../../Backend/functions/src/modules/shippers/owner-shippers.controller.ts)
- [shipper-notifications.controller.ts](../../Backend/functions/src/modules/shippers/shipper-notifications.controller.ts)

- [GPS_FRONTEND_INTEGRATION_GUIDE.md](GPS_FRONTEND_INTEGRATION_GUIDE.md) — Hướng dẫn tích hợp GPS/trip cho shipper
- [REVENUE_API_QUICK_REFERENCE.md](REVENUE_API_QUICK_REFERENCE.md) — Tham khảo nhanh API doanh thu
- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) — Trạng thái các module backend

- [SHIPPER_REVENUE_API_GUIDE.md](SHIPPER_REVENUE_API_GUIDE.md) — Hướng dẫn doanh thu shipper
- [VEHICLE_API_GUIDE.md](VEHICLE_API_GUIDE.md) — Quản lý phương tiện shipper

---

## 13. Support

Gặp vấn đề? Check:
1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Authentication & Firestore tabs
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository

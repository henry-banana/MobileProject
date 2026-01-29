### Payout Entity
```json
{
  "id": "payout_abc123",
  "shipperId": "shipper_123",
  "amount": 500000,
  "status": "PENDING", // PENDING | APPROVED | REJECTED | COMPLETED
  "bankAccount": "0123456789",
  "bankName": "Vietcombank",
  "createdAt": "2026-01-29T12:00:00Z",
  "approvedAt": null,
  "rejectedAt": null
}
```

### RevenueStatus Enum
| Status     | Ý nghĩa                |
|------------|------------------------|
| PENDING    | Đang chờ xử lý         |
| APPROVED   | Đã duyệt               |
| REJECTED   | Đã từ chối             |
| COMPLETED  | Đã hoàn thành          |
### 4.4. Payout Flow (Shipper)
1. Shipper vào màn hình "Rút tiền"
2. Nhập số tiền, thông tin ngân hàng → Submit
3. Backend kiểm tra số dư, tạo payout status = PENDING
4. Admin duyệt payout (APPROVED/REJECTED)
5. Khi payout COMPLETED, số dư shipper trừ, lịch sử cập nhật
### 4.5. UI Mockup: Payout
```
┌───────────────────────────────┐
│  Số dư: 1,200,000đ            │
│  [Nhập số tiền]               │
│  [Chọn ngân hàng]             │
│  [Rút tiền]                   │
│  [Lịch sử rút tiền]           │
└───────────────────────────────┘
### 4.6. UI Mockup: Revenue Status Table
| Trạng thái | UI/UX | Quyền truy cập |
|------------|-------|----------------|
| PENDING    | Đang chờ xử lý        | Chủ shop, shipper, admin |
| APPROVED   | Đã duyệt, chờ chuyển  | Admin, shipper          |
| REJECTED   | Đã từ chối            | Admin, shipper          |
| COMPLETED  | Đã hoàn thành          | Admin, shipper          |
| REV_007     | 403    | Cannot approve own payout         | Admin không tự duyệt payout của mình |
| REV_008     | 409    | Payout already processed          | Payout đã được xử lý trước đó        |
| REV_009     | 400    | Invalid payout status transition  | Chuyển trạng thái payout không hợp lệ |
**Negative Test Checklist (bổ sung):**
- [ ] Duyệt payout đã COMPLETED/REJECTED
- [ ] Admin tự duyệt payout của mình
- [ ] Chuyển trạng thái payout không hợp lệ
- [ ] Duyệt payout (admin)
- [ ] Test duyệt payout đã COMPLETED/REJECTED (bắt lỗi)
- [ ] Test admin tự duyệt payout của mình (bắt lỗi)
### Test Data (mẫu)
| Shop Name      | Revenue (month) | Orders | Shipper Revenue |
|----------------|-----------------|--------|-----------------|
| Hiệp Thập Cẩm  | 12,000,000      | 120    | 1,200,000       |
| Quán ABC       | 8,000,000       | 80     | 900,000         |
### Test Accounts
| Email                | Role   | Password   | Note         |
|----------------------|--------|------------|--------------|
| owner1@test.com      | OWNER  | Test123!   | Chủ shop     |
| admin1@test.com      | ADMIN  | Test123!   | Quản trị viên|
| shipper1@test.com    | SHIPPER| Test123!   | Shipper      |
### Best Practices & Security
- Lưu log mọi trạng thái payout, revenue, ledger
- Không cho phép admin tự duyệt payout của mình
- Khi tích hợp frontend:
  - Hiển thị rõ trạng thái payout, revenue breakdown
  - Disable nút rút tiền nếu số dư không đủ
  - Validate input phía client trước khi gửi API
  - Hiển thị realtime khi payout được duyệt
**Q: Làm sao kiểm tra trạng thái payout?**
A: Xem trường status trong entity payout, hoặc gọi API /wallets/payouts.
**Q: Admin có thể duyệt payout của chính mình không?**
A: Không. Backend sẽ trả lỗi REV_007.
**Q: Làm sao lấy lịch sử payout?**
A: Gọi API /wallets/payouts (role SHIPPER/ADMIN).
**Q: Làm sao lấy doanh thu theo ngày cụ thể?**
A: Dùng dailyBreakdown, lọc theo date mong muốn.
**Q: Khi nào payout chuyển COMPLETED?**
A: Khi admin duyệt và chuyển khoản thành công.
## 9. Related Docs & Files
- [USER_GUIDE.md](USER_GUIDE.md) — Thông tin user liên quan revenue
- [CHAT_GUIDE.md](CHAT_GUIDE.md) — Tích hợp chat hỏi về payout

## 10. Support
6. Tham khảo thêm các file docs mẫu khác trong thư mục backend
# Revenue Module Guide - KTX Delivery Backend

## 1. Overview

Module Revenue quản lý các API liên quan đến doanh thu của shop, shipper, admin. Bao gồm thống kê doanh thu theo ngày/tuần/tháng/năm, chi tiết giao dịch, tổng hợp, breakdown, và các flow liên quan đến ví, rút tiền, đối soát.

| Feature         | Endpoints | Description                       |
|-----------------|-----------|-----------------------------------|
| Shop Revenue    | 1         | Thống kê doanh thu shop           |
| Shipper Revenue | 1         | Thống kê doanh thu shipper        |
| Ledger         | 1         | Lịch sử giao dịch chi tiết        |
| Withdraw        | 1         | Rút tiền từ ví                    |
| Admin Stats     | 1         | Tổng hợp doanh thu toàn hệ thống  |

**Total: 5+ endpoints**

---

## 2. API Endpoints Reference

### 2.1. Get Shop Revenue (Owner)
```http
GET /api/shops/{id}/revenue?period=month
Authorization: Bearer <ID_TOKEN>
```
**Query Parameters:**
- `period`: today | week | month | year | all (default: month)

**Response:**
```json
{
  "today": 550000,
  "week": 3500000,
  "month": 12000000,
  "year": 25000000,
  "all": 25000000,
  "dailyBreakdown": [
    { "date": "2026-01-23", "amount": 45000, "orderCount": 2 },
    { "date": "2026-01-24", "amount": 60000, "orderCount": 3 }
  ],
  "calculatedAt": "2026-01-29T10:00:00Z"
}
```

---

### 2.2. Get Shipper Revenue (Shipper)
```http
GET /api/wallets/revenue?period=month
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "today": 55000,
  "week": 350000,
  "month": 1200000,
  "year": 2500000,
  "all": 2500000,
  "dailyBreakdown": [ ... ],
  "calculatedAt": "2026-01-29T10:00:00Z"
}
```

---

### 2.3. Get Ledger History (Shop/Shipper)
```http
GET /api/wallets/ledger?page=1&limit=20
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "entries": [
    {
      "id": "ledger_abc123",
      "type": "CREDIT", // Thu nhập
      "amount": 25000,
      "balanceBefore": 100000,
      "balanceAfter": 125000,
      "orderId": "order_xyz",
      "orderNumber": "ORD-20260129-001",
      "description": "Phí giao đơn ORD-20260129-001",
      "createdAt": "2026-01-29T10:00:00Z"
    }
  ],
  "page": 1,
  "limit": 20,
  "total": 50,
  "totalPages": 3
}
```

---

### 2.4. Withdraw (Shipper)
```http
POST /api/wallets/payout
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "amount": 500000,
  "bankAccount": "0123456789",
  "bankName": "Vietcombank"
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "id": "payout_abc123",
    "amount": 500000,
    "status": "PENDING",
    "createdAt": "2026-01-29T12:00:00Z"
  }
}
```

---

### 2.5. Get Admin Revenue Stats (Admin)
```http
GET /api/admin/revenue?period=month
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "totalRevenue": 100000000,
  "byShop": [ { "shopId": "shop_abc123", "amount": 12000000 } ],
  "byShipper": [ { "shipperId": "shipper_123", "amount": 1200000 } ],
  "period": "month"
}
```

---

## 3. Data Model - Full Example

### Revenue Breakdown
```json
{
  "today": 550000,
  "week": 3500000,
  "month": 12000000,
  "year": 25000000,
  "all": 25000000,
  "dailyBreakdown": [
    { "date": "2026-01-23", "amount": 45000, "orderCount": 2 },
    { "date": "2026-01-24", "amount": 60000, "orderCount": 3 }
  ],
  "calculatedAt": "2026-01-29T10:00:00Z"
}
```

### Ledger Entry
```json
{
  "id": "ledger_abc123",
  "type": "CREDIT", // CREDIT | DEBIT
  "amount": 25000,
  "balanceBefore": 100000,
  "balanceAfter": 125000,
  "orderId": "order_xyz",
  "orderNumber": "ORD-20260129-001",
  "description": "Phí giao đơn ORD-20260129-001",
  "createdAt": "2026-01-29T10:00:00Z"
}
```

---

## 4. Flow thực tế & UI/UX

### 4.1. Shop Revenue Flow
1. Chủ shop vào dashboard → chọn khoảng thời gian
2. Gọi API /shops/{id}/revenue
3. Hiển thị tổng doanh thu, breakdown theo ngày/tháng
4. Xem chi tiết từng đơn hàng, từng giao dịch

### 4.2. Shipper Revenue Flow
1. Shipper vào màn hình "Doanh thu"
2. Gọi API /wallets/revenue
3. Hiển thị tổng doanh thu, breakdown, biểu đồ
4. Xem lịch sử giao dịch, rút tiền

### 4.3. UI Mockup: Revenue Dashboard
```
┌───────────────────────────────┐
│  Doanh thu tháng này: 12tr    │
│  [Biểu đồ doanh thu]          │
│  [Breakdown theo ngày]        │
│  [Lịch sử giao dịch]          │
└───────────────────────────────┘
```

---

## 5. Error Codes & Negative Test Cases

| Code        | Status | Message                        | Mô tả                        |
|-------------|--------|--------------------------------|------------------------------|
| REV_001     | 403    | Not owner/shipper/admin        | Không đủ quyền               |
| REV_002     | 400    | Invalid period                 | Sai tham số period           |
| REV_003     | 404    | Shop/Shipper not found         | Không tìm thấy               |
| REV_004     | 400    | Invalid amount                 | Số tiền rút không hợp lệ     |
| REV_005     | 409    | Insufficient balance           | Không đủ số dư               |
| REV_006     | 400    | Invalid bank info              | Thông tin ngân hàng sai      |

**Negative Test Checklist:**
- [ ] Gọi API revenue với role không hợp lệ
- [ ] Gọi với period không hợp lệ
- [ ] Rút tiền vượt quá số dư
- [ ] Rút tiền với thông tin ngân hàng sai

---

## 6. Testing Checklist & cURL

- [ ] Lấy doanh thu shop (role OWNER)
- [ ] Lấy doanh thu shipper (role SHIPPER)
- [ ] Lấy lịch sử giao dịch
- [ ] Rút tiền
- [ ] Test rút tiền vượt số dư (bắt lỗi)
- [ ] Test với period không hợp lệ (bắt lỗi)

### cURL Example
```bash
# Lấy doanh thu shop
curl -X GET "http://localhost:3000/api/shops/shop_abc123/revenue?period=month" -H "Authorization: Bearer <ID_TOKEN>"

# Lấy doanh thu shipper
curl -X GET "http://localhost:3000/api/wallets/revenue?period=month" -H "Authorization: Bearer <ID_TOKEN>"

# Lấy lịch sử giao dịch
curl -X GET "http://localhost:3000/api/wallets/ledger?page=1&limit=20" -H "Authorization: Bearer <ID_TOKEN>"

# Rút tiền
curl -X POST http://localhost:3000/api/wallets/payout \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"amount":500000,"bankAccount":"0123456789","bankName":"Vietcombank"}'
```

---

## 7. Best Practices & Security

- Chỉ owner/shipper/admin đúng quyền mới được truy cập API
- Validate period, amount, bank info kỹ càng
- Không cho phép rút tiền vượt số dư
- Lưu log mọi giao dịch, payout, revenue breakdown
- Khi tích hợp frontend:
  - Hiển thị rõ breakdown, trạng thái payout
  - Validate input phía client trước khi gửi API
  - Hiển thị số dư, doanh thu, lịch sử giao dịch realtime nếu có

---

## 8. FAQ & Real-world Issues

**Q: Làm sao lấy doanh thu từng ngày/tháng?**
A: Dùng trường dailyBreakdown trong response.

**Q: Làm sao lấy doanh thu toàn hệ thống?**
A: Gọi API /admin/revenue (role ADMIN).

**Q: Khi nào shipper/shop nhận được tiền?**
A: Khi payout được admin duyệt hoặc tự động xử lý thành công.

**Q: Có thể xem chi tiết từng giao dịch không?**
A: Có. Xem API /wallets/ledger.

**Q: Làm sao lấy doanh thu theo sản phẩm?**
A: Hiện tại chưa hỗ trợ, cần tổng hợp từ order hoặc backend mở rộng.

---

## 9. Related Docs & Files

- [SHIPPER_REVENUE_API_GUIDE.md](SHIPPER_REVENUE_API_GUIDE.md) — Doanh thu shipper
- [SHOPS_GUIDE.md](SHOPS_GUIDE.md) — Doanh thu shop
- [PRODUCTS_GUIDE.md](PRODUCTS_GUIDE.md) — Sản phẩm liên quan doanh thu
- [ADMIN_GUIDE.md](ADMIN_GUIDE.md) — Thống kê admin
- [PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md](PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md) — Đánh giá ảnh hưởng doanh thu

---

## 10. Support
Gặp vấn đề? Kiểm tra:
1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Authentication & Firestore tabs
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository
5. Tham khảo thêm các file docs mẫu khác trong thư mục backend

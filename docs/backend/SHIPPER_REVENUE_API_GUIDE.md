# Shipper Revenue/Earnings API - Backend Clarification

> **GIẢI THÍCH:** Sự khác biệt giữa "Số dư ví" và "Doanh thu"

## TL;DR

- ✅ **SHIPPER có quyền** xem wallet và ledger
- ✅ `GET /api/wallets/me` → Số dư hiện tại + Tổng doanh thu
- ✅ `GET /api/wallets/revenue` → **[MỚI]** Doanh thu đã tính toán sẵn theo ngày/tuần/tháng/năm
- ✅ `GET /api/wallets/ledger` → Lịch sử giao dịch chi tiết (nếu cần raw data)

---

## API 1: Get Wallet Info

```http
GET /api/wallets/me
Authorization: Bearer <firebase-token>
```

**Response:**

```json
{
  "wallet": {
    "id": "wallet_shipper_uid_123",
    "type": "SHIPPER",
    "balance": 500000, // ← SỐ DƯ hiện tại (còn lại trong ví)
    "totalEarned": 2500000, // ← TỔNG DOANH THU từ trước đến giờ
    "totalWithdrawn": 2000000, // ← Tổng đã rút ra
    "createdAt": "2026-01-10T00:00:00Z",
    "updatedAt": "2026-01-29T10:00:00Z"
  }
}
```

**Công thức:**

```
balance = totalEarned - totalWithdrawn
500,000 = 2,500,000 - 2,000,000
```

---

## API 2: Get Ledger History (Chi tiết doanh thu)

```http
GET /api/wallets/ledger?page=1&limit=20
Authorization: Bearer <firebase-token>
```

**Response:**

```json
{
  "entries": [
    {
      "id": "ledger_abc123",
      "type": "CREDIT", // ← THU NHẬP (doanh thu từ giao đơn)
      "amount": 25000,
      "balanceBefore": 100000,
      "balanceAfter": 125000,
      "orderId": "order_xyz",
      "orderNumber": "ORD-20260129-001",
      "description": "Phí giao đơn ORD-20260129-001",
      "createdAt": "2026-01-29T10:00:00Z"
    },
    {
      "id": "ledger_def456",
      "type": "CREDIT",
      "amount": 30000,
      "balanceBefore": 125000,
      "balanceAfter": 155000,
      "orderId": "order_abc",
      "orderNumber": "ORD-20260129-002",
      "description": "Phí giao đơn ORD-20260129-002",
      "createdAt": "2026-01-29T14:30:00Z"
    },
    {
      "id": "ledger_ghi789",
      "type": "DEBIT", // ← RÚT TIỀN (không phải doanh thu)
      "amount": -100000,
      "balanceBefore": 155000,
      "balanceAfter": 55000,
      "description": "Rút tiền về tài khoản ngân hàng",
      "createdAt": "2026-01-29T18:00:00Z"
    }
  ],
  "page": 1,
  "limit": 20,
  "total": 50,
  "totalPages": 3
}
```

**Entry Types:**

- `CREDIT` = Thu nhập (tiền vào ví từ giao đơn hàng) → **TÍNH VÀO DOANH THU**
- `DEBIT` = Tiền ra (rút tiền, phạt, etc.) → **KHÔNG PHẢI DOANH THU**

> **Lưu ý:** Ledger API trả về raw data. Nếu chỉ cần doanh thu tổng hợp, dùng API 3 (Get Revenue Stats) để backend tính toán sẵn.

---

## API 3: Get Revenue Stats 🆕 **[RECOMMENDED]**

```http
GET /api/wallets/revenue?period=month
Authorization: Bearer <firebase-token>
```

**Query Parameters:**

| Parameter | Type   | Values                                  | Default | Description                     |
| --------- | ------ | --------------------------------------- | ------- | ------------------------------- |
| period    | string | `today`, `week`, `month`, `year`, `all` | `month` | Khoảng thời gian tính doanh thu |

**Response:**

```json
{
  "today": 55000, // Doanh thu hôm nay
  "week": 350000, // Doanh thu tuần này (Thứ 2 - Chủ nhật)
  "month": 1200000, // Doanh thu tháng này
  "year": 2500000, // Doanh thu năm nay
  "all": 2500000, // Tổng doanh thu từ trước đến giờ
  "dailyBreakdown": [
    // Chi tiết theo ngày (last 7/30 days tùy period)
    {
      "date": "2026-01-23", // YYYY-MM-DD
      "amount": 45000,
      "orderCount": 2 // Số đơn giao thành công
    },
    {
      "date": "2026-01-24",
      "amount": 60000,
      "orderCount": 3
    },
    {
      "date": "2026-01-25",
      "amount": 0,
      "orderCount": 0
    },
    {
      "date": "2026-01-26",
      "amount": 75000,
      "orderCount": 3
    },
    {
      "date": "2026-01-27",
      "amount": 50000,
      "orderCount": 2
    },
    {
      "date": "2026-01-28",
      "amount": 65000,
      "orderCount": 2
    },
    {
      "date": "2026-01-29",
      "amount": 55000,
      "orderCount": 2
    }
  ],
  "calculatedAt": "2026-01-29T10:00:00Z"
}
```

**Daily Breakdown Logic:**

| Period  | Daily Breakdown                     |
| ------- | ----------------------------------- |
| `today` | Last 7 days (daily)                 |
| `week`  | Last 7 days (daily)                 |
| `month` | Last 30 days (daily)                |
| `year`  | Last 12 months (monthly: `2026-01`) |
| `all`   | Last 30 days (daily)                |

**Ưu điểm API này:**

- ✅ Backend tính toán sẵn → Frontend chỉ việc display
- ✅ Giảm data transfer (quan trọng trên mobile)
- ✅ Không cần filter/group entries ở frontend
- ✅ Có breakdown sẵn cho chart
- ✅ Nhất quán business logic (backend control)

**Example Usage:**

```bash
# Doanh thu hôm nay
curl -X GET "https://api.example.com/wallets/revenue?period=today" \
  -H "Authorization: Bearer TOKEN"

# Doanh thu tuần này
curl -X GET "https://api.example.com/wallets/revenue?period=week" \
  -H "Authorization: Bearer TOKEN"

# Doanh thu tháng này (default)
curl -X GET "https://api.example.com/wallets/revenue" \
  -H "Authorization: Bearer TOKEN"
```

---

## Frontend Logic: Display Revenue (Simplified) 🎯

### Option 1: Dùng API Revenue (RECOMMENDED)

```kotlin
// Chỉ cần 1 API call, backend tính sẵn
suspend fun getRevenueStats(): RevenueStats {
    return walletApi.getRevenue(period = "month")
}

// Display trực tiếp
Text("Hôm nay: ${stats.today.formatCurrency()}")
Text("Tuần này: ${stats.week.formatCurrency()}")
Text("Tháng này: ${stats.month.formatCurrency()}")

// Chart data ready
LineChart(data = stats.dailyBreakdown)
```

### Option 2: Dùng API Ledger (nếu cần custom logic)

**Entry Types:**

- `CREDIT` = Thu nhập (tiền vào ví từ giao đơn hàng) → **TÍNH VÀO DOANH THU**
- `DEBIT` = Tiền ra (rút tiền, phạt, etc.) → **KHÔNG PHẢI DOANH THU**

---

## Frontend Logic (Legacy - Nếu không dùng Revenue API)

### Example 1: Doanh thu hôm nay

```kotlin
suspend fun getTodayRevenue(): Double {
    val ledger = walletApi.getLedger(page = 1, limit = 100)
    val today = LocalDate.now()

    return ledger.entries
        .filter { it.type == "CREDIT" }  // Chỉ lấy thu nhập
        .filter { it.createdAt.toLocalDate() == today }
        .sumOf { it.amount }
}
```

### Example 2: Doanh thu tuần này

```kotlin
data class RevenueStats(
    val daily: Double,
    val weekly: Double,
    val monthly: Double,
    val yearly: Double
)

suspend fun getRevenueStats(): RevenueStats {
    val allEntries = mutableListOf<LedgerEntry>()
    var page = 1
    var hasMore = true

    // Fetch all entries (hoặc giới hạn theo thời gian)
    while (hasMore) {
        val response = walletApi.getLedger(page = page, limit = 50)
        allEntries.addAll(response.entries)
        hasMore = page < response.totalPages
        page++
    }

    // Filter chỉ lấy CREDIT entries
    val revenueEntries = allEntries.filter { it.type == "CREDIT" }

    val now = LocalDate.now()
    val startOfWeek = now.with(DayOfWeek.MONDAY)
    val startOfMonth = now.withDayOfMonth(1)
    val startOfYear = now.withDayOfYear(1)

    return RevenueStats(
        daily = revenueEntries
            .filter { it.createdAt.toLocalDate() == now }
            .sumOf { it.amount },
        weekly = revenueEntries
            .filter { it.createdAt.toLocalDate() >= startOfWeek }
            .sumOf { it.amount },
        monthly = revenueEntries
            .filter { it.createdAt.toLocalDate() >= startOfMonth }
            .sumOf { it.amount },
        yearly = revenueEntries
            .filter { it.createdAt.toLocalDate() >= startOfYear }
            .sumOf { it.amount }
    )
}
```

### Example 3: Chart doanh thu 7 ngày gần nhất

```kotlin
data class DailyRevenue(
    val date: LocalDate,
    val amount: Double
)

suspend fun getLast7DaysRevenue(): List<DailyRevenue> {
    val ledger = walletApi.getLedger(page = 1, limit = 200)
    val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()) }

    return last7Days.map { date ->
        val dailyTotal = ledger.entries
            .filter { it.type == "CREDIT" }
            .filter { it.createdAt.toLocalDate() == date }
            .sumOf { it.amount }

        DailyRevenue(date, dailyTotal)
    }.reversed() // Sắp xếp từ cũ đến mới
}
```

---

## UI Mockup: Màn hình Doanh thu Shipper

```
┌─────────────────────────────────┐
│  Doanh thu                       │
│                                  │
│  Hôm nay        55,000 đ         │
│  Tuần này      350,000 đ         │
│  Tháng này   1,200,000 đ         │
│  Tổng cộng   2,500,000 đ ←totalEarned│
│                                  │
│  ┌─────────────────────────┐    │
│  │ Số dư ví: 500,000 đ     │←balance│
│  │ [Rút tiền]              │    │
│  └─────────────────────────┘    │
│                                  │
│  ╭─ Biểu đồ 7 ngày ───────╮    │
│  │     ▃                   │    │
│  │   ▃ █ ▄   ▂             │    │
│  │ ▂ █ █ █ ▄ █ ▃           │    │
│  │ M T W T F S S           │    │
│  ╰─────────────────────────╯    │
│                                  │
│  Lịch sử giao dịch               │
│  ┌───────────────────────────┐  │
│  │ 🟢 Phí giao đơn #001      │  │
│  │ 29/01/2026 10:00         │  │
│  │ +25,000 đ               │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ 🟢 Phí giao đơn #002      │  │
│  │ 29/01/2026 14:30         │  │
│  │ +30,000 đ               │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ 🔴 Rút tiền               │  │
│  │ 29/01/2026 18:00         │  │
│  │ -100,000 đ              │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

**Legend:**

- 🟢 CREDIT (màu xanh) - Thu nhập
- 🔴 DEBIT (màu đỏ) - Rút tiền

---

## Optimization Tips

### 1. Cache doanh thu hôm nay

```kotlin
// Không cần fetch lại liên tục, cache trong 5 phút
@Cacheable(duration = 5.minutes)
suspend fun getTodayRevenue(): Double { ... }
```

### 2. Pagination thông minh

```kotlin
// Chỉ fetch entries trong khoảng thời gian cần thiết
// Backend chưa hỗ trợ filter by date → Frontend cần fetch all và filter
// Tối ưu: Fetch theo batch, stop khi đã đủ data cho timeframe
```

### 3. Real-time updates (Optional)

```kotlin
// Lắng nghe Firestore collection: wallets/{walletId}/ledger
// Auto update UI khi có transaction mới
val ledgerRef = firestore
    .collection("wallets")
    .doc("wallet_shipper_${userId}")
    .collection("ledger")
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .limit(20)

ledgerRef.addSnapshotListener { snapshot, error ->
    if (snapshot != null) {
        val entries = snapshot.documents.map { it.toObject<LedgerEntry>() }
        updateRevenueStats(entries)
    }
}
```

---

## Backend Data Flow

### Khi shipper giao xong đơn:

1. Order state: `DELIVERING` → `DELIVERED`
2. Wallets service được gọi:
   ```typescript
   await walletsService.creditOrderCompletionPayment(
     ownerId,
     ownerAmount, // total - shipperPayout
     shipperId,
     shipperAmount, // shipperPayout
     orderId,
     orderNumber,
   );
   ```
3. Transaction update 2 wallets:
   - Owner wallet: `balance += ownerAmount`
   - Shipper wallet: `balance += shipperAmount` ← **DOANH THU**
4. Create 2 ledger entries:
   - Owner: `{ type: "CREDIT", amount: ownerAmount, orderId }`
   - Shipper: `{ type: "CREDIT", amount: shipperAmount, orderId }` ← **ENTRY DOANH THU**

---

## FAQ

**Q: Tại sao không có API riêng `/api/shippers/me/revenue`?**  
A: Không cần thiết. Wallet ledger đã chứa đủ thông tin. Frontend chỉ cần filter `type: "CREDIT"` và group by date.

**Q: Có thể lấy doanh thu theo khoảng thời gian không?**  
A: Backend hiện tại chỉ hỗ trợ pagination. Frontend cần fetch entries và filter by `createdAt`.

**Q: Làm sao biết entry nào là doanh thu?**  
A: `type: "CREDIT"` + có `orderId` → Doanh thu từ giao đơn.

**Q: `totalEarned` có bị sai không nếu có refund?**  
A: Hiện tại chưa hỗ trợ refund. Khi có refund, sẽ có entry `type: "DEBIT"` với `orderId`.

**Q: Shipper có thể rút tiền không?**  
A: Có. Endpoint `POST /api/wallets/payout` → Tạo entry `type: "DEBIT"`.

---

## Related Files

- [wallets.controller.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\controllers\wallets.controller.ts)
- [wallets.service.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\wallets.service.ts)
- [wallet.entity.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\entities\wallet.entity.ts)

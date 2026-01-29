# Revenue API - Quick Reference

> **API mới:** `GET /api/wallets/revenue` - Backend tính toán sẵn doanh thu

## Endpoint

```
GET /api/wallets/revenue?period={period}
```

**Authorization:** Bearer token (SHIPPER or OWNER role)

## Query Parameters

| Parameter | Type   | Required | Values                                  | Default | Description                     |
| --------- | ------ | -------- | --------------------------------------- | ------- | ------------------------------- |
| period    | string | No       | `today`, `week`, `month`, `year`, `all` | `month` | Khoảng thời gian tính doanh thu |

## Response Schema

```typescript
{
  today: number; // Doanh thu hôm nay
  week: number; // Doanh thu tuần này (Mon-Sun)
  month: number; // Doanh thu tháng này
  year: number; // Doanh thu năm nay
  all: number; // Tổng doanh thu từ trước đến giờ
  dailyBreakdown: {
    date: string; // YYYY-MM-DD hoặc YYYY-MM
    amount: number; // Doanh thu trong ngày/tháng đó
    orderCount: number; // Số đơn giao thành công
  }
  [];
  calculatedAt: string; // ISO timestamp
}
```

## Daily Breakdown Logic

| Period | Breakdown                |
| ------ | ------------------------ |
| today  | Last 7 days (daily)      |
| week   | Last 7 days (daily)      |
| month  | Last 30 days (daily)     |
| year   | Last 12 months (monthly) |
| all    | Last 30 days (daily)     |

## Example Requests

### 1. Doanh thu hôm nay

```bash
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/wallets/revenue?period=today" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

### 2. Doanh thu tuần này

```bash
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/wallets/revenue?period=week" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

### 3. Doanh thu tháng này (default)

```bash
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/wallets/revenue" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

### 4. Doanh thu năm nay

```bash
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/wallets/revenue?period=year" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

### 5. Tổng doanh thu từ trước đến giờ

```bash
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/wallets/revenue?period=all" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

## Example Response

```json
{
  "today": 55000,
  "week": 350000,
  "month": 1200000,
  "year": 2500000,
  "all": 2500000,
  "dailyBreakdown": [
    {
      "date": "2026-01-23",
      "amount": 45000,
      "orderCount": 2
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
  "calculatedAt": "2026-01-29T10:00:00.000Z"
}
```

## Frontend Implementation (Kotlin)

### Data Model

```kotlin
data class RevenueStats(
    val today: Double,
    val week: Double,
    val month: Double,
    val year: Double,
    val all: Double,
    val dailyBreakdown: List<DailyRevenue>,
    val calculatedAt: String
)

data class DailyRevenue(
    val date: String,      // YYYY-MM-DD or YYYY-MM
    val amount: Double,
    val orderCount: Int
)

enum class RevenuePeriod {
    TODAY, WEEK, MONTH, YEAR, ALL
}
```

### API Service

```kotlin
interface WalletApiService {
    @GET("wallets/revenue")
    suspend fun getRevenue(
        @Query("period") period: String? = "month"
    ): Response<RevenueStats>
}
```

### ViewModel

```kotlin
class EarningsViewModel(
    private val walletApi: WalletApiService
) : ViewModel() {

    private val _revenueStats = MutableStateFlow<RevenueStats?>(null)
    val revenueStats: StateFlow<RevenueStats?> = _revenueStats.asStateFlow()

    fun loadRevenue(period: RevenuePeriod = RevenuePeriod.MONTH) {
        viewModelScope.launch {
            try {
                val response = walletApi.getRevenue(period.name.lowercase())
                if (response.isSuccessful) {
                    _revenueStats.value = response.body()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

### Composable UI

```kotlin
@Composable
fun RevenueScreen(viewModel: EarningsViewModel) {
    val stats by viewModel.revenueStats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRevenue(RevenuePeriod.MONTH)
    }

    stats?.let { revenue ->
        Column {
            // Summary Cards
            RevenueCard("Hôm nay", revenue.today)
            RevenueCard("Tuần này", revenue.week)
            RevenueCard("Tháng này", revenue.month)
            RevenueCard("Tổng cộng", revenue.all)

            // Chart
            LineChart(
                data = revenue.dailyBreakdown.map {
                    ChartData(it.date, it.amount)
                }
            )

            // Daily List
            LazyColumn {
                items(revenue.dailyBreakdown) { day ->
                    DailyRevenueItem(
                        date = day.date,
                        amount = day.amount,
                        orderCount = day.orderCount
                    )
                }
            }
        }
    }
}

@Composable
fun RevenueCard(label: String, amount: Double) {
    Card {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                amount.formatCurrency(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

## Benefits

✅ **No client-side calculation** - Backend handles all logic  
✅ **Less data transfer** - Aggregated results only  
✅ **Ready for charts** - `dailyBreakdown` array  
✅ **Consistent logic** - Same calculation for all clients  
✅ **Easy to cache** - Results can be cached for performance

## Performance Notes

- Backend fetches ALL ledger entries for accurate calculation
- For large datasets (>1000 entries), consider:
  - Caching results (Redis)
  - Pre-computing daily aggregates (background job)
  - Pagination for breakdown if needed

## Error Handling

| Status | Error            | Reason                             |
| ------ | ---------------- | ---------------------------------- |
| 401    | Unauthorized     | Invalid/missing Firebase token     |
| 403    | Forbidden        | User is not SHIPPER or OWNER       |
| 404    | Wallet not found | Wallet hasn't been initialized     |
| 400    | Invalid period   | Period param not in allowed values |

## Related Files

- [get-revenue.dto.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\dto\get-revenue.dto.ts) - DTOs
- [wallets.service.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\wallets.service.ts#L405-L569) - Business logic
- [wallets.controller.ts](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\Backend\functions\src\modules\wallets\controllers\wallets.controller.ts#L117-L138) - Endpoint
- [SHIPPER_REVENUE_API_GUIDE.md](d:\2. Code\itus\3rd-year\mobi\project-app\MobileProject\docs\backend\SHIPPER_REVENUE_API_GUIDE.md) - Complete guide

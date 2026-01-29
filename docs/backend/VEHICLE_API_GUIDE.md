# Vehicle Management API - Frontend Guide

> **Chỉ dành cho SHIPPER role**

## API Endpoints

### 1. Lấy thông tin phương tiện

```http
GET /api/me/vehicle
Authorization: Bearer <firebase-token>
```

**Response 200:**

```json
{
  "vehicleType": "MOTORBIKE",
  "vehicleNumber": "59X1-12345",
  "driverLicenseUrl": "https://storage.googleapis.com/.../license.jpg"
}
```

**Response 403:**

```json
{
  "statusCode": 403,
  "message": "Only shippers can access vehicle information"
}
```

---

### 2. Cập nhật thông tin phương tiện (loại xe & biển số)

```http
PUT /api/me/vehicle
Authorization: Bearer <firebase-token>
Content-Type: application/json

{
  "vehicleType": "MOTORBIKE",  // MOTORBIKE | CAR | BICYCLE
  "vehicleNumber": "59X1-12345"
}
```

**Validation:**

- `vehicleType`: Enum (MOTORBIKE, CAR, BICYCLE)
- `vehicleNumber`: Format biển số Việt Nam (59X1-12345, 30A-98765, etc.)

**Response 200:**

```json
{
  "vehicleType": "MOTORBIKE",
  "vehicleNumber": "59X1-12345",
  "driverLicenseUrl": "https://storage.googleapis.com/.../license.jpg"
}
```

---

### 3. Upload ảnh bằng lái

```http
POST /api/me/vehicle/driver-license
Authorization: Bearer <firebase-token>
Content-Type: multipart/form-data

driverLicense: <File>
```

**Constraints:**

- File type: JPEG, PNG only
- Max size: 5MB
- Form field name: `driverLicense`

**Response 200:**

```json
{
  "driverLicenseUrl": "https://storage.googleapis.com/.../license.jpg"
}
```

**Response 400:**

```json
{
  "statusCode": 400,
  "message": "Only JPEG/PNG images are allowed"
}
```

---

### 4. Xóa ảnh bằng lái

```http
DELETE /api/me/vehicle/driver-license
Authorization: Bearer <firebase-token>
```

**Response 200:**

```json
{
  "message": "Driver license deleted successfully"
}
```

---

## Frontend Implementation (Kotlin/Android)

### 1. Data Model

```kotlin
enum class VehicleType {
    MOTORBIKE,
    CAR,
    BICYCLE
}

data class VehicleInfo(
    val vehicleType: VehicleType,
    val vehicleNumber: String,
    val driverLicenseUrl: String? = null
)

data class UpdateVehicleRequest(
    val vehicleType: VehicleType,
    val vehicleNumber: String
)
```

### 2. API Service

```kotlin
interface VehicleApiService {
    @GET("me/vehicle")
    suspend fun getVehicleInfo(): Response<VehicleInfo>

    @PUT("me/vehicle")
    suspend fun updateVehicle(@Body request: UpdateVehicleRequest): Response<VehicleInfo>

    @Multipart
    @POST("me/vehicle/driver-license")
    suspend fun uploadDriverLicense(
        @Part driverLicense: MultipartBody.Part
    ): Response<DriverLicenseResponse>

    @DELETE("me/vehicle/driver-license")
    suspend fun deleteDriverLicense(): Response<MessageResponse>
}

data class DriverLicenseResponse(
    val driverLicenseUrl: String
)

data class MessageResponse(
    val message: String
)
```

### 3. Upload Driver License Example

```kotlin
suspend fun uploadDriverLicense(uri: Uri): Result<String> {
    return try {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("driverLicense", file.name, requestFile)

        val response = vehicleApiService.uploadDriverLicense(part)
        if (response.isSuccessful) {
            Result.success(response.body()!!.driverLicenseUrl)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Use Cases

### Màn hình Profile Shipper

```
[Avatar]
Nguyễn Văn A
Shipper - Quán A Mập
⭐ 4.8 (150 đơn)

┌─────────────────────────────────┐
│ Phương tiện                      │
│ 🏍️ Xe máy                        │
│ 📋 59X1-12345                    │
│ [Chỉnh sửa]                      │
└─────────────────────────────────┘

┌─────────────────────────────────┐
│ Bằng lái xe                      │
│ [Ảnh bằng lái]                   │
│ [Cập nhật ảnh] [Xóa]             │
└─────────────────────────────────┘
```

### Flow cập nhật phương tiện

1. User click "Chỉnh sửa"
2. Bottom sheet hiện form:
   - Dropdown: Loại xe (Xe máy / Ô tô / Xe đạp)
   - TextField: Biển số (validate format)
3. Click "Lưu" → call `PUT /api/me/vehicle`
4. Success → update UI

### Flow upload bằng lái

1. User click "Cập nhật ảnh"
2. Open image picker (camera / gallery)
3. Compress image (nếu > 5MB)
4. Upload via `POST /api/me/vehicle/driver-license`
5. Show preview với URL mới

---

## Error Handling

| Status | Error                         | Giải pháp                                                            |
| ------ | ----------------------------- | -------------------------------------------------------------------- |
| 403    | Only shippers can...          | Kiểm tra user role trước khi hiển thị màn hình                       |
| 400    | Invalid vehicle number format | Validate client-side (regex: `^[0-9]{2}[A-Z]{1,2}[-\s]?[0-9]{4,5}$`) |
| 400    | File size must not exceed 5MB | Compress image trước khi upload                                      |
| 400    | Only JPEG/PNG images          | Filter file picker chỉ cho phép image/\*                             |

---

## Security Notes

- ✅ Chỉ SHIPPER role có quyền access các endpoints này
- ✅ Driver license URL được lưu trong `shipperInfo.driverLicenseUrl` (Firestore)
- ✅ File được lưu trong Firebase Storage: `shipper-documents/{userId}/driverLicense_*.jpg`
- ✅ Old file tự động bị xóa khi upload file mới

---

## Testing

### Curl Examples

```bash
# 1. Get vehicle info
curl -X GET "http://localhost:5001/foodappproject-7c136/us-central1/api/me/vehicle" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"

# 2. Update vehicle
curl -X PUT "http://localhost:5001/foodappproject-7c136/us-central1/api/me/vehicle" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleType": "MOTORBIKE",
    "vehicleNumber": "59X1-12345"
  }'

# 3. Upload driver license
curl -X POST "http://localhost:5001/foodappproject-7c136/us-central1/api/me/vehicle/driver-license" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  -F "driverLicense=@/path/to/license.jpg"

# 4. Delete driver license
curl -X DELETE "http://localhost:5001/foodappproject-7c136/us-central1/api/me/vehicle/driver-license" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

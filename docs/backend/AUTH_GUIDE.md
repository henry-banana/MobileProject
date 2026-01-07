# Authentication Guide - KTX Delivery Backend

## 📌 Tổng quan

Backend cung cấp 9 Auth APIs với 2 loại authentication:

- **Public APIs** (không cần token): Register, Login, OTP, Password Reset
- **Protected APIs** (cần ID token): Change Password, Logout

---

## 🔐 Token Types - Quan trọng phải hiểu!

### 1. Custom Token (Backend → Client)

```json
{
  "customToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

- **Mục đích**: Backend tạo để client sign in Firebase
- **Không dùng để call API**
- **TTL**: 1 giờ
- **Flow**: Login/Register → Nhận customToken → Sign in Firebase

### 2. ID Token (Client → Backend)

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

- **Mục đích**: Client dùng để call protected APIs
- **Backend verify** bằng `verifyIdToken()`
- **TTL**: 1 giờ (tự động refresh)
- **Flow**: Sau khi sign in → `user.getIdToken()` → Call API

### 3. User ID (UID)

```
"LUdujmyqJkgk0WjhfGfPGu8C3Er1"
```

- **Mục đích**: Định danh user trong hệ thống
- **Không dùng để authentication**

### 4. FCM Token

```
"dF3K2mPxQ8y..."
```

- **Mục đích**: Gửi push notifications
- **Lưu trong**: `users/{userId}/fcmTokens[]`

---

## 🚀 Authentication Flow - Client Implementation

### Step 1: Register User

```typescript
// Call backend API
const response = await fetch("http://localhost:3000/api/auth/register", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "user@example.com",
    password: "password123",
    displayName: "Nguyễn Văn A",
    phone: "0901234567", // Tự động convert sang +84901234567
    role: "CUSTOMER",
  }),
});

const { customToken, user } = await response.json();
```

### Step 2: Sign In to Firebase với Custom Token

```typescript
// Flutter
final userCredential = await FirebaseAuth.instance
  .signInWithCustomToken(customToken);
final user = userCredential.user!;

// Kotlin
val auth = FirebaseAuth.getInstance()
auth.signInWithCustomToken(customToken)
  .addOnSuccessListener { authResult ->
    val user = authResult.user
  }
```

### Step 3: Lấy ID Token

```typescript
// Flutter
final idToken = await FirebaseAuth.instance.currentUser!.getIdToken();

// Kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(false)
  ?.addOnSuccessListener { result ->
    val idToken = result.token
  }
```

### Step 4: Call Protected APIs

```typescript
// Flutter
final response = await http.put(
  Uri.parse('http://localhost:3000/api/auth/change-password'),
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer $idToken',  // ← ID Token ở đây
  },
  body: jsonEncode({
    'oldPassword': 'old123',
    'newPassword': 'new456'
  })
);

// Kotlin
val request = Request.Builder()
  .url("http://localhost:3000/api/auth/change-password")
  .addHeader("Authorization", "Bearer $idToken")  // ← ID Token
  .put(body)
  .build()
```

---

## 🧪 Testing với Swagger - Lấy ID Token

### Cách 1: Dùng Script Node.js (Nhanh nhất)

Script đã có sẵn trong `Backend/functions/get-id-token.js`.

**Chạy:**

```bash
cd Backend/functions
node get-id-token.js hoatong1211@gmail.com
```

**Output:**

```
🔑 ID Token for Swagger:
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

📋 Copy and paste vào Swagger Authorization header:
Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

⏰ Token expires in: 1 hour
💾 Token saved to: id-token.txt
```

Token cũng được lưu vào file `id-token.txt` trong cùng thư mục để dễ copy!

### Cách 2: Dùng Firebase Console (Manual)

1. Vào https://console.firebase.google.com
2. Project → **Authentication** → **Users**
3. Click vào user cần test
4. Copy **User UID**
5. Dùng script Python/Node để tạo ID token (tương tự cách 1)

### Cách 3: Từ Mobile App (Debugging)

```kotlin
// Kotlin - Log ID token trong debug build
FirebaseAuth.getInstance().currentUser?.getIdToken(false)
  ?.addOnSuccessListener { result ->
    Log.d("ID_TOKEN", result.token)  // Copy từ Logcat
  }
```

```dart
// Flutter
final idToken = await FirebaseAuth.instance.currentUser!.getIdToken();
debugPrint('ID_TOKEN: $idToken');  // Copy từ console
```

### Test trên Swagger

1. Mở http://localhost:3000/api/docs
2. Click **Authorize** (nút khóa ở góc phải)
3. Nhập: `Bearer <ID_TOKEN>` (có chữ Bearer phía trước)
4. Click **Authorize**
5. Giờ test các protected APIs: Change Password, Logout

---

## 📡 API Endpoints Reference

### Public APIs (Không cần token)

#### 1. Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "CUSTOMER"
}

Response:
{
  "success": true,
  "data": {
    "user": { "id": "...", "email": "...", "role": "CUSTOMER" },
    "customToken": "eyJhbGci..."  // ← Dùng để sign in Firebase
  }
}
```

#### 2. Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "user": { ... },
    "customToken": "eyJhbGci...",
    "message": "Đăng nhập thành công"
  }
}
```

#### 3. Send OTP (Email Verification)

```http
POST /api/auth/send-otp
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: { "message": "OTP đã được gửi đến email của bạn" }
```

#### 4. Verify OTP

```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}

Response: { "message": "Xác thực email thành công" }
```

#### 5. Forgot Password

```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response: { "message": "Mã xác nhận đã được gửi đến email của bạn" }
```

#### 6. Reset Password

```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "newPassword123"
}

Response: { "message": "Đặt lại mật khẩu thành công" }
```

### Protected APIs (Cần ID Token)

#### 7. Change Password

```http
PUT /api/auth/change-password
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json

{
  "oldPassword": "currentPassword",
  "newPassword": "newPassword123"
}

Response: { "message": "Đổi mật khẩu thành công" }
```

#### 8. Logout

```http
POST /api/auth/logout
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json

{
  "fcmToken": "device_fcm_token_here"  // Optional
}

Response: { "message": "Đăng xuất thành công" }
```

#### 9. Google Sign-In

```http
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "google_id_token_from_firebase_sdk",
  "role": "CUSTOMER"  // Optional, default CUSTOMER
}

Response:
{
  "user": { ... },
  "isNewUser": true
}
```

---

## ⚠️ Common Issues

### 1. "Missing authorization token"

- **Nguyên nhân**: Không gửi ID token hoặc gửi sai format
- **Fix**:
  ```
  ✅ Authorization: Bearer eyJhbGci...
  ❌ Authorization: eyJhbGci...  (thiếu "Bearer ")
  ❌ Gửi customToken thay vì ID token
  ```

### 2. "Invalid token" / Token expired

- **Nguyên nhân**: ID token hết hạn (1 giờ)
- **Fix**: Refresh token

  ```typescript
  // Flutter
  final idToken = await user.getIdToken(true);  // force refresh

  // Kotlin
  user.getIdToken(true)  // force refresh = true
  ```

### 3. OTP expired

- **Nguyên nhân**: OTP hết hạn sau 5 phút
- **Fix**: Gửi lại OTP bằng API `send-otp` hoặc `forgot-password`

### 4. "Email đã được sử dụng"

- **Nguyên nhân**: Email đã tồn tại trong Firebase Auth
- **Fix**: Dùng email khác hoặc login thay vì register

### 5. Phone number format error

- **Nguyên nhân**: Backend yêu cầu E.164 format
- **Fix**: Backend tự động convert:
  - `0901234567` → `+84901234567` ✅
  - Hoặc client gửi sẵn: `+84901234567`

---

## 🔒 Security Best Practices

### Client-side

1. **Không lưu password plaintext**
2. **ID token trong memory only** (không localStorage)
3. **Tự động refresh token** trước khi hết hạn
4. **Logout** = xóa token + gọi logout API

### Backend-side (Đã implement)

1. ✅ Password hashed bởi Firebase Auth
2. ✅ OTP rate limiting (60s)
3. ✅ OTP max attempts (3 lần)
4. ✅ OTP expiry (5 phút)
5. ✅ Email verification required
6. ✅ Token verification trên mọi protected APIs

---

## 📱 Flutter Example - Complete Flow

```dart
class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final String baseUrl = 'http://localhost:3000/api';

  // 1. Register
  Future<User> register({
    required String email,
    required String password,
    required String displayName,
    String? phone,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        'displayName': displayName,
        'phone': phone,
        'role': 'CUSTOMER',
      }),
    );

    final data = jsonDecode(response.body)['data'];
    final customToken = data['customToken'];

    // Sign in với custom token
    final userCredential = await _auth.signInWithCustomToken(customToken);
    return userCredential.user!;
  }

  // 2. Login
  Future<User> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    final data = jsonDecode(response.body)['data'];
    final customToken = data['customToken'];

    final userCredential = await _auth.signInWithCustomToken(customToken);
    return userCredential.user!;
  }

  // 3. Get ID Token (để call protected APIs)
  Future<String> getIdToken() async {
    final user = _auth.currentUser;
    if (user == null) throw Exception('User not signed in');
    return await user.getIdToken() ?? '';
  }

  // 4. Change Password
  Future<void> changePassword(String oldPassword, String newPassword) async {
    final idToken = await getIdToken();

    await http.put(
      Uri.parse('$baseUrl/auth/change-password'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $idToken',
      },
      body: jsonEncode({
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      }),
    );
  }

  // 5. Logout
  Future<void> logout() async {
    final idToken = await getIdToken();

    await http.post(
      Uri.parse('$baseUrl/auth/logout'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $idToken',
      },
    );

    await _auth.signOut();
  }
}
```

---

## 🎯 Testing Checklist

- [ ] Register với email/password → Nhận customToken
- [ ] Sign in Firebase với customToken → Thành công
- [ ] Lấy ID token → Không null
- [ ] Login → Nhận customToken mới
- [ ] Send OTP → Email nhận được
- [ ] Verify OTP → Email verified = true
- [ ] Forgot password → Nhận OTP qua email
- [ ] Reset password → Đổi password thành công
- [ ] Login với password mới → Thành công
- [ ] Change password (với ID token) → Thành công
- [ ] Logout → Token bị revoke

---

## 📞 Support

Gặp vấn đề? Check:

1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Authentication & Firestore tabs
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository

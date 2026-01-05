# NestJS Firebase Cloud Functions Backend

Backend cho ứng dụng mobile sử dụng NestJS chạy trên Firebase Cloud Functions.

## Cấu trúc thư mục

```
functions/
├── src/
│   ├── app.module.ts              # Root module
│   ├── main.ts                    # NestJS bootstrap
│   ├── index.ts                   # Cloud Functions entry point
│   │
│   ├── firebase/
│   │   ├── firebase.module.ts     # Firebase Admin module
│   │   └── firebase.provider.ts   # Firestore provider
│   │
│   └── products/
│       ├── product.controller.ts  # Product API endpoints
│       ├── product.service.ts     # Business logic
│       └── product.module.ts      # Product module
│
├── lib/                           # Compiled TypeScript output
├── package.json
├── tsconfig.json
└── firebase.json
```

## Cài đặt

```bash
cd functions
npm install
```

## Chạy local

```bash
npm run serve
```

## Deploy lên Firebase

```bash
npm run deploy
```

## API Endpoints

Base URL sau khi deploy: `https://<region>-<project-id>.cloudfunctions.net/api`

### GET /products

Lấy danh sách tất cả sản phẩm

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": "abc123",
      "name": "Sản phẩm 1",
      "price": 100000,
      "description": "Mô tả sản phẩm",
      "createdAt": "2026-01-05T00:00:00.000Z"
    }
  ]
}
```

### POST /products

Tạo sản phẩm mới

**Request Body:**

```json
{
  "name": "Sản phẩm mới",
  "price": 150000,
  "description": "Mô tả"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "xyz789",
    "name": "Sản phẩm mới",
    "price": 150000,
    "description": "Mô tả",
    "createdAt": "2026-01-05T00:00:00.000Z"
  },
  "message": "Product created successfully"
}
```

## Kiến trúc

- **Firebase Cloud Functions**: Runtime môi trường serverless
- **NestJS**: Framework TypeScript cho backend
- **Firebase Admin SDK**: Truy cập Firestore database
- **Express**: HTTP server (được NestJS sử dụng)

## Đặc điểm kỹ thuật

- Sử dụng caching để giảm cold start
- Không listen port (serverless)
- CommonJS modules (tương thích Firebase)
- TypeScript với decorators
- Dependency Injection pattern

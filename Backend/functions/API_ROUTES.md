# NestJS Firebase Cloud Functions - Domain Architecture

Backend với kiến trúc domain-driven cho ứng dụng Food App Mobile.

## 🏗️ Kiến trúc

```
functions/src/
├── owner/                          # Owner Domain
│   ├── owner.module.ts             # Owner root module
│   ├── dashboard/                  # Dashboard Sub-domain
│   │   ├── owner-dashboard.controller.ts
│   │   ├── owner-dashboard.service.ts
│   │   └── owner-dashboard.module.ts
│   └── profile/                    # Profile Sub-domain
│       ├── owner-profile.controller.ts
│       ├── owner-profile.service.ts
│       └── owner-profile.module.ts
│
├── shipper/                        # Shipper Domain
│   ├── shipper.module.ts           # Shipper root module
│   ├── profile/                    # Profile Sub-domain
│   │   ├── shipper-profile.controller.ts
│   │   ├── shipper-profile.service.ts
│   │   └── shipper-profile.module.ts
│   └── deliveries/                 # Deliveries Sub-domain
│       ├── shipper-delivery.controller.ts
│       ├── shipper-delivery.service.ts
│       └── shipper-delivery.module.ts
│
├── client/                         # Client Domain
│   ├── client.module.ts            # Client root module
│   ├── home/                       # Home Sub-domain
│   │   ├── client-home.controller.ts
│   │   ├── client-home.service.ts
│   │   └── client-home.module.ts
│   └── orders/                     # Orders Sub-domain
│       ├── client-order.controller.ts
│       ├── client-order.service.ts
│       └── client-order.module.ts
│
├── firebase/
│   ├── firebase.module.ts
│   └── firebase.provider.ts
│
├── app.module.ts                   # Root module
├── main.ts                         # NestJS bootstrap
└── index.ts                        # Cloud Function entry point
```

## 📡 API Endpoints

### Base URL (Local)

```
http://127.0.0.1:5001/<project-id>/<region>/api
```

### Owner Domain

- **GET** `/owner/dashboard` → Trả về: `{ "message": "Hello Owner Dashboard" }`
- **GET** `/owner/profile` → Trả về: `{ "message": "Hello Owner Profile" }`

### Shipper Domain

- **GET** `/shipper/profile` → Trả về: `{ "message": "Hello Shipper Profile" }`
- **GET** `/shipper/deliveries` → Trả về: `{ "message": "Hello Shipper Deliveries" }`

### Client Domain

- **GET** `/client/home` → Trả về: `{ "message": "Hello Client Home" }`
- **GET** `/client/orders` → Trả về: `{ "message": "Hello Client Orders" }`

### Health Check

- **GET** `/` → Root endpoint
- **GET** `/health` → Health check

## 🚀 Cài đặt & Chạy

### Cài đặt dependencies

```bash
cd functions
npm install
```

### Build TypeScript

```bash
npm run build
```

### Chạy Firebase Emulator (Local)

```bash
npm run serve
```

Emulator sẽ chạy tại: `http://127.0.0.1:5001`

### Test API với curl

```bash
# Health check
curl http://127.0.0.1:5001/<project-id>/us-central1/api/health

# Owner domain
curl http://127.0.0.1:5001/<project-id>/us-central1/api/owner/dashboard
curl http://127.0.0.1:5001/<project-id>/us-central1/api/owner/profile

# Shipper domain
curl http://127.0.0.1:5001/<project-id>/us-central1/api/shipper/profile
curl http://127.0.0.1:5001/<project-id>/us-central1/api/shipper/deliveries

# Client domain
curl http://127.0.0.1:5001/<project-id>/us-central1/api/client/home
curl http://127.0.0.1:5001/<project-id>/us-central1/api/client/orders
```

### Deploy lên Firebase

```bash
npm run deploy
```

## 🎯 Đặc điểm kỹ thuật

- **Framework**: NestJS (TypeScript)
- **Runtime**: Firebase Cloud Functions (Node.js 20)
- **Architecture**: Domain-Driven Design
- **Module Pattern**: Domain → Sub-domain → Controller/Service
- **Dependency Injection**: NestJS DI Container
- **No Database**: Tất cả response là hard-coded (testing purpose)
- **No Authentication**: Public endpoints

## 📝 Cấu trúc Module

### Domain Module

Mỗi domain (Owner, Shipper, Client) có module riêng import tất cả sub-domain modules.

```typescript
@Module({
  imports: [SubDomainModule1, SubDomainModule2],
})
export class DomainModule {}
```

### Sub-domain Module

Mỗi sub-domain có Controller và Service riêng.

```typescript
@Module({
  controllers: [SubDomainController],
  providers: [SubDomainService],
})
export class SubDomainModule {}
```

### App Module

Root module import tất cả domain modules.

```typescript
@Module({
  imports: [FirebaseModule, OwnerModule, ShipperModule, ClientModule],
})
export class AppModule {}
```

## 🔧 Development

### Thêm Sub-domain mới

1. Tạo thư mục mới trong domain tương ứng
2. Tạo 3 files: controller, service, module
3. Import module vào domain module cha
4. Build và test

### Code Template

**Controller:**

```typescript
@Controller("domain/subdomain")
export class SubdomainController {
  constructor(private readonly service: SubdomainService) {}

  @Get()
  getData() {
    return { message: this.service.getMessage() };
  }
}
```

**Service:**

```typescript
@Injectable()
export class SubdomainService {
  getMessage(): string {
    return "Hello Message";
  }
}
```

## 📦 Scripts

- `npm run build` - Compile TypeScript
- `npm run serve` - Chạy Firebase emulator
- `npm run deploy` - Deploy lên Firebase
- `npm run lint` - Lint code

## ⚡ Performance

- **Cold Start Optimization**: Express instance được cache
- **No Database Calls**: Response tức thì (no I/O)
- **Lightweight**: Chỉ routing logic, không có business logic phức tạp

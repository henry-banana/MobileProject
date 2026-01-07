# ER Diagram - KTX Delivery

> **Updated:** 2026-01-07 (AUTH, USER, ADMIN collections)

```mermaid
erDiagram
    %% ========================================
    %% ✅ DONE / IN-PROGRESS - Đang implement
    %% ========================================

    USERS {
        string id PK "Firebase UID"
        string email UK
        string displayName
        string phone
        string avatarUrl
        enum role "CUSTOMER | OWNER | SHIPPER | ADMIN"
        enum status "PENDING | ACTIVE | SUSPENDED | DELETED"
        array fcmTokens "Max 5 tokens"
        string shopId FK "If OWNER"
        object shipperInfo "If SHIPPER"
        timestamp createdAt
        timestamp updatedAt
    }

    USER_ADDRESSES {
        string id PK
        string userId FK
        string label
        string fullAddress
        string building
        string room
        string note
        boolean isDefault
        timestamp createdAt
    }

    USER_FAVORITES {
        string id PK "userId_productId"
        string userId FK
        string productId FK
        string productName "Denormalized"
        number productPrice "Denormalized"
        string shopId FK "Denormalized"
        string shopName "Denormalized"
        timestamp createdAt
    }

    USER_SETTINGS {
        string userId PK "Same as user.id"
        object notifications
        string language
        string currency
        object bankAccount
    }

    CATEGORIES {
        string id PK
        string name
        string nameEn
        string slug UK
        string icon "Material icon"
        string description
        number sortOrder
        boolean isActive
        timestamp createdAt
        timestamp updatedAt
    }

    %% ========================================
    %% 🔲 PLANNED - Chưa implement
    %% ========================================

    SHOPS

    PRODUCTS

    CARTS

    ORDERS

    PAYMENTS

    VOUCHERS

    WALLETS

    NOTIFICATIONS

    SUBSCRIPTIONS

    SHIPPER_APPLICATIONS

    %% ========================================
    %% RELATIONSHIPS (AUTH, USER, ADMIN scope)
    %% ========================================

    %% User relationships
    USERS ||--o{ USER_ADDRESSES : "has"
    USERS ||--o{ USER_FAVORITES : "has"
    USERS ||--o| USER_SETTINGS : "has"
    
    %% Future relationships (placeholder)
    USERS ||--o| SHOPS : "owns (OWNER)"
    USERS ||--o| CARTS : "has (CUSTOMER)"
    USERS ||--o{ ORDERS : "places (CUSTOMER)"
    USERS ||--o{ ORDERS : "delivers (SHIPPER)"
    USERS ||--o| WALLETS : "has (OWNER/SHIPPER)"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ SHIPPER_APPLICATIONS : "applies (SHIPPER)"

    %% Category relationships (Admin-managed)
    CATEGORIES ||--o{ PRODUCTS : "contains"

    %% Favorites relationships
    USER_FAVORITES }o--|| PRODUCTS : "favorites"
```

---

## 📊 Collection Details (AUTH, USER, ADMIN)

### USERS Collection

```typescript
interface User {
  id: string;              // = Firebase Auth UID
  email: string;
  displayName: string;
  phone?: string;
  avatarUrl?: string;
  role: 'CUSTOMER' | 'OWNER' | 'SHIPPER' | 'ADMIN';
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'DELETED';
  fcmTokens: string[];     // Max 5 tokens
  shopId?: string;         // If role = OWNER
  shipperInfo?: {          // If role = SHIPPER
    shopId: string;
    status: 'PENDING_APPROVAL' | 'ACTIVE' | 'RESIGNED';
    isOnline: boolean;
    currentOrders: string[];
    maxConcurrentOrders: number;
    totalDeliveries: number;
    rating: number;
    joinedShopAt: Timestamp;
  };
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### USER_ADDRESSES (Embedded or Subcollection)

```typescript
interface Address {
  id: string;
  label: string;           // "Phòng mình", "Tòa B"
  fullAddress: string;
  building?: string;
  room?: string;
  note?: string;
  isDefault: boolean;
  createdAt: Timestamp;
}
```

### USER_FAVORITES Collection

```typescript
// Document ID = {userId}_{productId}
interface UserFavorite {
  id: string;
  userId: string;
  productId: string;
  // Denormalized data for fast display
  productName: string;
  productPrice: number;
  productImage?: string;
  shopId: string;
  shopName: string;
  createdAt: Timestamp;
}
```

### CATEGORIES Collection (Admin-managed)

```typescript
interface Category {
  id: string;
  name: string;            // "Đồ ăn"
  nameEn?: string;         // "Food"
  slug: string;            // "do-an"
  icon?: string;           // Material icon name
  description?: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

---

## 🔑 Indexes (AUTH, USER, ADMIN)

```javascript
// users
users: email ASC (unique)
users: role, status

// userFavorites
userFavorites: userId, createdAt DESC

// categories
categories: isActive, sortOrder ASC
categories: slug ASC (unique)
```

---

## Legend

| Status | Meaning                |
| ------ | ---------------------- |
| ✅     | Đã implement           |
| 🚧     | Đang implement         |
| 🔲     | Chưa implement         |

---

## Implementation Progress

| Collection           | Backend | Owner  |
| -------------------- | ------- | ------ |
| USERS                | ✅ Done | Hòa    |
| USER_ADDRESSES       | 🔲      | Hiệp   |
| USER_FAVORITES       | 🔲      | Hiệp   |
| USER_SETTINGS        | 🔲      | Hiệp   |
| CATEGORIES           | 🔲      | Hòa    |
| SHOPS                | 🔲      | Ninh   |
| PRODUCTS             | 🔲      | Ninh   |
| CARTS                | 🔲      | Hiệp   |
| ORDERS               | 🔲      | Hòa    |
| PAYMENTS             | 🔲      | Hòa    |
| VOUCHERS             | 🔲      | Hòa    |
| WALLETS              | 🔲      | Hòa    |
| NOTIFICATIONS        | 🔲      | Hiệp   |
| SUBSCRIPTIONS        | 🔲      | Hòa    |
| SHIPPER_APPLICATIONS | 🔲      | Ninh   |

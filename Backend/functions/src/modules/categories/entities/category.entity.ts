import { Timestamp } from 'firebase-admin/firestore';

/**
 * Category Status - Trạng thái của category
 */
export enum CategoryStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
}

/**
 * Category Entity - Entity cho bảng categories
 *
 * Dựa theo docs: docs-god/architecture/02_DATA_MODEL.md
 *
 * Collection: categories
 * Indexes:
 * - status, displayOrder (ASC)
 *
 * Sử dụng trong:
 * - Products (categoryId reference)
 * - Admin Panel (CRUD categories)
 * - Public API (list active categories)
 */
export interface CategoryEntity {
  /** Document ID (auto-generated) */
  id?: string;

  /** Tên category */
  name: string;

  /** Slug dùng cho URL-friendly identifier */
  slug: string;

  /** Mô tả category */
  description?: string;

  /** URL ảnh đại diện */
  imageUrl?: string;

  /** Icon name (material icons hoặc custom) */
  icon?: string;

  /** Thứ tự hiển thị (số nhỏ hiển thị trước) */
  displayOrder: number;

  /** Trạng thái */
  status: CategoryStatus;

  /** Số lượng products trong category (denormalized) */
  productCount?: number;

  /** Thời gian tạo */
  createdAt: Timestamp;

  /** Thời gian cập nhật */
  updatedAt: Timestamp;
}

/**
 * CreateCategoryData - Data để tạo category mới
 * Không bao gồm id, createdAt, updatedAt (tự động generate)
 */
export type CreateCategoryData = Omit<CategoryEntity, 'id' | 'createdAt' | 'updatedAt'>;

/**
 * UpdateCategoryData - Data để update category
 * Tất cả fields đều optional
 */
export type UpdateCategoryData = Partial<Omit<CategoryEntity, 'id' | 'createdAt' | 'updatedAt'>>;

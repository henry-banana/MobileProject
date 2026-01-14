import { Injectable, Inject, NotFoundException } from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import {
  CategoryEntity,
  CategoryStatus,
  CreateCategoryData,
  UpdateCategoryData,
} from '../entities/category.entity';

/**
 * Collection name trong Firestore
 */
const COLLECTION_NAME = 'categories';

/**
 * CategoriesRepository - Repository pattern cho categories
 *
 * Responsibilities:
 * - CRUD operations cho categories collection
 * - Trừu tượng hóa Firestore operations
 * - Không chứa business logic
 *
 * Tuân theo:
 * - Repository Pattern
 * - Single Responsibility Principle
 */
@Injectable()
export class CategoriesRepository {
  private readonly collection;

  constructor(@Inject('FIRESTORE') private readonly firestore: Firestore) {
    this.collection = this.firestore.collection(COLLECTION_NAME);
  }

  /**
   * Tạo category mới
   */
  async create(data: CreateCategoryData): Promise<CategoryEntity> {
    const now = Timestamp.now();

    const docData = {
      ...data,
      productCount: data.productCount ?? 0,
      createdAt: now,
      updatedAt: now,
    };

    const docRef = await this.collection.add(docData);

    return {
      id: docRef.id,
      ...docData,
    };
  }

  /**
   * Lấy category theo ID
   */
  async findById(id: string): Promise<CategoryEntity | null> {
    const doc = await this.collection.doc(id).get();

    if (!doc.exists) {
      return null;
    }

    return {
      id: doc.id,
      ...doc.data(),
    } as CategoryEntity;
  }

  /**
   * Lấy category theo ID (throw error nếu không tìm thấy)
   */
  async findByIdOrThrow(id: string): Promise<CategoryEntity> {
    const category = await this.findById(id);

    if (!category) {
      throw new NotFoundException(`Category with ID "${id}" not found`);
    }

    return category;
  }

  /**
   * Lấy category theo slug
   */
  async findBySlug(slug: string): Promise<CategoryEntity | null> {
    const snapshot = await this.collection.where('slug', '==', slug).get();

    if (snapshot.empty) {
      return null;
    }

    const doc = snapshot.docs[0];
    return {
      id: doc.id,
      ...doc.data(),
    } as CategoryEntity;
  }

  /**
   * Lấy tất cả categories (cho Admin)
   */
  async findAll(): Promise<CategoryEntity[]> {
    const snapshot = await this.collection.orderBy('displayOrder', 'asc').get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as CategoryEntity[];
  }

  /**
   * Lấy categories active (cho Public API)
   */
  async findActive(): Promise<CategoryEntity[]> {
    const snapshot = await this.collection
      .where('status', '==', CategoryStatus.ACTIVE)
      .orderBy('displayOrder', 'asc')
      .get();

    return snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    })) as CategoryEntity[];
  }

  /**
   * Update category
   */
  async update(id: string, data: UpdateCategoryData): Promise<CategoryEntity> {
    const docRef = this.collection.doc(id);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new NotFoundException(`Category with ID "${id}" not found`);
    }

    const updateData = {
      ...data,
      updatedAt: Timestamp.now(),
    };

    await docRef.update(updateData);

    return {
      id: doc.id,
      ...doc.data(),
      ...updateData,
    } as CategoryEntity;
  }

  /**
   * Xóa category
   */
  async delete(id: string): Promise<void> {
    const docRef = this.collection.doc(id);
    const doc = await docRef.get();

    if (!doc.exists) {
      throw new NotFoundException(`Category with ID "${id}" not found`);
    }

    await docRef.delete();
  }

  /**
   * Kiểm tra slug đã tồn tại chưa (trừ category hiện tại)
   */
  async isSlugExist(slug: string, excludeId?: string): Promise<boolean> {
    const snapshot = await this.collection.where('slug', '==', slug).get();

    if (snapshot.empty) {
      return false;
    }

    // Nếu có excludeId, kiểm tra xem doc tìm được có phải là doc đang update không
    if (excludeId) {
      return snapshot.docs.some((doc) => doc.id !== excludeId);
    }

    return true;
  }

  /**
   * Lấy displayOrder lớn nhất hiện tại
   */
  async getMaxDisplayOrder(): Promise<number> {
    const snapshot = await this.collection.orderBy('displayOrder', 'desc').limit(1).get();

    if (snapshot.empty) {
      return 0;
    }

    const data = snapshot.docs[0].data();
    return data.displayOrder ?? 0;
  }

  /**
   * Tăng productCount của category
   */
  async incrementProductCount(id: string, delta: number = 1): Promise<void> {
    const docRef = this.collection.doc(id);
    const { FieldValue } = await import('firebase-admin/firestore');

    await docRef.update({
      productCount: FieldValue.increment(delta),
      updatedAt: Timestamp.now(),
    });
  }
}
